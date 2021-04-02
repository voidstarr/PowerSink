package tv.voidstar.powersink.event.listener;

import mekanism.api.energy.IStrictEnergyStorage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import tv.voidstar.powersink.Constants;
import tv.voidstar.powersink.PowerSink;
import tv.voidstar.powersink.PowerSinkData;
import tv.voidstar.powersink.Util;
import tv.voidstar.powersink.energy.compat.EnergyCapability;
import tv.voidstar.powersink.energy.compat.EnergyType;
import tv.voidstar.powersink.event.NodeType;
import tv.voidstar.powersink.event.RegisterNodeEvent;
import java.util.Optional;

@Mod.EventBusSubscriber
public class ForgeClickEventListener {

	@SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
	public void onPlayerLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
		EntityPlayer player = event.getEntityPlayer();
		ItemStack heldItemStack = player.getHeldItem(EnumHand.MAIN_HAND);
		String heldItemString = heldItemStack == null ? "nothing" : heldItemStack.getItem().getRegistryName().toString();
		NodeType nodeType = null;

		if(heldItemString == Constants.SINK_TRIGGER_ITEM) {
			nodeType = NodeType.SINK;
		} else if(heldItemString == Constants.SOURCE_TRIGGER_ITEM) {
			nodeType = NodeType.SOURCE;
		} else {
			return;
		}

		BlockPos blockPos = event.getPos();
		TileEntity tileEntity = event.getWorld().getTileEntity(blockPos);
		EnergyType energyType = EnergyCapability.getEnergyStorageType(tileEntity);

		if(energyType == EnergyType.NONE) {
			return;
		}

		int dimensionId = tileEntity.getWorld().provider.getDimension();

		MinecraftForge.EVENT_BUS.post(new RegisterNodeEvent(blockPos, dimensionId, energyType, nodeType, player.getUniqueID()));

		// DEBUG
		PowerSink.getLogger().info("Player left clicked: {}", tileEntity.getDisplayName());
		PowerSink.getLogger().info("Player world name: {}", tileEntity.getWorld().getWorldInfo().getWorldName());
		PowerSink.getLogger().info("Player is holding {}", heldItemString);
		Util.spongeLocationFromForgeBlockPos(blockPos, tileEntity.getWorld().provider.getDimension())
				.ifPresent((location) -> {
					PowerSink.getLogger().info(
							"Util.spongeLocationfromForgeBlockPos Sponge block at : {}",
							location.toString()
					);
					location.getBlock().
				}
				);
		Optional<Location<World>> location = Util.spongeLocationFromForgeBlockPos(blockPos, tileEntity.getWorld().provider.getDimension());
		if (location.isPresent()) {
			PowerSink.getLogger().info("Sponge block at Util.fromBlockPos: {}", location.get().toString());
		} else {
			PowerSink.getLogger().info("could not get location<world> from blockpos and dim id Util.fromBlockPos");
		}

		PowerSink.getLogger().info("\tblock at {} has Forge Energy capability.", tileEntity.getPos().toString());
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
			PowerSink.getLogger().info("\tblock isn't an instance of IEnergyStorage");
		}
	}
}
