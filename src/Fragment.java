import java.awt.*;
import java.awt.geom.*;
import java.util.*;

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
       // Adding randomness so each fragment moves in a slightly different way
        double dirX = centerX;
        double dirY = centerY;
        double length = Math.sqrt(dirX*dirX + dirY*dirY);

        if (length > 0) {
            dirX = dirX / length;
            dirY = dirY / length;
        }

        // Base speed nd upward or downward movement of EACH FRAGMENT
        velocityX = dirX * (1.5 + random.nextDouble() * 4) + (random.nextDouble() - 0.5) * 3;
        velocityY = dirY * (1.5 + random.nextDouble() * 4) - (1 + random.nextDouble() * 2);
        rotationSpeed = (random.nextDouble() - 0.5) * 0.3; // fragments will spin clockwise or anti-clockwise
    }

    void update(float gravity) // Updating the position,rotation of a fragment while simulating , falling , spinning behaviour
    {
        if (broken) {
            velocityY += gravity; // increases fragment falling speed over time
            centerX += velocityX; // moves frgament  left or right
            centerY += velocityY; // moves fragment upp oor down
            rotation += rotationSpeed; // rotates the fragment
        }
    }

    Shape getTransformedShape() {
        if (!broken) {
            return shape;
        }

        AffineTransform transform = new AffineTransform();
        Rectangle2D bounds = shape.getBounds2D();

        // Original center position of the shape
        double origCenterX = bounds.getCenterX();
        double origCenterY = bounds.getCenterY();

        // moves the shape from original to new center
        double deltaX = centerX - origCenterX;
        double deltaY = centerY - origCenterY;
        transform.translate(deltaX, deltaY);

        transform.rotate(rotation, origCenterX, origCenterY);

        return transform.createTransformedShape(shape);
    }

    boolean isOffScreen(int width, int height) // checking if a fragment has moved outside of the frame nd no longer needs rendering/updating
    {
        int centerScreenX = width / 2;
        int centerScreenY = height / 2;
        double screenX = centerScreenX + centerX;
        double screenY = centerScreenY + centerY;

        return screenY > height + 100 || screenX < -100 || screenX > width + 100; // checking if the fragment is shattered successfully nd too far off screen
    }
}
