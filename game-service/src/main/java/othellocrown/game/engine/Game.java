package othellocrown.game.engine;

import othellocrown.game.engine.component.Board;
import othellocrown.game.engine.component.Box;
import othellocrown.game.engine.component.Coordinate;
import othellocrown.game.engine.component.Direction;

import java.util.Iterator;

class Game {
    private Board board = new Board();

    Game() {
        updateClickable(Box.BLACK);
    }

    Board play(Box box, Coordinate coordinate) {
        boolean isValid = false;
        for (Direction direction : Direction.DEFAULT) {
            isValid |= fill(coordinate.clone(), direction, box);
        }
        if (!isValid) {
            throw new RuntimeException("Invalid move");
        }

        board.setBox(coordinate, box);
        updateClickable(box.getReverse());

        return board;
    }

    boolean isPass() {
        Iterator<Box> boxIterator = board.iterator();
        while (boxIterator.hasNext()) {
            Box box = boxIterator.next();
            if (box.isClickable()) {
                return false;
            }
        }

        return true;
    }

    private boolean fill(Coordinate coordinate, Direction direction, Box box) {
        Board board = this.board.clone();

        boolean isFilled = false;
        coordinate.move(direction);

        if (!Board.validCoordinat(coordinate)
                || !board.getBox(coordinate).equals(box.getReverse())) {
            return false;
        }

        while (Board.validCoordinat(coordinate)) {
            if (board.getBox(coordinate) == Box.EMPTY) {
                return false;
            }
            if (board.getBox(coordinate).equals(box)) {
                isFilled = true;
                break;
            }

            board.setBox(coordinate, box);
            coordinate.move(direction);
        }

        if (isFilled) {
            this.board = board;
        }

        return isFilled;
    }

    void updateClickable(Box box) {
        clearClickable();
        for (int i = 0; i < Board.SIZE; i++) {
            for (int j = 0; j < Board.SIZE; j++) {
                Coordinate coordinate = new Coordinate(i, j);
                if (board.getBox(coordinate) != Box.EMPTY) {
                    continue;
                }
                boolean isClickable = false;
                for (Direction direction : Direction.DEFAULT) {
                    isClickable |= checkClickable(board, coordinate.clone(), direction, box);
                }
                if (isClickable) {
                    board.setBox(coordinate, box.getClickable());
                }
            }
        }
    }

    private void clearClickable() {
        for (int i = 0; i < Board.SIZE; i++) {
            for (int j = 0; j < Board.SIZE; j++) {
                Coordinate coordinate = new Coordinate(i, j);
                if (board.getBox(coordinate).isClickable()) {
                    board.setBox(coordinate, Box.EMPTY);
                }
            }
        }
    }

    private boolean checkClickable(Board board,
                                   Coordinate coordinate,
                                   Direction direction, Box box) {
        boolean isClickable = false;
        boolean isValid = false;
        coordinate.move(direction);
        while (Board.validCoordinat(coordinate)) {
            if (board.getBox(coordinate).equals(box)) {
                isClickable = isValid;
                break;
            }
            if (!board.getBox(coordinate).equals(box.getReverse())) {
                break;
            }

            isValid = true;
            coordinate.move(direction);
        }

        return isClickable;
    }

    Board getBoard() {
        return board.clone();
    }

    Box getTheWinner() {
        Integer whiteSocre = 0;
        Integer blackScore = 0;

        Iterator<Box> boxIterator = board.iterator();
        while (boxIterator.hasNext()) {
            Box box = boxIterator.next();
            if (box.equals(Box.WHITE)) {
                whiteSocre += 1;
            } else if (box.equals(Box.BLACK)) {
                blackScore += 1;
            }
        }

        if (whiteSocre > blackScore) {
            return Box.WHITE;
        } else {
            return Box.BLACK;
        }
    }
}
