// file: juniorhockeysim/src/com/juniorhockeysim/ui/HockeyApp.java
package com.juniorhockeysim.ui;

import com.juniorhockeysim.core.*;
import com.juniorhockeysim.domain.*;
import com.juniorhockeysim.simulation.*;
import javafx.animation.*;
import javafx.application.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.*;
import javafx.util.*;

import java.util.*;
import java.util.stream.*;

/**
 * JavaFX main application — Crimson Dynasty Franchise Mode UI.
 * Dark arena theme with crimson splash + team-color accents in-game.
 */
public class HockeyApp extends Application {

    // ── State ──────────────────────────────────────────────────────────
    private FranchiseMode franchise;
    private String accentColor  = "#00D4FF";
    private String accent2Color = "#66E8FF";

    // ── Layout nodes ──────────────────────────────────────────────────
    private BorderPane root;
    private StackPane  mainContent;
    private VBox       sidePanel;
    private Label      statusDateLabel;
    private Label      statusTeamLabel;
    private Label      statusRecordLabel;
    private VBox       newsTickerBox;
    private Timeline   tickerTimeline;

    // Colors
    static final String BG_DARK      = "#0A0A0F";
    static final String BG_PANEL     = "#12121A";
    static final String BG_CARD      = "#1A1A26";
    static final String BG_HOVER     = "#22223A";
    static final String TEXT_PRIMARY  = "#E8E8F0";
    static final String TEXT_SECONDARY= "#888899";
    static final String TEXT_DIM      = "#444455";
    static final String BORDER_COLOR  = "#2A2A3A";
    static final String GREEN_WIN     = "#22C55E";
    static final String RED_LOSS      = "#EF4444";
    static final String GOLD          = "#FFB800";

    // Splash crimson palette
    private static final String CRIMSON       = "#DC143C";
    private static final String CRIMSON_LIGHT = "#FF4D6D";
    private static final String CRIMSON_DIM   = "#7A0020";
    private static final String SPLASH_BG1    = "#0A0002";
    private static final String SPLASH_BG2    = "#150005";
    private static final String SPLASH_BG3    = "#05000A";

    // ─────────────────────────────────────────────────────────────────
    // Entry Point
    // ─────────────────────────────────────────────────────────────────

    public static void launch(String[] args) {
        Application.launch(HockeyApp.class, args);
    }

    @Override
    public void start(Stage stage) {
        System.setProperty("prism.allowhidpi", "true");
        stage.setTitle("Crimson Dynasty — Franchise Mode");
        try {
            java.io.InputStream iconStream = HockeyApp.class.getResourceAsStream("/logos/biggerCrimDyn.png");
            if (iconStream != null) {
                stage.getIcons().add(new Image(iconStream));
            }
        } catch (Exception ignored) {}
        stage.setMinWidth(1000);
        stage.setMinHeight(700);
        stage.setResizable(true);
        stage.setMaximized(true);
        showSplashScreen(stage);
    }

    // ─────────────────────────────────────────────────────────────────
    // SPLASH / MAIN MENU SCREEN
    // ─────────────────────────────────────────────────────────────────

