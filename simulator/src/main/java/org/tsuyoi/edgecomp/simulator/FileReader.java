package org.tsuyoi.edgecomp.simulator;

import com.google.gson.Gson;
import io.cresco.library.data.TopicType;
import io.cresco.library.plugin.PluginBuilder;
import io.cresco.library.utilities.CLogger;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.tsuyoi.edgecomp.common.PluginStatics;
import org.tsuyoi.edgecomp.models.SwipeRecord;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Timer;
import java.util.TimerTask;

public class FileReader {
    private final PluginBuilder pluginBuilder;
    private final CLogger logger;

    private Path fileToWatch;
    private String siteId;
    private int checkDelay;
    private int checkFrequency;
    private Timer fileReaderTimer;

    public FileReader(PluginBuilder pluginBuilder) {
        this.pluginBuilder = pluginBuilder;
        this.logger = pluginBuilder.getLogger(CardReader.class.getName(), CLogger.Level.Trace);
        setFileToWatch(Paths.get(pluginBuilder.getConfig().getStringParam("file_to_watch")));
        setSiteId(pluginBuilder.getConfig().getStringParam("site_id", pluginBuilder.getAgent()));
        setCheckDelay(pluginBuilder.getConfig().getIntegerParam("check_delay", 1000));
        setCheckFrequency(pluginBuilder.getConfig().getIntegerParam("check_frequency", 500));
    }

    public void start() {
        if (!Files.exists(getFileToWatch())) {
            logger.error("Failed to start, file to watch does not exist.");
            return;
        }
        if (fileReaderTimer == null) {
            fileReaderTimer = new Timer();
            fileReaderTimer.scheduleAtFixedRate(new FileReaderTask(getFileToWatch(), getPluginBuilder()), getCheckDelay(), getCheckFrequency());
            logger.info("File reader started");
        } else {
            logger.error("File reader is already active");
        }
    }

    public void stop() {
        if (fileReaderTimer != null) {
            logger.info("Stopping file reader");
            fileReaderTimer.cancel();
            fileReaderTimer = null;
        } else {
            logger.error("File reader is not running");
        }
    }

    public PluginBuilder getPluginBuilder() {
        return pluginBuilder;
    }

    public Path getFileToWatch() {
        return fileToWatch;
    }
    public void setFileToWatch(Path fileToWatch) {
        this.fileToWatch = fileToWatch;
    }

    public String getSiteId() {
        return siteId;
    }
    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public int getCheckDelay() {
        return checkDelay;
    }
    public void setCheckDelay(int checkDelay) {
        this.checkDelay = checkDelay;
    }

    public int getCheckFrequency() {
        return checkFrequency;
    }
    public void setCheckFrequency(int checkFrequency) {
        this.checkFrequency = checkFrequency;
    }

    class FileReaderTask extends TimerTask {
        private final Path fileToWatch;
        private final PluginBuilder pluginBuilder;

        FileReaderTask(Path fileToWatch, PluginBuilder pluginBuilder) {
            this.fileToWatch = fileToWatch;
            this.pluginBuilder = pluginBuilder;
        }

        @Override
        public void run() {
            Gson gson = new Gson();
            logger.info("Checking: " + fileToWatch);
            try {
                String contents = new String(Files.readAllBytes(fileToWatch)).trim();
                if (contents.length() > 0) {
                    SwipeRecord record = new SwipeRecord(getSiteId(), contents, contents,
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
                    new FileOutputStream(fileToWatch.toFile()).close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
