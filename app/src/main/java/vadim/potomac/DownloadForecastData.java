package vadim.potomac;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import vadim.playpotomac.R;
import vadim.potomac.model.DailyAir;
import vadim.potomac.model.ForecastWeather;
import vadim.potomac.model.NoaaData;
import vadim.potomac.model.PlayspotType;
import vadim.potomac.model.RiverForecast;

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
	// convert InputStream to String
	private static String getStringFromInputStream(InputStream is) {

		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {

			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return sb.toString();

	}

	@Override
	protected NoaaData doInBackground(String... params) {
		NoaaData noaaData = null;
		try {
			// get parser given URL
			String resp = HttpUtil.readFromURLNew(params[0]);
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	    	XmlPullParser xpp = factory.newPullParser();
	    	xpp.setInput(new StringReader(resp));

	    	// create noaaData class given time preference
	    	Activity activity = fragmentWeakRef.get().getActivity();
	    	android.content.SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
			String timeRequested = prefs.getString("forecastPref", "12:00:00-00:00"); // if preferences not specified take noon
			noaaData = new NoaaData(timeRequested);

			// various initializations for the loop
			RiverForecast riverForecast = null;
	    	boolean inForecast = false, inObservation = false;

	    	// start parsing and get observation tags and forecast tags
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
		if (fragment == null || noaaData == null) return; // be cautious if fragment gets disposed by framework
		fragment.setNoaaData(noaaData);

		Activity activity = fragmentWeakRef.get().getActivity();
		List<ForecastWeather> fwl = ((FragmentActivityCommunicator)activity).getWeatherInfo();
		List<DailyAir> dal = populateDailyAirForecast (fwl);

		View rootView = fragment.getView();
		if (noaaData != null && rootView != null) try {
			LinearLayout nowProgress = rootView.findViewById(R.id.noaaProgress);
			if (nowProgress != null)
				nowProgress.setVisibility(View.GONE);

			// populate forecast
			populateForecastTableRow(rootView, noaaData, dal, 0, R.id.DayofWeek0, R.id.Level0, R.id.Trend0, R.id.Playspot0, R.id.Temp0);
			populateForecastTableRow(rootView, noaaData, dal, 1, R.id.DayofWeek1, R.id.Level1, R.id.Trend1, R.id.Playspot1, R.id.Temp1);
			populateForecastTableRow(rootView, noaaData, dal, 2, R.id.DayofWeek2, R.id.Level2, R.id.Trend2, R.id.Playspot2, R.id.Temp2);
			populateForecastTableRow(rootView, noaaData, dal, 3, R.id.DayofWeek3, R.id.Level3, R.id.Trend3, R.id.Playspot3, R.id.Temp3);
			// populate observations
			populateObservedRow(rootView, R.id.lastLevel, noaaData.getObservedNow(), R.id.change0Hr, noaaData.getObservedNow());
			populateObservedRow(rootView, R.id.level6Hr, noaaData.getObserved6Hr(), R.id.change6Hr, noaaData.getObservedNow());
			populateObservedRow(rootView, R.id.level12Hr, noaaData.getObserved12Hr(), R.id.change12Hr, noaaData.getObservedNow());
			populateObservedRow(rootView, R.id.level24Hr, noaaData.getObserved24Hr(), R.id.change24Hr, noaaData.getObservedNow());
		} catch (ParseException e) {
			String error = e.getMessage();
			if (error != null)
				Log.e(TAG, error);
		}
 	}

    private void populateObservedRow (View parent, int levelId, String observedLevel, int changeId, String currentLevel) {
	  	if (observedLevel != null) {
		  	TextView level = parent.findViewById(levelId);
		  	level.setText (observedLevel);
		  	
		  	if (currentLevel != null) {
			  	TextView change = parent.findViewById(changeId);
			  	BigDecimal now = new BigDecimal (currentLevel);
			  	BigDecimal before = new BigDecimal(observedLevel);
			  	BigDecimal result = now.subtract(before);
			  	change.setText (result.toPlainString());	
		  	} 	
	  	}			  	
    }

 	private void populateForecastTableRow (View parent, NoaaData noaaData, List<DailyAir> dal,
										   int row, int dayResource, int levelResource,
										   int trendResource, int playspotResource, int tempResource) throws ParseException {

 		ArrayList<RiverForecast> colRf = noaaData.getForecast();
	   	if (colRf.size() > row) {
	   		RiverForecast rf = colRf.get(row);
	   		String sLevel = rf.getLevel();
	   		if (sLevel != null) {
	   			Activity activity = fragmentWeakRef.get().getActivity();

	   			String dayOfWeek = WeatherUtil.dayOfTheWeek(rf.getDate());
	   	  		TextView dw0 = parent.findViewById(dayResource);
			   	dw0.setText (dayOfWeek);   

	   			TextView l = parent.findViewById(levelResource);
	   			l.setText(sLevel);
	   				 
	   			TextView t = parent.findViewById(trendResource);
	   			float trendedLevel = noaaData.getTrendingLevel (rf.getDate());
	   			t.setText(String.valueOf(trendedLevel));		   			
	   			
				android.content.SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
				PlayspotType boatPreference = PlayspotType.get (
						prefs.getString(PlayspotType.PREFS, PlayspotType.All.toString()));
				
	   			TextView p = parent.findViewById(playspotResource);
	   			float level =  Float.valueOf(sLevel);

	   			Playspots playspots = ((FragmentActivityCommunicator)activity).getPlayspots();
	   			p.setText(playspots.findBestPlayspot(level, boatPreference));

				DailyAir da = findMatchingDateDa(dal, rf.getDate());
				if (da != null) {
					TextView temp = parent.findViewById(tempResource);
					temp.setText(da.getHi() + "/" + da.getLow());
				}
	   		}
	   	}  	
 	}
 	
  	private List<DailyAir> populateDailyAirForecast (List<ForecastWeather> fwl) {
		List<DailyAir> dal = new ArrayList<>();
		if (fwl != null) {
			DailyAir da = new DailyAir();

			for (ForecastWeather fw : fwl) {
				if (fw.isDayTime()) {
					da.setHi(fw.getWindchill());
				} else { // night time - wrap it up and add the element to the array
					da.setLow(fw.getWindchill());
					da.setDate(fw.getDate());
					dal.add(da);
					da = new DailyAir(); // create new instance for next loop iteration
				}
			}
		}
		return dal;
	}
	
	private DailyAir findMatchingDateDa (List<DailyAir> dal, String date) {
    	try {
			for (DailyAir da : dal) {
				if (WeatherUtil.matchDate(da.getDate(), date)) return da;
			}
		} catch (Exception e) { // log it and return null, do not display temperature for that forecast
			Log.e(TAG, e.getMessage());
		}
		return null; // not found
	}
}
