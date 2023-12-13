import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import java.net.URISyntaxException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import images.Background;

import java.util.*;



public class Road extends Application{
    private final int SCREEN_WIDTH = 1280;
    private final int SCREEN_HEIGHT = 960;
    private static long serialVersionUID = 1L;
    private static int FPS = 60;
    private static int WIDTH = 1024;
    private static int HEIGHT = 768;
    private static int LANES = 3;
    private static int ROAD_WIDTH = 2000;
    private static int SEGMENT_LENGTH = 200;
    private static int RUMBLE_LENGTH = 3;
    private static int CAMERA_HEIGHT = 1000;
    private static int DRAW_DISTANCE = 300;
    private static int FIELD_OF_VIEW = 100;
    private static int FOG_DENSITY = 5;
    private static double MAX_SPEED = SEGMENT_LENGTH / (1000 / FPS);
    private static double ACCEL = MAX_SPEED / 5;
    private static double BREAKING = -MAX_SPEED;
    private static double DECEL = -MAX_SPEED / 5;
    private static double OFF_ROAD_DECEL = -MAX_SPEED / 2;
    private static double OFF_ROAD_LIMIT = MAX_SPEED / 4;
    private static int TRACK_LENGTH = 0;
    private static double CAMERA_DEPTH;
    private static double resolution; // scaling factor to provide resolution independence (computed)


    private boolean keyLeft = false;
    private boolean keyRight = false;
    private boolean keyFaster = false;
    private boolean keySlower = false;

    private double position = 0;
    private double speed = 0;
    private double playerX = 0;
    private double playerZ = 0;

    private ArrayList<Segment> segments = new ArrayList<>();
    private int trackLength;

    GraphicsContext ctx;

    private Canvas canvas;
    private Image background;
    private Image sprites;

    Util util = new Util();
    ImageLoader imageloader = new ImageLoader();

