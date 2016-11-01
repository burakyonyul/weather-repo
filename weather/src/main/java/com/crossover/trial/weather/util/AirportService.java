package com.crossover.trial.weather.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.crossover.trial.weather.pojo.AirportData;
import com.crossover.trial.weather.pojo.AtmosphericInformation;

public class AirportService {

	/** all known airports */
	public static List<AirportData> airportData = new ArrayList<AirportData>();
	/**
	 * Internal performance counter to better understand most requested
	 * information, this map can be improved but for now provides the basis for
	 * future performance optimizations. Due to the stateless deployment
	 * architecture we don't want to write this to disk, but will pull it off
	 * using a REST request and aggregate with other performance metrics
	 * {@link #ping()}
	 */
	public static Map<AirportData, Integer> requestFrequency = new HashMap<AirportData, Integer>();

	/** earth radius in KM */
	public static final double EARTH_RADIUS_KM = 6372.8;

	/**
	 * Add a new known airport to our list.
	 *
	 * @param iataCode
	 *            3 letter code
	 * @param latitude
	 *            in degrees
	 * @param longitude
	 *            in degrees
	 *
	 * @return the added airport
	 */
	public static AirportData addAirport(String iataCode, double latitude,
			double longitude) {

		// create an airport data using specific constructor with required
		// parameters
		AirportData ad = new AirportData(iataCode, latitude, longitude);
		// add into airport data list
		airportData.add(ad);

		AtmosphericInformation ai = new AtmosphericInformation();
		WeatherService.atmosphericInformation.add(ai);
		return ad;
	}

	/**
	 * Given an iataCode find the airport data
	 *
	 * @param iataCode
	 *            as a string
	 * @return airport data or null if not found
	 */
	public static AirportData findAirportData(String iataCode) {
		return airportData.stream().filter(ap -> ap.getIata().equals(iataCode))
				.findFirst().orElse(null);
	}

	/**
	 * Haversine distance between two airports.
	 *
	 * @param ad1
	 *            airport 1
	 * @param ad2
	 *            airport 2
	 * @return the distance in KM
	 */
	public static double calculateDistance(AirportData ad1, AirportData ad2) {
		double deltaLat = Math.toRadians(ad2.getLatitude() - ad1.getLatitude());
		double deltaLon = Math.toRadians(ad2.getLongitude()
				- ad1.getLongitude());
		double a = Math.pow(Math.sin(deltaLat / 2), 2)
				+ Math.pow(Math.sin(deltaLon / 2), 2)
				* Math.cos(ad1.getLatitude()) * Math.cos(ad2.getLatitude());
		double c = 2 * Math.asin(Math.sqrt(a));
		return AirportService.EARTH_RADIUS_KM * c;
	}

	/**
	 * Given an iataCode find the airport data
	 *
	 * @param iataCode
	 *            as a string
	 * @return airport data or null if not found
	 */
	public static int getAirportDataIdx(String iataCode) {
		AirportData ad = findAirportData(iataCode);
		return airportData.indexOf(ad);
	}

	public static int[] calculateDatasize() {
		int m = WeatherService.radiusFreq.keySet().stream()
				.max(Double::compare).orElse(1000.0).intValue() + 1;

		int[] hist = new int[m];
		for (Map.Entry<Double, Integer> e : WeatherService.radiusFreq
				.entrySet()) {
			int i = e.getKey().intValue() % 10;
			hist[i] += e.getValue();
		}
		return hist;
	}

	public static Map<String, Double> calculateIataFrequency() {
		Map<String, Double> freq = new HashMap<>();
		// fraction of queries
		for (AirportData data : airportData) {
			double frac = (double) requestFrequency.getOrDefault(data, 0)
					/ requestFrequency.size();
			freq.put(data.getIata(), frac);
		}
		return freq;
	}

	public static void updateAirportDataFrequency(String iata) {
		// find airport data using iata code
		AirportData airportData = AirportService.findAirportData(iata);
		// update airport data
		requestFrequency.put(airportData,
				requestFrequency.getOrDefault(airportData, 0) + 1);
	}

	public static int getAirportDataSize() {
		return airportData.size();
	}

	public static void init() {
		airportData.clear();
		requestFrequency.clear();

		addAirport("BOS", 42.364347, -71.005181);
		addAirport("EWR", 40.6925, -74.168667);
		addAirport("JFK", 40.639751, -73.778925);
		addAirport("LGA", 40.777245, -73.872608);
		addAirport("MMU", 40.79935, -74.4148747);
	}

	/**
	 * Find according to iata and delete it
	 * 
	 * @param iata
	 */
	public static void deleteAirport(String iata) {
		// TODO: Use hash map instead of list
		for (AirportData data : airportData) {
			if (iata.equals(data.getIata())) {
				// remove atmospheric information
				WeatherService.removeAtmosphericInfo(AirportService
						.getAirportDataIdx(data.getIata()));
				// remove airport data
				airportData.remove(data);
				return;
			}
		}
	}

}
