package cointoss.trade;

import cointoss.trade.FundManager;
import cointoss.trade.FundManagerModel;
import cointoss.util.Num;
import java.lang.Override;
import java.lang.StringBuilder;
import java.lang.Throwable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.DoubleUnaryOperator;
import java.util.function.UnaryOperator;
import javax.annotation.processing.Generated;

/**
 * Generated model for {@link FundManagerModel}.
 */
@Generated("Icy Manipulator")
public class FundManager implements FundManagerModel {

    /**
     * Deceive complier that the specified checked exception is unchecked exception.
     *
     * @param <T> A dummy type for {@link RuntimeException}.
     * @param throwable Any error.
     * @return A runtime error.
     * @throws T Dummy error to deceive compiler.
     */
    private static final <T extends Throwable> T quiet(Throwable throwable) throws T {
        throw (T) throwable;
    }

    /**
     * Create special method invoker.
     *
     * @param name A target method name.
     * @param parameterTypes A list of method parameter types.
     * @return A special method invoker.
     */
    private static final MethodHandle invoker(String name, Class... parameterTypes)  {
        try {
            Method method = FundManagerModel.class.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
            return MethodHandles.lookup().unreflect(method);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /** The overload or intercept method invoker. */
    private static final MethodHandle totalAssets$1093866057= invoker("totalAssets", long.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle totalAssets$912239839= invoker("totalAssets", double.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle validateRiskAssetsRatio$912239839= invoker("validateRiskAssetsRatio", double.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle validateLosscutRange$1377900837= invoker("validateLosscutRange", Num.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle validateRiskRewardRatio$912239839= invoker("validateRiskRewardRatio", double.class);

    /**
     * Create special property updater.
     *
     * @param name A target property name.
     * @return A special property updater.
     */
    private static final MethodHandle updater(String name)  {
        try {
            Field field = FundManager.class.getDeclaredField(name);
            field.setAccessible(true);
            return MethodHandles.lookup().unreflectSetter(field);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /** The final property updater. */
    private static final MethodHandle totalAssetsUpdater = updater("totalAssets");

    /** The final property updater. */
    private static final MethodHandle riskAssetsRatioUpdater = updater("riskAssetsRatio");

    /** The final property updater. */
    private static final MethodHandle losscutRangeUpdater = updater("losscutRange");

    /** The final property updater. */
    private static final MethodHandle riskRewardRatioUpdater = updater("riskRewardRatio");

    /** The final property updater. */
    private static final MethodHandle historicalVolatilityUpdater = updater("historicalVolatility");

    /** The final property updater. */
    private static final MethodHandle liquidityUpdater = updater("liquidity");

    /** The exposed property. */
    public final Num totalAssets;

    /** The exposed property. */
    public final double riskAssetsRatio;

    /** The exposed property. */
    public final Num losscutRange;

    /** The exposed property. */
    public final double riskRewardRatio;

    /** The exposed property. */
    public final Num historicalVolatility;

    /** The exposed property. */
    public final Num liquidity;

    /**
     * HIDE CONSTRUCTOR
     */
    protected FundManager() {
        this.totalAssets = null;
        this.riskAssetsRatio = FundManagerModel.super.riskAssetsRatio();
        this.losscutRange = FundManagerModel.super.losscutRange();
        this.riskRewardRatio = FundManagerModel.super.riskRewardRatio();
        this.historicalVolatility = FundManagerModel.super.historicalVolatility();
        this.liquidity = FundManagerModel.super.liquidity();
    }

    /**
     * Your total assets.
     *  
     *  @return
     */
    @Override
    public final Num totalAssets() {
        return this.totalAssets;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of totalAssets property.
     */
    @SuppressWarnings("unused")
    private final Num getTotalAssets() {
        return this.totalAssets;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of totalAssets property to assign.
     */
    private final void setTotalAssets(Num value) {
        if (value == null) {
            throw new IllegalArgumentException("The totalAssets property requires non-null value.");
        }
        try {
            totalAssetsUpdater.invoke(this, value);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Config the acceptable risk asset ratio.
     *  
     *  @return
     */
    @Override
    public final double riskAssetsRatio() {
        return this.riskAssetsRatio;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of riskAssetsRatio property.
     */
    @SuppressWarnings("unused")
    private final double getRiskAssetsRatio() {
        return this.riskAssetsRatio;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of riskAssetsRatio property to assign.
     */
    private final void setRiskAssetsRatio(double value) {
        try {
            riskAssetsRatioUpdater.invoke(this, validateRiskAssetsRatio$912239839.invoke(this, value));
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Config losscut range.
     *  
     *  @return
     */
    @Override
    public final Num losscutRange() {
        return this.losscutRange;
    }

    /**
     * Assign the new value of losscutRange property.
     *
     * @paran value The new losscutRange property value to assign.
     * @return Chainable API.
     */
    public final FundManager assignLosscutRange(Num value) {
        setLosscutRange(value);
        return this;
    }

    /**
     * Assign the new value of losscutRange property.
     *
     * @paran value The losscutRange property assigner which accepts the current value and returns new value.
     * @return Chainable API.
     */
    public final FundManager assignLosscutRange(UnaryOperator<Num> value) {
        setLosscutRange(value.apply(this.losscutRange));
        return this;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of losscutRange property.
     */
    @SuppressWarnings("unused")
    private final Num getLosscutRange() {
        return this.losscutRange;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of losscutRange property to assign.
     */
    private final void setLosscutRange(Num value) {
        if (value == null) {
            value = FundManagerModel.super.losscutRange();
        }
        try {
            losscutRangeUpdater.invoke(this, validateLosscutRange$1377900837.invoke(this, value));
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Config losscut range.
     *  
     *  @return
     */
    @Override
    public final double riskRewardRatio() {
        return this.riskRewardRatio;
    }

    /**
     * Assign the new value of riskRewardRatio property.
     *
     * @paran value The new riskRewardRatio property value to assign.
     * @return Chainable API.
     */
    public final FundManager assignRiskRewardRatio(double value) {
        setRiskRewardRatio(value);
        return this;
    }

    /**
     * Assign the new value of riskRewardRatio property.
     *
     * @paran value The riskRewardRatio property assigner which accepts the current value and returns new value.
     * @return Chainable API.
     */
    public final FundManager assignRiskRewardRatio(DoubleUnaryOperator value) {
        setRiskRewardRatio(value.applyAsDouble(this.riskRewardRatio));
        return this;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of riskRewardRatio property.
     */
    @SuppressWarnings("unused")
    private final double getRiskRewardRatio() {
        return this.riskRewardRatio;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of riskRewardRatio property to assign.
     */
    private final void setRiskRewardRatio(double value) {
        try {
            riskRewardRatioUpdater.invoke(this, validateRiskRewardRatio$912239839.invoke(this, value));
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Market histrical volatility.
     *  
     *  @return
     */
    @Override
    public final Num historicalVolatility() {
        return this.historicalVolatility;
    }

    /**
     * Assign the new value of historicalVolatility property.
     *
     * @paran value The new historicalVolatility property value to assign.
     * @return Chainable API.
     */
    public final FundManager assignHistoricalVolatility(Num value) {
        setHistoricalVolatility(value);
        return this;
    }

    /**
     * Assign the new value of historicalVolatility property.
     *
     * @paran value The historicalVolatility property assigner which accepts the current value and returns new value.
     * @return Chainable API.
     */
    public final FundManager assignHistoricalVolatility(UnaryOperator<Num> value) {
        setHistoricalVolatility(value.apply(this.historicalVolatility));
        return this;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of historicalVolatility property.
     */
    @SuppressWarnings("unused")
    private final Num getHistoricalVolatility() {
        return this.historicalVolatility;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of historicalVolatility property to assign.
     */
    private final void setHistoricalVolatility(Num value) {
        if (value == null) {
            value = FundManagerModel.super.historicalVolatility();
        }
        try {
            historicalVolatilityUpdater.invoke(this, value);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Market liquidity.
     *  
     *  @return
     */
    @Override
    public final Num liquidity() {
        return this.liquidity;
    }

    /**
     * Assign the new value of liquidity property.
     *
     * @paran value The new liquidity property value to assign.
     * @return Chainable API.
     */
    public final FundManager assignLiquidity(Num value) {
        setLiquidity(value);
        return this;
    }

    /**
     * Assign the new value of liquidity property.
     *
     * @paran value The liquidity property assigner which accepts the current value and returns new value.
     * @return Chainable API.
     */
    public final FundManager assignLiquidity(UnaryOperator<Num> value) {
        setLiquidity(value.apply(this.liquidity));
        return this;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of liquidity property.
     */
    @SuppressWarnings("unused")
    private final Num getLiquidity() {
        return this.liquidity;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of liquidity property to assign.
     */
    private final void setLiquidity(Num value) {
        if (value == null) {
            value = FundManagerModel.super.liquidity();
        }
        try {
            liquidityUpdater.invoke(this, value);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Show all property values.
     *
     * @return All property values.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("FundManager [");
        builder.append("totalAssets=").append(totalAssets).append(", ");
        builder.append("riskAssetsRatio=").append(riskAssetsRatio).append(", ");
        builder.append("losscutRange=").append(losscutRange).append(", ");
        builder.append("riskRewardRatio=").append(riskRewardRatio).append(", ");
        builder.append("historicalVolatility=").append(historicalVolatility).append(", ");
        builder.append("liquidity=").append(liquidity).append("]");
        return builder.toString();
    }

    /**
     * Generates a hash code for a sequence of property values. The hash code is generated as if all the property values were placed into an array, and that array were hashed by calling Arrays.hashCode(Object[]). 
     *
     * @return A hash value of the sequence of property values.
     */
    @Override
    public int hashCode() {
        return Objects.hash(totalAssets, riskAssetsRatio, losscutRange, riskRewardRatio, historicalVolatility, liquidity);
    }

    /**
     * Returns true if the all properties are equal to each other and false otherwise. Consequently, if both properties are null, true is returned and if exactly one property is null, false is returned. Otherwise, equality is determined by using the equals method of the base model. 
     *
     * @return true if the all properties are equal to each other and false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof FundManager == false) {
            return false;
        }

        FundManager other = (FundManager) o;
        if (!Objects.equals(totalAssets, other.totalAssets)) return false;
        if (riskAssetsRatio != other.riskAssetsRatio) return false;
        if (!Objects.equals(losscutRange, other.losscutRange)) return false;
        if (riskRewardRatio != other.riskRewardRatio) return false;
        if (!Objects.equals(historicalVolatility, other.historicalVolatility)) return false;
        if (!Objects.equals(liquidity, other.liquidity)) return false;
        return true;
    }

    /**
     * Create new {@link FundManager} with the specified property and copy other properties from this model.
     *
     * @param value A new value to assign.
     * @return A created new model instance.
     */
    public FundManager withLosscutRange(Num value) {
        if (this.losscutRange == value) {
            return this;
        }
        return with.totalAssets(this.totalAssets).riskAssetsRatio(this.riskAssetsRatio).losscutRange(value).riskRewardRatio(this.riskRewardRatio).historicalVolatility(this.historicalVolatility).liquidity(this.liquidity);
    }

    /**
     * Create new {@link FundManager} with the specified property and copy other properties from this model.
     *
     * @param value A new value to assign.
     * @return A created new model instance.
     */
    public FundManager withRiskRewardRatio(double value) {
        if (this.riskRewardRatio == value) {
            return this;
        }
        return with.totalAssets(this.totalAssets).riskAssetsRatio(this.riskAssetsRatio).losscutRange(this.losscutRange).riskRewardRatio(value).historicalVolatility(this.historicalVolatility).liquidity(this.liquidity);
    }

    /** The singleton builder. */
    public static final  Ìnstantiator<?> with = new Ìnstantiator();

    /**
     * Namespace for {@link FundManager}  builder methods.
     */
    public static class Ìnstantiator<Self extends FundManager & ÅssignableÅrbitrary<Self>> {

        /**
         * Create new {@link FundManager} with the specified totalAssets property.
         * 
         * @return The next assignable model.
         */
        public Self totalAssets(Num totalAssets) {
            Åssignable o = new Åssignable();
            o.totalAssets(totalAssets);
            return (Self)o;
        }

        /**
         * Create new {@link FundManager} with the specified totalAssets property.
         * 
         * @return The next assignable model.
         */
        public Self totalAssets(long value) {
            Åssignable o = new Åssignable();
            o.totalAssets(value);
            return (Self)o;
        }

        /**
         * Create new {@link FundManager} with the specified totalAssets property.
         * 
         * @return The next assignable model.
         */
        public Self totalAssets(double value) {
            Åssignable o = new Åssignable();
            o.totalAssets(value);
            return (Self)o;
        }
    }

    /**
     * Property assignment API.
     */
    public static interface ÅssignableTotalAssets<Next> {

        /**
         * Assign totalAssets property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next totalAssets(Num value) {
            ((FundManager) this).setTotalAssets(value);
            return (Next) this;
        }

        /**
         * Assign totalAssets property.
         * 
         * @return The next assignable model.
         */
        default Next totalAssets(long value) {
            try {
                return totalAssets((Num) totalAssets$1093866057.invoke(this, value));
            } catch (Throwable e) {
                throw quiet(e);
            }
        }

        /**
         * Assign totalAssets property.
         * 
         * @return The next assignable model.
         */
        default Next totalAssets(double value) {
            try {
                return totalAssets((Num) totalAssets$912239839.invoke(this, value));
            } catch (Throwable e) {
                throw quiet(e);
            }
        }
    }

    /**
     * Property assignment API.
     */
    public static interface ÅssignableÅrbitrary<Next extends FundManager> {

        /**
         * Assign riskAssetsRatio property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next riskAssetsRatio(double value) {
            ((FundManager) this).setRiskAssetsRatio(value);
            return (Next) this;
        }

        /**
         * Assign losscutRange property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next losscutRange(Num value) {
            ((FundManager) this).setLosscutRange(value);
            return (Next) this;
        }

        /**
         * Assign riskRewardRatio property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next riskRewardRatio(double value) {
            ((FundManager) this).setRiskRewardRatio(value);
            return (Next) this;
        }

        /**
         * Assign historicalVolatility property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next historicalVolatility(Num value) {
            ((FundManager) this).setHistoricalVolatility(value);
            return (Next) this;
        }

        /**
         * Assign liquidity property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next liquidity(Num value) {
            ((FundManager) this).setLiquidity(value);
            return (Next) this;
        }
    }

    /**
     * Internal aggregated API.
     */
    protected static interface ÅssignableAll extends ÅssignableTotalAssets {
    }

    /**
     * Mutable Model.
     */
    private static final class Åssignable extends FundManager implements ÅssignableAll, ÅssignableÅrbitrary {
    }

    /**
     * The identifier for properties.
     */
    static final class My {
        static final String TotalAssets = "totalAssets";
        static final String RiskAssetsRatio = "riskAssetsRatio";
        static final String LosscutRange = "losscutRange";
        static final String RiskRewardRatio = "riskRewardRatio";
        static final String HistoricalVolatility = "historicalVolatility";
        static final String Liquidity = "liquidity";
    }
}
