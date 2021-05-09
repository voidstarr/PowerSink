package tv.voidstar.powersink;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import tv.voidstar.powersink.energy.EnergyNode;
import tv.voidstar.powersink.energy.compat.EnergyCapability;
import tv.voidstar.powersink.event.listener.SpongeBlockBreakEventListener;
import tv.voidstar.powersink.event.listener.SpongeLeftClickListener;
import tv.voidstar.powersink.payout.MoneyCalculator;

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

    private PluginContainer container;

    private static PowerSink plugin;
    private static Cause cause;

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
        if(!rootDir.exists()) {
            if(!rootDir.mkdirs()) {
                PowerSink.getLogger().error("Unable to create root config dir");
            }
        }

        PowerSinkConfig.init(rootDir); // TODO: do configs
        PowerSinkData.init(rootDir);

    }

    @Listener
    public void onStart(GameStartedServerEvent event) {
        getLogger().info("PowerSink starting");
        Sponge.getPluginManager().fromInstance(PowerSink.getInstance())
                .ifPresent(pluginContainer -> container = pluginContainer);
        EnergyCapability.init();
        Optional<EconomyService> economyServiceOpt = Sponge.getServiceManager().provide(EconomyService.class);
        if(economyServiceOpt.isPresent()) {
            economyService = economyServiceOpt.get();
            currency = economyService.getDefaultCurrency();
            for(Currency c : economyService.getCurrencies()) {
                if (c.getName().equals(PowerSinkConfig.getNode("").getString())) {
                    currency = c;
                    break;
                }
            }
        } else {
            getLogger().error("No economy service installed. This plugin can not function without one.");
            return;
        }

        PowerSinkData.load();

        Sponge.getEventManager().registerListeners(this, new SpongeBlockBreakEventListener());
        Sponge.getEventManager().registerListeners(this, new SpongeLeftClickListener());
        // every x ticks, handle energy ticks for each energynode
        Sponge.getScheduler().createTaskBuilder()
                .execute(() -> {
                    for (EnergyNode node : PowerSinkData.getEnergyNodes().values()) {
                        node.handleEnergyTick();
                    }
                })
                .intervalTicks(PowerSinkConfig.getNode("powersink", "tickInterval").getInt())
                .submit(this.container);
    }

    @Listener
    public void onStop(GameStoppingServerEvent event) {
        getLogger().info("PowerSink stopping");
        PowerSinkData.save();
        PowerSinkConfig.save();
    }

    @Listener
    public void onReload(GameReloadEvent event) {
        getLogger().info("PowerSink reload");
        PowerSinkData.reload();
        PowerSinkConfig.load();
        MoneyCalculator.init();
    }

    public static PowerSink getInstance() {
        return plugin;
    }

    public static Logger getLogger() {
        return getInstance().logger;
    }

    public static EconomyService getEcoService() {
        return getInstance().economyService;
    }

    public static Currency getCurrency() {
        return getInstance().currency;
    }

    public static Cause getCause() {
        if (cause == null) {
            Optional<PluginContainer> pluginContainerOpt = Sponge.getPluginManager().fromInstance(PowerSink.getInstance());
            if(!pluginContainerOpt.isPresent()) {
                getLogger().error("can't get Plugin container from plugin instance. wtf?");
                return null;
            }

            EventContext context = EventContext.builder().add(EventContextKeys.PLUGIN, getInstance().container).build();

            cause = Cause.builder().append(pluginContainerOpt.get()).build(context);
        }
        return cause;
    }
}
