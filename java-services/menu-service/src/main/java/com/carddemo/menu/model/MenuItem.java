package com.carddemo.menu.model;

/**
 * Represents a single menu option migrated from the COBOL copybooks
 * COMEN02Y.cpy (regular user options) and COADM02Y.cpy (admin options).
 *
 * Each item maps an option number and name to the corresponding
 * microservice REST endpoint that replaces the original CICS program.
 */
public class MenuItem {

    private int optionNumber;
    private String name;
    private String serviceEndpoint;
    private String httpMethod;
    private String userTypeRequired; // "U" (any user) or "A" (admin only)

    public MenuItem() {
    }

    public MenuItem(int optionNumber, String name, String serviceEndpoint,
                    String httpMethod, String userTypeRequired) {
        this.optionNumber = optionNumber;
        this.name = name;
        this.serviceEndpoint = serviceEndpoint;
        this.httpMethod = httpMethod;
        this.userTypeRequired = userTypeRequired;
    }

    public int getOptionNumber() {
        return optionNumber;
    }

    public void setOptionNumber(int optionNumber) {
        this.optionNumber = optionNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getServiceEndpoint() {
        return serviceEndpoint;
    }

    public void setServiceEndpoint(String serviceEndpoint) {
        this.serviceEndpoint = serviceEndpoint;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getUserTypeRequired() {
        return userTypeRequired;
    }

    public void setUserTypeRequired(String userTypeRequired) {
        this.userTypeRequired = userTypeRequired;
    }
}
