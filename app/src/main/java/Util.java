import javafx.geometry.Point3D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import java.util.Random;
import java.lang.Math;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.paint.Color;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import java.util.List;
//TODO: project(World, Scale) 

public class Util {

    /**
     * Returns a random value
     * @param start
     * @param increment
     * @param max
     * @return new random value between 0 and max
     */
    private static final Random random = new Random();
    public static double increase(double start, double increment, double max) {
        double result = start + increment;
            
        while (result >= max) {
            result -= max;
        }
        while (result < 0) {
                result += max;
        }
            
        return result;
        }


    public double accelerate(double current_speed, double increment, double delta_time) {
        return current_speed + (increment * delta_time);
    }

    public double limit (double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }

    public double percentRemaining(double n, double total) {
        return (n % total) / total;
    }

    public double exponentialFog(double distance, double density) {
        return 1 / Math.pow(Math.E, (distance * distance * density));
    }

    /**
     * For pint projection on the screen
     * @param p
     * @param cameraX
     * @param cameraY
     * @param cameraZ
     * @param cameraDepth
     * @param width
     * @param height
     * @param roadWidth
     */
    public void project(
        Point3D_2 p, 
        double cameraX, 
        double cameraY, 
        double cameraZ, 
        double cameraDepth, 
        double width, 
        double height, 
        double roadWidth
        ) {
        double worldX = p.getWorld().getX();
        double worldY = p.getWorld().getY();
        double worldZ = p.getWorld().getZ();
        p.getCamera().setX((!Double.isNaN(worldX) ? worldX : 0) - cameraX);
        p.getCamera().setY((!Double.isNaN(worldY) ? worldY : 0) - cameraY);
        p.getCamera().setZ((!Double.isNaN(worldZ) ? worldZ : 0) - cameraZ);
        p.getScreen().setScale(cameraDepth / p.getCamera().getZ());
        p.getScreen().setX(Math.round((width / 2) + (p.getScreen().getScale() * p.getCamera().getX() * width / 2)));
        p.getScreen().setY(Math.round((height / 2) - (p.getScreen().getScale() * p.getCamera().getY() * height / 2)));
        p.getScreen().setWidth(Math.round(p.getScreen().getScale() * roadWidth * width / 2));
    }

    
    public static int randomInt(int low, int high) {
        return (int) Math.round(interpolate(low, high, random.nextFloat()));
    }

    public static <T> T randomChoice(List<T> options) {
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("Die Optionen dürfen nicht null oder leer sein.");
        }

        int randomIndex = randomInt(0, options.size() - 1);
        return options.get(randomIndex);
    }

    public double randomChoiceDouble(double[] options) {
        int randomIndex = randomInt(0, options.length - 1);
        return options[randomIndex];
    }

    public static double interpolate(double a, double b, double percent) {
        return a + (b - a) * percent;
    }

    public static int toInt(Object obj, int def) {
        if (obj != null) {
            try {
                return Integer.parseInt(obj.toString());
            } catch (NumberFormatException e) {
            }
        }
        return def;
    }

    public double easeIn(double a, double b, double percent) {
        return (a + (b - a) * Math.pow(percent, 2));
    }
    
    public double easeInOut(double a, double b, double percent) {
        return (a + (b - a) * (1 - Math.pow(1 - percent, 2)));
    }

    /**
     * Checks if two objects overlap
     * @param x1
     * @param w1
     * @param x2
     * @param w2
     * @param percent
     * @return true if the objects overlap
     */
    public static boolean overlap(double x1, double w1, double x2, double w2, double percent) {
        double half = (percent != 0) ? percent / 2 : 0.5;
        double min1 = x1 - (w1 * half);
        double max1 = x1 + (w1 * half);
        double min2 = x2 - (w2 * half);
        double max2 = x2 + (w2 * half);
        return !((max1 < min2) || (min1 > max2));
    }
}
