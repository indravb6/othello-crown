package othellocrown.game.model;

public class PlayerStorage {
    private String spectatorToken;
    private String invitationToken;
    private Integer roomId;
    private String status;
    private String player1;
    private String player2;

    public PlayerStorage() {

    }

    public PlayerStorage(String spectatorToken,
                         String invitationToken,
                         Integer roomId,
                         String status,
                         String player1,
                         String player2) {
        this.spectatorToken = spectatorToken;
        this.invitationToken = invitationToken;
        this.roomId = roomId;
        this.status = status;
        this.player1 = player1;
        this.player2 = player2;
    }

    public String getSpectatorToken() {
        return spectatorToken;
    }

    public String getInvitationToken() {
        return invitationToken;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public String getStatus() {
        return status;
    }

    public String getPlayer1() {
        return player1;
    }

    public String getPlayer2() {
        return player2;
    }
}
