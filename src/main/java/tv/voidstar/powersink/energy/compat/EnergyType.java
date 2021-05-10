package tv.voidstar.powersink.energy.compat;

public enum EnergyType {
    FORGE,
    MEKANISM,
    NONE;

    public static EnergyType fromString(String s) {
        switch (s) {
            case "forge":
                return EnergyType.FORGE;
            case "mekanism":
                return EnergyType.MEKANISM;
            default:
                return EnergyType.NONE;
        }
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
