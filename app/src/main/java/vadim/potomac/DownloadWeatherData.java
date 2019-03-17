package vadim.potomac;
	
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;


import vadim.playpotomac.R;
import vadim.potomac.model.ForecastWeather;
import vadim.potomac.util.HttpUtil;
import vadim.potomac.util.SunriseSunset;
import vadim.potomac.util.WeatherUtil;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
	

class DownloadWeatherData extends AsyncTask<String, Void, List<ForecastWeather>> {
	// constants
	private static final String FARH = "°F";	
	private static final String UNITS = "mph";
	private static final String TAG = "PlayPotomac.Weather";	
	
	private final WeakReference<CurrentConditionsFragment> fragmentWeakRef;
	
	DownloadWeatherData (CurrentConditionsFragment fragment)  {
		this.fragmentWeakRef = new WeakReference<>(fragment);
	}
	
	@Override
	protected List<ForecastWeather> doInBackground(String... params) {
					String weatherUrl = params[0];
		List<ForecastWeather> fwl = null;
		try {
			InputStream stream = HttpUtil.readFromURL(weatherUrl);
			fwl = readJsonStream(stream);
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
		return fwl;
	}

	private List<ForecastWeather> readJsonStream(InputStream in) throws IOException {
		try (JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"))) {
			try {
				return readForecastArray(reader);
			} finally {
				reader.close();
			}
		}
	}

	private List<ForecastWeather> readForecastArray(JsonReader reader) throws IOException {

		List<ForecastWeather> fw = null;
		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			if (name.equals("properties")) {
				fw = readProperties(reader);
			} else {
				reader.skipValue();
			}
		}
		reader.endObject();
		return fw;
	}

	private List<ForecastWeather> readProperties(JsonReader reader) throws IOException {

		List<ForecastWeather> fwl = new ArrayList<>();

		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			if (name.equals("periods")) {
				reader.beginArray();
				while (reader.hasNext()) {
					ForecastWeather fw = readForecast(reader);
					if (fw != null)
						fwl.add(fw);
				}
				reader.endArray();
			}  else {
				reader.skipValue();
			}
		}
		reader.endObject();
		return fwl;
	}

	private ForecastWeather readForecast(JsonReader reader) throws IOException {

		long id = 0, temperature = 0;
		boolean isDayTime = false;
		String windSpeed = null, shortForecast = null, day = null;
		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();

			switch (name) {
				case "number":
					id = reader.nextLong();
					break;
				case "name":
					day = reader.nextString();
					break;
				case "isDaytime":
					isDayTime = reader.nextBoolean();
					break;
				case "temperature":
					temperature = reader.nextLong();
					break;
				case "windSpeed":
					windSpeed = reader.nextString();
					break;
				case "shortForecast":
					shortForecast = reader.nextString();
					break;
				default:
					reader.skipValue();
					break;
			}
		}
		reader.endObject();
		return id != 0 ?
				new ForecastWeather (id, day, isDayTime, temperature, windSpeed, shortForecast) : null;
	}


	@SuppressLint("SetTextI18n")
	protected void onPostExecute(List<ForecastWeather> fwl) {
		try {
			super.onPostExecute(fwl);
			CurrentConditionsFragment fragment = this.fragmentWeakRef.get();
			if (fragment == null) return; // be cautious if fragment gets dropped
			fragment.setWeatherInfo(fwl);
			View rootView = fragment.getView();
			if (fwl != null && rootView != null ) {
				ForecastWeather fw = fwl.get(0);
				if (fw.getId() != 1) throw new Exception ("Cannot recognize weather");

				TextView at = rootView.findViewById(R.id.airTemp);
				at.setText (fw.getTemperature()+FARH);
				
			   	TextView condition = rootView.findViewById(R.id.condition);
			   	condition.setText (fw.getShortForecast());
	
			   	TextView wind = rootView.findViewById(R.id.wind);
			   	wind.setText (fw.getWindSpeed());

				TextView windchill = rootView.findViewById(R.id.windchill);
				windchill.setText (WeatherUtil.windChill(fw.getTemperature(), fw.getWindSpeed())+FARH);

				Calendar[] sunriseSunset = SunriseSunset.getCivilTwilight (
							new GregorianCalendar(), 39.0182, -77.2086);

		   		TextView sunrise = rootView.findViewById(R.id.sunrise);
		   		sunrise.setText(extractTime(sunriseSunset[0]));
			   	
		   		TextView sunset = rootView.findViewById(R.id.sunset);
				sunset.setText(extractTime(sunriseSunset[1]));
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
    }

    private static String extractTime (Calendar calendar) {
		return calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
	}
}
