package tv.voidstar.powersink.event.listener;

import mekanism.api.energy.IStrictEnergyStorage;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.energy.IEnergyStorage;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import tv.voidstar.powersink.*;
import tv.voidstar.powersink.energy.EnergyNode;
import tv.voidstar.powersink.energy.EnergySink;
import tv.voidstar.powersink.energy.EnergySource;
import tv.voidstar.powersink.energy.compat.EnergyCapability;
import tv.voidstar.powersink.energy.compat.EnergyType;
import tv.voidstar.powersink.event.NodeType;

import java.util.Optional;

public class SpongeLeftClickListener {
    @Listener
    public void onPlayerInteractBlock(InteractBlockEvent.Primary event, @First Player player) {
        Optional<ItemStack> heldItemOpt = player.getItemInHand(event.getHandType());
        if(!heldItemOpt.isPresent()) return;
        String heldItemString = heldItemOpt.get().getType().getName();

        boolean removeNode = false;
        NodeType nodeType = null;
        if(heldItemString.equals(PowerSinkConfig.getNode("activationItems", "sink").getString())) {
            if(!player.hasPermission(Constants.SETUP_SINK_PERMISSION)) {
                PowerSink.sendMessage("You don't have permission to register a sink.", player);
                return;
            }
            nodeType = NodeType.SINK;
        } else if(heldItemString.equals(PowerSinkConfig.getNode("activationItems", "source").getString())) {
            if(!player.hasPermission(Constants.SETUP_SOURCE_PERMISSION)) {
                PowerSink.sendMessage("You don't have permission to register a source.", player);
                return;
            }
            nodeType = NodeType.SOURCE;
        } else if (heldItemString.equals(PowerSinkConfig.getNode("activationItems", "remove").getString())) {
            if(!player.hasPermission(Constants.REMOVE_NODES_PERMISSION)) {
                return;
            }
            removeNode = true;
        } else {
            return;
        }

        Optional<Location<World>> locationOpt = event.getTargetBlock().getLocation();
        if(!locationOpt.isPresent()) return;
        Location<World> location = locationOpt.get();

        if(removeNode) {
            if(PowerSinkData.hasEnergyNode(location)) {
                PowerSinkData.delEnergyNode(location);
            } else {
                PowerSink.sendMessage("That block is not registered.", player);
            }
            return;
        }

        Optional<Integer> dimensionIdOpt = Util.dimensionIDFromSpongeLocation(location);
        if(!dimensionIdOpt.isPresent()) return;
        BlockPos blockPos = Util.forgeBlockPosFromSpongeLocation(location);

        TileEntity tileEntity = DimensionManager.getWorld(dimensionIdOpt.get()).getTileEntity(blockPos);

        if(tileEntity == null) {
            return;
        }

        EnergyType energyType = EnergyCapability.getEnergyStorageType(tileEntity);

        if (energyType == EnergyType.NONE){
            PowerSink.sendMessage("That block is not supported.", player);
            return;
        }

        if(energyType == EnergyType.FORGE) {
            IEnergyStorage energyStorage = (IEnergyStorage) EnergyCapability.getCapabilityInterface(tileEntity, energyType).get();
            if (nodeType == NodeType.SINK && !energyStorage.canReceive()) {
                PowerSink.sendMessage("That block can not receive energy.", player);
                return;
            } else if (nodeType == NodeType.SOURCE && !energyStorage.canExtract()) {
                PowerSink.sendMessage("That block can not send energy.", player);
                return;
            }
        }
//            PowerSink.getLogger().debug("\tEnergy stored: {}", energyStorage.getEnergyStored());
//            PowerSink.getLogger().debug("\tMax energy stored: {}", energyStorage.getMaxEnergyStored());
//            PowerSink.getLogger().debug("\tcan extract: {}", energyStorage.canExtract());
//            PowerSink.getLogger().debug("\tcan receive: {}", energyStorage.canReceive());
//        } else if (energyType == EnergyType.MEKANISM) {
//            IStrictEnergyStorage energyStorage = (IStrictEnergyStorage) EnergyCapability.getCapabilityInterface(tileEntity, energyType).get();
//            PowerSink.getLogger().debug("\tEnergy stored: {}", energyStorage.getEnergy());
//            PowerSink.getLogger().debug("\tMax energy stored: {}", energyStorage.getMaxEnergy());
//        }

        EnergyNode energyNode = null;
        switch (nodeType) {
            case SINK:
                energyNode = new EnergySink(location, player.getUniqueId(), energyType, tileEntity);
                break;
            case SOURCE:
                energyNode = new EnergySource(location, player.getUniqueId(), energyType, tileEntity);
                break;
        }

        if(!PowerSinkData.hasEnergyNode(location)) {
            PowerSinkData.addEnergyNode(energyNode);
        } else {
            PowerSink.sendMessage("That block is already registered.", player);
        }
    }
}
