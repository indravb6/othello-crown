package othellocrown.game.model;

import othellocrown.game.engine.component.Coordinate;

public class Command {
    private String token;
    private Integer roomId;
    private Coordinate coordinate;

    public Command() {
    }

    public Command(String token, Integer roomId, Coordinate coordinate) {
        this.token = token;
        this.roomId = roomId;
        this.coordinate = coordinate;
    }

    public String getToken() {
        return token;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }
}
