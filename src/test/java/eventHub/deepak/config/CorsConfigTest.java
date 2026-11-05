package eventHub.deepak.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;

import static org.junit.jupiter.api.Assertions.*;

class CorsConfigTest {
    @Test
    void allowsOnlyExplicitOriginsAndDoesNotEnableCredentials() {
        var source = new CorsConfig().corsConfigurationSource("https://events.example.test, https://admin.example.test");
        CorsConfiguration configuration = source.getCorsConfiguration(new MockHttpServletRequest());

        assertNotNull(configuration);
        assertEquals("https://events.example.test", configuration.checkOrigin("https://events.example.test"));
        assertNull(configuration.checkOrigin("https://attacker.example.test"));
        assertNull(configuration.checkOrigin("*"));
        assertFalse(configuration.getAllowCredentials());
    }

    @Test
    void emptyOriginListDoesNotEnableCrossOriginRequests() {
        var source = new CorsConfig().corsConfigurationSource("");
        CorsConfiguration configuration = source.getCorsConfiguration(new MockHttpServletRequest());

        assertNotNull(configuration);
        assertNull(configuration.checkOrigin("https://events.example.test"));
    }

    @Test
    void rejectsWildcardOrigins() {
        assertThrows(IllegalArgumentException.class,
                () -> new CorsConfig().corsConfigurationSource("*"));
    }
}
