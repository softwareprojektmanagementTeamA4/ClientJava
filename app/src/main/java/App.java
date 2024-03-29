import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;

import java.awt.Dimension;
import java.awt.Toolkit;

public class App extends Application {
    private Socket socket;
    private Label serverStatus;
    private Label connectedUsersLabel;
    private Label roadWidthLabel;
    private Label offlineModeLabel = new Label();
    private Label connectionStatusLabel;
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
    private static Scene gameScene;
    private Scene connectScene;
    private Scene settingsScene;
    private VBox connectBox;
    private Button connectbtn;
    private VBox buttonGameBox;
    private VBox playersConnectedBox;
    private static boolean isConnected = false;

    private boolean connectionErrorHandled = false;
    private boolean settingsChanged = false;

    private static int selectedRoadWidth = 2000;
    private static int selectedCameraHeight = 1000;
    private static int selectedDrawDistance = 300;
    private static int selectedFieldOfView = 100;
    private static int selectedFogDensity = 5;
    private static String selectedLanes = "3 Lanes";
    private static String selectedResolution = "High 1024x768";
    private static boolean isFullscreen = false;
    private boolean first_Build = false;

    private String clientID;
    private String hostID;
    private Map<String, String> clientdIDs = new HashMap<String, String>();
    private boolean isHost = false;
    private boolean playerReady = false;
    private boolean gameStart = false;
    private boolean canStart = false;
    private boolean gameStart2 = false;

    private Road road;

    /**
     * Launches the application and initializes the various scenes.
     *
     * @param primaryStage The main stage of the application.
     */
    @Override
    public void start(Stage primaryStage) {

        createConnectSzene(primaryStage);
        createGameSzene(primaryStage);
        createSettingsScene(primaryStage);
        primaryStage.setTitle("RaceGame");
        primaryStage.setScene(connectScene);
        primaryStage.show();

    }

    /**
     * Creates the connection scene with UI elements such as labels, text fields,
     * and buttons.
     *
     * @param primaryStage The main stage of the application.
     */
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
        connectbtn.setStyle(
                "-fx-background-color: grey; -fx-border-color: black; -fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 14px; -fx-border-width: 3px;");

        connectBox = new VBox(10);
        connectBox.setAlignment(Pos.CENTER);
        connectBox.getChildren().add(usernameField);
        connectBox.getChildren().add(connectbtn);
        connectBox.getChildren().add(serverStatus);

        Image backgroundImage = new Image("file:src/main/java/images/homescreen.jpg");
        ImageView backgroundImageView = new ImageView(backgroundImage);

        backgroundImageView.setFitWidth(SCREEN_WIDTH);
        backgroundImageView.setFitHeight(SCREEN_HEIGHT);

        Image logoImage = new Image("file:src/main/java/images/gametitle.png");
        ImageView logoImageView = new ImageView(logoImage);

        logoImageView.setFitWidth(SCREEN_WIDTH);
        logoImageView.setFitHeight(SCREEN_HEIGHT / 3);
        double logoYOffset = -(SCREEN_HEIGHT / 3);
        logoImageView.setTranslateY(logoYOffset);
        double logoXOffset = -(SCREEN_WIDTH / 15);
        logoImageView.setTranslateX(-logoXOffset);

        StackPane root = new StackPane();
        root.getChildren().add(backgroundImageView);
        root.getChildren().add(logoImageView);
        root.getChildren().add(connectBox);

