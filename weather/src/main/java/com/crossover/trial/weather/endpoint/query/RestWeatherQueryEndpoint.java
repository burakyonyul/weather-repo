package com.crossover.trial.weather.endpoint.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

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
 * @author code test administrator
 */
@Path("/query")
public class RestWeatherQueryEndpoint implements WeatherQueryEndpoint {

	private static final String RADIUS_FREQ = "radius_freq";

	private static final String IATA_FREQ = "iata_freq";

	private static final String DATASIZE = "datasize";

	private final static Logger LOGGER = Logger.getLogger("WeatherQuery");

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

		retval.put(DATASIZE, WeatherService.calculateRadiusFrequency());

		retval.put(IATA_FREQ, AirportService.calculateIataFrequency());

		retval.put(RADIUS_FREQ, AirportService.calculateDatasize());

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
		double radius = radiusString == null || radiusString.trim().isEmpty() ? 0
				: Double.valueOf(radiusString);
		updateRequestFrequency(iata, radius);

		List<AtmosphericInformation> retval = new ArrayList<AtmosphericInformation>();
		if (radius == 0) {
			int idx = AirportService.getAirportDataIdx(iata);
			retval.add(WeatherService.atmosphericInformation.get(idx));
		} else {
			AirportData ad = AirportService.findAirportData(iata);
			for (int i = 0; i < AirportService.getAirportDataSize(); i++) {
				if (AirportService.calculateDistance(ad,
						AirportService.airportData.get(i)) <= radius) {
					AtmosphericInformation ai = WeatherService.atmosphericInformation
							.get(i);
					if (ai.getCloudCover() != null || ai.getHumidity() != null
							|| ai.getPrecipitation() != null
							|| ai.getPressure() != null
							|| ai.getTemperature() != null
							|| ai.getWind() != null) {
						retval.add(ai);
					}
				}
			}
		}
		return Response.status(Response.Status.OK).entity(retval).build();
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
	}

	/**
	 * A dummy init method that loads hard coded data
	 */
	public static void init() {
		WeatherService.init();
		AirportService.init();
	}

}
