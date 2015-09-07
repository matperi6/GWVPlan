package de.gymwst.gwvplan;

import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Enumeration;
import java.util.Vector;

import de.gymwst.gwvplan.VPHandlerCharSequence.DATA;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableLayout.LayoutParams;
import android.widget.TableRow;
import android.widget.TextView;

public class GWVPlanActivity extends Activity {


	private static final String TAG = "GWVPlan.gui";
	
	public final static String PREFS_NAME = "GWVPLanPrefs";
    public final static String INTENT_EXTRA_DELAYMINUTES = "de.gymwst.gymwstvplan.DELAYMINUTES";
    
    //für die DialogAnzeige
    final Context context = this;

	private ConfigData configData;
	private VPData vpData;

	private TextView tvAuswahl;
	
	private TableLayout table_vertretung;
	private float dip; //Variable zum Auslesen der DisplayMetrics 

	private Button buttonSync;
	private Button buttonSettings;
	private Button buttonShowall;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Converting to dip unit, DisplayEinstellung auslesen und in dip eintragen
        dip = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 1, getResources().getDisplayMetrics());
        
        setContentView(R.layout.activity_main);
        	               
    	configData = new ConfigData(this);
		vpData = new VPData(this);
        
		tvAuswahl = (TextView)findViewById(R.id.tv_auswahl);
		
		buttonSync = (Button)findViewById(R.id.buttonSync);
		buttonSettings = (Button)findViewById(R.id.buttonSettings);
		buttonShowall = (Button)findViewById(R.id.buttonShowall);
		
        buttonSync.setOnClickListener(mUpdateListener);
		buttonSettings.setOnClickListener(mSettingsListener);
		buttonShowall.setOnClickListener(mShowallListener);
		
    	table_vertretung = (TableLayout)findViewById(R.id.tl_vertretung);
        	
    	fillAuswahl();
    	startUpdateThread();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    /**
     * Called when a menu item is selected.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	if (item.getItemId() == R.id.menu_info) {
    		AlertDialog.Builder alertDialogInfo = new AlertDialog.Builder(context);

		    // Setting Dialog Title
		    alertDialogInfo.setTitle("Über GWVPlan");
		
		    // Setting Dialog Message
		    alertDialogInfo.setMessage("Android-Vertretungsplan für\n" +
		    		"das Gymnasium Westerstede\n\nAutor: M. Perenthaler\n\nBuild 1.08 (2015)");
		
		    // Setting Icon to Dialog
		    alertDialogInfo.setIcon(R.drawable.i_green);
		
		    // Setting OK Button
		    alertDialogInfo.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int which) {
		            // Write your code here to execute after dialog closed
		             }
		    });

		    // Showing Alert Message
		    alertDialogInfo.show();
    	}
    	return true;
    }
   
  
    private OnClickListener mUpdateListener = new OnClickListener() {
        public void onClick(View v) {
        	startUpdateThread();
        }
    };    
    private OnClickListener mSettingsListener = new OnClickListener() {
        public void onClick(View v) { 	
        	EingabeDialog();
        }
    };    
    private OnClickListener mShowallListener = new OnClickListener() {
        public void onClick(View v) {
            doShowall();
        }
    };  
    
	public void EingabeDialog() {
		LayoutInflater li = LayoutInflater.from(context);
		View promptsView = li.inflate(R.layout.suchalert_dialog, null);
		
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

		// set prompts.xml to alertdialog builder
		alertDialogBuilder.setView(promptsView);

		final EditText userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);

		// set dialog message
		alertDialogBuilder
			.setCancelable(false)
			.setNegativeButton("Abbrechen",
			  new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog,int id) {
				dialog.cancel();
			    }
			  })
			.setPositiveButton("Speichern",
			  new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog,int id) {
				// get user input and set it to result
				// edit text
			    	if (userInput.getText().toString().length() < 2) {
			    		configData.treffer = "";
			    	} else configData.treffer = userInput.getText().toString();
			    	configData.writeConfig();
			    	fillAuswahl();
			    	startUpdateThread();		    	
			    }
			  });

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show(); 
    }
    
    public void doStart() {
		vpData.load();
		vpData.vpMsg = "";
		vpData.writeConfig();
    	configData.loadConfig();
    	configData.writeConfig();
    }

    public void doStop() {
    	configData.loadConfig();
    	configData.writeConfig();
   	} 
   
    public void doShowall() {
   		configData.treffer = "";
    	configData.writeConfig();
    	fillAuswahl();
    	startUpdateThread();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	fillAuswahl();
    	if (resultCode == RESULT_OK) {
    		readValues();
    		startUpdateThread();
    	}
    	super.onActivityResult(requestCode, resultCode, data);
    }    
    
    private void readValues() {
	}

	/**
     * from http://wowjava.wordpress.com/2011/04/01/dynamic-tablelayout-in-android/
     * @param vpList
     */
    public void fillMsgTable(String msg, int textColorID, int bgColorID) {
        table_vertretung.removeAllViewsInLayout();

		TextView textView = new TextView(this);
		textView.setTextColor(getResources().getColor(textColorID));
		textView.setText(msg);
		textView.setTypeface(null, 1);
		textView.setTextSize(14);
		textView.setWidth((int) (320*dip));
		textView.setPadding((int) (5*dip), 0, 0, 0);
		textView.setBackgroundColor(getResources().getColor(bgColorID));

    	TableRow row = new TableRow(this);
		row.addView(textView);
    	table_vertretung.addView(row);
    }

    public void fillAuswahl() {
    	String auswahlText;
    	configData.loadConfig();
    	if (configData.treffer.equals("")) {
    		auswahlText = "Alle Vertretungen werden angezeigt...";
    	} else {
    		auswahlText = "Vertretungen für: " + configData.treffer;    		
    	}
    	tvAuswahl.setText(auswahlText);
    }
    
	/**
     * from http://wowjava.wordpress.com/2011/04/01/dynamic-tablelayout-in-android/
     * @param vpList
     */
    public void fillTable() {
    	vpData.load();
		DATA data = GWVPlanUtil.deSerialize(vpData.vpMsg);
		VP[] vps = data.getVPs();
		table_vertretung.removeAllViewsInLayout();
        
		if (vps.length == 0) {
			fillMsgTable("Es wurden keine Einträge gefunden!", R.color.textdarkgrey2, R.color.white);      		
		} else {
				for (int current = 0; current < vps.length; current++) {
					//dunkelblau - hellblau abwechselnd
					//int bgColorID = (current%2==0)?R.color.lightblue:R.color.lightblue2;
					int bgColorID = R.color.white;
			        TableRow[] rows = createTableRow(vps[current], bgColorID);
			        for (TableRow row:rows) {
			        	table_vertretung.addView(row, new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			        }
		        }
		}
    }

	private TableRow[] createTableRow(VP vp, int bgColorID) {
        
        String ErsteZeileText = "";
        String ZweiteZeileText = "";
        String DritteZeileText = "";
        
        if (vp.klassen.equals("")){
        	vp.vertrtext = "Vertretung der Aufsicht";   
        }
        
        if (vp.klassen.equals("Abwesende Klassen")){
        	vp.vertrtext = "Abwesende Klasse(n): " + vp.stunde;
        }
        
        if (vp.vertrtext.equals("") && vp.lehrernach.equals("")) {
        	DritteZeileText = "Info: ---";
        }
      	if (vp.vertrtext.equals("") && !vp.lehrernach.equals("")) {
        	DritteZeileText = "Info: " + vp.lehrernach;
      	}
      	if (!vp.vertrtext.equals("") && vp.lehrernach.equals("")) {
        	DritteZeileText = "Info: " + vp.vertrtext;
      	}
      	if (!vp.vertrtext.equals("") && !vp.lehrernach.equals("")) {      	
        	DritteZeileText = "Info: " + vp.vertrtext + ", " + vp.lehrernach;
        } 
      	
        if (vp.klassen.equals("Abwesende Klassen")){
        	DritteZeileText = "Abwesende Klasse(n): " + vp.stunde;
        }
      	
      	if (vp.raum.equals("---")) vp.raum = "fällt aus";
      	
      	if (vp.klassen.equals("Abwesende Klassen")) {
      		ErsteZeileText = vp.wochentagTag + ", " + vp.wochentagZahl;  		
      	}
        if (vp.klassen.equals("")){
        	ErsteZeileText = vp.wochentagTag + ", " + vp.wochentagZahl + " in " + vp.stunde +". Stunde:  " + vp.raum;
        }
        if (vp.stunde.equals("")){
        		//Anmerkungen zum Tag
        		ErsteZeileText = vp.wochentagTag + ", " + vp.wochentagZahl;        		
        		DritteZeileText = "Info: " + vp.klassen;
        }
        if (!vp.klassen.equals("") && !vp.klassen.equals("Abwesende Klassen") && !vp.stunde.equals("")){       
            	ErsteZeileText = vp.klassen + ", " + vp.wochentagTag + ", " + vp.wochentagZahl + " in " + vp.stunde +". Stunde:  " + vp.raum;        		
        }    

		int PosLeerzeichenInString = vp.lehrer.indexOf(' ');
		String LehrerAbwesend;
		if (PosLeerzeichenInString > 0) {
			LehrerAbwesend = vp.lehrer.substring(0,PosLeerzeichenInString);
		} else LehrerAbwesend = vp.lehrer; 
	
        if (vp.fach.equals("/")) vp.fach = "";
        if (!vp.fach.equals("") && !vp.vertreter.equals("") && !vp.lehrer.equals("")) {
        	ZweiteZeileText = vp.fach + "    Vertretung: " + vp.vertreter + "    ursprünglich: " + LehrerAbwesend;       	
        }
        if (!vp.fach.equals("") && !vp.vertreter.equals("") && vp.lehrer.equals("")) {
        	ZweiteZeileText = vp.fach + "    Vertretung: " + vp.vertreter;
      	}
        if (!vp.fach.equals("") && vp.vertreter.equals("") && vp.lehrer.equals("")) {
        	ZweiteZeileText = vp.fach;
      	}
        if (!vp.fach.equals("") && vp.vertreter.equals("") && !vp.lehrer.equals("")) {
        	ZweiteZeileText = vp.fach + "    ursprünglich: " + LehrerAbwesend;
        }
        if (vp.fach.equals("") && !vp.vertreter.equals("") && !vp.lehrer.equals("")) {
        	ZweiteZeileText = "Vertretung: " + vp.vertreter + "    ursprünglich: " + LehrerAbwesend;
        } 
        if (vp.fach.equals("") && !vp.vertreter.equals("") && vp.lehrer.equals("")) {
        	ZweiteZeileText = "Vertretung: " + vp.vertreter;
        }
        if (vp.fach.equals("") && vp.vertreter.equals("") && !vp.lehrer.equals("")) {
        	ZweiteZeileText = "ursprünglich: " + LehrerAbwesend;
        }
    
       
        TableRow[] result = new TableRow[5];
        result[0] = new TableRow(this);
        addTableEntry(result[0], ErsteZeileText, 13, R.color.textdarkgrey1, R.color.beige, 1, 5, 1);
        
        result[1] = new TableRow(this);
        addTableEntry(result[1], ZweiteZeileText, 12, R.color.textdarkgrey2, bgColorID, 1, 5, 1);
        
        result[2] = new TableRow(this); 
        addTableEntry(result[2], DritteZeileText, 12, R.color.textdarkgrey2, bgColorID, 1, 5, 1);
        
        result[3] = new TableRow(this);
        addTableEntry(result[3], "", 1, R.color.darkblue, R.color.darkgrey, 1, 0, 1);
        
        result[4] = new TableRow(this);
        addTableEntry(result[4], "", 4, R.color.darkblue, R.color.grey, 1, 0, 1);
        
        return result;        
	}
    
	private void addTableEntry(TableRow row, String text, int textSize, int colorID, int bgColorID, int dipWidth, int dipPadding, int colspan) {
		TextView textView = new TextView(this);
		textView.setTextColor(getResources().getColor(colorID));
		textView.setText(text);
		textView.setTypeface(null, 1);
		textView.setTextSize(textSize);
		textView.setWidth((int) (dipWidth*dip));
		textView.setPadding((int) (dipPadding*dip), 0, 0, 0);
		textView.setBackgroundColor(getResources().getColor(bgColorID));
		if (colspan != 1) {
	        TableRow.LayoutParams params = new TableRow.LayoutParams();
	        params.span = colspan;
	        row.addView(textView, params);
		}
		else {
			row.addView(textView);
		}
	}

	private UpdateTableTask updateTableTask;
	
	private synchronized void startUpdateThread() {
		if (updateTableTask != null) {
			return;
		}
		configData.loadConfig();
		updateTableTask = new UpdateTableTask();
		updateTableTask.execute(configData.treffer);
	}
	
	private class UpdateTableTask extends AsyncTask<String, Integer, DATA> {
		private String errMsg;
		private int kd; //currentday
		private int kw; //currentweek
		private int kwFollow; //nextweek
	    
		protected DATA doInBackground(String... params) {
	    	try {
	    		errMsg = null;
		    	kd = GWVPlanUtil.currentDay();
	    		kw = GWVPlanUtil.currentWeek();
		    	kwFollow = kw + 1;
		    	if (kwFollow == 53) kwFollow = 0;
		    			    	
		    	String treffer = params[0];
		    	
		    	publishProgress(0);		
		    	
		    	InputStream in1 = GWVPlanUtil.getVpInput(kw);
		    	Vector<InputStream> inputStreams = new Vector<InputStream>();
		    	
		    	inputStreams.add(in1);
		    	
		    	if (kd != 1 && kd != 7) {
		    		InputStream in2 = GWVPlanUtil.getVpInput(kwFollow);
		    		inputStreams.add(in2);
		    	}
	    		    	
				Enumeration<InputStream> enu = inputStreams.elements();
				SequenceInputStream sis = new SequenceInputStream(enu);
				
		    	if (isCancelled()) { return null; }
		    	
		    	publishProgress(1);
		    	
				DATA data = GWVPlanUtil.parseVpInfo(sis, treffer);
		    	if (isCancelled()) { return null; }
		    	
		    	publishProgress(2);
		    	
		    	return data;
	    	}
	    	catch (Exception e) {
        		Log.e(TAG, "UPDATE fehlgeschlagen: "+e.getMessage(), e);
    			errMsg = "Fehler: "+e.getClass()+" - "+e.getMessage();
    	    	return null;
			}
	    }

	    protected void onProgressUpdate(Integer... progress) {
	    	String msg = "?";
	    	if (progress[0] == 0) {
	    		msg = "Verbinde mit Onlinevertretungsplan ...";
	    	}
	    	else if (progress[0] == 1) {
	    		msg = "Analysiere Einträge ...";
	    	}
	    	if (progress[0] == 2) {
	    		msg = "Anzeige wird vorbereitet ...";
	    	}
 			fillMsgTable(msg, R.color.textdarkgrey2, R.color.white);
	    }

		protected void onPostExecute(DATA data) {
			if (data == null) {
				//fillMsgTable(errMsg, R.color.textdarkgrey2, R.color.white);
				fillMsgTable("Auf den Vertretungsplan kann nicht zugegriffen werden. Bitte prüfen Sie: " 
						+ "\n\n1. Ist das Handy mit dem Internet verbunden?"
						+ "\n\n2. Ist der Vertretungsplan online?\n", R.color.textdarkgrey2, R.color.white);
				errMsg = null;
			}
/*			else if (!GWVPlanUtil.expectedMondayTitle(kw).equals(data.weekdayTitle)) {
				fillMsgTable("Der Vertretungsplan für die KW"+kw+" ist nicht aktuell ("+data.weekdayTitle+" statt "+GWVPlanUtil.expectedMondayTitle(kw)+")", R.color.textdarkgrey2, R.color.white);
			} */
			else {
				vpData.vpMsg = GWVPlanUtil.serialize(data);
				vpData.writeConfig();
				fillTable();
			}
			updateTableTask = null;
			return;
	     }

	 }	
	
}
