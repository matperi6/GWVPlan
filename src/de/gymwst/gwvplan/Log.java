package de.gymwst.gwvplan;

public class Log {

	public static boolean logEnabled = false;
	
	public static void d(String tag, String msg) {
		if (logEnabled) {
			android.util.Log.d(tag, msg);
		}
	}

	public static void i(String tag, String msg) {
		if (logEnabled) {
			android.util.Log.i(tag, msg);
		}
	}

	public static void e(String tag, String msg, Throwable tr) {
		if (logEnabled) {
			android.util.Log.e(tag, msg, tr);			
		}
	}

	public static void e(String tag, String msg) {
		if (logEnabled) {
			android.util.Log.e(tag, msg);
		}
	}

}
