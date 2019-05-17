package othellocrown.game.model;

public class ScoreboardData {
    private String username;
    private boolean isWinner;

    public ScoreboardData(String username, boolean isWinner) {
        this.username = username;
        this.isWinner = isWinner;
    }
}
