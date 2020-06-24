package org.tsuyoi.edgecomp.collector;

import io.cresco.library.messaging.MsgEvent;
import io.cresco.library.plugin.Executor;
import io.cresco.library.plugin.PluginBuilder;
import io.cresco.library.utilities.CLogger;

public class ExecutorImpl implements Executor {
    private PluginBuilder plugin;
    private CollectionEngine collectionEngine;
    private CLogger logger;

    public ExecutorImpl(PluginBuilder pluginBuilder, CollectionEngine collectionEngine) {
        this.plugin = pluginBuilder;
        this.collectionEngine = collectionEngine;
        logger = plugin.getLogger(ExecutorImpl.class.getName(), CLogger.Level.Trace);
    }

    @Override
    public MsgEvent executeCONFIG(MsgEvent incoming) {
        logger.trace("Received CONFIG message");
        return null;
    }
    @Override
    public MsgEvent executeDISCOVER(MsgEvent incoming) {
        logger.trace("Received DISCOVER message");
        return null;
    }
    @Override
    public MsgEvent executeERROR(MsgEvent incoming) {
        logger.trace("Received ERROR message");
        return null;
    }
    @Override
    public MsgEvent executeINFO(MsgEvent incoming) {
        logger.trace("Received INFO message");
        return null;
    }
    @Override
    public MsgEvent executeEXEC(MsgEvent incoming) {
        logger.trace("Received EXEC message");
        return null;
    }
    @Override
    public MsgEvent executeWATCHDOG(MsgEvent incoming) {
        logger.trace("Received WATCHDOG message");
        return null;
    }
    @Override
    public MsgEvent executeKPI(MsgEvent incoming) {
        logger.trace("Received KPI message");
        return null;
    }
}

