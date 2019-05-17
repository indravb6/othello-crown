package othellocrown.game.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;

@Configuration
@Profile("production")
@EnableAsync
public class ApplicationConfiguration implements AsyncConfigurer {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
