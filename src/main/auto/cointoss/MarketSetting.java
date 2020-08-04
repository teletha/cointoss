package cointoss;

import cointoss.Currency;
import cointoss.MarketSetting;
import cointoss.MarketSettingModel;
import cointoss.execution.ExecutionLogger;
import cointoss.util.Num;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
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
    private static final MethodHandle baseCurrencyMinimumBidPrice$927011984= invoker("baseCurrencyMinimumBidPrice", String.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle targetCurrencyMinimumBidSize$927011984= invoker("targetCurrencyMinimumBidSize", String.class);

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
    private static final MethodHandle targetUpdater = updater("target");

    /** The final property updater. */
    private static final MethodHandle baseCurrencyMinimumBidPriceUpdater = updater("baseCurrencyMinimumBidPrice");

    /** The final property updater. */
    private static final MethodHandle targetCurrencyMinimumBidSizeUpdater = updater("targetCurrencyMinimumBidSize");

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

    /** The exposed property. */
    public final Currency target;

    /** The exposed property. */
    public final Num baseCurrencyMinimumBidPrice;

    /** The exposed property. */
    public final Num targetCurrencyMinimumBidSize;

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

    /**
     * HIDE CONSTRUCTOR
     */
    protected MarketSetting() {
        this.target = null;
        this.baseCurrencyMinimumBidPrice = null;
        this.targetCurrencyMinimumBidSize = null;
        this.targetCurrencyBidSizes = MarketSettingModel.super.targetCurrencyBidSizes();
        this.baseCurrencyScaleSize = MarketSettingModel.super.baseCurrencyScaleSize();
        this.targetCurrencyScaleSize = MarketSettingModel.super.targetCurrencyScaleSize();
        this.acquirableExecutionSize = MarketSettingModel.super.acquirableExecutionSize();
        this.executionLogger = MarketSettingModel.super.executionLogger();
    }

    /**
     * Return the target property.
     *
     * @return A value of target property.
     */
    @Override
    public final Currency target() {
        return this.target;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of target property.
     */
    @SuppressWarnings("unused")
    private final Currency getTarget() {
        return this.target;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of target property to assign.
     */
    private final void setTarget(Currency value) {
        if (value == null) {
            throw new IllegalArgumentException("The target property requires non-null value.");
        }
        try {
            targetUpdater.invoke(this, value);
        } catch (Throwable e) {
            throw quiet(e);
        }
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
            targetCurrencyMinimumBidSizeUpdater.invoke(this, deriveByMinBid$473121199.invoke(this, value, (Consumer<List<Num>>) this::setTargetCurrencyBidSizes));
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
     * Show all property values.
     *
     * @return All property values.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("MarketSetting [");
        builder.append("target=").append(target).append(", ");
        builder.append("baseCurrencyMinimumBidPrice=").append(baseCurrencyMinimumBidPrice).append(", ");
        builder.append("targetCurrencyMinimumBidSize=").append(targetCurrencyMinimumBidSize).append(", ");
        builder.append("targetCurrencyBidSizes=").append(targetCurrencyBidSizes).append(", ");
        builder.append("baseCurrencyScaleSize=").append(baseCurrencyScaleSize).append(", ");
        builder.append("targetCurrencyScaleSize=").append(targetCurrencyScaleSize).append(", ");
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
        return Objects.hash(target, baseCurrencyMinimumBidPrice, targetCurrencyMinimumBidSize, targetCurrencyBidSizes, baseCurrencyScaleSize, targetCurrencyScaleSize, acquirableExecutionSize, executionLogger);
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
        if (!Objects.equals(baseCurrencyMinimumBidPrice, other.baseCurrencyMinimumBidPrice)) return false;
        if (!Objects.equals(targetCurrencyMinimumBidSize, other.targetCurrencyMinimumBidSize)) return false;
        if (!Objects.equals(targetCurrencyBidSizes, other.targetCurrencyBidSizes)) return false;
        if (baseCurrencyScaleSize != other.baseCurrencyScaleSize) return false;
        if (targetCurrencyScaleSize != other.targetCurrencyScaleSize) return false;
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
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> target(Currency target) {
            Åssignable o = new Åssignable();
            o.target(target);
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> abbc() {
            Åssignable o = new Åssignable();
            o.abbc();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> ada() {
            Åssignable o = new Åssignable();
            o.ada();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> ae() {
            Åssignable o = new Åssignable();
            o.ae();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> aed() {
            Åssignable o = new Åssignable();
            o.aed();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> afn() {
            Åssignable o = new Åssignable();
            o.afn();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> aion() {
            Åssignable o = new Åssignable();
            o.aion();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> algo() {
            Åssignable o = new Åssignable();
            o.algo();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> all() {
            Åssignable o = new Åssignable();
            o.all();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> amd() {
            Åssignable o = new Åssignable();
            o.amd();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> ampl() {
            Åssignable o = new Åssignable();
            o.ampl();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> anc() {
            Åssignable o = new Åssignable();
            o.anc();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> ang() {
            Åssignable o = new Åssignable();
            o.ang();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> ankr() {
            Åssignable o = new Åssignable();
            o.ankr();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> ant() {
            Åssignable o = new Åssignable();
            o.ant();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> aoa() {
            Åssignable o = new Åssignable();
            o.aoa();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> ardr() {
            Åssignable o = new Åssignable();
            o.ardr();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> ark() {
            Åssignable o = new Åssignable();
            o.ark();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> arn() {
            Åssignable o = new Åssignable();
            o.arn();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> ars() {
            Åssignable o = new Åssignable();
            o.ars();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> atom() {
            Åssignable o = new Åssignable();
            o.atom();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> aud() {
            Åssignable o = new Åssignable();
            o.aud();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> aur() {
            Åssignable o = new Åssignable();
            o.aur();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> ava() {
            Åssignable o = new Åssignable();
            o.ava();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> avt() {
            Åssignable o = new Åssignable();
            o.avt();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> awg() {
            Åssignable o = new Åssignable();
            o.awg();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> azn() {
            Åssignable o = new Åssignable();
            o.azn();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> bam() {
            Åssignable o = new Åssignable();
            o.bam();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> band() {
            Åssignable o = new Åssignable();
            o.band();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> bat() {
            Åssignable o = new Åssignable();
            o.bat();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> bbd() {
            Åssignable o = new Åssignable();
            o.bbd();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> bc() {
            Åssignable o = new Åssignable();
            o.bc();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> bca() {
            Åssignable o = new Åssignable();
            o.bca();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> bcc() {
            Åssignable o = new Åssignable();
            o.bcc();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> bcd() {
            Åssignable o = new Åssignable();
            o.bcd();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> bch() {
            Åssignable o = new Åssignable();
            o.bch();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> bdt() {
            Åssignable o = new Åssignable();
            o.bdt();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> beam() {
            Åssignable o = new Åssignable();
            o.beam();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> bgc() {
            Åssignable o = new Åssignable();
            o.bgc();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> bgn() {
            Åssignable o = new Åssignable();
            o.bgn();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> bhd() {
            Åssignable o = new Åssignable();
            o.bhd();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> bht() {
            Åssignable o = new Åssignable();
            o.bht();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> bif() {
            Åssignable o = new Åssignable();
            o.bif();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> bmd() {
            Åssignable o = new Åssignable();
            o.bmd();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> bnb() {
            Åssignable o = new Åssignable();
            o.bnb();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> bnd() {
            Åssignable o = new Åssignable();
            o.bnd();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> bnk() {
            Åssignable o = new Åssignable();
            o.bnk();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> bnt() {
            Åssignable o = new Åssignable();
            o.bnt();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> bob() {
            Åssignable o = new Åssignable();
            o.bob();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> brl() {
            Åssignable o = new Åssignable();
            o.brl();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> bsd() {
            Åssignable o = new Åssignable();
            o.bsd();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> bsv() {
            Åssignable o = new Åssignable();
            o.bsv();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> btc() {
            Åssignable o = new Åssignable();
            o.btc();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> btg() {
            Åssignable o = new Åssignable();
            o.btg();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> btm() {
            Åssignable o = new Åssignable();
            o.btm();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> btn() {
            Åssignable o = new Åssignable();
            o.btn();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> bts() {
            Åssignable o = new Åssignable();
            o.bts();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> btt() {
            Åssignable o = new Åssignable();
            o.btt();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> busd() {
            Åssignable o = new Åssignable();
            o.busd();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> bwp() {
            Åssignable o = new Åssignable();
            o.bwp();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> byr() {
            Åssignable o = new Åssignable();
            o.byr();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> bzd() {
            Åssignable o = new Åssignable();
            o.bzd();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> cad() {
            Åssignable o = new Åssignable();
            o.cad();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> cdf() {
            Åssignable o = new Åssignable();
            o.cdf();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> cel() {
            Åssignable o = new Åssignable();
            o.cel();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> celr() {
            Åssignable o = new Åssignable();
            o.celr();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> chf() {
            Åssignable o = new Åssignable();
            o.chf();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> chr() {
            Åssignable o = new Åssignable();
            o.chr();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> chsb() {
            Åssignable o = new Åssignable();
            o.chsb();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> chz() {
            Åssignable o = new Åssignable();
            o.chz();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> ckb() {
            Åssignable o = new Åssignable();
            o.ckb();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> clf() {
            Åssignable o = new Åssignable();
            o.clf();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> clp() {
            Åssignable o = new Åssignable();
            o.clp();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> cnc() {
            Åssignable o = new Åssignable();
            o.cnc();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> cnd() {
            Åssignable o = new Åssignable();
            o.cnd();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> cny() {
            Åssignable o = new Åssignable();
            o.cny();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> comp() {
            Åssignable o = new Åssignable();
            o.comp();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> cop() {
            Åssignable o = new Åssignable();
            o.cop();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> coti() {
            Åssignable o = new Åssignable();
            o.coti();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> crc() {
            Åssignable o = new Åssignable();
            o.crc();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> cro() {
            Åssignable o = new Åssignable();
            o.cro();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> crpt() {
            Åssignable o = new Åssignable();
            o.crpt();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> ctxc() {
            Åssignable o = new Åssignable();
            o.ctxc();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> cup() {
            Åssignable o = new Åssignable();
            o.cup();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> cve() {
            Åssignable o = new Åssignable();
            o.cve();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> cvt() {
            Åssignable o = new Åssignable();
            o.cvt();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> czk() {
            Åssignable o = new Åssignable();
            o.czk();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> dad() {
            Åssignable o = new Åssignable();
            o.dad();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> dai() {
            Åssignable o = new Åssignable();
            o.dai();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> dash() {
            Åssignable o = new Åssignable();
            o.dash();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> data() {
            Åssignable o = new Åssignable();
            o.data();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> dcr() {
            Åssignable o = new Åssignable();
            o.dcr();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> dgb() {
            Åssignable o = new Åssignable();
            o.dgb();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> dgc() {
            Åssignable o = new Åssignable();
            o.dgc();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> dgtx() {
            Åssignable o = new Åssignable();
            o.dgtx();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> divi() {
            Åssignable o = new Åssignable();
            o.divi();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> djf() {
            Åssignable o = new Åssignable();
            o.djf();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> dkk() {
            Åssignable o = new Åssignable();
            o.dkk();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> doge() {
            Åssignable o = new Åssignable();
            o.doge();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> dop() {
            Åssignable o = new Åssignable();
            o.dop();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> drgn() {
            Åssignable o = new Åssignable();
            o.drgn();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> drk() {
            Åssignable o = new Åssignable();
            o.drk();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> dvc() {
            Åssignable o = new Åssignable();
            o.dvc();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> dx() {
            Åssignable o = new Åssignable();
            o.dx();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> dzd() {
            Åssignable o = new Åssignable();
            o.dzd();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> edo() {
            Åssignable o = new Åssignable();
            o.edo();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> eek() {
            Åssignable o = new Åssignable();
            o.eek();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> egd() {
            Åssignable o = new Åssignable();
            o.egd();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> egp() {
            Åssignable o = new Åssignable();
            o.egp();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> ela() {
            Åssignable o = new Åssignable();
            o.ela();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> elf() {
            Åssignable o = new Åssignable();
            o.elf();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> eng() {
            Åssignable o = new Åssignable();
            o.eng();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> enj() {
            Åssignable o = new Åssignable();
            o.enj();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> eos() {
            Åssignable o = new Åssignable();
            o.eos();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> erd() {
            Åssignable o = new Åssignable();
            o.erd();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> etb() {
            Åssignable o = new Åssignable();
            o.etb();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> etc() {
            Åssignable o = new Åssignable();
            o.etc();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> eth() {
            Åssignable o = new Åssignable();
            o.eth();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> etn() {
            Åssignable o = new Åssignable();
            o.etn();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> eur() {
            Åssignable o = new Åssignable();
            o.eur();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> eurs() {
            Åssignable o = new Åssignable();
            o.eurs();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> fet() {
            Åssignable o = new Åssignable();
            o.fet();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> fjd() {
            Åssignable o = new Åssignable();
            o.fjd();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> fkp() {
            Åssignable o = new Åssignable();
            o.fkp();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> fsn() {
            Åssignable o = new Åssignable();
            o.fsn();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> ftc() {
            Åssignable o = new Åssignable();
            o.ftc();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> ftm() {
            Åssignable o = new Åssignable();
            o.ftm();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> ftt() {
            Åssignable o = new Åssignable();
            o.ftt();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> fun() {
            Åssignable o = new Åssignable();
            o.fun();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> fxc() {
            Åssignable o = new Åssignable();
            o.fxc();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> gbp() {
            Åssignable o = new Åssignable();
            o.gbp();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> gel() {
            Åssignable o = new Åssignable();
            o.gel();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> ghs() {
            Åssignable o = new Åssignable();
            o.ghs();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> gHs() {
            Åssignable o = new Åssignable();
            o.gHs();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> gip() {
            Åssignable o = new Åssignable();
            o.gip();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> gmd() {
            Åssignable o = new Åssignable();
            o.gmd();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> gnf() {
            Åssignable o = new Åssignable();
            o.gnf();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> gno() {
            Åssignable o = new Åssignable();
            o.gno();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> gnt() {
            Åssignable o = new Åssignable();
            o.gnt();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> grin() {
            Åssignable o = new Åssignable();
            o.grin();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> gt() {
            Åssignable o = new Åssignable();
            o.gt();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> gtq() {
            Åssignable o = new Åssignable();
            o.gtq();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> gvt() {
            Åssignable o = new Åssignable();
            o.gvt();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> gxc() {
            Åssignable o = new Åssignable();
            o.gxc();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> gyd() {
            Åssignable o = new Åssignable();
            o.gyd();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> hbar() {
            Åssignable o = new Åssignable();
            o.hbar();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> hc() {
            Åssignable o = new Åssignable();
            o.hc();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> hedg() {
            Åssignable o = new Åssignable();
            o.hedg();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> hive() {
            Åssignable o = new Åssignable();
            o.hive();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> hkd() {
            Åssignable o = new Åssignable();
            o.hkd();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> hnl() {
            Åssignable o = new Åssignable();
            o.hnl();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> hot() {
            Åssignable o = new Åssignable();
            o.hot();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> hpt() {
            Åssignable o = new Åssignable();
            o.hpt();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> hrk() {
            Åssignable o = new Åssignable();
            o.hrk();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> hsr() {
            Åssignable o = new Åssignable();
            o.hsr();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> ht() {
            Åssignable o = new Åssignable();
            o.ht();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> htg() {
            Åssignable o = new Åssignable();
            o.htg();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> huf() {
            Åssignable o = new Åssignable();
            o.huf();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> husd() {
            Åssignable o = new Åssignable();
            o.husd();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> hvn() {
            Åssignable o = new Åssignable();
            o.hvn();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> hyn() {
            Åssignable o = new Åssignable();
            o.hyn();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> icn() {
            Åssignable o = new Åssignable();
            o.icn();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> icx() {
            Åssignable o = new Åssignable();
            o.icx();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> idr() {
            Åssignable o = new Åssignable();
            o.idr();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> ils() {
            Åssignable o = new Åssignable();
            o.ils();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> inr() {
            Åssignable o = new Åssignable();
            o.inr();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> ioc() {
            Åssignable o = new Åssignable();
            o.ioc();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> iost() {
            Åssignable o = new Åssignable();
            o.iost();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> iot() {
            Åssignable o = new Åssignable();
            o.iot();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> iota() {
            Åssignable o = new Åssignable();
            o.iota();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> iotx() {
            Åssignable o = new Åssignable();
            o.iotx();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> ipx() {
            Åssignable o = new Åssignable();
            o.ipx();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> iqd() {
            Åssignable o = new Åssignable();
            o.iqd();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> iris() {
            Åssignable o = new Åssignable();
            o.iris();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> irr() {
            Åssignable o = new Åssignable();
            o.irr();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> isk() {
            Åssignable o = new Åssignable();
            o.isk();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> ixc() {
            Åssignable o = new Åssignable();
            o.ixc();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> jep() {
            Åssignable o = new Åssignable();
            o.jep();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> jmd() {
            Åssignable o = new Åssignable();
            o.jmd();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> jod() {
            Åssignable o = new Åssignable();
            o.jod();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> jpy() {
            Åssignable o = new Åssignable();
            o.jpy();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> kava() {
            Åssignable o = new Åssignable();
            o.kava();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> kcs() {
            Åssignable o = new Åssignable();
            o.kcs();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> kes() {
            Åssignable o = new Åssignable();
            o.kes();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> kgs() {
            Åssignable o = new Åssignable();
            o.kgs();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> khr() {
            Åssignable o = new Åssignable();
            o.khr();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> kick() {
            Åssignable o = new Åssignable();
            o.kick();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> kmd() {
            Åssignable o = new Åssignable();
            o.kmd();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> kmf() {
            Åssignable o = new Åssignable();
            o.kmf();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> knc() {
            Åssignable o = new Åssignable();
            o.knc();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> kpw() {
            Åssignable o = new Åssignable();
            o.kpw();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> krw() {
            Åssignable o = new Åssignable();
            o.krw();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> ksm() {
            Åssignable o = new Åssignable();
            o.ksm();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> kwd() {
            Åssignable o = new Åssignable();
            o.kwd();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> kyd() {
            Åssignable o = new Åssignable();
            o.kyd();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> kzt() {
            Åssignable o = new Åssignable();
            o.kzt();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> lak() {
            Åssignable o = new Åssignable();
            o.lak();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> lbp() {
            Åssignable o = new Åssignable();
            o.lbp();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> lend() {
            Åssignable o = new Åssignable();
            o.lend();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> leo() {
            Åssignable o = new Åssignable();
            o.leo();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> link() {
            Åssignable o = new Åssignable();
            o.link();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> lkr() {
            Åssignable o = new Åssignable();
            o.lkr();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> loki() {
            Åssignable o = new Åssignable();
            o.loki();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> lrc() {
            Åssignable o = new Åssignable();
            o.lrc();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> lrd() {
            Åssignable o = new Åssignable();
            o.lrd();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> lsk() {
            Åssignable o = new Åssignable();
            o.lsk();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> lsl() {
            Åssignable o = new Åssignable();
            o.lsl();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> ltc() {
            Åssignable o = new Åssignable();
            o.ltc();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> ltl() {
            Åssignable o = new Åssignable();
            o.ltl();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> luna() {
            Åssignable o = new Åssignable();
            o.luna();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> lvl() {
            Åssignable o = new Åssignable();
            o.lvl();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> lyd() {
            Åssignable o = new Åssignable();
            o.lyd();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> mad() {
            Åssignable o = new Åssignable();
            o.mad();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> maid() {
            Åssignable o = new Åssignable();
            o.maid();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> mana() {
            Åssignable o = new Åssignable();
            o.mana();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> matic() {
            Åssignable o = new Åssignable();
            o.matic();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> mco() {
            Åssignable o = new Åssignable();
            o.mco();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> mdl() {
            Åssignable o = new Åssignable();
            o.mdl();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> mec() {
            Åssignable o = new Åssignable();
            o.mec();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> mga() {
            Åssignable o = new Åssignable();
            o.mga();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> miota() {
            Åssignable o = new Åssignable();
            o.miota();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> mkd() {
            Åssignable o = new Åssignable();
            o.mkd();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> mkr() {
            Åssignable o = new Åssignable();
            o.mkr();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> mln() {
            Åssignable o = new Åssignable();
            o.mln();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> mmk() {
            Åssignable o = new Åssignable();
            o.mmk();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> mnt() {
            Åssignable o = new Åssignable();
            o.mnt();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> mod() {
            Åssignable o = new Åssignable();
            o.mod();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> mof() {
            Åssignable o = new Åssignable();
            o.mof();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> mona() {
            Åssignable o = new Åssignable();
            o.mona();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> mop() {
            Åssignable o = new Åssignable();
            o.mop();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> mro() {
            Åssignable o = new Åssignable();
            o.mro();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> msc() {
            Åssignable o = new Åssignable();
            o.msc();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> mtl() {
            Åssignable o = new Åssignable();
            o.mtl();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> mur() {
            Åssignable o = new Åssignable();
            o.mur();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> mvr() {
            Åssignable o = new Åssignable();
            o.mvr();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> mwk() {
            Åssignable o = new Åssignable();
            o.mwk();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> mx() {
            Åssignable o = new Åssignable();
            o.mx();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> mxc() {
            Åssignable o = new Åssignable();
            o.mxc();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> mxn() {
            Åssignable o = new Åssignable();
            o.mxn();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> myr() {
            Åssignable o = new Åssignable();
            o.myr();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> mzn() {
            Åssignable o = new Åssignable();
            o.mzn();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> nad() {
            Åssignable o = new Åssignable();
            o.nad();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> nano() {
            Åssignable o = new Åssignable();
            o.nano();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> nas() {
            Åssignable o = new Åssignable();
            o.nas();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> neo() {
            Åssignable o = new Åssignable();
            o.neo();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> nexo() {
            Åssignable o = new Åssignable();
            o.nexo();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> ngn() {
            Åssignable o = new Åssignable();
            o.ngn();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> nim() {
            Åssignable o = new Åssignable();
            o.nim();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> nio() {
            Åssignable o = new Åssignable();
            o.nio();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> nmc() {
            Åssignable o = new Åssignable();
            o.nmc();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> nmr() {
            Åssignable o = new Åssignable();
            o.nmr();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> nobs() {
            Åssignable o = new Åssignable();
            o.nobs();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> nok() {
            Åssignable o = new Åssignable();
            o.nok();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> npr() {
            Åssignable o = new Åssignable();
            o.npr();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> npxs() {
            Åssignable o = new Åssignable();
            o.npxs();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> nrg() {
            Åssignable o = new Åssignable();
            o.nrg();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> nuls() {
            Åssignable o = new Åssignable();
            o.nuls();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> nvc() {
            Åssignable o = new Åssignable();
            o.nvc();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> nxt() {
            Åssignable o = new Åssignable();
            o.nxt();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> nzd() {
            Åssignable o = new Åssignable();
            o.nzd();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> ocean() {
            Åssignable o = new Åssignable();
            o.ocean();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> ogn() {
            Åssignable o = new Åssignable();
            o.ogn();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> okb() {
            Åssignable o = new Åssignable();
            o.okb();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> omg() {
            Åssignable o = new Åssignable();
            o.omg();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> omr() {
            Åssignable o = new Åssignable();
            o.omr();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> one() {
            Åssignable o = new Åssignable();
            o.one();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> ont() {
            Åssignable o = new Åssignable();
            o.ont();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> orbs() {
            Åssignable o = new Åssignable();
            o.orbs();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> pab() {
            Åssignable o = new Åssignable();
            o.pab();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> pai() {
            Åssignable o = new Åssignable();
            o.pai();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> pax() {
            Åssignable o = new Åssignable();
            o.pax();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> paxg() {
            Åssignable o = new Åssignable();
            o.paxg();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> pen() {
            Åssignable o = new Åssignable();
            o.pen();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> perl() {
            Åssignable o = new Åssignable();
            o.perl();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> pgk() {
            Åssignable o = new Åssignable();
            o.pgk();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> php() {
            Åssignable o = new Åssignable();
            o.php();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> pivx() {
            Åssignable o = new Åssignable();
            o.pivx();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> pkr() {
            Åssignable o = new Åssignable();
            o.pkr();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> pln() {
            Åssignable o = new Åssignable();
            o.pln();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> pnk() {
            Åssignable o = new Åssignable();
            o.pnk();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> pnt() {
            Åssignable o = new Åssignable();
            o.pnt();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> poe() {
            Åssignable o = new Åssignable();
            o.poe();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> pot() {
            Åssignable o = new Åssignable();
            o.pot();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> powr() {
            Åssignable o = new Åssignable();
            o.powr();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> ppc() {
            Åssignable o = new Åssignable();
            o.ppc();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> pyg() {
            Åssignable o = new Åssignable();
            o.pyg();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> qar() {
            Åssignable o = new Åssignable();
            o.qar();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> qnt() {
            Åssignable o = new Åssignable();
            o.qnt();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> qrk() {
            Åssignable o = new Åssignable();
            o.qrk();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> qsp() {
            Åssignable o = new Åssignable();
            o.qsp();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> qtum() {
            Åssignable o = new Åssignable();
            o.qtum();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> rcn() {
            Åssignable o = new Åssignable();
            o.rcn();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> rdd() {
            Åssignable o = new Åssignable();
            o.rdd();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> ren() {
            Åssignable o = new Åssignable();
            o.ren();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> rep() {
            Åssignable o = new Åssignable();
            o.rep();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> req() {
            Åssignable o = new Åssignable();
            o.req();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> rif() {
            Åssignable o = new Åssignable();
            o.rif();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> rlc() {
            Åssignable o = new Åssignable();
            o.rlc();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> ron() {
            Åssignable o = new Åssignable();
            o.ron();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> rsd() {
            Åssignable o = new Åssignable();
            o.rsd();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> rsr() {
            Åssignable o = new Åssignable();
            o.rsr();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> rub() {
            Åssignable o = new Åssignable();
            o.rub();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> rur() {
            Åssignable o = new Åssignable();
            o.rur();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> rvn() {
            Åssignable o = new Åssignable();
            o.rvn();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> rwf() {
            Åssignable o = new Åssignable();
            o.rwf();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> sar() {
            Åssignable o = new Åssignable();
            o.sar();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> sbc() {
            Åssignable o = new Åssignable();
            o.sbc();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> sbd() {
            Åssignable o = new Åssignable();
            o.sbd();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> sc() {
            Åssignable o = new Åssignable();
            o.sc();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> scr() {
            Åssignable o = new Åssignable();
            o.scr();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> sdg() {
            Åssignable o = new Åssignable();
            o.sdg();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> seele() {
            Åssignable o = new Åssignable();
            o.seele();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> sek() {
            Åssignable o = new Åssignable();
            o.sek();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> sero() {
            Åssignable o = new Åssignable();
            o.sero();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> sgd() {
            Åssignable o = new Åssignable();
            o.sgd();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> shp() {
            Åssignable o = new Åssignable();
            o.shp();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> sll() {
            Åssignable o = new Åssignable();
            o.sll();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> smart() {
            Åssignable o = new Åssignable();
            o.smart();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> snt() {
            Åssignable o = new Åssignable();
            o.snt();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> snx() {
            Åssignable o = new Åssignable();
            o.snx();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> sol() {
            Åssignable o = new Åssignable();
            o.sol();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> solve() {
            Åssignable o = new Åssignable();
            o.solve();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> sos() {
            Åssignable o = new Åssignable();
            o.sos();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> srd() {
            Åssignable o = new Åssignable();
            o.srd();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> start() {
            Åssignable o = new Åssignable();
            o.start();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> std() {
            Åssignable o = new Åssignable();
            o.std();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> steem() {
            Åssignable o = new Åssignable();
            o.steem();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> storj() {
            Åssignable o = new Åssignable();
            o.storj();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> str() {
            Åssignable o = new Åssignable();
            o.str();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> strat() {
            Åssignable o = new Åssignable();
            o.strat();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> stx() {
            Åssignable o = new Åssignable();
            o.stx();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> svc() {
            Åssignable o = new Åssignable();
            o.svc();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> sxp() {
            Åssignable o = new Åssignable();
            o.sxp();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> syp() {
            Åssignable o = new Åssignable();
            o.syp();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> sys() {
            Åssignable o = new Åssignable();
            o.sys();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> szl() {
            Åssignable o = new Åssignable();
            o.szl();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> tfuel() {
            Åssignable o = new Åssignable();
            o.tfuel();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> thb() {
            Åssignable o = new Åssignable();
            o.thb();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> theta() {
            Åssignable o = new Åssignable();
            o.theta();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> tjs() {
            Åssignable o = new Åssignable();
            o.tjs();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> tmt() {
            Åssignable o = new Åssignable();
            o.tmt();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> tmtg() {
            Åssignable o = new Åssignable();
            o.tmtg();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> tnd() {
            Åssignable o = new Åssignable();
            o.tnd();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> tomo() {
            Åssignable o = new Åssignable();
            o.tomo();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> top() {
            Åssignable o = new Åssignable();
            o.top();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> trac() {
            Åssignable o = new Åssignable();
            o.trac();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> trc() {
            Åssignable o = new Åssignable();
            o.trc();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> TRUE() {
            Åssignable o = new Åssignable();
            o.TRUE();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> trx() {
            Åssignable o = new Åssignable();
            o.trx();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> TRY() {
            Åssignable o = new Åssignable();
            o.TRY();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> tshp() {
            Åssignable o = new Åssignable();
            o.tshp();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> tt() {
            Åssignable o = new Åssignable();
            o.tt();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> ttd() {
            Åssignable o = new Åssignable();
            o.ttd();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> tusd() {
            Åssignable o = new Åssignable();
            o.tusd();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> twd() {
            Åssignable o = new Åssignable();
            o.twd();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> tzs() {
            Åssignable o = new Åssignable();
            o.tzs();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> uah() {
            Åssignable o = new Åssignable();
            o.uah();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> ubt() {
            Åssignable o = new Åssignable();
            o.ubt();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> ugx() {
            Åssignable o = new Åssignable();
            o.ugx();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> usd() {
            Åssignable o = new Åssignable();
            o.usd();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> usdc() {
            Åssignable o = new Åssignable();
            o.usdc();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> usde() {
            Åssignable o = new Åssignable();
            o.usde();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> usdt() {
            Åssignable o = new Åssignable();
            o.usdt();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> utc() {
            Åssignable o = new Åssignable();
            o.utc();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> utk() {
            Åssignable o = new Åssignable();
            o.utk();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> uyu() {
            Åssignable o = new Åssignable();
            o.uyu();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> uzs() {
            Åssignable o = new Åssignable();
            o.uzs();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> vef() {
            Åssignable o = new Åssignable();
            o.vef();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> ven() {
            Åssignable o = new Åssignable();
            o.ven();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> vet() {
            Åssignable o = new Åssignable();
            o.vet();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> vgx() {
            Åssignable o = new Åssignable();
            o.vgx();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> vib() {
            Åssignable o = new Åssignable();
            o.vib();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> vlx() {
            Åssignable o = new Åssignable();
            o.vlx();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> vnd() {
            Åssignable o = new Åssignable();
            o.vnd();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> vsys() {
            Åssignable o = new Åssignable();
            o.vsys();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> vtho() {
            Åssignable o = new Åssignable();
            o.vtho();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> vuv() {
            Åssignable o = new Åssignable();
            o.vuv();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> wan() {
            Åssignable o = new Åssignable();
            o.wan();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> waves() {
            Åssignable o = new Åssignable();
            o.waves();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> waxp() {
            Åssignable o = new Åssignable();
            o.waxp();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> wdc() {
            Åssignable o = new Åssignable();
            o.wdc();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> wicc() {
            Åssignable o = new Åssignable();
            o.wicc();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> win() {
            Åssignable o = new Åssignable();
            o.win();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> wrx() {
            Åssignable o = new Åssignable();
            o.wrx();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> wst() {
            Åssignable o = new Åssignable();
            o.wst();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> wtc() {
            Åssignable o = new Åssignable();
            o.wtc();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> wxt() {
            Åssignable o = new Åssignable();
            o.wxt();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> xaf() {
            Åssignable o = new Åssignable();
            o.xaf();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> xas() {
            Åssignable o = new Åssignable();
            o.xas();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> xaur() {
            Åssignable o = new Åssignable();
            o.xaur();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> xcd() {
            Åssignable o = new Åssignable();
            o.xcd();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> xdce() {
            Åssignable o = new Åssignable();
            o.xdce();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> xdr() {
            Åssignable o = new Åssignable();
            o.xdr();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> xem() {
            Åssignable o = new Åssignable();
            o.xem();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> xhv() {
            Åssignable o = new Åssignable();
            o.xhv();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> xlm() {
            Åssignable o = new Åssignable();
            o.xlm();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> xmr() {
            Åssignable o = new Åssignable();
            o.xmr();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> xns() {
            Åssignable o = new Åssignable();
            o.xns();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> xof() {
            Åssignable o = new Åssignable();
            o.xof();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> xpf() {
            Åssignable o = new Åssignable();
            o.xpf();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> xpm() {
            Åssignable o = new Åssignable();
            o.xpm();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> xrb() {
            Åssignable o = new Åssignable();
            o.xrb();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> xrp() {
            Åssignable o = new Åssignable();
            o.xrp();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> xtz() {
            Åssignable o = new Åssignable();
            o.xtz();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> xvg() {
            Åssignable o = new Åssignable();
            o.xvg();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> xzc() {
            Åssignable o = new Åssignable();
            o.xzc();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> ybc() {
            Åssignable o = new Åssignable();
            o.ybc();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> yer() {
            Åssignable o = new Åssignable();
            o.yer();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> yoyo() {
            Åssignable o = new Åssignable();
            o.yoyo();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> zar() {
            Åssignable o = new Åssignable();
            o.zar();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> zb() {
            Åssignable o = new Åssignable();
            o.zb();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> zec() {
            Åssignable o = new Åssignable();
            o.zec();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> zen() {
            Åssignable o = new Åssignable();
            o.zen();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> zil() {
            Åssignable o = new Åssignable();
            o.zil();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> zmw() {
            Åssignable o = new Åssignable();
            o.zmw();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> zrc() {
            Åssignable o = new Åssignable();
            o.zrc();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> zrx() {
            Åssignable o = new Åssignable();
            o.zrx();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> zwl() {
            Åssignable o = new Åssignable();
            o.zwl();
            return o;
        }

        /**
         * Create new {@link MarketSetting} with the specified target property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableBaseCurrencyMinimumBidPrice<ÅssignableTargetCurrencyMinimumBidSize<Self>> _1st() {
            Åssignable o = new Åssignable();
            o._1st();
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
        default Next target(Currency value) {
            ((MarketSetting) this).setTarget(value);
            return (Next) this;
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next abbc() {
            return target(Currency.ABBC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next ada() {
            return target(Currency.ADA);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next ae() {
            return target(Currency.AE);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next aed() {
            return target(Currency.AED);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next afn() {
            return target(Currency.AFN);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next aion() {
            return target(Currency.AION);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next algo() {
            return target(Currency.ALGO);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next all() {
            return target(Currency.ALL);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next amd() {
            return target(Currency.AMD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next ampl() {
            return target(Currency.AMPL);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next anc() {
            return target(Currency.ANC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next ang() {
            return target(Currency.ANG);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next ankr() {
            return target(Currency.ANKR);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next ant() {
            return target(Currency.ANT);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next aoa() {
            return target(Currency.AOA);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next ardr() {
            return target(Currency.ARDR);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next ark() {
            return target(Currency.ARK);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next arn() {
            return target(Currency.ARN);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next ars() {
            return target(Currency.ARS);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next atom() {
            return target(Currency.ATOM);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next aud() {
            return target(Currency.AUD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next aur() {
            return target(Currency.AUR);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next ava() {
            return target(Currency.AVA);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next avt() {
            return target(Currency.AVT);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next awg() {
            return target(Currency.AWG);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next azn() {
            return target(Currency.AZN);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next bam() {
            return target(Currency.BAM);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next band() {
            return target(Currency.BAND);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next bat() {
            return target(Currency.BAT);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next bbd() {
            return target(Currency.BBD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next bc() {
            return target(Currency.BC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next bca() {
            return target(Currency.BCA);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next bcc() {
            return target(Currency.BCC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next bcd() {
            return target(Currency.BCD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next bch() {
            return target(Currency.BCH);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next bdt() {
            return target(Currency.BDT);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next beam() {
            return target(Currency.BEAM);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next bgc() {
            return target(Currency.BGC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next bgn() {
            return target(Currency.BGN);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next bhd() {
            return target(Currency.BHD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next bht() {
            return target(Currency.BHT);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next bif() {
            return target(Currency.BIF);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next bmd() {
            return target(Currency.BMD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next bnb() {
            return target(Currency.BNB);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next bnd() {
            return target(Currency.BND);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next bnk() {
            return target(Currency.BNK);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next bnt() {
            return target(Currency.BNT);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next bob() {
            return target(Currency.BOB);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next brl() {
            return target(Currency.BRL);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next bsd() {
            return target(Currency.BSD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next bsv() {
            return target(Currency.BSV);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next btc() {
            return target(Currency.BTC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next btg() {
            return target(Currency.BTG);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next btm() {
            return target(Currency.BTM);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next btn() {
            return target(Currency.BTN);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next bts() {
            return target(Currency.BTS);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next btt() {
            return target(Currency.BTT);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next busd() {
            return target(Currency.BUSD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next bwp() {
            return target(Currency.BWP);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next byr() {
            return target(Currency.BYR);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next bzd() {
            return target(Currency.BZD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next cad() {
            return target(Currency.CAD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next cdf() {
            return target(Currency.CDF);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next cel() {
            return target(Currency.CEL);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next celr() {
            return target(Currency.CELR);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next chf() {
            return target(Currency.CHF);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next chr() {
            return target(Currency.CHR);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next chsb() {
            return target(Currency.CHSB);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next chz() {
            return target(Currency.CHZ);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next ckb() {
            return target(Currency.CKB);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next clf() {
            return target(Currency.CLF);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next clp() {
            return target(Currency.CLP);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next cnc() {
            return target(Currency.CNC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next cnd() {
            return target(Currency.CND);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next cny() {
            return target(Currency.CNY);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next comp() {
            return target(Currency.COMP);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next cop() {
            return target(Currency.COP);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next coti() {
            return target(Currency.COTI);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next crc() {
            return target(Currency.CRC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next cro() {
            return target(Currency.CRO);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next crpt() {
            return target(Currency.CRPT);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next ctxc() {
            return target(Currency.CTXC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next cup() {
            return target(Currency.CUP);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next cve() {
            return target(Currency.CVE);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next cvt() {
            return target(Currency.CVT);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next czk() {
            return target(Currency.CZK);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next dad() {
            return target(Currency.DAD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next dai() {
            return target(Currency.DAI);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next dash() {
            return target(Currency.DASH);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next data() {
            return target(Currency.DATA);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next dcr() {
            return target(Currency.DCR);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next dgb() {
            return target(Currency.DGB);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next dgc() {
            return target(Currency.DGC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next dgtx() {
            return target(Currency.DGTX);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next divi() {
            return target(Currency.DIVI);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next djf() {
            return target(Currency.DJF);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next dkk() {
            return target(Currency.DKK);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next doge() {
            return target(Currency.DOGE);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next dop() {
            return target(Currency.DOP);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next drgn() {
            return target(Currency.DRGN);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next drk() {
            return target(Currency.DRK);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next dvc() {
            return target(Currency.DVC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next dx() {
            return target(Currency.DX);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next dzd() {
            return target(Currency.DZD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next edo() {
            return target(Currency.EDO);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next eek() {
            return target(Currency.EEK);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next egd() {
            return target(Currency.EGD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next egp() {
            return target(Currency.EGP);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next ela() {
            return target(Currency.ELA);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next elf() {
            return target(Currency.ELF);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next eng() {
            return target(Currency.ENG);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next enj() {
            return target(Currency.ENJ);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next eos() {
            return target(Currency.EOS);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next erd() {
            return target(Currency.ERD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next etb() {
            return target(Currency.ETB);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next etc() {
            return target(Currency.ETC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next eth() {
            return target(Currency.ETH);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next etn() {
            return target(Currency.ETN);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next eur() {
            return target(Currency.EUR);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next eurs() {
            return target(Currency.EURS);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next fet() {
            return target(Currency.FET);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next fjd() {
            return target(Currency.FJD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next fkp() {
            return target(Currency.FKP);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next fsn() {
            return target(Currency.FSN);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next ftc() {
            return target(Currency.FTC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next ftm() {
            return target(Currency.FTM);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next ftt() {
            return target(Currency.FTT);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next fun() {
            return target(Currency.FUN);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next fxc() {
            return target(Currency.FXC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next gbp() {
            return target(Currency.GBP);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next gel() {
            return target(Currency.GEL);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next ghs() {
            return target(Currency.GHS);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next gHs() {
            return target(Currency.GHS);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next gip() {
            return target(Currency.GIP);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next gmd() {
            return target(Currency.GMD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next gnf() {
            return target(Currency.GNF);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next gno() {
            return target(Currency.GNO);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next gnt() {
            return target(Currency.GNT);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next grin() {
            return target(Currency.GRIN);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next gt() {
            return target(Currency.GT);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next gtq() {
            return target(Currency.GTQ);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next gvt() {
            return target(Currency.GVT);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next gxc() {
            return target(Currency.GXC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next gyd() {
            return target(Currency.GYD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next hbar() {
            return target(Currency.HBAR);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next hc() {
            return target(Currency.HC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next hedg() {
            return target(Currency.HEDG);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next hive() {
            return target(Currency.HIVE);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next hkd() {
            return target(Currency.HKD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next hnl() {
            return target(Currency.HNL);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next hot() {
            return target(Currency.HOT);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next hpt() {
            return target(Currency.HPT);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next hrk() {
            return target(Currency.HRK);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next hsr() {
            return target(Currency.HSR);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next ht() {
            return target(Currency.HT);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next htg() {
            return target(Currency.HTG);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next huf() {
            return target(Currency.HUF);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next husd() {
            return target(Currency.HUSD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next hvn() {
            return target(Currency.HVN);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next hyn() {
            return target(Currency.HYN);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next icn() {
            return target(Currency.ICN);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next icx() {
            return target(Currency.ICX);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next idr() {
            return target(Currency.IDR);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next ils() {
            return target(Currency.ILS);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next inr() {
            return target(Currency.INR);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next ioc() {
            return target(Currency.IOC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next iost() {
            return target(Currency.IOST);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next iot() {
            return target(Currency.IOT);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next iota() {
            return target(Currency.IOTA);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next iotx() {
            return target(Currency.IOTX);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next ipx() {
            return target(Currency.IPX);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next iqd() {
            return target(Currency.IQD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next iris() {
            return target(Currency.IRIS);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next irr() {
            return target(Currency.IRR);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next isk() {
            return target(Currency.ISK);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next ixc() {
            return target(Currency.IXC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next jep() {
            return target(Currency.JEP);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next jmd() {
            return target(Currency.JMD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next jod() {
            return target(Currency.JOD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next jpy() {
            return target(Currency.JPY);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next kava() {
            return target(Currency.KAVA);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next kcs() {
            return target(Currency.KCS);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next kes() {
            return target(Currency.KES);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next kgs() {
            return target(Currency.KGS);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next khr() {
            return target(Currency.KHR);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next kick() {
            return target(Currency.KICK);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next kmd() {
            return target(Currency.KMD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next kmf() {
            return target(Currency.KMF);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next knc() {
            return target(Currency.KNC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next kpw() {
            return target(Currency.KPW);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next krw() {
            return target(Currency.KRW);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next ksm() {
            return target(Currency.KSM);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next kwd() {
            return target(Currency.KWD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next kyd() {
            return target(Currency.KYD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next kzt() {
            return target(Currency.KZT);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next lak() {
            return target(Currency.LAK);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next lbp() {
            return target(Currency.LBP);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next lend() {
            return target(Currency.LEND);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next leo() {
            return target(Currency.LEO);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next link() {
            return target(Currency.LINK);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next lkr() {
            return target(Currency.LKR);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next loki() {
            return target(Currency.LOKI);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next lrc() {
            return target(Currency.LRC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next lrd() {
            return target(Currency.LRD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next lsk() {
            return target(Currency.LSK);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next lsl() {
            return target(Currency.LSL);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next ltc() {
            return target(Currency.LTC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next ltl() {
            return target(Currency.LTL);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next luna() {
            return target(Currency.LUNA);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next lvl() {
            return target(Currency.LVL);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next lyd() {
            return target(Currency.LYD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next mad() {
            return target(Currency.MAD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next maid() {
            return target(Currency.MAID);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next mana() {
            return target(Currency.MANA);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next matic() {
            return target(Currency.MATIC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next mco() {
            return target(Currency.MCO);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next mdl() {
            return target(Currency.MDL);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next mec() {
            return target(Currency.MEC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next mga() {
            return target(Currency.MGA);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next miota() {
            return target(Currency.MIOTA);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next mkd() {
            return target(Currency.MKD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next mkr() {
            return target(Currency.MKR);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next mln() {
            return target(Currency.MLN);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next mmk() {
            return target(Currency.MMK);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next mnt() {
            return target(Currency.MNT);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next mod() {
            return target(Currency.MOD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next mof() {
            return target(Currency.MOF);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next mona() {
            return target(Currency.MONA);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next mop() {
            return target(Currency.MOP);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next mro() {
            return target(Currency.MRO);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next msc() {
            return target(Currency.MSC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next mtl() {
            return target(Currency.MTL);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next mur() {
            return target(Currency.MUR);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next mvr() {
            return target(Currency.MVR);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next mwk() {
            return target(Currency.MWK);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next mx() {
            return target(Currency.MX);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next mxc() {
            return target(Currency.MXC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next mxn() {
            return target(Currency.MXN);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next myr() {
            return target(Currency.MYR);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next mzn() {
            return target(Currency.MZN);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next nad() {
            return target(Currency.NAD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next nano() {
            return target(Currency.NANO);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next nas() {
            return target(Currency.NAS);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next neo() {
            return target(Currency.NEO);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next nexo() {
            return target(Currency.NEXO);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next ngn() {
            return target(Currency.NGN);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next nim() {
            return target(Currency.NIM);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next nio() {
            return target(Currency.NIO);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next nmc() {
            return target(Currency.NMC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next nmr() {
            return target(Currency.NMR);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next nobs() {
            return target(Currency.NOBS);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next nok() {
            return target(Currency.NOK);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next npr() {
            return target(Currency.NPR);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next npxs() {
            return target(Currency.NPXS);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next nrg() {
            return target(Currency.NRG);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next nuls() {
            return target(Currency.NULS);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next nvc() {
            return target(Currency.NVC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next nxt() {
            return target(Currency.NXT);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next nzd() {
            return target(Currency.NZD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next ocean() {
            return target(Currency.OCEAN);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next ogn() {
            return target(Currency.OGN);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next okb() {
            return target(Currency.OKB);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next omg() {
            return target(Currency.OMG);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next omr() {
            return target(Currency.OMR);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next one() {
            return target(Currency.ONE);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next ont() {
            return target(Currency.ONT);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next orbs() {
            return target(Currency.ORBS);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next pab() {
            return target(Currency.PAB);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next pai() {
            return target(Currency.PAI);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next pax() {
            return target(Currency.PAX);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next paxg() {
            return target(Currency.PAXG);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next pen() {
            return target(Currency.PEN);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next perl() {
            return target(Currency.PERL);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next pgk() {
            return target(Currency.PGK);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next php() {
            return target(Currency.PHP);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next pivx() {
            return target(Currency.PIVX);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next pkr() {
            return target(Currency.PKR);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next pln() {
            return target(Currency.PLN);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next pnk() {
            return target(Currency.PNK);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next pnt() {
            return target(Currency.PNT);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next poe() {
            return target(Currency.POE);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next pot() {
            return target(Currency.POT);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next powr() {
            return target(Currency.POWR);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next ppc() {
            return target(Currency.PPC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next pyg() {
            return target(Currency.PYG);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next qar() {
            return target(Currency.QAR);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next qnt() {
            return target(Currency.QNT);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next qrk() {
            return target(Currency.QRK);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next qsp() {
            return target(Currency.QSP);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next qtum() {
            return target(Currency.QTUM);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next rcn() {
            return target(Currency.RCN);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next rdd() {
            return target(Currency.RDD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next ren() {
            return target(Currency.REN);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next rep() {
            return target(Currency.REP);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next req() {
            return target(Currency.REQ);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next rif() {
            return target(Currency.RIF);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next rlc() {
            return target(Currency.RLC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next ron() {
            return target(Currency.RON);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next rsd() {
            return target(Currency.RSD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next rsr() {
            return target(Currency.RSR);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next rub() {
            return target(Currency.RUB);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next rur() {
            return target(Currency.RUR);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next rvn() {
            return target(Currency.RVN);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next rwf() {
            return target(Currency.RWF);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next sar() {
            return target(Currency.SAR);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next sbc() {
            return target(Currency.SBC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next sbd() {
            return target(Currency.SBD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next sc() {
            return target(Currency.SC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next scr() {
            return target(Currency.SCR);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next sdg() {
            return target(Currency.SDG);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next seele() {
            return target(Currency.SEELE);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next sek() {
            return target(Currency.SEK);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next sero() {
            return target(Currency.SERO);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next sgd() {
            return target(Currency.SGD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next shp() {
            return target(Currency.SHP);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next sll() {
            return target(Currency.SLL);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next smart() {
            return target(Currency.SMART);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next snt() {
            return target(Currency.SNT);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next snx() {
            return target(Currency.SNX);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next sol() {
            return target(Currency.SOL);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next solve() {
            return target(Currency.SOLVE);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next sos() {
            return target(Currency.SOS);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next srd() {
            return target(Currency.SRD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next start() {
            return target(Currency.START);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next std() {
            return target(Currency.STD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next steem() {
            return target(Currency.STEEM);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next storj() {
            return target(Currency.STORJ);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next str() {
            return target(Currency.STR);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next strat() {
            return target(Currency.STRAT);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next stx() {
            return target(Currency.STX);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next svc() {
            return target(Currency.SVC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next sxp() {
            return target(Currency.SXP);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next syp() {
            return target(Currency.SYP);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next sys() {
            return target(Currency.SYS);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next szl() {
            return target(Currency.SZL);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next tfuel() {
            return target(Currency.TFUEL);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next thb() {
            return target(Currency.THB);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next theta() {
            return target(Currency.THETA);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next tjs() {
            return target(Currency.TJS);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next tmt() {
            return target(Currency.TMT);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next tmtg() {
            return target(Currency.TMTG);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next tnd() {
            return target(Currency.TND);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next tomo() {
            return target(Currency.TOMO);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next top() {
            return target(Currency.TOP);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next trac() {
            return target(Currency.TRAC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next trc() {
            return target(Currency.TRC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next TRUE() {
            return target(Currency.TRUE);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next trx() {
            return target(Currency.TRX);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next TRY() {
            return target(Currency.TRY);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next tshp() {
            return target(Currency.TSHP);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next tt() {
            return target(Currency.TT);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next ttd() {
            return target(Currency.TTD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next tusd() {
            return target(Currency.TUSD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next twd() {
            return target(Currency.TWD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next tzs() {
            return target(Currency.TZS);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next uah() {
            return target(Currency.UAH);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next ubt() {
            return target(Currency.UBT);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next ugx() {
            return target(Currency.UGX);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next usd() {
            return target(Currency.USD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next usdc() {
            return target(Currency.USDC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next usde() {
            return target(Currency.USDE);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next usdt() {
            return target(Currency.USDT);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next utc() {
            return target(Currency.UTC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next utk() {
            return target(Currency.UTK);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next uyu() {
            return target(Currency.UYU);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next uzs() {
            return target(Currency.UZS);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next vef() {
            return target(Currency.VEF);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next ven() {
            return target(Currency.VEN);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next vet() {
            return target(Currency.VET);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next vgx() {
            return target(Currency.VGX);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next vib() {
            return target(Currency.VIB);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next vlx() {
            return target(Currency.VLX);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next vnd() {
            return target(Currency.VND);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next vsys() {
            return target(Currency.VSYS);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next vtho() {
            return target(Currency.VTHO);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next vuv() {
            return target(Currency.VUV);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next wan() {
            return target(Currency.WAN);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next waves() {
            return target(Currency.WAVES);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next waxp() {
            return target(Currency.WAXP);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next wdc() {
            return target(Currency.WDC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next wicc() {
            return target(Currency.WICC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next win() {
            return target(Currency.WIN);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next wrx() {
            return target(Currency.WRX);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next wst() {
            return target(Currency.WST);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next wtc() {
            return target(Currency.WTC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next wxt() {
            return target(Currency.WXT);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next xaf() {
            return target(Currency.XAF);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next xas() {
            return target(Currency.XAS);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next xaur() {
            return target(Currency.XAUR);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next xcd() {
            return target(Currency.XCD);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next xdce() {
            return target(Currency.XDCE);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next xdr() {
            return target(Currency.XDR);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next xem() {
            return target(Currency.XEM);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next xhv() {
            return target(Currency.XHV);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next xlm() {
            return target(Currency.XLM);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next xmr() {
            return target(Currency.XMR);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next xns() {
            return target(Currency.XNS);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next xof() {
            return target(Currency.XOF);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next xpf() {
            return target(Currency.XPF);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next xpm() {
            return target(Currency.XPM);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next xrb() {
            return target(Currency.XRB);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next xrp() {
            return target(Currency.XRP);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next xtz() {
            return target(Currency.XTZ);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next xvg() {
            return target(Currency.XVG);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next xzc() {
            return target(Currency.XZC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next ybc() {
            return target(Currency.YBC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next yer() {
            return target(Currency.YER);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next yoyo() {
            return target(Currency.YOYO);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next zar() {
            return target(Currency.ZAR);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next zb() {
            return target(Currency.ZB);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next zec() {
            return target(Currency.ZEC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next zen() {
            return target(Currency.ZEN);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next zil() {
            return target(Currency.ZIL);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next zmw() {
            return target(Currency.ZMW);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next zrc() {
            return target(Currency.ZRC);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next zrx() {
            return target(Currency.ZRX);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next zwl() {
            return target(Currency.ZWL);
        }

        /**
         * Assign target property.
         * 
         * @return The next assignable model.
         */
        default Next _1st() {
            return target(Currency._1ST);
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

        /**
         * Assign baseCurrencyMinimumBidPrice property.
         * 
         * @return The next assignable model.
         */
        default Next baseCurrencyMinimumBidPrice(String price) {
            try {
                return baseCurrencyMinimumBidPrice((Num) baseCurrencyMinimumBidPrice$927011984.invoke(this, price));
            } catch (Throwable e) {
                throw quiet(e);
            }
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

        /**
         * Assign targetCurrencyMinimumBidSize property.
         * 
         * @return The next assignable model.
         */
        default Next targetCurrencyMinimumBidSize(String price) {
            try {
                return targetCurrencyMinimumBidSize((Num) targetCurrencyMinimumBidSize$927011984.invoke(this, price));
            } catch (Throwable e) {
                throw quiet(e);
            }
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
    }

    /**
     * Internal aggregated API.
     */
    protected static interface ÅssignableAll extends ÅssignableTarget, ÅssignableBaseCurrencyMinimumBidPrice, ÅssignableTargetCurrencyMinimumBidSize {
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
        static final String BaseCurrencyMinimumBidPrice = "baseCurrencyMinimumBidPrice";
        static final String TargetCurrencyMinimumBidSize = "targetCurrencyMinimumBidSize";
        static final String TargetCurrencyBidSizes = "targetCurrencyBidSizes";
        static final String BaseCurrencyScaleSize = "baseCurrencyScaleSize";
        static final String TargetCurrencyScaleSize = "targetCurrencyScaleSize";
        static final String AcquirableExecutionSize = "acquirableExecutionSize";
        static final String ExecutionLogger = "executionLogger";
    }
}
