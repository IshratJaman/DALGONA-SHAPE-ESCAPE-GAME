import java.awt.geom.*;
import java.util.ArrayList;

public class ShapeFactory {

    public static Path2D.Double createCentralShape(ShapeType shapeType, int size) {
        Path2D.Double shape = new Path2D.Double();


        switch (shapeType) {
            // CIRCLE SHAPE
            case CIRCLE:
                shape.append(new Ellipse2D.Double(-size/2.0, -size/2.0, size, size), false);
                break;

            // TRIANGLE SHAPE
            case TRIANGLE:
                double triangleSize = size * 1.2 ;
                shape.moveTo(0,  -triangleSize/2.0);
                shape.lineTo(-triangleSize/2.0, triangleSize/2.0);
                shape.lineTo(triangleSize/2.0, triangleSize/2.0);
                shape.closePath();
                break;

            // SQUARE SHAPE
            case SQUARE:
                double squareSize = size;
                shape.append(new Rectangle2D.Double(-squareSize /2.0, -squareSize /2.0, squareSize, squareSize), false);
                break;

            // DIAMOND SHAPE
            case DIAMOND:
                double diamondSize = (size * 1.3) / 100.0;
                shape.moveTo(0, -60 * diamondSize);
                shape.lineTo(40 * diamondSize, 0);
                shape.lineTo(0, 60 * diamondSize);
                shape.lineTo(-40 * diamondSize, 0);
                shape.closePath();
                break;

            // HEART SHAPE
            case HEART:
                double heartSize = (size * 1.4) / 80.0;
                shape.moveTo(0, 0);
                shape.curveTo(-12 * heartSize, -12 * heartSize, -25 * heartSize, 5 * heartSize, 0, 25 * heartSize);
                shape.curveTo(25 * heartSize, 5 * heartSize, 12 * heartSize, -12 * heartSize, 0, 0);
                shape.closePath();
                break;

            // STAR SHAPE
            case STAR:
                double starSize = size * 1.2;
                for (int i = 0; i < 10; i++) {
                    double angle = Math.PI * i / 5;
                    double radius = (i % 2 == 0) ? starSize/2.0 : starSize/4.0;
                    double x = radius * Math.cos(angle - Math.PI/2);
                    double y = radius * Math.sin(angle - Math.PI/2);
                    if (i == 0) {
                        shape.moveTo(x, y);
                    }
                    else {
                        shape.lineTo(x, y);
                    }
                }
                shape.closePath();
                break;

                // UMBRELLA SHAPE
                case UMBRELLA:
                double umbrellaSize = (size * 1.3) / 60.0;
                shape.append(new Arc2D.Double(-25 * umbrellaSize, -15 * umbrellaSize,
                        50 * umbrellaSize, 30 * umbrellaSize, 0, 180, Arc2D.CHORD), false);
                shape.moveTo(0, 0);
                shape.lineTo(0, 30 * umbrellaSize);
                shape.lineTo(8 * umbrellaSize, 30 * umbrellaSize);
                break;

            // CRESCENT MOON SHAPE
            case CRESCENT_MOON:
                double moonSize = (size * 1.2) / 60.0;
                Ellipse2D.Double outer = new Ellipse2D.Double(-25 * moonSize, -25 * moonSize,
                        50 * moonSize, 50 * moonSize);
                Ellipse2D.Double inner = new Ellipse2D.Double(-15 * moonSize, -25 * moonSize,
                        50 * moonSize, 50 * moonSize);
                Area outerArea = new Area(outer);
                Area innerArea = new Area(inner);
                outerArea.subtract(innerArea);
                shape.append(outerArea, false);
                break;
        }

        return shape;
    }

    public static ArrayList<Fragment> generateFragments(int candyRadius, int level) {
        ArrayList<Fragment> fragments = new ArrayList<>();

        int NumOfMainFragments = 15 + level;

        // Main fragments for all levels
        for (int i = 0; i < NumOfMainFragments; i++) {
            double startAngle = (2 * Math.PI * i) / NumOfMainFragments;
            double endAngle = (2 * Math.PI * (i + 1)) / NumOfMainFragments;

            // Angle variation for each fragment
            double angleVariation = (Math.random() - 0.5) * 0.05;
            startAngle += angleVariation;
            endAngle += angleVariation;

            Path2D.Double fragmentShape = new Path2D.Double();
            fragmentShape.moveTo(0, 0);

            // Creating outer fragments
            int steps = 4;
            for (int j = 0; j <= steps; j++) {
                double angle = startAngle + (endAngle - startAngle) * j / steps;

                double radiusVariation = 0.9 + Math.random() * 0.15; // Making the fragment's curve edge jagged or cracked
                double x = candyRadius * radiusVariation * Math.cos(angle);
                double y = candyRadius * radiusVariation * Math.sin(angle);

                if(j == 0) {
                    fragmentShape.lineTo(x, y);
                } else {
                    fragmentShape.lineTo(x, y);
                }
            }
            fragmentShape.closePath();

            Rectangle2D bounds = fragmentShape.getBounds2D();
            if (bounds.getWidth() > 18 && bounds.getHeight() > 18) // Each fragment must be 18 pixel width nd tall
            {
                fragments.add(new Fragment(fragmentShape));
            }
        }

        int NumOfInnerFragments = 6 + (level / 2);

        for (int i = 0; i < NumOfInnerFragments; i++) {
            Path2D.Double fragmentShape = new Path2D.Double();

            // Creating inner fragments
            int NumOfPoints = 4; // Inner fragments -> quadric/rectangle/diamond like this
            double centerRadius = candyRadius * (0.5 + Math.random() * 0.25);

            double[] angles = new double[NumOfPoints];
            double[] radii = new double[NumOfPoints];

            for (int j = 0; j < NumOfPoints; j++) {
                // Variation for predictable fragments
                double angleVariation = (Math.random() - 0.5) * 0.2;
                double radiusVariation = 0.8 + Math.random() * 0.2;

                angles[j] = (2 * Math.PI * j) / NumOfPoints + angleVariation;
                radii[j] = centerRadius * radiusVariation;
            }

            for (int j = 0; j < NumOfPoints; j++) {
                double x = radii[j] * Math.cos(angles[j]);
                double y = radii[j] * Math.sin(angles[j]);
                if (j == 0) {
                    fragmentShape.moveTo(x, y);
                }
                else {
                    fragmentShape.lineTo(x, y);
                }
            }
            fragmentShape.closePath();

            // Minimum size for inner fragments
            Rectangle2D bounds = fragmentShape.getBounds2D();
            if (bounds.getWidth() > 25 && bounds.getHeight() > 25) {
                fragments.add(new Fragment(fragmentShape));
            }
        }

        return fragments;
    }
}