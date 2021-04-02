package tv.voidstar.powersink;

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.world.Dimension;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class Util {
    public static Optional<Location<World>> spongeLocationFromForgeBlockPos(BlockPos blockPos, int dimension) {
        for (World w: Sponge.getGame().getServer().getWorlds()) {
            Optional<Integer> wDim = w.getProperties().getAdditionalProperties().getInt(DataQuery.of("SpongeData", "dimensionId"));
            if(wDim.isPresent() && wDim.get() == dimension)
                return Optional.of(w.getLocation(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
        }
        return Optional.empty();
    }

    public static BlockPos forgeBlockPosFromSpongeLocation(Location<World> location) {
        return new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static Optional<Integer> dimensionIDFromSpongeLocation(Location<World> location) {
        return location.getExtent().getProperties().getAdditionalProperties().getInt(DataQuery.of("SpongeData", "dimensionId"));
    }
}
