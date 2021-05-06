package tv.voidstar.powersink.energy.compat;

import mekanism.api.energy.IStrictEnergyStorage;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.Loader;
import tv.voidstar.powersink.PowerSink;
import tv.voidstar.powersink.energy.EnergyNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// rftools powercell, integrated dynamics batteries are compliant with IEnergyStorage forge
public class EnergyCapability {

    private static Map<EnergyType, Class> energyClasses = new HashMap<>();

    public static void init() {
        energyClasses.put(EnergyType.FORGE, IEnergyStorage.class);
        if (Loader.isModLoaded("mekanism")) {
            energyClasses.put(EnergyType.MEKANISM, IStrictEnergyStorage.class);
        }
    }

    public static EnergyType getEnergyStorageType(TileEntity tileEntity) {
        if(tileEntity.hasCapability(CapabilityEnergy.ENERGY, null)) {
            for (EnergyType energyType : energyClasses.keySet()) {
                if (energyClasses.get(energyType).isAssignableFrom(tileEntity.getClass())) {
                    return energyType;
                }
            }
        }
        return EnergyType.NONE;
    }

    public static void removeEnergyAndPay(EnergyNode energyNode) {
        Optional<TileEntity> tileEntityOpt = energyNode.getTileEntity();
        if(!tileEntityOpt.isPresent()) {
            PowerSink.getLogger().error(
                    "Unable to process EnergyNode at {} for Player({}). tileEntity not defined.",
                    energyNode.getLocation(),
                    energyNode.getPlayerOwner()
                    );
            // TODO: get tile entity or remove offending node
            return;
        }
        TileEntity tileEntity = tileEntityOpt.get();
        switch (energyNode.getEnergyType()) {
            case FORGE:
                ForgeCompat.removeEnergyAndPay(tileEntity, energyNode.getPlayerOwner());
                break;
            case MEKANISM:
                MekanismCompat.removeEnergyAndPay(tileEntity, energyNode.getPlayerOwner());
                break;
        }
    }

    public static void withdrawPaymentAndAddEnergy(EnergyNode energyNode) {
        Optional<TileEntity> tileEntityOpt = energyNode.getTileEntity();
        if(!tileEntityOpt.isPresent()) {
            PowerSink.getLogger().error(
                    "Unable to process EnergyNode at {} for Player({}). tileEntity not defined.",
                    energyNode.getLocation(),
                    energyNode.getPlayerOwner()
            );
            // TODO: get tile entity or remove offending node
        }
        TileEntity tileEntity = tileEntityOpt.get();
        switch (energyNode.getEnergyType()) {
            case FORGE:
                ForgeCompat.withdrawPaymentAndAddEnergy(tileEntity, energyNode.getPlayerOwner());
                break;
            case MEKANISM:
                MekanismCompat.withdrawPaymentAndAddEnergy(tileEntity, energyNode.getPlayerOwner());
                break;
        }
    }
}
