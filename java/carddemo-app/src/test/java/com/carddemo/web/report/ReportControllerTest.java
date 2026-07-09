package com.carddemo.web.report;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.carddemo.domain.Transaction;
import com.carddemo.repository.TransactionRepository;

/**
 * End-to-end tests for the ported {@code CORPT00C} transaction-report request flow. Uses seed
 * reference data ({@code cardxref.txt}/{@code trantype.txt}/{@code trancatg.txt}) plus a small
 * set of online transactions inserted per test (the seed loads the daily file, not the online
 * {@code card_transaction} table). Card {@code 9680294154603697} maps to account
 * {@code 00000000001} in the seeded xref.
 */
@SpringBootTest(properties = "carddemo.seed.enabled=true")
@ActiveProfiles("test")
@Transactional
@WithMockUser(username = "USER0001", roles = "USER")
class ReportControllerTest {

    private static final String CARD = "9680294154603697";
    private static final String URL = "/api/reports/transactions";

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private TransactionRepository transactionRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        transactionRepository.save(tran("9000000000000001", "01", 1,
                new BigDecimal("100.00"), "2023-05-10 09:00:00.000000"));
        transactionRepository.save(tran("9000000000000002", "01", 1,
                new BigDecimal("50.25"), "2023-05-20 09:00:00.000000"));
        transactionRepository.save(tran("9000000000000003", "01", 1,
                new BigDecimal("999.99"), "2024-01-01 09:00:00.000000"));
    }

    private static Transaction tran(String id, String type, int cat, BigDecimal amt, String ts) {
        Transaction t = new Transaction();
        t.setTranId(id);
        t.setTranTypeCd(type);
        t.setTranCatCd(cat);
        t.setTranAmt(amt);
        t.setTranCardNum(CARD);
        t.setTranSource("POS");
        t.setTranOrigTs(ts);
        t.setTranProcTs(ts);
        return t;
    }

    private static String custom(String sm, String sd, String sy, String em, String ed,
            String ey, String confirm) {
        StringBuilder sb = new StringBuilder("{\"reportType\":\"CUSTOM\"");
        sb.append(",\"startMonth\":\"").append(sm).append("\"");
        sb.append(",\"startDay\":\"").append(sd).append("\"");
        sb.append(",\"startYear\":\"").append(sy).append("\"");
        sb.append(",\"endMonth\":\"").append(em).append("\"");
        sb.append(",\"endDay\":\"").append(ed).append("\"");
        sb.append(",\"endYear\":\"").append(ey).append("\"");
        if (confirm != null) {
            sb.append(",\"confirm\":\"").append(confirm).append("\"");
        }
        return sb.append("}").toString();
    }

    @Test
    void validCustomRangeGeneratesReportFilteredByDate() throws Exception {
        // 2023-05-01..2023-05-31 includes the two May 2023 rows, excludes the 2024 row.
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                .content(custom("05", "01", "2023", "05", "31", "2023", "Y")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.submitted").value(true))
            .andExpect(jsonPath("$.reportName").value("Custom"))
            .andExpect(jsonPath("$.startDate").value("2023-05-01"))
            .andExpect(jsonPath("$.endDate").value("2023-05-31"))
            .andExpect(jsonPath("$.message").value("Custom report submitted for printing ..."))
            .andExpect(jsonPath("$.report.recordCount").value(2))
            .andExpect(jsonPath("$.report.grandTotal").value(150.25))
            .andExpect(jsonPath("$.report.lines[0].accountId").value("00000000001"))
            .andExpect(jsonPath("$.report.lines[0].typeDesc").value("Purchase"))
            .andExpect(jsonPath("$.report.lines[0].categoryDesc").value("Regular Sales Draft"))
            .andExpect(jsonPath("$.report.accountTotals[0].accountId").value("00000000001"))
            .andExpect(jsonPath("$.report.accountTotals[0].total").value(150.25));
    }

    @Test
    void rangeThatExcludesAllTransactionsGivesEmptyReport() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                .content(custom("01", "01", "2020", "12", "31", "2020", "Y")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.submitted").value(true))
            .andExpect(jsonPath("$.report.recordCount").value(0))
            .andExpect(jsonPath("$.report.grandTotal").value(0));
    }

    @Test
    void unconfirmedRequestPromptsToConfirmWithResolvedDates() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                .content(custom("05", "01", "2023", "05", "31", "2023", null)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.confirmationRequired").value(true))
            .andExpect(jsonPath("$.submitted").value(false))
            .andExpect(jsonPath("$.startDate").value("2023-05-01"))
            .andExpect(jsonPath("$.message").value("Please confirm to print the Custom report..."));
    }

    @Test
    void monthlyReportResolvesToCurrentMonth() throws Exception {
        LocalDate first = LocalDate.now().withDayOfMonth(1);
        String expectedStart = first.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String expectedEnd = first.plusMonths(1).minusDays(1)
                .format(DateTimeFormatter.ISO_LOCAL_DATE);

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reportType\":\"MONTHLY\",\"confirm\":\"Y\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reportName").value("Monthly"))
            .andExpect(jsonPath("$.startDate").value(expectedStart))
            .andExpect(jsonPath("$.endDate").value(expectedEnd));
    }

    @Test
    void missingReportTypeReturnsCobolMessage() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Select a report type to print report..."));
    }

    @Test
    void emptyCustomStartMonthReturnsCobolMessage() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                .content(custom("", "01", "2023", "05", "31", "2023", "Y")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Start Date - Month can NOT be empty..."));
    }

    @Test
    void invalidCustomMonthReturnsCobolMessage() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                .content(custom("13", "01", "2023", "05", "31", "2023", "Y")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Start Date - Not a valid Month..."));
    }

    @Test
    void impossibleCustomDateReturnsCobolMessage() throws Exception {
        // 2023-02-30 passes the month/day range checks but fails the calendar-date check.
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                .content(custom("02", "30", "2023", "05", "31", "2023", "Y")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Start Date - Not a valid date..."));
    }

    @Test
    void invalidConfirmValueReturnsCobolMessage() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                .content(custom("05", "01", "2023", "05", "31", "2023", "Z")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("\"Z\" is not a valid value to confirm..."));
    }
}
