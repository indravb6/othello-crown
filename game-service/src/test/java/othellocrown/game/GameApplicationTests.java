package othellocrown.game;

import com.google.gson.Gson;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import othellocrown.game.engine.component.Box;
import othellocrown.game.engine.component.Coordinate;
import othellocrown.game.engine.component.GameState;
import othellocrown.game.engine.component.GameStatus;
import othellocrown.game.GameStep.GameStepPlayer1Win;
import othellocrown.game.GameStep.GameStepPlayer2Win;
import othellocrown.game.StompHandler.ChatStompHandler;
import othellocrown.game.StompHandler.GameStompHandler;
import othellocrown.game.common.utils.Jwt;
import othellocrown.game.model.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = GameApplication.class,
        properties = "spring.profiles.active=test",
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = "test")
public class GameApplicationTests {
    private Gson gson = new Gson();

    private String account1 = "Andi";
    private String account2 = "Budi";
    private String account3 = "Caca";

    private Integer roomId;
    private String token1, token2, token3;
    private String invitationToken, spectatorToken;

    private WebSocketStompClient stompClient1, stompClient2;
    private StompSession stompSession1, stompSession2;
    private GameStompHandler gameHandler1, gameHandler2;
    private ChatStompHandler chatHandler1, chatHandler2;

    @Autowired
    private Jwt jwt;

    @Autowired
    private RestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Value("${service.scoreboard}")
    private String scoreboardServiceBaseUrl;

    private MockRestServiceServer mockScoreboardServer;

    @Before
    public void setup() {
        RestAssured.baseURI = String.format("http://localhost:%s", port);
        token1 = "Bearer " + jwt.encode(account1);
        token2 = "Bearer " + jwt.encode(account2);
        token3 = "Bearer " + jwt.encode(account3);
        mockScoreboardServer = MockRestServiceServer.createServer(restTemplate);
    }

    private RequestSpecification newRequest(String credentials) {
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json");
        if (credentials != null) {
            request.header(new Header("Authorization", credentials));
        }
        return request;
    }

    private void guestCanPing() {
        Response response = newRequest(null).get("/ping/");
        Assert.assertEquals(200, response.getStatusCode());
    }

    private void user1CanGetHisBlankPlayerStorage() {
        Response response = newRequest(token1).get("/storage/");
        PlayerStorage playerStorage = gson.fromJson(response.getBody().asString(), PlayerStorage.class);
        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertNull(playerStorage.getPlayer1());
        Assert.assertNull(playerStorage.getPlayer2());
        Assert.assertNull(playerStorage.getRoomId());
        Assert.assertNull(playerStorage.getStatus());
    }

    private void guestCantCreateGameRoom() {
        Response response = newRequest(null).post("/create/");
        Assert.assertEquals(400, response.getStatusCode());
    }

    private void hackerCantCreateGameRoom() {
        Response response = newRequest("fake token").post("/create/");
        Assert.assertEquals(401, response.getStatusCode());
    }

    private void user3CantLeave() {
        Response response = newRequest(token3).post("/leave/");
        Map responseBody = gson.fromJson(response.getBody().asString(), Map.class);
        Assert.assertEquals(400, response.getStatusCode());
        Assert.assertEquals(responseBody.get("message"), "You aren't in a game");
    }

    private void user3CantGetHisPlayerStorage() {
        Response response = newRequest(token3).get("/storage/");
        Assert.assertEquals(200, response.getStatusCode());
    }

    private void user1CanCreateGameRoom() {
        Response response = newRequest(token1).post("/create/");
        Assert.assertEquals(200, response.getStatusCode());

        CreateRoomResponse createRoomResponse = gson.fromJson(response.getBody().asString(), CreateRoomResponse.class);

        roomId = createRoomResponse.getRoomId();
        invitationToken = createRoomResponse.getInvitationToken();
        spectatorToken = createRoomResponse.getSpectatorToken();

        Assert.assertEquals(16, invitationToken.length());
        Assert.assertEquals(5, spectatorToken.length());
    }

    private void user1CanGetHisPlayerStorageBeforePlayer2Join() {
        Response response = newRequest(token1).get("/storage/");
        PlayerStorage playerStorage = gson.fromJson(response.getBody().asString(), PlayerStorage.class);
        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals(account1, playerStorage.getPlayer1());
        Assert.assertEquals(roomId, playerStorage.getRoomId());
        Assert.assertEquals("player1", playerStorage.getStatus());
    }

