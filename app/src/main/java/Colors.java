import javafx.scene.paint.Color;

public class Colors {
    private static final Color SKY = Color.web("#72D7EE");
    private static final Color TREE = Color.web("#005108");
    private static final Color FOG = Color.web("#005108");

    private static final Color ROAD_LIGHT = Color.web("#6B6B6B");
    static final Color GRASS_LIGHT = Color.web("#10AA10");
    private static final Color RUMBLE_LIGHT = Color.web("#555555");
    static final Color LANE_LIGHT = Color.web("#CCCCCC");

    private static final Color ROAD_DARK = Color.web("#6B6B6B");
    static final Color GRASS_DARK = Color.web("#009A00");
    static final Color RUMBLE_DARK = Color.web("#BBBBBB");

    private static final Color ROAD_START = Color.web("#FFFFFF");
    private static final Color GRASS_START = Color.web("#FFFFFF");
    private static final Color RUMBLE_START = Color.web("#FFFFFF");

    private static final Color ROAD_FINISH = Color.web("#000000");
    private static final Color GRASS_FINISH = Color.web("#000000");
    private static final Color RUMBLE_FINISH = Color.web("#000000");

    public static Color getRumbleColor(int segmentIndex, int rumbleLength) {
        if ((segmentIndex / rumbleLength) % 2 == 0) {
            return RUMBLE_LIGHT;
        } else {
            return RUMBLE_DARK;
        }
    }

    public static Color getRoadColorLight() {
        return ROAD_LIGHT;
    }

    public static Color getRoadColorDark() {
        return ROAD_DARK;
    }

    public static Color getGrassColorLight() {
        return GRASS_LIGHT;
    }

    public static Color getGrassColorDark() {
        return GRASS_DARK;
    }

    public static Color getGrassColor(int segmentIndex, int rumbleLength) {
        if ((segmentIndex / rumbleLength) % 2 == 0) {
            return GRASS_LIGHT;
        } else {
            return GRASS_DARK;
        }
    }

    public static Color getRumbleColorDark() {
        return RUMBLE_DARK;
    }

    public static Color getRoadStart() {
        return ROAD_START;
    }
    public static Color getRoadFinish() {
        return ROAD_FINISH;
    }

    public static Color getFogColor() {
        return FOG;
    }


}