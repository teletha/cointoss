package cointoss.execution;

import cointoss.Direction;
import cointoss.util.Num;
import icy.manipulator.Accessor;
import icy.manipulator.Manipulatable;
import java.time.ZonedDateTime;

/**
 * {@link Manipulatable} model for {@link ExecutionXModel}.
 */
public abstract class ExecutionX extends ExecutionXModel implements Manipulatable<ExecutionX> {

     /** The final property updater. */
     private static final java.lang.invoke.MethodHandle idUpdater = icy.manipulator.Manipulator.updater(ExecutionXModel.class, "id");

     /** The final property updater. */
     private static final java.lang.invoke.MethodHandle sideUpdater = icy.manipulator.Manipulator.updater(ExecutionXModel.class, "side");

     /** The final property updater. */
     private static final java.lang.invoke.MethodHandle priceUpdater = icy.manipulator.Manipulator.updater(ExecutionXModel.class, "price");

     /** The final property updater. */
     private static final java.lang.invoke.MethodHandle sizeUpdater = icy.manipulator.Manipulator.updater(ExecutionXModel.class, "size");

     /** The final property updater. */
     private static final java.lang.invoke.MethodHandle cumulativeSizeUpdater = icy.manipulator.Manipulator.updater(ExecutionXModel.class, "cumulativeSize");

     /** The final property updater. */
     private static final java.lang.invoke.MethodHandle dateUpdater = icy.manipulator.Manipulator.updater(ExecutionXModel.class, "date");

     /** The final property updater. */
     private static final java.lang.invoke.MethodHandle millsUpdater = icy.manipulator.Manipulator.updater(ExecutionXModel.class, "mills");

     /** The final property updater. */
     private static final java.lang.invoke.MethodHandle consecutiveUpdater = icy.manipulator.Manipulator.updater(ExecutionXModel.class, "consecutive");

     /** The final property updater. */
     private static final java.lang.invoke.MethodHandle delayUpdater = icy.manipulator.Manipulator.updater(ExecutionXModel.class, "delay");

     /** The model manipulator for reuse. */
     private static final Manipulator MANIPULATOR = new Manipulator(null);

     /**
      * HIDE CONSTRUCTOR
      */
     protected ExecutionX() {
     }

     /**
     * Retrieve id property.
     */
     public long id() {
         return this.id;
     }

     /**
     * Modify id property.
     */
     public ExecutionX id(long value) {
         try {
             idUpdater.invoke(this, value);
         } catch (Throwable e) {
             throw new Error(e);
         }

         return this;
     }

     /**
     * Retrieve side property.
     */
     public Direction side() {
         return this.side;
     }

     /**
     * Modify side property.
     */
     public ExecutionX side(Direction value) {
         try {
             sideUpdater.invoke(this, value);
         } catch (Throwable e) {
             throw new Error(e);
         }

         return this;
     }

     /**
     * Retrieve price property.
     */
     public Num price() {
         return this.price;
     }

     /**
     * Modify price property.
     */
     public ExecutionX price(Num value) {
         try {
             priceUpdater.invoke(this, value);
         } catch (Throwable e) {
             throw new Error(e);
         }

         return this;
     }

     /**
     * Retrieve size property.
     */
     public Num size() {
         return this.size;
     }

     /**
     * Modify size property.
     */
     public ExecutionX size(Num value) {
         try {
             sizeUpdater.invoke(this, value);
         } catch (Throwable e) {
             throw new Error(e);
         }

         return this;
     }

     /**
     * Retrieve cumulativeSize property.
     */
     public Num cumulativeSize() {
         return this.cumulativeSize;
     }

     /**
     * Modify cumulativeSize property.
     */
     public ExecutionX cumulativeSize(Num value) {
         try {
             cumulativeSizeUpdater.invoke(this, value);
         } catch (Throwable e) {
             throw new Error(e);
         }

         return this;
     }

     /**
     * Retrieve date property.
     */
     public ZonedDateTime date() {
         return this.date;
     }

     /**
     * Modify date property.
     */
     public ExecutionX date(ZonedDateTime value) {
         try {
             dateUpdater.invoke(this, value);
         } catch (Throwable e) {
             throw new Error(e);
         }

         return this;
     }

     /**
     * Retrieve mills property.
     */
     public long mills() {
         return this.mills;
     }

