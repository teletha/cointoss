package cointoss.order;

import cointoss.order.OrderResponse;
import cointoss.order.OrderResponseModel;
import cointoss.order.OrderResponseType;
import cointoss.util.arithmetic.Num;
import java.lang.Override;
import java.lang.StringBuilder;
import java.lang.Throwable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Objects;
import javax.annotation.processing.Generated;

/**
 * Generated model for {@link OrderResponseModel}.
 */
@Generated("Icy Manipulator")
public abstract class OrderResponse implements OrderResponseModel {

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
            Field field = OrderResponse.class.getDeclaredField(name);
            field.setAccessible(true);
            return MethodHandles.lookup().unreflectSetter(field);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /** The final property updater. */
    private static final MethodHandle typeUpdater = updater("type");

    /** The final property updater. */
    private static final MethodHandle sizeUpdater = updater("size");

    /** The final property updater. */
    private static final MethodHandle priceUpdater = updater("price");

    /** The exposed property. */
    public final OrderResponseType type;

    /** The exposed property. */
    public final Num size;

    /** The exposed property. */
    public final Num price;

    /**
     * HIDE CONSTRUCTOR
     */
    protected OrderResponse() {
        this.type = null;
        this.size = null;
        this.price = null;
    }

    /**
     * Return the type property.
     *
     * @return A value of type property.
     */
    @Override
    public final OrderResponseType type() {
        return this.type;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of type property.
     */
    @SuppressWarnings("unused")
    private final OrderResponseType getType() {
        return this.type;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of type property to assign.
     */
    private final void setType(OrderResponseType value) {
        if (value == null) {
            throw new IllegalArgumentException("The type property requires non-null value.");
        }
        try {
            typeUpdater.invoke(this, value);
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
    private final void setSize(Num value) {
        if (value == null) {
            throw new IllegalArgumentException("The size property requires non-null value.");
        }
        try {
            sizeUpdater.invoke(this, value);
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
        StringBuilder builder = new StringBuilder("OrderResponse [");
        builder.append("type=").append(type).append(", ");
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
        return Objects.hash(type, size, price);
    }

    /**
     * Returns true if the all properties are equal to each other and false otherwise. Consequently, if both properties are null, true is returned and if exactly one property is null, false is returned. Otherwise, equality is determined by using the equals method of the base model. 
     *
     * @return true if the all properties are equal to each other and false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof OrderResponse == false) {
            return false;
        }

        OrderResponse other = (OrderResponse) o;
        if (!Objects.equals(type, other.type)) return false;
        if (!Objects.equals(size, other.size)) return false;
        if (!Objects.equals(price, other.price)) return false;
        return true;
    }

    /** The singleton builder. */
    public static final  Ìnstantiator<?> with = new Ìnstantiator();

    /**
     * Namespace for {@link OrderResponse}  builder methods.
     */
    public static class Ìnstantiator<Self extends OrderResponse & ÅssignableÅrbitrary<Self>> {

        /**
         * Create new {@link OrderResponse} with the specified type property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableSize<ÅssignablePrice<Self>> type(OrderResponseType type) {
            Åssignable o = new Åssignable();
            o.type(type);
            return o;
        }

        /**
         * Create new {@link OrderResponse} with the specified type property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableSize<ÅssignablePrice<Self>> accepted() {
            Åssignable o = new Åssignable();
            o.accepted();
            return o;
        }

        /**
         * Create new {@link OrderResponse} with the specified type property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableSize<ÅssignablePrice<Self>> cancelled() {
            Åssignable o = new Åssignable();
            o.cancelled();
            return o;
        }

        /**
         * Create new {@link OrderResponse} with the specified type property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableSize<ÅssignablePrice<Self>> executed() {
            Åssignable o = new Åssignable();
            o.executed();
            return o;
        }

        /**
         * Create new {@link OrderResponse} with the specified type property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableSize<ÅssignablePrice<Self>> rejected() {
            Åssignable o = new Åssignable();
            o.rejected();
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
        default Next type(OrderResponseType value) {
            ((OrderResponse) this).setType(value);
            return (Next) this;
        }

        /**
         * Assign type property.
         * 
         * @return The next assignable model.
         */
        default Next accepted() {
            return type(OrderResponseType.Accepted);
        }

        /**
         * Assign type property.
         * 
         * @return The next assignable model.
         */
        default Next cancelled() {
            return type(OrderResponseType.Cancelled);
        }

        /**
         * Assign type property.
         * 
         * @return The next assignable model.
         */
        default Next executed() {
            return type(OrderResponseType.Executed);
        }

        /**
         * Assign type property.
         * 
         * @return The next assignable model.
         */
        default Next rejected() {
            return type(OrderResponseType.Rejected);
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
            ((OrderResponse) this).setSize(value);
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
            ((OrderResponse) this).setPrice(value);
            return (Next) this;
        }
    }

    /**
     * Property assignment API.
     */
    public static interface ÅssignableÅrbitrary<Next extends OrderResponse> {
    }

    /**
     * Internal aggregated API.
     */
    protected static interface ÅssignableAll extends ÅssignableType, ÅssignableSize, ÅssignablePrice {
    }

    /**
     * Mutable Model.
     */
    private static final class Åssignable extends OrderResponse implements ÅssignableAll, ÅssignableÅrbitrary {
    }

    /**
     * The identifier for properties.
     */
    static final class My {
        static final String Type = "type";
        static final String Size = "size";
        static final String Price = "price";
    }
}
