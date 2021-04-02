package tv.voidstar.powersink.energy.compat;

import mekanism.api.energy.IStrictEnergyStorage;
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

public class MekanismCompat {

    public static void removeEnergyAndPay(TileEntity tileEntity, UUID playerOwner) {

    }

    public static void withdrawPaymentAndAddEnergy(TileEntity tileEntity, UUID playerOwner) {

    }
}
