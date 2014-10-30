/*
 * 
 * @author Danny
 * @version 0.1
 *
 * 
 * 29/7/13 - string externalisation complete - no action required.
 * 7/8/13 - Icon added to animation sequence
 * 
 */


package uk.co.darnster.chillcentral;


import uk.co.darnster.chillcentral.R;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;

public class SplashActivity extends BlankActivity implements View.OnClickListener {
	
	public static final String APP_SETTINGS = "AppSettings"; // String to hold
	public static final String RULES_EXIST = "RulesExist"; // String
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); 
        // if we've got here there are no rules, so let the phone receiver know this
        appSettings = getSharedPreferences(APP_SETTINGS, 0);
    	SharedPreferences.Editor prefEditor = appSettings.edit();
		prefEditor.putBoolean(RULES_EXIST, false);
		prefEditor.commit();
		
   		startAnimation();
        
       }
    
	
	public void startAnimation(){
		
		ImageView icon_image = (ImageView) findViewById(R.id.chill_logo);
		//icon_image.setVisibility(0);
		Animation icon_fade = AnimationUtils.loadAnimation(this, R.anim.icon_anim);
		icon_image.startAnimation(icon_fade);		
		
		TextView message = (TextView) findViewById(R.id.scream);
		message.setText(getResources().getString(R.string.splashMessageOne));
        
		Animation fade1 = AnimationUtils.loadAnimation(this, R.anim.first_text_block);
        message.startAnimation(fade1);
        //message.setVisibility(8);
        
        TextView message2 = (TextView) findViewById(R.id.scream2);
        Animation fade2 = AnimationUtils.loadAnimation(this, R.anim.second_text_block);
        message2.setText(getResources().getString(R.string.splashMessageTwo));
        message2.startAnimation(fade2);
        //message2.setVisibility(8);
        
        TextView message3 = (TextView) findViewById(R.id.scream3);
        Animation fade3 = AnimationUtils.loadAnimation(this, R.anim.third_text_block);
        message3.setText(getResources().getString(R.string.splashMessageThree));
        message3.startAnimation(fade3);
        //message3.setVisibility(8);
        
        Button addRule = (Button) findViewById(R.id.button_add_rule); 
        Animation fade4 = AnimationUtils.loadAnimation(this, R.anim.fade_in_button);
        addRule.startAnimation(fade4);
        
	}
	
	public void addRule(View view) {
		finish();
		startActivity(new Intent(this, AddRuleActivity.class));
	}

	public void onBackPressed() {
    	finish();		
	}
	
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}


}