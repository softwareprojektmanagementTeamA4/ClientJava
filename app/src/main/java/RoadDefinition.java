public class RoadDefinition {
    
    public enum Length {
        NONE(0), SHORT(25), MEDIUM(50), LONG(100);

        private final int value;

        Length(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum Curve {
        NONE(0), EASY(2), MEDIUM(4), HARD(6);

        private final int value;

        Curve(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}