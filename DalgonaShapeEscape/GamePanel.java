import javax.swing.*;
import javax.imageio.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

public class GamePanel extends JPanel implements MouseListener, ActionListener {

    // ========== STATE MACHINE ==========
    private GameState gameState = GameState.SHAPE_SELECTION;

    // ========== LEVEL DATA ==========
    private int currentLevel = 1;
    private final int MAX_LEVELS = 4;
    private final ShapeType[][] levelShapes = {
            {ShapeType.CIRCLE, ShapeType.TRIANGLE},
            {ShapeType.SQUARE, ShapeType.HEXAGON},
            {ShapeType.HEART, ShapeType.STAR},
            {ShapeType.UMBRELLA, ShapeType.SQUID}
    };

    // ========== SELECTION STATE ==========
    private int shapesSelected = 0;
    private final int SHAPES_TO_SELECT = 2;
    private boolean[] selectedShapes = new boolean[2];
    private int[] selectionOrder = new int[2];
    private Rectangle[] shapeButtons = new Rectangle[2];

    // ========== GAMEPLAY STATE ==========
    private ShapeType currentShape;
    private int currentShapeIndex = 0;
    private int shapesCompleted = 0;
    private int score = 0;

    // ========== CANDY / SHAPE ==========
    private final int CANDY_RADIUS = 150;
    private Path2D.Double centralShape;
    private ArrayList<Fragment> fragments = new ArrayList<>();

    // ========== TIMER ==========
    private int timeRemaining = 50;
    private Timer gameTimer;
    private boolean timerRunning = false;

    // ========== PHYSICS ==========
    private final float GRAVITY = 0.5f;
    private Timer physicsTimer;

    // ========== UI BUTTONS (Gameplay) ==========
    private Rectangle pauseButton;
    private Rectangle resumeButton;
    private Rectangle restartButton;
    private Rectangle exitButton;

    // ========== POPUP BUTTONS ==========
    private Rectangle popupButton1;  // Primary button (Continue / Go to Next Level / Restart / Play Again)
    private Rectangle popupButton2;  // Secondary button (Exit)

    // ========== IMAGES ==========
    private BufferedImage backgroundImage;
    private BufferedImage passPinkImage;
    private BufferedImage victoryPinkImage;
    private BufferedImage gameOverImage;
    private BufferedImage finalImage;

    // ========== CONSTRUCTOR ==========
    public GamePanel() {
        setPreferredSize(new Dimension(900, 700));
        setBackground(new Color(20, 20, 30));
        addMouseListener(this);

        // Shape selection button positions
        shapeButtons[0] = new Rectangle(250, 350, 120, 120);
        shapeButtons[1] = new Rectangle(530, 350, 120, 120);

        selectionOrder[0] = -1;
        selectionOrder[1] = -1;

        // Load images
        loadImages();

        // Physics timer for fragment movement
        physicsTimer = new Timer(16, e -> {
            if (gameState == GameState.PLAYING) {
                boolean needsRepaint = false;
                for (Fragment fragment : fragments) {
                    if (fragment.broken) {
                        fragment.update(GRAVITY);
                        needsRepaint = true;
                    }
                }
                if (needsRepaint) {
                    repaint();
                }
                checkWinCondition();
            }
        });
        physicsTimer.start();

        // Game countdown timer (created but NOT started until gameplay begins)
        gameTimer = new Timer(1000, e -> {
            if (gameState == GameState.PLAYING && timerRunning) {
                timeRemaining--;
                if (timeRemaining <= 0) {
                    timeRemaining = 0;
                    triggerGameOver();
                }
                repaint();
            }
        });
    }

    // ========== IMAGE LOADING ==========
    private void loadImages() {
        try {
            // Get the directory where the class files / source are
            String basePath = System.getProperty("user.dir") + File.separator;

            File bgFile = findImageFile("background");
            if (bgFile != null) backgroundImage = ImageIO.read(bgFile);

            File passFile = findImageFile("passpink");
            if (passFile != null) passPinkImage = ImageIO.read(passFile);

            File victoryFile = findImageFile("victorypink");
            if (victoryFile != null) victoryPinkImage = ImageIO.read(victoryFile);

            File goFile = findImageFile("gameover");
            if (goFile != null) gameOverImage = ImageIO.read(goFile);

            File finalFile = findImageFile("final");
            if (finalFile != null) finalImage = ImageIO.read(finalFile);

        } catch (Exception e) {
            System.err.println("Error loading images: " + e.getMessage());
        }
    }

