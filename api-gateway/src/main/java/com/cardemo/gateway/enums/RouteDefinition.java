package com.cardemo.gateway.enums;

/**
 * Maps COBOL program names to REST API routes and downstream services.
 * Derived from COMEN02Y.cpy (regular menu) and COADM02Y.cpy (admin menu).
 */
public enum RouteDefinition {

    // Regular user menu options (COMEN02Y.cpy)
    ACCOUNT_VIEW("COACTVWC", "Account View", "/api/v1/accounts/{id}", "account-service", "GET", UserType.USER),
    ACCOUNT_UPDATE("COACTUPC", "Account Update", "/api/v1/accounts/{id}", "account-service", "PUT", UserType.USER),
    CARD_LIST("COCRDLIC", "Credit Card List", "/api/v1/cards", "card-service", "GET", UserType.USER),
    CARD_VIEW("COCRDSLC", "Credit Card View", "/api/v1/cards/{cardNum}", "card-service", "GET", UserType.USER),
    CARD_UPDATE("COCRDUPC", "Credit Card Update", "/api/v1/cards/{cardNum}", "card-service", "PUT", UserType.USER),
    TRANSACTION_LIST("COTRN00C", "Transaction List", "/api/v1/transactions", "transaction-service", "GET", UserType.USER),
    TRANSACTION_VIEW("COTRN01C", "Transaction View", "/api/v1/transactions/{id}", "transaction-service", "GET", UserType.USER),
    TRANSACTION_ADD("COTRN02C", "Transaction Add", "/api/v1/transactions", "transaction-service", "POST", UserType.USER),
    REPORT_SUBMIT("CORPT00C", "Transaction Reports", "/api/v1/reports", "reports-service", "POST", UserType.USER),
    BILL_PAYMENT("COBIL00C", "Bill Payment", "/api/v1/transactions/bill-payment", "transaction-service", "POST", UserType.USER),

    // Admin menu options (COADM02Y.cpy)
    USER_LIST("COUSR00C", "User List (Security)", "/api/v1/users", "auth-service", "GET", UserType.ADMIN),
    USER_ADD("COUSR01C", "User Add (Security)", "/api/v1/users", "auth-service", "POST", UserType.ADMIN),
    USER_UPDATE("COUSR02C", "User Update (Security)", "/api/v1/users/{id}", "auth-service", "PUT", UserType.ADMIN),
    USER_DELETE("COUSR03C", "User Delete (Security)", "/api/v1/users/{id}", "auth-service", "DELETE", UserType.ADMIN),

    // Auth (COSGN00C)
    SIGNON("COSGN00C", "Sign On", "/api/v1/auth/login", "auth-service", "POST", null),

    // Menu programs
    MAIN_MENU("COMEN01C", "Main Menu", "/api/v1/menu", "menu-navigation-service", "GET", UserType.USER),
    ADMIN_MENU("COADM01C", "Admin Menu", "/api/v1/menu/admin", "menu-navigation-service", "GET", UserType.ADMIN);

    private final String programName;
    private final String description;
    private final String routePath;
    private final String serviceName;
    private final String httpMethod;
    private final UserType requiredUserType;

    RouteDefinition(String programName, String description, String routePath,
                    String serviceName, String httpMethod, UserType requiredUserType) {
        this.programName = programName;
        this.description = description;
        this.routePath = routePath;
        this.serviceName = serviceName;
        this.httpMethod = httpMethod;
        this.requiredUserType = requiredUserType;
    }

    public String getProgramName() {
        return programName;
    }

    public String getDescription() {
        return description;
    }

    public String getRoutePath() {
        return routePath;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public UserType getRequiredUserType() {
        return requiredUserType;
    }

    public boolean requiresAdmin() {
        return requiredUserType == UserType.ADMIN;
    }

    public boolean isPublic() {
        return requiredUserType == null;
    }

    public static RouteDefinition fromProgramName(String programName) {
        for (RouteDefinition route : values()) {
            if (route.programName.equalsIgnoreCase(programName)) {
                return route;
            }
        }
        return null;
    }
}
