package src.main.java.ecojuego.logic.automata;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import src.main.java.ecojuego.util.TextUtils;

/**
 * Loads vocabulary definitions from disk, builds deterministic finite automata (DFA)
 * for each category and exposes an analysis API used by the UI.
 */
public final class AutomataEngine {

    private static final Path DATA_DIR = Path.of("data");
    private static final Path VOCAB_FILE = DATA_DIR.resolve("afd_vocab.txt");

    private static Map<AutomataCategory, Automaton> automata = new EnumMap<>(AutomataCategory.class);
    private static Map<AutomataCategory, AutomatonMetric> metrics = new EnumMap<>(AutomataCategory.class);
    private static Map<AutomataCategory, List<String>> vocabulary = new EnumMap<>(AutomataCategory.class);

    static {
        reload();
    }

    private AutomataEngine() {
    }

    public static synchronized void reload() {
        ensureVocabularyFile();
        Map<AutomataCategory, List<String>> vocab = loadVocabulary();
        Map<AutomataCategory, Automaton> built = new EnumMap<>(AutomataCategory.class);
        Map<AutomataCategory, AutomatonMetric> builtMetrics = new EnumMap<>(AutomataCategory.class);
        for (AutomataCategory category : AutomataCategory.values()) {
            List<String> words = vocab.getOrDefault(category, List.of());
            Automaton automaton = Automaton.build(words);
            built.put(category, automaton);
            builtMetrics.put(category, new AutomatonMetric(automaton.originalStates(), automaton.minimizedStates()));
        }
        automata = built;
        metrics = builtMetrics;
        vocabulary = vocab;
    }

    public static AutomataAnalysis analyze(String text) {
        List<String> tokens = TextUtils.tokenize(text);
        return analyzeTokens(tokens);
    }

    public static AutomataAnalysis analyzeTokens(List<String> tokens) {
        if (tokens == null) {
            tokens = List.of();
        }
        List<String> normalizedTokens = List.copyOf(tokens);
        Map<AutomataCategory, List<TokenRun>> runsByCategory = new EnumMap<>(AutomataCategory.class);
        Map<AutomataCategory, List<String>> accepted = new EnumMap<>(AutomataCategory.class);
        Map<AutomataCategory, Integer> votes = new EnumMap<>(AutomataCategory.class);
        for (AutomataCategory category : AutomataCategory.values()) {
            runsByCategory.put(category, new ArrayList<>());
            accepted.put(category, new ArrayList<>());
            votes.put(category, 0);
        }
        List<String> unrecognized = new ArrayList<>();
        for (String token : normalizedTokens) {
            boolean recognized = false;
            for (AutomataCategory category : AutomataCategory.values()) {
                Automaton automaton = automata.get(category);
                TokenRun run = automaton.run(token);
                runsByCategory.get(category).add(run);
                if (run.accepted()) {
                    recognized = true;
                    accepted.get(category).add(token);
                    votes.computeIfPresent(category, (key, value) -> value + 1);
                }
            }
            if (!recognized) {
                unrecognized.add(token);
            }
        }
        AutomataCategory winner = determineWinner(votes);
        boolean tie = isTie(votes, winner);
        List<TokenTrace> trace = collectTrace(runsByCategory, winner);
        Map<AutomataCategory, List<String>> acceptedCopy = copyMapOfLists(accepted);
        Map<AutomataCategory, Integer> votesCopy = new EnumMap<>(votes);
        return new AutomataAnalysis(
            normalizedTokens,
            acceptedCopy,
            List.copyOf(unrecognized),
            votesCopy,
            winner,
            tie,
            trace,
            metrics(),
            "AFD"
        );
    }

    public static Map<AutomataCategory, AutomatonMetric> metrics() {
        return Collections.unmodifiableMap(metrics);
    }

    public static Map<AutomataCategory, List<String>> vocabulary() {
        return Collections.unmodifiableMap(vocabulary);
    }

