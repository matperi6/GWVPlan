package de.gymwst.gwvplan;

import java.util.Map;

import android.content.Context;
import android.util.Log;
import de.gymwst.util.db.SimpleDB;

public class SimplePersistence {

	private final static String TAG = "GWVPlan.persist";

	private String storeName;
	private String seed;
	private SimpleDB simpleDB;
	private Map<String, String> cachedProperties;

	public SimplePersistence(Context ctx, String storeName) {
		this.storeName = storeName;
		this.seed = "f+" + this.storeName + "-h";
		this.simpleDB = new SimpleDB(ctx, storeName);
		reload();
	}

	public void reload() {
		simpleDB.open();
		this.cachedProperties = simpleDB.getAllKeyValues();
		simpleDB.close();
		debugLog("LOAD[" + storeName + "]", cachedProperties);
	}

	public void commit() {
		simpleDB.open();
		simpleDB.putAll(cachedProperties);
		simpleDB.close();
		debugLog("SAVE[" + storeName + "]", cachedProperties);
	}

	private void debugLog(String title, Map<String, String> data) {
		Log.d(TAG, title);
		for (String key : data.keySet()) {
			String value = data.get(key);
			Log.d(TAG, "  " + key + "='" + value + "'");
		}
	}

	public String getString(String key, String defaultValue) {
		String value = cachedProperties.get(key);
		if (value == null)
			return defaultValue;
		return value;
	}

	public String getDecryptedString(String key, String defaultValue) {
		String result = defaultValue;
		String encrypted = getString(key, null);
		if (encrypted == null)
			return defaultValue;
		try {
			result = SimpleCrypto.decrypt(seed, encrypted);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
		return result;
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		boolean result = defaultValue;
		try {
			String value = getString(key, null);
			if (value != null) {
				result = Boolean.parseBoolean(value);
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
		return result;
	}

	public float getFloat(String key, float defaultValue) {
		float result = defaultValue;
		try {
			String value = getString(key, null);
			if (value != null) {
				result = Float.parseFloat(value);
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
		return result;
	}

	public int getInt(String key, int defaultValue) {
		int result = defaultValue;
		try {
			String value = getString(key, null);
			if (value != null) {
				result = Integer.parseInt(value);
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
		return result;
	}

	public long getLong(String key, long defaultValue) {
		long result = defaultValue;
		try {
			String value = getString(key, null);
			if (value != null) {
				result = Long.parseLong(value);
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
		return result;
	}

	public void remove(String key) {
		cachedProperties.put(key, null);
	}

	public void putString(String key, String value) {
		cachedProperties.put(key, value);
	}

	public void putEncryptedString(String key, String value) {
		if (value == null)
			remove(key);
		else
			try {
				putString(key, SimpleCrypto.encrypt(seed, value));
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			}
	}

	public void putBoolean(String key, boolean value) {
		putString(key, Boolean.toString(value));
	}

	public void putFloat(String key, float value) {
		putString(key, Float.toString(value));
	}

	public void putInt(String key, int value) {
		putString(key, Integer.toString(value));
	}

	public void putLong(String key, long value) {
		putString(key, Long.toString(value));
	}

	public void setSeed(String seed) {
		this.seed = seed;
	}

}
