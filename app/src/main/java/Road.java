import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.scene.effect.Light.Point;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import java.net.URISyntaxException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;



public class Road extends Application{
    private final int SCREEN_WIDTH = 1280;
    private final int SCREEN_HEIGHT = 960;
    private long serialVersionUID = 1L;
    private int FPS = 60;
    private int WIDTH = 1024;
    private int HEIGHT = 768;
    private int LANES = 3;
    private int ROAD_WIDTH = 2000;
    private int SEGMENT_LENGTH = 200;
    private int RUMBLE_LENGTH = 3;
    private int CAMERA_HEIGHT = 1000;
    private int DRAW_DISTANCE = 300;
    private int FIELD_OF_VIEW = 100;
    private int FOG_DENSITY = 5;
    private double MAX_SPEED = SEGMENT_LENGTH / (1.0 / FPS);
    private double ACCEL = MAX_SPEED / 5;
    private double BREAKING = -MAX_SPEED;
    private double DECEL = -MAX_SPEED / 5;
    private double OFF_ROAD_DECEL = -MAX_SPEED / 2;
    private double OFF_ROAD_LIMIT = MAX_SPEED / 4;
    private double TRACK_LENGTH;
    private double CAMERA_DEPTH;
    private double resolution; // scaling factor to provide resolution independence (computed)
    private double globalDeltaTime = 0;
    private long lastTime = 0;
    private double centrifugal_force = 0.3;        // centrifugal force multiplier when going around curves
    private double skySpeed = 0.001;                  // background sky layer scroll speed when going around curve (or up hill)
    private double hillSpeed = 0.002;                 // background hill layer scroll speed when going around curve (or up hill)
    private double treeSpeed = 0.003;                 // background tree layer scroll speed when going around curve (or up hill)
    private double skyOffset = 0;                       // current sky scroll offset
    private double hillOffset = 0;                       // current hill scroll offset
    private double treeOffset = 0;                       // current tree scroll offset    

    String path_background_sky = ("background/sky.png");
    String path_background_hills = ("background/hills.png");
    String path_background_trees = ("background/trees.png");

    private boolean keyLeft = false;
    private boolean keyRight = false;
    private boolean keyFaster = false;
    private boolean keySlower = false;

    private double position = 0;
    private double speed = 0;
    private double playerX = 0;
    private double playerZ = 0;

    private ArrayList<Segment> segments = new ArrayList<>();
    
    private Image background = new Image("file:src/main/java/images/background.png");
    private Image sprites = new Image("file:src/main/java/images/sprites.png");

    Util util = new Util();
    Render render = new Render();

