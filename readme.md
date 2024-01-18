#Code Struktur Java

---

###Einleitung
Wir haben ein Multiplayer-Racing-Game in einem Java Client (JDK 21) erstellt. Zur Darstellung nutzten wir JavaFX mit Gradle und socketIO.

---

###Hauptklasse (App):

Verantwortlich für den Start der Anwendung und die Erstellung von Szenen.
Enthält separate Methoden zur Erstellung der Connect-, Game- und Settings-Szenen.
Organisiert die Struktur der Anwendung.

---

###Road-Klasse: 
Ist für die Darstellung und Logik eines Rennspiels verantwortlich. Sie beinhaltet Funktionen zur Straßenerzeugung, Segmentrendering, Sprite- und Spielerpositionshandhabung. Die Klasse unterstützt auch Multiplayer-Interaktion über Sockets und enthält UI-Handler für Einstellungen und Anzeige von HUD-Elementen.

---

##Unterschied Sprites/Sprite Klasse
###Sprites-Klasse:
Enthält statische Instanzen von Sprite-Objekten, die verschiedene Elemente im Spiel repräsentieren (z.B., Bäume, Fahrzeuge).
Verwendet Arrays und Listen, um Gruppen von Sprites zu organisieren (z.B., BILLBOARDS, PLANTS, CARS).
Bietet Methoden, um bestimmte Sprite-Objekte basierend auf ihrem Index abzurufen.

##Sprite-Klasse:

Repräsentiert ein grafisches Element im Spiel, definiert durch seine Position (x, y) und Dimensionen (Breite und Höhe).
Enthält auch Attribute wie Offset, Quelle (für animierte Sprites) und Name.

---

###RoadDefinition-Klasse:

Definiert Enums für Längen, Kurven und Hügel, die möglicherweise für die Generierung der Straße und ihrer Merkmale verwendet werden.

---

###Segment-Klasse:

Repräsentiert einen Abschnitt der Straße.
Enthält Informationen wie Index, Farbe, Schleifenstatus, Nebel, Klippwert, Kurve und Punkte (p1, p2).
Kann auch eine Liste von Autos und Sprites enthalten, die auf diesem Straßensegment vorhanden sind.

---

###Util-Klasse:

Enthält verschiedene Hilfsmethoden, z.B., für die Zufallsgenerierung, Limitierung von Werten und Projektion von 3D-Punkten auf den Bildschirm.

---

###Render-Klasse:

Verantwortlich für das Rendern der Hintergründe, Straßensegmente, Spieler und anderer Spielobjekte auf dem Canvas.
Enthält Methoden wie background, segment, polygon, fog, und sprite, um verschiedene Elemente zu zeichnen.
Nutzt auch die Util-, Sprites- und Colors-Klassen für Hilfsfunktionen, Bildressourcen und Farbdefinitionen.

---

###Background-Klasse:

Repräsentiert einen Hintergrund mit Position (x, y) und Abmessungen (width, height).
Wird von der Render-Klasse für die Hintergründe "HILLS", "SKY" und "TREES" verwendet.

---
##Point Klassen:
###Point3D_3-Klasse:

Repräsentiert einen dreidimensionalen Punkt im Raum mit den Koordinaten x, y und z.

###Point3D_2-Klasse:

Verbindet einen dreidimensionalen Weltkoordinatenpunkt mit einem Kamerapunkt und einem Bildschirmkoordinatenpunkt.
Enthält Instanzen von Point3D_3 und Point2D_2.

###Point2D_2-Klasse:

Repräsentiert einen zweidimensionalen Punkt im Raum mit den Koordinaten x, y, einer Breite (width) und einem Maßstab (scale).

---

###Car-Klasse:

Repräsentiert ein Fahrzeug im Spiel
Attribute: Position (z), Geschwindigkeit, Offset (Spielerposition auf der Straße), Benutzerinformationen, aktuelle Runde
Methoden zum Setzen und Abrufen der Werte
Handhabung von Nitro und Animationssprites

---

###Colors-Klasse:

Definiert Farben für Spiellemente wie Straße, Gras und Rumble-Bereiche
Statische Konstanten für Farbwerte
Methoden zum Abrufen von Farbvariationen basierend auf Spielbedingungen (Rumble-Länge, Segmentindex)

---

###Hud-Klasse:

Verwaltet HUD-Elemente für das Spiel
Eigenschaften: Geschwindigkeit (DoubleProperty), aktuelle Rundenzeit (StringProperty), letzte Rundenzeit (StringProperty), schnellste Rundenzeit (StringProperty)
Konstruktor initialisiert die Eigenschaften mit Standardwerten
Methoden zum Abrufen und Setzen der Werte, sowie Zugriff auf die Eigenschaften als Properties

---

### Ordner Strukturen
#### Bilder und Ressourcen (Images-Ordner): 
- Images-Ordner: Alle verwendeten Bilder befinden sich im "Images"-Ordner.  
- Zugriff und Verwendung: Die Bilder werden direkt geladen, ohne die Verwendung eines speziellen ImageLoaders.

---

###Netzwerk-Kommunikation
Für die Netzwerkkommunikation wird das Socket.IO-Protokoll und die Socket.IO-Bibliothek in Java verwendet. Hier sind die Schlüsselfunktionen:

Empfang von Spielerdaten (receive_data Event):

Aktualisiert Informationen über andere Spieler im Spiel.
Verwendet Platform.runLater() für sichere Benutzeroberflächenaktualisierung im JavaFX-Thread.
Empfang der Spielerreihenfolge (receive_order Event):

Ermittelt die Position des aktuellen Clients in der Reihenfolge der Spieler.
Empfang von NPC-Auto-Daten (receive_npc_car_data Event):

Speichert NPC-Autodaten lokal und verteilt sie auf entsprechende Straßensegmente.
Empfang der Spielerstartpositionen (receive_start_position Event):

Speichert die Startposition des Spielers lokal.
Weitere Funktionen (putCarsIntoSegments, send_data):

putCarsIntoSegments platziert NPC-Autos in den entsprechenden Straßensegmenten.
send_data sendet Spielerdaten an den Server.
Das Netzwerkprotokoll ermöglicht zuverlässige Synchronisation und den Austausch von Spielinformationen zwischen Server und Clients. Die wichtigsten Socket.IO-Events sind:

EVENT_CONNECT:

Wird ausgelöst, wenn eine Verbindung zum Server hergestellt wird.
Aktualisiert die Benutzeroberfläche nach erfolgreicher Verbindung.
EVENT_CONNECT_ERROR:

Wird ausgelöst, wenn ein Verbindungsfehler auftritt.
Behandelt Verbindungsfehler und aktualisiert die Benutzeroberfläche entsprechend.
"playersConnected":

Wird ausgelöst, wenn Spieler mit dem Server verbunden sind.
Verarbeitet die verbundenen Spielerinformationen und aktualisiert die Benutzeroberfläche.
"getHostID":

Wird ausgelöst, um die ID des Hosts zu erhalten.
Setzt den Host basierend auf den erhaltenen Serverinformationen.
"getPlayerID":

Wird ausgelöst, um die ID des Spielers zu erhalten.
Setzt die Spieler-ID basierend auf den erhaltenen Serverinformationen.
"start":

Wird ausgelöst, wenn das Spiel gestartet werden soll.
Aktualisiert den Spielstartstatus und passt die Benutzeroberfläche entsprechend an.
"all_players_ready":

Wird ausgelöst, wenn alle Spieler bereit sind, das Spiel zu starten.
Setzt den Status, ob das Spiel gestartet werden kann, basierend auf den Serverinformationen.


