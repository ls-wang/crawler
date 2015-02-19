package crawler;
import java.util.Enumeration;
import java.util.ResourceBundle;

public class Constant {
	
	public String getPara(String paraName){

		ResourceBundle rb = ResourceBundle.getBundle("cfg.config");
		Enumeration <String> keys = rb.getKeys();
		String key = null;
		String value = null;
		while (keys.hasMoreElements()) {
			key = keys.nextElement();
			value = rb.getString(key);
			if(paraName.equalsIgnoreCase(key))
				break;
		}
		return value;
	}
}