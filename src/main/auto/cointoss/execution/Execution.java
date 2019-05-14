package cointoss.execution;

import cointoss.Direction;
import cointoss.util.Num;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.ZonedDateTime;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import javax.annotation.processing.Generated;

/**
 * Generated model for {@link ExecutionModel}.
 */
@Generated("Icy Manipulator")
public abstract class Execution extends ExecutionModel {

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
            throw new Error(e);
        }
    }

    /** The overload or intercept method invoker. */
    private static final MethodHandle size$101282980= invoker("size", int.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle size$765519919= invoker("size", float.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle size$1096207375= invoker("size", long.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle size$1360390150= invoker("size", double.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle initializeCumulativeSize$393740119= invoker("initializeCumulativeSize", Num.class, Consumer.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle price$101282980= invoker("price", int.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle price$1096207375= invoker("price", long.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle price$765519919= invoker("price", float.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle price$1360390150= invoker("price", double.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle date$852566916= invoker("date", int.class, int.class, int.class, int.class, int.class, int.class, int.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle mills$1373199327= invoker("mills", ZonedDateTime.class, LongConsumer.class);

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
            throw new Error(e);
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
    private static final MethodHandle cumulativeSizeUpdater = updater("cumulativeSize");

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
    public Num cumulativeSize;

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
        this.cumulativeSize = super.cumulativeSize();
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
    @SuppressWarnings("unused")
    private void setDirection(Direction value) {
        ((ÅssignableDirection) this).direction(value);
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
    @SuppressWarnings("unused")
    private void setSize(Num value) {
        ((ÅssignableSize) this).size(value);
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
    @SuppressWarnings("unused")
    private void setId(long value) {
        ((ÅssignableÅrbitrary) this).id(value);
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
    @SuppressWarnings("unused")
    private void setPrice(Num value) {
        ((ÅssignableÅrbitrary) this).price(value);
    }

    /**
     * Size.
     *  
     *  @return
     */
    @Override
    public final Num cumulativeSize() {
        return this.cumulativeSize;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of cumulativeSize property.
     */
    @SuppressWarnings("unused")
    private final Num getCumulativeSize() {
        return this.cumulativeSize;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of cumulativeSize property to assign.
     */
    @SuppressWarnings("unused")
    private void setCumulativeSize(Num value) {
        ((ÅssignableÅrbitrary) this).cumulativeSize(value);
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
    @SuppressWarnings("unused")
    private void setDate(ZonedDateTime value) {
        ((ÅssignableÅrbitrary) this).date(value);
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
    @SuppressWarnings("unused")
    private void setMills(long value) {
        ((ÅssignableÅrbitrary) this).mills(value);
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
    @SuppressWarnings("unused")
    private void setConsecutive(int value) {
        ((ÅssignableÅrbitrary) this).consecutive(value);
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
    @SuppressWarnings("unused")
    private void setDelay(int value) {
        ((ÅssignableÅrbitrary) this).delay(value);
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
        public final Self direction(Direction direction, Num num) {
            Åssignable o = new Åssignable();
            o.direction(direction);
            o.size(num);
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

        /** Set direction property with Direction.BUY */
        public final Self buy(Num num) {
            Åssignable o = new Åssignable();
            o.buy();
            o.size(num);
            return (Self) o;
        }

        /** Set direction property with Direction.BUY */
        public final Self buy(int size) {
            Åssignable o = new Åssignable();
            o.buy();
            o.size(size);
            return (Self) o;
        }

        /** Set direction property with Direction.BUY */
        public final Self buy(float size) {
            Åssignable o = new Åssignable();
            o.buy();
            o.size(size);
            return (Self) o;
        }

        /** Set direction property with Direction.BUY */
        public final Self buy(long size) {
            Åssignable o = new Åssignable();
            o.buy();
            o.size(size);
            return (Self) o;
        }

        /** Set direction property with Direction.BUY */
        public final Self buy(double size) {
            Åssignable o = new Åssignable();
            o.buy();
            o.size(size);
            return (Self) o;
        }

        /** Set direction property with Direction.SELL */
        public final Self sell(Num num) {
            Åssignable o = new Åssignable();
            o.sell();
            o.size(num);
            return (Self) o;
        }

        /** Set direction property with Direction.SELL */
        public final Self sell(int size) {
            Åssignable o = new Åssignable();
            o.sell();
            o.size(size);
            return (Self) o;
        }

        /** Set direction property with Direction.SELL */
        public final Self sell(float size) {
            Åssignable o = new Åssignable();
            o.sell();
            o.size(size);
            return (Self) o;
        }

        /** Set direction property with Direction.SELL */
        public final Self sell(long size) {
            Åssignable o = new Åssignable();
            o.sell();
            o.size(size);
            return (Self) o;
        }

        /** Set direction property with Direction.SELL */
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
            try {
                directionUpdater.invoke(this, value);
            } catch (Throwable e) {
                throw new Error(e);
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
    public static interface ÅssignableSize<Next> {

        /**
         * Assign size property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next size(Num value) {
            try {
                sizeUpdater.invoke(this, initializeCumulativeSize$393740119.invoke(this, value, (Consumer<Num>) ((Åssignable) this)::cumulativeSize));
            } catch (Throwable e) {
                throw new Error(e);
            }
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
                return size((Num) size$101282980.invoke(this, size));
            } catch (Throwable e) {
                throw new Error(e);
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
                return size((Num) size$765519919.invoke(this, size));
            } catch (Throwable e) {
                throw new Error(e);
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
                return size((Num) size$1096207375.invoke(this, size));
            } catch (Throwable e) {
                throw new Error(e);
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
                return size((Num) size$1360390150.invoke(this, size));
            } catch (Throwable e) {
                throw new Error(e);
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
            try {
                idUpdater.invoke(this, value);
            } catch (Throwable e) {
                throw new Error(e);
            }
            return (Next) this;
        }

        /**
         * Assign price property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next price(Num value) {
            try {
                priceUpdater.invoke(this, value);
            } catch (Throwable e) {
                throw new Error(e);
            }
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
                return price((Num) price$101282980.invoke(this, price));
            } catch (Throwable e) {
                throw new Error(e);
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
                return price((Num) price$1096207375.invoke(this, price));
            } catch (Throwable e) {
                throw new Error(e);
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
                return price((Num) price$765519919.invoke(this, price));
            } catch (Throwable e) {
                throw new Error(e);
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
                return price((Num) price$1360390150.invoke(this, price));
            } catch (Throwable e) {
                throw new Error(e);
            }
        }

        /**
         * Assign cumulativeSize property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next cumulativeSize(Num value) {
            try {
                cumulativeSizeUpdater.invoke(this, value);
            } catch (Throwable e) {
                throw new Error(e);
            }
            return (Next) this;
        }

        /**
         * Assign date property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next date(ZonedDateTime value) {
            try {
                dateUpdater.invoke(this, mills$1373199327.invoke(this, value, (LongConsumer) ((Åssignable) this)::mills));
            } catch (Throwable e) {
                throw new Error(e);
            }
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
                return date((ZonedDateTime) date$852566916.invoke(this, year, month, day, hour, minute, second, ms));
            } catch (Throwable e) {
                throw new Error(e);
            }
        }

        /**
         * Assign mills property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next mills(long value) {
            try {
                millsUpdater.invoke(this, value);
            } catch (Throwable e) {
                throw new Error(e);
            }
            return (Next) this;
        }

        /**
         * Assign consecutive property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next consecutive(int value) {
            try {
                consecutiveUpdater.invoke(this, value);
            } catch (Throwable e) {
                throw new Error(e);
            }
            return (Next) this;
        }

        /**
         * Assign delay property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next delay(int value) {
            try {
                delayUpdater.invoke(this, value);
            } catch (Throwable e) {
                throw new Error(e);
            }
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
        static final String CumulativeSize = "cumulativeSize";
        static final String Date = "date";
        static final String Mills = "mills";
        static final String Consecutive = "consecutive";
        static final String Delay = "delay";
    }
}
