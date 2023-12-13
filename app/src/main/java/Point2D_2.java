public class Point2D_2{
    private double width;
    private double scale;
    private double x;
    private double y;

    Point2D_2(double x, double y, double width, double scale){
        this.x = x;
        this.y = y;
        this.width = width;
        this.scale = scale;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }
}