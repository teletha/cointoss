package cointoss;

import cointoss.MarketSetting;
import cointoss.MarketSettingModel;
import cointoss.execution.ExecutionLogger;
import cointoss.util.Num;
import cointoss.util.Retry;
import java.lang.Class;
import java.lang.Override;
import java.lang.StringBuilder;
import java.lang.Throwable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.processing.Generated;

/**
 * Generated model for {@link MarketSettingModel}.
 */
@Generated("Icy Manipulator")
public class MarketSetting implements MarketSettingModel {

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
            Method method = MarketSettingModel.class.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
            return MethodHandles.lookup().unreflect(method);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /** The overload or intercept method invoker. */
    private static final MethodHandle deriveByMinBid$473121199= invoker("deriveByMinBid", Num.class, Consumer.class);

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
    private static final MethodHandle baseCurrencyMinimumBidPriceUpdater = updater("baseCurrencyMinimumBidPrice");

    /** The final property updater. */
    private static final MethodHandle targetCurrencyMinimumBidSizeUpdater = updater("targetCurrencyMinimumBidSize");

    /** The final property updater. */
    private static final MethodHandle orderBookGroupRangesUpdater = updater("orderBookGroupRanges");

    /** The final property updater. */
    private static final MethodHandle targetCurrencyBidSizesUpdater = updater("targetCurrencyBidSizes");

    /** The final property updater. */
    private static final MethodHandle baseCurrencyScaleSizeUpdater = updater("baseCurrencyScaleSize");

    /** The final property updater. */
    private static final MethodHandle targetCurrencyScaleSizeUpdater = updater("targetCurrencyScaleSize");

    /** The final property updater. */
    private static final MethodHandle acquirableExecutionSizeUpdater = updater("acquirableExecutionSize");

    /** The final property updater. */
    private static final MethodHandle executionLoggerUpdater = updater("executionLogger");

    /** The final property updater. */
    private static final MethodHandle executionWithSequentialIdUpdater = updater("executionWithSequentialId");

    /** The final property updater. */
    private static final MethodHandle retryPolicyUpdater = updater("retryPolicy");

    /** The exposed property. */
    public final Num baseCurrencyMinimumBidPrice;

    /** The exposed property. */
    public final Num targetCurrencyMinimumBidSize;

    /** The exposed property. */
    public final Num[] orderBookGroupRanges;

    /** The exposed property. */
    public final List<Num> targetCurrencyBidSizes;

    /** The exposed property. */
    public final int baseCurrencyScaleSize;

    /** The exposed property. */
    public final int targetCurrencyScaleSize;

    /** The exposed property. */
    public final int acquirableExecutionSize;

    /** The exposed property. */
    public final Class<? extends ExecutionLogger> executionLogger;

    /** The exposed property. */
    public final boolean executionWithSequentialId;

    /** The exposed property. */
    public final Retry retryPolicy;

    /**
     * HIDE CONSTRUCTOR
     */
    protected MarketSetting() {
        this.baseCurrencyMinimumBidPrice = null;
        this.targetCurrencyMinimumBidSize = null;
        this.orderBookGroupRanges = null;
        this.targetCurrencyBidSizes = MarketSettingModel.super.targetCurrencyBidSizes();
        this.baseCurrencyScaleSize = MarketSettingModel.super.baseCurrencyScaleSize();
        this.targetCurrencyScaleSize = MarketSettingModel.super.targetCurrencyScaleSize();
        this.acquirableExecutionSize = MarketSettingModel.super.acquirableExecutionSize();
        this.executionLogger = MarketSettingModel.super.executionLogger();
        this.executionWithSequentialId = MarketSettingModel.super.executionWithSequentialId();
        this.retryPolicy = MarketSettingModel.super.retryPolicy();
    }

