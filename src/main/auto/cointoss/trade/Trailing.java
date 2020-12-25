package cointoss.trade;

import cointoss.Market;
import cointoss.ticker.Span;
import cointoss.trade.Trailing;
import cointoss.trade.TrailingModel;
import cointoss.util.arithmetic.Num;
import java.lang.Override;
import java.lang.StringBuilder;
import java.lang.Throwable;
import java.lang.UnsupportedOperationException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Function;
import javax.annotation.processing.Generated;
import kiss.Signal;

/**
 * Generated model for {@link TrailingModel}.
 */
@Generated("Icy Manipulator")
public class Trailing extends TrailingModel {

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
            Method method = TrailingModel.class.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
            return MethodHandles.lookup().unreflect(method);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /** The overload or intercept method invoker. */
    private static final MethodHandle losscut$912239839= invoker("losscut", double.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle losscut$1093866057= invoker("losscut", long.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle losscut$2101382901= invoker("losscut", Num.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle profit$912239839= invoker("profit", double.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle profit$1093866057= invoker("profit", long.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle update$8298921= invoker("update", Span.class);

    /**
     * Create special property updater.
     *
     * @param name A target property name.
     * @return A special property updater.
     */
    private static final MethodHandle updater(String name)  {
        try {
            Field field = Trailing.class.getDeclaredField(name);
            field.setAccessible(true);
            return MethodHandles.lookup().unreflectSetter(field);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /** The final property updater. */
    private static final MethodHandle losscutUpdater = updater("losscut");

    /** The final property updater. */
    private static final MethodHandle profitUpdater = updater("profit");

    /** The final property updater. */
    private static final MethodHandle updateUpdater = updater("update");

    /** The exposed property. */
    public final Num losscut;

    /** The exposed property. */
    public final Num profit;

    /** The exposed property. */
    public final Function<Market, Signal<Num>> update;

    /**
     * HIDE CONSTRUCTOR
     */
    protected Trailing() {
        this.losscut = null;
        this.profit = super.profit();
        this.update = super.update();
    }

    /**
     * Setting the losscut price range.
     *  
     *  @return
     */
    @Override
    public final Num losscut() {
        return this.losscut;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of losscut property.
     */
    @SuppressWarnings("unused")
    private final Num getLosscut() {
        return this.losscut;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of losscut property to assign.
     */
    private final void setLosscut(Num value) {
        if (value == null) {
            throw new IllegalArgumentException("The losscut property requires non-null value.");
        }
        try {
            losscutUpdater.invoke(this, losscut$2101382901.invoke(this, value));
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Setting the profit price range.
     *  
     *  @return
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
    private final void setProfit(Num value) {
        if (value == null) {
            value = super.profit();
        }
        try {
            profitUpdater.invoke(this, value);
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Setting the price update timing
     *  
     *  @return
     */
    @Override
    public final Function<Market, Signal<Num>> update() {
        return this.update;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of update property.
     */
    @SuppressWarnings("unused")
    private final Function<Market, Signal<Num>> getUpdate() {
        return this.update;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of update property to assign.
     */
    private final void setUpdate(Function<Market, Signal<Num>> value) {
        if (value == null) {
            value = super.update();
        }
        try {
            updateUpdater.invoke(this, value);
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
        StringBuilder builder = new StringBuilder("Trailing [");
        builder.append("losscut=").append(losscut).append(", ");
        builder.append("profit=").append(profit).append(", ");
        builder.append("update=").append(update).append("]");
        return builder.toString();
    }

    /**
     * Generates a hash code for a sequence of property values. The hash code is generated as if all the property values were placed into an array, and that array were hashed by calling Arrays.hashCode(Object[]). 
     *
     * @return A hash value of the sequence of property values.
     */
    @Override
    public int hashCode() {
        return Objects.hash(losscut, profit, update);
    }

    /**
     * Returns true if the all properties are equal to each other and false otherwise. Consequently, if both properties are null, true is returned and if exactly one property is null, false is returned. Otherwise, equality is determined by using the equals method of the base model. 
     *
     * @return true if the all properties are equal to each other and false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof Trailing == false) {
            return false;
        }

        Trailing other = (Trailing) o;
        if (!Objects.equals(losscut, other.losscut)) return false;
        if (!Objects.equals(profit, other.profit)) return false;
        if (!Objects.equals(update, other.update)) return false;
        return true;
    }

    /** The singleton builder. */
    public static final  Ìnstantiator<?> with = new Ìnstantiator();

    /**
     * Namespace for {@link Trailing}  builder methods.
     */
    public static class Ìnstantiator<Self extends Trailing & ÅssignableÅrbitrary<Self>> {

        /**
         * Create new {@link Trailing} with the specified losscut property.
         * 
         * @return The next assignable model.
         */
        public Self losscut(Num losscut) {
            Åssignable o = new Åssignable();
            o.losscut(losscut);
            return (Self)o;
        }

        /**
         * Create new {@link Trailing} with the specified losscut property.
         * 
         * @return The next assignable model.
         */
        public Self losscut(double price) {
            Åssignable o = new Åssignable();
            o.losscut(price);
            return (Self)o;
        }

        /**
         * Create new {@link Trailing} with the specified losscut property.
         * 
         * @return The next assignable model.
         */
        public Self losscut(long price) {
            Åssignable o = new Åssignable();
            o.losscut(price);
            return (Self)o;
        }
    }

    /**
     * Property assignment API.
     */
    public static interface ÅssignableLosscut<Next> {

        /**
         * Assign losscut property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next losscut(Num value) {
            ((Trailing) this).setLosscut(value);
            return (Next) this;
        }

        /**
         * Assign losscut property.
         * 
         * @return The next assignable model.
         */
        default Next losscut(double price) {
            try {
                return losscut((Num) losscut$912239839.invoke(this, price));
            } catch (Throwable e) {
                throw quiet(e);
            }
        }

        /**
         * Assign losscut property.
         * 
         * @return The next assignable model.
         */
        default Next losscut(long price) {
            try {
                return losscut((Num) losscut$1093866057.invoke(this, price));
            } catch (Throwable e) {
                throw quiet(e);
            }
        }
    }

    /**
     * Property assignment API.
     */
    public static interface ÅssignableÅrbitrary<Next extends Trailing> {

        /**
         * Assign profit property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next profit(Num value) {
            ((Trailing) this).setProfit(value);
            return (Next) this;
        }

        /**
         * Assign profit property.
         * 
         * @return The next assignable model.
         */
        default Next profit(double price) {
            try {
                return profit((Num) profit$912239839.invoke(this, price));
            } catch (Throwable e) {
                throw quiet(e);
            }
        }

        /**
         * Assign profit property.
         * 
         * @return The next assignable model.
         */
        default Next profit(long price) {
            try {
                return profit((Num) profit$1093866057.invoke(this, price));
            } catch (Throwable e) {
                throw quiet(e);
            }
        }

        /**
         * Assign update property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next update(Function<Market, Signal<Num>> value) {
            ((Trailing) this).setUpdate(value);
            return (Next) this;
        }

        /**
         * Assign update property.
         * 
         * @return The next assignable model.
         */
        default Next update(Span span) {
            try {
                return update((Function<Market, Signal<Num>>) update$8298921.invoke(this, span));
            } catch (Throwable e) {
                throw quiet(e);
            }
        }
    }

    /**
     * Internal aggregated API.
     */
    protected static interface ÅssignableAll extends ÅssignableLosscut {
    }

    /**
     * Mutable Model.
     */
    private static final class Åssignable extends Trailing implements ÅssignableAll, ÅssignableÅrbitrary {
    }

    /**
     * The identifier for properties.
     */
    static final class My {
        static final String Losscut = "losscut";
        static final String Profit = "profit";
        static final String Update = "update";
    }
}