    private static boolean isTie(Map<AutomataCategory, Integer> votes, AutomataCategory winner) {
        if (winner == null) {
            return false;
        }
        int max = votes.getOrDefault(winner, 0);
        long contenders = votes.values().stream().filter(value -> value == max).count();
        return contenders > 1;
    }

    private static AutomataCategory determineWinner(Map<AutomataCategory, Integer> votes) {
        AutomataCategory winner = null;
        int best = 0;
        for (Map.Entry<AutomataCategory, Integer> entry : votes.entrySet()) {
            int value = entry.getValue();
            if (value == 0) {
                continue;
            }
            if (value > best) {
                winner = entry.getKey();
                best = value;
            } else if (value == best) {
                winner = null;
            }
        }
        return winner;
    }

    private static List<TokenTrace> collectTrace(Map<AutomataCategory, List<TokenRun>> runs, AutomataCategory winner) {
        if (winner == null) {
            return List.of();
        }
        List<TokenRun> data = runs.getOrDefault(winner, List.of());
        List<TokenTrace> traces = new ArrayList<>(data.size());
        for (TokenRun run : data) {
            List<TransitionStep> steps = new ArrayList<>(run.steps().size());
            for (TransitionStep step : run.steps()) {
                steps.add(step);
            }
            traces.add(new TokenTrace(run.token(), steps, run.accepted()));
        }
        return List.copyOf(traces);
    }

