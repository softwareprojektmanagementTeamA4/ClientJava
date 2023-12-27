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
//TODO: project(World, Scale) 

public class Util {
    private static final Random random = new Random();
    public static double increase(double start, double increment, double max) {
        /*
         * Erhöhe den Wert um das Inkrement, überschreite aber nicht das Maximum
         * Laufe um, falls wir das Maximum überschreiten
         */
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
        /*
         * Erhöhe den Speed um das Inkrement, überschreite aber nicht das Maximum
         * Laufe um, falls wir das Maximum überschreiten
         */
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
        p.getCamera().setZ((!Double.isNaN(worldZ) ? worldZ : 0) - cameraZ); // Gucken ob gleiche Funktionalität
        p.getScreen().setScale(cameraDepth / p.getCamera().getZ());
        p.getScreen().setX(Math.round((width / 2) + (p.getScreen().getScale() * p.getCamera().getX() * width / 2)));
        p.getScreen().setY(Math.round((height / 2) - (p.getScreen().getScale() * p.getCamera().getY() * height / 2)));
        p.getScreen().setWidth(Math.round(p.getScreen().getScale() * roadWidth * width / 2));
    }

    
    public int randomInt(int low, int high) {
        return random.nextInt(high - low + 1) + low;
    }

    public int randomChoice(int[] options) {
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
                // Handle the exception or log if needed
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
}
