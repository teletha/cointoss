package cointoss.order;

import cointoss.Direction;
import cointoss.util.Num;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.time.ZonedDateTime;
import javax.annotation.processing.Generated;

/**
 * Generated model for {@link PositionModel}.
 */
@Generated("Icy Manipulator")
public abstract class Position extends PositionModel {

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
            Field field = Position.class.getDeclaredField(name);
            field.setAccessible(true);
            return MethodHandles.lookup().unreflectSetter(field);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /** The final property updater. */
    private static final MethodHandle directionUpdater = updater("direction");

    /** The final property updater. */
    private static final MethodHandle priceUpdater = updater("price");

    /** The final property updater. */
    private static final MethodHandle sizeUpdater = updater("size");

    /** The final property updater. */
    private static final MethodHandle dateUpdater = updater("date");

    /** The final property updater. */
    private static final MethodHandle profitUpdater = updater("profit");

    /** The exposed property. */
    public final Direction direction;

    /** The exposed property. */
    public final Num price;

    /** The exposed property. */
    public final Num size;

    /** The exposed property. */
    public final ZonedDateTime date;

    /** The exposed property. */
    public final Num profit;

    /**
     * HIDE CONSTRUCTOR
     */
    protected Position() {
        this.direction = null;
        this.price = null;
        this.size = null;
        this.date = null;
        this.profit = super.profit();
    }

    /** {@inheritDoc} */
    @Override
    public final Direction direction() {
        return this.direction;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of direction property.
     */
    @SuppressWarnings("unused")
    private final Direction getDirection() {
        return this.direction;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of direction property to assign.
     */
    final void setDirection(Direction value) {
        ((ÅssignableDirection) this).direction(value);
    }

    /**
     * Return the price property.
     *
     * @return A value of price property.
     */
    @Override
    public final Num price() {
        return this.price;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of price property.
     */
    @SuppressWarnings("unused")
    private final Num getPrice() {
        return this.price;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of price property to assign.
     */
    final void setPrice(Num value) {
        ((ÅssignablePrice) this).price(value);
    }

    /**
     * Return the size property.
     *
     * @return A value of size property.
     */
    @Override
    public final Num size() {
        return this.size;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of size property.
     */
    @SuppressWarnings("unused")
    private final Num getSize() {
        return this.size;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of size property to assign.
     */
    final void setSize(Num value) {
        ((ÅssignableSize) this).size(value);
    }

    /**
     * Return the date property.
     *
     * @return A value of date property.
     */
    @Override
    public final ZonedDateTime date() {
        return this.date;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of date property.
     */
    @SuppressWarnings("unused")
    private final ZonedDateTime getDate() {
        return this.date;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of date property to assign.
     */
    final void setDate(ZonedDateTime value) {
        ((ÅssignableDate) this).date(value);
    }

    /**
     * Return the profit property.
     *
     * @return A value of profit property.
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
        ((ÅssignableÅrbitrary) this).profit(value);
    }

    /**
     * Provide accesser to super default value.
     *
     * @return A default value.
     */
    private final Num åccessToDefaultProfit() {
        return super.profit();
    }

    /** The singleton builder. */
    public static final  Ìnstantiator<?> with = new Ìnstantiator();

    /**
     * Namespace for {@link Position}  builder methods.
     */
    public static final class Ìnstantiator<Self extends Position & ÅssignableÅrbitrary<Self>> {

        /**
         * Create new {@link Position} with the specified direction property.
         * 
         * @return The next assignable model.
         */
        public final <T extends ÅssignablePrice<ÅssignableSize<ÅssignableDate<Self>>>> T direction(Direction direction) {
            Åssignable o = new Åssignable();
            o.direction(direction);
            return (T) o;
        }

        /** Set direction property with Direction.BUY */
        public final <T extends ÅssignablePrice<ÅssignableSize<ÅssignableDate<Self>>>> T buy() {
            Åssignable o = new Åssignable();
            o.buy();
            return (T) o;
        }

        /** Set direction property with Direction.SELL */
        public final <T extends ÅssignablePrice<ÅssignableSize<ÅssignableDate<Self>>>> T sell() {
            Åssignable o = new Åssignable();
            o.sell();
            return (T) o;
        }
    }

    /**
     * Property assignment API.
     */
    public static interface ÅssignableDirection<Next> {

        /**
         * Assign direction property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next direction(Direction value) {
            if (value == null) {
                throw new IllegalArgumentException("The direction property requires non-null value.");
            }
            try {
                directionUpdater.invoke(this, value);
            } catch (Throwable e) {
                throw quiet(e);
            }
            return (Next) this;
        }

        /**
         * Assign {@link Direction#BUY} to direction property.
         * 
         * @return The next assignable model.
         */
        default Next buy() {
            return direction(Direction.BUY);
        }

        /**
         * Assign {@link Direction#SELL} to direction property.
         * 
         * @return The next assignable model.
         */
        default Next sell() {
            return direction(Direction.SELL);
        }
    }

    /**
     * Property assignment API.
     */
    public static interface ÅssignablePrice<Next> {

        /**
         * Assign price property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next price(Num value) {
            if (value == null) {
                throw new IllegalArgumentException("The price property requires non-null value.");
            }
            try {
                priceUpdater.invoke(this, value);
            } catch (Throwable e) {
                throw quiet(e);
            }
            return (Next) this;
        }
    }

    /**
     * Property assignment API.
     */
    public static interface ÅssignableSize<Next> {

        /**
         * Assign size property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next size(Num value) {
            if (value == null) {
                throw new IllegalArgumentException("The size property requires non-null value.");
            }
            try {
                sizeUpdater.invoke(this, value);
            } catch (Throwable e) {
                throw quiet(e);
            }
            return (Next) this;
        }
    }

    /**
     * Property assignment API.
     */
    public static interface ÅssignableDate<Next> {

        /**
         * Assign date property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next date(ZonedDateTime value) {
            if (value == null) {
                throw new IllegalArgumentException("The date property requires non-null value.");
            }
            try {
                dateUpdater.invoke(this, value);
            } catch (Throwable e) {
                throw quiet(e);
            }
            return (Next) this;
        }
    }

    /**
     * Property assignment API.
     */
    public static interface ÅssignableÅrbitrary<Next extends Position> {

        /**
         * Assign profit property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next profit(Num value) {
            if (value == null) {
                value = ((Position) this).åccessToDefaultProfit();
            }
            try {
                profitUpdater.invoke(this, value);
            } catch (Throwable e) {
                throw quiet(e);
            }
            return (Next) this;
        }
    }

    /**
     * Internal aggregated API.
     */
    protected static interface ÅssignableAll extends ÅssignableDirection, ÅssignablePrice, ÅssignableSize, ÅssignableDate {
    }

    /**
     * Mutable Model.
     */
    private static final class Åssignable extends Position implements ÅssignableAll, ÅssignableÅrbitrary {
    }

    /**
     * The identifier for properties.
     */
    static final class My {
        static final String Direction = "direction";
        static final String Price = "price";
        static final String Size = "size";
        static final String Date = "date";
        static final String Profit = "profit";
    }
}
