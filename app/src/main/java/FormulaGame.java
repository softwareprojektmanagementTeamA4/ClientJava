import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.Line;
import javax.swing.GroupLayout.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;

public class FormulaGame {
    private final int WINDOW_WIDTH = 1280;
    private final int WINDOW_HEIGHT = 960;
    private final int show_N_seg = 300;
    private final int segL = 200;
    private final Color light_grass = Color.rgb(16, 200, 16);
    private final Color dark_grass = Color.rgb(0, 154, 0);
    private final Color white_rumble = Color.rgb(255, 255, 255);
    private final Color black_rumble = Color.rgb(0, 0, 0);
    private final Color dark_road = Color.rgb(105, 105, 105);
    private final Color light_road = Color.rgb(150, 150, 150);

    private Image background;
    private Canvas canvas;
    private GraphicsContext gc;
    private Image backgroundRepeatable;
    private List<Line> lines;

    private double playerX = 0; // Initialisieren Sie den Spieler-X-Wert
    private double playerY = 0; // Initialisieren Sie den Spieler-Y-Wert
    private double pos = 0; // Initialisieren Sie die Position
    private int NumberOfLines; // Initialisieren Sie die Anzahl der Linien
    private double x = 0; // Initialisieren Sie x
    private double dx = 0; // Initialisieren Sie dx
    private double camH = 0; // Initialisieren Sie die Kamera-Höhe
    private double maxy = 0; // Initialisieren Sie maxy

    private Rectangle backgroundRect; // Definieren Sie das Hintergrund-Rechteck
    private GraphicsContext windowSurface; // Definieren Sie die Fensteroberfläche

    private final int UP_ARROW = 0;
    private final int DOWN_ARROW = 1;
    private final int RIGHT_ARROW = 2;
    private final int LEFT_ARROW = 3;
    private final int W_KEY = 4;
    private final int S_KEY = 5;
    private final int TAB_KEY = 6;

    private double speed = 0;
    private boolean[] keys = new boolean[7];
    private int startPos = 0;

    public void initialize(Stage primaryStage) {
        // Initialisieren Sie die Game-Elemente hier
        canvas = new Canvas(WINDOW_WIDTH, WINDOW_HEIGHT);
        gc = canvas.getGraphicsContext2D();
        lines = new ArrayList<>();
        background = new Image("file:src/media/backgroundRepeatable.png");

        createRoadLines();
        createBackground();

        Scene scene = new Scene(new Group(canvas));
        scene.setOnKeyPressed(event -> {
            KeyCode keyCode = event.getCode();
            handleKeyPress(keyCode);
        });

        primaryStage.setTitle("Pseudo 3D Road");
        primaryStage.setScene(new Scene(new Group(canvas)));
        primaryStage.show();

        startGameLoop();
    }

    private void createRoadLines() {
        lines = new ArrayList<>();
        for (int i = 0; i < 1600; i++) {
            Line line = new Line(i);
            line.z = i * segL + 0.00001;

            Color grassColor = ((i / 3) % 2 == 0) ? light_grass : dark_grass;
            Color rumbleColor = ((i / 3) % 2 == 0) ? white_rumble : black_rumble;
            Color roadColor = ((i / 3) % 2 == 0) ? light_road : dark_road;

            line.grassColor = grassColor;
            line.rumbleColor = rumbleColor;
            line.roadColor = roadColor;

            if (300 < i && i < 700) {
                line.curve = 0.5;
            }
            if (i > 750) {
                line.y = Math.sin(i / 30.0) * 1500;
            }
            if (i > 1100) {
                line.curve = -0.7;
            }

            lines.add(line);
        }
        NumberOfLines = lines.size(); // Setzen Sie die Anzahl der Linien entsprechend der erstellten Linien
    }

    private void createBackground() {
        Image backgroundImage = new Image("file:src/media/backgroundRepeatable.png");
    
        double windowWidth = WINDOW_WIDTH;
        double windowHeight = WINDOW_HEIGHT;
    
        double imageWidth = backgroundImage.getWidth();
        double imageHeight = backgroundImage.getHeight();
    
        double scaleWidth = windowWidth / 3.0 / imageWidth;
        double scaleHeight = windowHeight / imageHeight;
    
        Image scaledBackground = new Image("file:src/media/backgroundRepeatable.png", scaleWidth, scaleHeight, false, false);
    
        int repeatCount = 3;
        int totalWidth = (int) (imageWidth * repeatCount);
    
        WritableImage backgroundSurface = new WritableImage(totalWidth, (int) imageHeight);
        PixelWriter pixelWriter = backgroundSurface.getPixelWriter();
        PixelReader pixelReader = scaledBackground.getPixelReader();
    
        for (int i = 0; i < repeatCount; i++) {
            for (int x = 0; x < imageWidth; x++) {
                for (int y = 0; y < imageHeight; y++) {
                    Color color = pixelReader.getColor(x, y);
                    pixelWriter.setColor(x + (i * (int) imageWidth), y, color);
                }
            }
        }
    
        gc.drawImage(backgroundSurface, -imageWidth, 0);
    }

