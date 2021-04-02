package tv.voidstar.powersink.energy;

import net.minecraft.tileentity.TileEntity;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import tv.voidstar.powersink.energy.compat.EnergyCapability;
import tv.voidstar.powersink.energy.compat.EnergyType;

import java.util.UUID;

public class EnergySource extends EnergyNode {
    EnergySource(Location<World> location, UUID playerOwner, EnergyType energyType, TileEntity tileEntity) {
        super(location, playerOwner, energyType, tileEntity);
    }

    @Override
    public void handleEnergyTick() {
        EnergyCapability.removeEnergyAndPay(this);
    }
}
