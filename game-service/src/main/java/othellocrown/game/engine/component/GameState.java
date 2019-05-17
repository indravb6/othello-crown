package othellocrown.game.engine.component;

public class GameState {
    private Box[][] boxes;
    private GameStatus gameStatus;
    private String player1;
    private String player2;

    public GameState() {
    }

    public GameState(Box[][] boxes, GameStatus gameStatus, String player1, String player2) {
        this.boxes = boxes;
        this.gameStatus = gameStatus;
        this.player1 = player1;
        this.player2 = player2;
    }

    public Box[][] getBoxes() {
        return boxes;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public String getPlayer1() {
        return player1;
    }

    public String getPlayer2() {
        return player2;
    }
}
