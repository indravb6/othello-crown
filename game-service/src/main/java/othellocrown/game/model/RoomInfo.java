package othellocrown.game.model;

public class RoomInfo {
    private Integer roomId;
    private String player1;
    private String player2;

    public RoomInfo(Integer roomId, String player1, String player2) {
        this.roomId = roomId;
        this.player1 = player1;
        this.player2 = player2;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public String getPlayer1() {
        return player1;
    }

    public String getPlayer2() {
        return player2;
    }
}
