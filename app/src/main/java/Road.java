import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.Light.Point;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Screen;
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
    private int totalCars = 200;   
    private int currentLapTime = 0;
    private int lastLapTime = 0;
    private int currentLap = 4;
    private int maxLap = 3;
    private int place = 1;

    private int lanes = 3;
    private double currentRoadWidth = 0;
    private double currentCameraHeight = 0;
    private double currentDrawDistance = 0;
    private double currentFieldOfView = 0;
    private double currentFogDensity = 0;

    private String path_background_sky = ("background/sky.png");
    private String path_background_hills = ("background/hills.png");
    private String path_background_trees = ("background/trees.png");

    private boolean keyLeft = false;
    private boolean keyRight = false;
    private boolean keyFaster = false;
    private boolean keySlower = false;

    private double position = 0;
    private double speed = 0;
    private double playerX = 0;
    private double playerZ = 0;

    private boolean nitrokey = false;
    private double nitro = 100;
    private double maxNitro = 100;
    private boolean nitroRecharge = false;
    private boolean nitroActive = false;
    private boolean fullscreen = false;

    private ArrayList<Segment> segments = new ArrayList<>();
    private ArrayList<Car> cars = new ArrayList<>();
    private ArrayList<String> finishedPlayers = new ArrayList<>();

    private Image background = new Image("file:src/main/java/images/background.png");
    private Image sprites = new Image("file:src/main/java/images/spritesheet.png");
    private Image nitroBottle = new Image("file:src/main/java/images/nitro.png");
    private Image nitroBottleEmpty = new Image("file:src/main/java/images/nitro_empty.png");

    Util util = new Util();
    Render render = new Render();
    
    HashMap<String, Image> images = new HashMap<>();
    ImageLoader imageLoader = new ImageLoader();

    private boolean[] keysPressed = new boolean[256]; // Array zur Verfolgung der gedrückten Tasten

    private ComboBox<String> lanesComboBox = new ComboBox<>();
    
    private Sprites SPRITES = new Sprites();


    @Override
    public void start(Stage primaryStage) {
        getSettingsFromApp(primaryStage);

        primaryStage.setTitle("Racegame");
        primaryStage.setResizable(false);

        StackPane root = new StackPane();
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext ctx = canvas.getGraphicsContext2D();

        root.getChildren().addAll(canvas);

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
                case SPACE:
                    nitrokey = true;
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
                case SPACE:
                    nitrokey = false; 
                    break;
            }
        });
        reset();
        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                frame(ctx);
                endScreen(ctx);
                updateHUD(ctx);
            }
        };
        gameLoop.start();
		
        primaryStage.setScene(scene);
        if(fullscreen) {
            primaryStage.setFullScreen(true);
            root.maxWidthProperty().bind(primaryStage.widthProperty());
            root.maxHeightProperty().bind(primaryStage.heightProperty());
        }
        primaryStage.show();

    }

    //=========================================================================
    // UPDATE THE GAME WORLD
    //=========================================================================

    private void update(double delta_time) {
        Car car;
        double carW;
        Sprite sprite;
        double spriteW;

        Segment playerSegment = findSegment(position + playerZ);
        double playerW = SPRITES.PLAYER_STRAIGHT.getW() * SPRITES.SCALE;
        double speedPercent = speed / MAX_SPEED;
        double dx = delta_time * 2 * speedPercent; // at top speed, should be able to cross from left to right (-1 to 1) in 1 second
        double startPosition = position;

        updateCars(delta_time, playerSegment, playerW);

        position = util.increase(position, delta_time * speed, TRACK_LENGTH);

        skyOffset  = util.increase(skyOffset,  skySpeed  * playerSegment.getCurve() * (position-startPosition)/SEGMENT_LENGTH, 1);
        hillOffset = util.increase(hillOffset, hillSpeed * playerSegment.getCurve() * (position-startPosition)/SEGMENT_LENGTH, 1);
        treeOffset = util.increase(treeOffset, treeSpeed * playerSegment.getCurve() * (position-startPosition)/SEGMENT_LENGTH, 1);

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

        if (nitrokey) {
            if (nitro <= 0) {
                nitroRecharge = true;
            } else if (!nitroRecharge && speed > 0) {
                nitroActive = true;
                speed = util.accelerate(speed, ACCEL * 2.5, delta_time); // 2,5x so schnell Beschleunigen
                nitro -= 1;
            }
        } else {
            nitroActive = false;
        }

        if (nitroRecharge) {
            nitroActive = false;
            nitro += 0.0625;
            nitro = Math.min(nitro, maxNitro);
            nitroRecharge = nitro < maxNitro;
        }
        
        
        if ((playerX < -1) || (playerX > 1)) {

            if (speed > OFF_ROAD_LIMIT)
                speed = util.accelerate(speed, OFF_ROAD_DECEL, delta_time);
            
            for (int n = 0; n < playerSegment.getSprites().size(); n++) {
                sprite = playerSegment.getSprites().get(n);
                spriteW = sprite.getW() * SPRITES.SCALE;
                if (util.overlap(playerX, playerW, sprite.getOffset() + spriteW / 2 * (sprite.getOffset() > 0 ? 1 : -1), spriteW, Double.NaN)) { // 0 richtig?
                    speed = MAX_SPEED / 5;
                    position = util.increase(playerSegment.getP1().getWorld().getZ(), -playerZ, TRACK_LENGTH); // stop in front of sprite (at front of segment)
                    break;
                }
            }
        }
            
        for (int n = 0; n < playerSegment.getCars().size(); n++) {
            car = playerSegment.getCars().get(n);
            carW = car.getSprite().getW() * SPRITES.SCALE;
            if (speed > car.getSpeed()) {
                if (util.overlap(playerX, playerW, car.getOffset(), carW, 0.8)) {
                    speed = car.getSpeed() * (car.getSpeed() / speed);
                    position = util.increase(car.getZ(), -playerZ, TRACK_LENGTH);
                    break;
                }
            }
        }
            
        playerX = util.limit(playerX, -2, 2);         // dont ever let it go too far out of bounds
        speed = util.limit(speed, 0, MAX_SPEED);      // or exceed maxSpeed

        if (position > playerZ) {
            if (currentLapTime != 0) { 
                if (startPosition < playerZ) {
                    lastLapTime = currentLapTime;
                    currentLapTime = 0;
                    currentLap += 1;
                }
            } else {
                currentLapTime += delta_time;
            }
        }
    }

    private void updateCars(double dt, Segment playerSegment, double playerW) {
        Segment oldSegment, newSegment;
        for (int n = 0; n < cars.size(); n++) {
            Car car = cars.get(n);
            oldSegment = findSegment(car.getZ());
            car.setOffset(car.getOffset() + updateCarOffset(car, oldSegment, playerSegment, playerW));
            car.setZ(util.increase(car.getZ(), dt * car.getSpeed(), TRACK_LENGTH));
            car.setPercent(util.percentRemaining(car.getZ(), SEGMENT_LENGTH)); // useful for interpolation during rendering phase
            newSegment = findSegment(car.getZ());
            if (oldSegment != newSegment) {
                int index = oldSegment.getCars().indexOf(car);
                oldSegment.getCars().remove(index);
                newSegment.getCars().add(car);
            }
        }
    }

    private double updateCarOffset(Car car, Segment carSegment, Segment playerSegment, double playerW) {
        double dir = 0;
        Segment segment;
        Car otherCar;
        double otherCarW;
        double lookahead = 20;
        double carW = car.getSprite().getW() * SPRITES.SCALE;
    
        // Optimierung: Kein Lenken um andere Autos, wenn außerhalb des Sichtbereichs des Spielers
        if ((carSegment.getIndex() - playerSegment.getIndex()) > DRAW_DISTANCE)
            return 0;
    
        for (int i = 1; i < lookahead; i++) {
            segment = segments.get((carSegment.getIndex() + i) % segments.size());
    
            if ((segment == playerSegment) && (car.getSpeed() > speed) && (util.overlap(playerX, playerW, car.getOffset(), carW, 1.2))) {
                if (playerX > 0.5)
                    dir = -1;
                else if (playerX < -0.5)
                    dir = 1;
                else
                    dir = (car.getOffset() > playerX) ? 1 : -1;
                return dir * 1/i * (car.getSpeed() - speed) / MAX_SPEED; // je näher die Autos beieinander sind (kleiner i) und je größer das Geschwindigkeitsverhältnis ist, desto größer ist der Offset
            }
    
            for (int j = 0; j < segment.getCars().size(); j++) {
                otherCar = segment.getCars().get(j);
                otherCarW = otherCar.getSprite().getW() * SPRITES.SCALE;
                if ((car.getSpeed() > otherCar.getSpeed()) && util.overlap(car.getOffset(), carW, otherCar.getOffset(), otherCarW, 1.2)) {
                    if (otherCar.getOffset() > 0.5)
                        dir = -1;
                    else if (otherCar.getOffset() < -0.5)
                        dir = 1;
                    else
                        dir = (car.getOffset() > otherCar.getOffset()) ? 1 : -1;
                    return dir * 1/i * (car.getSpeed() - otherCar.getSpeed()) / MAX_SPEED;
                }
            }
        }
    
        // Wenn keine Autos voraus sind, aber ich aus irgendeinem Grund von der Straße abgekommen bin, dann wieder zurücklenken
        if (car.getOffset() < -0.9)
            return 0.1;
        else if (car.getOffset() > 0.9)
            return -0.1;
        else
            return 0;
    }
    //=========================================================================
    // RENDER THE GAME WORLD
    //=========================================================================
    private void render(GraphicsContext ctx) {
        Segment baseSegment = findSegment(position);
        double basePercent = util.percentRemaining(position, SEGMENT_LENGTH);
        Segment playerSegment = findSegment(position + playerZ);
        double playerPercent = util.percentRemaining(position + playerZ, SEGMENT_LENGTH);
        double playerY = util.interpolate(playerSegment.getP1().getWorld().getY(), playerSegment.getP2().getWorld().getY(), playerPercent);
        double maxy = HEIGHT;

        double x = 0;
        double dx = - (baseSegment.getCurve() * basePercent);

        ctx.clearRect(0, 0, WIDTH, HEIGHT);
        //ctx.setFill(Color.GREEN);
        //ctx.fillRect(0, 0, WIDTH, HEIGHT);
        
        render.background(ctx, background, WIDTH, HEIGHT, Background.SKY, skyOffset, resolution * skySpeed * playerY); // Was muss Rotation und Offset sein?
        render.background(ctx, background, WIDTH, HEIGHT, Background.HILLS, hillOffset, resolution * hillSpeed * playerY);
        render.background(ctx, background, WIDTH, HEIGHT, Background.TREES, treeOffset, resolution * treeSpeed * playerY);

        
        Car car;
        Sprite sprite;
        double spriteScale;
        double spriteX;
        double spriteY;

        for(int n = 0; n < DRAW_DISTANCE; n++) {
            Segment segment = segments.get((baseSegment.getIndex() + n) % segments.size());
            segment.setLooped(segment.getIndex() < baseSegment.getIndex());
            segment.setFog(util.exponentialFog(n / DRAW_DISTANCE, FOG_DENSITY));
            segment.setClip(maxy);

            util.project(segment.getP1(), (playerX * ROAD_WIDTH) -x,    playerY + CAMERA_HEIGHT, position - (segment.isLooped() ? TRACK_LENGTH : 0), CAMERA_DEPTH, WIDTH, HEIGHT, ROAD_WIDTH);
            util.project(segment.getP2(), (playerX * ROAD_WIDTH) -x -dx, playerY + CAMERA_HEIGHT, position - (segment.isLooped() ? TRACK_LENGTH : 0), CAMERA_DEPTH, WIDTH, HEIGHT, ROAD_WIDTH);

            x = x + dx;
            dx = dx + segment.getCurve();

            if((segment.getP1().getCamera().getZ() <= CAMERA_DEPTH) || 
               (segment.getP2().getScreen().getY() >= segment.getP1().getScreen().getY()) ||
               (segment.getP2().getScreen().getY() >= maxy))
               {
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
            
            maxy = segment.getP1().getScreen().getY();
            }
            for(int n = (DRAW_DISTANCE - 1); n > 0; n--) {
                Segment segment = segments.get((baseSegment.getIndex() + n) % segments.size());

                for(int i = 0; i < segment.getCars().size(); i++) {
                    car = segment.getCars().get(i);
                    sprite = car.getSprite();
                    spriteScale = util.interpolate(segment.getP1().getScreen().getScale(), segment.getP2().getScreen().getScale(), car.getPercent());
                    spriteX = util.interpolate(segment.getP1().getScreen().getX(), segment.getP2().getScreen().getX(), car.getPercent()) + (spriteScale * car.getOffset() * ROAD_WIDTH * WIDTH / 2);
                    spriteY = util.interpolate(segment.getP1().getScreen().getY(), segment.getP2().getScreen().getY(), car.getPercent());
                    render.sprite(ctx, WIDTH, HEIGHT, resolution, ROAD_WIDTH, sprites,  car.getSprite(), spriteScale, spriteX, spriteY, -0.5, -1, segment.getClip());
                }

                for(int i = 0; i < segment.getSprites().size(); i++) {
                    sprite = segment.getSprites().get(i);
                    spriteScale = segment.getP1().getScreen().getScale();
                    spriteX = segment.getP1().getScreen().getX() + (spriteScale * sprite.getOffset() * ROAD_WIDTH * WIDTH / 2);
                    spriteY = segment.getP1().getScreen().getY();
                    render.sprite(ctx, WIDTH, HEIGHT, resolution, ROAD_WIDTH,sprites, sprite, spriteScale, spriteX, spriteY,  (sprite.getOffset() < 0 ? -1 : 0), -1, segment.getClip());
                }

                if (segment == playerSegment) {
                    render.player(
                        ctx,
                        sprites,
                        WIDTH,
                        HEIGHT,
                        resolution,
                        ROAD_WIDTH,
                        speed / MAX_SPEED,
                        CAMERA_DEPTH / playerZ,
                        WIDTH / 2,
                        (HEIGHT / 2) - (CAMERA_DEPTH / playerZ * util.interpolate(playerSegment.getP1().getCamera().getY(), playerSegment.getP2().getCamera().getY(), playerPercent) * HEIGHT / 2),
                        speed * (keyLeft ? -1 : keyRight ? 1 : 0),
                        playerSegment.getP2().getWorld().getY() - playerSegment.getP1().getWorld().getY(),
                        nitrokey);
                }
            }
    }

    Segment findSegment(double z) {
        return segments.get((int) Math.floor(z / SEGMENT_LENGTH) % segments.size());
    }
   
    //=========================================================================
    // BUILD ROAD GEOMETRY
    //=========================================================================

    private double lastY() {
        return (segments.size() == 0) ? 0 : segments.get(segments.size() - 1).getP2().getWorld().getY();
    }

    private void addSegment(double curve, double y) {
        int n = segments.size();
        Color rumbleColor = Colors.getRumbleColor(n, RUMBLE_LENGTH);
        segments.add(new Segment(
                n,
                new Point3D_2(0, lastY(), n * SEGMENT_LENGTH),
                new Point3D_2(0, y, (n + 1) * SEGMENT_LENGTH),
                curve,
                new ArrayList<>(),
                new ArrayList<>(),
                rumbleColor
        ));
    }

    private void addSprite(int n, Sprite sprite, double offset) { // #TODO warum funktioniert das mit setSource nicht?? bzw muss ich das anders machen?
        sprite.setOffset(offset);
        segments.get(n).getSprites().add(sprite);
        //segments.get(n).getSprites().add(new Sprite(offset, sprite));
      }

    private void addRoad(int enter, int hold, int leave, int curve, int d){
        double startY = lastY();
        double endY = startY + (util.toInt(d, 0) * SEGMENT_LENGTH);
        int n; 
        double total = enter + hold + leave;

        for(n = 0; n < enter; n++){
            addSegment(util.easeIn(0, curve, n / enter), util.easeInOut(startY, endY, n / total));
        }
        for(n = 0; n < hold; n++){
            addSegment(curve, util.easeInOut(startY, endY, (enter + n) / total));
        }
        for(n = 0; n < leave; n++){
            addSegment(util.easeInOut(curve, 0, n / leave), util.easeInOut(startY, endY, (enter + hold + n) / total));
        }
    }

    public void addStraight(Integer num) {
        if (num == null) {
            num = RoadDefinition.Length.MEDIUM.getValue();
        }
        addRoad(num, num, num, 0, 0);
    }

    public void addCurve(Integer num, Integer curve, Integer height) {
        if (num == null) {
            num = RoadDefinition.Length.MEDIUM.getValue();
        }
        if (curve == null) {
            curve = RoadDefinition.Curve.MEDIUM.getValue();
        }
        if (height == null) {
            height = RoadDefinition.Hill.NONE.getValue();
        }
        addRoad(num, num, num, curve, height);
    }

    public void addHill(Integer num, Integer height) {
        if (num == null) {
            num = RoadDefinition.Length.MEDIUM.getValue();
        }
        if (height == null) {
            height = RoadDefinition.Hill.MEDIUM.getValue();
        }
        addRoad(num, num, num, 0, height);
    }

    public void addSCurves() {
        addRoad(RoadDefinition.Length.MEDIUM.getValue(), RoadDefinition.Length.MEDIUM.getValue(),
                RoadDefinition.Length.MEDIUM.getValue(), -RoadDefinition.Curve.EASY.getValue(),
                RoadDefinition.Hill.NONE.getValue());
        
        addRoad(RoadDefinition.Length.MEDIUM.getValue(), RoadDefinition.Length.MEDIUM.getValue(),
                RoadDefinition.Length.MEDIUM.getValue(), RoadDefinition.Curve.MEDIUM.getValue(),
                RoadDefinition.Hill.MEDIUM.getValue());
        
        addRoad(RoadDefinition.Length.MEDIUM.getValue(), RoadDefinition.Length.MEDIUM.getValue(),
                RoadDefinition.Length.MEDIUM.getValue(), RoadDefinition.Curve.EASY.getValue(),
                -RoadDefinition.Hill.LOW.getValue());
        
        addRoad(RoadDefinition.Length.MEDIUM.getValue(), RoadDefinition.Length.MEDIUM.getValue(),
                RoadDefinition.Length.MEDIUM.getValue(), -RoadDefinition.Curve.EASY.getValue(),
                RoadDefinition.Hill.MEDIUM.getValue());
        
        addRoad(RoadDefinition.Length.MEDIUM.getValue(), RoadDefinition.Length.MEDIUM.getValue(),
                RoadDefinition.Length.MEDIUM.getValue(), -RoadDefinition.Curve.MEDIUM.getValue(),
                -RoadDefinition.Hill.MEDIUM.getValue());
    }

    public void addBumps() {
        addRoad(10, 10, 10, 0,  5);
        addRoad(10, 10, 10, 0, -2);
        addRoad(10, 10, 10, 0, -5);
        addRoad(10, 10, 10, 0,  8);
        addRoad(10, 10, 10, 0,  5);
        addRoad(10, 10, 10, 0, -7);
        addRoad(10, 10, 10, 0,  5);
        addRoad(10, 10, 10, 0, -2);
      }

    private void addDownhillToEnd(Integer num) {
        if (num == null) {
            num = 200;
        }
        addRoad(num, num, num, -RoadDefinition.Curve.EASY.getValue(), (int) -lastY() / SEGMENT_LENGTH);

    }

    private void addLowRollingHills(Integer num, Integer height)
    {
        if (num == null) {
            num = RoadDefinition.Length.SHORT.getValue();
        }
        if (height == null) {
            height = RoadDefinition.Hill.LOW.getValue();
        }
        addRoad(num, num, num, 0, height / 2);
        addRoad(num, num, num, 0, -height);
        addRoad(num, num, num, RoadDefinition.Curve.EASY.getValue(), height);
        addRoad(num, num, num, 0, 0);
        addRoad(num, num, num, -RoadDefinition.Curve.EASY.getValue(), height / 2);
        addRoad(num, num, num, 0, 0);
    }

    private void resetRoad() {
        segments.clear();
        addStraight(RoadDefinition.Length.SHORT.getValue());
        addHill(RoadDefinition.Length.MEDIUM.getValue(), RoadDefinition.Hill.LOW.getValue());
        addLowRollingHills(null, null);
        addBumps();
        addCurve(RoadDefinition.Length.MEDIUM.getValue(), RoadDefinition.Curve.MEDIUM.getValue(), RoadDefinition.Hill.LOW.getValue());
        //addBumps();
        //addLowRollingHills(null, null);
        addCurve(RoadDefinition.Length.LONG.getValue(), RoadDefinition.Curve.MEDIUM.getValue(), RoadDefinition.Hill.MEDIUM.getValue());
        //addStraight(null);
        //addHill(RoadDefinition.Length.MEDIUM.getValue(), RoadDefinition.Hill.HIGH.getValue());
        addCurve(RoadDefinition.Length.LONG.getValue(), -RoadDefinition.Curve.MEDIUM.getValue(), RoadDefinition.Hill.MEDIUM.getValue());
        //addHill(RoadDefinition.Length.LONG.getValue(), RoadDefinition.Hill.HIGH.getValue());
        addCurve(RoadDefinition.Length.LONG.getValue(), -RoadDefinition.Curve.MEDIUM.getValue(), RoadDefinition.Hill.LOW.getValue());
        //addBumps();
        //addHill(RoadDefinition.Length.LONG.getValue(), -RoadDefinition.Hill.MEDIUM.getValue());
        addStraight(null);
        addDownhillToEnd(null);

        resetSprites();
        resetCars();
        
        segments.get(findSegment(playerZ).getIndex() + 2).setColor(Colors.getRoadStart());
        segments.get(findSegment(playerZ).getIndex() + 3).setColor(Colors.getRoadStart());
        for (int n = 0; n < RUMBLE_LENGTH; n++) {
            segments.get(segments.size() - 1 - n).setColor(Colors.getRoadFinish());
        }
        TRACK_LENGTH = segments.size() * SEGMENT_LENGTH;
    }

    public void resetSprites() {
        List<Integer> intList = new ArrayList<>(List.of(1, -1));
        addSprite(50,  SPRITES.BILLBOARD07, -1);
        addSprite(40,  SPRITES.BILLBOARD06, -1);
        addSprite(60,  SPRITES.BILLBOARD08, -1);
        addSprite(80,  SPRITES.BILLBOARD09, -1);
        addSprite(100, SPRITES.BILLBOARD01, -1);
        addSprite(120, SPRITES.BILLBOARD02, -1);
        addSprite(140, SPRITES.BILLBOARD03, -1);
        addSprite(160, SPRITES.BILLBOARD04, -1);
        addSprite(180, SPRITES.BILLBOARD05, -1);
        
        addSprite(240,SPRITES.BILLBOARD07, -1.2);
        addSprite(240,SPRITES.BILLBOARD06, 1.2);
        addSprite(segments.size() - 25, SPRITES.BILLBOARD07, -1.2);
        addSprite(segments.size() - 25, SPRITES.BILLBOARD06,  1.2);


        for (int n = 10; n < 200; n += 4 + Math.floor(n / 100)) {
            addSprite(n, SPRITES.PALM_TREE, 0.5 + Math.random() * 0.5);
            addSprite(n, SPRITES.PALM_TREE,   1 + Math.random() * 2);
        }

        for (int n = 250; n < 1000; n += 5) {
            addSprite(n, SPRITES.COLUMN , 1.1);
            addSprite(n + util.randomInt(0, 5), SPRITES.TREE1, -1 - (Math.random() * 2));
            addSprite(n + util.randomInt(0, 5), SPRITES.TREE2, -1 - (Math.random() * 2));
        }

        for (int n = 200; n < segments.size(); n += 3) {
            addSprite(n, util.randomChoice(SPRITES.PLANTS), util.randomChoice(intList) * (2 + Math.random() * 5));
        }

        for (int n = 1000; n < (segments.size() - 50); n += 100) {
            double side = util.randomChoice(intList);
            addSprite(n + util.randomInt(0, 50), util.randomChoice(SPRITES.BILLBOARDS), -side);
            for (int i = 0; i < 20; i++) {
                Sprite sprite = util.randomChoice(SPRITES.PLANTS);
                double offset = side * (1.5 + Math.random());
                addSprite(n + util.randomInt(0,50), sprite, offset);
            }
        }

    }

    public void resetCars() {
        cars.clear();
        for (int n = 0; n < totalCars; n++) {
            List<Double> choicesList = new ArrayList<>(Arrays.asList(-0.8, 0.8));
            double offset = Math.random() * util.randomChoice(choicesList);
            double z = Math.floor(Math.random() * segments.size()) * SEGMENT_LENGTH;
            Sprite sprite = util.randomChoice(SPRITES.CARS);
            System.out.println(sprite);
            double speed = MAX_SPEED / 4 + Math.random() * MAX_SPEED / (sprite == SPRITES.SEMI ? 4 : 2);
            Car car = new Car(offset, z, sprite, speed);
            Segment segment = findSegment(car.getZ());
            segment.getCars().add(car);
            cars.add(car);
        }
    }
    
    //=========================================================================
    // THE GAME LOOP
    //=========================================================================e Segmprivate void gameLoop(GraphicsContext gtx) {

    private void reset() {
        CAMERA_DEPTH = 1 / Math.tan((FIELD_OF_VIEW / 2) * Math.PI / 180);
        playerZ = (CAMERA_HEIGHT * CAMERA_DEPTH);
        resolution = HEIGHT / 480;
        resetRoad();
    }

    public void frame(GraphicsContext ctx) {
        long timeNow = System.currentTimeMillis();
        double deltaTime = Math.min(1, (timeNow - lastTime) / 1000.0);
        globalDeltaTime += deltaTime;
        double step = 1.0 / FPS; 
        update(step);
        render(ctx);
        lastTime = timeNow;
    }

    //=========================================================================
    // TWEAK UI HANDLERS
    //=========================================================================

    private void getSettingsFromApp(Stage primaryStage){ //#TODO 
        ROAD_WIDTH = App.getRoadWidthSliderValue();
        LANES = App.getLanesSliderValue();
        lanes = LANES;
        CAMERA_HEIGHT = App.getCameraHeightSliderValue();
        DRAW_DISTANCE = App.getDrawDistanceSliderValue();
        FIELD_OF_VIEW = App.getFieldOfViewSliderValue();
        FOG_DENSITY = App.getFogDensitySliderValue();
        WIDTH = App.getResolutionSliderValueWidth();
        HEIGHT = App.getResolutionSliderValueHeight();
        if(App.getFullscreenToggleValue()){
            fullscreen = true;
                }
    }

    public void endScreen(GraphicsContext ctx) {
        String username = "Player 1";
        if (currentLap > maxLap) {
            if(!finishedPlayers.contains(username)){
            finishedPlayers.add(username);
            }
            double canvasWidth = WIDTH;
            double canvasHeight = HEIGHT;

            ctx.setFill(Color.RED);
            ctx.setFont(Font.font("Arial", FontWeight.BOLD, 60));
            ctx.fillText("RACE FINISHED", canvasWidth / 3.5 , canvasHeight / 4);


            ctx.setFill(Color.WHITE);
            ctx.setFont(Font.font("Arial", FontWeight.BOLD, 20)); // Beispiel für die Schriftgröße und Schriftart
            for (int i = 0; i < finishedPlayers.size(); i++) {
                String playerLabel = (i + 1)+ "# " + finishedPlayers.get(i);

                Text text = new Text(playerLabel);
                text.setFont(Font.font("Arial", FontWeight.BOLD, 20)); // Schriftgröße und Schriftart festlegen
                double textWidth = text.getBoundsInLocal().getWidth();
                double textHeight = text.getBoundsInLocal().getHeight();

                ctx.fillText(playerLabel, canvasWidth / 3.5, canvasHeight / 3 + i * textHeight * 1.5);
            }
        }
    }

    public void updateHUD(GraphicsContext ctx) {
        ctx.setFill(Color.rgb(255, 0, 0, 0.3));
        ctx.fillRect(0, 0, WIDTH, HEIGHT / 8);

        ctx.setFill(Color.BLACK);
        ctx.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        ctx.fillText((int) speed / 100 + " Km/h", 0, 20);
        ctx.fillText("Last Lap: " +  lastLapTime + " Sekunden", 0, 45); // #TODO
        ctx.fillText(currentLap + "/4 Laps", 0, 70);
    
        double nitroHud = nitro / 100;
        double maxNitroHud = maxNitro / 100;
    
        double totalBlackBarWidth = (50 * 10) + 20; 
    
        ctx.setFill(Color.BLACK);
        ctx.fillRect(300 - 10, 25 - 10, totalBlackBarWidth, 40 + 20);
    
        ctx.setStroke(Color.BLACK);
        ctx.setLineWidth(5); 
        ctx.strokeRect(300, 25, (50 * 10) * nitroHud, 40);
    
        if (nitroRecharge) {
            ctx.setFill(Color.rgb(255, 0, 0));
        } else {
            ctx.setFill(Color.rgb(77, 187, 255));
        }
        ctx.fillRect(300, 25, (50 * 10) * nitroHud, 40);
    
        ctx.setFill(Color.BLACK);
        ctx.setGlobalAlpha(0.5);
        ctx.fillRect(300, 25, (50 * 10) * maxNitroHud, 40);
        ctx.setGlobalAlpha(1.0);
    
        if (nitroRecharge) {
            ctx.setFill(Color.rgb(255, 0, 0));
            ctx.drawImage(nitroBottleEmpty, 295 + 550, 30, 40 * 2.5, 13 * 2.5);
        } else {
            ctx.setFill(Color.rgb(77, 187, 230));
            ctx.drawImage(nitroBottle, 295 + 550, 30, 40 * 2.5, 13 * 2.5);
        }
    
        if (App.getOfflineMode()) { 
            ctx.setFill(Color.RED);
            ctx.setFont(Font.font("Arial", FontWeight.BOLD, 24));
            ctx.fillText(place + ".", 0, 95);
        }
    }
}