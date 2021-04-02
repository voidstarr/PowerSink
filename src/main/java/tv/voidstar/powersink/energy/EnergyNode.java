package tv.voidstar.powersink.energy;

import net.minecraft.tileentity.TileEntity;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import tv.voidstar.powersink.PowerSinkData;
import tv.voidstar.powersink.energy.compat.EnergyType;

import java.util.Optional;
import java.util.UUID;

@ConfigSerializable
public abstract class EnergyNode {
    protected TileEntity tileEntity = null;
    @Setting
    protected Location<World> location = null;
    @Setting
    protected UUID playerOwner = null;
    @Setting
    protected EnergyType energyType = null;

    EnergyNode(Location<World> location, UUID playerOwner, EnergyType energyType, TileEntity tileEntity) {
        this.location = location;
        this.playerOwner = playerOwner;
        this.energyType = energyType;
        this.tileEntity = tileEntity;
    }

    EnergyNode(Location<World> location, UUID playerOwner, EnergyType energyType) {
        this.location = location;
        this.playerOwner = playerOwner;
        this.energyType = energyType;
    }

    public Location<World> getLocation() {
        return location;
    }

    public EnergyType getEnergyType() {
        return energyType;
    }

    public UUID getPlayerOwner() {
        return playerOwner;
    }

    public Optional<TileEntity> getTileEntity() {
        return Optional.ofNullable(tileEntity);
    }

    public void setTileEntity(TileEntity tileEntity) {
        this.tileEntity = tileEntity;
    }

    public abstract void handleEnergyTick();

    public EnergyNode build() {

    }
}
