package de.gymwst.util.htmlparser;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SimpleHtmlParserCharSequence {

	private HtmlHandlerCharSequence handler;

	private char[] chars;
	int maxPos;

	public class Chars implements CharSequence {
		int start;
		int end;
		String str;

		private void init(int start, int end) {
			this.start = start;
			this.end = end;
			this.str = null;
		}

		// @Override
		@Override
		public char charAt(int i) {
			return chars[i];
		}

		// @Override
		@Override
		public CharSequence subSequence(int start, int end) {
			Chars result = new Chars();
			result.init(start, end);
			return result;
		}

		// @Override
		@Override
		public int length() {
			return end - start;
		}

		// @Override
		@Override
		public String toString() {
			if (str == null) {
				str = new String(chars, start, end - start);
			}
			return str;
		}

		// @Override
		@Override
		public boolean equals(Object other) {
			if (other == null) {
				return false;
			}
			if (other instanceof CharSequence) {
				CharSequence otherCS = (CharSequence) other;
				if (otherCS.length() != length()) {
					return false;
				}
				return toString().equals(otherCS.toString());
			}
			return false;
		}
	}

	public class Attributes {
		int start;
		int end;
		Map<String, String> keyValueMap;

		private void init(int start, int end) {
			this.start = start;
			this.end = end;
			this.keyValueMap = null;
		}

		public String getAttributeValue(String name) {
			return getMap().get(name);
		}

		public Collection<String> getAttributeNames() {
			return getMap().keySet();
		}

		private Map<String, String> getMap() {
			if (keyValueMap == null) {
				keyValueMap = new HashMap<String, String>();
				int pos = start;
				while (pos < end) {
					int posKeyStart = skipSpaces(pos);
					int posKeyEQ = findNext(posKeyStart + 1, '=');
					int posKeyEnd = reverseSkipSpaces(posKeyEQ - 1) + 1;
					int posQuote = skipSpaces(posKeyEQ + 1);
					if ((posQuote == -1) || (posQuote >= end)) {
						break;
					}
					char quote = chars[posQuote];
					if ((quote != '"') && (quote != '\'')) {
						break;
					}
					int posQuoteEnd = findNext(posQuote + 1, quote);
					if ((posQuoteEnd == -1) || (posQuoteEnd >= end)) {
						break;
					}
					String key = new String(chars, posKeyStart, posKeyEnd
							- posKeyStart);
					String value = new String(chars, posQuote + 1, posQuoteEnd
							- posQuote - 1);
					keyValueMap.put(key, value);
					pos = posQuoteEnd + 1;
				}
			}
			return keyValueMap;
		}
	}

	public SimpleHtmlParserCharSequence(HtmlHandlerCharSequence handler) {
		this.handler = handler;
	}

	public void parse(String htmlText) throws IOException {
		Chars stringWrapper = new Chars();
		Attributes attributes = new Attributes();
		chars = htmlText.toCharArray();
		maxPos = chars.length;
		int pos = 0;
		handler.startDocument();
		while (pos < maxPos) {
			int posLT = findNext(pos, '<');
			if (posLT == -1) {
				stringWrapper.init(pos, maxPos);
				handler.characters(stringWrapper);
				break;
			}
			stringWrapper.init(pos, posLT);
			handler.characters(stringWrapper);
			int posTagStart = skipSpaces(posLT + 1);
			if (posTagStart == -1) {
				break;
			}
			boolean endTag = chars[posTagStart] == '/';
			if (endTag) {
				posTagStart = skipSpaces(posTagStart + 1);
			}
			int posTagEnd = skipAlpha(posTagStart);
			posTagEnd = skipAlphaNum(posTagEnd);
			int posGT = findNext(pos, '>');
			if (posGT == -1) {
				break;
			}
			stringWrapper.init(posTagStart, posTagEnd);
			if (endTag) {
				handler.endElement(stringWrapper);
			} else {
				attributes.init(posTagEnd, posGT);
				handler.startElement(stringWrapper, attributes);
			}
			pos = posGT + 1;
		}
		handler.endDocument();
	}

	private int findNext(int pos, char c) {
		if (pos == -1) {
			return -1;
		}
		for (int i = pos; i < maxPos; i++) {
			if (chars[i] == c) {
				return i;
			}
		}
		return -1;
	}

	private int skipSpaces(int pos) {
		if (pos == -1) {
			return -1;
		}
		for (int i = pos; i < maxPos; i++) {
			char c = chars[i];
			if ((c != ' ') && (c != '\t')) {
				return i;
			}
		}
		return -1;
	}

	private int reverseSkipSpaces(int pos) {
		if (pos == -1) {
			return -1;
		}
		for (int i = pos; i >= 0; i--) {
			char c = chars[i];
			if ((c != ' ') && (c != '\t')) {
				return i;
			}
		}
		return -1;
	}

	private int skipAlpha(int pos) {
		if (pos == -1) {
			return -1;
		}
		for (int i = pos; i < maxPos; i++) {
			char c = chars[i];
			if (((c < 'a') || (c > 'z')) && ((c < 'A') || (c > 'Z'))) {
				return i;
			}
		}
		return -1;
	}

	private int skipAlphaNum(int pos) {
		if (pos == -1) {
			return -1;
		}
		for (int i = pos; i < maxPos; i++) {
			char c = chars[i];
			if (((c < 'a') || (c > 'z')) && ((c < 'A') || (c > 'Z'))
					&& ((c < '0') || (c > '9'))) {
				return i;
			}
		}
		return -1;
	}

}