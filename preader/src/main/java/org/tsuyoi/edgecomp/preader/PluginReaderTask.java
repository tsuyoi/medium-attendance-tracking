package org.tsuyoi.edgecomp.preader;

import com.google.gson.Gson;
import io.cresco.library.data.TopicType;
import io.cresco.library.plugin.PluginBuilder;
import io.cresco.library.utilities.CLogger;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hid4java.HidDevice;
import org.tsuyoi.edgecomp.common.PluginStatics;
import org.tsuyoi.edgecomp.models.SwipeRecord;
import org.tsuyoi.edgecomp.reader.CardReaderTask;
import org.tsuyoi.edgecomp.utilities.RollingFileAppender;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class PluginReaderTask implements CardReaderTask {
    private final PluginBuilder pluginBuilder;
    private final CLogger logger;
    private String data;
    private RollingFileAppender backupFileAppender;

    private String siteId;

    public PluginReaderTask(PluginBuilder pluginBuilder) {
        this.pluginBuilder = pluginBuilder;
        this.logger = pluginBuilder.getLogger(PluginReaderTask.class.getName(), CLogger.Level.Info);
        setSiteId(pluginBuilder.getConfig().getStringParam("site_id", pluginBuilder.getAgent()));
        this.data = "";
        try {
            backupFileAppender = new RollingFileAppender(Paths.get(pluginBuilder.getConfig().getStringParam("swipe_logs", "swipe_logs")));
        } catch (IllegalArgumentException e) {
            logger.error("Error creating swipe backup log utility: {}", e.getMessage());
        } catch (IOException e) {
            logger.error("Error creating swipe backup log utility: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    public String getSiteId() {
        return siteId;
    }
    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    @Override
    public void startLoop() {
        logger.trace("Starting loop");
    }

    @Override
    public void startRead() {
        logger.trace("Starting read");
    }

    @Override
    public void readError(HidDevice device) {
        logger.error("Handling error");
        logger.error("Error: " + device.getLastErrorMessage());
    }

    @Override
    public void readPiece(byte[] piece) {
        Gson gson = new Gson();
        String character = Translator.translate(piece);
        if (character != null) {
            if (character.equals("\n")) {
                int stripeOneStart = data.indexOf("%") + 1;
                int stripeOneEnd = data.indexOf("?");
                int stripeTwoStart = data.indexOf(";") + 1;
                int stripeTwoEnd = data.indexOf("=");
                int stripeThreeStart = stripeTwoEnd + 1;
                int stripeThreeEnd = data.lastIndexOf("?");
                boolean hasStripeOne = stripeOneStart > 0;
                int stripeOneLength = stripeOneEnd - stripeOneStart;
                boolean hasStripeTwo = (stripeTwoEnd - stripeTwoStart) > 0;
                int stripeTwoLength = stripeTwoEnd - stripeTwoStart;
                boolean hasStripeThree = stripeThreeStart > 0;
                int stripeThreeLength = stripeThreeEnd - stripeThreeStart;
                String id = null;
                if (hasStripeOne && stripeOneLength == 9)
                    id = data.substring(stripeOneStart, stripeOneEnd);
                else if (hasStripeTwo && stripeTwoLength == 9)
                    id = data.substring(stripeTwoStart, stripeTwoEnd);
                else if (hasStripeThree && stripeThreeLength == 9)
                    id = data.substring(stripeThreeStart, stripeThreeEnd);
                SwipeRecord record = new SwipeRecord(getSiteId(), data, id,
                        pluginBuilder.getRegion(), pluginBuilder.getAgent(), pluginBuilder.getPluginID());
                backupSwipe(record);
                logger.info("New record: {}", record);
                try {
                    TextMessage updateMsg = pluginBuilder.getAgentService().getDataPlaneService()
                            .createTextMessage();
                    updateMsg.setText(gson.toJson(record));
                    updateMsg.setStringProperty(PluginStatics.SWIPE_RECORD_DATA_PLANE_IDENTIFIER_KEY,
                            PluginStatics.SWIPE_RECORD_DATA_PLANE_IDENTIFIER_VALUE);
                    pluginBuilder.getAgentService().getDataPlaneService().sendMessage(TopicType.AGENT, updateMsg);
                    updateMsg.setStringProperty(PluginStatics.SWIPE_RECORD_DATA_PLANE_IDENTIFIER_KEY,
                            PluginStatics.getSiteSwipeRecordDataPlaneValue(getSiteId()));
                    pluginBuilder.getAgentService().getDataPlaneService().sendMessage(TopicType.AGENT, updateMsg);
                } catch (JMSException e) {
                    logger.error("Failed to generate swipe message: {}, code: {}", e.getMessage(), e.getErrorCode());
                    logger.trace("JMSException:\n" + ExceptionUtils.getStackTrace(e));
                }
                data = "";
            } else {
                data += character;
            }
        }
    }

    private void backupSwipe(SwipeRecord swipeRecord) {
        if (backupFileAppender != null && swipeRecord != null)
            try {
                backupFileAppender.append(String.format("%s,%s,%s,%s",
                        swipeRecord.getTsAsDate(),
                        swipeRecord.getSite(),
                        swipeRecord.getSwipe(),
                        swipeRecord.getId()));
            } catch (IOException e) {
                logger.error("Failed to log swipe ({},{},{},{})",
                        swipeRecord.getTsAsDate(),
                        swipeRecord.getSite(),
                        swipeRecord.getSwipe(),
                        swipeRecord.getId());
            }
    }

    @Override
    public void readComplete() {
        logger.trace("Read complete");
    }

    public static class Translator {
        private static final Map<String, String> SpecialMap = new HashMap<String, String>() {{
            put( "8", "E");
            put("34", "%");
            put("46", "+");
            put("56", "?");
        }};
        private static final Map<String, String> CharacterMap = new HashMap<String, String>() {{
            put("30", "1");
            put("31", "2");
            put("32", "3");
            put("33", "4");
            put("34", "5");
            put("35", "6");
            put("36", "7");
            put("37", "8");
            put("38", "9");
            put("39", "0");
            put("46", "=");
            put("51", ";");
            put("88", "\n");
        }};

        public static String translate(byte[] toTranslate) {
            if (toTranslate == null || toTranslate.length < 3)
                return null;
            String specialFlag = Byte.toString(toTranslate[0]);
            String characterFlag = Byte.toString(toTranslate[2]);
            if (!characterFlag.equals("0")) {
                String character = null;
                if (specialFlag.equals("2")) {
                    if (SpecialMap.containsKey(characterFlag)) {
                        character = SpecialMap.get(characterFlag);
                    } else {
                        System.err.println("Failed to find special character: " + characterFlag);
                    }
                } else if (specialFlag.equals("0")) {
                    if (CharacterMap.containsKey(characterFlag)) {
                        character = CharacterMap.get(characterFlag);
                    } else {
                        System.err.println("Failed to find regular character: " + characterFlag);
                    }
                } else {
                    System.err.println("Found erroneous special flag: " + specialFlag);
                }
                return character;
            } else {
                System.out.println("characterFlag is '0'");
                return null;
            }
        }
    }
}
