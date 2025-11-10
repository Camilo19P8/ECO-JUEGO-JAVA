package src.main.java.ecojuego.ui;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;

import src.main.java.ecojuego.logic.Category;
import src.main.java.ecojuego.logic.ClassificationEngines;
import src.main.java.ecojuego.logic.EcoCatalogStore;
import src.main.java.ecojuego.logic.EcoData;
import src.main.java.ecojuego.logic.EcoGame;
import src.main.java.ecojuego.logic.EcoItem;
import src.main.java.ecojuego.logic.UserProfile;
import src.main.java.ecojuego.logic.ClassificationEngines.Trace;
import src.main.java.ecojuego.logic.EcoGame.CheckResult;
import src.main.java.ecojuego.logic.EcoGame.Example;
import src.main.java.ecojuego.logic.EcoGame.Progress;
import src.main.java.ecojuego.logic.EcoGame.RoundRecord;
import src.main.java.ecojuego.logic.EcoGame.Summary;
import src.main.java.ecojuego.logic.automata.AutomataEngine;
import src.main.java.ecojuego.logic.automata.AutomataEngine.AutomataAnalysis;
import src.main.java.ecojuego.logic.automata.AutomataEngine.AutomataCategory;
import src.main.java.ecojuego.logic.automata.AutomataEngine.TokenTrace;
import src.main.java.ecojuego.logic.automata.AutomataEngine.TransitionStep;
import src.main.java.ecojuego.util.TextUtils;

