package com.carddemo.session.web;

import java.util.Map;

/**
 * Body of a {@code POST /api/nav} turn.
 *
 * @param tranId the target transaction to launch/select (e.g. a menu option's TRANSID);
 *               omit for a turn on the screen already in control.
 * @param pfKey  the attention key pressed ({@code ENTER}, {@code PF3}, …); defaults ENTER.
 * @param fields raw screen input fields for the current screen handler.
 */
public record NavigationRequest(String tranId, String pfKey, Map<String, String> fields) {
}
