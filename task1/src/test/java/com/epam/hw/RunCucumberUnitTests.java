package com.epam.hw;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources",
        glue = {"com.epam.hw.unit.steps", "com.epam.hw"},
        plugin = {"pretty", "html:target/cucumber-unit-report.html"}
)
public class RunCucumberUnitTests {
}