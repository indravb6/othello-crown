package othellocrown.game;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import othellocrown.game.common.error.BadRequestException;
import othellocrown.game.engine.GameRoom;

import java.util.HashMap;

@Component
public class GameManager {
    private HashMap<Integer, GameRoom> gameRooms = new HashMap<>();
    private SimpMessagingTemplate messagingTemplate;
    private ScoreboardService scoreboardService;

    public GameManager(SimpMessagingTemplate messagingTemplate,
                       ScoreboardService scoreboardService) {
        this.messagingTemplate = messagingTemplate;
        this.scoreboardService = scoreboardService;
    }

    GameRoom create() {
        GameRoom gameRoom = new GameRoom(messagingTemplate, scoreboardService);
        int gameRoomId = gameRoom.getRoomId();
        gameRooms.put(gameRoomId, gameRoom);
        return gameRoom;
    }

    void remove(Integer gameRoomId) {
        gameRooms.remove(gameRoomId);
    }

    public interface FindStrategy {
        boolean isMatch(GameRoom gameRoom);
    }

    GameRoom find(FindStrategy findStrategy) {
        for (HashMap.Entry<Integer, GameRoom> gameRoomWithKey : gameRooms.entrySet()) {
            GameRoom gameRoom = gameRoomWithKey.getValue();
            if (findStrategy.isMatch(gameRoom)) {
                return gameRoom;
            }
        }

        throw new BadRequestException("Game room not found");
    }
}
