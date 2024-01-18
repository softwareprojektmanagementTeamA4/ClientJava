import java.io.Serializable;


public class Sprite implements Serializable {
        private double x;
        private double y;
        private double w;
        private double h;
        private double offset;
        private Sprite source;
        private String name;

        public Sprite(double x, double y, double w, double h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        public Sprite(double offset, Sprite source) {
            this.offset = offset;
            this.source = source;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
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

    }
