package com.carddemo.common;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CardDemoSessionContext} and {@link UserType}, verifying
 * correct mapping from the COBOL COMMAREA ({@code COCOM01Y.cpy}).
 */
class CardDemoSessionContextTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Nested
    @DisplayName("Field mapping from COBOL COMMAREA")
    class FieldMapping {

        @Test
        @DisplayName("should hold all COMMAREA fields with correct values")
        void shouldMapAllFields() {
            CardDemoSessionContext ctx = new CardDemoSessionContext();
            ctx.setFromTransactionId("CC00");
            ctx.setFromProgram("COSGN00C");
            ctx.setToTransactionId("CM00");
            ctx.setToProgram("COMEN01C");
            ctx.setUserId("USER0001");
            ctx.setUserType(UserType.USER);
            ctx.setProgramContext(0);
            ctx.setCustomerId(123456789L);
            ctx.setCustomerFirstName("JOHN");
            ctx.setCustomerMiddleName("M");
            ctx.setCustomerLastName("DOE");
            ctx.setAccountId(12345678901L);
            ctx.setAccountStatus("Y");
            ctx.setCardNumber("1234567890123456");
            ctx.setLastMap("COSGN0A");
            ctx.setLastMapset("COSGN00");

            assertEquals("CC00", ctx.getFromTransactionId());
            assertEquals("COSGN00C", ctx.getFromProgram());
            assertEquals("CM00", ctx.getToTransactionId());
            assertEquals("COMEN01C", ctx.getToProgram());
            assertEquals("USER0001", ctx.getUserId());
            assertEquals(UserType.USER, ctx.getUserType());
            assertEquals(0, ctx.getProgramContext());
            assertEquals(123456789L, ctx.getCustomerId());
            assertEquals("JOHN", ctx.getCustomerFirstName());
            assertEquals("M", ctx.getCustomerMiddleName());
            assertEquals("DOE", ctx.getCustomerLastName());
            assertEquals(12345678901L, ctx.getAccountId());
            assertEquals("Y", ctx.getAccountStatus());
            assertEquals("1234567890123456", ctx.getCardNumber());
            assertEquals("COSGN0A", ctx.getLastMap());
            assertEquals("COSGN00", ctx.getLastMapset());
        }

        @Test
        @DisplayName("should support admin user type")
        void shouldSupportAdminType() {
            CardDemoSessionContext ctx = new CardDemoSessionContext();
            ctx.setUserId("ADMIN001");
            ctx.setUserType(UserType.ADMIN);

            assertEquals(UserType.ADMIN, ctx.getUserType());
        }
    }

    @Nested
    @DisplayName("Program context flags (CDEMO-PGM-CONTEXT)")
    class ProgramContext {

        @Test
        @DisplayName("context=0 should indicate initial entry")
        void shouldIndicateInitialEntry() {
            CardDemoSessionContext ctx = new CardDemoSessionContext();
            ctx.setProgramContext(0);

            assertTrue(ctx.isInitialEntry());
            assertFalse(ctx.isReentry());
        }

        @Test
        @DisplayName("context=1 should indicate re-entry")
        void shouldIndicateReentry() {
            CardDemoSessionContext ctx = new CardDemoSessionContext();
            ctx.setProgramContext(1);

            assertFalse(ctx.isInitialEntry());
            assertTrue(ctx.isReentry());
        }
    }

    @Nested
    @DisplayName("Bean Validation")
    class BeanValidation {

        @Test
        @DisplayName("should pass validation with all required fields")
        void shouldPassWithValidData() {
            CardDemoSessionContext ctx = new CardDemoSessionContext();
            ctx.setUserId("USER0001");
            ctx.setUserType(UserType.USER);

            Set<ConstraintViolation<CardDemoSessionContext>> violations = validator.validate(ctx);
            assertTrue(violations.isEmpty(), "Expected no violations but got: " + violations);
        }

        @Test
        @DisplayName("should fail validation when userId is blank")
        void shouldFailWithBlankUserId() {
            CardDemoSessionContext ctx = new CardDemoSessionContext();
            ctx.setUserId("");
            ctx.setUserType(UserType.USER);

            Set<ConstraintViolation<CardDemoSessionContext>> violations = validator.validate(ctx);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("userId")));
        }

        @Test
        @DisplayName("should fail validation when userType is null")
        void shouldFailWithNullUserType() {
            CardDemoSessionContext ctx = new CardDemoSessionContext();
            ctx.setUserId("USER0001");
            ctx.setUserType(null);

            Set<ConstraintViolation<CardDemoSessionContext>> violations = validator.validate(ctx);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("userType")));
        }

        @Test
        @DisplayName("should fail validation when field exceeds max length")
        void shouldFailWhenFieldExceedsMaxLength() {
            CardDemoSessionContext ctx = new CardDemoSessionContext();
            ctx.setUserId("USER0001");
            ctx.setUserType(UserType.USER);
            ctx.setFromTransactionId("TOOLONG");  // max 4

            Set<ConstraintViolation<CardDemoSessionContext>> violations = validator.validate(ctx);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("fromTransactionId")));
        }
    }

    @Nested
    @DisplayName("UserType enum")
    class UserTypeTest {

        @Test
        @DisplayName("ADMIN should have code 'A'")
        void adminShouldHaveCodeA() {
            assertEquals('A', UserType.ADMIN.getCode());
        }

        @Test
        @DisplayName("USER should have code 'U'")
        void userShouldHaveCodeU() {
            assertEquals('U', UserType.USER.getCode());
        }

        @Test
        @DisplayName("fromCode('A') should return ADMIN")
        void shouldResolveAdminFromCode() {
            assertEquals(UserType.ADMIN, UserType.fromCode('A'));
        }

        @Test
        @DisplayName("fromCode('U') should return USER")
        void shouldResolveUserFromCode() {
            assertEquals(UserType.USER, UserType.fromCode('U'));
        }

        @Test
        @DisplayName("fromCode with unknown code should throw")
        void shouldThrowOnUnknownCode() {
            assertThrows(IllegalArgumentException.class, () -> UserType.fromCode('X'));
        }
    }

    @Test
    @DisplayName("toString should include key fields")
    void toStringShouldIncludeKeyFields() {
        CardDemoSessionContext ctx = new CardDemoSessionContext();
        ctx.setUserId("USER0001");
        ctx.setUserType(UserType.USER);
        ctx.setAccountId(123L);

        String str = ctx.toString();
        assertTrue(str.contains("USER0001"));
        assertTrue(str.contains("USER"));
        assertTrue(str.contains("123"));
    }

    @Test
    @DisplayName("toString should mask card number for PCI-DSS compliance")
    void toStringShouldMaskCardNumber() {
        CardDemoSessionContext ctx = new CardDemoSessionContext();
        ctx.setUserId("USER0001");
        ctx.setUserType(UserType.USER);
        ctx.setCardNumber("1234567890123456");

        String str = ctx.toString();
        assertFalse(str.contains("1234567890123456"), "Full card number must not appear in toString()");
        assertTrue(str.contains("****3456"), "Masked card number should show last 4 digits");
    }
}
