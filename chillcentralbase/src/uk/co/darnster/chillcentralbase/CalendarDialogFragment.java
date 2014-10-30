package uk.co.darnster.chillcentralbase;

import uk.co.darnster.chillcentralbase.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/*
 * Still need to integrate this code with add/edit activity - Done, surely?
 * 
 * 12/7/13 String abstraction complete
 * 2/8/13 - Dlog added
 */

public class CalendarDialogFragment extends DialogFragment {
	public CharSequence[] calendars;
	public int selection;
	public String operation;
	


	  public static CalendarDialogFragment newInstance( CharSequence[] calendars,int selection, String operation ) {
		  CalendarDialogFragment frag = new CalendarDialogFragment();
	        Bundle args = new Bundle();
	        args.putString("operation", operation);
	        args.putCharSequenceArray("calendars", calendars);
	     // set here to capture what was selected previously!!
	        args.putInt("selection", selection);
	        frag.setArguments(args);
	        
	               
	        return frag;
	    }
	  	// .setMultiChoiceItems(groups, 
		//selections,
        //new DialogSelectionClickHandler())
	    @Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	    	super.onCreateDialog(savedInstanceState);
	    	//Integer theme =  R.style.MyDialog;
	        calendars = getArguments().getCharSequenceArray("calendars");
	        selection = getArguments().getInt("selection");
	        operation = getArguments().getString("operation");

	        return new AlertDialog.Builder(getActivity())
	                .setIcon(R.drawable.ic_launcher)
	                .setTitle(R.string.choose_calendar)
	                .setSingleChoiceItems(calendars, selection, new DialogSelectionClickListener())
	                .setPositiveButton(R.string.OK,
	                        new DialogInterface.OnClickListener() {
	                            public void onClick(DialogInterface dialog, int whichButton) {
	                            	
	                            	if (operation == "add") {
	                            		((AddRuleActivity) getActivity())
                                        .okClickedCalendar( selection );
	                            	}
	                            	else
	                            	{
	                            		((EditRuleActivity) getActivity())
                                        .okClickedCalendar( selection );
	                            	}

	                            }
	                        })

	        
			         .setNegativeButton(R.string.cancel, 
			        		 new DialogInterface.OnClickListener() { 
			        	 		public void onClick(DialogInterface dialog, int whichButton) {
			        	 			
			        	 			//*****
			        	 	if (operation == "add") {
			        	 				((AddRuleActivity) getActivity()) .cancelClickedCalendar();
			        	 			}
			        	 			else
			        	 			{
			        	 				((EditRuleActivity) getActivity()) .cancelClickedCalendar();
			        	 			}
			        	 	} })
			        	 				         
			         .create();
	    }

	    
	    // class below switched to correct listener - for single choice
	    public class DialogSelectionClickListener implements
	            DialogInterface.OnClickListener {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				selection = which;
				if (Dlog.getState()) {
					Dlog.i( this.getClass().toString() + " Calendar clicked position " + Integer.toString(which));					
				}
				
			}
	    }

	}