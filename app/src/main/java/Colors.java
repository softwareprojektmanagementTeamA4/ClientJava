import javafx.scene.paint.Color;

public class Colors {
    public static final Color SKY = Color.web("#72D7EE");
    public static final Color TREE = Color.web("#005108");
    public static final Color FOG = Color.web("#005108");

    public static final Color ROAD_LIGHT = Color.web("#6B6B6B");
    public static final Color GRASS_LIGHT = Color.web("#10AA10");
    public static final Color RUMBLE_LIGHT = Color.web("#555555");
    public static final Color LANE_LIGHT = Color.web("#CCCCCC");

    public static final Color ROAD_DARK = Color.web("#6B6B6B");
    public static final Color GRASS_DARK = Color.web("#009A00");
    public static final Color RUMBLE_DARK = Color.web("#BBBBBB");

    public static final Color ROAD_START = Color.web("#FFFFFF");
    public static final Color GRASS_START = Color.web("#FFFFFF");
    public static final Color RUMBLE_START = Color.web("#FFFFFF");

    public static final Color ROAD_FINISH = Color.web("#000000");
    public static final Color GRASS_FINISH = Color.web("#000000");
    public static final Color RUMBLE_FINISH = Color.web("#000000");

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
        return ROAD_LIGHT;
    }

    public static Color getGrassColorLight() {
        return GRASS_LIGHT;
    }

    public static Color getGrassColorDark() {
        return GRASS_LIGHT;
    }

    public static Color getGrassColor(int segmentIndex, int rumbleLength) {
        if ((segmentIndex / rumbleLength) % 2 == 0) {
            return GRASS_LIGHT;
        } else {
            return GRASS_DARK;
        }
    }


}