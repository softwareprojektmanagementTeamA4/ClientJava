package images;
public class Background {
    public static final Background HILLS = new Background(5, 5, 1280, 480);
    public static final Background SKY = new Background(5, 495, 1280, 480);
    public static final Background TREES = new Background(5, 985, 1280, 480);

    private int x;
    private int y;
    private int w;
    private int h;

    public Background(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }
}