import javafx.scene.paint.Color;

public class Colors {
    public static final Color SKY = Color.rgb(114, 215, 238);
    public static final Color TREE = Color.rgb(0, 81, 8);
    public static final Color FOG = Color.rgb(0, 81, 8);

    public static final Color ROAD_LIGHT = Color.rgb(107, 107, 107);
    public static final Color GRASS_LIGHT = Color.rgb(16, 170, 16);
    public static final Color RUMBLE_LIGHT = Color.rgb(0, 0, 0);
    public static final Color LANE_LIGHT = Color.rgb(204, 204, 204);

    public static final Color ROAD_DARK = Color.rgb(107, 107, 107);
    public static final Color GRASS_DARK = Color.rgb(0, 154, 0);
    public static final Color RUMBLE_DARK = Color.rgb(255, 255, 255);

    public static final Color ROAD_START = Color.WHITE;
    public static final Color GRASS_START = Color.WHITE;
    public static final Color RUMBLE_START = Color.WHITE;

    public static final Color ROAD_FINISH = Color.BLACK;
    public static final Color GRASS_FINISH = Color.BLACK;
    public static final Color RUMBLE_FINISH = Color.BLACK;

    public static Color getRumbleColor(int segmentIndex, int rumbleLength) {
        if ((segmentIndex / rumbleLength) % 2 == 0) {
            return RUMBLE_LIGHT;
        } else {
            return RUMBLE_DARK;
        }
    }

    public static Color getRoadColor() {
        return ROAD_LIGHT;
    }

    public static Color getGrassColor() {
        return GRASS_LIGHT;
    }

}