    private void startGameLoop() {
        new AnimationTimer() {
            long lastTime = System.nanoTime();
            double elapsedTime;

            @Override
            public void handle(long now) {
                elapsedTime = (now - lastTime) / 1000000000.0;
                lastTime = now;

                update(elapsedTime);
                render();
            }
        }.start();
    }

    private void update(double elapsedTime) {
        double speed = 0;
        boolean[] keys = checkKeys(); // Überprüfen Sie die Tasteneingaben (entsprechend der Logik des Python-Codes)

        if (keys[UP_ARROW]) {
            speed += segL; // Muss ein Vielfaches der Segmentlänge sein
        }
        if (keys[DOWN_ARROW]) {
            speed -= segL; // Muss ein Vielfaches der Segmentlänge sein
        }
        if (keys[RIGHT_ARROW]) {
            playerX += 200;
        }
        if (keys[LEFT_ARROW]) {
            playerX -= 200;
        }
        if (keys[W_KEY]) {
            playerY += 100;
        }
        if (keys[S_KEY]) {
            playerY -= 100;
        }
        if (playerY < 500) {
            playerY = 500;
        }
        if (keys[TAB_KEY]) {
            speed *= 2; // Muss ein Vielfaches der Segmentlänge sein
        }

        pos += speed;

        while (pos >= NumberOfLines * segL) {
            pos -= NumberOfLines * segL;
        }
        while (pos < 0) {
            pos += NumberOfLines * segL;
        }
        int startPos = (int) (pos / segL);

        double x = 0, dx = 0;

        double camH = lines.get(startPos).y + playerY;
        double maxy = WINDOW_HEIGHT;

        if (speed > 0) {
            backgroundRect.x -= lines.get(startPos).curve * 2;
        } else if (speed < 0) {
            backgroundRect.x += lines.get(startPos).curve * 2;
        }

        if (backgroundRect.getRight() < WINDOW_WIDTH) {
            backgroundRect.x = -WINDOW_WIDTH;
        } else if (backgroundRect.getLeft() > 0) {
            backgroundRect.x = -WINDOW_WIDTH;
        }

        drawBackground();
        drawRoad(startPos, maxy, x, dx, camH);
    }

    private void render() {
        windowSurface.setFill(Color.rgb(135, 206, 235)); // Hintergrundfarbe setzen
    
        // Eventuell vorhandene Hintergrundbilder oder -elemente rendern
        // Verwenden Sie GraphicsContext, um das Hintergrundbild zu rendern
        drawBackground();
    
        // Straßen-Rendering
        for (int n = startPos; n < startPos + show_N_seg; n++) {
            Line current = lines.get(n % NumberOfLines);
            current.project(playerX - x, camH, pos - (NumberOfLines * segL * (n >= NumberOfLines ? 1 : 0)));
    
            x += dx;
            dx += current.curve;
    
            if (current.Y >= maxy) {
                continue;
            }
            maxy = current.Y;
    
            Line prev = lines.get((n - 1 + NumberOfLines) % NumberOfLines);
    
            drawQuad(current.grassColor, 0, prev.Y, WINDOW_WIDTH, 0, current.Y, WINDOW_WIDTH);
            drawQuad(current.rumbleColor, prev.X, prev.Y, prev.W * 1.2, current.X, current.Y, current.W * 1.2);
            drawQuad(current.roadColor, prev.X, prev.Y, prev.W, current.X, current.Y, current.W);
        }
    
        // Fenster aktualisieren
        gc.drawImage(windowSurface, 0, 0);
        primaryStage.show();
    }

    private void drawBackground() {
        // Stellen Sie sicher, dass backgroundRepeatable initialisiert ist und das Hintergrundbild darstellt
        // Nehmen wir an, das Hintergrundbild soll horizontal wiederholt werden
        int repeatCount = 3; // Anzahl der Wiederholungen
    
        // Breite und Höhe des Hintergrundbilds
        double imageWidth = backgroundRepeatable.getWidth();
        double imageHeight = backgroundRepeatable.getHeight();
    
        for (int i = 0; i < repeatCount; i++) {
            // Zeichnen des Hintergrundbilds wiederholt, um die Wiederholung zu erzeugen
            gc.drawImage(backgroundRepeatable, i * imageWidth, 0);
        }
    }

