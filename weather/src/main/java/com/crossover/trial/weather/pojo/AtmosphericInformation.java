package com.crossover.trial.weather.pojo;

/**
 * encapsulates sensor information for a particular location
 */
public class AtmosphericInformation {

	/** temperature in degrees celsius */
	private DataPoint temperature;

	/** wind speed in km/h */
	private DataPoint wind;

	/** humidity in percent */
	private DataPoint humidity;

	/** precipitation in cm */
	private DataPoint precipitation;

	/** pressure in mmHg */
	private DataPoint pressure;

	/** cloud cover percent from 0 - 100 (integer) */
	private DataPoint cloudCover;

	/** the last time this data was updated, in milliseconds since UTC epoch */
	private long lastUpdateTime;

	public AtmosphericInformation() {

	}

	public AtmosphericInformation(DataPoint temperature, DataPoint wind,
			DataPoint humidity, DataPoint percipitation, DataPoint pressure,
			DataPoint cloudCover) {
		this.temperature = temperature;
		this.wind = wind;
		this.humidity = humidity;
		this.precipitation = percipitation;
		this.pressure = pressure;
		this.cloudCover = cloudCover;
		this.lastUpdateTime = System.currentTimeMillis();
	}

	public DataPoint getCloudCover() {
		return cloudCover;
	}

	public DataPoint getHumidity() {
		return humidity;
	}

	public long getLastUpdateTime() {
		return this.lastUpdateTime;
	}

	public DataPoint getPrecipitation() {
		return precipitation;
	}

	public DataPoint getPressure() {
		return pressure;
	}

	public DataPoint getTemperature() {
		return temperature;
	}

	public DataPoint getWind() {
		return wind;
	}

	/**
	 * Checks the class has at least one not-null {@link DataPoint} field
	 * 
	 * @return <code>true</code> if it has any <code>false</code> if it has all
	 *         null value
	 */
	public boolean hasAnyDataPointValue() {
		return this.cloudCover != null || this.humidity != null
				|| this.pressure != null || this.precipitation != null
				|| this.temperature != null || this.wind != null;
	}

	public void setCloudCover(DataPoint cloudCover) {
		this.cloudCover = cloudCover;
	}

	public void setHumidity(DataPoint humidity) {
		this.humidity = humidity;
	}

	public void setLastUpdateTime(long lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	public void setPrecipitation(DataPoint precipitation) {
		this.precipitation = precipitation;
	}

	public void setPressure(DataPoint pressure) {
		this.pressure = pressure;
	}

	public void setTemperature(DataPoint temperature) {
		this.temperature = temperature;
	}

	public void setWind(DataPoint wind) {
		this.wind = wind;
	}

	@Override
	public String toString() {
		return "AtmosphericInformation [temperature=" + temperature + ", wind="
				+ wind + ", humidity=" + humidity + ", precipitation="
				+ precipitation + ", pressure=" + pressure + ", cloudCover="
				+ cloudCover + ", lastUpdateTime=" + lastUpdateTime + "]";
	}

}
