package uk.co.darnster.chillcentral;


/*
 * This Fragment serves Add and Edit functions.
 * It uses a String called "operation" passed in to the newInstance() method to allow control to be passed back
 * to the correct calling Activity class 
 * 
 * 16/7/13 - Strings abstracted
 * 2/8/13 - Dlog added
 */

import java.util.HashMap;

import uk.co.darnster.chillcentral.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class GroupDialogFragment extends DialogFragment {
	public CharSequence[] groups;
	public boolean[] selections;
	// Bool array below added to cater for when user presses cancel after selecting/de-selecting groups
	public static boolean[] selectionsToRestoreIfCancelled;
	public static HashMap<String, String> groupSelectIfCancelled;
	
	public static HashMap<String, String> groupSelectHash = new HashMap <String, String>();

	  public static GroupDialogFragment newInstance( CharSequence[] groups, boolean[] selections, HashMap<String, String> selectedGroups, String operation ) {
		  	GroupDialogFragment frag = new GroupDialogFragment();
		  	selectionsToRestoreIfCancelled = selections;
		  	//Thank you "http://stackoverflow.com/questions/5785745/make-copy-of-array-java"		  	
		  	selectionsToRestoreIfCancelled = (boolean[])selections.clone();
		  	groupSelectIfCancelled = ( HashMap<String, String> )selectedGroups.clone();
		  	Bundle args = new Bundle();
	        args.putString("operation", operation);
	        args.putCharSequenceArray("groups", groups);
	        args.putBooleanArray("selections", selections);
	        frag.setArguments(args);
	        // set here to capture what was selected previously!!
	        groupSelectHash = selectedGroups;
	               
	        return frag;
	    }

	    @Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	    	super.onCreateDialog(savedInstanceState);
  	        groups = getArguments().getCharSequenceArray("groups");
	        selections = getArguments().getBooleanArray("selections");
	        if (Dlog.getState()) {
	        	for (int i = 0; i < selections.length; i++) {
	        		Dlog.i( this.getClass().toString() + " Selections Dialog Init: " + selections[i]);
	        	}
	        }
	        final String operation = getArguments().getString("operation");
	        return new AlertDialog.Builder(getActivity())  //,theme
	                .setIcon(R.drawable.ic_launcher)
	                .setTitle(R.string.chooseGroups)
	                .setMultiChoiceItems(groups, 
	                		selections,
	                        new DialogSelectionClickHandler())
	                .setPositiveButton(R.string.OK,
	                        new DialogInterface.OnClickListener() {
	                            public void onClick(DialogInterface dialog, int whichButton) {
	                            	if (operation == "add") {
	                            		((AddRuleActivity) getActivity())
	                                        .okClicked( groupSelectHash );
	                            	}
	                            	else
	                            	{
	                            		((EditRuleActivity) getActivity())
                                        .okClicked( groupSelectHash );
	                            	}
	                            	
	                            }
	                        })

	        
			         .setNegativeButton(R.string.cancel, 
			        		 new DialogInterface.OnClickListener() { 
			        	 		public void onClick(DialogInterface dialog, int whichButton) {
			        	 			if (operation == "add") {
			        	 				((AddRuleActivity) getActivity()) .cancelClicked( selectionsToRestoreIfCancelled, groupSelectIfCancelled );
			        	 			}
			        	 			else
			        	 			{
			        	 				((EditRuleActivity) getActivity()) .cancelClicked( selectionsToRestoreIfCancelled, groupSelectIfCancelled );
			        	 			}
			        	 			} })
			         .create();
	        
	        
	          
	    }

	    
	    
	    public class DialogSelectionClickHandler implements
	            DialogInterface.OnMultiChoiceClickListener {
	        public void onClick(DialogInterface dialog, int clicked,
	                boolean selected) {
				if (selected) {
					// write to a hashmap
					groupSelectHash.put(groups[clicked].toString(), "");
				}
				else
				{
					// remove from hashmap
					groupSelectHash.remove(groups[clicked].toString());
				}
				if (Dlog.getState()) {
					Dlog.i( this.getClass().toString() + " groupSelectHash" + groupSelectHash.toString());
				}
	        }
	    }

	}