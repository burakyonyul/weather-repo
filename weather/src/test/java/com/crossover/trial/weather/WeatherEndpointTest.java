package com.crossover.trial.weather;

import com.crossover.trial.weather.endpoint.collector.RestWeatherCollectorEndpoint;
import com.crossover.trial.weather.endpoint.collector.WeatherCollectorEndpoint;
import com.crossover.trial.weather.endpoint.query.RestWeatherQueryEndpoint;
import com.crossover.trial.weather.endpoint.query.WeatherQueryEndpoint;
import com.crossover.trial.weather.pojo.AirportData;
import com.crossover.trial.weather.pojo.AtmosphericInformation;
import com.crossover.trial.weather.pojo.DataPoint;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class WeatherEndpointTest {

	private WeatherQueryEndpoint _query = new RestWeatherQueryEndpoint();

	private WeatherCollectorEndpoint _update = new RestWeatherCollectorEndpoint();

	private Gson _gson = new Gson();

	private DataPoint _dp;

	@Before
	public void setUp() throws Exception {
		RestWeatherQueryEndpoint.init();
		_dp = new DataPoint.Builder().withCount(10).withFirst(10)
				.withMedian(20).withLast(30).withMean(22).build();
		_update.updateWeather("BOS", "wind", _gson.toJson(_dp));
		_query.weather("BOS", "0").getEntity();
	}

	@Test
	public void testPing() throws Exception {
		String ping = _query.ping();
		JsonElement pingResult = new JsonParser().parse(ping);
		assertEquals(1, pingResult.getAsJsonObject().get("datasize").getAsInt());
		assertEquals(5, pingResult.getAsJsonObject().get("iata_freq")
				.getAsJsonObject().entrySet().size());
	}

	@Test
	public void testGet() throws Exception {
		@SuppressWarnings("unchecked")
		List<AtmosphericInformation> ais = (List<AtmosphericInformation>) _query
				.weather("BOS", "0").getEntity();
		assertEquals(ais.get(0).getWind(), _dp);
	}

	@Test
	public void testGetNearby() throws Exception {
		// check datasize response
		_update.updateWeather("JFK", "wind", _gson.toJson(_dp));
		_dp.setMean(40);
		_update.updateWeather("EWR", "wind", _gson.toJson(_dp));
		_dp.setMean(30);
		_update.updateWeather("LGA", "wind", _gson.toJson(_dp));

		@SuppressWarnings("unchecked")
		List<AtmosphericInformation> ais = (List<AtmosphericInformation>) _query
				.weather("JFK", "200").getEntity();
		assertEquals(3, ais.size());
	}

	@Test
	public void testUpdate() throws Exception {

		DataPoint windDp = new DataPoint.Builder().withCount(10).withFirst(10)
				.withMedian(20).withLast(30).withMean(22).build();
		_update.updateWeather("BOS", "wind", _gson.toJson(windDp));
		_query.weather("BOS", "0").getEntity();

		String ping = _query.ping();
		JsonElement pingResult = new JsonParser().parse(ping);
		assertEquals(1, pingResult.getAsJsonObject().get("datasize").getAsInt());

		DataPoint cloudCoverDp = new DataPoint.Builder().withCount(4)
				.withFirst(10).withMedian(60).withLast(100).withMean(50)
				.build();
		_update.updateWeather("BOS", "cloudcover", _gson.toJson(cloudCoverDp));

		@SuppressWarnings("unchecked")
		List<AtmosphericInformation> ais = (List<AtmosphericInformation>) _query
				.weather("BOS", "0").getEntity();
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

		// create two sample airport data
		AirportData firstAirport = new AirportData("AEE", 28.7565, -45.5859);
		AirportData secondAirport = new AirportData("BCD", -15.5859, 21.7565);

		// get all airports
		@SuppressWarnings("unchecked")
		Set<String> airportCodesBefore = (Set<String>) _update.getAirports()
				.getEntity();

		// check the set is not null
		assertNotNull(airportCodesBefore);
		// assert first airport list size is 5 because of creating 5 airports
		// internally
		assertEquals(5, airportCodesBefore.size());

		// add first airport
		_update.addAirport(firstAirport.getIata(),
				String.valueOf(firstAirport.getLatitude()),
				String.valueOf(firstAirport.getLongitude()));

		// add second airport
		_update.addAirport(secondAirport.getIata(),
				String.valueOf(secondAirport.getLatitude()),
				String.valueOf(secondAirport.getLongitude()));

		@SuppressWarnings("unchecked")
		Set<String> airportCodesAfter = (Set<String>) _update.getAirports()
				.getEntity();
		// check the set is not null
		assertNotNull(airportCodesAfter);
		// check the size of the list has increased by two
		assertEquals(7, airportCodesAfter.size());
	}

}