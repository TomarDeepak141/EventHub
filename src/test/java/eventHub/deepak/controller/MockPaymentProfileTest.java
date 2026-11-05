package eventHub.deepak.controller;

import eventHub.deepak.service.payment.PaymentProvider;
import eventHub.deepak.service.payment.MockPaymentProvider;
import eventHub.deepak.service.payment.UnavailablePaymentProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import eventHub.deepak.service.payment.PaymentService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class MockPaymentProfileTest {
    private final ApplicationContextRunner contexts = new ApplicationContextRunner()
            .withUserConfiguration(Profiles.class);

    @Test
    void simulationEndpointIsUnavailableWithoutExplicitDemoProfile() {
        contexts.withInitializer(context -> context.getEnvironment().setActiveProfiles("prod")).run(context -> {
            assertTrue(context.getBeansOfType(MockPaymentController.class).isEmpty());
            assertInstanceOf(UnavailablePaymentProvider.class, context.getBean(PaymentProvider.class));
            assertFalse(context.getBean(PaymentProvider.class).isEnabled());
        });
    }

    @Test
    void mockProviderAndSimulationEndpointAreEnabledOnlyInExplicitLocalProfile() {
        contexts.withInitializer(context -> context.getEnvironment().setActiveProfiles("demo")).run(context -> {
            assertEquals(1, context.getBeansOfType(MockPaymentController.class).size());
            assertInstanceOf(MockPaymentProvider.class, context.getBean(PaymentProvider.class));
            assertTrue(context.getBean(PaymentProvider.class).isEnabled());
        });
    }

    @Test
    void productionProfileWinsIfDemoIsAlsoAccidentallyActive() {
        contexts.withInitializer(context -> context.getEnvironment().setActiveProfiles("prod", "demo")).run(context -> {
            assertTrue(context.getBeansOfType(MockPaymentController.class).isEmpty());
            assertInstanceOf(UnavailablePaymentProvider.class, context.getBean(PaymentProvider.class));
            assertFalse(context.getBean(PaymentProvider.class).isEnabled());
        });
    }

    @Test
    void explicitDevelopmentProfileEnablesTheSandbox() {
        contexts.withInitializer(context -> context.getEnvironment().setActiveProfiles("development")).run(context -> {
            assertEquals(1, context.getBeansOfType(MockPaymentController.class).size());
            assertInstanceOf(MockPaymentProvider.class, context.getBean(PaymentProvider.class));
        });
    }

    @Configuration(proxyBeanMethods = false)
    @org.springframework.context.annotation.Import({MockPaymentController.class, MockPaymentProvider.class, UnavailablePaymentProvider.class})
    static class Profiles {
        @Bean PaymentService paymentService() { return mock(PaymentService.class); }
    }
}
