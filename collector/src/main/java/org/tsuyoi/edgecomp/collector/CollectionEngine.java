package org.tsuyoi.edgecomp.collector;

import com.google.gson.Gson;
import org.tsuyoi.edgecomp.common.PluginStatics;
import org.tsuyoi.edgecomp.models.SwipeRecord;
import io.cresco.library.data.TopicType;
import io.cresco.library.plugin.PluginBuilder;
import io.cresco.library.utilities.CLogger;
import org.tsuyoi.edgecomp.lookup.LookupClient;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.List;

public class CollectionEngine {
    final private PluginBuilder pluginBuilder;
    final private CLogger logger;
    final private LookupClient lookupClient;

    private String siteId;
    private String listenerId = null;

    private List<SwipeRecord> records = new ArrayList<>();
    private List<String> ids = new ArrayList<>();

    public CollectionEngine(PluginBuilder pluginBuilder) {
        this.pluginBuilder = pluginBuilder;
        this.logger = pluginBuilder.getLogger(CollectionEngine.class.getName(), CLogger.Level.Trace);
        this.lookupClient = new LookupClient(
                pluginBuilder.getConfig().getStringParam("lookup_url", "localhost"),
                pluginBuilder.getConfig().getIntegerParam("lookup_port", 8500),
                pluginBuilder.getConfig().getStringParam("lookup_path", "lookup"),
                pluginBuilder.getConfig().getStringParam("lookup_param", "id"),
                pluginBuilder.getConfig().getStringParam("lookup_username", "user"),
                pluginBuilder.getConfig().getStringParam("lookup_password", "password")
        );
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
                                    swipe.getTsAsDate());
                            swipe.addLookupResult(lookupClient.lookupUserInfo(swipe.getUserId()));
                            // Todo: Save the swipe
                            TextMessage updateMsg = pluginBuilder.getAgentService().getDataPlaneService()
                                    .createTextMessage();
                            updateMsg.setText(gson.toJson(swipe));
                            updateMsg.setStringProperty(PluginStatics.SWIPE_RESULT_DATA_PLANE_IDENTIFIER_KEY,
                                    PluginStatics.getSiteSwipeResultDataPlaneValue(swipe.getSite()));
                            pluginBuilder.getAgentService().getDataPlaneService().sendMessage(TopicType.AGENT, updateMsg);
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
