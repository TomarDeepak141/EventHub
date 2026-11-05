package eventHub.deepak;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import eventHub.deepak.entity.User;
import eventHub.deepak.enums.Role;
import eventHub.deepak.repository.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PublicEndpointSecurityTest {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void anonymousCustomersCanDiscoverAndViewEvents() throws Exception {
        mvc.perform(get("/events")).andExpect(status().isOk());
        mvc.perform(get("/events/999999")).andExpect(status().isNotFound());
    }

    @Test
    void anonymousUsersCanReachRegistrationAndLoginValidation() throws Exception {
        mvc.perform(post("/user/register").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void duplicateRegistrationReturnsEmailConflictWithSignInGuidance() throws Exception {
        users.saveAndFlush(User.builder().name("Legacy account").email("Legacy.Case@eventhub.test")
                .password("encoded-password").role(Role.CUSTOMER).isActive(true).build());

        mvc.perform(post("/user/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New account\",\"email\":\"legacy.case@eventhub.test\",\"password\":\"password123\"}"))
                .andExpect(status().isConflict())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.message").value("An account with this email already exists. Sign in or use a different email."));
    }

    @Test
    @Transactional
    void loginFindsLegacyAccountRegardlessOfEmailCase() throws Exception {
        users.saveAndFlush(User.builder().name("Legacy account").email("Legacy.Case@eventhub.test")
                .password(passwordEncoder.encode("password123")).role(Role.CUSTOMER).isActive(true).build());

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"legacy.case@eventhub.test\",\"password\":\"password123\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void accountAndOrganizerEventRoutesRemainProtected() throws Exception {
        mvc.perform(get("/user/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/events/mine")).andExpect(status().isUnauthorized());
    }

    @Test
    void reviewEligibilityRemainsProtected() throws Exception {
        mvc.perform(get("/events/999999/reviews/eligibility")).andExpect(status().isUnauthorized());
    }
}
