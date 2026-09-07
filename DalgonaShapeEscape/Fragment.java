import java.awt.*;
import java.awt.geom.*;
import java.util.Random;

public class Fragment{
    Shape shape;
    boolean broken = false;
    double centerX, centerY;
    double velocityX = 0;
    double velocityY = 0;
    double rotation = 0;
    double rotationSpeed = 0;
    private static final Random random = new Random();

    Fragment(Shape shape) {
        this.shape = shape;
        Rectangle2D bounds = shape.getBounds2D();
        this.centerX = bounds.getCenterX();
        this.centerY = bounds.getCenterY();
    }

    void initPhysics() {
        // Initialize physics when broken
        double dirX = centerX;
        double dirY = centerY;
        double length = Math.sqrt(dirX*dirX + dirY*dirY);

        if (length > 0) {
            dirX /= length;
            dirY /= length;
        }

        velocityX = dirX * (2 + random.nextDouble() * 3) + (random.nextDouble() - 0.5) * 2;
        velocityY = dirY * (2 + random.nextDouble() * 3) - 2; // Initial upward boost
        rotationSpeed = (random.nextDouble() - 0.5) * 0.2;
    }

    void update(float gravity) {
        if (broken) {
            velocityY += gravity;
            centerX += velocityX;
            centerY += velocityY;
            rotation += rotationSpeed;
        }
    }

    Shape getTransformedShape() {
        if (!broken) {
            return shape;
        }

        AffineTransform transform = new AffineTransform();
        Rectangle2D bounds = shape.getBounds2D();
        double origCenterX = bounds.getCenterX();
        double origCenterY = bounds.getCenterY();
        double deltaX = centerX - origCenterX;
        double deltaY = centerY - origCenterY;
        transform.translate(deltaX, deltaY);
        transform.rotate(rotation, origCenterX, origCenterY);

        return transform.createTransformedShape(shape);
    }

    boolean isOffScreen(int width, int height) {
        int centerScreenX = width / 2;
        int centerScreenY = height / 2;
        double screenX = centerScreenX + centerX;
        double screenY = centerScreenY + centerY;

        return screenY > height + 100 ||
                screenX < -100 ||
                screenX > width + 100;
    }
}