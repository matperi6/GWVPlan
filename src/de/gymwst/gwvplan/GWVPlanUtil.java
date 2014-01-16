package de.gymwst.gwvplan;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.StatusLine;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;

import android.util.Log;
import de.gymwst.gwvplan.VPHandlerCharSequence.DATA;
import de.gymwst.util.htmlparser.SimpleHtmlParserCharSequence;

public class GWVPlanUtil {

	private final static String TAG = "GWVPlan.UTL";

	public static String formatWeek(int week) {
		String result = Integer.toString(week);
		if (result.length() == 1) {
			result = "0" + result;
		}
		return result;
	}


	private static String readStream(InputStream in, String encoding)
			throws IOException {
		byte[] buf = new byte[4096];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while (true) {
			int cnt = in.read(buf);
			if (cnt <= 0) {
				break;
			}
			baos.write(buf, 0, cnt);
		}
		return baos.toString(encoding);
	}

	public static InputStream httpGetStream(String urlReq, String userAgent)
			throws Exception {
		DefaultHttpClient client = new DefaultHttpClient();
		client.setRedirectHandler(new RedirectHandler() {
			@Override
			public boolean isRedirectRequested(HttpResponse response,
					HttpContext context) {
				return false;
			}

			@Override
			public URI getLocationURI(HttpResponse response, HttpContext context)
					throws ProtocolException {
				return null;
			}
		});
		HttpGet method = new HttpGet(new URI(urlReq));
		method.setHeader("user-agent", userAgent);
		HttpResponse res = client.execute(method);
		StatusLine status = res.getStatusLine();
		if (status.getStatusCode() != 200) {
			throw new RuntimeException("could not get url '" + urlReq + "': "
					+ status.getReasonPhrase());
		}

		Header[] header = res.getAllHeaders(); // Last-Modified: Fri, 05 Oct
												// 2012 12:23:20 GMT
		for (int i = 0; i < header.length; i++) {
			Log.i(TAG, header[i].getName() + "='" + header[i].getValue()
					+ "'\n");
		}

		InputStream is = res.getEntity().getContent();
		return is;
	}
	
	public static int currentDay() {
		GregorianCalendar calendar = (GregorianCalendar) Calendar.getInstance();
		int dow = calendar.get(Calendar.DAY_OF_WEEK);
		return dow;
	}
	
	public static int currentWeek() {
		GregorianCalendar calendar = (GregorianCalendar) Calendar.getInstance();
		int dow = calendar.get(Calendar.DAY_OF_WEEK);
		if (dow == Calendar.SATURDAY) {
			calendar.add(Calendar.DATE, 2);
		}
		if (dow == Calendar.SUNDAY) {
			calendar.add(Calendar.DATE, 1);
		}
		int week = calendar.get(Calendar.WEEK_OF_YEAR);
		return week;
	}

	public static String getMondayDate(int kw) {
		GregorianCalendar calendar = (GregorianCalendar) Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		calendar.clear();
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.WEEK_OF_YEAR, kw);
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		int month = calendar.get(Calendar.MONTH);
		int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
		return Integer.toString(dayOfMonth) + "." + Integer.toString(month + 1)
				+ ".";
	}

	public static String toDate(int weekOfYear, int wochentag) {

		GregorianCalendar calendar = (GregorianCalendar) Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		calendar.clear();
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.WEEK_OF_YEAR, weekOfYear);

		calendar.add(Calendar.DAY_OF_WEEK, wochentag + 1);

		Date datum = calendar.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.");
		String result = sdf.format(datum);
		return result;
	}

	public static String serialize(DATA data) {
		StringBuffer result = new StringBuffer();
		result.append("@MondayTitle@").append(data.weekdayTitle)
				.append("@MondayTitle@");
		for (VP vp : data.getVPs()) {
			result.append("@VP@").append(vp.serialize());
		}
		result.append("@VP@");
		return result.toString();
	}

	public static DATA deSerialize(String vpsMsg) {
		DATA result = new DATA();
		String text = vpsMsg;
		if (text.startsWith("@MondayTitle@")) {
			String[] split = text.split("@MondayTitle@");
			result.weekdayTitle = split[1];
			text = split[2];
		}
		if (text.startsWith("@VP@")) {
			String[] split = text.split("@VP@");
			for (int i = 1; i < split.length; i++) {
				VP vp = new VP();
				vp.deSerialize(split[i]);
				result.vpList.add(vp);
			}
		}
		return result;
	}

	public static InputStream getVpInput(int kw)
			throws Exception {
		int week = kw == 0 ? currentWeek() : kw;

		String url = "http://www.ovp.gymnasium-westerstede.de/" + formatWeek(week) + "/w/w00000.htm";
		Log.i(TAG, "URL: " + url);
		String userAgent = "GWVPlan Android App";
		InputStream result = httpGetStream(url, userAgent);
		
		return result;
	}

	public static DATA parseVpInfo(InputStream in, String treffer)
			throws IOException {
		VPHandlerCharSequence vpHandler = new VPHandlerCharSequence(treffer);
		SimpleHtmlParserCharSequence htmlParser = new SimpleHtmlParserCharSequence(
				vpHandler);

		String input = readStream(in, "iso-8859-1");
		htmlParser.parse(input);
		DATA result = vpHandler.getData();
		return result;
	}

	public static String expectedMondayTitle(int kw) {
		String result = GWVPlanUtil.getMondayDate(kw) + " Montag";
		return result;
	}

}
