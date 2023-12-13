import javafx.geometry.Point3D;

class Point3D_2 {
    private Point3D_3 world;
    private Point3D_3 camera;
    private Point2D_2 screen;

    public Point3D_2(double x, double y, double z) {
        this.camera = new Point3D_3(x,y,z);
        this.world = new Point3D_3(x,y,z);
        this.screen = new Point2D_2(0,0,0,0);
    }

    public Point3D_3 getCamera() {
        return camera;
    }
    public Point3D_3 getWorld() {
        return world;
    }

    public void setWorld(Point3D_3 world) {
        this.world = world;
    }

    public Point2D_2 getScreen() {
        return screen;
    }

    public void setScreen(Point2D_2 screen) {
        this.screen = screen;
    }
}
