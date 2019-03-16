package vadim.potomac.model;

import java.util.LinkedList;
import java.util.List;

// class contains times for lows and highs for tides on today date for DC Potomac
public class TideInfo {
	private final List<String> lowTideReadings = new LinkedList<>();
	private final List<String> highTideReadings = new LinkedList<>();

	public void addReading (String time, String type) {
		if (type.equals("L"))
			lowTideReadings.add(time);
		else // must be high tides
			highTideReadings.add(time);
	}

	public List<String> getLows () {
		return lowTideReadings;
	}
	public List<String> getHighs () {
		return highTideReadings;
	}
}
