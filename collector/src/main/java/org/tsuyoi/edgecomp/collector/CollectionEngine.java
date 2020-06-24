package org.tsuyoi.edgecomp.collector;

import com.google.gson.Gson;
import org.tsuyoi.edgecomp.common.PluginStatics;
import org.tsuyoi.edgecomp.common.SwipeRecord;
import io.cresco.library.data.TopicType;
import io.cresco.library.messaging.MsgEvent;
import io.cresco.library.plugin.PluginBuilder;
import io.cresco.library.utilities.CLogger;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused", "WeakerAccess"})
public class CollectionEngine {
    private PluginBuilder pluginBuilder;
    private CLogger logger;

    private String siteId;
    private String listenerId = null;

    private List<SwipeRecord> records = new ArrayList<>();
    private List<String> ids = new ArrayList<>();

    public CollectionEngine(PluginBuilder pluginBuilder) {
        this.pluginBuilder = pluginBuilder;
        this.logger = pluginBuilder.getLogger(CollectionEngine.class.getName(), CLogger.Level.Trace);
        setSiteId(pluginBuilder.getConfig().getStringParam("site_id", pluginBuilder.getAgent()));
    }

    public void start() {
        if (listenerId == null) {
            MessageListener ml = (Message msg) -> {
                try {
                    if (msg instanceof TextMessage) {
                        Gson gson = new Gson();
                        TextMessage textMessage = (TextMessage) msg;
                        SwipeRecord swipe = gson.fromJson(textMessage.getText(), SwipeRecord.class);
                        if (swipe.getId() != null) {
                            logger.info("Received: {} from {} ({}-{}-{}) at {}", swipe.getId(), swipe.getSite(),
                                    swipe.getCrescoRegion(), swipe.getCrescoAgent(), swipe.getCrescoPlugin(),
                                    swipe.getDateAsDate());
                            MsgEvent ack = pluginBuilder.getGlobalPluginMsgEvent(MsgEvent.Type.INFO,
                                    swipe.getCrescoRegion(), swipe.getCrescoAgent(), swipe.getCrescoPlugin());
                            ack.setParam("success", Boolean.toString(true));
                            ack.setParam("name", "Some Name");
                            ack.setParam("id", swipe.getSwipe());
                            ack.setParam("time", swipe.getDate());
                            pluginBuilder.msgOut(ack);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            };
            listenerId = pluginBuilder.getAgentService().getDataPlaneService().addMessageListener(
                    TopicType.AGENT,
                    ml,
                    String.format(
                            "%s='%s'",
                            PluginStatics.SWIPE_RECORD_DATA_PLANE_IDENTIFIER_KEY,
                            PluginStatics.SWIPE_RECORD_DATA_PLANE_IDENTIFIER_VALUE
                    )
            );
        }
    }

    public void stop() {
        if (listenerId != null) {
            pluginBuilder.getAgentService().getDataPlaneService().removeMessageListener(listenerId);
        }
    }

    public String getSiteId() {
        return siteId;
    }
    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public List<SwipeRecord> getRecords() {
        return records;
    }
    public void setRecords(List<SwipeRecord> records) {
        this.records = records;
    }

    public List<String> getIds() {
        return ids;
    }
    public void setIds(List<String> ids) {
        this.ids = ids;
    }
}
