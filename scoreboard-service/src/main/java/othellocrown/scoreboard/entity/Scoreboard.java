package othellocrown.scoreboard.entity;

import org.hibernate.validator.constraints.Length;
import othellocrown.scoreboard.common.entity.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "scoreboard")
public class Scoreboard extends BaseEntity {
    @Length(min = 3, max = 10)
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false)
    private int totalWin;

    @Column(nullable = false)
    private int totalDefeat;

    public Scoreboard() {

    }

    public Scoreboard(String username) {
        this.username = username;
        this.score = 0;
        this.totalDefeat = 0;
        this.totalWin = 0;
    }

    public String getUsername() {
        return username;
    }

    public int getScore() {
        return score;
    }

    public int getTotalWin() {
        return totalWin;
    }

    public int getTotalDefeat() {
        return totalDefeat;
    }

    public void setOnWin() {
        this.totalWin = this.totalWin + 1;
    }

    public void setOnDefeat() {
        this.totalDefeat = this.totalDefeat + 1;
    }

    public void addScore(int score) {
        this.score = this.score + score;
    }
}
