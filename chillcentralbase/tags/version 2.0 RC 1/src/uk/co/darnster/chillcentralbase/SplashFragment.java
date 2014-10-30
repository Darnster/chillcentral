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


package uk.co.darnster.chillcentralbase;


import uk.co.darnster.chillcentralbase.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;

public class SplashFragment extends Fragment implements View.OnClickListener {
	
	public static final String APP_SETTINGS = "AppSettings"; // String to hold
	public static final String RULES_EXIST = "RulesExist"; // String
	private int layoutView = R.layout.frag_splash;
	public static Context ctx;
	private View v;
	public SharedPreferences appSettings;

    @Override
public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {

		ctx = getActivity();
		
		v = inflater.inflate(layoutView, container, false);
		
		// if we've got here there are no rules, so let the phone receiver know this
		
   		startAnimation();
        return v;
       }
    
	
	public void startAnimation(){
		
		ImageView icon_image = (ImageView) v.findViewById(R.id.chill_logo);
		//icon_image.setVisibility(0);
		Animation icon_fade = AnimationUtils.loadAnimation(ctx, R.anim.icon_anim);
		icon_image.startAnimation(icon_fade);		
		
		TextView message = (TextView) v.findViewById(R.id.scream);
		message.setText(getResources().getString(R.string.splashMessageOne));
        
		Animation fade1 = AnimationUtils.loadAnimation(ctx, R.anim.first_text_block);
        message.startAnimation(fade1);
        //message.setVisibility(8);
        
        TextView message2 = (TextView) v.findViewById(R.id.scream2);
        Animation fade2 = AnimationUtils.loadAnimation(ctx, R.anim.second_text_block);
        message2.setText(getResources().getString(R.string.splashMessageTwo));
        message2.startAnimation(fade2);
        //message2.setVisibility(8);
        
        TextView message3 = (TextView) v.findViewById(R.id.scream3);
        Animation fade3 = AnimationUtils.loadAnimation(ctx, R.anim.third_text_block);
        message3.setText(getResources().getString(R.string.splashMessageThree));
        message3.startAnimation(fade3);
        //message3.setVisibility(8);
        
        Button addRule = (Button) v.findViewById(R.id.button_add_rule); 
        Animation fade4 = AnimationUtils.loadAnimation(ctx, R.anim.fade_in_button);
        addRule.startAnimation(fade4);
        
	}
	

	 public static final SplashFragment newInstance(Context context) {
		 ctx = context;
		 SplashFragment f = new SplashFragment();
		   return f;

		 }

		public boolean onCreateOptionsMenu(Menu menu) {
	        // use an inflater to populate the ActionBar with items
	        MenuInflater inflater = ((Activity) ctx).getMenuInflater();
	        inflater.inflate(R.menu.global_menu, menu);
	  
		        return true;
	    	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
	
}