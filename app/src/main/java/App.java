
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
    private TextField usernameField;
    private final int SCREEN_WIDTH = 1280;
    private final int SCREEN_HEIGHT = 960;
    private String username;
    private Button btnStart;
    private Button btnSettings;
    private Button btnQuit;
    private Scene gameScene;
    private Scene connectScene;
    private VBox connectBox;
    private Button connectbtn;
    private VBox buttonGameBox;
    private VBox playersConnectedBox;

    @Override
    public void start(Stage primaryStage) {
        
        creatingConnectSzene(primaryStage);
        creatingGameSzene(primaryStage);

        primaryStage.setTitle("Mulitplayer Game Racing");
        primaryStage.setScene(connectScene);
        primaryStage.show();
        
        FormulaGame formulaGame = new FormulaGame();
        formulaGame.start(primaryStage); // Starte die FormulaGame-Klasse
        switchScene(primaryStage, formulaGame.getScene()); // Wechsle zur Szene der FormulaGame-Klasse

    }

    private void creatingConnectSzene(Stage primaryStage) {
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

        connectBox = new VBox(10); // Abstand
        connectBox.setAlignment(Pos.CENTER); // Zentrieren
        connectBox.getChildren().add(usernameField); 
        connectBox.getChildren().add(connectbtn); 
        connectBox.getChildren().add(serverStatus);

        connectScene = new Scene(connectBox, SCREEN_WIDTH, SCREEN_HEIGHT);
        connectScene.getRoot().setStyle("-fx-background-color: blue;");

    }

    private void creatingGameSzene(Stage primaryStage) {
        connectedUsersLabel = new Label();
        connectedUsersLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-font-size: 18px;");
        
        btnStart = new Button();
        btnStart.setText("Start");

        btnStart.setOnAction(event -> {
            
        });
        
        btnSettings = new Button();
        btnSettings.setText("Settings");
        
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
    

    private void establishConnection(Stage primaryStage) {
        try {
            // Erstellen der Optionen
            IO.Options options = IO.Options.builder()
                    .setExtraHeaders(Collections.singletonMap("username", Collections.singletonList(username)))
                    .build();

            socket = IO.socket("http://3.71.101.250:3000/", options);

            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Platform.runLater(() -> {
                        serverStatus.setText("Verbunden mit dem Server");
                        switchScene(primaryStage, gameScene);
                    });
                }
            });

            socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Platform.runLater(() -> serverStatus.setText("Verbindungsfehler: " + args[0]));
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
            Platform.runLater(() -> serverStatus.setText("Ungültige Serveradresse oder Port"));
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

    private void switchScene(Stage stage, Scene scene) {
        Platform.runLater(() -> {
            stage.setScene(scene);
            stage.show();
        });
    }
    

    public static void main(String[] args) {
        launch(args);
    }
}
