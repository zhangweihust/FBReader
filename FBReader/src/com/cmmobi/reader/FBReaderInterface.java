package com.cmmobi.reader;

import android.app.Activity;
import android.content.Intent;

public interface FBReaderInterface {
	public void navigate();
	public void hideSelectionPanel();
	public void showSelectionPanel();
	
	public int getDataConnectionPort();
	public void startActivity(Intent i);
	public Activity getActivity();

}
