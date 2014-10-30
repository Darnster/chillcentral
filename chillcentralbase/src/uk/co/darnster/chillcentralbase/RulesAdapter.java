package uk.co.darnster.chillcentralbase;

/*
 * 2/8/13 - Dlog added
 * 
 * 
 */

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.app.Activity;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class RulesAdapter extends ArrayAdapter<Rules> {
	
	Context context; 
    int layoutResourceId;    
    private ArrayList<Rules> rules_data;
	
	public RulesAdapter(Context context, int layoutResourceId, ArrayList<Rules> the_rules) {
		
        super(context, layoutResourceId, the_rules);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.rules_data = the_rules;		
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        RulesHolder holder = null;
        
        if(row == null)
        {
   
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            
            holder = new RulesHolder();
           
            holder.group = (TextView)row.findViewById(R.id.rule_group);
            holder.calendar = (TextView)row.findViewById(R.id.rule_calendar);
            holder.tag = (TextView)row.findViewById(R.id.tag_text);
            
            row.setTag(holder);
        }
        else
        {
        	holder = (RulesHolder)row.getTag();
        }
        
        Rules rule = rules_data.get(position);
        if (Dlog.getState()) {
        	Dlog.i(this.getClass().toString() + " RulesAdapter - position " + Integer.toString(position) );
        }
        
        //holder.position = rules_data.get(position);
        holder.tag.setText(rule.Tag);
        if (Dlog.getState()) {
        	Dlog.i(this.getClass().toString() + " RulesAdapter - rule.Tag " + rule.Tag);
        }
        holder.group.setText(rule.Group);
        holder.calendar.setText(rule.Calendar);
        
        return row;
    }
    
    static class RulesHolder
    {
    	TextView tag;
        TextView group;
        TextView calendar;
    }
}
	

