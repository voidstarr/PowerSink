package tv.voidstar.powersink;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.io.IOException;

public class PowerSinkConfig {
    private static File configFile;
    private static @NonNull HoconConfigurationLoader loader;
    private static CommentedConfigurationNode configs;

    public static void init(File rootDir) throws IOException {
        configFile = new File(rootDir, "powersink.conf");
        loader = HoconConfigurationLoader.builder().setFile(configFile).build();
        load();
    }

    public static void load() {
        try {
            if(!configFile.exists()) {
                configFile.getParentFile().mkdirs();
                configFile.createNewFile();
            }
            configs = loader.load();
        } catch (IOException e) {
            PowerSink.getLogger().error("Unable to load config file.", e);
        }

        // defaults
        configs.getNode("powersink").setComment("General PowerSink configurations.");
        getValOrSetDefault(configs.getNode("powersink", "currency"), "dollar");

        configs.getNode("activation_items").setComment("What item a player should hold to register a sink or source.");
        getValOrSetDefault(configs.getNode("activation_items", "source"),"minecraft:redstone");
        getValOrSetDefault(configs.getNode("activation_items", "sink"), "minecraft:glowstone_dust");

        configs.getNode("rates").setComment("How money given/withdrawn should be calculated.");
        getValOrSetDefault(configs.getNode("rates", "max_energy_transaction"),10000);
        getValOrSetDefault(configs.getNode("rates", "function"), "log");
        getValOrSetDefault(configs.getNode("rates", "base"),1.0);
        getValOrSetDefault(configs.getNode("rates", "multiplier"),1.0);
        getValOrSetDefault(configs.getNode("rates", "shift"),0.0);

        save();
    }

    public static void save() {
        try {
            loader.save(configs);
        } catch (IOException e) {
            PowerSink.getLogger().error("Unable to save config file.", e);
        }
    }

    public static CommentedConfigurationNode getNode(String... path) {
        return configs.getNode((Object[]) path);
    }

    private static void getValOrSetDefault(CommentedConfigurationNode node, String def) {
        if(node.getString() == null)
            node.setValue(def);
    }

    private static void getValOrSetDefault(CommentedConfigurationNode node, Number def) {
        if(!(node.getValue() instanceof Number))
            node.setValue(def);
    }
}
