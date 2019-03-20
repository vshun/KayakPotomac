package vadim.potomac.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.text.ParseException;
import java.text.SimpleDateFormat;


public 	class WeatherUtil {

    public static float celsiusToFahrenheit(float tCelsius) {
         return (9.0f / 5.0f) * tCelsius + 32;
    }

    private static final SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
	private static final SimpleDateFormat df = new SimpleDateFormat("E", Locale.getDefault());

	public static String dayOfTheWeek (String date) throws ParseException {
    	Date myDate  = f.parse(date);
    	return df.format(myDate);
    }

    public static boolean matchDate (String weatherDate, String noaaDate) throws ParseException {
        Date wDate = f.parse(weatherDate);
        Date nDate = f.parse(noaaDate);
        return wDate.compareTo(nDate) == 0;
    }

    public static long windChill (long t, String windSpeed) {
	    try {
            double v = deriveWindSpeed(windSpeed);
            return Math.round (35.74 + 0.6215*t + (0.4275*t - 35.75) * Math.pow(v, 0.16));
        } catch (RuntimeException e) { // something is off with return string
	        return t;
        }
    }

    // e.g. "5 to 15 mph" or "7 mph". If range returns max
    private static double deriveWindSpeed (String windSpeed) {
	    String[] splitWind = windSpeed.split("\\s+");
	    String strWind = splitWind [splitWind.length-2];
	    return Double.valueOf(strWind);
    }
}