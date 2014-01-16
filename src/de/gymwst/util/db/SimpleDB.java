package de.gymwst.util.db;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

public class SimpleDB {

	private KeyValueDataSource keyValueDataSource;

	public SimpleDB(Context context, String storeName) {
		this.keyValueDataSource = new KeyValueDataSource(context, storeName);
	}

	public void open() {
		keyValueDataSource.open();
	}

	public void close() {
		keyValueDataSource.close();
	}

	public String getValue(String key) {
		String result = null;
		KeyValue keyValue = keyValueDataSource.findKeyValueByKey(key);
		if (keyValue != null) {
			result = keyValue.getValue();
		}
		return result;
	}

	public void putValue(String key, String value) {
		KeyValue keyValue = new KeyValue();
		keyValue.setKey(key);
		keyValue.setValue(value);
		if (value == null) {
			keyValueDataSource.deleteKeyValue(keyValue);
		} else {
			keyValueDataSource.createKeyValue(keyValue);
		}
	}

	public Map<String, String> getAllKeyValues() {
		Map<String, KeyValue> allKeyValues = keyValueDataSource
				.getAllKeyValues();
		Map<String, String> result = new HashMap<String, String>();
		for (String key : allKeyValues.keySet()) {
			result.put(key, allKeyValues.get(key).getValue());
		}
		return result;
	}

	public void putAll(Map<String, String> keyValues) {
		if (keyValues != null) {
			for (String key : keyValues.keySet()) {
				String value = keyValues.get(key);
				putValue(key, value);
			}
		}
	}

}
