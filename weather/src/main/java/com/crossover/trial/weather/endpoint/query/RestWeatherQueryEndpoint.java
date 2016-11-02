package com.crossover.trial.weather.endpoint.query;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.crossover.trial.weather.pojo.AirportData;
import com.crossover.trial.weather.pojo.AtmosphericInformation;
import com.crossover.trial.weather.util.AirportService;
import com.crossover.trial.weather.util.WeatherService;
import com.google.gson.Gson;

/**
 * The Weather App REST endpoint allows clients to query, update and check
 * health stats. Currently, all data is held in memory. The end point deploys to
 * a single container
 *
 * @author burak
 */
@Path("/query")
public class RestWeatherQueryEndpoint implements WeatherQueryEndpoint {

	private static final String RADIUS_FREQ = "radius_freq";

	private static final String IATA_FREQ = "iata_freq";

	private static final String DATASIZE = "datasize";

	private final static Logger logger = Logger
			.getLogger(RestWeatherQueryEndpoint.class);

	/** shared gson json to object factory */
	public static final Gson gson = new Gson();

	static {
		RestWeatherQueryEndpoint.init();
	}

	/**
	 * Retrieve service health including total size of valid data points and
	 * request frequency information.
	 *
	 * @return health stats for the service as a string
	 */
	@Override
	public String ping() {
		Map<String, Object> retval = new HashMap<>();

		retval.put(DATASIZE, WeatherService.calculateDataSize());

		retval.put(IATA_FREQ, AirportService.calculateIataFrequency());

		retval.put(RADIUS_FREQ, WeatherService.calculateRadiusFrequency());

		return gson.toJson(retval);
	}

	/**
	 * Given a query in json format {'iata': CODE, 'radius': km} extracts the
	 * requested airport information and return a list of matching atmosphere
	 * information.
	 *
	 * @param iata
	 *            the iataCode
	 * @param radiusString
	 *            the radius in km
	 *
	 * @return a list of atmospheric information
	 */
	@Override
	public Response weather(String iata, String radiusString) {
		// get radius from parameter
		double radius = getRadiusValue(radiusString);
		// update request frequency using iata and radius
		updateRequestFrequency(iata, radius);
		// build a list of atmospheric info for given iata and radius
		List<AtmosphericInformation> atmInfoList = buildAtmosphericInfoList(
				iata, radius);
		return Response.status(Response.Status.OK).entity(atmInfoList).build();
	}

	/**
	 * This method retrieves nearby atmospheric information for the airport with
	 * the given iata code and using given radius
	 * 
	 * @param iata
	 *            iata code of the airport to collect atmospheric information
	 *            for
	 * @param radius
	 *            radius to get atmospheric information of nearby airports
	 * @return {@link List} of {@link AtmosphericInformation}s
	 */
	private List<AtmosphericInformation> buildAtmosphericInfoList(String iata,
			double radius) {
		// define list of atmospheric information
		List<AtmosphericInformation> atmInfoList = new ArrayList<AtmosphericInformation>();
		// fill atmospheric information list
		if (radius == 0) {
			atmInfoList.add(WeatherService.getAtmosphericInformation(iata));
		} else {
			// get airport data value from iata code
			AirportData referenceAirportData = AirportService
					.findAirportData(iata);
			// iterate on airport values
			for (AirportData currentData : AirportService.getAirportValues()) {
				if (AirportService.calculateDistance(referenceAirportData,
						currentData) <= radius) {
					// get atmospheric information
					AtmosphericInformation atmInfo = WeatherService
							.getAtmosphericInformation(currentData.getIata());
					// add atmospheric information to the list if it has any
					// data point value
					if (atmInfo.hasAnyDataPointValue()) {
						atmInfoList.add(atmInfo);
					}
				}
			}
		}
		logger.debug(MessageFormat.format(
				"Atmospheric information list has been consructed: \"{0}\"",
				atmInfoList));
		return atmInfoList;
	}

	/**
	 * get {@link Double} type radius value from string
	 * 
	 * @param radiusString
	 *            radius value in {@link String} type
	 * @return radius value in {@link Double} type
	 */
	private double getRadiusValue(String radiusString) {
		double radius = radiusString == null || radiusString.trim().isEmpty() ? 0
				: Double.valueOf(radiusString);
		logger.debug(MessageFormat.format("Radius value is: \"{0}\"", radius));
		return radius;
	}

	/**
	 * Records information about how often requests are made
	 *
	 * @param iata
	 *            an iata code
	 * @param radius
	 *            query radius
	 */
	private void updateRequestFrequency(String iata, Double radius) {
		AirportService.updateAirportDataFrequency(iata);
		WeatherService.updateRadiusDataFrequency(radius);
		logger.debug("Request Frequency has been updated!");
	}

	/**
	 * A dummy init method that loads hard coded data
	 */
	public static void init() {
		WeatherService.init();
		AirportService.init();
	}

}
