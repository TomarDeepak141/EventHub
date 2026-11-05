package eventHub.deepak.controller;

import eventHub.deepak.globalExceptionHandler.GlobalExceptionHandler;
import eventHub.deepak.service.interfaces.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.mock;

class AuthControllerTest {
    @Test void rejectsInvalidLoginPayloadWithFieldErrors() throws Exception {
        AuthService service=mock(AuthService.class);
        MockMvc mvc=standaloneSetup(new AuthController(service)).setControllerAdvice(new GlobalExceptionHandler()).build();
        mvc.perform(post("/auth/login").contentType("application/json").content("{\"email\":\"bad\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.email").exists()).andExpect(jsonPath("$.errors.password").exists());
    }
}
