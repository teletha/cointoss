package cointoss.execution;

import cointoss.Direction;
import cointoss.util.Num;
import icy.manipulator.Accessor;
import icy.manipulator.Manipulatable;
import java.time.ZonedDateTime;

/**
 * {@link Manipulatable} model for {@link ExecutionModel}.
 */
public abstract class Execution extends ExecutionModel implements Manipulatable<Execution> {

     /** The final property updater. */
     private static final java.lang.invoke.MethodHandle dateUpdater = icy.manipulator.Manipulator.updater(ExecutionModel.class, "date");

     /** The final property updater. */
     private static final java.lang.invoke.MethodHandle millsUpdater = icy.manipulator.Manipulator.updater(ExecutionModel.class, "mills");

     /** The model manipulator for reuse. */
     private static final Manipulator MANIPULATOR = new Manipulator(null);

     /**
      * HIDE CONSTRUCTOR
      */
     protected Execution() {
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
     public Execution id(long value) {
         this.id = value;

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
     public Execution side(Direction value) {
         this.side = value;

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
     public Execution price(Num value) {
         this.price = value;

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
     public Execution size(Num value) {
         this.size = value;

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
     public Execution cumulativeSize(Num value) {
         this.cumulativeSize = value;

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
     public Execution date(ZonedDateTime value) {
         try {
             dateUpdater.invoke(this, value);
             super.mills(this);
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
     protected Execution mills(long value) {
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
     public Execution consecutive(int value) {
         this.consecutive = value;

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
     public Execution delay(int value) {
         this.delay = value;

         return this;
     }

     /**
      * Create model builder without base model.
      */
     public static final Execution with() {
         return new Melty(null);
     }

     /**
      * Create model builder using the specified definition as base model.
      */
     public static final Execution with(Execution base) {
         return new Melty(base);
     }

     /**
      * Create model manipulator.
      */
     public static final Manipulator<Execution>manipulate() {
         return MANIPULATOR;
     }

     /**
      * Immutable Model.
      */
     private static final class Icy extends Execution {

         /**
          * HIDE CONSTRUCTOR
          */
         private Icy(long id, Direction side, Num price, Num size, Num cumulativeSize, ZonedDateTime date, long mills, int consecutive, int delay) {
                 this.id = id;
                 this.side = side;
                 this.price = price;
                 this.size = size;
                 this.cumulativeSize = cumulativeSize;
                 super.date(date);
                 super.mills(mills);
                 this.consecutive = consecutive;
                 this.delay = delay;
         }

         /**
          * {@inheritDoc}
          */
         @Override
         public Execution melt() {
             return new Melty(this);
         }

         /**
          * {@inheritDoc}
          */
         @Override
         public Execution id(long value) {
             if (this.id == value) {
                 return this;
             }
             return new Icy(value, this.side, this.price, this.size, this.cumulativeSize, this.date, this.mills, this.consecutive, this.delay);
         }

         /**
          * {@inheritDoc}
          */
         @Override
         public Execution side(Direction value) {
             if (this.side == value) {
                 return this;
             }
             return new Icy(this.id, value, this.price, this.size, this.cumulativeSize, this.date, this.mills, this.consecutive, this.delay);
         }

         /**
          * {@inheritDoc}
          */
         @Override
         public Execution price(Num value) {
             if (this.price == value) {
                 return this;
             }
             return new Icy(this.id, this.side, value, this.size, this.cumulativeSize, this.date, this.mills, this.consecutive, this.delay);
         }

         /**
          * {@inheritDoc}
          */
         @Override
         public Execution size(Num value) {
             if (this.size == value) {
                 return this;
             }
             return new Icy(this.id, this.side, this.price, value, this.cumulativeSize, this.date, this.mills, this.consecutive, this.delay);
         }

         /**
          * {@inheritDoc}
          */
         @Override
         public Execution cumulativeSize(Num value) {
             if (this.cumulativeSize == value) {
                 return this;
             }
             return new Icy(this.id, this.side, this.price, this.size, value, this.date, this.mills, this.consecutive, this.delay);
         }

         /**
          * {@inheritDoc}
          */
         @Override
         public Execution date(ZonedDateTime value) {
             if (this.date == value) {
                 return this;
             }
             return new Icy(this.id, this.side, this.price, this.size, this.cumulativeSize, value, this.mills, this.consecutive, this.delay);
         }

         /**
          * {@inheritDoc}
          */
         @Override
         public Execution consecutive(int value) {
             if (this.consecutive == value) {
                 return this;
             }
             return new Icy(this.id, this.side, this.price, this.size, this.cumulativeSize, this.date, this.mills, value, this.delay);
         }

         /**
          * {@inheritDoc}
          */
         @Override
         public Execution delay(int value) {
             if (this.delay == value) {
                 return this;
             }
             return new Icy(this.id, this.side, this.price, this.size, this.cumulativeSize, this.date, this.mills, this.consecutive, value);
         }

     }
     /**
      * Mutable Model.
      */
     private static final class Melty extends Execution {

         /**
          * HIDE CONSTRUCTOR
          */
         private Melty(Execution base) {
             if (base != null) {
                 this.id = base.id;
                 this.side = base.side;
                 this.price = base.price;
                 this.size = base.size;
                 this.cumulativeSize = base.cumulativeSize;
                 super.date(base.date);
                 super.mills(base.mills);
                 this.consecutive = base.consecutive;
                 this.delay = base.delay;
             }
         }

         /**
          * {@inheritDoc}
          */
         @Override
         public Execution ice() {
             return new Icy(id, side, price, size, cumulativeSize, date, mills, consecutive, delay);
         }
     }
     /**
      * Model Manipulator.
      */
     public static final class Manipulator<RootModel> extends icy.manipulator.Manipulator<RootModel,Execution> {

         /** The accessor for id property. */
         private static final Accessor ID = Accessor.<Execution, Long> of(Execution::id,  Execution::id);

         /** The accessor for side property. */
         private static final Accessor SIDE = Accessor.<Execution, Direction> of(Execution::side,  Execution::side);

         /** The accessor for price property. */
         private static final Accessor PRICE = Accessor.<Execution, Num> of(Execution::price,  Execution::price);

         /** The accessor for size property. */
         private static final Accessor SIZE = Accessor.<Execution, Num> of(Execution::size,  Execution::size);

         /** The accessor for cumulativeSize property. */
         private static final Accessor CUMULATIVESIZE = Accessor.<Execution, Num> of(Execution::cumulativeSize,  Execution::cumulativeSize);

         /** The accessor for date property. */
         private static final Accessor DATE = Accessor.<Execution, ZonedDateTime> of(Execution::date,  Execution::date);

         /** The accessor for mills property. */
         private static final Accessor MILLS = Accessor.<Execution, Long> of(Execution::mills,  Execution::mills);

         /** The accessor for consecutive property. */
         private static final Accessor CONSECUTIVE = Accessor.<Execution, Integer> of(Execution::consecutive,  Execution::consecutive);

         /** The accessor for delay property. */
         private static final Accessor DELAY = Accessor.<Execution, Integer> of(Execution::delay,  Execution::delay);

         /**
          * Construct operator.
          */
         public Manipulator(Accessor<RootModel,Execution> parent) {
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
