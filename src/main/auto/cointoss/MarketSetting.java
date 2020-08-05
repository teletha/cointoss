package cointoss;

import cointoss.CurrencySetting;
import cointoss.MarketSetting;
import cointoss.MarketSettingModel;
import cointoss.execution.ExecutionLogger;
import cointoss.util.Num;
import java.lang.Class;
import java.lang.Override;
import java.lang.StringBuilder;
import java.lang.Throwable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import javax.annotation.processing.Generated;

/**
 * Generated model for {@link MarketSettingModel}.
 */
@Generated("Icy Manipulator")
public abstract class MarketSetting implements MarketSettingModel {

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
     * Create special property updater.
     *
     * @param name A target property name.
     * @return A special property updater.
     */
    private static final MethodHandle updater(String name)  {
        try {
            Field field = MarketSetting.class.getDeclaredField(name);
            field.setAccessible(true);
            return MethodHandles.lookup().unreflectSetter(field);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /** The final property updater. */
    private static final MethodHandle targetUpdater = updater("target");

    /** The final property updater. */
    private static final MethodHandle baseUpdater = updater("base");

    /** The final property updater. */
    private static final MethodHandle targetCurrencyBidSizesUpdater = updater("targetCurrencyBidSizes");

    /** The final property updater. */
    private static final MethodHandle acquirableExecutionSizeUpdater = updater("acquirableExecutionSize");

    /** The final property updater. */
    private static final MethodHandle executionLoggerUpdater = updater("executionLogger");

    /** The exposed property. */
    public final CurrencySetting target;

    /** The exposed property. */
    public final CurrencySetting base;

    /** The exposed property. */
    public final List<Num> targetCurrencyBidSizes;

    /** The exposed property. */
    public final int acquirableExecutionSize;

    /** The exposed property. */
    public final Class<? extends ExecutionLogger> executionLogger;

    /**
     * HIDE CONSTRUCTOR
     */
    protected MarketSetting() {
        this.target = null;
        this.base = null;
        this.targetCurrencyBidSizes = MarketSettingModel.super.targetCurrencyBidSizes();
        this.acquirableExecutionSize = MarketSettingModel.super.acquirableExecutionSize();
        this.executionLogger = MarketSettingModel.super.executionLogger();
    }

    /**
     * Specify the target currency.
     *  
     *  @return
     */
    @Override
    public final CurrencySetting target() {
        return this.target;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of target property.
     */
    @SuppressWarnings("unused")
    private final CurrencySetting getTarget() {
        return this.target;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of target property to assign.
     */
    private final void setTarget(CurrencySetting value) {
        if (value == null) {
            throw new IllegalArgumentException("The target property requires non-null value.");
        }
        try {
            targetUpdater.invoke(this, value);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Specify the base currency.
     *  
     *  @return
     */
    @Override
    public final CurrencySetting base() {
        return this.base;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of base property.
     */
    @SuppressWarnings("unused")
    private final CurrencySetting getBase() {
        return this.base;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of base property to assign.
     */
    private final void setBase(CurrencySetting value) {
        if (value == null) {
            throw new IllegalArgumentException("The base property requires non-null value.");
        }
        try {
            baseUpdater.invoke(this, value);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /** Get the bid size range of target currency. */
    @Override
    public final List<Num> targetCurrencyBidSizes() {
        return this.targetCurrencyBidSizes;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of targetCurrencyBidSizes property.
     */
    @SuppressWarnings("unused")
    private final List<Num> getTargetCurrencyBidSizes() {
        return this.targetCurrencyBidSizes;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of targetCurrencyBidSizes property to assign.
     */
    private final void setTargetCurrencyBidSizes(List<Num> value) {
        if (value == null) {
            value = MarketSettingModel.super.targetCurrencyBidSizes();
        }
        try {
            targetCurrencyBidSizesUpdater.invoke(this, value);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Configure max acquirable execution size per one request.
     *  
     *  @return
     */
    @Override
    public final int acquirableExecutionSize() {
        return this.acquirableExecutionSize;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of acquirableExecutionSize property.
     */
    @SuppressWarnings("unused")
    private final int getAcquirableExecutionSize() {
        return this.acquirableExecutionSize;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of acquirableExecutionSize property to assign.
     */
    private final void setAcquirableExecutionSize(int value) {
        try {
            acquirableExecutionSizeUpdater.invoke(this, value);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Configure {@link ExecutionLog} parser.
     *  
     *  @return
     */
    @Override
    public final Class<? extends ExecutionLogger> executionLogger() {
        return this.executionLogger;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of executionLogger property.
     */
    @SuppressWarnings("unused")
    private final Class<? extends ExecutionLogger> getExecutionLogger() {
        return this.executionLogger;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of executionLogger property to assign.
     */
    private final void setExecutionLogger(Class<? extends ExecutionLogger> value) {
        if (value == null) {
            value = MarketSettingModel.super.executionLogger();
        }
        try {
            executionLoggerUpdater.invoke(this, value);
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
        StringBuilder builder = new StringBuilder("MarketSetting [");
        builder.append("target=").append(target).append(", ");
        builder.append("base=").append(base).append(", ");
        builder.append("targetCurrencyBidSizes=").append(targetCurrencyBidSizes).append(", ");
        builder.append("acquirableExecutionSize=").append(acquirableExecutionSize).append(", ");
        builder.append("executionLogger=").append(executionLogger).append("]");
        return builder.toString();
    }

    /**
     * Generates a hash code for a sequence of property values. The hash code is generated as if all the property values were placed into an array, and that array were hashed by calling Arrays.hashCode(Object[]). 
     *
     * @return A hash value of the sequence of property values.
     */
    @Override
    public int hashCode() {
        return Objects.hash(target, base, targetCurrencyBidSizes, acquirableExecutionSize, executionLogger);
    }

    /**
     * Returns true if the all properties are equal to each other and false otherwise. Consequently, if both properties are null, true is returned and if exactly one property is null, false is returned. Otherwise, equality is determined by using the equals method of the base model. 
     *
     * @return true if the all properties are equal to each other and false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof MarketSetting == false) {
            return false;
        }

        MarketSetting other = (MarketSetting) o;
        if (!Objects.equals(target, other.target)) return false;
        if (!Objects.equals(base, other.base)) return false;
        if (!Objects.equals(targetCurrencyBidSizes, other.targetCurrencyBidSizes)) return false;
        if (acquirableExecutionSize != other.acquirableExecutionSize) return false;
        if (!Objects.equals(executionLogger, other.executionLogger)) return false;
        return true;
    }

    /** The singleton builder. */
    public static final  Ìnstantiator<?> with = new Ìnstantiator();

    /**
     * Namespace for {@link MarketSetting}  builder methods.
     */
    public static class Ìnstantiator<Self extends MarketSetting & ÅssignableÅrbitrary<Self>> {

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBase<Self> target(CurrencySetting target) {
            Åssignable o = new Åssignable();
            o.target(target);
            return o;
        }
    }

    /**
     * Property assignment API.
     */
    public static interface ÅssignableTarget<Next> {

        /**
         * Assign target property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next target(CurrencySetting value) {
            ((MarketSetting) this).setTarget(value);
            return (Next) this;
        }
    }

    /**
     * Property assignment API.
     */
    public static interface ÅssignableBase<Next> {

        /**
         * Assign base property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next base(CurrencySetting value) {
            ((MarketSetting) this).setBase(value);
            return (Next) this;
        }
    }

    /**
     * Property assignment API.
     */
    public static interface ÅssignableÅrbitrary<Next extends MarketSetting> {

        /**
         * Assign targetCurrencyBidSizes property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next targetCurrencyBidSizes(List<Num> value) {
            ((MarketSetting) this).setTargetCurrencyBidSizes(value);
            return (Next) this;
        }

        /**
         * Assign targetCurrencyBidSizes property.
         * 
         * @return The next assignable model.
         */
        default Next targetCurrencyBidSizes(Num... values) {
            return targetCurrencyBidSizes(List.of(values));
        }

        /**
         * Assign acquirableExecutionSize property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next acquirableExecutionSize(int value) {
            ((MarketSetting) this).setAcquirableExecutionSize(value);
            return (Next) this;
        }

        /**
         * Assign executionLogger property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next executionLogger(Class<? extends ExecutionLogger> value) {
            ((MarketSetting) this).setExecutionLogger(value);
            return (Next) this;
        }
    }

    /**
     * Internal aggregated API.
     */
    protected static interface ÅssignableAll extends ÅssignableTarget, ÅssignableBase {
    }

    /**
     * Mutable Model.
     */
    private static final class Åssignable extends MarketSetting implements ÅssignableAll, ÅssignableÅrbitrary {
    }

    /**
     * The identifier for properties.
     */
    static final class My {
        static final String Target = "target";
        static final String Base = "base";
        static final String TargetCurrencyBidSizes = "targetCurrencyBidSizes";
        static final String AcquirableExecutionSize = "acquirableExecutionSize";
        static final String ExecutionLogger = "executionLogger";
    }
}
