package vadim.potomac;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;

import vadim.playpotomac.R;
import vadim.potomac.model.PlayspotType;
import vadim.potomac.model.RiverData;
import vadim.potomac.util.HttpUtil;
import vadim.potomac.util.WeatherUtil;


	
class USGSTask extends AsyncTask<String, Void, RiverData> {
	// constants
	private static final String FARH = "°F";
	private static final String TAG = "PlayPotomac.USGSTask";
	private static final int USGS_LEVEL_ID= 69929; // time series ID for gauge level
	private static final int USGS_TEMP_ID = 69932; // time series ID for temperature
	
	private final WeakReference<CurrentConditionsFragment> fragmentWeakRef;
	private final Playspots m_playspots;
	
	USGSTask(CurrentConditionsFragment fragment,
			 Playspots playspots) {
		this.fragmentWeakRef = new WeakReference<>(fragment);
		m_playspots = playspots;
	}	

	@Override
	protected RiverData doInBackground(String... params) {
		String usgsUrl = params[0];
		try {
			return readFromUSGS (usgsUrl);
		} catch (Exception e) {
			Log.e (TAG, e.getMessage());
		}		
		return null;
	}
	
    @SuppressLint("SetTextI18n")
	protected void onPostExecute(RiverData usgsResponce) {
		try {
			super.onPostExecute(usgsResponce);
			CurrentConditionsFragment fragment = this.fragmentWeakRef.get();

			if (usgsResponce != null && fragment != null) {
				View topView = fragment.getView();
				if (topView == null) return; // extra precaution
				LinearLayout nowProgress = topView.findViewById(R.id.nowProgress);
			    if (nowProgress != null)
			      	nowProgress.setVisibility(View.GONE);
			   	TextView lvVw = topView.findViewById(R.id.level);
				float level = Float.valueOf(usgsResponce.getLevel());
			   	fragment.setCurrentLevel(level);
				((FragmentActivityCommunicator)fragment.getActivity()).onLevelLoaded ();
				lvVw.setText (usgsResponce.getLevel()); 		   	

			   	TextView waterTempVw = topView.findViewById(R.id.waterTemp);
				float icTemp = Float.parseFloat (usgsResponce.getWaterTemp().trim());
				float ifTemp = WeatherUtil.celsiusToFahrenheit(icTemp);
				waterTempVw.setText(String.valueOf((int)ifTemp)+FARH);
				
			    TextView oa = topView.findViewById(R.id.observedAt);
			    oa.setText(usgsResponce.getObservedAt());			
			
			   	TextView playspot = topView.findViewById(R.id.playspot);
				android.content.SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(fragment.getActivity());
				PlayspotType boatPreference = PlayspotType.get (prefs.getString(PlayspotType.PREFS, PlayspotType.All.toString()));
				playspot.setText (m_playspots.findBestPlayspot(level, boatPreference));   				
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
    }
	  
    private RiverData readFromUSGS (String usgsUrl) throws IOException {
    	InputStream stream = HttpUtil.readFromURL(usgsUrl);
	    BufferedReader br = new BufferedReader(new InputStreamReader(stream));
	    String line, observedAt = null, level = null, temperature = null;
	    
	    while ((line = br.readLine()) != null) {
	    	// skip lines starting with '#'
	    	if (line.startsWith("USGS")) {
    			String[] all = line.split(" |\t");
				if (all.length < 7) continue; // avoid malformed string in response, must have at least 7 columns to read
				int tsID=Integer.parseInt(all[2]); // based on time series to filter and process
				if (tsID == USGS_LEVEL_ID) { // level
	    			String[] time = all[5].split(":"); // process time to get rid of seconds
	    			observedAt = time[0] + ":" + time[1];
		    		level = all[7];
	    		} else if (tsID == USGS_TEMP_ID) { // capture the temperature
	    			temperature = all[7];
	    		}
	    	}	
	    }	
	
	    br.close();
	    return new RiverData (observedAt, level, temperature);
    }      
}

