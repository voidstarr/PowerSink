package tv.voidstar.powersink;

import com.google.common.reflect.TypeToken;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import tv.voidstar.powersink.energy.compat.EnergyType;
import tv.voidstar.powersink.energy.EnergyNode;
import tv.voidstar.powersink.energy.EnergySink;
import tv.voidstar.powersink.energy.EnergySource;
import tv.voidstar.powersink.event.RegisterNodeEvent;
import tv.voidstar.powersink.serializer.EnergyTypeSerializer;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PowerSinkData {
    private static File energynodesFile;
    private static File cmdsDir;
    private static ConfigurationNode energynodesRoot;
    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    private static Map<Location<World>, EnergyNode> energyNodes = new HashMap<>();
    private static Hashtable<UUID, Stack<Location<World>>> storedLocations = new Hashtable<>();
    private static Currency currency = null;

    public static void init(File rootDir) throws IOException {
        energynodesFile = new File(rootDir, "energynodes.json");
        energynodesFile.createNewFile();

        TypeSerializerCollection serializers = TypeSerializers.getDefaultSerializers().newChild();
        serializers.registerType(TypeToken.of(EnergyType.class), new EnergyTypeSerializer());
        //serializers.registerType(TypeToken.of(Currency.class), new CurrencySerializer());
        ConfigurationOptions options = ConfigurationOptions.defaults().setSerializers(serializers);

        loader = HoconConfigurationLoader.builder().setFile(energynodesFile).build();
        energynodesRoot = loader.load(options);
    }

    public static void load() {
        for (ConfigurationNode energyNode : energynodesRoot.getNode("energyNodes").getChildrenList()) {
            try {
                pushEnergyNode((EnergyNode) energyNode.getNode("energyNode").getValue(TypeToken.of(Class.forName(energyNode.getNode("type").toString()))));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void save() {
        energynodesRoot.removeChild("energyNodes");
        for (EnergyNode energyNode : energyNodes.values()) {
            ConfigurationNode energyNodeNode = energynodesRoot.getNode("energyNodes").getAppendedNode();
            try {
                if (energyNode instanceof EnergySource) {
                    energyNodeNode.getNode("energyNode").setValue(TypeToken.of(EnergySource.class), (EnergySource) energyNode);
                } else if (energyNode instanceof EnergySink) {
                    energyNodeNode.getNode("energyNode").setValue(TypeToken.of(EnergySink.class), (EnergySink) energyNode);
                } else {
                    continue;
                }
                energyNodeNode.getNode("type").setValue(energyNode.getClass().getName());
            } catch (ObjectMappingException e) {
                e.printStackTrace();
            }
        }
    }

    public static void pushEnergyNode(EnergyNode node) {
        energyNodes.put(node.getLocation(), node);
        Event registerEvent = null;
        if (node instanceof EnergySource) {
            registerEvent = new RegisterNodeEvent((EnergySource) node);
        } else if (node instanceof EnergySink) {
            registerEvent = new RegisterNodeEvent((EnergySink) node);
        } else {
            //
            return;
        }
        MinecraftForge.EVENT_BUS.post(registerEvent);
    }

    public static void addEnergyNode(EnergyNode node) {
        pushEnergyNode(node);
        save();
    }

    public static void delEnergyNode(Location<World> location) {

    }

    public static Collection<EnergyNode> getEnergyNodes() {
        return energyNodes.values();
    }
}
