package cointoss.verify;

import cointoss.MarketService;
import cointoss.execution.LogType;
import cointoss.trade.Trader;
import cointoss.verify.BackTest;
import hypatia.Num;
import java.lang.Override;
import java.lang.StringBuilder;
import java.lang.Throwable;
import java.lang.UnsupportedOperationException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Generated model for {@link BackTestModel}.
 * 
 * @see <a href="https://github.com/teletha/icymanipulator">Icy Manipulator (Code Generator)</a>
 */
public class BackTest implements BackTestModel {

     /** Determines if the execution environment is a Native Image of GraalVM. */
    private static final boolean NATIVE = "runtime".equals(System.getProperty("org.graalvm.nativeimage.imagecode"));

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
            Method method = cointoss.verify.BackTestModel.class.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
            return MethodHandles.lookup().unreflect(method);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /** The overload or intercept method invoker. */
    private static final MethodHandle start$1860699197= invoker("start", int.class, int.class, int.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle startRandom$1= invoker("startRandom");

    /** The overload or intercept method invoker. */
    private static final MethodHandle end$1860699197= invoker("end", int.class, int.class, int.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle endDuration$101354429= invoker("endDuration", int.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle initialBaseCurrency$912239839= invoker("initialBaseCurrency", double.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle initialTargetCurrency$912239839= invoker("initialTargetCurrency", double.class);

    /**
     * Create special property updater.
     *
     * @param name A target property name.
     * @return A special property updater.
     */
    private static final Field updater(String name)  {
        try {
            Field field = BackTest.class.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Create fast property updater.
     *
     * @param field A target field.
     * @return A fast property updater.
     */
    private static final MethodHandle handler(Field field)  {
        try {
            return MethodHandles.lookup().unreflectSetter(field);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /** The final property updater. */
    private static final Field serviceField = updater("service");

    /** The fast final property updater. */
    private static final MethodHandle serviceUpdater = handler(serviceField);

    /** The final property updater. */
    private static final Field startField = updater("start");

    /** The fast final property updater. */
    private static final MethodHandle startUpdater = handler(startField);

    /** The final property updater. */
    private static final Field endField = updater("end");

    /** The fast final property updater. */
    private static final MethodHandle endUpdater = handler(endField);

    /** The final property updater. */
    private static final Field tradersField = updater("traders");

    /** The fast final property updater. */
    private static final MethodHandle tradersUpdater = handler(tradersField);

    /** The final property updater. */
    private static final Field initialBaseCurrencyField = updater("initialBaseCurrency");

    /** The fast final property updater. */
    private static final MethodHandle initialBaseCurrencyUpdater = handler(initialBaseCurrencyField);

    /** The final property updater. */
    private static final Field initialTargetCurrencyField = updater("initialTargetCurrency");

    /** The fast final property updater. */
    private static final MethodHandle initialTargetCurrencyUpdater = handler(initialTargetCurrencyField);

    /** The final property updater. */
    private static final Field detailField = updater("detail");

    /** The fast final property updater. */
    private static final MethodHandle detailUpdater = handler(detailField);

    /** The final property updater. */
    private static final Field typeField = updater("type");

    /** The fast final property updater. */
    private static final MethodHandle typeUpdater = handler(typeField);

    /** The exposed property. */
    public final MarketService service;

    /** The exposed property. */
    public final ZonedDateTime start;

    /** The exposed property. */
    public final ZonedDateTime end;

    /** The exposed property. */
    public final List<Trader> traders;

    /** The exposed property. */
    public final Num initialBaseCurrency;

    /** The exposed property. */
    public final Num initialTargetCurrency;

    /** The exposed property. */
    public final boolean detail;

    /** The exposed property. */
    public final LogType type;

    /**
     * HIDE CONSTRUCTOR
     */
    protected BackTest() {
        this.service = null;
        this.start = null;
        this.end = null;
        this.traders = null;
        this.initialBaseCurrency = cointoss.verify.BackTestModel.super.initialBaseCurrency();
        this.initialTargetCurrency = cointoss.verify.BackTestModel.super.initialTargetCurrency();
        this.detail = cointoss.verify.BackTestModel.super.detail();
        this.type = cointoss.verify.BackTestModel.super.type();
    }

    /**
     * Set the target market.
     *  
     *  @return
     */
    @Override
    public final MarketService service() {
        return this.service;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of service property.
     */
    @SuppressWarnings("unused")
    private final MarketService getService() {
        return this.service;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of service property to assign.
     */
    private final void setService(MarketService value) {
        if (value == null) {
            throw new IllegalArgumentException("The service property requires non-null value.");
        }
        try {
            serviceUpdater.invoke(this, value);
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Set the start date.
     *  
     *  @return
     */
    @Override
    public final ZonedDateTime start() {
        return this.start;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of start property.
     */
    @SuppressWarnings("unused")
    private final ZonedDateTime getStart() {
        return this.start;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of start property to assign.
     */
    private final void setStart(ZonedDateTime value) {
        if (value == null) {
            throw new IllegalArgumentException("The start property requires non-null value.");
        }
        try {
            startUpdater.invoke(this, value);
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Set the end date.
     *  
     *  @return
     */
    @Override
    public final ZonedDateTime end() {
        return this.end;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of end property.
     */
    @SuppressWarnings("unused")
    private final ZonedDateTime getEnd() {
        return this.end;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of end property to assign.
     */
    private final void setEnd(ZonedDateTime value) {
        if (value == null) {
            throw new IllegalArgumentException("The end property requires non-null value.");
        }
        try {
            endUpdater.invoke(this, value);
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Set the end date.
     *  
     *  @return
     */
    @Override
    public final List<Trader> traders() {
        return this.traders;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of traders property.
     */
    @SuppressWarnings("unused")
    private final List<Trader> getTraders() {
        return this.traders;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of traders property to assign.
     */
    private final void setTraders(List<Trader> value) {
        if (value == null) {
            throw new IllegalArgumentException("The traders property requires non-null value.");
        }
        try {
            tradersUpdater.invoke(this, value);
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Set the initial assets.
     *  
     *  @return
     */
    @Override
    public final Num initialBaseCurrency() {
        return this.initialBaseCurrency;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of initialBaseCurrency property.
     */
    @SuppressWarnings("unused")
    private final Num getInitialBaseCurrency() {
        return this.initialBaseCurrency;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of initialBaseCurrency property to assign.
     */
    private final void setInitialBaseCurrency(Num value) {
        if (value == null) {
            value = cointoss.verify.BackTestModel.super.initialBaseCurrency();
        }
        try {
            initialBaseCurrencyUpdater.invoke(this, value);
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Set the initial assets.
     *  
     *  @return
     */
    @Override
    public final Num initialTargetCurrency() {
        return this.initialTargetCurrency;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of initialTargetCurrency property.
     */
    @SuppressWarnings("unused")
    private final Num getInitialTargetCurrency() {
        return this.initialTargetCurrency;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of initialTargetCurrency property to assign.
     */
    private final void setInitialTargetCurrency(Num value) {
        if (value == null) {
            value = cointoss.verify.BackTestModel.super.initialTargetCurrency();
        }
        try {
            initialTargetCurrencyUpdater.invoke(this, value);
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Set the detail option.
     *  
     *  @return
     */
    @Override
    public final boolean detail() {
        return this.detail;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of detail property.
     */
    @SuppressWarnings("unused")
    private final boolean getDetail() {
        return this.detail;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of detail property to assign.
     */
    private final void setDetail(boolean value) {
        try {
            if (NATIVE) {
                detailField.setBoolean(this, (boolean) value);
            } else {
                detailUpdater.invoke(this, value);
            }
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Use fast log.
     *  
     *  @return
     */
    @Override
    public final LogType type() {
        return this.type;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of type property.
     */
    @SuppressWarnings("unused")
    private final LogType getType() {
        return this.type;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of type property to assign.
     */
    private final void setType(LogType value) {
        if (value == null) {
            value = cointoss.verify.BackTestModel.super.type();
        }
        try {
            typeUpdater.invoke(this, value);
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
        StringBuilder builder = new StringBuilder("BackTest [");
        builder.append("service=").append(service).append(", ");
        builder.append("start=").append(start).append(", ");
        builder.append("end=").append(end).append(", ");
        builder.append("traders=").append(traders).append(", ");
        builder.append("initialBaseCurrency=").append(initialBaseCurrency).append(", ");
        builder.append("initialTargetCurrency=").append(initialTargetCurrency).append(", ");
        builder.append("detail=").append(detail).append(", ");
        builder.append("type=").append(type).append("]");
        return builder.toString();
    }

    /**
     * Generates a hash code for a sequence of property values. The hash code is generated as if all the property values were placed into an array, and that array were hashed by calling Arrays.hashCode(Object[]). 
     *
     * @return A hash value of the sequence of property values.
     */
    @Override
    public int hashCode() {
        return Objects.hash(service, start, end, traders, initialBaseCurrency, initialTargetCurrency, detail, type);
    }

    /**
     * Returns true if the all properties are equal to each other and false otherwise. Consequently, if both properties are null, true is returned and if exactly one property is null, false is returned. Otherwise, equality is determined by using the equals method of the base model. 
     *
     * @return true if the all properties are equal to each other and false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof BackTest == false) {
            return false;
        }

        BackTest other = (BackTest) o;
        if (!Objects.equals(service, other.service)) return false;
        if (!Objects.equals(start, other.start)) return false;
        if (!Objects.equals(end, other.end)) return false;
        if (!Objects.equals(traders, other.traders)) return false;
        if (!Objects.equals(initialBaseCurrency, other.initialBaseCurrency)) return false;
        if (!Objects.equals(initialTargetCurrency, other.initialTargetCurrency)) return false;
        if (detail != other.detail) return false;
        if (!Objects.equals(type, other.type)) return false;
        return true;
    }

    /** The singleton builder. */
    public static final  Ìnstantiator<?> with = new Ìnstantiator();

    /**
     * Namespace for {@link BackTest}  builder methods.
     */
    public static class Ìnstantiator<Self extends BackTest & ÅssignableÅrbitrary<Self>> {

        /**
         * Create new {@link BackTest} with the specified service property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableStart<ÅssignableEnd<ÅssignableTraders<Self>>> service(MarketService service) {
            Åssignable o = new Åssignable();
            o.service(service);
            return o;
        }
    }

    /**
     * Property assignment API.
     */
    public static interface ÅssignableService<Next> {

        /**
         * Assign service property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next service(MarketService value) {
            ((BackTest) this).setService(value);
            return (Next) this;
        }
    }

    /**
     * Property assignment API.
     */
    public static interface ÅssignableStart<Next> {

        /**
         * Assign start property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next start(ZonedDateTime value) {
            ((BackTest) this).setStart(value);
            return (Next) this;
        }

        /**
         * Set the start date.
         *  
         *  @return
         */
        default Next start(int year, int month, int day) {
            try {
                return start((ZonedDateTime) start$1860699197.invoke(this, year, month, day));
            } catch (Throwable e) {
                throw quiet(e);
            }
        }

        /**
         * Set the start date.
         *  
         *  @return
         */
        default Next startRandom() {
            try {
                return start((ZonedDateTime) startRandom$1.invoke(this));
            } catch (Throwable e) {
                throw quiet(e);
            }
        }
    }

    /**
     * Property assignment API.
     */
    public static interface ÅssignableEnd<Next> {

        /**
         * Assign end property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next end(ZonedDateTime value) {
            ((BackTest) this).setEnd(value);
            return (Next) this;
        }

        /**
         * Set the end date.
         *  
         *  @return
         */
        default Next end(int year, int month, int day) {
            try {
                return end((ZonedDateTime) end$1860699197.invoke(this, year, month, day));
            } catch (Throwable e) {
                throw quiet(e);
            }
        }

        /**
         * Set the end date.
         *  
         *  @return
         */
        default Next endDuration(int day) {
            try {
                return end((ZonedDateTime) endDuration$101354429.invoke(this, day));
            } catch (Throwable e) {
                throw quiet(e);
            }
        }
    }

    /**
     * Property assignment API.
     */
    public static interface ÅssignableTraders<Next> {

        /**
         * Assign traders property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next traders(List<? extends Trader> value) {
            ((BackTest) this).setTraders((java.util.List)value);
            return (Next) this;
        }

        /**
         * Assign traders property.
         * 
         * @return The next assignable model.
         */
        default Next traders(Trader... values) {
            return traders(List.of(values));
        }
    }

    /**
     * Property assignment API.
     */
    public static interface ÅssignableÅrbitrary<Next extends BackTest> {

        /**
         * Assign initialBaseCurrency property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next initialBaseCurrency(Num value) {
            ((BackTest) this).setInitialBaseCurrency(value);
            return (Next) this;
        }

        /**
         * Set the initial assets.
         *  
         *  @return
         */
        default Next initialBaseCurrency(double value) {
            try {
                return initialBaseCurrency((Num) initialBaseCurrency$912239839.invoke(this, value));
            } catch (Throwable e) {
                throw quiet(e);
            }
        }

        /**
         * Assign initialTargetCurrency property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next initialTargetCurrency(Num value) {
            ((BackTest) this).setInitialTargetCurrency(value);
            return (Next) this;
        }

        /**
         * Set the initial assets.
         *  
         *  @return
         */
        default Next initialTargetCurrency(double value) {
            try {
                return initialTargetCurrency((Num) initialTargetCurrency$912239839.invoke(this, value));
            } catch (Throwable e) {
                throw quiet(e);
            }
        }

        /**
         * Assign detail property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next detail(boolean value) {
            ((BackTest) this).setDetail(value);
            return (Next) this;
        }

        /**
         * Assign type property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next type(LogType value) {
            ((BackTest) this).setType(value);
            return (Next) this;
        }

        /**
         * Assign type property.
         * 
         * @return The next assignable model.
         */
        default Next fast() {
            return type(LogType.Fast);
        }

        /**
         * Assign type property.
         * 
         * @return The next assignable model.
         */
        default Next normal() {
            return type(LogType.Normal);
        }
    }

    /**
     * Internal aggregated API.
     */
    protected static interface ÅssignableAll extends ÅssignableService, ÅssignableStart, ÅssignableEnd, ÅssignableTraders {
    }

    /**
     * Mutable Model.
     */
    private static final class Åssignable extends BackTest implements ÅssignableAll, ÅssignableÅrbitrary {
    }

    /**
     * The identifier for properties.
     */
    static final class My {
        static final String Service = "service";
        static final String Start = "start";
        static final String End = "end";
        static final String Traders = "traders";
        static final String InitialBaseCurrency = "initialBaseCurrency";
        static final String InitialTargetCurrency = "initialTargetCurrency";
        static final String Detail = "detail";
        static final String Type = "type";
    }
}
