package no.lau;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonFunctions {

    private static Logger logger = LoggerFactory.getLogger(CommonFunctions.class);

    public static String fetchEnvsOrEx(String... keys) {
        for (String key : keys) {
            String returnedResult =  System.getenv(key);
            if(returnedResult != null && !returnedResult.isEmpty()) {
                return returnedResult;
            }
        }
        String kz = String.join(",", keys);
        logger.error("Could not find System env for: {}", kz);
        throw new RuntimeException("No environment keys for " + String.join(",", kz));
    }

    public static String envOrDefault(String envName, String defaultPath) {
        String envPath = System.getenv(envName);
        if (envPath == null || envPath.isEmpty())
            return defaultPath;
        else
            return envPath;
    }
}
