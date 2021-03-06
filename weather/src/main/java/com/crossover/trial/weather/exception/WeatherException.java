package com.crossover.trial.weather.exception;

/**
 * An internal exception marker specific for weather application
 */
public class WeatherException extends Exception {
	/**
	 * Serial version of the exception
	 */
	private static final long serialVersionUID = -3717617059428945841L;

	public WeatherException(String message) {
		super(message);
	}

}
