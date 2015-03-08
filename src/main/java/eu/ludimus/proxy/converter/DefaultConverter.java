package eu.ludimus.proxy.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

public class DefaultConverter implements Converter {
    public static final String MATCH = "match";
    public static final String TO = "to";
    public static final int NOT_FOUND = -1;
    Logger logger = LoggerFactory.getLogger(getClass());
    public static final String IN = "--> ";
    public static final String OUT = "<-- ";
    public static final String CONVERTED_IN = "@ --> ";
    public static final String CONVERTED_OUT = "<-- @ ";
    private Properties converters = new Properties();
    private File file;

    public DefaultConverter(String convertScriptFile) {
        if(convertScriptFile != null) {
            file = new File(convertScriptFile);
            if(file.exists()) {
                new Timer(true).schedule(getPoller(), 100, 1000);
            } else {
                logger.warn(String.format("converterScriptFile %s does not exist!", convertScriptFile));
            }
        }
    }


    @Override
    public byte[] convertBeforeForward(byte[] bytes) {
        String value = new String(bytes, Charset.defaultCharset());
        logger.debug(IN + value);
        value = convert(value, "convertBeforeForward.\\d\\d*.match", CONVERTED_IN);
        return value.getBytes(Charset.defaultCharset());
    }

    @Override
    public byte[] convertBeforeReturn(byte[] bytes) {
        String value = new String(bytes, Charset.defaultCharset());
        logger.debug(OUT + value);
        value = convert(value, "convertBeforeReturn.\\d\\d*.match", CONVERTED_OUT);
        return value.getBytes(Charset.defaultCharset());
    }

    private String convert(String value, String keyPattern, String logPrefix) {
        for(Object o :converters.keySet()) { //iterate through all keys
            String key = o.toString();
            if (key.matches(keyPattern)) {
                String toValueKey = key.replace(MATCH, TO);
                if (converters.containsKey(toValueKey)) { //set is found in converterScript file
                    String matchPattern = converters.getProperty(key);
                    if (value.matches(matchPattern)) {
                        value = converters.getProperty(toValueKey);
                        logger.debug(logPrefix + value);
                        return value;
                    }
                }
            }
        }
        return value;
    }

    private TimerTask getPoller() {
        return new TimerTask() {
            private long propertyFileLastModified = 0;
            @Override
            public void run() {
                if(file.lastModified() > propertyFileLastModified) {
                    propertyFileLastModified = file.lastModified();
                    try {
                        converters.clear();
                        logger.info("property file has been read.");
                        converters.load(new FileReader(file));
                    } catch (IOException e) {
                        logger.error("No converter used", e);
                    }
                }
            }
        };
    }
}
