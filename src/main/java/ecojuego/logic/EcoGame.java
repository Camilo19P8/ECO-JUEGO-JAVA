package src.main.java.ecojuego.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import src.main.java.ecojuego.logic.ClassificationEngines.ClassificationResult;

public final class EcoGame {

    private final List<EcoItem> items;
    private final int requestedRounds;
    private int maxRounds;
    private final Random random = new Random();
    private final Map<Category, List<String>> tips;

    private int index;
    private int score;
    private int streak;
    private int correctCount;
    private final List<RoundRecord> history = new ArrayList<>();

    public EcoGame(List<EcoItem> items, int maxRounds) {
        this.items = new ArrayList<>(items);
        this.requestedRounds = Math.max(1, maxRounds);
        this.maxRounds = Math.min(this.requestedRounds, this.items.size());
        this.tips = new EnumMap<>(Category.class);
        this.tips.putAll(EcoData.tips());
        reset();
    }

    public void reset() {
        Collections.shuffle(items, random);
        index = 0;
        score = 0;
        streak = 0;
        correctCount = 0;
        history.clear();
        maxRounds = Math.min(requestedRounds, items.size());
    }

    public EcoItem nextItem() {
        if (index >= maxRounds) {
            return null;
        }
        EcoItem item = items.get(index);
        index++;
        return item;
    }

    public Example present(EcoItem item) {
        ClassificationResult result = ClassificationEngines.classify(item.description());
        return new Example(item, result.category(), result.engine(), result.trigger());
    }

    public CheckResult check(Example example, Category answer) {
        Category expected = example.expected();
        String message;
        boolean correct = false;
        int delta = 0;
        if (expected == Category.DESCONOCIDO) {
            message = "La clasificacion es ambigua; busca palabras clave mas especificas.";
            history.add(new RoundRecord(example.item().description(), expected, answer, false, example.engine(), example.trigger()));
            return new CheckResult(
                false,
                0,
                message,
                tipFor(Category.DESCONOCIDO),
                expected,
                example.engine(),
                example.trigger(),
                example.item().reason(),
                example.item().handling()
            );
        }

        if (answer == expected) {
            streak++;
            correctCount++;
            delta = 10 + Math.max(0, (streak - 1) * 5);
            score += delta;
            correct = true;
            message = "Correcto (+" + delta + ")";
        } else {
            streak = 0;
            delta = -5;
            score += delta;
            message = "Incorrecto (" + delta + ")";
        }

        history.add(new RoundRecord(example.item().description(), expected, answer, correct, example.engine(), example.trigger()));
        return new CheckResult(
            correct,
            delta,
            message,
            tipFor(correct ? expected : answer == Category.DESCONOCIDO ? Category.DESCONOCIDO : expected),
            expected,
            example.engine(),
            example.trigger(),
            example.item().reason(),
            example.item().handling()
        );
    }

    public Progress progress() {
        return new Progress(index, maxRounds);
    }

    public Summary summary() {
        return new Summary(score, correctCount, maxRounds, List.copyOf(history));
    }

    public int score() {
        return score;
    }

    public int streak() {
        return streak;
    }

    private String tipFor(Category category) {
        List<String> options = tips.getOrDefault(category, tips.get(Category.DESCONOCIDO));
        if (options == null || options.isEmpty()) {
            return "Clasificar tus residuos es el primer paso para un entorno limpio.";
        }
        return options.get(random.nextInt(options.size()));
    }

    public void replaceItems(List<EcoItem> newItems) {
        items.clear();
        items.addAll(newItems);
        reset();
    }

    public record Example(EcoItem item, Category expected, String engine, String trigger) {
    }

    public record CheckResult(
        boolean correct,
        int delta,
        String message,
        String tip,
        Category expected,
        String engine,
        String trigger,
        String reason,
        String handling
    ) {
    }

    public record Progress(int current, int total) {
    }

    public record RoundRecord(String item, Category expected, Category answer, boolean correct, String engine, String trigger) {
    }

    public record Summary(int score, int correct, int total, List<RoundRecord> history) {
    }
}
