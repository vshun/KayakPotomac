package vadim.potomac.model;

public class ForecastWeather {
	private final long id;
	private final String day;
	private final boolean isDayTime;
	private final long temperature;
	private final String windSpeed;
	private final String shortForecast;

	public ForecastWeather (long id, String day, boolean isDayTime, long temperature,
							String windSpeed, String shortForecast) {
		this.id = id;
		this.day = day;
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

	public boolean isDayTime() {
		return isDayTime;
	}

	public long getTemperature() {
		return temperature;
	}

	public String getWindSpeed() {
		return windSpeed;
	}

	public String getShortForecast() {
		return shortForecast;
	}
}
