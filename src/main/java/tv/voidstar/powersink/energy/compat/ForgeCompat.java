package tv.voidstar.powersink.energy.compat;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.energy.IEnergyStorage;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.text.Text;
import tv.voidstar.powersink.Constants;
import tv.voidstar.powersink.PowerSink;

import java.math.BigDecimal;
import java.util.UUID;

public class ForgeCompat {
    public static void removeEnergyAndPay(TileEntity tileEntity, UUID playerOwner) {
        IEnergyStorage energyStorage = (IEnergyStorage) tileEntity;
        int energyToExtract = energyStorage.extractEnergy(Constants.ENERGY_TO_TRANSACT, true);
        BigDecimal moneyToDeposit = PowerSink.getMoneyCalculator().covertEnergyToMoney(energyToExtract);

        Account acc = PowerSink.getEcoService().getOrCreateAccount(playerOwner).get();
        TransactionResult result = acc.deposit(PowerSink.getCurrency(), moneyToDeposit, PowerSink.getCause());

        if(result.getResult() == ResultType.ACCOUNT_NO_SPACE) {
            Sponge.getGame().getServer().getPlayer(playerOwner).ifPresent((player -> {
                player.sendMessage(Text.builder("account full. unable to deposit funds.").build());
            }));
            // TODO: disable the offending player's energy sources
         } else {
            energyStorage.extractEnergy(energyToExtract, false);
        }
    }

    public static void withdrawPaymentAndAddEnergy(TileEntity tileEntity, UUID playerOwner) {
        IEnergyStorage energyStorage = (IEnergyStorage) tileEntity;
        int energyToGive = energyStorage.receiveEnergy(Constants.ENERGY_TO_TRANSACT, true);
        BigDecimal moneyToWithdraw = PowerSink.getMoneyCalculator().covertEnergyToMoney(energyToGive);

        Account acc = PowerSink.getEcoService().getOrCreateAccount(playerOwner).get();
        TransactionResult result = acc.withdraw(PowerSink.getCurrency(), moneyToWithdraw, PowerSink.getCause());

        if(result.getResult() == ResultType.ACCOUNT_NO_FUNDS) {
            Sponge.getGame().getServer().getPlayer(playerOwner).ifPresent((player -> {
                player.sendMessage(Text.builder("not enough funds. unable to give energy").build());
            }));
            // TODO: disable the offending player's energy sink
        } else {
            energyStorage.receiveEnergy(energyToGive, false);
        }
    }
}
