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
        PowerSink.getLogger().info("InteractBlockEvent.Primary.MainHand");
        Optional<ItemStack> heldItemOpt = player.getItemInHand(event.getHandType());
        if(!heldItemOpt.isPresent()) return;
        String heldItemString = heldItemOpt.get().getType().getName();

        PowerSink.getLogger().info("Player is holding {}", heldItemString);

        NodeType nodeType = null;
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

        PowerSink.getLogger().info("Sponge block at : {}", location.toString());

        Optional<Integer> dimensionIdOpt = Util.dimensionIDFromSpongeLocation(location);
        if(!dimensionIdOpt.isPresent()) return;
        BlockPos blockPos = Util.forgeBlockPosFromSpongeLocation(location);

        TileEntity tileEntity = DimensionManager.getWorld(dimensionIdOpt.get()).getTileEntity(blockPos);
        EnergyType energyType = EnergyCapability.getEnergyStorageType(tileEntity);

        PowerSink.getLogger().info("Player left clicked: {}", tileEntity.getDisplayName());
        PowerSink.getLogger().info("\tblock at {} has Energy capability.", tileEntity.getPos().toString());
        if(energyType == EnergyType.FORGE) {
            IEnergyStorage energyStorage = (IEnergyStorage) tileEntity;
            PowerSink.getLogger().info("\tEnergy stored: {}", energyStorage.getEnergyStored());
            PowerSink.getLogger().info("\tMax energy stored: {}", energyStorage.getMaxEnergyStored());
            PowerSink.getLogger().info("\tcan extract: {}", energyStorage.canExtract());
            PowerSink.getLogger().info("\tcan receive: {}", energyStorage.canReceive());
        } else if (energyType == EnergyType.MEKANISM) {
            IStrictEnergyStorage energyStorage = (IStrictEnergyStorage) tileEntity;
            PowerSink.getLogger().info("\tEnergy stored: {}", energyStorage.getEnergy());
            PowerSink.getLogger().info("\tMax energy stored: {}", energyStorage.getMaxEnergy());
        } else {
            PowerSink.getLogger().info("\tblock isn't supported");
        }

        if(energyType == EnergyType.NONE) {
            return;
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

        if(energyNode != null) {
            if(!PowerSinkData.getEnergyNodes().containsKey(location)) {
                PowerSinkData.addEnergyNode(energyNode);
            } else {
                Text message = Text.builder("[PowerSink] That block is already registered.").build();
                player.sendMessage(message);
            }
        } else {
            PowerSink.getLogger().error("EnergyNode at {} unable to be determined as SINK or SOURCE", location.toString());
        }
    }
}
