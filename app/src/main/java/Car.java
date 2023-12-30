public class Car {
    double z;
    double speed;
    double offset;
    double percent;
    Sprite sprite;



    public Car(double z, double speed, double offset, double percent, Sprite sprite) {
        this.z = z;
        this.speed = speed;
        this.offset = offset;
        this.percent = percent;
        this.sprite = sprite;
    }

    public double getZ() {
        return z;
    }

    public double getSpeed() {
        return speed;
    }

    public double getOffset() {
        return offset;
    }

    public Sprite getSprite() {
        return sprite;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }

    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }

    public double getPercent() {
        return percent;
    }

}
