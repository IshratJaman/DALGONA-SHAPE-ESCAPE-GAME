import javax.swing.*;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class GamePanel extends JPanel implements MouseListener, ActionListener {

    private GameState gameState = GameState.STARTING_SCREEN;
    private int currentLevel = 1;
    private int maxLevels = 4;
    private int shapesSelected = 0;
    private int shapesCompleted = 0;
    private int shapesToSelect = 2;

    private ShapeType[][] levelShapes = {
            {ShapeType.CIRCLE, ShapeType.TRIANGLE},
            {ShapeType.SQUARE, ShapeType.DIAMOND},
            {ShapeType.HEART, ShapeType.STAR},
            {ShapeType.UMBRELLA, ShapeType.CRESCENT_MOON}
    };

    private boolean[] selectedShapes = new boolean[2];
    private int[] selectionOrder = new int[2];
    private ShapeType currentShape;
    private int currentShapeIndex = 0;

    private int[] levelTimes = {50, 48, 45, 40};
    private int timeRemaining = 50;
    private Timer gameTimer;

    private final int CANDY_RADIUS = 180;
    private Path2D.Double centralShape;
    private ArrayList<Fragment> fragments = new ArrayList<>();

    private final float GRAVITY = 0.5f;
    private Timer physicsTimer;

    private Rectangle[] shapeButtons = new Rectangle[2];
    private int score = 0;

    private Clip backgroundMusic;
    private Clip crackSound;
    private Clip gameOverSound;

    private Timer startingTimer;
    private JFrame GameOver;
    private boolean GameOverOccurred = false;

    private boolean allFragmentsAreBroken = false;

    private BufferedImage shapeSuccessImage;
    private BufferedImage levelCompleteImage;
    private BufferedImage GameOverImage;
    private static BufferedImage bgImage;

    private Rectangle GoToNextLevelButton = new Rectangle(275, 450, 150, 50);
    private Rectangle ExitButton = new Rectangle(450, 450, 100, 50);

    public GamePanel() {
        setPreferredSize(new Dimension(800, 600));
        setBackground(new Color(20, 20, 30));
        addMouseListener(this);

        shapeButtons[0] = new Rectangle(220, 320, 160, 160);
        shapeButtons[1] = new Rectangle(420, 320, 160, 160);

        resetSelectionState();
        initializeAudio();
        loadImages();

        startingTimer = new Timer(5000, e -> {
            gameState = GameState.MENU;
            repaint();
            ((Timer)e.getSource()).stop();
        });
        startingTimer.setRepeats(false);
        startingTimer.start();

        SwingUtilities.invokeLater(() -> playBackgroundMusic());

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

        // Manages the countdown timer
        gameTimer = new Timer(1000, e -> {
            if (gameState == GameState.PLAYING) {
                timeRemaining--;
                if (timeRemaining <= 0) {
                    timeRemaining = 0;
                    gameState = GameState.GAME_OVER;
                    GameOverOccurred = true;
                    showGameOverDialog();
                    playGameOverSound();
                }
                repaint();
            }
        });
        gameTimer.start();
    }

    private void loadImages() {
        try {
            File shapeImg = new File("C:\\Users\\ishra\\IdeaProjects\\Dalgona Shape Escape SPL FINAL\\pookie animated guard 2.jpg");
            File levelImg = new File("C:\\Users\\ishra\\IdeaProjects\\Dalgona Shape Escape SPL FINAL\\pookie invation guard.jpeg");
            File gameOverImg = new File("C:\\Users\\ishra\\IdeaProjects\\Dalgona Shape Escape SPL FINAL\\square guard game over.png");
            File bgImg = new File("C:\\Users\\ishra\\IdeaProjects\\Dalgona Shape Escape SPL FINAL\\s game bg.jpg");

            if (shapeImg.exists()) {
                shapeSuccessImage = ImageIO.read(shapeImg);
            }
            if (levelImg.exists()) {
                levelCompleteImage = ImageIO.read(levelImg);
            }
            if (gameOverImg.exists()) {
                GameOverImage = ImageIO.read(gameOverImg);
            }
            if (bgImg.exists()) {
                bgImage = ImageIO.read(bgImg);
            }
        }
        catch (Exception e) {
            System.err.println("Error!" + e.getMessage());
        }
    }

    private void resetSelectionState() {
        shapesSelected = 0;
        selectedShapes[0] = false;
        selectedShapes[1] = false;
        selectionOrder[0] = -1;
        selectionOrder[1] = -1;
    }

    private void initializeAudio() {
        try {
            backgroundMusic = loadAudioClip("C:\\Users\\ishra\\IdeaProjects\\Dalgona Shape Escape SPL FINAL\\src\\Dalgona-Background-Music.wav");
            crackSound = loadAudioClip("C:\\Users\\ishra\\IdeaProjects\\Dalgona Shape Escape SPL FINAL\\Ice-Sound-Effects.wav");
            gameOverSound = loadAudioClip("C:\\Users\\ishra\\IdeaProjects\\Dalgona Shape Escape SPL FINAL\\Game-Over-sound-effect.wav");
        }
        catch (Exception e) {
            System.err.println("Error!" + e.getMessage());
        }
    }

    private Clip loadAudioClip(String filename) {
        try {
            File audioFile = new File(filename);
            if (audioFile.exists()) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                return clip;
            }
        }
        catch (Exception e) {
            System.err.println("Error!" + filename);
        }
        return null;
    }

    private void playBackgroundMusic() {
        if (backgroundMusic != null) {
            try {
                backgroundMusic.setFramePosition(0);
                backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
                FloatControl volumeControl = (FloatControl) backgroundMusic.getControl(FloatControl.Type.MASTER_GAIN);
                volumeControl.setValue(-10.0f);
            }
            catch (Exception e) {
                System.err.println("Error!");
            }
        }
    }

    private void playCrackSound() {
        if (crackSound != null) {
            try {
                if (crackSound.isRunning()) {
                    crackSound.stop();
                }
                crackSound.setFramePosition(0);
                crackSound.start();
            }
            catch (Exception e) {
                System.err.println("Error!");
            }
        }
    }

    private void playGameOverSound() {
        if (gameOverSound != null) {
            try {
                if (backgroundMusic != null && backgroundMusic.isRunning()) {
                    backgroundMusic.stop();
                }
                gameOverSound.setFramePosition(0);
                gameOverSound.start();
            } catch (Exception e) {
                System.err.println("Error!");
            }
        }
    }

    private void createCentralShape() {
        centralShape = null;
        int shapeSize = 170;
        centralShape = ShapeFactory.createCentralShape(currentShape, shapeSize);
    }

    private void generateFragments() {
        fragments.clear();
        fragments = ShapeFactory.generateFragments(CANDY_RADIUS, currentLevel);
        allFragmentsAreBroken = false;

        for (Fragment fragment : fragments) {
            fragment.broken = false;
        }
    }

    private void stopTimers() {
       // Future time additions if system needs any change
    }

    private void checkWinCondition() {
        if (gameState != GameState.PLAYING || GameOverOccurred || allFragmentsAreBroken) {
            return;
        }

        int TotalFragments = fragments.size();
        int BrokenFragments = 0;

        if (TotalFragments == 0) return;

        for (Fragment fragment : fragments) {
            if (fragment.broken) {
                BrokenFragments++;
            }
        }

        if (BrokenFragments == TotalFragments && !allFragmentsAreBroken) {
            allFragmentsAreBroken = true;
            Timer transitionTimer = new Timer(500, e -> {
                proceedWithWin();
                ((Timer)e.getSource()).stop();
            });
            transitionTimer.setRepeats(false);
            transitionTimer.start();
        }
    }

    private void proceedWithWin() {
        shapesCompleted++;
        score += 50;
        stopTimers();

        if (shapesCompleted < shapesSelected) {
             showShapeSuccessDialog();
        }
        else {
            if (currentLevel < maxLevels) {
                gameState = GameState.LEVEL_SUCCESS;
                repaint();
            }
            else {
                if (gameTimer != null && gameTimer.isRunning()) {
                    gameTimer.stop();
                }
                if (physicsTimer != null && physicsTimer.isRunning()) {
                    physicsTimer.stop();
                }
                showWinDialog();
            }
        }
    }

    private void showShapeSuccessDialog() {
        if (GameOver != null) {
            GameOver.dispose();
        }

        JFrame mainFrame = (JFrame) SwingUtilities.getWindowAncestor(this);

        GameOver = new JFrame();
        GameOver.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        GameOver.setSize(400, 400);
        GameOver.setLocationRelativeTo(mainFrame);
        GameOver.setUndecorated(true);
        GameOver.setAlwaysOnTop(true);

        ShapeSuccessPanel shapeSuccessPanel = new ShapeSuccessPanel();
        GameOver.add(shapeSuccessPanel);
        GameOver.setVisible(true);

        Timer autoCloseTimer = new Timer(2000, e -> {
            GameOver.dispose();
            moveToNextShape();
            ((Timer)e.getSource()).stop();
        });
        autoCloseTimer.setRepeats(false);
        autoCloseTimer.start();
    }

    private void moveToNextShape() {
        currentShapeIndex++;

        if (currentShapeIndex < selectionOrder.length && selectionOrder[currentShapeIndex] != -1) {
            int nextShapeIndex = selectionOrder[currentShapeIndex];
            currentShape = levelShapes[currentLevel-1][nextShapeIndex];

            centralShape = null;
            fragments.clear();

            createCentralShape();
            generateFragments();

            timeRemaining = levelTimes[currentLevel-1];
            allFragmentsAreBroken = false;
            GameOverOccurred = false;

            repaint();

            Timer startTimer = new Timer(300, e -> {
                gameState = GameState.PLAYING;
                repaint();
                ((Timer)e.getSource()).stop();
            });
            startTimer.setRepeats(false);
            startTimer.start();
        }
        else {
            if (currentLevel < maxLevels) {
                gameState = GameState.LEVEL_SUCCESS;
                repaint();
            } else {
                showWinDialog();
            }
        }
    }

    private void showGameOverDialog() {
        if (GameOver != null) {
            GameOver.dispose();
        }

        JFrame mainFrame = (JFrame) SwingUtilities.getWindowAncestor(this);

        GameOver = new JFrame("Game Over");
        GameOver.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        GameOver.setSize(300, 200);
        GameOver.setLocationRelativeTo(mainFrame);
        GameOver.setUndecorated(true);
        GameOver.setAlwaysOnTop(true);

        GameOverPanel gameOverPanel = new GameOverPanel();
        GameOver.add(gameOverPanel);
        GameOver.setVisible(true);
    }

    private class GameOverPanel extends JPanel {
        public GameOverPanel() {
            setBackground(Color.BLACK);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleGameOverFrameClick(e.getX(), e.getY());
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (GameOverImage != null) {
                g2d.drawImage(GameOverImage, 0, 0, getWidth(), getHeight(), null);
            } else {
                g2d.setColor(Color.RED);
                g2d.setFont(new Font("Arial", Font.BOLD, 24));
                FontMetrics fm = g2d.getFontMetrics();
                String text = "GAME OVER!";
                int textWidth = fm.stringWidth(text);
                g2d.drawString(text, (getWidth() - textWidth) / 2, getHeight() / 2 - 20);
            }

            g2d.setColor(new Color(100, 200, 100));
            g2d.fillRect(100, 150, 100, 30);
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(100, 150, 100, 30);

            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            FontMetrics fm = g2d.getFontMetrics();
            String buttonText = "Restart";
            int textWidth = fm.stringWidth(buttonText);
            g2d.drawString(buttonText, 100 + (100 - textWidth) / 2, 150 + 20);
        }

        private void handleGameOverFrameClick(int x, int y) {
            Rectangle restartRect = new Rectangle(100, 150, 100, 30);
            if (restartRect.contains(x, y)) {
                GameOver.dispose();
                restartCurrentLevel();
            }
        }
    }

    private class ShapeSuccessPanel extends JPanel {
        public ShapeSuccessPanel() {
            setBackground(Color.white);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            BufferedImage image = null;

            if (shapesCompleted == 1 && shapeSuccessImage != null) {
                image = shapeSuccessImage;
            }

            if (image != null) {
                g2d.drawImage(image, 0, 0, getWidth(), getHeight(), null);
            }
        }
    }

    private void resetForNewLevel() {
        resetSelectionState();
        shapesCompleted = 0;
        currentShapeIndex = 0;
        timeRemaining = levelTimes[currentLevel-1];
        allFragmentsAreBroken = false;
        GameOverOccurred = false;
    }

    private void showWinDialog() {
        stopTimers();
        if (gameTimer != null && gameTimer.isRunning()) {
            gameTimer.stop();
        }
        if (physicsTimer != null && physicsTimer.isRunning()) {
            physicsTimer.stop();
        }

        if (GameOver != null) {
            GameOver.dispose();
        }

        JFrame mainFrame = (JFrame) SwingUtilities.getWindowAncestor(this);

        GameOver = new JFrame();
        GameOver.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        GameOver.setSize(mainFrame.getWidth(), mainFrame.getHeight());
        GameOver.setLocation(mainFrame.getX(), mainFrame.getY());
        GameOver.setUndecorated(true);
        GameOver.setAlwaysOnTop(true);

        WinPanel winPanel = new WinPanel();
        GameOver.add(winPanel);
        GameOver.setVisible(true);
    }

    private class WinPanel extends JPanel {
        private BufferedImage winImage;

        public WinPanel() {
            setBackground(Color.BLACK);
            loadWinImage();
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleWinClick(e.getX(), e.getY());
                }
            });
        }

        private void loadWinImage() {
            try {
                File imageFile = new File("C:\\Users\\ishra\\IdeaProjects\\Dalgona Shape Escape SPL FINAL\\s game last play again bg.png");
                if (imageFile.exists()) {
                    winImage = ImageIO.read(imageFile);
                }
            } catch (Exception e) {
                System.err.println("Error loading win image");
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (winImage != null) {
                g2d.drawImage(winImage, 0, 0, getWidth(), getHeight(), null);
            }

            String playAgainText = "PLAY AGAIN";
            String exitText = "EXIT";
            int buttonWidth = 120;
            int buttonHeight = 40;
            int buttonSpacing = 50;
            int totalButtonWidth = (buttonWidth * 2) + buttonSpacing;
            int playAgainButtonX = (getWidth() - totalButtonWidth) / 2;
            int exitButtonX = playAgainButtonX + buttonWidth + buttonSpacing;
            int buttonY = getHeight() - 50;

            // PLAY AGAIN button
            g2d.setColor(new Color(100, 200, 100));
            g2d.fillRect(playAgainButtonX, buttonY, buttonWidth, buttonHeight);
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(playAgainButtonX, buttonY, buttonWidth, buttonHeight);

            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            FontMetrics fm = g2d.getFontMetrics();
            int playAgainTextWidth = fm.stringWidth(playAgainText);
            int textHeight = fm.getHeight();
            g2d.drawString(playAgainText, playAgainButtonX + (buttonWidth - playAgainTextWidth) / 2,
                    buttonY + (buttonHeight + textHeight) / 2 - 5);

            // EXIT button
            g2d.setColor(new Color(200, 100, 100));
            g2d.fillRect(exitButtonX, buttonY, buttonWidth, buttonHeight);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(exitButtonX, buttonY, buttonWidth, buttonHeight);

            int exitTextWidth = fm.stringWidth(exitText);
            g2d.drawString(exitText, exitButtonX + (buttonWidth - exitTextWidth) / 2,
                    buttonY + (buttonHeight + textHeight) / 2 - 5);
        }

        private void handleWinClick(int x, int y) {
            int buttonWidth = 120;
            int buttonHeight = 40;
            int buttonSpacing = 50;
            int totalButtonWidth = (buttonWidth * 2) + buttonSpacing;
            int playAgainButtonX = (getWidth() - totalButtonWidth) / 2;
            int exitButtonX = playAgainButtonX + buttonWidth + buttonSpacing;
            int buttonY = getHeight() - 50;

            Rectangle playAgainRect = new Rectangle(playAgainButtonX, buttonY, buttonWidth, buttonHeight);
            Rectangle exitRect = new Rectangle(exitButtonX, buttonY, buttonWidth, buttonHeight);

            if (playAgainRect.contains(x, y)) {
                GameOver.dispose();
                resetGame();
                playBackgroundMusic();
            }
            else if (exitRect.contains(x, y)) {
                GameOver.dispose();
                resetGame();
                gameState = GameState.MENU;
                repaint();
                playBackgroundMusic();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        switch (gameState) {
            case STARTING_SCREEN:
                GameRenderer.drawStartingScreen(g2d, getWidth(), getHeight());
                break;

            case MENU:
                GameRenderer.drawMenuScreen(g2d, getWidth(), getHeight());
                break;

            case SHAPE_SELECTION:
                GameRenderer.drawShapeSelection(g2d, getWidth(), getHeight(), currentLevel,
                        timeRemaining, shapesSelected, levelShapes,
                        selectedShapes, shapeButtons, selectionOrder);
                break;

            case PLAYING:
                GameRenderer.drawGameScreen(g2d, getWidth(), getHeight(), currentLevel,
                        timeRemaining, score, fragments,
                        centralShape, CANDY_RADIUS, false);
                break;

            case PAUSED:
                GameRenderer.drawGameScreen(g2d, getWidth(), getHeight(), currentLevel,
                        timeRemaining, score, fragments,
                        centralShape, CANDY_RADIUS, true);
                break;

            case GAME_OVER:
                GameRenderer.drawGameScreen(g2d, getWidth(), getHeight(), currentLevel,
                        timeRemaining, score, fragments,
                        centralShape, CANDY_RADIUS, false);
                break;

            case LEVEL_SUCCESS:
                drawLevelCompleteScreen(g2d);
                break;
        }
    }

    private void drawLevelCompleteScreen(Graphics2D g2d) {
        if (bgImage != null) {
            g2d.drawImage(bgImage, 0, 0, getWidth(), getHeight(), null);
        } else {
            g2d.setColor(new Color(20, 20, 30));
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        if (levelCompleteImage != null) {
            int x = (getWidth() - levelCompleteImage.getWidth()) / 2;
            int y = (getHeight() - levelCompleteImage.getHeight()) / 2 - 50;
            g2d.drawImage(levelCompleteImage, x, y, null);
        }

        g2d.setColor(new Color(100, 200, 100));
        g2d.fillRect(GoToNextLevelButton.x, GoToNextLevelButton.y, GoToNextLevelButton.width, GoToNextLevelButton.height);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(GoToNextLevelButton.x, GoToNextLevelButton.y, GoToNextLevelButton.width, GoToNextLevelButton.height);

        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = g2d.getFontMetrics();
        String nextText = "Go to Next Level";
        int nextTextWidth = fm.stringWidth(nextText);
        g2d.drawString(nextText, GoToNextLevelButton.x + (GoToNextLevelButton.width - nextTextWidth) / 2,
                GoToNextLevelButton.y + (GoToNextLevelButton.height + fm.getHeight()) / 2 - 5);

        g2d.setColor(new Color(200, 100, 100));
        g2d.fillRect(ExitButton.x, ExitButton.y, ExitButton.width, ExitButton.height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(ExitButton.x, ExitButton.y, ExitButton.width, ExitButton.height);

        String exitText = "EXIT";
        int exitTextWidth = fm.stringWidth(exitText);
        g2d.drawString(exitText, ExitButton.x + (ExitButton.width - exitTextWidth) / 2,
                ExitButton.y + (ExitButton.height + fm.getHeight()) / 2 - 5);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        switch (gameState) {
            case MENU:
                handleMenuClick(e.getX(), e.getY());
                break;
            case SHAPE_SELECTION:
                handleShapeSelectionClick(e.getX(), e.getY());
                break;
            case PLAYING:
                handleGamePlayClick(e.getX(), e.getY());
                break;
            case LEVEL_SUCCESS:
                handleLevelCompleteClick(e.getX(), e.getY());
                break;
        }

        handleGameControlButtons(e.getX(), e.getY());
    }

    private void handleLevelCompleteClick(int x, int y) {
        if (GoToNextLevelButton.contains(x, y)) {
            currentLevel++;
            resetForNewLevel();
            gameState = GameState.SHAPE_SELECTION;
            repaint();
        } else if (ExitButton.contains(x, y)) {
            resetGame();
            gameState = GameState.MENU;
            repaint();
        }
    }

    private void handleMenuClick(int x, int y) {
        int buttonWidth = 120;
        int buttonHeight = 50;
        int buttonSpacing = 40;
        int totalButtonWidth = (buttonWidth * 2) + buttonSpacing;
        int startX = (getWidth() - totalButtonWidth) / 2;
        int buttonY = getHeight() / 2 - 60;

        Rectangle playGameButton = new Rectangle(startX, buttonY, buttonWidth, buttonHeight);
        Rectangle exitButton = new Rectangle(startX + buttonWidth + buttonSpacing, buttonY, buttonWidth, buttonHeight);

        if (playGameButton.contains(x, y)) {
            gameState = GameState.SHAPE_SELECTION;
            repaint();
        } else if (exitButton.contains(x, y)) {
            System.exit(0);
        }
    }

    private void handleGameControlButtons(int x, int y) {
        if (y >= 10 && y <= 40) {
            if (x >= 450 && x <= 520 && !GameOverOccurred) {
                if (gameState == GameState.PLAYING) {
                    gameState = GameState.PAUSED;
                    repaint();
                }
            }
            else if (x >= 530 && x <= 600 && !GameOverOccurred) {
                if (gameState == GameState.PAUSED) {
                    gameState = GameState.PLAYING;
                    repaint();
                }
            }
            else if (x >= 610 && x <= 680) {
                if (GameOver != null) {
                    GameOver.dispose();
                }
                restartCurrentLevel();
            }
            else if (x >= 690 && x <= 760) {
                resetGame();
                gameState = GameState.MENU;
                repaint();
            }
        }
    }

    private void handleShapeSelectionClick(int x, int y) {
        for (int i = 0; i < 2; i++) {
            int centerX = shapeButtons[i].x + shapeButtons[i].width / 2;
            int centerY = shapeButtons[i].y + shapeButtons[i].height / 2;

            if (Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2) <= Math.pow(85, 2)) {
                if (selectedShapes[i]) {
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
                }
                else if (shapesSelected < shapesToSelect) {
                    selectedShapes[i] = true;
                    selectionOrder[shapesSelected] = i;
                    shapesSelected++;
                }
                repaint();
                return;
            }
        }

        if (shapesSelected == 2 && new Rectangle(320, 540, 80, 35).contains(x, y)) {
            startGame();
        }

        if (new Rectangle(420, 540, 80, 35).contains(x, y)) {
            gameState = GameState.MENU;
            resetSelectionState();
            repaint();
        }
    }

    private void startGame() {
        currentShapeIndex = 0;
        int firstShapeIndex = selectionOrder[currentShapeIndex];
        currentShape = levelShapes[currentLevel-1][firstShapeIndex];

        createCentralShape();
        generateFragments();
        gameState = GameState.PLAYING;
        timeRemaining = levelTimes[currentLevel-1];
        repaint();
    }

    private void handleGamePlayClick(int x, int y) {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int centeredX = x - centerX;
        int centeredY = y - centerY;

        if (centralShape != null && centralShape.contains(centeredX, centeredY)) {
            gameState = GameState.GAME_OVER;
            GameOverOccurred = true;
            showGameOverDialog();
            playGameOverSound();
            repaint();
            return;
        }

        for (Fragment fragment : fragments) {
            if (!fragment.broken && fragment.shape.contains(centeredX, centeredY)) {
                fragment.broken = true;
                fragment.initPhysics();
                playCrackSound();
                repaint();
                break;
            }
        }
    }

    private void restartCurrentLevel() {
        stopTimers();
        resetSelectionState();
        shapesCompleted = 0;
        currentShapeIndex = 0;
        gameState = GameState.SHAPE_SELECTION;
        GameOverOccurred = false;
        timeRemaining = levelTimes[currentLevel-1];
        allFragmentsAreBroken = false;
        score = 0;

        if (physicsTimer != null && !physicsTimer.isRunning()) {
            physicsTimer.start();
        }
        if (gameTimer != null && !gameTimer.isRunning()) {
            gameTimer.start();
        }

        fragments.clear();
        centralShape = null;

        playBackgroundMusic();
        repaint();
    }

    private void resetGame() {
        stopTimers();
        currentLevel = 1;
        resetSelectionState();
        shapesCompleted = 0;
        currentShapeIndex = 0;
        score = 0;
        gameState = GameState.MENU;
        GameOverOccurred = false;
        timeRemaining = levelTimes[0];
        allFragmentsAreBroken = false;

        if (physicsTimer != null && !physicsTimer.isRunning()) {
            physicsTimer.start();
        }
        if (gameTimer != null && !gameTimer.isRunning()) {
            gameTimer.start();
        }

        fragments.clear();
        centralShape = null;

        playBackgroundMusic();
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}
}
