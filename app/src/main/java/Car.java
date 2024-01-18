import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;


public class Car implements Serializable{
    
    @JsonProperty("position")
    private double z;
    @JsonProperty("speed")
    private double speed;
    @JsonProperty("playerX")
    private double offset;
    private double percent;
    private Sprite sprite;

    @JsonProperty("id")
    private String id;
    @JsonProperty("username")
    private String username;
    @JsonProperty("player_num")
    private int player_num;
    @JsonProperty("nitro")
    private boolean nitro;
    @JsonProperty("current_lap")
    private int current_lap;

    public Car() {
    }

    public Car(double z, double speed, double offset, double percent, Sprite sprite) {
        this.z = z;
        this.speed = speed;
        this.offset = offset;
        this.percent = percent;
        this.sprite = sprite;
    }

    public Car(double offset, double z, Sprite sprite, double speed) {
        this.offset = offset;
        this.z = z;
        this.sprite = sprite;
        this.speed = speed;
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

    public int getPlayer_num() {
        return player_num;
    }

    public String getUsername() {
        return username;
    }

    public String getId() {
        return id;
    }

    public int getCurrent_lap() {
        return current_lap;
    }

    public boolean getIsNitro() {
        return nitro;
    }

    public void setCurrent_lap(int current_lap) {
        this.current_lap = current_lap;
    }

    public void setNitro(boolean nitro) {
        this.nitro = nitro;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPlayer_num(int player_num) {
        this.player_num = player_num;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public void setId(String id) {
        this.id = id;
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
