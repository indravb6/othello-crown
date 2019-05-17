package othellocrown.scoreboard.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import othellocrown.scoreboard.entity.Scoreboard;

import java.util.List;
import java.util.Optional;

@Component
@Qualifier("cached")
public class CachedScoreboardRepository implements ScoreboardRepository {
    private JpaScoreboardRepository jpaScoreboardRepository;
    private List<Scoreboard> scoreboards;

    public CachedScoreboardRepository(JpaScoreboardRepository jpaScoreboardRepository) {
        this.jpaScoreboardRepository = jpaScoreboardRepository;
        updateCache();
    }

    @Override
    public Optional<Scoreboard> findByUsername(String username) {
        return jpaScoreboardRepository.findByUsername(username);
    }

    @Override
    public List<Scoreboard> findTop10ByOrderByScoreDescTotalWinDesc() {
        return scoreboards;
    }

    @Override
    public Scoreboard save(Scoreboard scoreboard) {
        Scoreboard savedScoreboard = jpaScoreboardRepository.save(scoreboard);
        updateCache();
        return savedScoreboard;
    }

    private void updateCache() {
        scoreboards = jpaScoreboardRepository.findTop10ByOrderByScoreDescTotalWinDesc();
    }
}
