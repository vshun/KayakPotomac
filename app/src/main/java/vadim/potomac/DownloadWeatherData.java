package vadim.potomac;
	
import java.io.InputStream;
import java.lang.ref.WeakReference;


	
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import vadim.potomac.model.CurrentWeather;
import vadim.potomac.model.ForecastWeather;
import vadim.potomac.model.WeatherInfo;
import vadim.potomac.util.HttpUtil;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
	

class DownloadWeatherData extends AsyncTask<String, Void, WeatherInfo> {
	// constants
	private static final String FARH = "°F";	
	private static final String UNITS = "mph";
	private static final String TAG = "PlayPotomac.Weather";	
	
	private final WeakReference<CurrentConditionsFragment> fragmentWeakRef;
	
	DownloadWeatherData (CurrentConditionsFragment fragment)  {
		this.fragmentWeakRef = new WeakReference<>(fragment);
	}
	
	@Override
	protected WeatherInfo doInBackground(String... params) {
		try {
			String weatherUrl = params[0];
	    	InputStream stream = HttpUtil.readFromURL(weatherUrl);

	    	XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	    	XmlPullParser xpp = factory.newPullParser();
	    	xpp.setInput(stream, null);
	 
	        WeatherInfo weatherInfo = new WeatherInfo();
	    	CurrentWeather currentWeather = new CurrentWeather ();
	    	weatherInfo.setCurrentWeather(currentWeather);
	 
	        for (int eventType = xpp.getEventType();eventType != XmlPullParser.END_DOCUMENT; eventType = xpp.next()) {
	          if((eventType == XmlPullParser.START_TAG)) {
	        		  String name = xpp.getName();
	        		  if (name.equals("yweather:wind")) {
	        			  currentWeather.setWindchill (xpp.getAttributeValue(null, "chill"));  
	        			  currentWeather.setWind (xpp.getAttributeValue(null, "speed"));  
	   	        	  } 
	        		  if (name.equals("yweather:atmosphere")) 
	   	        		  currentWeather.setHumidity(xpp.getAttributeValue(null, "humidity"));
	        		  
	        		  if (name.equals("yweather:astronomy")) {
	        			  currentWeather.setSunrise(xpp.getAttributeValue(null, "sunrise"));
	        			  currentWeather.setSunset(xpp.getAttributeValue(null, "sunset"));
	        		  }	  
	        		  if (name.equals ("yweather:condition")) {
	        			  currentWeather.setCondition(xpp.getAttributeValue(null,"text"));
	        			  currentWeather.setTemp(xpp.getAttributeValue(null,"temp"));	        			  
	        		  }	  
	        		  // populate forecast
	        		  if (name.equals ("yweather:forecast")) {
	        		    	ForecastWeather forecast = new ForecastWeather();
	        		    	forecast.setDayOfWeek (xpp.getAttributeValue(null,"day"));
	        		    	forecast.setLow(xpp.getAttributeValue(null,"low"));    	
	        		    	forecast.setHigh(xpp.getAttributeValue(null,"high"));
	        		    	weatherInfo.addForecastData(forecast);
	        		  } 	  
	        	  }
	          }       
	        return weatherInfo;
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return null;
	}
	
    @SuppressLint("SetTextI18n")
	protected void onPostExecute(WeatherInfo weatherInfo) {
		try {
			super.onPostExecute(weatherInfo);
			CurrentConditionsFragment fragment = this.fragmentWeakRef.get();
			if (fragment == null) return; // be cautious if fragment gets dropped
			fragment.setWeatherInfo(weatherInfo);
			View rootView = fragment.getView();
			if (weatherInfo != null && rootView != null) {
			   	TextView at = (TextView)rootView.findViewById(R.id.airTemp);
				at.setText (weatherInfo.getCurrentWeather().getTemp()+FARH);    
				
			   	TextView condition = (TextView)rootView.findViewById(R.id.condition);
			   	condition.setText (weatherInfo.getCurrentWeather().getCondition());
	
			   	TextView wind = (TextView)rootView.findViewById(R.id.wind);
			   	wind.setText (weatherInfo.getCurrentWeather().getWind()+UNITS);
			   	
			   	TextView windchill = (TextView)rootView.findViewById(R.id.windchill);
			   	windchill.setText (weatherInfo.getCurrentWeather().getWindchill()+FARH);
			   	
			   	TextView humidity = (TextView)rootView.findViewById(R.id.humidity);
			   	humidity.setText (weatherInfo.getCurrentWeather().getHumidity()+"%");			   	

		   		TextView sunrise = (TextView)rootView.findViewById(R.id.sunrise);
		   		sunrise.setText(weatherInfo.getCurrentWeather().getSunrise());
			   	
		   		TextView sunset = (TextView)rootView.findViewById(R.id.sunset);
		   		sunset.setText(weatherInfo.getCurrentWeather().getSunset());
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
    }
}
