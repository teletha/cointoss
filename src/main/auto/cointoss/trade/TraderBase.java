package cointoss.trade;

import cointoss.trade.TraderBase;
import cointoss.trade.TraderBaseModel;
import cointoss.util.ObservableNumProperty;
import cointoss.util.arithmetic.Num;
import java.lang.Override;
import java.lang.StringBuilder;
import java.lang.Throwable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import javax.annotation.processing.Generated;
import kiss.Signal;

/**
 * Generated model for {@link TraderBaseModel}.
 */
@Generated("Icy Manipulator")
public abstract class TraderBase extends TraderBaseModel {

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
            Field field = TraderBase.class.getDeclaredField(name);
            field.setAccessible(true);
            return MethodHandles.lookup().unreflectSetter(field);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /** The final property updater. */
    private static final MethodHandle holdSizeUpdater = updater("holdSize");

    /** The final property updater. */
    private static final MethodHandle holdMaxSizeUpdater = updater("holdMaxSize");

    /** The final property updater. */
    private static final MethodHandle profitUpdater = updater("profit");

    /** The exposed property. */
    public final Num holdSize;

    /** The property customizer. */
    private final ObservableNumProperty holdSizeCustomizer = new ObservableNumProperty() {

        @Override
        public Num get() {
            return holdSize;
        }
    };

    /** The exposed property. */
    public final Num holdMaxSize;

    /** The property customizer. */
    private final ObservableNumProperty holdMaxSizeCustomizer = new ObservableNumProperty() {

        @Override
        public Num get() {
            return holdMaxSize;
        }
    };

    /** The exposed property. */
    public final Num profit;

    /** The property customizer. */
    private final ObservableNumProperty profitCustomizer = new ObservableNumProperty() {

        @Override
        public Num get() {
            return profit;
        }
    };

    /**
     * HIDE CONSTRUCTOR
     */
    protected TraderBase() {
        this.holdSize = super.holdSize();
        this.holdMaxSize = super.holdMaxSize();
        this.profit = super.profit();
    }

