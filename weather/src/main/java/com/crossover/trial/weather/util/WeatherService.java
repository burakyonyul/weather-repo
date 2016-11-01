package com.crossover.trial.weather.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.crossover.trial.weather.pojo.AtmosphericInformation;

public class WeatherService {

	/**
	 * atmospheric information for each airport, idx corresponds with
	 * airportData
	 */
	public static List<AtmosphericInformation> atmosphericInformation = new LinkedList<AtmosphericInformation>();
	public static Map<Double, Integer> radiusFreq = new HashMap<Double, Integer>();

	public static int calculateRadiusFrequency() {
		int datasize = 0;
		for (AtmosphericInformation ai : atmosphericInformation) {
			// we only count recent readings
			if (ai.getCloudCover() != null || ai.getHumidity() != null
					|| ai.getPressure() != null
					|| ai.getPrecipitation() != null
					|| ai.getTemperature() != null || ai.getWind() != null) {
				// updated in the last day
				if (ai.getLastUpdateTime() > System.currentTimeMillis() - 86400000) {
					datasize++;
				}
			}
		}
		return datasize;
	}

	public static void updateRadiusDataFrequency(Double radius) {
		radiusFreq.put(radius, radiusFreq.getOrDefault(radius, 0));
	}

	public static AtmosphericInformation findAtmosphericInfo(int airportData) {
		return atmosphericInformation.get(airportData);
	}

	public static void init() {
		atmosphericInformation.clear();
	}

	public static void removeAtmosphericInfo(int airportDataIdx) {
		AtmosphericInformation atmosphericInfo = findAtmosphericInfo(airportDataIdx);
		atmosphericInformation.remove(atmosphericInfo);
	}

}