    StackPane root = new StackPane();
    Scene scene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT);
    

    private boolean[] keysPressed = new boolean[256]; // Array zur Verfolgung der gedrückten Tasten
    

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Javascript Racer - v1 (straight)");
        primaryStage.setResizable(false);

        canvas = new Canvas(WIDTH, HEIGHT);
        ctx = canvas.getGraphicsContext2D();

        StackPane root = new StackPane();
        root.getChildren().add(canvas);

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.show();

        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case LEFT:
                case A:
                    keyLeft = true;
                    break;
                case RIGHT:
                case D:
                    keyRight = true;
                    break;
                case UP:
                case W:
                    keyFaster = true;
                    break;
                case DOWN:
                case S:
                    keySlower = true;
                    break;
                // Handle other keys if needed
            }
        });
        
        scene.setOnKeyReleased(event -> {
            switch (event.getCode()) {
                case LEFT:
                case A:
                    keyLeft = false;
                    break;
                case RIGHT:
                case D:
                    keyRight = false;
                    break;
                case UP:
                case W:
                    keyFaster = false;
                    break;
                case DOWN:
                case S:
                    keySlower = false;
                    break;
                // Handle other keys if needed
            }
        });

        reset();
        imageloader.loadImagesFromFolder("src/main/images");
        gameLoop(ctx);
    }

    //=========================================================================
    // UPDATE THE GAME WORLD
    //=========================================================================

    private void update(int delta_time) {
        position = util.increase(position, delta_time * speed, TRACK_LENGTH);

        double dx = delta_time * 2 * (speed / MAX_SPEED);

        if (keyLeft)
            playerX = playerX - dx;
        else if (keyRight)
            playerX = playerX + dx;

        if(keyFaster)
            speed = util.accelerate(speed, ACCEL, delta_time);
        else if (keySlower)
            speed = util.accelerate(speed, BREAKING, delta_time);
        else
            speed = util.accelerate(speed, DECEL, delta_time);
        
        if (((playerX < -1) || (playerX > 1)) && (speed > OFF_ROAD_LIMIT))
            speed = util.accelerate(speed, OFF_ROAD_DECEL, delta_time);
        
        playerX = util.limit(playerX, -2, 2);     // dont ever let it go too far out of bounds
        speed = util.limit(speed, 0, MAX_SPEED); // or exceed maxSpeed
    
    }
    //=========================================================================
    // RENDER THE GAME WORLD
    //=========================================================================
    private void render(GraphicsContext gtx) {

        Segment baseSegment = findSegment(position);
        double maxy = HEIGHT;

        ctx.clearRect(0, 0, WIDTH, HEIGHT);
        ctx.setFill(Color.GREEN);
        ctx.fillRect(0, 0, WIDTH, HEIGHT);

        Render render = new Render();
        render.background(ctx, background, WIDTH, HEIGHT, Background.SKY, 0,0); // Was muss Rotation und Offset sein?
        render.background(ctx, background, WIDTH, HEIGHT, Background.HILLS, 0,0);
        render.background(ctx, background, WIDTH, HEIGHT, Background.TREES, 0,0);

        int n, i, segmentCount = 0;

        for(n = 0; n < DRAW_DISTANCE; n++) {
            Segment segment = segments.get((baseSegment.getIndex() + n) % segments.size());
            segment.setLooped(segment.getIndex() < baseSegment.getIndex());
            segment.setFog(util.exponentialFog(n / DRAW_DISTANCE, FOG_DENSITY));
            //segment.clip = maxy;

            util.project(segment.getP1(), (playerX * ROAD_WIDTH), CAMERA_HEIGHT, position - (segment.isLooped() ? trackLength : 0), CAMERA_DEPTH, WIDTH, HEIGHT, ROAD_WIDTH);
            util.project(segment.getP2(), (playerX * ROAD_WIDTH), CAMERA_HEIGHT, position - (segment.isLooped() ? trackLength : 0), CAMERA_DEPTH, WIDTH, HEIGHT, ROAD_WIDTH);


            if((segment.getP1().getCamera().getZ() <= CAMERA_DEPTH) || (segment.getP2().getScreen().getY() >= maxy)){
                continue;}
            
            render.segment(
                ctx,
                WIDTH, 
                LANES, 
                segment.getP1().getScreen().getX(),  //segment.getP1().getScreen().getX(),
                segment.getP1().getScreen().getY(), 
                segment.getP1().getScreen().getWidth(), 
                segment.getP2().getScreen().getX(), 
                segment.getP2().getScreen().getY(), 
                segment.getP2().getScreen().getWidth(), 
                segment.getFog(),
                segment.getColor());
            
            maxy = segment.getP2().getScreen().getY();
            }

            render.player(
                ctx,
                WIDTH,
                HEIGHT,
                resolution,
                ROAD_WIDTH,
                sprites,
                speed / MAX_SPEED,
                CAMERA_DEPTH / playerZ,
                WIDTH / 2,
                HEIGHT,
                speed * (keyLeft ? -1 : keyRight ? 1 : 0),
                0);
    }
   

    //=========================================================================
    // BUILD ROAD GEOMETRY
    //========================================================================= 
    private void resetRoad() {
        segments.clear();
        int numSegments = 500; // Anzahl der Segmente

        for (int n = 0; n < numSegments; n++) {
            Point3D_2 p1 = new Point3D_2(0, 0, n * SEGMENT_LENGTH);
            Point3D_2 p2 = new Point3D_2(0, 0, (n + 1) * SEGMENT_LENGTH);
            int colorIndex = (int) Math.floor(n / RUMBLE_LENGTH) % 2;
            Color color = (colorIndex == 0) ? Segment.SegmentColor.DARK.getColor() : Segment.SegmentColor.LIGHT.getColor();
            segments.add(new Segment(n, p1, p2, color));
        }

        int index = findSegment(playerZ).getIndex();
        segments.get(index + 2).setColor(Segment.SegmentColor.START.getColor());
        segments.get(index + 3).setColor(Segment.SegmentColor.START.getColor());

        for (int n = 0; n < RUMBLE_LENGTH; n++) {
            segments.get(segments.size() - 1 - n).setColor(Segment.SegmentColor.FINISH.getColor());
        }

        TRACK_LENGTH = segments.size() * SEGMENT_LENGTH;
        
    }
    
    //=========================================================================
    // THE GAME LOOP
    //=========================================================================e Segmprivate void gameLoop(GraphicsContext gtx) {
    public void gameLoop(GraphicsContext gtx) {
        AnimationTimer timer = new AnimationTimer() {
            long lastTime = System.nanoTime();
            final double ns = 1000000000.0 / FPS;
            double delta = 0;
    
            @Override
            public void handle(long now) {
                delta += (now - lastTime) / ns;
                lastTime = now;
                while (delta >= 1) {
                    update(1); // Hier wird 1 als deltaTime angenommen, Sie können eine passende Delta-Zeit berechnen
                    delta--;
                }
                render(gtx);
            }
        };
        timer.start();
    }

    private void reset() { //Map<String, Object> options
        /*int width = util.toInt(options.get("width"), WIDTH);
        int height = util.toInt(options.get("height"), HEIGHT);
        int lanes = util.toInt(options.get("lanes"), LANES);
        int roadWidth = util.toInt(options.get("roadWidth"), ROAD_WIDTH);
        int cameraHeight = util.toInt(options.get("cameraHeight"), CAMERA_HEIGHT);
        int drawDistance = util.toInt(options.get("drawDistance"), DRAW_DISTANCE);
        int fogDensity = util.toInt(options.get("fogDensity"), FOG_DENSITY);
        int fieldOfView = util.toInt(options.get("fieldOfView"), FIELD_OF_VIEW);
        int segmentLength = util.toInt(options.get("segmentLength"), SEGMENT_LENGTH);
        int rumbleLength = util.toInt(options.get("rumbleLength"), RUMBLE_LENGTH);*/
    
        CAMERA_DEPTH = 1 / Math.tan((FIELD_OF_VIEW / 2) * Math.PI / 180);
        playerZ = (CAMERA_HEIGHT * CAMERA_DEPTH);
        resolution = HEIGHT / 480;

        //if (segments.isEmpty() || options.containsKey("segmentLength") || options.containsKey("rumbleLength"))
        resetRoad(); // only rebuild road when necessary
    }
    
    Segment findSegment(double z) {
        return segments.get((int) Math.floor(z / SEGMENT_LENGTH) % segments.size());
    }
    

    
}
