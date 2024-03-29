package cointoss.execution;

import cointoss.Direction;
import cointoss.execution.Execution;
import hypatia.Num;
import hypatia.Orientational;
import java.lang.Override;
import java.lang.String;
import java.lang.Throwable;
import java.lang.UnsupportedOperationException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntUnaryOperator;
import java.util.function.LongConsumer;
import java.util.function.UnaryOperator;

/**
 * Generated model for {@link ExecutionModel}.
 * 
 * @see <a href="https://github.com/teletha/icymanipulator">Icy Manipulator (Code Generator)</a>
 */
public class Execution extends ExecutionModel {

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
            Method method = cointoss.execution.ExecutionModel.class.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
            return MethodHandles.lookup().unreflect(method);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /** The overload or intercept method invoker. */
    private static final MethodHandle direction$1158975041= invoker("direction", Orientational.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle size$101354429= invoker("size", int.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle size$698532521= invoker("size", float.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle size$1093866057= invoker("size", long.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle size$912239839= invoker("size", double.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle assignWithAccumulative$301451316= invoker("assignWithAccumulative", Num.class, DoubleConsumer.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle price$101354429= invoker("price", int.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle price$1093866057= invoker("price", long.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle price$698532521= invoker("price", float.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle price$912239839= invoker("price", double.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle date$1790350653= invoker("date", int.class, int.class, int.class, int.class, int.class, int.class, int.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle assignWithMills$1104079711= invoker("assignWithMills", ZonedDateTime.class, LongConsumer.class);

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
    private static final MethodHandle orientationUpdater = updater("orientation");

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

    /** The final property updater. */
    private static final MethodHandle infoUpdater = updater("info");

    /** The exposed property. */
    public final Direction orientation;

    /** The exposed property. */
    public final Num size;

    /** The exposed property. */
    public final long id;

    /** The exposed property. */
    public final Num price;

    /** The exposed property. */
    public final double accumulative;

    /** The exposed property. */
    public final ZonedDateTime date;

    /** The exposed property. */
    public final long mills;

    /** The exposed property. */
    public final int consecutive;

    /** The exposed property. */
    public final int delay;

    /** The exposed property. */
    public final String info;

    /**
     * HIDE CONSTRUCTOR
     */
    protected Execution() {
        this.orientation = null;
        this.size = null;
        this.id = super.id();
        this.price = super.price();
        this.accumulative = super.accumulative();
        this.date = super.date();
        this.mills = super.mills();
        this.consecutive = super.consecutive();
        this.delay = super.delay();
        this.info = super.info();
    }

    /**
     * Execution {@link Direction}.
     *  
     *  @return
     */
    @Override
    public final Direction orientation() {
        return this.orientation;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of orientation property.
     */
    @SuppressWarnings("unused")
    private final Direction getOrientation() {
        return this.orientation;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of orientation property to assign.
     */
    private final void setOrientation(Direction value) {
        if (value == null) {
            throw new IllegalArgumentException("The orientation property requires non-null value.");
        }
        try {
            orientationUpdater.invoke(this, value);
        } catch (UnsupportedOperationException e) {
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
            sizeUpdater.invoke(this, assignWithAccumulative$301451316.invoke(this, value, (DoubleConsumer) this::setAccumulative));
        } catch (UnsupportedOperationException e) {
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
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
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
            value = super.price();
        }
        try {
            priceUpdater.invoke(this, value);
        } catch (UnsupportedOperationException e) {
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
    public final double accumulative() {
        return this.accumulative;
    }

    /**
     * Assign the new value of accumulative property.
     *
     * @paran value The new accumulative property value to assign.
     * @return Chainable API.
     */
    public final Execution assignAccumulative(double value) {
        setAccumulative(value);
        return this;
    }

    /**
     * Assign the new value of accumulative property.
     *
     * @paran value The accumulative property assigner which accepts the current value and returns new value.
     * @return Chainable API.
     */
    public final Execution assignAccumulative(DoubleUnaryOperator value) {
        setAccumulative(value.applyAsDouble(this.accumulative));
        return this;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of accumulative property.
     */
    @SuppressWarnings("unused")
    private final double getAccumulative() {
        return this.accumulative;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of accumulative property to assign.
     */
    private final void setAccumulative(double value) {
        try {
            accumulativeUpdater.invoke(this, value);
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Executed date-time.
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
            value = super.date();
        }
        try {
            dateUpdater.invoke(this, assignWithMills$1104079711.invoke(this, value, (LongConsumer) this::setMills));
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /** {@inheritDoc} */
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
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Consecutive state.
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
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Delay time.
     *  
     *  @return
     */
    @Override
    public final int delay() {
        return this.delay;
    }

    /**
     * Assign the new value of delay property.
     *
     * @paran value The new delay property value to assign.
     * @return Chainable API.
     */
    public final Execution assignDelay(int value) {
        setDelay(value);
        return this;
    }

    /**
     * Assign the new value of delay property.
     *
     * @paran value The delay property assigner which accepts the current value and returns new value.
     * @return Chainable API.
     */
    public final Execution assignDelay(IntUnaryOperator value) {
        setDelay(value.applyAsInt(this.delay));
        return this;
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
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Accessor for {@link Execution} related info.
     *  
     *  @return
     */
    @Override
    public final String info() {
        return this.info;
    }

    /**
     * Assign the new value of info property.
     *
     * @paran value The new info property value to assign.
     * @return Chainable API.
     */
    public final Execution assignInfo(String value) {
        setInfo(value);
        return this;
    }

    /**
     * Assign the new value of info property.
     *
     * @paran value The info property assigner which accepts the current value and returns new value.
     * @return Chainable API.
     */
    public final Execution assignInfo(UnaryOperator<String> value) {
        setInfo(value.apply(this.info));
        return this;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of info property.
     */
    @SuppressWarnings("unused")
    private final String getInfo() {
        return this.info;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of info property to assign.
     */
    private final void setInfo(String value) {
        if (value == null) {
            value = super.info();
        }
        try {
            infoUpdater.invoke(this, value);
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Generates a hash code for a sequence of property values. The hash code is generated as if all the property values were placed into an array, and that array were hashed by calling Arrays.hashCode(Object[]). 
     *
     * @return A hash value of the sequence of property values.
     */
    @Override
    public int hashCode() {
        return Objects.hash(orientation, size, id, price, accumulative, date, mills, consecutive, delay, info);
    }

    /**
     * Returns true if the all properties are equal to each other and false otherwise. Consequently, if both properties are null, true is returned and if exactly one property is null, false is returned. Otherwise, equality is determined by using the equals method of the base model. 
     *
     * @return true if the all properties are equal to each other and false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof Execution == false) {
            return false;
        }

        Execution other = (Execution) o;
        if (!Objects.equals(orientation, other.orientation)) return false;
        if (!Objects.equals(size, other.size)) return false;
        if (id != other.id) return false;
        if (!Objects.equals(price, other.price)) return false;
        if (accumulative != other.accumulative) return false;
        if (!Objects.equals(date, other.date)) return false;
        if (mills != other.mills) return false;
        if (consecutive != other.consecutive) return false;
        if (delay != other.delay) return false;
        if (!Objects.equals(info, other.info)) return false;
        return true;
    }

    /**
     * Create new {@link Execution} with the specified property and copy other properties from this model.
     *
     * @param value A new value to assign.
     * @return A created new model instance.
     */
    public Execution withSize(Num value) {
        if (this.size == value) {
            return this;
        }
        return with.orientation(this.orientation, value).id(this.id).price(this.price).accumulative(this.accumulative).date(this.date).mills(this.mills).consecutive(this.consecutive).delay(this.delay).info(this.info);
    }

    /** The singleton builder. */
    public static final  Ìnstantiator<?> with = new Ìnstantiator();

    /**
     * Namespace for {@link Execution}  builder methods.
     */
    public static class Ìnstantiator<Self extends Execution & ÅssignableÅrbitrary<Self>> {

        /**
         * Create new {@link Execution} with the specified orientation property.
         * 
         * @return The next assignable model.
         */
        public Self orientation(Direction orientation, Num size) {
            Åssignable o = new Åssignable();
            o.orientation(orientation);
            o.size(size);
            return (Self)o;
        }

        /**
         * Create new {@link Execution} with the specified orientation property.
         * 
         * @return The next assignable model.
         */
        public Self orientation(Direction orientation, int size) {
            Åssignable o = new Åssignable();
            o.orientation(orientation);
            o.size(size);
            return (Self)o;
        }

        /**
         * Create new {@link Execution} with the specified orientation property.
         * 
         * @return The next assignable model.
         */
        public Self orientation(Direction orientation, float size) {
            Åssignable o = new Åssignable();
            o.orientation(orientation);
            o.size(size);
            return (Self)o;
        }

        /**
         * Create new {@link Execution} with the specified orientation property.
         * 
         * @return The next assignable model.
         */
        public Self orientation(Direction orientation, long size) {
            Åssignable o = new Åssignable();
            o.orientation(orientation);
            o.size(size);
            return (Self)o;
        }

        /**
         * Create new {@link Execution} with the specified orientation property.
         * 
         * @return The next assignable model.
         */
        public Self orientation(Direction orientation, double size) {
            Åssignable o = new Åssignable();
            o.orientation(orientation);
            o.size(size);
            return (Self)o;
        }

        /**
         * Create new {@link Execution} with the specified orientation property.
         * 
         * @return The next assignable model.
         */
        public Self buy(Num size) {
            Åssignable o = new Åssignable();
            o.buy();
            o.size(size);
            return (Self)o;
        }

        /**
         * Create new {@link Execution} with the specified orientation property.
         * 
         * @return The next assignable model.
         */
        public Self buy(int size) {
            Åssignable o = new Åssignable();
            o.buy();
            o.size(size);
            return (Self)o;
        }

        /**
         * Create new {@link Execution} with the specified orientation property.
         * 
         * @return The next assignable model.
         */
        public Self buy(float size) {
            Åssignable o = new Åssignable();
            o.buy();
            o.size(size);
            return (Self)o;
        }

        /**
         * Create new {@link Execution} with the specified orientation property.
         * 
         * @return The next assignable model.
         */
        public Self buy(long size) {
            Åssignable o = new Åssignable();
            o.buy();
            o.size(size);
            return (Self)o;
        }

        /**
         * Create new {@link Execution} with the specified orientation property.
         * 
         * @return The next assignable model.
         */
        public Self buy(double size) {
            Åssignable o = new Åssignable();
            o.buy();
            o.size(size);
            return (Self)o;
        }

        /**
         * Create new {@link Execution} with the specified orientation property.
         * 
         * @return The next assignable model.
         */
        public Self sell(Num size) {
            Åssignable o = new Åssignable();
            o.sell();
            o.size(size);
            return (Self)o;
        }

        /**
         * Create new {@link Execution} with the specified orientation property.
         * 
         * @return The next assignable model.
         */
        public Self sell(int size) {
            Åssignable o = new Åssignable();
            o.sell();
            o.size(size);
            return (Self)o;
        }

        /**
         * Create new {@link Execution} with the specified orientation property.
         * 
         * @return The next assignable model.
         */
        public Self sell(float size) {
            Åssignable o = new Åssignable();
            o.sell();
            o.size(size);
            return (Self)o;
        }

        /**
         * Create new {@link Execution} with the specified orientation property.
         * 
         * @return The next assignable model.
         */
        public Self sell(long size) {
            Åssignable o = new Åssignable();
            o.sell();
            o.size(size);
            return (Self)o;
        }

        /**
         * Create new {@link Execution} with the specified orientation property.
         * 
         * @return The next assignable model.
         */
        public Self sell(double size) {
            Åssignable o = new Åssignable();
            o.sell();
            o.size(size);
            return (Self)o;
        }

        /**
         * Create new {@link Execution} with the specified orientation property.
         * 
         * @return The next assignable model.
         */
        public Self direction(Orientational<Direction> direction, Num size) {
            Åssignable o = new Åssignable();
            o.direction(direction);
            o.size(size);
            return (Self)o;
        }

        /**
         * Create new {@link Execution} with the specified orientation property.
         * 
         * @return The next assignable model.
         */
        public Self direction(Orientational<Direction> direction, int size) {
            Åssignable o = new Åssignable();
            o.direction(direction);
            o.size(size);
            return (Self)o;
        }

        /**
         * Create new {@link Execution} with the specified orientation property.
         * 
         * @return The next assignable model.
         */
        public Self direction(Orientational<Direction> direction, float size) {
            Åssignable o = new Åssignable();
            o.direction(direction);
            o.size(size);
            return (Self)o;
        }

        /**
         * Create new {@link Execution} with the specified orientation property.
         * 
         * @return The next assignable model.
         */
        public Self direction(Orientational<Direction> direction, long size) {
            Åssignable o = new Åssignable();
            o.direction(direction);
            o.size(size);
            return (Self)o;
        }

        /**
         * Create new {@link Execution} with the specified orientation property.
         * 
         * @return The next assignable model.
         */
        public Self direction(Orientational<Direction> direction, double size) {
            Åssignable o = new Åssignable();
            o.direction(direction);
            o.size(size);
            return (Self)o;
        }
    }

    /**
     * Property assignment API.
     */
    public static interface ÅssignableOrientation<Next> {

        /**
         * Assign orientation property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next orientation(Direction value) {
            ((Execution) this).setOrientation(value);
            return (Next) this;
        }

        /**
         * Assign orientation property.
         * 
         * @return The next assignable model.
         */
        default Next buy() {
            return orientation(Direction.BUY);
        }

        /**
         * Assign orientation property.
         * 
         * @return The next assignable model.
         */
        default Next sell() {
            return orientation(Direction.SELL);
        }

        /**
         * Assign orientation property.
         * 
         * @return The next assignable model.
         */
        default Next direction(Orientational<Direction> direction) {
            try {
                return orientation((Direction) direction$1158975041.invoke(this, direction));
            } catch (Throwable e) {
                throw quiet(e);
            }
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
                return size((Num) size$101354429.invoke(this, size));
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
                return size((Num) size$698532521.invoke(this, size));
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
                return size((Num) size$1093866057.invoke(this, size));
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
                return size((Num) size$912239839.invoke(this, size));
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
                return price((Num) price$101354429.invoke(this, price));
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
                return price((Num) price$1093866057.invoke(this, price));
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
                return price((Num) price$698532521.invoke(this, price));
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
                return price((Num) price$912239839.invoke(this, price));
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
        default Next accumulative(double value) {
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
                return date((ZonedDateTime) date$1790350653.invoke(this, year, month, day, hour, minute, second, ms));
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

        /**
         * Assign info property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next info(String value) {
            ((Execution) this).setInfo(value);
            return (Next) this;
        }
    }

    /**
     * Internal aggregated API.
     */
    protected static interface ÅssignableAll extends ÅssignableOrientation, ÅssignableSize {
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
        static final String Orientation = "orientation";
        static final String Size = "size";
        static final String Id = "id";
        static final String Price = "price";
        static final String Accumulative = "accumulative";
        static final String Date = "date";
        static final String Mills = "mills";
        static final String Consecutive = "consecutive";
        static final String Delay = "delay";
        static final String Info = "info";
    }
}
