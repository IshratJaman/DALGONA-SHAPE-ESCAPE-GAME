import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class GameRenderer {
    private static final Color CANDY_COLOR = new Color(220, 168, 105);
    private static final Color SHAPE_COLOR = new Color(220, 168, 105);

    private static BufferedImage startingImage;
    private static BufferedImage bg2Image;
    private static BufferedImage bgImage;

    static {
        loadImages();
    }

    private static void loadImages() {
        try {
            startingImage = loadImageOrPlaceholder("C:\\Users\\ishra\\IdeaProjects\\Dalgona Shape Escape SPL FINAL\\s game start bg.png", 400, 300, "Starting Image", Color.BLUE);
            bg2Image = loadImageOrPlaceholder("C:\\Users\\ishra\\IdeaProjects\\Dalgona Shape Escape SPL FINAL\\s game bg2.png", 800, 600, "Menu Background", Color.DARK_GRAY);
            bgImage = loadImageOrPlaceholder("C:\\Users\\ishra\\IdeaProjects\\Dalgona Shape Escape SPL FINAL\\s game bg.jpg", 800, 600, "Game Background", Color.BLACK);
        }
        catch (Exception e) {
            // Exception Handling
        }
    }

    private static BufferedImage loadImageOrPlaceholder(String filename, int width, int height, String text, Color color) {
        try {
            File imageFile = new File(filename);
            if (imageFile.exists()) {
                return ImageIO.read(imageFile);
            } else {
                return createPlaceholderImage(width, height, text, color);
            }
        } catch (Exception e) {
            return createPlaceholderImage(width, height, text, color);
        }
    }

    private static BufferedImage createPlaceholderImage(int width, int height, String text, Color color) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(color);
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        g2d.drawString(text, (width - textWidth) / 2, (height + textHeight) / 2);

        g2d.dispose();
        return img;
    }

    public static void drawStartingScreen(Graphics2D g2d, int width, int height) {
        if (startingImage != null) {
            g2d.drawImage(startingImage, 0, 0, width, height, null);
        }
    }

    public static void drawMenuScreen(Graphics2D g2d, int width, int height) {
        if (bg2Image != null) {
            g2d.drawImage(bg2Image, 0, 0, width, height, null);
        }
        else {
            g2d.setColor(new Color(20, 20, 50));
            g2d.fillRect(0, 0, width, height);
        }

        int buttonWidth = 120;
        int buttonHeight = 50;
        int buttonSpacing = 40;
        int totalButtonWidth = (buttonWidth * 2) + buttonSpacing;
        int startX = (width - totalButtonWidth) / 2;
        int buttonY = height / 2 - 40;

        // PLAY GAME button
        g2d.setColor(new Color(100, 200, 100));
        g2d.fillRect(startX, buttonY, buttonWidth, buttonHeight);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(startX, buttonY, buttonWidth, buttonHeight);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics metrics = g2d.getFontMetrics();
        String playText = "PLAY GAME";
        int playTextWidth = metrics.stringWidth(playText);
        g2d.drawString(playText, startX + (buttonWidth - playTextWidth) / 2, buttonY + (buttonHeight + metrics.getHeight()) / 2 - 5);

        // EXIT button
        int exitButtonX = startX + buttonWidth + buttonSpacing;
        g2d.setColor(new Color(200, 100, 100));
        g2d.fillRect(exitButtonX, buttonY, buttonWidth, buttonHeight);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(exitButtonX, buttonY, buttonWidth, buttonHeight);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        String exitText = "EXIT";
        int exitTextWidth = metrics.stringWidth(exitText);
        g2d.drawString(exitText, exitButtonX + (buttonWidth - exitTextWidth) / 2, buttonY + (buttonHeight + metrics.getHeight()) / 2 - 5);
    }

    public static void drawShapeSelection(Graphics2D g2d, int width, int height, int currentLevel,
                                          int timeRemaining, int shapesSelected, ShapeType[][] levelShapes,
                                          boolean[] selectedShapes, Rectangle[] shapeButtons, int[] selectionOrder) {
        if (bgImage != null) {
            g2d.drawImage(bgImage, 0, 0, width, height, null);
        }

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, 50);
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Level: " + currentLevel, 20, 30);

        int[] levelTimes = {50, 48, 45, 40};
        int levelTime = levelTimes[currentLevel - 1];
        g2d.drawString("Time: " + levelTime, 150, 30);

        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Arial", Font.BOLD, 28));
        String title = "LEVEL " + currentLevel + " : CHOOSE 2 SHAPES";
        FontMetrics titleMetrics = g2d.getFontMetrics();
        int titleWidth = titleMetrics.stringWidth(title);
        g2d.drawString(title, (width - titleWidth) / 2, 130);

        // Shape Selection
        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        String selected = "Selected : " + shapesSelected + "/2";
        FontMetrics selectedMetrics = g2d.getFontMetrics();
        int selectedWidth = selectedMetrics.stringWidth(selected);
        g2d.drawString(selected, (width - selectedWidth) / 2, 170);

        ShapeType[] levelShapeOptions = levelShapes[currentLevel - 1];
        int shapesY = 350;

        for (int i = 0; i < 2; i++) {
            int centerX = shapeButtons[i].x + shapeButtons[i].width / 2;
            int centerY = shapesY;

            // CANDY CIRCLE
            if (selectedShapes[i]) {
                g2d.setColor(Color.GREEN);
                g2d.setStroke(new BasicStroke(3));
                g2d.drawOval(centerX - 80, centerY - 80, 160, 160);
            }

            g2d.setColor(CANDY_COLOR);
            g2d.fillOval(centerX - 80, centerY - 80, 160, 160);

            // SHAPE PREVIEW
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            int selectionShapeSize = 60;
            drawShapePreview(g2d, levelShapeOptions[i], centerX, centerY, selectionShapeSize);

            // SHAPE NAME ON PREVIEW
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 18));
            FontMetrics shapeMetrics = g2d.getFontMetrics();
            String shapeName = getShapeName(levelShapeOptions[i]);
            int shapeNameWidth = shapeMetrics.stringWidth(shapeName);
            g2d.drawString(shapeName, centerX - shapeNameWidth / 2, centerY + 110);
        }

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        String instruction = "Click on shapes to select 2 challenges";
        FontMetrics instructionMetrics = g2d.getFontMetrics();
        int instructionWidth = instructionMetrics.stringWidth(instruction);
        g2d.drawString(instruction, (width - instructionWidth) / 2, 500);

        // START BUTTON
        if (shapesSelected == 2) {
            g2d.setColor(new Color(100, 200, 100));
            g2d.fillRect(320, 540, 80, 35);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(320, 540, 80, 35);
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            g2d.drawString("START", 335, 562);
        }

        // EXIT BUTTON
        g2d.setColor(new Color(200, 100, 100));
        g2d.fillRect(420, 540, 80, 35);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(420, 540, 80, 35);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("EXIT", 445, 562);
    }

    public static void drawGameScreen(Graphics2D g2d, int width, int height, int currentLevel,
                                      int timeRemaining, int score, ArrayList<Fragment> fragments,
                                      Path2D.Double centralShape, int candyRadius, boolean isPaused) {
        if (bgImage != null) {
            g2d.drawImage(bgImage, 0, 0, width, height, null);
        }

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, 50);
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Level: " + currentLevel, 20, 30);
        g2d.drawString("Time: " + timeRemaining, 150, 30);
        g2d.drawString("Score: " + score, 280, 30);

        String[] buttonLabels = {"Pause", "Resume", "Restart", "Exit"};
        for (int i = 0; i < buttonLabels.length; i++) {
            int buttonX = 450 + i * 80;
            g2d.setColor(new Color(100, 100, 100));
            g2d.fillRect(buttonX, 10, 70, 30);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(buttonX, 10, 70, 30);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
            FontMetrics buttonMetrics = g2d.getFontMetrics();
            int textWidth = buttonMetrics.stringWidth(buttonLabels[i]);
            g2d.drawString(buttonLabels[i], buttonX + (70 - textWidth) / 2, 28);
        }

        int centerX = width / 2;
        int centerY = height / 2;
        g2d.translate(centerX, centerY);

        boolean anyBroken = false;
        for (Fragment fragment : fragments) {
            if (fragment.broken) {
                anyBroken = true;
                break;
            }
        }

        if (!anyBroken) {
            g2d.setColor(CANDY_COLOR);
            g2d.fill(new Ellipse2D.Double(-candyRadius, -candyRadius, candyRadius * 2, candyRadius * 2));
        } else {
            drawFragments(g2d, fragments, width, height);
        }

        drawCentralShape(g2d, centralShape, false);
        g2d.translate(-centerX, -centerY);

        if (isPaused) {
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, 0, width, height);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 48));
            FontMetrics pauseMetrics = g2d.getFontMetrics();
            String pauseText = "PAUSED";
            int textWidth = pauseMetrics.stringWidth(pauseText);
            g2d.drawString(pauseText, (width - textWidth) / 2, height / 2);
        }
    }

    private static void drawShapePreview(Graphics2D g2d, ShapeType shapeType, int centerX, int centerY, int size) {
        switch (shapeType) {
            case CIRCLE:
                g2d.drawOval(centerX - size / 2, centerY - size / 2, size, size);
                break;
            case TRIANGLE:
                int[] xPoints = {centerX, centerX - size / 2, centerX + size / 2};
                int[] yPoints = {centerY - size / 2, centerY + size / 2, centerY + size / 2};
                g2d.drawPolygon(xPoints, yPoints, 3);
                break;
            case SQUARE:
                g2d.drawRect(centerX - size / 2, centerY - size / 2, size, size);
                break;
            case DIAMOND:
                double diamondScale = size / 120.0;
                int[] diamondX = {centerX, centerX + (int) (40 * diamondScale), centerX, centerX - (int) (40 * diamondScale)};
                int[] diamondY = {centerY - (int) (60 * diamondScale), centerY, centerY + (int) (60 * diamondScale), centerY};
                g2d.drawPolygon(diamondX, diamondY, 4);
                break;
            case HEART:
                Path2D.Double heartPreview = new Path2D.Double();
                double scale = size / 80.0;
                heartPreview.moveTo(centerX, centerY);
                heartPreview.curveTo(centerX - 12 * scale, centerY - 12 * scale,
                        centerX - 25 * scale, centerY + 5 * scale,
                        centerX, centerY + 25 * scale);
                heartPreview.curveTo(centerX + 25 * scale, centerY + 5 * scale,
                        centerX + 12 * scale, centerY - 12 * scale,
                        centerX, centerY);
                g2d.draw(heartPreview);
                break;
            case STAR:
                int[] starX = new int[10];
                int[] starY = new int[10];
                for (int i = 0; i < 10; i++) {
                    double angle = Math.PI * i / 5;
                    double radius = (i % 2 == 0) ? size / 2.0 : size / 4.0;
                    starX[i] = centerX + (int) (radius * Math.cos(angle - Math.PI / 2));
                    starY[i] = centerY + (int) (radius * Math.sin(angle - Math.PI / 2));
                }
                g2d.drawPolygon(starX, starY, 10);
                break;
            case UMBRELLA:
                double umbrellaScale = size / 60.0;
                g2d.drawArc(centerX - (int) (25 * umbrellaScale), centerY - (int) (15 * umbrellaScale),
                        (int) (50 * umbrellaScale), (int) (30 * umbrellaScale), 0, 180);
                g2d.drawLine(centerX, centerY, centerX, centerY + (int) (30 * umbrellaScale));
                g2d.drawLine(centerX, centerY + (int) (30 * umbrellaScale),
                        centerX + (int) (8 * umbrellaScale), centerY + (int) (30 * umbrellaScale));
                break;
            case CRESCENT_MOON:
                double moonScale = size / 60.0;
                Ellipse2D.Double outerPreview = new Ellipse2D.Double(
                        centerX - 25 * moonScale, centerY - 25 * moonScale,
                        50 * moonScale, 50 * moonScale);
                Ellipse2D.Double innerPreview = new Ellipse2D.Double(
                        centerX - 15 * moonScale, centerY - 25 * moonScale,
                        50 * moonScale, 50 * moonScale);
                Area outerAreaPreview = new Area(outerPreview);
                Area innerAreaPreview = new Area(innerPreview);
                outerAreaPreview.subtract(innerAreaPreview);
                g2d.draw(outerAreaPreview);
                break;
        }
    }

    private static String getShapeName(ShapeType shapeType) {
        switch (shapeType) {
            case CIRCLE:
                return "CIRCLE";
            case TRIANGLE:
                return "TRIANGLE";
            case SQUARE:
                return "SQUARE";
            case DIAMOND:
                return "DIAMOND";
            case HEART:
                return "HEART";
            case STAR:
                return "STAR";
            case UMBRELLA:
                return "UMBRELLA";
            case CRESCENT_MOON:
                return "CRESCENT MOON";
            default:
                return "UNKNOWN";
        }
    }

    private static void drawFragments(Graphics2D g2d, ArrayList<Fragment> fragments, int width, int height) {
        g2d.setColor(CANDY_COLOR);
        for (Fragment fragment : fragments) {
            if (!fragment.broken) {
                g2d.fill(fragment.shape);
            } else if (!fragment.isOffScreen(width, height)) {
                Shape drawShape = fragment.getTransformedShape();
                if (drawShape != null) {
                    g2d.fill(drawShape);
                }
            }
        }
    }

    private static void drawCentralShape(Graphics2D g2d, Path2D.Double centralShape, boolean showOutline) {
        if (centralShape != null) {
            g2d.setColor(SHAPE_COLOR);
            g2d.fill(centralShape);

            if (showOutline) {
                g2d.setColor(Color.DARK_GRAY);
                g2d.setStroke(new BasicStroke(2));
                g2d.draw(centralShape);
            }
        }
    }
}
