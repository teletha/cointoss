package cointoss.trade;

import cointoss.trade.ScenarioBase;
import cointoss.util.ObservableNumProperty;
import hypatia.Num;
import java.lang.Override;
import java.lang.StringBuilder;
import kiss.Signal;

/**
 * Generated model for {@link ScenarioBaseModel}.
 * 
 * @see <a href="https://github.com/teletha/icymanipulator">Icy Manipulator (Code Generator)</a>
 */
public class ScenarioBase extends ScenarioBaseModel {

    /** The property. */
    protected Num entrySize;

    /** The property customizer. */
    private final ObservableNumProperty entrySizeCustomizer = new ObservableNumProperty() {

        @Override
        public Num get() {
            return entrySize;
        }
    };

    /** The property. */
    protected Num entryExecutedSize;

    /** The property customizer. */
    private final ObservableNumProperty entryExecutedSizeCustomizer = new ObservableNumProperty() {

        @Override
        public Num get() {
            return entryExecutedSize;
        }
    };

    /** The property. */
    protected Num entryPrice;

    /** The property customizer. */
    private final ObservableNumProperty entryPriceCustomizer = new ObservableNumProperty() {

        @Override
        public Num get() {
            return entryPrice;
        }
    };

    /** The property. */
    protected Num entryCommission;

    /** The property. */
    protected Num exitSize;

    /** The property customizer. */
    private final ObservableNumProperty exitSizeCustomizer = new ObservableNumProperty() {

        @Override
        public Num get() {
            return exitSize;
        }
    };

    /** The property. */
    protected Num exitExecutedSize;

    /** The property customizer. */
    private final ObservableNumProperty exitExecutedSizeCustomizer = new ObservableNumProperty() {

        @Override
        public Num get() {
            return exitExecutedSize;
        }
    };

    /** The property. */
    protected Num exitPrice;

    /** The property customizer. */
    private final ObservableNumProperty exitPriceCustomizer = new ObservableNumProperty() {

        @Override
        public Num get() {
            return exitPrice;
        }
    };

    /** The property. */
    protected Num exitCommission;

    /** The property. */
    protected Num realizedProfit;

    /** The property customizer. */
    private final ObservableNumProperty realizedProfitCustomizer = new ObservableNumProperty() {

        @Override
        public Num get() {
            return realizedProfit;
        }
    };

    /**
     * HIDE CONSTRUCTOR
     */
    protected ScenarioBase() {
        this.entrySize = super.entrySize();
        this.entryExecutedSize = super.entryExecutedSize();
        this.entryPrice = super.entryPrice();
        this.entryCommission = super.entryCommission();
        this.exitSize = super.exitSize();
        this.exitExecutedSize = super.exitExecutedSize();
        this.exitPrice = super.exitPrice();
        this.exitCommission = super.exitCommission();
        this.realizedProfit = super.realizedProfit();
    }

