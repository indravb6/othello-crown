package othellocrown.game;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import othellocrown.game.engine.component.Box;
import othellocrown.game.engine.component.GameState;
import othellocrown.game.engine.component.GameStatus;
import othellocrown.game.engine.GameRoom;
import othellocrown.game.common.error.BadRequestException;
import othellocrown.game.common.utils.Jwt;
import othellocrown.game.model.*;

@RestController
@CrossOrigin
public class GameController {
    private Jwt jwt;
    private GameManager gameManager;
    private SimpMessagingTemplate messagingTemplate;

    public GameController(Jwt jwt,
                          GameManager gameManager,
                          SimpMessagingTemplate messagingTemplate) {
        this.jwt = jwt;
        this.gameManager = gameManager;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/create/")
    public CreateRoomResponse createRoom(@RequestHeader("Authorization") String token) {
        String account = jwt.decode(token);

        GameRoom gameRoom = gameManager.create();
        gameRoom.setPlayer1(account);

        return new CreateRoomResponse(gameRoom.getRoomId(),
                gameRoom.getInvitationToken(),
                gameRoom.getSpectatorToken());
    }

    @PostMapping("/join/{invitationToken}/")
    public JoinRoomResponse joinRoom(@RequestHeader("Authorization") String token,
                                     @PathVariable("invitationToken") String invitationToken) {
        String account = jwt.decode(token);
        GameRoom gameRoom = gameManager.find(room ->
                room.getInvitationToken().equals(invitationToken));
        gameRoom.join(account);

        String player1 = gameRoom.getPlayer1();
        String player2 = gameRoom.getPlayer2();

        return new JoinRoomResponse(player1,
                player2,
                gameRoom.getSpectatorToken(),
                gameRoom.getRoomId());
    }

    @PostMapping("/watch/{spectatorToken}/")
    public RoomInfo watchRoom(@PathVariable("spectatorToken") String spectatorToken) {
        GameRoom gameRoom = gameManager.find(room ->
                room.getSpectatorToken().equals(spectatorToken));

        String player1 = gameRoom.getPlayer1();
        String player2 = gameRoom.getPlayer2();

        return new RoomInfo(gameRoom.getRoomId(), player1, player2);
    }

    @PostMapping("/leave/")
    public void leave(@RequestHeader("Authorization") String token) {
        String account = jwt.decode(token);

        GameRoom gameRoom;
        try {
            gameRoom = gameManager.find(room -> room.isPlayer(account));
        } catch (BadRequestException exception) {
            throw new BadRequestException("You aren't in a game");
        }

        if (!gameRoom.getGameStatus().equals(GameStatus.WAITING_OPPONENT)) {
            String player1 = gameRoom.getPlayer1();
            String player2 = gameRoom.getPlayer2();
            String path = "/watch/" + gameRoom.getSpectatorToken();
            GameStatus gameStatus = player1.equals(account)
                    ? GameStatus.PLAYER1_LEFT : GameStatus.PLAYER2_LEFT;
            Box[][] boxes = gameRoom.getBoard().getBoxes();
            GameState gameState = new GameState(boxes, gameStatus, player1, player2);
            messagingTemplate.convertAndSend(path, gameState);
        }

        gameManager.remove(gameRoom.getRoomId());
    }

    @GetMapping("/ping/")
    public void ping() { }

    @GetMapping("/storage/")
    public PlayerStorage getStorage(@RequestHeader("Authorization") String token) {
        String account = jwt.decode(token);

        GameRoom gameRoom;
        try {
            gameRoom = gameManager.find(room -> room.isPlayer(account));
        } catch (BadRequestException exception) {
            return new PlayerStorage();
        }

        String status = (gameRoom.getPlayer1().equals(account) ? "player1" : "player2");

        return new PlayerStorage(gameRoom.getSpectatorToken(),
                gameRoom.getInvitationToken(),
                gameRoom.getRoomId(),
                status,
                gameRoom.getPlayer1(),
                gameRoom.getPlayer2());
    }
}
