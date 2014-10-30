package uk.co.darnster.chillcentral;

/*
* 15/7/13 - Externlised strings - needs merging with code base and testing (may need a context for getString calls)
*
*
*/

import uk.co.darnster.chillcentral.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.DialogFragment;

public class DeleteRuleDialogFragment extends DialogFragment {

	public static DeleteRuleDialogFragment newInstance(String caller) {
		DeleteRuleDialogFragment frag = new DeleteRuleDialogFragment();
		Bundle args = new Bundle();
		args.putString("caller", caller);
		frag.setArguments(args);
 	    return frag;
	}
	
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreateDialog(savedInstanceState);
    	//Integer theme =  R.style.MyDialog;
		final String caller = getArguments().getString("caller");
        return new AlertDialog.Builder(getActivity())
        .setIcon(R.drawable.ic_launcher)
        .setTitle(getString(R.string.app_name) + " - " + getString(R.string.rule_delete) )
        .setMessage(R.string.rule_delete_question_mark).setPositiveButton(R.string.OK,new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	if (caller == "editRuleActivity") {
                    		((EditRuleActivity) getActivity())
                            .deleteClicked();
                    	
                    	}
                    	else
                    		((MainActivity) getActivity())
                            .deleteClicked();
                    }
                })
                .setNegativeButton(R.string.cancel, null).create();
    }

}
