package othellocrown.scoreboard.repository;

import jdk.nashorn.internal.ir.annotations.Reference;
import org.springframework.data.repository.Repository;
import othellocrown.scoreboard.entity.Scoreboard;

@Reference
public interface JpaScoreboardRepository extends Repository<Scoreboard, Long>,
        ScoreboardRepository {
}