    private boolean[] checkKeys() {
        boolean[] keys = new boolean[7];
    
        keys[UP_ARROW] = checkIfUpArrowPressed(); // Methode, die den Status der UP_ARROW-Taste überprüft
        keys[DOWN_ARROW] = checkIfDownArrowPressed(); // Methode, die den Status der DOWN_ARROW-Taste überprüft
        keys[RIGHT_ARROW] = checkIfRightArrowPressed(); // Methode, die den Status der RIGHT_ARROW-Taste überprüft
        keys[LEFT_ARROW] = checkIfLeftArrowPressed(); // Methode, die den Status der LEFT_ARROW-Taste überprüft
        keys[W_KEY] = checkIfWKeyPressed(); // Methode, die den Status der W-Taste überprüft
        keys[S_KEY] = checkIfSKeyPressed(); // Methode, die den Status der S-Taste überprüft
        keys[TAB_KEY] = checkIfTabKeyPressed(); // Methode, die den Status der TAB-Taste überprüft
    
        return keys;
    }

    private void drawRoad(int startPos, double maxy, double x, double dx, double camH) {
        int show_N_seg = 300; // Anzahl der anzuzeigenden Segmente
        for (int n = startPos; n < startPos + show_N_seg; n++) {
            Line current = lines.get(n % lines.size());
            current.project(playerX - x, camH, pos - (lines.size() * segL * ((n >= lines.size()) ? 1 : 0)));
    
            x += dx;
            dx += current.curve;
    
            if (current.Y >= maxy) {
                continue;
            }
            maxy = current.Y;
    
            Line prev = lines.get((n - 1 + lines.size()) % lines.size());
    
            drawQuad(current.grassColor, 0, prev.Y, WINDOW_WIDTH, 0, current.Y, WINDOW_WIDTH, 4);
            drawQuad(current.rumbleColor, prev.X, prev.Y, prev.W * 1.2, current.X, current.Y, current.W * 1.2, 4);
            drawQuad(current.roadColor, prev.X, prev.Y, prev.W, current.X, current.Y, current.W, 4);
        }
    }
    
    private void drawQuad(Color color, double x1, double y1, double w1, double x2, double y2, double w2, int points) {
        gc.setFill(color);
        gc.fillPolygon(new double[]{x1 - w1, x2 - w2, x2 + w2, x1 + w1}, new double[]{y1, y2, y2, y1}, points);
    }
    
    private void drawQuad(Color color, int x1, int y1, int w1, int x2, int y2, int w2) {
        gc.setFill(color);
        gc.fillPolygon(new double[]{x1 - w1, x2 - w2, x2 + w2, x1 + w1}, new double[]{y1, y2, y2, y1}, 4);
    }

    private void handleKeyPress(KeyCode keyCode) {
        switch (keyCode) {
            case UP:
                // Logik für Taste "UP"
                speed += segL; // Muss ein Vielfaches der Segmentlänge sein
                break;
            case DOWN:
                // Logik für Taste "DOWN"
                speed -= segL; // Muss ein Vielfaches der Segmentlänge sein
                break;
            case LEFT:
                // Logik für Taste "LEFT"
                playerX -= 200;
                break;
            case RIGHT:
                // Logik für Taste "RIGHT"
                playerX += 200;
                break;
            case W:
                // Logik für Taste "W"
                playerY += 100;
                break;
            case S:
                // Logik für Taste "S"
                playerY -= 100;
                if (playerY < 500) {
                    playerY = 500;
                }
                break;
            case TAB:
                // Logik für Taste "TAB"
                speed *= 2; // Muss ein Vielfaches der Segmentlänge sein
                break;
        }
    }
    
    

    private boolean checkIfDownArrowPressed() {
        Scene scene = primaryStage.getScene();
        return scene.isKeyPressed(KeyCode.DOWN);
    }
    
    private boolean checkIfRightArrowPressed() {
        Scene scene = primaryStage.getScene();
        return scene.isKeyPressed(KeyCode.RIGHT);
    }
    
    private boolean checkIfLeftArrowPressed() {
        Scene scene = primaryStage.getScene();
        return scene.isKeyPressed(KeyCode.LEFT);
    }
    
    private boolean checkIfWKeyPressed() {
        Scene scene = primaryStage.getScene();
        return scene.isKeyPressed(KeyCode.W);
    }
    
    private boolean checkIfSKeyPressed() {
        Scene scene = primaryStage.getScene();
        return scene.isKeyPressed(KeyCode.S);
    }
    
    private boolean checkIfTabKeyPressed() {
        Scene scene = primaryStage.getScene();
        return scene.isKeyPressed(KeyCode.TAB);
    }
    private boolean checkIfUpArrowPressed() {
        Scene scene = primaryStage.getScene();
        return scene.isKeyPressed(KeyCode.UP);
    }
}
