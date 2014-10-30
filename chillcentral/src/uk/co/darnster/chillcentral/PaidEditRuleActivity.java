package uk.co.darnster.chillcentral;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import uk.co.darnster.chillcentralbase.EditRuleActivity;
import uk.co.darnster.chillcentralbase.MainActivity;

public class PaidEditRuleActivity extends EditRuleActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.paid_main, menu);
		super.onCreateOptionsMenu(menu);
		return true;
	}

	private void backTarget() {
		// Used to redirect to appropraiate class
		startActivity(new Intent(this, PaidMainActivity.class));
	}
}
