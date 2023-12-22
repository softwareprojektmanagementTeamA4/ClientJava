import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;


public class Sprite {
        private final double x;
        private final double y;
        private final double w;
        private final double h;

        public Sprite(double x, double y, double w, double h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
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

        public Image getImage() {
        Image spriteSheet = new Image("file:src/main/resources/spritesheet.png");
        PixelReader pixelReader = spriteSheet.getPixelReader();
        WritableImage croppedImage = new WritableImage(pixelReader, (int) x, (int) y, (int) w, (int) h);
        return croppedImage;
        }
    }
