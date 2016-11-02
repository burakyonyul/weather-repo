package com.crossover.trial.weather.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

/**
 * A simple airport loader which reads a file from disk and sends entries to the
 * webservice
 *
 * TODO: Implement the Airport Loader
 * 
 * @author code test administrator
 */
public class AirportLoader {

	private static final int DEFAULT_DATA_SIZE = 11;

	private static final String PARAM_LONG = "long";

	private static final String PARAM_LAT = "lat";

	private static final String PARAM_IATA = "iata";

	private static final String ADD_AIRPORT_URI_EXTENSION = "/airport/{"
			+ PARAM_IATA + "}/{" + PARAM_LAT + "}/{" + PARAM_LONG + "}";

	private static final String DOUBLE_QUOTE = "\"";

	private static final String COMMA = ",";

	/** end point for read queries */
	private WebTarget query;

	/** end point to supply updates */
	private WebTarget collect;

	public AirportLoader() {
		Client client = ClientBuilder.newClient();
		query = client.target("http://localhost:9090/query");
		collect = client.target("http://localhost:9090/collect");
	}

	public static void main(String args[]) throws IOException,
			InterruptedException, ExecutionException {
		File airportDataFile = new File(args[0]);
		if (!airportDataFile.exists() || airportDataFile.length() == 0) {
			System.err.println(airportDataFile + " is not a valid input");
			System.exit(1);
		}

		AirportLoader al = new AirportLoader();
		al.upload(new FileInputStream(airportDataFile));
		System.exit(0);
	}

	public void upload(InputStream airportDataStream) throws IOException,
			InterruptedException, ExecutionException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				airportDataStream));
		String l = null;
		while ((l = reader.readLine()) != null) {
			// split comma-separated line into pieces
			String[] linePieces = l.split(COMMA);
			// check if current line is complete as given in the example
			// airports.dat file
			if (linePieces.length == DEFAULT_DATA_SIZE) {
				// get iata code
				String iataCode = linePieces[4].trim();
				// get latitude
				String latitude = linePieces[6].trim();
				// get longitude
				String longitude = linePieces[7].trim();
				// get iata code from double quoted "" version
				iataCode = extractExactIataCode(iataCode).trim();
				// sends request to collector endpoint to add a new airport
				sendAddAirportRequest(iataCode, latitude, longitude);
			}
		}
	}

	/**
	 * This method extracts iata code from double quote
	 * 
	 * @param iataCode
	 * @return
	 */
	private String extractExactIataCode(String iataCode) {
		if (iataCode.startsWith(DOUBLE_QUOTE)
				&& iataCode.endsWith(DOUBLE_QUOTE) && iataCode.length() > 1) {
			iataCode = iataCode.substring(1, iataCode.length() - 1);
		}
		return iataCode;
	}

	/**
	 * This method sends post request to collector endpoint for adding new
	 * airport
	 * 
	 * @param iataCode
	 *            iata code of the airport
	 * @param latitude
	 *            latitute information
	 * @param longitude
	 *            longitude information
	 */
	private void sendAddAirportRequest(String iataCode, String latitude,
			String longitude) {
		collect.path(ADD_AIRPORT_URI_EXTENSION)
				.resolveTemplate(PARAM_IATA, iataCode)
				.resolveTemplate(PARAM_LAT, latitude)
				.resolveTemplate(PARAM_LONG, longitude).request()
				.post(Entity.entity(null, MediaType.APPLICATION_JSON));
	}
}
