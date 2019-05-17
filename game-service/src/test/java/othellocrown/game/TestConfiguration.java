package othellocrown.game;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.web.client.RestTemplate;

@Configuration
@Profile("test")
class TestConfiguration implements AsyncConfigurer {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}