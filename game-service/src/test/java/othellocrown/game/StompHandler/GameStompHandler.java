package othellocrown.game.StompHandler;

import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import othellocrown.game.engine.component.GameState;

import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class GameStompHandler implements StompFrameHandler {
    private CompletableFuture<GameState> completableFuture;

    public GameStompHandler() {
        completableFuture = new CompletableFuture<>();
    }

    @Override
    public Type getPayloadType(StompHeaders stompHeaders) {
        return GameState.class;
    }

    @Override
    public void handleFrame(StompHeaders stompHeaders, Object o) {
        completableFuture.complete((GameState) o);
    }

    public GameState get() throws InterruptedException, ExecutionException, TimeoutException {
        GameState gameState = completableFuture.get(1, TimeUnit.SECONDS);
        completableFuture = new CompletableFuture<>();
        return gameState;
    }
}