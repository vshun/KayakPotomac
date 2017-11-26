package vadim.potomac.model;

import java.text.DateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class NoaaData {
	
	private String observedNow;
	private String observed6Hr;
	private String observed12Hr;
	private String observed24Hr;
	
	private ArrayList<RiverForecast> noaaForecast;
	private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

	private static final Date now = new Date();
	private final String mTimeRequested;
	
	public NoaaData  (String timeRequested) {
		noaaForecast = new ArrayList<>();
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		mTimeRequested = timeRequested;
	}
	
	public void addObservation (RiverForecast rf) throws ParseException {

		if (getObservedNow() != null && getObserved6Hr()!= null && getObserved12Hr() != null && getObserved24Hr() != null) return;
		Date time = df.parse(rf.getDate());
		int hoursDiff = Math.abs(hoursDiff (now, time));
		if (hoursDiff == 0 || (getObservedNow() == null && hoursDiff == 1))
			observedNow = rf.getLevel();
		else if (observed6Hr == null && hoursDiff == 6)
			observed6Hr = rf.getLevel();
		else if (observed12Hr == null && hoursDiff == 12)
			observed12Hr = rf.getLevel();
		else if (observed24Hr == null && hoursDiff == 24)
			observed24Hr = rf.getLevel();	
	}
	
	private static final long MILLISECONDS_IN_HOUR = 1000*60*60;

	private static int hoursDiff(Date d1, Date d2) {
			double diff = d2.getTime() - d1.getTime();
			return (int) (diff / MILLISECONDS_IN_HOUR);
		}
	
	public void addForecast (RiverForecast rf) {
		noaaForecast.add (rf);
	}
	
	public void deflate () {
    	
	    	ArrayList<RiverForecast> collapsedForecast = new ArrayList<>();

	    	// NOAA raw forecast can start with a) old dates prior to today, or b) today or c) tomorrow 
	    	// first set to ignore old forecasts
	    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
	    	sdf.setTimeZone (TimeZone.getDefault());
	    	String todayDate = sdf.format(new Date ());
	    	int counter;
	    	for (counter = 0; counter < noaaForecast.size(); counter++) {
	    		RiverForecast rawForecast = noaaForecast.get(counter);
		    	String[] firstTime = rawForecast.getDate().split ("T");
	 	    	String firstDate = firstTime[0];
	 	    	if (firstDate.equalsIgnoreCase(todayDate)) {
	 	    		RiverForecast rf = new RiverForecast ();
	 	    		rf.setDate(firstDate);
	 	    		rf.setLevel(rawForecast.getLevel());
	 	 	    	collapsedForecast.add (rf);
	 	    		break;
	 	    	}
	    	}
	    	if (collapsedForecast.size() == 0)  // forecast does not contain today
	    		counter = -1; // reset so future forecasts could be added at correct point
	    	// next loop will capture forecasts starting from tomorrow
	    	for (int i=counter+1; i<noaaForecast.size(); i++ ) {
	    	  	RiverForecast rawForecast = noaaForecast.get(i);
		    	String[] current = rawForecast.getDate().split ("T");
		    	String date = current[0];
		    	String time = current[1];
	        	if (date.equals(todayDate)) continue; // today date already captured in the first loop
	        	if (time.startsWith(mTimeRequested) ) {
		 	    	RiverForecast rf = new RiverForecast ();
		 	    	rf.setDate(date);
		 	    	rf.setLevel(rawForecast.getLevel());
		 	 	   	collapsedForecast.add (rf);   	
	        	}
	    	}
	    	noaaForecast = collapsedForecast;
	    }	
	
		public ArrayList<RiverForecast> getForecast() {
			return noaaForecast;
		}

		public String getObservedNow() {
			return observedNow;
		}

		public String getObserved6Hr() {
			return observed6Hr;
		}

		public String getObserved12Hr() {
			return observed12Hr;
		}

		public String getObserved24Hr() {
			return observed24Hr;
		}
		
		private float getHourlyChange() {
			if (observedNow == null || observed6Hr == null) return 0;
			return (Float.parseFloat(observedNow)-Float.parseFloat(observed6Hr))/6;
		}
		
		public float getTrendingLevel (String date) throws ParseException {
			Date myDate;
			myDate = sdf.parse(date);

			long hoursDiff = hoursDiff(now, myDate)+12; // target noon forecast
			if (hoursDiff<0) hoursDiff = 0;

			if (observedNow == null) return 0; // if data is missing
			float result = Float.parseFloat (observedNow) + getHourlyChange()*hoursDiff ;
			result = Math.round(result*100.0)/100.0f;
			return result;
		}
}
