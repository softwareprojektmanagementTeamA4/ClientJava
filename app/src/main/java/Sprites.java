import java.util.Arrays;
import java.util.List;
import javafx.scene.image.Image;

public class Sprites {

    public static Sprite PALM_TREE = new Sprite(5, 5, 215, 540);
    public static Sprite BILLBOARD08 = new Sprite(230, 5, 385, 265);
    public static Sprite TREE1 = new Sprite(625, 5, 360, 360);
    public static Sprite DEAD_TREE1 = new Sprite(5, 555, 135, 332);
    public static Sprite BILLBOARD09 = new Sprite(150, 555, 328, 282);
    public static Sprite BOULDER3 = new Sprite(230, 280, 320, 220);
    public static Sprite COLUMN = new Sprite(995, 5, 200, 315);
    public static Sprite BILLBOARD01 = new Sprite(625, 375, 300, 170);
    public static Sprite BILLBOARD06 = new Sprite(488, 555, 298, 190);
    public static Sprite BILLBOARD05 = new Sprite(5, 897, 298, 190);
    public static Sprite BILLBOARD07 = new Sprite(313, 897, 298, 190);
    public static Sprite BOULDER2 = new Sprite(621, 897, 298, 140);
    public static Sprite TREE2 = new Sprite(1205, 5, 282, 295);
    public static Sprite BILLBOARD04 = new Sprite(1205, 310, 268, 170);
    public static Sprite DEAD_TREE2 = new Sprite(1205, 490, 150, 260);
    public static Sprite BOULDER1 = new Sprite(1205, 760, 168, 248);
    public static Sprite BUSH1 = new Sprite(5, 1097, 240, 155);
    public static Sprite CACTUS = new Sprite(929, 897, 235, 118);
    public static Sprite BUSH2 = new Sprite(255, 1097, 232, 152);
    public static Sprite BILLBOARD03 = new Sprite(5, 1262, 230, 220);
    public static Sprite BILLBOARD02 = new Sprite(245, 1262, 215, 220);
    public static Sprite STUMP = new Sprite(995, 330, 195, 140);
    public static Sprite SEMI = new Sprite(1365, 490, 122, 144);
    public static Sprite TRUCK = new Sprite(1365, 644, 100, 78);
    public static Sprite CAR03 = new Sprite(1383, 760, 88, 55);
    public static Sprite CAR02 = new Sprite(1383, 825, 80, 59);
    public static Sprite CAR04 = new Sprite(1383, 894, 80, 57);
    public static Sprite CAR01 = new Sprite(1205, 1018, 80, 56);
    public static Sprite PLAYER_UPHILL_LEFT = new Sprite(1383, 961, 80, 45);
    public static Sprite PLAYER_UPHILL_STRAIGHT = new Sprite(1295, 1018, 80, 45);
    public static Sprite PLAYER_UPHILL_RIGHT = new Sprite(1385, 1018, 80, 45);
    public static Sprite PLAYER_LEFT = new Sprite(995, 480, 80, 41);
    public static Sprite PLAYER_STRAIGHT = new Sprite(1085, 480, 80, 41);
    public static Sprite PLAYER_RIGHT = new Sprite(995, 531, 80, 41);

    public static Sprite PLAYER_1_UPHILL_LEFT = new Sprite(1383, 961, 80, 45);
    public static Sprite PLAYER_1_UPHILL_STRAIGHT = new Sprite(1295, 1018, 80, 45);
    public static Sprite PLAYER_1_UPHILL_RIGHT = new Sprite(1385, 1018, 80, 45);
    public static Sprite PLAYER_1_LEFT = new Sprite(995, 480, 80, 41);
    public static Sprite PLAYER_1_STRAIGHT = new Sprite(1085, 480, 80, 41);
    public static Sprite PLAYER_1_RIGHT = new Sprite(995, 531, 80, 41);

    public static Sprite PLAYER_1_UPHILL_LEFT_NITRO = new Sprite(3812, 0, 80, 46);
    public static Sprite PLAYER_1_UPHILL_STRAIGHT_NITRO = new Sprite(3652, 0, 80, 46);
    public static Sprite PLAYER_1_UPHILL_RIGHT_NITRO = new Sprite(3732, 0, 80, 46);
    public static Sprite PLAYER_1_LEFT_NITRO = new Sprite(3492, 0, 80, 41);
    public static Sprite PLAYER_1_STRAIGHT_NITRO = new Sprite(3412, 0, 80, 41);
    public static Sprite PLAYER_1_RIGHT_NITRO = new Sprite(3572, 0, 80, 41);

