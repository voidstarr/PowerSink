package tv.voidstar.powersink.event.listener;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import tv.voidstar.powersink.energy.EnergyNode;
import tv.voidstar.powersink.event.RegisterNodeEvent;
import tv.voidstar.powersink.event.UnRegisterNodeEvent;

@Mod.EventBusSubscriber
public class EnergyNodeRegistrationListener {
    @SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
    public void onRegisterEnergyNode(RegisterNodeEvent event) {
        // when the event is fired from sponge,
        //  how do I get the tile entity I need to tick?
        // construct energy node
        EnergyNode energyNode;

        // register in data
    }

    @SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
    public void onUnRegisterEnergyNode(UnRegisterNodeEvent event) {

    }
}