    /**
     * Return the current hold size of target currency. Positive number means long position,
     *  negative number means short position. Zero means no position.
     *  
     *  @return A current hold size.
     */
    @Override
    public final Num holdSize() {
        return this.holdSize;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of holdSize property.
     */
    @SuppressWarnings("unused")
    private final Num getHoldSize() {
        return this.holdSize;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of holdSize property to assign.
     */
    final void setHoldSize(Num value) {
        if (value == null) {
            value = super.holdSize();
        }
        try {
            holdSizeUpdater.invoke(this, value);
            holdSizeCustomizer.accept(this.holdSize);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Observe property diff.
     *  
     *  @return
     */
    public final Signal<Num> observeHoldSizeDiff() {
        return holdSizeCustomizer.observe$Diff();
    }

    /**
     * Observe property modification.
     *  
     *  @return
     */
    public final Signal<Num> observeHoldSize() {
        return holdSizeCustomizer.observe$();
    }

    /**
     * Observe property modification with the current value.
     *  
     *  @return
     */
    public final Signal<Num> observeHoldSizeNow() {
        return holdSizeCustomizer.observe$Now();
    }

    /**
     * Return the maximum hold size of target currency. (historical data)
     *  
     *  @return A maximum hold size.
     */
    @Override
    public final Num holdMaxSize() {
        return this.holdMaxSize;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of holdMaxSize property.
     */
    @SuppressWarnings("unused")
    private final Num getHoldMaxSize() {
        return this.holdMaxSize;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of holdMaxSize property to assign.
     */
    final void setHoldMaxSize(Num value) {
        if (value == null) {
            value = super.holdMaxSize();
        }
        try {
            holdMaxSizeUpdater.invoke(this, value);
            holdMaxSizeCustomizer.accept(this.holdMaxSize);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Observe property diff.
     *  
     *  @return
     */
    public final Signal<Num> observeHoldMaxSizeDiff() {
        return holdMaxSizeCustomizer.observe$Diff();
    }

    /**
     * Observe property modification.
     *  
     *  @return
     */
    public final Signal<Num> observeHoldMaxSize() {
        return holdMaxSizeCustomizer.observe$();
    }

    /**
     * Observe property modification with the current value.
     *  
     *  @return
     */
    public final Signal<Num> observeHoldMaxSizeNow() {
        return holdMaxSizeCustomizer.observe$Now();
    }

    /**
     * Calculate the current profit and loss.
     *  
     *  @return A current profit and loss.
     */
    @Override
    public final Num profit() {
        return this.profit;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of profit property.
     */
    @SuppressWarnings("unused")
    private final Num getProfit() {
        return this.profit;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of profit property to assign.
     */
    final void setProfit(Num value) {
        if (value == null) {
            value = super.profit();
        }
        try {
            profitUpdater.invoke(this, value);
            profitCustomizer.accept(this.profit);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Observe property diff.
     *  
     *  @return
     */
    public final Signal<Num> observeProfitDiff() {
        return profitCustomizer.observe$Diff();
    }

    /**
     * Observe property modification.
     *  
     *  @return
     */
    public final Signal<Num> observeProfit() {
        return profitCustomizer.observe$();
    }

    /**
     * Observe property modification with the current value.
     *  
     *  @return
     */
    public final Signal<Num> observeProfitNow() {
        return profitCustomizer.observe$Now();
    }

    /**
     * Show all property values.
     *
     * @return All property values.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("TraderBase [");
        builder.append("holdSize=").append(holdSize).append(", ");
        builder.append("holdMaxSize=").append(holdMaxSize).append(", ");
        builder.append("profit=").append(profit).append("]");
        return builder.toString();
    }

    /** The singleton builder. */
    public static final  Ìnstantiator<?> with = new Ìnstantiator();

    /**
     * Namespace for {@link TraderBase}  builder methods.
     */
    public static class Ìnstantiator<Self extends TraderBase & ÅssignableÅrbitrary<Self>> {

        /**
         * Create initialized {@link TraderBase}.
         *
         * @return A initialized model.
         */
        public Self create() {
            return (Self) new Åssignable();
        }

        /**
         * Create initialized {@link TraderBase} with holdSize property.
         *
         * @param value A value to assign.
         * @return A initialized model.
         */
        public Self holdSize(Num value) {
            return create().holdSize(value);
        }

        /**
         * Create initialized {@link TraderBase} with holdMaxSize property.
         *
         * @param value A value to assign.
         * @return A initialized model.
         */
        public Self holdMaxSize(Num value) {
            return create().holdMaxSize(value);
        }

        /**
         * Create initialized {@link TraderBase} with profit property.
         *
         * @param value A value to assign.
         * @return A initialized model.
         */
        public Self profit(Num value) {
            return create().profit(value);
        }
    }

    /**
     * Property assignment API.
     */
    public static interface ÅssignableÅrbitrary<Next extends TraderBase> {

        /**
         * Assign holdSize property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next holdSize(Num value) {
            ((TraderBase) this).setHoldSize(value);
            return (Next) this;
        }

        /**
         * Assign holdMaxSize property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next holdMaxSize(Num value) {
            ((TraderBase) this).setHoldMaxSize(value);
            return (Next) this;
        }

        /**
         * Assign profit property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next profit(Num value) {
            ((TraderBase) this).setProfit(value);
            return (Next) this;
        }
    }

    /**
     * Internal aggregated API.
     */
    protected static interface ÅssignableAll {
    }

    /**
     * Mutable Model.
     */
    private static final class Åssignable extends TraderBase implements ÅssignableAll, ÅssignableÅrbitrary {
    }

    /**
     * The identifier for properties.
     */
    static final class My {
        static final String HoldSize = "holdSize";
        static final String HoldMaxSize = "holdMaxSize";
        static final String Profit = "profit";
    }
}
