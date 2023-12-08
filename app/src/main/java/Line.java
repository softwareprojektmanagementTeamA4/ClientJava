private class Line {
    // Ähnlich zur Python-Implementierung, um Linienattribute wie Position, Farbe usw. zu speichern
    private int i;
    private double x, y, z; // Spielposition (3D-Raum)
    private double X, Y, W; // Spielposition (2D-Projektion)
    private double scale; // Skalierung in Bezug auf die Kameraposition
    private double curve; // Kurvenradius

    private Color grassColor; // Farbe für Gras
    private Color rumbleColor; // Farbe für Rumble
    private Color roadColor; // Farbe für Straße

    public void project(int camX, int camY, int camZ) {
        // Berechnen, wie die Linie in Bezug auf die Kamera auf dem Bildschirm projiziert wird (von 3D nach 2D)
        this.scale = camD / (this.z - camZ);
        this.X = (1 + this.scale * (this.x - camX)) * WINDOW_WIDTH / 2;
        this.Y = (1 - this.scale * (this.y - camY)) * WINDOW_HEIGHT / 2;
        this.W = this.scale * roadW * WINDOW_WIDTH / 2;
    }
}