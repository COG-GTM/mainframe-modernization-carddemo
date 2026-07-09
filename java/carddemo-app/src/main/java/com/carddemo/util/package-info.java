/**
 * Framework-light, reusable utilities migrated from the CardDemo COBOL common routines.
 *
 * <p>Currently hosts {@link com.carddemo.util.DateValidator}, the Java translation of the
 * COBOL date-validation program {@code CSUTLDTC} (a wrapper over the Language Environment
 * {@code CEEDAYS} callable service) together with the reusable edit paragraphs in the
 * {@code CSUTLDPY}/{@code CSUTLDWY} copybooks.</p>
 *
 * <p>Later migration waves that today perform their own inline date checks (e.g. account,
 * card and transaction maintenance screens) are expected to delegate to this component
 * instead of re-implementing the calendar rules.</p>
 */
package com.carddemo.util;