    private void user1CanLeave() {
        Response response = newRequest(token1).post("/leave/");
        Assert.assertEquals(200, response.getStatusCode());
    }

    private void user2CantJoinGameRoomWithInvalidInvitationToken() {
        Response response = newRequest(token1).post("/join/12345/");
        Map responseBody = gson.fromJson(response.getBody().asString(), Map.class);
        Assert.assertEquals(400, response.getStatusCode());
        Assert.assertEquals(responseBody.get("message"), "Game room not found");
    }

    private void user2CanJoinGameRoom() {
        Response response = newRequest(token2).post("/join/" + invitationToken + "/");
        Assert.assertEquals(200, response.getStatusCode());
    }

    private void user2CanGetHisPlayerStorage() {
        Response response = newRequest(token2).get("/storage/");
        PlayerStorage playerStorage = gson.fromJson(response.getBody().asString(), PlayerStorage.class);
        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals(account1, playerStorage.getPlayer1());
        Assert.assertEquals(account2, playerStorage.getPlayer2());
        Assert.assertEquals(roomId, playerStorage.getRoomId());
        Assert.assertEquals("player2", playerStorage.getStatus());
    }

    private void user2CanJoinAgainGameRoom() {
        Response response = newRequest(token2).post("/join/" + invitationToken + "/");
        Assert.assertEquals(200, response.getStatusCode());
    }

    private void user3CantJoinGameRoom() {
        Response response = newRequest(token3).post("/join/" + invitationToken + "/");
        Assert.assertEquals(400, response.getStatusCode());
    }

    private void user3CantWatchGameRoomWithInvalidSpectatorToken() {
        Response response = newRequest(token3).post("/watch/12345/");
        Assert.assertEquals(400, response.getStatusCode());
    }

    private void user3CanWatchGameRoom() {
        Response response = newRequest(token3).post("/watch/" + spectatorToken + "/");
        Assert.assertEquals(200, response.getStatusCode());
    }

    private void user1CanSubscribeGameRoom() throws InterruptedException, ExecutionException, TimeoutException  {
        stompClient1 = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient1.setMessageConverter(new MappingJackson2MessageConverter());

        stompSession1 = stompClient1.connect("ws://localhost:" + port + "/ws",
                new StompSessionHandlerAdapter() {}).get(1, TimeUnit.SECONDS);

        gameHandler1 = new GameStompHandler();
        stompSession1.subscribe("/watch/" + spectatorToken, gameHandler1);
        GameState response = gameHandler1.get();
        Assert.assertEquals(Box.CLICKABLE_BLACK, response.getBoxes()[3][2]);
    }

    private void user2CanSubscribeGameRoom() throws InterruptedException, ExecutionException, TimeoutException  {
        stompClient2 = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient2.setMessageConverter(new MappingJackson2MessageConverter());

        stompSession2 = stompClient2.connect("ws://localhost:" + port + "/ws",
                new StompSessionHandlerAdapter() {}).get(1, TimeUnit.SECONDS);

        gameHandler2 = new GameStompHandler();
        stompSession2.subscribe("/watch/" + spectatorToken, gameHandler2);
        GameState response = gameHandler2.get();
        Assert.assertEquals(Box.CLICKABLE_BLACK, response.getBoxes()[3][2]);
    }

    private void user1CantPlayWithInvalidRoomId() throws InterruptedException, ExecutionException {
        Command command = new Command(token1, roomId + 1, new Coordinate(3, 2));
        stompSession1.send("/game/play", command);
        try {
            gameHandler1.get();
            Assert.fail();
        } catch (TimeoutException exception) {
        }
    }

    private void user1CanPlay() throws InterruptedException, ExecutionException, TimeoutException {
        Command command = new Command(token1, roomId, new Coordinate(3, 2));
        stompSession1.send("/game/play", command);
        GameState response = gameHandler1.get();
        gameHandler2.get();

        Assert.assertEquals(Box.BLACK, response.getBoxes()[3][2]);
        Assert.assertEquals(Box.CLICKABLE_WHITE, response.getBoxes()[4][2]);
    }

