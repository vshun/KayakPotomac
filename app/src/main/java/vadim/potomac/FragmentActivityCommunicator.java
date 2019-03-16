package vadim.potomac;

import java.util.List;

import vadim.potomac.model.ForecastWeather;
import vadim.potomac.model.NoaaData;

interface FragmentActivityCommunicator {
	  void onForecastLoaded(NoaaData noaaData);
      void onLevelLoaded();
      Playspots getPlayspots ();
      float getCurrentLevel ();
      List<ForecastWeather> getWeatherInfo ();
      NoaaData getNoaaData();
}
