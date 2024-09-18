package cointoss;

import cointoss.MarketSetting;
import cointoss.execution.ExecutionLogger;
import hypatia.Num;
import java.lang.Class;
import java.lang.Override;
import java.lang.StringBuilder;
import java.lang.Throwable;
import java.lang.UnsupportedOperationException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * Generated model for {@link MarketSettingModel}.
 * 
 * @see <a href="https://github.com/teletha/icymanipulator">Icy Manipulator (Code Generator)</a>
 */
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
    private static final MethodHandle typeUpdater = updater("type");

    /** The final property updater. */
    private static final MethodHandle targetUpdater = updater("target");

    /** The final property updater. */
    private static final MethodHandle baseUpdater = updater("base");

    /** The final property updater. */
    private static final MethodHandle targetCurrencyBidSizesUpdater = updater("targetCurrencyBidSizes");

    /** The final property updater. */
    private static final MethodHandle priceRangeModifierUpdater = updater("priceRangeModifier");

    /** The final property updater. */
    private static final MethodHandle orderbookMaxSizeUpdater = updater("orderbookMaxSize");

    /** The final property updater. */
    private static final MethodHandle acquirableExecutionSizeUpdater = updater("acquirableExecutionSize");

    /** The final property updater. */
    private static final MethodHandle acquirableExecutionBulkModifierUpdater = updater("acquirableExecutionBulkModifier");

    /** The final property updater. */
    private static final MethodHandle executionLoggerUpdater = updater("executionLogger");

    /** The final property updater. */
    private static final MethodHandle takerFeeUpdater = updater("takerFee");

    /** The final property updater. */
    private static final MethodHandle makerFeeUpdater = updater("makerFee");

    /** The final property updater. */
    private static final MethodHandle targetWithdrawingFeeUpdater = updater("targetWithdrawingFee");

    /** The final property updater. */
    private static final MethodHandle baseWithdrawingFeeUpdater = updater("baseWithdrawingFee");

    /** The exposed property. */
    public final MarketType type;

    /** The exposed property. */
    public final CurrencySetting target;

    /** The exposed property. */
    public final cointoss.CurrencySetting base;

    /** The exposed property. */
    public final List<Num> targetCurrencyBidSizes;

    /** The exposed property. */
    public final int priceRangeModifier;

    /** The exposed property. */
    public final int orderbookMaxSize;

    /** The exposed property. */
    public final int acquirableExecutionSize;

    /** The exposed property. */
    public final int acquirableExecutionBulkModifier;

    /** The exposed property. */
    public final Class<? extends ExecutionLogger> executionLogger;

    /** The exposed property. */
    public final UnaryOperator<Num> takerFee;

    /** The exposed property. */
    public final UnaryOperator<Num> makerFee;

    /** The exposed property. */
    public final UnaryOperator<Num> targetWithdrawingFee;

    /** The exposed property. */
    public final UnaryOperator<Num> baseWithdrawingFee;

    /**
     * HIDE CONSTRUCTOR
     */
    protected MarketSetting() {
        this.type = null;
        this.target = null;
        this.base = null;
        this.targetCurrencyBidSizes = cointoss.MarketSettingModel.super.targetCurrencyBidSizes();
        this.priceRangeModifier = cointoss.MarketSettingModel.super.priceRangeModifier();
        this.orderbookMaxSize = cointoss.MarketSettingModel.super.orderbookMaxSize();
        this.acquirableExecutionSize = cointoss.MarketSettingModel.super.acquirableExecutionSize();
        this.acquirableExecutionBulkModifier = cointoss.MarketSettingModel.super.acquirableExecutionBulkModifier();
        this.executionLogger = cointoss.MarketSettingModel.super.executionLogger();
        this.takerFee = cointoss.MarketSettingModel.super.takerFee();
        this.makerFee = cointoss.MarketSettingModel.super.makerFee();
        this.targetWithdrawingFee = cointoss.MarketSettingModel.super.targetWithdrawingFee();
        this.baseWithdrawingFee = cointoss.MarketSettingModel.super.baseWithdrawingFee();
    }

    /**
     * Sepcify the market type.
     *  
     *  @return
     */
    @Override
    public final cointoss.MarketType type() {
        return this.type;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of type property.
     */
    @SuppressWarnings("unused")
    private final cointoss.MarketType getType() {
        return this.type;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of type property to assign.
     */
    private final void setType(cointoss.MarketType value) {
        if (value == null) {
            throw new IllegalArgumentException("The type property requires non-null value.");
        }
        try {
            typeUpdater.invoke(this, value);
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Specify the target currency.
     *  
     *  @return
     */
    @Override
    public final cointoss.CurrencySetting target() {
        return this.target;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of target property.
     */
    @SuppressWarnings("unused")
    private final cointoss.CurrencySetting getTarget() {
        return this.target;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of target property to assign.
     */
    private final void setTarget(cointoss.CurrencySetting value) {
        if (value == null) {
            throw new IllegalArgumentException("The target property requires non-null value.");
        }
        try {
            targetUpdater.invoke(this, value);
        } catch (UnsupportedOperationException e) {
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
    public final cointoss.CurrencySetting base() {
        return this.base;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of base property.
     */
    @SuppressWarnings("unused")
    private final cointoss.CurrencySetting getBase() {
        return this.base;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of base property to assign.
     */
    private final void setBase(cointoss.CurrencySetting value) {
        if (value == null) {
            throw new IllegalArgumentException("The base property requires non-null value.");
        }
        try {
            baseUpdater.invoke(this, value);
        } catch (UnsupportedOperationException e) {
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
            value = cointoss.MarketSettingModel.super.targetCurrencyBidSizes();
        }
        try {
            targetCurrencyBidSizesUpdater.invoke(this, value);
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /** Get the price range modifier of base currency. */
    @Override
    public final int priceRangeModifier() {
        return this.priceRangeModifier;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of priceRangeModifier property.
     */
    @SuppressWarnings("unused")
    private final int getPriceRangeModifier() {
        return this.priceRangeModifier;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of priceRangeModifier property to assign.
     */
    private final void setPriceRangeModifier(int value) {
        try {
            priceRangeModifierUpdater.invoke(this, value);
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /** Get the maximum orderbook size in one side. */
    @Override
    public final int orderbookMaxSize() {
        return this.orderbookMaxSize;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of orderbookMaxSize property.
     */
    @SuppressWarnings("unused")
    private final int getOrderbookMaxSize() {
        return this.orderbookMaxSize;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of orderbookMaxSize property to assign.
     */
    private final void setOrderbookMaxSize(int value) {
        try {
            orderbookMaxSizeUpdater.invoke(this, value);
        } catch (UnsupportedOperationException e) {
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
        } catch (UnsupportedOperationException e) {
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
    public final int acquirableExecutionBulkModifier() {
        return this.acquirableExecutionBulkModifier;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of acquirableExecutionBulkModifier property.
     */
    @SuppressWarnings("unused")
    private final int getAcquirableExecutionBulkModifier() {
        return this.acquirableExecutionBulkModifier;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of acquirableExecutionBulkModifier property to assign.
     */
    private final void setAcquirableExecutionBulkModifier(int value) {
        try {
            acquirableExecutionBulkModifierUpdater.invoke(this, value);
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Configure {@link ExecutionLogger} parser.
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
            value = cointoss.MarketSettingModel.super.executionLogger();
        }
        try {
            executionLoggerUpdater.invoke(this, value);
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /** Get the fee on taking order. */
    @Override
    public final UnaryOperator<Num> takerFee() {
        return this.takerFee;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of takerFee property.
     */
    @SuppressWarnings("unused")
    private final UnaryOperator<Num> getTakerFee() {
        return this.takerFee;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of takerFee property to assign.
     */
    private final void setTakerFee(UnaryOperator<Num> value) {
        if (value == null) {
            value = cointoss.MarketSettingModel.super.takerFee();
        }
        try {
            takerFeeUpdater.invoke(this, value);
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /** Get the fee on making order. */
    @Override
    public final UnaryOperator<Num> makerFee() {
        return this.makerFee;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of makerFee property.
     */
    @SuppressWarnings("unused")
    private final UnaryOperator<Num> getMakerFee() {
        return this.makerFee;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of makerFee property to assign.
     */
    private final void setMakerFee(UnaryOperator<Num> value) {
        if (value == null) {
            value = cointoss.MarketSettingModel.super.makerFee();
        }
        try {
            makerFeeUpdater.invoke(this, value);
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /** Get the fee on withdraw. */
    @Override
    public final UnaryOperator<Num> targetWithdrawingFee() {
        return this.targetWithdrawingFee;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of targetWithdrawingFee property.
     */
    @SuppressWarnings("unused")
    private final UnaryOperator<Num> getTargetWithdrawingFee() {
        return this.targetWithdrawingFee;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of targetWithdrawingFee property to assign.
     */
    private final void setTargetWithdrawingFee(UnaryOperator<Num> value) {
        if (value == null) {
            value = cointoss.MarketSettingModel.super.targetWithdrawingFee();
        }
        try {
            targetWithdrawingFeeUpdater.invoke(this, value);
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /** Get the fee on withdraw. */
    @Override
    public final UnaryOperator<Num> baseWithdrawingFee() {
        return this.baseWithdrawingFee;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of baseWithdrawingFee property.
     */
    @SuppressWarnings("unused")
    private final UnaryOperator<Num> getBaseWithdrawingFee() {
        return this.baseWithdrawingFee;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of baseWithdrawingFee property to assign.
     */
    private final void setBaseWithdrawingFee(UnaryOperator<Num> value) {
        if (value == null) {
            value = cointoss.MarketSettingModel.super.baseWithdrawingFee();
        }
        try {
            baseWithdrawingFeeUpdater.invoke(this, value);
        } catch (UnsupportedOperationException e) {
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
        builder.append("type=").append(type).append(", ");
        builder.append("target=").append(target).append(", ");
        builder.append("base=").append(base).append(", ");
        builder.append("targetCurrencyBidSizes=").append(targetCurrencyBidSizes).append(", ");
        builder.append("priceRangeModifier=").append(priceRangeModifier).append(", ");
        builder.append("orderbookMaxSize=").append(orderbookMaxSize).append(", ");
        builder.append("acquirableExecutionSize=").append(acquirableExecutionSize).append(", ");
        builder.append("acquirableExecutionBulkModifier=").append(acquirableExecutionBulkModifier).append(", ");
        builder.append("executionLogger=").append(executionLogger).append(", ");
        builder.append("takerFee=").append(takerFee).append(", ");
        builder.append("makerFee=").append(makerFee).append(", ");
        builder.append("targetWithdrawingFee=").append(targetWithdrawingFee).append(", ");
        builder.append("baseWithdrawingFee=").append(baseWithdrawingFee).append("]");
        return builder.toString();
    }

    /**
     * Generates a hash code for a sequence of property values. The hash code is generated as if all the property values were placed into an array, and that array were hashed by calling Arrays.hashCode(Object[]). 
     *
     * @return A hash value of the sequence of property values.
     */
    @Override
    public int hashCode() {
        return Objects.hash(type, target, base, targetCurrencyBidSizes, priceRangeModifier, orderbookMaxSize, acquirableExecutionSize, acquirableExecutionBulkModifier, executionLogger, takerFee, makerFee, targetWithdrawingFee, baseWithdrawingFee);
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
        if (!Objects.equals(type, other.type)) return false;
        if (!Objects.equals(target, other.target)) return false;
        if (!Objects.equals(base, other.base)) return false;
        if (!Objects.equals(targetCurrencyBidSizes, other.targetCurrencyBidSizes)) return false;
        if (priceRangeModifier != other.priceRangeModifier) return false;
        if (orderbookMaxSize != other.orderbookMaxSize) return false;
        if (acquirableExecutionSize != other.acquirableExecutionSize) return false;
        if (acquirableExecutionBulkModifier != other.acquirableExecutionBulkModifier) return false;
        if (!Objects.equals(executionLogger, other.executionLogger)) return false;
        if (!Objects.equals(takerFee, other.takerFee)) return false;
        if (!Objects.equals(makerFee, other.makerFee)) return false;
        if (!Objects.equals(targetWithdrawingFee, other.targetWithdrawingFee)) return false;
        if (!Objects.equals(baseWithdrawingFee, other.baseWithdrawingFee)) return false;
        return true;
    }

    /** The singleton builder. */
    public static final  Ìnstantiator<?> with = new Ìnstantiator();

    /**
     * Namespace for {@link MarketSetting}  builder methods.
     */
    public static class Ìnstantiator<Self extends MarketSetting & ÅssignableÅrbitrary<Self>> {

        /**
         * Create new {@link MarketSetting} with the specified type property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableTarget<ÅssignableBase<Self>> type(cointoss.MarketType type) {
            Åssignable o = new Åssignable();
            o.type(type);
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified type property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableTarget<ÅssignableBase<Self>> derivative() {
            Åssignable o = new Åssignable();
            o.derivative();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified type property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableTarget<ÅssignableBase<Self>> future() {
            Åssignable o = new Åssignable();
            o.future();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified type property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableTarget<ÅssignableBase<Self>> spot() {
            Åssignable o = new Åssignable();
            o.spot();
            return o;
        }
    }

    /**
     * Property assignment API.
     */
    public static interface ÅssignableType<Next> {

        /**
         * Assign type property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next type(cointoss.MarketType value) {
            ((MarketSetting) this).setType(value);
            return (Next) this;
        }

        /**
         * Assign type property.
         * 
         * @return The next assignable model.
         */
        default Next derivative() {
            return type(cointoss.MarketType.DERIVATIVE);
        }

        /**
         * Assign type property.
         * 
         * @return The next assignable model.
         */
        default Next future() {
            return type(cointoss.MarketType.FUTURE);
        }

        /**
         * Assign type property.
         * 
         * @return The next assignable model.
         */
        default Next spot() {
            return type(cointoss.MarketType.SPOT);
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
        default Next target(cointoss.CurrencySetting value) {
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
        default Next base(cointoss.CurrencySetting value) {
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
        default Next targetCurrencyBidSizes(List<? extends Num> value) {
            ((MarketSetting) this).setTargetCurrencyBidSizes((java.util.List)value);
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
         * Assign priceRangeModifier property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next priceRangeModifier(int value) {
            ((MarketSetting) this).setPriceRangeModifier(value);
            return (Next) this;
        }

        /**
         * Assign orderbookMaxSize property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next orderbookMaxSize(int value) {
            ((MarketSetting) this).setOrderbookMaxSize(value);
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
         * Assign acquirableExecutionBulkModifier property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next acquirableExecutionBulkModifier(int value) {
            ((MarketSetting) this).setAcquirableExecutionBulkModifier(value);
            return (Next) this;
        }

        /**
         * Assign executionLogger property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next executionLogger(Class<? extends ExecutionLogger> value) {
            ((MarketSetting) this).setExecutionLogger((java.lang.Class)value);
            return (Next) this;
        }

        /**
         * Assign takerFee property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next takerFee(UnaryOperator<? extends Num> value) {
            ((MarketSetting) this).setTakerFee((java.util.function.UnaryOperator)value);
            return (Next) this;
        }

        /**
         * Assign makerFee property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next makerFee(UnaryOperator<? extends Num> value) {
            ((MarketSetting) this).setMakerFee((java.util.function.UnaryOperator)value);
            return (Next) this;
        }

        /**
         * Assign targetWithdrawingFee property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next targetWithdrawingFee(UnaryOperator<? extends Num> value) {
            ((MarketSetting) this).setTargetWithdrawingFee((java.util.function.UnaryOperator)value);
            return (Next) this;
        }

        /**
         * Assign baseWithdrawingFee property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next baseWithdrawingFee(UnaryOperator<? extends Num> value) {
            ((MarketSetting) this).setBaseWithdrawingFee((java.util.function.UnaryOperator)value);
            return (Next) this;
        }
    }

    /**
     * Internal aggregated API.
     */
    protected static interface ÅssignableAll extends ÅssignableType, ÅssignableTarget, ÅssignableBase {
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
        static final String Type = "type";
        static final String Target = "target";
        static final String Base = "base";
        static final String TargetCurrencyBidSizes = "targetCurrencyBidSizes";
        static final String PriceRangeModifier = "priceRangeModifier";
        static final String OrderbookMaxSize = "orderbookMaxSize";
        static final String AcquirableExecutionSize = "acquirableExecutionSize";
        static final String AcquirableExecutionBulkModifier = "acquirableExecutionBulkModifier";
        static final String ExecutionLogger = "executionLogger";
        static final String TakerFee = "takerFee";
        static final String MakerFee = "makerFee";
        static final String TargetWithdrawingFee = "targetWithdrawingFee";
        static final String BaseWithdrawingFee = "baseWithdrawingFee";
    }
}
