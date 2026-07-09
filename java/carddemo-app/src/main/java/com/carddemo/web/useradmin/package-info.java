/**
 * Admin user-maintenance REST layer (CS-9) — the online face of {@code COUSR00C} (list),
 * {@code COUSR01C} (add), {@code COUSR02C} (update) and {@code COUSR03C} (delete), all
 * admin-only (guarded by {@code ROLE_ADMIN}). Screen handlers plug the four programs into the
 * CS-3 navigation framework ({@link com.carddemo.session.ScreenHandler}).
 */
package com.carddemo.web.useradmin;
