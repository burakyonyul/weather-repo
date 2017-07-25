package com.crossover.trial.weather;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.crossover.trial.weather.exception.WeatherException;
import com.crossover.trial.weather.pojo.AirportData;
import com.crossover.trial.weather.pojo.AtmosphericInformation;
import com.crossover.trial.weather.pojo.DataPoint;
import com.crossover.trial.weather.util.AirportService;
import com.crossover.trial.weather.util.WeatherService;

/**
 * This class asserts critical functionalities of {@link WeatherService} class
 * 
 * @author burak
 *
 */
public class WeatherServiceTest {

	/**
	 * Sample Airport with ADB iata code
	 */
	private static final AirportData ADB = new AirportData("ADB", 38.4225,
			27.155);

	@Before
	public void before() {
		WeatherService.clear();
	}

	@After
	public void after() {
		WeatherService.clear();
	}

	/**
	 * This method validates both adding {@link AtmosphericInformation} while
	 * creating an airport and get added {@link AtmosphericInformation}
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAddAtmosphericInfo() throws Exception {
		// add an airport to airport service, so it will add atmospheric
		// information too
		AirportService.addAirport(ADB.getIata(), ADB.getLatitude(),
				ADB.getLongitude());
		// check if it is not null
		assertNotNull(WeatherService.getAtmosphericInformation(ADB.getIata()));
	}

	@Test
	public void testAddDataPoint() throws Exception {

		// add an airport to airport service, so it will add atmospheric
		// information too
		AirportService.addAirport(ADB.getIata(), ADB.getLatitude(),
				ADB.getLongitude());

		DataPoint windDp = new DataPoint.Builder().withCount(10).withFirst(10)
				.withMedian(20).withLast(30).withMean(22).build();

		// update weather of BOS airport as wind data point
		WeatherService.addDataPoint(ADB.getIata(), "wind", windDp);

		// get atmospheric information of ADB airport
		AtmosphericInformation atmosphericInformation = WeatherService
				.getAtmosphericInformation(ADB.getIata());
		// check if it is not null
		assertNotNull(atmosphericInformation);
		// check that atmInfo has at least one data point value
		assertTrue(atmosphericInformation.hasAnyDataPointValue());
		// get wind value from atmospheric information
		DataPoint retrievedDataPoint = atmosphericInformation.getWind();
		// check if it is not null
		assertNotNull(retrievedDataPoint);
		// assert created wind data point is same with retrieved data point
		assertEquals(windDp, retrievedDataPoint);

		// check if other than windy data point is not available in atmospheric
		// information
		assertNull(atmosphericInformation.getPressure());
	}

	@Test(expected = WeatherException.class)
	public void testAddNonTypedDataPoint() throws Exception {
		// add an airport to airport service, so it will add atmospheric
		// information too
		AirportService.addAirport(ADB.getIata(), ADB.getLatitude(),
				ADB.getLongitude());

		DataPoint windDp = new DataPoint.Builder().withCount(10).withFirst(10)
				.withMedian(20).withLast(30).withMean(22).build();

		// update weather of BOS airport as wind data point
		WeatherService.addDataPoint(ADB.getIata(), "rain", windDp);
	}
}
