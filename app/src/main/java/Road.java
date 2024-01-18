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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class Road extends Application {
    private int FPS = 55;
    private static int WIDTH = 1024;
    private int HEIGHT = 768;
    private int LANES = 3;
    private int ROAD_WIDTH = 2000;
    private int SEGMENT_LENGTH = 200;
    private int RUMBLE_LENGTH = 3;
    private int CAMERA_HEIGHT = 1000;
    private double DRAW_DISTANCE = 300;
    private int FIELD_OF_VIEW = 100;
    private int FOG_DENSITY = 5;
    private double MAX_SPEED = SEGMENT_LENGTH / (1.0 / (FPS +5)) ;
    private double ACCEL = MAX_SPEED / 5;
    private double BREAKING = -MAX_SPEED;
    private double DECEL = -MAX_SPEED / 5;
    private double OFF_ROAD_DECEL = -MAX_SPEED / 2;
    private double OFF_ROAD_LIMIT = MAX_SPEED / 4;
    private double TRACK_LENGTH;
    private double CAMERA_DEPTH;
    private double resolution; 
    private double centrifugal_force = 0.3; 
    private double skySpeed = 0.001;  
    private double hillSpeed = 0.002;        
    private double treeSpeed = 0.003;              
    private double skyOffset = 0;                    
    private double hillOffset = 0;                     
    private double treeOffset = 0;                       
    private int totalCars = 100;   
    private double currentLapTime = 0;
    private double lastLapTime = 0;
    private int currentLap = 0;
    private int maxLap = 3;
    private int place;
    private int playerNum = 1;


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
    private static boolean nitroRecharge = false;
    private boolean nitroActive = false;
    private boolean fullscreen = false;
    private boolean gameFinished = false;

    private ArrayList<Segment> segments = new ArrayList<>();
    private ArrayList<Car> cars = new ArrayList<>();
    private Object playerCarsLock = new Object();
    private Object npcCarsLock = new Object();
    private ArrayList<Car> playerCars = new ArrayList<>();
    private ArrayList<String> finishedPlayers = new ArrayList<>();
    private JSONArray player_start_positions = new JSONArray();

    private Image background = new Image("file:src/main/java/images/background.png");
    private Image sprites = new Image("file:src/main/java/images/spritesheet.png");
    private Image nitroBottle = new Image("file:src/main/java/images/nitro.png");
    private Image nitroBottleEmpty = new Image("file:src/main/java/images/nitro_empty.png");

    Util util = new Util();
    Render render = new Render();

    private Sprites SPRITES = new Sprites();
    private static double hudScale = 1;
    private boolean isOfflineMode;
    private String clientID;
    private String username;
    private boolean isHost;
    private Map<String, String> clientIDs;
    private long lastTime = System.currentTimeMillis();
    private Socket socket;
    AnimationTimer gameLoop;

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
            if (!gameFinished) {
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
            } else{
                switch (event.getCode()) {
                    case Q:
                        App.switchScene(primaryStage, App.getGameScene());
                        gameLoop.stop();
                        break;
                }
            }
        });

        scene.setOnKeyReleased(event -> {
            if (!gameFinished) {
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
            }
        });

        reset();

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                frame(ctx);
            }
        };
        if(!isOfflineMode){
            socket.emit("game_start");
        }
        gameLoop.start();
        
        primaryStage.setScene(scene);
        if (fullscreen) {
            primaryStage.setFullScreen(true);
            root.maxWidthProperty().bind(primaryStage.widthProperty());
            root.maxHeightProperty().bind(primaryStage.heightProperty());
        }
        primaryStage.show();

    }


    //=========================================================================
    // SocketIO Eventhandler
    //=========================================================================
    private void socketIOEventHandler() {
        /**
         * SocketIO Eventhandler for receiving data from the server
         */

        // Receive the player Car data from the server
        socket.on("receive_data", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Platform.runLater(() -> {
                    JSONObject data = (JSONObject) args[0];
                    String key = (String)data.keys().next();
                    Car car = null;
                    try {
                        data = data.getJSONObject(key);
                        ObjectMapper objectMapper = new ObjectMapper();
                        car = objectMapper.readValue(data.toString(), Car.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                    synchronized (playerCarsLock) {
                        // Update the current car data
                        for (int i = 0; i < playerCars.size(); i++) {
                            Car existingCar = playerCars.get(i);
                            if (existingCar.getId().equals(car.getId())) {
                                playerCars.set(i, car);
                                return;
                            }
                        }
                        playerCars.add(car);
                    }
                });
            }
        });

        // Receive the player order from server
        socket.on("receive_order", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Platform.runLater(() -> {
                        JSONArray data = (JSONArray) args[0];
                        try {
                            // Deserialize the player order
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject element = data.getJSONObject(i);
                                String id = element.getString("id");
                                if (id.equals(clientID)) {
                                    place = data.length() - i;
                                }
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                });
            }
        });

        // Receive npc cardata
        socket.on("receive_npc_car_data", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Platform.runLater(() -> {
                    if (isHost || isOfflineMode) return;
                    JSONArray data = (JSONArray) args[0];
                    
                    cars.clear();
                    try {
                        // Deserialize the npc car data
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject element = data.getJSONObject(i);
                            JSONArray sprite = (JSONArray) element.get("sprite");
                            JSONObject source = (JSONObject) sprite.get(1);
                            Sprite carSprite = new Sprite(((Integer)source.get("x")).doubleValue(), ((Integer) source.get("y")).doubleValue(), ((Integer) source.get("w")).doubleValue(), ((Integer) source.get("h")).doubleValue());
                            Car car = new Car(element.getDouble("offset"), element.getDouble("z"), carSprite, element.getDouble("speed"));
                            cars.add(car);
                            putCarsIntoSegments();
                    }
                    } catch (Exception e) {
                    e.printStackTrace();
                    }
                });
            }
        });

        // Receive the player start positions
        socket.on("receive_start_position", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Platform.runLater(() -> {
                    JSONArray data = (JSONArray) args[0];
                    try {
                        // Deserialize the player start positions
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject element = data.getJSONObject(i);
                            if (!(element.getString("id").equals(clientID))) continue;
                            playerX = element.getDouble("offset");
                            position = element.getDouble("z");
                            playerNum = element.getInt("player_num");
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });


    }

    private void putCarsIntoSegments() {
        synchronized (npcCarsLock) {
            for (Segment segment : segments) {
                segment.getCars().clear();
            }
            for (Car car : cars) {
                Segment segment = findSegment(car.getZ());
                segment.getCars().add(car);
            }
        }
    }

    private void send_data() {
        /**
         * Sends the player data to the server
         */
        try {
            socket.emit("player_data", new JSONObject()
                    .put("playerX", playerX)
                    .put("player_num", playerNum)
                    .put("nitro", nitroActive)
                    .put("id", clientID)
                    .put("position", position)
                    .put("speed", speed)
                    .put("current_lap", currentLap)
                    .put("username", username)
            );
            

            if (isHost) {
                try {
                    // ObjectMapper objectMapper = JsonMapper.builder()
                    //     .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                    //     .build();
                    ObjectMapper objectMapper = new ObjectMapper();
                    String jsonCarArray = objectMapper.writeValueAsString(cars);

                    JSONArray jsonArray = new JSONArray();
                    for (Car car : cars) {
                        JSONObject jsonObject = new JSONObject()
                            .put("z", car.getZ())
                            .put("speed", car.getSpeed())
                            .put("offset", car.getOffset())
                            .put("percent", car.getPercent())
                            .put("sprite", new JSONArray()
                                .put(Sprites.getSpriteName(car.getSprite().getX(), car.getSprite().getY(), car.getSprite().getW(), car.getSprite().getH()))
                                .put(new JSONObject()
                                    .put("x", car.getSprite().getX())
                                    .put("y", car.getSprite().getY())
                                    .put("w", car.getSprite().getW())
                                    .put("h", car.getSprite().getH())
                                )
                            );
                        jsonArray.put(jsonObject);
                    }

                    socket.emit("npc_car_data", jsonArray);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } 
        
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    

    //=========================================================================
    // UPDATE THE GAME WORLD
    // =========================================================================

    private void update(double delta_time) {

        send_data();
        
        Car car;
        double carW;
        Sprite sprite;
        double spriteW;

        Segment playerSegment = findSegment(position + playerZ);
        double playerW = SPRITES.PLAYER_STRAIGHT.getW() * SPRITES.SCALE;
        double speedPercent = speed / MAX_SPEED;
        double dx = delta_time * 2 * speedPercent; 
        double startPosition = position;

        if (isHost) updateCars(delta_time, playerSegment, playerW);

        position = util.increase(position, delta_time * speed, TRACK_LENGTH);

        skyOffset = util.increase(skyOffset,
                skySpeed * playerSegment.getCurve() * (position - startPosition) / SEGMENT_LENGTH, 1);
        hillOffset = util.increase(hillOffset,
                hillSpeed * playerSegment.getCurve() * (position - startPosition) / SEGMENT_LENGTH, 1);
        treeOffset = util.increase(treeOffset,
                treeSpeed * playerSegment.getCurve() * (position - startPosition) / SEGMENT_LENGTH, 1);

        if (keyLeft)
            playerX = playerX - dx;
        else if (keyRight)
            playerX = playerX + dx;

        playerX = playerX - (dx * speedPercent * playerSegment.getCurve() * centrifugal_force); 

        if (keyFaster)
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
                speed = util.accelerate(speed, ACCEL * 2.5, delta_time);
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
                spriteW = sprite.getSource().getW() * SPRITES.SCALE;
                double spriteX = sprite.getOffset() + spriteW / 2 * (sprite.getOffset() > 0 ? 1 : -1);
                if (util.overlap(playerX, playerW, spriteX, spriteW, 0)) { 
                    speed = MAX_SPEED / 5;
                    position = util.increase(playerSegment.getP1().getWorld().getZ(), -playerZ, TRACK_LENGTH);
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

        // Collision with other players
        for (int i = 0; i < playerCars.size(); i++) {
            Car otherPlayer = playerCars.get(i);
            if (otherPlayer.getId().equals(clientID)) continue;
            Segment otherPlayerSegment = findSegment(otherPlayer.getZ() + playerZ);
            if (playerSegment != otherPlayerSegment) continue;

            double otherPlayerW = SPRITES.PLAYER_STRAIGHT.getW() * SPRITES.SCALE;
            if (speed > otherPlayer.getSpeed()) {
                if (util.overlap(playerX, playerW, otherPlayer.getOffset(), otherPlayerW, 0.8)) {
                    speed = otherPlayer.getSpeed() * (otherPlayer.getSpeed() / speed);
                    position = util.increase(otherPlayer.getZ(), -playerZ, TRACK_LENGTH);
                    break;
                }
            }
        }


            
        playerX = util.limit(playerX, -2, 2); 
        speed = util.limit(speed, 0, MAX_SPEED);

        if (!isOfflineMode) {
            send_data();
        }

        if (position > playerZ) {
            if (currentLapTime != 0 && (startPosition < playerZ)) {
                lastLapTime = currentLapTime;
                currentLapTime = 0;
                currentLap += 1;
            } else {
                currentLapTime += delta_time;
            }
        }
    }

    public static boolean getNitroRecharge() {
        return nitroRecharge;
    }

    private void updateCars(double dt, Segment playerSegment, double playerW) {
        Segment oldSegment, newSegment;
        for (int n = 0; n < cars.size(); n++) {
            Car car = cars.get(n);
            oldSegment = findSegment(car.getZ());
            car.setOffset(car.getOffset() + updateCarOffset(car, oldSegment, playerSegment, playerW));
            car.setZ(util.increase(car.getZ(), dt * car.getSpeed(), TRACK_LENGTH));
            car.setPercent(util.percentRemaining(car.getZ(), SEGMENT_LENGTH)); 
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

        if ((carSegment.getIndex() - playerSegment.getIndex()) > DRAW_DISTANCE)
            return 0;

        for (int i = 1; i < lookahead; i++) {
            segment = segments.get((carSegment.getIndex() + i) % segments.size());

            if ((segment == playerSegment) && (car.getSpeed() > speed)
                    && (util.overlap(playerX, playerW, car.getOffset(), carW, 1.2))) {
                if (playerX > 0.5)
                    dir = -1;
                else if (playerX < -0.5)
                    dir = 1;
                else
                    dir = (car.getOffset() > playerX) ? 1 : -1;
                return dir * 1 / i * (car.getSpeed() - speed) / MAX_SPEED; 
            }

            for (int j = 0; j < segment.getCars().size(); j++) {
                otherCar = segment.getCars().get(j);
                otherCarW = otherCar.getSprite().getW() * SPRITES.SCALE;
                if ((car.getSpeed() > otherCar.getSpeed())
                        && util.overlap(car.getOffset(), carW, otherCar.getOffset(), otherCarW, 1.2)) {
                    if (otherCar.getOffset() > 0.5)
                        dir = -1;
                    else if (otherCar.getOffset() < -0.5)
                        dir = 1;
                    else
                        dir = (car.getOffset() > otherCar.getOffset()) ? 1 : -1;
                    return dir * 1 / i * (car.getSpeed() - otherCar.getSpeed()) / MAX_SPEED;
                }
            }
        }


        if (car.getOffset() < -0.9)
            return 0.1;
        else if (car.getOffset() > 0.9)
            return -0.1;
        else
            return 0;
    }

    // =========================================================================
    // RENDER THE GAME WORLD
    // =========================================================================
    private void render(GraphicsContext ctx) {
        Segment baseSegment = findSegment(position);
        double basePercent = util.percentRemaining(position, SEGMENT_LENGTH);
        Segment playerSegment = findSegment(position + playerZ);
        double playerPercent = util.percentRemaining(position + playerZ, SEGMENT_LENGTH);
        double playerY = util.interpolate(playerSegment.getP1().getWorld().getY(),
                playerSegment.getP2().getWorld().getY(), playerPercent);
        double maxy = HEIGHT;

        double x = 0;
        double dx = -(baseSegment.getCurve() * basePercent);

        ctx.clearRect(0, 0, WIDTH, HEIGHT);
        ctx.setFill(Color.web("#72D7EE"));
        ctx.fillRect(0, 0, WIDTH, HEIGHT);

        render.background(ctx, background, WIDTH, HEIGHT, Background.SKY, skyOffset, resolution * skySpeed * playerY); 
        render.background(ctx, background, WIDTH, HEIGHT, Background.HILLS, hillOffset,
                resolution * hillSpeed * playerY);
        render.background(ctx, background, WIDTH, HEIGHT, Background.TREES, treeOffset,
                resolution * treeSpeed * playerY);

        Car car;
        Sprite sprite;
        double spriteScale;
        double spriteX;
        double spriteY;

        for (int n = 0; n < DRAW_DISTANCE; n++) {
            Segment segment = segments.get((baseSegment.getIndex() + n) % segments.size());
            segment.setLooped(segment.getIndex() < baseSegment.getIndex());
            segment.setFog(util.exponentialFog(n / DRAW_DISTANCE, FOG_DENSITY));
            segment.setClip(maxy);

            util.project(segment.getP1(), (playerX * ROAD_WIDTH) - x, playerY + CAMERA_HEIGHT,
                    position - (segment.isLooped() ? TRACK_LENGTH : 0), CAMERA_DEPTH, WIDTH, HEIGHT, ROAD_WIDTH);
            util.project(segment.getP2(), (playerX * ROAD_WIDTH) - x - dx, playerY + CAMERA_HEIGHT,
                    position - (segment.isLooped() ? TRACK_LENGTH : 0), CAMERA_DEPTH, WIDTH, HEIGHT, ROAD_WIDTH);

            x = x + dx;
            dx = dx + segment.getCurve();

            if ((segment.getP1().getCamera().getZ() <= CAMERA_DEPTH) ||
                    (segment.getP2().getScreen().getY() >= segment.getP1().getScreen().getY()) ||
                    (segment.getP2().getScreen().getY() >= maxy)) {
                continue;
            }

            render.segment(
                    ctx,
                    WIDTH,
                    LANES,
                    segment.getP1().getScreen().getX(), 
                    segment.getP1().getScreen().getY(),
                    segment.getP1().getScreen().getWidth(),
                    segment.getP2().getScreen().getX(),
                    segment.getP2().getScreen().getY(),
                    segment.getP2().getScreen().getWidth(),
                    segment.getFog(),
                    segment.getColor());

            maxy = segment.getP1().getScreen().getY();
        }
        for (int n = ((int)DRAW_DISTANCE - 1); n > 0; n--) {
            Segment segment = segments.get((baseSegment.getIndex() + n) % segments.size());

            for (int i = 0; i < segment.getCars().size(); i++) {
                car = segment.getCars().get(i);
                sprite = car.getSprite();
                spriteScale = util.interpolate(segment.getP1().getScreen().getScale(),
                        segment.getP2().getScreen().getScale(), car.getPercent());
                spriteX = util.interpolate(segment.getP1().getScreen().getX(), segment.getP2().getScreen().getX(),
                        car.getPercent()) + (spriteScale * car.getOffset() * ROAD_WIDTH * WIDTH / 2);
                spriteY = util.interpolate(segment.getP1().getScreen().getY(), segment.getP2().getScreen().getY(),
                        car.getPercent());
                render.sprite(ctx, WIDTH, HEIGHT, resolution, ROAD_WIDTH, sprites, car.getSprite(), spriteScale,
                        spriteX, spriteY, -0.5, -1, segment.getClip());
            }

                // Side Sprites
                for(int i = 0; i < segment.getSprites().size(); i++) {
                    sprite = segment.getSprites().get(i);
                    spriteScale = segment.getP1().getScreen().getScale();
                    spriteX = segment.getP1().getScreen().getX() + (spriteScale * sprite.getOffset() * ROAD_WIDTH * WIDTH / 2);
                    spriteY = segment.getP1().getScreen().getY();
                    render.sprite(ctx, WIDTH, HEIGHT, resolution, ROAD_WIDTH,sprites, sprite.getSource(), spriteScale, spriteX, spriteY,  (sprite.getOffset() < 0 ? -1 : 0), -1, segment.getClip());
                }

                // Render other players (if multiplayer)
                if (!isOfflineMode) {
                    synchronized (playerCarsLock) {
                        for (int i = 0; i < playerCars.size(); i++) {
                            if (playerCars.get(i).getId().equals(clientID)) continue;
                            Car otherCar = playerCars.get(i);
                            Segment otherCarSegment = findSegment(otherCar.getZ() + playerZ);
                            Sprite otherCarSprite = null;

                            if (otherCar.getCurrent_lap() > maxLap) {
                                if (!finishedPlayers.contains(otherCar.getUsername()))
                                    finishedPlayers.add(otherCar.getUsername());
                            }
                            

                            if (segment == otherCarSegment) {
                                int otherPlayerNum = otherCar.getPlayer_num();
                                double otherCarPercent = util.percentRemaining(otherCar.getZ() + playerZ, SEGMENT_LENGTH);

                                if (otherCar.getIsNitro()) {
                                    switch (otherPlayerNum) {
                                        case 1:
                                            otherCarSprite = Sprites.PLAYER_1_STRAIGHT_NITRO;
                                            break;
                                        case 2:
                                            otherCarSprite = Sprites.PLAYER_2_STRAIGHT_NITRO;
                                            break;
                                        case 3:
                                            otherCarSprite = Sprites.PLAYER_3_STRAIGHT_NITRO;
                                            break;
                                    }
                                }
                                else {
                                    switch (otherPlayerNum) {
                                        case 1:
                                            otherCarSprite = Sprites.PLAYER_1_STRAIGHT;
                                            break;
                                        case 2:
                                            otherCarSprite = Sprites.PLAYER_2_STRAIGHT;
                                            break;
                                        case 3:
                                            otherCarSprite = Sprites.PLAYER_3_STRAIGHT;
                                            break;
                                    }
                                }

                                double otherSpriteScale = util.interpolate(otherCarSegment.getP1().getScreen().getScale(), otherCarSegment.getP2().getScreen().getScale(), otherCarPercent);
                                double otherCarSpriteX = util.interpolate(otherCarSegment.getP1().getScreen().getX(), otherCarSegment.getP2().getScreen().getX(), otherCarPercent) + (otherSpriteScale * otherCar.getOffset() * ROAD_WIDTH * WIDTH / 2);
                                double otherCarSpriteY = util.interpolate(otherCarSegment.getP1().getScreen().getY(), otherCarSegment.getP2().getScreen().getY(), otherCarPercent);
                                render.sprite(ctx, WIDTH, HEIGHT, resolution, ROAD_WIDTH, sprites, otherCarSprite, otherSpriteScale, otherCarSpriteX, otherCarSpriteY, -0.5, -1, segment.getClip());
                            }
                        }
                    }
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
                        (HEIGHT / 2) - (CAMERA_DEPTH / playerZ
                                * util.interpolate(playerSegment.getP1().getCamera().getY(),
                                        playerSegment.getP2().getCamera().getY(), playerPercent)
                                * HEIGHT / 2),
                        speed * (keyLeft ? -1 : keyRight ? 1 : 0),
                        playerSegment.getP2().getWorld().getY() - playerSegment.getP1().getWorld().getY(),
                        nitrokey,
                        playerNum);
                }
            }
    }

    Segment findSegment(double z) {
        return segments.get((int) Math.floor(z / SEGMENT_LENGTH) % segments.size());
    }

    // =========================================================================
    // BUILD ROAD GEOMETRY
    // =========================================================================

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
                rumbleColor));
    }

    private void addSprite(int n, Sprite sprite, double offset) {
        segments.get(n).getSprites().add(new Sprite(offset, sprite));
    }

    private void addRoad(int enter, int hold, int leave, int curve, int d) {
        double startY = lastY();
        double endY = startY + (util.toInt(d, 0) * SEGMENT_LENGTH);
        int n;
        double total = enter + hold + leave;

        for (n = 0; n < enter; n++) {
            addSegment(util.easeIn(0, curve, n / enter), util.easeInOut(startY, endY, n / total));
        }
        for (n = 0; n < hold; n++) {
            addSegment(curve, util.easeInOut(startY, endY, (enter + n) / total));
        }
        for (n = 0; n < leave; n++) {
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
        addRoad(10, 10, 10, 0, 5);
        addRoad(10, 10, 10, 0, -2);
        addRoad(10, 10, 10, 0, -5);
        addRoad(10, 10, 10, 0, 8);
        addRoad(10, 10, 10, 0, 5);
        addRoad(10, 10, 10, 0, -7);
        addRoad(10, 10, 10, 0, 5);
        addRoad(10, 10, 10, 0, -2);
    }

    private void addDownhillToEnd(Integer num) {
        if (num == null) {
            num = 200;
        }
        addRoad(num, num, num, -RoadDefinition.Curve.EASY.getValue(), (int) -lastY() / SEGMENT_LENGTH);

    }

    private void addLowRollingHills(Integer num, Integer height) {
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
        addCurve(RoadDefinition.Length.MEDIUM.getValue(), RoadDefinition.Curve.MEDIUM.getValue(),
                RoadDefinition.Hill.LOW.getValue());
        addCurve(RoadDefinition.Length.LONG.getValue(), RoadDefinition.Curve.MEDIUM.getValue(),
                RoadDefinition.Hill.MEDIUM.getValue());
        addCurve(RoadDefinition.Length.LONG.getValue(), -RoadDefinition.Curve.MEDIUM.getValue(),
                RoadDefinition.Hill.MEDIUM.getValue());
        addCurve(RoadDefinition.Length.LONG.getValue(), -RoadDefinition.Curve.MEDIUM.getValue(),
                RoadDefinition.Hill.LOW.getValue());
        addStraight(null);
        addDownhillToEnd(null);


        
        segments.get(findSegment(playerZ).getIndex() + 2).setColor(Colors.getRoadStart());
        segments.get(findSegment(playerZ).getIndex() + 3).setColor(Colors.getRoadStart());
        for (int n = 0; n < RUMBLE_LENGTH; n++) {
            segments.get(segments.size() - 1 - n).setColor(Colors.getRoadFinish());
        }
        TRACK_LENGTH = segments.size() * SEGMENT_LENGTH;
    }

    public void resetSprites() {
        List<Integer> intList = new ArrayList<>(List.of(1, -1));
        addSprite(50, SPRITES.BILLBOARD07, -1);
        addSprite(40, SPRITES.BILLBOARD06, -1);
        addSprite(60, SPRITES.BILLBOARD08, -1);
        addSprite(80, SPRITES.BILLBOARD09, -1);
        addSprite(100, SPRITES.BILLBOARD01, -1);
        addSprite(120, SPRITES.BILLBOARD02, -1);
        addSprite(140, SPRITES.BILLBOARD03, -1);
        addSprite(160, SPRITES.BILLBOARD04, -1);
        addSprite(180, SPRITES.BILLBOARD05, -1);

        addSprite(240, SPRITES.BILLBOARD07, -1.2);
        addSprite(240, SPRITES.BILLBOARD06, 1.2);
        addSprite(segments.size() - 25, SPRITES.BILLBOARD07, -1.2);
        addSprite(segments.size() - 25, SPRITES.BILLBOARD06, 1.2);

        /*
         * for (int n = 10; n < 200; n += 4 + Math.floor(n / 100)) {
         * addSprite(n, SPRITES.PALM_TREE, 0.5 + Math.random() * 0.5);
         * addSprite(n, SPRITES.PALM_TREE, 1 + Math.random() * 2);
         * }
         */

        for (int n = 250; n < 1000; n += 50) {
            double offset = Math.floorDiv(n, 50);
            double value1 = n + (offset % 6);
            double value2 = -1 - ((offset % 11) /5);

            addSprite(n, SPRITES.COLUMN, 1.1);
            addSprite((int)(n + value1), SPRITES.TREE1, value2);
            addSprite(n, SPRITES.TREE2, (n % 2 == 0) ? -1 : 1);
        }

        for (int n = 200; n < segments.size(); n += 30) {
            int key_index = n % SPRITES.PLANTS.size();
            Sprite key = SPRITES.PLANTS.get(key_index);
            double offset1 = ((Math.floorDiv(n, 30) % 2) * 2) -1;
            double offset2 = 2 + (Math.floorDiv(n, 30)) % 5;
            addSprite(n, key, offset1 * offset2);
        }

        for (int n = 1000; n < (segments.size() - 50); n += 500) {
            double side = ((Math.floorDiv(n, 500) % 2) *2 ) -1;
            int key_index = n % SPRITES.BILLBOARDS.size();
            Sprite key = SPRITES.BILLBOARDS.get(key_index);

            addSprite(n + Math.floorDiv(n, 50) % 50,  key, -side * (1.5 + ((Math.floorDiv(n, 50) % 100) / 100)));
            for (int i = 0; i < 20; i++) {
                key_index = (Math.floorDiv(n, 50) + i) % SPRITES.PLANTS.size();
                key = SPRITES.PLANTS.get(key_index);
                Sprite sprite = SPRITES.PLANTS.get(key_index);
                double offset = side * (1.5 +(Math.floorDiv(n, 50 + i) % 100) / 100);
                addSprite(n + Math.floorDiv(n, 50), sprite, offset);
            }
        }

    }

    public JSONArray reset_player_start_positions() {
        player_start_positions = new JSONArray();
        Double offset = -0.6;
        int player_num = 1;

        for (String key : clientIDs.keySet()) {
            try {
                JSONObject new_player_start_positions = new JSONObject()
                    .put("id", key)
                    .put("offset", offset)
                    .put("z", 0)
                    .put("player_num", player_num)
                    .put("speed", 0);
                player_start_positions.put(new_player_start_positions);
                offset += 0.66;
                player_num += 1;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        socket.emit("player_start_positions_data", player_start_positions);
        return player_start_positions;
    }


    public void resetCars() {
        cars.clear();
        for (int n = 0; n < totalCars; n++) {
            List<Double> choicesList = new ArrayList<>(Arrays.asList(-0.8, 0.8));
            double offset = Math.random() * util.randomChoice(choicesList);
            double z = Math.floor(Math.random() * segments.size()) * SEGMENT_LENGTH;
            Sprite sprite = util.randomChoice(SPRITES.CARS);
            double speed = MAX_SPEED / 4 + Math.random() * MAX_SPEED / (sprite == SPRITES.SEMI ? 4 : 2);
            Car car = new Car(offset, z, sprite, speed);
            Segment segment = findSegment(car.getZ());
            segment.getCars().add(car);
            cars.add(car);
        }
    }

    // =========================================================================
    // THE GAME LOOP
    // =========================================================================e

        private void reset() {
            CAMERA_DEPTH = 1 / Math.tan((FIELD_OF_VIEW / 2) * Math.PI / 180);
            playerZ = (CAMERA_HEIGHT * CAMERA_DEPTH);
            resolution = HEIGHT / 480;
            currentLap = 0;
            resetRoad();
            resetSprites();
    
            if (isHost) {
                resetCars();
                if (!isOfflineMode) {
                    reset_player_start_positions();
                    socket.emit("npc_car_data", cars);
                }
            }
            else {
                if (!isOfflineMode) {
                    socket.emit("request_start_position"); 
                }
            }
    
        }

    public void frame(GraphicsContext ctx) {
        long now = System.currentTimeMillis();
        double targetFrameTime = 1.0 / FPS;
        double delta_time = (now - lastTime) / 1000.0; 

        if (delta_time > targetFrameTime){
            update(delta_time);
            render(ctx);
            endScreen(ctx);
            updateHUD(ctx);
            lastTime = now;
        }

        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
    }

    // =========================================================================
    // TWEAK UI HANDLERS
    // =========================================================================

    private void getSettingsFromApp(Stage primaryStage) { 
        ROAD_WIDTH = App.getRoadWidthSliderValue();
        LANES = App.getLanesSliderValue();
        CAMERA_HEIGHT = App.getCameraHeightSliderValue();
        DRAW_DISTANCE = App.getDrawDistanceSliderValue();
        FIELD_OF_VIEW = App.getFieldOfViewSliderValue();
        FOG_DENSITY = App.getFogDensitySliderValue();
        WIDTH = App.getResolutionSliderValueWidth();
        HEIGHT = App.getResolutionSliderValueHeight();
        if (App.getFullscreenToggleValue()) {
            fullscreen = true;
        }
    }

    public void endScreen(GraphicsContext ctx) {;
        if (currentLap > maxLap) {
            String username = clientIDs.get(clientID);
            if (!finishedPlayers.contains(username)) {
                finishedPlayers.add(username);
            }
            gameFinished = true;
            double canvasWidth = WIDTH;
            double canvasHeight = HEIGHT;

            ctx.setFill(Color.RED);
            ctx.setFont(Font.font("Arial", FontWeight.BOLD, 60 * hudScale));
            ctx.fillText("RACE FINISHED", canvasWidth / 3.5, canvasHeight / 4);

            ctx.setFill(Color.RED);
            ctx.setFont(Font.font("Arial", FontWeight.BOLD, 20 * hudScale));
            ctx.fillText("Press Q to quit to the main menu", canvasWidth / 2.8, canvasHeight / 6);

            ctx.setFill(Color.WHITE);
            ctx.setFont(Font.font("Arial", FontWeight.BOLD, 20 * hudScale)); 

            for (int i = 0; i < finishedPlayers.size(); i++) {
                String playerLabel = (i + 1) + "# " + finishedPlayers.get(i);

                Text text = new Text(playerLabel);
                text.setFont(Font.font("Arial", FontWeight.BOLD, 20 * hudScale)); 

                double textWidth = text.getBoundsInLocal().getWidth();
                double textHeight = text.getBoundsInLocal().getHeight();

                ctx.fillText(playerLabel, canvasWidth / 3.5, canvasHeight / 3 + i * textHeight * 1.5);
            }
        }
    }

    public void updateHUD(GraphicsContext ctx) {
        ctx.setFill(Color.rgb(255, 0, 0, 0.3));
        ctx.fillRect(0, 0, WIDTH, (HEIGHT / 8));

        ctx.setFill(Color.BLACK);
        ctx.setFont(Font.font("Arial", FontWeight.BOLD, 20 * hudScale));
        ctx.fillText((int) speed / 100 + " Km/h", 0, 20 * hudScale);
        ctx.fillText("Last Lap: " + (String.format("%.2f", lastLapTime)) + " Sekunden", 0, 45 * hudScale);
        ctx.fillText(currentLap + "/4 Laps", 0, 70 * hudScale);

        double nitroHud = nitro / 100;
        double maxNitroHud = maxNitro / 100;

        double totalBlackBarWidth = (50 * 10) + 20;

        ctx.setFill(Color.BLACK);
        ctx.fillRect(300 * hudScale - 10 * hudScale, 25 * hudScale - 10 * hudScale, totalBlackBarWidth * hudScale,
                (40 + 20) * hudScale);

        ctx.setStroke(Color.BLACK);
        ctx.setLineWidth(5 * hudScale);
        ctx.strokeRect(300 * hudScale, 25 * hudScale, (50 * 10) * nitroHud * hudScale, 40 * hudScale);

        if (nitroRecharge) {
            ctx.setFill(Color.rgb(255, 0, 0));
        } else {
            ctx.setFill(Color.rgb(77, 187, 255));
        }
        ctx.fillRect(300 * hudScale, 25 * hudScale, (50 * 10) * nitroHud * hudScale, 40 * hudScale);

        ctx.setFill(Color.BLACK);
        ctx.setGlobalAlpha(0.5);
        ctx.fillRect(300 * hudScale, 25 * hudScale, (50 * 10) * maxNitroHud * hudScale, 40 * hudScale);
        ctx.setGlobalAlpha(1.0);

        if (nitroRecharge) {
            ctx.setFill(Color.rgb(255, 0, 0));
            ctx.drawImage(nitroBottleEmpty, (295 + 550) * hudScale, 30 * hudScale, (40 * 2.5) * hudScale,
                    (13 * 2.5) * hudScale);
        } else {
            ctx.setFill(Color.rgb(77, 187, 230));
            ctx.drawImage(nitroBottle, (295 + 550) * hudScale, 30 * hudScale, (40 * 2.5) * hudScale,
                    (13 * 2.5) * hudScale);
        }

        if (App.getOfflineMode()) {
            ctx.setFill(Color.RED);
            ctx.setFont(Font.font("Arial", FontWeight.BOLD, 40 * hudScale));
            ctx.fillText(place + ".", 0, 130 * hudScale);
        }
    }

    public static double getWindowWidth() {
        return WIDTH;
    }

    public static double getHudScale() {
        return hudScale;
    }

    public static void setHudScale(double hudScale) {
        Road.hudScale = hudScale;
    }

    public Road(boolean isOfflineMode, String clientID, Map<String, String> clientIDs, boolean isHost, String username, Socket socket, int playerNum) {
        this.isOfflineMode = isOfflineMode;
        this.clientID = clientID;
        this.clientIDs = clientIDs;
        this.isHost = isHost;
        this.username = username;
        this.socket = socket;

        socketIOEventHandler();
    }

    public void setClientIDs(Map<String, String> clientIDs) {
        this.clientIDs = clientIDs;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public void setHost(boolean isHost) {
        this.isHost = isHost;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setOfflineMode(boolean isOfflineMode) {
        this.isOfflineMode = isOfflineMode;
    }

}