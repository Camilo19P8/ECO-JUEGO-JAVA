package src.main.java.ecojuego.util;

import java.awt.Color;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextUtils {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("[a-z]+");

    private TextUtils() {
    }

    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String lower = value.toLowerCase(Locale.ROOT);
        String decomposed = Normalizer.normalize(lower, Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}", "");
    }

    public static List<String> tokenize(String value) {
        List<String> tokens = new ArrayList<>();
        Matcher matcher = TOKEN_PATTERN.matcher(normalize(value));
        while (matcher.find()) {
            tokens.add(matcher.group());
        }
        return tokens;
    }

    public static Color lighten(Color color, double factor) {
        factor = Math.max(0.0, Math.min(1.0, factor));
        int r = (int) Math.round(color.getRed() + (255 - color.getRed()) * factor);
        int g = (int) Math.round(color.getGreen() + (255 - color.getGreen()) * factor);
        int b = (int) Math.round(color.getBlue() + (255 - color.getBlue()) * factor);
        return new Color(Math.min(255, r), Math.min(255, g), Math.min(255, b));
    }
}
