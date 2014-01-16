package de.gymwst.gwvplan;

import android.content.Context;

public class VPData {

	private static final String PREFSTORE_VPDATA = "VPData";

	public String vpMsg;
	
	private SimplePersistence persist;

	public VPData(Context ctx) {
		persist = new SimplePersistence(ctx, PREFSTORE_VPDATA);
		load();
	}
	
	public void load() {
		persist.reload();
		vpMsg = persist.getString("vpMsg", "");
	}
	
	public void writeConfig() {
		persist.putString("vpMsg", vpMsg);
		persist.commit();
	}
	@Override
	public String toString() {
		return vpMsg;
	}
}
