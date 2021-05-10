package tv.voidstar.powersink.energy.compat;

import mekanism.api.energy.IStrictEnergyStorage;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.text.Text;
import tv.voidstar.powersink.PowerSink;
import tv.voidstar.powersink.PowerSinkConfig;
import tv.voidstar.powersink.payout.MoneyCalculator;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public class MekanismCompat {

    public static void removeEnergyAndPay(TileEntity tileEntity, UUID playerOwner) {
        Optional<Object> mekanismEnergyStorageOpt = EnergyCapability.getCapabilityInterface(tileEntity, EnergyType.MEKANISM);
        if (!mekanismEnergyStorageOpt.isPresent()) {
            PowerSink.getLogger().error("Unable to get Energy Storage Interface for block at {}", tileEntity.getPos().toString());
            return;
        }

        IStrictEnergyStorage mekanismEnergyStorage = (IStrictEnergyStorage) mekanismEnergyStorageOpt.get();
        int currentEnergy = Double.valueOf(mekanismEnergyStorage.getEnergy()).intValue();
        int energyToExtract = 0;
        int maxEnergyToTransact = PowerSinkConfig.getNode("rates", "maxEnergyTransaction").getInt();
        if (currentEnergy <= 0) {
            return;
        } else if (currentEnergy - maxEnergyToTransact < 0) {
            energyToExtract = currentEnergy;
        } else if (currentEnergy - maxEnergyToTransact >= 0) {
            energyToExtract = maxEnergyToTransact;
        }

        BigDecimal moneyToDeposit = MoneyCalculator.getMoneyCalculator().covertEnergyToMoney(energyToExtract);

        Account acc = PowerSink.getEcoService().getOrCreateAccount(playerOwner).get();
        TransactionResult result = acc.deposit(PowerSink.getCurrency(), moneyToDeposit, PowerSink.getCause());
        if (result.getResult() == ResultType.ACCOUNT_NO_SPACE) {
            Sponge.getGame().getServer().getPlayer(playerOwner).ifPresent((player -> {
                player.sendMessage(Text.builder("account full. unable to deposit funds.").build());
            }));
            // TODO: disable the offending player's energy sources
        } else {
            mekanismEnergyStorage.setEnergy(currentEnergy - energyToExtract);
        }
    }

    public static void withdrawPaymentAndAddEnergy(TileEntity tileEntity, UUID playerOwner) {
        Optional<Object> mekanismEnergyStorageOpt = EnergyCapability.getCapabilityInterface(tileEntity, EnergyType.MEKANISM);
        if (!mekanismEnergyStorageOpt.isPresent()) {
            PowerSink.getLogger().error("Unable to get Energy Storage Interface for block at {}", tileEntity.getPos().toString());
            return;
        }

        IStrictEnergyStorage mekanismEnergyStorage = (IStrictEnergyStorage) mekanismEnergyStorageOpt.get();
        int currentEnergy = Double.valueOf(mekanismEnergyStorage.getEnergy()).intValue();
        int energyToGive = 0;
        int maxEnergyToTransact = PowerSinkConfig.getNode("rates", "maxEnergyTransaction").getInt();
        int maxEnergy = Double.valueOf(mekanismEnergyStorage.getMaxEnergy()).intValue();
        if (currentEnergy == maxEnergy) {
            return;
        } else if (currentEnergy + maxEnergyToTransact > maxEnergy) {
            energyToGive = maxEnergy - currentEnergy;
        } else if (currentEnergy + maxEnergyToTransact <= maxEnergy) {
            energyToGive = maxEnergyToTransact;
        }

        BigDecimal moneyToWithdraw = MoneyCalculator.getMoneyCalculator().covertEnergyToMoney(energyToGive);

        Account acc = PowerSink.getEcoService().getOrCreateAccount(playerOwner).get();
        TransactionResult result = acc.withdraw(PowerSink.getCurrency(), moneyToWithdraw, PowerSink.getCause());

        if (result.getResult() == ResultType.ACCOUNT_NO_FUNDS) {
            Sponge.getGame().getServer().getPlayer(playerOwner).ifPresent((player -> {
                player.sendMessage(Text.builder("not enough funds. unable to give energy").build());
            }));
            // TODO: disable the offending player's energy sink
        } else {
            mekanismEnergyStorage.setEnergy(energyToGive + currentEnergy);
        }

    }
}
