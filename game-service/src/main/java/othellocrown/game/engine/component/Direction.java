package othellocrown.game.engine.component;

public class Direction {
    public static final Direction[] DEFAULT = {
            new Direction(-1, 1),
            new Direction(-1, 0),
            new Direction(-1, -1),
            new Direction(0, -1),
            new Direction(1, -1),
            new Direction(1, 0),
            new Direction(1, 1),
            new Direction(0, 1),
    };

    private Integer moveRow;
    private Integer moveColumn;

    private Direction(Integer moveRow, Integer moveColumn) {
        this.moveRow = moveRow;
        this.moveColumn = moveColumn;
    }

    Integer getMoveRow() {
        return moveRow;
    }

    Integer getMoveColumn() {
        return moveColumn;
    }
}
