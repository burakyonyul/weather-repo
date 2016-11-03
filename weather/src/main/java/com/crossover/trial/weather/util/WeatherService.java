package com.crossover.trial.weather.util;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.crossover.trial.weather.exception.WeatherException;
import com.crossover.trial.weather.pojo.AtmosphericInformation;
import com.crossover.trial.weather.pojo.DataPoint;
import com.crossover.trial.weather.pojo.DataPointType;

/**
 * 
 * This is the service class for weather related operations and containment of
 * atmospheric information and radius frequencies
 * 
 * @author burak
 *
 */
public class WeatherService {

	private static final String TYPE_MISMATCH_WARNING = "Couldn't update atmospheric data because of data point type mismatch with value: \"{0}\"";

	private static final int DAY_IN_MILLIS = 24 * 60 * 60 * 1000;

	private static final Logger logger = Logger.getLogger(WeatherService.class);

	/**
	 * atmospheric information for each airport, idx corresponds with
	 * airportData
	 */
	private static Map<String, AtmosphericInformation> atmosphericInformationMap = new HashMap<String, AtmosphericInformation>();

	/**
	 * radius frequency map
	 */
	private static Map<Double, Integer> radiusFrequencyMap = new HashMap<Double, Integer>();

	/**
	 * datasize naming constant
	 */
	public static final String DATASIZE = "datasize";

	/**
	 * radius frequency naming constant
	 */
	public static final String RADIUS_FREQ = "radius_freq";

	/**
	 * This method puts given {@link AtmosphericInformation} value to the map
	 * for given iataCode as the key
	 * 
	 * @param iataCode
	 *            iata code of the airport as key
	 * @param atmosphericInformation
	 *            {@link AtmosphericInformation} as value
	 */
	public static void addAtmosphericInformation(String iataCode,
			AtmosphericInformation atmosphericInformation) {
		atmosphericInformationMap.put(iataCode, atmosphericInformation);
	}

	/**
	 * Update the airports weather data with the collected data.
	 *
	 * @param iataCode
	 *            the 3 letter IATA code
	 * @param pointType
	 *            the point type {@link DataPointType}
	 * @param dataPoint
	 *            a datapoint object holding pointType data
	 *
	 * @throws WeatherException
	 *             if the update can not be completed
	 */
	public static void addDataPoint(String iataCode, String pointType,
			DataPoint dataPoint) throws WeatherException {
		AtmosphericInformation atmInfo = getAtmosphericInformation(iataCode);
		WeatherService.updateWeather(atmInfo, pointType, dataPoint);
		logger.debug(MessageFormat
				.format("New DataPoint: \"{0}\" has been added to the Atmospheric Info: \"{1}\"",
						dataPoint, atmInfo));
	}

