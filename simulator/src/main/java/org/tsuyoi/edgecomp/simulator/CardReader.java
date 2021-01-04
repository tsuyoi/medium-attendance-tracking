package org.tsuyoi.edgecomp.simulator;

import com.google.common.collect.EvictingQueue;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CardReader {
    private PluginBuilder pluginBuilder;
    private CLogger logger;

    private String siteId;
    private EvictingQueue<SwipeRecord> records = EvictingQueue.create(10000);
    private CardReaderWorker cardReaderWorker = null;

    public CardReader(PluginBuilder pluginBuilder) {
        this.pluginBuilder = pluginBuilder;
        this.logger = pluginBuilder.getLogger(CardReader.class.getName(), CLogger.Level.Trace);
        setSiteId(pluginBuilder.getConfig().getStringParam("site_id", pluginBuilder.getAgent()));
    }

    public void start() {
        if (cardReaderWorker == null) {
            cardReaderWorker = new CardReaderWorker();
            new Thread(cardReaderWorker).start();
            logger.info("Simulator started");
        } else {
            logger.error("Card reader is already active");
        }
    }

    public void stop() {
        if (cardReaderWorker != null) {
            cardReaderWorker.stop();
            cardReaderWorker = null;
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

    public List<SwipeRecord> getRecords() {
        return new ArrayList<>(records);
    }

    private class CardReaderWorker implements Runnable {
        private Random random = new Random();
        private CLogger logger;
        private boolean running = true;

        public CardReaderWorker() {
            this.logger = pluginBuilder.getLogger(CardReaderWorker.class.getName(), CLogger.Level.Trace);
        }

        public void stop() {
            this.running = false;
        }

        @Override
        public void run() {
            Gson gson = new Gson();
            String data = "";
            try {
                logger.info("Now reading...");
                while (running) {
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
                    records.add(record);
                    logger.info("New record: {}", record);
                    try {
                        /*TextMessage updateMsg = pluginBuilder.getAgentService().getDataPlaneService()
                                .createTextMessage();
                        updateMsg.setText(gson.toJson(record));
                        updateMsg.setStringProperty(PluginStatics.STATION_HEARTBEAT_DATA_PLANE_IDENTIFIER_KEY,
                                getSiteId());
                        pluginBuilder.getAgentService().getDataPlaneService().sendMessage(TopicType.AGENT, updateMsg);*/
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
                    int multiplier = random.nextInt(20) + 1;
                    Thread.sleep(1000 * multiplier);
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
