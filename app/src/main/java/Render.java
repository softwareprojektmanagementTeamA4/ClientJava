import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import java.util.List;
import java.util.ArrayList;

public class Render {
    Util util = new Util();
    private static Sprites SPRITES = new Sprites();

    Color roadColor = Colors.getRoadColorDark();

    public static final Background HILLS = new Background(5, 5, 1280, 480);
    public static final Background SKY = new Background(5, 495, 1280, 480);
    public static final Background TREES = new Background(5, 985, 1280, 480);

    //=========================================================================
    // canvas rendering helpers
    //=========================================================================

    public void background(GraphicsContext gc, Image background, int width, int height,
                                  Background background2, double rotation, double offset) //bennenung letzter parameter?? 
                                  {

        
        rotation = rotation != 0 ? rotation : 0;
        offset = offset != 0 ? offset : 0;

        int imageW = background2.getW() / 2;
        int imageH = background2.getH();

        int sourceX = background2.getX() + (int) Math.floor(background2.getW() * rotation);
        int sourceY = background2.getY();
        int sourceW = Math.min(imageW, background2.getX() + background2.getW() - sourceX);
        int sourceH = imageH;
        
        int destX = 0;
        double destY = offset;
        int destW = (int) Math.floor(width * (sourceW / (double) imageW));
        int destH = height;

        gc.drawImage(background, sourceX, sourceY, sourceW, sourceH, destX, destY, destW, destH);
        if (sourceW < imageW)
            gc.drawImage(background, background2.getX(), sourceY, imageW - sourceW, sourceH, destW - 1, destY, width - destW, destH);
    }

    public void segment(
            GraphicsContext ctx,
            int width,
            int lanes,
            double x1,
            double y1,
            double w1,
            double x2,
            double y2,
            double w2,
            double fog,
            Color color
    ) {
        double r1 = rumbleWidth(w1, lanes);
        double r2 = rumbleWidth(w2, lanes);
        double l1 = laneMarkerWidth(w1, lanes);
        double l2 = laneMarkerWidth(w2, lanes);
        double lanew1, lanew2, lanex1, lanex2, lane;
        

        if (color == Colors.RUMBLE_DARK) {
            ctx.setFill(Colors.GRASS_DARK);
        } else {
            ctx.setFill(Colors.GRASS_LIGHT);
        }
        
        ctx.fillRect(0, y2, width, y1 - y2);

        
        polygon(ctx, x1 - w1 - r1, y1, x1 - w1, y1, x2 - w2, y2, x2 - w2 - r2, y2, color);
        polygon(ctx, x1 + w1 + r1, y1, x1 + w1, y1, x2 + w2, y2, x2 + w2 + r2, y2, color);
        polygon(ctx, x1 - w1, y1, x1 + w1, y1, x2 + w2, y2, x2 - w2, y2, roadColor);


        if (color.equals(Color.WHITE)) {
            lanew1 = w1 * 2 / lanes;
            lanew2 = w2 * 2 / lanes;
            lanex1 = x1 - w1 + lanew1;
            lanex2 = x2 - w2 + lanew2;
            for (lane = 1; lane < lanes; lane++) {
                polygon(ctx, lanex1 - l1 / 2, y1, lanex1 + l1 / 2, y1, lanex2 + l2 / 2, y2, lanex2 - l2 / 2, y2, color);
                lanex1 += lanew1;
                lanex2 += lanew2; 
            }
            
        }
       fog(ctx, 0, y1, width, y2 - y1, fog);
    }

    private double rumbleWidth(double projectedRoadWidth, double lanes) {
        return projectedRoadWidth / Math.max(6, 2 * lanes);
    }

    private double laneMarkerWidth(double projectedRoadWidth, double lanes) {
        return projectedRoadWidth / Math.max(32, 8 * lanes);
    }

    public void polygon(GraphicsContext ctx, double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4, Color color) {
        ctx.setFill(color);
        ctx.beginPath();
        ctx.moveTo(x1, y1);
        ctx.lineTo(x2, y2);
        ctx.lineTo(x3, y3);
        ctx.lineTo(x4, y4);
        ctx.closePath();
        ctx.fill();
    }

