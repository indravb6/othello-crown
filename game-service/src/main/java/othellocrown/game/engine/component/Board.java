package othellocrown.game.engine.component;

import java.util.Iterator;

public class Board implements Iterable<Box> {
    public static final Integer SIZE = 8;
    private Box[][] boxes = new Box[SIZE][SIZE];

    public Board() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                boxes[i][j] = Box.EMPTY;
            }
        }
        boxes[3][3] = Box.WHITE;
        boxes[4][3] = Box.BLACK;
        boxes[3][4] = Box.BLACK;
        boxes[4][4] = Box.WHITE;
    }

    public void setBox(Coordinate coordinate, Box box) {
        boxes[coordinate.getCurrentRow()][coordinate.getCurrentColumn()] = box;
    }

    public Box getBox(Coordinate coordinate) {
        return boxes[coordinate.getCurrentRow()][coordinate.getCurrentColumn()];
    }

    public static boolean validCoordinat(Coordinate coordinate) {
        Integer x = coordinate.getCurrentRow();
        Integer y = coordinate.getCurrentColumn();
        return (0 <= x && x < SIZE && 0 <= y && y < SIZE);
    }

    private void setBoxes(Box[][] boxes) {
        this.boxes = boxes;
    }

    public Board clone() {
        Board board = new Board();
        Box[][] boxes = new Box[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                boxes[i][j] = this.boxes[i][j];
            }
        }
        board.setBoxes(boxes);
        return board;
    }

    public Box[][] getBoxes() {
        return boxes;
    }

    @Override
    public Iterator<Box> iterator() {
        return new Iterator<Box>() {
            private int currentRow = 0;
            private int currentColumn = 0;

            @Override
            public boolean hasNext() {
                return currentRow != Board.SIZE;
            }

            @Override
            public Box next() {
                Box box = boxes[currentRow][currentColumn++];
                if (currentColumn == Board.SIZE) {
                    currentColumn = 0;
                    currentRow++;
                }

                return box;
            }
        };
    }
}
