package src.main.java.ecojuego.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import src.main.java.ecojuego.logic.automata.AutomataEngine;
import src.main.java.ecojuego.util.TextUtils;

public final class ClassificationEngines {

    private static final Map<Category, Pattern> REGEX_PATTERNS = new EnumMap<>(Category.class);
    private static final Map<Category, List<String>> TOKEN_CLASSES = new EnumMap<>(Category.class);

    static {
        REGEX_PATTERNS.put(Category.ORGANICO, Pattern.compile(
            "(banano|banana|cascara|cascaron|restos|comida|manzana|fruta|verdura|vegetal|huevo|hojas?|cafe|posos|te|infusion|pan|compost)"
        ));
        REGEX_PATTERNS.put(Category.RECICLABLE, Pattern.compile(
            "(plastico|botella|envase|lata|vidrio|papel|carton|revista|tarro|vaso|tetra(brik)?|brick|detergente|hdpe|pp|conserva)"
        ));
        REGEX_PATTERNS.put(Category.PELIGROSO, Pattern.compile(
            "(bateria|pilas?|servilleta|papel higienico|aceite|medicamento|mercurio|termometro|bombilla|fluorescente|aerosol|pintura|litio)"
        ));

        TOKEN_CLASSES.put(Category.ORGANICO, List.of(
            "banano", "banana", "cascara", "cascaron", "restos", "comida",
            "manzana", "fruta", "verdura", "vegetal", "huevo", "hojas",
            "posos", "cafe", "te", "infusion", "pan"
        ));
        TOKEN_CLASSES.put(Category.RECICLABLE, List.of(
            "plastico", "botella", "envase", "lata", "vidrio", "papel",
            "carton", "revista", "tarro", "vaso", "tetrabrik", "tetra",
            "brick", "detergente", "conserva", "hdpe", "pp"
        ));
        TOKEN_CLASSES.put(Category.PELIGROSO, List.of(
            "bateria", "pilas", "pila", "servilleta", "higienico", "aceite",
            "medicamento", "mercurio", "termometro", "bombilla", "fluorescente",
            "aerosol", "pintura", "litio"
        ));
    }

    private ClassificationEngines() {
    }

    public static ClassificationResult classify(String text) {
        String normalized = TextUtils.normalize(text);
        for (Map.Entry<Category, Pattern> entry : REGEX_PATTERNS.entrySet()) {
            Matcher matcher = entry.getValue().matcher(normalized);
            if (matcher.find()) {
                return new ClassificationResult(entry.getKey(), "Expresion regular", matcher.group(0));
            }
        }

        Category dfaCategory = Category.DESCONOCIDO;
        String trigger = null;
        for (String token : TextUtils.tokenize(text)) {
            for (Map.Entry<Category, List<String>> entry : TOKEN_CLASSES.entrySet()) {
                if (entry.getValue().contains(token)) {
                    dfaCategory = entry.getKey();
                    trigger = token;
                }
            }
        }

        if (dfaCategory != Category.DESCONOCIDO) {
            return new ClassificationResult(dfaCategory, "Automata finito", trigger);
        }
        return new ClassificationResult(Category.DESCONOCIDO, null, null);
    }

    public static Trace trace(String text) {
        String normalized = TextUtils.normalize(text);
        List<String> tokens = TextUtils.tokenize(text);
        ClassificationResult result = classify(text);

        Map<Category, List<String>> regexMatches = new EnumMap<>(Category.class);
        for (Map.Entry<Category, Pattern> entry : REGEX_PATTERNS.entrySet()) {
            Matcher matcher = entry.getValue().matcher(normalized);
            List<String> matches = new ArrayList<>();
            while (matcher.find()) {
                matches.add(matcher.group(0));
            }
            if (!matches.isEmpty()) {
                regexMatches.put(entry.getKey(), List.copyOf(matches));
            }
        }

        Map<Category, List<String>> automataMatches = new EnumMap<>(Category.class);
        Map<String, Integer> matchCounter = new HashMap<>();
        for (String token : tokens) {
            for (Map.Entry<Category, List<String>> entry : TOKEN_CLASSES.entrySet()) {
                if (entry.getValue().contains(token)) {
                    automataMatches.computeIfAbsent(entry.getKey(), key -> new ArrayList<>()).add(token);
                    matchCounter.merge(token, 1, Integer::sum);
                }
            }
        }
        automataMatches.replaceAll((key, value) -> List.copyOf(value));

        List<String> unmatched = new ArrayList<>();
        Map<String, Integer> remaining = new HashMap<>(matchCounter);
        for (String token : tokens) {
            int count = remaining.getOrDefault(token, 0);
            if (count > 0) {
                remaining.put(token, count - 1);
            } else {
                unmatched.add(token);
            }
        }

        AutomataEngine.AutomataAnalysis automataAnalysis = AutomataEngine.analyzeTokens(tokens);

        return new Trace(
            result,
            Map.copyOf(regexMatches),
            Map.copyOf(automataMatches),
            List.copyOf(tokens),
            List.copyOf(unmatched),
            normalized,
            automataAnalysis
        );
    }

    public static Map<Category, Pattern> regexDefinitions() {
        return Collections.unmodifiableMap(REGEX_PATTERNS);
    }

    public static Map<Category, List<String>> tokenDefinitions() {
        Map<Category, List<String>> copy = new EnumMap<>(Category.class);
        for (Map.Entry<Category, List<String>> entry : TOKEN_CLASSES.entrySet()) {
            copy.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return Collections.unmodifiableMap(copy);
    }

    public record ClassificationResult(Category category, String engine, String trigger) {
    }

    public record Trace(
        ClassificationResult result,
        Map<Category, List<String>> regexMatches,
        Map<Category, List<String>> automataMatches,
        List<String> tokens,
        List<String> unmatchedTokens,
        String normalizedText,
        AutomataEngine.AutomataAnalysis automataAnalysis
    ) {
    }
}
