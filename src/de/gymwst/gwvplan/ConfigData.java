package de.gymwst.gwvplan;

import android.content.Context;

public class ConfigData {

	public String treffer;

	private SimplePersistence persist;

	public ConfigData(Context ctx) {
		persist = new SimplePersistence(ctx, "ConfigData");
		loadConfig();
	}

	public void loadConfig() {
		persist.reload();
		treffer = persist.getString("treffer", "");
	}

	public void writeConfig() {
		persist.putString("treffer", treffer);
		persist.commit();
	}
	
}
