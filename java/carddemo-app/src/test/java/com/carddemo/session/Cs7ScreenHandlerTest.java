package com.carddemo.session;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.carddemo.web.billpay.BillPayScreenHandler;
import com.carddemo.web.report.ReportScreenHandler;
import com.carddemo.web.report.dto.ReportResponse;
import com.carddemo.web.billpay.dto.BillPayResponse;

/**
 * Verifies the CS-7 {@link ScreenHandler} beans ({@code COBIL00} / {@code CORPT00}) are
 * registered in the {@link ScreenRegistry} and drive their services correctly across a
 * pseudo-conversational ENTER → RE-ENTER turn.
 */
@SpringBootTest(properties = "carddemo.seed.enabled=true")
@ActiveProfiles("test")
@Transactional
class Cs7ScreenHandlerTest {

    @Autowired
    private ScreenRegistry registry;

    @Test
    void billPaymentHandlerIsRegisteredAgainstItsProgram() {
        ScreenHandler handler = registry.forProgram(CardDemoProgram.BILL_PAYMENT).orElseThrow();
        assertThat(handler).isInstanceOf(BillPayScreenHandler.class);
    }

    @Test
    void reportsHandlerIsRegisteredAgainstItsProgram() {
        ScreenHandler handler = registry.forProgram(CardDemoProgram.REPORTS).orElseThrow();
        assertThat(handler).isInstanceOf(ReportScreenHandler.class);
    }

    @Test
    void billPaymentHandlerShowsBlankThenPromptsToConfirm() {
        ScreenHandler handler = registry.forProgram(CardDemoProgram.BILL_PAYMENT).orElseThrow();
        CardDemoCommarea commarea = new CardDemoCommarea();

        // First entry (ENTER): blank screen, no message.
        ScreenResult first = handler.handle(
                new ScreenRequest(PfKey.ENTER, Map.of()), commarea);
        assertThat(first.type()).isEqualTo(ScreenResult.Type.STAY);
        assertThat(first.message()).isNull();

        // RE-ENTER with an account id and no confirmation -> balance shown, prompt to confirm.
        ScreenResult second = handler.handle(
                new ScreenRequest(PfKey.ENTER, Map.of(BillPayScreenHandler.FIELD_ACCT_ID,
                        "00000000001")), commarea);
        assertThat(second.type()).isEqualTo(ScreenResult.Type.STAY);
        assertThat(second.message()).isEqualTo("Confirm to make a bill payment...");
        assertThat(second.model()).isInstanceOf(BillPayResponse.class);
    }

    @Test
    void reportsHandlerShowsBlankThenPromptsToConfirm() {
        ScreenHandler handler = registry.forProgram(CardDemoProgram.REPORTS).orElseThrow();
        CardDemoCommarea commarea = new CardDemoCommarea();

        ScreenResult first = handler.handle(
                new ScreenRequest(PfKey.ENTER, Map.of()), commarea);
        assertThat(first.type()).isEqualTo(ScreenResult.Type.STAY);
        assertThat(first.message()).isNull();

        ScreenResult second = handler.handle(new ScreenRequest(PfKey.ENTER, Map.of(
                ReportScreenHandler.FIELD_REPORT_TYPE, "CUSTOM",
                ReportScreenHandler.FIELD_START_MONTH, "05",
                ReportScreenHandler.FIELD_START_DAY, "01",
                ReportScreenHandler.FIELD_START_YEAR, "2023",
                ReportScreenHandler.FIELD_END_MONTH, "05",
                ReportScreenHandler.FIELD_END_DAY, "31",
                ReportScreenHandler.FIELD_END_YEAR, "2023")), commarea);
        assertThat(second.type()).isEqualTo(ScreenResult.Type.STAY);
        assertThat(second.message()).isEqualTo("Please confirm to print the Custom report...");
        assertThat(second.model()).isInstanceOf(ReportResponse.class);
    }
}
