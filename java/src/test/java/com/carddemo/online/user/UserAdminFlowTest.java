package com.carddemo.online.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.carddemo.model.dto.CardDemoCommarea;
import com.carddemo.model.entity.SecurityUser;
import com.carddemo.repository.SecurityUserRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

/**
 * COBOL programs COUSR00C, COUSR01C, COUSR02C and COUSR03C: browse with paging plus the full
 * add / read / update / delete cycle on USRSEC.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserAdminFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SecurityUserRepository securityUserRepository;

    private MockHttpSession session;

    @BeforeEach
    void signedOnAsAdmin() {
        securityUserRepository.deleteAll();
        session = new MockHttpSession();
        CardDemoCommarea commarea = new CardDemoCommarea();
        commarea.setUserId("ADMIN001");
        commarea.setUserType(CardDemoCommarea.USER_TYPE_ADMIN);
        commarea.setFromProgram("COADM01C");
        commarea.setFromTransactionId("CA00");
        session.setAttribute(CardDemoCommarea.SESSION_KEY, commarea);
    }

    private void givenUsers(int count) {
        List<SecurityUser> users = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            users.add(SecurityUser.builder()
                    .userId("TSTU%04d".formatted(i))
                    .firstName("First" + i)
                    .lastName("Last" + i)
                    .password("PWD%05d".formatted(i))
                    .userType(i % 2 == 0 ? "A" : "U")
                    .build());
        }
        securityUserRepository.saveAll(users);
    }

    private MockHttpServletRequestBuilder json(String url, String body) {
        return post(url).session(session).contentType(MediaType.APPLICATION_JSON).content(body);
    }

    @Test
    void pagesForwardAndBackwardThroughTheUserList() throws Exception {
        givenUsers(25);

        mockMvc.perform(get("/api/users").session(session))
                .andExpect(jsonPath("$.pageNumber").value(1))
                .andExpect(jsonPath("$.nextPageAvailable").value(true))
                .andExpect(jsonPath("$.users.length()").value(10))
                .andExpect(jsonPath("$.users[0].userId").value("TSTU0001"))
                .andExpect(jsonPath("$.users[9].userId").value("TSTU0010"))
                .andExpect(jsonPath("$.errorMessage").doesNotExist());

        mockMvc.perform(json("/api/users/pf7", "{}"))
                .andExpect(jsonPath("$.errorMessage").value("You are already at the top of the page..."));

        mockMvc.perform(json("/api/users/pf8", "{}"))
                .andExpect(jsonPath("$.pageNumber").value(2))
                .andExpect(jsonPath("$.users[0].userId").value("TSTU0011"))
                .andExpect(jsonPath("$.users[9].userId").value("TSTU0020"));

        mockMvc.perform(json("/api/users/pf8", "{}"))
                .andExpect(jsonPath("$.pageNumber").value(3))
                .andExpect(jsonPath("$.users.length()").value(5))
                .andExpect(jsonPath("$.users[4].userId").value("TSTU0025"))
                .andExpect(jsonPath("$.nextPageAvailable").value(false))
                .andExpect(jsonPath("$.errorMessage")
                        .value("You have reached the bottom of the page..."));

        mockMvc.perform(json("/api/users/pf8", "{}"))
                .andExpect(jsonPath("$.errorMessage")
                        .value("You are already at the bottom of the page..."));

        mockMvc.perform(json("/api/users/pf7", "{}"))
                .andExpect(jsonPath("$.pageNumber").value(2))
                .andExpect(jsonPath("$.users[0].userId").value("TSTU0011"))
                .andExpect(jsonPath("$.users[9].userId").value("TSTU0020"));

        mockMvc.perform(json("/api/users/pf7", "{}"))
                .andExpect(jsonPath("$.pageNumber").value(1))
                .andExpect(jsonPath("$.users[0].userId").value("TSTU0001"));
    }

    @Test
    void aBrowseKeyPastTheLastUserReportsTheTopOfThePage() throws Exception {
        givenUsers(3);

        mockMvc.perform(json("/api/users/enter", "{\"userId\":\"ZZZZZZZZ\"}"))
                .andExpect(jsonPath("$.users.length()").value(0))
                .andExpect(jsonPath("$.errorMessage").value("You are at the top of the page..."));
    }

    @Test
    void repositionsTheBrowseOnTheKeyedUserId() throws Exception {
        givenUsers(25);

        mockMvc.perform(json("/api/users/enter", "{\"userId\":\"TSTU0015\"}"))
                .andExpect(jsonPath("$.pageNumber").value(1))
                .andExpect(jsonPath("$.users[0].userId").value("TSTU0015"))
                .andExpect(jsonPath("$.users[9].userId").value("TSTU0024"));
    }

    @Test
    void rejectsASelectionOtherThanUpdateOrDelete() throws Exception {
        givenUsers(3);

        mockMvc.perform(json(
                        "/api/users/enter",
                        "{\"rows\":[{\"selection\":\"X\",\"userId\":\"TSTU0001\"}]}"))
                .andExpect(jsonPath("$.errorMessage").value("Invalid selection. Valid values are U and D"))
                .andExpect(jsonPath("$.users.length()").value(3));
    }

    @Test
    void selectingUserTransfersToTheUpdateProgramAndPrefillsTheScreen() throws Exception {
        givenUsers(3);

        mockMvc.perform(json(
                        "/api/users/enter",
                        "{\"rows\":[{\"selection\":\"u\",\"userId\":\"TSTU0002\"}]}"))
                .andExpect(jsonPath("$.nextProgram").value("COUSR02C"))
                .andExpect(jsonPath("$.nextTransactionId").value("CU02"));

        mockMvc.perform(get("/api/users/update").session(session))
                .andExpect(jsonPath("$.userId").value("TSTU0002"))
                .andExpect(jsonPath("$.firstName").value("First2"))
                .andExpect(jsonPath("$.errorMessage").value("Press PF5 key to save your updates ..."))
                .andExpect(jsonPath("$.messageColor").value("NEUTRAL"));
    }

    @Test
    void selectingDeleteTransfersToTheDeleteProgramWithoutThePassword() throws Exception {
        givenUsers(3);

        mockMvc.perform(json(
                        "/api/users/enter",
                        "{\"rows\":[{\"selection\":\"D\",\"userId\":\"TSTU0003\"}]}"))
                .andExpect(jsonPath("$.nextProgram").value("COUSR03C"));

        mockMvc.perform(get("/api/users/delete").session(session))
                .andExpect(jsonPath("$.userId").value("TSTU0003"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.errorMessage").value("Press PF5 key to delete this user ..."));
    }

    @Test
    void addsReadsUpdatesAndDeletesAUser() throws Exception {
        String form = """
                {"userId":"NEWUSR01","firstName":"Ada","lastName":"Lovelace",
                 "password":"SECRET01","userType":"U"}""";

        mockMvc.perform(json("/api/users/add/enter", form))
                .andExpect(jsonPath("$.errorMessage").value("User NEWUSR01 has been added ..."))
                .andExpect(jsonPath("$.messageColor").value("GREEN"))
                .andExpect(jsonPath("$.userId").doesNotExist());

        assertThat(securityUserRepository.findById("NEWUSR01")).isPresent();

        mockMvc.perform(json("/api/users/add/enter", form))
                .andExpect(jsonPath("$.errorMessage").value("User ID already exist..."));

        mockMvc.perform(json("/api/users/update/enter", "{\"userId\":\"NEWUSR01\"}"))
                .andExpect(jsonPath("$.firstName").value("Ada"))
                .andExpect(jsonPath("$.lastName").value("Lovelace"))
                .andExpect(jsonPath("$.password").value("SECRET01"))
                .andExpect(jsonPath("$.userType").value("U"))
                .andExpect(jsonPath("$.errorMessage").value("Press PF5 key to save your updates ..."));

        mockMvc.perform(json("/api/users/update/pf5", form))
                .andExpect(jsonPath("$.errorMessage").value("Please modify to update ..."))
                .andExpect(jsonPath("$.messageColor").value("RED"));

        String changed = """
                {"userId":"NEWUSR01","firstName":"Ada","lastName":"Byron",
                 "password":"SECRET02","userType":"A"}""";
        mockMvc.perform(json("/api/users/update/pf5", changed))
                .andExpect(jsonPath("$.errorMessage").value("User NEWUSR01 has been updated ..."))
                .andExpect(jsonPath("$.messageColor").value("GREEN"));

        SecurityUser stored = securityUserRepository.findById("NEWUSR01").orElseThrow();
        assertThat(stored.getLastName()).isEqualTo("Byron");
        assertThat(stored.getPassword()).isEqualTo("SECRET02");
        assertThat(stored.getUserType()).isEqualTo("A");

        mockMvc.perform(json("/api/users/delete/enter", "{\"userId\":\"NEWUSR01\"}"))
                .andExpect(jsonPath("$.errorMessage").value("Press PF5 key to delete this user ..."));

        mockMvc.perform(json("/api/users/delete/pf5", "{\"userId\":\"NEWUSR01\"}"))
                .andExpect(jsonPath("$.errorMessage").value("User NEWUSR01 has been deleted ..."))
                .andExpect(jsonPath("$.messageColor").value("GREEN"));

        assertThat(securityUserRepository.findById("NEWUSR01")).isEmpty();

        mockMvc.perform(json("/api/users/delete/enter", "{\"userId\":\"NEWUSR01\"}"))
                .andExpect(jsonPath("$.errorMessage").value("User ID NOT found..."));
    }

    @Test
    void addValidatesTheFieldsInTheCobolOrder() throws Exception {
        mockMvc.perform(json("/api/users/add/enter", "{}"))
                .andExpect(jsonPath("$.errorMessage").value("First Name can NOT be empty..."))
                .andExpect(jsonPath("$.cursorField").value("FNAME"));

        mockMvc.perform(json("/api/users/add/enter", "{\"firstName\":\"Ada\"}"))
                .andExpect(jsonPath("$.errorMessage").value("Last Name can NOT be empty..."));

        mockMvc.perform(json(
                        "/api/users/add/enter",
                        "{\"firstName\":\"Ada\",\"lastName\":\"Lovelace\"}"))
                .andExpect(jsonPath("$.errorMessage").value("User ID can NOT be empty..."));

        mockMvc.perform(json(
                        "/api/users/add/enter",
                        "{\"firstName\":\"Ada\",\"lastName\":\"Lovelace\",\"userId\":\"NEWUSR02\"}"))
                .andExpect(jsonPath("$.errorMessage").value("Password can NOT be empty..."));

        mockMvc.perform(json(
                        "/api/users/add/enter",
                        """
                        {"firstName":"Ada","lastName":"Lovelace","userId":"NEWUSR02",
                         "password":"SECRET01"}"""))
                .andExpect(jsonPath("$.errorMessage").value("User Type can NOT be empty..."))
                .andExpect(jsonPath("$.cursorField").value("USRTYPE"));

        assertThat(securityUserRepository.findById("NEWUSR02")).isEmpty();
    }

    @Test
    void updateValidatesTheFieldsAndReportsAnUnknownUser() throws Exception {
        mockMvc.perform(json("/api/users/update/enter", "{}"))
                .andExpect(jsonPath("$.errorMessage").value("User ID can NOT be empty..."));

        mockMvc.perform(json("/api/users/update/enter", "{\"userId\":\"NOSUCH01\"}"))
                .andExpect(jsonPath("$.errorMessage").value("User ID NOT found..."));

        mockMvc.perform(json("/api/users/update/pf5", "{\"userId\":\"NOSUCH01\"}"))
                .andExpect(jsonPath("$.errorMessage").value("First Name can NOT be empty..."));

        mockMvc.perform(json(
                        "/api/users/update/pf5",
                        """
                        {"userId":"NOSUCH01","firstName":"Ada","lastName":"Lovelace",
                         "password":"SECRET01","userType":"U"}"""))
                .andExpect(jsonPath("$.errorMessage").value("User ID NOT found..."));
    }

    @Test
    void deleteRequiresAUserIdAndPf12GoesBackToTheAdminMenu() throws Exception {
        mockMvc.perform(json("/api/users/delete/enter", "{}"))
                .andExpect(jsonPath("$.errorMessage").value("User ID can NOT be empty..."));

        mockMvc.perform(post("/api/users/delete/pf12").session(session))
                .andExpect(jsonPath("$.nextProgram").value("COADM01C"))
                .andExpect(jsonPath("$.nextTransactionId").value("CA00"));

        CardDemoCommarea commarea =
                (CardDemoCommarea) session.getAttribute(CardDemoCommarea.SESSION_KEY);
        assertThat(commarea.getFromProgram()).isEqualTo("COUSR03C");
        assertThat(commarea.getProgramContext()).isZero();
    }

    @Test
    void pf3OnTheUserListGoesBackToTheAdminMenu() throws Exception {
        mockMvc.perform(post("/api/users/pf3").session(session))
                .andExpect(jsonPath("$.nextProgram").value("COADM01C"));
    }

    @Test
    void anyOtherKeyIsRejectedOnEveryScreen() throws Exception {
        mockMvc.perform(json("/api/users/other-key", "{}"))
                .andExpect(jsonPath("$.errorMessage").value("Invalid key pressed. Please see below..."));
        mockMvc.perform(json("/api/users/add/other-key", "{}"))
                .andExpect(jsonPath("$.errorMessage").value("Invalid key pressed. Please see below..."));
        mockMvc.perform(json("/api/users/update/other-key", "{}"))
                .andExpect(jsonPath("$.errorMessage").value("Invalid key pressed. Please see below..."));
        mockMvc.perform(json("/api/users/delete/other-key", "{}"))
                .andExpect(jsonPath("$.errorMessage").value("Invalid key pressed. Please see below..."));
    }

    @Test
    void withoutACommareaTheUserScreensReturnToTheSignonProgram() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(jsonPath("$.nextProgram").value("COSGN00C"));
        mockMvc.perform(get("/api/users/add"))
                .andExpect(jsonPath("$.nextProgram").value("COSGN00C"));
    }
}
