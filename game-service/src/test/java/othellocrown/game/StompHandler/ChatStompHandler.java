package othellocrown.game.StompHandler;

import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import othellocrown.game.model.Chat;

import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ChatStompHandler implements StompFrameHandler {
    private CompletableFuture<Chat> completableFuture;

    public ChatStompHandler() {
        completableFuture = new CompletableFuture<>();
    }

    @Override
    public Type getPayloadType(StompHeaders stompHeaders) {
        return Chat.class;
    }

    @Override
    public void handleFrame(StompHeaders stompHeaders, Object o) {
        completableFuture.complete((Chat) o);
    }

    public Chat get() throws InterruptedException, ExecutionException, TimeoutException {
        Chat chat = completableFuture.get(3, TimeUnit.SECONDS);
        completableFuture = new CompletableFuture<>();
        return chat;
    }
}