    /**
     * A total size of entry orders.
     *  
     *  @return A total size of entry orders.
     */
    @Override
    public final Num entrySize() {
        return this.entrySize;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of entrySize property.
     */
    @SuppressWarnings("unused")
    private final Num getEntrySize() {
        return this.entrySize;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of entrySize property to assign.
     */
    final void setEntrySize(Num value) {
        if (value == null) {
            value = super.entrySize();
        }
        this.entrySize = value;
        entrySizeCustomizer.accept(this.entrySize);
    }

    /**
     * Observe property diff.
     *  
     *  @return
     */
    public final Signal<Num> observeEntrySizeDiff() {
        return entrySizeCustomizer.observe$Diff();
    }

    /**
     * Observe property modification.
     *  
     *  @return
     */
    public final Signal<Num> observeEntrySize() {
        return entrySizeCustomizer.observe$();
    }

    /**
     * Observe property modification with the current value.
     *  
     *  @return
     */
    public final Signal<Num> observeEntrySizeNow() {
        return entrySizeCustomizer.observe$Now();
    }

    /**
     * A total size of executed entry orders.
     *  
     *  @return A total size of executed entry orders.
     */
    @Override
    public final Num entryExecutedSize() {
        return this.entryExecutedSize;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of entryExecutedSize property.
     */
    @SuppressWarnings("unused")
    private final Num getEntryExecutedSize() {
        return this.entryExecutedSize;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of entryExecutedSize property to assign.
     */
    final void setEntryExecutedSize(Num value) {
        if (value == null) {
            value = super.entryExecutedSize();
        }
        this.entryExecutedSize = value;
        entryExecutedSizeCustomizer.accept(this.entryExecutedSize);
    }

    /**
     * Observe property diff.
     *  
     *  @return
     */
    public final Signal<Num> observeEntryExecutedSizeDiff() {
        return entryExecutedSizeCustomizer.observe$Diff();
    }

    /**
     * Observe property modification.
     *  
     *  @return
     */
    public final Signal<Num> observeEntryExecutedSize() {
        return entryExecutedSizeCustomizer.observe$();
    }

    /**
     * Observe property modification with the current value.
     *  
     *  @return
     */
    public final Signal<Num> observeEntryExecutedSizeNow() {
        return entryExecutedSizeCustomizer.observe$Now();
    }

    /**
     * An average price of executed entry orders.
     *  
     *  @return An average price of executed entry orders.
     */
    @Override
    public final Num entryPrice() {
        return this.entryPrice;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of entryPrice property.
     */
    @SuppressWarnings("unused")
    private final Num getEntryPrice() {
        return this.entryPrice;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of entryPrice property to assign.
     */
    final void setEntryPrice(Num value) {
        if (value == null) {
            value = super.entryPrice();
        }
        this.entryPrice = value;
        entryPriceCustomizer.accept(this.entryPrice);
    }

    /**
     * Observe property diff.
     *  
     *  @return
     */
    public final Signal<Num> observeEntryPriceDiff() {
        return entryPriceCustomizer.observe$Diff();
    }

    /**
     * Observe property modification.
     *  
     *  @return
     */
    public final Signal<Num> observeEntryPrice() {
        return entryPriceCustomizer.observe$();
    }

    /**
     * Observe property modification with the current value.
     *  
     *  @return
     */
    public final Signal<Num> observeEntryPriceNow() {
        return entryPriceCustomizer.observe$Now();
    }

    /**
     * The total of all entry orders.
     *  
     *  @return The total of all entry orders.
     */
    @Override
    public final Num entryCommission() {
        return this.entryCommission;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of entryCommission property.
     */
    @SuppressWarnings("unused")
    private final Num getEntryCommission() {
        return this.entryCommission;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of entryCommission property to assign.
     */
    final void setEntryCommission(Num value) {
        if (value == null) {
            value = super.entryCommission();
        }
        this.entryCommission = value;
    }

    /**
     * A total size of exit orders.
     *  
     *  @return A total size of exit orders.
     */
    @Override
    public final Num exitSize() {
        return this.exitSize;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of exitSize property.
     */
    @SuppressWarnings("unused")
    private final Num getExitSize() {
        return this.exitSize;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of exitSize property to assign.
     */
    final void setExitSize(Num value) {
        if (value == null) {
            value = super.exitSize();
        }
        this.exitSize = value;
        exitSizeCustomizer.accept(this.exitSize);
    }

    /**
     * Observe property diff.
     *  
     *  @return
     */
    public final Signal<Num> observeExitSizeDiff() {
        return exitSizeCustomizer.observe$Diff();
    }

    /**
     * Observe property modification.
     *  
     *  @return
     */
    public final Signal<Num> observeExitSize() {
        return exitSizeCustomizer.observe$();
    }

    /**
     * Observe property modification with the current value.
     *  
     *  @return
     */
    public final Signal<Num> observeExitSizeNow() {
        return exitSizeCustomizer.observe$Now();
    }

    /**
     * A total size of executed exit orders.
     *  
     *  @return A total size of executed exit orders.
     */
    @Override
    public final Num exitExecutedSize() {
        return this.exitExecutedSize;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of exitExecutedSize property.
     */
    @SuppressWarnings("unused")
    private final Num getExitExecutedSize() {
        return this.exitExecutedSize;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of exitExecutedSize property to assign.
     */
    final void setExitExecutedSize(Num value) {
        if (value == null) {
            value = super.exitExecutedSize();
        }
        this.exitExecutedSize = value;
        exitExecutedSizeCustomizer.accept(this.exitExecutedSize);
    }

    /**
     * Observe property diff.
     *  
     *  @return
     */
    public final Signal<Num> observeExitExecutedSizeDiff() {
        return exitExecutedSizeCustomizer.observe$Diff();
    }

    /**
     * Observe property modification.
     *  
     *  @return
     */
    public final Signal<Num> observeExitExecutedSize() {
        return exitExecutedSizeCustomizer.observe$();
    }

    /**
     * Observe property modification with the current value.
     *  
     *  @return
     */
    public final Signal<Num> observeExitExecutedSizeNow() {
        return exitExecutedSizeCustomizer.observe$Now();
    }

    /**
     * An average price of executed exit orders.
     *  
     *  @return An average price of executed exit orders.
     */
    @Override
    public final Num exitPrice() {
        return this.exitPrice;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of exitPrice property.
     */
    @SuppressWarnings("unused")
    private final Num getExitPrice() {
        return this.exitPrice;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of exitPrice property to assign.
     */
    final void setExitPrice(Num value) {
        if (value == null) {
            value = super.exitPrice();
        }
        this.exitPrice = value;
        exitPriceCustomizer.accept(this.exitPrice);
    }

    /**
     * Observe property diff.
     *  
     *  @return
     */
    public final Signal<Num> observeExitPriceDiff() {
        return exitPriceCustomizer.observe$Diff();
    }

    /**
     * Observe property modification.
     *  
     *  @return
     */
    public final Signal<Num> observeExitPrice() {
        return exitPriceCustomizer.observe$();
    }

    /**
     * Observe property modification with the current value.
     *  
     *  @return
     */
    public final Signal<Num> observeExitPriceNow() {
        return exitPriceCustomizer.observe$Now();
    }

    /**
     * The total of all exit orders.
     *  
     *  @return The total of all exit orders.
     */
    @Override
    public final Num exitCommission() {
        return this.exitCommission;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of exitCommission property.
     */
    @SuppressWarnings("unused")
    private final Num getExitCommission() {
        return this.exitCommission;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of exitCommission property to assign.
     */
    final void setExitCommission(Num value) {
        if (value == null) {
            value = super.exitCommission();
        }
        this.exitCommission = value;
    }

    /** {@inheritDoc} */
    @Override
    public final Num realizedProfit() {
        return this.realizedProfit;
    }

    /**
     * Provide classic getter API.
     *
     * @return A value of realizedProfit property.
     */
    @SuppressWarnings("unused")
    private final Num getRealizedProfit() {
        return this.realizedProfit;
    }

    /**
     * Provide classic setter API.
     *
     * @paran value A new value of realizedProfit property to assign.
     */
    final void setRealizedProfit(Num value) {
        if (value == null) {
            value = super.realizedProfit();
        }
        this.realizedProfit = value;
        realizedProfitCustomizer.accept(this.realizedProfit);
    }

    /**
     * Observe property diff.
     *  
     *  @return
     */
    public final Signal<Num> observeRealizedProfitDiff() {
        return realizedProfitCustomizer.observe$Diff();
    }

    /**
     * Observe property modification.
     *  
     *  @return
     */
    public final Signal<Num> observeRealizedProfit() {
        return realizedProfitCustomizer.observe$();
    }

    /**
     * Observe property modification with the current value.
     *  
     *  @return
     */
    public final Signal<Num> observeRealizedProfitNow() {
        return realizedProfitCustomizer.observe$Now();
    }

    /**
     * Show all property values.
     *
     * @return All property values.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("ScenarioBase [");
        builder.append("entrySize=").append(entrySize).append(", ");
        builder.append("entryExecutedSize=").append(entryExecutedSize).append(", ");
        builder.append("entryPrice=").append(entryPrice).append(", ");
        builder.append("entryCommission=").append(entryCommission).append(", ");
        builder.append("exitSize=").append(exitSize).append(", ");
        builder.append("exitExecutedSize=").append(exitExecutedSize).append(", ");
        builder.append("exitPrice=").append(exitPrice).append(", ");
        builder.append("exitCommission=").append(exitCommission).append(", ");
        builder.append("realizedProfit=").append(realizedProfit).append("]");
        return builder.toString();
    }

    /** The singleton builder. */
    public static final  Ìnstantiator<?> with = new Ìnstantiator();

    /**
     * Namespace for {@link ScenarioBase}  builder methods.
     */
    public static class Ìnstantiator<Self extends ScenarioBase & ÅssignableÅrbitrary<Self>> {

        /**
         * Create initialized {@link ScenarioBase}.
         *
         * @return A initialized model.
         */
        public Self create() {
            return (Self) new Åssignable();
        }

        /**
         * Create initialized {@link ScenarioBase} with entrySize property.
         *
         * @param value A value to assign.
         * @return A initialized model.
         */
        public Self entrySize(Num value) {
            return create().entrySize(value);
        }

        /**
         * Create initialized {@link ScenarioBase} with entryExecutedSize property.
         *
         * @param value A value to assign.
         * @return A initialized model.
         */
        public Self entryExecutedSize(Num value) {
            return create().entryExecutedSize(value);
        }

        /**
         * Create initialized {@link ScenarioBase} with entryPrice property.
         *
         * @param value A value to assign.
         * @return A initialized model.
         */
        public Self entryPrice(Num value) {
            return create().entryPrice(value);
        }

        /**
         * Create initialized {@link ScenarioBase} with entryCommission property.
         *
         * @param value A value to assign.
         * @return A initialized model.
         */
        public Self entryCommission(Num value) {
            return create().entryCommission(value);
        }

        /**
         * Create initialized {@link ScenarioBase} with exitSize property.
         *
         * @param value A value to assign.
         * @return A initialized model.
         */
        public Self exitSize(Num value) {
            return create().exitSize(value);
        }

        /**
         * Create initialized {@link ScenarioBase} with exitExecutedSize property.
         *
         * @param value A value to assign.
         * @return A initialized model.
         */
        public Self exitExecutedSize(Num value) {
            return create().exitExecutedSize(value);
        }

        /**
         * Create initialized {@link ScenarioBase} with exitPrice property.
         *
         * @param value A value to assign.
         * @return A initialized model.
         */
        public Self exitPrice(Num value) {
            return create().exitPrice(value);
        }

        /**
         * Create initialized {@link ScenarioBase} with exitCommission property.
         *
         * @param value A value to assign.
         * @return A initialized model.
         */
        public Self exitCommission(Num value) {
            return create().exitCommission(value);
        }

        /**
         * Create initialized {@link ScenarioBase} with realizedProfit property.
         *
         * @param value A value to assign.
         * @return A initialized model.
         */
        public Self realizedProfit(Num value) {
            return create().realizedProfit(value);
        }
    }

    /**
     * Property assignment API.
     */
    public static interface ÅssignableÅrbitrary<Next extends ScenarioBase> {

        /**
         * Assign entrySize property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next entrySize(Num value) {
            ((ScenarioBase) this).setEntrySize(value);
            return (Next) this;
        }

        /**
         * Assign entryExecutedSize property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next entryExecutedSize(Num value) {
            ((ScenarioBase) this).setEntryExecutedSize(value);
            return (Next) this;
        }

        /**
         * Assign entryPrice property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next entryPrice(Num value) {
            ((ScenarioBase) this).setEntryPrice(value);
            return (Next) this;
        }

        /**
         * Assign entryCommission property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next entryCommission(Num value) {
            ((ScenarioBase) this).setEntryCommission(value);
            return (Next) this;
        }

        /**
         * Assign exitSize property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next exitSize(Num value) {
            ((ScenarioBase) this).setExitSize(value);
            return (Next) this;
        }

        /**
         * Assign exitExecutedSize property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next exitExecutedSize(Num value) {
            ((ScenarioBase) this).setExitExecutedSize(value);
            return (Next) this;
        }

        /**
         * Assign exitPrice property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next exitPrice(Num value) {
            ((ScenarioBase) this).setExitPrice(value);
            return (Next) this;
        }

        /**
         * Assign exitCommission property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next exitCommission(Num value) {
            ((ScenarioBase) this).setExitCommission(value);
            return (Next) this;
        }

        /**
         * Assign realizedProfit property.
         * 
         * @param value A new value to assign.
         * @return The next assignable model.
         */
        default Next realizedProfit(Num value) {
            ((ScenarioBase) this).setRealizedProfit(value);
            return (Next) this;
        }
    }

    /**
     * Internal aggregated API.
     */
    protected static interface ÅssignableAll {
    }

    /**
     * Mutable Model.
     */
    private static final class Åssignable extends ScenarioBase implements ÅssignableAll, ÅssignableÅrbitrary {
    }

    /**
     * The identifier for properties.
     */
    static final class My {
        static final String EntrySize = "entrySize";
        static final String EntryExecutedSize = "entryExecutedSize";
        static final String EntryPrice = "entryPrice";
        static final String EntryCommission = "entryCommission";
        static final String ExitSize = "exitSize";
        static final String ExitExecutedSize = "exitExecutedSize";
        static final String ExitPrice = "exitPrice";
        static final String ExitCommission = "exitCommission";
        static final String RealizedProfit = "realizedProfit";
    }
}
