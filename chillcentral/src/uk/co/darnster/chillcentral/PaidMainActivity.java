package uk.co.darnster.chillcentral;

// doesn't do anything as this App simply calls chillcentralbase Activities


import uk.co.darnster.chillcentralbase.MainActivity;
import android.os.Bundle;
import android.view.Menu;


public class PaidMainActivity extends MainActivity {
	
	public static boolean upgradeShown = false;  // used to prevent the dialog from appearing > 1 during each session
	public boolean showUpgradeDialog = false; // used to control whether a particular screen should show the upgrade Dialog


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.paid_main, menu);
		super.onCreateOptionsMenu(menu);
		return true;
	}

}
