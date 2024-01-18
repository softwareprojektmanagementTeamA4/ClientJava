import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Hud {
    private DoubleProperty speed;
    private StringProperty currentLapTime;
    private StringProperty lastLapTime;
    private StringProperty fastLapTime;

    public Hud() {
        this.speed = new SimpleDoubleProperty();
        this.currentLapTime = new SimpleStringProperty();
        this.lastLapTime = new SimpleStringProperty();
        this.fastLapTime = new SimpleStringProperty();
    }

    public double getSpeed() {
        return speed.get();
    }

    public void setSpeed(double speed) {
        this.speed.set(speed);
    }

    public DoubleProperty speedProperty() {
        return speed;
    }

    public String getCurrentLapTime() {
        return currentLapTime.get();
    }

    public void setCurrentLapTime(String currentLapTime) {
        this.currentLapTime.set(currentLapTime);
    }

    public StringProperty currentLapTimeProperty() {
        return currentLapTime;
    }

    public String getLastLapTime() {
        return lastLapTime.get();
    }

    public void setLastLapTime(String lastLapTime) {
        this.lastLapTime.set(lastLapTime);
    }

    public StringProperty lastLapTimeProperty() {
        return lastLapTime;
    }

    public String getFastLapTime() {
        return fastLapTime.get();
    }

    public void setFastLapTime(String fastLapTime) {
        this.fastLapTime.set(fastLapTime);
    }

    public StringProperty fastLapTimeProperty() {
        return fastLapTime;
    }
}