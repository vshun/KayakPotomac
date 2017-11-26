package vadim.potomac;


import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import vadim.potomac.model.ForecastWeather;
import vadim.potomac.model.NoaaData;
import vadim.potomac.model.PlayspotType;
import vadim.potomac.model.RiverForecast;
import vadim.potomac.model.WeatherInfo;

import vadim.potomac.util.HttpUtil;
import vadim.potomac.util.WeatherUtil;
import android.app.Activity;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;


@SuppressWarnings("ConstantConditions")
class DownloadForecastData extends AsyncTask<String, Void, NoaaData> {
	private static final String TAG = "PlayPotomac.Forecast";	
	
	private final WeakReference<ForecastConditionsFragment> fragmentWeakRef;

	// Container Activity must implement this interface
    DownloadForecastData(ForecastConditionsFragment fragment)  {
		this.fragmentWeakRef = new WeakReference<>(fragment);
	}
	
	@Override
	protected NoaaData doInBackground(String... params) {
		NoaaData noaaData = null;
		try {
	       	InputStream stream = HttpUtil.readFromURL(params[0]);  

	      	XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	    	XmlPullParser xpp = factory.newPullParser();
	    	xpp.setInput(stream, null);

	    	Activity activity = fragmentWeakRef.get().getActivity();
	    	android.content.SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
			String timeRequested = prefs.getString("forecastPref", "12:00:00-00:00"); // if preferences not specified take noon

	    	noaaData = new NoaaData(timeRequested);
	    	RiverForecast riverForecast = null;
	    	boolean inForecast = false, inObservation = false;
	 
	        for (int eventType = xpp.getEventType();eventType != XmlPullParser.END_DOCUMENT; eventType = xpp.next()) {
	          if((eventType == XmlPullParser.START_TAG)) {
	        	  String tagName = xpp.getName();
	        	  if (tagName.equals("forecast"))
	        		  inForecast = true;
	        	  else if (tagName.equals("observed"))
	        		  inObservation = true;
	        	  if ((inForecast || inObservation) && tagName.equals ("datum"))
	        		  riverForecast = new RiverForecast ();
	        	 
	        	  if (riverForecast != null) {
	        		  if (tagName.equals("valid")) {
	        			  xpp.next();
	        			  riverForecast.setDate (xpp.getText());
	        		  } if (tagName.equals("primary")) { 
	        			  xpp.next();  
	        			  riverForecast.setLevel(xpp.getText());
	        		  }	  
	        	   } 
	          }       
	          else if(eventType == XmlPullParser.END_TAG) {
	    	  	  if ((riverForecast!=null) && (xpp.getName().equals("datum")))  {
	    	  		if (inForecast)  
	    	  			noaaData.addForecast(riverForecast);
	    	  		else if (inObservation)
	    	  			noaaData.addObservation(riverForecast);
	    	  		riverForecast = null;    	  	
	    	  	  }
	    	  	  if (xpp.getName().equals("forecast")) inForecast = false;
	    	  	  else if (xpp.getName().equals("observed")) inObservation = false;
	           }      
	        }  
	        noaaData.deflate ();
	        ((FragmentActivityCommunicator)activity).onForecastLoaded (noaaData);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}	   
		return noaaData;
	} 	    



