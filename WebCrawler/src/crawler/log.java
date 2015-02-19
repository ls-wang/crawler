package crawler;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.LoggingMXBean;


public class log {
	private static Logger logger = null;
	 
	public static Logger getLogger(){
	    if (null == logger) {
	        InputStream is  = LoggingMXBean.class.getClass().getResourceAsStream("/cfg/logger.properties");
	        try {
	            LogManager.getLogManager().readConfiguration(is);
	        } catch (Exception e) {
	            logging.warning("input properties file is error.\n" + e.toString());
	        }finally{
	            try {
	                is.close();
	            } catch (IOException e) {
	                logging.warning("close FileInputStream a case.\n" + e.toString());
	            }
	        }
	         
	        logger = Logger.getLogger("LOGGER");
	    }
	    return logger;
	}
	 
	private static Logger logging = Logger.getLogger(log.class.getName());
}
