package tv.voidstar.powersink.event;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Event;
import tv.voidstar.powersink.Util;
import tv.voidstar.powersink.energy.EnergyNode;
import tv.voidstar.powersink.energy.compat.EnergyType;
import tv.voidstar.powersink.energy.EnergySink;
import tv.voidstar.powersink.energy.EnergySource;

import java.util.Optional;
import java.util.UUID;

public class RegisterNodeEvent extends Event {
    private Integer dimension = null;
    private final BlockPos blockPos;
    private final EnergyType energyType;
    private final NodeType nodeType;
    private final UUID playerOwner;

    public RegisterNodeEvent(BlockPos blockPos, int dimension, EnergyType energyType, NodeType nodeType, UUID playerOwner) {
        this.blockPos = blockPos;
        this.energyType = energyType;
        this.nodeType = nodeType;
        this.playerOwner = playerOwner;
        this.dimension = dimension;
    }

    public RegisterNodeEvent(EnergyNode node) {
        this.blockPos = Util.forgeBlockPosFromSpongeLocation(node.getLocation());
        this.energyType = node.getEnergyType();
        this.nodeType = (node instanceof EnergySource) ? NodeType.SOURCE : NodeType.SINK;
        this.playerOwner = node.getPlayerOwner();
        Util.dimensionIDFromSpongeLocation(node.getLocation())
                .ifPresent(dim -> this.dimension = dim);
    }

    public final BlockPos getBlockPos() {
        return blockPos;
    }

    public final EnergyType getEnergyType() {
        return energyType;
    }

    public final UUID getPlayerOwner() {
        return playerOwner;
    }

    public final Optional<Integer> getDimension() {
        return Optional.ofNullable(dimension);
    }

    public final NodeType getNodeType() {
        return nodeType;
    }
}