     /**
     * Modify mills property.
     */
     public ExecutionX mills(long value) {
         try {
             millsUpdater.invoke(this, value);
         } catch (Throwable e) {
             throw new Error(e);
         }

         return this;
     }

     /**
     * Retrieve consecutive property.
     */
     public int consecutive() {
         return this.consecutive;
     }

     /**
     * Modify consecutive property.
     */
     public ExecutionX consecutive(int value) {
         try {
             consecutiveUpdater.invoke(this, value);
         } catch (Throwable e) {
             throw new Error(e);
         }

         return this;
     }

     /**
     * Retrieve delay property.
     */
     public int delay() {
         return this.delay;
     }

     /**
     * Modify delay property.
     */
     public ExecutionX delay(int value) {
         try {
             delayUpdater.invoke(this, value);
         } catch (Throwable e) {
             throw new Error(e);
         }

         return this;
     }

     /**
      * Create model builder without base model.
      */
     public static final ExecutionX with() {
         return new Melty(null);
     }

     /**
      * Create model builder using the specified definition as base model.
      */
     public static final ExecutionX with(ExecutionX base) {
         return new Melty(base);
     }

     /**
      * Create model manipulator.
      */
     public static final Manipulator<ExecutionX>manipulate() {
         return MANIPULATOR;
     }

     /**
      * Immutable Model.
      */
     private static final class Icy extends ExecutionX {

         /**
          * HIDE CONSTRUCTOR
          */
         private Icy(long id, Direction side, Num price, Num size, Num cumulativeSize, ZonedDateTime date, long mills, int consecutive, int delay) {
                 super.id(id);
                 super.side(side);
                 super.price(price);
                 super.size(size);
                 super.cumulativeSize(cumulativeSize);
                 super.date(date);
                 super.mills(mills);
                 super.consecutive(consecutive);
                 super.delay(delay);
         }

         /**
          * {@inheritDoc}
          */
         @Override
         public ExecutionX melt() {
             return new Melty(this);
         }

         /**
          * {@inheritDoc}
          */
         @Override
         public ExecutionX id(long value) {
             if (this.id == value) {
                 return this;
             }
             return new Icy(value, this.side, this.price, this.size, this.cumulativeSize, this.date, this.mills, this.consecutive, this.delay);
         }

         /**
          * {@inheritDoc}
          */
         @Override
         public ExecutionX side(Direction value) {
             if (this.side == value) {
                 return this;
             }
             return new Icy(this.id, value, this.price, this.size, this.cumulativeSize, this.date, this.mills, this.consecutive, this.delay);
         }

         /**
          * {@inheritDoc}
          */
         @Override
         public ExecutionX price(Num value) {
             if (this.price == value) {
                 return this;
             }
             return new Icy(this.id, this.side, value, this.size, this.cumulativeSize, this.date, this.mills, this.consecutive, this.delay);
         }

         /**
          * {@inheritDoc}
          */
         @Override
         public ExecutionX size(Num value) {
             if (this.size == value) {
                 return this;
             }
             return new Icy(this.id, this.side, this.price, value, this.cumulativeSize, this.date, this.mills, this.consecutive, this.delay);
         }

         /**
          * {@inheritDoc}
          */
         @Override
         public ExecutionX cumulativeSize(Num value) {
             if (this.cumulativeSize == value) {
                 return this;
             }
             return new Icy(this.id, this.side, this.price, this.size, value, this.date, this.mills, this.consecutive, this.delay);
         }

         /**
          * {@inheritDoc}
          */
         @Override
         public ExecutionX date(ZonedDateTime value) {
             if (this.date == value) {
                 return this;
             }
             return new Icy(this.id, this.side, this.price, this.size, this.cumulativeSize, value, this.mills, this.consecutive, this.delay);
         }

         /**
          * {@inheritDoc}
          */
         @Override
         public ExecutionX mills(long value) {
             if (this.mills == value) {
                 return this;
             }
             return new Icy(this.id, this.side, this.price, this.size, this.cumulativeSize, this.date, value, this.consecutive, this.delay);
         }

         /**
          * {@inheritDoc}
          */
         @Override
         public ExecutionX consecutive(int value) {
             if (this.consecutive == value) {
                 return this;
             }
             return new Icy(this.id, this.side, this.price, this.size, this.cumulativeSize, this.date, this.mills, value, this.delay);
         }

