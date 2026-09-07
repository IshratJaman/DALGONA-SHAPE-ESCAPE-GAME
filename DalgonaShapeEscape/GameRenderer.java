import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class GameRenderer {
    private static final Color CANDY_COLOR = new Color(220, 168, 105);
    private static final Color CANDY_OUTLINE = new Color(180, 130, 70);
    private static final Color SHAPE_COLOR = new Color(220, 168, 105);
    private static final Color HEADER_BG = new Color(255, 255, 255, 230);
    private static final Color BTN_GREY = new Color(100, 100, 110);
    private static final Color BTN_GREY_HOVER = new Color(130, 130, 140);
    private static final Color GREEN_BTN = new Color(80, 200, 80);
    private static final Color DARK_GREEN = new Color(0, 120, 0);
    private static final Color PINK_ACCENT = new Color(220, 50, 80);
    private static final Color OVERLAY = new Color(0, 0, 0, 160);

    // ========== BACKGROUND ==========
    public static void drawBackground(Graphics2D g2d, int w, int h, BufferedImage bgImage) {
        if (bgImage != null) {
            g2d.drawImage(bgImage, 0, 0, w, h, null);
        } else {
            // Fallback dark gradient
            GradientPaint gp = new GradientPaint(0, 0, new Color(30, 20, 30),
                    0, h, new Color(15, 10, 20));
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, w, h);
        }
    }

    // ========== SHAPE SELECTION SCREEN ==========
    public static void drawShapeSelection(Graphics2D g2d, int w, int h, int currentLevel,
                                          int timeDisplay, int shapesSelected, ShapeType[][] levelShapes,
                                          boolean[] selectedShapes, Rectangle[] shapeButtons, int[] selectionOrder) {
        // Semi-transparent overlay for readability
        g2d.setColor(new Color(0, 0, 0, 120));
        g2d.fillRect(0, 0, w, h);

        // Header bar
        g2d.setColor(HEADER_BG);
        g2d.fillRect(0, 0, w, 55);
        g2d.setColor(new Color(220, 50, 80));
        g2d.fillRect(0, 53, w, 3);

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.drawString("Level: " + currentLevel, 25, 35);
        g2d.drawString("Time: " + timeDisplay, w - 140, 35);

        // Title
        g2d.setColor(new Color(255, 220, 50));
        g2d.setFont(new Font("Arial", Font.BOLD, 34));
        String title = "LEVEL " + currentLevel + ": CHOOSE 2 SHAPES";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(title, (w - fm.stringWidth(title)) / 2, 130);

        // Selection counter
        g2d.setColor(shapesSelected == 2 ? new Color(100, 255, 100) : new Color(255, 220, 50));
        g2d.setFont(new Font("Arial", Font.BOLD, 26));
        String counter = "Selected: " + shapesSelected + "/2";
        fm = g2d.getFontMetrics();
        g2d.drawString(counter, (w - fm.stringWidth(counter)) / 2, 180);

        // Shape option circles
        ShapeType[] options = levelShapes[currentLevel - 1];
        int shapesY = shapeButtons[0].y + shapeButtons[0].height / 2;

        for (int i = 0; i < 2; i++) {
            int cx = shapeButtons[i].x + shapeButtons[i].width / 2;
            int cy = shapesY;

            // Outer selection ring
            if (selectedShapes[i]) {
                g2d.setColor(new Color(0, 255, 100));
                g2d.setStroke(new BasicStroke(4));
                g2d.drawOval(cx - 68, cy - 68, 136, 136);
                // Glow effect
                g2d.setColor(new Color(0, 255, 100, 40));
                g2d.fillOval(cx - 72, cy - 72, 144, 144);
            } else {
                g2d.setColor(new Color(255, 255, 255, 60));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawOval(cx - 65, cy - 65, 130, 130);
            }

            // Candy circle fill
            g2d.setColor(CANDY_COLOR);
            g2d.fillOval(cx - 60, cy - 60, 120, 120);
            g2d.setColor(CANDY_OUTLINE);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(cx - 60, cy - 60, 120, 120);

            // Shape preview inside circle
            g2d.setColor(new Color(60, 40, 20));
            g2d.setStroke(new BasicStroke(2.5f));
            drawShapePreview(g2d, options[i], cx, cy, 45);

            // Shape name
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            String name = getShapeName(options[i]);
            fm = g2d.getFontMetrics();
            g2d.drawString(name, cx - fm.stringWidth(name) / 2, cy + 95);
        }

        // Instruction
        g2d.setColor(new Color(200, 200, 200));
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        String instr = "Click on shapes to select 2 challenges";
        fm = g2d.getFontMetrics();
        g2d.drawString(instr, (w - fm.stringWidth(instr)) / 2, shapesY + 140);

        // Start button (only when 2 selected)
        if (shapesSelected == 2) {
            Rectangle startBtn = getStartButtonRect(w);
            // Button shadow
            g2d.setColor(new Color(0, 80, 0));
            g2d.fillRoundRect(startBtn.x + 2, startBtn.y + 2, startBtn.width, startBtn.height, 12, 12);
            // Button
            g2d.setColor(GREEN_BTN);
            g2d.fillRoundRect(startBtn.x, startBtn.y, startBtn.width, startBtn.height, 12, 12);
            g2d.setColor(DARK_GREEN);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(startBtn.x, startBtn.y, startBtn.width, startBtn.height, 12, 12);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            fm = g2d.getFontMetrics();
            g2d.drawString("START", startBtn.x + (startBtn.width - fm.stringWidth("START")) / 2,
                    startBtn.y + 28);
        }
    }

    public static Rectangle getStartButtonRect(int w) {
        return new Rectangle(w / 2 - 65, 590, 130, 42);
    }

    // ========== GAMEPLAY SCREEN ==========
    public static void drawGameScreen(Graphics2D g2d, int w, int h, int currentLevel,
                                      int timeRemaining, int score, ArrayList<Fragment> fragments,
                                      Path2D.Double centralShape, int candyRadius, ShapeType currentShape) {
        // Header bar
        g2d.setColor(HEADER_BG);
        g2d.fillRect(0, 0, w, 55);
        g2d.setColor(PINK_ACCENT);
        g2d.fillRect(0, 53, w, 3);

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.drawString("Level: " + currentLevel, 25, 35);

        // Time with color warning
        if (timeRemaining <= 10) {
            g2d.setColor(Color.RED);
        } else {
            g2d.setColor(Color.BLACK);
        }
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        String timeStr = "Time: " + timeRemaining;
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(timeStr, (w - fm.stringWidth(timeStr)) / 2, 35);

        // Set up centered coordinates
        int centerX = w / 2;
        int centerY = h / 2 + 20;
        g2d.translate(centerX, centerY);

        // Draw candy disc + fragments
        boolean anyBroken = false;
        for (Fragment fragment : fragments) {
            if (fragment.broken) {
                anyBroken = true;
                break;
            }
        }

        if (!anyBroken) {
            // Full candy disc
            g2d.setColor(CANDY_COLOR);
            g2d.fill(new Ellipse2D.Double(-candyRadius, -candyRadius, candyRadius * 2, candyRadius * 2));
            g2d.setColor(CANDY_OUTLINE);
            g2d.setStroke(new BasicStroke(2));
            g2d.draw(new Ellipse2D.Double(-candyRadius, -candyRadius, candyRadius * 2, candyRadius * 2));
        } else {
            drawFragments(g2d, fragments, w, h);
        }

        // Draw the protected central shape
        drawCentralShape(g2d, centralShape);

        // Reset transform
        g2d.translate(-centerX, -centerY);
    }

    // ========== GAMEPLAY BUTTONS ==========
    public static Rectangle[] drawGameplayButtons(Graphics2D g2d, int w) {
        int btnW = 75;
        int btnH = 28;
        int btnY = 12;
        int startX = w - 340;
        int gap = 82;

        String[] labels = {"Pause", "Resume", "Restart", "Exit"};
        Rectangle[] buttons = new Rectangle[4];

        for (int i = 0; i < 4; i++) {
            int bx = startX + i * gap;
            buttons[i] = new Rectangle(bx, btnY, btnW, btnH);

            g2d.setColor(BTN_GREY);
            g2d.fillRoundRect(bx, btnY, btnW, btnH, 8, 8);
            g2d.setColor(new Color(70, 70, 80));
            g2d.setStroke(new BasicStroke(1));
            g2d.drawRoundRect(bx, btnY, btnW, btnH, 8, 8);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(labels[i], bx + (btnW - fm.stringWidth(labels[i])) / 2, btnY + 19);
        }

        return buttons;
    }

    // ========== PAUSE OVERLAY ==========
    public static void drawPauseOverlay(Graphics2D g2d, int w, int h) {
        g2d.setColor(OVERLAY);
        g2d.fillRect(0, 56, w, h - 56);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 52));
        String txt = "PAUSED";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(txt, (w - fm.stringWidth(txt)) / 2, h / 2);

        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        String sub = "Click 'Resume' to continue";
        fm = g2d.getFontMetrics();
        g2d.setColor(new Color(200, 200, 200));
        g2d.drawString(sub, (w - fm.stringWidth(sub)) / 2, h / 2 + 40);
    }

    // ========== SUCCESS POPUP ==========
    public static Rectangle drawSuccessPopup(Graphics2D g2d, int w, int h,
                                             BufferedImage passPinkImage,
                                             ShapeType shape, int shapesCompleted, int total) {
        // Overlay
        g2d.setColor(OVERLAY);
        g2d.fillRect(0, 0, w, h);

        int dw = 360, dh = 400;
        int dx = (w - dw) / 2, dy = (h - dh) / 2;

        // Dialog background
        g2d.setColor(new Color(30, 30, 40, 240));
        g2d.fillRoundRect(dx, dy, dw, dh, 24, 24);

        // Image fills the entire popup
        if (passPinkImage != null) {
            // Clip to rounded rect
            Shape oldClip = g2d.getClip();
            g2d.setClip(new java.awt.geom.RoundRectangle2D.Double(dx, dy, dw, dh, 24, 24));
            g2d.drawImage(passPinkImage, dx, dy, dw, dh, null);
            g2d.setClip(oldClip);
        }

        // Semi-transparent gradient overlay at bottom for text readability
        GradientPaint gp = new GradientPaint(0, dy + dh - 160, new Color(0, 0, 0, 0),
                                              0, dy + dh, new Color(0, 0, 0, 200));
        g2d.setPaint(gp);
        g2d.fillRoundRect(dx, dy + dh - 160, dw, 160, 0, 0);

        // Border
        g2d.setColor(PINK_ACCENT);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(dx, dy, dw, dh, 24, 24);

        // Text on top of image
        g2d.setColor(new Color(255, 220, 50));
        g2d.setFont(new Font("Arial", Font.BOLD, 26));
        String successText = getShapeName(shape) + " COMPLETE!";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(successText, dx + (dw - fm.stringWidth(successText)) / 2, dy + dh - 95);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        String progress = "Shapes completed: " + shapesCompleted + "/" + total;
        fm = g2d.getFontMetrics();
        g2d.drawString(progress, dx + (dw - fm.stringWidth(progress)) / 2, dy + dh - 70);

        // Continue button at bottom
        int btnW = 160, btnH = 42;
        int btnX = dx + (dw - btnW) / 2;
        int btnY = dy + dh - 55;

        g2d.setColor(GREEN_BTN);
        g2d.fillRoundRect(btnX, btnY, btnW, btnH, 10, 10);
        g2d.setColor(DARK_GREEN);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(btnX, btnY, btnW, btnH, 10, 10);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        String btnLabel = shapesCompleted < total ? "CONTINUE" : "SUCCESS";
        fm = g2d.getFontMetrics();
        g2d.drawString(btnLabel, btnX + (btnW - fm.stringWidth(btnLabel)) / 2, btnY + 28);

        return new Rectangle(btnX, btnY, btnW, btnH);
    }

    // ========== LEVEL VICTORY POPUP ==========
    public static Rectangle[] drawLevelVictoryPopup(Graphics2D g2d, int w, int h,
                                                     BufferedImage victoryImage, int currentLevel) {
        // Full overlay
        g2d.setColor(OVERLAY);
        g2d.fillRect(0, 0, w, h);

        int dw = 400, dh = 450;
        int dx = (w - dw) / 2, dy = (h - dh) / 2;

        // Dialog background
        g2d.setColor(new Color(30, 30, 40, 240));
        g2d.fillRoundRect(dx, dy, dw, dh, 24, 24);

        // Image fills the entire popup
        if (victoryImage != null) {
            Shape oldClip = g2d.getClip();
            g2d.setClip(new java.awt.geom.RoundRectangle2D.Double(dx, dy, dw, dh, 24, 24));
            g2d.drawImage(victoryImage, dx, dy, dw, dh, null);
            g2d.setClip(oldClip);
        }

        // Gradient overlay at bottom for text readability
        GradientPaint gp = new GradientPaint(0, dy + dh - 200, new Color(0, 0, 0, 0),
                                              0, dy + dh, new Color(0, 0, 0, 220));
        g2d.setPaint(gp);
        g2d.fillRoundRect(dx, dy + dh - 200, dw, 200, 0, 0);

        // Border
        g2d.setColor(new Color(255, 215, 0));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(dx, dy, dw, dh, 24, 24);

        // Title
        g2d.setColor(new Color(255, 220, 50));
        g2d.setFont(new Font("Arial", Font.BOLD, 30));
        String title = "LEVEL " + currentLevel + " COMPLETE!";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(title, dx + (dw - fm.stringWidth(title)) / 2, dy + dh - 130);

        // Next level button
        int btnW = 180, btnH = 42;
        int btnX = dx + (dw - btnW) / 2;
        int btnY = dy + dh - 105;

        g2d.setColor(GREEN_BTN);
        g2d.fillRoundRect(btnX, btnY, btnW, btnH, 10, 10);
        g2d.setColor(DARK_GREEN);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(btnX, btnY, btnW, btnH, 10, 10);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        String nextLabel = "GO TO LEVEL " + (currentLevel + 1);
        fm = g2d.getFontMetrics();
        g2d.drawString(nextLabel, btnX + (btnW - fm.stringWidth(nextLabel)) / 2, btnY + 28);

        // Exit button
        int exitBtnY = btnY + 52;
        g2d.setColor(BTN_GREY);
        g2d.fillRoundRect(btnX, exitBtnY, btnW, btnH, 10, 10);
        g2d.setColor(new Color(70, 70, 80));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(btnX, exitBtnY, btnW, btnH, 10, 10);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        fm = g2d.getFontMetrics();
        g2d.drawString("EXIT", btnX + (btnW - fm.stringWidth("EXIT")) / 2, exitBtnY + 28);

        return new Rectangle[]{
            new Rectangle(btnX, btnY, btnW, btnH),
            new Rectangle(btnX, exitBtnY, btnW, btnH)
        };
    }

    // ========== GAME OVER POPUP ==========
    public static Rectangle drawGameOverPopup(Graphics2D g2d, int w, int h,
                                              BufferedImage gameOverImage) {
        g2d.setColor(OVERLAY);
        g2d.fillRect(0, 0, w, h);

        int dw = 380, dh = 440;
        int dx = (w - dw) / 2, dy = (h - dh) / 2;

        // Dialog background
        g2d.setColor(new Color(30, 10, 10, 240));
        g2d.fillRoundRect(dx, dy, dw, dh, 24, 24);

        // Image fills the entire popup box
        if (gameOverImage != null) {
            Shape oldClip = g2d.getClip();
            g2d.setClip(new RoundRectangle2D.Double(dx, dy, dw, dh, 24, 24));
            g2d.drawImage(gameOverImage, dx, dy, dw, dh, null);
            g2d.setClip(oldClip);
        }

        // Dark gradient overlay at bottom for text & button readability
        GradientPaint gp = new GradientPaint(0, dy + dh - 170, new Color(0, 0, 0, 0),
                                              0, dy + dh, new Color(0, 0, 0, 230));
        g2d.setPaint(gp);
        g2d.fillRoundRect(dx, dy + dh - 170, dw, 170, 0, 0);

        // Red border
        g2d.setColor(new Color(220, 30, 30));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(dx, dy, dw, dh, 24, 24);

        // Game Over text with drop shadow
        g2d.setFont(new Font("Arial", Font.BOLD, 34));
        String goText = "GAME OVER";
        FontMetrics fm = g2d.getFontMetrics();
        int textX = dx + (dw - fm.stringWidth(goText)) / 2;
        int textY = dy + dh - 82;
        g2d.setColor(new Color(0, 0, 0, 220));
        g2d.drawString(goText, textX + 2, textY + 2);
        g2d.setColor(new Color(255, 50, 50));
        g2d.drawString(goText, textX, textY);

        // Restart button
        int btnW = 160, btnH = 42;
        int btnX = dx + (dw - btnW) / 2;
        int btnY = dy + dh - 58;

        g2d.setColor(new Color(200, 40, 40));
        g2d.fillRoundRect(btnX, btnY, btnW, btnH, 10, 10);
        g2d.setColor(new Color(140, 20, 20));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(btnX, btnY, btnW, btnH, 10, 10);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        fm = g2d.getFontMetrics();
        g2d.drawString("RESTART", btnX + (btnW - fm.stringWidth("RESTART")) / 2, btnY + 28);

        return new Rectangle(btnX, btnY, btnW, btnH);
    }

    // ========== FINAL CONGRATULATIONS ==========
    public static Rectangle[] drawFinalCongratulations(Graphics2D g2d, int w, int h,
                                                        BufferedImage finalImage, int score) {
        // Image as full-screen background
        if (finalImage != null) {
            g2d.drawImage(finalImage, 0, 0, w, h, null);
        } else {
            g2d.setColor(new Color(10, 10, 15));
            g2d.fillRect(0, 0, w, h);
        }

        // Dark overlay for text readability
        g2d.setColor(new Color(0, 0, 0, 140));
        g2d.fillRect(0, 0, w, h);

        // Gradient darker at center-bottom for buttons
        GradientPaint gp = new GradientPaint(0, h / 2 - 50, new Color(0, 0, 0, 0),
                                              0, h / 2 + 100, new Color(0, 0, 0, 120));
        g2d.setPaint(gp);
        g2d.fillRect(0, h / 2 - 50, w, h / 2 + 50);

        // Title
        g2d.setColor(new Color(255, 215, 0));
        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        String congrats = "CONGRATULATIONS!";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(congrats, (w - fm.stringWidth(congrats)) / 2, h / 2 - 60);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 22));
        String sub = "You completed all 4 levels!";
        fm = g2d.getFontMetrics();
        g2d.drawString(sub, (w - fm.stringWidth(sub)) / 2, h / 2 - 20);

        // Play Again button
        int btnW = 200, btnH = 46;
        int btnX = (w - btnW) / 2;
        int btnY = h / 2 + 30;

        g2d.setColor(GREEN_BTN);
        g2d.fillRoundRect(btnX, btnY, btnW, btnH, 10, 10);
        g2d.setColor(DARK_GREEN);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(btnX, btnY, btnW, btnH, 10, 10);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        fm = g2d.getFontMetrics();
        g2d.drawString("PLAY AGAIN", btnX + (btnW - fm.stringWidth("PLAY AGAIN")) / 2, btnY + 30);

        // Exit button
        int exitBtnY = btnY + 60;
        g2d.setColor(BTN_GREY);
        g2d.fillRoundRect(btnX, exitBtnY, btnW, btnH, 10, 10);
        g2d.setColor(new Color(70, 70, 80));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(btnX, exitBtnY, btnW, btnH, 10, 10);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        fm = g2d.getFontMetrics();
        g2d.drawString("EXIT", btnX + (btnW - fm.stringWidth("EXIT")) / 2, exitBtnY + 30);

        return new Rectangle[]{
            new Rectangle(btnX, btnY, btnW, btnH),
            new Rectangle(btnX, exitBtnY, btnW, btnH)
        };
    }

    // ========== SHAPE PREVIEW (for selection screen) ==========
    private static void drawShapePreview(Graphics2D g2d, ShapeType shapeType, int cx, int cy, int size) {
        Path2D.Double path = ShapeFactory.createCentralShape(shapeType, size);
        if (path != null) {
            AffineTransform tx = AffineTransform.getTranslateInstance(cx, cy);
            Shape transformed = tx.createTransformedShape(path);
            g2d.draw(transformed);
        }
    }

    // ========== FRAGMENT DRAWING ==========
    private static void drawFragments(Graphics2D g2d, ArrayList<Fragment> fragments, int w, int h) {
        for (Fragment fragment : fragments) {
            if (!fragment.broken) {
                // Unbroken fragment - solid candy color
                g2d.setColor(CANDY_COLOR);
                g2d.fill(fragment.shape);
                g2d.setColor(new Color(200, 150, 90, 80));
                g2d.setStroke(new BasicStroke(0.5f));
                g2d.draw(fragment.shape);
            } else if (!fragment.isOffScreen(w, h)) {
                // Broken fragment - animate falling
                Shape drawShape = fragment.getTransformedShape();
                g2d.setColor(CANDY_COLOR);
                g2d.fill(drawShape);
            }
        }
    }

    // ========== CENTRAL SHAPE DRAWING ==========
    private static void drawCentralShape(Graphics2D g2d, Path2D.Double centralShape) {
        if (centralShape == null) return;

        // Fill with same candy color — shape is invisible but still there for hit-testing
        g2d.setColor(CANDY_COLOR);
        g2d.fill(centralShape);
    }

    // ========== UTILITY ==========
    private static String getShapeName(ShapeType shapeType) {
        if (shapeType == null) return "UNKNOWN";
        switch (shapeType) {
            case CIRCLE: return "CIRCLE";
            case TRIANGLE: return "TRIANGLE";
            case SQUARE: return "SQUARE";
            case HEXAGON: return "HEXAGON";
            case HEART: return "HEART";
            case STAR: return "STAR";
            case UMBRELLA: return "UMBRELLA";
            case SQUID: return "SQUID";
            default: return "UNKNOWN";
        }
    }
}