import java.awt.BorderLayout;
import java.awt.BasicStroke;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.text.DecimalFormat;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class EcoGameFrame extends JFrame {

    private static final Color BACKGROUND = new Color(0x06, 0x18, 0x26);
    private static final Color CARD = new Color(0x0d, 0x23, 0x3b);
    private static final Color ITEM_PANEL = new Color(0x15, 0x2c, 0x46);
    private static final Font TITLE_FONT = new Font("Segoe UI Semibold", Font.PLAIN, 24);
    private static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font ITEM_FONT = new Font("Segoe UI Semibold", Font.PLAIN, 22);
    private static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FEEDBACK_FONT = new Font("Segoe UI Semibold", Font.PLAIN, 16);
    private static final Font MANAGEMENT_FONT = new Font("Segoe UI", Font.PLAIN, 15);
    private static final Font BUTTON_FONT = new Font("Segoe UI Semibold", Font.PLAIN, 15);
    private static final Font SECONDARY_BUTTON_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    private static final int SLIDE_SPEED = 18;
    private static final String DEFAULT_TIP = "Clasificar tus residuos es el primer paso para un entorno limpio.";
    private static final Map<Category, String> CATEGORY_MOTTO = Map.of(
        Category.ORGANICO, "Compost y contenedor marron",
        Category.RECICLABLE, "Envases limpios y secos",
        Category.PELIGROSO, "Puntos limpios especializados",
        Category.DESCONOCIDO, "Investiga antes de desechar"
    );

    private final EcoGame game;
    private final List<EcoItem> catalog;
    private final boolean professorMode;
    private Example currentExample;
    private Timer slideTimer;
    private Timer tipTimer;

    private final JPanel itemPanel = new JPanel(null);
    private final ItemBackdrop itemBackdrop = new ItemBackdrop();
    private final JPanel itemBadgeStrip = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 6));
    private final List<CategoryBadge> badgeList = new ArrayList<>();
    private final JLabel itemLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel progressLabel = new JLabel("Ronda 0 / 0");
    private final JProgressBar progressBar = new JProgressBar();
    private final JLabel feedbackLabel = new JLabel("");
    private final JLabel detailLabel = new JLabel("");
    private final JLabel scoreLabel = new JLabel("Puntaje: 0  |  Racha: 0");
    private final JLabel managementLabel = new JLabel();
    private final JButton nextButton = new JButton("Siguiente");
    private final JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
    private final Map<Category, JButton> categoryButtons = new EnumMap<>(Category.class);
    private final JPanel startOverlay = new JPanel();
    private JPanel cardPanel;
    private final JButton restartButton = new JButton("Volver a intentar");
    private JPanel summaryPanel;
    private java.awt.Component summarySpacer;
    private final String playerName;
    private final String playerRole;
    private final Map<Category, TipBox> tipBoxes = new EnumMap<>(Category.class);
    private final Map<Category, List<String>> tipSources = new EnumMap<>(Category.class);
    private final Map<Category, Integer> tipIndices = new EnumMap<>(Category.class);
    private final Map<Category, Timer> tipResetTimers = new EnumMap<>(Category.class);
    private final LanguagePanel languagePanel = new LanguagePanel();
    private Trace currentTrace;
    private JSplitPane splitPane;
    private JTabbedPane profileTabs;
    private ProfessorDashboard professorDashboard;
    private LanguageInsightPanel languageInspector;
    private final Runnable logoutAction;
    private JLabel headerTitle;
    private JLabel headerSubtitle;
    private JLabel headerProfile;
    private double zoomFactor = 1.0;
    private static final double ZOOM_MIN = 0.85;
    private static final double ZOOM_MAX = 1.3;
    private static final double ZOOM_STEP = 0.1;
    private String lastDetailText = "";
    private String lastReasonText = "";
    private String lastHandlingText = "";
    private String lastItemText = "";
    private int automataAgreements;
    private int automataComparisons;

    public EcoGameFrame(UserProfile profile, Runnable logoutAction) {
        super("EcoJuego - Separacion de residuos");
        this.playerName = profile == null || profile.name() == null || profile.name().trim().isEmpty() ? "Jugador" : profile.name().trim();
        this.playerRole = profile == null || profile.role() == null || profile.role().trim().isEmpty() ? "Invitado" : profile.role().trim();
        this.professorMode = "PROFESOR".equalsIgnoreCase(this.playerRole);
        this.catalog = new ArrayList<>(EcoData.sampleItems());
        this.game = new EcoGame(catalog, 10);
        this.logoutAction = logoutAction == null ? () -> {
        } : logoutAction;
        configureWindow();
        buildLayout();
        installResponsiveBehavior();
        installKeyboardShortcuts();
        initializeTipSources();
        resetTipContent();
        startTipRotation();
        updateResponsiveLayout();
        applyZoomStyles();
    }

    private void configureWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1040, 680));
        getContentPane().setBackground(BACKGROUND);
    }

    private void buildLayout() {
        JComponent studentRoot = createStudentView();
        if (professorMode) {
            professorDashboard = new ProfessorDashboard();
            languageInspector = new LanguageInsightPanel();
            profileTabs = new JTabbedPane();
            profileTabs.setBorder(BorderFactory.createEmptyBorder());
            profileTabs.setBackground(BACKGROUND);
            profileTabs.setForeground(new Color(0xe2, 0xe8, 0xf0));
            profileTabs.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
            profileTabs.addTab("Panel docente", professorDashboard);
            profileTabs.addTab("Laboratorio de lenguajes", languageInspector);
            profileTabs.addTab("Modo juego", studentRoot);
            profileTabs.setSelectedIndex(0);
            setContentPane(profileTabs);
        } else {
            languageInspector = null;
            setContentPane(studentRoot);
        }
    }

    private JPanel createStudentView() {
        JPanel root = new JPanel(new BorderLayout(28, 0));
        root.setBorder(BorderFactory.createEmptyBorder(28, 32, 36, 32));
        root.setBackground(BACKGROUND);

        JPanel mainColumn = new JPanel();
        mainColumn.setLayout(new BoxLayout(mainColumn, BoxLayout.Y_AXIS));
        mainColumn.setBackground(BACKGROUND);
        mainColumn.setMinimumSize(new Dimension(720, 520));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(BACKGROUND);
        headerTitle = new JLabel("EcoJuego: Separa y Aprende");
        headerTitle.setFont(TITLE_FONT);
        headerTitle.setForeground(new Color(0x7d, 0xf3, 0x8b));
        JButton logoutButton = createLogoutButton();
        JPanel topLine = new JPanel();
        topLine.setLayout(new BoxLayout(topLine, BoxLayout.X_AXIS));
        topLine.setOpaque(false);
        topLine.add(headerTitle);
        topLine.add(Box.createHorizontalGlue());
        topLine.add(logoutButton);
        headerSubtitle = new JLabel("Hola " + playerName + ", clasifica cada residuo apoyandote en gramaticas regulares y un automata finito.");
        headerSubtitle.setFont(SUBTITLE_FONT);
        headerSubtitle.setForeground(new Color(0x94, 0xa3, 0xb8));
        headerProfile = new JLabel("Perfil: " + playerRole);
        headerProfile.setFont(BODY_FONT);
        headerProfile.setForeground(new Color(0xb8, 0xca, 0xe0));
        header.add(topLine);
        header.add(Box.createVerticalStrut(4));
        header.add(headerSubtitle);
        header.add(Box.createVerticalStrut(2));
        header.add(headerProfile);
        JPanel headerWrapper = new JPanel();
        headerWrapper.setLayout(new BoxLayout(headerWrapper, BoxLayout.Y_AXIS));
        headerWrapper.setOpaque(false);
        headerWrapper.add(header);
        headerWrapper.add(Box.createVerticalStrut(18));
        mainColumn.add(headerWrapper);
        mainColumn.add(Box.createVerticalStrut(22));

        cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBackground(CARD);
        cardPanel.setBorder(BorderFactory.createEmptyBorder(32, 36, 32, 36));
        cardPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.setMaximumSize(new Dimension(1000, Integer.MAX_VALUE));

        itemPanel.setBackground(ITEM_PANEL);
        itemPanel.setPreferredSize(new Dimension(720, 240));
        itemPanel.setBorder(BorderFactory.createEmptyBorder());
        itemPanel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        itemPanel.setOpaque(false);
        itemPanel.add(itemBackdrop);
        itemBadgeStrip.setOpaque(false);
        itemBadgeStrip.removeAll();
        badgeList.clear();
        for (Category category : List.of(Category.ORGANICO, Category.RECICLABLE, Category.PELIGROSO)) {
            CategoryBadge badge = new CategoryBadge(category);
            badgeList.add(badge);
            itemBadgeStrip.add(badge);
        }
        itemPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                refreshItemFont();
                recenterItemLabel();
                layoutItemDecorations();
            }
        });
        cardPanel.add(itemPanel);
        cardPanel.add(Box.createVerticalStrut(10));
        itemBadgeStrip.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        cardPanel.add(itemBadgeStrip);

        itemLabel.setForeground(Color.WHITE);
        itemLabel.setFont(ITEM_FONT);
        itemPanel.add(itemLabel);
        layoutItemDecorations();

        configureStartOverlay(cardPanel);

        cardPanel.add(Box.createVerticalStrut(22));
        progressLabel.setFont(BODY_FONT);
        progressLabel.setForeground(new Color(0xe2, 0xe8, 0xf0));
        cardPanel.add(progressLabel);

        progressBar.setMaximum(game.summary().total());
        progressBar.setValue(0);
        progressBar.setPreferredSize(new Dimension(460, 18));
        progressBar.setBackground(new Color(0x12, 0x26, 0x3f));
        progressBar.setForeground(new Color(0x38, 0xbd, 0xf8));
        progressBar.setBorder(BorderFactory.createEmptyBorder());
        cardPanel.add(progressBar);
        cardPanel.add(Box.createVerticalStrut(20));

        feedbackLabel.setFont(FEEDBACK_FONT);
        feedbackLabel.setForeground(new Color(0x7d, 0xf3, 0x8b));
        feedbackLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        feedbackLabel.setHorizontalAlignment(SwingConstants.CENTER);
        feedbackLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        cardPanel.add(feedbackLabel);
        cardPanel.add(Box.createVerticalStrut(6));

        detailLabel.setFont(BODY_FONT);
        detailLabel.setForeground(new Color(0xe2, 0xe8, 0xf0));
        detailLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        detailLabel.setHorizontalAlignment(SwingConstants.CENTER);
        detailLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        detailLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        setDetailMessage("");
        cardPanel.add(detailLabel);
        cardPanel.add(Box.createVerticalStrut(12));

        languagePanel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        cardPanel.add(languagePanel);
        cardPanel.add(Box.createVerticalStrut(12));

        managementLabel.setFont(MANAGEMENT_FONT);
        managementLabel.setForeground(new Color(0xd6, 0xf0, 0xff));
        managementLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        managementLabel.setHorizontalAlignment(SwingConstants.CENTER);
        managementLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        managementLabel.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));
        managementLabel.setVisible(false);
        setManagementContent("", "");
        cardPanel.add(managementLabel);
        cardPanel.add(Box.createVerticalStrut(16));

        scoreLabel.setFont(BODY_FONT);
        scoreLabel.setForeground(new Color(0xe2, 0xe8, 0xf0));
        scoreLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        cardPanel.add(scoreLabel);
        cardPanel.add(Box.createVerticalStrut(20));

        nextButton.setFont(BUTTON_FONT);
        nextButton.setBackground(new Color(0x38, 0xbd, 0xf8));
        nextButton.setForeground(new Color(0x06, 0x18, 0x26));
        nextButton.setBorder(BorderFactory.createEmptyBorder(10, 28, 10, 28));
        nextButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        nextButton.setFocusPainted(false);
        nextButton.setVisible(false);
        nextButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        nextButton.addActionListener(e -> handleNext());
        cardPanel.add(nextButton);
        cardPanel.add(Box.createVerticalStrut(24));

        buttonsPanel.setOpaque(false);
        buttonsPanel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        cardPanel.add(buttonsPanel);
        cardPanel.add(Box.createVerticalStrut(16));

        createCategoryButton("Organico", Category.ORGANICO, new Color(0x4e, 0x9f, 0x3d));
        createCategoryButton("Reciclable", Category.RECICLABLE, new Color(0x1f, 0x7a, 0x8c));
        createCategoryButton("Peligroso", Category.PELIGROSO, new Color(0xe6, 0x39, 0x46));

        buttonsPanel.setVisible(false);

        restartButton.setFont(SECONDARY_BUTTON_FONT);
        restartButton.setBackground(new Color(0x1f, 0x2f, 0x46));
        restartButton.setForeground(new Color(0xe2, 0xe8, 0xf0));
        restartButton.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        restartButton.setFocusPainted(false);
        restartButton.setVisible(false);
        restartButton.addActionListener(e -> startGame());
        restartButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        cardPanel.add(restartButton);

        mainColumn.add(Box.createVerticalStrut(18));
        cardPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                refreshRichTextBlocks();
            }
        });
        JPanel centeredCard = new JPanel();
        centeredCard.setOpaque(false);
        centeredCard.setLayout(new BoxLayout(centeredCard, BoxLayout.X_AXIS));
        centeredCard.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        centeredCard.add(Box.createHorizontalGlue());
        cardPanel.setMinimumSize(new Dimension(560, 0));
        cardPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centeredCard.add(cardPanel);
        centeredCard.add(Box.createHorizontalGlue());

        mainColumn.add(centeredCard);
        mainColumn.add(Box.createVerticalGlue());

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mainColumn, buildTipColumn());
        splitPane.setResizeWeight(0.72);
        splitPane.setDividerLocation(0.72);
        splitPane.setDividerSize(10);
        splitPane.setContinuousLayout(true);
        splitPane.setOpaque(false);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        root.add(splitPane, BorderLayout.CENTER);
        return root;
    }

    private JComponent buildTipColumn() {
        JPanel tipCard = new JPanel();
        tipCard.setLayout(new BoxLayout(tipCard, BoxLayout.Y_AXIS));
        tipCard.setBackground(CARD);
        tipCard.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));
        tipCard.setPreferredSize(new Dimension(340, 0));
        tipCard.setMinimumSize(new Dimension(300, 420));
        tipCard.setMaximumSize(new Dimension(420, Integer.MAX_VALUE));

        JLabel tipTitle = new JLabel("Guías por contenedor");
        tipTitle.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        tipTitle.setForeground(new Color(0x7d, 0xf3, 0x8b));
        tipTitle.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        tipCard.add(tipTitle);
        tipCard.add(Box.createVerticalStrut(12));

        JPanel tipsGrid = new JPanel(new GridLayout(0, 1, 0, 12));
        tipsGrid.setOpaque(false);
        tipsGrid.add(createTipBox(Category.ORGANICO, "Consejos verdes", colorFor(Category.ORGANICO)));
        tipsGrid.add(createTipBox(Category.RECICLABLE, "Consejos azules", colorFor(Category.RECICLABLE)));
        tipsGrid.add(createTipBox(Category.PELIGROSO, "Consejos rojos", colorFor(Category.PELIGROSO)));
        tipCard.add(tipsGrid);
        tipCard.add(Box.createVerticalGlue());
        return tipCard;
    }

    private void installKeyboardShortcuts() {
        SwingUtilities.invokeLater(() -> {
            JComponent target = getRootPane();
            if (target == null) {
                return;
            }
            registerKey(target, "answer_organico", "1", () -> triggerCategory(Category.ORGANICO));
            registerKey(target, "answer_reciclable", "2", () -> triggerCategory(Category.RECICLABLE));
            registerKey(target, "answer_peligroso", "3", () -> triggerCategory(Category.PELIGROSO));
            registerKey(target, "next_round", "SPACE", () -> {
                if (nextButton.isVisible()) {
                    nextButton.doClick();
                }
            });
            registerZoomShortcuts(target);
        });
    }

    private void registerZoomShortcuts(JComponent target) {
        registerKey(target, "zoom_in_shift_plus", KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.SHIFT_DOWN_MASK), () -> adjustZoom(ZOOM_STEP));
        registerKey(target, "zoom_in_plus_key", KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0), () -> adjustZoom(ZOOM_STEP));
        registerKey(target, "zoom_in_add_key", KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0), () -> adjustZoom(ZOOM_STEP));
        registerKey(target, "zoom_out_minus", KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0), () -> adjustZoom(-ZOOM_STEP));
        registerKey(target, "zoom_out_subtract", KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0), () -> adjustZoom(-ZOOM_STEP));
        registerKey(target, "zoom_out_shift_underscore", KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.SHIFT_DOWN_MASK), () -> adjustZoom(-ZOOM_STEP));
    }

    private void registerKey(JComponent target, String actionKey, String stroke, Runnable task) {
        registerKey(target, actionKey, KeyStroke.getKeyStroke(stroke), task);
    }

    private void registerKey(JComponent target, String actionKey, KeyStroke stroke, Runnable task) {
        if (stroke == null) {
            return;
        }
        target.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(stroke, actionKey);
        target.getActionMap().put(actionKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                task.run();
            }
        });
    }

    private void triggerCategory(Category category) {
        JButton button = categoryButtons.get(category);
        if (button != null && button.isEnabled()) {
            button.doClick();
        }
    }

    private void installResponsiveBehavior() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateResponsiveLayout();
            }
        });
    }

    private void updateResponsiveLayout() {
        int width = Math.max(getWidth(), getMinimumSize().width);
        if (splitPane != null) {
            double ratio = width < 1400 ? 0.64 : 0.72;
            splitPane.setResizeWeight(ratio);
            splitPane.setDividerLocation(ratio);
        }
        refreshItemFont();
        recenterItemLabel();
        refreshRichTextBlocks();
    }

    private void refreshItemFont() {
        if (itemPanel.getWidth() <= 0) {
            return;
        }
        String display = lastItemText == null ? "" : lastItemText.toUpperCase(Locale.ROOT);
        itemLabel.setText(display);
        int availableWidth = Math.max(160, itemPanel.getWidth() - 60);
        int availableHeight = Math.max(48, itemPanel.getHeight() - 40);
        Font base = ITEM_FONT;
        Font current = base;
        java.awt.FontMetrics metrics = itemPanel.getFontMetrics(current);
        while ((metrics.stringWidth(display) > availableWidth || metrics.getHeight() > availableHeight) && current.getSize2D() > 16f) {
            current = base.deriveFont(current.getSize2D() - 1f);
            metrics = itemPanel.getFontMetrics(current);
        }
        itemLabel.setFont(current);
    }

    private void recenterItemLabel() {
        if (slideTimer != null && slideTimer.isRunning()) {
            return;
        }
        if (itemPanel.getWidth() <= 0 || itemPanel.getHeight() <= 0) {
            return;
        }
        Dimension preferred = itemLabel.getPreferredSize();
        int targetX = Math.max(0, (itemPanel.getWidth() - preferred.width) / 2);
        int targetY = Math.max(0, (itemPanel.getHeight() - preferred.height) / 2);
        itemLabel.setBounds(targetX, targetY, preferred.width, preferred.height);
    }

    private void refreshRichTextBlocks() {
        detailLabel.setText(formatDetail(lastDetailText));
        managementLabel.setText(formatManagement(lastReasonText, lastHandlingText));
        languagePanel.refreshWidth(contentWidth());
    }

    private void persistCatalogChanges() {
        EcoCatalogStore.save(catalog);
        game.replaceItems(catalog);
        if (professorDashboard != null) {
            professorDashboard.refreshData();
        }
    }

    private void layoutItemDecorations() {
        if (itemPanel.getWidth() <= 0 || itemPanel.getHeight() <= 0) {
            return;
        }
        itemBackdrop.setBounds(0, 0, itemPanel.getWidth(), itemPanel.getHeight());
        itemPanel.setComponentZOrder(itemLabel, 0);
        itemPanel.setComponentZOrder(itemBackdrop, 1);
    }

    private void resetBadges() {
        for (CategoryBadge badge : badgeList) {
            badge.reset();
        }
    }

    private void highlightBadge(Category category, boolean success) {
        for (CategoryBadge badge : badgeList) {
            badge.showState(category, success);
        }
    }

    private void setDetailMessage(String text) {
        lastDetailText = text == null ? "" : text;
        detailLabel.setText(formatDetail(lastDetailText));
    }

    private void setManagementContent(String reason, String handling) {
        lastReasonText = reason == null ? "" : reason;
        lastHandlingText = handling == null ? "" : handling;
        managementLabel.setText(formatManagement(lastReasonText, lastHandlingText));
    }

    private int contentWidth() {
        if (cardPanel == null) {
            return 520;
        }
        return Math.max(360, cardPanel.getWidth() - 160);
    }

    private void configureStartOverlay(JPanel container) {
        startOverlay.setLayout(new BoxLayout(startOverlay, BoxLayout.Y_AXIS));
        startOverlay.setOpaque(false);
        startOverlay.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        startOverlay.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel welcome = new JLabel("Bienvenido, " + playerName, SwingConstants.CENTER);
        welcome.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 20));
        welcome.setForeground(new Color(0x7d, 0xf3, 0x8b));
        welcome.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        JLabel intro = new JLabel(
            "<html><div style='text-align:center;width:420px;'>Aprende a separar residuos identificando palabras clave en cada tarjeta. Despues de responder usa el boton 'Siguiente' para avanzar.</div></html>"
        );
        intro.setFont(BODY_FONT);
        intro.setForeground(new Color(0xe2, 0xe8, 0xf0));
        intro.setAlignmentX(JComponent.CENTER_ALIGNMENT);

        JButton startButton = new JButton("Comenzar partida");
        stylePrimaryButton(startButton);
        startButton.addActionListener(e -> startGame());
        startButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);

        startOverlay.add(welcome);
        startOverlay.add(Box.createVerticalStrut(10));
        startOverlay.add(intro);
        startOverlay.add(Box.createVerticalStrut(20));
        startOverlay.add(startButton);

        container.add(Box.createVerticalStrut(12));
        container.add(startOverlay);
    }

    private JButton createLogoutButton() {
        JButton button = new JButton("Cerrar sesión");
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setBackground(new Color(0x25, 0x3f, 0x4f));
        button.setForeground(new Color(0xff, 0xee, 0xea));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        button.addActionListener(e -> requestLogout());
        return button;
    }

    private void requestLogout() {
        int option = JOptionPane.showConfirmDialog(
            this,
            "¿Deseas cerrar sesión y cambiar de perfil?",
            "Cerrar sesión",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        if (option == JOptionPane.YES_OPTION) {
            logoutAction.run();
        }
    }

    private void adjustZoom(double delta) {
        double newZoom = Math.max(ZOOM_MIN, Math.min(ZOOM_MAX, zoomFactor + delta));
        if (Math.abs(newZoom - zoomFactor) < 0.001) {
            return;
        }
        zoomFactor = newZoom;
        applyZoomStyles();
    }

    private void applyZoomStyles() {
        float titleSize = (float) (TITLE_FONT.getSize2D() * zoomFactor);
        if (headerTitle != null) {
            headerTitle.setFont(TITLE_FONT.deriveFont(titleSize));
        }
        if (headerSubtitle != null) {
            headerSubtitle.setFont(SUBTITLE_FONT.deriveFont((float) (SUBTITLE_FONT.getSize2D() * zoomFactor)));
        }
        if (headerProfile != null) {
            headerProfile.setFont(BODY_FONT.deriveFont((float) (BODY_FONT.getSize2D() * zoomFactor)));
        }

        itemLabel.setFont(ITEM_FONT.deriveFont((float) (ITEM_FONT.getSize2D() * zoomFactor)));
        Font bodyFont = BODY_FONT.deriveFont((float) (BODY_FONT.getSize2D() * zoomFactor));
        progressLabel.setFont(bodyFont);
        detailLabel.setFont(bodyFont);
        scoreLabel.setFont(bodyFont);
        feedbackLabel.setFont(FEEDBACK_FONT.deriveFont((float) (FEEDBACK_FONT.getSize2D() * zoomFactor)));
        managementLabel.setFont(MANAGEMENT_FONT.deriveFont((float) (MANAGEMENT_FONT.getSize2D() * zoomFactor)));
        nextButton.setFont(BUTTON_FONT.deriveFont((float) (BUTTON_FONT.getSize2D() * zoomFactor)));
        restartButton.setFont(SECONDARY_BUTTON_FONT.deriveFont((float) (SECONDARY_BUTTON_FONT.getSize2D() * zoomFactor)));

        int verticalPadding = (int) Math.round(32 * zoomFactor);
        int horizontalPadding = (int) Math.round(36 * zoomFactor);
        cardPanel.setBorder(BorderFactory.createEmptyBorder(verticalPadding, horizontalPadding, verticalPadding, horizontalPadding));

        int panelWidth = (int) Math.round(720 * zoomFactor);
        int panelHeight = (int) Math.round(240 * zoomFactor);
        itemPanel.setPreferredSize(new Dimension(panelWidth, panelHeight));
        itemPanel.setMaximumSize(new Dimension((int) Math.round(900 * zoomFactor), (int) Math.round(280 * zoomFactor)));
        cardPanel.setMaximumSize(new Dimension((int) Math.round(1000 * zoomFactor), Integer.MAX_VALUE));

        int barWidth = (int) Math.round(460 * zoomFactor);
        int barHeight = (int) Math.max(12, Math.round(18 * zoomFactor));
        progressBar.setPreferredSize(new Dimension(barWidth, barHeight));

        languagePanel.applyZoom(zoomFactor);
        for (TipBox box : tipBoxes.values()) {
            box.applyZoom(zoomFactor);
        }
        for (CategoryBadge badge : badgeList) {
            badge.applyZoom(zoomFactor);
        }
        cardPanel.revalidate();
        cardPanel.repaint();
    }

    private static String buildSummaryMessage(Summary summary) {
        if (summary.total() == 0) {
            return "Juega una ronda completa para obtener una evaluación.";
        }
        double ratio = (double) summary.correct() / summary.total();
        if (summary.score() < 0) {
            return "Tu puntaje final es negativo. Repasa las pistas y vuelve a intentarlo.";
        }
        if (ratio == 1.0) {
            return "¡Excelente! Clasificaste correctamente todos los residuos.";
        }
        if (ratio >= 0.5) {
            return "Buen trabajo, ya dominas más de la mitad. Sigue practicando para llegar al 100%.";
        }
        return "Necesitamos reforzar conceptos: intenta observar las palabras clave y los consejos.";
    }


    private void stylePrimaryButton(JButton button) {
        button.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        button.setBackground(new Color(0x38, 0xbd, 0xf8));
        button.setForeground(new Color(0x06, 0x18, 0x26));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        button.setOpaque(true);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(TextUtils.lighten(button.getBackground(), 0.2));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(0x38, 0xbd, 0xf8));
            }
        });
    }

    private void createCategoryButton(String text, Category category, Color baseColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 16));
        button.setBackground(baseColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        button.setEnabled(false);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(220, 64));
        button.setMinimumSize(new Dimension(160, 56));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(TextUtils.lighten(baseColor, 0.2));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(baseColor);
            }
        });
        button.addActionListener(e -> answer(category));
        buttonsPanel.add(button);
        categoryButtons.put(category, button);
    }

    private TipBox createTipBox(Category category, String title, Color color) {
        TipBox box = new TipBox(title, color, category);
        tipBoxes.put(category, box);
        return box;
    }

    private void initializeTipSources() {
        Map<Category, List<String>> baseTips = EcoData.tips();
        for (Category category : List.of(Category.ORGANICO, Category.RECICLABLE, Category.PELIGROSO)) {
            List<String> tips = new ArrayList<>(baseTips.getOrDefault(
                category,
                baseTips.getOrDefault(Category.DESCONOCIDO, List.of(DEFAULT_TIP))
            ));
            if (tips.isEmpty()) {
                tips.add(DEFAULT_TIP);
            }
            tipSources.put(category, tips);
            tipIndices.put(category, 0);
        }
    }

    private void resetTipContent() {
        for (Category category : tipBoxes.keySet()) {
            cancelTipReset(category);
            List<String> tips = tipSources.get(category);
            if (tips != null && tips.size() > 1) {
                Collections.shuffle(tips);
            }
            tipIndices.put(category, 0);
            setTipForCategory(category, false);
        }
    }

    private void cancelTipReset(Category category) {
        Timer timer = tipResetTimers.remove(category);
        if (timer != null) {
            timer.stop();
        }
    }

    private void startTipRotation() {
        if (tipBoxes.isEmpty()) {
            return;
        }
        if (tipTimer != null) {
            tipTimer.stop();
        }
        tipTimer = new Timer(8000, e -> rotateTips());
        tipTimer.setRepeats(true);
        tipTimer.start();
    }

    private void rotateTips() {
        for (Category category : tipBoxes.keySet()) {
            if (tipResetTimers.containsKey(category)) {
                continue;
            }
            advanceTip(category);
        }
    }

    private void advanceTip(Category category) {
        setTipForCategory(category, true);
    }

    private void setTipForCategory(Category category, boolean advance) {
        TipBox box = tipBoxes.get(category);
        if (box == null) {
            return;
        }
        List<String> tips = tipSources.get(category);
        if (tips == null || tips.isEmpty()) {
            box.setTip(formatTip(DEFAULT_TIP));
            box.highlight(false);
            return;
        }
        int index = tipIndices.getOrDefault(category, 0);
        if (index < 0 || index >= tips.size()) {
            index = 0;
        }
        String tip = tips.get(index);
        box.setTip(formatTip(tip));
        box.highlight(false);
        if (advance) {
            index = (index + 1) % tips.size();
        }
        tipIndices.put(category, index);
    }

    private void showTemporaryTip(Category category, String tip) {
        Category target = tipBoxes.containsKey(category) ? category : Category.ORGANICO;
        TipBox box = tipBoxes.get(target);
        if (box == null) {
            return;
        }
        cancelTipReset(target);
        box.setTip(formatTip(tip));
        box.highlight(true);
        Timer timer = new Timer(5500, e -> {
            Timer source = (Timer) e.getSource();
            source.stop();
            box.highlight(false);
            tipResetTimers.remove(target);
            advanceTip(target);
        });
        timer.setRepeats(false);
        timer.start();
        tipResetTimers.put(target, timer);
    }

    private void startGame() {
        game.replaceItems(catalog);
        currentExample = null;
        currentTrace = null;
        automataAgreements = 0;
        automataComparisons = 0;
        updateProgress(new Progress(0, game.summary().total()));
        scoreLabel.setText("Puntaje: 0  |  Racha: 0");
        feedbackLabel.setText("");
        setDetailMessage("");
        setManagementContent("", "");
        managementLabel.setVisible(false);
        nextButton.setVisible(false);
        resetTipContent();
        startTipRotation();
        removeSummaryPanel();
        restartButton.setVisible(false);
        startOverlay.setVisible(false);
        buttonsPanel.setVisible(true);
        buttonsPanel.setOpaque(false);
        languagePanel.reset();
        if (languageInspector != null) {
            languageInspector.clearTrace();
        }
        resetBadges();
        lastItemText = "";
        for (JButton button : categoryButtons.values()) {
            button.setEnabled(true);
        }
        animateItem("");
        nextRound();
    }

    private void nextRound() {
        if (slideTimer != null) {
            slideTimer.stop();
        }

        var item = game.nextItem();
        if (item == null) {
            showSummary();
            return;
        }

        currentExample = game.present(item);
        currentTrace = ClassificationEngines.trace(item.description());
        registerAutomataAgreement(currentTrace);
        animateItem(capitalize(currentExample.item().description()));
        updateProgress(game.progress());
        feedbackLabel.setText("");
        setDetailMessage("");
        setManagementContent("", "");
        managementLabel.setVisible(false);
        nextButton.setVisible(false);
        resetBadges();
        languagePanel.preview(currentTrace);
        if (languageInspector != null) {
            languageInspector.previewTrace("Ronda " + game.progress().current(), currentTrace);
        }
    }

    private void registerAutomataAgreement(Trace trace) {
        if (trace == null || trace.result() == null) {
            return;
        }
        AutomataAnalysis analysis = trace.automataAnalysis();
        if (analysis == null || analysis.tie() || analysis.winner() == null) {
            return;
        }
        analysis.winner().toGameCategory().ifPresent(mapped -> {
            automataComparisons++;
            if (trace.result().category() == mapped) {
                automataAgreements++;
            }
        });
    }

    private void answer(Category category) {
        if (currentExample == null) {
            return;
        }
        for (JButton button : categoryButtons.values()) {
            button.setEnabled(false);
        }
        CheckResult result = game.check(currentExample, category);
        feedbackLabel.setText(result.message());
        feedbackLabel.setForeground(result.correct() ? new Color(0x7d, 0xf3, 0x8b) : new Color(0xfc, 0xa5, 0xa5));

        StringBuilder builder = new StringBuilder();
        if (result.engine() != null) {
            builder.append("Motor: ").append(result.engine());
        }
        if (result.trigger() != null) {
            if (!builder.isEmpty()) {
                builder.append(" | ");
            }
            builder.append("Palabra clave: '").append(result.trigger()).append('\'');
        }
        if (result.expected() != Category.DESCONOCIDO) {
            if (!builder.isEmpty()) {
                builder.append(" | ");
            }
            builder.append("Categoria correcta: ").append(result.expected().display());
        }
        setDetailMessage(builder.toString());

        setManagementContent(result.reason(), result.handling());
        managementLabel.setVisible(true);

        showTemporaryTip(result.expected(), result.tip());
        scoreLabel.setText("Puntaje: " + game.score() + "  |  Racha: " + game.streak());

        Trace snapshot = currentTrace;
        languagePanel.showResult(result, snapshot);
        if (languageInspector != null && snapshot != null) {
            languageInspector.showResult("Ronda " + game.progress().current(), result, snapshot);
        }
        currentTrace = null;
        highlightBadge(result.expected(), result.correct());

        nextButton.setVisible(true);
        nextButton.requestFocusInWindow();
    }

    private void showSummary() {
        buttonsPanel.setVisible(false);
        restartButton.setVisible(true);
        currentExample = null;
        managementLabel.setText("");
        managementLabel.setVisible(false);

        removeSummaryPanel();

        Summary summary = game.summary();
        summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setOpaque(false);
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        JLabel scoreResume = new JLabel("Residuos correctos: " + summary.correct() + " de " + summary.total());
        scoreResume.setFont(BODY_FONT);
        scoreResume.setForeground(new Color(0xe2, 0xe8, 0xf0));
        scoreResume.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        scoreResume.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel finalScore = new JLabel("Puntaje final: " + summary.score());
        finalScore.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        finalScore.setForeground(new Color(0x7d, 0xf3, 0x8b));
        finalScore.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        finalScore.setHorizontalAlignment(SwingConstants.CENTER);
        summaryPanel.add(scoreResume);
        summaryPanel.add(Box.createVerticalStrut(6));
        summaryPanel.add(finalScore);
        JLabel summaryFeedback = new JLabel(buildSummaryMessage(summary));
        summaryFeedback.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        summaryFeedback.setForeground(new Color(0xff, 0xec, 0xc4));
        summaryFeedback.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        summaryFeedback.setHorizontalAlignment(SwingConstants.CENTER);
        summaryPanel.add(Box.createVerticalStrut(6));
        summaryPanel.add(summaryFeedback);
        if (automataComparisons > 0) {
            int percent = (int) Math.round(((double) automataAgreements / automataComparisons) * 100);
            JLabel automataLabel = new JLabel(
                "Modo Automatas (AFD): acuerdo " + percent + "% (" + automataAgreements + "/" + automataComparisons + ")"
            );
            automataLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            automataLabel.setForeground(new Color(0x7d, 0xf3, 0x8b));
            automataLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            automataLabel.setHorizontalAlignment(SwingConstants.CENTER);
            summaryPanel.add(Box.createVerticalStrut(4));
            summaryPanel.add(automataLabel);
        }

        if (!summary.history().isEmpty()) {
            summaryPanel.add(Box.createVerticalStrut(12));
            summaryPanel.add(buildHistoryPanel(summary.history()));
        }

        summarySpacer = Box.createVerticalStrut(12);
        cardPanel.add(summarySpacer);
        cardPanel.add(summaryPanel);
        cardPanel.revalidate();
        cardPanel.repaint();
        showSummaryDialog(summary);
        languagePanel.showSummary();
    }

    private void showSummaryDialog(Summary summary) {
        SummaryDialog dialog = new SummaryDialog(this, summary);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void animateItem(String text) {
        if (slideTimer != null) {
            slideTimer.stop();
        }

        if (itemPanel.getWidth() < 10) {
            SwingUtilities.invokeLater(() -> animateItem(text));
            return;
        }

        lastItemText = text == null ? "" : text;
        String displayText = lastItemText;
        itemLabel.setText(displayText.toUpperCase(Locale.ROOT));
        refreshItemFont();
        Dimension preferred = itemLabel.getPreferredSize();
        int targetX = Math.max(0, (itemPanel.getWidth() - preferred.width) / 2);
        int targetY = Math.max(0, (itemPanel.getHeight() - preferred.height) / 2);
        itemLabel.setBounds(-preferred.width, targetY, preferred.width, preferred.height);

        slideTimer = new Timer(16, event -> {
            int nextX = itemLabel.getX() + SLIDE_SPEED;
            if (nextX >= targetX) {
                nextX = targetX;
                slideTimer.stop();
            }
            itemLabel.setLocation(nextX, targetY);
        });
        slideTimer.start();
    }

    private void updateProgress(Progress progress) {
        progressLabel.setText("Ronda " + progress.current() + " / " + progress.total());
        progressBar.setMaximum(progress.total());
        progressBar.setValue(progress.current());
    }

    private void removeSummaryPanel() {
        if (summarySpacer != null) {
            cardPanel.remove(summarySpacer);
            summarySpacer = null;
        }
        if (summaryPanel != null) {
            cardPanel.remove(summaryPanel);
            summaryPanel = null;
        }
        cardPanel.revalidate();
        cardPanel.repaint();
    }

    private String formatTip(String tip) {
        String text = tip == null || tip.isBlank()
            ? DEFAULT_TIP
            : tip;
        int width = 300;
        return "<html><div style='width:" + width + "px;padding:8px 4px;line-height:1.45;font-size:14px;'>"
            + escapeHtml(text)
            + "</div></html>";
    }

    private String formatDetail(String text) {
        String base = text == null || text.isBlank() ? "" : text;
        return "<html><div style='width:" + contentWidth() + "px;line-height:1.4;'>"
            + escapeHtml(base)
            + "</div></html>";
    }

    private String formatManagement(String reason, String handling) {
        String reasonText = (reason == null || reason.isBlank()) ? DEFAULT_TIP : reason;
        String handlingText = (handling == null || handling.isBlank())
            ? "Depositalo en el contenedor correcto y evita mezclarlo con otros residuos."
            : handling;
        return "<html><div style='width:" + Math.max(320, contentWidth() - 60) + "px;padding:6px 0;line-height:1.5;'>"
            + "<span style='color:#7df38b;font-weight:600;'>Por que?</span><br/>"
            + escapeHtml(reasonText)
            + "<br/><br/><span style='color:#38bdf8;font-weight:600;'>Como gestionarlo?</span><br/>"
            + escapeHtml(handlingText)
            + "</div></html>";
    }

    private void handleNext() {
        nextButton.setVisible(false);
        for (JButton button : categoryButtons.values()) {
            button.setEnabled(true);
        }
        nextRound();
    }

    private JComponent buildHistoryPanel(List<RoundRecord> history) {
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(new Color(0x12, 0x26, 0x3f));
        listPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x0d, 0x23, 0x3b)),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        for (int i = 0; i < history.size(); i++) {
            RoundRecord record = history.get(i);
            JPanel row = new JPanel();
            row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
            row.setOpaque(false);
            JPanel indicator = new JPanel();
            indicator.setPreferredSize(new Dimension(12, 12));
            indicator.setMaximumSize(new Dimension(12, 12));
            indicator.setBackground(record.correct() ? new Color(0x4e, 0x9f, 0x3d) : new Color(0xe6, 0x39, 0x46));
            indicator.setBorder(BorderFactory.createLineBorder(new Color(0x06, 0x18, 0x26)));
            row.add(indicator);
            row.add(Box.createHorizontalStrut(10));
            JLabel entryLabel = new JLabel(historyLine(record));
            entryLabel.setFont(BODY_FONT);
            entryLabel.setForeground(record.correct() ? new Color(0xb8, 0xf7, 0xcc) : new Color(0xff, 0xd5, 0xd5));
            row.add(entryLabel);
            row.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            listPanel.add(row);
            if (i < history.size() - 1) {
                listPanel.add(Box.createVerticalStrut(8));
            }
        }

        if (history.size() <= 6) {
            return listPanel;
        }
        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setPreferredSize(new Dimension(360, 220));
        scrollPane.setBackground(new Color(0x12, 0x26, 0x3f));
        scrollPane.getViewport().setBackground(new Color(0x12, 0x26, 0x3f));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return scrollPane;
    }

    private String historyLine(RoundRecord record) {
        StringBuilder builder = new StringBuilder();
        builder.append(record.item()).append(" -> ").append(record.expected().display());
        if (!record.correct() && record.answer() != null) {
            builder.append(" | Tu respuesta: ").append(record.answer().display());
        }
        return "<html><div style='width:340px;line-height:1.35;'>" + escapeHtml(builder.toString()) + "</div></html>";
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");
    }

    private final class LanguagePanel extends JPanel {
        private final JLabel titleLabel = new JLabel("Laboratorio de lenguajes automatas");
        private final JLabel engineLabel = new JLabel();
        private final JLabel automataLabel = new JLabel();
        private final JLabel clueLabel = new JLabel();
        private final JProgressBar confidenceBar = new JProgressBar(0, 100);
        private final JPanel chipsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        private final BadgeLabel regexBadge = new BadgeLabel("Patrones 0", new Color(0x4f, 0xc2, 0xff));
        private final BadgeLabel automataBadge = new BadgeLabel("Automata 0", new Color(0x7d, 0xf3, 0x8b));
        private final BadgeLabel tokenBadge = new BadgeLabel("Tokens 0", new Color(0xff, 0xd5, 0x8a));
        private final JLabel unmatchedLabel = new JLabel();
        private int widthHint = 520;
        private String engineText = "";
        private String automataText = "";
        private String clueText = "";

        LanguagePanel() {
            setOpaque(true);
            setBackground(new Color(0x12, 0x26, 0x3f));
            setBorder(BorderFactory.createEmptyBorder(14, 18, 16, 18));
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            titleLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
            titleLabel.setForeground(new Color(0x7d, 0xf3, 0x8b));
            titleLabel.setAlignmentX(LEFT_ALIGNMENT);

            for (JLabel label : List.of(engineLabel, automataLabel, clueLabel)) {
                label.setFont(BODY_FONT);
                label.setForeground(new Color(0xe2, 0xe8, 0xf0));
                label.setAlignmentX(LEFT_ALIGNMENT);
            }

            chipsPanel.setOpaque(false);
            chipsPanel.setAlignmentX(LEFT_ALIGNMENT);
            chipsPanel.add(regexBadge);
            chipsPanel.add(automataBadge);
            chipsPanel.add(tokenBadge);

            unmatchedLabel.setFont(BODY_FONT);
            unmatchedLabel.setForeground(new Color(0xff, 0xec, 0xc4));
            unmatchedLabel.setAlignmentX(LEFT_ALIGNMENT);

            confidenceBar.setStringPainted(true);
            confidenceBar.setForeground(new Color(0x38, 0xbd, 0xf8));
            confidenceBar.setBackground(new Color(0x0d, 0x1f, 0x34));
            confidenceBar.setMaximum(100);
            confidenceBar.setMinimum(0);
            confidenceBar.setAlignmentX(LEFT_ALIGNMENT);

            add(titleLabel);
            add(Box.createVerticalStrut(6));
            add(engineLabel);
            add(Box.createVerticalStrut(6));
            add(automataLabel);
            add(Box.createVerticalStrut(6));
            add(clueLabel);
            add(Box.createVerticalStrut(8));
            add(chipsPanel);
            add(Box.createVerticalStrut(4));
            add(unmatchedLabel);
            add(Box.createVerticalStrut(8));
            add(confidenceBar);
            reset();
        }

        void reset() {
            setEngineText("Motor de lenguaje preparado. Usa las pistas para deducir el contenedor correcto.");
            setAutomataText("El automata esperara a que leas la tarjeta antes de revelar coincidencias.");
            setClueText("Responde para ver como las gramaticas regulares justifican la respuesta.");
            regexBadge.setValue("Patrones 0");
            regexBadge.emphasize(false);
            automataBadge.setValue("Automata 0");
            automataBadge.emphasize(false);
            tokenBadge.setValue("Tokens 0");
            tokenBadge.emphasize(false);
            tokenBadge.setToolTipText("Tokens sin clasificar: 0");
            updateUnmatched(List.of());
            confidenceBar.setValue(25);
            confidenceBar.setString("Listo");
        }

        void preview(Trace trace) {
            if (trace == null) {
                reset();
                return;
            }
            int regexHits = trace.regexMatches().values().stream().mapToInt(List::size).sum();
            int automataHits = trace.automataMatches().values().stream().mapToInt(List::size).sum();
            String engineName = trace.result().engine() == null ? "Analizador hibrido" : trace.result().engine();
            setEngineText("Motor activo: " + engineName + ". Tokens leidos: " + trace.tokens().size());
            setAutomataText("Coincidencias de lenguaje: " + regexHits + " patrones regulares y "
                + automataHits + " tokens del automata determinista.");
            setClueText("El sistema esta procesando las palabras clave sin mostrar la respuesta.");
            regexBadge.setValue("Patrones " + regexHits);
            regexBadge.emphasize(regexHits > 0);
            automataBadge.setValue("Automata " + automataHits);
            automataBadge.emphasize(automataHits > 0);
            tokenBadge.setValue("Tokens " + trace.tokens().size());
            tokenBadge.emphasize(trace.tokens().size() > 0);
            tokenBadge.setToolTipText("Tokens sin clasificar: " + trace.unmatchedTokens().size());
            updateUnmatched(trace.unmatchedTokens());
            int confidence = Math.min(70, 30 + regexHits * 5 + automataHits * 5);
            confidenceBar.setValue(confidence);
            confidenceBar.setString("Analizando...");
        }

        void showResult(CheckResult result, Trace trace) {
            if (result == null) {
                return;
            }
            List<String> matches = new ArrayList<>();
            int regexMatchesCount = 0;
            int automataMatchesCount = 0;
            if (trace != null) {
                List<String> regexMatches = trace.regexMatches().getOrDefault(result.expected(), List.of());
                List<String> automataMatches = trace.automataMatches().getOrDefault(result.expected(), List.of());
                matches.addAll(regexMatches);
                matches.addAll(automataMatches);
                regexMatchesCount = regexMatches.size();
                automataMatchesCount = automataMatches.size();
            }
            if (matches.isEmpty() && result.trigger() != null) {
                matches = List.of(result.trigger());
            }
            String engineName = result.engine() == null ? "Hibrido" : result.engine();
            setEngineText("Resolucion del motor: " + engineName + ". Categoria esperada: " + result.expected().display() + ".");
            if (matches.isEmpty()) {
                setAutomataText("No se detectaron palabras exactas; se aplico inferencia contextual.");
                setClueText("Revisa la descripcion del residuo para encontrar terminos mas precisos.");
            } else {
                setAutomataText("El automata marco estas pistas: " + String.join(", ", matches) + ".");
                setClueText("Relaciona las palabras resaltadas con las guias laterales para reforzar el aprendizaje.");
            }
            regexBadge.emphasize("Expresion regular".equalsIgnoreCase(engineName));
            automataBadge.emphasize(engineName.toLowerCase(Locale.ROOT).contains("automata"));
            tokenBadge.emphasize(result.correct());
            regexBadge.setValue("Patrones " + regexMatchesCount);
            automataBadge.setValue("Automata " + automataMatchesCount);
            int totalTokens = trace == null ? 0 : trace.tokens().size();
            tokenBadge.setValue("Tokens " + totalTokens);
            tokenBadge.setToolTipText("Tokens sin clasificar: " + (trace == null ? 0 : trace.unmatchedTokens().size()));
            updateUnmatched(trace == null ? List.of() : trace.unmatchedTokens());
            int confidence = Math.min(100, (result.correct() ? 50 : 30) + matches.size() * 12);
            confidenceBar.setValue(confidence);
            confidenceBar.setString("Confianza " + confidence + "%");
        }

        void showSummary() {
            setAutomataText("Analisis finalizado. Usa 'Volver a intentar' para generar nuevas trazas de lenguaje.");
            setClueText("Tus respuestas quedan registradas para reforzar los patrones que reconoce el automata.");
            regexBadge.emphasize(false);
            automataBadge.emphasize(false);
            tokenBadge.emphasize(false);
            tokenBadge.setToolTipText("Tokens sin clasificar: 0");
            updateUnmatched(List.of());
            confidenceBar.setValue(100);
            confidenceBar.setString("Sesion terminada");
        }

        void refreshWidth(int width) {
            widthHint = Math.max(320, width);
            applyTexts();
        }

        private void setEngineText(String text) {
            engineText = text;
            engineLabel.setText(asHtml(engineText));
        }

        private void setAutomataText(String text) {
            automataText = text;
            automataLabel.setText(asHtml(automataText));
        }

        private void setClueText(String text) {
            clueText = text;
            clueLabel.setText(asHtml(clueText));
        }

        private void applyTexts() {
            engineLabel.setText(asHtml(engineText));
            automataLabel.setText(asHtml(automataText));
            clueLabel.setText(asHtml(clueText));
        }

        private String asHtml(String text) {
            return "<html><div style='width:" + widthHint + "px;line-height:1.4;'>"
                + escapeHtml(text == null ? "" : text)
                + "</div></html>";
        }

        private void updateUnmatched(List<String> tokens) {
            if (tokens == null || tokens.isEmpty()) {
                unmatchedLabel.setText("Todos los tokens fueron reconocidos por el automata.");
            } else {
                unmatchedLabel.setText("Tokens sin clasificar: " + String.join(", ", tokens));
            }
        }

        void applyZoom(double factor) {
            float title = (float) (14 * factor);
            titleLabel.setFont(titleLabel.getFont().deriveFont(title));
            Font body = BODY_FONT.deriveFont((float) (BODY_FONT.getSize2D() * factor));
            engineLabel.setFont(body);
            automataLabel.setFont(body);
            clueLabel.setFont(body);
            unmatchedLabel.setFont(body);
            regexBadge.applyZoom(factor);
            automataBadge.applyZoom(factor);
            tokenBadge.applyZoom(factor);
            Dimension pref = confidenceBar.getPreferredSize();
            int height = (int) Math.max(12, Math.round(18 * factor));
            confidenceBar.setPreferredSize(new Dimension(pref.width, height));
            revalidate();
            repaint();
        }
    }

    private final class LanguageInsightPanel extends JPanel {
        private final JTextArea inputArea = new JTextArea(4, 32);
        private final JButton analyzeButton = new JButton("Analizar texto");
        private final JLabel sourceLabel = new JLabel("Sin datos recientes");
        private final JLabel classificationLabel = new JLabel("-");
        private final JLabel engineLabel = new JLabel("-");
        private final JLabel normalizedLabel = new JLabel("-");
        private final JLabel unmatchedLabel = new JLabel("-");
        private final LanguageSummaryTableModel summaryModel = new LanguageSummaryTableModel();
        private final JTable summaryTable = new JTable(summaryModel);
        private final TokenBreakdownTableModel tokenModel = new TokenBreakdownTableModel();
        private final JTable tokenTable = new JTable(tokenModel);
        private final TokenChipsView tokenChipsView = new TokenChipsView();
        private final JToggleButton automataToggle = new JToggleButton("Modo Automatas (AFD)");
        private final AutomataView automataView = new AutomataView();
        private final BadgeLabel regexBadge = new BadgeLabel("Patrones 0", new Color(0x4f, 0xc2, 0xff));
        private final BadgeLabel automataBadge = new BadgeLabel("Automata 0", new Color(0x7d, 0xf3, 0x8b));
        private final BadgeLabel tokensBadge = new BadgeLabel("Tokens 0", new Color(0xff, 0xd5, 0x8a));
        private final JPanel automataTabContainer = new JPanel(new CardLayout());
        private final JPanel automataPlaceholder = createAutomataPlaceholder();
        private AutomataAnalysis latestAutomata;
        private Trace latestTrace;

        LanguageInsightPanel() {
            setLayout(new BorderLayout(16, 16));
            setBackground(BACKGROUND);
            setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

            JComponent header = buildHeader();
            header.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
            add(header, BorderLayout.NORTH);

            JSplitPane split = buildMainSplit();
            split.setBorder(BorderFactory.createEmptyBorder());
            add(split, BorderLayout.CENTER);

            JTabbedPane tabs = buildDetailTabs();
            tabs.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
            add(tabs, BorderLayout.SOUTH);

            clearTrace();
        }

        private JPanel buildHeader() {
            JPanel panel = new JPanel();
            panel.setOpaque(false);
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            JLabel title = new JLabel("Laboratorio de lenguajes");
            title.setForeground(new Color(0x7d, 0xf3, 0x8b));
            title.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 16));
            JLabel subtitle = new JLabel("Explora paso a paso como los patrones y el AFD justifican cada clasificacion.");
            subtitle.setForeground(new Color(0xe2, 0xe8, 0xf0));
            subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            panel.add(title);
            panel.add(Box.createVerticalStrut(2));
            panel.add(subtitle);
            return panel;
        }

        private JSplitPane buildMainSplit() {
            JPanel inputColumn = buildInputColumn();
            JPanel summaryColumn = buildSummaryColumn();
            inputColumn.setPreferredSize(new Dimension(420, 0));
            summaryColumn.setPreferredSize(new Dimension(420, 0));
            JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputColumn, summaryColumn);
            split.setResizeWeight(0.48);
            split.setOpaque(false);
            split.setBorder(BorderFactory.createEmptyBorder());
            split.setPreferredSize(new Dimension(0, 360));
            split.setMinimumSize(new Dimension(0, 280));
            return split;
        }

        private JPanel buildInputColumn() {
            inputArea.setLineWrap(true);
            inputArea.setWrapStyleWord(true);
            inputArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            inputArea.setBackground(new Color(0x0d, 0x23, 0x3b));
            inputArea.setForeground(new Color(0xe2, 0xe8, 0xf0));
            inputArea.setCaretColor(new Color(0xe2, 0xe8, 0xf0));

            analyzeButton.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
            analyzeButton.setBackground(new Color(0x38, 0xbd, 0xf8));
            analyzeButton.setForeground(new Color(0x06, 0x18, 0x26));
            analyzeButton.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
            analyzeButton.setFocusPainted(false);
            analyzeButton.addActionListener(e -> analyzeManual());

            automataToggle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            automataToggle.setForeground(new Color(0xe2, 0xe8, 0xf0));
            automataToggle.setBackground(new Color(0x15, 0x2c, 0x46));
            automataToggle.setBorder(BorderFactory.createLineBorder(new Color(0x1f, 0x2f, 0x46)));
            automataToggle.setFocusPainted(false);
            automataToggle.addActionListener(e -> updateAutomataVisibility());

            JPanel cardContent = new JPanel();
            cardContent.setOpaque(false);
            cardContent.setLayout(new BoxLayout(cardContent, BoxLayout.Y_AXIS));
            JLabel instruction = new JLabel("Escribe un residuo y pulsa Analizar para ver la justificacion completa.");
            instruction.setForeground(new Color(0xe2, 0xe8, 0xf0));
            instruction.setAlignmentX(LEFT_ALIGNMENT);
            JScrollPane inputScroll = new JScrollPane(inputArea);
            inputScroll.setBorder(BorderFactory.createLineBorder(new Color(0x1f, 0x2f, 0x46)));
            analyzeButton.setAlignmentX(LEFT_ALIGNMENT);
            automataToggle.setAlignmentX(LEFT_ALIGNMENT);
            cardContent.add(instruction);
            cardContent.add(Box.createVerticalStrut(6));
            cardContent.add(inputScroll);
            cardContent.add(Box.createVerticalStrut(8));
            cardContent.add(analyzeButton);
            cardContent.add(Box.createVerticalStrut(6));
            cardContent.add(automataToggle);

            JPanel column = new JPanel();
            column.setOpaque(false);
            column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
            column.add(wrapCard("Entrada manual", cardContent));
            column.setAlignmentX(LEFT_ALIGNMENT);
            column.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            return column;
        }

        private JPanel buildSummaryColumn() {
            JPanel infoList = new JPanel();
            infoList.setOpaque(false);
            infoList.setLayout(new BoxLayout(infoList, BoxLayout.Y_AXIS));
            for (JLabel label : List.of(sourceLabel, classificationLabel, engineLabel, normalizedLabel, unmatchedLabel)) {
                label.setForeground(new Color(0xe2, 0xe8, 0xf0));
                label.setAlignmentX(LEFT_ALIGNMENT);
                label.setFont(BODY_FONT);
                infoList.add(label);
                infoList.add(Box.createVerticalStrut(4));
            }
            JPanel badges = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            badges.setOpaque(false);
            badges.add(regexBadge);
            badges.add(automataBadge);
            badges.add(tokensBadge);

            JPanel infoWrapper = new JPanel(new BorderLayout());
            infoWrapper.setOpaque(false);
            infoWrapper.add(infoList, BorderLayout.CENTER);
            infoWrapper.add(badges, BorderLayout.SOUTH);

            JPanel column = new JPanel();
            column.setOpaque(false);
            column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
            column.add(wrapCard("Resumen del analisis", infoWrapper));
            column.add(Box.createVerticalStrut(12));
            column.add(wrapCard("Tokens normalizados", createTokenChipCard()));
            column.add(Box.createVerticalStrut(6));
            column.setAlignmentX(LEFT_ALIGNMENT);
            column.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            return column;
        }

        private JTabbedPane buildDetailTabs() {
            summaryTable.setRowHeight(28);
            summaryTable.setBackground(new Color(0x12, 0x26, 0x3f));
            summaryTable.setForeground(new Color(0xe2, 0xe8, 0xf0));
            summaryTable.setShowHorizontalLines(false);
            summaryTable.setShowVerticalLines(false);
            summaryTable.getTableHeader().setReorderingAllowed(false);
            summaryTable.getTableHeader().setBackground(new Color(0x15, 0x2c, 0x46));
            summaryTable.getTableHeader().setForeground(Color.BLACK);
            summaryTable.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
            summaryTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            LanguageSummaryCellRenderer renderer = new LanguageSummaryCellRenderer(summaryModel);
            for (int i = 0; i < summaryModel.getColumnCount(); i++) {
                summaryTable.getColumnModel().getColumn(i).setCellRenderer(renderer);
            }
            JScrollPane summaryScroll = new JScrollPane(summaryTable);
            summaryScroll.setBorder(BorderFactory.createEmptyBorder());
            summaryScroll.getViewport().setBackground(new Color(0x12, 0x26, 0x3f));
            summaryScroll.setPreferredSize(new Dimension(0, 220));

            tokenTable.setRowHeight(26);
            tokenTable.setBackground(new Color(0x12, 0x26, 0x3f));
            tokenTable.setForeground(new Color(0xe2, 0xe8, 0xf0));
            tokenTable.setShowHorizontalLines(false);
            tokenTable.setShowVerticalLines(false);
            tokenTable.getTableHeader().setReorderingAllowed(false);
            tokenTable.getTableHeader().setBackground(new Color(0x15, 0x2c, 0x46));
            tokenTable.getTableHeader().setForeground(Color.BLACK);
            tokenTable.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
            tokenTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            JScrollPane tokenScroll = new JScrollPane(tokenTable);
            tokenScroll.setBorder(BorderFactory.createEmptyBorder());
            tokenScroll.getViewport().setBackground(new Color(0x12, 0x26, 0x3f));
            tokenScroll.setPreferredSize(new Dimension(0, 200));

            automataView.setOpaque(false);
            JScrollPane automataScroll = new JScrollPane(automataView);
            automataScroll.setBorder(BorderFactory.createEmptyBorder());
            automataScroll.getViewport().setBackground(new Color(0x12, 0x26, 0x3f));
            automataTabContainer.setOpaque(false);
            automataTabContainer.add(automataPlaceholder, "placeholder");
            automataTabContainer.add(automataScroll, "data");

            JTextArea regexDefArea = createDefinitionArea(buildRegexDefinitionText());
            JTextArea tokensDefArea = createDefinitionArea(buildTokenDefinitionText());
            JPanel definitionPanel = new JPanel(new GridLayout(1, 2, 12, 0));
            definitionPanel.setOpaque(false);
            definitionPanel.add(regexDefArea);
            definitionPanel.add(tokensDefArea);

            JTabbedPane tabs = new JTabbedPane();
            tabs.setBackground(BACKGROUND);
            tabs.setForeground(new Color(0xe2, 0xe8, 0xf0));
            tabs.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
            tabs.setMaximumSize(new Dimension(Integer.MAX_VALUE, 420));
            tabs.setPreferredSize(new Dimension(0, 280));
            tabs.addTab("Tabla comparativa", wrapTabSection(
                "Coincidencias por contenedor",
                "Observa cuantos patrones y tokens pertenecen a cada categoria.",
                summaryScroll
            ));
            tabs.addTab("Analisis por token", wrapTabSection(
                "Estado de cada token",
                "Cada palabra se marca como aceptada o sin coincidencia dentro del AFD.",
                tokenScroll
            ));
            tabs.addTab("Modo Automatas", wrapTabSection(
                "Traza y visualizacion AFD",
                "Activa el modo para desplegar la tabla y el flujo grafico de estados.",
                automataTabContainer
            ));
            tabs.addTab("Diccionarios", wrapTabSection(
                "Referencias de patrones y vocabularios",
                "Consulta la configuracion actual de regex y vocabulario AFD.",
                definitionPanel
            ));
            return tabs;
        }

        private JScrollPane createTokenChipCard() {
            JScrollPane scroll = new JScrollPane(tokenChipsView);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scroll.getViewport().setBackground(new Color(0x12, 0x26, 0x3f));
            scroll.setPreferredSize(new Dimension(0, 140));
            return scroll;
        }

        private JScrollPane createTokensTableCard() {
            JScrollPane scroll = new JScrollPane(summaryTable);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            scroll.getViewport().setBackground(new Color(0x12, 0x26, 0x3f));
            scroll.setPreferredSize(new Dimension(0, 180));
            return scroll;
        }

        private JPanel wrapCard(String title, JComponent content) {
            JPanel card = new JPanel(new BorderLayout());
            card.setOpaque(false);
            card.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0x1f, 0x2f, 0x46)),
                title,
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI", Font.PLAIN, 12),
                new Color(0x94, 0xa3, 0xb8)
            ));
            card.add(content, BorderLayout.CENTER);
            card.setAlignmentX(LEFT_ALIGNMENT);
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            return card;
        }

        private JComponent wrapTabSection(String heading, String description, JComponent body) {
            JPanel wrapper = new JPanel();
            wrapper.setOpaque(false);
            wrapper.setLayout(new BorderLayout(0, 6));
            JPanel labels = new JPanel();
            labels.setOpaque(false);
            labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
            JLabel title = new JLabel(heading);
            title.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
            title.setForeground(new Color(0xe2, 0xe8, 0xf0));
            labels.add(title);
            if (description != null && !description.isBlank()) {
                JLabel subtitle = new JLabel(description);
                subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                subtitle.setForeground(new Color(0x94, 0xa3, 0xb8));
                labels.add(subtitle);
            }
            wrapper.add(labels, BorderLayout.NORTH);
            wrapper.add(body, BorderLayout.CENTER);
            return wrapper;
        }

        private JPanel createAutomataPlaceholder() {
            JPanel panel = new JPanel();
            panel.setOpaque(false);
            panel.setLayout(new BorderLayout());
            JLabel message = new JLabel("Activa el toggle para ver la traza del AFD.");
            message.setForeground(new Color(0xe2, 0xe8, 0xf0));
            message.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(message, BorderLayout.CENTER);
            return panel;
        }

        private void analyzeManual() {
            String text = inputArea.getText() == null ? "" : inputArea.getText().trim();
            if (text.isEmpty()) {
                return;
            }
            Trace trace = ClassificationEngines.trace(text);
            updateFromTrace("Analisis manual", trace, null);
        }

        void previewTrace(String source, Trace trace) {
            updateFromTrace(source, trace, null);
        }

        void showResult(String source, CheckResult result, Trace trace) {
            updateFromTrace(source + " (resultado)", trace, result);
        }

        void clearTrace() {
            updateFromTrace("Sin datos recientes", null, null);
        }

        private void updateFromTrace(String source, Trace trace, CheckResult result) {
            sourceLabel.setText("Origen: " + source);
            latestTrace = trace;
            latestAutomata = trace == null ? null : trace.automataAnalysis();
            if (trace == null || trace.result() == null) {
                classificationLabel.setText("Clasificacion: sin datos");
                engineLabel.setText("Motor: -");
                normalizedLabel.setText("Normalizado: -");
                unmatchedLabel.setText("Tokens sin clasificar: -");
                summaryModel.setTrace(null);
                summaryModel.setHighlight(null);
                updateBadges(null);
                updateTokenBreakdown();
                applyAutomataData();
                return;
            }
            classificationLabel.setText("Clasificacion: " + (trace.result().category() == null ? "Desconocido" : trace.result().category().display()));
            String engine = trace.result().engine() == null ? "Indefinido" : trace.result().engine();
            engineLabel.setText("Motor: " + engine);
            normalizedLabel.setText("Normalizado: " + trace.normalizedText());
            unmatchedLabel.setText("Tokens sin clasificar: " + (trace.unmatchedTokens().isEmpty()
                ? "ninguno"
                : String.join(", ", trace.unmatchedTokens())));
            summaryModel.setTrace(trace);
            summaryModel.setHighlight(trace.result().category());
            if (result != null) {
                sourceLabel.setText(source + " ? " + (result.correct() ? "Correcto" : "Incorrecto"));
            }
            updateBadges(trace);
            updateTokenBreakdown();
            applyAutomataData();
        }

        private void updateAutomataVisibility() {
            applyAutomataData();
        }

        private void applyAutomataData() {
            CardLayout layout = (CardLayout) automataTabContainer.getLayout();
            if (!automataToggle.isSelected()) {
                automataView.clear();
                layout.show(automataTabContainer, "placeholder");
                return;
            }
            layout.show(automataTabContainer, "data");
            automataView.update(latestTrace, latestAutomata);
        }

        private void updateBadges(Trace trace) {
            if (trace == null) {
                regexBadge.setValue("Patrones 0");
                regexBadge.emphasize(false);
                automataBadge.setValue("Automata 0");
                automataBadge.emphasize(false);
                tokensBadge.setValue("Tokens 0");
                tokensBadge.emphasize(false);
                return;
            }
            int regexMatches = trace.regexMatches().values().stream().mapToInt(List::size).sum();
            int automataMatches = trace.automataMatches().values().stream().mapToInt(List::size).sum();
            int tokens = trace.tokens().size();
            regexBadge.setValue("Patrones " + regexMatches);
            regexBadge.emphasize(regexMatches > 0);
            automataBadge.setValue("Automata " + automataMatches);
            automataBadge.emphasize(automataMatches > 0);
            tokensBadge.setValue("Tokens " + tokens);
            tokensBadge.emphasize(tokens > 0);
        }

        private void updateTokenBreakdown() {
            if (latestAutomata == null) {
                tokenModel.setRows(List.of());
                return;
            }
            List<String> baseTokens = latestTrace != null ? latestTrace.tokens() : latestAutomata.tokens();
            Map<String, List<String>> tokenCategories = new LinkedHashMap<>();
            for (String token : baseTokens) {
                tokenCategories.putIfAbsent(token, new ArrayList<>());
            }
            latestAutomata.unrecognizedTokens().forEach(token -> tokenCategories.putIfAbsent(token, new ArrayList<>()));
            latestAutomata.acceptedTokens().forEach((category, tokens) -> {
                for (String token : tokens) {
                    tokenCategories.computeIfAbsent(token, key -> new ArrayList<>()).add(category.display());
                }
            });
            List<TokenBreakdownRow> rows = new ArrayList<>();
            List<TokenChipData> chips = new ArrayList<>();
            for (Map.Entry<String, List<String>> entry : tokenCategories.entrySet()) {
                List<String> categories = entry.getValue();
                if (categories == null) {
                    categories = List.of();
                }
                boolean recognized = categories != null && !categories.isEmpty();
                String joined = recognized ? String.join(", ", categories) : "Sin coincidencias";
                rows.add(new TokenBreakdownRow(entry.getKey(), joined, recognized ? "Aceptado" : "No reconocido"));
                chips.add(new TokenChipData(entry.getKey(), recognized ? List.copyOf(categories) : List.of(), recognized));
            }
            tokenModel.setRows(rows);
            tokenChipsView.setTokens(chips);
        }

        private JTextArea createDefinitionArea(String text) {
            JTextArea area = new JTextArea(text);
            area.setEditable(false);
            area.setOpaque(false);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setFont(new Font("Consolas", Font.PLAIN, 11));
            area.setForeground(new Color(0xb8, 0xca, 0xe0));
            area.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0x1f, 0x2f, 0x46)),
                "Definicion",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI", Font.PLAIN, 12),
                new Color(0x94, 0xa3, 0xb8)
            ));
            return area;
        }

        private String buildRegexDefinitionText() {
            StringBuilder builder = new StringBuilder("Gramaticas regulares definidas:\n");
            ClassificationEngines.regexDefinitions().forEach((category, pattern) -> {
                builder.append("- ").append(category.display()).append(": ").append(pattern.pattern()).append('\n');
            });
            return builder.toString();
        }

        private String buildTokenDefinitionText() {
            StringBuilder builder = new StringBuilder("Tokens del modo Automatas:\n");
            AutomataEngine.vocabulary().forEach((category, tokens) -> {
                builder.append("- ").append(category.display()).append(": ").append(String.join(", ", tokens)).append('\n');
            });
            return builder.toString();
        }

        private final class AutomataView extends JPanel {
            private final JLabel title = new JLabel("Modo Automatas (AFD)");
            private final JTextArea tokenList = new JTextArea();
            private final AutomataTraceTableModel traceModel = new AutomataTraceTableModel();
            private final JTable traceTable = new JTable(traceModel);
            private final JLabel winnerLabel = new JLabel("Categoria asignada: -");
            private final JLabel agreementLabel = new JLabel("Acuerdo con motor actual: -");
            private final JLabel unknownLabel = new JLabel("Tokens no reconocidos: -");
            private final JTextArea acceptedArea = new JTextArea();
            private final JTextArea metricsArea = new JTextArea();
            private final TokenFlowView tokenFlowView = new TokenFlowView();

            AutomataView() {
                setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0x1f, 0x2f, 0x46)),
                    BorderFactory.createEmptyBorder(12, 12, 12, 12)
                ));
                setOpaque(false);

                title.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
                title.setForeground(new Color(0x7d, 0xf3, 0x8b));
                title.setAlignmentX(LEFT_ALIGNMENT);

                configureReadOnlyArea(tokenList, "Tokens normalizados");
                configureReadOnlyArea(acceptedArea, "Tokens aceptados por categoria");
                configureReadOnlyArea(metricsArea, "Metricas AFD");

                traceTable.setRowHeight(26);
                traceTable.setShowHorizontalLines(false);
                traceTable.setShowVerticalLines(false);
                traceTable.setBackground(new Color(0x0d, 0x23, 0x3b));
                traceTable.setForeground(new Color(0xe2, 0xe8, 0xf0));
                traceTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                traceTable.getTableHeader().setReorderingAllowed(false);
                traceTable.getTableHeader().setBackground(new Color(0x15, 0x2c, 0x46));
                traceTable.getTableHeader().setForeground(Color.BLACK);
                traceTable.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.PLAIN, 11));
                traceTable.getColumnModel().getColumn(4).setPreferredWidth(70);
                JScrollPane traceScroll = new JScrollPane(traceTable);
                traceScroll.setBorder(BorderFactory.createLineBorder(new Color(0x1f, 0x2f, 0x46)));
                traceScroll.getViewport().setBackground(new Color(0x0d, 0x23, 0x3b));

                JPanel flowCard = new JPanel(new BorderLayout());
                flowCard.setOpaque(false);
                flowCard.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(0x1f, 0x2f, 0x46)),
                    "Flujo grafico de tokens",
                    javax.swing.border.TitledBorder.LEFT,
                    javax.swing.border.TitledBorder.TOP,
                    new Font("Segoe UI", Font.PLAIN, 12),
                    new Color(0x94, 0xa3, 0xb8)
                ));
                JLabel legend = new JLabel("Verde = aceptado, Gris = sin coincidencias");
                legend.setForeground(new Color(0xe2, 0xe8, 0xf0));
                legend.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                legend.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 4));
                flowCard.add(legend, BorderLayout.NORTH);
                flowCard.add(tokenFlowView, BorderLayout.CENTER);

                for (JLabel label : List.of(winnerLabel, agreementLabel, unknownLabel)) {
                    label.setFont(BODY_FONT);
                    label.setForeground(new Color(0xe2, 0xe8, 0xf0));
                    label.setAlignmentX(LEFT_ALIGNMENT);
                }

                add(title);
                add(Box.createVerticalStrut(8));
                add(tokenList);
                add(Box.createVerticalStrut(8));
                add(flowCard);
                add(Box.createVerticalStrut(8));
                add(traceScroll);
                add(Box.createVerticalStrut(8));
                add(winnerLabel);
                add(agreementLabel);
                add(unknownLabel);
                add(Box.createVerticalStrut(8));
                add(acceptedArea);
                add(Box.createVerticalStrut(8));
                add(metricsArea);
                clear();
            }

            void update(Trace trace, AutomataAnalysis analysis) {
                if (analysis == null) {
                    clear();
                    return;
                }
                tokenList.setText(String.join(", ", analysis.tokens()));
                traceModel.setData(analysis.winnerTrace());
                winnerLabel.setText(buildWinnerText(analysis));
                agreementLabel.setText(buildAgreementText(trace, analysis));
                unknownLabel.setText(buildUnknownText(analysis));
                acceptedArea.setText(formatAccepted(analysis));
                metricsArea.setText(formatMetrics(analysis.metrics()));
                tokenFlowView.setSegments(buildFlowSegments(analysis));
            }

            void clear() {
                tokenList.setText("sin datos");
                traceModel.setData(List.of());
                winnerLabel.setText("Categoria asignada: -");
                agreementLabel.setText("Acuerdo con motor actual: -");
                unknownLabel.setText("Tokens no reconocidos: -");
                acceptedArea.setText("sin datos");
                metricsArea.setText("sin datos");
                tokenFlowView.setSegments(List.of());
            }

            private void configureReadOnlyArea(JTextArea area, String titleText) {
                area.setEditable(false);
                area.setLineWrap(true);
                area.setWrapStyleWord(true);
                area.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                area.setBackground(new Color(0x0d, 0x23, 0x3b));
                area.setForeground(new Color(0xe2, 0xe8, 0xf0));
                area.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(0x1f, 0x2f, 0x46)),
                    titleText,
                    javax.swing.border.TitledBorder.LEFT,
                    javax.swing.border.TitledBorder.TOP,
                    new Font("Segoe UI", Font.PLAIN, 12),
                    new Color(0x94, 0xa3, 0xb8)
                ));
                area.setAlignmentX(LEFT_ALIGNMENT);
            }

            private String buildWinnerText(AutomataAnalysis analysis) {
                if (analysis == null || analysis.winner() == null) {
                    return "Categoria asignada: indefinida";
                }
                return analysis.tie()
                    ? "Categoria asignada: " + analysis.winner().display() + " (empate)"
                    : "Categoria asignada: " + analysis.winner().display();
            }

            private String buildAgreementText(Trace trace, AutomataAnalysis analysis) {
                if (analysis == null || analysis.winner() == null || trace == null || trace.result() == null || trace.result().category() == null) {
                    return "Acuerdo con motor actual: sin datos";
                }
                boolean match = analysis.winner()
                    .toGameCategory()
                    .map(cat -> cat == trace.result().category())
                    .orElse(false);
                return match ? "Acuerdo con motor actual: 100% (coincide)" : "Acuerdo con motor actual: 0% (difiere)";
            }

            private String buildUnknownText(AutomataAnalysis analysis) {
                if (analysis == null || analysis.unrecognizedTokens().isEmpty()) {
                    return "Tokens no reconocidos: ninguno";
                }
                return "Tokens no reconocidos: " + String.join(", ", analysis.unrecognizedTokens());
            }

            private String formatAccepted(AutomataAnalysis analysis) {
                if (analysis == null) {
                    return "sin datos";
                }
                StringBuilder builder = new StringBuilder();
                for (AutomataCategory category : AutomataCategory.values()) {
                    List<String> tokens = analysis.acceptedTokens().getOrDefault(category, List.of());
                    builder.append("- ").append(category.display()).append(": ");
                    builder.append(tokens.isEmpty() ? "sin coincidencias" : String.join(", ", tokens));
                    builder.append('\n');
                }
                return builder.toString();
            }

            private String formatMetrics(Map<AutomataCategory, AutomataEngine.AutomatonMetric> map) {
                if (map == null || map.isEmpty()) {
                    return "Metricas no disponibles";
                }
                StringBuilder builder = new StringBuilder();
                for (AutomataCategory category : AutomataCategory.values()) {
                    var metric = map.get(category);
                    builder.append("- AFD ").append(category.display()).append(": ");
                    if (metric == null) {
                        builder.append("sin datos");
                    } else {
                        builder.append(metric.originalStates())
                            .append("->")
                            .append(metric.minimizedStates())
                            .append(" estados (minimizado)");
                    }
                    builder.append('\n');
                }
                return builder.toString();
            }

            private List<TokenFlowSegment> buildFlowSegments(AutomataAnalysis analysis) {
                if (analysis == null) {
                    return List.of();
                }
                Map<String, Integer> acceptedCounts = new LinkedHashMap<>();
                analysis.acceptedTokens().values().forEach(tokens -> {
                    for (String token : tokens) {
                        acceptedCounts.merge(token, 1, Integer::sum);
                    }
                });
                List<TokenFlowSegment> segments = new ArrayList<>();
                for (String token : analysis.tokens()) {
                    int count = acceptedCounts.getOrDefault(token, 0);
                    boolean recognized = count > 0;
                    if (recognized) {
                        acceptedCounts.put(token, count - 1);
                    }
                    segments.add(new TokenFlowSegment(token, recognized));
                }
                return segments;
            }
        }

        private final class AutomataTraceTableModel extends AbstractTableModel {
            private final String[] columns = {"Token", "Estado", "Simbolo", "Siguiente", "Acepta"};
            private final List<TraceRow> rows = new ArrayList<>();

            @Override
            public int getRowCount() {
                return rows.size();
            }

            @Override
            public int getColumnCount() {
                return columns.length;
            }

            @Override
            public String getColumnName(int column) {
                return columns[column];
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                TraceRow row = rows.get(rowIndex);
                return switch (columnIndex) {
                    case 0 -> row.token();
                    case 1 -> row.from();
                    case 2 -> row.symbol();
                    case 3 -> row.to();
                    case 4 -> row.accepting() ? "si" : "no";
                    default -> "";
                };
            }

            void setData(List<TokenTrace> traces) {
                rows.clear();
                if (traces != null) {
                    for (TokenTrace trace : traces) {
                        if (trace.steps().isEmpty()) {
                            rows.add(new TraceRow(trace.token(), "-", "-", "-", trace.accepted()));
                            continue;
                        }
                        for (TransitionStep step : trace.steps()) {
                            rows.add(new TraceRow(trace.token(), step.fromState(), step.symbol(), step.toState(), step.accepting()));
                        }
                    }
                }
                fireTableDataChanged();
            }
        }

        private record TraceRow(String token, String from, String symbol, String to, boolean accepting) {
        }
    }
    private static final class LanguageSummaryTableModel extends AbstractTableModel {
        private final List<LanguageRow> rows = new ArrayList<>();
        private final String[] columns = {"Contenedor", "Patrones encontrados", "Tokens automata"};
        private Category highlight;

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            LanguageRow row = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> row.category().display();
                case 1 -> row.patterns();
                case 2 -> row.tokens();
                default -> "";
            };
        }

        void setTrace(Trace trace) {
            rows.clear();
            if (trace != null) {
                for (Category category : List.of(Category.ORGANICO, Category.RECICLABLE, Category.PELIGROSO)) {
                    String patternMatches = formatList(trace.regexMatches().getOrDefault(category, List.of()));
                    String tokenMatches = formatList(trace.automataMatches().getOrDefault(category, List.of()));
                    rows.add(new LanguageRow(category, patternMatches, tokenMatches));
                }
            }
            fireTableDataChanged();
        }

        void setHighlight(Category category) {
            this.highlight = category;
            fireTableDataChanged();
        }

        Category highlight() {
            return highlight;
        }

        LanguageRow rowAt(int index) {
            if (index < 0 || index >= rows.size()) {
                return null;
            }
            return rows.get(index);
        }

        private String formatList(List<String> values) {
            if (values == null || values.isEmpty()) {
                return "—";
            }
            return String.join(", ", values);
        }
    }

    private record LanguageRow(Category category, String patterns, String tokens) {
    }

    private static final class TokenBreakdownTableModel extends AbstractTableModel {
        private final String[] columns = {"Token", "Categorias AFD", "Estado"};
        private List<TokenBreakdownRow> rows = new ArrayList<>();

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            TokenBreakdownRow row = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> row.token();
                case 1 -> row.categories();
                case 2 -> row.state();
                default -> "";
            };
        }

        void setRows(List<TokenBreakdownRow> rows) {
            this.rows = rows == null ? new ArrayList<>() : new ArrayList<>(rows);
            fireTableDataChanged();
        }
    }

    private record TokenBreakdownRow(String token, String categories, String state) {
    }

    private static final class TokenChipsView extends JPanel {
        private final JPanel chipPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));

        TokenChipsView() {
            setLayout(new BorderLayout());
            setOpaque(false);
            chipPanel.setOpaque(false);
            add(chipPanel, BorderLayout.CENTER);
        }

        void setTokens(List<TokenChipData> chips) {
            chipPanel.removeAll();
            if (chips == null || chips.isEmpty()) {
                JLabel empty = new JLabel("Sin tokens disponibles");
                empty.setForeground(new Color(0x94, 0xa3, 0xb8));
                chipPanel.add(empty);
            } else {
                for (TokenChipData chip : chips) {
                    chipPanel.add(createChip(chip));
                }
            }
            revalidate();
            repaint();
        }

        private JComponent createChip(TokenChipData chip) {
            JLabel label = new JLabel(chip.token());
            label.setOpaque(true);
            label.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
            if (chip.accepted()) {
                label.setBackground(new Color(0x7d, 0xf3, 0x8b));
                label.setForeground(new Color(0x05, 0x12, 0x1f));
                label.setToolTipText("Aceptado por: " + String.join(", ", chip.categories()));
            } else {
                label.setBackground(new Color(0x3a, 0x4a, 0x62));
                label.setForeground(new Color(0xe2, 0xe8, 0xf0));
                label.setToolTipText("Sin coincidencias en el AFD");
            }
            label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x1f, 0x2f, 0x46), 1, true),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
            ));
            return label;
        }
    }

    private record TokenChipData(String token, List<String> categories, boolean accepted) {
    }

    private static final class TokenFlowView extends JComponent {
        private List<TokenFlowSegment> segments = List.of();

        TokenFlowView() {
            setOpaque(false);
            setPreferredSize(new Dimension(0, 140));
            setMinimumSize(new Dimension(220, 110));
        }

        void setSegments(List<TokenFlowSegment> data) {
            segments = data == null ? List.of() : List.copyOf(data);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int width = getWidth();
            int height = getHeight();
            g2.setColor(new Color(0x12, 0x26, 0x3f));
            g2.fillRoundRect(0, 0, width - 1, height - 1, 18, 18);
            g2.setColor(new Color(0x1f, 0x2f, 0x46));
            g2.drawRoundRect(0, 0, width - 1, height - 1, 18, 18);

            if (segments.isEmpty()) {
                g2.setColor(new Color(0x94, 0xa3, 0xb8));
                g2.drawString("Sin tokens disponibles", 24, height / 2);
                g2.dispose();
                return;
            }

            int timelineY = height / 2;
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(new Color(0x33, 0x4e, 0x68));
            g2.drawLine(30, timelineY, width - 30, timelineY);

            int gap = 14;
            int available = Math.max(60, width - 60);
            int boxWidth = Math.max(60, (available - gap * (segments.size() - 1)) / segments.size());
            int totalWidth = boxWidth * segments.size() + gap * (segments.size() - 1);
            int startX = Math.max(30, (width - totalWidth) / 2);

            Font tokenFont = new Font("Segoe UI", Font.PLAIN, 12);
            g2.setFont(tokenFont);
            for (int i = 0; i < segments.size(); i++) {
                TokenFlowSegment segment = segments.get(i);
                int x = startX + i * (boxWidth + gap);
                Color fill = segment.accepted() ? new Color(0x7d, 0xf3, 0x8b) : new Color(0x94, 0xa3, 0xb8);
                g2.setColor(fill);
                g2.fillRoundRect(x, timelineY - 24, boxWidth, 48, 14, 14);
                g2.setColor(new Color(0x05, 0x12, 0x1f));
                g2.drawRoundRect(x, timelineY - 24, boxWidth, 48, 14, 14);
                g2.setColor(new Color(0x05, 0x12, 0x1f));
                String label = segment.token();
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(label);
                int textX = x + Math.max(6, (boxWidth - textWidth) / 2);
                int textY = timelineY + fm.getAscent() / 2 - 4;
                g2.drawString(label, textX, textY);
            }
            g2.dispose();
        }
    }

    private record TokenFlowSegment(String token, boolean accepted) {
    }

    private static final class LanguageSummaryCellRenderer extends DefaultTableCellRenderer {
        private final LanguageSummaryTableModel model;

        LanguageSummaryCellRenderer(LanguageSummaryTableModel model) {
            this.model = model;
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            int modelRow = table.convertRowIndexToModel(row);
            LanguageRow item = model.rowAt(modelRow);
            Color base = new Color(0x12, 0x26, 0x3f);
            if (item != null && item.category() == model.highlight()) {
                base = new Color(0x1e, 0x3a, 0x27);
            }
            component.setBackground(isSelected ? base.brighter() : base);
            component.setForeground(new Color(0xe2, 0xe8, 0xf0));
            if (component instanceof JLabel label) {
                label.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
            }
            return component;
        }
    }

    private static final class BadgeLabel extends JLabel {
        private static final Font BADGE_FONT = new Font("Segoe UI Semibold", Font.PLAIN, 12);
        private final Color baseColor;
        private final Color highlightColor;

        BadgeLabel(String text, Color baseColor) {
            super(text);
            this.baseColor = baseColor;
            this.highlightColor = TextUtils.lighten(baseColor, 0.25);
            setOpaque(true);
            setBackground(baseColor);
            setForeground(new Color(0x05, 0x12, 0x1f));
            setFont(BADGE_FONT);
            setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        }

        void setValue(String text) {
            setText(text);
        }

        void emphasize(boolean active) {
            setBackground(active ? highlightColor : baseColor);
        }

        void applyZoom(double factor) {
            float size = (float) (BADGE_FONT.getSize2D() * factor);
            setFont(BADGE_FONT.deriveFont(size));
            int padding = (int) Math.max(6, Math.round(12 * factor));
            int vertical = (int) Math.max(3, Math.round(4 * factor));
            setBorder(BorderFactory.createEmptyBorder(vertical, padding, vertical, padding));
        }
    }

    private static final class SummaryDialog extends JDialog {
        SummaryDialog(JFrame owner, Summary summary) {
            super(owner, "Resumen de la ronda", true);
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            JPanel content = new JPanel(new BorderLayout(18, 18));
            content.setBackground(new Color(0x0d, 0x1f, 0x34));
            content.setBorder(BorderFactory.createEmptyBorder(22, 26, 18, 26));

            JLabel heading = new JLabel("Resumen de la ronda");
            heading.setForeground(new Color(0x7d, 0xf3, 0x8b));
            heading.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 18));

            JPanel metrics = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 8));
            metrics.setOpaque(false);
            metrics.add(createMetric("Residuos correctos", summary.correct() + " / " + summary.total()));
            metrics.add(createMetric("Puntaje final", String.valueOf(summary.score())));
            double accuracy = summary.total() == 0 ? 0.0 : (double) summary.correct() / summary.total();
            DecimalFormat df = new DecimalFormat("0%");
            metrics.add(createMetric("Exactitud", df.format(accuracy)));

            JLabel dialogMessage = new JLabel(buildSummaryMessage(summary));
            dialogMessage.setForeground(new Color(0xff, 0xec, 0xc4));
            dialogMessage.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            dialogMessage.setAlignmentX(LEFT_ALIGNMENT);

            JPanel north = new JPanel();
            north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
            north.setOpaque(false);
            north.add(heading);
            north.add(Box.createVerticalStrut(6));
            north.add(metrics);
            north.add(Box.createVerticalStrut(4));
            north.add(dialogMessage);
            content.add(north, BorderLayout.NORTH);

            RecordTableModel model = new RecordTableModel(summary.history());
            JTable table = new JTable(model);
            table.setRowHeight(34);
            table.setFillsViewportHeight(true);
            table.setShowHorizontalLines(false);
            table.setShowVerticalLines(false);
            table.setBackground(new Color(0x12, 0x26, 0x3f));
            table.setForeground(new Color(0xe2, 0xe8, 0xf0));
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            table.getTableHeader().setReorderingAllowed(false);
            table.getTableHeader().setBackground(new Color(0x15, 0x2c, 0x46));
            table.getTableHeader().setForeground(Color.BLACK);
            table.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
            SummaryCellRenderer renderer = new SummaryCellRenderer(model);
            for (int i = 0; i < model.getColumnCount(); i++) {
                table.getColumnModel().getColumn(i).setCellRenderer(renderer);
            }
            table.getColumnModel().getColumn(0).setPreferredWidth(260);
            table.getColumnModel().getColumn(1).setPreferredWidth(160);
            table.getColumnModel().getColumn(2).setPreferredWidth(160);

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.getViewport().setBackground(new Color(0x12, 0x26, 0x3f));
            content.add(scrollPane, BorderLayout.CENTER);

            JButton close = new JButton("Cerrar");
            close.addActionListener(e -> dispose());
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            actions.setOpaque(false);
            actions.add(close);
            content.add(actions, BorderLayout.SOUTH);

            setContentPane(content);
            setSize(780, 500);
        }

        private JComponent createMetric(String title, String value) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setOpaque(false);
            JLabel titleLabel = new JLabel(title);
            titleLabel.setForeground(new Color(0x94, 0xa3, 0xb8));
            titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            JLabel valueLabel = new JLabel(value);
            valueLabel.setForeground(new Color(0xe2, 0xe8, 0xf0));
            valueLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 16));
            panel.add(titleLabel);
            panel.add(valueLabel);
            return panel;
        }

        private static final class SummaryCellRenderer extends DefaultTableCellRenderer {
            private final RecordTableModel model;

            SummaryCellRenderer(RecordTableModel model) {
                this.model = model;
                setOpaque(true);
            }

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                int modelRow = table.convertRowIndexToModel(row);
                boolean correct = model.isCorrect(modelRow);
                Color base = correct ? new Color(0x1c, 0x36, 0x28) : new Color(0x3b, 0x1d, 0x1d);
                component.setBackground(isSelected ? base.brighter() : base);
                component.setForeground(new Color(0xe2, 0xe8, 0xf0));
                if (component instanceof JLabel label) {
                    label.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
                }
                return component;
            }
        }
    }

    private static final class RecordTableModel extends AbstractTableModel {
        private final List<RoundRecord> history;
        private final String[] columns = {"Residuo", "Tu elección", "Correcta"};

        RecordTableModel(List<RoundRecord> history) {
            this.history = new ArrayList<>(history);
        }

        @Override
        public int getRowCount() {
            return history.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            RoundRecord record = history.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> record.item();
                case 1 -> record.answer() == null ? "-" : record.answer().display();
                case 2 -> record.expected().display();
                default -> "";
            };
        }

        boolean isCorrect(int rowIndex) {
            return history.get(rowIndex).correct();
        }
    }

    private final class ProfessorDashboard extends JPanel {
        private final DatasetTableModel model;
        private final JTable table;
        private final JTextField searchField = new JTextField(18);
        private final TableRowSorter<DatasetTableModel> sorter;
        private final Map<Category, MetricCard> cards = new LinkedHashMap<>();
        private final CategoryBreakdownChart chart = new CategoryBreakdownChart();
        private final JTextField descriptionField = new JTextField(28);
        private final JTextArea reasonArea = createArea();
        private final JTextArea handlingArea = createArea();
        private final JComboBox<Category> categoryBox = new JComboBox<>(new DefaultComboBoxModel<>(new Category[]{
            Category.ORGANICO,
            Category.RECICLABLE,
            Category.PELIGROSO
        }));
        private final JButton saveButton = new JButton("Agregar residuo");
        private final JButton clearButton = new JButton("Limpiar");
        private int editingRow = -1;

        ProfessorDashboard() {
            setLayout(new BorderLayout(20, 16));
            setBackground(BACKGROUND);
            setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

            JLabel heading = new JLabel("Panel docente de separación");
            heading.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 20));
            heading.setForeground(new Color(0x7d, 0xf3, 0x8b));
            JLabel subtitle = new JLabel("Analiza, agrega y edita el catálogo completo de residuos con sus contenedores.");
            subtitle.setForeground(new Color(0x94, 0xa3, 0xb8));
            subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));

            JPanel header = new JPanel();
            header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
            header.setOpaque(false);
            header.add(heading);
            header.add(Box.createVerticalStrut(4));
            header.add(subtitle);
            add(header, BorderLayout.NORTH);

            JPanel insights = new JPanel();
            insights.setLayout(new BoxLayout(insights, BoxLayout.Y_AXIS));
            insights.setOpaque(false);
            JPanel cardRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 12));
            cardRow.setOpaque(false);
            for (Category category : List.of(Category.ORGANICO, Category.RECICLABLE, Category.PELIGROSO)) {
                MetricCard card = new MetricCard(category, colorFor(category));
                cards.put(category, card);
                cardRow.add(card);
            }
            insights.add(cardRow);
            insights.add(Box.createVerticalStrut(12));

            JPanel chartWrapper = new JPanel(new BorderLayout());
            chartWrapper.setOpaque(false);
            chartWrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x15, 0x2c, 0x46)),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
            ));
            chartWrapper.add(chart, BorderLayout.CENTER);
            insights.add(chartWrapper);

            model = new DatasetTableModel(catalog);
            table = new JTable(model);
            table.setRowHeight(36);
            table.setFillsViewportHeight(true);
            table.setBackground(new Color(0x12, 0x26, 0x3f));
            table.setForeground(new Color(0xe2, 0xe8, 0xf0));
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            table.getTableHeader().setReorderingAllowed(false);
            table.getTableHeader().setBackground(new Color(0x15, 0x2c, 0x46));
            table.getTableHeader().setForeground(Color.BLACK);
            table.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
            table.getColumnModel().getColumn(0).setPreferredWidth(220);
            table.getColumnModel().getColumn(2).setPreferredWidth(240);
            table.getSelectionModel().addListSelectionListener(e -> loadSelection());

            sorter = new TableRowSorter<>(model);
            table.setRowSorter(sorter);

            searchField.setMaximumSize(new Dimension(220, 28));
            searchField.setBackground(new Color(0x12, 0x26, 0x3f));
            searchField.setForeground(new Color(0xe2, 0xe8, 0xf0));
            searchField.setCaretColor(new Color(0xe2, 0xe8, 0xf0));
            searchField.setBorder(BorderFactory.createLineBorder(new Color(0x1f, 0x2f, 0x46)));
            searchField.getDocument().addDocumentListener(new DocumentCallback(() -> applyFilter(searchField.getText())));

            JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
            filterRow.setOpaque(false);
            JLabel filterLabel = new JLabel("Buscar residuo o contenedor:");
            filterLabel.setForeground(new Color(0xe2, 0xe8, 0xf0));
            filterRow.add(filterLabel);
            filterRow.add(searchField);

            JPanel tablePanel = new JPanel(new BorderLayout(8, 12));
            tablePanel.setOpaque(false);
            tablePanel.add(filterRow, BorderLayout.NORTH);
            JScrollPane tableScroll = new JScrollPane(table);
            tableScroll.setBorder(BorderFactory.createEmptyBorder());
            tableScroll.getViewport().setBackground(new Color(0x12, 0x26, 0x3f));
            tablePanel.add(tableScroll, BorderLayout.CENTER);
            tablePanel.add(buildFormPanel(), BorderLayout.SOUTH);

            JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, insights, tablePanel);
            verticalSplit.setResizeWeight(0.35);
            verticalSplit.setOpaque(false);
            verticalSplit.setBorder(BorderFactory.createEmptyBorder());
            add(verticalSplit, BorderLayout.CENTER);

            refreshMetrics();
        }

        private JTextArea createArea() {
            JTextArea area = new JTextArea(3, 28);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            area.setBackground(new Color(0x12, 0x26, 0x3f));
            area.setForeground(new Color(0xe2, 0xe8, 0xf0));
            area.setCaretColor(new Color(0xe2, 0xe8, 0xf0));
            return area;
        }

        private JPanel buildFormPanel() {
            JPanel form = new JPanel();
            form.setOpaque(false);
            form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x15, 0x2c, 0x46)),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
            ));
            form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

            styleField(descriptionField);

            form.add(formLabel("Descripción del residuo"));
            form.add(descriptionField);
            form.add(Box.createVerticalStrut(8));

            form.add(formLabel("Categoría / contenedor"));
            categoryBox.setMaximumSize(new Dimension(240, 28));
            categoryBox.setBackground(new Color(0x12, 0x26, 0x3f));
            categoryBox.setForeground(new Color(0xe2, 0xe8, 0xf0));
            form.add(categoryBox);
            form.add(Box.createVerticalStrut(8));

            form.add(formLabel("Motivo educativo"));
            form.add(wrapArea(reasonArea));
            form.add(Box.createVerticalStrut(8));

            form.add(formLabel("Indicaciones de gestión"));
            form.add(wrapArea(handlingArea));
            form.add(Box.createVerticalStrut(10));

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            actions.setOpaque(false);
            saveButton.addActionListener(e -> submitForm());
            clearButton.addActionListener(e -> clearForm());
            actions.add(clearButton);
            actions.add(saveButton);
            form.add(actions);

            return form;
        }

        private JScrollPane wrapArea(JTextArea area) {
            JScrollPane pane = new JScrollPane(area);
            pane.setPreferredSize(new Dimension(400, 80));
            pane.setBorder(BorderFactory.createLineBorder(new Color(0x1f, 0x2f, 0x46)));
            pane.getViewport().setBackground(new Color(0x12, 0x26, 0x3f));
            return pane;
        }

        private JLabel formLabel(String text) {
            JLabel label = new JLabel(text);
            label.setForeground(new Color(0xe2, 0xe8, 0xf0));
            label.setAlignmentX(LEFT_ALIGNMENT);
            return label;
        }

        private void styleField(JTextField field) {
            field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
            field.setBackground(new Color(0x12, 0x26, 0x3f));
            field.setForeground(new Color(0xe2, 0xe8, 0xf0));
            field.setCaretColor(new Color(0xe2, 0xe8, 0xf0));
            field.setBorder(BorderFactory.createLineBorder(new Color(0x1f, 0x2f, 0x46)));
        }

        private void loadSelection() {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) {
                clearForm();
                return;
            }
            int modelRow = table.convertRowIndexToModel(viewRow);
            EcoItem item = catalog.get(modelRow);
            descriptionField.setText(item.description());
            reasonArea.setText(item.reason());
            handlingArea.setText(item.handling());
            categoryBox.setSelectedItem(item.category());
            editingRow = modelRow;
            saveButton.setText("Actualizar residuo");
        }

        private void submitForm() {
            String description = descriptionField.getText() == null ? "" : descriptionField.getText().trim();
            String reason = reasonArea.getText() == null ? "" : reasonArea.getText().trim();
            String handling = handlingArea.getText() == null ? "" : handlingArea.getText().trim();
            Category category = (Category) categoryBox.getSelectedItem();
            if (description.isEmpty() || reason.isEmpty() || handling.isEmpty() || category == null) {
                JOptionPane.showMessageDialog(ProfessorDashboard.this, "Completa todos los campos antes de guardar.", "Datos incompletos", JOptionPane.WARNING_MESSAGE);
                return;
            }
            EcoItem item = new EcoItem(description, category, reason, handling);
            if (editingRow >= 0) {
                catalog.set(editingRow, item);
                model.updateItem(editingRow, item);
                JOptionPane.showMessageDialog(ProfessorDashboard.this, "Residuo actualizado correctamente.", "Actualizado", JOptionPane.INFORMATION_MESSAGE);
            } else {
                catalog.add(item);
                model.addItem(item);
                JOptionPane.showMessageDialog(ProfessorDashboard.this, "Residuo agregado al catálogo.", "Guardado", JOptionPane.INFORMATION_MESSAGE);
            }
            persistCatalogChanges();
            clearForm();
        }

        private void clearForm() {
            descriptionField.setText("");
            reasonArea.setText("");
            handlingArea.setText("");
            categoryBox.setSelectedIndex(0);
            table.clearSelection();
            editingRow = -1;
            saveButton.setText("Agregar residuo");
        }

        private void applyFilter(String text) {
            if (text == null || text.isBlank()) {
                sorter.setRowFilter(null);
                return;
            }
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text.trim())));
        }

        private void refreshMetrics() {
            Map<Category, Integer> counts = new EnumMap<>(Category.class);
            for (EcoItem item : catalog) {
                counts.merge(item.category(), 1, Integer::sum);
            }
            for (Category category : cards.keySet()) {
                cards.get(category).update(counts.getOrDefault(category, 0), catalog.size());
            }
            chart.update(counts, catalog.size());
        }

        void refreshData() {
            model.replaceAll(catalog);
            refreshMetrics();
        }
    }

    private static final class DatasetTableModel extends AbstractTableModel {
        private final List<EcoItem> items;
        private final String[] columns = {"Residuo", "Contenedor", "Motivo educativo", "Indicacion"};

        DatasetTableModel(List<EcoItem> items) {
            this.items = new ArrayList<>(items);
        }

        @Override
        public int getRowCount() {
            return items.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            EcoItem item = items.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> item.description();
                case 1 -> item.category().display();
                case 2 -> item.reason();
                case 3 -> item.handling();
                default -> "";
            };
        }

        void replaceAll(List<EcoItem> newItems) {
            items.clear();
            items.addAll(newItems);
            fireTableDataChanged();
        }

        void addItem(EcoItem item) {
            items.add(item);
            int row = items.size() - 1;
            fireTableRowsInserted(row, row);
        }

        void updateItem(int row, EcoItem item) {
            items.set(row, item);
            fireTableRowsUpdated(row, row);
        }

        EcoItem getItem(int row) {
            return items.get(row);
        }
    }

    private final class MetricCard extends JPanel {
        private final Category category;
        private final Color accent;
        private final JLabel countLabel = new JLabel("0");
        private final JLabel detailLabel = new JLabel("");

        MetricCard(Category category, Color accent) {
            this.category = category;
            this.accent = accent;
            setPreferredSize(new Dimension(180, 110));
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

            JLabel title = new JLabel(category.display());
            title.setForeground(new Color(0xe2, 0xe8, 0xf0));
            title.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));

            countLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
            countLabel.setForeground(accent);

            detailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            detailLabel.setForeground(new Color(0x94, 0xa3, 0xb8));
            detailLabel.setText(CATEGORY_MOTTO.getOrDefault(category, ""));

            add(title, BorderLayout.NORTH);
            add(countLabel, BorderLayout.CENTER);
            add(detailLabel, BorderLayout.SOUTH);
        }

        void update(int count, int total) {
            countLabel.setText(String.valueOf(count));
            double ratio = total == 0 ? 0.0 : (double) count / total;
            DecimalFormat df = new DecimalFormat("0%");
            detailLabel.setText(df.format(ratio) + " del catálogo");
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0x12, 0x26, 0x3f));
            RoundRectangle2D.Float shape = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20);
            g2.fill(shape);
            g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 140));
            g2.setStroke(new BasicStroke(2f));
            g2.draw(shape);
            g2.dispose();
        }
    }

    private final class CategoryBreakdownChart extends JComponent {
        private final Map<Category, Integer> counts = new EnumMap<>(Category.class);
        private int total;

        CategoryBreakdownChart() {
            setPreferredSize(new Dimension(420, 180));
            setOpaque(false);
        }

        void update(Map<Category, Integer> values, int total) {
            counts.clear();
            counts.putAll(values);
            this.total = total;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int barHeight = 28;
            int gap = 16;
            int x = 60;
            int y = 12;
            int usableWidth = getWidth() - x - 20;
            for (Category category : List.of(Category.ORGANICO, Category.RECICLABLE, Category.PELIGROSO)) {
                double ratio = total == 0 ? 0.0 : counts.getOrDefault(category, 0) / (double) total;
                int barWidth = (int) Math.round(usableWidth * ratio);
                g2.setColor(new Color(0x19, 0x2f, 0x4a));
                g2.fillRoundRect(x, y, usableWidth, barHeight, 18, 18);
                g2.setColor(new Color(colorFor(category).getRed(), colorFor(category).getGreen(), colorFor(category).getBlue(), 220));
                g2.fillRoundRect(x, y, Math.max(12, barWidth), barHeight, 18, 18);
                g2.setColor(new Color(0xe2, 0xe8, 0xf0));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2.drawString(category.display(), 0, y + barHeight - 8);
                DecimalFormat df = new DecimalFormat("0%");
                g2.drawString(df.format(ratio), x + usableWidth + 4, y + barHeight - 8);
                y += barHeight + gap;
            }
            g2.dispose();
        }
    }

    private static final class DocumentCallback implements DocumentListener {
        private final Runnable callback;

        DocumentCallback(Runnable callback) {
            this.callback = callback;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            callback.run();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            callback.run();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            callback.run();
        }
    }

    private final class CategoryBadge extends JPanel {
        private static final Font BADGE_TITLE_FONT = new Font("Segoe UI Semibold", Font.PLAIN, 14);
        private static final Font BADGE_SUBTITLE_FONT = new Font("Segoe UI", Font.PLAIN, 11);
        private final Category category;
        private final Color accent;
        private final JLabel titleLabel = new JLabel();
        private final JLabel subtitleLabel = new JLabel();
        private boolean active;
        private boolean success;

        CategoryBadge(Category category) {
            this.category = category;
            this.accent = colorFor(category);
            setOpaque(false);
            setLayout(new BorderLayout(8, 0));
            setPreferredSize(new Dimension(200, 68));
            setMaximumSize(new Dimension(260, 80));

            CategoryIcon icon = new CategoryIcon(category, TextUtils.lighten(accent, 0.3));
            icon.setPreferredSize(new Dimension(48, 48));
            icon.setMinimumSize(new Dimension(48, 48));
            add(icon, BorderLayout.WEST);

            JPanel textPanel = new JPanel();
            textPanel.setOpaque(false);
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            titleLabel.setFont(BADGE_TITLE_FONT);
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setText(category.display());
            subtitleLabel.setFont(BADGE_SUBTITLE_FONT);
            subtitleLabel.setForeground(new Color(0xb8, 0xca, 0xe0));
            subtitleLabel.setText(CATEGORY_MOTTO.getOrDefault(category, ""));
            textPanel.add(titleLabel);
            textPanel.add(subtitleLabel);
            add(textPanel, BorderLayout.CENTER);
        }

        void reset() {
            active = false;
            success = false;
            subtitleLabel.setText(CATEGORY_MOTTO.getOrDefault(category, ""));
            repaint();
        }

        void applyZoom(double factor) {
            titleLabel.setFont(BADGE_TITLE_FONT.deriveFont((float) (BADGE_TITLE_FONT.getSize2D() * factor)));
            subtitleLabel.setFont(BADGE_SUBTITLE_FONT.deriveFont((float) (BADGE_SUBTITLE_FONT.getSize2D() * factor)));
        }

        void showState(Category expected, boolean resultCorrect) {
            active = category == expected;
            success = resultCorrect;
            if (active) {
                subtitleLabel.setText(resultCorrect ? "¡Bien clasificado!" : "Revisa este contenedor");
            } else {
                subtitleLabel.setText(CATEGORY_MOTTO.getOrDefault(category, ""));
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), active ? 140 : 80);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
            if (active) {
                g2.setStroke(new BasicStroke(2f));
                g2.setColor(success ? new Color(0x7d, 0xf3, 0x8b) : new Color(0xfc, 0xa5, 0xa5));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 24, 24);
            }
            g2.dispose();
        }
    }

    private Color colorFor(Category category) {
        return switch (category) {
            case ORGANICO -> new Color(0x2f, 0x8f, 0x4e);
            case RECICLABLE -> new Color(0x1d, 0x6e, 0xa2);
            case PELIGROSO -> new Color(0xc7, 0x3d, 0x49);
            default -> new Color(0x38, 0xbd, 0xf8);
        };
    }

    private final class ItemBackdrop extends JComponent {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint paint = new GradientPaint(
                0, 0, new Color(0x15, 0x2c, 0x46),
                0, getHeight(), new Color(0x06, 0x18, 0x26)
            );
            g2.setPaint(paint);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 32, 32);
            g2.setColor(new Color(255, 255, 255, 25));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(6, 6, getWidth() - 12, getHeight() - 12, 28, 28);
            g2.dispose();
        }
    }

    private static final class TipBox extends JPanel {
        private static final Font TIP_TITLE_FONT = new Font("Segoe UI Semibold", Font.PLAIN, 13);
        private static final Font TIP_BODY_FONT = new Font("Segoe UI", Font.PLAIN, 12);
        private static final Font TIP_MOTTO_FONT = new Font("Segoe UI", Font.PLAIN, 11);
        private final JLabel textLabel = new JLabel();
        private final JLabel titleLabel;
        private final JLabel mottoLabel;
        private final Color borderColor;
        private final Color restColor;
        private final Color highlightColor;

        TipBox(String title, Color color, Category category) {
            this.borderColor = TextUtils.lighten(color, 0.4);
            this.restColor = darken(color, 0.25);
            this.highlightColor = TextUtils.lighten(color, 0.3);
            setOpaque(true);
            setBackground(restColor);
            setLayout(new BorderLayout(16, 0));
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1, true),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
            ));
            setAlignmentX(JComponent.LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 170));

            CategoryIcon icon = new CategoryIcon(category, TextUtils.lighten(color, 0.25));
            icon.setPreferredSize(new Dimension(68, 68));
            icon.setMinimumSize(new Dimension(64, 64));
            add(icon, BorderLayout.WEST);

            JPanel content = new JPanel();
            content.setOpaque(false);
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

            titleLabel = new JLabel(title);
            titleLabel.setFont(TIP_TITLE_FONT);
            titleLabel.setForeground(TextUtils.lighten(color, 0.7));

            mottoLabel = new JLabel(CATEGORY_MOTTO.getOrDefault(category, ""));
            mottoLabel.setFont(TIP_MOTTO_FONT);
            mottoLabel.setForeground(new Color(0xb8, 0xca, 0xe0));

            textLabel.setFont(TIP_BODY_FONT);
            textLabel.setForeground(new Color(0xf3, 0xff, 0xff));
            textLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);

            content.add(titleLabel);
            content.add(Box.createVerticalStrut(4));
            content.add(mottoLabel);
            content.add(Box.createVerticalStrut(8));
            content.add(textLabel);

            add(content, BorderLayout.CENTER);
        }

        void setTip(String html) {
            textLabel.setText(html);
        }

        void highlight(boolean active) {
            setBackground(active ? highlightColor : restColor);
            repaint();
        }

        void applyZoom(double factor) {
            titleLabel.setFont(TIP_TITLE_FONT.deriveFont((float) (TIP_TITLE_FONT.getSize2D() * factor)));
            mottoLabel.setFont(TIP_MOTTO_FONT.deriveFont((float) (TIP_MOTTO_FONT.getSize2D() * factor)));
            textLabel.setFont(TIP_BODY_FONT.deriveFont((float) (TIP_BODY_FONT.getSize2D() * factor)));
            int padding = (int) Math.max(10, Math.round(14 * factor));
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1, true),
                BorderFactory.createEmptyBorder(padding, padding, padding, padding)
            ));
            revalidate();
        }
    }

    private static final class CategoryIcon extends JComponent {
        private final Category category;
        private final Color accent;

        CategoryIcon(Category category, Color accent) {
            this.category = category;
            this.accent = accent;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int size = Math.min(getWidth(), getHeight());
            int padding = size / 6;
            int diameter = size - padding;
            int x = (getWidth() - diameter) / 2;
            int y = (getHeight() - diameter) / 2;

            g2.setColor(new Color(0x12, 0x1f, 0x2e));
            g2.fillOval(x, y, diameter, diameter);
            g2.setColor(accent);
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(x, y, diameter, diameter);

            switch (category) {
                case ORGANICO -> drawLeaf(g2, x, y, diameter);
                case RECICLABLE -> drawRecycle(g2, x, y, diameter);
                case PELIGROSO -> drawHazard(g2, x, y, diameter);
                default -> {
                }
            }
            g2.dispose();
        }

        private void drawLeaf(Graphics2D g2, int x, int y, int diameter) {
            double cx = x + diameter / 2.0;
            double cy = y + diameter / 2.0;
            Path2D leaf = new Path2D.Double();
            leaf.moveTo(cx - diameter * 0.3, cy);
            leaf.curveTo(cx - diameter * 0.05, cy - diameter * 0.45, cx + diameter * 0.35, cy - diameter * 0.1, cx + diameter * 0.15, cy + diameter * 0.35);
            leaf.curveTo(cx - diameter * 0.05, cy + diameter * 0.45, cx - diameter * 0.35, cy + diameter * 0.15, cx - diameter * 0.3, cy);
            leaf.closePath();
            g2.setColor(new Color(0x44, 0xc4, 0x70));
            g2.fill(leaf);
            g2.setColor(new Color(0x9d, 0xf7, 0xc6));
            g2.setStroke(new BasicStroke(2f));
            g2.draw(leaf);

            Path2D stem = new Path2D.Double();
            stem.moveTo(cx, cy);
            stem.curveTo(cx + diameter * 0.08, cy + diameter * 0.08, cx, cy + diameter * 0.35, cx - diameter * 0.1, cy + diameter * 0.45);
            g2.draw(stem);
        }

        private void drawRecycle(Graphics2D g2, int x, int y, int diameter) {
            g2.setColor(new Color(0x4f, 0xc2, 0xff));
            g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            double cx = x + diameter / 2.0;
            double cy = y + diameter / 2.0;
            double r = diameter * 0.32;

            Path2D arrow1 = new Path2D.Double();
            arrow1.moveTo(cx, cy - r);
            arrow1.lineTo(cx + r * 0.8, cy - r * 0.2);
            arrow1.lineTo(cx + r * 0.6, cy - r * 0.4);
            arrow1.moveTo(cx + r * 0.8, cy - r * 0.2);
            arrow1.lineTo(cx + r * 0.7, cy - r * 0.55);
            g2.draw(arrow1);

            Path2D arrow2 = new Path2D.Double();
            arrow2.moveTo(cx + r * 0.5, cy + r * 0.6);
            arrow2.lineTo(cx - r * 0.3, cy + r * 0.8);
            arrow2.lineTo(cx - r * 0.1, cy + r * 0.6);
            arrow2.moveTo(cx - r * 0.3, cy + r * 0.8);
            arrow2.lineTo(cx - r * 0.45, cy + r * 0.55);
            g2.draw(arrow2);

            Path2D arrow3 = new Path2D.Double();
            arrow3.moveTo(cx - r * 0.9, cy - r * 0.1);
            arrow3.lineTo(cx - r * 0.3, cy - r * 0.9);
            arrow3.lineTo(cx - r * 0.35, cy - r * 0.55);
            arrow3.moveTo(cx - r * 0.3, cy - r * 0.9);
            arrow3.lineTo(cx - r * 0.1, cy - r * 0.75);
            g2.draw(arrow3);
        }

        private void drawHazard(Graphics2D g2, int x, int y, int diameter) {
            int inset = (int) (diameter * 0.2);
            int[] xs = {x + diameter / 2, x + diameter - inset, x + inset};
            int[] ys = {y + inset, y + diameter - inset, y + diameter - inset};
            g2.setColor(new Color(0xff, 0x91, 0x7e));
            g2.fillPolygon(xs, ys, 3);
            g2.setColor(new Color(0xf4, 0x43, 0x36));
            g2.setStroke(new BasicStroke(2f));
            g2.drawPolygon(xs, ys, 3);

            g2.setColor(new Color(0x06, 0x18, 0x26));
            int ex = x + diameter / 2;
            int ey = y + diameter / 2;
            g2.setStroke(new BasicStroke(3f));
            g2.drawLine(ex, ey - 10, ex, ey + 6);
            g2.fillOval(ex - 2, ey + 10, 4, 4);
        }
    }

    private static Color darken(Color color, double factor) {
        factor = Math.max(0.0, Math.min(1.0, factor));
        int r = (int) Math.round(color.getRed() * (1.0 - factor));
        int g = (int) Math.round(color.getGreen() * (1.0 - factor));
        int b = (int) Math.round(color.getBlue() * (1.0 - factor));
        return new Color(Math.max(0, r), Math.max(0, g), Math.max(0, b));
    }

    private static String capitalize(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        String[] parts = trimmed.toLowerCase(Locale.ROOT).split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }
        return builder.toString();
    }
}
