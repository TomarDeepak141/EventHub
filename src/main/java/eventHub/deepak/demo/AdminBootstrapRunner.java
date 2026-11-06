package eventHub.deepak.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration(proxyBeanMethods = false)
public class AdminBootstrapRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapRunner.class);

    @Bean
    @ConditionalOnProperty(name = "ADMIN_BOOTSTRAP_EMAIL")
    ApplicationRunner bootstrapAdmin(AdminBootstrapService service,
                                     org.springframework.core.env.Environment environment) {
        return args -> {
            String email = environment.getProperty("ADMIN_BOOTSTRAP_EMAIL", "");
            if (!StringUtils.hasText(email)) {
                return;
            }
            if (service.promoteExistingAccount(email)) {
                log.info("Administrator bootstrap matched an existing account");
            } else {
                log.warn("Administrator bootstrap did not find an existing account");
            }
        };
    }
}
