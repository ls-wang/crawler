package crawler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class CommonFunc {
	
	public static String[] splite_String(String sLine, String sDelimiter) {
//		String[] sArray;
//		//拆開字串
//		for(String Paragraph: sLine.split(sDelimiter)){
//			sArray..add(Paragraph);
//		}
		return sLine.split(sDelimiter);
	}
	public static String getToday(String theFormat) {
		SimpleDateFormat formatter = new SimpleDateFormat(theFormat,
				Locale.CHINESE);
		Date thedate = new Date();
		return formatter.format(thedate);
	}
	public static String getToday_yyyyMMddHHmmss() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss",
				Locale.CHINESE);
		Date thedate = new Date();
		return formatter.format(thedate);
	}	
	public static String getToday_yyyyMMddHHmmss_log() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss",
				Locale.CHINESE);
		Date thedate = new Date();
		return "log/" + formatter.format(thedate)+".log";
	}	
	public static int getRandom(int iSec) {
		Random random = new Random();
		return random.nextInt(iSec);
	}
}
