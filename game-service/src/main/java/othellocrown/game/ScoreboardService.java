package othellocrown.game;


import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import othellocrown.game.model.ScoreboardData;

@Component
public class ScoreboardService {
    @Value("${service.scoreboard}")
    private String scoreboardServiceBaseUrl;

    @Value("${jwt.key}")
    private String privateKey;

    private RestTemplate restTemplate;

    public ScoreboardService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", privateKey);

        return headers;
    }

    @Async
    public void update(ScoreboardData scoreboardData) {
        String url = scoreboardServiceBaseUrl + "scoreboards/";

        HttpHeaders headers = createHeaders();

        Gson gson = new Gson();
        String body = gson.toJson(scoreboardData);

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        try {
            restTemplate.postForObject(url, request, String.class);
        } catch (Exception exception) {
            System.out.println("Can't update the scoreboard");
        }
    }
}