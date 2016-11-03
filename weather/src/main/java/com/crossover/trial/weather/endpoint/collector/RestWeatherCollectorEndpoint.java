package com.crossover.trial.weather.endpoint.collector;

import java.text.MessageFormat;
import java.util.Set;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.crossover.trial.weather.exception.WeatherException;
import com.crossover.trial.weather.pojo.AirportData;
import com.crossover.trial.weather.pojo.DataPoint;
import com.crossover.trial.weather.util.AirportService;
import com.crossover.trial.weather.util.WeatherService;
import com.google.gson.Gson;

/**
 * A REST implementation of the WeatherCollector API. Accessible only to airport
 * weather collection sites via secure VPN.
 * 
 * !!!! IMPORTANT !!!!
 * This class has annotations in its method parameters like in the
 * {@link WeatherCollectorEndpoint} interface. So this situation was causing a
 * major bug while parameter passing to the endpoint call. I also removed reused
 * rest parameter annotations in this concrete class implementation and did not
 * change anything in interfaces.
 *
 * @author burak
 */

@Path("/collect")
public class RestWeatherCollectorEndpoint implements WeatherCollectorEndpoint {
	public final static Logger logger = Logger
			.getLogger(RestWeatherCollectorEndpoint.class);

	@Override
	public Response addAirport(String iata, String latString, String longString) {
		AirportService.addAirport(iata, Double.valueOf(latString),
				Double.valueOf(longString));
		return Response.status(Response.Status.OK).build();
	}

	@Override
	public Response deleteAirport(String iata) {
		AirportData airportData = AirportService.deleteAirport(iata);
		logger.debug(MessageFormat.format("Deleted airport: \"{0}\"",
				airportData));
		return Response.status(Response.Status.OK).entity(airportData).build();
	}

	@Override
	public Response exit() {
		System.exit(0);
		return Response.noContent().build();
	}

	@Override
	public Response getAirport(String iata) {
		AirportData airportData = AirportService.findAirportData(iata);
		logger.debug(MessageFormat.format("Retrieved airport: \"{0}\"",
				airportData));
		return Response.status(Response.Status.OK).entity(airportData).build();
	}

	@Override
	public Response getAirports() {
		Set<String> airportKeys = AirportService.getAirportKeys();
		logger.debug(MessageFormat.format(
				"Retrieved airports with codes: \"{0}\"", airportKeys));
		return Response.status(Response.Status.OK).entity(airportKeys).build();
	}

	@Override
	public Response ping() {
		return Response.status(Response.Status.OK).entity("ready").build();
	}

	@Override
	public Response updateWeather(String iataCode, String pointType,
			String datapointJson) {
		try {
			WeatherService.addDataPoint(iataCode, pointType,
					new Gson().fromJson(datapointJson, DataPoint.class));
		} catch (WeatherException e) {
			logger.error(e.getMessage());
		}
		return Response.status(Response.Status.OK).build();
	}

}
