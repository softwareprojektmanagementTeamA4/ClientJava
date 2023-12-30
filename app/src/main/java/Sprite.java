import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;


public class Sprite {
        private double x;
        private double y;
        private double w;
        private double h;
        private double offset;
        private Sprite source;

        public Sprite(double x, double y, double w, double h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        public Sprite(double x, double y, double w, double h, double offset, Sprite source) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.offset = offset;
            this.source = source;
        }
        public Sprite(double offset, Sprite source) {
            this.offset = offset;
            this.source = source;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getW() {
            return w;
        }

        public double getH() {
            return h;
        }

        public double getOffset(){
            return offset;
        }

        public Sprite getSource() {
            return source;
        }

        public void setSource(Sprite source) {
            this.source = source;
        }
        public void setOffset(double offset) {
            this.offset = offset;
        }

        public Image getImage() { 
        Image spriteSheet = new Image("file:src/main/java/images/sprites.png");
        PixelReader pixelReader = spriteSheet.getPixelReader();
        WritableImage croppedImage = new WritableImage(pixelReader, (int) x, (int) y, (int) w, (int) h);
        return croppedImage;
        }
    }
