package tv.voidstar.powersink.event.listener;

import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import tv.voidstar.powersink.PowerSinkData;
import tv.voidstar.powersink.energy.EnergyNode;

public class ForgeTickEventListener {
    int ticks;

    public ForgeTickEventListener(){
        ticks = 0;
    }

    @SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
    public void onTick(TickEvent.ServerTickEvent event) {
        if(ticks % 20 == 0) {
            for(EnergyNode energyNode : PowerSinkData.getEnergyNodes()) {
                energyNode.handleEnergyTick();
            }
        }
        ticks++;
    }
}
