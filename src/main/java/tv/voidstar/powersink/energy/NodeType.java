package tv.voidstar.powersink.energy;

public enum NodeType {
    SINK,
    SOURCE,
    NONE;

    public static NodeType fromString(String s) {
        switch (s) {
            case "sink":
                return NodeType.SINK;
            case "source":
                return NodeType.SOURCE;
            default:
                return NodeType.NONE;
        }
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}