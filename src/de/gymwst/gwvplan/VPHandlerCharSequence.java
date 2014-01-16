package de.gymwst.gwvplan;

import java.util.ArrayList;
import java.util.List;

import de.gymwst.util.htmlparser.HtmlHandlerCharSequence;
import de.gymwst.util.htmlparser.SimpleHtmlParserCharSequence.Attributes;

public class VPHandlerCharSequence extends HtmlHandlerCharSequence {

	public static class DATA {
		public List<VP> vpList;
		public String weekdayTitle;
		public String weekdayZahl;
		public String weekdayTag;

		public DATA() {
			vpList = new ArrayList<VP>();
			weekdayTitle = "?";
		}

		public VP[] getVPs() {
			return vpList.toArray(new VP[vpList.size()]);
		}
	}

	private DATA data;

	private String treffer;

	private int depthDiv;
	private int vertretungDivDepth;
	private boolean parseNextB;
	private boolean inB;
	private boolean inTable;
	private boolean inTr;
	private boolean inTd;
	private boolean inStrike;
	private int numTable;
	private int numTd;
	private boolean matchFound;
	private StringBuffer[] tdTexte;

	public VPHandlerCharSequence(String treffer) {
		this.treffer = treffer;
	}

	@Override
	public void startDocument() {
		data = new DATA();
		depthDiv = 0;
		vertretungDivDepth = 0;
		parseNextB = true;
		inB = false;
		inTable = false;
		inTr = false;
		inTd = false;
		inStrike = false;
		numTable = 0;
		numTd = 0;
		matchFound = false;
		//tdTexte vorher definieren
		tdTexte = new StringBuffer[10];
		for (int i = 0; i < 10; i++) {
			tdTexte[i] = new StringBuffer();
		}
	}

	@Override
	public void startElement(CharSequence localName, Attributes attributes) {
		if (localName.equals("div")) {
			String value = attributes.getAttributeValue("id");
			depthDiv += 1;
			if ("vertretung".equals(value)) {
				vertretungDivDepth = depthDiv;
			}
			return;
		}
		if (vertretungDivDepth == 0) {
			return;
		}
		if (localName.equals("table")) {
			inTable = true;
			numTable += 1;
			return;
		}
		if (localName.equals("b")) {
			inB = true;
		}
		if (!inTable) {
			return;
		}
		if (localName.equals("tr")) {
			inTr = true;
			numTd = 0;
			matchFound = false;
			return;
		}
		if (!inTr) {
			return;
		}
		if (localName.equals("td")) {
			inTd = true;
			numTd += 1;
			return;
		}
		if (localName.equals("strike")) {
			inStrike = true;
			return;
		}	
	}

	@Override
	public void endElement(CharSequence localName) {
		if (depthDiv == 0) {
			return;
		}
		if (localName.equals("div")) {
			if (vertretungDivDepth == depthDiv) {
				vertretungDivDepth = 0;
			}
			depthDiv -= 1;
			return;
		}
		if (inB && localName.equals("b")) {
			inB = false;
			parseNextB = false;
		}
		if (!inTable) {
			return;
		}
		if (localName.equals("table")) {
			inTable = false;
			return;
		}
		if (!inTr) {
			return;
		}
		if (localName.equals("tr")) {
			if (matchFound) {
				VP vp = new VP();
				vp.wochentagTag = data.weekdayTag;
				vp.wochentagZahl = data.weekdayZahl;		
				vp.klassen = removeNBSP(tdTexte[0].toString());
				vp.stunde = removeNBSP(tdTexte[1].toString());
				vp.vertreter = removeNBSP(tdTexte[2].toString());
				vp.raum = removeNBSP(tdTexte[3].toString());
				vp.fach = removeNBSP(tdTexte[4].toString());
				vp.vertrvon = removeNBSP(tdTexte[5].toString());
				vp.vertrtext = removeNBSP(tdTexte[6].toString());
				vp.lehrer = removeNBSP(tdTexte[7].toString());
				vp.lehrernach = removeNBSP(tdTexte[8].toString());
				data.vpList.add(vp);			
			}
			inTr = false;
			//tdTexte nach Verlassen einer Zeile zurücksetzen
			tdTexte = new StringBuffer[10];
			for (int i = 0; i < 10; i++) {
				tdTexte[i] = new StringBuffer();
			}
			return;
		}
		if (!inTd) {
			return;
		}
		if (localName.equals("td")) {
			inTd = false;
			if (inStrike) inStrike = false;
			return;
		}
	}