    StackPane root = new StackPane();
    Scene scene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT);

    HashMap<String, Image> images = new HashMap<>();
    ImageLoader imageLoader = new ImageLoader();

    private boolean[] keysPressed = new boolean[256]; // Array zur Verfolgung der gedrückten Tasten
    

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Javascript Racer - v1 (straight)");
        primaryStage.setResizable(false);

        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext ctx = canvas.getGraphicsContext2D();


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
            }
        });

        // ImageLoader
        images = imageLoader.loadImagesFromFolder("src/main/java/images/sprites", images);
        images = imageLoader.loadImagesFromFolder("src/main/java/images", images);

        //JavaFX Timeline = free form animation defined by KeyFrames and their duration 
		Timeline tl = new Timeline(new KeyFrame(Duration.seconds(1.0 / FPS), e -> gameLoop(ctx)));
		//number of cycles in animation INDEFINITE = repeat indefinitely
		tl.setCycleCount(Timeline.INDEFINITE);
        primaryStage.setScene(scene);
        primaryStage.show();
        tl.play();
    }

    //=========================================================================
    // UPDATE THE GAME WORLD
    //=========================================================================

    private void update(double delta_time) {
        Segment playerSegment = findSegment(position + playerZ);
        double speedPercent = speed / MAX_SPEED;
        double dx = delta_time * 2 * speedPercent; // at top speed, should be able to cross from left to right (-1 to 1) in 1 second

        position = util.increase(position, delta_time * speed, TRACK_LENGTH);

        skyOffset  = util.increase(skyOffset,  skySpeed  * playerSegment.getCurve() * speedPercent, 1);
        hillOffset = util.increase(hillOffset, hillSpeed * playerSegment.getCurve() * speedPercent, 1);
        treeOffset = util.increase(treeOffset, treeSpeed * playerSegment.getCurve() * speedPercent, 1);

        if (keyLeft)
            playerX = playerX - dx;
        else if (keyRight)
            playerX = playerX + dx;

        playerX = playerX - (dx * speedPercent * playerSegment.getCurve() * centrifugal_force); // centrifugal force multiplier

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
    private void render(GraphicsContext ctx) {

        Segment baseSegment = findSegment(position);
        double basePercent = util.percentRemaining(position, SEGMENT_LENGTH);
        double maxy = HEIGHT;

        double x = 0;
        double dx = - (baseSegment.getCurve() * basePercent);

        ctx.clearRect(0, 0, WIDTH, HEIGHT);
        //ctx.setFill(Color.GREEN);
        //ctx.fillRect(0, 0, WIDTH, HEIGHT);
        
        render.background(ctx, background, WIDTH, HEIGHT, Background.SKY, skyOffset,0); // Was muss Rotation und Offset sein?
        render.background(ctx, background, WIDTH, HEIGHT, Background.HILLS, hillOffset,0);
        render.background(ctx, background, WIDTH, HEIGHT, Background.TREES, treeOffset,0);

        int n;
        for(n = 0; n < DRAW_DISTANCE; n++) {
            Segment segment = segments.get((baseSegment.getIndex() + n) % segments.size());
            segment.setLooped(segment.getIndex() < baseSegment.getIndex());
            segment.setFog(util.exponentialFog(n / DRAW_DISTANCE, FOG_DENSITY));
            //segment.clip = maxy;

            util.project(segment.getP1(), (playerX * ROAD_WIDTH) -x, CAMERA_HEIGHT, position - (segment.isLooped() ? TRACK_LENGTH : 0), CAMERA_DEPTH, WIDTH, HEIGHT, ROAD_WIDTH);
            util.project(segment.getP2(), (playerX * ROAD_WIDTH) -x -dx, CAMERA_HEIGHT, position - (segment.isLooped() ? TRACK_LENGTH : 0), CAMERA_DEPTH, WIDTH, HEIGHT, ROAD_WIDTH);

            x = x + dx;
            dx = dx + segment.getCurve();

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
    private void addSegment(double curve) {
        int n = segments.size();
        segments.add(new Segment(
                n,
                new Point3D_2(0, 0, n * SEGMENT_LENGTH),
                new Point3D_2(0, 0, (n + 1) * SEGMENT_LENGTH),
                curve,
                (n / RUMBLE_LENGTH) % 2 == 1 ? Colors.Dark.ROAD : Colors.Light.ROAD
        ));
    }

    private void addRoad(int enter, int hold, int leave, int curve){
        int n;
        for(n = 0; n < enter; n++){
            addSegment(util.easeIn(0, curve, n / enter));
        }
        for(n = 0; n < hold; n++){
            addSegment(curve);
        }
        for(n = 0; n < leave; n++){
            addSegment(util.easeInOut(curve, 0, n / leave));
        }
    }

    public void addStraight(Integer num) {
        if (num == null) {
            num = RoadDefinition.Length.MEDIUM.getValue();
        }
        addRoad(num, num, num, 0);
    }

    public void addCurve(Integer num, Integer curve) {
        if (num == null) {
            num = RoadDefinition.Length.MEDIUM.getValue();
        }
        if (curve == null) {
            curve = RoadDefinition.Curve.MEDIUM.getValue();
        }
        addRoad(num, num, num, curve);
    }

    public void addSCurves() {
        addRoad(RoadDefinition.Length.MEDIUM.getValue(), RoadDefinition.Length.MEDIUM.getValue(),
                RoadDefinition.Length.MEDIUM.getValue(), -RoadDefinition.Curve.EASY.getValue());
        
        addRoad(RoadDefinition.Length.MEDIUM.getValue(), RoadDefinition.Length.MEDIUM.getValue(),
                RoadDefinition.Length.MEDIUM.getValue(), RoadDefinition.Curve.MEDIUM.getValue());
        
        addRoad(RoadDefinition.Length.MEDIUM.getValue(), RoadDefinition.Length.MEDIUM.getValue(),
                RoadDefinition.Length.MEDIUM.getValue(), RoadDefinition.Curve.EASY.getValue());
        
        addRoad(RoadDefinition.Length.MEDIUM.getValue(), RoadDefinition.Length.MEDIUM.getValue(),
                RoadDefinition.Length.MEDIUM.getValue(), -RoadDefinition.Curve.EASY.getValue());
        
        addRoad(RoadDefinition.Length.MEDIUM.getValue(), RoadDefinition.Length.MEDIUM.getValue(),
                RoadDefinition.Length.MEDIUM.getValue(), -RoadDefinition.Curve.MEDIUM.getValue());
    }



    private void resetRoad() {
        segments.clear();
        addStraight(RoadDefinition.Length.SHORT.getValue()/4);
        addSCurves();
        addStraight(RoadDefinition.Length.LONG.getValue());
        addCurve(RoadDefinition.Length.MEDIUM.getValue(), RoadDefinition.Curve.MEDIUM.getValue());
        addCurve(RoadDefinition.Length.LONG.getValue(), RoadDefinition.Curve.MEDIUM.getValue());
        addStraight(null);
        addSCurves();
        addCurve(RoadDefinition.Length.LONG.getValue(), -RoadDefinition.Curve.MEDIUM.getValue());
        addCurve(RoadDefinition.Length.LONG.getValue(), RoadDefinition.Curve.MEDIUM.getValue());
        addStraight(null);
        addSCurves();
        addCurve(RoadDefinition.Length.LONG.getValue(), -RoadDefinition.Curve.EASY.getValue());

        segments.get(findSegment(playerZ).getIndex() + 2).setColor(Colors.Start.ROAD);
        segments.get(findSegment(playerZ).getIndex() + 3).setColor(Colors.Start.ROAD);
        for (int n = 0; n < RUMBLE_LENGTH; n++) {
            segments.get(segments.size() - 1 - n).setColor(Colors.Finish.ROAD);
        }
        TRACK_LENGTH = segments.size() * SEGMENT_LENGTH;
    }
    
    //=========================================================================
    // THE GAME LOOP
    //=========================================================================e Segmprivate void gameLoop(GraphicsContext gtx) {
    public void gameLoop(GraphicsContext ctx) {
        reset(null);
        while (true) {
            frame(ctx);
            render(ctx);
        }
    }

    private void reset(HashMap<String, Integer> options) { //Map<String, Object> options
        options = (options != null) ? options : new HashMap<>();
        int width = util.toInt(options.get("width"), WIDTH);
        int height = util.toInt(options.get("height"), HEIGHT);
        int lanes = util.toInt(options.get("lanes"), LANES);
        int roadWidth = util.toInt(options.get("roadWidth"), ROAD_WIDTH);
        int cameraHeight = util.toInt(options.get("cameraHeight"), CAMERA_HEIGHT);
        int drawDistance = util.toInt(options.get("drawDistance"), DRAW_DISTANCE);
        int fogDensity = util.toInt(options.get("fogDensity"), FOG_DENSITY);
        int fieldOfView = util.toInt(options.get("fieldOfView"), FIELD_OF_VIEW);
        int segmentLength = util.toInt(options.get("segmentLength"), SEGMENT_LENGTH);
        int rumbleLength = util.toInt(options.get("rumbleLength"), RUMBLE_LENGTH);
    
        CAMERA_DEPTH = 1 / Math.tan((FIELD_OF_VIEW / 2) * Math.PI / 180);
        playerZ = (CAMERA_HEIGHT * CAMERA_DEPTH);
        resolution = HEIGHT / 480;

        if (segments.isEmpty() || options.containsKey("segmentLength") || options.containsKey("rumbleLength")){
            resetRoad(); // only rebuild road when necessary
        }
    }
    
    Segment findSegment(double z) {
        return segments.get((int) Math.floor(z / SEGMENT_LENGTH) % segments.size());
    }

    public void frame(GraphicsContext ctx) {
        long timeNow = System.currentTimeMillis();
        double deltaTime = Math.min(1, (timeNow - lastTime) / 1000.0);
        globalDeltaTime += deltaTime;

        double step = 1.0 / FPS; 

        // while (globalDeltaTime > step) {
        //     globalDeltaTime -= step;
        //     update(step);
        // }
        update(step);

        render(ctx);

        lastTime = timeNow;
    }
}
