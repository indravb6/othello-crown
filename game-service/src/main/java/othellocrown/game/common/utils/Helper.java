package othellocrown.game.common.utils;


public class Helper {
    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public String createToken(Integer length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int character = (int)(Math.random() * ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }
}
