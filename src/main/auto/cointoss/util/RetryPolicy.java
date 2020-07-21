package cointoss.util;

import cointoss.util.RetryPolicy;
import cointoss.util.RetryPolicyModel;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.Throwable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.LongFunction;
import javax.annotation.processing.Generated;

/**
 * Generated model for {@link RetryPolicyModel}.
 */
@Generated("Icy Manipulator")
public abstract class RetryPolicy extends RetryPolicyModel {

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
            Method method = RetryPolicyModel.class.getDeclaredMethod(name, parameterTypes);
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

    /**
     * Create special property updater.
     *
     * @param name A target property name.
     * @return A special property updater.
     */
    private static final MethodHandle updater(String name)  {
        try {
            Field field = RetryPolicy.class.getDeclaredField(name);
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

    /** The final property updater. */
    private static final MethodHandle autoResetUpdater = updater("autoReset");

    /** The final property updater. */
    private static final MethodHandle ignoreUpdater = updater("ignore");

    /** The final property updater. */
    private static final MethodHandle debugUpdater = updater("debug");

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

    /** The exposed property. */
    final boolean autoReset;

    /** The exposed property. */
    final List<Class<? extends Throwable>> ignore;

    /** The exposed property. */
    final String debug;

    /**
     * HIDE CONSTRUCTOR
     */
    protected RetryPolicy() {
        this.limit = 0L;
        this.delay = super.delay();
        this.delayMinimum = super.delayMinimum();
        this.delayMaximum = super.delayMaximum();
        this.scheduler = super.scheduler();
        this.autoReset = super.autoReset();
        this.ignore = super.ignore();
        this.debug = super.debug();
    }

    /**
     * Set maximum number of trials.
     *  
     *  @return
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
     * Set the delay time between trials.
     *  
     *  @return
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
     * Set the minimum time to delay. The default is 0 seconds.
     *  
     *  @return
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
     * Set the maximum time to delay. The default is 10 minutes.
     *  
     *  @return
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
     * Set the scheduler to manage the delay.
     *  
     *  @return
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
     * Set the scheduler to manage the delay.
     *  
     *  @return
     */
    @Override
    final boolean autoReset() {
        return this.autoReset;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of autoReset property.
     */
    @SuppressWarnings("unused")
    private final boolean getAutoReset() {
        return this.autoReset;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of autoReset property to assign.
     */
    private final void setAutoReset(boolean value) {
        try {
            autoResetUpdater.invoke(this, value);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Ignore the specified error types.
     *  
     *  @return
     */
    @Override
    final List<Class<? extends Throwable>> ignore() {
        return this.ignore;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of ignore property.
     */
    @SuppressWarnings("unused")
    private final List<Class<? extends Throwable>> getIgnore() {
        return this.ignore;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of ignore property to assign.
     */
    private final void setIgnore(List<Class<? extends Throwable>> value) {
        if (value == null) {
            value = super.ignore();
        }
        try {
            ignoreUpdater.invoke(this, value);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Show debuggable message with the specified name.
     *  
     *  @return
     */
    @Override
    final String debug() {
        return this.debug;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of debug property.
     */
    @SuppressWarnings("unused")
    private final String getDebug() {
        return this.debug;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of debug property to assign.
     */
    private final void setDebug(String value) {
        if (value == null) {
            value = super.debug();
        }
        try {
            debugUpdater.invoke(this, value);
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
        return Objects.hash(limit, delay, delayMinimum, delayMaximum, scheduler, autoReset, ignore, debug);
    }

    /**
     * Returns true if the all properties are equal to each other and false otherwise. Consequently, if both properties are null, true is returned and if exactly one property is null, false is returned. Otherwise, equality is determined by using the equals method of the base model. 
     *
     * @return true if the all properties are equal to each other and false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof RetryPolicy == false) {
            return false;
        }

        RetryPolicy other = (RetryPolicy) o;
        if (limit != other.limit) return false;
        if (!Objects.equals(delay, other.delay)) return false;
        if (!Objects.equals(delayMinimum, other.delayMinimum)) return false;
        if (!Objects.equals(delayMaximum, other.delayMaximum)) return false;
        if (!Objects.equals(scheduler, other.scheduler)) return false;
        if (autoReset != other.autoReset) return false;
        if (!Objects.equals(ignore, other.ignore)) return false;
        if (!Objects.equals(debug, other.debug)) return false;
        return true;
    }

    /** The singleton builder. */
    public static final  Ìnstantiator<?> with = new Ìnstantiator();

    /**
     * Namespace for {@link RetryPolicy}  builder methods.
     */
    public static class Ìnstantiator<Self extends RetryPolicy & ÅssignableÅrbitrary<Self>> {

        /**
         * Create new {@link RetryPolicy} with the specified limit property.
         * 
         * @return The next assignable model.
         */
        public Self limit(long limit) {
            Åssignable o = new Åssignable();
            o.limit(limit);
            return (Self)o;
        }

        /**
         * Set limit number to {@link Long#MAX_VALUE}.
         *  
         *  @return
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
            ((RetryPolicy) this).setLimit(value);
            return (Next) this;
        }

        /**
         * Set limit number to {@link Long#MAX_VALUE}.
         *  
         *  @return
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
    public static interface ÅssignableÅrbitrary<Next extends RetryPolicy> {

        /**
         * Assign delay property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next delay(LongFunction<Duration> value) {
            ((RetryPolicy) this).setDelay(value);
            return (Next) this;
        }

        /**
         * Set the delay time between trials.
         *  
         *  @param delay
         *  @return
         */
        default Next delay(Duration delay) {
            try {
                return delay((LongFunction<Duration>) delay$823551244.invoke(this, delay));
            } catch (Throwable e) {
                throw quiet(e);
            }
        }

        /**
         * Set the delay time between trials.
         *  
         *  @param delay
         *  @return
         */
        default Next delay(long time, ChronoUnit unit) {
            try {
                return delay((LongFunction<Duration>) delay$968000397.invoke(this, time, unit));
            } catch (Throwable e) {
                throw quiet(e);
            }
        }

        /**
         * Set the delay time between trials.
         *  
         *  @param delay
         *  @return
         */
        default Next delay(long time, TimeUnit unit) {
            try {
                return delay((LongFunction<Duration>) delay$2234397.invoke(this, time, unit));
            } catch (Throwable e) {
                throw quiet(e);
            }
        }

        /**
         * Set the linear delay time between trials.
         *  
         *  @param baseDelay
         *  @return
         */
        default Next delayLinear(Duration baseDelay) {
            try {
                return delay((LongFunction<Duration>) delayLinear$823551244.invoke(this, baseDelay));
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
            ((RetryPolicy) this).setDelayMinimum(value);
            return (Next) this;
        }

        /**
         * Assign delayMaximum property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next delayMaximum(Duration value) {
            ((RetryPolicy) this).setDelayMaximum(value);
            return (Next) this;
        }

        /**
         * Assign scheduler property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next scheduler(ScheduledExecutorService value) {
            ((RetryPolicy) this).setScheduler(value);
            return (Next) this;
        }

        /**
         * Assign autoReset property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next autoReset(boolean value) {
            ((RetryPolicy) this).setAutoReset(value);
            return (Next) this;
        }

        /**
         * Assign ignore property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next ignore(List<Class<? extends Throwable>> value) {
            ((RetryPolicy) this).setIgnore(value);
            return (Next) this;
        }

        /**
         * Assign ignore property.
         * 
         * @return The next assignable model.
         */
        default Next ignore(Class<? extends Throwable>... values) {
            return ignore(List.of(values));
        }

        /**
         * Assign debug property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next debug(String value) {
            ((RetryPolicy) this).setDebug(value);
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
    private static final class Åssignable extends RetryPolicy implements ÅssignableAll, ÅssignableÅrbitrary {
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
        static final String AutoReset = "autoReset";
        static final String Ignore = "ignore";
        static final String Debug = "debug";
    }
}
