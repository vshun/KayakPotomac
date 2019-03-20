package vadim.potomac.model;

import vadim.potomac.util.WeatherUtil;

public class ForecastWeather {
	private final long id;
	private final String day;
	private final String date;
	private final boolean isDayTime;
	private final long temperature;
	private final String windSpeed;
	private final String shortForecast;

	public ForecastWeather (long id, String day, String date, boolean isDayTime, long temperature,
							String windSpeed, String shortForecast) {
		this.id = id;
		this.day = day;
		this.date = date;
		this.isDayTime = isDayTime;
		this.temperature = temperature;
		this.windSpeed = windSpeed;
		this.shortForecast = shortForecast;
	}

	public long getId() {
		return id;
	}

	public String getDay() {
		return day;
	}

	public String getDate() {
		return date;
	}

	public boolean isDayTime() {
		return isDayTime;
	}

	public long getTemperature() {
		return temperature;
	}

	public String getWindSpeed() {
		return windSpeed;
	}

	public long getWindchill () {
		return WeatherUtil.windChill(temperature, windSpeed);
	}

	public static final int maxLength = 15;

	public String getShortForecast() {
		if (shortForecast.length() <= maxLength)
			return shortForecast;
		else
			return shortForecast.substring(0, maxLength-2) + "..";
	}
}
