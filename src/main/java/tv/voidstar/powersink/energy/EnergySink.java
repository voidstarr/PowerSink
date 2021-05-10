package tv.voidstar.powersink.energy;

import net.minecraft.tileentity.TileEntity;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import tv.voidstar.powersink.energy.compat.EnergyCapability;
import tv.voidstar.powersink.energy.compat.EnergyType;

import java.util.UUID;

@ConfigSerializable
public class EnergySink extends EnergyNode {

    public EnergySink(Location<World> location, UUID playerOwner, EnergyType energyType) {
        super(location, playerOwner, energyType);
    }

    public EnergySink(Location<World> location, UUID playerOwner, EnergyType energyType, TileEntity tileEntity) {
        super(location, playerOwner, energyType, tileEntity);
    }

    public EnergySink() {

    }

    @Override
    public void handleEnergyTick() {
        EnergyCapability.withdrawPaymentAndAddEnergy(this);
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.SINK;
    }
}