         /**
          * {@inheritDoc}
          */
         @Override
         public ExecutionX delay(int value) {
             if (this.delay == value) {
                 return this;
             }
             return new Icy(this.id, this.side, this.price, this.size, this.cumulativeSize, this.date, this.mills, this.consecutive, value);
         }

     }
     /**
      * Mutable Model.
      */
     private static final class Melty extends ExecutionX {

         /**
          * HIDE CONSTRUCTOR
          */
         private Melty(ExecutionX base) {
             if (base != null) {
                 super.id(base.id);
                 super.side(base.side);
                 super.price(base.price);
                 super.size(base.size);
                 super.cumulativeSize(base.cumulativeSize);
                 super.date(base.date);
                 super.mills(base.mills);
                 super.consecutive(base.consecutive);
                 super.delay(base.delay);
             }
         }

         /**
          * {@inheritDoc}
          */
         @Override
         public ExecutionX ice() {
             return new Icy(id, side, price, size, cumulativeSize, date, mills, consecutive, delay);
         }
     }
     /**
      * Model Manipulator.
      */
     public static final class Manipulator<RootModel> extends icy.manipulator.Manipulator<RootModel,ExecutionX> {

         /** The accessor for id property. */
         private static final Accessor ID = Accessor.<ExecutionX, Long> of(ExecutionX::id,  ExecutionX::id);

         /** The accessor for side property. */
         private static final Accessor SIDE = Accessor.<ExecutionX, Direction> of(ExecutionX::side,  ExecutionX::side);

         /** The accessor for price property. */
         private static final Accessor PRICE = Accessor.<ExecutionX, Num> of(ExecutionX::price,  ExecutionX::price);

         /** The accessor for size property. */
         private static final Accessor SIZE = Accessor.<ExecutionX, Num> of(ExecutionX::size,  ExecutionX::size);

         /** The accessor for cumulativeSize property. */
         private static final Accessor CUMULATIVESIZE = Accessor.<ExecutionX, Num> of(ExecutionX::cumulativeSize,  ExecutionX::cumulativeSize);

         /** The accessor for date property. */
         private static final Accessor DATE = Accessor.<ExecutionX, ZonedDateTime> of(ExecutionX::date,  ExecutionX::date);

         /** The accessor for mills property. */
         private static final Accessor MILLS = Accessor.<ExecutionX, Long> of(ExecutionX::mills,  ExecutionX::mills);

         /** The accessor for consecutive property. */
         private static final Accessor CONSECUTIVE = Accessor.<ExecutionX, Integer> of(ExecutionX::consecutive,  ExecutionX::consecutive);

         /** The accessor for delay property. */
         private static final Accessor DELAY = Accessor.<ExecutionX, Integer> of(ExecutionX::delay,  ExecutionX::delay);

         /**
          * Construct operator.
          */
         public Manipulator(Accessor<RootModel,ExecutionX> parent) {
             super(parent);
         }

         /**
          * Property operator.
          */
         public Accessor<RootModel,Long> id() {
             return parent.then(ID);
         }

         /**
          * Property operator.
          */
         public Accessor<RootModel,Direction> side() {
             return parent.then(SIDE);
         }

         /**
          * Property operator.
          */
         public Accessor<RootModel,Num> price() {
             return parent.then(PRICE);
         }

         /**
          * Property operator.
          */
         public Accessor<RootModel,Num> size() {
             return parent.then(SIZE);
         }

         /**
          * Property operator.
          */
         public Accessor<RootModel,Num> cumulativeSize() {
             return parent.then(CUMULATIVESIZE);
         }

         /**
          * Property operator.
          */
         public Accessor<RootModel,ZonedDateTime> date() {
             return parent.then(DATE);
         }

         /**
          * Property operator.
          */
         public Accessor<RootModel,Long> mills() {
             return parent.then(MILLS);
         }

         /**
          * Property operator.
          */
         public Accessor<RootModel,Integer> consecutive() {
             return parent.then(CONSECUTIVE);
         }

         /**
          * Property operator.
          */
         public Accessor<RootModel,Integer> delay() {
             return parent.then(DELAY);
         }

     }
}
