package com.carddemo.menu.dto;

/**
 * Data Transfer Object for a single menu item returned to the client.
 */
public class MenuItemDto {

    private int optionNumber;
    private String name;
    private String serviceEndpoint;
    private String httpMethod;

    public MenuItemDto() {
    }

    public MenuItemDto(int optionNumber, String name, String serviceEndpoint, String httpMethod) {
        this.optionNumber = optionNumber;
        this.name = name;
        this.serviceEndpoint = serviceEndpoint;
        this.httpMethod = httpMethod;
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
}
