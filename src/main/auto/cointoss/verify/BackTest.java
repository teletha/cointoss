package cointoss.verify;

import cointoss.MarketService;
import cointoss.util.Num;
import cointoss.verify.BackTest;
import cointoss.verify.BackTestModel;
import java.lang.Override;
import java.lang.StringBuilder;
import java.lang.Throwable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.ZonedDateTime;
import java.util.Objects;
import javax.annotation.processing.Generated;

/**
 * Generated model for {@link BackTestModel}.
 */
@Generated("Icy Manipulator")
public abstract class BackTest extends BackTestModel {

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
            Method method = BackTestModel.class.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
            return MethodHandles.lookup().unreflect(method);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /** The overload or intercept method invoker. */
    private static final MethodHandle start$1860699197= invoker("start", int.class, int.class, int.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle end$1860699197= invoker("end", int.class, int.class, int.class);

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
    private static final MethodHandle updater(String name)  {
        try {
            Field field = BackTest.class.getDeclaredField(name);
            field.setAccessible(true);
            return MethodHandles.lookup().unreflectSetter(field);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /** The final property updater. */
    private static final MethodHandle serviceUpdater = updater("service");

    /** The final property updater. */
    private static final MethodHandle startUpdater = updater("start");

    /** The final property updater. */
    private static final MethodHandle endUpdater = updater("end");

    /** The final property updater. */
    private static final MethodHandle initialBaseCurrencyUpdater = updater("initialBaseCurrency");

    /** The final property updater. */
    private static final MethodHandle initialTargetCurrencyUpdater = updater("initialTargetCurrency");

    /** The exposed property. */
    public final MarketService service;

    /** The exposed property. */
    public final ZonedDateTime start;

    /** The exposed property. */
    public final ZonedDateTime end;

    /** The exposed property. */
    public final Num initialBaseCurrency;

    /** The exposed property. */
    public final Num initialTargetCurrency;

    /**
     * HIDE CONSTRUCTOR
     */
    protected BackTest() {
        this.service = null;
        this.start = null;
        this.end = null;
        this.initialBaseCurrency = super.initialBaseCurrency();
        this.initialTargetCurrency = super.initialTargetCurrency();
    }

    /**
     * Return the service property.
     *
     * @return A value of service property.
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
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Return the start property.
     *
     * @return A value of start property.
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
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Return the end property.
     *
     * @return A value of end property.
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
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Return the initialBaseCurrency property.
     *
     * @return A value of initialBaseCurrency property.
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
            value = super.initialBaseCurrency();
        }
        try {
            initialBaseCurrencyUpdater.invoke(this, value);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Return the initialTargetCurrency property.
     *
     * @return A value of initialTargetCurrency property.
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
            value = super.initialTargetCurrency();
        }
        try {
            initialTargetCurrencyUpdater.invoke(this, value);
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
        builder.append("initialBaseCurrency=").append(initialBaseCurrency).append(", ");
        builder.append("initialTargetCurrency=").append(initialTargetCurrency).append("]");
        return builder.toString();
    }

    /**
     * Generates a hash code for a sequence of property values. The hash code is generated as if all the property values were placed into an array, and that array were hashed by calling Arrays.hashCode(Object[]). 
     *
     * @return A hash value of the sequence of property values.
     */
    @Override
    public int hashCode() {
        return Objects.hash(service, start, end, initialBaseCurrency, initialTargetCurrency);
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
        if (!Objects.equals(initialBaseCurrency, other.initialBaseCurrency)) return false;
        if (!Objects.equals(initialTargetCurrency, other.initialTargetCurrency)) return false;
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
        public ÅssignableStart<ÅssignableEnd<Self>> service(MarketService service) {
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
         * Assign start property.
         * 
         * @return The next assignable model.
         */
        default Next start(int year, int month, int day) {
            try {
                return start((ZonedDateTime) start$1860699197.invoke(this, year, month, day));
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
         * Assign end property.
         * 
         * @return The next assignable model.
         */
        default Next end(int year, int month, int day) {
            try {
                return end((ZonedDateTime) end$1860699197.invoke(this, year, month, day));
            } catch (Throwable e) {
                throw quiet(e);
            }
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
         * Assign initialBaseCurrency property.
         * 
         * @return The next assignable model.
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
         * Assign initialTargetCurrency property.
         * 
         * @return The next assignable model.
         */
        default Next initialTargetCurrency(double value) {
            try {
                return initialTargetCurrency((Num) initialTargetCurrency$912239839.invoke(this, value));
            } catch (Throwable e) {
                throw quiet(e);
            }
        }
    }

    /**
     * Internal aggregated API.
     */
    protected static interface ÅssignableAll extends ÅssignableService, ÅssignableStart, ÅssignableEnd {
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
        static final String InitialBaseCurrency = "initialBaseCurrency";
        static final String InitialTargetCurrency = "initialTargetCurrency";
    }
}
