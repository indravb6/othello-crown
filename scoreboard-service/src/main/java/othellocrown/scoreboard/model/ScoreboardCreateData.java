package othellocrown.scoreboard.model;

public class ScoreboardCreateData {
    private String username;
    private boolean isWinner;

    public ScoreboardCreateData() {

    }

    public ScoreboardCreateData(String username, boolean isWinner) {
        this.username = username;
        this.isWinner = isWinner;
    }

    public String getUsername() {
        return username;
    }

    public boolean getIsWinner() {
        return isWinner;
    }
}
