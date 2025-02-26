

import org.apache.commons.io.input.Tailer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.File;
import java.util.Optional;

public class ExecTemplateJDK8ADR {

    static {
        Logger logger = LogManager.getRootLogger();
        Configurator.setAllLevels(logger.getName(), Level.getLevel("DEBUG"));
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        Optional<Appender> appender = config.getAppenders().values().stream().filter(a -> a instanceof FileAppender).findFirst();

        if (appender.isPresent()) {
            String logFilePath = ((FileAppender)appender.get()).getFileName();
            logger.info("Log file location: {}", logFilePath);
            tailer(logFilePath);
        } else {
            logger.warn("File appender not found.");
        }
    }

    private static void tailer(String logFilePath) {
        TailerListener listener = new TailerListener();
		Tailer tailer = Tailer.create(new File(logFilePath), listener, 3000l);
        new Thread(tailer).start();
    }


}
