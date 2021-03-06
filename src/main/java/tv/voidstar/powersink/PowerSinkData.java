package tv.voidstar.powersink;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import tv.voidstar.powersink.energy.EnergyNode;
import tv.voidstar.powersink.energy.EnergySink;
import tv.voidstar.powersink.energy.EnergySource;
import tv.voidstar.powersink.energy.NodeType;
import tv.voidstar.powersink.energy.compat.EnergyType;
import tv.voidstar.powersink.serializer.EnergyTypeSerializer;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PowerSinkData {
    private static File energynodesFile;
    private static File cmdsDir;
    private static ConfigurationNode energyNodesList;
    private static ConfigurationOptions options;
    private static @NonNull HoconConfigurationLoader loader;
    private static final HashMap<Location<World>, EnergyNode> energyNodes = new LinkedHashMap<>();
    private static final Hashtable<UUID, Stack<Location<World>>> storedLocations = new Hashtable<>();
    private static final Currency currency = null;

    public static void init(File rootDir) throws IOException {
        energynodesFile = new File(rootDir, "energynodes.conf");
        if (!energynodesFile.exists())
            energynodesFile.createNewFile();

        TypeSerializerCollection serializers = TypeSerializerCollection.defaults().newChild();
        serializers.register(TypeToken.of(EnergyType.class), new EnergyTypeSerializer());
        options = ConfigurationOptions.defaults().withSerializers(serializers);

        loader = HoconConfigurationLoader.builder().setFile(energynodesFile).build();
        energyNodesList = loader.load(options);
    }

    public static void load() {
        for (ConfigurationNode energyNode : energyNodesList.getNode("energyNodes").getChildrenList()) {
            PowerSink.getLogger().debug("loading energy node. {}", energyNode.toString());
            try {
                String energyNodeClassName = energyNode.getNode("type").getString();
                Class energyNodeClass = Class.forName(energyNodeClassName);
                PowerSink.getLogger().debug("class: {}", energyNodeClass);
                if (energyNodeClassName == null) continue;
                if (energyNodeClassName.contains("EnergySource")) {
                    EnergySource node = (EnergySource) energyNode.getNode("energyNode").getValue(TypeToken.of(Class.forName(energyNodeClassName)));
                    node.fetchTileEntity();
                    energyNodes.put(node.getLocation(), node);
                } else if (energyNodeClassName.contains("EnergySink")) {
                    EnergySink node = (EnergySink) energyNode.getNode("energyNode").getValue(TypeToken.of(Class.forName(energyNodeClassName)));
                    node.fetchTileEntity();
                    energyNodes.put(node.getLocation(), node);
                }
            } catch (Exception e) {
                PowerSink.getLogger().error("Error loading EnergyNode", e);
            }
        }
    }

    public static void save() {
        energyNodesList.removeChild("energyNodes");
        for (EnergyNode energyNode : energyNodes.values()) {
            ConfigurationNode energyNodeNode = energyNodesList.getNode("energyNodes").appendListNode();
            try {
                PowerSink.getLogger().debug("serialize {}", energyNode.getClass().getName());
                if (energyNode.getNodeType() == NodeType.SOURCE) {
                    energyNodeNode.getNode("energyNode").setValue(TypeToken.of(EnergySource.class), (EnergySource) energyNode);
                } else if (energyNode.getNodeType() == NodeType.SINK) {
                    energyNodeNode.getNode("energyNode").setValue(TypeToken.of(EnergySink.class), (EnergySink) energyNode);
                } else {
                    continue;
                }
            } catch (ObjectMappingException e) {
                PowerSink.getLogger().error("Error saving EnergyNode at {}", energyNode.getLocation().toString(), e);
            }
            energyNodeNode.getNode("type").setValue(energyNode.getClass().getName());
        }
        try {
            loader.save(energyNodesList);
        } catch (IOException e) {
            PowerSink.getLogger().error("Could not save energy nodes to disk", e);
        }
    }

    public static void addEnergyNode(EnergyNode node) {
        energyNodes.put(node.getLocation(), node);
        notifyPlayerNodeModified(node, "created");
        save();
    }

    public static void delEnergyNode(Location<World> location) {
        EnergyNode node = energyNodes.get(location);
        energyNodes.remove(location);
        notifyPlayerNodeModified(node, "deleted");
        save();
    }

    public static void notifyPlayerNodeModified(EnergyNode node, String verb) {
        String nodeType = node.getNodeType().toString();
        nodeType = "Energy".concat(nodeType.substring(0, 1).toUpperCase().concat(nodeType.substring(1)));

        PowerSink.getLogger().info("{} at {} {}.", nodeType, node.getLocation(), verb);
        Text nodeTextWithLocation = Text.builder(nodeType.concat(" "))
                .color(TextColors.YELLOW)
                .onHover(TextActions.showText(Text.of(node.getLocation())))
                .build();

        Text ownedOverAllowed = Text.builder(
                Integer.toString(PowerSinkData.countNodes(node.getPlayerOwner(), node.getNodeType()))
                .concat("/")
                .concat(Integer.toString(PowerSinkConfig.getNodeLimit(node.getPlayerOwner(), node.getNodeType())))
        ).build();

        Sponge.getServer().getPlayer(node.getPlayerOwner()).ifPresent((player -> {
            player.sendMessage(Text.of("[PowerSink] ", nodeTextWithLocation, Text.of(verb.concat(". "), ownedOverAllowed)));
        }));
    }

    public static boolean hasEnergyNode(Location<World> location) {
        return energyNodes.containsKey(location);
    }

    public static int countNodes(UUID player, NodeType nodeType) {
        int count = 0;
        for (EnergyNode energyNode : energyNodes.values()) {
            if (energyNode.getPlayerOwner().equals(player)) {
                if (nodeType == null) {
                    ++count;
                } else if (energyNode.getNodeType() == nodeType) {
                    ++count;
                }
            }
        }
        return count;
    }

    public static void reload() {
        energyNodes.clear();
        load();
    }

    public static Map<Location<World>, EnergyNode> getEnergyNodes() {
        return Collections.synchronizedMap(energyNodes);
    }
}
