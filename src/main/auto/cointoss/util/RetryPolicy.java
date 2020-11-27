package cointoss.util;

import cointoss.util.RetryPolicy;
import cointoss.util.RetryPolicyModel;
import java.lang.Override;
import java.lang.String;
import java.lang.Throwable;
import java.lang.UnsupportedOperationException;
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
    private static final MethodHandle nameUpdater = updater("name");

    /** The final property updater. */
    private static final MethodHandle schedulerUpdater = updater("scheduler");

    /** The exposed property. */
    final long limit;

    /** The exposed property. */
    final LongFunction<Duration> delay;

    /** The exposed property. */
    final String name;

    /** The exposed property. */
    final ScheduledExecutorService scheduler;

    /**
     * HIDE CONSTRUCTOR
     */
    protected RetryPolicy() {
        this.limit = 0L;
        this.delay = super.delay();
        this.name = super.name();
        this.scheduler = super.scheduler();
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
        } catch (UnsupportedOperationException e) {
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
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Show debuggable name with the specified name.
     *  
     *  @return
     */
    @Override
    final String name() {
        return this.name;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of name property.
     */
    @SuppressWarnings("unused")
    private final String getName() {
        return this.name;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of name property to assign.
     */
    private final void setName(String value) {
        if (value == null) {
            value = super.name();
        }
        try {
            nameUpdater.invoke(this, value);
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Set the name of this policy.
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
        return Objects.hash(limit, delay, name, scheduler);
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
        if (!Objects.equals(name, other.name)) return false;
        if (!Objects.equals(scheduler, other.scheduler)) return false;
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
         * Assign name property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next name(String value) {
            ((RetryPolicy) this).setName(value);
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
        static final String Name = "name";
        static final String Scheduler = "scheduler";
    }
}
