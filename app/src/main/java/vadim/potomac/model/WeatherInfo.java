package vadim.potomac.model;

import java.util.ArrayList;

public class WeatherInfo {
	private CurrentWeather currentWeather;
	private final ArrayList<ForecastWeather> forecastData = new ArrayList<>();
	
	public void setCurrentWeather (CurrentWeather currentWeather) {
		this.currentWeather = currentWeather;
	}

	public CurrentWeather getCurrentWeather() {
		return currentWeather;
	}

	public void addForecastData(ForecastWeather forecastData) {
		this.forecastData.add(forecastData);
	}

	public ForecastWeather getForecast (String dayOfWeek) {
		for (ForecastWeather fw : forecastData) {
			if (fw.getDayOfWeek().equals(dayOfWeek))
				return fw;
		}
		return null;
	}
}