    public static Sprite PLAYER_2_UPHILL_LEFT = new Sprite(1732, 0, 80, 45);
    public static Sprite PLAYER_2_UPHILL_STRAIGHT = new Sprite(1892, 0, 80, 45);
    public static Sprite PLAYER_2_UPHILL_RIGHT = new Sprite(1812, 0, 80, 45);
    public static Sprite PLAYER_2_LEFT = new Sprite(1492, 0, 80, 41);
    public static Sprite PLAYER_2_STRAIGHT = new Sprite(1652, 0, 80, 41);
    public static Sprite PLAYER_2_RIGHT = new Sprite(1572, 0, 80, 41);

    public static Sprite PLAYER_2_UPHILL_LEFT_NITRO = new Sprite(2692, 0, 80, 46);
    public static Sprite PLAYER_2_UPHILL_STRAIGHT_NITRO = new Sprite(2772, 0, 80, 46);
    public static Sprite PLAYER_2_UPHILL_RIGHT_NITRO = new Sprite(2852, 0, 80, 46);
    public static Sprite PLAYER_2_LEFT_NITRO = new Sprite(2452, 0, 80, 41);
    public static Sprite PLAYER_2_STRAIGHT_NITRO = new Sprite(2532, 0, 80, 41);
    public static Sprite PLAYER_2_RIGHT_NITRO = new Sprite(2612, 0, 80, 41);

    public static Sprite PLAYER_3_UPHILL_LEFT = new Sprite(2212, 0, 80, 45);
    public static Sprite PLAYER_3_UPHILL_STRAIGHT = new Sprite(2372, 0, 80, 45);
    public static Sprite PLAYER_3_UPHILL_RIGHT = new Sprite(2292, 0, 80, 45);
    public static Sprite PLAYER_3_LEFT = new Sprite(1972, 0, 80, 41);
    public static Sprite PLAYER_3_STRAIGHT = new Sprite(2132, 0, 80, 41);
    public static Sprite PLAYER_3_RIGHT = new Sprite(2052, 0, 80, 41);

    public static Sprite PLAYER_3_UPHILL_LEFT_NITRO = new Sprite(3172, 0, 80, 46);
    public static Sprite PLAYER_3_UPHILL_STRAIGHT_NITRO = new Sprite(3252, 0, 80, 46);
    public static Sprite PLAYER_3_UPHILL_RIGHT_NITRO = new Sprite(3332, 0, 80, 46);
    public static Sprite PLAYER_3_LEFT_NITRO = new Sprite(2932, 0, 80, 41);
    public static Sprite PLAYER_3_STRAIGHT_NITRO = new Sprite(3092, 0, 80, 41);
    public static Sprite PLAYER_3_RIGHT_NITRO = new Sprite(3012, 0, 80, 41);

    public static double SCALE = 0.3 * (1 / PLAYER_STRAIGHT.getW());

    public static double getScale() {
        return SCALE;
    }

    public final List<Sprite> BILLBOARDS = Arrays.asList(
            BILLBOARD01, BILLBOARD02, BILLBOARD03, BILLBOARD04, BILLBOARD05, BILLBOARD06, BILLBOARD07, BILLBOARD08,
            BILLBOARD09);

    public final List<Sprite> PLANTS = Arrays.asList(
            PALM_TREE, DEAD_TREE1, DEAD_TREE2, BOULDER1, BOULDER2, BOULDER3, COLUMN, TREE1, TREE2, CACTUS, BUSH1, BUSH2,
            STUMP);

    public final List<Sprite> CARS = Arrays.asList(
            CAR01, CAR02, CAR03, CAR04, TRUCK, SEMI);

    public Sprite getCar(int index) {
        return CARS.get(index);
    }

    public Sprite getBillboard(int index) {
        return BILLBOARDS.get(index);
    }

    public Sprite getPlant(int index) {
        return PLANTS.get(index);
    }

    static Sprite[] allCarSprites = new Sprite[] {
            CAR01, CAR02, CAR03, CAR04, TRUCK, SEMI
    };

    public static String getSpriteName(double x, double y, double w, double h) {
        for (int i = 0; i < allCarSprites.length; i++) {
            Sprite s = allCarSprites[i];
            if (s.getX() == x && s.getY() == y && s.getW() == w && s.getH() == h) {
                if (i < 4)
                    return "CAR0" + (i + 1);
                else if (i == 4)
                    return "TRUCK";
                else if (i == 5)
                    return "SEMI";
                else {
                    return "unknown";
                }
            }

        }
        return "unknown";
    }

}