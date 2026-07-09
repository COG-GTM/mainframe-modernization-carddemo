/**
 * User-administration service layer — the Java port of the online COBOL user-maintenance
 * programs {@code COUSR00C} (list), {@code COUSR01C} (add), {@code COUSR02C} (update) and
 * {@code COUSR03C} (delete). All operate over the USRSEC store ({@code CSUSR01Y} /
 * {@link com.carddemo.domain.SecurityUser}) and are admin-only (see CS-9 mapping doc).
 */
package com.carddemo.service.useradmin;
