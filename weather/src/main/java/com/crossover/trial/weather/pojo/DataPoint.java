package com.crossover.trial.weather.pojo;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * A collected point, including some information about the range of collected
 * values
 *
 * @author burak
 */
public class DataPoint {

	static public class Builder {
		int first;
		int mean;
		int median;
		int last;
		int count;

		public Builder() {
		}

		public DataPoint build() {
			return new DataPoint(this.first, this.mean, this.median, this.last,
					this.count);
		}

		public Builder withCount(int count) {
			this.count = count;
			return this;
		}

		public Builder withFirst(int first) {
			this.first = first;
			return this;
		}

		public Builder withLast(int last) {
			this.last = last;
			return this;
		}

		public Builder withMean(int mean) {
			this.mean = mean;
			return this;
		}

		public Builder withMedian(int median) {
			this.median = median;
			return this;
		}
	}

	public double mean = 0.0;

	public int first = 0;

	public int second = 0;

	public int third = 0;

	public int count = 0;

	/** private constructor, use the builder to create this object */
	@SuppressWarnings("unused")
	private DataPoint() {
	}

	protected DataPoint(int first, int second, int mean, int third, int count) {
		this.setFirst(first);
		this.setMean(mean);
		this.setSecond(second);
		this.setThird(third);
		this.setCount(count);
	}

	public boolean equals(Object that) {
		return this.toString().equals(that.toString());
	}

	/** the total number of measurements */
	public int getCount() {
		return count;
	}

	/** 1st quartile -- useful as a lower bound */
	public int getFirst() {
		return first;
	}

	/** the mean of the observations */
	public double getMean() {
		return mean;
	}

	/** 2nd quartile -- median value */
	public int getSecond() {
		return second;
	}

	/** 3rd quartile value -- less noisy upper value */
	public int getThird() {
		return third;
	}

	public void setMean(double mean) {
		this.mean = mean;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this,
				ToStringStyle.NO_CLASS_NAME_STYLE);
	}

	protected void setCount(int count) {
		this.count = count;
	}

	protected void setFirst(int first) {
		this.first = first;
	}

	protected void setSecond(int second) {
		this.second = second;
	}

	protected void setThird(int third) {
		this.third = third;
	}
}
