package othellocrown.game.engine.component;

public class Coordinate {
    private Integer currentRow;
    private Integer currentColumn;

    public Coordinate() {

    }

    public Coordinate(Integer currentRow, Integer currentColumn) {
        this.currentRow = currentRow;
        this.currentColumn = currentColumn;
    }

    public void move(Direction direction) {
        currentRow += direction.getMoveRow();
        currentColumn += direction.getMoveColumn();
    }

    public Integer getCurrentRow() {
        return currentRow;
    }

    public Integer getCurrentColumn() {
        return currentColumn;
    }

    public Coordinate clone() {
        return new Coordinate(currentRow, currentColumn);
    }
}
