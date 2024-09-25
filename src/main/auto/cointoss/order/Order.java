package cointoss.order;

import cointoss.Direction;
import cointoss.order.Order;
import cointoss.util.ObservableNumProperty;
import cointoss.util.ObservableProperty;
import hypatia.Num;
import hypatia.Orientational;
import java.lang.String;
import java.lang.Throwable;
import java.lang.UnsupportedOperationException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.ZonedDateTime;
import java.util.function.Consumer;
import kiss.Signal;

/**
 * Generated model for {@link OrderModel}.
 * 
 * @see <a href="https://github.com/teletha/icymanipulator">Icy Manipulator (Code Generator)</a>
 */
public class Order extends OrderModel {

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
            Method method = cointoss.order.OrderModel.class.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
            return MethodHandles.lookup().unreflect(method);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /** The overload or intercept method invoker. */
    private static final MethodHandle orientation$1158975041= invoker("orientation", Orientational.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle orientation$927011984= invoker("orientation", String.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle size$1093866057= invoker("size", long.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle size$912239839= invoker("size", double.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle validateSize$253442283= invoker("validateSize", Num.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle price$1093866057= invoker("price", long.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle price$912239839= invoker("price", double.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle price$2141541105= invoker("price", Num.class, Consumer.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle executedSize$1093866057= invoker("executedSize", long.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle executedSize$912239839= invoker("executedSize", double.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle checkExecutedSize$253442283= invoker("checkExecutedSize", Num.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle validateState$41283920= invoker("validateState", OrderState.class);

    /**
     * Create special property updater.
     *
     * @param name A target property name.
     * @return A special property updater.
     */
    private static final MethodHandle updater(String name)  {
        try {
            Field field = Order.class.getDeclaredField(name);
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
    private static final MethodHandle priceUpdater = updater("price");

    /** The final property updater. */
    private static final MethodHandle typeUpdater = updater("type");

    /** The final property updater. */
    private static final MethodHandle quantityConditionUpdater = updater("quantityCondition");

    /** The final property updater. */
    private static final MethodHandle executedSizeUpdater = updater("executedSize");

    /** The final property updater. */
    private static final MethodHandle commissionUpdater = updater("commission");

    /** The final property updater. */
    private static final MethodHandle idUpdater = updater("id");

    /** The final property updater. */
    private static final MethodHandle creationTimeUpdater = updater("creationTime");

    /** The final property updater. */
    private static final MethodHandle terminationTimeUpdater = updater("terminationTime");

    /** The final property updater. */
    private static final MethodHandle stateUpdater = updater("state");

    /** The exposed property. */
    public final Direction orientation;

    /** The exposed property. */
    public final Num size;

    /** The exposed property. */
    public final Num price;

    /** The exposed property. */
    public final OrderType type;

    /** The exposed property. */
    public final QuantityCondition quantityCondition;

    /** The exposed property. */
    public final Num executedSize;

    /** The property customizer. */
    private final ObservableNumProperty executedSizeCustomizer = new ObservableNumProperty() {

        @Override
        public Num get() {
            return executedSize;
        }
    };

    /** The exposed property. */
    public final Num commission;

    /** The exposed property. */
    public final String id;

    /** The exposed property. */
    public final ZonedDateTime creationTime;

    /** The property customizer. */
    private final ObservableProperty<ZonedDateTime> creationTimeCustomizer = new ObservableProperty<ZonedDateTime>() {

        @Override
        public ZonedDateTime get() {
            return creationTime;
        }
    };

    /** The exposed property. */
    public final ZonedDateTime terminationTime;

    /** The property customizer. */
    private final ObservableProperty<ZonedDateTime> terminationTimeCustomizer = new ObservableProperty<ZonedDateTime>() {

        @Override
        public ZonedDateTime get() {
            return terminationTime;
        }
    };

    /** The exposed property. */
    public final cointoss.order.OrderState state;

    /** The property customizer. */
    private final ObservableProperty<cointoss.order.OrderState> stateCustomizer = new ObservableProperty<cointoss.order.OrderState>() {

        @Override
        public cointoss.order.OrderState get() {
            return state;
        }
    };

    /**
     * HIDE CONSTRUCTOR
     */
    protected Order() {
        this.orientation = null;
        this.size = null;
        this.price = super.price();
        this.type = super.type();
        this.quantityCondition = super.quantityCondition();
        this.executedSize = super.executedSize();
        this.commission = super.commission();
        this.id = super.id();
        this.creationTime = super.creationTime();
        this.terminationTime = super.terminationTime();
        this.state = super.state();
    }

    /** {@inheritDoc} */
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
     * The initial ordered size.
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
            sizeUpdater.invoke(this, validateSize$253442283.invoke(this, value));
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * An average price.
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
    final void setPrice(Num value) {
        if (value == null) {
            value = super.price();
        }
        try {
            priceUpdater.invoke(this, price$2141541105.invoke(this, value, (Consumer<cointoss.order.OrderType>) this::setType));
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * The order type.
     *  
     *  @return
     */
    @Override
    public final cointoss.order.OrderType type() {
        return this.type;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of type property.
     */
    @SuppressWarnings("unused")
    private final cointoss.order.OrderType getType() {
        return this.type;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of type property to assign.
     */
    private final void setType(cointoss.order.OrderType value) {
        if (value == null) {
            value = super.type();
        }
        try {
            typeUpdater.invoke(this, value);
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * The quantity conditions enforcement.
     *  
     *  @return
     */
    @Override
    public final cointoss.order.QuantityCondition quantityCondition() {
        return this.quantityCondition;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of quantityCondition property.
     */
    @SuppressWarnings("unused")
    private final cointoss.order.QuantityCondition getQuantityCondition() {
        return this.quantityCondition;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of quantityCondition property to assign.
     */
    private final void setQuantityCondition(cointoss.order.QuantityCondition value) {
        if (value == null) {
            value = super.quantityCondition();
        }
        try {
            quantityConditionUpdater.invoke(this, value);
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Calculate the executed size.
     *  
     *  @return The executed size.
     */
    @Override
    public final Num executedSize() {
        return this.executedSize;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of executedSize property.
     */
    @SuppressWarnings("unused")
    private final Num getExecutedSize() {
        return this.executedSize;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of executedSize property to assign.
     */
    final void setExecutedSize(Num value) {
        if (value == null) {
            value = super.executedSize();
        }
        try {
            executedSizeUpdater.invoke(this, checkExecutedSize$253442283.invoke(this, value));
            executedSizeCustomizer.accept(this.executedSize);
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Observe property diff.
     *  
     *  @return
     */
    public final Signal<Num> observeExecutedSizeDiff() {
        return executedSizeCustomizer.observe$Diff();
    }

    /**
     * Observe property modification.
     *  
     *  @return
     */
    public final Signal<Num> observeExecutedSize() {
        return executedSizeCustomizer.observe$();
    }

    /**
     * Observe property modification with the current value.
     *  
     *  @return
     */
    public final Signal<Num> observeExecutedSizeNow() {
        return executedSizeCustomizer.observe$Now();
    }

    /**
     * The commission.
     *  
     *  @return The commission.
     */
    @Override
    public final Num commission() {
        return this.commission;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of commission property.
     */
    @SuppressWarnings("unused")
    private final Num getCommission() {
        return this.commission;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of commission property to assign.
     */
    final void setCommission(Num value) {
        if (value == null) {
            value = super.commission();
        }
        try {
            commissionUpdater.invoke(this, value);
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * The order identifier for the specific market.
     *  
     *  @return
     */
    @Override
    public final String id() {
        return this.id;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of id property.
     */
    @SuppressWarnings("unused")
    private final String getId() {
        return this.id;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of id property to assign.
     */
    final void setId(String value) {
        if (value == null) {
            value = super.id();
        }
        try {
            idUpdater.invoke(this, value);
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * The requested time of this order.
     *  
     *  @return
     */
    @Override
    public final ZonedDateTime creationTime() {
        return this.creationTime;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of creationTime property.
     */
    @SuppressWarnings("unused")
    private final ZonedDateTime getCreationTime() {
        return this.creationTime;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of creationTime property to assign.
     */
    final void setCreationTime(ZonedDateTime value) {
        if (value == null) {
            value = super.creationTime();
        }
        try {
            creationTimeUpdater.invoke(this, value);
            creationTimeCustomizer.accept(this.creationTime);
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Observe property modification.
     *  
     *  @return
     */
    public final Signal<ZonedDateTime> observeCreationTime() {
        return creationTimeCustomizer.observe$();
    }

    /**
     * Observe property modification with the current value.
     *  
     *  @return
     */
    public final Signal<ZonedDateTime> observeCreationTimeNow() {
        return creationTimeCustomizer.observe$Now();
    }

    /**
     * The termiated time of this order.
     *  
     *  @return
     */
    @Override
    public final ZonedDateTime terminationTime() {
        return this.terminationTime;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of terminationTime property.
     */
    @SuppressWarnings("unused")
    private final ZonedDateTime getTerminationTime() {
        return this.terminationTime;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of terminationTime property to assign.
     */
    final void setTerminationTime(ZonedDateTime value) {
        if (value == null) {
            value = super.terminationTime();
        }
        try {
            terminationTimeUpdater.invoke(this, value);
            terminationTimeCustomizer.accept(this.terminationTime);
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Observe property modification.
     *  
     *  @return
     */
    public final Signal<ZonedDateTime> observeTerminationTime() {
        return terminationTimeCustomizer.observe$();
    }

    /**
     * Observe property modification with the current value.
     *  
     *  @return
     */
    public final Signal<ZonedDateTime> observeTerminationTimeNow() {
        return terminationTimeCustomizer.observe$Now();
    }

    /**
     * The termiated time of this order.
     *  
     *  @return
     */
    @Override
    public final cointoss.order.OrderState state() {
        return this.state;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of state property.
     */
    @SuppressWarnings("unused")
    private final cointoss.order.OrderState getState() {
        return this.state;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of state property to assign.
     */
    final void setState(cointoss.order.OrderState value) {
        if (value == null) {
            value = super.state();
        }
        try {
            stateUpdater.invoke(this, validateState$41283920.invoke(this, value));
            stateCustomizer.accept(this.state);
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Observe property modification.
     *  
     *  @return
     */
    public final Signal<cointoss.order.OrderState> observeState() {
        return stateCustomizer.observe$();
    }

    /**
     * Observe property modification with the current value.
     *  
     *  @return
     */
    public final Signal<cointoss.order.OrderState> observeStateNow() {
        return stateCustomizer.observe$Now();
    }

    /** The singleton builder. */
    public static final  Ìnstantiator<?> with = new Ìnstantiator();

    /**
     * Namespace for {@link Order}  builder methods.
     */
    public static class Ìnstantiator<Self extends Order & ÅssignableÅrbitrary<Self>> {

        /**
         * Create new {@link Order} with the specified orientation property.
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
         * Create new {@link Order} with the specified orientation property.
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
         * Create new {@link Order} with the specified orientation property.
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
         * Create new {@link Order} with the specified orientation property.
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
         * Create new {@link Order} with the specified orientation property.
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
         * Create new {@link Order} with the specified orientation property.
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
         * Create new {@link Order} with the specified orientation property.
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
         * Create new {@link Order} with the specified orientation property.
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
         * Create new {@link Order} with the specified orientation property.
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
         * Specify direction by {@link Orientational}.
         *  
         *  @param direction A directional data.
         *  @return
         */
        public Self orientation(Orientational<Direction> direction, Num size) {
            Åssignable o = new Åssignable();
            o.orientation(direction);
            o.size(size);
            return (Self)o;
        }

        /**
         * Specify direction by {@link Orientational}.
         *  
         *  @param direction A directional data.
         *  @return
         */
        public Self orientation(Orientational<Direction> direction, long size) {
            Åssignable o = new Åssignable();
            o.orientation(direction);
            o.size(size);
            return (Self)o;
        }

        /**
         * Specify direction by {@link Orientational}.
         *  
         *  @param direction A directional data.
         *  @return
         */
        public Self orientation(Orientational<Direction> direction, double size) {
            Åssignable o = new Åssignable();
            o.orientation(direction);
            o.size(size);
            return (Self)o;
        }

        /**
         * Specify direction by literal.
         *  
         *  @param direction A direction literal.
         *  @return A parsed direction.
         */
        public Self orientation(String direction, Num size) {
            Åssignable o = new Åssignable();
            o.orientation(direction);
            o.size(size);
            return (Self)o;
        }

        /**
         * Specify direction by literal.
         *  
         *  @param direction A direction literal.
         *  @return A parsed direction.
         */
        public Self orientation(String direction, long size) {
            Åssignable o = new Åssignable();
            o.orientation(direction);
            o.size(size);
            return (Self)o;
        }

        /**
         * Specify direction by literal.
         *  
         *  @param direction A direction literal.
         *  @return A parsed direction.
         */
        public Self orientation(String direction, double size) {
            Åssignable o = new Åssignable();
            o.orientation(direction);
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
            ((Order) this).setOrientation(value);
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
         * Specify direction by {@link Orientational}.
         *  
         *  @param direction A directional data.
         *  @return
         */
        default Next orientation(Orientational<Direction> direction) {
            try {
                return orientation((Direction) orientation$1158975041.invoke(this, direction));
            } catch (Throwable e) {
                throw quiet(e);
            }
        }

        /**
         * Specify direction by literal.
         *  
         *  @param direction A direction literal.
         *  @return A parsed direction.
         */
        default Next orientation(String direction) {
            try {
                return orientation((Direction) orientation$927011984.invoke(this, direction));
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
            ((Order) this).setSize(value);
            return (Next) this;
        }

        /**
         * Set order size by value.
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
         * Set order size by value.
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
    public static interface ÅssignableÅrbitrary<Next extends Order> {

        /**
         * Assign price property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next price(Num value) {
            ((Order) this).setPrice(value);
            return (Next) this;
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
        default Next price(double price) {
            try {
                return price((Num) price$912239839.invoke(this, price));
            } catch (Throwable e) {
                throw quiet(e);
            }
        }

        /**
         * Assign type property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next type(cointoss.order.OrderType value) {
            ((Order) this).setType(value);
            return (Next) this;
        }

        /**
         * Assign type property.
         * 
         * @return The next assignable model.
         */
        default Next maker() {
            return type(cointoss.order.OrderType.Maker);
        }

        /**
         * Assign type property.
         * 
         * @return The next assignable model.
         */
        default Next taker() {
            return type(cointoss.order.OrderType.Taker);
        }

        /**
         * Assign quantityCondition property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next quantityCondition(cointoss.order.QuantityCondition value) {
            ((Order) this).setQuantityCondition(value);
            return (Next) this;
        }

        /**
         * Assign quantityCondition property.
         * 
         * @return The next assignable model.
         */
        default Next goodTillCanceled() {
            return quantityCondition(cointoss.order.QuantityCondition.GoodTillCanceled);
        }

        /**
         * Assign quantityCondition property.
         * 
         * @return The next assignable model.
         */
        default Next immediateOrCancel() {
            return quantityCondition(cointoss.order.QuantityCondition.ImmediateOrCancel);
        }

        /**
         * Assign quantityCondition property.
         * 
         * @return The next assignable model.
         */
        default Next fillOrKill() {
            return quantityCondition(cointoss.order.QuantityCondition.FillOrKill);
        }

        /**
         * Assign executedSize property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next executedSize(Num value) {
            ((Order) this).setExecutedSize(value);
            return (Next) this;
        }

        /**
         * Calculate executed size of this order.
         *  
         *  @param size An executed size.
         *  @return Chainable API.
         */
        default Next executedSize(long size) {
            try {
                return executedSize((Num) executedSize$1093866057.invoke(this, size));
            } catch (Throwable e) {
                throw quiet(e);
            }
        }

        /**
         * Calculate executed size of this order.
         *  
         *  @param size An executed size.
         *  @return Chainable API.
         */
        default Next executedSize(double size) {
            try {
                return executedSize((Num) executedSize$912239839.invoke(this, size));
            } catch (Throwable e) {
                throw quiet(e);
            }
        }

        /**
         * Assign commission property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next commission(Num value) {
            ((Order) this).setCommission(value);
            return (Next) this;
        }

        /**
         * Assign id property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next id(String value) {
            ((Order) this).setId(value);
            return (Next) this;
        }

        /**
         * Assign creationTime property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next creationTime(ZonedDateTime value) {
            ((Order) this).setCreationTime(value);
            return (Next) this;
        }

        /**
         * Assign terminationTime property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next terminationTime(ZonedDateTime value) {
            ((Order) this).setTerminationTime(value);
            return (Next) this;
        }

        /**
         * Assign state property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next state(cointoss.order.OrderState value) {
            ((Order) this).setState(value);
            return (Next) this;
        }

        /**
         * Assign state property.
         * 
         * @return The next assignable model.
         */
        default Next init() {
            return state(cointoss.order.OrderState.INIT);
        }

        /**
         * Assign state property.
         * 
         * @return The next assignable model.
         */
        default Next requesting() {
            return state(cointoss.order.OrderState.REQUESTING);
        }

        /**
         * Assign state property.
         * 
         * @return The next assignable model.
         */
        default Next active() {
            return state(cointoss.order.OrderState.ACTIVE);
        }

        /**
         * Assign state property.
         * 
         * @return The next assignable model.
         */
        default Next active_partial() {
            return state(cointoss.order.OrderState.ACTIVE_PARTIAL);
        }

        /**
         * Assign state property.
         * 
         * @return The next assignable model.
         */
        default Next completed() {
            return state(cointoss.order.OrderState.COMPLETED);
        }

        /**
         * Assign state property.
         * 
         * @return The next assignable model.
         */
        default Next canceled() {
            return state(cointoss.order.OrderState.CANCELED);
        }

        /**
         * Assign state property.
         * 
         * @return The next assignable model.
         */
        default Next expired() {
            return state(cointoss.order.OrderState.EXPIRED);
        }

        /**
         * Assign state property.
         * 
         * @return The next assignable model.
         */
        default Next rejected() {
            return state(cointoss.order.OrderState.REJECTED);
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
    private static final class Åssignable extends Order implements ÅssignableAll, ÅssignableÅrbitrary {
    }

    /**
     * The identifier for properties.
     */
    static final class My {
        static final String Orientation = "orientation";
        static final String Size = "size";
        static final String Price = "price";
        static final String Type = "type";
        static final String QuantityCondition = "quantityCondition";
        static final String ExecutedSize = "executedSize";
        static final String Commission = "commission";
        static final String Id = "id";
        static final String CreationTime = "creationTime";
        static final String TerminationTime = "terminationTime";
        static final String State = "state";
    }
}