    private void showSplashScreen(Stage stage) {
        StackPane screen = new StackPane();
        screen.setStyle("-fx-background-color: " + SPLASH_BG1 + ";");

        // Background gradient — binds to screen size so DPI scaling doesn't cut it off
        Rectangle bgRect = new Rectangle();
        bgRect.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web(SPLASH_BG1)),
                new Stop(0.4, Color.web(SPLASH_BG2)),
                new Stop(1, Color.web(SPLASH_BG3))
        ));
        bgRect.widthProperty().bind(screen.widthProperty());
        bgRect.heightProperty().bind(screen.heightProperty());

        // Subtle rink lines
        VBox rinkLines = new VBox();
        for (int i = 0; i < 30; i++) {
            Rectangle line = new Rectangle();
            line.setFill(Color.web("#2E0005", 0.5));
            line.widthProperty().bind(screen.widthProperty());
            line.setHeight(1);
            VBox.setMargin(line, new Insets(36, 0, 0, 0));
            rinkLines.getChildren().add(line);
        }
        rinkLines.setOpacity(0.35);

        // Center content
        VBox center = new VBox(18);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(60));

        // ── Logo ──────────────────────────────────────────────────────
        StackPane logoArea = new StackPane();
        logoArea.setPrefSize(320, 320);
        logoArea.setMaxSize(320, 320);

        boolean logoLoaded = false;
        try {
            java.io.InputStream is = HockeyApp.class.getResourceAsStream("/logos/biggerCrimMask.png");
            if (is != null) {
                Image logoImg = new Image(is);
                if (!logoImg.isError()) {
                    ImageView iv = new ImageView(logoImg);
                    iv.setFitWidth(320);
                    iv.setFitHeight(320);
                    iv.setPreserveRatio(true);
                    iv.setSmooth(true);
                    logoArea.getChildren().add(iv);
                    logoLoaded = true;
                }
            }
        } catch (Exception ignored) {}

        if (!logoLoaded) {
            // Fallback: crimson circle with initials
            Circle outerRing = new Circle(88);
            outerRing.setFill(Color.TRANSPARENT);
            outerRing.setStroke(Color.web(CRIMSON, 0.7));
            outerRing.setStrokeWidth(2.5);
            Circle innerRing = new Circle(70);
            innerRing.setFill(Color.web(CRIMSON, 0.06));
            innerRing.setStroke(Color.web(CRIMSON, 0.3));
            innerRing.setStrokeWidth(1.5);
            Label phLabel = new Label("CD");
            phLabel.setStyle("-fx-font-size: 42px; -fx-font-weight: bold; -fx-text-fill: " + CRIMSON + "; -fx-font-family: 'Courier New';");
            logoArea.getChildren().addAll(outerRing, innerRing, phLabel);
        }

        DropShadow glowEffect = new DropShadow(35, Color.web(CRIMSON, 0.55));
        glowEffect.setSpread(0.1);
        logoArea.setEffect(glowEffect);

        // Pulse animation on logo
        ScaleTransition pulse = new ScaleTransition(Duration.seconds(2.5), logoArea);
        pulse.setFromX(1.0); pulse.setToX(1.05);
        pulse.setFromY(1.0); pulse.setToY(1.05);
        pulse.setAutoReverse(true); pulse.setCycleCount(Timeline.INDEFINITE);
        pulse.play();

        // ── Wordmark ─────────────────────────────────────────────────
        StackPane wordmarkArea = new StackPane();
        wordmarkArea.setMaxWidth(440);
        wordmarkArea.setPrefHeight(80);

        boolean wordmarkLoaded = false;
        try {
            java.io.InputStream wis = HockeyApp.class.getResourceAsStream("/logos/wordmarkCrimDyn.png");
            if (wis != null) {
                Image wordmarkImg = new Image(wis);
                if (!wordmarkImg.isError()) {
                    ImageView wiv = new ImageView(wordmarkImg);
                    wiv.setFitWidth(440);
                    wiv.setFitHeight(80);
                    wiv.setPreserveRatio(true);
                    wiv.setSmooth(true);
                    DropShadow wordmarkGlow = new DropShadow(20, Color.web(CRIMSON, 0.45));
                    wiv.setEffect(wordmarkGlow);
                    wordmarkArea.getChildren().add(wiv);
                    wordmarkLoaded = true;
                }
            }
        } catch (Exception ignored) {}

        if (!wordmarkLoaded) {
            // Fallback text title
            VBox fallbackTitle = new VBox(4);
            fallbackTitle.setAlignment(Pos.CENTER);
            Label line1 = new Label("CRIMSON");
            line1.setStyle("-fx-font-size: 52px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF; -fx-font-family: 'Courier New'; -fx-letter-spacing: 6px;");
            Label line2 = new Label("DYNASTY");
            line2.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + CRIMSON + "; -fx-font-family: 'Courier New'; -fx-letter-spacing: 14px;");
            fallbackTitle.getChildren().addAll(line1, line2);
            wordmarkArea.getChildren().add(fallbackTitle);
        }

        // Animated underline beneath wordmark
        StackPane underlineStack = new StackPane();
        Rectangle underlineBg = new Rectangle(0, 2);
        underlineBg.setFill(Color.web(CRIMSON, 0.2));
        Rectangle underlineFg = new Rectangle(0, 2);
        underlineFg.setFill(Color.web(CRIMSON));
        underlineFg.setEffect(new Glow(0.8));
        underlineStack.getChildren().addAll(underlineBg, underlineFg);
        underlineStack.setMaxWidth(440);
        Timeline grow = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(underlineFg.widthProperty(), 0),
                        new KeyValue(underlineBg.widthProperty(), 0)),
                new KeyFrame(Duration.millis(1200),
                        new KeyValue(underlineFg.widthProperty(), 440),
                        new KeyValue(underlineBg.widthProperty(), 440))
        );
        grow.setDelay(Duration.millis(600));
        grow.play();

        // Beta + version badge
        HBox versionRow = new HBox(12);
        versionRow.setAlignment(Pos.CENTER);
        Label version = new Label("JUNIOR HOCKEY MANAGER");
        version.setStyle("-fx-font-size: 11px; -fx-text-fill: " + CRIMSON_DIM + "; -fx-letter-spacing: 3px;");
        Label betaBadge = new Label("BETA");
        betaBadge.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; -fx-text-fill: #0A0002; -fx-background-color: " + CRIMSON + "; -fx-padding: 2 6; -fx-background-radius: 2px; -fx-letter-spacing: 1px;");
        versionRow.getChildren().addAll(version, betaBadge);

        // ── Menu buttons ──────────────────────────────────────────────
        VBox menuButtons = new VBox(12);
        menuButtons.setAlignment(Pos.CENTER);
        menuButtons.setMaxWidth(340);

        boolean hasSave = SaveManager.saveExists();

        if (hasSave) {
            Button continueBtn = splashButton("CONTINUE FRANCHISE", CRIMSON, true);
            continueBtn.setOnAction(e -> {
                try {
                    FranchiseMode loaded = (FranchiseMode) SaveManager.load();
                    franchise = loaded;
                    accentColor  = TeamColors.getPrimary(franchise.getUserTeam().getName());
                    accent2Color = TeamColors.getSecondary(franchise.getUserTeam().getName());
                    showMainScreen(stage);
                } catch (Exception ex) {
                    showSplashError(stage, "Save file corrupted. Starting new franchise.");
                }
            });
            menuButtons.getChildren().add(continueBtn);
        }

        Button newGameBtn = splashButton(hasSave ? "NEW FRANCHISE" : "START FRANCHISE", CRIMSON, !hasSave);
        newGameBtn.setOnAction(e -> showTeamSelectScreen(stage));
        menuButtons.getChildren().add(newGameBtn);

        Button howToBtn = splashButton("HOW TO PLAY", CRIMSON_DIM, false);
        howToBtn.setOnAction(e -> showHowToPlayDialog(stage));
        menuButtons.getChildren().add(howToBtn);

        center.getChildren().addAll(logoArea, wordmarkArea, underlineStack, versionRow, menuButtons);

        // Copyright
        Label credit = new Label("© 2026 Crimson Dynasty Franchise Mode");
        credit.setStyle("-fx-font-size: 10px; -fx-text-fill: #1A0008;");
        StackPane.setAlignment(credit, Pos.BOTTOM_CENTER);
        StackPane.setMargin(credit, new Insets(0, 0, 16, 0));

        screen.getChildren().addAll(bgRect, rinkLines, center, credit);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(800), screen);
        fadeIn.setFromValue(0); fadeIn.setToValue(1); fadeIn.play();

        Scene scene = new Scene(screen);
        scene.setFill(Color.web(SPLASH_BG1));
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.F11) {
                stage.setFullScreen(!stage.isFullScreen());
            }
        });
    }

    private Button splashButton(String text, String color, boolean primary) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setPrefHeight(50);
        String normal, hover;
        if (primary) {
            normal = String.format(
                    "-fx-background-color: linear-gradient(to bottom, %s, %s); " +
                            "-fx-text-fill: #F0E8E8; -fx-font-size: 14px; -fx-font-weight: bold; " +
                            "-fx-background-radius: 2px; -fx-cursor: hand; -fx-letter-spacing: 4px; " +
                            "-fx-border-color: %s; -fx-border-radius: 2px; -fx-border-width: 1px; " +
                            "-fx-effect: dropshadow(gaussian, %s, 10, 0.3, 0, 2);",
                    color, CRIMSON_DIM, color, color);
            hover = String.format(
                    "-fx-background-color: linear-gradient(to bottom, %s, %s); " +
                            "-fx-text-fill: #FFFFFF; -fx-font-size: 14px; -fx-font-weight: bold; " +
                            "-fx-background-radius: 2px; -fx-cursor: hand; -fx-letter-spacing: 4px; " +
                            "-fx-border-color: %s; -fx-border-radius: 2px; -fx-border-width: 1px; " +
                            "-fx-effect: dropshadow(gaussian, %s, 18, 0.5, 0, 0);",
                    CRIMSON_LIGHT, color, CRIMSON_LIGHT, CRIMSON_LIGHT);
        } else {
            normal = String.format(
                    "-fx-background-color: transparent; -fx-text-fill: %s; -fx-font-size: 13px; " +
                            "-fx-font-weight: bold; -fx-background-radius: 2px; " +
                            "-fx-border-color: %s55; -fx-border-radius: 2px; -fx-border-width: 1px; " +
                            "-fx-cursor: hand; -fx-letter-spacing: 3px;", color, color);
            hover = String.format(
                    "-fx-background-color: %s15; -fx-text-fill: %s; -fx-font-size: 13px; " +
                            "-fx-font-weight: bold; -fx-background-radius: 2px; " +
                            "-fx-border-color: %s; -fx-border-radius: 2px; -fx-border-width: 1px; " +
                            "-fx-cursor: hand; -fx-letter-spacing: 3px;", CRIMSON, CRIMSON_LIGHT, CRIMSON);
        }
        b.setStyle(normal);
        b.setOnMouseEntered(ev -> b.setStyle(hover));
        b.setOnMouseExited(ev -> b.setStyle(normal));
        return b;
    }

    private void showSplashError(Stage stage, String msg) {
        showTeamSelectScreen(stage);
    }

    private void showHowToPlayDialog(Stage stage) {
        Stage d = new Stage();
        d.initModality(Modality.APPLICATION_MODAL);
        d.setTitle("How to Play");
        VBox content = new VBox(14);
        content.setPadding(new Insets(28));
        content.setPrefWidth(480);
        content.setStyle("-fx-background-color: #0A0002;");

        content.getChildren().add(makeLabel("HOW TO PLAY", 16, CRIMSON, true));
        String[] tips = {
                "Pick your team and manage your franchise across multiple seasons.",
                "Sim individual games (NEXT GAME) or bulk-simulate to end of season.",
                "Trade players before the deadline, sign free agents competitively.",
                "After the playoffs, run the offseason: develop players, draft prospects.",
                "The draft is pick-by-pick — watch AI picks then choose your prospect.",
                "History tab tracks your championships and regular season titles.",
                "Save your game anytime. Your save loads automatically on next launch."
        };
        for (String tip : tips) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.TOP_LEFT);
            Label bullet = makeLabel("▶", 11, CRIMSON, false);
            Label text   = makeLabel(tip, 13, "#888899", false);
            text.setWrapText(true);
            row.getChildren().addAll(bullet, text);
            content.getChildren().add(row);
        }
        Button closeBtn = new Button("CLOSE");
        closeBtn.setStyle("-fx-background-color: " + CRIMSON + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 28; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> d.close());
        content.getChildren().add(closeBtn);
        d.setScene(new Scene(content));
        d.show();
    }

    private Label makeLabel(String text, double size, String color, boolean bold) {
        Label l = new Label(text);
        l.setStyle(String.format("-fx-font-size: %.0fpx; -fx-text-fill: %s;%s", size, color, bold ? " -fx-font-weight: bold;" : ""));
        return l;
    }

    // ─────────────────────────────────────────────────────────────────
    // Team Selection Screen
    // ─────────────────────────────────────────────────────────────────

    private void showTeamSelectScreen(Stage stage) {
        VBox screen = new VBox(0);
        screen.setStyle("-fx-background-color: " + BG_DARK + ";");

        // Header
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(40, 40, 24, 40));
        header.setStyle("-fx-background-color: linear-gradient(to bottom, #0D0D1A, " + BG_DARK + ");");

        HBox backRow = new HBox();
        backRow.setAlignment(Pos.CENTER_LEFT);
        backRow.setMaxWidth(1100);
        Button backBtn = ghostButton("← MAIN MENU");
        backBtn.setOnAction(e -> showSplashScreen(stage));
        backRow.getChildren().add(backBtn);

        Label title = label("SELECT YOUR TEAM", 32, TEXT_PRIMARY, "bold");
        title.setStyle(title.getStyle() + " -fx-font-family: 'Courier New'; -fx-letter-spacing: 8px;");
        Label sub = label("Choose your franchise and begin your journey", 14, TEXT_SECONDARY, "normal");

        header.getChildren().addAll(backRow, title, sub);

        // Team grid
        List<Team> teams = buildTeamList();
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);
        grid.setPadding(new Insets(16, 50, 40, 50));
        grid.setAlignment(Pos.CENTER);

        for (int i = 0; i < teams.size(); i++) {
            Team t = teams.get(i);
            String col = TeamColors.getPrimary(t.getName());
            VBox card = buildTeamCard(t, col, i);
            final Team ft = t;
            card.setOnMouseClicked(e -> startNewGame(ft, teams, stage));
            grid.add(card, i % 4, i / 4);
        }

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        screen.getChildren().addAll(header, scroll);

        Scene scene = new Scene(screen);
        stage.setScene(scene);
        stage.setMaximized(true);

        FadeTransition ft2 = new FadeTransition(Duration.millis(400), screen);
        ft2.setFromValue(0); ft2.setToValue(1); ft2.play();
    }

    private VBox buildTeamCard(Team team, String color, int idx) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(18, 14, 18, 14));
        card.setPrefWidth(240);
        String normalStyle =
                "-fx-background-color: " + BG_CARD + ";" +
                        "-fx-border-color: " + BORDER_COLOR + ";" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 6px;" +
                        "-fx-background-radius: 6px;" +
                        "-fx-cursor: hand;";
        card.setStyle(normalStyle);

        StackPane logoPane = buildTeamLogoPane(team.getName(), color, 38);

        Label nameLabel = label(team.getName(), 13, TEXT_PRIMARY, "bold");
        nameLabel.setWrapText(true);
        nameLabel.setTextAlignment(TextAlignment.CENTER);
        nameLabel.setMaxWidth(200);

        HBox ovrRow = new HBox(6);
        ovrRow.setAlignment(Pos.CENTER);
        Label ovrVal = label("OVR " + team.teamOverall(), 14, color, "bold");
        ovrRow.getChildren().add(ovrVal);

        HBox stats = new HBox(16);
        stats.setAlignment(Pos.CENTER);
        stats.getChildren().addAll(
                miniStat("OFF", team.teamOffense(), color),
                miniStat("DEF", team.teamDefense(), color)
        );

        card.getChildren().addAll(logoPane, nameLabel, ovrRow, stats);

        String hoverStyle =
                "-fx-background-color: " + BG_HOVER + ";" +
                        "-fx-border-color: " + color + ";" +
                        "-fx-border-width: 1.5px;" +
                        "-fx-border-radius: 6px;" +
                        "-fx-background-radius: 6px;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, " + color + ", 14, 0.25, 0, 0);";
        card.setOnMouseEntered(e -> card.setStyle(hoverStyle));
        card.setOnMouseExited(e -> card.setStyle(normalStyle));

        card.setOpacity(0);
        card.setTranslateY(16);
        PauseTransition delay = new PauseTransition(Duration.millis(40 * idx));
        delay.setOnFinished(ev -> {
            FadeTransition fade = new FadeTransition(Duration.millis(260), card);
            fade.setFromValue(0); fade.setToValue(1); fade.play();
            TranslateTransition slide = new TranslateTransition(Duration.millis(260), card);
            slide.setFromY(16); slide.setToY(0); slide.play();
        });
        delay.play();

        return card;
    }

    private StackPane buildTeamLogoPane(String teamName, String color, double maxSize) {
        StackPane pane = new StackPane();
        pane.setPrefSize(maxSize, maxSize);
        pane.setMaxSize(maxSize, maxSize);
        pane.setMinSize(maxSize * 0.5, maxSize * 0.5);

        Image img = TeamColors.getLogoImage(teamName);
        if (img != null && !img.isError()) {
            ImageView iv = new ImageView(img);
            iv.setFitWidth(maxSize);
            iv.setFitHeight(maxSize);
            iv.setPreserveRatio(true);
            iv.setSmooth(true);
            pane.getChildren().add(iv);
        } else {
            Circle c = new Circle(maxSize * 0.5);
            c.setFill(Color.web(color, 0.15));
            c.setStroke(Color.web(color, 0.7));
            c.setStrokeWidth(Math.max(1, maxSize * 0.04));
            Label icon = label(TeamColors.getLogo(teamName), maxSize * 0.55, color, "bold");
            pane.getChildren().addAll(c, icon);
        }
        return pane;
    }

    private VBox miniStat(String label, int value, String color) {
        VBox box = new VBox(2);
        box.setAlignment(Pos.CENTER);
        box.getChildren().addAll(
                label(String.valueOf(value), 16, color, "bold"),
                label(label, 10, TEXT_DIM, "normal")
        );
        return box;
    }

    // ─────────────────────────────────────────────────────────────────
    // Main Screen
    // ─────────────────────────────────────────────────────────────────

    private void startNewGame(Team userTeam, List<Team> teams, Stage stage) {
        franchise = new FranchiseMode(teams, userTeam);
        accentColor  = TeamColors.getPrimary(userTeam.getName());
        accent2Color = TeamColors.getSecondary(userTeam.getName());
        showMainScreen(stage);
    }

    private void showMainScreen(Stage stage) {
        root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG_DARK + ";");

        root.setTop(buildTopBar());
        root.setLeft(buildSideNav());

        mainContent = new StackPane();
        mainContent.setStyle("-fx-background-color: " + BG_DARK + ";");
        // Ensure mainContent always fills the available center space
        VBox.setVgrow(mainContent, Priority.ALWAYS);
        HBox.setHgrow(mainContent, Priority.ALWAYS);
        root.setCenter(mainContent);

        root.setRight(buildNewsPanel());

        showDashboard();

        Scene scene = new Scene(root);
        scene.setFill(Color.web(BG_DARK));

        // F11 fullscreen on main scene too
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.F11) {
                stage.setFullScreen(!stage.isFullScreen());
            }
        });

        stage.setScene(scene);
        stage.setTitle("Crimson Dynasty — " + franchise.getUserTeam().getName());
        stage.setMaximized(true);
        stage.show();

        FadeTransition ft = new FadeTransition(Duration.millis(400), root);
        ft.setFromValue(0); ft.setToValue(1); ft.play();

        startNewsTicker();
    }

    // ─────────────────────────────────────────────────────────────────
    // Top Status Bar
    // ─────────────────────────────────────────────────────────────────

    private HBox buildTopBar() {
        HBox bar = new HBox(0);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle(
                "-fx-background-color: " + BG_PANEL + ";" +
                        "-fx-border-color: transparent transparent " + BORDER_COLOR + " transparent;" +
                        "-fx-border-width: 0 0 1 0;"
        );
        bar.setPadding(new Insets(0, 20, 0, 0));
        bar.setMinHeight(48);
        bar.setPrefHeight(54);

        HBox teamSection = new HBox(12);
        teamSection.setAlignment(Pos.CENTER_LEFT);
        teamSection.setPadding(new Insets(0, 30, 0, 16));
        teamSection.setPrefWidth(220);
        teamSection.setStyle("-fx-background-color: " + accentColor + "18;");

        StackPane logoMini = buildTeamLogoPane(franchise.getUserTeam().getName(), accentColor, 18);

        VBox teamInfo = new VBox(1);
        Label teamNameLabel = label(franchise.getUserTeam().getName(), 13, TEXT_PRIMARY, "bold");
        statusRecordLabel = label("0-0  |  0 pts", 11, accentColor, "normal");
        teamInfo.getChildren().addAll(teamNameLabel, statusRecordLabel);

        teamSection.getChildren().addAll(logoMini, teamInfo);

        Region sep1 = new Region();
        sep1.setPrefWidth(1); sep1.setPrefHeight(30);
        sep1.setStyle("-fx-background-color: " + BORDER_COLOR + ";");
        HBox.setMargin(sep1, new Insets(12, 20, 12, 0));

        VBox dateBox = new VBox(2);
        dateBox.setAlignment(Pos.CENTER_LEFT);
        statusDateLabel = label("October 1, 2026", 14, TEXT_PRIMARY, "bold");
        statusDateLabel.setStyle(statusDateLabel.getStyle() + " -fx-font-family: 'Courier New';");
        Label phaseLabel = label("REGULAR SEASON", 10, accentColor, "normal");
        phaseLabel.setStyle(phaseLabel.getStyle() + " -fx-letter-spacing: 3px;");
        dateBox.getChildren().addAll(statusDateLabel, phaseLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox quickActions = new HBox(8);
        quickActions.setAlignment(Pos.CENTER_RIGHT);

        Button btnNextGame = accentButton("NEXT GAME", true);
        btnNextGame.setOnAction(e -> handleSimToNextGame());

        Button btnDay = accentButton("ADV. DAY", false);
        btnDay.setOnAction(e -> handleAdvanceDay());

        Button btnSave = ghostButton("SAVE");
        btnSave.setOnAction(e -> handleSave());

        quickActions.getChildren().addAll(btnNextGame, btnDay, btnSave);

        bar.getChildren().addAll(teamSection, sep1, dateBox, spacer, quickActions);

        updateTopBar();
        return bar;
    }

    private void updateTopBar() {
        if (franchise == null) return;
        Team ut = franchise.getUserTeam();
        statusDateLabel.setText(franchise.getCurrentDate().toString());
        statusRecordLabel.setText(ut.getRecord() + "  |  " + ut.getPoints() + " pts");
    }

    // ─────────────────────────────────────────────────────────────────
    // Side Navigation
    // ─────────────────────────────────────────────────────────────────

    private ScrollPane buildSideNav() {
        sidePanel = new VBox(4);
        sidePanel.setPrefWidth(180);
        sidePanel.setPadding(new Insets(16, 8, 16, 8));
        sidePanel.setStyle("-fx-background-color: " + BG_PANEL + ";");

        sidePanel.getChildren().addAll(
                navSection("GAME"),
                navItem("Dashboard",     () -> showDashboard()),
                navItem("Simulate Day",  () -> handleAdvanceDay()),
                navItem("Next Game",     () -> handleSimToNextGame()),
                navItem("Sim to DL",     () -> handleSimToDeadline()),
                navItem("End of Season", () -> handleSimToEndSeason()),
                navSpacer(),
                navSection("MY TEAM"),
                navItem("Roster",        () -> showRoster(franchise.getUserTeam())),
                navItem("Lines",         () -> showLines(franchise.getUserTeam())),
                navItem("Team Info",     () -> showTeamInfo(franchise.getUserTeam())),
                navSpacer(),
                navSection("LEAGUE"),
                navItem("Standings",     () -> showStandings()),
                navItem("Leaders",       () -> showLeaders()),
                navItem("Schedule",      () -> showSchedule()),
                navItem("Other Teams",   () -> showOtherTeams()),
                navItem("History",       () -> showHistory()),
                navSpacer(),
                navSection("MANAGEMENT"),
                navItem("Trade",         () -> showTradeScreen()),
                navItem("Free Agents",   () -> showFreeAgents()),
                navItem("Playoffs",      () -> showPlayoffs()),
                navItem("Offseason",     () -> showOffseason()),
                navSpacer(),
                navItem("Save & Exit",   () -> { handleSave(); Platform.exit(); }),
                navItem("↺ Restart",     () -> confirmRestartFranchise())
        );

        ScrollPane navScroll = new ScrollPane(sidePanel);
        navScroll.setFitToWidth(true);
        navScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        navScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        navScroll.setStyle(
                "-fx-background: " + BG_PANEL + ";" +
                        "-fx-background-color: " + BG_PANEL + ";" +
                        "-fx-border-color: transparent " + BORDER_COLOR + " transparent transparent;" +
                        "-fx-border-width: 0 1 0 0;"
        );
        navScroll.setPrefWidth(180);
        return navScroll;
    }

    private Label navSection(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_DIM + ";" +
                "-fx-padding: 12 8 4 8; -fx-letter-spacing: 2px;");
        return l;
    }

    private Button navItem(String text, Runnable action) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setAlignment(Pos.CENTER_LEFT);
        b.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: " + TEXT_SECONDARY + ";" +
                        "-fx-font-size: 13px;" +
                        "-fx-padding: 8 12 8 12;" +
                        "-fx-cursor: hand;" +
                        "-fx-background-radius: 4px;"
        );
        b.setOnMouseEntered(e -> b.setStyle(
                "-fx-background-color: " + accentColor + "18;" +
                        "-fx-text-fill: " + accentColor + ";" +
                        "-fx-font-size: 13px;" +
                        "-fx-padding: 8 12 8 12;" +
                        "-fx-cursor: hand;" +
                        "-fx-background-radius: 4px;"
        ));
        b.setOnMouseExited(e -> b.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: " + TEXT_SECONDARY + ";" +
                        "-fx-font-size: 13px;" +
                        "-fx-padding: 8 12 8 12;" +
                        "-fx-cursor: hand;" +
                        "-fx-background-radius: 4px;"
        ));
        b.setOnAction(e -> { animateNavClick(b); action.run(); });
        return b;
    }

    private void animateNavClick(Button b) {
        ScaleTransition st = new ScaleTransition(Duration.millis(80), b);
        st.setFromX(1.0); st.setToX(0.95);
        st.setAutoReverse(true); st.setCycleCount(2); st.play();
    }

    private Region navSpacer() {
        Region r = new Region(); r.setPrefHeight(6); return r;
    }

    // ─────────────────────────────────────────────────────────────────
    // News Panel
    // ─────────────────────────────────────────────────────────────────

    private VBox buildNewsPanel() {
        VBox panel = new VBox(0);
        panel.setPrefWidth(220);
        panel.setStyle("-fx-background-color: " + BG_PANEL + ";" +
                "-fx-border-color: transparent transparent transparent " + BORDER_COLOR + ";" +
                "-fx-border-width: 0 0 0 1;");

        Label header = label("LEAGUE NEWS", 10, TEXT_DIM, "bold");
        header.setStyle("-fx-text-fill: " + TEXT_DIM + "; -fx-font-size: 10px; -fx-font-weight: bold;" +
                "-fx-letter-spacing: 3px; -fx-padding: 14 12 10 12;" +
                "-fx-border-color: transparent transparent " + BORDER_COLOR + " transparent;" +
                "-fx-border-width: 0 0 1 0;");

        newsTickerBox = new VBox(0);
        newsTickerBox.setPadding(new Insets(8, 0, 8, 0));

        ScrollPane sp = new ScrollPane(newsTickerBox);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(sp, Priority.ALWAYS);

        panel.getChildren().addAll(header, sp);

        updateNewsTicker();
        return panel;
    }

    private void updateNewsTicker() {
        newsTickerBox.getChildren().clear();
        List<String> news = franchise.getRecentNews();
        for (String item : news.stream().limit(25).collect(Collectors.toList())) {
            Label l = new Label(item);
            l.setWrapText(true);
            l.setStyle(
                    "-fx-font-size: 11px;" +
                            "-fx-text-fill: " + TEXT_SECONDARY + ";" +
                            "-fx-padding: 6 12 6 12;" +
                            "-fx-border-color: transparent transparent " + BORDER_COLOR + " transparent;" +
                            "-fx-border-width: 0 0 1 0;"
            );
            newsTickerBox.getChildren().add(l);
        }
        if (news.isEmpty()) {
            Label l = label("No news yet.", 11, TEXT_DIM, "normal");
            l.setPadding(new Insets(8, 12, 8, 12));
            newsTickerBox.getChildren().add(l);
        }
    }

    private void startNewsTicker() {
        if (tickerTimeline != null) tickerTimeline.stop();
        tickerTimeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> updateNewsTicker()));
        tickerTimeline.setCycleCount(Timeline.INDEFINITE);
        tickerTimeline.play();
    }

    // ─────────────────────────────────────────────────────────────────
    // DASHBOARD
    // ─────────────────────────────────────────────────────────────────

    private void showDashboard() {
        VBox dashboard = new VBox(16);
        dashboard.setPadding(new Insets(20, 20, 20, 20));
        dashboard.setStyle("-fx-background-color: " + BG_DARK + ";");

        ScrollPane sp = new ScrollPane(dashboard);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        sp.setFitToWidth(true);

        dashboard.getChildren().add(pageHeader("DASHBOARD", franchise.getUserTeam().getName()));

        HBox topRow = new HBox(12);
        topRow.getChildren().addAll(
                buildRecordCard(),
                buildNextGameCard(),
                buildSeasonProgressCard()
        );
        dashboard.getChildren().add(topRow);

        HBox midRow = new HBox(12);
        VBox recentResults = buildRecentResultsCard();
        VBox standingsSnippet = buildStandingsSnippetCard();
        HBox.setHgrow(recentResults, Priority.ALWAYS);
        HBox.setHgrow(standingsSnippet, Priority.ALWAYS);
        midRow.getChildren().addAll(recentResults, standingsSnippet);
        dashboard.getChildren().add(midRow);

        HBox botRow = new HBox(12);
        botRow.getChildren().addAll(buildInjuryCard(), buildTopPerformersCard());
        dashboard.getChildren().add(botRow);

        setContent(sp);
    }

    private VBox buildRecordCard() {
        Team t = franchise.getUserTeam();
        VBox card = card("TEAM RECORD", 220);

        HBox recordRow = new HBox(6);
        recordRow.setAlignment(Pos.CENTER);
        String[] parts = t.getRecord().split("-");
        recordRow.getChildren().add(bigStat(parts.length > 0 ? parts[0] : "0", "W", accentColor));
        recordRow.getChildren().add(bigStat(parts.length > 1 ? parts[1] : "0", "L", RED_LOSS));
        if (parts.length > 2) recordRow.getChildren().add(bigStat(parts[2], "OTL", TEXT_SECONDARY));

        VBox pts = new VBox(2);
        pts.setAlignment(Pos.CENTER);
        Label ptsNum = label(String.valueOf(t.getPoints()), 36, accentColor, "bold");
        Label ptsTxt = label("POINTS", 9, TEXT_DIM, "normal");
        ptsTxt.setStyle(ptsTxt.getStyle() + " -fx-letter-spacing: 2px;");
        pts.getChildren().addAll(ptsNum, ptsTxt);

        String streakTxt = t.getWinStreak() >= 2 ? "W" + t.getWinStreak() + " STREAK"
                : t.getLoseStreak() >= 2 ? "L" + t.getLoseStreak() + " STREAK" : "—";
        String streakCol = t.getWinStreak() >= 2 ? GREEN_WIN : t.getLoseStreak() >= 2 ? RED_LOSS : TEXT_DIM;
        Label streak = label(streakTxt, 12, streakCol, "bold");

        List<Team> standings = franchise.getSortedStandings();
        int rank = standings.indexOf(t) + 1;
        String rankTxt = "#" + rank + (rank <= 8 ? "  •  PLAYOFF SPOT" : "  •  OUT OF PLAYOFFS");
        String rankCol = rank <= 8 ? GREEN_WIN : RED_LOSS;
        Label rankLabel = label(rankTxt, 11, rankCol, "normal");

        card.getChildren().addAll(recordRow, divider(), pts, streak, rankLabel);
        return card;
    }

    private VBox buildNextGameCard() {
        ScheduledGame next = franchise.getNextUserGame();
        VBox card = card("NEXT GAME", 260);

        if (next == null) {
            card.getChildren().add(label("No upcoming games", 13, TEXT_DIM, "normal"));
        } else {
            boolean isHome = next.getHomeTeam().equals(franchise.getUserTeam());
            Team opponent = isHome ? next.getAwayTeam() : next.getHomeTeam();
            String venue = isHome ? "HOME" : "AWAY";
            String venueCol = isHome ? GREEN_WIN : TEXT_SECONDARY;

            Label venueLabel = label(venue, 10, venueCol, "bold");
            venueLabel.setStyle(venueLabel.getStyle() + " -fx-letter-spacing: 4px;");
            Label dateLabel = label(next.getDate().toString(), 12, TEXT_SECONDARY, "normal");

            HBox vsRow = new HBox(16);
            vsRow.setAlignment(Pos.CENTER);
            VBox myTeam = teamMiniDisplay(franchise.getUserTeam().getName(), accentColor);
            Label vs = label("VS", 14, TEXT_DIM, "bold");
            String oppColor = TeamColors.getPrimary(opponent.getName());
            VBox oppTeam = teamMiniDisplay(opponent.getName(), oppColor);
            vsRow.getChildren().addAll(myTeam, vs, oppTeam);

            Label ovrGap = label("OVR: " + franchise.getUserTeam().teamOverall() + " vs " + opponent.teamOverall(),
                    11, TEXT_DIM, "normal");

            card.getChildren().addAll(venueLabel, dateLabel, divider(), vsRow, ovrGap);

            Button simBtn = accentButton("SIM THIS GAME", true);
            simBtn.setMaxWidth(Double.MAX_VALUE);
            simBtn.setOnAction(e -> handleSimToNextGame());
            card.getChildren().add(simBtn);
        }
        return card;
    }

    private VBox teamMiniDisplay(String name, String color) {
        VBox box = new VBox(4);
        box.setAlignment(Pos.CENTER);
        StackPane logo = buildTeamLogoPane(name, color, 22);
        String displayName = name.length() > 10 ? name.substring(0, 10) + "." : name;
        box.getChildren().addAll(logo, label(displayName, 10, TEXT_SECONDARY, "normal"));
        return box;
    }

    private VBox buildSeasonProgressCard() {
        VBox card = card("SEASON STATUS", 200);

        FranchiseMode.SeasonPhase phase = franchise.getPhase();
        String phaseText = switch (phase) {
            case REGULAR_SEASON -> "Regular Season";
            case PLAYOFFS       -> "Playoffs";
            case OFF_SEASON     -> "Off Season";
            default             -> "Pre-Season";
        };
        card.getChildren().add(label(phaseText, 16, accentColor, "bold"));

        if (phase == FranchiseMode.SeasonPhase.REGULAR_SEASON) {
            int remaining = franchise.countRemainingGames();
            int total = 44;
            int played = total - remaining;
            double pct = (double) played / total;

            card.getChildren().add(divider());
            card.getChildren().add(label(played + " / " + total + " games played", 12, TEXT_SECONDARY, "normal"));
            card.getChildren().add(buildProgressBar(pct, accentColor));
            card.getChildren().add(label(remaining + " games remaining", 11, TEXT_DIM, "normal"));
            card.getChildren().add(label("DL: " + franchise.getTradeDeadline(), 11, GOLD, "normal"));
        } else if (phase == FranchiseMode.SeasonPhase.PLAYOFFS) {
            card.getChildren().add(divider());
            PlayoffBracket bracket = franchise.getPlayoffBracket();
            if (bracket != null) {
                card.getChildren().add(label(bracket.getCurrentRoundName(), 13, accentColor, "bold"));
                for (PlayoffSeries s : bracket.getActiveSeries()) {
                    card.getChildren().add(label(s.getStatusLine(), 11, TEXT_SECONDARY, "normal"));
                }
            }
        } else {
            card.getChildren().add(label("Season complete!", 13, GREEN_WIN, "normal"));
        }
        return card;
    }

    private VBox buildRecentResultsCard() {
        VBox card = card("RECENT RESULTS", -1);
        List<ScheduledGame> recent = franchise.getRecentUserGames(5);
        if (recent.isEmpty()) {
            card.getChildren().add(label("No results yet.", 12, TEXT_DIM, "normal"));
        } else {
            for (int i = recent.size() - 1; i >= 0; i--) {
                ScheduledGame g = recent.get(i);
                GameResult r = g.getResult();
                if (r == null) continue;
                boolean userIsHome = g.getHomeTeam().equals(franchise.getUserTeam());
                int userScore  = userIsHome ? r.getHomeScore() : r.getAwayScore();
                int otherScore = userIsHome ? r.getAwayScore() : r.getHomeScore();
                String opponent = userIsHome ? g.getAwayTeam().getName() : g.getHomeTeam().getName();
                boolean won = userScore > otherScore;
                String suffix = r.isShootoutResult() ? " SO" : r.isOvertimeResult() ? " OT" : "";
                String resultTxt = (won ? "W" : "L") + "  " + userScore + "-" + otherScore + suffix;
                String col = won ? GREEN_WIN : RED_LOSS;

                HBox row = new HBox(12);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(5, 8, 5, 8));
                row.setStyle("-fx-border-color: transparent transparent " + BORDER_COLOR + " transparent; -fx-border-width: 0 0 1 0;");
                Label res = label(resultTxt, 13, col, "bold");
                res.setPrefWidth(80);
                Label opp = label(opponent, 12, TEXT_SECONDARY, "normal");
                Label dt  = label(g.getDate().toShortString(), 11, TEXT_DIM, "normal");
                Region sp2 = new Region(); HBox.setHgrow(sp2, Priority.ALWAYS);
                row.getChildren().addAll(res, opp, sp2, dt);
                card.getChildren().add(row);
            }
        }
        return card;
    }

    private VBox buildStandingsSnippetCard() {
        VBox card = card("STANDINGS", -1);
        List<Team> standings = franchise.getSortedStandings();

        for (int i = 0; i < Math.min(standings.size(), 8); i++) {
            Team t = standings.get(i);
            boolean isUser = t.equals(franchise.getUserTeam());
            boolean playoffLine = i == 7;

            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(5, 8, 5, 8));
            String rowBg = isUser ? accentColor + "18" : "transparent";
            row.setStyle("-fx-background-color: " + rowBg + ";" +
                    "-fx-border-color: transparent transparent " + BORDER_COLOR + " transparent;" +
                    "-fx-border-width: 0 0 1 0;");

            Label rankL = label(String.valueOf(i + 1), 12, i < 8 ? accentColor : TEXT_DIM, "bold");
            rankL.setPrefWidth(20);
            Label nameL = label(t.getName(), 12, isUser ? accentColor : TEXT_PRIMARY, isUser ? "bold" : "normal");
            HBox.setHgrow(nameL, Priority.ALWAYS);
            Label recL  = label(t.getRecord(), 11, TEXT_SECONDARY, "normal");
            recL.setPrefWidth(55);
            Label ptsL  = label(String.valueOf(t.getPoints()), 13, isUser ? accentColor : TEXT_PRIMARY, "bold");
            ptsL.setPrefWidth(28);

            row.getChildren().addAll(rankL, nameL, recL, ptsL);
            card.getChildren().add(row);

            if (playoffLine) {
                Region divLine = new Region();
                divLine.setPrefHeight(1);
                divLine.setStyle("-fx-background-color: " + accentColor + "60;");
                VBox.setMargin(divLine, new Insets(2, 8, 2, 8));
                card.getChildren().add(divLine);
            }
        }

        Button fullStandings = ghostButton("Full Standings");
        fullStandings.setOnAction(e -> showStandings());
        VBox.setMargin(fullStandings, new Insets(8, 0, 0, 0));
        card.getChildren().add(fullStandings);
        return card;
    }

    private VBox buildInjuryCard() {
        VBox card = card("INJURY REPORT", 300);
        List<Player> injured = franchise.getUserTeam().getInjuredPlayers();
        if (injured.isEmpty()) {
            card.getChildren().add(label("All players healthy", 13, GREEN_WIN, "normal"));
        } else {
            for (Player p : injured) {
                HBox row = new HBox(8);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(4, 0, 4, 0));
                Label nameL = label(p.getName(), 12, TEXT_PRIMARY, "normal");
                HBox.setHgrow(nameL, Priority.ALWAYS);
                Label daysL = label(p.getInjuryDays() + "d", 12, RED_LOSS, "bold");
                Label descL = label(p.getInjuryDescription(), 10, TEXT_DIM, "normal");
                VBox info = new VBox(2);
                info.getChildren().addAll(nameL, descL);
                HBox.setHgrow(info, Priority.ALWAYS);
                row.getChildren().addAll(info, daysL);
                card.getChildren().add(row);
            }
        }
        return card;
    }

    private VBox buildTopPerformersCard() {
        VBox card = card("TOP PERFORMERS", -1);
        List<Player> pts = franchise.getLeaguePointLeaders(5);
        card.getChildren().add(label("POINTS LEADERS", 10, TEXT_DIM, "bold"));
        for (Player p : pts) {
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(4, 0, 4, 0));
            Label nameL = label(p.getName(), 12, TEXT_PRIMARY, "normal");
            Label teamL = label(franchise.getTeamOf(p), 10, TEXT_DIM, "normal");
            VBox info = new VBox(1);
            info.getChildren().addAll(nameL, teamL);
            HBox.setHgrow(info, Priority.ALWAYS);
            Label pts2 = label(p.getPoints() + "pts", 13, accentColor, "bold");
            row.getChildren().addAll(info, pts2);
            card.getChildren().add(row);
        }
        return card;
    }

    // ─────────────────────────────────────────────────────────────────
    // STANDINGS SCREEN
    // ─────────────────────────────────────────────────────────────────

    private void showStandings() {
        VBox page = new VBox(16);
        page.setPadding(new Insets(20));
        page.setStyle("-fx-background-color: " + BG_DARK + ";");
        page.getChildren().add(pageHeader("STANDINGS", franchise.getSeasonYear() + "-" + (franchise.getSeasonYear()+1) + " Season"));

        VBox tableCard = new VBox(0);
        tableCard.setStyle("-fx-background-color: " + BG_CARD + ";" +
                "-fx-border-color: " + BORDER_COLOR + ";" +
                "-fx-border-radius: 4px; -fx-background-radius: 4px;");

        HBox header = tableRow(true, false);
        header.getChildren().addAll(
                tableCell("RK",    30,  TEXT_DIM,  true),
                tableCell("TEAM",  240, TEXT_DIM,  true),
                tableCell("GP",    40,  TEXT_DIM,  true),
                tableCell("W",     40,  TEXT_DIM,  true),
                tableCell("L",     40,  TEXT_DIM,  true),
                tableCell("OTL",   40,  TEXT_DIM,  true),
                tableCell("PTS",   50,  TEXT_DIM,  true),
                tableCell("OVR",   50,  TEXT_DIM,  true),
                tableCell("STREAK",70,  TEXT_DIM,  true)
        );
        tableCard.getChildren().add(header);

        List<Team> standings = franchise.getSortedStandings();
        for (int i = 0; i < standings.size(); i++) {
            Team t = standings.get(i);
            boolean isUser = t.equals(franchise.getUserTeam());
            boolean isPlayoffCutoff = i == 7;

            if (isPlayoffCutoff) {
                Region div = new Region(); div.setPrefHeight(1);
                div.setStyle("-fx-background-color: " + accentColor + "50;");
                tableCard.getChildren().add(div);
            }

            HBox row = tableRow(false, isUser);
            String rankCol  = i < 8 ? accentColor : TEXT_DIM;
            String nameCol  = isUser ? accentColor : TEXT_PRIMARY;
            String streakCol = t.getWinStreak() >= 2 ? GREEN_WIN : t.getLoseStreak() >= 2 ? RED_LOSS : TEXT_DIM;
            String streakTxt = t.getWinStreak() >= 2 ? "W" + t.getWinStreak() :
                    t.getLoseStreak() >= 2 ? "L" + t.getLoseStreak() : "—";

            HBox teamCell = new HBox(8);
            teamCell.setAlignment(Pos.CENTER_LEFT);
            teamCell.setPrefWidth(240);
            String teamColor = TeamColors.getPrimary(t.getName());
            StackPane miniLogo = buildTeamLogoPane(t.getName(), teamColor, 13);
            Label nameL = label(t.getName(), 13, nameCol, isUser ? "bold" : "normal");
            teamCell.getChildren().addAll(miniLogo, nameL);

            row.getChildren().addAll(
                    tableCell(String.valueOf(i + 1), 30, rankCol, true),
                    teamCell,
                    tableCell(String.valueOf(t.getGamesPlayed()), 40, TEXT_SECONDARY, false),
                    tableCell(String.valueOf(t.getWins()),        40, TEXT_PRIMARY,   true),
                    tableCell(String.valueOf(t.getLosses()),      40, TEXT_SECONDARY, false),
                    tableCell(String.valueOf(t.getOTLosses()),    40, TEXT_SECONDARY, false),
                    tableCell(String.valueOf(t.getPoints()),      50, isUser ? accentColor : TEXT_PRIMARY, true),
                    tableCell(String.valueOf(t.teamOverall()),    50, TEXT_SECONDARY, false),
                    tableCell(streakTxt,                          70, streakCol,      false)
            );
            tableCard.getChildren().add(row);
        }

        Label legend = label("— — — Playoff cutline (top 8)", 11, accentColor + "90", "normal");
        legend.setPadding(new Insets(8, 0, 0, 0));

        page.getChildren().addAll(tableCard, legend);

        ScrollPane sp = new ScrollPane(page);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        sp.setFitToWidth(true);
        setContent(sp);
    }

    // ─────────────────────────────────────────────────────────────────
    // ROSTER SCREEN
    // ─────────────────────────────────────────────────────────────────

    private void showRoster(Team team) {
        VBox page = new VBox(16);
        page.setPadding(new Insets(20));
        page.setStyle("-fx-background-color: " + BG_DARK + ";");

        boolean isUser = team.equals(franchise.getUserTeam());
        String col = TeamColors.getPrimary(team.getName());
        page.getChildren().add(pageHeader(team.getName().toUpperCase() + " — ROSTER",
                team.getRecord() + "  |  " + team.getPoints() + " pts"));

        // Roster warning
        int fwdCount = team.getForwards().size();
        int defCount = team.getDefenders().size();
        int goaCount = team.getGoalies().size();
        List<String> warnings = new ArrayList<>();
        if (fwdCount < 9) warnings.add("⚠ Only " + fwdCount + " forwards (need 9 minimum for 3 full lines)");
        if (defCount < 4) warnings.add("⚠ Only " + defCount + " defensemen (need 4 minimum for 2 full pairs)");
        if (goaCount < 1) warnings.add("⚠ NO GOALIE! Sign or draft a goalie immediately!");
        else if (goaCount < 2) warnings.add("⚠ Only 1 goalie — consider signing a backup");

        if (!warnings.isEmpty()) {
            VBox warnBox = new VBox(4);
            warnBox.setPadding(new Insets(10, 14, 10, 14));
            warnBox.setStyle("-fx-background-color: " + RED_LOSS + "22; -fx-border-color: " + RED_LOSS + "; -fx-border-radius: 4px; -fx-background-radius: 4px;");
            warnBox.getChildren().add(label("ROSTER ALERT", 11, RED_LOSS, "bold"));
            for (String w : warnings) warnBox.getChildren().add(label(w, 12, GOLD, "normal"));
            if (isUser) warnBox.getChildren().add(label("→ Use Offseason → Free Agents or Entry Draft to fill roster", 11, TEXT_DIM, "normal"));
            page.getChildren().add(warnBox);
        }

        page.getChildren().add(rosterSection("FORWARDS", team.getForwards(), col));
        page.getChildren().add(rosterSection("DEFENSE", team.getDefenders(), col));
        page.getChildren().add(goalieSection("GOALIES", team.getGoalies(), col));

        if (isUser) {
            Button releaseBtn = ghostButton("Release a Player");
            releaseBtn.setOnAction(e -> showReleaseDialog(team));
            page.getChildren().add(releaseBtn);
        }

        ScrollPane sp = new ScrollPane(page);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        sp.setFitToWidth(true);
        setContent(sp);
    }

    private VBox rosterSection(String title, List<Player> players, String color) {
        VBox section = new VBox(0);
        Label hdr = label(title, 11, TEXT_DIM, "bold");
        hdr.setStyle(hdr.getStyle() + " -fx-letter-spacing: 3px; -fx-padding: 12 8 8 8;");
        section.getChildren().add(hdr);

        VBox table = new VBox(0);
        table.setStyle("-fx-background-color: " + BG_CARD + ";" +
                "-fx-border-color: " + BORDER_COLOR + ";" +
                "-fx-border-radius: 4px; -fx-background-radius: 4px;");

        HBox hRow = tableRow(true, false);
        hRow.getChildren().addAll(
                tableCell("NAME",   200, TEXT_DIM, true),
                tableCell("AGE",    40,  TEXT_DIM, false),
                tableCell("OVR",    45,  TEXT_DIM, false),
                tableCell("POT",    45,  TEXT_DIM, false),
                tableCell("YRS",    45,  TEXT_DIM, false),
                tableCell("SHT",    45,  TEXT_DIM, false),
                tableCell("PAS",    45,  TEXT_DIM, false),
                tableCell("SKT",    45,  TEXT_DIM, false),
                tableCell("DEF",    45,  TEXT_DIM, false),
                tableCell("G",      35,  TEXT_DIM, false),
                tableCell("A",      35,  TEXT_DIM, false),
                tableCell("PTS",    40,  TEXT_DIM, false),
                tableCell("MOR",    60,  TEXT_DIM, false),
                tableCell("",       80,  TEXT_DIM, false)
        );
        table.getChildren().add(hRow);

        for (Player p : players.stream().sorted(Comparator.comparingInt(Player::overallRating).reversed()).collect(Collectors.toList())) {
            HBox row = tableRow(false, false);
            boolean injured = p.isInjured();
            int cyr = p.getContractYearsRemaining();
            boolean finalYear = p.isLastYear();
            String nameCol = injured ? RED_LOSS : finalYear ? GOLD : TEXT_PRIMARY;
            String inj = injured ? " [INJ " + p.getInjuryDays() + "d]" : "";
            String finalYearTag = finalYear ? " ⚠" : "";
            int ovr = p.overallRating();
            String ovrCol = ovr >= 75 ? accentColor : ovr >= 65 ? TEXT_PRIMARY : TEXT_SECONDARY;
            String cyrStr = cyr <= 0 ? "UFA" : (p.isLastYear() ? "LAST" : String.valueOf(cyr));
            String cyrCol = cyr <= 0 ? RED_LOSS : p.isLastYear() ? GOLD : TEXT_SECONDARY;

            row.getChildren().addAll(
                    tableCell(p.getName() + inj + finalYearTag, 200, nameCol, false),
                    tableCell(String.valueOf(p.getAge()),       40, TEXT_SECONDARY, false),
                    tableCell(String.valueOf(ovr),              45, ovrCol,         true),
                    tableCell(String.valueOf(p.getPotential()), 45, color + "CC",   false),
                    tableCell(cyrStr,                           45, cyrCol,         true),
                    tableCell(String.valueOf(p.getShooting()),  45, TEXT_SECONDARY, false),
                    tableCell(String.valueOf(p.getPassing()),   45, TEXT_SECONDARY, false),
                    tableCell(String.valueOf(p.getSkating()),   45, TEXT_SECONDARY, false),
                    tableCell(String.valueOf(p.getDefense()),   45, TEXT_SECONDARY, false),
                    tableCell(String.valueOf(p.getGoals()),     35, TEXT_PRIMARY,   true),
                    tableCell(String.valueOf(p.getAssists()),   35, TEXT_SECONDARY, false),
                    tableCell(String.valueOf(p.getPoints()),    40, TEXT_PRIMARY,   true),
                    tableCell(p.getMoraleLabel().substring(0, Math.min(4, p.getMoraleLabel().length())), 60, moralColor(p.getMorale()), false),
                    tableCell("Val:" + p.tradeValue(),          80, TEXT_DIM,       false)
            );
            row.setOnMouseClicked(e -> showPlayerCard(p));
            row.setStyle(row.getStyle() + " -fx-cursor: hand;");
            table.getChildren().add(row);
        }
        section.getChildren().add(table);
        return section;
    }

    private VBox goalieSection(String title, List<Player> goalies, String color) {
        VBox section = new VBox(0);
        Label hdr = label(title, 11, TEXT_DIM, "bold");
        hdr.setStyle(hdr.getStyle() + " -fx-letter-spacing: 3px; -fx-padding: 12 8 8 8;");
        section.getChildren().add(hdr);

        VBox table = new VBox(0);
        table.setStyle("-fx-background-color: " + BG_CARD + ";" +
                "-fx-border-color: " + BORDER_COLOR + ";" +
                "-fx-border-radius: 4px; -fx-background-radius: 4px;");

        HBox hRow = tableRow(true, false);
        hRow.getChildren().addAll(
                tableCell("NAME",  200, TEXT_DIM, true),
                tableCell("AGE",    40, TEXT_DIM, false),
                tableCell("OVR",    45, TEXT_DIM, false),
                tableCell("POT",    45, TEXT_DIM, false),
                tableCell("GP",     40, TEXT_DIM, false),
                tableCell("W",      40, TEXT_DIM, false),
                tableCell("SV%",    70, TEXT_DIM, false),
                tableCell("SO",     40, TEXT_DIM, false)
        );
        table.getChildren().add(hRow);

        for (Player p : goalies) {
            HBox row = tableRow(false, false);
            row.getChildren().addAll(
                    tableCell(p.getName(), 200, TEXT_PRIMARY, false),
                    tableCell(String.valueOf(p.getAge()),  40, TEXT_SECONDARY, false),
                    tableCell(String.valueOf(p.overallRating()), 45, accentColor, true),
                    tableCell(String.valueOf(p.getPotential()), 45, color + "CC", false),
                    tableCell(String.valueOf(p.getGamesPlayed()), 40, TEXT_SECONDARY, false),
                    tableCell(String.valueOf(p.getGoalieWins()), 40, TEXT_PRIMARY, true),
                    tableCell(String.format("%.3f", p.getSavePercentage()), 70, TEXT_PRIMARY, false),
                    tableCell(String.valueOf(p.getShutouts()), 40, TEXT_SECONDARY, false)
            );
            row.setOnMouseClicked(e -> showPlayerCard(p));
            row.setStyle(row.getStyle() + " -fx-cursor: hand;");
            table.getChildren().add(row);
        }
        section.getChildren().add(table);
        return section;
    }

    // ─────────────────────────────────────────────────────────────────
    // LINES SCREEN
    // ─────────────────────────────────────────────────────────────────

    private Player lineEditorSelected = null;

    private void showLines(Team team) {
        lineEditorSelected = null;
        VBox page = new VBox(16);
        page.setPadding(new Insets(20));
        page.setStyle("-fx-background-color: " + BG_DARK + ";");
        String col = TeamColors.getPrimary(team.getName());
        page.getChildren().add(pageHeader("LINE COMBINATIONS", "Click a player to select, then click another to swap"));

        Label tip = label("🖱  SELECT a player → SELECT another to SWAP them  |  Click again to deselect", 11, GOLD, "normal");
        tip.setPadding(new Insets(8, 14, 8, 14));
        tip.setStyle("-fx-background-color: " + GOLD + "18; -fx-border-color: " + GOLD + "40; -fx-border-radius: 3px; -fx-background-radius: 3px;");
        page.getChildren().add(tip);

        buildLineEditorContent(page, team, col);

        ScrollPane sp = new ScrollPane(page);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        sp.setFitToWidth(true);
        setContent(sp);
    }

    private void buildLineEditorContent(VBox page, Team team, String col) {
        while (page.getChildren().size() > 2) page.getChildren().remove(2);

        HBox actions = new HBox(10);
        Button resetBtn = ghostButton("↺ RESET LINES");
        resetBtn.setOnAction(e -> {
            team.setupLines();
            lineEditorSelected = null;
            buildLineEditorContent(page, team, col);
        });
        actions.getChildren().add(resetBtn);
        page.getChildren().add(actions);

        HBox columns = new HBox(16);

        VBox fwdBox = new VBox(10);
        fwdBox.getChildren().add(sectionHeader("FORWARD LINES", col));
        String[] lineLabels = {"LINE 1", "LINE 2", "LINE 3", "LINE 4"};
        List<List<Player>> fwdLines = List.of(team.getLine1(), team.getLine2(), team.getLine3(), team.getLine4());
        for (int i = 0; i < 4; i++) {
            fwdBox.getChildren().add(buildEditableLineCard(lineLabels[i], fwdLines.get(i), col, page, team, 3, i));
        }

        List<Player> allFwd = new ArrayList<>(team.getForwards());
        List<Player> linedUp = new ArrayList<>();
        fwdLines.forEach(linedUp::addAll);
        List<Player> benchFwd = allFwd.stream().filter(p -> !linedUp.contains(p)).collect(Collectors.toList());
        if (!benchFwd.isEmpty()) {
            fwdBox.getChildren().add(buildEditableLineCard("BENCH (FORWARDS)", benchFwd, TEXT_DIM, page, team, 0, -1));
        }
        HBox.setHgrow(fwdBox, Priority.ALWAYS);

        VBox defBox = new VBox(10);
        defBox.getChildren().add(sectionHeader("DEFENSE PAIRS", col));
        String[] pairLabels = {"PAIR 1", "PAIR 2", "PAIR 3"};
        List<List<Player>> pairs = List.of(team.getPair1(), team.getPair2(), team.getPair3());
        for (int i = 0; i < 3; i++) {
            defBox.getChildren().add(buildEditableLineCard(pairLabels[i], pairs.get(i), col, page, team, 2, i + 4));
        }
        List<Player> allDef = new ArrayList<>(team.getDefenders());
        List<Player> pairedUp = new ArrayList<>();
        pairs.forEach(pairedUp::addAll);
        List<Player> benchDef = allDef.stream().filter(p -> !pairedUp.contains(p)).collect(Collectors.toList());
        if (!benchDef.isEmpty()) {
            defBox.getChildren().add(buildEditableLineCard("BENCH (DEFENSE)", benchDef, TEXT_DIM, page, team, 0, -1));
        }

        defBox.getChildren().add(sectionHeader("GOALIES", col));
        List<Player> goalies = new ArrayList<>();
        if (team.getStartingGoalie() != null) goalies.add(team.getStartingGoalie());
        if (team.getBackupGoalie() != null && team.getBackupGoalie() != team.getStartingGoalie()) goalies.add(team.getBackupGoalie());
        defBox.getChildren().add(buildEditableLineCard("STARTERS", goalies, col, page, team, 0, -1));
        HBox.setHgrow(defBox, Priority.ALWAYS);

        columns.getChildren().addAll(fwdBox, defBox);
        page.getChildren().add(columns);
    }

    private VBox buildEditableLineCard(String label, List<Player> players, String color, VBox page, Team team, int maxSize, int lineIdx) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(10, 14, 10, 14));
        card.setStyle("-fx-background-color: " + BG_CARD + ";" +
                "-fx-border-color: " + BORDER_COLOR + ";" +
                "-fx-border-radius: 4px; -fx-background-radius: 4px;");

        HBox headerRow = new HBox(8);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        Label lbl = label(label, 10, TEXT_DIM, "bold");
        lbl.setStyle(lbl.getStyle() + " -fx-letter-spacing: 2px;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        int avgOvr = players.isEmpty() ? 0 : (int) players.stream().mapToInt(Player::overallRating).average().orElse(0);
        Label ovrL = label(avgOvr > 0 ? "AVG " + avgOvr : "", 10, color, "normal");
        headerRow.getChildren().addAll(lbl, sp, ovrL);
        card.getChildren().add(headerRow);

        if (players.isEmpty() && lineIdx < 0) {
            card.getChildren().add(label("Empty", 12, TEXT_DIM, "normal"));
        } else {
            for (Player p : players) {
                card.getChildren().add(buildPlayerSwapRow(p, color, page, team));
            }
        }
        // Show "ADD PLAYER" placeholder when a bench player is selected and line has open space
        if (lineIdx >= 0 && lineEditorSelected != null && players.size() < maxSize) {
            final int finalLineIdx = lineIdx;
            HBox addRow = new HBox(8);
            addRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            addRow.setPadding(new Insets(5, 8, 5, 8));
            addRow.setCursor(javafx.scene.Cursor.HAND);
            addRow.setStyle("-fx-background-color: " + GOLD + "18; -fx-border-color: " + GOLD + "60; -fx-border-width: 1px; -fx-border-radius: 3px; -fx-background-radius: 3px;");
            Label addLbl = label("+ ADD " + lineEditorSelected.getName() + " HERE", 11, GOLD, "bold");
            addRow.getChildren().add(addLbl);
            addRow.setOnMouseClicked(e -> {
                team.addPlayerToLine(lineEditorSelected, finalLineIdx);
                lineEditorSelected = null;
                buildLineEditorContent(page, team, color);
            });
            card.getChildren().add(addRow);
        }
        return card;
    }

    private HBox buildPlayerSwapRow(Player p, String color, VBox page, Team team) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(5, 8, 5, 8));
        row.setCursor(javafx.scene.Cursor.HAND);

        boolean isSelected = (lineEditorSelected == p);
        boolean hasSel     = (lineEditorSelected != null);

        String normalStyle  = "-fx-background-color: transparent; -fx-border-radius: 3px; -fx-background-radius: 3px;";
        String selStyle     = "-fx-background-color: " + color + "33; -fx-border-color: " + color + "; -fx-border-width: 1.5px; -fx-border-radius: 3px; -fx-background-radius: 3px;";
        String swapStyle    = "-fx-background-color: " + GOLD + "15; -fx-border-color: " + GOLD + "80; -fx-border-width: 1px; -fx-border-radius: 3px; -fx-background-radius: 3px;";
        row.setStyle(isSelected ? selStyle : (hasSel ? swapStyle : normalStyle));

        Label nameL = label(p.getName(), 13, isSelected ? color : TEXT_PRIMARY, isSelected ? "bold" : "normal");
        HBox.setHgrow(nameL, Priority.ALWAYS);
        String posCol = p.isInjured() ? RED_LOSS : TEXT_DIM;
        Label posL  = label(p.isInjured() ? "INJ" : p.getPosition().toString(), 10, posCol, "normal");
        Label ovrP  = label(String.valueOf(p.overallRating()), 13, color, "bold");

        if (isSelected) {
            Label selTag = label("◀ SELECTED", 9, color, "bold");
            row.getChildren().addAll(nameL, posL, ovrP, selTag);
        } else if (hasSel) {
            Label swapTag = label("⇄ SWAP?", 9, GOLD, "normal");
            row.getChildren().addAll(nameL, posL, ovrP, swapTag);
        } else {
            row.getChildren().addAll(nameL, posL, ovrP);
        }

        row.setOnMouseEntered(e -> {
            if (lineEditorSelected != p) {
                row.setStyle(row.getStyle() + "-fx-opacity: 0.85;");
            }
        });
        row.setOnMouseExited(e -> row.setStyle(isSelected ? selStyle : (hasSel ? swapStyle : normalStyle)));

        row.setOnMouseClicked(e -> {
            if (lineEditorSelected == null) {
                lineEditorSelected = p;
            } else if (lineEditorSelected == p) {
                lineEditorSelected = null;
            } else {
                team.swapPlayers(lineEditorSelected, p);
                lineEditorSelected = null;
            }
            buildLineEditorContent(page, team, color);
        });

        return row;
    }

    private VBox lineCard(String label, List<Player> players, String color) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(12, 14, 12, 14));
        card.setStyle("-fx-background-color: " + BG_CARD + ";" +
                "-fx-border-color: " + BORDER_COLOR + ";" +
                "-fx-border-radius: 4px; -fx-background-radius: 4px;");

        HBox headerRow = new HBox(8);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        Label lbl = label(label, 10, TEXT_DIM, "bold");
        lbl.setStyle(lbl.getStyle() + " -fx-letter-spacing: 2px;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        int avgOvr = players.isEmpty() ? 0 : (int) players.stream().mapToInt(Player::overallRating).average().orElse(0);
        Label ovrL = label("AVG " + avgOvr, 10, color, "normal");
        headerRow.getChildren().addAll(lbl, sp, ovrL);

        card.getChildren().add(headerRow);
        if (players.isEmpty()) {
            card.getChildren().add(label("Empty", 12, TEXT_DIM, "normal"));
        } else {
            for (Player p : players) {
                HBox pRow = new HBox(8);
                pRow.setAlignment(Pos.CENTER_LEFT);
                Label nameL = label(p.getName(), 13, TEXT_PRIMARY, "normal");
                HBox.setHgrow(nameL, Priority.ALWAYS);
                Label ovrP = label(String.valueOf(p.overallRating()), 13, color, "bold");
                String posCol = p.isInjured() ? RED_LOSS : TEXT_DIM;
                Label posL = label(p.isInjured() ? "INJ" : p.getPosition().toString(), 11, posCol, "normal");
                pRow.getChildren().addAll(nameL, posL, ovrP);
                card.getChildren().add(pRow);
            }
        }
        return card;
    }

    // ─────────────────────────────────────────────────────────────────
    // TEAM INFO SCREEN
    // ─────────────────────────────────────────────────────────────────

    private void showTeamInfo(Team team) {
        VBox page = new VBox(16);
        page.setPadding(new Insets(20));
        page.setStyle("-fx-background-color: " + BG_DARK + ";");
        String col = TeamColors.getPrimary(team.getName());
        page.getChildren().add(pageHeader(team.getName().toUpperCase(), "Team Profile"));

        HBox top = new HBox(16);

        VBox profile = card("PROFILE", 300);
        profile.getChildren().addAll(
                bigStatRow("Record", team.getRecord(), col),
                bigStatRow("Points", String.valueOf(team.getPoints()), col),
                bigStatRow("Overall", String.valueOf(team.teamOverall()), col),
                bigStatRow("Offense", String.valueOf(team.teamOffense()), TEXT_PRIMARY),
                bigStatRow("Defense", String.valueOf(team.teamDefense()), TEXT_PRIMARY)
        );
        List<Team> stgs = franchise.getSortedStandings();
        int rank = stgs.indexOf(team) + 1;
        profile.getChildren().add(bigStatRow("Rank", "#" + rank + (rank <= 8 ? " (PLAYOFF)" : " (OUT)"),
                rank <= 8 ? GREEN_WIN : RED_LOSS));

        VBox injCard = card("INJURY REPORT", -1);
        List<Player> injured = team.getInjuredPlayers();
        if (injured.isEmpty()) {
            injCard.getChildren().add(label("All players healthy", 12, GREEN_WIN, "normal"));
        } else {
            for (Player p : injured) {
                HBox row = new HBox(8);
                row.setAlignment(Pos.CENTER_LEFT);
                Label nameL = label(p.getName(), 12, TEXT_PRIMARY, "normal");
                HBox.setHgrow(nameL, Priority.ALWAYS);
                Label daysL = label(p.getInjuryDays() + " days", 12, RED_LOSS, "bold");
                row.getChildren().addAll(nameL, daysL);
                injCard.getChildren().add(row);
                injCard.getChildren().add(label(p.getInjuryDescription(), 10, TEXT_DIM, "normal"));
            }
        }
        HBox.setHgrow(injCard, Priority.ALWAYS);

        top.getChildren().addAll(profile, injCard);
        page.getChildren().add(top);

        ScrollPane sp = new ScrollPane(page);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        sp.setFitToWidth(true);
        setContent(sp);
    }

    // ─────────────────────────────────────────────────────────────────
    // LEAGUE LEADERS
    // ─────────────────────────────────────────────────────────────────

    private void showLeaders() {
        VBox page = new VBox(16);
        page.setPadding(new Insets(20));
        page.setStyle("-fx-background-color: " + BG_DARK + ";");
        page.getChildren().add(pageHeader("LEAGUE LEADERS", franchise.getSeasonYear() + "-" + (franchise.getSeasonYear()+1)));

        HBox row = new HBox(16);
        row.getChildren().addAll(
                buildLeaderBoard("GOALS", franchise.getLeagueGoalLeaders(10), "goals"),
                buildLeaderBoard("POINTS", franchise.getLeaguePointLeaders(10), "points"),
                buildGoalieLeaderBoard()
        );
        page.getChildren().add(row);

        ScrollPane sp = new ScrollPane(page);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        sp.setFitToWidth(true);
        setContent(sp);
    }

    private VBox buildLeaderBoard(String title, List<Player> players, String stat) {
        VBox card = card(title, -1);
        HBox.setHgrow(card, Priority.ALWAYS);

        HBox hdr = new HBox(8);
        hdr.getChildren().addAll(
                tableCell("RK", 30, TEXT_DIM, true),
                tableCell("PLAYER", 160, TEXT_DIM, true),
                tableCell("TEAM", 120, TEXT_DIM, false),
                tableCell("G", 40, TEXT_DIM, false),
                tableCell("A", 40, TEXT_DIM, false),
                tableCell("PTS", 50, TEXT_DIM, true)
        );
        card.getChildren().add(hdr);
        card.getChildren().add(divider());

        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            boolean userTeam = franchise.getUserTeam().getPlayers().contains(p);
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(5, 0, 5, 0));
            String rankCol = i == 0 ? GOLD : i < 3 ? TEXT_PRIMARY : TEXT_SECONDARY;
            row.getChildren().addAll(
                    tableCell(String.valueOf(i + 1), 30, rankCol, true),
                    tableCell(p.getName(), 160, userTeam ? accentColor : TEXT_PRIMARY, false),
                    tableCell(franchise.getTeamOf(p), 120, TEXT_DIM, false),
                    tableCell(String.valueOf(p.getGoals()), 40, TEXT_SECONDARY, false),
                    tableCell(String.valueOf(p.getAssists()), 40, TEXT_SECONDARY, false),
                    tableCell(String.valueOf(p.getPoints()), 50, i == 0 ? GOLD : accentColor, true)
            );
            card.getChildren().add(row);
        }
        return card;
    }

    private VBox buildGoalieLeaderBoard() {
        VBox card = card("GOALIES (SV%)", -1);
        HBox.setHgrow(card, Priority.ALWAYS);
        List<Player> goalies = franchise.getTopGoalies(8);

        HBox hdr = new HBox(8);
        hdr.getChildren().addAll(
                tableCell("RK",     30,  TEXT_DIM, true),
                tableCell("GOALIE", 160, TEXT_DIM, true),
                tableCell("TEAM",   120, TEXT_DIM, false),
                tableCell("W",       40, TEXT_DIM, false),
                tableCell("SV%",     70, TEXT_DIM, true)
        );
        card.getChildren().add(hdr);
        card.getChildren().add(divider());

        for (int i = 0; i < goalies.size(); i++) {
            Player p = goalies.get(i);
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(5, 0, 5, 0));
            row.getChildren().addAll(
                    tableCell(String.valueOf(i + 1),    30,  i == 0 ? GOLD : TEXT_SECONDARY, true),
                    tableCell(p.getName(),              160, TEXT_PRIMARY, false),
                    tableCell(franchise.getTeamOf(p),  120, TEXT_DIM, false),
                    tableCell(String.valueOf(p.getGoalieWins()), 40, TEXT_SECONDARY, false),
                    tableCell(String.format("%.3f", p.getSavePercentage()), 70, i == 0 ? GOLD : accentColor, true)
            );
            card.getChildren().add(row);
        }
        return card;
    }

    // ─────────────────────────────────────────────────────────────────
    // SCHEDULE SCREEN
    // ─────────────────────────────────────────────────────────────────

    private void showSchedule() {
        VBox page = new VBox(16);
        page.setPadding(new Insets(20));
        page.setStyle("-fx-background-color: " + BG_DARK + ";");
        page.getChildren().add(pageHeader("SCHEDULE", franchise.getUserTeam().getName()));

        VBox table = new VBox(0);
        table.setStyle("-fx-background-color: " + BG_CARD + ";" +
                "-fx-border-color: " + BORDER_COLOR + ";" +
                "-fx-border-radius: 4px; -fx-background-radius: 4px;");

        HBox hdr = tableRow(true, false);
        hdr.getChildren().addAll(
                tableCell("DATE",   130, TEXT_DIM, false),
                tableCell("",        50, TEXT_DIM, false),
                tableCell("OPPONENT",180, TEXT_DIM, false),
                tableCell("RESULT",  80, TEXT_DIM, true)
        );
        table.getChildren().add(hdr);

        List<ScheduledGame> games = franchise.getSchedule().stream()
                .filter(g -> g.involves(franchise.getUserTeam()))
                .collect(Collectors.toList());

        for (ScheduledGame g : games) {
            boolean isHome = g.getHomeTeam().equals(franchise.getUserTeam());
            Team opp = isHome ? g.getAwayTeam() : g.getHomeTeam();
            String oppCol = TeamColors.getPrimary(opp.getName());

            HBox row = tableRow(false, false);
            boolean isNext = !g.isPlayed() && franchise.getNextUserGame() != null &&
                    g.getDate().isSameDate(franchise.getNextUserGame().getDate());
            if (isNext) row.setStyle(row.getStyle() + "-fx-background-color: " + accentColor + "15;");

            String resultTxt = "—";
            String resultCol = TEXT_DIM;
            if (g.isPlayed() && g.getResult() != null) {
                GameResult r = g.getResult();
                int us = isHome ? r.getHomeScore() : r.getAwayScore();
                int them = isHome ? r.getAwayScore() : r.getHomeScore();
                boolean won = us > them;
                String suf = r.isShootoutResult() ? " SO" : r.isOvertimeResult() ? " OT" : "";
                resultTxt = (won ? "W " : "L ") + us + "-" + them + suf;
                resultCol = won ? GREEN_WIN : RED_LOSS;
            }

            HBox oppCell = new HBox(8);
            oppCell.setAlignment(Pos.CENTER_LEFT);
            oppCell.setPrefWidth(180);
            StackPane ml = buildTeamLogoPane(opp.getName(), oppCol, 12);
            oppCell.getChildren().addAll(ml, label(opp.getName(), 12, TEXT_PRIMARY, "normal"));

            row.getChildren().addAll(
                    tableCell(g.getDate().toString(), 130, TEXT_SECONDARY, false),
                    tableCell(isHome ? "vs" : "@",    50,  isHome ? GREEN_WIN : TEXT_DIM, false),
                    oppCell,
                    tableCell(resultTxt, 80, resultCol, true)
            );
            table.getChildren().add(row);
        }

        ScrollPane sp = new ScrollPane(table);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        sp.setFitToWidth(true);
        sp.setPrefViewportHeight(600);

        page.getChildren().add(sp);

        ScrollPane outer = new ScrollPane(page);
        outer.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        outer.setFitToWidth(true);
        setContent(outer);
    }

    // ─────────────────────────────────────────────────────────────────
    // OTHER TEAMS
    // ─────────────────────────────────────────────────────────────────

    private void showOtherTeams() {
        VBox page = new VBox(16);
        page.setPadding(new Insets(20));
        page.setStyle("-fx-background-color: " + BG_DARK + ";");
        page.getChildren().add(pageHeader("TEAMS", "All 12 Teams"));

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(12);

        List<Team> sorted = franchise.getSortedStandings();
        for (int i = 0; i < sorted.size(); i++) {
            Team t = sorted.get(i);
            String col = TeamColors.getPrimary(t.getName());
            VBox card = new VBox(8);
            card.setPadding(new Insets(14, 16, 14, 16));
            card.setPrefWidth(280);

            HBox h = new HBox(10); h.setAlignment(Pos.CENTER_LEFT);
            StackPane lp = buildTeamLogoPane(t.getName(), col, 16);
            VBox info = new VBox(2);
            info.getChildren().addAll(
                    label((i+1) + ". " + t.getName(), 13, TEXT_PRIMARY, "bold"),
                    label(t.getRecord() + " | " + t.getPoints() + " pts", 11, TEXT_SECONDARY, "normal")
            );
            HBox.setHgrow(info, Priority.ALWAYS);
            Label ovrL = label(String.valueOf(t.teamOverall()), 18, col, "bold");
            h.getChildren().addAll(lp, info, ovrL);
            card.getChildren().add(h);

            final Team ft = t;
            final String normalStyle = "-fx-background-color: " + BG_CARD + "; -fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-cursor: hand;";
            final String hoverStyle  = "-fx-background-color: " + BG_HOVER + "; -fx-border-color: " + col + "; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, " + col + ", 8, 0.2, 0, 0);";
            card.setStyle(normalStyle);
            card.setOnMouseClicked(e -> showTeamDetailDialog(ft));
            card.setOnMouseEntered(e -> card.setStyle(hoverStyle));
            card.setOnMouseExited(e -> card.setStyle(normalStyle));

            grid.add(card, i % 3, i / 3);
        }

        ScrollPane sp = new ScrollPane(grid);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        sp.setFitToWidth(true);
        page.getChildren().add(sp);

        ScrollPane outer = new ScrollPane(page);
        outer.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        outer.setFitToWidth(true);
        setContent(outer);
    }

    private void showTeamDetailDialog(Team team) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(team.getName());

        VBox content = new VBox(0);
        content.setStyle("-fx-background-color: " + BG_DARK + ";");
        content.setPrefWidth(700);
        content.setPrefHeight(500);

        TabPane tabs = new TabPane();
        tabs.setStyle("-fx-background-color: " + BG_DARK + ";" +
                "-fx-tab-min-width: 100px;");

        Tab rosterTab = new Tab("Roster");
        rosterTab.setClosable(false);
        rosterTab.setContent(rosterSection("SKATERS", team.getForwards(), TeamColors.getPrimary(team.getName())));

        Tab infoTab = new Tab("Team Info");
        infoTab.setClosable(false);
        VBox infoContent = new VBox(8);
        infoContent.setPadding(new Insets(16));
        infoContent.getChildren().addAll(
                label(team.getName(), 22, TeamColors.getPrimary(team.getName()), "bold"),
                label(team.getRecord() + "  |  " + team.getPoints() + " pts", 14, TEXT_SECONDARY, "normal"),
                label("OVR: " + team.teamOverall() + "  OFF: " + team.teamOffense() + "  DEF: " + team.teamDefense(), 13, TEXT_PRIMARY, "normal")
        );
        infoTab.setContent(infoContent);

        tabs.getTabs().addAll(infoTab, rosterTab);
        content.getChildren().add(tabs);

        Scene scene = new Scene(content);
        dialog.setScene(scene);
        dialog.show();
    }

    // ─────────────────────────────────────────────────────────────────
    // TRADE SCREEN
    // ─────────────────────────────────────────────────────────────────

    private void showTradeScreen() {
        VBox page = new VBox(16);
        page.setPadding(new Insets(20));
        page.setStyle("-fx-background-color: " + BG_DARK + ";");
        page.getChildren().add(pageHeader("TRADE CENTER", "Propose a trade before the deadline"));

        if (franchise.getCurrentDate().isAfter(franchise.getTradeDeadline()) &&
                franchise.getPhase() == FranchiseMode.SeasonPhase.REGULAR_SEASON) {
            page.getChildren().add(label("Trade deadline has passed.", 16, RED_LOSS, "bold"));
            setContent(page); return;
        }

        Label selectLabel = label("SELECT A TEAM TO TRADE WITH:", 11, TEXT_DIM, "bold");
        selectLabel.setStyle(selectLabel.getStyle() + " -fx-letter-spacing: 2px;");
        page.getChildren().add(selectLabel);

        FlowPane teamButtons = new FlowPane(8, 8);
        for (Team t : franchise.getSortedStandings()) {
            if (t.equals(franchise.getUserTeam())) continue;
            Button btn = teamButton(t);
            btn.setOnAction(e -> showTradeWithTeam(t));
            teamButtons.getChildren().add(btn);
        }
        page.getChildren().add(teamButtons);

        setContent(page);
    }

    private Button teamButton(Team t) {
        String col = TeamColors.getPrimary(t.getName());
        Button btn = new Button(t.getName() + "  OVR:" + t.teamOverall());
        String normalStyle = "-fx-background-color: " + BG_CARD + "; -fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: 13px; -fx-padding: 10 16 10 16; -fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-cursor: hand;";
        String hoverStyle  = "-fx-background-color: " + BG_HOVER + "; -fx-text-fill: " + col + "; -fx-font-size: 13px; -fx-padding: 10 16 10 16; -fx-border-color: " + col + "; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-cursor: hand;";
        btn.setStyle(normalStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(normalStyle));
        return btn;
    }

    private void showTradeWithTeam(Team targetTeam) {
        VBox page = new VBox(16);
        page.setPadding(new Insets(20));
        page.setStyle("-fx-background-color: " + BG_DARK + ";");
        page.getChildren().add(pageHeader("TRADE: " + franchise.getUserTeam().getName() + " ↔ " + targetTeam.getName(), ""));

        List<Player> myPlayersSorted = franchise.getUserTeam().getPlayers().stream()
                .sorted(Comparator.comparingInt(Player::tradeValue).reversed())
                .collect(Collectors.toList());
        List<Player> theirPlayersSorted = targetTeam.getPlayers().stream()
                .sorted(Comparator.comparingInt(Player::tradeValue).reversed())
                .collect(Collectors.toList());

        List<Player> mySelected    = new ArrayList<>();
        List<Player> theirSelected = new ArrayList<>();

        Label myValLabel    = label("Your offer:  0 trade value", 13, TEXT_SECONDARY, "normal");
        Label theirValLabel = label("You receive: 0 trade value", 13, TEXT_SECONDARY, "normal");
        Label verdict       = label("Select players to build a trade", 13, TEXT_DIM, "normal");
        Button proposeBtn   = accentButton("PROPOSE TRADE", true);
        proposeBtn.setDisable(true);

        HBox content = new HBox(16);

        VBox myBox = new VBox(8);
        myBox.getChildren().add(label("YOUR ROSTER — select to offer", 11, TEXT_DIM, "bold"));
        VBox myTable = new VBox(0);
        myTable.setStyle("-fx-background-color: " + BG_CARD + "; -fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 4px; -fx-background-radius: 4px;");
        for (Player p : myPlayersSorted) {
            CheckBox cb = new CheckBox();
            cb.setStyle("-fx-text-fill: " + TEXT_PRIMARY + ";");
            HBox row = buildTradeRow(p, cb);
            cb.setOnAction(e -> {
                if (cb.isSelected()) mySelected.add(p); else mySelected.remove(p);
                updateTradeUI(mySelected, theirSelected, myValLabel, theirValLabel, verdict, proposeBtn);
            });
            myTable.getChildren().add(row);
        }
        myBox.getChildren().add(myTable);
        HBox.setHgrow(myBox, Priority.ALWAYS);

        VBox theirBox = new VBox(8);
        theirBox.getChildren().add(label(targetTeam.getName().toUpperCase() + " — select to request", 11, TEXT_DIM, "bold"));
        VBox theirTable = new VBox(0);
        theirTable.setStyle("-fx-background-color: " + BG_CARD + "; -fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 4px; -fx-background-radius: 4px;");
        for (Player p : theirPlayersSorted) {
            CheckBox cb = new CheckBox();
            cb.setStyle("-fx-text-fill: " + TEXT_PRIMARY + ";");
            HBox row = buildTradeRow(p, cb);
            cb.setOnAction(e -> {
                if (cb.isSelected()) theirSelected.add(p); else theirSelected.remove(p);
                updateTradeUI(mySelected, theirSelected, myValLabel, theirValLabel, verdict, proposeBtn);
            });
            theirTable.getChildren().add(row);
        }
        theirBox.getChildren().add(theirTable);
        HBox.setHgrow(theirBox, Priority.ALWAYS);

        content.getChildren().addAll(myBox, theirBox);
        page.getChildren().add(content);

        VBox summary = card("TRADE SUMMARY", -1);
        summary.getChildren().addAll(myValLabel, theirValLabel, divider(), verdict, proposeBtn);
        page.getChildren().add(summary);

        proposeBtn.setOnAction(e -> {
            String result = franchise.proposeTrade(new ArrayList<>(mySelected), targetTeam, new ArrayList<>(theirSelected));
            showResultDialog(result.contains("COMPLETED") ? "TRADE COMPLETED" : "TRADE REJECTED", result,
                    result.contains("COMPLETED") ? GREEN_WIN : RED_LOSS);
            updateTopBar();
            updateNewsTicker();
            showTradeScreen();
        });

        ScrollPane sp = new ScrollPane(page);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        sp.setFitToWidth(true);
        setContent(sp);
    }

    private HBox buildTradeRow(Player p, CheckBox cb) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(7, 10, 7, 10));
        row.setStyle("-fx-border-color: transparent transparent " + BORDER_COLOR + " transparent; -fx-border-width: 0 0 1 0;");
        Label nameL = label(p.getName(), 12, TEXT_PRIMARY, "normal");
        HBox.setHgrow(nameL, Priority.ALWAYS);
        Label posL  = label(p.getPosition().toString(), 11, TEXT_DIM, "normal");
        Label ovrL  = label("OVR:" + p.overallRating(), 12, accentColor, "bold");
        Label valL  = label("Val:" + p.tradeValue(), 11, TEXT_DIM, "normal");
        row.getChildren().addAll(cb, nameL, posL, ovrL, valL);
        if (p.isInjured()) row.getChildren().add(label("[INJ]", 11, RED_LOSS, "bold"));
        return row;
    }

    private void updateTradeUI(List<Player> mine, List<Player> theirs,
                               Label myVal, Label theirVal, Label verdict, Button btn) {
        int mv = mine.stream().mapToInt(Player::tradeValue).sum();
        int tv = theirs.stream().mapToInt(Player::tradeValue).sum();
        myVal.setText("Your offer: " + mv + " trade value (" + mine.size() + " player" + (mine.size() != 1 ? "s" : "") + ")");
        theirVal.setText("You receive: " + tv + " trade value (" + theirs.size() + " player" + (theirs.size() != 1 ? "s" : "") + ")");
        boolean fair = tv > 0 && mv >= tv * 0.8;
        verdict.setText(mine.isEmpty() || theirs.isEmpty() ? "Select players on both sides" :
                fair ? "Likely to be accepted" : "May be rejected — improve your offer");
        verdict.setStyle("-fx-font-size: 13px; -fx-text-fill: " + (fair ? GREEN_WIN : RED_LOSS) + ";");
        btn.setDisable(mine.isEmpty() || theirs.isEmpty());
    }

    // ─────────────────────────────────────────────────────────────────
    // FREE AGENTS SCREEN
    // ─────────────────────────────────────────────────────────────────

    private void showFreeAgents() {
        VBox page = new VBox(16);
        page.setPadding(new Insets(20));
        page.setStyle("-fx-background-color: " + BG_DARK + ";");
        page.getChildren().add(pageHeader("FREE AGENTS", franchise.getFreeAgentManager().size() + " available"));

        HBox filterRow = new HBox(8);
        filterRow.setAlignment(Pos.CENTER_LEFT);
        filterRow.getChildren().add(label("FILTER: ", 11, TEXT_DIM, "normal"));
        String[] poses = {"ALL", "F", "D", "G"};
        ToggleGroup tg = new ToggleGroup();
        for (String pos : poses) {
            ToggleButton tb = new ToggleButton(pos);
            tb.setToggleGroup(tg);
            tb.setStyle("-fx-background-color: " + BG_CARD + ";" +
                    "-fx-text-fill: " + TEXT_SECONDARY + ";" +
                    "-fx-font-size: 12px; -fx-padding: 6 14 6 14;" +
                    "-fx-border-color: " + BORDER_COLOR + ";" +
                    "-fx-border-radius: 3px; -fx-background-radius: 3px; -fx-cursor: hand;");
            if (pos.equals("ALL")) { tb.setSelected(true); }
            filterRow.getChildren().add(tb);
        }
        page.getChildren().add(filterRow);

        VBox table = buildFATable(franchise.getFreeAgentManager().getFreeAgents());
        page.getChildren().add(table);

        tg.selectedToggleProperty().addListener((obs, o, n) -> {
            if (n == null) return;
            ToggleButton sel = (ToggleButton) n;
            List<Player> filtered;
            switch (sel.getText()) {
                case "F" -> filtered = franchise.getFreeAgentManager().getFreeAgentsByPosition(Position.FORWARD);
                case "D" -> filtered = franchise.getFreeAgentManager().getFreeAgentsByPosition(Position.DEFENSE);
                case "G" -> filtered = franchise.getFreeAgentManager().getFreeAgentsByPosition(Position.GOALIE);
                default  -> filtered = franchise.getFreeAgentManager().getFreeAgents();
            }
            int idx = page.getChildren().size() - 1;
            if (idx >= 0) page.getChildren().remove(idx);
            page.getChildren().add(buildFATable(filtered));
        });

        ScrollPane sp = new ScrollPane(page);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        sp.setFitToWidth(true);
        setContent(sp);
    }

    private VBox buildFATable(List<Player> players) {
        VBox table = new VBox(0);
        table.setStyle("-fx-background-color: " + BG_CARD + ";" +
                "-fx-border-color: " + BORDER_COLOR + ";" +
                "-fx-border-radius: 4px; -fx-background-radius: 4px;");

        HBox hdr = tableRow(true, false);
        hdr.getChildren().addAll(
                tableCell("NAME",  200, TEXT_DIM, true),
                tableCell("POS",    45, TEXT_DIM, false),
                tableCell("AGE",    40, TEXT_DIM, false),
                tableCell("OVR",    45, TEXT_DIM, true),
                tableCell("POT",    45, TEXT_DIM, false),
                tableCell("VAL",    50, TEXT_DIM, false),
                tableCell("",       80, TEXT_DIM, false)
        );
        table.getChildren().add(hdr);

        for (Player p : players) {
            HBox row = tableRow(false, false);
            row.getChildren().addAll(
                    tableCell(p.getName(),              200, TEXT_PRIMARY, false),
                    tableCell(p.getPosition().toString(), 45, TEXT_SECONDARY, false),
                    tableCell(String.valueOf(p.getAge()), 40, TEXT_SECONDARY, false),
                    tableCell(String.valueOf(p.overallRating()), 45, accentColor, true),
                    tableCell(String.valueOf(p.getPotential()), 45, TEXT_SECONDARY, false),
                    tableCell(String.valueOf(p.tradeValue()),   50, TEXT_SECONDARY, false)
            );

            Button signBtn = smallAccentButton("SIGN");
            final Player fp = p;
            signBtn.setOnAction(e -> showSignDialog(fp));
            HBox btnBox = new HBox(signBtn);
            btnBox.setPrefWidth(80);
            btnBox.setAlignment(Pos.CENTER);
            row.getChildren().add(btnBox);

            table.getChildren().add(row);
        }

        if (players.isEmpty()) {
            HBox empty = new HBox();
            empty.setPadding(new Insets(20));
            empty.getChildren().add(label("No free agents available.", 13, TEXT_DIM, "normal"));
            table.getChildren().add(empty);
        }

        return table;
    }

    private void showSignDialog(Player p) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Sign " + p.getName());
        dialog.setWidth(460);

        VBox content = new VBox(14);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: " + BG_PANEL + ";");

        content.getChildren().add(label("FREE AGENT SIGNING", 10, TEXT_DIM, "bold"));

        HBox playerRow = new HBox(14);
        playerRow.setAlignment(Pos.CENTER_LEFT);
        StackPane logo = buildTeamLogoPane(franchise.getUserTeam().getName(), accentColor, 24);
        VBox pInfo = new VBox(3);
        pInfo.getChildren().addAll(
                label(p.getName(), 20, TEXT_PRIMARY, "bold"),
                label(p.getPosition() + "  •  OVR: " + p.overallRating() + "  •  Age: " + p.getAge(), 12, TEXT_SECONDARY, "normal")
        );
        playerRow.getChildren().addAll(logo, pInfo);
        content.getChildren().add(playerRow);

        int budget = franchise.getSigningBudget();
        int maxBudget = franchise.getSigningBudgetMax();
        HBox budgetRow = new HBox(10);
        budgetRow.setAlignment(Pos.CENTER_LEFT);
        budgetRow.setPadding(new Insets(8, 12, 8, 12));
        budgetRow.setStyle("-fx-background-color: " + BG_DARK + "; -fx-border-radius: 3px; -fx-background-radius: 3px;");
        Label budgetLbl = label("SIGNING BUDGET", 10, TEXT_DIM, "bold");
        Region budgetSp = new Region(); HBox.setHgrow(budgetSp, Priority.ALWAYS);
        String budgetCol = budget > 60 ? GREEN_WIN : budget > 30 ? GOLD : RED_LOSS;
        Label budgetVal = label(budget + " / " + maxBudget + " pts", 13, budgetCol, "bold");
        StackPane barBg = new StackPane();
        barBg.setPrefSize(100, 6);
        barBg.setMaxHeight(6);
        Rectangle barBack = new Rectangle(100, 6, Color.web(BORDER_COLOR));
        barBack.setArcWidth(3); barBack.setArcHeight(3);
        Rectangle barFill = new Rectangle(Math.max(0, 100.0 * budget / maxBudget), 6, Color.web(budgetCol));
        barFill.setArcWidth(3); barFill.setArcHeight(3);
        StackPane.setAlignment(barFill, Pos.CENTER_LEFT);
        barBg.getChildren().addAll(barBack, barFill);
        budgetRow.getChildren().addAll(budgetLbl, budgetSp, barBg, budgetVal);
        content.getChildren().add(budgetRow);

        List<FreeAgentManager.TeamOffer> aiOffers = franchise.getFreeAgentManager().getAIOffers(p, franchise.getUserTeam(), franchise.getLeagueTeams());
        VBox competitionBox = new VBox(5);
        competitionBox.setPadding(new Insets(8, 12, 8, 12));
        competitionBox.setStyle("-fx-background-color: " + BG_DARK + "; -fx-border-radius: 3px; -fx-background-radius: 3px;");
        if (aiOffers.isEmpty()) {
            competitionBox.getChildren().add(label("✓  No other teams interested — easy signing!", 12, GREEN_WIN, "normal"));
        } else {
            competitionBox.getChildren().add(label("⚠  " + aiOffers.size() + " team(s) competing:", 11, GOLD, "bold"));
            for (FreeAgentManager.TeamOffer offer : aiOffers) {
                String strength = offer.offerScore >= 80 ? "Max offer" : offer.offerScore >= 60 ? "Strong" : offer.offerScore >= 40 ? "Moderate" : "Low";
                competitionBox.getChildren().add(label("  • " + offer.team.getName() + "  " + offer.years + "yr  (" + strength + ")", 11, TEXT_SECONDARY, "normal"));
            }
        }
        content.getChildren().add(competitionBox);
        content.getChildren().add(divider());

        HBox yearRow = new HBox(8);
        yearRow.setAlignment(Pos.CENTER_LEFT);
        yearRow.getChildren().add(label("Length:", 12, TEXT_SECONDARY, "normal"));
        ToggleGroup yearGroup = new ToggleGroup();
        int[] selectedYears = {1};
        for (int y : new int[]{1, 2, 3}) {
            ToggleButton tb = new ToggleButton(y + " YR");
            tb.setToggleGroup(yearGroup);
            if (y == 1) tb.setSelected(true);
            String tnormal = "-fx-background-color: " + BG_CARD + "; -fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 12px; -fx-padding: 6 14; -fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 3px; -fx-background-radius: 3px; -fx-cursor: hand;";
            String tsel    = "-fx-background-color: " + accentColor + "; -fx-text-fill: " + BG_DARK + "; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 6 14; -fx-border-color: " + accentColor + "; -fx-border-radius: 3px; -fx-background-radius: 3px; -fx-cursor: hand;";
            tb.setStyle(y == 1 ? tsel : tnormal);
            final int fy = y;
            tb.selectedProperty().addListener((obs, o, n) -> {
                tb.setStyle(n ? tsel : tnormal);
                if (n) selectedYears[0] = fy;
            });
            yearRow.getChildren().add(tb);
        }
        content.getChildren().add(yearRow);

        content.getChildren().add(label("Offer quality:", 12, TEXT_SECONDARY, "normal"));
        Slider offerSlider = new Slider(20, 100, 50);
        offerSlider.setShowTickLabels(true);
        offerSlider.setMajorTickUnit(20);
        offerSlider.setStyle("-fx-control-inner-background: " + BG_CARD + ";");

        HBox offerInfoRow = new HBox(10);
        offerInfoRow.setAlignment(Pos.CENTER_LEFT);
        Label offerLabel = label("Moderate Offer (50)", 12, accentColor, "bold");
        Region offerSp = new Region(); HBox.setHgrow(offerSp, Priority.ALWAYS);
        Label costLabel = label("Cost: 13 pts", 11, TEXT_DIM, "normal");
        offerInfoRow.getChildren().addAll(offerLabel, offerSp, costLabel);

        offerSlider.valueProperty().addListener((obs, o, n) -> {
            int v = n.intValue();
            int cost = Math.max(3, (int) Math.ceil(v * 0.25));
            String q = v >= 80 ? "Max Offer" : v >= 60 ? "Strong Offer" : v >= 40 ? "Moderate Offer" : "Low Offer";
            offerLabel.setText(q + " (" + v + ")");
            String offerCol = v >= 60 ? GREEN_WIN : v >= 40 ? GOLD : RED_LOSS;
            offerLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + offerCol + "; -fx-font-weight: bold;");
            String costCol = cost > budget ? RED_LOSS : TEXT_DIM;
            costLabel.setText("Cost: " + cost + " pts" + (cost > budget ? " — INSUFFICIENT!" : ""));
            costLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + costCol + ";");
        });
        content.getChildren().addAll(offerSlider, offerInfoRow);

        HBox btns = new HBox(10);
        Button submitBtn = accentButton("SUBMIT OFFER", true);
        submitBtn.setOnAction(e -> {
            int score = (int) offerSlider.getValue();
            FreeAgentManager.SigningResult result = franchise.signFreeAgentCompetitive(p, selectedYears[0], score);
            dialog.close();
            if (result.userWon) {
                showResultDialog("SIGNED!", result.message, GREEN_WIN);
                updateTopBar(); updateNewsTicker(); showFreeAgents();
            } else {
                showResultDialog("SIGNING FAILED", result.message, RED_LOSS);
                updateNewsTicker(); showFreeAgents();
            }
        });
        Button cancelBtn = ghostButton("Cancel");
        cancelBtn.setOnAction(e -> dialog.close());
        btns.getChildren().addAll(submitBtn, cancelBtn);
        content.getChildren().add(btns);

        Scene scene = new Scene(content);
        dialog.setScene(scene);
        dialog.show();
    }

    private void showPlayerCard(Player p) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(p.getName() + " — Player Details");

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: " + BG_DARK + ";");
        root.setPrefWidth(480);

        // Header bar
        HBox header = new HBox(14);
        header.setPadding(new Insets(20, 24, 16, 24));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: " + BG_PANEL + "; -fx-border-color: " + BORDER_COLOR + "; -fx-border-width: 0 0 1 0;");

        String teamName = franchise.getLeagueTeams().stream()
                .filter(t -> t.getPlayers().contains(p))
                .map(Team::getName).findFirst().orElse("");
        String col = teamName.isEmpty() ? accentColor : TeamColors.getPrimary(teamName);

        StackPane logoPane = buildTeamLogoPane(teamName, col, 42);
        VBox nameBox = new VBox(3);
        String gemTag = p.isGem() ? " 💎" : "";
        Label nameLabel = new Label(p.getName() + gemTag);
        nameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");
        Label posLabel = new Label(p.getPosition().toString() + (teamName.isEmpty() ? "" : "  •  " + teamName));
        posLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + TEXT_SECONDARY + ";");
        nameBox.getChildren().addAll(nameLabel, posLabel);
        HBox.setHgrow(nameBox, Priority.ALWAYS);
        header.getChildren().addAll(logoPane, nameBox);

        VBox content = new VBox(16);
        content.setPadding(new Insets(20, 24, 24, 24));

        // Overall + status row
        HBox ovr = new HBox(20);
        ovr.setAlignment(Pos.CENTER_LEFT);
        VBox ovrBox = statPill("OVR", String.valueOf(p.overallRating()), col);
        VBox potBox = statPill("POT", String.valueOf(p.getPotential()), p.isGem() ? GOLD : TEXT_SECONDARY);
        VBox ageBox = statPill("AGE", String.valueOf(p.getAge()), TEXT_PRIMARY);
        String cyrStr = p.getContractYearsRemaining() <= 0 ? "UFA" : (p.isLastYear() ? "LAST" : String.valueOf(p.getContractYearsRemaining()));
        String cyrCol = p.getContractYearsRemaining() <= 0 ? RED_LOSS : p.isLastYear() ? GOLD : TEXT_SECONDARY;
        VBox ctrBox = statPill("CTR", cyrStr, cyrCol);
        ovr.getChildren().addAll(ovrBox, potBox, ageBox, ctrBox);
        if (p.isGem()) {
            Label gemLbl = new Label("💎 GENERATIONAL TALENT");
            gemLbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + GOLD + "; -fx-background-color: #2A2000; -fx-padding: 4 10; -fx-background-radius: 4px;");
            ovr.getChildren().add(gemLbl);
        }
        if (p.isInjured()) {
            Label injLbl = new Label("🤕 INJ: " + p.getInjuryDescription() + " (" + p.getInjuryDays() + "d)");
            injLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: " + RED_LOSS + "; -fx-background-color: " + RED_LOSS + "22; -fx-padding: 4 10; -fx-background-radius: 4px;");
            ovr.getChildren().add(injLbl);
        }
        content.getChildren().add(ovr);

        // Ratings section (skaters)
        if (p.getPosition() != com.juniorhockeysim.domain.Position.GOALIE) {
            content.getChildren().add(cardSectionLabel("RATINGS"));
            GridPane ratingsGrid = new GridPane();
            ratingsGrid.setHgap(16); ratingsGrid.setVgap(10);
            ratingsGrid.setStyle("-fx-background-color: " + BG_CARD + "; -fx-padding: 14; -fx-background-radius: 6px;");
            addRatingBar(ratingsGrid, 0, "Shooting", p.getShooting(), col);
            addRatingBar(ratingsGrid, 1, "Passing", p.getPassing(), col);
            addRatingBar(ratingsGrid, 2, "Skating", p.getSkating(), col);
            addRatingBar(ratingsGrid, 3, "Defense", p.getDefense(), col);
            addRatingBar(ratingsGrid, 4, "Physical", p.getPhysical(), col);
            content.getChildren().add(ratingsGrid);
        } else {
            content.getChildren().add(cardSectionLabel("RATINGS"));
            GridPane ratingsGrid = new GridPane();
            ratingsGrid.setHgap(16); ratingsGrid.setVgap(10);
            ratingsGrid.setStyle("-fx-background-color: " + BG_CARD + "; -fx-padding: 14; -fx-background-radius: 6px;");
            addRatingBar(ratingsGrid, 0, "Save Rating", p.getSaveRating(), col);
            content.getChildren().add(ratingsGrid);
        }

        // Attributes
        content.getChildren().add(cardSectionLabel("ATTRIBUTES"));
        HBox attrsRow = new HBox(16);
        attrsRow.setStyle("-fx-background-color: " + BG_CARD + "; -fx-padding: 14; -fx-background-radius: 6px;");
        attrsRow.getChildren().addAll(
                attrBox("Work Ethic", p.getWorkEthic(), col),
                attrBox("Consistency", p.getConsistency(), col),
                attrBox("Morale", p.getMorale(), moralColor(p.getMorale()))
        );
        content.getChildren().add(attrsRow);

        // Season stats
        content.getChildren().add(cardSectionLabel("THIS SEASON"));
        HBox statsRow = new HBox(16);
        statsRow.setStyle("-fx-background-color: " + BG_CARD + "; -fx-padding: 14; -fx-background-radius: 6px;");
        if (p.getPosition() != com.juniorhockeysim.domain.Position.GOALIE) {
            statsRow.getChildren().addAll(
                    statBox("GP", String.valueOf(p.getGamesPlayed())),
                    statBox("G", String.valueOf(p.getGoals())),
                    statBox("A", String.valueOf(p.getAssists())),
                    statBox("PTS", String.valueOf(p.getPoints())),
                    statBox("+/-", (p.getPlusMinus() >= 0 ? "+" : "") + p.getPlusMinus())
            );
        } else {
            statsRow.getChildren().addAll(
                    statBox("GP", String.valueOf(p.getGoalieGamesPlayed())),
                    statBox("W", String.valueOf(p.getGoalieWins())),
                    statBox("SV%", String.format("%.3f", p.getSavePercentage())),
                    statBox("GAA", String.format("%.2f", p.getGAA())),
                    statBox("SO", String.valueOf(p.getShutouts()))
            );
        }
        content.getChildren().add(statsRow);

        // Career stats
        content.getChildren().add(cardSectionLabel("CAREER TOTALS"));
        HBox careerRow = new HBox(16);
        careerRow.setStyle("-fx-background-color: " + BG_CARD + "; -fx-padding: 14; -fx-background-radius: 6px;");
        careerRow.getChildren().addAll(
                statBox("GP", String.valueOf(p.getCareerGP())),
                statBox("G", String.valueOf(p.getCareerGoals())),
                statBox("A", String.valueOf(p.getCareerAssists())),
                statBox("PTS", String.valueOf(p.getCareerGoals() + p.getCareerAssists()))
        );
        content.getChildren().add(careerRow);

        // Trade value
        Label tvalLabel = new Label("Trade Value: " + p.tradeValue());
        tvalLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + TEXT_DIM + ";");
        content.getChildren().add(tvalLabel);

        Button closeBtn = new Button("CLOSE");
        closeBtn.setStyle("-fx-background-color: " + col + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 28; -fx-background-radius: 4px; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> dialog.close());
        content.getChildren().add(closeBtn);

        ScrollPane sp = new ScrollPane(content);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        sp.setFitToWidth(true);

        root.getChildren().addAll(header, sp);
        dialog.setScene(new Scene(root, 480, 640));
        dialog.setResizable(false);
        dialog.show();
    }

    private VBox statPill(String label, String value, String color) {
        VBox box = new VBox(2);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(8, 14, 8, 14));
        box.setStyle("-fx-background-color: " + BG_CARD + "; -fx-background-radius: 6px; -fx-border-color: " + color + "55; -fx-border-radius: 6px;");
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 9px; -fx-text-fill: " + TEXT_DIM + "; -fx-letter-spacing: 1px;");
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        box.getChildren().addAll(lbl, val);
        return box;
    }

    private VBox attrBox(String name, int value, String col) {
        VBox box = new VBox(4);
        box.setAlignment(Pos.CENTER);
        HBox.setHgrow(box, Priority.ALWAYS);
        Label nameLbl = new Label(name);
        nameLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: " + TEXT_DIM + ";");
        Label valLbl = new Label(String.valueOf(value));
        valLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + col + ";");
        box.getChildren().addAll(nameLbl, valLbl);
        return box;
    }

    private VBox statBox(String name, String value) {
        VBox box = new VBox(4);
        box.setAlignment(Pos.CENTER);
        HBox.setHgrow(box, Priority.ALWAYS);
        Label nameLbl = new Label(name);
        nameLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: " + TEXT_DIM + ";");
        Label valLbl = new Label(value);
        valLbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");
        box.getChildren().addAll(nameLbl, valLbl);
        return box;
    }

    private Label cardSectionLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 10px; -fx-text-fill: " + TEXT_DIM + "; -fx-letter-spacing: 2px; -fx-padding: 4 0 0 0;");
        return l;
    }

    private void addRatingBar(GridPane grid, int row, String name, int value, String color) {
        Label nameLbl = new Label(name);
        nameLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: " + TEXT_SECONDARY + "; -fx-min-width: 80px;");
        Label valLbl = new Label(String.valueOf(value));
        valLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + color + "; -fx-min-width: 30px;");
        ProgressBar bar = new ProgressBar(value / 99.0);
        bar.setPrefWidth(200);
        bar.setPrefHeight(8);
        bar.setStyle("-fx-accent: " + color + "; -fx-control-inner-background: " + BG_DARK + ";");
        grid.add(nameLbl, 0, row);
        grid.add(bar, 1, row);
        grid.add(valLbl, 2, row);
    }

    private void showReleaseDialog(Team team) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Release Player");
        dialog.setWidth(400);

        VBox content = new VBox(12);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: " + BG_PANEL + ";");
        content.getChildren().add(label("RELEASE PLAYER", 11, TEXT_DIM, "bold"));
        content.getChildren().add(label("Select a player to release to free agency:", 13, TEXT_SECONDARY, "normal"));

        ComboBox<String> combo = new ComboBox<>();
        combo.setStyle("-fx-background-color: " + BG_CARD + ";" +
                "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                "-fx-border-color: " + BORDER_COLOR + ";");
        combo.setPrefWidth(340);

        List<Player> releaseable = team.getPlayers().stream()
                .sorted(Comparator.comparingInt(Player::tradeValue))
                .collect(Collectors.toList());
        Map<String, Player> playerMap = new LinkedHashMap<>();
        for (Player p : releaseable) {
            String key = p.getName() + " — " + p.getPosition() + " OVR:" + p.overallRating();
            combo.getItems().add(key);
            playerMap.put(key, p);
        }

        Button releaseBtn = new Button("RELEASE");
        releaseBtn.setStyle("-fx-background-color: " + RED_LOSS + "; -fx-text-fill: white; -fx-padding: 8 20; -fx-cursor: hand;");
        releaseBtn.setOnAction(e -> {
            String sel = combo.getValue();
            if (sel != null && playerMap.containsKey(sel)) {
                Player p = playerMap.get(sel);
                franchise.releasePlayer(p);
                dialog.close();
                showResultDialog("RELEASED", p.getName() + " has been released to free agency.", TEXT_SECONDARY);
                updateTopBar(); updateNewsTicker();
                showFreeAgents();
            }
        });

        Button cancelBtn = ghostButton("Cancel");
        cancelBtn.setOnAction(e -> dialog.close());

        HBox row = new HBox(8, releaseBtn, cancelBtn);
        content.getChildren().addAll(combo, row);

        Scene scene = new Scene(content);
        dialog.setScene(scene);
        dialog.show();
    }

    // ─────────────────────────────────────────────────────────────────
    // PLAYOFFS SCREEN
    // ─────────────────────────────────────────────────────────────────

    private void showPlayoffs() {
        VBox page = new VBox(16);
        page.setPadding(new Insets(20));
        page.setStyle("-fx-background-color: " + BG_DARK + ";");
        page.getChildren().add(pageHeader("PLAYOFFS", franchise.getSeasonYear() + "-" + (franchise.getSeasonYear()+1)));

        if (franchise.getPhase() == FranchiseMode.SeasonPhase.REGULAR_SEASON) {
            page.getChildren().add(label("Playoffs begin after the regular season ends.", 14, TEXT_DIM, "normal"));
            page.getChildren().add(label("Regular season ends: " + franchise.getSeasonEndDate(), 13, accentColor, "normal"));
            setContent(page); return;
        }

        PlayoffBracket bracket = franchise.getPlayoffBracket();
        if (bracket == null) {
            page.getChildren().add(label("No playoff data available.", 13, TEXT_DIM, "normal"));
            setContent(page); return;
        }

        if (!bracket.isComplete()) {
            page.getChildren().add(label(bracket.getCurrentRoundName().toUpperCase(), 16, accentColor, "bold"));

            for (PlayoffSeries s : bracket.getActiveSeries()) {
                VBox seriesCard = card(s.getHigherSeed().getName() + " vs " + s.getLowerSeed().getName(), -1);
                String col1 = TeamColors.getPrimary(s.getHigherSeed().getName());
                String col2 = TeamColors.getPrimary(s.getLowerSeed().getName());
                HBox seriesRow = new HBox(20);
                seriesRow.setAlignment(Pos.CENTER);
                seriesRow.getChildren().addAll(
                        teamMiniDisplay(s.getHigherSeed().getName(), col1),
                        label(s.getHigherWins() + " — " + s.getLowerWins(), 24, TEXT_PRIMARY, "bold"),
                        teamMiniDisplay(s.getLowerSeed().getName(), col2)
                );
                seriesCard.getChildren().add(seriesRow);
                String leader = s.getHigherWins() > s.getLowerWins() ? s.getHigherSeed().getName() :
                        s.getLowerWins() > s.getHigherWins() ? s.getLowerSeed().getName() : "Tied";
                seriesCard.getChildren().add(label(leader + " leads — Game " + (s.getGameNum()+1), 12, TEXT_SECONDARY, "normal"));
                seriesCard.getChildren().add(label("Next game: " + s.getNextGameDate(), 11, TEXT_DIM, "normal"));
                page.getChildren().add(seriesCard);
            }

            Button simBtn = accentButton("SIMULATE NEXT DAY", true);
            simBtn.setOnAction(e -> {
                franchise.advanceDaySilent();
                updateTopBar(); updateNewsTicker();
                showPlayoffs();
            });
            Button simAllBtn = ghostButton("Sim Entire Playoffs");
            simAllBtn.setOnAction(e -> {
                franchise.simulateEntirePlayoffs();
                updateTopBar(); updateNewsTicker();
                showPlayoffs();
            });
            HBox btnRow = new HBox(8, simBtn, simAllBtn);
            page.getChildren().add(btnRow);
        } else {
            Team champ = bracket.getChampion();
            String champCol = TeamColors.getPrimary(champ.getName());

            VBox champCard = new VBox(16);
            champCard.setAlignment(Pos.CENTER);
            champCard.setPadding(new Insets(40));
            champCard.setStyle("-fx-background-color: " + champCol + "15;" +
                    "-fx-border-color: " + champCol + ";" +
                    "-fx-border-radius: 6px;");
            StackPane lp = buildTeamLogoPane(champ.getName(), champCol, 50);
            Label trophy = label("CHAMPIONS", 36, champCol, "bold");
            Label champName = label(champ.getName(), 24, TEXT_PRIMARY, "bold");
            champCard.getChildren().addAll(lp, trophy, champName,
                    label(franchise.getSeasonYear() + "-" + (franchise.getSeasonYear()+1) + " Season", 14, TEXT_SECONDARY, "normal"));
            page.getChildren().add(champCard);
        }

        if (!bracket.getCompletedSeries().isEmpty()) {
            page.getChildren().add(sectionHeader("COMPLETED SERIES", accentColor));
            for (PlayoffSeries s : bracket.getCompletedSeries()) {
                if (s.getWinner() == null) continue;
                HBox row = new HBox(12);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(4, 0, 4, 0));
                String winnerCol = TeamColors.getPrimary(s.getWinner().getName());
                row.getChildren().addAll(
                        label(s.getWinner().getName() + " wins", 13, winnerCol, "bold"),
                        label(s.getSummary(), 12, TEXT_SECONDARY, "normal")
                );
                page.getChildren().add(row);
            }
        }

        ScrollPane sp = new ScrollPane(page);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        sp.setFitToWidth(true);
        setContent(sp);
    }

    // ─────────────────────────────────────────────────────────────────
    // OFFSEASON SCREEN
    // ─────────────────────────────────────────────────────────────────

    private void showOffseason() {
        VBox page = new VBox(16);
        page.setPadding(new Insets(20));
        page.setStyle("-fx-background-color: " + BG_DARK + ";");
        page.getChildren().add(pageHeader("OFFSEASON", "End of " + franchise.getSeasonYear() + "-" + (franchise.getSeasonYear()+1)));

        if (franchise.getPhase() != FranchiseMode.SeasonPhase.OFF_SEASON) {
            page.getChildren().add(label("The season is still in progress.", 14, TEXT_DIM, "normal"));
            setContent(page); return;
        }

        // Roster warnings for user team
        Team ut = franchise.getUserTeam();
        int fwds = ut.getForwards().size(), defs = ut.getDefenders().size(), goas = ut.getGoalies().size();
        if (fwds < 9 || defs < 4 || goas < 1) {
            VBox warnBox = new VBox(4);
            warnBox.setPadding(new Insets(10, 14, 10, 14));
            warnBox.setStyle("-fx-background-color: " + RED_LOSS + "22; -fx-border-color: " + RED_LOSS + "; -fx-border-radius: 4px; -fx-background-radius: 4px;");
            warnBox.getChildren().add(label("⚠ ROSTER WARNING — YOUR TEAM NEEDS PLAYERS", 12, RED_LOSS, "bold"));
            if (fwds < 9) warnBox.getChildren().add(label("• Only " + fwds + " forwards (need 9 for 3 full lines)", 12, GOLD, "normal"));
            if (defs < 4) warnBox.getChildren().add(label("• Only " + defs + " defensemen (need 4 for 2 full pairs)", 12, GOLD, "normal"));
            if (goas < 1) warnBox.getChildren().add(label("• No goalie! Sign one via Free Agents or Draft!", 12, GOLD, "normal"));
            page.getChildren().add(warnBox);
        }

        VBox stepsCard = card("OFFSEASON STEPS", -1);
        stepsCard.getChildren().add(label("Complete these steps in order to begin the next season:", 13, TEXT_SECONDARY, "normal"));
        stepsCard.getChildren().add(divider());

        boolean devDone = franchise.isDevelopmentDone();
        Button devBtn = accentButton("1. PLAYER DEVELOPMENT + AGING" + (devDone ? "  ✓ DONE" : ""), devDone ? false : true);
        devBtn.setMaxWidth(Double.MAX_VALUE);
        if (devDone) {
            devBtn.setDisable(true);
        } else {
            devBtn.setOnAction(e -> {
                List<String> log = franchise.endSeasonDevelopment();
                String userTeamName = franchise.getUserTeam().getName();
                List<String> userLog = log.stream()
                        .filter(line -> line.startsWith(userTeamName + ":"))
                        .map(line -> line.substring(userTeamName.length() + 2))
                        .collect(Collectors.toList());
                if (userLog.isEmpty()) userLog.add("No notable development changes for your team this offseason.");
                showResultDialog("YOUR TEAM DEVELOPMENT",
                        String.join("\n", userLog.stream().limit(25).collect(Collectors.toList())), GREEN_WIN);
                updateNewsTicker();
                showOffseason();
            });
        }

        boolean draftDoneFlag = franchise.isDraftDone();
        Button draftBtn = accentButton("2. ENTRY DRAFT" + (draftDoneFlag ? "  ✓ DONE" : ""), !draftDoneFlag);
        draftBtn.setMaxWidth(Double.MAX_VALUE);
        if (draftDoneFlag) {
            draftBtn.setDisable(true);
        } else {
            draftBtn.setOnAction(e -> showDraftScreen());
        }

        Button newSeasonBtn = accentButton("3. START NEW SEASON", true);
        newSeasonBtn.setMaxWidth(Double.MAX_VALUE);
        newSeasonBtn.setOnAction(e -> {
            franchise.newSeason();
            updateTopBar(); updateNewsTicker();
            showDashboard();
        });

        stepsCard.getChildren().addAll(devBtn, draftBtn, newSeasonBtn);
        page.getChildren().add(stepsCard);
        setContent(page);
    }

    // ── Draft ──────────────────────────────────────────────────────────
    private void showDraftScreen() {
        DraftManager dm      = franchise.getDraftManager();
        List<Team>   order   = dm.getDraftOrder();
        List<Player> pool    = new ArrayList<>(dm.getDraftClass());

        int userPickNum = -1;
        for (int i = 0; i < order.size(); i++) {
            if (order.get(i).equals(franchise.getUserTeam())) { userPickNum = i + 1; break; }
        }

        Map<Integer, Player> picks = new LinkedHashMap<>();
        List<Player> takenPlayers = new ArrayList<>();

        buildDraftView(dm, order, pool, picks, takenPlayers, userPickNum);
    }

    private void buildDraftView(DraftManager dm, List<Team> order, List<Player> pool,
                                Map<Integer, Player> picks, List<Player> takenPlayers, int userPickNum) {
        VBox page = new VBox(16);
        page.setPadding(new Insets(20));
        page.setStyle("-fx-background-color: " + BG_DARK + ";");

        int totalPicks = order.size() * com.juniorhockeysim.core.DraftManager.TOTAL_ROUNDS;
        int nextPick = picks.size() + 1;
        int currentRound = (nextPick - 1) / order.size() + 1;
        int pickInRound = ((nextPick - 1) % order.size()) + 1;
        // Snake draft: odd rounds normal order, even rounds reversed
        int effectivePickInRound = (currentRound % 2 == 0) ? (order.size() - pickInRound + 1) : pickInRound;
        int userPickInRound = (currentRound % 2 == 0) ? (order.size() - userPickNum + 2) : userPickNum;
        boolean userTurn = (pickInRound == userPickInRound);
        boolean draftDone = (nextPick > totalPicks || pool.isEmpty());

        String subtitle = draftDone ? "Draft Complete"
                : userTurn  ? "R" + currentRound + " YOUR PICK — #" + pickInRound + " in round"
                :             "Round " + currentRound + " of " + com.juniorhockeysim.core.DraftManager.TOTAL_ROUNDS + ", Pick #" + pickInRound + "/" + order.size();
        page.getChildren().add(pageHeader("ENTRY DRAFT " + (franchise.getSeasonYear() + 1), subtitle));

        if (!draftDone && !userTurn && pickInRound < userPickInRound) {
            VBox banner = new VBox(4);
            banner.setPadding(new Insets(10, 14, 10, 14));
            banner.setStyle("-fx-background-color: " + accentColor + "15;" +
                    "-fx-border-color: " + accentColor + ";" +
                    "-fx-border-radius: 4px; -fx-background-radius: 4px;");
            int picksLeft = userPickInRound - pickInRound;
            banner.getChildren().addAll(
                    label("YOUR PICK: Round " + currentRound + ", #" + userPickInRound + " in round", 12, accentColor, "bold"),
                    label(picksLeft + " pick" + (picksLeft == 1 ? "" : "s") + " until your turn", 11, TEXT_SECONDARY, "normal")
            );
            page.getChildren().add(banner);
        }

        // Show gem alert banner if a gem is still available in the pool
        boolean gemAvailable = pool.stream().anyMatch(Player::isGem);
        if (gemAvailable) {
            HBox gemBanner = new HBox(10);
            gemBanner.setPadding(new Insets(10, 16, 10, 16));
            gemBanner.setAlignment(Pos.CENTER_LEFT);
            gemBanner.setStyle("-fx-background-color: #2A2000; -fx-border-color: " + GOLD + "; -fx-border-radius: 4px; -fx-background-radius: 4px;");
            gemBanner.getChildren().addAll(
                    label("💎", 20, GOLD, "normal"),
                    label("GENERATIONAL TALENT AVAILABLE", 13, GOLD, "bold"),
                    label("— A once-in-a-generation prospect is in this draft class!", 12, TEXT_SECONDARY, "normal")
            );
            page.getChildren().add(gemBanner);
        }

        HBox columns = new HBox(16);
        columns.setAlignment(Pos.TOP_LEFT);

        VBox logBox = new VBox(0);
        logBox.setPrefWidth(380);
        logBox.setStyle("-fx-background-color: " + BG_CARD + ";" +
                "-fx-border-color: " + BORDER_COLOR + ";" +
                "-fx-border-radius: 4px; -fx-background-radius: 4px;");

        HBox logHdr = tableRow(true, false);
        logHdr.getChildren().addAll(
                tableCell("PICK", 50,  TEXT_DIM, false),
                tableCell("TEAM", 130, TEXT_DIM, false),
                tableCell("PLAYER", 130, TEXT_DIM, true),
                tableCell("OVR",  45, TEXT_DIM, false)
        );
        logBox.getChildren().add(logHdr);

        for (int round = 1; round <= com.juniorhockeysim.core.DraftManager.TOTAL_ROUNDS; round++) {
            HBox roundHdr = new HBox();
            roundHdr.setPadding(new Insets(4, 8, 4, 8));
            roundHdr.setStyle("-fx-background-color: " + accentColor + "25;");
            roundHdr.getChildren().add(label("ROUND " + round, 10, accentColor, "bold"));
            logBox.getChildren().add(roundHdr);
            for (int i = 0; i < order.size(); i++) {
                int pickIdx = (round % 2 == 1) ? i : (order.size() - 1 - i);
                Team t = order.get(pickIdx);
                boolean isUser = t.equals(franchise.getUserTeam());
                HBox row = tableRow(false, isUser);
                String pickCol = isUser ? accentColor : TEXT_SECONDARY;
                int globalPick = (round - 1) * order.size() + i + 1;
                row.getChildren().add(tableCell(String.valueOf(i+1), 50, pickCol, true));
                row.getChildren().add(tableCell(t.getName(), 130, isUser ? accentColor : TEXT_PRIMARY, false));
                if (picks.containsKey(globalPick)) {
                    Player drafted = picks.get(globalPick);
                    row.getChildren().add(tableCell(drafted.getName(), 130, isUser ? accentColor : TEXT_PRIMARY, false));
                    row.getChildren().add(tableCell(String.valueOf(drafted.overallRating()), 45, isUser ? accentColor : TEXT_SECONDARY, true));
                } else if (globalPick == nextPick && !draftDone) {
                    row.getChildren().add(tableCell("← ON THE CLOCK", 175, GOLD, true));
                } else {
                    row.getChildren().add(tableCell("—", 175, TEXT_DIM, false));
                }
                logBox.getChildren().add(row);
            }
        }

        ScrollPane logScroll = new ScrollPane(logBox);
        logScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        logScroll.setPrefViewportHeight(460);
        logScroll.setFitToWidth(true);

        VBox rightBox = new VBox(10);
        HBox.setHgrow(rightBox, Priority.ALWAYS);

        if (draftDone) {
            franchise.markDraftDone();
            VBox done = card("DRAFT COMPLETE", -1);
            done.getChildren().add(label("All picks have been made.", 13, TEXT_DIM, "normal"));
            Button backBtn = accentButton("BACK TO OFFSEASON", true);
            backBtn.setOnAction(e -> showOffseason());
            done.getChildren().add(backBtn);
            rightBox.getChildren().add(done);

        } else if (userTurn) {
            VBox pickCard = card("YOUR PICK — SELECT A PLAYER", -1);
            pickCard.getChildren().add(label("Choose wisely — " + pool.size() + " players available", 11, TEXT_DIM, "normal"));

            VBox pickTable = new VBox(0);
            pickTable.setStyle("-fx-background-color: " + BG_CARD + ";" +
                    "-fx-border-color: " + accentColor + "50;" +
                    "-fx-border-radius: 4px; -fx-background-radius: 4px;");
            HBox ph = tableRow(true, false);
            ph.getChildren().addAll(
                    tableCell("NAME", 190, TEXT_DIM, true),
                    tableCell("POS",   50, TEXT_DIM, false),
                    tableCell("OVR",   50, TEXT_DIM, true),
                    tableCell("POT",   50, TEXT_DIM, false),
                    tableCell("",      80, TEXT_DIM, false)
            );
            pickTable.getChildren().add(ph);

            // Sort: gems first, then by OVR descending
            List<Player> sortedPool = pool.stream()
                    .sorted(Comparator.<Player, Boolean>comparing(p -> !p.isGem())
                            .thenComparingInt(p -> -p.overallRating()))
                    .collect(Collectors.toList());

            for (Player p : sortedPool) {
                boolean isGem = p.isGem();
                HBox pr = tableRow(false, false);
                if (isGem) {
                    pr.setStyle("-fx-background-color: #2A2000; -fx-border-color: " + GOLD + "; -fx-border-width: 1.5px; -fx-border-radius: 3px; -fx-background-radius: 3px;");
                }
                String nameDisplay = isGem ? "💎 " + p.getName() : p.getName();
                String nameCol = isGem ? GOLD : TEXT_PRIMARY;
                String potCol  = isGem ? GOLD : TEXT_SECONDARY;
                pr.getChildren().addAll(
                        tableCell(nameDisplay, 190, nameCol, isGem),
                        tableCell(p.getPosition().toString(), 50, TEXT_SECONDARY, false),
                        tableCell(String.valueOf(p.overallRating()), 50, accentColor, true),
                        tableCell(String.valueOf(p.getPotential()) + (isGem ? " ★" : ""), 50, potCol, isGem)
                );
                Button draftBtn = smallAccentButton(isGem ? "DRAFT 💎" : "DRAFT");
                if (isGem) draftBtn.setStyle("-fx-background-color: " + GOLD + "; -fx-text-fill: #0A0A00; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 3px; -fx-cursor: hand;");
                draftBtn.setOnAction(ev -> {
                    p.signContract(6); // full career contract
                    franchise.getUserTeam().addPlayer(p);
                    pool.remove(p);
                    picks.put(nextPick, p);
                    franchise.addNews("DRAFT: " + franchise.getUserTeam().getName() + " selects " + p.getName()
                            + " (" + p.getPosition() + ", OVR:" + p.overallRating() + ")");
                    updateNewsTicker();
                    buildDraftView(dm, order, pool, picks, takenPlayers, userPickNum);
                });
                HBox btnCell = new HBox(draftBtn);
                btnCell.setPrefWidth(80); btnCell.setAlignment(Pos.CENTER);
                pr.getChildren().add(btnCell);
                pickTable.getChildren().add(pr);
            }

            ScrollPane ps = new ScrollPane(pickTable);
            ps.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
            ps.setPrefViewportHeight(380);
            ps.setFitToWidth(true);
            pickCard.getChildren().add(ps);
            rightBox.getChildren().add(pickCard);

        } else {
            int r2 = (nextPick-1)/order.size()+1; int pir2=(nextPick-1)%order.size(); int pidx2=(r2%2==1)?pir2:(order.size()-1-pir2);
            VBox simCard = card("AI ON THE CLOCK: " + order.get(pidx2).getName().toUpperCase(), -1);

            Player aiPick = pool.stream()
                    .filter(Player::isGem).findFirst()
                    .orElseGet(() -> pool.stream()
                            .sorted(Comparator.comparingInt(Player::overallRating).reversed())
                            .findFirst().orElse(null));

            if (aiPick != null) {
                HBox preview = new HBox(12);
                preview.setAlignment(Pos.CENTER_LEFT);
                preview.setPadding(new Insets(8));
                preview.setStyle("-fx-background-color: " + BG_DARK + "; -fx-border-radius: 3px; -fx-background-radius: 3px;");
                preview.getChildren().addAll(
                        label("Top available:", 11, TEXT_DIM, "normal"),
                        label(aiPick.getName(), 13, TEXT_PRIMARY, "bold"),
                        label(aiPick.getPosition().toString(), 11, TEXT_SECONDARY, "normal"),
                        label("OVR " + aiPick.overallRating(), 13, accentColor, "bold")
                );
                simCard.getChildren().add(preview);
            }

            HBox simBtns = new HBox(10);
            Button nextPickBtn = accentButton("SIM NEXT PICK ▶", true);
            nextPickBtn.setOnAction(e -> {
                if (aiPick != null) {
                    int r = (nextPick - 1) / order.size() + 1;
                    int pir = (nextPick - 1) % order.size();
                    int pidx = (r % 2 == 1) ? pir : (order.size() - 1 - pir);
                    Team pickingTeam = order.get(pidx);
                    aiPick.signContract(6); // full career contract
                    pickingTeam.addPlayer(aiPick);
                    pool.remove(aiPick);
                    picks.put(nextPick, aiPick);
                    franchise.addNews("DRAFT: " + pickingTeam.getName() + " selects " + aiPick.getName()
                            + " (" + aiPick.getPosition() + ", OVR:" + aiPick.overallRating() + ")");
                    updateNewsTicker();
                }
                buildDraftView(dm, order, pool, picks, takenPlayers, userPickNum);
            });

            int picksToUser = userPickNum - nextPick;
            if (picksToUser > 1) {
                Button simToMyPickBtn = ghostButton("SIM TO MY PICK (#" + userPickNum + ")");
                simToMyPickBtn.setOnAction(e -> {
                    int curr = nextPick;
                    while (curr <= totalPicks && !pool.isEmpty() && !userTurn) {
                        int rr = (curr - 1) / order.size() + 1;
                        int pirr = (curr - 1) % order.size();
                        int pidxr = (rr % 2 == 1) ? pirr : (order.size() - 1 - pirr);
                        int uPickInRoundCurr = (rr % 2 == 0) ? (order.size() - userPickNum + 2) : userPickNum;
                        if (pirr + 1 == uPickInRoundCurr) break; // stop at user pick
                        Player best = pool.stream()
                                .filter(Player::isGem).findFirst()
                                .orElseGet(() -> pool.stream()
                                        .sorted(Comparator.comparingInt(Player::overallRating).reversed())
                                        .findFirst().orElse(null));
                        if (best == null) break;
                        Team pt = order.get(pidxr);
                        best.signContract(6);
                        pt.addPlayer(best);
                        pool.remove(best);
                        picks.put(curr, best);
                        franchise.addNews("DRAFT: " + pt.getName() + " selects " + best.getName());
                        curr++;
                    }
                    updateNewsTicker();
                    buildDraftView(dm, order, pool, picks, takenPlayers, userPickNum);
                });
                simBtns.getChildren().add(simToMyPickBtn);
            }
            simBtns.getChildren().add(nextPickBtn);
            simCard.getChildren().add(simBtns);
            rightBox.getChildren().add(simCard);

            VBox availCard = card("AVAILABLE PROSPECTS (" + pool.size() + " remaining)", -1);
            VBox availTable = new VBox(0);
            availTable.setStyle("-fx-background-color: " + BG_CARD + ";" +
                    "-fx-border-color: " + BORDER_COLOR + ";" +
                    "-fx-border-radius: 4px; -fx-background-radius: 4px;");
            HBox ah = tableRow(true, false);
            ah.getChildren().addAll(
                    tableCell("NAME", 180, TEXT_DIM, true),
                    tableCell("POS",   50, TEXT_DIM, false),
                    tableCell("OVR",   50, TEXT_DIM, true),
                    tableCell("POT",   50, TEXT_DIM, false)
            );
            availTable.getChildren().add(ah);
            pool.stream()
                    .sorted(Comparator.<Player, Boolean>comparing(p -> !p.isGem())
                            .thenComparingInt(p -> -p.overallRating()))
                    .limit(15).forEach(p -> {
                        boolean isGemP = p.isGem();
                        HBox ar = tableRow(false, false);
                        if (isGemP) ar.setStyle("-fx-background-color: #2A2000; -fx-border-color: " + GOLD + "; -fx-border-width: 1px; -fx-border-radius: 2px; -fx-background-radius: 2px;");
                        String nd = isGemP ? "💎 " + p.getName() : p.getName();
                        ar.getChildren().addAll(
                                tableCell(nd, 180, isGemP ? GOLD : TEXT_PRIMARY, isGemP),
                                tableCell(p.getPosition().toString(), 50, TEXT_SECONDARY, false),
                                tableCell(String.valueOf(p.overallRating()), 50, accentColor, true),
                                tableCell(String.valueOf(p.getPotential()) + (isGemP ? " ★" : ""), 50, isGemP ? GOLD : TEXT_SECONDARY, isGemP)
                        );
                        availTable.getChildren().add(ar);
                    });
            availCard.getChildren().add(availTable);
            rightBox.getChildren().add(availCard);
        }

        columns.getChildren().addAll(new VBox(logScroll), rightBox);
        page.getChildren().add(columns);

        ScrollPane outer = new ScrollPane(page);
        outer.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        outer.setFitToWidth(true);
        setContent(outer);
    }

    // ── Season History screen ─────────────────────────────────────────
    private void showHistory() {
        VBox page = new VBox(16);
        page.setPadding(new Insets(20));
        page.setStyle("-fx-background-color: " + BG_DARK + ";");
        page.getChildren().add(pageHeader("FRANCHISE HISTORY", franchise.getUserTeam().getName()));

        List<FranchiseMode.SeasonRecord> history = franchise.getSeasonHistory();

        if (history.isEmpty()) {
            VBox empty = card("NO HISTORY YET", -1);
            empty.getChildren().add(label("Complete your first season to see history here.", 13, TEXT_DIM, "normal"));
            page.getChildren().add(empty);
            setContent(page);
            return;
        }

        String userTeamName = franchise.getUserTeam().getName();
        long titles = history.stream().filter(r -> r.champion.equals(userTeamName)).count();
        long rsWins  = history.stream().filter(r -> r.regularSeasonLeader.equals(userTeamName)).count();

        HBox trophyRow = new HBox(20);
        trophyRow.setAlignment(Pos.CENTER);

        VBox titleBox = new VBox(4);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(16, 32, 16, 32));
        titleBox.setStyle("-fx-background-color: " + (titles > 0 ? GOLD + "18" : BG_CARD) + ";" +
                "-fx-border-color: " + (titles > 0 ? GOLD : BORDER_COLOR) + ";" +
                "-fx-border-radius: 6px; -fx-background-radius: 6px;");
        titleBox.getChildren().addAll(
                label(String.valueOf(titles), 40, titles > 0 ? GOLD : TEXT_DIM, "bold"),
                label("🏆 CHAMPIONSHIPS", 11, titles > 0 ? GOLD : TEXT_DIM, "bold")
        );

        VBox rsBox = new VBox(4);
        rsBox.setAlignment(Pos.CENTER);
        rsBox.setPadding(new Insets(16, 32, 16, 32));
        rsBox.setStyle("-fx-background-color: " + (rsWins > 0 ? accentColor + "18" : BG_CARD) + ";" +
                "-fx-border-color: " + (rsWins > 0 ? accentColor : BORDER_COLOR) + ";" +
                "-fx-border-radius: 6px; -fx-background-radius: 6px;");
        rsBox.getChildren().addAll(
                label(String.valueOf(rsWins), 40, rsWins > 0 ? accentColor : TEXT_DIM, "bold"),
                label("🥇 REGULAR SEASON TITLES", 11, rsWins > 0 ? accentColor : TEXT_DIM, "bold")
        );

        trophyRow.getChildren().addAll(titleBox, rsBox);
        page.getChildren().add(trophyRow);
        page.getChildren().add(label(history.size() + " SEASON" + (history.size() == 1 ? "" : "S") + " PLAYED", 11, TEXT_SECONDARY, "normal"));

        VBox histCard = card("SEASON RESULTS", -1);

        VBox table = new VBox(0);
        table.setStyle("-fx-background-color: " + BG_CARD + ";" +
                "-fx-border-color: " + BORDER_COLOR + ";" +
                "-fx-border-radius: 4px; -fx-background-radius: 4px;");

        HBox hdr = tableRow(true, false);
        hdr.getChildren().addAll(
                tableCell("SEASON",    90, TEXT_DIM, false),
                tableCell("🏆 CHAMPION", 200, TEXT_DIM, true),
                tableCell("REG. SEASON LEADER", 200, TEXT_DIM, false),
                tableCell("PTS", 60, TEXT_DIM, false)
        );
        table.getChildren().add(hdr);

        List<FranchiseMode.SeasonRecord> reversed = new ArrayList<>(history);
        Collections.reverse(reversed);

        for (FranchiseMode.SeasonRecord rec : reversed) {
            boolean userWonTitle = rec.champion.equals(userTeamName);
            boolean userWonRS    = rec.regularSeasonLeader.equals(userTeamName);
            HBox row = tableRow(false, userWonTitle || userWonRS);
            row.getChildren().addAll(
                    tableCell(rec.seasonLabel(), 90, TEXT_SECONDARY, false),
                    tableCell((userWonTitle ? "🏆 " : "") + rec.champion, 200,
                            userWonTitle ? GOLD : TEXT_PRIMARY, userWonTitle),
                    tableCell((userWonRS ? "🥇 " : "") + rec.regularSeasonLeader, 200,
                            userWonRS ? accentColor : TEXT_PRIMARY, userWonRS),
                    tableCell(String.valueOf(rec.leaderPoints), 60, TEXT_SECONDARY, false)
            );
            table.getChildren().add(row);
        }

        histCard.getChildren().add(table);
        page.getChildren().add(histCard);

        ScrollPane sp = new ScrollPane(page);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        sp.setFitToWidth(true);
        setContent(sp);
    }

    // ─────────────────────────────────────────────────────────────────
    // SIM HANDLERS
    // ─────────────────────────────────────────────────────────────────

    private void handleAdvanceDay() {
        List<String> events = franchise.advanceDay();
        updateTopBar();
        updateNewsTicker();
        showSimResultsAndRefresh(events);
    }

    private void handleSimToNextGame() {
        ScheduledGame played = franchise.simulateNextUserGame();
        updateTopBar();
        updateNewsTicker();
        if (played != null && played.isPlayed() && played.getResult() != null) {
            showGameResultScreen(played);
        } else {
            showDashboard();
        }
    }

    private void showGameResultScreen(ScheduledGame game) {
        GameResult r = game.getResult();
        boolean userIsHome = game.getHomeTeam().equals(franchise.getUserTeam());
        Team userSide  = franchise.getUserTeam();
        Team oppSide   = userIsHome ? game.getAwayTeam() : game.getHomeTeam();
        int  userScore = userIsHome ? r.getHomeScore() : r.getAwayScore();
        int  oppScore  = userIsHome ? r.getAwayScore()  : r.getHomeScore();
        boolean userWon = userScore > oppScore;
        String oppColor = TeamColors.getPrimary(oppSide.getName());

        String endLabel = r.isShootoutResult() ? "FINAL — SHOOTOUT"
                : r.isOvertimeResult() ? "FINAL — OVERTIME" : "FINAL";

        VBox page = new VBox(20);
        page.setPadding(new Insets(30, 40, 30, 40));
        page.setStyle("-fx-background-color: " + BG_DARK + ";");
        page.setAlignment(Pos.TOP_CENTER);

        VBox banner = new VBox(8);
        banner.setAlignment(Pos.CENTER);
        banner.setPadding(new Insets(24));
        String bannerBg = userWon ? accentColor + "15" : RED_LOSS + "10";
        String bannerBorder = userWon ? accentColor : RED_LOSS;
        banner.setStyle("-fx-background-color: " + bannerBg + ";" +
                "-fx-border-color: " + bannerBorder + ";" +
                "-fx-border-radius: 6px; -fx-background-radius: 6px;");

        Label endLbl = label(endLabel, 11, TEXT_DIM, "bold");
        endLbl.setStyle(endLbl.getStyle() + " -fx-letter-spacing: 4px;");
        Label outcomeLabel = label(userWon ? "VICTORY" : "DEFEAT", 42, bannerBorder, "bold");
        outcomeLabel.setStyle(outcomeLabel.getStyle() + " -fx-font-family: 'Courier New';");
        if (userWon) outcomeLabel.setEffect(new Glow(0.4));

        HBox scoreRow = new HBox(40);
        scoreRow.setAlignment(Pos.CENTER);

        VBox myScoreBox = new VBox(4);
        myScoreBox.setAlignment(Pos.CENTER);
        StackPane myLogo = teamLogoStack(userSide.getName(), accentColor, 32);
        Label myNameL  = label(userSide.getName(), 13, TEXT_SECONDARY, "normal");
        Label myScoreL = label(String.valueOf(userScore), 52, accentColor, "bold");
        myScoreBox.getChildren().addAll(myLogo, myNameL, myScoreL);

        Label vsDash = label("—", 32, TEXT_DIM, "normal");

        VBox oppScoreBox = new VBox(4);
        oppScoreBox.setAlignment(Pos.CENTER);
        StackPane oppLogo = teamLogoStack(oppSide.getName(), oppColor, 32);
        Label oppNameL  = label(oppSide.getName(), 13, TEXT_SECONDARY, "normal");
        Label oppScoreL = label(String.valueOf(oppScore), 52, TEXT_SECONDARY, "bold");
        oppScoreBox.getChildren().addAll(oppLogo, oppNameL, oppScoreL);

        scoreRow.getChildren().addAll(myScoreBox, vsDash, oppScoreBox);
        banner.getChildren().addAll(endLbl, outcomeLabel, scoreRow);

        VBox eventsCard = card("SCORING SUMMARY", -1);
        eventsCard.setMaxWidth(660);
        if (r.getEvents() == null || r.getEvents().isEmpty()) {
            eventsCard.getChildren().add(label("No scoring events recorded.", 12, TEXT_DIM, "normal"));
        } else {
            for (String ev : r.getEvents()) {
                if (ev == null || ev.isBlank()) continue;
                Label evL = new Label(ev.trim());
                evL.setWrapText(true);
                String evColor = ev.contains(userSide.getName()) ? accentColor
                        : ev.contains("PENALTY") ? GOLD
                        : ev.contains("INJURY")  ? RED_LOSS
                        : TEXT_SECONDARY;
                evL.setStyle("-fx-font-size: 12px; -fx-text-fill: " + evColor + "; -fx-padding: 3 0 3 0;");
                eventsCard.getChildren().add(evL);
            }
        }

        VBox starsCard = card("STARS OF THE GAME", -1);
        starsCard.setMaxWidth(660);
        String[] starLabels = {"1ST STAR", "2ND STAR", "3RD STAR"};
        Player[] stars = { r.getFirstStar(), r.getSecondStar(), r.getThirdStar() };
        boolean anyStars = false;
        for (int i = 0; i < 3; i++) {
            if (stars[i] == null) continue;
            anyStars = true;
            HBox starRow = new HBox(12);
            starRow.setAlignment(Pos.CENTER_LEFT);
            starRow.setPadding(new Insets(4, 0, 4, 0));
            String starColor = i == 0 ? GOLD : i == 1 ? TEXT_PRIMARY : TEXT_SECONDARY;
            Label num = label(starLabels[i], 10, starColor, "bold");
            num.setStyle(num.getStyle() + " -fx-letter-spacing: 2px;");
            num.setPrefWidth(65);
            Label name = label(stars[i].getName(), 14, starColor, "bold");
            String starTeam = franchise.getTeamOf(stars[i]);
            Label team = label(starTeam, 11, TEXT_DIM, "normal");
            VBox info = new VBox(1, name, team);
            starRow.getChildren().addAll(num, info);
            starsCard.getChildren().add(starRow);
        }
        if (!anyStars) starsCard.getChildren().add(label("No star data.", 12, TEXT_DIM, "normal"));

        HBox btnRow = new HBox(12);
        btnRow.setAlignment(Pos.CENTER);
        Button nextGameBtn = accentButton("NEXT GAME", true);
        nextGameBtn.setOnAction(e -> handleSimToNextGame());
        Button dashBtn = ghostButton("Dashboard");
        dashBtn.setOnAction(e -> showDashboard());
        Button schedBtn = ghostButton("My Schedule");
        schedBtn.setOnAction(e -> showSchedule());
        btnRow.getChildren().addAll(nextGameBtn, dashBtn, schedBtn);

        page.getChildren().addAll(banner, eventsCard, starsCard, btnRow);

        page.setOpacity(0);
        ScrollPane sp = new ScrollPane(page);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        sp.setFitToWidth(true);
        setContent(sp);
        FadeTransition ft = new FadeTransition(Duration.millis(300), page);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    private StackPane teamLogoStack(String teamName, String color, double radius) {
        return buildTeamLogoPane(teamName, color, radius);
    }

    private void handleSimToDeadline() {
        franchise.simulateToTradeDeadline();
        updateTopBar(); updateNewsTicker();
        showDashboard();
    }

    private void handleSimToEndSeason() {
        Stage confirm = new Stage();
        confirm.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        confirm.setTitle("Confirm Sim to End of Season");
        confirm.setWidth(380);
        VBox content = new VBox(16);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: " + BG_PANEL + ";");
        content.getChildren().add(label("SIM TO END OF SEASON?", 14, TEXT_PRIMARY, "bold"));
        content.getChildren().add(label("This will simulate all remaining regular season games.\nAre you sure?", 12, TEXT_SECONDARY, "normal"));
        HBox btns = new HBox(10);
        Button yesBtn = accentButton("YES, SIM TO END", true);
        yesBtn.setStyle(yesBtn.getStyle() + "-fx-background-color: " + RED_LOSS + ";");
        yesBtn.setOnAction(e -> {
            confirm.close();
            franchise.simulateToEndOfSeason();
            updateTopBar(); updateNewsTicker();
            showDashboard();
        });
        Button cancelBtn = ghostButton("Cancel");
        cancelBtn.setOnAction(e -> confirm.close());
        btns.getChildren().addAll(yesBtn, cancelBtn);
        content.getChildren().add(btns);
        confirm.setScene(new javafx.scene.Scene(content));
        confirm.show();
    }

    private void showSimResultsAndRefresh(List<String> events) {
        Stage popup = new Stage();
        popup.initModality(Modality.NONE);
        popup.setTitle("Simulation Results");
        popup.setX(200); popup.setY(100);

        VBox content = new VBox(4);
        content.setPadding(new Insets(16));
        content.setStyle("-fx-background-color: " + BG_PANEL + ";");
        content.setPrefWidth(400);
        content.setPrefHeight(300);

        ScrollPane sp = new ScrollPane();
        VBox evBox = new VBox(2);
        for (String e : events) {
            Label l = new Label(e);
            l.setWrapText(true);
            l.setStyle("-fx-font-size: 12px; -fx-text-fill: " + TEXT_SECONDARY + ";");
            evBox.getChildren().add(l);
        }
        sp.setContent(evBox);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        sp.setFitToWidth(true);
        sp.setPrefHeight(260);

        Button close = ghostButton("Close");
        close.setOnAction(e -> popup.close());

        content.getChildren().addAll(sp, close);
        popup.setScene(new Scene(content));
        popup.show();

        showDashboard();

        PauseTransition pt = new PauseTransition(Duration.seconds(6));
        pt.setOnFinished(e -> popup.close());
        pt.play();
    }

    private void handleSave() {
        try {
            SaveManager.save(franchise);
            showResultDialog("SAVED", "Game saved successfully.", GREEN_WIN);
        } catch (Exception e) {
            showResultDialog("SAVE FAILED", e.getMessage(), RED_LOSS);
        }
    }

    private void confirmRestartFranchise() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Restart Franchise");

        VBox content = new VBox(16);
        content.setPadding(new Insets(28));
        content.setStyle("-fx-background-color: " + BG_PANEL + ";" +
                "-fx-border-color: " + RED_LOSS + ";" +
                "-fx-border-width: 0 0 0 4;");
        content.setPrefWidth(420);

        Label hdr = label("RESTART FRANCHISE", 16, RED_LOSS, "bold");
        Label msg = new Label("This will erase your current save and return you to the main menu. Are you sure?");
        msg.setWrapText(true);
        msg.setStyle("-fx-font-size: 13px; -fx-text-fill: " + TEXT_SECONDARY + ";");

        HBox btns = new HBox(10);
        Button confirmBtn = new Button("YES, RESTART");
        confirmBtn.setStyle("-fx-background-color: " + RED_LOSS + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 3px; -fx-cursor: hand;");
        confirmBtn.setOnAction(e -> {
            dialog.close();
            try {
                java.io.File saveFile = new java.io.File("franchise_save.dat");
                if (saveFile.exists()) saveFile.delete();
            } catch (Exception ignored) {}
            franchise = null;
            Stage stage = (Stage) root.getScene().getWindow();
            showSplashScreen(stage);
        });
        Button cancelBtn = ghostButton("Cancel");
        cancelBtn.setOnAction(e -> dialog.close());
        btns.getChildren().addAll(confirmBtn, cancelBtn);

        content.getChildren().addAll(hdr, msg, btns);
        dialog.setScene(new Scene(content));
        dialog.show();
    }

    // ─────────────────────────────────────────────────────────────────
    // UTILITY — UI Builders
    // ─────────────────────────────────────────────────────────────────

    private void setContent(Node node) {
        // Stretch the node to fill all available space in the center pane
        if (node instanceof ScrollPane sp) {
            sp.setFitToWidth(true);
            sp.setFitToHeight(true);
            StackPane.setAlignment(sp, Pos.TOP_LEFT);
        }
        StackPane.setAlignment(node, Pos.TOP_LEFT);
        mainContent.getChildren().setAll(node);
        FadeTransition ft = new FadeTransition(Duration.millis(180), node);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    private Label label(String text, double size, String color, String weight) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: " + size + "px; -fx-text-fill: " + color + ";" +
                (weight.equals("bold") ? " -fx-font-weight: bold;" : ""));
        return l;
    }

    private VBox card(String title, double width) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(16, 16, 16, 16));
        card.setStyle("-fx-background-color: " + BG_CARD + ";" +
                "-fx-border-color: " + BORDER_COLOR + ";" +
                "-fx-border-radius: 4px; -fx-background-radius: 4px;");
        if (width > 0) card.setPrefWidth(width);

        if (!title.isEmpty()) {
            Label hdr = label(title, 10, TEXT_DIM, "bold");
            hdr.setStyle(hdr.getStyle() + " -fx-letter-spacing: 2px;");
            card.getChildren().add(hdr);
            card.getChildren().add(divider());
        }
        return card;
    }

    private HBox tableRow(boolean isHeader, boolean isHighlighted) {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(0, 8, 0, 8));
        row.setPrefHeight(isHeader ? 34 : 40);
        String normalBg = isHeader ? BG_DARK : isHighlighted ? accentColor + "12" : "transparent";
        String borderStyle = isHeader ? "" : "-fx-border-color: transparent transparent " + BORDER_COLOR + " transparent; -fx-border-width: 0 0 1 0;";
        row.setStyle("-fx-background-color: " + normalBg + ";" + borderStyle);
        if (!isHeader) {
            row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: " + BG_HOVER + ";" + borderStyle));
            row.setOnMouseExited(e -> row.setStyle("-fx-background-color: " + normalBg + ";" + borderStyle));
        }
        return row;
    }

    private Label tableCell(String text, double width, String color, boolean bold) {
        Label l = new Label(text);
        l.setPrefWidth(width);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: " + color + ";" +
                (bold ? " -fx-font-weight: bold;" : "") +
                " -fx-padding: 0 4 0 4;");
        l.setEllipsisString("…");
        return l;
    }

    private Region divider() {
        Region r = new Region(); r.setPrefHeight(1);
        r.setStyle("-fx-background-color: " + BORDER_COLOR + ";");
        VBox.setMargin(r, new Insets(4, 0, 4, 0));
        return r;
    }

    private Label sectionHeader(String text, String color) {
        Label l = label(text, 11, color, "bold");
        l.setStyle(l.getStyle() + " -fx-letter-spacing: 3px; -fx-padding: 8 0 4 0;");
        return l;
    }

    private Node pageHeader(String title, String subtitle) {
        VBox hdr = new VBox(4);
        hdr.setPadding(new Insets(0, 0, 8, 0));
        hdr.setStyle("-fx-border-color: transparent transparent " + BORDER_COLOR + " transparent; -fx-border-width: 0 0 1 0;");

        Label tl = label(title, 22, TEXT_PRIMARY, "bold");
        tl.setStyle(tl.getStyle() + " -fx-font-family: 'Courier New';");

        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        Rectangle accent = new Rectangle(3, 26);
        accent.setFill(Color.web(accentColor));
        accent.setEffect(new Glow(0.5));
        row.getChildren().addAll(accent, tl);

        hdr.getChildren().add(row);
        if (!subtitle.isEmpty()) hdr.getChildren().add(label(subtitle, 12, TEXT_DIM, "normal"));
        return hdr;
    }

    private Button accentButton(String text, boolean filled) {
        Button b = new Button(text);
        if (filled) {
            String normalStyle = "-fx-background-color: " + accentColor + "; -fx-text-fill: " + BG_DARK + "; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 8 18 8 18; -fx-background-radius: 3px; -fx-cursor: hand;";
            String hoverStyle  = "-fx-background-color: " + accent2Color + "; -fx-text-fill: " + BG_DARK + "; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 8 18 8 18; -fx-background-radius: 3px; -fx-cursor: hand;";
            b.setStyle(normalStyle);
            b.setOnMouseEntered(e -> b.setStyle(hoverStyle));
            b.setOnMouseExited(e -> b.setStyle(normalStyle));
        } else {
            String normalStyle = "-fx-background-color: transparent; -fx-text-fill: " + accentColor + "; -fx-font-size: 12px; -fx-padding: 8 18 8 18; -fx-border-color: " + accentColor + "; -fx-border-radius: 3px; -fx-background-radius: 3px; -fx-cursor: hand;";
            String hoverStyle  = "-fx-background-color: " + accentColor + "18; -fx-text-fill: " + accentColor + "; -fx-font-size: 12px; -fx-padding: 8 18 8 18; -fx-border-color: " + accentColor + "; -fx-border-radius: 3px; -fx-background-radius: 3px; -fx-cursor: hand;";
            b.setStyle(normalStyle);
            b.setOnMouseEntered(e -> b.setStyle(hoverStyle));
            b.setOnMouseExited(e -> b.setStyle(normalStyle));
        }
        return b;
    }

    private Button ghostButton(String text) {
        Button b = new Button(text);
        String normalStyle = "-fx-background-color: transparent; -fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 12px; -fx-padding: 8 16 8 16; -fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 3px; -fx-background-radius: 3px; -fx-cursor: hand;";
        String hoverStyle  = "-fx-background-color: transparent; -fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: 12px; -fx-padding: 8 16 8 16; -fx-border-color: " + TEXT_SECONDARY + "; -fx-border-radius: 3px; -fx-background-radius: 3px; -fx-cursor: hand;";
        b.setStyle(normalStyle);
        b.setOnMouseEntered(e -> b.setStyle(hoverStyle));
        b.setOnMouseExited(e -> b.setStyle(normalStyle));
        return b;
    }

    private Button smallAccentButton(String text) {
        Button b = new Button(text);
        String normalStyle = "-fx-background-color: " + accentColor + "; -fx-text-fill: " + BG_DARK + "; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 10 4 10; -fx-background-radius: 3px; -fx-cursor: hand;";
        String hoverStyle  = "-fx-background-color: " + accent2Color + "; -fx-text-fill: " + BG_DARK + "; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 10 4 10; -fx-background-radius: 3px; -fx-cursor: hand;";
        b.setStyle(normalStyle);
        b.setOnMouseEntered(e -> b.setStyle(hoverStyle));
        b.setOnMouseExited(e -> b.setStyle(normalStyle));
        return b;
    }

    private VBox bigStat(String value, String label, String color) {
        VBox box = new VBox(2);
        box.setAlignment(Pos.CENTER);
        box.getChildren().addAll(
                label(value, 28, color, "bold"),
                label(label, 10, TEXT_DIM, "normal")
        );
        return box;
    }

    private HBox bigStatRow(String label, String value, String color) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(3, 0, 3, 0));
        Label lbl = label(label, 12, TEXT_SECONDARY, "normal");
        lbl.setPrefWidth(70);
        row.getChildren().addAll(lbl, label(value, 14, color, "bold"));
        return row;
    }

    private StackPane buildProgressBar(double pct, String color) {
        StackPane container = new StackPane();
        container.setAlignment(Pos.CENTER_LEFT);
        container.setMaxHeight(8);
        Rectangle bg = new Rectangle(0, 8);
        bg.setFill(Color.web(BORDER_COLOR));
        bg.widthProperty().bind(container.widthProperty());
        Rectangle fg = new Rectangle(0, 8);
        fg.setFill(Color.web(color));
        fg.setEffect(new Glow(0.3));
        fg.widthProperty().bind(container.widthProperty().multiply(pct));
        container.getChildren().addAll(bg, fg);
        VBox.setMargin(container, new Insets(4, 0, 4, 0));
        return container;
    }

    private String moralColor(int morale) {
        if (morale >= 80) return GREEN_WIN;
        if (morale >= 60) return TEXT_SECONDARY;
        return RED_LOSS;
    }

    private void showResultDialog(String title, String message, String color) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(title);

        VBox content = new VBox(12);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: " + BG_PANEL + ";" +
                "-fx-border-color: " + color + ";" +
                "-fx-border-width: 0 0 0 4;");
        content.setPrefWidth(420);

        Label hdr = label(title, 16, color, "bold");
        Label msg = new Label(message);
        msg.setWrapText(true);
        msg.setStyle("-fx-font-size: 13px; -fx-text-fill: " + TEXT_SECONDARY + ";");

        Button ok = accentButton("OK", true);
        ok.setOnAction(e -> dialog.close());

        content.getChildren().addAll(hdr, msg, ok);
        dialog.setScene(new Scene(content));
        dialog.show();
    }

    private String buildCSS() { return ""; }

    // ─────────────────────────────────────────────────────────────────
    // Team list builder
    // ─────────────────────────────────────────────────────────────────

    private List<Team> buildTeamList() {
        List<Team> t = new ArrayList<>();
        t.add(new Team("North Hawks"));    t.add(new Team("East Kings"));
        t.add(new Team("South Wolves"));   t.add(new Team("West Falcons"));
        t.add(new Team("Central Bears"));  t.add(new Team("River Knights"));
        t.add(new Team("Metro Rangers"));  t.add(new Team("Coastal Storm"));
        t.add(new Team("Prairie Giants")); t.add(new Team("Valley Titans"));
        t.add(new Team("Forest Blades"));  t.add(new Team("Mountain Lions"));
        return t;
    }
}