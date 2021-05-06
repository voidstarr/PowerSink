package tv.voidstar.powersink.payout;

import tv.voidstar.powersink.PowerSinkConfig;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public abstract class MoneyCalculator {
  protected static final MathContext CALCULATION_PRECISION = MathContext.DECIMAL128;
  protected static final int RESULT_DIGITS = 4;
  protected static final RoundingMode RESULT_ROUNDING_MODE = RoundingMode.HALF_EVEN;
  private static MoneyCalculator moneyCalculator = null;

  public abstract BigDecimal covertEnergyToMoney(long energy);

  protected static BigDecimal roundResult(BigDecimal val) {
    return val.setScale(RESULT_DIGITS, RESULT_ROUNDING_MODE);
  }

  public static void init() {
    double base = PowerSinkConfig.getNode("rates", "base").getDouble();
    double multiplier = PowerSinkConfig.getNode("rates", "multiplier").getDouble();
    double shift = PowerSinkConfig.getNode("rates", "shift").getDouble();
    switch (PowerSinkConfig.getNode("rates", "function").getString()) {
      default:
      case "root":
        moneyCalculator = new MoneyCalculatorRoot(multiplier, base, shift);
        break;
      case "log":
        moneyCalculator = new MoneyCalculatorLog(multiplier, base, shift);
        break;
    }
  }

  public static MoneyCalculator getMoneyCalculator() {
    return moneyCalculator;
  }
}