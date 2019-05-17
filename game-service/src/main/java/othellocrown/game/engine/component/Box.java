package othellocrown.game.engine.component;

public enum Box {
    EMPTY,
    WHITE,
    BLACK,
    CLICKABLE_WHITE,
    CLICKABLE_BLACK;

    public Box getReverse() {
        if (this.equals(WHITE)) {
            return BLACK;
        } else {
            return WHITE;
        }
    }

    public Box getClickable() {
        if (this == WHITE) {
            return CLICKABLE_WHITE;
        } else {
            return CLICKABLE_BLACK;
        }
    }

    public boolean isClickable() {
        return (this == CLICKABLE_WHITE || this == CLICKABLE_BLACK);
    }
}