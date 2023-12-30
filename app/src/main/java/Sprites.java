import java.util.Arrays;
import java.util.List;
import javafx.scene.image.Image;

public class Sprites {

public static Sprite PALM_TREE = new Sprite(5, 5, 215, 540);
public static Image palm_tree = PALM_TREE.getImage();

public static Sprite BILLBOARD08 = new Sprite(230, 5, 385, 265);
public static Image billboard08 = BILLBOARD08.getImage();

public static Sprite TREE1 = new Sprite(625, 5, 360, 360);
public static Image tree1 = TREE1.getImage();

public static Sprite DEAD_TREE1 = new Sprite(5, 555, 135, 332);
public static Image dead_tree1 = DEAD_TREE1.getImage();

public static Sprite BILLBOARD09 = new Sprite(150, 555, 328, 282);
public static Image billboard09 = BILLBOARD09.getImage();

public static Sprite BOULDER3 = new Sprite(230, 280, 320, 220);
public static Image boulder3 = BOULDER3.getImage();

public static Sprite COLUMN = new Sprite(995, 5, 200, 315);
public static Image column = COLUMN.getImage();
    
public static Sprite BILLBOARD01 = new Sprite(625, 375, 300, 170);
public static Image billboard01 = BILLBOARD01.getImage();

public static Sprite BILLBOARD06 = new Sprite(488, 555, 298, 190);
public static Image billboard06 = BILLBOARD06.getImage();

public static Sprite BILLBOARD05 = new Sprite(5, 897, 298, 190);
public static Image billboard05 = BILLBOARD05.getImage();

public static Sprite BILLBOARD07 = new Sprite(313, 897, 298, 190);
public static Image billboard07 = BILLBOARD07.getImage();

public static Sprite BOULDER2 = new Sprite(621, 897, 298, 140);
public static Image boulder2 = BOULDER2.getImage();

public static Sprite TREE2 = new Sprite(1205, 5, 282, 295);
public static Image tree2 = TREE2.getImage();

public static Sprite BILLBOARD04 = new Sprite(1205, 310, 268, 170);
public static Image billboard04 = BILLBOARD04.getImage();

public static Sprite DEAD_TREE2 = new Sprite(1205, 490, 150, 260);
public static Image dead_tree2 = DEAD_TREE2.getImage();

public static Sprite BOULDER1 = new Sprite(1205, 760, 168, 248);
public static Image boulder1 = BOULDER1.getImage();

public static Sprite BUSH1 = new Sprite(5, 1097, 240, 155);
public static Image bush1 = BUSH1.getImage();

public static Sprite CACTUS = new Sprite(929, 897, 235, 118);
public static Image cactus = CACTUS.getImage();

public static Sprite BUSH2 = new Sprite(255, 1097, 232, 152);
public static Image bush2 = BUSH2.getImage();

public static Sprite BILLBOARD03 = new Sprite(5, 1262, 230, 220);
public static Image billboard03 = BILLBOARD03.getImage();

public static Sprite BILLBOARD02 = new Sprite(245, 1262, 215, 220);
public static Image billboard02 = BILLBOARD02.getImage();

public static Sprite STUMP = new Sprite(995, 330, 195, 140);
public static Image stump = STUMP.getImage();

public static Sprite SEMI = new Sprite(1365, 490, 122, 144);
public static Image semi = SEMI.getImage();

public static Sprite TRUCK = new Sprite(1365, 644, 100, 78);
public static Image truck = TRUCK.getImage();

public static Sprite CAR03 = new Sprite(1383, 760, 88, 55);
public static Image car03 = CAR03.getImage();

public static Sprite CAR02 = new Sprite(1383, 825, 80, 59);
public static Image car02 = CAR02.getImage();

public static Sprite CAR04 = new Sprite(1383, 894, 80, 57);
public static Image car04 = CAR04.getImage();

public static Sprite CAR01 = new Sprite(1205, 1018, 80, 56);
public static Image car01 = CAR01.getImage();

public static Sprite PLAYER_UPHILL_LEFT = new Sprite(1383, 961, 80, 45);
public static Image player_uphill_left = PLAYER_UPHILL_LEFT.getImage();

public static Sprite PLAYER_UPHILL_STRAIGHT = new Sprite(1295, 1018, 80, 45);
public static Image player_uphill_straight = PLAYER_UPHILL_STRAIGHT.getImage();

public static Sprite PLAYER_UPHILL_RIGHT = new Sprite(1385, 1018, 80, 45);
public static Image player_uphill_right = PLAYER_UPHILL_RIGHT.getImage();

public static Sprite PLAYER_LEFT = new Sprite(995, 480, 80, 41);
public static Image player_left = PLAYER_LEFT.getImage();

public static Sprite PLAYER_STRAIGHT = new Sprite(1085, 480, 80, 41);
public static Image player_straight = PLAYER_STRAIGHT.getImage();

public static Sprite PLAYER_RIGHT = new Sprite(995, 531, 80, 41);
public static Image player_right = PLAYER_RIGHT.getImage();

    public static double SCALE = 0.3 * (1 / PLAYER_STRAIGHT.getW()); // the reference sprite width should be 1/3rd the (half-)roadWidth
    

    public static double getScale() {
        return SCALE;
    }

    public final List<Sprite> BILLBOARDS = Arrays.asList(
            BILLBOARD01, BILLBOARD02, BILLBOARD03, BILLBOARD04, BILLBOARD05, BILLBOARD06, BILLBOARD07, BILLBOARD08, BILLBOARD09
    );

    public final List<Sprite> PLANTS = Arrays.asList(
        PALM_TREE, DEAD_TREE1, DEAD_TREE2, BOULDER1, BOULDER2, BOULDER3, COLUMN, TREE1, TREE2, CACTUS, BUSH1, BUSH2, STUMP
    );

    public final List<Sprite> CARS = Arrays.asList(
        CAR01, CAR02, CAR03, CAR04, TRUCK, SEMI
    );

    public Sprite getCar(int index) {
        return CARS.get(index);
    }

    public Sprite getBillboard(int index) {
        return BILLBOARDS.get(index);
    }

    public Sprite getPlant(int index) {
        return PLANTS.get(index);
    }
}