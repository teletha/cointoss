package cointoss.util;

import cointoss.util.Retry;
import cointoss.util.RetryModel;
import java.lang.Override;
import java.lang.StringBuilder;
import java.lang.Throwable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.LongFunction;
import javax.annotation.processing.Generated;

/**
 * Generated model for {@link RetryModel}.
 */
@Generated("Icy Manipulator")
public abstract class Retry extends RetryModel {

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
            Method method = RetryModel.class.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
            return MethodHandles.lookup().unreflect(method);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /** The overload or intercept method invoker. */
    private static final MethodHandle unlimit$1= invoker("unlimit");

    /** The overload or intercept method invoker. */
    private static final MethodHandle delay$823551244= invoker("delay", Duration.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle delay$968000397= invoker("delay", long.class, ChronoUnit.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle delay$2234397= invoker("delay", long.class, TimeUnit.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle delayLinear$823551244= invoker("delayLinear", Duration.class);

    /** The overload or intercept method invoker. */
    private static final MethodHandle delayExponential$823551244= invoker("delayExponential", Duration.class);

    /**
     * Create special property updater.
     *
     * @param name A target property name.
     * @return A special property updater.
     */
    private static final MethodHandle updater(String name)  {
        try {
            Field field = Retry.class.getDeclaredField(name);
            field.setAccessible(true);
            return MethodHandles.lookup().unreflectSetter(field);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /** The final property updater. */
    private static final MethodHandle limitUpdater = updater("limit");

    /** The final property updater. */
    private static final MethodHandle delayUpdater = updater("delay");

    /** The final property updater. */
    private static final MethodHandle delayMinimumUpdater = updater("delayMinimum");

    /** The final property updater. */
    private static final MethodHandle delayMaximumUpdater = updater("delayMaximum");

    /** The final property updater. */
    private static final MethodHandle schedulerUpdater = updater("scheduler");

    /** The exposed property. */
    final long limit;

    /** The exposed property. */
    final LongFunction<Duration> delay;

    /** The exposed property. */
    final Duration delayMinimum;

    /** The exposed property. */
    final Duration delayMaximum;

    /** The exposed property. */
    final ScheduledExecutorService scheduler;

    /**
     * HIDE CONSTRUCTOR
     */
    protected Retry() {
        this.limit = 0L;
        this.delay = super.delay();
        this.delayMinimum = super.delayMinimum();
        this.delayMaximum = super.delayMaximum();
        this.scheduler = super.scheduler();
    }

    /**
     * Return the limit property.
     *
     * @return A value of limit property.
     */
    @Override
    final long limit() {
        return this.limit;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of limit property.
     */
    @SuppressWarnings("unused")
    private final long getLimit() {
        return this.limit;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of limit property to assign.
     */
    private final void setLimit(long value) {
        try {
            limitUpdater.invoke(this, value);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Return the delay property.
     *
     * @return A value of delay property.
     */
    @Override
    final LongFunction<Duration> delay() {
        return this.delay;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of delay property.
     */
    @SuppressWarnings("unused")
    private final LongFunction<Duration> getDelay() {
        return this.delay;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of delay property to assign.
     */
    private final void setDelay(LongFunction<Duration> value) {
        if (value == null) {
            value = super.delay();
        }
        try {
            delayUpdater.invoke(this, value);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Return the delayMinimum property.
     *
     * @return A value of delayMinimum property.
     */
    @Override
    final Duration delayMinimum() {
        return this.delayMinimum;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of delayMinimum property.
     */
    @SuppressWarnings("unused")
    private final Duration getDelayMinimum() {
        return this.delayMinimum;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of delayMinimum property to assign.
     */
    private final void setDelayMinimum(Duration value) {
        if (value == null) {
            value = super.delayMinimum();
        }
        try {
            delayMinimumUpdater.invoke(this, value);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Return the delayMaximum property.
     *
     * @return A value of delayMaximum property.
     */
    @Override
    final Duration delayMaximum() {
        return this.delayMaximum;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of delayMaximum property.
     */
    @SuppressWarnings("unused")
    private final Duration getDelayMaximum() {
        return this.delayMaximum;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of delayMaximum property to assign.
     */
    private final void setDelayMaximum(Duration value) {
        if (value == null) {
            value = super.delayMaximum();
        }
        try {
            delayMaximumUpdater.invoke(this, value);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Return the scheduler property.
     *
     * @return A value of scheduler property.
     */
    @Override
    final ScheduledExecutorService scheduler() {
        return this.scheduler;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of scheduler property.
     */
    @SuppressWarnings("unused")
    private final ScheduledExecutorService getScheduler() {
        return this.scheduler;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of scheduler property to assign.
     */
    private final void setScheduler(ScheduledExecutorService value) {
        if (value == null) {
            value = super.scheduler();
        }
        try {
            schedulerUpdater.invoke(this, value);
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
        StringBuilder builder = new StringBuilder("Retry [");
        builder.append("limit=").append(limit).append(", ");
        builder.append("delay=").append(delay).append(", ");
        builder.append("delayMinimum=").append(delayMinimum).append(", ");
        builder.append("delayMaximum=").append(delayMaximum).append(", ");
        builder.append("scheduler=").append(scheduler).append("]");
        return builder.toString();
    }

    /**
     * Generates a hash code for a sequence of property values. The hash code is generated as if all the property values were placed into an array, and that array were hashed by calling Arrays.hashCode(Object[]). 
     *
     * @return A hash value of the sequence of property values.
     */
    @Override
    public int hashCode() {
        return Objects.hash(limit, delay, delayMinimum, delayMaximum, scheduler);
    }

    /**
     * Returns true if the all properties are equal to each other and false otherwise. Consequently, if both properties are null, true is returned and if exactly one property is null, false is returned. Otherwise, equality is determined by using the equals method of the base model. 
     *
     * @return true if the all properties are equal to each other and false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof Retry == false) {
            return false;
        }

        Retry other = (Retry) o;
        if (limit != other.limit) return false;
        if (!Objects.equals(delay, other.delay)) return false;
        if (!Objects.equals(delayMinimum, other.delayMinimum)) return false;
        if (!Objects.equals(delayMaximum, other.delayMaximum)) return false;
        if (!Objects.equals(scheduler, other.scheduler)) return false;
        return true;
    }

    /** The singleton builder. */
    public static final  Ìnstantiator<?> with = new Ìnstantiator();

    /**
     * Namespace for {@link Retry}  builder methods.
     */
    public static class Ìnstantiator<Self extends Retry & ÅssignableÅrbitrary<Self>> {

        /**
         * Create new {@link Retry} with the specified limit property.
         * 
         * @return The next assignable model.
         */
        public Self limit(long limit) {
            Åssignable o = new Åssignable();
            o.limit(limit);
            return (Self)o;
        }

        /**
         * Create new {@link Retry} with the specified limit property.
         * 
         * @return The next assignable model.
         */
        public Self unlimit() {
            Åssignable o = new Åssignable();
            o.unlimit();
            return (Self)o;
        }
    }

    /**
     * Property assignment API.
     */
    public static interface ÅssignableLimit<Next> {

        /**
         * Assign limit property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next limit(long value) {
            ((Retry) this).setLimit(value);
            return (Next) this;
        }

        /**
         * Assign limit property.
         * 
         * @return The next assignable model.
         */
        default Next unlimit() {
            try {
                return limit((long) unlimit$1.invoke(this));
            } catch (Throwable e) {
                throw quiet(e);
            }
        }
    }

    /**
     * Property assignment API.
     */
    public static interface ÅssignableÅrbitrary<Next extends Retry> {

        /**
         * Assign delay property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next delay(LongFunction<Duration> value) {
            ((Retry) this).setDelay(value);
            return (Next) this;
        }

        /**
         * Assign delay property.
         * 
         * @return The next assignable model.
         */
        default Next delay(Duration delay) {
            try {
                return delay((LongFunction<Duration>) delay$823551244.invoke(this, delay));
            } catch (Throwable e) {
                throw quiet(e);
            }
        }

        /**
         * Assign delay property.
         * 
         * @return The next assignable model.
         */
        default Next delay(long time, ChronoUnit unit) {
            try {
                return delay((LongFunction<Duration>) delay$968000397.invoke(this, time, unit));
            } catch (Throwable e) {
                throw quiet(e);
            }
        }

        /**
         * Assign delay property.
         * 
         * @return The next assignable model.
         */
        default Next delay(long time, TimeUnit unit) {
            try {
                return delay((LongFunction<Duration>) delay$2234397.invoke(this, time, unit));
            } catch (Throwable e) {
                throw quiet(e);
            }
        }

        /**
         * Assign delay property.
         * 
         * @return The next assignable model.
         */
        default Next delayLinear(Duration delay) {
            try {
                return delay((LongFunction<Duration>) delayLinear$823551244.invoke(this, delay));
            } catch (Throwable e) {
                throw quiet(e);
            }
        }

        /**
         * Assign delay property.
         * 
         * @return The next assignable model.
         */
        default Next delayExponential(Duration delay) {
            try {
                return delay((LongFunction<Duration>) delayExponential$823551244.invoke(this, delay));
            } catch (Throwable e) {
                throw quiet(e);
            }
        }

        /**
         * Assign delayMinimum property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next delayMinimum(Duration value) {
            ((Retry) this).setDelayMinimum(value);
            return (Next) this;
        }

        /**
         * Assign delayMaximum property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next delayMaximum(Duration value) {
            ((Retry) this).setDelayMaximum(value);
            return (Next) this;
        }

        /**
         * Assign scheduler property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next scheduler(ScheduledExecutorService value) {
            ((Retry) this).setScheduler(value);
            return (Next) this;
        }
    }

    /**
     * Internal aggregated API.
     */
    protected static interface ÅssignableAll extends ÅssignableLimit {
    }

    /**
     * Mutable Model.
     */
    private static final class Åssignable extends Retry implements ÅssignableAll, ÅssignableÅrbitrary {
    }

    /**
     * The identifier for properties.
     */
    static final class My {
        static final String Limit = "limit";
        static final String Delay = "delay";
        static final String DelayMinimum = "delayMinimum";
        static final String DelayMaximum = "delayMaximum";
        static final String Scheduler = "scheduler";
    }
}
