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