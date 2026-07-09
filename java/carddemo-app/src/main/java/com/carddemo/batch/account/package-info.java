/**
 * WAVE 3 (CS-10) — batch account &amp; interest programs.
 *
 * <p>Java (Spring Batch 5) port of the CardDemo batch account programs:</p>
 * <ul>
 *   <li>{@code CBACT01C} → {@code accountFileJob} (read/print the account master)</li>
 *   <li>{@code CBACT02C} → {@code cardFileJob} (read/print the card file)</li>
 *   <li>{@code CBACT03C} → {@code xrefFileJob} (read/print the card cross-reference)</li>
 *   <li>{@code CBCUS01C} → {@code customerFileJob} (read/print the customer file)</li>
 *   <li>{@code CBACT04C} → {@code intcalcJob} (INTCALC — monthly interest calculation)</li>
 * </ul>
 *
 * <p>Every class of this wave lives under {@code com.carddemo.batch.account} to avoid
 * conflicts with sibling batch waves. Nothing here modifies shared scaffolding, the data
 * model, {@code pom.xml} or {@code application.yml}.</p>
 */
package com.carddemo.batch.account;
