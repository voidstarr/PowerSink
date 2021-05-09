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
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import tv.voidstar.powersink.PowerSink;
import tv.voidstar.powersink.PowerSinkConfig;
import tv.voidstar.powersink.PowerSinkData;
import tv.voidstar.powersink.Util;
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
        PowerSink.getLogger().debug("InteractBlockEvent.Primary.MainHand");
        Optional<ItemStack> heldItemOpt = player.getItemInHand(event.getHandType());
        if(!heldItemOpt.isPresent()) return;
        String heldItemString = heldItemOpt.get().getType().getName();

        PowerSink.getLogger().debug("Player is holding {}", heldItemString);

        NodeType nodeType;
        if(heldItemString.equals(PowerSinkConfig.getNode("activationItems", "sink").getString())) {
            nodeType = NodeType.SINK;
        } else if(heldItemString.equals(PowerSinkConfig.getNode("activationItems", "source").getString())) {
            nodeType = NodeType.SOURCE;
        } else {
            return;
        }

        Optional<Location<World>> locationOpt = event.getTargetBlock().getLocation();
        if(!locationOpt.isPresent()) return;
        Location<World> location = locationOpt.get();

        PowerSink.getLogger().debug("Sponge block at : {}", location.toString());

        Optional<Integer> dimensionIdOpt = Util.dimensionIDFromSpongeLocation(location);
        if(!dimensionIdOpt.isPresent()) return;
        BlockPos blockPos = Util.forgeBlockPosFromSpongeLocation(location);

        TileEntity tileEntity = DimensionManager.getWorld(dimensionIdOpt.get()).getTileEntity(blockPos);

        if(tileEntity == null) {
            PowerSink.getLogger().debug("TE==null");
            return;
        }

        EnergyType energyType = EnergyCapability.getEnergyStorageType(tileEntity);

        PowerSink.getLogger().debug("Player left clicked: {}", tileEntity.getDisplayName());

        if (energyType == EnergyType.NONE){
            Text message = Text.builder("[PowerSink] That block is not supported.").build();
            player.sendMessage(message);
            return;
        }

        PowerSink.getLogger().debug("\tblock at {} has Energy capability.", tileEntity.getPos().toString());
        PowerSink.getLogger().debug("\tEnergy type: {}", energyType.toString());
        if(energyType == EnergyType.FORGE) {
            IEnergyStorage energyStorage = (IEnergyStorage) EnergyCapability.getCapabilityInterface(tileEntity, energyType).get();
            if(nodeType == NodeType.SINK && !energyStorage.canReceive()) {
                Text message = Text.builder("[PowerSink] That block can not receive energy.").build();
                player.sendMessage(message);
                return;
            } else if(nodeType == NodeType.SOURCE && !energyStorage.canExtract()) {
                Text message = Text.builder("[PowerSink] That block can not send energy.").build();
                player.sendMessage(message);
                return;
            }
            PowerSink.getLogger().debug("\tEnergy stored: {}", energyStorage.getEnergyStored());
            PowerSink.getLogger().debug("\tMax energy stored: {}", energyStorage.getMaxEnergyStored());
            PowerSink.getLogger().debug("\tcan extract: {}", energyStorage.canExtract());
            PowerSink.getLogger().debug("\tcan receive: {}", energyStorage.canReceive());
        } else if (energyType == EnergyType.MEKANISM) {
            IStrictEnergyStorage energyStorage = (IStrictEnergyStorage) EnergyCapability.getCapabilityInterface(tileEntity, energyType).get();
            PowerSink.getLogger().debug("\tEnergy stored: {}", energyStorage.getEnergy());
            PowerSink.getLogger().debug("\tMax energy stored: {}", energyStorage.getMaxEnergy());
        }

        EnergyNode energyNode = null;
        switch (nodeType) {
            case SINK:
                energyNode = new EnergySink(location, player.getUniqueId(), energyType, tileEntity);
                break;
            case SOURCE:
                energyNode = new EnergySource(location, player.getUniqueId(), energyType, tileEntity);
                break;
        }

        if(!PowerSinkData.getEnergyNodes().containsKey(location)) {
            PowerSinkData.addEnergyNode(energyNode);
        } else {
            Text message = Text.builder("[PowerSink] That block is already registered.").build();
            player.sendMessage(message);
        }
    }
}
