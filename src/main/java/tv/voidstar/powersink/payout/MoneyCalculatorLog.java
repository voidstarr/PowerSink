package tv.voidstar.powersink.payout;

import org.nevec.rjm.BigDecimalMath;

import java.math.BigDecimal;

public class MoneyCalculatorLog extends MoneyCalculator {
  private final double baseMultiplier;
  private final double logBase;
  private final double shift;

  private final BigDecimal baseMultiplierBD;

  private final BigDecimal baseMultiplierHelper;

  /**
   * = 1 / ln(logBase)<br>
   * <br>
   * This is a constant used to calculate the log of the money.<br>
   * Since we only have a log2 for BigInteger, the formula is <code>log_b(x) = ln(x) / ln(b)
   * </code>. So we can precalculate <code>ln(b)</code>. And since multiplication is faster than
   * division we calculate the inverse too, so we just need to multiply it later.
   */
  private final BigDecimal logHelper;

  private final BigDecimal shiftBD;

  private final BigDecimal logBaseBD;

  public MoneyCalculatorLog(double baseMultiplier, double logBase, double shift) {
    this.baseMultiplier = baseMultiplier;
    this.logBase = logBase;
    this.shift = shift;

    baseMultiplierBD = BigDecimal.valueOf(baseMultiplier);
    baseMultiplierHelper = BigDecimal.ONE.divide(baseMultiplierBD);
    logHelper = BigDecimal.ONE.divide(BigDecimal.valueOf(Math.log(logBase)), CALCULATION_PRECISION);
    shiftBD = BigDecimal.valueOf(shift);
    logBaseBD = BigDecimal.valueOf(logBase);
  }

  @Override
  public BigDecimal covertEnergyToMoney(long energy) {
    if (energy < 0) throw new IllegalArgumentException("energy must not be negative");

    final BigDecimal tempResult;

    if (energy == 0) {
      tempResult = BigDecimal.ZERO;
    } else {
      // baseMultiplier * ((logHelper * ln(money)) + 1)
      // which is also
      // baseMultiplier * (log_logBase(money) + 1)
      tempResult =
          baseMultiplierBD.multiply(
              logHelper
                  .multiply(BigDecimal.valueOf(Math.log(energy)), CALCULATION_PRECISION)
                  .add(BigDecimal.ONE, CALCULATION_PRECISION),
              CALCULATION_PRECISION);
    }

    return roundResult(shiftBD.add(tempResult, CALCULATION_PRECISION));
  }

  public int covertMoneyToEnergy(BigDecimal money) { // TODO: is this needed?
    if (money.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("money must not be negative");

    if (money.compareTo(BigDecimal.ZERO) == 0) {
      return 0;
    } else {
      return BigDecimalMath.pow(logBaseBD, money
              .subtract(shiftBD, CALCULATION_PRECISION)
              .subtract(BigDecimal.ONE, CALCULATION_PRECISION)
              .multiply(baseMultiplierHelper, CALCULATION_PRECISION))
              .intValue();
    }
  }
}