    /** Get the minimum bid price of the base currency. */
    @Override
    public final Num baseCurrencyMinimumBidPrice() {
        return this.baseCurrencyMinimumBidPrice;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of baseCurrencyMinimumBidPrice property.
     */
    @SuppressWarnings("unused")
    private final Num getBaseCurrencyMinimumBidPrice() {
        return this.baseCurrencyMinimumBidPrice;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of baseCurrencyMinimumBidPrice property to assign.
     */
    private final void setBaseCurrencyMinimumBidPrice(Num value) {
        if (value == null) {
            throw new IllegalArgumentException("The baseCurrencyMinimumBidPrice property requires non-null value.");
        }
        try {
            baseCurrencyMinimumBidPriceUpdater.invoke(this, value);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /** Get the minimum bid size of the target currency. */
    @Override
    public final Num targetCurrencyMinimumBidSize() {
        return this.targetCurrencyMinimumBidSize;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of targetCurrencyMinimumBidSize property.
     */
    @SuppressWarnings("unused")
    private final Num getTargetCurrencyMinimumBidSize() {
        return this.targetCurrencyMinimumBidSize;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of targetCurrencyMinimumBidSize property to assign.
     */
    private final void setTargetCurrencyMinimumBidSize(Num value) {
        if (value == null) {
            throw new IllegalArgumentException("The targetCurrencyMinimumBidSize property requires non-null value.");
        }
        try {
            targetCurrencyMinimumBidSizeUpdater.invoke(this, deriveByMinBid$473121199.invoke(this, value, (Consumer<List<Num>>) ((Åssignable) this)::targetCurrencyBidSizes));
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /** Get the price range of grouped order books. */
    @Override
    public final Num[] orderBookGroupRanges() {
        return this.orderBookGroupRanges;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of orderBookGroupRanges property.
     */
    @SuppressWarnings("unused")
    private final Num[] getOrderBookGroupRanges() {
        return this.orderBookGroupRanges;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of orderBookGroupRanges property to assign.
     */
    private final void setOrderBookGroupRanges(Num[] value) {
        if (value == null) {
            throw new IllegalArgumentException("The orderBookGroupRanges property requires non-null value.");
        }
        try {
            orderBookGroupRangesUpdater.invoke(this, value);
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

    /** Get the human readable size of base currency. */
    @Override
    public final int baseCurrencyScaleSize() {
        return this.baseCurrencyScaleSize;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of baseCurrencyScaleSize property.
     */
    @SuppressWarnings("unused")
    private final int getBaseCurrencyScaleSize() {
        return this.baseCurrencyScaleSize;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of baseCurrencyScaleSize property to assign.
     */
    private final void setBaseCurrencyScaleSize(int value) {
        try {
            baseCurrencyScaleSizeUpdater.invoke(this, value);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /** Get the human readable size of target currency. */
    @Override
    public final int targetCurrencyScaleSize() {
        return this.targetCurrencyScaleSize;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of targetCurrencyScaleSize property.
     */
    @SuppressWarnings("unused")
    private final int getTargetCurrencyScaleSize() {
        return this.targetCurrencyScaleSize;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of targetCurrencyScaleSize property to assign.
     */
    private final void setTargetCurrencyScaleSize(int value) {
        try {
            targetCurrencyScaleSizeUpdater.invoke(this, value);
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
     * Configure {@link ExecutionLog} parser.
     *  
     *  @return
     */
    @Override
    public final boolean executionWithSequentialId() {
        return this.executionWithSequentialId;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of executionWithSequentialId property.
     */
    @SuppressWarnings("unused")
    private final boolean getExecutionWithSequentialId() {
        return this.executionWithSequentialId;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of executionWithSequentialId property to assign.
     */
    private final void setExecutionWithSequentialId(boolean value) {
        try {
            executionWithSequentialIdUpdater.invoke(this, value);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Configure {@link RetryPolicy}.
     *  
     *  @return
     */
    @Override
    public final Retry retryPolicy() {
        return this.retryPolicy;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of retryPolicy property.
     */
    @SuppressWarnings("unused")
    private final Retry getRetryPolicy() {
        return this.retryPolicy;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of retryPolicy property to assign.
     */
    private final void setRetryPolicy(Retry value) {
        if (value == null) {
            value = MarketSettingModel.super.retryPolicy();
        }
        try {
            retryPolicyUpdater.invoke(this, value);
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
        builder.append("baseCurrencyMinimumBidPrice=").append(baseCurrencyMinimumBidPrice).append(", ");
        builder.append("targetCurrencyMinimumBidSize=").append(targetCurrencyMinimumBidSize).append(", ");
        builder.append("orderBookGroupRanges=").append(orderBookGroupRanges).append(", ");
        builder.append("targetCurrencyBidSizes=").append(targetCurrencyBidSizes).append(", ");
        builder.append("baseCurrencyScaleSize=").append(baseCurrencyScaleSize).append(", ");
        builder.append("targetCurrencyScaleSize=").append(targetCurrencyScaleSize).append(", ");
        builder.append("acquirableExecutionSize=").append(acquirableExecutionSize).append(", ");
        builder.append("executionLogger=").append(executionLogger).append(", ");
        builder.append("executionWithSequentialId=").append(executionWithSequentialId).append(", ");
        builder.append("retryPolicy=").append(retryPolicy).append("]");
        return builder.toString();
    }

    /**
     * Generates a hash code for a sequence of property values. The hash code is generated as if all the property values were placed into an array, and that array were hashed by calling Arrays.hashCode(Object[]). 
     *
     * @return A hash value of the sequence of property values.
     */
    @Override
    public int hashCode() {
        return Objects.hash(baseCurrencyMinimumBidPrice, targetCurrencyMinimumBidSize, orderBookGroupRanges, targetCurrencyBidSizes, baseCurrencyScaleSize, targetCurrencyScaleSize, acquirableExecutionSize, executionLogger, executionWithSequentialId, retryPolicy);
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
        if (!Objects.equals(baseCurrencyMinimumBidPrice, other.baseCurrencyMinimumBidPrice)) return false;
        if (!Objects.equals(targetCurrencyMinimumBidSize, other.targetCurrencyMinimumBidSize)) return false;
        if (!Objects.equals(orderBookGroupRanges, other.orderBookGroupRanges)) return false;
        if (!Objects.equals(targetCurrencyBidSizes, other.targetCurrencyBidSizes)) return false;
        if (baseCurrencyScaleSize != other.baseCurrencyScaleSize) return false;
        if (targetCurrencyScaleSize != other.targetCurrencyScaleSize) return false;
        if (acquirableExecutionSize != other.acquirableExecutionSize) return false;
        if (!Objects.equals(executionLogger, other.executionLogger)) return false;
        if (executionWithSequentialId != other.executionWithSequentialId) return false;
        if (!Objects.equals(retryPolicy, other.retryPolicy)) return false;
        return true;
    }

    /**
     * Create new {@link MarketSetting} with the specified property and copy other properties from this model.
     *
     * @param value A new value to assign.
     * @return A created new model instance.
     */
    public MarketSetting withRetryPolicy(Retry value) {
        if (this.retryPolicy == value) {
            return this;
        }
        return with.baseCurrencyMinimumBidPrice(this.baseCurrencyMinimumBidPrice).targetCurrencyMinimumBidSize(this.targetCurrencyMinimumBidSize).orderBookGroupRanges(this.orderBookGroupRanges).targetCurrencyBidSizes(this.targetCurrencyBidSizes).baseCurrencyScaleSize(this.baseCurrencyScaleSize).targetCurrencyScaleSize(this.targetCurrencyScaleSize).acquirableExecutionSize(this.acquirableExecutionSize).executionLogger(this.executionLogger).executionWithSequentialId(this.executionWithSequentialId).retryPolicy(value);
    }

    /** The singleton builder. */
    public static final  Ìnstantiator<?> with = new Ìnstantiator();

    /**
     * Namespace for {@link MarketSetting}  builder methods.
     */
    public static class Ìnstantiator<Self extends MarketSetting & ÅssignableÅrbitrary<Self>> {

        /**
         * Create new {@link MarketSetting} with the specified baseCurrencyMinimumBidPrice property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableTargetCurrencyMinimumBidSize<ÅssignableOrderBookGroupRanges<Self>> baseCurrencyMinimumBidPrice(Num baseCurrencyMinimumBidPrice) {
            Åssignable o = new Åssignable();
            o.baseCurrencyMinimumBidPrice(baseCurrencyMinimumBidPrice);
            return o;
        }
    }

    /**
     * Property assignment API.
     */
    public static interface ÅssignableBaseCurrencyMinimumBidPrice<Next> {

        /**
         * Assign baseCurrencyMinimumBidPrice property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next baseCurrencyMinimumBidPrice(Num value) {
            ((MarketSetting) this).setBaseCurrencyMinimumBidPrice(value);
            return (Next) this;
        }
    }

    /**
     * Property assignment API.
     */
    public static interface ÅssignableTargetCurrencyMinimumBidSize<Next> {

        /**
         * Assign targetCurrencyMinimumBidSize property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next targetCurrencyMinimumBidSize(Num value) {
            ((MarketSetting) this).setTargetCurrencyMinimumBidSize(value);
            return (Next) this;
        }
    }

    /**
     * Property assignment API.
     */
    public static interface ÅssignableOrderBookGroupRanges<Next> {

        /**
         * Assign orderBookGroupRanges property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next orderBookGroupRanges(Num... value) {
            ((MarketSetting) this).setOrderBookGroupRanges(value);
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
         * Assign baseCurrencyScaleSize property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next baseCurrencyScaleSize(int value) {
            ((MarketSetting) this).setBaseCurrencyScaleSize(value);
            return (Next) this;
        }

        /**
         * Assign targetCurrencyScaleSize property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next targetCurrencyScaleSize(int value) {
            ((MarketSetting) this).setTargetCurrencyScaleSize(value);
            return (Next) this;
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

        /**
         * Assign executionWithSequentialId property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next executionWithSequentialId(boolean value) {
            ((MarketSetting) this).setExecutionWithSequentialId(value);
            return (Next) this;
        }

        /**
         * Assign retryPolicy property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next retryPolicy(Retry value) {
            ((MarketSetting) this).setRetryPolicy(value);
            return (Next) this;
        }
    }

    /**
     * Internal aggregated API.
     */
    protected static interface ÅssignableAll extends ÅssignableBaseCurrencyMinimumBidPrice, ÅssignableTargetCurrencyMinimumBidSize, ÅssignableOrderBookGroupRanges {
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
        static final String BaseCurrencyMinimumBidPrice = "baseCurrencyMinimumBidPrice";
        static final String TargetCurrencyMinimumBidSize = "targetCurrencyMinimumBidSize";
        static final String OrderBookGroupRanges = "orderBookGroupRanges";
        static final String TargetCurrencyBidSizes = "targetCurrencyBidSizes";
        static final String BaseCurrencyScaleSize = "baseCurrencyScaleSize";
        static final String TargetCurrencyScaleSize = "targetCurrencyScaleSize";
        static final String AcquirableExecutionSize = "acquirableExecutionSize";
        static final String ExecutionLogger = "executionLogger";
        static final String ExecutionWithSequentialId = "executionWithSequentialId";
        static final String RetryPolicy = "retryPolicy";
    }
}
