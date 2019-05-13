package cointoss.market.bitflyer;

import cointoss.Direction;
import cointoss.util.Num;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.annotation.processing.Generated;

/**
 * Generated model for {@link BitFlyerExecution2Model}.
 */
@Generated("Icy Manipulator")
public abstract class BitFlyerExecution2 extends BitFlyerExecution2Model {

    /**
     * Create special method invoker.
     *
     * @param name A target method name.
     * @param parameterTypes A list of method parameter types.
     * @return A special method invoker.
     */
    private static final MethodHandle invoker(String name, Class... parameterTypes)  {
        try {
            Method method = BitFlyerExecution2Model.class.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
            return MethodHandles.lookup().unreflect(method);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /**
     * Create special property updater.
     *
     * @param name A target property name.
     * @return A special property updater.
     */
    private static final MethodHandle updater(String name)  {
        try {
            Field field = BitFlyerExecution2.class.getDeclaredField(name);
            field.setAccessible(true);
            return MethodHandles.lookup().unreflectSetter(field);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /**
     * HIDE CONSTRUCTOR
     */
    protected BitFlyerExecution2() {
    }

    /** The singleton builder. */
    public static final  Ìnstantiator<?> with = new Ìnstantiator();

    /**
     * Builder namespace for {@link BitFlyerExecution2}.
     */
    public static final class Ìnstantiator<Self extends BitFlyerExecution2 & ÅssignableÅrbitrary<Self>> {
        /**
         * Create uninitialized {@link BitFlyerExecution2}.
         */
        public final <T extends Self> T direction(Direction direction, Num num) {
            Åssignable o = new Åssignable();
            o.direction(direction);
            o.size(num);
            return (T) o;
        }
        /**
         * Create uninitialized {@link BitFlyerExecution2}.
         */
        public final <T extends Self> T direction(Direction direction, int size) {
            Åssignable o = new Åssignable();
            o.direction(direction);
            o.size(size);
            return (T) o;
        }
        /**
         * Create uninitialized {@link BitFlyerExecution2}.
         */
        public final <T extends Self> T direction(Direction direction, float size) {
            Åssignable o = new Åssignable();
            o.direction(direction);
            o.size(size);
            return (T) o;
        }
        /**
         * Create uninitialized {@link BitFlyerExecution2}.
         */
        public final <T extends Self> T direction(Direction direction, long size) {
            Åssignable o = new Åssignable();
            o.direction(direction);
            o.size(size);
            return (T) o;
        }
        /**
         * Create uninitialized {@link BitFlyerExecution2}.
         */
        public final <T extends Self> T direction(Direction direction, double size) {
            Åssignable o = new Åssignable();
            o.direction(direction);
            o.size(size);
            return (T) o;
        }
        /**
         * Create uninitialized {@link BitFlyerExecution2}.
         */
        public final <T extends Self> T buy(Num num) {
            Åssignable o = new Åssignable();
            o.buy();
            o.size(num);
            return (T) o;
        }
        /**
         * Create uninitialized {@link BitFlyerExecution2}.
         */
        public final <T extends Self> T buy(int size) {
            Åssignable o = new Åssignable();
            o.buy();
            o.size(size);
            return (T) o;
        }
        /**
         * Create uninitialized {@link BitFlyerExecution2}.
         */
        public final <T extends Self> T buy(float size) {
            Åssignable o = new Åssignable();
            o.buy();
            o.size(size);
            return (T) o;
        }
        /**
         * Create uninitialized {@link BitFlyerExecution2}.
         */
        public final <T extends Self> T buy(long size) {
            Åssignable o = new Åssignable();
            o.buy();
            o.size(size);
            return (T) o;
        }
        /**
         * Create uninitialized {@link BitFlyerExecution2}.
         */
        public final <T extends Self> T buy(double size) {
            Åssignable o = new Åssignable();
            o.buy();
            o.size(size);
            return (T) o;
        }
        /**
         * Create uninitialized {@link BitFlyerExecution2}.
         */
        public final <T extends Self> T sell(Num num) {
            Åssignable o = new Åssignable();
            o.sell();
            o.size(num);
            return (T) o;
        }
        /**
         * Create uninitialized {@link BitFlyerExecution2}.
         */
        public final <T extends Self> T sell(int size) {
            Åssignable o = new Åssignable();
            o.sell();
            o.size(size);
            return (T) o;
        }
        /**
         * Create uninitialized {@link BitFlyerExecution2}.
         */
        public final <T extends Self> T sell(float size) {
            Åssignable o = new Åssignable();
            o.sell();
            o.size(size);
            return (T) o;
        }
        /**
         * Create uninitialized {@link BitFlyerExecution2}.
         */
        public final <T extends Self> T sell(long size) {
            Åssignable o = new Åssignable();
            o.sell();
            o.size(size);
            return (T) o;
        }
        /**
         * Create uninitialized {@link BitFlyerExecution2}.
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
    public static interface ÅssignableÅrbitrary<Next extends BitFlyerExecution2> extends cointoss.execution.Execution.ÅssignableÅrbitrary<Next> {
    }

    /**
     * Internal aggregated API.
     */
    protected static interface ÅssignableAll extends cointoss.execution.Execution.ÅssignableAll {
    }

    /**
     * Mutable Model.
     */
    private static final class Åssignable extends BitFlyerExecution2 implements ÅssignableAll, ÅssignableÅrbitrary {
    }

    /**
     * The identifier for properties.
     */
    static final class My {
    }
}
