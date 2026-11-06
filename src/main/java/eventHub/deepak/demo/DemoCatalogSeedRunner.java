package eventHub.deepak.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class DemoCatalogSeedRunner {
    private static final Logger log = LoggerFactory.getLogger(DemoCatalogSeedRunner.class);

    @Bean
    @ConditionalOnProperty(name = "SEED_DEMO_CATALOG", havingValue = "true")
    ApplicationRunner seedDemoCatalog(DemoCatalogSeeder seeder) {
        return args -> {
            seeder.seed();
            log.info("Opt-in EventHub demo catalog is ready");
        };
    }
}
