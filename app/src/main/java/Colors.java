import javafx.scene.paint.Color;

public class Colors {
    public static final Color SKY = Color.rgb(114, 215, 238);
    public static final Color TREE = Color.rgb(0, 81, 8);
    public static final Color FOG = Color.rgb(0, 81, 8);
    
    public static class Light {
        public static final Color ROAD = Color.rgb(107, 107, 107);
        public static final Color GRASS = Color.rgb(16, 170, 16);
        public static final Color RUMBLE = Color.rgb(85, 85, 85);
        public static final Color LANE = Color.rgb(204, 204, 204);
    }
    
    public static class Dark {
        public static final Color ROAD = Color.rgb(107, 107, 107);
        public static final Color GRASS = Color.rgb(0, 154, 0);
        public static final Color RUMBLE = Color.rgb(187, 187, 187);
    }
    
    public static class Start {
        public static final Color ROAD = Color.WHITE;
        public static final Color GRASS = Color.WHITE;
        public static final Color RUMBLE = Color.WHITE;
    }
    
    public static class Finish {
        public static final Color ROAD = Color.BLACK;
        public static final Color GRASS = Color.BLACK;
        public static final Color RUMBLE = Color.BLACK;
    }
}