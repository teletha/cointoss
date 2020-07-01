package cointoss.util;

import cointoss.util.APILimiter;
import cointoss.util.APILimiterModel;
import java.lang.Override;
import java.lang.StringBuilder;
import java.lang.Throwable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Objects;
import javax.annotation.processing.Generated;

/**
 * Generated model for {@link APILimiterModel}.
 */
@Generated("Icy Manipulator")
public abstract class APILimiter extends APILimiterModel {

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
            Method method = APILimiterModel.class.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
            return MethodHandles.lookup().unreflect(method);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /** The overload or intercept method invoker. */
    private static final MethodHandle config$823551244= invoker("config", Duration.class);

    /**
     * Create special property updater.
     *
     * @param name A target property name.
     * @return A special property updater.
     */
    private static final MethodHandle updater(String name)  {
        try {
            Field field = APILimiter.class.getDeclaredField(name);
            field.setAccessible(true);
            return MethodHandles.lookup().unreflectSetter(field);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /** The final property updater. */
    private static final MethodHandle limitUpdater = updater("limit");

    /** The final property updater. */
    private static final MethodHandle refreshUpdater = updater("refresh");

    /** The final property updater. */
    private static final MethodHandle expressUpdater = updater("express");

    /** The exposed property. */
    public final int limit;

    /** The exposed property. */
    public final Duration refresh;

    /** The exposed property. */
    public final int express;

    /**
     * HIDE CONSTRUCTOR
     */
    protected APILimiter() {
        this.limit = 0;
        this.refresh = null;
        this.express = super.express();
    }

    /**
     * Return the limit property.
     *
     * @return A value of limit property.
     */
    @Override
    public final int limit() {
        return this.limit;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of limit property.
     */
    @SuppressWarnings("unused")
    private final int getLimit() {
        return this.limit;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of limit property to assign.
     */
    private final void setLimit(int value) {
        try {
            limitUpdater.invoke(this, value);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Return the refresh property.
     *
     * @return A value of refresh property.
     */
    @Override
    public final Duration refresh() {
        return this.refresh;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of refresh property.
     */
    @SuppressWarnings("unused")
    private final Duration getRefresh() {
        return this.refresh;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of refresh property to assign.
     */
    private final void setRefresh(Duration value) {
        if (value == null) {
            throw new IllegalArgumentException("The refresh property requires non-null value.");
        }
        try {
            refreshUpdater.invoke(this, config$823551244.invoke(this, value));
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Return the express property.
     *
     * @return A value of express property.
     */
    @Override
    public final int express() {
        return this.express;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of express property.
     */
    @SuppressWarnings("unused")
    private final int getExpress() {
        return this.express;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of express property to assign.
     */
    private final void setExpress(int value) {
        try {
            expressUpdater.invoke(this, value);
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
        StringBuilder builder = new StringBuilder("APILimiter [");
        builder.append("limit=").append(limit).append(", ");
        builder.append("refresh=").append(refresh).append(", ");
        builder.append("express=").append(express).append("]");
        return builder.toString();
    }

    /**
     * Generates a hash code for a sequence of property values. The hash code is generated as if all the property values were placed into an array, and that array were hashed by calling Arrays.hashCode(Object[]). 
     *
     * @return A hash value of the sequence of property values.
     */
    @Override
    public int hashCode() {
        return Objects.hash(limit, refresh, express);
    }

    /**
     * Returns true if the all properties are equal to each other and false otherwise. Consequently, if both properties are null, true is returned and if exactly one property is null, false is returned. Otherwise, equality is determined by using the equals method of the base model. 
     *
     * @return true if the all properties are equal to each other and false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof APILimiter == false) {
            return false;
        }

        APILimiter other = (APILimiter) o;
        if (limit != other.limit) return false;
        if (!Objects.equals(refresh, other.refresh)) return false;
        if (express != other.express) return false;
        return true;
    }

    /** The singleton builder. */
    public static final  Ìnstantiator<?> with = new Ìnstantiator();

    /**
     * Namespace for {@link APILimiter}  builder methods.
     */
    public static class Ìnstantiator<Self extends APILimiter & ÅssignableÅrbitrary<Self>> {

        /**
         * Create new {@link APILimiter} with the specified limit property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableRefresh<Self> limit(int limit) {
            Åssignable o = new Åssignable();
            o.limit(limit);
            return o;
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
        default Next limit(int value) {
            ((APILimiter) this).setLimit(value);
            return (Next) this;
        }
    }

    /**
     * Property assignment API.
     */
    public static interface ÅssignableRefresh<Next> {

        /**
         * Assign refresh property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next refresh(Duration value) {
            ((APILimiter) this).setRefresh(value);
            return (Next) this;
        }
    }

    /**
     * Property assignment API.
     */
    public static interface ÅssignableÅrbitrary<Next extends APILimiter> {

        /**
         * Assign express property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next express(int value) {
            ((APILimiter) this).setExpress(value);
            return (Next) this;
        }
    }

    /**
     * Internal aggregated API.
     */
    protected static interface ÅssignableAll extends ÅssignableLimit, ÅssignableRefresh {
    }

    /**
     * Mutable Model.
     */
    private static final class Åssignable extends APILimiter implements ÅssignableAll, ÅssignableÅrbitrary {
    }

    /**
     * The identifier for properties.
     */
    static final class My {
        static final String Limit = "limit";
        static final String Refresh = "refresh";
        static final String Express = "express";
    }
}
