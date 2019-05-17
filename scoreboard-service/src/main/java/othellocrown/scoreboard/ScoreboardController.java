package othellocrown.scoreboard;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import othellocrown.scoreboard.common.error.UnauthorizedException;
import othellocrown.scoreboard.entity.Scoreboard;
import othellocrown.scoreboard.model.ScoreboardCreateData;
import othellocrown.scoreboard.repository.JpaScoreboardRepository;
import othellocrown.scoreboard.repository.ScoreboardRepository;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin
public class ScoreboardController {


    @Value("${appKey}")
    private String appKey;
    private ScoreboardRepository scoreboardRepository;

    public ScoreboardController(@Qualifier("cached") ScoreboardRepository scoreboardRepository) {
        this.scoreboardRepository = scoreboardRepository;
    }

    @PostMapping("/scoreboards/")
    public Scoreboard create(@RequestHeader(value = "Authorization") String key,
                             @RequestBody ScoreboardCreateData scoreboardCreateData) {
        if (!appKey.equals(key)) {
            throw new UnauthorizedException("Credential invalid!");
        }

        String username = scoreboardCreateData.getUsername();
        Optional<Scoreboard> maybeScoreboard = scoreboardRepository.findByUsername(username);

        Scoreboard scoreboard;
        if (maybeScoreboard.isPresent()) {
            scoreboard = maybeScoreboard.get();
        } else {
            scoreboard = new Scoreboard(scoreboardCreateData.getUsername());
        }

        if (scoreboardCreateData.getIsWinner()) {
            scoreboard.addScore(25);
            scoreboard.setOnWin();
        } else {
            scoreboard.addScore(-25);
            scoreboard.setOnDefeat();
        }

        scoreboardRepository.save(scoreboard);
        return scoreboard;
    }

    @GetMapping("/scoreboards/")
    public List<Scoreboard> getTop10Scoreboard() {
        return scoreboardRepository.findTop10ByOrderByScoreDescTotalWinDesc();
    }

    @GetMapping("/ping/")
    public void ping() {

    }
}