    private static Map<AutomataCategory, List<String>> loadVocabulary() {
        Map<AutomataCategory, Set<String>> accumulator = new EnumMap<>(AutomataCategory.class);
        for (AutomataCategory category : AutomataCategory.values()) {
            accumulator.put(category, new java.util.LinkedHashSet<>());
        }
        List<String> seedLines = readAllLines();
        for (String line : seedLines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#") || !trimmed.contains("=")) {
                continue;
            }
            String[] parts = trimmed.split("=", 2);
            AutomataCategory category = AutomataCategory.fromLabel(parts[0]);
            if (category == null) {
                continue;
            }
            for (String rawToken : parts[1].split(",")) {
                List<String> tokens = TextUtils.tokenize(rawToken);
                accumulator.get(category).addAll(tokens);
            }
        }
        Map<AutomataCategory, List<String>> normalized = new EnumMap<>(AutomataCategory.class);
        for (AutomataCategory category : AutomataCategory.values()) {
            List<String> values = new ArrayList<>(accumulator.getOrDefault(category, Set.of()));
            if (values.isEmpty()) {
                values.addAll(defaultVocabulary(category));
            }
            normalized.put(category, List.copyOf(values));
        }
        return normalized;
    }

    private static List<String> readAllLines() {
        try {
            return Files.readAllLines(VOCAB_FILE, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            return List.of();
        }
    }

    private static List<String> defaultVocabulary(AutomataCategory category) {
        return switch (category) {
            case ORGANICO -> List.of("cascara", "comida", "restos", "servilleta", "hueso");
            case RECICLABLE -> List.of("botella", "lata", "plastico", "metal", "brik");
            case PAPEL_CARTON -> List.of("papel", "carton", "cuaderno", "revista");
            case VIDRIO -> List.of("vidrio", "frasco", "botella");
            case PELIGROSO -> List.of("pila", "bateria", "aceite", "pintura", "aerosol");
        };
    }

    private static void ensureVocabularyFile() {
        try {
            Files.createDirectories(DATA_DIR);
            if (Files.notExists(VOCAB_FILE)) {
                List<String> lines = new ArrayList<>();
                lines.add("# Definicion de vocabulario para el modo Automatas (AFD)");
                lines.add("# Formato: categoria=palabra1,palabra2,...");
                lines.add("organico=cascara,comida,restos,servilleta,hueso");
                lines.add("reciclable=botella,lata,plastico,metal,brik");
                lines.add("papel_carton=papel,carton,cuaderno,revista");
                lines.add("vidrio=vidrio,frasco,botella-vidrio");
                lines.add("peligrosos=pila,bateria,aceite,pintura,aerosol");
                Files.write(
                    VOCAB_FILE,
                    lines,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
                );
            }
        } catch (IOException ignored) {
        }
    }

    private static Map<AutomataCategory, List<String>> copyMapOfLists(Map<AutomataCategory, List<String>> source) {
        Map<AutomataCategory, List<String>> copy = new EnumMap<>(AutomataCategory.class);
        for (Map.Entry<AutomataCategory, List<String>> entry : source.entrySet()) {
            copy.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return copy;
    }

    private static final class Automaton {
        private final List<State> states;
        private final int startState;
        private final int originalStates;

        private Automaton(List<State> states, int startState, int originalStates) {
            this.states = states;
            this.startState = startState;
            this.originalStates = originalStates;
        }

        static Automaton build(List<String> tokens) {
            Node root = new Node();
            for (String rawToken : tokens) {
                String token = TextUtils.normalize(rawToken);
                if (token.isBlank()) {
                    continue;
                }
                Node current = root;
                for (char symbol : token.toCharArray()) {
                    current = current.children.computeIfAbsent(symbol, key -> new Node());
                }
                current.accepting = true;
            }
            int totalStates = countNodes(root);
            MinimizationResult result = minimize(root);
            return new Automaton(result.states(), result.startState(), totalStates);
        }

        TokenRun run(String token) {
            if (token == null) {
                token = "";
            }
            String normalized = TextUtils.normalize(token);
            List<TransitionStep> steps = new ArrayList<>();
            int current = startState;
            if (normalized.isEmpty()) {
                boolean accepting = current >= 0 && states.get(current).accepting();
                return new TokenRun(token, List.of(new TransitionStep(label(current), "ε", label(current), accepting)), accepting);
            }
            for (char symbol : normalized.toCharArray()) {
                int next = -1;
                if (current >= 0) {
                    next = states.get(current).transition(symbol);
                }
                boolean accepting = next >= 0 && states.get(next).accepting();
                steps.add(new TransitionStep(label(current), String.valueOf(symbol), label(next), accepting));
                current = next;
            }
            boolean accepted = current >= 0 && states.get(current).accepting();
            return new TokenRun(token, steps, accepted);
        }

        int originalStates() {
            return originalStates;
        }

        int minimizedStates() {
            return states.size();
        }

        private static int countNodes(Node node) {
            int count = 1;
            for (Node child : node.children.values()) {
                count += countNodes(child);
            }
            return count;
        }

        private static MinimizationResult minimize(Node root) {
            Map<Node, Integer> nodeCache = new IdentityHashMap<>();
            Map<NodeSignature, Integer> canonical = new HashMap<>();
            List<StateBuilder> builders = new ArrayList<>();
            int start = collapse(root, nodeCache, canonical, builders);
            List<State> finalStates = new ArrayList<>(builders.size());
            for (StateBuilder builder : builders) {
                finalStates.add(builder.toState());
            }
            return new MinimizationResult(finalStates, start);
        }

        private static int collapse(
            Node node,
            Map<Node, Integer> cache,
            Map<NodeSignature, Integer> canonical,
            List<StateBuilder> builders
        ) {
            Integer cached = cache.get(node);
            if (cached != null) {
                return cached;
            }
            List<Map.Entry<Character, Node>> entries = new ArrayList<>(node.children.entrySet());
            entries.sort(Map.Entry.comparingByKey());
            List<Map.Entry<Character, Integer>> transitions = new ArrayList<>();
            for (Map.Entry<Character, Node> entry : entries) {
                int target = collapse(entry.getValue(), cache, canonical, builders);
                transitions.add(Map.entry(entry.getKey(), target));
            }
            NodeSignature signature = new NodeSignature(node.accepting, transitions);
            Integer id = canonical.get(signature);
            if (id == null) {
                id = builders.size();
                StateBuilder builder = new StateBuilder(node.accepting);
                for (Map.Entry<Character, Integer> transition : transitions) {
                    builder.addTransition(transition.getKey(), transition.getValue());
                }
                builders.add(builder);
                canonical.put(signature, id);
            }
            cache.put(node, id);
            return id;
        }

        private static String label(int id) {
            return id < 0 ? "-" : "q" + id;
        }
    }

    private static final class Node {
        private final Map<Character, Node> children = new HashMap<>();
        private boolean accepting;
    }

    private static final class State {
        private final boolean accepting;
        private final Map<Character, Integer> transitions;

        State(boolean accepting, Map<Character, Integer> transitions) {
            this.accepting = accepting;
            this.transitions = transitions;
        }

        boolean accepting() {
            return accepting;
        }

        int transition(char symbol) {
            return transitions.getOrDefault(symbol, -1);
        }
    }

    private static final class StateBuilder {
        private final boolean accepting;
        private final Map<Character, Integer> transitions = new LinkedHashMap<>();

        StateBuilder(boolean accepting) {
            this.accepting = accepting;
        }

        void addTransition(char symbol, int target) {
            transitions.put(symbol, target);
        }

        State toState() {
            return new State(accepting, Map.copyOf(transitions));
        }
    }

    private record NodeSignature(boolean accepting, List<Map.Entry<Character, Integer>> transitions) {
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof NodeSignature other)) {
                return false;
            }
            return accepting == other.accepting && Objects.equals(transitions, other.transitions);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accepting, transitions);
        }
    }

    private record MinimizationResult(List<State> states, int startState) {
    }

    public enum AutomataCategory {
        ORGANICO("Organico"),
        RECICLABLE("Reciclable"),
        PAPEL_CARTON("Papel/Carton"),
        VIDRIO("Vidrio"),
        PELIGROSO("Peligroso");

        private final String display;

        AutomataCategory(String display) {
            this.display = display;
        }

        public String display() {
            return display;
        }

        public static AutomataCategory fromLabel(String value) {
            if (value == null) {
                return null;
            }
            String normalized = value.trim().toUpperCase(Locale.ROOT);
            return switch (normalized) {
                case "ORGANICO" -> ORGANICO;
                case "RECICLABLE" -> RECICLABLE;
                case "PAPEL_CARTON", "PAPEL/CARTON", "PAPEL", "CARTON" -> PAPEL_CARTON;
                case "VIDRIO" -> VIDRIO;
                case "PELIGROSO", "PELIGROSOS" -> PELIGROSO;
                default -> null;
            };
        }

        public Optional<src.main.java.ecojuego.logic.Category> toGameCategory() {
            return switch (this) {
                case ORGANICO -> Optional.of(src.main.java.ecojuego.logic.Category.ORGANICO);
                case RECICLABLE -> Optional.of(src.main.java.ecojuego.logic.Category.RECICLABLE);
                case PAPEL_CARTON, VIDRIO -> Optional.of(src.main.java.ecojuego.logic.Category.RECICLABLE);
                case PELIGROSO -> Optional.of(src.main.java.ecojuego.logic.Category.PELIGROSO);
            };
        }
    }

    public record AutomatonMetric(int originalStates, int minimizedStates) {
        @Override
        public String toString() {
            return originalStates + "->" + minimizedStates;
        }
    }

    public record TransitionStep(String fromState, String symbol, String toState, boolean accepting) {
    }

    public record TokenTrace(String token, List<TransitionStep> steps, boolean accepted) {
    }

    private record TokenRun(String token, List<TransitionStep> steps, boolean accepted) {
    }

    public record AutomataAnalysis(
        List<String> tokens,
        Map<AutomataCategory, List<String>> acceptedTokens,
        List<String> unrecognizedTokens,
        Map<AutomataCategory, Integer> votes,
        AutomataCategory winner,
        boolean tie,
        List<TokenTrace> winnerTrace,
        Map<AutomataCategory, AutomatonMetric> metrics,
        String engine
    ) {
        public boolean hasWinner() {
            return winner != null && !tie;
        }
    }
}
