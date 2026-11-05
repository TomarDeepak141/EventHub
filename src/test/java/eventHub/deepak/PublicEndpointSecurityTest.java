package eventHub.deepak;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PublicEndpointSecurityTest {
    @Autowired MockMvc mvc;

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
    void accountAndOrganizerEventRoutesRemainProtected() throws Exception {
        mvc.perform(get("/user/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/events/mine")).andExpect(status().isUnauthorized());
    }

    @Test
    void reviewEligibilityRemainsProtected() throws Exception {
        mvc.perform(get("/events/999999/reviews/eligibility")).andExpect(status().isUnauthorized());
    }
}
