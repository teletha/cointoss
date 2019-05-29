package cointoss.execution;

import cointoss.Direction;
import cointoss.execution.Execution;
import cointoss.execution.ExecutionModel;
import cointoss.util.Num;
import java.lang.Throwable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.ZonedDateTime;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.function.UnaryOperator;
import javax.annotation.processing.Generated;

/**
 * Generated model for {@link ExecutionModel}.
 */
@Generated("Icy Manipulator")
public abstract class Execution extends ExecutionModel {

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
            Method method = ExecutionModel.class.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
            return MethodHandles.lookup().unreflect(method);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /** The overload or intercept method invoker. */
    private static final MethodHandle size$926550818= invoker("size", int.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle size$1073516310= invoker("size", float.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle size$406915549= invoker("size", long.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle size$1933934151= invoker("size", double.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle assignWithAccumulative$2067028491= invoker("assignWithAccumulative", Num.class, Consumer.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle price$926550818= invoker("price", int.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle price$406915549= invoker("price", long.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle price$1073516310= invoker("price", float.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle price$1933934151= invoker("price", double.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle date$10485054= invoker("date", int.class, int.class, int.class, int.class, int.class, int.class, int.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle assignWithMills$1926139999= invoker("assignWithMills", ZonedDateTime.class, LongConsumer.class);

    /**
     * Create special property updater.
     *
     * @param name A target property name.
     * @return A special property updater.
     */
    private static final MethodHandle updater(String name)  {
        try {
            Field field = Execution.class.getDeclaredField(name);
            field.setAccessible(true);
            return MethodHandles.lookup().unreflectSetter(field);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /** The final property updater. */
    private static final MethodHandle directionUpdater = updater("direction");

    /** The final property updater. */
    private static final MethodHandle sizeUpdater = updater("size");

    /** The final property updater. */
    private static final MethodHandle idUpdater = updater("id");

    /** The final property updater. */
    private static final MethodHandle priceUpdater = updater("price");

    /** The final property updater. */
    private static final MethodHandle accumulativeUpdater = updater("accumulative");

    /** The final property updater. */
    private static final MethodHandle dateUpdater = updater("date");

    /** The final property updater. */
    private static final MethodHandle millsUpdater = updater("mills");

    /** The final property updater. */
    private static final MethodHandle consecutiveUpdater = updater("consecutive");

    /** The final property updater. */
    private static final MethodHandle delayUpdater = updater("delay");

    /** The exposed property. */
    public final Direction direction;

    /** The exposed property. */
    public final Num size;

    /** The exposed property. */
    public final long id;

    /** The exposed property. */
    public final Num price;

    /** The exposed property. */
    public final Num accumulative;

    /** The exposed property. */
    public final ZonedDateTime date;

    /** The exposed property. */
    public final long mills;

    /** The exposed property. */
    public final int consecutive;

    /** The exposed property. */
    public final int delay;

    /**
     * HIDE CONSTRUCTOR
     */
    protected Execution() {
        this.direction = null;
        this.size = null;
        this.id = super.id();
        this.price = super.price();
        this.accumulative = super.accumulative();
        this.date = super.date();
        this.mills = super.mills();
        this.consecutive = super.consecutive();
        this.delay = super.delay();
    }

    /**
     * Execution {@link Direction}.
     *  
     *  @return
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
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Size.
     *  
     *  @return
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
            sizeUpdater.invoke(this, assignWithAccumulative$2067028491.invoke(this, value, (Consumer<Num>) ((Åssignable) this)::accumulative));
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Execution id.
     *  
     *  @return
     */
    @Override
    public final long id() {
        return this.id;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of id property.
     */
    @SuppressWarnings("unused")
    private final long getId() {
        return this.id;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of id property to assign.
     */
    private final void setId(long value) {
        try {
            idUpdater.invoke(this, value);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Provide accesser to super default value.
     *
     * @return A default value.
     */
    private final long åccessToDefaultId() {
        return super.id();
    }

    /**
     * Exectution price.
     *  
     *  @return
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
            value = ((Execution) this).åccessToDefaultPrice();
        }
        try {
            priceUpdater.invoke(this, value);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Provide accesser to super default value.
     *
     * @return A default value.
     */
    private final Num åccessToDefaultPrice() {
        return super.price();
    }

    /**
     * Size.
     *  
     *  @return
     */
    @Override
    public final Num accumulative() {
        return this.accumulative;
    }

    /**
     * Assign the new value of accumulative property.
     *
     * @paran value The accumulative property assigner which accepts the current value and returns new value.
     * @return Chainable API.
     */
    public final Execution accumulative(UnaryOperator<Num> value) {
        setAccumulative(value.apply(this.accumulative));
        return this;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of accumulative property.
     */
    @SuppressWarnings("unused")
    private final Num getAccumulative() {
        return this.accumulative;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of accumulative property to assign.
     */
    private final void setAccumulative(Num value) {
        if (value == null) {
            value = ((Execution) this).åccessToDefaultAccumulative();
        }
        try {
            accumulativeUpdater.invoke(this, value);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Provide accesser to super default value.
     *
     * @return A default value.
     */
    private final Num åccessToDefaultAccumulative() {
        return super.accumulative();
    }

    /**
     * Accessor for {@link #price}.
     *  
     *  @return
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
    private final void setDate(ZonedDateTime value) {
        if (value == null) {
            value = ((Execution) this).åccessToDefaultDate();
        }
        try {
            dateUpdater.invoke(this, assignWithMills$1926139999.invoke(this, value, (LongConsumer) ((Åssignable) this)::mills));
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Provide accesser to super default value.
     *
     * @return A default value.
     */
    private final ZonedDateTime åccessToDefaultDate() {
        return super.date();
    }

    /**
     * Accessor for {@link #price}.
     *  
     *  @return
     */
    @Override
    public final long mills() {
        return this.mills;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of mills property.
     */
    @SuppressWarnings("unused")
    private final long getMills() {
        return this.mills;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of mills property to assign.
     */
    private final void setMills(long value) {
        try {
            millsUpdater.invoke(this, value);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Provide accesser to super default value.
     *
     * @return A default value.
     */
    private final long åccessToDefaultMills() {
        return super.mills();
    }

    /**
     * Accessor for {@link #price}.
     *  
     *  @return
     */
    @Override
    public final int consecutive() {
        return this.consecutive;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of consecutive property.
     */
    @SuppressWarnings("unused")
    private final int getConsecutive() {
        return this.consecutive;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of consecutive property to assign.
     */
    private final void setConsecutive(int value) {
        try {
            consecutiveUpdater.invoke(this, value);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Provide accesser to super default value.
     *
     * @return A default value.
     */
    private final int åccessToDefaultConsecutive() {
        return super.consecutive();
    }

    /**
     * Accessor for {@link #price}.
     *  
     *  @return
     */
    @Override
    public final int delay() {
        return this.delay;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of delay property.
     */
    @SuppressWarnings("unused")
    private final int getDelay() {
        return this.delay;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of delay property to assign.
     */
    private final void setDelay(int value) {
        try {
            delayUpdater.invoke(this, value);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Provide accesser to super default value.
     *
     * @return A default value.
     */
    private final int åccessToDefaultDelay() {
        return super.delay();
    }

    /** The singleton builder. */
    public static final  Ìnstantiator<?> with = new Ìnstantiator();

    /**
     * Namespace for {@link Execution}  builder methods.
     */
    public static final class Ìnstantiator<Self extends Execution & ÅssignableÅrbitrary<Self>> {

        /**
         * Create new {@link Execution} with the specified direction property.
         * 
         * @return The next assignable model.
         */
        public final Self direction(Direction direction, Num size) {
            Åssignable o = new Åssignable();
            o.direction(direction);
            o.size(size);
            return (Self) o;
        }

        /**
         * Create new {@link Execution} with the specified direction property.
         * 
         * @return The next assignable model.
         */
        public final Self direction(Direction direction, int size) {
            Åssignable o = new Åssignable();
            o.direction(direction);
            o.size(size);
            return (Self) o;
        }

        /**
         * Create new {@link Execution} with the specified direction property.
         * 
         * @return The next assignable model.
         */
        public final Self direction(Direction direction, float size) {
            Åssignable o = new Åssignable();
            o.direction(direction);
            o.size(size);
            return (Self) o;
        }

        /**
         * Create new {@link Execution} with the specified direction property.
         * 
         * @return The next assignable model.
         */
        public final Self direction(Direction direction, long size) {
            Åssignable o = new Åssignable();
            o.direction(direction);
            o.size(size);
            return (Self) o;
        }

        /**
         * Create new {@link Execution} with the specified direction property.
         * 
         * @return The next assignable model.
         */
        public final Self direction(Direction direction, double size) {
            Åssignable o = new Åssignable();
            o.direction(direction);
            o.size(size);
            return (Self) o;
        }

        /** Set direction property with BUY. */
        public final Self buy(Num size) {
            Åssignable o = new Åssignable();
            o.buy();
            o.size(size);
            return (Self) o;
        }

        /** Set direction property with BUY. */
        public final Self buy(int size) {
            Åssignable o = new Åssignable();
            o.buy();
            o.size(size);
            return (Self) o;
        }

        /** Set direction property with BUY. */
        public final Self buy(float size) {
            Åssignable o = new Åssignable();
            o.buy();
            o.size(size);
            return (Self) o;
        }

        /** Set direction property with BUY. */
        public final Self buy(long size) {
            Åssignable o = new Åssignable();
            o.buy();
            o.size(size);
            return (Self) o;
        }

        /** Set direction property with BUY. */
        public final Self buy(double size) {
            Åssignable o = new Åssignable();
            o.buy();
            o.size(size);
            return (Self) o;
        }

        /** Set direction property with SELL. */
        public final Self sell(Num size) {
            Åssignable o = new Åssignable();
            o.sell();
            o.size(size);
            return (Self) o;
        }

        /** Set direction property with SELL. */
        public final Self sell(int size) {
            Åssignable o = new Åssignable();
            o.sell();
            o.size(size);
            return (Self) o;
        }

        /** Set direction property with SELL. */
        public final Self sell(float size) {
            Åssignable o = new Åssignable();
            o.sell();
            o.size(size);
            return (Self) o;
        }

        /** Set direction property with SELL. */
        public final Self sell(long size) {
            Åssignable o = new Åssignable();
            o.sell();
            o.size(size);
            return (Self) o;
        }

        /** Set direction property with SELL. */
        public final Self sell(double size) {
            Åssignable o = new Åssignable();
            o.sell();
            o.size(size);
            return (Self) o;
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
            ((Execution) this).setDirection(value);
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
    public static interface ÅssignableSize<Next> {

        /**
         * Assign size property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next size(Num value) {
            ((Execution) this).setSize(value);
            return (Next) this;
        }

        /**
         * Set executed size by value.
         *  
         *  @param size An executed size.
         *  @return Chainable API.
         */
        default Next size(int size) {
            try {
                return size((Num) size$926550818.invoke(this, size));
            } catch (Throwable e) {
                throw quiet(e);
            }
        }

        /**
         * Set executed size by value.
         *  
         *  @param size An executed size.
         *  @return Chainable API.
         */
        default Next size(float size) {
            try {
                return size((Num) size$1073516310.invoke(this, size));
            } catch (Throwable e) {
                throw quiet(e);
            }
        }

        /**
         * Set executed size by value.
         *  
         *  @param size An executed size.
         *  @return Chainable API.
         */
        default Next size(long size) {
            try {
                return size((Num) size$406915549.invoke(this, size));
            } catch (Throwable e) {
                throw quiet(e);
            }
        }

        /**
         * Set executed size by value.
         *  
         *  @param size An executed size.
         *  @return Chainable API.
         */
        default Next size(double size) {
            try {
                return size((Num) size$1933934151.invoke(this, size));
            } catch (Throwable e) {
                throw quiet(e);
            }
        }
    }

    /**
     * Property assignment API.
     */
    public static interface ÅssignableÅrbitrary<Next extends Execution> {

        /**
         * Assign id property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next id(long value) {
            ((Execution) this).setId(value);
            return (Next) this;
        }

        /**
         * Assign price property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next price(Num value) {
            ((Execution) this).setPrice(value);
            return (Next) this;
        }

        /**
         * Set price by value.
         *  
         *  @param price A price.
         *  @return Chainable API.
         */
        default Next price(int price) {
            try {
                return price((Num) price$926550818.invoke(this, price));
            } catch (Throwable e) {
                throw quiet(e);
            }
        }

        /**
         * Set price by value.
         *  
         *  @param price A price.
         *  @return Chainable API.
         */
        default Next price(long price) {
            try {
                return price((Num) price$406915549.invoke(this, price));
            } catch (Throwable e) {
                throw quiet(e);
            }
        }

        /**
         * Set price by value.
         *  
         *  @param price A price.
         *  @return Chainable API.
         */
        default Next price(float price) {
            try {
                return price((Num) price$1073516310.invoke(this, price));
            } catch (Throwable e) {
                throw quiet(e);
            }
        }

        /**
         * Set price by value.
         *  
         *  @param price A price.
         *  @return Chainable API.
         */
        default Next price(double price) {
            try {
                return price((Num) price$1933934151.invoke(this, price));
            } catch (Throwable e) {
                throw quiet(e);
            }
        }

        /**
         * Assign accumulative property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next accumulative(Num value) {
            ((Execution) this).setAccumulative(value);
            return (Next) this;
        }

        /**
         * Assign date property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next date(ZonedDateTime value) {
            ((Execution) this).setDate(value);
            return (Next) this;
        }

        /**
         * Assign executed date.
         *  
         *  @param year Year.
         *  @param month Month.
         *  @param day Day of month.
         *  @param hour Hour.
         *  @param minute Minute.
         *  @param second Second.
         *  @param ms Mill second.
         *  @return
         */
        default Next date(int year, int month, int day, int hour, int minute, int second, int ms) {
            try {
                return date((ZonedDateTime) date$10485054.invoke(this, year, month, day, hour, minute, second, ms));
            } catch (Throwable e) {
                throw quiet(e);
            }
        }

        /**
         * Assign mills property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next mills(long value) {
            ((Execution) this).setMills(value);
            return (Next) this;
        }

        /**
         * Assign consecutive property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next consecutive(int value) {
            ((Execution) this).setConsecutive(value);
            return (Next) this;
        }

        /**
         * Assign delay property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next delay(int value) {
            ((Execution) this).setDelay(value);
            return (Next) this;
        }
    }

    /**
     * Internal aggregated API.
     */
    protected static interface ÅssignableAll extends ÅssignableDirection, ÅssignableSize {
    }

    /**
     * Mutable Model.
     */
    private static final class Åssignable extends Execution implements ÅssignableAll, ÅssignableÅrbitrary {
    }

    /**
     * The identifier for properties.
     */
    static final class My {
        static final String Direction = "direction";
        static final String Size = "size";
        static final String Id = "id";
        static final String Price = "price";
        static final String Accumulative = "accumulative";
        static final String Date = "date";
        static final String Mills = "mills";
        static final String Consecutive = "consecutive";
        static final String Delay = "delay";
    }
}