    private File findImageFile(String baseName) {
        String[] extensions = {".jpg", ".jpeg", ".png", ".gif"};
        // Try current directory first
        String dir = System.getProperty("user.dir");
        for (String ext : extensions) {
            File f = new File(dir + File.separator + baseName + ext);
            if (f.exists()) return f;
        }
        // Try source directory
        try {
            String classPath = getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            File classDir = new File(classPath).getParentFile();
            if (classDir != null) {
                for (String ext : extensions) {
                    File f = new File(classDir, baseName + ext);
                    if (f.exists()) return f;
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    // ========== TIMER MANAGEMENT ==========
    private void startLevelTimer() {
        stopTimer();
        timerRunning = true;
        gameTimer.start();
    }

    private void pauseTimer() {
        timerRunning = false;
    }

    private void resumeTimer() {
        timerRunning = true;
        if (!gameTimer.isRunning()) {
            gameTimer.start();
        }
    }

    private void stopTimer() {
        timerRunning = false;
        if (gameTimer != null) {
            gameTimer.stop();
        }
    }

    // ========== SHAPE CREATION ==========
    private void createCentralShape() {
        int shapeSize = 125;
        centralShape = ShapeFactory.createCentralShape(currentShape, shapeSize);
    }

    private void generateFragments() {
        fragments = ShapeFactory.generateFragments(CANDY_RADIUS, centralShape);
    }

    // ========== WIN CONDITION ==========
    private void checkWinCondition() {
        if (gameState != GameState.PLAYING) return;

        boolean allBroken = true;
        for (Fragment fragment : fragments) {
            if (!fragment.broken) {
                allBroken = false;
                break;
            }
        }

        if (allBroken && fragments.size() > 0) {
            // Current shape completed
            shapesCompleted++;
            score += 50;

            if (shapesCompleted < SHAPES_TO_SELECT) {
                // Show shape success popup after completing one shape
                pauseTimer();
                gameState = GameState.SUCCESS_POPUP;
                repaint();
            } else {
                // Both shapes completed (level complete) — show level victory directly
                stopTimer();
                if (currentLevel < MAX_LEVELS) {
                    gameState = GameState.LEVEL_VICTORY;
                } else {
                    gameState = GameState.FINAL_CONGRATULATIONS;
                }
                repaint();
            }
        }
    }

    // ========== GAME OVER ==========
    private void triggerGameOver() {
        stopTimer();
        gameState = GameState.GAME_OVER;
        repaint();
    }

    // ========== STATE TRANSITIONS ==========
    private void onSuccessContinue() {
        if (shapesCompleted < SHAPES_TO_SELECT) {
            // Load next shape in this level
            currentShapeIndex++;
            if (currentShapeIndex < selectionOrder.length && selectionOrder[currentShapeIndex] != -1) {
                int nextShapeIndex = selectionOrder[currentShapeIndex];
                currentShape = levelShapes[currentLevel - 1][nextShapeIndex];
                createCentralShape();
                generateFragments();
                gameState = GameState.PLAYING;
                resumeTimer();
                repaint();
            }
        } else {
            // Both shapes completed for this level
            stopTimer();
            if (currentLevel < MAX_LEVELS) {
                gameState = GameState.LEVEL_VICTORY;
            } else {
                gameState = GameState.FINAL_CONGRATULATIONS;
            }
            repaint();
        }
    }

    private void onGoToNextLevel() {
        currentLevel++;
        resetForNewLevel();
        gameState = GameState.SHAPE_SELECTION;
        repaint();
    }

    private void resetForNewLevel() {
        shapesSelected = 0;
        shapesCompleted = 0;
        selectedShapes[0] = false;
        selectedShapes[1] = false;
        selectionOrder[0] = -1;
        selectionOrder[1] = -1;
        currentShapeIndex = 0;
        timeRemaining = 50;
        fragments.clear();
    }

    private void resetLevel() {
        stopTimer();
        shapesSelected = 0;
        shapesCompleted = 0;
        selectedShapes[0] = false;
        selectedShapes[1] = false;
        selectionOrder[0] = -1;
        selectionOrder[1] = -1;
        currentShapeIndex = 0;
        timeRemaining = 50;
        fragments.clear();
        gameState = GameState.SHAPE_SELECTION;
        repaint();
    }

    private void resetGame() {
        stopTimer();
        currentLevel = 1;
        score = 0;
        resetForNewLevel();
        gameState = GameState.SHAPE_SELECTION;
        repaint();
    }

    // ========== PAINTING ==========
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        switch (gameState) {
            case SHAPE_SELECTION:
                GameRenderer.drawBackground(g2d, w, h, backgroundImage);
                GameRenderer.drawShapeSelection(g2d, w, h, currentLevel, 50,
                        shapesSelected, levelShapes, selectedShapes, shapeButtons, selectionOrder);
                break;

            case PLAYING:
                GameRenderer.drawBackground(g2d, w, h, backgroundImage);
                GameRenderer.drawGameScreen(g2d, w, h, currentLevel, timeRemaining, score,
                        fragments, centralShape, CANDY_RADIUS, currentShape);
                Rectangle[] buttons = GameRenderer.drawGameplayButtons(g2d, w);
                pauseButton = buttons[0];
                resumeButton = buttons[1];
                restartButton = buttons[2];
                exitButton = buttons[3];
                break;

            case PAUSED:
                GameRenderer.drawBackground(g2d, w, h, backgroundImage);
                GameRenderer.drawGameScreen(g2d, w, h, currentLevel, timeRemaining, score,
                        fragments, centralShape, CANDY_RADIUS, currentShape);
                buttons = GameRenderer.drawGameplayButtons(g2d, w);
                pauseButton = buttons[0];
                resumeButton = buttons[1];
                restartButton = buttons[2];
                exitButton = buttons[3];
                GameRenderer.drawPauseOverlay(g2d, w, h);
                break;

            case SUCCESS_POPUP:
                GameRenderer.drawBackground(g2d, w, h, backgroundImage);
                GameRenderer.drawGameScreen(g2d, w, h, currentLevel, timeRemaining, score,
                        fragments, centralShape, CANDY_RADIUS, currentShape);
                popupButton1 = GameRenderer.drawSuccessPopup(g2d, w, h, passPinkImage,
                        currentShape, shapesCompleted, SHAPES_TO_SELECT);
                break;

            case LEVEL_VICTORY:
                GameRenderer.drawBackground(g2d, w, h, backgroundImage);
                Rectangle[] victoryButtons = GameRenderer.drawLevelVictoryPopup(g2d, w, h,
                        victoryPinkImage, currentLevel);
                popupButton1 = victoryButtons[0];
                popupButton2 = victoryButtons[1];
                break;

            case GAME_OVER:
                GameRenderer.drawBackground(g2d, w, h, backgroundImage);
                GameRenderer.drawGameScreen(g2d, w, h, currentLevel, timeRemaining, score,
                        fragments, centralShape, CANDY_RADIUS, currentShape);
                popupButton1 = GameRenderer.drawGameOverPopup(g2d, w, h, gameOverImage);
                break;

            case FINAL_CONGRATULATIONS:
                Rectangle[] finalButtons = GameRenderer.drawFinalCongratulations(g2d, w, h,
                        finalImage, score);
                popupButton1 = finalButtons[0];
                popupButton2 = finalButtons[1];
                break;
        }
    }

    // ========== CLICK HANDLING ==========
    @Override
    public void mouseClicked(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        switch (gameState) {
            case SHAPE_SELECTION:
                handleShapeSelectionClick(x, y);
                break;

            case PLAYING:
                // Check gameplay buttons first
                if (pauseButton != null && pauseButton.contains(x, y)) {
                    pauseTimer();
                    gameState = GameState.PAUSED;
                    repaint();
                    return;
                }
                if (restartButton != null && restartButton.contains(x, y)) {
                    resetLevel();
                    return;
                }
                if (exitButton != null && exitButton.contains(x, y)) {
                    stopTimer();
                    if (physicsTimer != null) physicsTimer.stop();
                    System.exit(0);
                    return;
                }
                handleGamePlayClick(x, y);
                break;

            case PAUSED:
                if (resumeButton != null && resumeButton.contains(x, y)) {
                    gameState = GameState.PLAYING;
                    resumeTimer();
                    repaint();
                    return;
                }
                if (restartButton != null && restartButton.contains(x, y)) {
                    resetLevel();
                    return;
                }
                if (exitButton != null && exitButton.contains(x, y)) {
                    stopTimer();
                    if (physicsTimer != null) physicsTimer.stop();
                    System.exit(0);
                    return;
                }
                // No gameplay interaction while paused
                break;

            case SUCCESS_POPUP:
                if (popupButton1 != null && popupButton1.contains(x, y)) {
                    onSuccessContinue();
                }
                // No gameplay interaction during popup
                break;

            case LEVEL_VICTORY:
                if (popupButton1 != null && popupButton1.contains(x, y)) {
                    onGoToNextLevel();
                }
                if (popupButton2 != null && popupButton2.contains(x, y)) {
                    stopTimer();
                    if (physicsTimer != null) physicsTimer.stop();
                    System.exit(0);
                }
                break;

            case GAME_OVER:
                if (popupButton1 != null && popupButton1.contains(x, y)) {
                    resetLevel();
                }
                break;

            case FINAL_CONGRATULATIONS:
                if (popupButton1 != null && popupButton1.contains(x, y)) {
                    resetGame();
                }
                if (popupButton2 != null && popupButton2.contains(x, y)) {
                    stopTimer();
                    if (physicsTimer != null) physicsTimer.stop();
                    System.exit(0);
                }
                break;
        }
    }

    private void handleShapeSelectionClick(int x, int y) {
        // Check shape selection circles
        for (int i = 0; i < 2; i++) {
            int centerX = shapeButtons[i].x + shapeButtons[i].width / 2;
            int centerY = shapeButtons[i].y + shapeButtons[i].height / 2;

            if (Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2) <= Math.pow(65, 2)) {
                if (selectedShapes[i]) {
                    // Deselect
                    selectedShapes[i] = false;
                    shapesSelected--;
                    for (int j = 0; j < selectionOrder.length; j++) {
                        if (selectionOrder[j] == i) {
                            for (int k = j; k < selectionOrder.length - 1; k++) {
                                selectionOrder[k] = selectionOrder[k + 1];
                            }
                            selectionOrder[selectionOrder.length - 1] = -1;
                            break;
                        }
                    }
                } else if (shapesSelected < SHAPES_TO_SELECT) {
                    // Select
                    selectedShapes[i] = true;
                    selectionOrder[shapesSelected] = i;
                    shapesSelected++;
                }
                repaint();
                return;
            }
        }

        // Check Start button
        if (shapesSelected == 2) {
            Rectangle startBtn = GameRenderer.getStartButtonRect(getWidth());
            if (startBtn.contains(x, y)) {
                startGame();
            }
        }
    }

    private void startGame() {
        currentShapeIndex = 0;
        shapesCompleted = 0;
        int firstShapeIndex = selectionOrder[currentShapeIndex];
        currentShape = levelShapes[currentLevel - 1][firstShapeIndex];

        createCentralShape();
        generateFragments();
        timeRemaining = 50;
        gameState = GameState.PLAYING;
        startLevelTimer();
        repaint();
    }

    private void handleGamePlayClick(int x, int y) {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2 + 20; // Must match the translate offset in GameRenderer.drawGameScreen
        int centeredX = x - centerX;
        int centeredY = y - centerY;

        // Check if clicking on central protected shape → GAME OVER
        if (centralShape != null && centralShape.contains(centeredX, centeredY)) {
            triggerGameOver();
            return;
        }

        // Check if clicking on any unbroken fragment with tolerance radius
        int tolerance = 8;
        for (Fragment fragment : fragments) {
            if (!fragment.broken) {
                // Check exact point first, then check tolerance area
                boolean hit = fragment.shape.contains(centeredX, centeredY);
                if (!hit) {
                    // Check nearby points for thin fragments
                    for (int dx = -tolerance; dx <= tolerance && !hit; dx += 4) {
                        for (int dy = -tolerance; dy <= tolerance && !hit; dy += 4) {
                            if (fragment.shape.contains(centeredX + dx, centeredY + dy)) {
                                hit = true;
                            }
                        }
                    }
                }
                if (hit) {
                    fragment.broken = true;
                    fragment.initPhysics();
                    score += 10;
                    repaint();
                    break;
                }
            }
        }
    }

    // ========== UNUSED MOUSE EVENTS ==========
    @Override
    public void actionPerformed(ActionEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
}
