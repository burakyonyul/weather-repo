package com.crossover.trial.weather;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ DoNotChangeTest.class, WeatherEndpointTest.class,
		AirportServiceTest.class, WeatherServiceTest.class })
public class AllTests {
}
