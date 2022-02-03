package cointoss.ticker.data;

import cointoss.Direction;
import cointoss.ticker.data.Liquidation;
import cointoss.util.arithmetic.Num;
import java.lang.Override;
import java.lang.StringBuilder;
import java.lang.Throwable;
import java.lang.UnsupportedOperationException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Generated model for {@link LiquidationModel}.
 * 
 * @see <a href="https://github.com/teletha/icymanipulator">Icy Manipulator (Code Generator)</a>
 */
public class Liquidation implements LiquidationModel {

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
            Field field = Liquidation.class.getDeclaredField(name);
            field.setAccessible(true);
            return MethodHandles.lookup().unreflectSetter(field);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /** The final property updater. */
    private static final MethodHandle dateUpdater = updater("date");

    /** The final property updater. */
    private static final MethodHandle directionUpdater = updater("direction");

    /** The final property updater. */
    private static final MethodHandle sizeUpdater = updater("size");

    /** The final property updater. */
    private static final MethodHandle priceUpdater = updater("price");

    /** The exposed property. */
    public final ZonedDateTime date;

    /** The exposed property. */
    public final Direction direction;

    /** The exposed property. */
    public final double size;

    /** The exposed property. */
    public final Num price;

    /**
     * HIDE CONSTRUCTOR
     */
    protected Liquidation() {
        this.date = null;
        this.direction = null;
        this.size = 0D;
        this.price = null;
    }

    /** {@inheritDoc} */
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
    private final void setDate(ZonedDateTime value) {
        if (value == null) {
            throw new IllegalArgumentException("The date property requires non-null value.");
        }
        try {
            dateUpdater.invoke(this, value);
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Return the direction property.
     *
     * @return A value of direction property.
     */
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
    private final void setDirection(Direction value) {
        if (value == null) {
            throw new IllegalArgumentException("The direction property requires non-null value.");
        }
        try {
            directionUpdater.invoke(this, value);
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Return the size property.
     *
     * @return A value of size property.
     */
    @Override
    public final double size() {
        return this.size;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of size property.
     */
    @SuppressWarnings("unused")
    private final double getSize() {
        return this.size;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of size property to assign.
     */
    private final void setSize(double value) {
        try {
            sizeUpdater.invoke(this, value);
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
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
    private final void setPrice(Num value) {
        if (value == null) {
            throw new IllegalArgumentException("The price property requires non-null value.");
        }
        try {
            priceUpdater.invoke(this, value);
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
        StringBuilder builder = new StringBuilder("Liquidation [");
        builder.append("date=").append(date).append(", ");
        builder.append("direction=").append(direction).append(", ");
        builder.append("size=").append(size).append(", ");
        builder.append("price=").append(price).append("]");
        return builder.toString();
    }

    /**
     * Generates a hash code for a sequence of property values. The hash code is generated as if all the property values were placed into an array, and that array were hashed by calling Arrays.hashCode(Object[]). 
     *
     * @return A hash value of the sequence of property values.
     */
    @Override
    public int hashCode() {
        return Objects.hash(date, direction, size, price);
    }

    /**
     * Returns true if the all properties are equal to each other and false otherwise. Consequently, if both properties are null, true is returned and if exactly one property is null, false is returned. Otherwise, equality is determined by using the equals method of the base model. 
     *
     * @return true if the all properties are equal to each other and false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof Liquidation == false) {
            return false;
        }

        Liquidation other = (Liquidation) o;
        if (!Objects.equals(date, other.date)) return false;
        if (!Objects.equals(direction, other.direction)) return false;
        if (size != other.size) return false;
        if (!Objects.equals(price, other.price)) return false;
        return true;
    }

    /** The singleton builder. */
    public static final  Ìnstantiator<?> with = new Ìnstantiator();

    /**
     * Namespace for {@link Liquidation}  builder methods.
     */
    public static class Ìnstantiator<Self extends Liquidation & ÅssignableÅrbitrary<Self>> {

        /**
         * Create new {@link Liquidation} with the specified date property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableDirection<ÅssignableSize<ÅssignablePrice<Self>>> date(ZonedDateTime date) {
            Åssignable o = new Åssignable();
            o.date(date);
            return o;
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
            ((Liquidation) this).setDate(value);
            return (Next) this;
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
            ((Liquidation) this).setDirection(value);
            return (Next) this;
        }

        /**
         * Assign direction property.
         * 
         * @return The next assignable model.
         */
        default Next buy() {
            return direction(Direction.BUY);
        }

        /**
         * Assign direction property.
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
    public static interface ÅssignableSize<Next> {

        /**
         * Assign size property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next size(double value) {
            ((Liquidation) this).setSize(value);
            return (Next) this;
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
            ((Liquidation) this).setPrice(value);
            return (Next) this;
        }
    }

    /**
     * Property assignment API.
     */
    public static interface ÅssignableÅrbitrary<Next extends Liquidation> {
    }

    /**
     * Internal aggregated API.
     */
    protected static interface ÅssignableAll extends ÅssignableDate, ÅssignableDirection, ÅssignableSize, ÅssignablePrice {
    }

    /**
     * Mutable Model.
     */
    private static final class Åssignable extends Liquidation implements ÅssignableAll, ÅssignableÅrbitrary {
    }

    /**
     * The identifier for properties.
     */
    static final class My {
        static final String Date = "date";
        static final String Direction = "direction";
        static final String Size = "size";
        static final String Price = "price";
    }
}
