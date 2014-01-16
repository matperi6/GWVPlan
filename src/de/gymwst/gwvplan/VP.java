package de.gymwst.gwvplan;

import android.util.Log;

public class VP {

	private final static String TAG = "GWVPlan.VP";

	public String wochentagTag;
	public String wochentagZahl;
	public String klassen;
	public String stunde;
	public String vertreter;
	public String raum;
	public String fach;
	public String vertrvon;
	public String vertrtext;
	public String lehrer;
	public String lehrernach;

	@Override
	public String toString() {
		String result = wochentagTag + ". " + wochentagZahl + " " + klassen + " [Std: "
				+ stunde + "] " + vertreter + ": " + fach + " " + raum + " "
				+ lehrer + " " + lehrernach;
		return result;
	}

	public String serialize() {
		String result = wochentagTag + "~" + wochentagZahl + "~" + klassen + "~" + stunde + "~" + vertreter
				+ "~" + raum + "~" + fach + "~" + vertrvon + "~"
				+ vertrtext + "~" + lehrer + "~" + lehrernach;
		return result;
	}

	public void deSerialize(String ser) {
		Log.i(TAG, ser);
		String[] data = ser.split("[~]");
		if (data.length < 12) {
			String[] newData = new String[] { "", "", "", "", "", "", "", "", "", "", "" };
			for (int i = 0; i < data.length; i++) {
				newData[i] = data[i];
			}
			data = newData;
		}
		Log.i(TAG, "len=" + data.length);
		wochentagTag = data[0];
		wochentagZahl = data[1];
		klassen = data[2];
		stunde = data[3];
		vertreter = data[4];
		raum = data[5];
		fach = data[6];
		vertrvon = data[7];
		vertrtext = data[8];
		lehrer = data[9];
		lehrernach = data[10];
	}

}