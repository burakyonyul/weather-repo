package com.crossover.trial.weather.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.crossover.trial.weather.pojo.AirportData;
import com.crossover.trial.weather.pojo.AtmosphericInformation;

public class AirportService {

	/** all known airports */
	private static Map<String, AirportData> airportMap = new HashMap<String, AirportData>();
	/**
	 * Internal performance counter to better understand most requested
	 * information, this map can be improved but for now provides the basis for
	 * future performance optimizations. Due to the stateless deployment
	 * architecture we don't want to write this to disk, but will pull it off
	 * using a REST request and aggregate with other performance metrics
	 * {@link #ping()}
	 */
	private static Map<AirportData, Integer> requestFrequency = new HashMap<AirportData, Integer>();

	/** earth radius in KM */
	private static final double EARTH_RADIUS_KM = 6372.8;

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
		airportMap.put(iataCode, ad);

		WeatherService.addAtmosphericInformation(iataCode,
				new AtmosphericInformation());
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
		return airportMap.get(iataCode);
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

	public static Map<String, Double> calculateIataFrequency() {
		Map<String, Double> freq = new HashMap<>();
		// fraction of queries
		for (AirportData data : airportMap.values()) {
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
		return airportMap.size();
	}

	public static void init() {
		airportMap.clear();
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
		// remove airport data from map
		airportMap.remove(iata);
		// remove atmospheric information
		WeatherService.removeAtmosphericInfo(iata);
		return;
	}

	public static Set<String> getAirportKeys() {
		return airportMap.keySet();
	}

	public static Collection<AirportData> getAirportValues() {
		return airportMap.values();
	}

}
