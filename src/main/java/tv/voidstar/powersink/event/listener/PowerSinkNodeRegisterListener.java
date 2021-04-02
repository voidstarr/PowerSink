package tv.voidstar.powersink.event.listener;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import tv.voidstar.powersink.event.RegisterNodeEvent;

@Mod.EventBusSubscriber
public class PowerSinkNodeRegisterListener {
    @SubscribeEvent(priority= EventPriority.NORMAL, receiveCanceled=true)
    public void onSourceRegister(RegisterNodeEvent event) {
        // add block
    }
}
