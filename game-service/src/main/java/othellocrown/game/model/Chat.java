package othellocrown.game.model;

public class Chat {
    private String from;
    private String message;

    public Chat() {

    }

    public Chat(String from, String message) {
        this.from = from;
        this.message = message;
    }

    public String getFrom() {
        return from;
    }

    public String getMessage() {
        return message;
    }
}
