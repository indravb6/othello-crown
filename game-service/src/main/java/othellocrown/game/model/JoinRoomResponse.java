package othellocrown.game.model;

public class JoinRoomResponse {
    private String player1;
    private String player2;
    private String spectatorToken;
    private Integer roomId;

    public JoinRoomResponse(String player1, String player2, String spectatorToken, Integer roomId) {
        this.player1 = player1;
        this.player2 = player2;
        this.spectatorToken = spectatorToken;
        this.roomId = roomId;
    }

    public String getPlayer1() {
        return player1;
    }

    public String getPlayer2() {
        return player2;
    }

    public String getSpectatorToken() {
        return spectatorToken;
    }

    public Integer getRoomId() {
        return roomId;
    }
}
