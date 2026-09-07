import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Random;

public class ShapeFactory {

    private static final Random random = new Random();

    public static Path2D.Double createCentralShape(ShapeType shapeType, int size) {
        Path2D.Double shape = new Path2D.Double();

        switch (shapeType) {
            case CIRCLE:
                double cw = size * 0.58;
                double ch = size * 0.50;
                shape.append(new Ellipse2D.Double(-cw, -ch, cw * 2.0, ch * 2.0), false);
                break;

            case TRIANGLE:
                double tw = size * 0.70;
                double th = size * 0.46;
                shape.moveTo(0, -th);
                shape.lineTo(-tw, th);
                shape.lineTo(tw, th);
                shape.closePath();
                break;

            case SQUARE:
                double sw = size * 0.58;
                double sh = size * 0.48;
                shape.append(new Rectangle2D.Double(-sw, -sh, sw * 2.0, sh * 2.0), false);
                break;

            case HEXAGON:
                // Broader and wider hexagon
                for (int i = 0; i < 6; i++) {
                    double angle = Math.PI * i / 3.0 - Math.PI / 2.0;
                    double x = (size * 0.68) * Math.cos(angle);
                    double y = (size * 0.50) * Math.sin(angle);
                    if (i == 0) {
                        shape.moveTo(x, y);
                    } else {
                        shape.lineTo(x, y);
                    }
                }
                shape.closePath();
                break;

            case HEART:
                // Broader and wider heart shape with plump lobes
                double scale = size / 80.0;
                shape.moveTo(0, -8 * scale);
                // Left plump broad lobe
                shape.curveTo(-10 * scale, -28 * scale, -46 * scale, -28 * scale, -46 * scale, -6 * scale);
                shape.curveTo(-46 * scale, 14 * scale, -24 * scale, 26 * scale, 0, 36 * scale);
                // Right plump broad lobe
                shape.curveTo(24 * scale, 26 * scale, 46 * scale, 14 * scale, 46 * scale, -6 * scale);
                shape.curveTo(46 * scale, -28 * scale, 10 * scale, -28 * scale, 0, -8 * scale);
                shape.closePath();
                break;

            case STAR:
                // Broader and wider 5-pointed star
                for (int i = 0; i < 10; i++) {
                    double angle = Math.PI * i / 5;
                    double r = (i % 2 == 0) ? size * 0.52 : size * 0.28;
                    double x = r * 1.25 * Math.cos(angle - Math.PI / 2);
                    double y = r * Math.sin(angle - Math.PI / 2);
                    if (i == 0) {
                        shape.moveTo(x, y);
                    } else {
                        shape.lineTo(x, y);
                    }
                }
                shape.closePath();
                break;

            case UMBRELLA:
                // Umbrella shape - broad dome + handle
                double us = size / 100.0;
                // Broad dome (semicircle chord)
                shape.append(new Arc2D.Double(-60 * us, -38 * us, 120 * us, 56 * us, 0, 180, Arc2D.CHORD), false);
                // Broad stem
                Path2D.Double stem = new Path2D.Double();
                stem.moveTo(-5 * us, -10 * us);
                stem.lineTo(-5 * us, 30 * us);
                // Hook at bottom
                stem.curveTo(-5 * us, 44 * us, 18 * us, 44 * us, 18 * us, 30 * us);
                stem.lineTo(18 * us, 28 * us);
                stem.curveTo(18 * us, 38 * us, 4 * us, 38 * us, 4 * us, 30 * us);
                stem.lineTo(4 * us, -10 * us);
                stem.closePath();
                shape.append(stem, false);
                break;

            case SQUID:
                // Squid Game squid shape - broad and wide
                double sq = size / 100.0;
                // Top head dome - smooth, wide, rounded top
                shape.moveTo(0, -38 * sq);
                shape.curveTo(-28 * sq, -38 * sq, -50 * sq, -24 * sq, -50 * sq, -8 * sq);
                // Left mantle - broad body
                shape.curveTo(-50 * sq, 4 * sq, -42 * sq, 14 * sq, -38 * sq, 20 * sq);
                // Left outer fin/tentacle - flares wide!
                shape.lineTo(-60 * sq, 32 * sq);
                shape.lineTo(-38 * sq, 26 * sq);
                // Left inner tentacle
                shape.lineTo(-30 * sq, 38 * sq);
                shape.lineTo(-18 * sq, 26 * sq);
                // Center tentacle
                shape.lineTo(-8 * sq, 38 * sq);
                shape.lineTo(0, 24 * sq);
                shape.lineTo(8 * sq, 38 * sq);
                // Right inner tentacle
                shape.lineTo(18 * sq, 26 * sq);
                shape.lineTo(30 * sq, 38 * sq);
                // Right outer fin/tentacle - flares wide!
                shape.lineTo(38 * sq, 26 * sq);
                shape.lineTo(60 * sq, 32 * sq);
                // Right mantle - broad body
                shape.lineTo(38 * sq, 20 * sq);
                shape.curveTo(42 * sq, 14 * sq, 50 * sq, 4 * sq, 50 * sq, -8 * sq);
                // Top right head dome
                shape.curveTo(50 * sq, -24 * sq, 28 * sq, -38 * sq, 0, -38 * sq);
                shape.closePath();
                break;
        }

        return shape;
    }

