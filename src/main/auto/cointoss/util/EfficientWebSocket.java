package cointoss.util;

import cointoss.util.EfficientWebSocket;
import cointoss.util.EfficientWebSocketModel;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.Throwable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.net.http.HttpClient;
import java.util.Objects;
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
    private static final MethodHandle maximumSubscriptionsUpdater = updater("maximumSubscriptions");

    /** The final property updater. */
    private static final MethodHandle ignoreMessageIfUpdater = updater("ignoreMessageIf");

    /** The final property updater. */
    private static final MethodHandle enableDebugUpdater = updater("enableDebug");

    /** The final property updater. */
    private static final MethodHandle clientUpdater = updater("client");

    /** The exposed property. */
    public final String address;

    /** The exposed property. */
    public final Function<JSON, String> extractId;

    /** The exposed property. */
    public final int maximumSubscriptions;

    /** The exposed property. */
    public final Predicate<JSON> ignoreMessageIf;

    /** The exposed property. */
    public final boolean enableDebug;

    /** The exposed property. */
    public final HttpClient client;

    /**
     * HIDE CONSTRUCTOR
     */
    protected EfficientWebSocket() {
        this.address = null;
        this.extractId = null;
        this.maximumSubscriptions = super.maximumSubscriptions();
        this.ignoreMessageIf = super.ignoreMessageIf();
        this.enableDebug = super.enableDebug();
        this.client = super.client();
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
    public final boolean enableDebug() {
        return this.enableDebug;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of enableDebug property.
     */
    @SuppressWarnings("unused")
    private final boolean getEnableDebug() {
        return this.enableDebug;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of enableDebug property to assign.
     */
    private final void setEnableDebug(boolean value) {
        try {
            enableDebugUpdater.invoke(this, value);
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
        builder.append("maximumSubscriptions=").append(maximumSubscriptions).append(", ");
        builder.append("ignoreMessageIf=").append(ignoreMessageIf).append(", ");
        builder.append("enableDebug=").append(enableDebug).append(", ");
        builder.append("client=").append(client).append("]");
        return builder.toString();
    }

    /**
     * Generates a hash code for a sequence of property values. The hash code is generated as if all the property values were placed into an array, and that array were hashed by calling Arrays.hashCode(Object[]). 
     *
     * @return A hash value of the sequence of property values.
     */
    @Override
    public int hashCode() {
        return Objects.hash(address, extractId, maximumSubscriptions, ignoreMessageIf, enableDebug, client);
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
        if (maximumSubscriptions != other.maximumSubscriptions) return false;
        if (!Objects.equals(ignoreMessageIf, other.ignoreMessageIf)) return false;
        if (enableDebug != other.enableDebug) return false;
        if (!Objects.equals(client, other.client)) return false;
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
        return with.address(value).extractId(this.extractId).maximumSubscriptions(this.maximumSubscriptions).ignoreMessageIf(this.ignoreMessageIf).enableDebug(this.enableDebug).client(this.client);
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
        return with.address(this.address).extractId(this.extractId).maximumSubscriptions(this.maximumSubscriptions).ignoreMessageIf(this.ignoreMessageIf).enableDebug(this.enableDebug).client(value);
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
         * Assign enableDebug property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next enableDebug(boolean value) {
            ((EfficientWebSocket) this).setEnableDebug(value);
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
        static final String MaximumSubscriptions = "maximumSubscriptions";
        static final String IgnoreMessageIf = "ignoreMessageIf";
        static final String EnableDebug = "enableDebug";
        static final String Client = "client";
    }
}
