package cointoss.util;

import cointoss.util.EfficientWebSocket;
import cointoss.util.EfficientWebSocketModel;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.Throwable;
import java.lang.UnsupportedOperationException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.processing.Generated;
import kiss.JSON;

/**
 * Generated model for {@link EfficientWebSocketModel}.
 */
@Generated("Icy Manipulator")
public abstract class EfficientWebSocket extends EfficientWebSocketModel {

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
     * Create special property updater.
     *
     * @param name A target property name.
     * @return A special property updater.
     */
    private static final MethodHandle updater(String name)  {
        try {
            Field field = EfficientWebSocket.class.getDeclaredField(name);
            field.setAccessible(true);
            return MethodHandles.lookup().unreflectSetter(field);
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /** The final property updater. */
    private static final MethodHandle addressUpdater = updater("address");

    /** The final property updater. */
    private static final MethodHandle extractIdUpdater = updater("extractId");

    /** The final property updater. */
    private static final MethodHandle updateIdUpdater = updater("updateId");

    /** The final property updater. */
    private static final MethodHandle maximumSubscriptionsUpdater = updater("maximumSubscriptions");

    /** The final property updater. */
    private static final MethodHandle ignoreMessageIfUpdater = updater("ignoreMessageIf");

    /** The final property updater. */
    private static final MethodHandle recconnectIfUpdater = updater("recconnectIf");

    /** The final property updater. */
    private static final MethodHandle stopRecconnectIfUpdater = updater("stopRecconnectIf");

    /** The final property updater. */
    private static final MethodHandle pongIfUpdater = updater("pongIf");

    /** The final property updater. */
    private static final MethodHandle whenConnectedUpdater = updater("whenConnected");

    /** The final property updater. */
    private static final MethodHandle clientUpdater = updater("client");

    /** The final property updater. */
    private static final MethodHandle schedulerUpdater = updater("scheduler");

    /** The exposed property. */
    public final String address;

    /** The exposed property. */
    public final Function<JSON, String> extractId;

    /** The exposed property. */
    public final Function<JSON, String> updateId;

    /** The exposed property. */
    public final int maximumSubscriptions;

    /** The exposed property. */
    public final Predicate<JSON> ignoreMessageIf;

    /** The exposed property. */
    public final Predicate<JSON> recconnectIf;

    /** The exposed property. */
    public final Predicate<JSON> stopRecconnectIf;

    /** The exposed property. */
    public final Function<JSON, String> pongIf;

    /** The exposed property. */
    public final Consumer<WebSocket> whenConnected;

    /** The exposed property. */
    public final HttpClient client;

    /** The exposed property. */
    public final ScheduledExecutorService scheduler;

    /**
     * HIDE CONSTRUCTOR
     */
    protected EfficientWebSocket() {
        this.address = null;
        this.extractId = null;
        this.updateId = super.updateId();
        this.maximumSubscriptions = super.maximumSubscriptions();
        this.ignoreMessageIf = super.ignoreMessageIf();
        this.recconnectIf = super.recconnectIf();
        this.stopRecconnectIf = super.stopRecconnectIf();
        this.pongIf = super.pongIf();
        this.whenConnected = super.whenConnected();
        this.client = super.client();
        this.scheduler = super.scheduler();
    }

    /**
     * Return the address property.
     *
     * @return A value of address property.
     */
    @Override
    public final String address() {
        return this.address;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of address property.
     */
    @SuppressWarnings("unused")
    private final String getAddress() {
        return this.address;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of address property to assign.
     */
    private final void setAddress(String value) {
        if (value == null) {
            throw new IllegalArgumentException("The address property requires non-null value.");
        }
        try {
            addressUpdater.invoke(this, value);
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Extract channel id from massage.
     *  
     *  @param extractId An id extractor.
     *  @return Chainable API.
     */
    @Override
    public final Function<JSON, String> extractId() {
        return this.extractId;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of extractId property.
     */
    @SuppressWarnings("unused")
    private final Function<JSON, String> getExtractId() {
        return this.extractId;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of extractId property to assign.
     */
    private final void setExtractId(Function<JSON, String> value) {
        if (value == null) {
            throw new IllegalArgumentException("The extractId property requires non-null value.");
        }
        try {
            extractIdUpdater.invoke(this, value);
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * The subscription ID may be determined by the content of the response, so we must extract the
     *  new ID from the response.
     *  
     *  @return Chainable API.
     */
    @Override
    public final Function<JSON, String> updateId() {
        return this.updateId;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of updateId property.
     */
    @SuppressWarnings("unused")
    private final Function<JSON, String> getUpdateId() {
        return this.updateId;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of updateId property to assign.
     */
    private final void setUpdateId(Function<JSON, String> value) {
        if (value == null) {
            value = super.updateId();
        }
        try {
            updateIdUpdater.invoke(this, value);
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Sets the maximum number of subscriptions per connection. Default value is 25.
     *  
     *  @param size The maximum number of subscriptions per connection. A number less than or equal
     *             to 0 is considered unlimited.
     *  @return Chainable API.
     */
    @Override
    public final int maximumSubscriptions() {
        return this.maximumSubscriptions;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of maximumSubscriptions property.
     */
    @SuppressWarnings("unused")
    private final int getMaximumSubscriptions() {
        return this.maximumSubscriptions;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of maximumSubscriptions property to assign.
     */
    private final void setMaximumSubscriptions(int value) {
        try {
            maximumSubscriptionsUpdater.invoke(this, value);
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Ignore JSON that match the specified criteria. This process is very efficient because it is
     *  tried only once for each JSON data on the base stream.
     *  
     *  @param condition
     *  @return Chainable API.
     */
    @Override
    public final Predicate<JSON> ignoreMessageIf() {
        return this.ignoreMessageIf;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of ignoreMessageIf property.
     */
    @SuppressWarnings("unused")
    private final Predicate<JSON> getIgnoreMessageIf() {
        return this.ignoreMessageIf;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of ignoreMessageIf property to assign.
     */
    private final void setIgnoreMessageIf(Predicate<JSON> value) {
        if (value == null) {
            value = super.ignoreMessageIf();
        }
        try {
            ignoreMessageIfUpdater.invoke(this, value);
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Reconnect socket when some message match the specified criteria.
     *  
     *  @param condition
     *  @return Chainable API.
     */
    @Override
    public final Predicate<JSON> recconnectIf() {
        return this.recconnectIf;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of recconnectIf property.
     */
    @SuppressWarnings("unused")
    private final Predicate<JSON> getRecconnectIf() {
        return this.recconnectIf;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of recconnectIf property to assign.
     */
    private final void setRecconnectIf(Predicate<JSON> value) {
        if (value == null) {
            value = super.recconnectIf();
        }
        try {
            recconnectIfUpdater.invoke(this, value);
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Stop reconnecting socket when some message match the specified criteria.
     *  
     *  @param condition
     *  @return Chainable API.
     */
    @Override
    public final Predicate<JSON> stopRecconnectIf() {
        return this.stopRecconnectIf;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of stopRecconnectIf property.
     */
    @SuppressWarnings("unused")
    private final Predicate<JSON> getStopRecconnectIf() {
        return this.stopRecconnectIf;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of stopRecconnectIf property to assign.
     */
    private final void setStopRecconnectIf(Predicate<JSON> value) {
        if (value == null) {
            value = super.stopRecconnectIf();
        }
        try {
            stopRecconnectIfUpdater.invoke(this, value);
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Pong when some message match the specified criteria.
     *  
     *  @param condition
     *  @return Chainable API.
     */
    @Override
    public final Function<JSON, String> pongIf() {
        return this.pongIf;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of pongIf property.
     */
    @SuppressWarnings("unused")
    private final Function<JSON, String> getPongIf() {
        return this.pongIf;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of pongIf property to assign.
     */
    private final void setPongIf(Function<JSON, String> value) {
        if (value == null) {
            value = super.pongIf();
        }
        try {
            pongIfUpdater.invoke(this, value);
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Invoked when connection is established.
     *  
     *  @return
     */
    @Override
    public final Consumer<WebSocket> whenConnected() {
        return this.whenConnected;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of whenConnected property.
     */
    @SuppressWarnings("unused")
    private final Consumer<WebSocket> getWhenConnected() {
        return this.whenConnected;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of whenConnected property to assign.
     */
    private final void setWhenConnected(Consumer<WebSocket> value) {
        if (value == null) {
            value = super.whenConnected();
        }
        try {
            whenConnectedUpdater.invoke(this, value);
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Outputs a detailed log.
     *  
     *  @return Chainable API.
     */
    @Override
    public final HttpClient client() {
        return this.client;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of client property.
     */
    @SuppressWarnings("unused")
    private final HttpClient getClient() {
        return this.client;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of client property to assign.
     */
    private final void setClient(HttpClient value) {
        if (value == null) {
            value = super.client();
        }
        try {
            clientUpdater.invoke(this, value);
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            throw quiet(e);
        }
    }

    /**
     * Outputs a detailed log.
     *  
     *  @return Chainable API.
     */
    @Override
    public final ScheduledExecutorService scheduler() {
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
     * Show all property values.
     *
     * @return All property values.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("EfficientWebSocket [");
        builder.append("address=").append(address).append(", ");
        builder.append("extractId=").append(extractId).append(", ");
        builder.append("updateId=").append(updateId).append(", ");
        builder.append("maximumSubscriptions=").append(maximumSubscriptions).append(", ");
        builder.append("ignoreMessageIf=").append(ignoreMessageIf).append(", ");
        builder.append("recconnectIf=").append(recconnectIf).append(", ");
        builder.append("stopRecconnectIf=").append(stopRecconnectIf).append(", ");
        builder.append("pongIf=").append(pongIf).append(", ");
        builder.append("whenConnected=").append(whenConnected).append(", ");
        builder.append("client=").append(client).append(", ");
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
        return Objects.hash(address, extractId, updateId, maximumSubscriptions, ignoreMessageIf, recconnectIf, stopRecconnectIf, pongIf, whenConnected, client, scheduler);
    }

    /**
     * Returns true if the all properties are equal to each other and false otherwise. Consequently, if both properties are null, true is returned and if exactly one property is null, false is returned. Otherwise, equality is determined by using the equals method of the base model. 
     *
     * @return true if the all properties are equal to each other and false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof EfficientWebSocket == false) {
            return false;
        }

        EfficientWebSocket other = (EfficientWebSocket) o;
        if (!Objects.equals(address, other.address)) return false;
        if (!Objects.equals(extractId, other.extractId)) return false;
        if (!Objects.equals(updateId, other.updateId)) return false;
        if (maximumSubscriptions != other.maximumSubscriptions) return false;
        if (!Objects.equals(ignoreMessageIf, other.ignoreMessageIf)) return false;
        if (!Objects.equals(recconnectIf, other.recconnectIf)) return false;
        if (!Objects.equals(stopRecconnectIf, other.stopRecconnectIf)) return false;
        if (!Objects.equals(pongIf, other.pongIf)) return false;
        if (!Objects.equals(whenConnected, other.whenConnected)) return false;
        if (!Objects.equals(client, other.client)) return false;
        if (!Objects.equals(scheduler, other.scheduler)) return false;
        return true;
    }

    /**
     * Create new {@link EfficientWebSocket} with the specified property and copy other properties from this model.
     *
     * @param value A new value to assign.
     * @return A created new model instance.
     */
    public EfficientWebSocket withAddress(String value) {
        if (this.address == value) {
            return this;
        }
        return with.address(value).extractId(this.extractId).updateId(this.updateId).maximumSubscriptions(this.maximumSubscriptions).ignoreMessageIf(this.ignoreMessageIf).recconnectIf(this.recconnectIf).stopRecconnectIf(this.stopRecconnectIf).pongIf(this.pongIf).whenConnected(this.whenConnected).client(this.client).scheduler(this.scheduler);
    }

    /**
     * Create new {@link EfficientWebSocket} with the specified property and copy other properties from this model.
     *
     * @param value A new value to assign.
     * @return A created new model instance.
     */
    public EfficientWebSocket withClient(HttpClient value) {
        if (this.client == value) {
            return this;
        }
        return with.address(this.address).extractId(this.extractId).updateId(this.updateId).maximumSubscriptions(this.maximumSubscriptions).ignoreMessageIf(this.ignoreMessageIf).recconnectIf(this.recconnectIf).stopRecconnectIf(this.stopRecconnectIf).pongIf(this.pongIf).whenConnected(this.whenConnected).client(value).scheduler(this.scheduler);
    }

    /**
     * Create new {@link EfficientWebSocket} with the specified property and copy other properties from this model.
     *
     * @param value A new value to assign.
     * @return A created new model instance.
     */
    public EfficientWebSocket withScheduler(ScheduledExecutorService value) {
        if (this.scheduler == value) {
            return this;
        }
        return with.address(this.address).extractId(this.extractId).updateId(this.updateId).maximumSubscriptions(this.maximumSubscriptions).ignoreMessageIf(this.ignoreMessageIf).recconnectIf(this.recconnectIf).stopRecconnectIf(this.stopRecconnectIf).pongIf(this.pongIf).whenConnected(this.whenConnected).client(this.client).scheduler(value);
    }

    /** The singleton builder. */
    public static final  Ìnstantiator<?> with = new Ìnstantiator();

    /**
     * Namespace for {@link EfficientWebSocket}  builder methods.
     */
    public static class Ìnstantiator<Self extends EfficientWebSocket & ÅssignableÅrbitrary<Self>> {

        /**
         * Create new {@link EfficientWebSocket} with the specified address property.
         * 
         * @return The next assignable model.
         */
        public ÅssignableExtractId<Self> address(String address) {
            Åssignable o = new Åssignable();
            o.address(address);
            return o;
        }
    }

    /**
     * Property assignment API.
     */
    public static interface ÅssignableAddress<Next> {

        /**
         * Assign address property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next address(String value) {
            ((EfficientWebSocket) this).setAddress(value);
            return (Next) this;
        }
    }

    /**
     * Property assignment API.
     */
    public static interface ÅssignableExtractId<Next> {

        /**
         * Assign extractId property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next extractId(Function<JSON, String> value) {
            ((EfficientWebSocket) this).setExtractId(value);
            return (Next) this;
        }
    }

    /**
     * Property assignment API.
     */
    public static interface ÅssignableÅrbitrary<Next extends EfficientWebSocket> {

        /**
         * Assign updateId property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next updateId(Function<JSON, String> value) {
            ((EfficientWebSocket) this).setUpdateId(value);
            return (Next) this;
        }

        /**
         * Assign maximumSubscriptions property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next maximumSubscriptions(int value) {
            ((EfficientWebSocket) this).setMaximumSubscriptions(value);
            return (Next) this;
        }

        /**
         * Assign ignoreMessageIf property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next ignoreMessageIf(Predicate<JSON> value) {
            ((EfficientWebSocket) this).setIgnoreMessageIf(value);
            return (Next) this;
        }

        /**
         * Assign recconnectIf property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next recconnectIf(Predicate<JSON> value) {
            ((EfficientWebSocket) this).setRecconnectIf(value);
            return (Next) this;
        }

        /**
         * Assign stopRecconnectIf property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next stopRecconnectIf(Predicate<JSON> value) {
            ((EfficientWebSocket) this).setStopRecconnectIf(value);
            return (Next) this;
        }

        /**
         * Assign pongIf property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next pongIf(Function<JSON, String> value) {
            ((EfficientWebSocket) this).setPongIf(value);
            return (Next) this;
        }

        /**
         * Assign whenConnected property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next whenConnected(Consumer<WebSocket> value) {
            ((EfficientWebSocket) this).setWhenConnected(value);
            return (Next) this;
        }

        /**
         * Assign client property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next client(HttpClient value) {
            ((EfficientWebSocket) this).setClient(value);
            return (Next) this;
        }

        /**
         * Assign scheduler property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next scheduler(ScheduledExecutorService value) {
            ((EfficientWebSocket) this).setScheduler(value);
            return (Next) this;
        }
    }

    /**
     * Internal aggregated API.
     */
    protected static interface ÅssignableAll extends ÅssignableAddress, ÅssignableExtractId {
    }

    /**
     * Mutable Model.
     */
    private static final class Åssignable extends EfficientWebSocket implements ÅssignableAll, ÅssignableÅrbitrary {
    }

    /**
     * The identifier for properties.
     */
    static final class My {
        static final String Address = "address";
        static final String ExtractId = "extractId";
        static final String UpdateId = "updateId";
        static final String MaximumSubscriptions = "maximumSubscriptions";
        static final String IgnoreMessageIf = "ignoreMessageIf";
        static final String RecconnectIf = "recconnectIf";
        static final String StopRecconnectIf = "stopRecconnectIf";
        static final String PongIf = "pongIf";
        static final String WhenConnected = "whenConnected";
        static final String Client = "client";
        static final String Scheduler = "scheduler";
    }
}
