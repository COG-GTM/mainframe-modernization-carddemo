package com.carddemo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Replaces the JCL DD statements that bound programs to datasets. */
@ConfigurationProperties(prefix = "carddemo")
public class CardDemoProperties {

    /** Directory holding the legacy fixed width ASCII extracts (IDCAMS load input). */
    private String dataDirectory = "../app/data/ASCII";

    /** Directory the statement and report jobs write their output to. */
    private String outputDirectory = "./target/carddemo-output";

    public String getDataDirectory() {
        return dataDirectory;
    }

    public void setDataDirectory(String dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
}
