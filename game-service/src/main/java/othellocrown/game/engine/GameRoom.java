package othellocrown.game.engine;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import othellocrown.game.engine.component.*;
import othellocrown.game.ScoreboardService;
import othellocrown.game.common.error.BadRequestException;
import othellocrown.game.common.utils.Helper;
import othellocrown.game.model.*;

public class GameRoom {
    private static Integer ROOM_COUNTER = 1;

    private Integer roomId;
    private String player1;
    private String player2;
    private String invitationToken;
    private String spectatorToken;
    private Game game = new Game();
    private GameStatus gameStatus;
    private SimpMessagingTemplate messagingTemplate;
    private ScoreboardService scoreboardService;

    public GameRoom(SimpMessagingTemplate messagingTemplate, ScoreboardService scoreboardService) {
        roomId = ROOM_COUNTER++;

        Helper helper = new Helper();
        invitationToken = helper.createToken(16);
        spectatorToken = helper.createToken(5);

        gameStatus = GameStatus.WAITING_OPPONENT;

        this.messagingTemplate = messagingTemplate;
        this.scoreboardService = scoreboardService;
    }

    public void join(String account) {
        if (player2 != null) {
            if (getPlayer2().equals(account)) {
                return;
            } else {
                throw new BadRequestException("game room is already full");
            }
        }
        setPlayer2(account);
        gameStatus = GameStatus.GAME_STARTED;

        String path = "/watch/" + getSpectatorToken();
        Box[][] boxes = game.getBoard().getBoxes();
        GameState gameState = new GameState(boxes, gameStatus, player1, player2);

        messagingTemplate.convertAndSend(path, gameState);
    }

    private void switchPlayer() {
        if (gameStatus.equals(GameStatus.PLAYER1_TURN)
                || gameStatus.equals(GameStatus.GAME_STARTED)) {
            gameStatus = GameStatus.PLAYER2_TURN;
        } else {
            gameStatus = GameStatus.PLAYER1_TURN;
        }
    }

    private void confirmCanPlay(String account) {
        String currentPlayer = getCurrentPlayer();
        if (!currentPlayer.equals(account)) {
            throw new RuntimeException("Invalid move");
        }
    }

    public Board play(String account, Coordinate coordinate) {
        confirmCanPlay(account);
        Box box = getBox(account);
        Board board = game.play(box, coordinate);

        if (game.isPass()) {
            game.updateClickable(box);
            if (game.isPass()) {
                Box winnerBox = game.getTheWinner();
                if (winnerBox.equals(Box.BLACK)) {
                    gameStatus = GameStatus.GAME_OVER_PLAYER1_WINNER;
                    scoreboardService.update(new ScoreboardData(player1, true));
                    scoreboardService.update(new ScoreboardData(player2, false));
                } else {
                    gameStatus = GameStatus.GAME_OVER_PLAYER2_WINNER;
                    scoreboardService.update(new ScoreboardData(player1, false));
                    scoreboardService.update(new ScoreboardData(player2, true));
                }
            }
        } else {
            switchPlayer();
        }

        return board;
    }

    public Board getBoard() {
        return game.getBoard();
    }

    private Box getBox(String account) {
        if (player1.equals(account)) {
            return Box.BLACK;
        } else {
            return Box.WHITE;
        }
    }

    public void setPlayer1(String player1) {
        this.player1 = player1;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    private void setPlayer2(String player2) {
        this.player2 = player2;
    }

    public String getPlayer1() {
        return player1;
    }

    public String getPlayer2() {
        return player2;
    }

    public Integer getRoomId() {
        return roomId;
    }

    private String getCurrentPlayer() {
        if (gameStatus.equals(GameStatus.PLAYER1_TURN)
                || gameStatus.equals(GameStatus.GAME_STARTED)) {
            return player1;
        } else {
            return player2;
        }
    }

    public boolean isPlayer(String player) {
        return player1.equals(player) || (player2 != null && player2.equals(player));
    }

    public String getInvitationToken() {
        return invitationToken;
    }

    public String getSpectatorToken() {
        return spectatorToken;
    }
}
