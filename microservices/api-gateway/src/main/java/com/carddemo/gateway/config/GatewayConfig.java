package com.carddemo.gateway.config;

import org.springframework.context.annotation.Configuration;

/**
 * Gateway configuration.
 *
 * Route definitions are specified in application.yml, mapping the original
 * COBOL menu options to downstream microservices:
 *
 * From COMEN02Y.cpy (Regular User Menu - COMEN01C.cbl):
 *   Option 1-2:  Account View/Update  (COACTVWC/COACTUPC) -> account-service
 *   Option 3-5:  Card List/View/Update (COCRDLIC/COCRDSLC/COCRDUPC) -> card-service
 *   Option 6-8:  Transaction List/View/Add (COTRN00C/01C/02C) -> transaction-service
 *   Option 9:    Transaction Reports (CORPT00C) -> billing-service
 *   Option 10:   Bill Payment (COBIL00C) -> billing-service
 *
 * From COADM02Y.cpy (Admin Menu - COADM01C.cbl):
 *   Option 1-4:  User List/Add/Update/Delete (COUSR00C-03C) -> user-admin-service
 *
 * Additional services not in original COBOL menus:
 *   - auth-service: Replaces COSGN00C sign-on functionality
 *   - statement-service: Statement generation (from batch CBSTM03A)
 */
@Configuration
public class GatewayConfig {
}
