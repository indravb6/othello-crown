package othellocrown.game.model;

public class ChatData {
    private String token;
    private String message;

    public ChatData() {

    }

    public ChatData(String token, String message) {
        this.token = token;
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public String getMessage() {
        return message;
    }
}
