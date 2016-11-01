package com.crossover.trial.weather.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.crossover.trial.weather.exception.WeatherException;
import com.crossover.trial.weather.pojo.AtmosphericInformation;
import com.crossover.trial.weather.pojo.DataPoint;
import com.crossover.trial.weather.pojo.DataPointType;

public class WeatherService {

	private static final int DAY_IN_MILLIS = 24 * 60 * 60 * 1000;
	/**
	 * atmospheric information for each airport, idx corresponds with
	 * airportData
	 */
	private static Map<String, AtmosphericInformation> atmosphericInformationMap = new HashMap<String, AtmosphericInformation>();
	private static Map<Double, Integer> radiusFreq = new HashMap<Double, Integer>();

	public static int calculateRadiusFrequency() {
		int datasize = 0;
		for (AtmosphericInformation ai : atmosphericInformationMap.values()) {
			// we only count recent readings and updated in the last day
			if (ai.hasAnyDataPointValue()
					&& ai.getLastUpdateTime() > getOneDayBeforeInMillis()) {
				datasize++;
			}
		}
		return datasize;
	}

	private static long getOneDayBeforeInMillis() {
		return System.currentTimeMillis() - DAY_IN_MILLIS;
	}

	public static void updateRadiusDataFrequency(Double radius) {
		radiusFreq.put(radius, radiusFreq.getOrDefault(radius, 0));
	}

	public static void init() {
		atmosphericInformationMap.clear();
	}

	public static AtmosphericInformation getAtmosphericInformation(String iata) {
		return atmosphericInformationMap.get(iata);
	}

	public static void removeAtmosphericInfo(String iata) {
		atmosphericInformationMap.remove(iata);
	}

	/**
	 * Update the airports weather data with the collected data.
	 *
	 * @param iataCode
	 *            the 3 letter IATA code
	 * @param pointType
	 *            the point type {@link DataPointType}
	 * @param dp
	 *            a datapoint object holding pointType data
	 *
	 * @throws WeatherException
	 *             if the update can not be completed
	 */
	public static void addDataPoint(String iataCode, String pointType,
			DataPoint dp) throws WeatherException {
		WeatherService.updateAtmosphericInformation(
				getAtmosphericInformation(iataCode), pointType, dp);
	}

	/**
	 * update atmospheric information with the given data point for the given
	 * point type
	 *
	 * @param ai
	 *            the atmospheric information object to update
	 * @param pointType
	 *            the data point type as a string
	 * @param dp
	 *            the actual data point
	 */
	public static void updateAtmosphericInformation(AtmosphericInformation ai,
			String pointType, DataPoint dp) throws WeatherException {

		switch (DataPointType.valueOf(pointType.toUpperCase())) {
		case WIND:
			updateWind(dp, ai);
			return;
		case TEMPERATURE:
			updateTemperature(dp, ai);
			return;
		case HUMIDTY:
			updateHumidity(dp, ai);
			return;
		case PRESSURE:
			updatePressure(dp, ai);
			return;
		case CLOUDCOVER:
			updateCloudOver(dp, ai);
			return;
		case PRECIPITATION:
			updatePrecipitation(dp, ai);
			return;
		default: {
			throw new IllegalStateException("couldn't update atmospheric data");
		}
		}

	}

	private static void updatePressure(DataPoint dp, AtmosphericInformation ai) {
		if (dp.getMean() >= 650 && dp.getMean() < 800) {
			ai.setPressure(dp);
			ai.setLastUpdateTime(System.currentTimeMillis());
		}
	}

	private static void updatePrecipitation(DataPoint dp,
			AtmosphericInformation ai) {
		if (dp.getMean() >= 0 && dp.getMean() < 100) {
			ai.setPrecipitation(dp);
			ai.setLastUpdateTime(System.currentTimeMillis());
		}
	}

	private static void updateCloudOver(DataPoint dp, AtmosphericInformation ai) {
		if (dp.getMean() >= 0 && dp.getMean() < 100) {
			ai.setCloudCover(dp);
			ai.setLastUpdateTime(System.currentTimeMillis());
		}
	}

	private static void updateHumidity(DataPoint dp, AtmosphericInformation ai) {
		if (dp.getMean() >= 0 && dp.getMean() < 100) {
			ai.setHumidity(dp);
			ai.setLastUpdateTime(System.currentTimeMillis());
		}
	}

	private static void updateTemperature(DataPoint dp,
			AtmosphericInformation ai) {
		if (dp.getMean() >= -50 && dp.getMean() < 100) {
			ai.setTemperature(dp);
			ai.setLastUpdateTime(System.currentTimeMillis());
		}
	}

	private static void updateWind(DataPoint dp, AtmosphericInformation ai) {
		if (dp.getMean() >= 0) {
			ai.setWind(dp);
			ai.setLastUpdateTime(System.currentTimeMillis());
		}
	}

	public static void addAtmosphericInformation(String iataCode,
			AtmosphericInformation atmosphericInformation) {
		atmosphericInformationMap.put(iataCode, atmosphericInformation);
	}

	public static int[] calculateDatasize() {
		int m = radiusFreq.keySet().stream().max(Double::compare)
				.orElse(1000.0).intValue() + 1;

		int[] hist = new int[m];
		for (Map.Entry<Double, Integer> e : radiusFreq.entrySet()) {
			int i = e.getKey().intValue() % 10;
			hist[i] += e.getValue();
		}
		return hist;
	}

}
