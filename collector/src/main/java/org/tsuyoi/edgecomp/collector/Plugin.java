package org.tsuyoi.edgecomp.collector;

import io.cresco.library.agent.AgentService;
import io.cresco.library.messaging.MsgEvent;
import io.cresco.library.plugin.PluginBuilder;
import io.cresco.library.plugin.PluginService;
import io.cresco.library.utilities.CLogger;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.*;
import org.tsuyoi.edgecomp.services.SwipeRecordService;
import org.tsuyoi.edgecomp.utilities.SessionFactoryManager;

import java.util.Map;

@SuppressWarnings("unused")
@Component(
        service = { PluginService.class },
        scope= ServiceScope.PROTOTYPE,
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        servicefactory = true,
        reference=@Reference(name="io.cresco.library.agent.AgentService", service= AgentService.class)
)
public class Plugin implements PluginService {
    public BundleContext context;
    private PluginBuilder pluginBuilder;
    private CLogger logger;
    private Map<String,Object> map;
    private CollectionEngine collectionEngine;

    @Activate
    void activate(BundleContext context, Map<String, Object> map) {
        this.context = context;
        this.map = map;
    }

    @Modified
    void modified(BundleContext context, Map<String, Object> map) {
        if (logger != null)
            logger.info("Modified Config Map PluginID:" + map.get("pluginID"));
        else
            System.out.println("Modified Config Map PluginID:" + map.get("pluginID"));
        SessionFactoryManager.close();
        collectionEngine.loadConfig();
    }

    @Deactivate
    void deactivate(BundleContext context, Map<String,Object> map) {
        this.context = null;
        this.map = null;
    }

    @Override
    public boolean isActive() {
        return pluginBuilder.isActive();
    }

    @Override
    public void setIsActive(boolean isActive) {
        pluginBuilder.setIsActive(isActive);
    }

    @Override
    public boolean inMsg(MsgEvent incoming) {
        pluginBuilder.msgIn(incoming);
        return true;
    }

    @Override
    public boolean isStarted() {
        try {
            if(pluginBuilder == null) {
                pluginBuilder = new PluginBuilder(this.getClass().getName(), context, map);
                this.logger = pluginBuilder.getLogger(Plugin.class.getName(), CLogger.Level.Trace);
                this.collectionEngine = new CollectionEngine(pluginBuilder);
                pluginBuilder.setExecutor(new ExecutorImpl(pluginBuilder, collectionEngine));

                while (!pluginBuilder.getAgentService().getAgentState().isActive()) {
                    logger.info("Plugin " + pluginBuilder.getPluginID() + " waiting on Agent Init");
                    Thread.sleep(1000);
                }
                pluginBuilder.setIsActive(true);

                // Propogate pluginBuilder for logging/messaging purposes
                SessionFactoryManager.setPluginBuilder(pluginBuilder);
                SwipeRecordService.setPluginBuilder(pluginBuilder);
                // Start collection engine
                collectionEngine.start();
            }
            return true;
        } catch(Exception ex) {
            if (logger != null)
                logger.error("isStarted() Exception: {}", ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean isStopped() {
        if (collectionEngine != null)
            collectionEngine.stop();
        if (pluginBuilder != null) {
            pluginBuilder.setExecutor(null);
            pluginBuilder.setIsActive(false);
        }
        return true;
    }
}