	/**
	 * This method calculates data size using atmospheric information map
	 * 
	 * @return
	 */
	public static int calculateDataSize() {
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

	/**
	 * This method calculate radius frequencies
	 * 
	 * @return calculated radius frequency map
	 */
	public static int[] calculateRadiusFrequency() {
		int max = radiusFrequencyMap.keySet().stream().max(Double::compare)
				.orElse(1000.0).intValue() + 1;

		int[] hist = new int[max];
		for (Map.Entry<Double, Integer> e : radiusFrequencyMap.entrySet()) {
			int i = e.getKey().intValue() % 10;
			hist[i] += e.getValue();
		}
		return hist;
	}

	/**
	 * Returns {@link AtmosphericInformation} value from map according to given
	 * iata key
	 * 
	 * @param iata
	 *            iata code given
	 * @return {@link AtmosphericInformation} value
	 */
	public static AtmosphericInformation getAtmosphericInformation(String iata) {
		return atmosphericInformationMap.get(iata);
	}

	/**
	 * clears the contents of the maps
	 */
	public static void clear() {
		atmosphericInformationMap.clear();
		radiusFrequencyMap.clear();
	}

	/**
	 * removes {@link AtmosphericInformation} value of the iata code as given
	 * key
	 * 
	 * @param iataCode
	 */
	public static void removeAtmosphericInfo(String iataCode) {
		atmosphericInformationMap.remove(iataCode);
	}

	/**
	 * updates radius data frequency of the given radius
	 * 
	 * @param radius
	 *            radius value
	 */
	public static void updateRadiusDataFrequency(Double radius) {
		radiusFrequencyMap.put(radius,
				radiusFrequencyMap.getOrDefault(radius, 0));
	}

	/**
	 * update atmospheric information with the given data point for the given
	 * point type
	 *
	 * @param atmInfo
	 *            the atmospheric information object to update
	 * @param pointType
	 *            the data point type as a string
	 * @param dataPoint
	 *            the actual data point
	 */
	public static void updateWeather(AtmosphericInformation atmInfo,
			String pointType, DataPoint dataPoint) throws WeatherException {
		try {

			// switch point type value as data type to jump into data type cases
			switch (DataPointType.valueOf(pointType.toUpperCase())) {
			case WIND:
				updateWind(dataPoint, atmInfo);
				return;
			case TEMPERATURE:
				updateTemperature(dataPoint, atmInfo);
				return;
			case HUMIDTY:
				updateHumidity(dataPoint, atmInfo);
				return;
			case PRESSURE:
				updatePressure(dataPoint, atmInfo);
				return;
			case CLOUDCOVER:
				updateCloudCover(dataPoint, atmInfo);
				return;
			case PRECIPITATION:
				updatePrecipitation(dataPoint, atmInfo);
				return;
			}
		} catch (IllegalArgumentException iae) {
			throw new WeatherException(MessageFormat.format(
					TYPE_MISMATCH_WARNING, pointType));
		}

	}

	private static long getOneDayBeforeInMillis() {
		return System.currentTimeMillis() - DAY_IN_MILLIS;
	}

	/**
	 * Updates cloud cover value of {@link AtmosphericInformation} given by
	 * checking mean value of given data point
	 * 
	 * @param dataPoint
	 * @param atmInfo
	 */
	private static void updateCloudCover(DataPoint dataPoint,
			AtmosphericInformation atmInfo) {
		if (dataPoint.getMean() >= 0 && dataPoint.getMean() < 100) {
			atmInfo.setCloudCover(dataPoint);
			atmInfo.setLastUpdateTime(System.currentTimeMillis());
			logger.debug("Cloudcover value updated");
		}
	}

	/**
	 * Updates humidity value of {@link AtmosphericInformation} given by
	 * checking mean value of given data point
	 * 
	 * @param dataPoint
	 * @param atmInfo
	 */
	private static void updateHumidity(DataPoint dataPoint,
			AtmosphericInformation atmInfo) {
		if (dataPoint.getMean() >= 0 && dataPoint.getMean() < 100) {
			atmInfo.setHumidity(dataPoint);
			atmInfo.setLastUpdateTime(System.currentTimeMillis());
			logger.debug("Humidity value updated");
		}
	}

	/**
	 * Updates precipitation value of {@link AtmosphericInformation} given by
	 * checking mean value of given data point
	 * 
	 * @param dataPoint
	 * @param atmInfo
	 */
	private static void updatePrecipitation(DataPoint dataPoint,
			AtmosphericInformation atmInfo) {
		if (dataPoint.getMean() >= 0 && dataPoint.getMean() < 100) {
			atmInfo.setPrecipitation(dataPoint);
			atmInfo.setLastUpdateTime(System.currentTimeMillis());
			logger.debug("Precipitation value updated");
		}
	}

	/**
	 * Updates pressure value of {@link AtmosphericInformation} given by
	 * checking mean value of given data point
	 * 
	 * @param dataPoint
	 * @param atmInfo
	 */
	private static void updatePressure(DataPoint dataPoint,
			AtmosphericInformation atmInfo) {
		if (dataPoint.getMean() >= 650 && dataPoint.getMean() < 800) {
			atmInfo.setPressure(dataPoint);
			atmInfo.setLastUpdateTime(System.currentTimeMillis());
			logger.debug("Pressure value updated");
		}
	}

	/**
	 * Updates temperature value of {@link AtmosphericInformation} given by
	 * checking mean value of given data point
	 * 
	 * @param dataPoint
	 * @param atmInfo
	 */
	private static void updateTemperature(DataPoint dataPoint,
			AtmosphericInformation atmInfo) {
		if (dataPoint.getMean() >= -50 && dataPoint.getMean() < 100) {
			atmInfo.setTemperature(dataPoint);
			atmInfo.setLastUpdateTime(System.currentTimeMillis());
			logger.debug("Temperature value updated");
		}
	}

	/**
	 * Updates wind value of {@link AtmosphericInformation} given by checking
	 * mean value of given data point
	 * 
	 * @param dataPoint
	 * @param atmInfo
	 */
	private static void updateWind(DataPoint dataPoint,
			AtmosphericInformation atmInfo) {
		if (dataPoint.getMean() >= 0) {
			atmInfo.setWind(dataPoint);
			atmInfo.setLastUpdateTime(System.currentTimeMillis());
			logger.debug("Wind value updated");
		}
	}

}