    /**
     * Generate fragments around the candy area but EXCLUDING the central protected shape.
     * Fragments are irregular polygon pieces that surround the target.
     */
    public static ArrayList<Fragment> generateFragments(int candyRadius, Path2D.Double centralShape) {
        ArrayList<Fragment> fragments = new ArrayList<>();

        // Create main radial wedge fragments (the candy disc)
        int numMainFragments = 14;
        for (int i = 0; i < numMainFragments; i++) {
            double startAngle = (2 * Math.PI * i) / numMainFragments;
            double endAngle = (2 * Math.PI * (i + 1)) / numMainFragments;

            Path2D.Double fragmentShape = new Path2D.Double();
            fragmentShape.moveTo(0, 0);

            int steps = 10;
            for (int j = 0; j <= steps; j++) {
                double angle = startAngle + (endAngle - startAngle) * j / steps;
                // Add slight irregularity to outer edge
                double radiusVariation = candyRadius * (0.9 + random.nextDouble() * 0.15);
                double x = radiusVariation * Math.cos(angle);
                double y = radiusVariation * Math.sin(angle);
                fragmentShape.lineTo(x, y);
            }
            fragmentShape.closePath();

            // Only add if the fragment doesn't overlap too much with central shape
            fragments.add(new Fragment(fragmentShape));
        }

        // Add scattered irregular fragments for variety
        int numRandomFragments = 10;
        for (int i = 0; i < numRandomFragments; i++) {
            Path2D.Double fragmentShape = new Path2D.Double();

            int numPoints = 4 + random.nextInt(3); // 4-6 points
            double baseAngle = random.nextDouble() * 2 * Math.PI;
            double centerRadius = candyRadius * (0.5 + random.nextDouble() * 0.4);
            double fragCenterX = centerRadius * Math.cos(baseAngle);
            double fragCenterY = centerRadius * Math.sin(baseAngle);

            double fragSize = candyRadius * (0.1 + random.nextDouble() * 0.15);

            for (int j = 0; j < numPoints; j++) {
                double angle = (2 * Math.PI * j) / numPoints + (random.nextDouble() - 0.5) * 0.8;
                double radius = fragSize * (0.6 + random.nextDouble() * 0.4);
                double x = fragCenterX + radius * Math.cos(angle);
                double y = fragCenterY + radius * Math.sin(angle);
                if (j == 0) {
                    fragmentShape.moveTo(x, y);
                } else {
                    fragmentShape.lineTo(x, y);
                }
            }
            fragmentShape.closePath();

            fragments.add(new Fragment(fragmentShape));
        }

        return fragments;
    }

    // Overload for backward compatibility
    public static ArrayList<Fragment> generateFragments(int candyRadius) {
        return generateFragments(candyRadius, null);
    }
}
