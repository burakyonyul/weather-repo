package com.crossover.trial.weather.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.crossover.trial.weather.pojo.AirportData;
import com.crossover.trial.weather.pojo.AtmosphericInformation;

/**
 * This is the service class for airport related operations and containment of
 * airports
 * 
 * @author burak
 *
 */
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
	 * Add a new known airport to the airport map.
	 *
	 * @param iataCode
	 *            3 letter code
	 * @param latitude
	 *            in degrees
	 * @param longitude
	 *            in degrees
	 *
	 */
	public static void addAirport(String iataCode, double latitude,
			double longitude) {

		// create an airport data using specific constructor with required
		// parameters
		AirportData airportData = new AirportData(iataCode, latitude, longitude);
		// add into airport data list
		airportMap.put(iataCode, airportData);
		// add atmospheric information of the related airport to the weather
		// service
		WeatherService.addAtmosphericInformation(iataCode,
				new AtmosphericInformation());
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
	 * Calculates frequencies of airports
	 * 
	 * @return frequency {@link Map} instance as <key = iata, value = frequency>
	 */
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

	/**
	 * Find according to iata and delete it
	 * 
	 * @param iata
	 * @return deleted {@link AirportData} instance
	 */
	public static AirportData deleteAirport(String iata) {
		// remove airport data from map
		AirportData removedAirport = airportMap.remove(iata);
		// remove atmospheric information
		WeatherService.removeAtmosphericInfo(iata);
		return removedAirport;
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

	public static int getAirportDataSize() {
		return airportMap.size();
	}

	public static Set<String> getAirportKeys() {
		return airportMap.keySet();
	}

	public static Collection<AirportData> getAirportValues() {
		return airportMap.values();
	}

	public static void init() {
		airportMap.clear();
		requestFrequency.clear();

		// TODO: this should be removed for real use afterwards
		// add statically 5 airports to the map
		addAirport("BOS", 42.364347, -71.005181);
		addAirport("EWR", 40.6925, -74.168667);
		addAirport("JFK", 40.639751, -73.778925);
		addAirport("LGA", 40.777245, -73.872608);
		addAirport("MMU", 40.79935, -74.4148747);
	}

	/**
	 * Update frequency of airport with given iata code value
	 * 
	 * @param iata
	 *            iata code of an airport
	 */
	public static void updateAirportDataFrequency(String iata) {
		// find airport data using iata code
		AirportData airportData = AirportService.findAirportData(iata);
		// update airport data
		requestFrequency.put(airportData,
				requestFrequency.getOrDefault(airportData, 0) + 1);
	}

}
