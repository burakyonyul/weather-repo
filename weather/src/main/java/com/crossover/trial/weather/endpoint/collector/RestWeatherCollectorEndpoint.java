package com.crossover.trial.weather.endpoint.collector;

import java.util.logging.Logger;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

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
 * @author code test administrator
 */

@Path("/collect")
public class RestWeatherCollectorEndpoint implements WeatherCollectorEndpoint {
	public final static Logger LOGGER = Logger
			.getLogger(RestWeatherCollectorEndpoint.class.getName());

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
			e.printStackTrace();
		}
		return Response.status(Response.Status.OK).build();
	}

	@Override
	public Response getAirports() {
		return Response.status(Response.Status.OK)
				.entity(AirportService.getAirportKeys()).build();
	}

	@Override
	public Response getAirport(String iata) {
		AirportData ad = AirportService.findAirportData(iata);
		return Response.status(Response.Status.OK).entity(ad).build();
	}

	@Override
	public Response addAirport(String iata, String latString, String longString) {
		AirportService.addAirport(iata, Double.valueOf(latString),
				Double.valueOf(longString));
		return Response.status(Response.Status.OK).build();
	}

	@Override
	public Response deleteAirport(String iata) {
		AirportService.deleteAirport(iata);
		return Response.status(Response.Status.OK).build();
	}

	@Override
	public Response exit() {
		System.exit(0);
		return Response.noContent().build();
	}

}
