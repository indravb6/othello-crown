package othellocrown.game.engine.component;

public enum GameStatus {
    WAITING_OPPONENT,
    GAME_STARTED,
    PLAYER1_TURN,
    PLAYER2_TURN,
    PLAYER1_LEFT,
    PLAYER2_LEFT,
    GAME_OVER_PLAYER1_WINNER,
    GAME_OVER_PLAYER2_WINNER;

    public boolean isGameOver() {
        return (this.equals(GAME_OVER_PLAYER1_WINNER)
                || this.equals(GAME_OVER_PLAYER2_WINNER));
    }
}
