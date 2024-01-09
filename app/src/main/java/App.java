
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class App extends Application {
    private Socket socket;
    private Label serverStatus;
    private Label connectedUsersLabel;
    private Label roadWidthLabel;
    private TextField usernameField;
    private TextField roadWidthOutput;
    private final int SCREEN_WIDTH = 1280;
    private final int SCREEN_HEIGHT = 960;
    private String username;
    private static Slider roadWidthSlider;
    private Button btnStart;
    private Button btnSettings;
    private Button btnQuit;
    private Button btnSave;
    private Scene gameScene;
    private Scene connectScene;
    private Scene settingsScene;
    private VBox connectBox;
    private Button connectbtn;
    private VBox buttonGameBox;
    private VBox playersConnectedBox;
    private boolean isConnected = false;

    private boolean connectionErrorHandled = false;
    private boolean settingsChanged = false;

    private static int selectedRoadWidth = 2000;
    private static int selectedCameraHeight = 1000;
    private static int selectedDrawDistance = 300;
    private static int selectedFieldOfView = 100;
    private static int selectedFogDensity = 5;
    private static String selectedLanes = "1 Lane";
    private static String selectedResolution = "High 1024x768";
    private static boolean isFullscreen = false;

    @Override
    public void start(Stage primaryStage) {

        createConnectSzene(primaryStage);
        createGameSzene(primaryStage);
        createSettingsScene(primaryStage);
        primaryStage.setTitle("Mulitplayer Game Racing");
        primaryStage.setScene(connectScene);
        primaryStage.show();

    }

    private void createConnectSzene(Stage primaryStage) {
        serverStatus = new Label();
        serverStatus.setStyle("-fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 18px;");
        serverStatus.setText("Verbinden Sie sich mit dem Server!");

        usernameField = new TextField();
        usernameField.setPromptText("Benutzername eingeben");
        usernameField.setMaxWidth(200);
        usernameField.setPrefColumnCount(10);

        connectbtn = new Button();
        connectbtn.setText("Verbindung herstellen");
        connectbtn.setOnAction(event -> {
            username = usernameField.getText();
            establishConnection(primaryStage);
        });

        connectBox = new VBox(10);
        connectBox.setAlignment(Pos.CENTER);
        connectBox.getChildren().add(usernameField);
        connectBox.getChildren().add(connectbtn);
        connectBox.getChildren().add(serverStatus);

        connectScene = new Scene(connectBox, SCREEN_WIDTH, SCREEN_HEIGHT);
        connectScene.getRoot().setStyle("-fx-background-color: blue;");

    }

    private void createGameSzene(Stage primaryStage) {
        connectedUsersLabel = new Label();
        connectedUsersLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-font-size: 18px;");

        btnStart = new Button();
        btnStart.setText("Start");

        btnStart.setOnAction(event -> {
            Road road = new Road();
            road.start(primaryStage);
        });

        btnSettings = new Button();
        btnSettings.setText("Settings");

        btnSettings.setOnAction(event -> switchScene(primaryStage, settingsScene));

        btnQuit = new Button();
        btnQuit.setText("Quit");

        btnQuit.setOnAction(event -> {
            Stage stage = (Stage) btnQuit.getScene().getWindow();
            stage.close();
        });

        btnStart.setPrefWidth(200);
        btnSettings.setPrefWidth(200);
        btnQuit.setPrefWidth(200);

        btnStart.setPrefHeight(40);
        btnSettings.setPrefHeight(40);
        btnQuit.setPrefHeight(40);

        buttonGameBox = new VBox(10); // Abstand
        playersConnectedBox = new VBox(10);
        buttonGameBox.setAlignment(Pos.CENTER); // Zentrieren der Buttons
        playersConnectedBox.setAlignment(Pos.TOP_LEFT);
        playersConnectedBox.getChildren().add(connectedUsersLabel);
        buttonGameBox.getChildren().addAll(btnStart, btnSettings, btnQuit); // Buttons in der Mitte hinzufügen

        StackPane root = new StackPane();
        root.getChildren().addAll(playersConnectedBox, buttonGameBox);

        gameScene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT);
        gameScene.getRoot().setStyle("-fx-background-color: blue;");
    }

    private void createSettingsScene(Stage primaryStage) {
        connectedUsersLabel = new Label();
        connectedUsersLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-font-size: 18px;");

        Label saveConfirmationLabel = new Label("");
        saveConfirmationLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-font-size: 14px;");

        // Road Width
        roadWidthLabel = new Label("Road Width:");
        roadWidthSlider = new Slider(500, 3000, 2000);
        roadWidthSlider.setBlockIncrement(1);
        roadWidthOutput = new TextField();
        roadWidthOutput.setEditable(false);
        roadWidthOutput.setPrefWidth(50);
        roadWidthSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> roadWidthOutput.setText(String.valueOf(newValue.intValue())));

        roadWidthLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
        roadWidthOutput.setStyle("-fx-background-color: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        // Camera Height
        Label cameraHeightLabel = new Label("Camera Height:");
        Slider cameraHeightSlider = new Slider(100, 500, 250); // Wertebereich für die Kamerahöhe
        cameraHeightSlider.setBlockIncrement(1);
        TextField cameraHeightOutput = new TextField();
        cameraHeightOutput.setEditable(false);
        cameraHeightOutput.setPrefWidth(50);
        cameraHeightSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> cameraHeightOutput.setText(String.valueOf(newValue.intValue())));

        // Draw Distance
        Label drawDistanceLabel = new Label("Draw Distance:");
        Slider drawDistanceSlider = new Slider(500, 2000, 1000); // Wertebereich für die Sichtweite
        drawDistanceSlider.setBlockIncrement(1);
        TextField drawDistanceOutput = new TextField();
        drawDistanceOutput.setEditable(false);
        drawDistanceOutput.setPrefWidth(50);
        drawDistanceSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> drawDistanceOutput.setText(String.valueOf(newValue.intValue())));

        // Field of View
        Label fieldOfViewLabel = new Label("Field of View:");
        Slider fieldOfViewSlider = new Slider(60, 120, 90); // Wertebereich für das Sichtfeld
        fieldOfViewSlider.setBlockIncrement(1);
        TextField fieldOfViewOutput = new TextField();
        fieldOfViewOutput.setEditable(false);
        fieldOfViewOutput.setPrefWidth(50);
        fieldOfViewSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> fieldOfViewOutput.setText(String.valueOf(newValue.intValue())));

        // Fog Density
        Label fogDensityLabel = new Label("Fog Density:");
        Slider fogDensitySlider = new Slider(0.1, 1.0, 0.5); // Wertebereich für die Nebeldichte
        fogDensitySlider.setBlockIncrement(0.1);
        TextField fogDensityOutput = new TextField();
        fogDensityOutput.setEditable(false);
        fogDensityOutput.setPrefWidth(50);
        fogDensitySlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> fogDensityOutput.setText(String.valueOf(newValue.doubleValue())));

        // Lanes Dropdown-Menü
        ComboBox<String> lanesDropdown = new ComboBox<>();
        lanesDropdown.getItems().addAll("1 Lane", "2 Lanes", "3 Lanes", "4 Lanes");
        lanesDropdown.setValue("1 Lane"); // Standardwert setzen

        // Resolution Dropdown-Menü
        ComboBox<String> resolutionDropdown = new ComboBox<>();
        resolutionDropdown.getItems().addAll("Low 480x360", "Medium 640x480", "High 1024x768", "Fine 1280x960");
        resolutionDropdown.setValue("High 1024x768"); // Standardwert setzen

        // Fullscreen Toggle-Schalter
        ToggleButton fullscreenToggle = new ToggleButton("Fullscreen");
        fullscreenToggle.setSelected(false); // Standardwert setzen

        // Save and Quit Buttons
        btnSave = new Button("Save");
        btnSave.setOnAction(event -> {
            settingsChanged = true;
            selectedRoadWidth = (int) roadWidthSlider.getValue();
            selectedCameraHeight = (int) cameraHeightSlider.getValue();
            selectedDrawDistance = (int) drawDistanceSlider.getValue();
            selectedFieldOfView = (int) fieldOfViewSlider.getValue();
            selectedFogDensity = (int) fogDensitySlider.getValue();
            selectedLanes = lanesDropdown.getValue();
            selectedResolution = resolutionDropdown.getValue();
            isFullscreen = fullscreenToggle.isSelected();

            System.out.println(getRoadWidthSliderValue());
            System.out.println(getCameraHeightSliderValue());
            System.out.println(getLanesSliderValue());
            System.out.println(getFullscreenToggleValue());
            System.out.println(getFogDensitySliderValue());
            System.out.println(getFieldOfViewSliderValue());
            System.out.println(getDrawDistanceSliderValue());
            System.out.println(getCameraHeightSliderValue());
            System.out.println(getResolutionSliderValueWidth());
            System.out.println(getResolutionSliderValueHeight());
            System.out.println(getFullscreenToggleValue());

            saveConfirmationLabel.setText("Einstellungen gespeichert!");
        });

        btnQuit = new Button("Quit");
        btnQuit.setOnAction(event -> {
            if (settingsChanged) {
                switchScene(primaryStage, gameScene);
            }
        });

        // VBox für Layout der Dropdowns und Toggle
        VBox settingsLayout = new VBox(10);
        settingsLayout.setAlignment(Pos.CENTER);
        settingsLayout.getChildren().addAll(
                roadWidthLabel, roadWidthSlider, roadWidthOutput,
                cameraHeightLabel, cameraHeightSlider, cameraHeightOutput,
                drawDistanceLabel, drawDistanceSlider, drawDistanceOutput,
                fieldOfViewLabel, fieldOfViewSlider, fieldOfViewOutput,
                fogDensityLabel, fogDensitySlider, fogDensityOutput,
                lanesDropdown, resolutionDropdown, fullscreenToggle, // Hinzufügen der neuen UI-Elemente
                btnSave, btnQuit, saveConfirmationLabel);

        StackPane root = new StackPane();
        root.getChildren().add(settingsLayout);

        settingsScene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT);
        settingsScene.getRoot().setStyle("-fx-background-color: blue;");
    }

    private void establishConnection(Stage primaryStage) {
        try {
            IO.Options options = IO.Options.builder()
                    .setExtraHeaders(Collections.singletonMap("username", Collections.singletonList(username)))
                    .build();

            socket = IO.socket("http://3.71.101.250:3000/", options);

            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Platform.runLater(() -> {
                        serverStatus.setText("Verbunden mit dem Server");
                        isConnected = true;
                        switchScene(primaryStage, gameScene);
                        System.out.println("Verbunden mit dem Server");
                    });
                }
            });

            socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    if (!connectionErrorHandled) {
                        Platform.runLater(() -> {
                            serverStatus.setText("Verbindungsfehler: " + args[0]);
                            setOfflineMode(primaryStage);
                        });
                        connectionErrorHandled = true;
                    }
                }
            });

            socket.on("playersConnected", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Platform.runLater(() -> onPlayersConnected(args));
                }
            });

            socket.connect();
        } catch (URISyntaxException e) {
            // Fehler beim Parsen der URI
            e.printStackTrace();
            Platform.runLater(() -> {
                serverStatus.setText("Ungültige Serveradresse oder Port");
            });
        } catch (Exception e) {
            // Allgemeiner Fehler
            e.printStackTrace();
            Platform.runLater(() -> serverStatus.setText("Verbindungsfehler: " + e.getMessage()));
        }
    }

    private void onPlayersConnected(Object... args) {
        if (args.length > 0 && args[0] instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) args[0];
            if (jsonObject.has("usernames")) {
                try {
                    JSONArray usernamesArray = jsonObject.getJSONArray("usernames");

                    StringBuilder usersStringBuilder = new StringBuilder();
                    for (int i = 0; i < usernamesArray.length(); i++) {
                        String username = usernamesArray.getString(i);
                        usersStringBuilder.append(username).append(" Online").append("\n");
                    }

                    Platform.runLater(() -> {
                        connectedUsersLabel.setText(usersStringBuilder.toString());
                        connectedUsersLabel.setVisible(true);
                        connectedUsersLabel.getParent().requestLayout(); // Fordert ein erneutes Layout an
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setOfflineMode(Stage primaryStage) {
        isConnected = false;
        serverStatus.setText("Offline-Modus: Keine Verbindung zum Server");
        switchScene(primaryStage, gameScene);
    }

    private void switchScene(Stage stage, Scene scene) {
        Platform.runLater(() -> {
            stage.setScene(scene);
            stage.show();
        });
        System.out.println("Szene gewechselt");
    }

    public static int getRoadWidthSliderValue() {
        return selectedRoadWidth;
    }

    public static int getLanesSliderValue() {
        int firstNumber = Character.getNumericValue(selectedLanes.charAt(0));
        return firstNumber;
    }

    public static int getCameraHeightSliderValue() {
        return selectedCameraHeight;
    }

    public static int getDrawDistanceSliderValue() {
        return selectedDrawDistance;
    }

    public static int getFieldOfViewSliderValue() {
        return selectedFieldOfView;
    }

    public static int getFogDensitySliderValue() {
        return selectedFogDensity;
    }

    public static int getResolutionSliderValueWidth() {
        String selectedResolution = App.selectedResolution;
        int separatorIndex = selectedResolution.indexOf("x");
        
        int widthStartIndex = separatorIndex - 4;
        
        if (Character.isWhitespace(selectedResolution.charAt(widthStartIndex))) {
            widthStartIndex++; 
        }
        
        String widthStr = selectedResolution.substring(widthStartIndex, separatorIndex); 
        int width = Integer.parseInt(widthStr); 
        return width;
    }

    public static int getResolutionSliderValueHeight() {
        int separatorIndex = selectedResolution.indexOf("x");
        String heightStr = selectedResolution.substring(separatorIndex + 1);
        int height = Integer.parseInt(heightStr);
        return height;
    }

    public static boolean getFullscreenToggleValue() {
        return isFullscreen;
    }



    public static void main(String[] args) {
        launch(args);
    }
}
