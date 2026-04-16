package com.cardemo.migration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "migration")
public class MigrationConfig {

    private EbcdicConfig ebcdic = new EbcdicConfig();
    private ValidationConfig validation = new ValidationConfig();

    public EbcdicConfig getEbcdic() {
        return ebcdic;
    }

    public void setEbcdic(EbcdicConfig ebcdic) {
        this.ebcdic = ebcdic;
    }

    public ValidationConfig getValidation() {
        return validation;
    }

    public void setValidation(ValidationConfig validation) {
        this.validation = validation;
    }

    public static class EbcdicConfig {
        private String dataDir = "app/data/EBCDIC";

        public String getDataDir() {
            return dataDir;
        }

        public void setDataDir(String dataDir) {
            this.dataDir = dataDir;
        }
    }

    public static class ValidationConfig {
        private int spotCheckCount = 3;

        public int getSpotCheckCount() {
            return spotCheckCount;
        }

        public void setSpotCheckCount(int spotCheckCount) {
            this.spotCheckCount = spotCheckCount;
        }
    }
}
