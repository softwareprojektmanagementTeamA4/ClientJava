import javafx.scene.paint.Color;




public class CustomLine {
    private static final int SCREEN_WIDTH = 1280;
    private static final int SCREEN_HEIGHT = 960;
    private static final double camD = 0.84; // Kameratiefenkonstante
    private static final double roadW = 2000; // Straßenbreitenkonstante
    // Ähnlich zur Python-Implementierung, um Linienattribute wie Position, Farbe usw. zu speichern
    private int i;
    private double x, y, z; // Spielposition (3D-Raum)
    private double X, Y, W; // Spielposition (2D-Projektion)
    private double scale; // Skalierung in Bezug auf die Kameraposition
    private double curve; // Kurvenradius

    private Color grassColor; // Farbe für Gras
    private Color rumbleColor; // Farbe für Rumble
    private Color roadColor; // Farbe für Straße

    public CustomLine(int i) {
        // Standardkonstruktor: Initialisiert die Werte auf Standardwerte
        this.i = 0;
        this.x = 0.0;
        this.y = 0.0;
        this.z = 0.0;
        this.X = 0.0;
        this.Y = 0.0;
        this.W = 0.0;
        this.scale = 0.0;
        this.curve = 0.0;
        this.grassColor = Color.BLACK; // Beispiel-Farbwerte
        this.rumbleColor = Color.WHITE; // Beispiel-Farbwerte
        this.roadColor = Color.GRAY; // Beispiel-Farbwerte
    }

    public void project(int camX, int camY, int camZ) {
        // Berechnen, wie die Linie in Bezug auf die Kamera auf dem Bildschirm projiziert wird (von 3D nach 2D)
        this.scale = camD / (this.z - camZ);
        this.X = (1 + this.scale * (this.x - camX)) * SCREEN_WIDTH / 2;
        this.Y = (1 - this.scale * (this.y - camY)) * SCREEN_HEIGHT / 2;
        this.W = this.scale * roadW * SCREEN_WIDTH / 2;
    }
}