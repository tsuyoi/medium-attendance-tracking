package org.tsuyoi.edgecomp.simulator;

import com.google.gson.Gson;
import org.tsuyoi.edgecomp.common.PluginStatics;
import org.tsuyoi.edgecomp.models.SwipeRecord;
import io.cresco.library.data.TopicType;
import io.cresco.library.plugin.PluginBuilder;
import io.cresco.library.utilities.CLogger;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.*;

public class CardReader {
    private final PluginBuilder pluginBuilder;
    private final CLogger logger;

    private String siteId;
    private Timer cardReaderTimer;

    public CardReader(PluginBuilder pluginBuilder) {
        this.pluginBuilder = pluginBuilder;
        this.logger = pluginBuilder.getLogger(CardReader.class.getName(), CLogger.Level.Trace);
        loadConfig();
    }

    public void loadConfig() {
        setSiteId(pluginBuilder.getConfig().getStringParam("site_id", pluginBuilder.getAgent()));
    }

    public void start() {
        if (cardReaderTimer == null) {
            cardReaderTimer = new Timer();
            cardReaderTimer.scheduleAtFixedRate(new CardReaderTask(), 5000, 5000);
            logger.info("Simulator started");
        } else {
            logger.error("Card reader is already active");
        }
    }

    public void stop() {
        if (cardReaderTimer != null) {
            cardReaderTimer.cancel();
            cardReaderTimer = null;
        } else {
            logger.error("Card reader is not running");
        }
    }

    public String getSiteId() {
        return siteId;
    }
    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    private class CardReaderTask extends TimerTask {
        private final Random random = new Random();
        private final CLogger logger;

        public CardReaderTask() {
            this.logger = pluginBuilder.getLogger(CardReaderTask.class.getName(), CLogger.Level.Trace);
        }

        @Override
        public void run() {
            Gson gson = new Gson();
            String data;
            try {
                int type = random.nextInt(10);
                if (type < 7) {
                    data = generateCompleteSwipe();
                } else if (type < 9) {
                    data = generatePartialSwipe();
                } else {
                    data = generateSwipeError();
                }
                int idStart = data.indexOf("%") + 1;
                int idEnd = data.indexOf("?");
                String id = null;
                if ((idEnd - idStart) == 9)
                    id = data.substring(idStart, (idEnd - idStart + 1));
                SwipeRecord record = new SwipeRecord(getSiteId(), data, id,
                        pluginBuilder.getRegion(), pluginBuilder.getAgent(), pluginBuilder.getPluginID());
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
                } catch (JMSException e) {
                    logger.error("Failed to generate swipe message: {}, code: {}",
                            e.getMessage(), e.getErrorCode());
                    logger.trace("JMSException:\n" + ExceptionUtils.getStackTrace(e));
                }
            } catch (Exception e) {
                logger.error("Exception: {}", e.getMessage());
                logger.trace("Exception:\n" + ExceptionUtils.getStackTrace(e));
            }
        }

        private String generateCompleteSwipe() {
            return "%"
                    + generateID()
                    + "?;6085719100260423=491212000000?";
        }

        private String generatePartialSwipe() {
            return "%"
                    + generateID()
                    + "?;E?";
        }

        private String generateSwipeError() {
            switch (random.nextInt(3)) {
                case 0:
                    return ";E?";
                case 1:
                    return ";E?+E?";
                default:
                    return "%E?;E?";
            }
        }

        private String generateID() {
            return "9"
                    + ((random.nextInt(10) > 0) ? "1" : "0")
                    + "00"
                    + StringUtils.leftPad(
                    Integer.toString(random.nextInt(100000)), 5, '0');
        }
    }
}
