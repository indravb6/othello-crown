package othellocrown.game.model;

public class CreateRoomResponse {
    private Integer roomId;
    private String invitationToken;
    private String spectatorToken;

    public CreateRoomResponse(Integer roomId, String invitationToken, String spectatorToken) {
        this.roomId = roomId;
        this.invitationToken = invitationToken;
        this.spectatorToken = spectatorToken;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public String getInvitationToken() {
        return invitationToken;
    }

    public String getSpectatorToken() {
        return spectatorToken;
    }
}