        connectScene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT);
    }

    /**
     * Creates the game scene with UI elements such as labels, buttons, and displays
     * for connected players.
     *
     * @param primaryStage The main stage of the application.
     */
    private void createGameSzene(Stage primaryStage) {
        connectedUsersLabel = new Label();
        connectedUsersLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-font-size: 18px;");

        btnStart = new Button();
        setStartButton();
        btnStart.setOnAction(event -> {
            if (canStart || clientdIDs.size() <= 1) {
                if (road == null) {
                    road = new Road(!isConnected, clientID, clientdIDs, isHost, username, socket, 1);
                    gameStart = false;
                    canStart = false;
                    playerReady = false;
                    socket.emit("player_ready", playerReady);
                } else {
                    road.start(primaryStage);
                    road.setOfflineMode(!isConnected);
                    road.setClientID(clientID);
                    road.setClientIDs(clientdIDs);
                    road.setHost(isHost);
                    road.setUsername(username);
                    road.setSocket(socket);
                }
                road.start(primaryStage);
            } else if (!playerReady && !isHost) {
                playerReady = true;
                socket.emit("player_ready", playerReady);
            } else if (playerReady && !isHost) {
                playerReady = false;
                socket.emit("player_ready", playerReady);

            }
            setStartButton();
        });

        btnSettings = new Button();
        btnSettings.setText("Settings");
        btnSettings.setStyle(
                "-fx-background-color: grey; -fx-border-color: black; -fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 14px; -fx-border-width: 3px;");

        btnSettings.setOnAction(event -> switchScene(primaryStage, settingsScene));

        btnQuit = new Button();
        btnQuit.setText("Quit");
        btnQuit.setStyle(
                "-fx-background-color: grey; -fx-border-color: black; -fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 14px; -fx-border-width: 3px;");

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

        buttonGameBox = new VBox(10);
        playersConnectedBox = new VBox(10);
        buttonGameBox.setAlignment(Pos.CENTER);
        playersConnectedBox.setAlignment(Pos.TOP_LEFT);
        playersConnectedBox.getChildren().add(connectedUsersLabel);
        playersConnectedBox.getChildren().add(offlineModeLabel);
        buttonGameBox.getChildren().addAll(btnStart, btnSettings, btnQuit);

        connectionStatusLabel = new Label();
        connectionStatusLabel.setStyle("-fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 18px;");

        Button reconnectButton = new Button("Reconnect");
        reconnectButton.setPrefWidth(200);
        reconnectButton.setPrefHeight(40);
        reconnectButton.setStyle(
                "-fx-background-color: grey; -fx-border-color: black; -fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 14px; -fx-border-width: 3px;");
        reconnectButton.setOnAction(event -> {
            connectionStatusLabel.setText("Reconnecting...");
            connectionStatusLabel.setTextFill(Color.BLACK);
            connectionStatusLabel.setVisible(true);
            connectionErrorHandled = false;
            establishConnection(primaryStage);
        });

        VBox reconnectBox = new VBox(10);
        reconnectBox.setAlignment(Pos.CENTER);
        reconnectBox.getChildren().addAll(reconnectButton, connectionStatusLabel);

        buttonGameBox.getChildren().add(reconnectBox);

        Image backgroundImage = new Image("file:src/main/java/images/homescreen.jpg");
        ImageView backgroundImageView = new ImageView(backgroundImage);

        backgroundImageView.setFitWidth(SCREEN_WIDTH);
        backgroundImageView.setFitHeight(SCREEN_HEIGHT);

        Image logoImage = new Image("file:src/main/java/images/gametitle.png");
        ImageView logoImageView = new ImageView(logoImage);

        logoImageView.setFitWidth(SCREEN_WIDTH);
        logoImageView.setFitHeight(SCREEN_HEIGHT / 3);
        double logoYOffset = -(SCREEN_HEIGHT / 3);
        logoImageView.setTranslateY(logoYOffset);
        double logoXOffset = -(SCREEN_WIDTH / 15);
        logoImageView.setTranslateX(-logoXOffset);

        StackPane root = new StackPane();
        root.getChildren().add(backgroundImageView);
        root.getChildren().add(logoImageView);
        root.getChildren().addAll(playersConnectedBox, buttonGameBox);

        gameScene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT);
    }

    /**
     * Creates the settings scene with UI elements such as sliders, buttons, and
     * dropdown menus.
     *
     * @param primaryStage The main stage of the application.
     */
    private void createSettingsScene(Stage primaryStage) {

        Label saveConfirmationLabel = new Label("");
        saveConfirmationLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-font-size: 14px;");

        // Road Width
        roadWidthLabel = new Label("Road Width");
        roadWidthLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: grey;");
        roadWidthSlider = new Slider(500, 3000, 2000);
        roadWidthSlider.setStyle("-fx-base: #000000; -fx-scale-y: 2.0; -fx-scale-x: 2.0;");
        roadWidthSlider.setMaxWidth(150);
        roadWidthSlider.setBlockIncrement(1);
        roadWidthOutput = new TextField("2000");
        roadWidthOutput.setEditable(false);
        roadWidthOutput.setMaxWidth(50);
        roadWidthOutput.setMaxHeight(10);
        roadWidthOutput.setStyle(
                "-fx-background-color: grey; -fx-font-size: 10px; -fx-font-weight: bold; -fx-border-color: black; -fx-border-width: 3px;");
        roadWidthSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> roadWidthOutput.setText(String.valueOf(newValue.intValue())));

        // Camera Height
        Label cameraHeightLabel = new Label("Camera Height");
        cameraHeightLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: grey;");
        Slider cameraHeightSlider = new Slider(500, 5000, 1000);
        cameraHeightSlider.setMaxWidth(150);
        cameraHeightSlider.setStyle("-fx-base: #000000; -fx-scale-y: 2.0; -fx-scale-x: 2.0;");
        cameraHeightSlider.setBlockIncrement(1);
        TextField cameraHeightOutput = new TextField("1000");
        cameraHeightOutput.setEditable(false);
        cameraHeightOutput.setMaxWidth(50);
        cameraHeightOutput.setMaxHeight(10);
        cameraHeightOutput.setStyle(
                "-fx-background-color: grey; -fx-font-size: 10px; -fx-font-weight: bold; -fx-border-color: black; -fx-border-width: 3px;");
        cameraHeightSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> cameraHeightOutput.setText(String.valueOf(newValue.intValue())));

        // Draw Distance
        Label drawDistanceLabel = new Label("Draw Distance");
        drawDistanceLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: grey;");
        Slider drawDistanceSlider = new Slider(100, 500, 300);
        drawDistanceSlider.setMaxWidth(150);
        drawDistanceSlider.setStyle("-fx-base: #000000; -fx-scale-y: 2.0; -fx-scale-x: 2.0;");
        drawDistanceSlider.setBlockIncrement(1);
        TextField drawDistanceOutput = new TextField("300");
        drawDistanceOutput.setEditable(false);
        drawDistanceOutput.setMaxWidth(50);
        drawDistanceOutput.setMaxHeight(10);
        drawDistanceOutput.setStyle(
                "-fx-background-color: grey; -fx-font-size: 10px; -fx-font-weight: bold; -fx-border-color: black; -fx-border-width: 3px;");
        drawDistanceSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> drawDistanceOutput.setText(String.valueOf(newValue.intValue())));

        // Field of View
        Label fieldOfViewLabel = new Label("Field of View");
        fieldOfViewLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: grey;");
        Slider fieldOfViewSlider = new Slider(80, 140, 100);
        fieldOfViewSlider.setMaxWidth(150);
        fieldOfViewSlider.setStyle("-fx-base: #000000; -fx-scale-y: 2.0; -fx-scale-x: 2.0;");
        TextField fieldOfViewOutput = new TextField("100");
        fieldOfViewOutput.setEditable(false);
        fieldOfViewOutput.setMaxWidth(50);
        fieldOfViewOutput.setMaxHeight(10);
        fieldOfViewOutput.setStyle(
                "-fx-background-color: grey; -fx-font-size: 10px; -fx-font-weight: bold; -fx-border-color: black; -fx-border-width: 3px;");
        fieldOfViewSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> fieldOfViewOutput.setText(String.valueOf(newValue.intValue())));

        // Fog Density
        Label fogDensityLabel = new Label("Fog Density");
        fogDensityLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: grey;");
        Slider fogDensitySlider = new Slider(0, 50, 5);
        fogDensitySlider.setMaxWidth(150);
        fogDensitySlider.setStyle("-fx-base: #000000; -fx-scale-y: 2.0; -fx-scale-x: 2.0;");
        fogDensitySlider.setBlockIncrement(1);
        TextField fogDensityOutput = new TextField("5");
        fogDensityOutput.setEditable(false);
        fogDensityOutput.setMaxWidth(50);
        fogDensityOutput.setMaxHeight(10);
        fogDensityOutput.setStyle(
                "-fx-background-color: grey; -fx-font-size: 10px; -fx-font-weight: bold; -fx-border-color: black; -fx-border-width: 3px;");
        fogDensitySlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> fogDensityOutput.setText(String.valueOf(newValue.intValue())));

        // Lanes Dropdown-Menu
        ComboBox<String> lanesDropdown = new ComboBox<>();
        lanesDropdown.getItems().addAll("1 Lane", "2 Lanes", "3 Lanes", "4 Lanes");
        lanesDropdown.setValue("3 Lanes");
        lanesDropdown.setStyle("-fx-background-color: grey; -fx-font-size: 18px; -fx-scaley: 2.0; -fx-scalex: 2.0;");
        lanesDropdown.setMaxHeight(400);

        // Resolution Dropdown-Menu
        ComboBox<String> resolutionDropdown = new ComboBox<>();
        resolutionDropdown.getItems().addAll("Low 480x360", "Medium 640x480", "High 1024x768", "Fine 1280x960");
        resolutionDropdown.setValue("High 1024x768");
        resolutionDropdown
                .setStyle("-fx-background-color: grey; -fx-font-size: 18px; -fx-scaley: 2.0; -fx-scalex: 2.0;");
        resolutionDropdown.setMaxHeight(400);

        resolutionDropdown.setOnAction(event -> {
            if (resolutionDropdown.getValue() != "High 1024x768") {
                double hudScale = 1.0;
                switch (resolutionDropdown.getValue()) {
                    case "Low 480x360":
                        hudScale = 480 / Road.getWindowWidth();
                        break;

                    case "Medium 640x480":
                        hudScale = 640 / Road.getWindowWidth();
                        break;

                    case "Fine 1280x960":
                        hudScale = 1280 / Road.getWindowWidth();
                        break;
                }
                Road.setHudScale(hudScale);
            }
        });

        CheckBox fullscreenCheckBox = new CheckBox("Fullscreen");
        fullscreenCheckBox.setSelected(false);
        fullscreenCheckBox
                .setStyle("-fx-background-color: grey; -fx-font-size: 18px; -fx-scaley: 2.0; -fx-scalex: 2.0;");
        fullscreenCheckBox.setMaxHeight(400);

        fullscreenCheckBox.setOnAction(event -> {
            if (fullscreenCheckBox.isSelected()) {
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                double screenWidth = screenSize.getWidth();
                double screenHeight = screenSize.getHeight();
                double aspectRatio = 4.0 / 3.0;
                double maxResolutionWidth = Math.min(screenWidth, screenHeight * aspectRatio);
                double maxResolutionHeight = maxResolutionWidth / aspectRatio;

                resolutionDropdown.setValue("Custom " + (int) maxResolutionWidth + "x" + (int) maxResolutionHeight);
                Road.setHudScale(maxResolutionWidth / Road.getWindowWidth());
                isFullscreen = true;
            } else {
                isFullscreen = false;
            }
        });

        // Save Button
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
            isFullscreen = fullscreenCheckBox.isSelected();

            saveConfirmationLabel.setText("Einstellungen gespeichert!");
            switchScene(primaryStage, gameScene);
        });
        btnSave.setStyle(
                "-fx-background-color: grey; -fx-border-color: black; -fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 30px; -fx-border-width: 3px;");
        btnSave.setPrefWidth(200);
        btnSave.setPrefHeight(50);
        VBox settingsLayout = new VBox(40);
        settingsLayout.setAlignment(Pos.CENTER_LEFT);

        VBox topRightBox = new VBox(10);
        topRightBox.getChildren().addAll(fullscreenCheckBox, resolutionDropdown, lanesDropdown);
        topRightBox.setAlignment(Pos.TOP_LEFT);

        HBox outputAndSliders = new HBox(10);
        outputAndSliders.setAlignment(Pos.CENTER);

        VBox labels = new VBox(35);
        labels.getChildren().addAll(
                roadWidthLabel, cameraHeightLabel, drawDistanceLabel,
                fieldOfViewLabel, fogDensityLabel);
        labels.setAlignment(Pos.CENTER_LEFT);
        labels.setPadding(new Insets(23, 0, 0, 0));

        VBox sliders = new VBox(10);
        sliders.getChildren().addAll(
                roadWidthOutput, roadWidthSlider,
                cameraHeightOutput, cameraHeightSlider,
                drawDistanceOutput, drawDistanceSlider,
                fieldOfViewOutput, fieldOfViewSlider,
                fogDensityOutput, fogDensitySlider);
        sliders.setAlignment(Pos.CENTER);

        outputAndSliders.getChildren().addAll(sliders);

        HBox lablesandSlider = new HBox(430);
        lablesandSlider.getChildren().addAll(labels, outputAndSliders);
        lablesandSlider.setAlignment(Pos.CENTER_LEFT);

        VBox saveConfirmationLayout = new VBox(10);
        saveConfirmationLayout.setAlignment(Pos.CENTER);
        saveConfirmationLayout.getChildren().addAll(btnSave, saveConfirmationLabel);

        settingsLayout.getChildren().addAll(topRightBox, lablesandSlider, saveConfirmationLayout);

        Image backgroundImage = new Image("file:src/main/java/images/homescreen.jpg");
        ImageView backgroundImageView = new ImageView(backgroundImage);

        backgroundImageView.setFitWidth(SCREEN_WIDTH);
        backgroundImageView.setFitHeight(SCREEN_HEIGHT);

        Image logoImage = new Image("file:src/main/java/images/gametitle.png");
        ImageView logoImageView = new ImageView(logoImage);

        logoImageView.setFitWidth(SCREEN_WIDTH);
        logoImageView.setFitHeight(SCREEN_HEIGHT / 3);
        double logoYOffset = -(SCREEN_HEIGHT / 3);
        logoImageView.setTranslateY(logoYOffset);
        double logoXOffset = -(SCREEN_WIDTH / 15);
        logoImageView.setTranslateX(-logoXOffset);

        StackPane root = new StackPane();
        root.getChildren().add(backgroundImageView);
        root.getChildren().add(logoImageView);
        root.getChildren().add(settingsLayout);

        settingsScene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT);
    }

    /**
     * Establishes a connection to the server and registers event handlers for
     * various events.
     *
     * @param primaryStage The main stage of the application.
     */
    private void establishConnection(Stage primaryStage) {
        try {
            IO.Options options = IO.Options.builder()
                    .setExtraHeaders(Collections.singletonMap("username", Collections.singletonList(username)))
                    .build();

            socket = IO.socket("http://35.246.239.15:3000/", options);

            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Platform.runLater(() -> {
                        if (primaryStage.getScene() == connectScene) {
                            serverStatus.setText("Verbunden mit dem Server");
                            isConnected = true;
                            switchScene(primaryStage, gameScene);
                        } else {
                            connectionStatusLabel.setText("Connected");
                            connectionStatusLabel.setTextFill(Color.GREEN);
                            connectionStatusLabel.setVisible(true);
                        }
                    });
                }
            });

            socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    if (!connectionErrorHandled) {
                        Platform.runLater(() -> {
                            if (primaryStage.getScene() == connectScene) {
                                setOfflineMode(primaryStage);
                                serverStatus.setText("Verbindungsfehler: " + args[0]);
                            } else {
                                connectionStatusLabel.setText("Connection Error");
                                connectionStatusLabel.setTextFill(Color.RED);
                                connectionStatusLabel.setVisible(true);
                            }
                        });
                        connectionErrorHandled = true;
                    }
                }
            });

            socket.on("playersConnected", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Platform.runLater(() -> {
                        try {
                            onPlayersConnected(args);
                        } catch (JSONException e) {

                            e.printStackTrace();
                        }
                    });
                }
            });

            socket.on("getHostID", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Platform.runLater(() -> createHost(args));
                }
            });

            socket.on("getPlayerID", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Platform.runLater(() -> getPlayerID(args));
                }
            });

            socket.on("start", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Platform.runLater(() -> setGameStart(primaryStage, args));
                }
            });

            socket.on("all_players_ready", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Platform.runLater(() -> setCanStart(args));
                }
            });

            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Platform.runLater(() -> {
            });
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
            });
        }

    }

    /**
     * Processes events when players are connected to the server and updates the
     * user interface accordingly.
     *
     * @param args The event arguments containing information about the connected
     *             players.
     * @throws JSONException In case of an error during JSON processing.
     */
    private void onPlayersConnected(Object... args) throws JSONException {
        if (args.length > 0 && args[0] instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) args[0];
            JSONArray usernamesArray = new JSONArray();
            ArrayList<String> keysList = new ArrayList<String>();

            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = jsonObject.get(key);
                usernamesArray.put(value);
                keysList.add(key);
                clientdIDs.put(key, (String) value);
            }
            setStartButton();

            try {
                StringBuilder usersStringBuilder = new StringBuilder();
                for (int i = 0; i < usernamesArray.length(); i++) {
                    String username = usernamesArray.getString(i);
                    String key = keysList.get(i);
                    if (key.equals(hostID)) {
                        usersStringBuilder.append(username).append(" Online").append(" Host").append("\n");
                    } else {
                        usersStringBuilder.append(username).append(" Online").append("\n");
                    }
                }

                Platform.runLater(() -> {
                    connectedUsersLabel.setText(usersStringBuilder.toString());
                    connectedUsersLabel.setVisible(true);
                    connectedUsersLabel.getParent().requestLayout();
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sets the host based on the received server information and updates the start
     * button accordingly.
     *
     * @param args The event arguments containing the host information.
     */
    public void createHost(Object... args) {
        hostID = args[0].toString();
        if (clientID.equals(hostID)) {
            isHost = true;
        }
        setStartButton();
    }

    /**
     * Sets the player ID based on the received server information.
     *
     * @param args The event arguments containing the player ID.
     */
    public void getPlayerID(Object... args) {
        clientID = args[0].toString();
    }

    /**
     * Sets the state of whether the game can be started based on the received
     * server information.
     *
     * @param args The event arguments containing the start status of the game.
     */
    public void setCanStart(Object... args) {
        canStart = args[0].toString().equals("true");
        setStartButton();
    }

    /**
     * Sets the game state when it is started and initializes the game scene.
     *
     * @param primaryStage The main stage of the application.
     * @param args         The event arguments containing the start status of the
     *                     game.
     */
    public void setGameStart(Stage primaryStage, Object... args) {
        gameStart = true;
        if (gameStart && !gameStart2) {
            road = new Road(!isConnected, clientID, clientdIDs, isHost, username, socket, 1);
            road.start(primaryStage);
            gameStart2 = true;
        }
    }

    private void setOfflineMode(Stage primaryStage) {
        isConnected = false;
        isHost = true;
        serverStatus.setText("Offline-Modus: Keine Verbindung zum Server");
        offlineModeLabel.setText("Offlinemode");
        offlineModeLabel.setVisible(true);
        offlineModeLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 18px;");

        switchScene(primaryStage, gameScene);
    }

    /**
     * Sets the state and appearance of the start button based on various
     * conditions.
     * - If not connected or the host, the button is red with the label "Start" or
     * "Not Ready" (depending on player readiness).
     * - If the game can be started or there is only one player, the button is
     * green.
     * - If the player is ready, the button is green with the label "Ready".
     */
    private void setStartButton() {
        if (!isConnected || isHost) {
            btnStart.setText("Start");
            btnStart.setStyle("-fx-background-color: red; -fx-border-color: black; -fx-text-fill: black; " +
                    "-fx-font-weight: bold; -fx-font-size: 14px; -fx-border-width: 3px;");
            if (canStart || clientdIDs.size() <= 1) {
                btnStart.setStyle("-fx-background-color: green; -fx-border-color: black; -fx-text-fill: black; " +
                        "-fx-font-weight: bold; -fx-font-size: 14px; -fx-border-width: 3px;");
            }
        } else if (!playerReady) {
            btnStart.setText("Not Ready");
            btnStart.setStyle("-fx-background-color: red; -fx-border-color: black; -fx-text-fill: black; " +
                    "-fx-font-weight: bold; -fx-font-size: 14px; -fx-border-width: 3px;");
        } else {
            btnStart.setText("Ready");
            btnStart.setStyle("-fx-background-color: green; -fx-border-color: black; -fx-text-fill: black; " +
                    "-fx-font-weight: bold; -fx-font-size: 14px; -fx-border-width: 3px;");
        }
    }

    /**
     * Switches the scene on the JavaFX platform runtime.
     * - This method is executed on the JavaFX Application Thread to ensure
     * that changes to the scene are made on the correct thread.
     * - The method updates the scene of the given stage with the new scene and
     * shows the stage.
     * - Prints "Scene switched" to the console to indicate a successful scene
     * change.
     *
     * @param stage The stage whose scene is to be changed.
     * @param scene The new scene to be displayed on the stage.
     */
    public static void switchScene(Stage stage, Scene scene) {
        Platform.runLater(() -> {
            stage.setScene(scene);
            stage.show();
        });
    }

    public static Scene getGameScene() {
        return gameScene;
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

    public static boolean getOfflineMode() {
        return isConnected;
    }
}