    private void user1CantPlayInThisTurn() throws InterruptedException, ExecutionException {
        Command command = new Command(token1, roomId, new Coordinate(2, 4));
        stompSession1.send("/game/play", command);
        try {
            gameHandler1.get();
            Assert.fail();
        } catch (TimeoutException exception) {
        }
    }

    private void user2CanPlay() throws InterruptedException, ExecutionException, TimeoutException {
        Command command = new Command(token2, roomId, new Coordinate(2, 4));
        stompSession2.send("/game/play", command);
        GameState response = gameHandler2.get();
        gameHandler1.get();

        Assert.assertEquals(Box.WHITE, response.getBoxes()[2][4]);
    }

    private void user1CantPlayWithInvalidMove() throws InterruptedException, ExecutionException {
        Command command = new Command(token1, roomId, new Coordinate(0, 0));
        stompSession1.send("/game/play", command);
        try {
            gameHandler1.get();
            Assert.fail();
        } catch (TimeoutException exception) {
        }
    }

    private void user1CanGetHisPlayerStorage() {
        Response response = newRequest(token1).get("/storage/");
        PlayerStorage playerStorage = gson.fromJson(response.getBody().asString(), PlayerStorage.class);
        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals(account1, playerStorage.getPlayer1());
        Assert.assertEquals(account2, playerStorage.getPlayer2());
        Assert.assertEquals(roomId, playerStorage.getRoomId());
        Assert.assertEquals("player1", playerStorage.getStatus());
    }

    private void user1CanSubscribeChat() {
        chatHandler1 = new ChatStompHandler();
        stompSession1.subscribe("/chat/" + spectatorToken, chatHandler1);
    }

    private void user2CanSubscribeChat() {
        chatHandler2 = new ChatStompHandler();
        stompSession2.subscribe("/chat/" + spectatorToken, chatHandler2);
    }

    private void user1CanSendChat() throws InterruptedException, ExecutionException, TimeoutException {
        ChatData chatData = new ChatData(token1, "Hallo");
        stompSession1.send("/game/chat/" + spectatorToken, chatData);

        Chat response = chatHandler1.get();
        Assert.assertEquals(account1, response.getFrom());
        Assert.assertEquals("Hallo", response.getMessage());

        response = chatHandler2.get();
        Assert.assertEquals(account1, response.getFrom());
        Assert.assertEquals("Hallo", response.getMessage());
    }

    private void user1CanSendBlankChat() {
        ChatData chatData = new ChatData(token1, "");
        stompSession1.send("/game/chat/" + spectatorToken, chatData);
    }

    private void user2CanLeave() throws InterruptedException, ExecutionException, TimeoutException {
        Response response = newRequest(token2).post("/leave/");
        Assert.assertEquals(200, response.getStatusCode());

        GameState gameState = gameHandler1.get();
        Assert.assertEquals(GameStatus.PLAYER2_LEFT, gameState.getGameStatus());
    }

    private void user2CantLeaveAgain() {
        Response response = newRequest(token2).post("/leave/");
        Map responseBody = gson.fromJson(response.getBody().asString(), Map.class);
        Assert.assertEquals(400, response.getStatusCode());
        Assert.assertEquals(responseBody.get("message"), "You aren't in a game");
    }

