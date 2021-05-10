package tv.voidstar.powersink;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import org.checkerframework.checker.nullness.qual.NonNull;
import tv.voidstar.powersink.energy.NodeType;
import tv.voidstar.powersink.payout.MoneyCalculator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

public class PowerSinkConfig {
    private static File configFile;
    private static @NonNull HoconConfigurationLoader loader;
    private static CommentedConfigurationNode configs;

    private static final LinkedHashMap<String, List<Integer>> groupLimits = new LinkedHashMap<>();

    public static void init(File rootDir) throws IOException {
        configFile = new File(rootDir, "powersink.conf");
        loader = HoconConfigurationLoader.builder().setFile(configFile).build();
        load();
        MoneyCalculator.init();
    }

    public static void load() {
        try {
            if (!configFile.exists()) {
                configFile.createNewFile();
            }
            configs = loader.load();
        } catch (IOException e) {
            PowerSink.getLogger().error("Unable to load config file.", e);
        }

        // defaults
        configs.getNode("powersink").setComment("General PowerSink configurations.");
        getValOrSetDefault(configs.getNode("powersink", "currency"), "dollar");
        getValOrSetDefault(configs.getNode("powersink", "tickInterval"), 2);

        configs.getNode("activationItems").setComment("What item a player should hold to register a sink or source.");
        getValOrSetDefault(configs.getNode("activationItems", "source"), "minecraft:redstone");
        getValOrSetDefault(configs.getNode("activationItems", "sink"), "minecraft:glowstone_dust");
        getValOrSetDefault(configs.getNode("activationItems", "remove"), "minecraft:bedrock");

        configs.getNode("rates").setComment("How money given/withdrawn should be calculated.");
        getValOrSetDefault(configs.getNode("rates", "maxEnergyTransaction"), 10000);
        getValOrSetDefault(configs.getNode("rates", "function"), "log");
        getValOrSetDefault(configs.getNode("rates", "base"), 100.0);
        getValOrSetDefault(configs.getNode("rates", "multiplier"), 10.0);
        getValOrSetDefault(configs.getNode("rates", "shift"), 10.0);

        CommentedConfigurationNode limits = configs.getNode("limits");
        if (!limits.isList()) {
            limits.setComment("Group limits on sources and sinks.");
            CommentedConfigurationNode admin = limits.appendListNode();
            getValOrSetDefault(admin.getNode("group"), "admin");
            getValOrSetDefault(admin.getNode("sink"), 1000);
            getValOrSetDefault(admin.getNode("source"), 1000);
            CommentedConfigurationNode player = limits.appendListNode();
            getValOrSetDefault(player.getNode("group"), "player");
            getValOrSetDefault(player.getNode("sink"), 4);
            getValOrSetDefault(player.getNode("source"), 4);
            CommentedConfigurationNode defaultGroup = limits.appendListNode();
            getValOrSetDefault(defaultGroup.getNode("group"), "default");
            getValOrSetDefault(defaultGroup.getNode("sink"), 1);
            getValOrSetDefault(defaultGroup.getNode("source"), 1);
        } else {
            for (CommentedConfigurationNode group : limits.getChildrenList()) {
                ArrayList<Integer> lim = new ArrayList<>();
                lim.add(group.getNode("sink").getInt());
                lim.add(group.getNode("source").getInt());
                groupLimits.put(group.getNode("group").getString("ERROR"), lim);
            }
            if (!groupLimits.containsKey("default")) {
                ArrayList<Integer> lim = new ArrayList<>();
                lim.add(0);
                lim.add(0);
                groupLimits.put("default", lim);
            }
        }

        save();
    }

    public static void save() {
        try {
            loader.save(configs);
        } catch (IOException e) {
            PowerSink.getLogger().error("Unable to save config file.", e);
        }
    }

    public static int getNodeLimit(UUID player, NodeType nodeType) {
        for (String group : groupLimits.keySet()) {
            if (PowerSink.getUserStorageService().get(player).get().hasPermission(Constants.LIMIT_BASE_PERMISSION + group)) {
                if (nodeType == NodeType.SINK)
                    return groupLimits.get(group).get(0);
                else if (nodeType == NodeType.SOURCE)
                    return groupLimits.get(group).get(1);
            }
        }
        if (nodeType == NodeType.SINK)
            return groupLimits.get("default").get(0);
        else if (nodeType == NodeType.SOURCE)
            return groupLimits.get("default").get(1);
        else
            return 0;
    }

    public static CommentedConfigurationNode getNode(String... path) {
        return configs.getNode((Object[]) path);
    }

    private static void getValOrSetDefault(CommentedConfigurationNode node, String def) {
        if (node.getString() == null)
            node.setValue(def);
    }

    private static void getValOrSetDefault(CommentedConfigurationNode node, Number def) {
        if (!(node.getValue() instanceof Number))
            node.setValue(def);
    }
}
