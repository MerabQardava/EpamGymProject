package com.epam.WorkloadMicroservice;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources",
        glue = {"com.epam.WorkloadMicroservice.unit.steps", "com.epam.WorkloadMicroservice"},
        plugin = {"pretty", "html:target/cucumber-unit-report.html"}
)
public class RunCucumberTests {
}