    private void user1AndUser2CanFinishTheirGame() throws InterruptedException, ExecutionException, TimeoutException, URISyntaxException {
        String expectedContent = gson.toJson(new ScoreboardData(account1, false));
        mockScoreboardServer.expect(ExpectedCount.once(),
                requestTo(new URI(scoreboardServiceBaseUrl + "scoreboards/")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(expectedContent))
                .andRespond(withStatus(HttpStatus.OK));

        expectedContent = gson.toJson(new ScoreboardData(account2, true));
        mockScoreboardServer.expect(ExpectedCount.once(),
                requestTo(new URI(scoreboardServiceBaseUrl + "scoreboards/")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(expectedContent))
                .andRespond(withStatus(HttpStatus.OK));

        GameStatus gameStatus;
        int player1Step = 0, player2Step = 0;
        do {
            GameState response = gameHandler1.get();
            gameStatus = response.getGameStatus();
            if (gameStatus.equals(GameStatus.GAME_STARTED) || gameStatus.equals(GameStatus.PLAYER1_TURN)) {
                Command command = new Command(token1, roomId, GameStepPlayer2Win.step1[player1Step++]);
                stompSession1.send("/game/play", command);
            } else if (gameStatus.equals(GameStatus.PLAYER2_TURN)) {
                Command command = new Command(token2, roomId, GameStepPlayer2Win.step2[player2Step++]);
                stompSession2.send("/game/play", command);
            }
        } while (!gameStatus.isGameOver());
        Assert.assertEquals(gameStatus, GameStatus.GAME_OVER_PLAYER2_WINNER);
        Assert.assertEquals(GameStepPlayer2Win.step1.length, player1Step);
        Assert.assertEquals(GameStepPlayer2Win.step2.length, player2Step);

        mockScoreboardServer.verify();
    }

    private void user1AndUser2CanFinishTheirGame2() throws InterruptedException, ExecutionException, TimeoutException, URISyntaxException {
        mockScoreboardServer = MockRestServiceServer.createServer(restTemplate);

        String expectedContent = gson.toJson(new ScoreboardData(account1, true));
        mockScoreboardServer.expect(ExpectedCount.once(),
                requestTo(new URI(scoreboardServiceBaseUrl + "scoreboards/")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(expectedContent))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        expectedContent = gson.toJson(new ScoreboardData(account2, false));
        mockScoreboardServer.expect(ExpectedCount.once(),
                requestTo(new URI(scoreboardServiceBaseUrl + "scoreboards/")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(expectedContent))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        GameStatus gameStatus;
        int player1Step = 0, player2Step = 0;
        do {
            GameState response = gameHandler1.get();
            gameStatus = response.getGameStatus();
            if (gameStatus.equals(GameStatus.GAME_STARTED) || gameStatus.equals(GameStatus.PLAYER1_TURN)) {
                Command command = new Command(token1, roomId, GameStepPlayer1Win.step1[player1Step++]);
                stompSession1.send("/game/play", command);
            } else if (gameStatus.equals(GameStatus.PLAYER2_TURN)) {
                Command command = new Command(token2, roomId, GameStepPlayer1Win.step2[player2Step++]);
                stompSession2.send("/game/play", command);
            }
        } while (!gameStatus.isGameOver());
        Assert.assertEquals(gameStatus, GameStatus.GAME_OVER_PLAYER1_WINNER);

        mockScoreboardServer.verify();
    }

    @Test
    public void basicFlow() throws InterruptedException, ExecutionException, TimeoutException, URISyntaxException{
        guestCanPing();
        user1CanGetHisBlankPlayerStorage();

        guestCantCreateGameRoom();
        hackerCantCreateGameRoom();

        user1CanCreateGameRoom();
        user1CanGetHisPlayerStorageBeforePlayer2Join();
        user1CanLeave();
        user1CanCreateGameRoom();

        user3CantLeave();
        user3CantGetHisPlayerStorage();

        user2CantJoinGameRoomWithInvalidInvitationToken();
        user2CanJoinGameRoom();
        user2CanGetHisPlayerStorage();
        user2CanJoinAgainGameRoom();
        user3CantJoinGameRoom();

        user3CantWatchGameRoomWithInvalidSpectatorToken();
        user3CanWatchGameRoom();

        user1CanSubscribeGameRoom();
        user2CanSubscribeGameRoom();

        user1CantPlayWithInvalidRoomId();
        user1CanPlay();
        user1CantPlayInThisTurn();
        user2CanPlay();
        user1CantPlayWithInvalidMove();

        user1CanGetHisPlayerStorage();

        user1CanSubscribeChat();
        user2CanSubscribeChat();
        user1CanSendChat();
        user1CanSendBlankChat();

        user2CanLeave();
        user2CantLeaveAgain();

        user1CanCreateGameRoom();
        user1CanSubscribeGameRoom();
        user2CanJoinGameRoom();
        user2CanSubscribeGameRoom();
        user1AndUser2CanFinishTheirGame();
        user1CanLeave();

        user1CanCreateGameRoom();
        user1CanSubscribeGameRoom();
        user2CanJoinGameRoom();
        user2CanSubscribeGameRoom();
        user1AndUser2CanFinishTheirGame2();
    }

    private List<Transport> createTransportClient() {
        List<Transport> transports = new ArrayList<>(1);
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        return transports;
    }
}
