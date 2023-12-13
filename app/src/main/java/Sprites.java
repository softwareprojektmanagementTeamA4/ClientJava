import java.util.Arrays;
import java.util.List;

public class Sprites {

    public static final Sprite PALM_TREE = new Sprite(5, 5, 215, 540);
    public static final Sprite BILLBOARD08 = new Sprite(230, 5, 385, 265);
    public static final Sprite TREE1 = new Sprite(625, 5, 360, 360);
    public static final Sprite DEAD_TREE1 = new Sprite(5, 555, 135, 332);
    public static final Sprite BILLBOARD09 = new Sprite(150, 555, 328, 282);
    public static final Sprite BOULDER3 = new Sprite(230, 280, 320, 220);
    public static final Sprite COLUMN = new Sprite(995, 5, 200, 315);
    public static final Sprite BILLBOARD01 = new Sprite(625, 375, 300, 170);
    public static final Sprite BILLBOARD06 = new Sprite(488, 555, 298, 190);
    public static final Sprite BILLBOARD05 = new Sprite(5, 897, 298, 190);
    public static final Sprite BILLBOARD07 = new Sprite(313, 897, 298, 190);
    public static final Sprite BOULDER2 = new Sprite(621, 897, 298, 140);
    public static final Sprite TREE2 = new Sprite(1205, 5, 282, 295);
    public static final Sprite BILLBOARD04 = new Sprite(1205, 310, 268, 170);
    public static final Sprite DEAD_TREE2 = new Sprite(1205, 490, 150, 260);
    public static final Sprite BOULDER1 = new Sprite(1205, 760, 168, 248);
    public static final Sprite BUSH1 = new Sprite(5, 1097, 240, 155);
    public static final Sprite CACTUS = new Sprite(929, 897, 235, 118);
    public static final Sprite BUSH2 = new Sprite(255, 1097, 232, 152);
    public static final Sprite BILLBOARD03 = new Sprite(5, 1262, 230, 220);
    public static final Sprite BILLBOARD02 = new Sprite(245, 1262, 215, 220);
    public static final Sprite STUMP = new Sprite(995, 330, 195, 140);
    public static final Sprite SEMI = new Sprite(1365, 490, 122, 144);
    public static final Sprite TRUCK = new Sprite(1365, 644, 100, 78);
    public static final Sprite CAR03 = new Sprite(1383, 760, 88, 55);
    public static final Sprite CAR02 = new Sprite(1383, 825, 80, 59);
    public static final Sprite CAR04 = new Sprite(1383, 894, 80, 57);
    public static final Sprite CAR01 = new Sprite(1205, 1018, 80, 56);
    public static final Sprite PLAYER_UPHILL_LEFT = new Sprite(1383, 961, 80, 45);
    public static final Sprite PLAYER_UPHILL_STRAIGHT = new Sprite(1295, 1018, 80, 45);
    public static final Sprite PLAYER_UPHILL_RIGHT = new Sprite(1385, 1018, 80, 45);
    public static final Sprite PLAYER_LEFT = new Sprite(995, 480, 80, 41);
    public static final Sprite PLAYER_STRAIGHT = new Sprite(1085, 480, 80, 41);
    public static final Sprite PLAYER_RIGHT = new Sprite(995, 531, 80, 41);

    public static double SCALE = 0.3 * (1 / PLAYER_STRAIGHT.getW()); // the reference sprite width should be 1/3rd the (half-)roadWidth
    

    public static double getScale() {
        return SCALE;
    }

    public static final List<Sprite> BILLBOARDS = Arrays.asList(
            BILLBOARD01, BILLBOARD02, BILLBOARD03, BILLBOARD04, BILLBOARD05, BILLBOARD06, BILLBOARD07, BILLBOARD08, BILLBOARD09
    );

    public static final List<Sprite> PLANTS = Arrays.asList(
            TREE1, TREE2, DEAD_TREE1, DEAD_TREE2, PALM_TREE, BUSH1, BUSH2, CACTUS, STUMP, BOULDER1, BOULDER2, BOULDER3
    );

    public static final List<Sprite> CARS = Arrays.asList(
            CAR01, CAR02, CAR03, CAR04, SEMI, TRUCK
    );
}