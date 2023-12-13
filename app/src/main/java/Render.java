import images.Background;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color; 

public class Render {
    Util util = new Util();
    public static final Background HILLS = new Background(5, 5, 1280, 480);
    public static final Background SKY = new Background(5, 495, 1280, 480);
    public static final Background TREES = new Background(5, 985, 1280, 480);

    //=========================================================================
    // canvas rendering helpers
    //=========================================================================

    public void background(GraphicsContext gc, Image background, int width, int height,
                                  Background background2, int rotation, int offset) {

        
        rotation = rotation != 0 ? rotation : 0;
        offset = offset != 0 ? offset : 0;

        int imageW = background2.getW() / 2;
        int imageH = background2.getH();

        int sourceX = background2.getX() + (int) Math.floor(background2.getW() * rotation);
        int sourceY = background2.getY();
        int sourceW = Math.min(imageW, background2.getX() + background2.getW() - sourceX);
        int sourceH = imageH;
        
        int destX = 0;
        int destY = offset;
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

        ctx.setFill(color.darker());
        ctx.fillRect(0, y2, width, y1 - y2);

        polygon(ctx, x1 - w1 - r1, y1, x1 - w1, y1, x2 - w2, y2, x2 - w2 - r2, y2, color);
        polygon(ctx, x1 + w1 + r1, y1, x1 + w1, y1, x2 + w2, y2, x2 + w2 + r2, y2, color);
        polygon(ctx, x1 - w1, y1, x1 + w1, y1, x2 + w2, y2, x2 - w2, y2, color.brighter());

        if (color.equals(Color.WHITE)) {
            lanew1 = w1 * 2 / lanes;
            lanew2 = w2 * 2 / lanes;
            lanex1 = x1 - w1 + lanew1;
            lanex2 = x2 - w2 + lanew2;

            for (lane = 1; lane < lanes; lanex1 += lanew1, lanex2 += lanew2, lane++) {
                polygon(ctx, lanex1 - l1 / 2, y1, lanex1 + l1 / 2, y1, lanex2 + l2 / 2, y2, lanex2 - l2 / 2, y2, color);
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

    private void polygon(GraphicsContext ctx, double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4, Color color) {
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
            ctx.setFill(Color.GRAY); // Ändern Sie dies entsprechend Ihrer Farbdefinition
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
        double destW = (sprite.getW() * scale * width / 2) * (Sprites.getScale() * roadWidth);
        double destH = (sprite.getH() * scale * width / 2) * (Sprites.getScale() * roadWidth);

        destX = destX + (destW * offsetX);
        destY = destY + (destH * offsetY);

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
            double width,
            double height,
            double resolution,
            double roadWidth,
            Image sprites,
            double speedPercent,
            double scale,
            double destX,
            double destY,
            double steer,
            double updown
    ) {
        double bounce = (1.5 * Math.random() * speedPercent * resolution) * util.randomChoice(new int[]{-1, 1});
        Sprite playerSprite;

        if (steer < 0)
            playerSprite = (updown > 0) ? Sprites.PLAYER_UPHILL_LEFT : Sprites.PLAYER_LEFT;
        else if (steer > 0)
            playerSprite = (updown > 0) ? Sprites.PLAYER_UPHILL_RIGHT : Sprites.PLAYER_RIGHT;
        else
            playerSprite = (updown > 0) ? Sprites.PLAYER_UPHILL_STRAIGHT : Sprites.PLAYER_STRAIGHT;

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
                0 // Wissen nicht was clipY ist
        );
    }
    
}
