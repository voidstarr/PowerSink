package tv.voidstar.powersink.energy;

import net.minecraft.tileentity.TileEntity;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import tv.voidstar.powersink.energy.compat.EnergyCapability;
import tv.voidstar.powersink.energy.compat.EnergyType;

import java.util.UUID;

@ConfigSerializable
public class EnergySource extends EnergyNode {
    public EnergySource(Location<World> location, UUID playerOwner, EnergyType energyType, TileEntity tileEntity) {
        super(location, playerOwner, energyType, tileEntity);
    }

    public EnergySource(Location<World> location, UUID playerOwner, EnergyType energyType) {
        super(location, playerOwner, energyType);
    }

    public EnergySource() {

    }

    @Override
    public void handleEnergyTick() {
        EnergyCapability.removeEnergyAndPay(this);
    }
}
