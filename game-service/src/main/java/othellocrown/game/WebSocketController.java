package othellocrown.game;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import othellocrown.game.engine.component.Board;
import othellocrown.game.engine.component.GameState;
import othellocrown.game.engine.GameRoom;
import othellocrown.game.common.utils.Jwt;
import othellocrown.game.model.*;

@Controller
public class WebSocketController {
    private SimpMessagingTemplate messagingTemplate;
    private GameManager gameManager;
    private Jwt jwt;

    public WebSocketController(SimpMessagingTemplate messagingTemplate,
                               GameManager gameManager,
                               Jwt jwt) {
        this.gameManager = gameManager;
        this.messagingTemplate = messagingTemplate;
        this.jwt = jwt;
    }

    @MessageMapping("/play")
    public void play(Command command) {
        String account = jwt.decode(command.getToken());
        GameRoom gameRoom = gameManager.find(room -> room.getRoomId().equals(command.getRoomId()));
        Board board = gameRoom.play(account, command.getCoordinate());
        String path = "/watch/" + gameRoom.getSpectatorToken();

        GameState gameState = new GameState(board.getBoxes(),
                gameRoom.getGameStatus(),
                gameRoom.getPlayer1(),
                gameRoom.getPlayer2());

        messagingTemplate.convertAndSend(path, gameState);
    }

    @SubscribeMapping("/{spectatorToken}")
    public GameState onSubstribe(@DestinationVariable String spectatorToken) {
        GameRoom gameRoom = gameManager.find(room ->
                room.getSpectatorToken().equals(spectatorToken));
        Board board = gameRoom.getBoard();

        return new GameState(board.getBoxes(),
                gameRoom.getGameStatus(),
                gameRoom.getPlayer1(),
                gameRoom.getPlayer2());
    }

    @MessageMapping("/chat/{spectatorToken}")
    public void sendChat(@DestinationVariable String spectatorToken, ChatData chatData) {
        String account = jwt.decode(chatData.getToken());

        if (!chatData.getMessage().equals("")) {
            Chat chat = new Chat(account, chatData.getMessage());
            messagingTemplate.convertAndSend("/chat/" + spectatorToken, chat);
        }
    }
}
