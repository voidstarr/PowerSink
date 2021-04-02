package tv.voidstar.powersink.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Event;
import tv.voidstar.powersink.energy.compat.EnergyType;

public class UnRegisterNodeEvent extends Event {
    private BlockPos blockPos;
    private EnergyType energyType;
    private EntityPlayer entityPlayer;
    private int dimension;

    public UnRegisterNodeEvent(BlockPos blockPos, EnergyType energyType, EntityPlayer entityPlayer) {
        this.blockPos = blockPos;
        this.energyType = energyType;
        this.entityPlayer = entityPlayer;
        this.dimension = entityPlayer.dimension;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public EnergyType getEnergyType() {
        return energyType;
    }

    public EntityPlayer getEntityPlayer() {
        return entityPlayer;
    }
}
