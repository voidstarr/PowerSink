package tv.voidstar.powersink.event.listener;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import tv.voidstar.powersink.PowerSink;
import tv.voidstar.powersink.PowerSinkData;

public class SpongeBlockBreakEventListener {
    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event, @First Player player) {
        PowerSink.getLogger().info("Player({}) broke block", player);
        event.getTransactions().forEach((blockSnapshotTransaction -> {
            blockSnapshotTransaction.getFinal().getLocation().ifPresent((location -> {
                PowerSinkData.delEnergyNode(location);
            }));
        }));

    }
}
