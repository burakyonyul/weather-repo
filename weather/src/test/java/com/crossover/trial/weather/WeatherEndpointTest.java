package com.crossover.trial.weather;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.crossover.trial.weather.endpoint.collector.RestWeatherCollectorEndpoint;
import com.crossover.trial.weather.endpoint.collector.WeatherCollectorEndpoint;
import com.crossover.trial.weather.endpoint.query.RestWeatherQueryEndpoint;
import com.crossover.trial.weather.endpoint.query.WeatherQueryEndpoint;
import com.crossover.trial.weather.pojo.AirportData;
import com.crossover.trial.weather.pojo.AtmosphericInformation;
import com.crossover.trial.weather.pojo.DataPoint;
import com.crossover.trial.weather.util.AirportService;
import com.crossover.trial.weather.util.WeatherService;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class WeatherEndpointTest {

	private static final String IATA_FREQ = "iata_freq";

	private static final String DATASIZE = "datasize";

	private static final String CLOUDCOVER = "cloudcover";

	private static final String WIND = "wind";

	private static final String ZERO = "0";

	private WeatherQueryEndpoint _query = new RestWeatherQueryEndpoint();

	private WeatherCollectorEndpoint _update = new RestWeatherCollectorEndpoint();

	private Gson _gson = new Gson();

	private DataPoint _dp;

	private static final AirportData BOS = new AirportData("BOS", 42.364347,
			-71.005181);
	private static final AirportData EWR = new AirportData("EWR", 40.6925,
			-74.168667);
	private static final AirportData JFK = new AirportData("JFK", 40.639751,
			-73.778925);
	private static final AirportData LGA = new AirportData("LGA", 40.777245,
			-73.872608);
	private static final AirportData MMU = new AirportData("MMU", 40.79935,
			-74.4148747);

	@Before
	public void setUp() throws Exception {

		// initialize airport service
		initializeAirportService();
		// create a sample datapoint
		_dp = new DataPoint.Builder().withCount(10).withFirst(10)
				.withMedian(20).withLast(30).withMean(22).build();
		// update weather using iata code of BOS airport
		_update.updateWeather(BOS.getIata(), WIND, _gson.toJson(_dp));
		// get weather data matching with BOS airport
		_query.weather(BOS.getIata(), ZERO).getEntity();
	}

	@After
	public void tearDown() {
		// clear contents of the service
		AirportService.clear();
		WeatherService.clear();
	}

	/**
	 * initializes AirportService by adding 5 sample airport data
	 */
	private void initializeAirportService() {
		AirportService.addAirport(BOS.getIata(), BOS.getLatitude(),
				BOS.getLongitude());
		AirportService.addAirport(EWR.getIata(), EWR.getLatitude(),
				EWR.getLongitude());
		AirportService.addAirport(JFK.getIata(), JFK.getLatitude(),
				JFK.getLongitude());
		AirportService.addAirport(LGA.getIata(), LGA.getLatitude(),
				LGA.getLongitude());
		AirportService.addAirport(MMU.getIata(), MMU.getLatitude(),
				MMU.getLongitude());
	}

	@Test
	public void testPing() throws Exception {
		String ping = _query.ping();
		JsonElement pingResult = new JsonParser().parse(ping);
		assertEquals(1, pingResult.getAsJsonObject().get(DATASIZE).getAsInt());
		assertEquals(5, pingResult.getAsJsonObject().get(IATA_FREQ)
				.getAsJsonObject().entrySet().size());
	}

	@Test
	public void testGet() throws Exception {
		@SuppressWarnings("unchecked")
		List<AtmosphericInformation> ais = (List<AtmosphericInformation>) _query
				.weather(BOS.getIata(), ZERO).getEntity();
		assertEquals(ais.get(0).getWind(), _dp);
	}

	@Test
	public void testGetNearby() throws Exception {
		// first update weather of JFK, EWR and LGA airports appropriately
		_update.updateWeather(JFK.getIata(), WIND, _gson.toJson(_dp));
		_dp.setMean(40);
		_update.updateWeather(EWR.getIata(), WIND, _gson.toJson(_dp));
		_dp.setMean(30);
		_update.updateWeather(LGA.getIata(), WIND, _gson.toJson(_dp));

		// request weather info related to JFK airports
		@SuppressWarnings("unchecked")
		List<AtmosphericInformation> ais = (List<AtmosphericInformation>) _query
				.weather(JFK.getIata(), "200").getEntity();
		// check datasize response
		assertEquals(3, ais.size());
	}

	@Test
	public void testUpdate() throws Exception {

		// create a sample data point
		DataPoint windDp = new DataPoint.Builder().withCount(10).withFirst(10)
				.withMedian(20).withLast(30).withMean(22).build();
		// update weather of BOS airport as wind data point
		_update.updateWeather(BOS.getIata(), WIND, _gson.toJson(windDp));
		// query weather information of BOS airport
		_query.weather(BOS.getIata(), ZERO).getEntity();

		// ping to query endpoint
		String ping = _query.ping();
		JsonElement pingResult = new JsonParser().parse(ping);
		// get data size value
		assertEquals(1, pingResult.getAsJsonObject().get(DATASIZE).getAsInt());

		// create a new data point
		DataPoint cloudCoverDp = new DataPoint.Builder().withCount(4)
				.withFirst(10).withMedian(60).withLast(100).withMean(50)
				.build();
		// update weather of BOS airport again as cloudcover data point
		_update.updateWeather(BOS.getIata(), CLOUDCOVER,
				_gson.toJson(cloudCoverDp));

		// query weather of BOS airport
		@SuppressWarnings("unchecked")
		List<AtmosphericInformation> ais = (List<AtmosphericInformation>) _query
				.weather(BOS.getIata(), ZERO).getEntity();
		// check retrieved weather information
		assertEquals(ais.get(0).getWind(), windDp);
		assertEquals(ais.get(0).getCloudCover(), cloudCoverDp);
	}

	@Test
	public void testAddAirport() throws Exception {
		// create sample airport data
		AirportData sampleAirportData = new AirportData("AEE", 28.7565,
				-45.5859);
		// try to get the airport data using iata code
		AirportData airport = (AirportData) _update.getAirport(
				sampleAirportData.getIata()).getEntity();
		// assert that retrieved airport is null
		assertNull(airport);
		// add new airport with the data of sample airport
		_update.addAirport(sampleAirportData.getIata(),
				String.valueOf(sampleAirportData.getLatitude()),
				String.valueOf(sampleAirportData.getLongitude()));
		// get airport data again
		airport = (AirportData) _update.getAirport(sampleAirportData.getIata())
				.getEntity();
		// check that it is not null
		assertNotNull(airport);
		// check they are same with sample airport data
		assertEquals(sampleAirportData, airport);
	}

	@Test
	public void testDeleteAirport() throws Exception {
		// create sample airport data
		AirportData sampleAirportData = new AirportData("AEE", 28.7565,
				-45.5859);
		// add new airport with the data of sample airport
		_update.addAirport(sampleAirportData.getIata(),
				String.valueOf(sampleAirportData.getLatitude()),
				String.valueOf(sampleAirportData.getLongitude()));
		// delete airport with the iata value of sample airport
		AirportData deletedAirport = (AirportData) _update.deleteAirport(
				sampleAirportData.getIata()).getEntity();
		// check that it is not null
		assertNotNull(deletedAirport);
		// check they are same with sample airport data
		assertEquals(sampleAirportData, deletedAirport);
	}

	@Test
	public void testGetSpecificAirport() throws Exception {
		// create sample airport data
		AirportData sampleAirportData = new AirportData("AEE", 28.7565,
				-45.5859);
		// add new airport with the data of sample airport
		_update.addAirport(sampleAirportData.getIata(),
				String.valueOf(sampleAirportData.getLatitude()),
				String.valueOf(sampleAirportData.getLongitude()));
		// get airport data using iata code of sample airport
		AirportData retrivedAirport = (AirportData) _update.getAirport(
				sampleAirportData.getIata()).getEntity();
		// check that it is not null
		assertNotNull(retrivedAirport);
		// check they are same with sample airport data
		assertEquals(sampleAirportData, retrivedAirport);
	}

	@Test
	public void testGetAllAirports() throws Exception {

		// clear all data contained in Airport and Weather services for exact
		// control of get all airports service
		tearDown();

		// create two sample airport data
		AirportData ADB = new AirportData("ADB", 38.4225, 27.155);
		AirportData SAW = new AirportData("SAW", 40.898333, 29.309167);

		// get all airports
		@SuppressWarnings("unchecked")
		Set<String> airportCodesBefore = (Set<String>) _update.getAirports()
				.getEntity();

		// check the set is not null
		assertNotNull(airportCodesBefore);
		// check if retrieved airport code list is empty
		assertTrue(airportCodesBefore.isEmpty());

		// add first airport
		_update.addAirport(ADB.getIata(), String.valueOf(ADB.getLatitude()),
				String.valueOf(ADB.getLongitude()));

		// add second airport
		_update.addAirport(SAW.getIata(), String.valueOf(SAW.getLatitude()),
				String.valueOf(SAW.getLongitude()));

		@SuppressWarnings("unchecked")
		Set<String> airportCodesAfter = (Set<String>) _update.getAirports()
				.getEntity();
		// check the set is not null
		assertNotNull(airportCodesAfter);
		// check the size of the list has increased by two
		assertEquals(2, airportCodesAfter.size());
		// check if first and second airport iata codes are contained by after
		// code list
		assertTrue(airportCodesAfter.contains(ADB.getIata()));
		assertTrue(airportCodesAfter.contains(SAW.getIata()));
	}

}