	private String removeNBSP(String text) {
		String result = text.replaceAll("&nbsp;", "").trim();
		return result;
	}

	@Override
	public void characters(CharSequence chars) {
		
		String TeilTreffer1 = "";
		String TeilTreffer2 = "";
		String RestTreffer1 = "";
		String RestTreffer2 = "";
		String CharTeil1 = "";
		String CharTeil2 = "";
		
		int KlassenNummerLaenge = 0;
		int KlassenNummer = 0;
		
		if (inB) {
			//Datum mit in die Datenstruktur aufnehmen
			if (parseNextB) data.weekdayTitle = chars.toString();
			
			//Datum ausschneiden (ohne Wochentag)
			int PosLeerzeichenInString = chars.toString().indexOf(' ');
			data.weekdayTag = chars.toString().substring(PosLeerzeichenInString+1,PosLeerzeichenInString+3);
			data.weekdayZahl = chars.toString().substring(0, PosLeerzeichenInString);
			
		}
		
		//numTd == 1 (Klasse) wird abgefragt, auch für zusammengesetzte Lerngruppen		
		if ((numTd == 1) && inTd) {
			if (chars.toString().indexOf(treffer) != -1) {
				matchFound = true;
			} else {

				try {
					TeilTreffer1 = treffer.substring(0,1); 
					KlassenNummer = Integer.parseInt(TeilTreffer1);
					KlassenNummerLaenge = 1;
					try {
						TeilTreffer2 = treffer.substring(0,2);
						KlassenNummer = Integer.parseInt(TeilTreffer2);
						KlassenNummerLaenge = 2;
						} catch (NumberFormatException nfe) {
						}
					} catch (NumberFormatException nfe) {
				}
				switch(KlassenNummerLaenge) {
				case 1:
					CharTeil1 = chars.toString().substring(0,1);
					if (CharTeil1.equals(TeilTreffer1)) {
						RestTreffer1 = treffer.substring(1);
						CharTeil1 = chars.toString().substring(1);
						if (CharTeil1.contains(RestTreffer1)) {
							matchFound = true;
						}
					}
					break;
				case 2:
					CharTeil2 = chars.toString().substring(0,2);
					if (CharTeil2.equals(TeilTreffer2)) {
						RestTreffer2 = treffer.substring(2);
						CharTeil2 = chars.toString().substring(2);
						if (CharTeil2.contains(RestTreffer2)) {
							matchFound = true;
						}
					}
					break;
				default:
					break;
				}

			}
		}	
		
		//numTd == 3 (Lehrer) wird abgefragt
		if ((numTd == 3) && inTd) {
			if (chars.toString().indexOf(treffer) != -1) {
				matchFound = true;
			}	
		}	
		
		//numTd == 8 (Lehrer Ausfall) wird abgefragt
		if ((numTd == 8) && inTd) {
			if (chars.toString().indexOf(treffer) != -1) {
				matchFound = true;
			}	
		}		

		if (numTd > 0 && inTd) {
			tdTexte[numTd-1].append(chars.toString());
			
			//InStrike ist wahr, wenn ein Lehrer oder Fach in der htm durchgestrichen ist.
			if (inStrike) { 
				tdTexte[numTd-1].append(" (f.a.)");
				inStrike = false;	
			}
		}
 
	}

	public DATA getData() {
		return data;
	}
}