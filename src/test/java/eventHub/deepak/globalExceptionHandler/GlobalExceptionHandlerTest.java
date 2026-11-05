package eventHub.deepak.globalExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void uniqueViolationDuringRegistrationGetsDuplicateEmailGuidance() {
        HttpServletRequest request = registrationRequest();
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
                "duplicate key", new SQLException("duplicate key", "23505"));

        var response = handler.handleConflict(exception, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("An account with this email already exists. Sign in or use a different email.",
                response.getBody().message());
    }

    @Test
    void checkViolationDuringRegistrationIsNotReportedAsDuplicateEmail() {
        HttpServletRequest request = registrationRequest();
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
                "check violation", new SQLException("violates role check", "23514"));

        var response = handler.handleConflict(exception, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("The request conflicts with existing data", response.getBody().message());
    }

    private MockHttpServletRequest registrationRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/user/register");
        return request;
    }
}
