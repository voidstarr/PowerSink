package tv.voidstar.powersink.energy.compat;

import mekanism.api.energy.IStrictEnergyStorage;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.energy.CapabilityEnergy;
import tv.voidstar.powersink.PowerSink;
import tv.voidstar.powersink.energy.EnergyNode;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

// rftools powercell, integrated dynamics batteries are compliant with IEnergyStorage forge
public class EnergyCapability {

    private static Map<EnergyType, Capability<?>> energyCapabilities = new LinkedHashMap<>();

    @CapabilityInject(IStrictEnergyStorage.class)
    public static void registerMekanismIStrictEnergyStorage(Capability<?> capability) {
        PowerSink.getLogger().info("Enable Mekanism integration");
        energyCapabilities.put(EnergyType.MEKANISM, capability);
    }

    public static void init() {
        energyCapabilities.put(EnergyType.FORGE, CapabilityEnergy.ENERGY);
    }

    public static Optional<Capability<?>> getCapability(EnergyType type) {
        return Optional.ofNullable(energyCapabilities.get(type));
    }

    // TODO: do this better?
    // store capability interface on EnergyNode? how to detect when it changes?
    public static Optional<Object> getCapabilityInterface(TileEntity tileEntity, EnergyType type) {
        for(EnumFacing facing : EnumFacing.VALUES) {
            for (Map.Entry<EnergyType, Capability<?>> entry : energyCapabilities.entrySet()) {
                if (tileEntity.hasCapability(entry.getValue(), facing)) {
                    //PowerSink.getLogger().info("TE at {} has {} [{}] on {} side", tileEntity.getPos().toString(), entry.getValue().toString(), entry.getKey().toString(), facing.toString());
                    return Optional.ofNullable((Object) tileEntity.getCapability(entry.getValue(), facing));
                }
            }
        }
        for (Map.Entry<EnergyType, Capability<?>> entry : energyCapabilities.entrySet()) {
            if (tileEntity.hasCapability(entry.getValue(), null)) {
                //PowerSink.getLogger().info("TE at {} has {} [{}]", tileEntity.getPos().toString(), entry.getValue().toString(), entry.getKey().toString());
                return Optional.ofNullable((Object) tileEntity.getCapability(entry.getValue(), null));
            }
        }
        return Optional.empty();
    }

    public static EnergyType getEnergyStorageType(TileEntity tileEntity) {
        for(EnumFacing facing : EnumFacing.VALUES) {
            for (Map.Entry<EnergyType, Capability<?>> entry : energyCapabilities.entrySet()) {
                if (tileEntity.hasCapability(entry.getValue(), facing)) {
                    PowerSink.getLogger().debug("TE at {} has {} [{}] on {} side", tileEntity.getPos().toString(), entry.getValue().toString(), entry.getKey().toString(), facing.toString());
                    return entry.getKey();
                }
            }
        }
        for (Map.Entry<EnergyType, Capability<?>> entry : energyCapabilities.entrySet()) {
            if (tileEntity.hasCapability(entry.getValue(), null)) {
                PowerSink.getLogger().debug("TE at {} has {} [{}]", tileEntity.getPos().toString(), entry.getValue().toString(), entry.getKey().toString());
                return entry.getKey();
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
