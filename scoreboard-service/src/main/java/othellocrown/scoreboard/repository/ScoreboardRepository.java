package othellocrown.scoreboard.repository;

import othellocrown.scoreboard.entity.Scoreboard;

import java.util.List;
import java.util.Optional;

public interface ScoreboardRepository {
    Optional<Scoreboard> findByUsername(String username);

    List<Scoreboard> findTop10ByOrderByScoreDescTotalWinDesc();

    Scoreboard save(Scoreboard scoreboard);
}
