package tv.voidstar.powersink;

import com.google.inject.Inject;
import net.minecraftforge.common.MinecraftForge;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import tv.voidstar.powersink.energy.compat.EnergyCapability;
import tv.voidstar.powersink.event.listener.ForgeClickEventListener;
import tv.voidstar.powersink.event.listener.ForgeTickEventListener;
import tv.voidstar.powersink.event.listener.SpongeBlockBreakEventListener;
import tv.voidstar.powersink.payout.MoneyCalculator;
import tv.voidstar.powersink.payout.MoneyCalculatorRoot;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@Plugin(
        id = "powersink",
        name = "PowerSink",
        version = "0.1.0",
        dependencies = {@Dependency(id = "mekanism", optional = true)}
)
public class PowerSink {

    private File rootDir;

    private static PowerSink plugin;

    @Inject
    private Logger logger;
    private EconomyService economyService = null;
    private Currency currency = null;

    @Inject
    @ConfigDir(sharedRoot = true)
    private File defaultConfigDir;
    private MoneyCalculator moneyCalculator;

    @Listener
    public void onInit(GameInitializationEvent event) throws IOException {
        plugin = this;

        rootDir = new File(defaultConfigDir, "powersink");

        moneyCalculator = new MoneyCalculatorRoot(100, 10, 10); // TODO: change this
        PowerSinkConfig.init(rootDir); // TODO: do configs
        PowerSinkData.init(rootDir);

    }

    @Listener
    public void onEnable(GameStartingServerEvent event) {

    }

    @Listener
    public void onStart(GameStartedServerEvent event) {
        EnergyCapability.init();
        Optional<EconomyService> economyServiceOpt = Sponge.getServiceManager().provide(EconomyService.class);
        if(economyServiceOpt.isPresent()) {
            economyService = economyServiceOpt.get();
            currency = economyService.getDefaultCurrency();
        } else {
            getLogger().error("No economy service installed. This plugin can not function without one.");
            return;
        }
        MinecraftForge.EVENT_BUS.register(new ForgeTickEventListener());
        MinecraftForge.EVENT_BUS.register(new ForgeClickEventListener());
        Sponge.getEventManager().registerListeners(this, new SpongeBlockBreakEventListener());
    }

    public static PowerSink getInstance()
    {
        return plugin;
    }

    public static Logger getLogger()
    {
        return getInstance().logger;
    }

    public static EconomyService getEcoService()
    {
        return getInstance().economyService;
    }

    public static Currency getCurrency() {
        return getInstance().currency;
    }

    public static Cause getCause() {
        return Cause.builder()
                .build(EventContext.builder()
                        .add(EventContextKeys.PLUGIN, Sponge.getPluginManager().fromInstance(PowerSink.getInstance()).get()
                        ).build()
                );
    }

    public static MoneyCalculator getMoneyCalculator() {
        return getInstance().moneyCalculator;
    }
}
