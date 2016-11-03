package com.crossover.trial.weather;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.crossover.trial.weather.pojo.AirportData;
import com.crossover.trial.weather.util.AirportService;

public class AirportServiceTest {

	private static final Double ZERO_POINT_FIVE = new Double(0.5);

	private static final Double DISTANCE_IN_RADIANS = new Double(
			181.29685614469278);

	/**
	 * Sample Airport with ADB iata code
	 */
	private static final AirportData ADB = new AirportData("ADB", 38.4225,
			27.155);
	/**
	 * Sample Airport with SAW iata code
	 */
	private static final AirportData SAW = new AirportData("SAW", 40.898333,
			29.309167);

	@Before
	public void before() {
		AirportService.clear();
	}

	@After
	public void after() {
		AirportService.clear();
	}

	@Test
	public void testAddAirport() throws Exception {
		// check airport service contains no data before
		assertTrue(AirportService.getAirportValues().isEmpty());
		// add sample airport to the service
		AirportService.addAirport(ADB.getIata(), ADB.getLatitude(),
				ADB.getLongitude());
		// check if airport has been added to the service
		assertEquals(1, AirportService.getAirportDataSize());
		assertEquals(ADB, AirportService.findAirportData(ADB.getIata()));
	}

	@Test
	public void testDeleteAirport() throws Exception {
		// first add a new airport
		AirportService.addAirport(ADB.getIata(), ADB.getLatitude(),
				ADB.getLongitude());
		// check the data size
		assertEquals(1, AirportService.getAirportDataSize());
		// remove airport from service
		AirportData removedAirport = AirportService
				.deleteAirport(ADB.getIata());
		// check added and removed airports are same
		assertEquals(ADB, removedAirport);
		// check there is no airport registered to the service afterwards
		assertEquals(0, AirportService.getAirportDataSize());
	}

	@Test
	public void testFindAirport() throws Exception {
		// first add a new airport
		AirportService.addAirport(ADB.getIata(), ADB.getLatitude(),
				ADB.getLongitude());
		// find added airport
		AirportData foundAirport = AirportService
				.findAirportData(ADB.getIata());
		// check found airport is not null
		assertNotNull(foundAirport);
		// check found and added airports are same
		assertEquals(ADB, foundAirport);
	}

	@Test
	public void testCalculateDistance() throws Exception {
		// calculate distance between ADB and SAW airports
		Double actual = new Double(AirportService.calculateDistance(ADB, SAW));
		// check if it is as expected
		assertEquals(DISTANCE_IN_RADIANS, actual);
	}

	/**
	 * This test also provides correctness of both updating data frequency map
	 * and calculating iata frequencies
	 * 
	 * @throws Exception
	 */
	@Test
	public void calculateFrequencyTest() throws Exception {
		// first add two airports to the service
		AirportService.addAirport(ADB.getIata(), ADB.getLatitude(),
				ADB.getLongitude());
		AirportService.addAirport(SAW.getIata(), SAW.getLatitude(),
				SAW.getLongitude());
		// update data frequencies of airports
		AirportService.updateAirportDataFrequency(ADB.getIata());
		AirportService.updateAirportDataFrequency(SAW.getIata());
		// calculate data frequencies
		Map<String, Double> frequency = AirportService.calculateIataFrequency();
		// check if it is not null
		assertNotNull(frequency);
		// check if it is expected
		assertEquals(ZERO_POINT_FIVE, frequency.get(ADB.getIata()));
		assertEquals(ZERO_POINT_FIVE, frequency.get(SAW.getIata()));
	}

}
