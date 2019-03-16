package vadim.potomac;

import vadim.playpotomac.R;
import vadim.potomac.model.Playspot;
import vadim.potomac.model.PlayspotType;

import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class PlayspotsFragment extends Fragment {
	private static final String TAG = "PlayPotomac.Playspots";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.playspots, container, false);
        if (savedInstanceState != null) {
	       try {
				populatePlayspotsTab ();
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}	
        }   
	    return rootView;
    }

    public void refresh () {
      	View rootView = getView();
    	if (rootView == null) return; // not inflated yet
       	TableLayout table  = rootView.findViewById(R.id.playspotsData);
    	table.removeAllViews(); // clear table to account that this operation could be triggered by refresh or prefs changed
    }

    @SuppressLint("SetTextI18n")
	public void populatePlayspotsTab () {
		int HORIZONTAL_PAD = 5;
    	Activity parent = getActivity();
    	View rootView = getView();
    	if (parent == null || rootView == null) return;
    	Playspots playspots = ((FragmentActivityCommunicator)parent).getPlayspots();
    	if (playspots==null) return;
    	float currentLevel = ((FragmentActivityCommunicator)parent).getCurrentLevel();
    	TableLayout table  = rootView.findViewById(R.id.playspotsData);
    	android.content.SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(parent);
		PlayspotType boatPreference = PlayspotType.get (
			prefs.getString(PlayspotType.PREFS, PlayspotType.All.toString()));
		
		TableRow.LayoutParams params = new 
				TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT, 1f);
								
		for (Playspot ps:playspots.getPlayspots() ) {
			if (!ps.typeAcceptable(boatPreference)) continue;
    		TableRow tr = new TableRow(parent);
    		TextView nv = new TextView(parent);
    		nv.setLayoutParams(params);
    		nv.setText(ps.getName());
    		nv.setPadding(HORIZONTAL_PAD, 0, HORIZONTAL_PAD, 0);
    		
    		tr.addView(nv);
    		TextView minv = new TextView(parent);
    		minv.setText(Float.valueOf(ps.getMin()).toString());
    		minv.setLayoutParams(params);
    		minv.setPadding(HORIZONTAL_PAD, 0, HORIZONTAL_PAD, 0);
    		tr.addView(minv);	    	
    		
    		TextView maxv = new TextView(parent);
    		maxv.setText(Float.valueOf(ps.getMax()).toString());
    		maxv.setPadding(HORIZONTAL_PAD, 0, HORIZONTAL_PAD, 0);
    		maxv.setLayoutParams(params);
    		tr.addView(maxv);	    	
    		
    		TextView best = new TextView(parent);
    		best.setText(Float.valueOf(ps.getBest()).toString());
    		best.setPadding(HORIZONTAL_PAD, 0, HORIZONTAL_PAD, 0);
    		best.setLayoutParams(params);
    		tr.addView(best);
    		
    		TextView classv = new TextView(parent);
    		classv.setText(String.valueOf(ps.getClassification()));
    		classv.setPadding(HORIZONTAL_PAD, 0, HORIZONTAL_PAD, 0);
    		classv.setLayoutParams(params);
    		tr.addView(classv);
    		if ((currentLevel != 0.0f) && ps.isPlayable(currentLevel)) 
    			tr.setBackgroundColor(Color.CYAN); 
    		table.addView(tr);
    	}	
  	}   
}	
