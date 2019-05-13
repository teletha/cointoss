package cointoss.execution;

import cointoss.Direction;
import cointoss.util.Num;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.ZonedDateTime;
import java.util.function.Consumer;
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
    private static final MethodHandle sizeint= invoker("size", int.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle sizefloat= invoker("size", float.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle sizelong= invoker("size", long.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle sizedouble= invoker("size", double.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle initializeCumulativeSizeNumConsumer= invoker("initializeCumulativeSize", Num.class, Consumer.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle priceint= invoker("price", int.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle pricelong= invoker("price", long.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle pricefloat= invoker("price", float.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle pricedouble= invoker("price", double.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle dateintintintintintintint= invoker("date", int.class, int.class, int.class, int.class, int.class, int.class, int.class);

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
     * Retrieve direction property.
     */
    @Override
    public final Direction direction() {
        return this.direction;
    }

    /**
     * Provide classic getter API.
     */
    @SuppressWarnings("unused")
    private final Direction getDirection() {
        return this.direction;
    }

    /**
     * Provide classic setter API.
     */
    @SuppressWarnings("unused")
     void setDirection(Direction value) {
        ((ÅssignableDirection) this).direction(value);
    }

    /**
     * Retrieve size property.
     */
    @Override
    public final Num size() {
        return this.size;
    }

    /**
     * Provide classic getter API.
     */
    @SuppressWarnings("unused")
    private final Num getSize() {
        return this.size;
    }

    /**
     * Provide classic setter API.
     */
    @SuppressWarnings("unused")
     void setSize(Num value) {
        ((ÅssignableSize) this).size(value);
    }

    /**
     * Retrieve id property.
     */
    @Override
    public final long id() {
        return this.id;
    }

    /**
     * Provide classic getter API.
     */
    @SuppressWarnings("unused")
    private final long getId() {
        return this.id;
    }

    /**
     * Provide classic setter API.
     */
    @SuppressWarnings("unused")
     void setId(long value) {
        ((ÅssignableÅrbitrary) this).id(value);
    }

    /**
     * Retrieve price property.
     */
    @Override
    public final Num price() {
        return this.price;
    }

    /**
     * Provide classic getter API.
     */
    @SuppressWarnings("unused")
    private final Num getPrice() {
        return this.price;
    }

    /**
     * Provide classic setter API.
     */
    @SuppressWarnings("unused")
     void setPrice(Num value) {
        ((ÅssignableÅrbitrary) this).price(value);
    }

    /**
     * Retrieve cumulativeSize property.
     */
    @Override
    public final Num cumulativeSize() {
        return this.cumulativeSize;
    }

    /**
     * Provide classic getter API.
     */
    @SuppressWarnings("unused")
    private final Num getCumulativeSize() {
        return this.cumulativeSize;
    }

    /**
     * Provide classic setter API.
     */
    @SuppressWarnings("unused")
     void setCumulativeSize(Num value) {
        ((ÅssignableÅrbitrary) this).cumulativeSize(value);
    }

    /**
     * Retrieve date property.
     */
    @Override
    public final ZonedDateTime date() {
        return this.date;
    }

    /**
     * Provide classic getter API.
     */
    @SuppressWarnings("unused")
    private final ZonedDateTime getDate() {
        return this.date;
    }

    /**
     * Provide classic setter API.
     */
    @SuppressWarnings("unused")
     void setDate(ZonedDateTime value) {
        ((ÅssignableÅrbitrary) this).date(value);
    }

    /**
     * Retrieve mills property.
     */
    @Override
    public final long mills() {
        return this.mills;
    }

    /**
     * Provide classic getter API.
     */
    @SuppressWarnings("unused")
    private final long getMills() {
        return this.mills;
    }

    /**
     * Provide classic setter API.
     */
    @SuppressWarnings("unused")
     void setMills(long value) {
        ((ÅssignableÅrbitrary) this).mills(value);
    }

    /**
     * Retrieve consecutive property.
     */
    @Override
    public final int consecutive() {
        return this.consecutive;
    }

    /**
     * Provide classic getter API.
     */
    @SuppressWarnings("unused")
    private final int getConsecutive() {
        return this.consecutive;
    }

    /**
     * Provide classic setter API.
     */
    @SuppressWarnings("unused")
     void setConsecutive(int value) {
        ((ÅssignableÅrbitrary) this).consecutive(value);
    }

    /**
     * Retrieve delay property.
     */
    @Override
    public final int delay() {
        return this.delay;
    }

    /**
     * Provide classic getter API.
     */
    @SuppressWarnings("unused")
    private final int getDelay() {
        return this.delay;
    }

    /**
     * Provide classic setter API.
     */
    @SuppressWarnings("unused")
     void setDelay(int value) {
        ((ÅssignableÅrbitrary) this).delay(value);
    }

    /** The singleton builder. */
    public static final  Ìnstantiator<?> with = new Ìnstantiator();

    /**
     * Builder namespace for {@link Execution}.
     */
    public static final class Ìnstantiator<Self extends Execution & ÅssignableÅrbitrary<Self>> {
        /**
         * Create uninitialized {@link Execution}.
         */
        public final <T extends Self> T direction(Direction direction, Num num) {
            Åssignable o = new Åssignable();
            o.direction(direction);
            o.size(num);
            return (T) o;
        }
        /**
         * Create uninitialized {@link Execution}.
         */
        public final <T extends Self> T direction(Direction direction, int size) {
            Åssignable o = new Åssignable();
            o.direction(direction);
            o.size(size);
            return (T) o;
        }
        /**
         * Create uninitialized {@link Execution}.
         */
        public final <T extends Self> T direction(Direction direction, float size) {
            Åssignable o = new Åssignable();
            o.direction(direction);
            o.size(size);
            return (T) o;
        }
        /**
         * Create uninitialized {@link Execution}.
         */
        public final <T extends Self> T direction(Direction direction, long size) {
            Åssignable o = new Åssignable();
            o.direction(direction);
            o.size(size);
            return (T) o;
        }
        /**
         * Create uninitialized {@link Execution}.
         */
        public final <T extends Self> T direction(Direction direction, double size) {
            Åssignable o = new Åssignable();
            o.direction(direction);
            o.size(size);
            return (T) o;
        }
        /**
         * Create uninitialized {@link Execution}.
         */
        public final <T extends Self> T buy(Num num) {
            Åssignable o = new Åssignable();
            o.buy();
            o.size(num);
            return (T) o;
        }
        /**
         * Create uninitialized {@link Execution}.
         */
        public final <T extends Self> T buy(int size) {
            Åssignable o = new Åssignable();
            o.buy();
            o.size(size);
            return (T) o;
        }
        /**
         * Create uninitialized {@link Execution}.
         */
        public final <T extends Self> T buy(float size) {
            Åssignable o = new Åssignable();
            o.buy();
            o.size(size);
            return (T) o;
        }
        /**
         * Create uninitialized {@link Execution}.
         */
        public final <T extends Self> T buy(long size) {
            Åssignable o = new Åssignable();
            o.buy();
            o.size(size);
            return (T) o;
        }
        /**
         * Create uninitialized {@link Execution}.
         */
        public final <T extends Self> T buy(double size) {
            Åssignable o = new Åssignable();
            o.buy();
            o.size(size);
            return (T) o;
        }
        /**
         * Create uninitialized {@link Execution}.
         */
        public final <T extends Self> T sell(Num num) {
            Åssignable o = new Åssignable();
            o.sell();
            o.size(num);
            return (T) o;
        }
        /**
         * Create uninitialized {@link Execution}.
         */
        public final <T extends Self> T sell(int size) {
            Åssignable o = new Åssignable();
            o.sell();
            o.size(size);
            return (T) o;
        }
        /**
         * Create uninitialized {@link Execution}.
         */
        public final <T extends Self> T sell(float size) {
            Åssignable o = new Åssignable();
            o.sell();
            o.size(size);
            return (T) o;
        }
        /**
         * Create uninitialized {@link Execution}.
         */
        public final <T extends Self> T sell(long size) {
            Åssignable o = new Åssignable();
            o.sell();
            o.size(size);
            return (T) o;
        }
        /**
         * Create uninitialized {@link Execution}.
         */
        public final <T extends Self> T sell(double size) {
            Åssignable o = new Åssignable();
            o.sell();
            o.size(size);
            return (T) o;
        }
    }

    /**
     * Property assignment API.
     */
    public static interface ÅssignableDirection<Next> {
        /**
         * The base setter.
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
         * The overload setter.
         */
        default Next buy() {
            return direction(Direction.BUY);
        }

        /**
         * The overload setter.
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
         * The base setter.
         */
        default Next size(Num value) {
            try {
                sizeUpdater.invoke(this, initializeCumulativeSizeNumConsumer.invoke(this, value, (Consumer<Num>) ((Åssignable) this)::cumulativeSize));
            } catch (Throwable e) {
                throw new Error(e);
            }
            return (Next) this;
        }

        /**
         * The overload setter.
         */
        default Next size(int size) {
            try {
                return size((Num) sizeint.invoke(this, size));
            } catch (Throwable e) {
                throw new Error(e);
            }
        }

        /**
         * The overload setter.
         */
        default Next size(float size) {
            try {
                return size((Num) sizefloat.invoke(this, size));
            } catch (Throwable e) {
                throw new Error(e);
            }
        }

        /**
         * The overload setter.
         */
        default Next size(long size) {
            try {
                return size((Num) sizelong.invoke(this, size));
            } catch (Throwable e) {
                throw new Error(e);
            }
        }

        /**
         * The overload setter.
         */
        default Next size(double size) {
            try {
                return size((Num) sizedouble.invoke(this, size));
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
         * The base setter.
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
         * The base setter.
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
         * The overload setter.
         */
        default Next price(int price) {
            try {
                return price((Num) priceint.invoke(this, price));
            } catch (Throwable e) {
                throw new Error(e);
            }
        }

        /**
         * The overload setter.
         */
        default Next price(long price) {
            try {
                return price((Num) pricelong.invoke(this, price));
            } catch (Throwable e) {
                throw new Error(e);
            }
        }

        /**
         * The overload setter.
         */
        default Next price(float price) {
            try {
                return price((Num) pricefloat.invoke(this, price));
            } catch (Throwable e) {
                throw new Error(e);
            }
        }

        /**
         * The overload setter.
         */
        default Next price(double price) {
            try {
                return price((Num) pricedouble.invoke(this, price));
            } catch (Throwable e) {
                throw new Error(e);
            }
        }
        /**
         * The base setter.
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
         * The base setter.
         */
        default Next date(ZonedDateTime value) {
            try {
                dateUpdater.invoke(this, value);
            } catch (Throwable e) {
                throw new Error(e);
            }
            return (Next) this;
        }

        /**
         * The overload setter.
         */
        default Next date(int year, int month, int day, int hour, int minute, int second, int ms) {
            try {
                return date((ZonedDateTime) dateintintintintintintint.invoke(this, year, month, day, hour, minute, second, ms));
            } catch (Throwable e) {
                throw new Error(e);
            }
        }
        /**
         * The base setter.
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
         * The base setter.
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
         * The base setter.
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