    protected void onPostExecute(NoaaData noaaData) {
    
    	ForecastConditionsFragment fragment = this.fragmentWeakRef.get();
		if (fragment == null) return; // be cautious if fragment gets disposed by framework
		fragment.setNoaaData(noaaData);
		View rootView = fragment.getView();
		if (noaaData != null && rootView != null) {	try {
			LinearLayout nowProgress = (LinearLayout) rootView.findViewById(R.id.noaaProgress);
		    if (nowProgress != null)
		      	nowProgress.setVisibility(View.GONE);
	   		// populate forecast
   		  	populateForecastTableRow (rootView, noaaData, 0, R.id.DayofWeek0, R.id.Level0, R.id.Trend0, R.id.Playspot0, R.id.Temp0);	
		  	populateForecastTableRow (rootView, noaaData, 1, R.id.DayofWeek1, R.id.Level1, R.id.Trend1, R.id.Playspot1, R.id.Temp1);	
		  	populateForecastTableRow (rootView, noaaData, 2, R.id.DayofWeek2, R.id.Level2, R.id.Trend2, R.id.Playspot2, R.id.Temp2);	
		  	populateForecastTableRow (rootView, noaaData, 3, R.id.DayofWeek3, R.id.Level3, R.id.Trend3, R.id.Playspot3, R.id.Temp3);	
		  	// populate observations
		  	populateObservedRow (rootView, R.id.lastLevel, noaaData.getObservedNow(), R.id.change0Hr, noaaData.getObservedNow());
		  	populateObservedRow (rootView, R.id.level6Hr, noaaData.getObserved6Hr(), R.id.change6Hr, noaaData.getObservedNow());
		  	populateObservedRow (rootView, R.id.level12Hr, noaaData.getObserved12Hr(), R.id.change12Hr, noaaData.getObservedNow());
		  	populateObservedRow (rootView, R.id.level24Hr, noaaData.getObserved24Hr(), R.id.change24Hr, noaaData.getObservedNow());
		} catch (Exception e) {
			String error = e.getMessage();
			if (error != null)
				Log.e(TAG, error);
		}}
 	}

    private void populateObservedRow (View parent, int levelId, String observedLevel, int changeId, String currentLevel) {
	  	if (observedLevel != null) {
		  	TextView level = (TextView)parent.findViewById(levelId);
		  	level.setText (observedLevel);
		  	
		  	if (currentLevel != null) {
			  	TextView change = (TextView)parent.findViewById(changeId);
			  	BigDecimal now = new BigDecimal (currentLevel);
			  	BigDecimal before = new BigDecimal(observedLevel);
			  	BigDecimal result = now.subtract(before);
			  	change.setText (result.toPlainString());	
		  	} 	
	  	}			  	
    }

 	private void populateForecastTableRow (View parent, NoaaData noaaData, int row, 
 			int dayResource, int levelResource, int trendResource, 
 			int playspotResource, int tempResource) throws ParseException {

 		ArrayList<RiverForecast> colRf = noaaData.getForecast();
	   	if (colRf.size() > row) {
	   		RiverForecast rf = colRf.get(row);
	   		String sLevel = rf.getLevel();
	   		if (sLevel != null) {
	   			Activity activity = fragmentWeakRef.get().getActivity();
	   			WeatherInfo weatherInfo = ((FragmentActivityCommunicator)activity).getWeatherInfo();	

	   			String dayOfWeek = WeatherUtil.dayOfTheWeek(rf.getDate());
	   	  		TextView dw0 = (TextView)parent.findViewById(dayResource);
			   	dw0.setText (dayOfWeek);   
			   	
	   			TextView l = (TextView)parent.findViewById(levelResource);
	   			l.setText(sLevel);
	   				 
	   			TextView t = (TextView)parent.findViewById(trendResource);
	   			float trendedLevel = noaaData.getTrendingLevel (rf.getDate());
	   			t.setText(String.valueOf(trendedLevel));		   			
	   			
				android.content.SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
				PlayspotType boatPreference = PlayspotType.get (
						prefs.getString(PlayspotType.PREFS, PlayspotType.All.toString()));
				
	   			TextView p = (TextView)parent.findViewById(playspotResource);
	   			float level =  Float.valueOf(sLevel);

	   			Playspots playspots = ((FragmentActivityCommunicator)activity).getPlayspots();
	   			p.setText(playspots.findBestPlayspot(level, boatPreference));
	   		
	   			TextView temp = (TextView)parent.findViewById(tempResource);
	   			ForecastWeather fw = weatherInfo.getForecast(dayOfWeek);
	   			if (fw != null) 
	   				temp.setText(fw.getLow()+"/"+fw.getHigh());		   			
	   		}
	   	}  	
 	}
}