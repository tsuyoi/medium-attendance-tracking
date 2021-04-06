package org.tsuyoi.edgecomp.collector;

import com.google.gson.Gson;
import org.tsuyoi.edgecomp.common.PluginStatics;
import org.tsuyoi.edgecomp.models.SwipeRecord;
import io.cresco.library.data.TopicType;
import io.cresco.library.plugin.PluginBuilder;
import io.cresco.library.utilities.CLogger;
import org.tsuyoi.edgecomp.lookup.LookupClient;
import org.tsuyoi.edgecomp.services.SwipeRecordService;
import org.tsuyoi.edgecomp.utilities.RollingFileAppender;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.io.IOException;
import java.nio.file.Paths;

public class CollectionEngine {
    final private PluginBuilder pluginBuilder;
    final private CLogger logger;
    private boolean useLookupClient;
    private LookupClient lookupClient;
    private RollingFileAppender backupFileAppender;

    private String listenerId = null;

    public CollectionEngine(PluginBuilder pluginBuilder) {
        this.pluginBuilder = pluginBuilder;
        this.logger = pluginBuilder.getLogger(CollectionEngine.class.getName(), CLogger.Level.Trace);
        loadConfig();
    }

    public void loadConfig() {
        buildLookupClient();
        buildBackupFileAppender();
    }

    private void buildLookupClient() {
        this.useLookupClient = pluginBuilder.getConfig().getBooleanParam("lookup_swipes", true);
        this.lookupClient = new LookupClient(
                pluginBuilder.getConfig().getStringParam("lookup_url", "localhost"),
                pluginBuilder.getConfig().getIntegerParam("lookup_port", 8500),
                pluginBuilder.getConfig().getStringParam("lookup_path", "lookup"),
                pluginBuilder.getConfig().getStringParam("lookup_param", "id"),
                pluginBuilder.getConfig().getStringParam("lookup_username", "user"),
                pluginBuilder.getConfig().getStringParam("lookup_password", "password")
        );
    }

    private void buildBackupFileAppender() {
        try {
            backupFileAppender = new RollingFileAppender(Paths.get(pluginBuilder.getConfig().getStringParam("swipe_logs", "swipe_logs")));
        } catch (IllegalArgumentException e) {
            logger.error("Error creating swipe backup log utility: {}", e.getMessage());
        } catch (IOException e) {
            logger.error("Error creating swipe backup log utility: {}", e.getMessage());
            e.printStackTrace();
        }
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
                            if (useLookupClient)
                                swipe.addLookupResult(lookupClient.lookupUserInfo(swipe.getUserId()));
                            backupSwipe(swipe);
                            SwipeRecordService.create(swipe);
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

    private void backupSwipe(SwipeRecord swipeRecord) {
        if (backupFileAppender != null && swipeRecord != null)
            try {
                backupFileAppender.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s",
                        swipeRecord.getTsAsDate(),
                        swipeRecord.getSite(),
                        swipeRecord.getId(),
                        (swipeRecord.getUserId() != null) ? swipeRecord.getUserId() : "",
                        (swipeRecord.getUserEmail() != null) ? swipeRecord.getUserEmail() : "",
                        (swipeRecord.getUserFirstName() != null) ? swipeRecord.getUserFirstName() : "",
                        (swipeRecord.getUserLastName() != null) ? swipeRecord.getUserLastName() : "",
                        (swipeRecord.getError() != null) ? swipeRecord.getError() : ""));
            } catch (IOException e) {
                logger.error("Failed to log swipe ({},{},{},{},{},{},{},{})",
                        swipeRecord.getTsAsDate(),
                        swipeRecord.getSite(),
                        swipeRecord.getId(),
                        (swipeRecord.getUserId() != null) ? swipeRecord.getUserId() : "",
                        (swipeRecord.getUserEmail() != null) ? swipeRecord.getUserEmail() : "",
                        (swipeRecord.getUserFirstName() != null) ? swipeRecord.getUserFirstName() : "",
                        (swipeRecord.getUserLastName() != null) ? swipeRecord.getUserLastName() : "",
                        (swipeRecord.getError() != null) ? swipeRecord.getError() : "");
            }
    }
}
