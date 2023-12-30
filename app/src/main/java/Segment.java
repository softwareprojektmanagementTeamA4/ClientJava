import javafx.scene.paint.Color;
import java.util.ArrayList;


class Segment {
    private int index;
    private Color color;
    private boolean looped;
    private double fog;
    private Point3D_2 p1;
    private Point3D_2 p2;
    private double clip;
    private double curve;
    private ArrayList<Car> cars = new ArrayList<>();
    private ArrayList<Sprite> sprites = new ArrayList<>();


    public Segment(int index) {
        this.index = index;
    }

    public Segment() {
    }

    public Segment(int index, Color color) {
        this.index = index;
        this.color = color;
    }
    public Segment(int index, Color color, double curve) {
        this.index = index;
        this.color = color;
        this.curve = curve;
    }

    public Segment(int index, Point3D_2 p1, Point3D_2 p2, double curve, Color color) {
        this.index = index;
        this.p1 = p1;
        this.p2 = p2;
        this.curve = curve;
        this.color = color;
    }

    public Segment(int index, Point3D_2 p1, Point3D_2 p2, Color color) {
        this.index = index;
        this.p1 = p1;
        this.p2 = p2;
        this.color = color;
    }

    public Segment(int index, Point3D_2 p1, Point3D_2 p2, Color color, boolean looped) {
        this.index = index;
        this.p1 = p1;
        this.p2 = p2;
        this.color = color;
        this.looped = looped;
    }
    
    public Segment(int index, Point3D_2 p1, Point3D_2 p2, Color color, boolean looped, double fog) {
        this.index = index;
        this.p1 = p1;
        this.p2 = p2;
        this.color = color;
        this.looped = looped;
        this.fog = fog;
    }

    public Segment(int index, Point3D_2 p1, Point3D_2 p2, Color color, boolean looped, double fog, int clip) {
        this.index = index;
        this.p1 = p1;
        this.p2 = p2;
        this.color = color;
        this.looped = looped;
        this.fog = fog;
        this.clip = clip;
    }

    public Segment(int index, Point3D_2 p1, Point3D_2 p2, Color color, boolean looped, double fog, int clip, Point2D_2 screen) {
        this.index = index;
        this.p1 = p1;
        this.p2 = p2;
        this.color = color;
        this.looped = looped;
        this.fog = fog;
        this.clip = clip;
    }


    public int getIndex() {
        return index;
    }

    public int setIndex(int index) {
        return this.index = index;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public boolean isLooped() {
        return looped;
    }

    public void setLooped(boolean looped) {
        this.looped = looped;
    }

    public double getFog() {
        return fog;
    }

    public void setFog(double fog) {
        this.fog = fog;
    }

    public Point3D_2 getP1() {
        return p1;
    }

    public Point3D_2 getP2() {
        return p2;
    }

    public double getClip() {
        return clip;
    }

    public void setClip(double clip) {
        this.clip = clip;
    }

    public double getCurve(){
        return curve;
    }

    public void setCurve(double curve){
        this.curve = curve;
    }

    public ArrayList<Car> getCars() {
        return cars;
    }

    public Car getCar(int index){
        return cars.get(index);
    }

    public ArrayList<Sprite> getSprites() {
        return sprites;
    }

    public Sprite getSprite(int index){
        return sprites.get(index);
    }
}