    private void fog(GraphicsContext ctx, double x, double y, double width, double height, double fog) {
        if (fog < 1) {
            ctx.setGlobalAlpha(1 - fog);
            ctx.setFill(Colors.getFogColor()); // Ändern Sie dies entsprechend Ihrer Farbdefinition
            ctx.fillRect(x, y, width, height);
            ctx.setGlobalAlpha(1);
        }
    }

    public void sprite(
            GraphicsContext ctx,
            double width,
            double height,
            double resolution,
            double roadWidth,
            Image sprites,
            Sprite sprite,
            double scale,
            double destX,
            double destY,
            double offsetX,
            double offsetY,
            double clipY
    ) {
        // Skalierung für die Projektion UND relativ zur roadWidth (für tweakUI)
        double destW = (sprite.getW() * scale * width / 2) * (SPRITES.getScale() * roadWidth);
        double destH = (sprite.getH() * scale * width / 2) * (SPRITES.getScale() * roadWidth);

        destX = destX + (Double.isNaN(offsetX) ? 0 : destW * offsetX);
        destY = destY + (Double.isNaN(offsetY) ? 0 : destH * offsetY);

        double clipH = clipY > 0 ? Math.max(0, destY + destH - clipY) : 0;
        if (clipH < destH) {
            ctx.drawImage(
                    sprites,
                    sprite.getX(),
                    sprite.getY(),
                    sprite.getW(),
                    sprite.getH() - (sprite.getH() * clipH / destH),
                    destX,
                    destY,
                    destW,
                    destH - clipH
            );
        }
    }

    public void player(
        GraphicsContext ctx,
        Image sprites,
        double width,
        double height,
        double resolution,
        double roadWidth,
        double speedPercent,
        double scale,
        double destX,
        double destY,
        double steer,
        double updown,
        boolean nitro
) {
    List<Integer> intList = new ArrayList<>(List.of(-1, 1));
    double bounce = (1.5 * Math.random() * speedPercent * resolution) * util.randomChoice(intList);
    
    Sprite playerSprite;

    /*if (steer < 0)
        playerSprite = (updown > 0) ? SPRITES.PLAYER_UPHILL_LEFT : SPRITES.PLAYER_LEFT;
    else if (steer > 0)
        playerSprite = (updown > 0) ? SPRITES.PLAYER_UPHILL_RIGHT : SPRITES.PLAYER_RIGHT;
    else
        playerSprite = (updown > 0) ? SPRITES.PLAYER_UPHILL_STRAIGHT : SPRITES.PLAYER_STRAIGHT;*/

    if (steer < 0) {
        playerSprite = (updown > 0) ? SPRITES.PLAYER_1_UPHILL_LEFT : SPRITES.PLAYER_1_LEFT;
        if (nitro && !Road.getNitroRecharge()) {
            playerSprite = (updown > 0) ? SPRITES.PLAYER_1_UPHILL_LEFT_NITRO : SPRITES.PLAYER_1_LEFT_NITRO;
        }
    } else if (steer > 0) {
        playerSprite = (updown > 0) ? SPRITES.PLAYER_UPHILL_RIGHT : SPRITES.PLAYER_RIGHT;
        if (nitro && !Road.getNitroRecharge()) {
            playerSprite = (updown > 0) ? SPRITES.PLAYER_1_UPHILL_RIGHT_NITRO : SPRITES.PLAYER_1_RIGHT_NITRO;
        }
    } else {
        playerSprite = (updown > 0) ? SPRITES.PLAYER_UPHILL_STRAIGHT : SPRITES.PLAYER_STRAIGHT;
        if (nitro && !Road.getNitroRecharge()) {
            playerSprite = (updown > 0) ? SPRITES.PLAYER_1_UPHILL_STRAIGHT_NITRO : SPRITES.PLAYER_1_STRAIGHT_NITRO;
        }
    }

    sprite(
            ctx,
            width,
            height,
            resolution,
            roadWidth,
            sprites,
            playerSprite,
            scale,
            destX,
            destY + bounce,
            -0.5,
            -1,
            0 // Not sure about clipY
    );
}
    
}
