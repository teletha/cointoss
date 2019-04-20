package cointoss;

import cointoss.execution.ExecutionLogger;
import cointoss.util.Num;
import cointoss.util.RetryPolicy;
import com.google.common.base.MoreObjects;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Var;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import org.immutables.value.Generated;

/**
 * @version 2018/08/22 19:55:10
 */
@Generated(from = "MarketSettingData", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class MarketSetting implements MarketSettingData {
  private final Num baseCurrencyMinimumBidPrice;
  private final Num targetCurrencyMinimumBidSize;
  private final Num[] orderBookGroupRanges;
  private final int targetCurrencyScaleSize;
  private final int acquirableExecutionSize;
  private final Class<? extends ExecutionLogger> executionLogger;
  private final RetryPolicy retryPolicy;

  private MarketSetting(MarketSetting.Builder builder) {
    this.baseCurrencyMinimumBidPrice = builder.baseCurrencyMinimumBidPrice;
    this.targetCurrencyMinimumBidSize = builder.targetCurrencyMinimumBidSize;
    this.orderBookGroupRanges = builder.orderBookGroupRanges;
    if (builder.targetCurrencyScaleSizeIsSet()) {
      initShim.targetCurrencyScaleSize(builder.targetCurrencyScaleSize);
    }
    if (builder.acquirableExecutionSizeIsSet()) {
      initShim.acquirableExecutionSize(builder.acquirableExecutionSize);
    }
    if (builder.executionLogger != null) {
      initShim.executionLogger(builder.executionLogger);
    }
    if (builder.retryPolicy != null) {
      initShim.retryPolicy(builder.retryPolicy);
    }
    this.targetCurrencyScaleSize = initShim.targetCurrencyScaleSize();
    this.acquirableExecutionSize = initShim.acquirableExecutionSize();
    this.executionLogger = initShim.executionLogger();
    this.retryPolicy = initShim.retryPolicy();
    this.initShim = null;
  }

  private MarketSetting(
      Num baseCurrencyMinimumBidPrice,
      Num targetCurrencyMinimumBidSize,
      Num[] orderBookGroupRanges,
      int targetCurrencyScaleSize,
      int acquirableExecutionSize,
      Class<? extends ExecutionLogger> executionLogger,
      RetryPolicy retryPolicy) {
    this.baseCurrencyMinimumBidPrice = baseCurrencyMinimumBidPrice;
    this.targetCurrencyMinimumBidSize = targetCurrencyMinimumBidSize;
    this.orderBookGroupRanges = orderBookGroupRanges;
    this.targetCurrencyScaleSize = targetCurrencyScaleSize;
    this.acquirableExecutionSize = acquirableExecutionSize;
    this.executionLogger = executionLogger;
    this.retryPolicy = retryPolicy;
    this.initShim = null;
  }

  private static final byte STAGE_INITIALIZING = -1;
  private static final byte STAGE_UNINITIALIZED = 0;
  private static final byte STAGE_INITIALIZED = 1;
  @SuppressWarnings("Immutable")
  private transient volatile InitShim initShim = new InitShim();

  @Generated(from = "MarketSettingData", generator = "Immutables")
  private final class InitShim {
    private byte targetCurrencyScaleSizeBuildStage = STAGE_UNINITIALIZED;
    private int targetCurrencyScaleSize;

    int targetCurrencyScaleSize() {
      if (targetCurrencyScaleSizeBuildStage == STAGE_INITIALIZING) throw new IllegalStateException(formatInitCycleMessage());
      if (targetCurrencyScaleSizeBuildStage == STAGE_UNINITIALIZED) {
        targetCurrencyScaleSizeBuildStage = STAGE_INITIALIZING;
        this.targetCurrencyScaleSize = targetCurrencyScaleSizeInitialize();
        targetCurrencyScaleSizeBuildStage = STAGE_INITIALIZED;
      }
      return this.targetCurrencyScaleSize;
    }

    void targetCurrencyScaleSize(int targetCurrencyScaleSize) {
      this.targetCurrencyScaleSize = targetCurrencyScaleSize;
      targetCurrencyScaleSizeBuildStage = STAGE_INITIALIZED;
    }

    private byte acquirableExecutionSizeBuildStage = STAGE_UNINITIALIZED;
    private int acquirableExecutionSize;

    int acquirableExecutionSize() {
      if (acquirableExecutionSizeBuildStage == STAGE_INITIALIZING) throw new IllegalStateException(formatInitCycleMessage());
      if (acquirableExecutionSizeBuildStage == STAGE_UNINITIALIZED) {
        acquirableExecutionSizeBuildStage = STAGE_INITIALIZING;
        this.acquirableExecutionSize = acquirableExecutionSizeInitialize();
        acquirableExecutionSizeBuildStage = STAGE_INITIALIZED;
      }
      return this.acquirableExecutionSize;
    }

    void acquirableExecutionSize(int acquirableExecutionSize) {
      this.acquirableExecutionSize = acquirableExecutionSize;
      acquirableExecutionSizeBuildStage = STAGE_INITIALIZED;
    }

    private byte executionLoggerBuildStage = STAGE_UNINITIALIZED;
    private Class<? extends ExecutionLogger> executionLogger;

    Class<? extends ExecutionLogger> executionLogger() {
      if (executionLoggerBuildStage == STAGE_INITIALIZING) throw new IllegalStateException(formatInitCycleMessage());
      if (executionLoggerBuildStage == STAGE_UNINITIALIZED) {
        executionLoggerBuildStage = STAGE_INITIALIZING;
        this.executionLogger = Objects.requireNonNull(executionLoggerInitialize(), "executionLogger");
        executionLoggerBuildStage = STAGE_INITIALIZED;
      }
      return this.executionLogger;
    }

    void executionLogger(Class<? extends ExecutionLogger> executionLogger) {
      this.executionLogger = executionLogger;
      executionLoggerBuildStage = STAGE_INITIALIZED;
    }

    private byte retryPolicyBuildStage = STAGE_UNINITIALIZED;
    private RetryPolicy retryPolicy;

    RetryPolicy retryPolicy() {
      if (retryPolicyBuildStage == STAGE_INITIALIZING) throw new IllegalStateException(formatInitCycleMessage());
      if (retryPolicyBuildStage == STAGE_UNINITIALIZED) {
        retryPolicyBuildStage = STAGE_INITIALIZING;
        this.retryPolicy = Objects.requireNonNull(retryPolicyInitialize(), "retryPolicy");
        retryPolicyBuildStage = STAGE_INITIALIZED;
      }
      return this.retryPolicy;
    }

    void retryPolicy(RetryPolicy retryPolicy) {
      this.retryPolicy = retryPolicy;
      retryPolicyBuildStage = STAGE_INITIALIZED;
    }

    private String formatInitCycleMessage() {
      List<String> attributes = new ArrayList<>();
      if (targetCurrencyScaleSizeBuildStage == STAGE_INITIALIZING) attributes.add("targetCurrencyScaleSize");
      if (acquirableExecutionSizeBuildStage == STAGE_INITIALIZING) attributes.add("acquirableExecutionSize");
      if (executionLoggerBuildStage == STAGE_INITIALIZING) attributes.add("executionLogger");
      if (retryPolicyBuildStage == STAGE_INITIALIZING) attributes.add("retryPolicy");
      return "Cannot build MarketSetting, attribute initializers form cycle " + attributes;
    }
  }

  private int targetCurrencyScaleSizeInitialize() {
    return MarketSettingData.super.targetCurrencyScaleSize();
  }

  private int acquirableExecutionSizeInitialize() {
    return MarketSettingData.super.acquirableExecutionSize();
  }

  private Class<? extends ExecutionLogger> executionLoggerInitialize() {
    return MarketSettingData.super.executionLogger();
  }

  private RetryPolicy retryPolicyInitialize() {
    return MarketSettingData.super.retryPolicy();
  }

  /**
   * Get the minimum bid price of the base currency.
   */
  @Override
  public Num baseCurrencyMinimumBidPrice() {
    return baseCurrencyMinimumBidPrice;
  }

  /**
   * Get the minimum bid size of the target currency.
   */
  @Override
  public Num targetCurrencyMinimumBidSize() {
    return targetCurrencyMinimumBidSize;
  }

  /**
   * Get the price range of grouped order books.
   */
  @Override
  public Num[] orderBookGroupRanges() {
    return orderBookGroupRanges.clone();
  }

  /**
   * Get the human readable size of target currency.
   */
  @Override
  public int targetCurrencyScaleSize() {
    InitShim shim = this.initShim;
    return shim != null
        ? shim.targetCurrencyScaleSize()
        : this.targetCurrencyScaleSize;
  }

  /**
   * Configure max acquirable execution size per one request.
   * 
   * @return
   */
  @Override
  public int acquirableExecutionSize() {
    InitShim shim = this.initShim;
    return shim != null
        ? shim.acquirableExecutionSize()
        : this.acquirableExecutionSize;
  }

  /**
   * Configure {@link ExecutionLog} parser.
   * 
   * @return
   */
  @Override
  public Class<? extends ExecutionLogger> executionLogger() {
    InitShim shim = this.initShim;
    return shim != null
        ? shim.executionLogger()
        : this.executionLogger;
  }

  /**
   * Configure {@link RetryPolicy}.
   * 
   * @return
   */
  @Override
  public RetryPolicy retryPolicy() {
    InitShim shim = this.initShim;
    return shim != null
        ? shim.retryPolicy()
        : this.retryPolicy;
  }

  /**
   * Copy the current immutable object by setting a value for the {@link MarketSetting#baseCurrencyMinimumBidPrice() baseCurrencyMinimumBidPrice} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for baseCurrencyMinimumBidPrice
   * @return A modified copy of the {@code this} object
   */
  public final MarketSetting withBaseCurrencyMinimumBidPrice(Num value) {
    if (this.baseCurrencyMinimumBidPrice == value) return this;
    Num newValue = Objects.requireNonNull(value, "baseCurrencyMinimumBidPrice");
    return new MarketSetting(
        newValue,
        this.targetCurrencyMinimumBidSize,
        this.orderBookGroupRanges,
        this.targetCurrencyScaleSize,
        this.acquirableExecutionSize,
        this.executionLogger,
        this.retryPolicy);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link MarketSetting#targetCurrencyMinimumBidSize() targetCurrencyMinimumBidSize} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for targetCurrencyMinimumBidSize
   * @return A modified copy of the {@code this} object
   */
  public final MarketSetting withTargetCurrencyMinimumBidSize(Num value) {
    if (this.targetCurrencyMinimumBidSize == value) return this;
    Num newValue = Objects.requireNonNull(value, "targetCurrencyMinimumBidSize");
    return new MarketSetting(
        this.baseCurrencyMinimumBidPrice,
        newValue,
        this.orderBookGroupRanges,
        this.targetCurrencyScaleSize,
        this.acquirableExecutionSize,
        this.executionLogger,
        this.retryPolicy);
  }

  /**
   * Copy the current immutable object with elements that replace the content of {@link MarketSetting#orderBookGroupRanges() orderBookGroupRanges}.
   * The array is cloned before being saved as attribute values.
   * @param elements The non-null elements for orderBookGroupRanges
   * @return A modified copy of {@code this} object
   */
  public final MarketSetting withOrderBookGroupRanges(Num... elements) {
    Num[] newValue = elements.clone();
    return new MarketSetting(
        this.baseCurrencyMinimumBidPrice,
        this.targetCurrencyMinimumBidSize,
        newValue,
        this.targetCurrencyScaleSize,
        this.acquirableExecutionSize,
        this.executionLogger,
        this.retryPolicy);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link MarketSetting#targetCurrencyScaleSize() targetCurrencyScaleSize} attribute.
   * A value equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for targetCurrencyScaleSize
   * @return A modified copy of the {@code this} object
   */
  public final MarketSetting withTargetCurrencyScaleSize(int value) {
    if (this.targetCurrencyScaleSize == value) return this;
    return new MarketSetting(
        this.baseCurrencyMinimumBidPrice,
        this.targetCurrencyMinimumBidSize,
        this.orderBookGroupRanges,
        value,
        this.acquirableExecutionSize,
        this.executionLogger,
        this.retryPolicy);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link MarketSetting#acquirableExecutionSize() acquirableExecutionSize} attribute.
   * A value equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for acquirableExecutionSize
   * @return A modified copy of the {@code this} object
   */
  public final MarketSetting withAcquirableExecutionSize(int value) {
    if (this.acquirableExecutionSize == value) return this;
    return new MarketSetting(
        this.baseCurrencyMinimumBidPrice,
        this.targetCurrencyMinimumBidSize,
        this.orderBookGroupRanges,
        this.targetCurrencyScaleSize,
        value,
        this.executionLogger,
        this.retryPolicy);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link MarketSetting#executionLogger() executionLogger} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for executionLogger
   * @return A modified copy of the {@code this} object
   */
  public final MarketSetting withExecutionLogger(Class<? extends ExecutionLogger> value) {
    if (this.executionLogger == value) return this;
    Class<? extends ExecutionLogger> newValue = Objects.requireNonNull(value, "executionLogger");
    return new MarketSetting(
        this.baseCurrencyMinimumBidPrice,
        this.targetCurrencyMinimumBidSize,
        this.orderBookGroupRanges,
        this.targetCurrencyScaleSize,
        this.acquirableExecutionSize,
        newValue,
        this.retryPolicy);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link MarketSetting#retryPolicy() retryPolicy} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for retryPolicy
   * @return A modified copy of the {@code this} object
   */
  public final MarketSetting withRetryPolicy(RetryPolicy value) {
    if (this.retryPolicy == value) return this;
    RetryPolicy newValue = Objects.requireNonNull(value, "retryPolicy");
    return new MarketSetting(
        this.baseCurrencyMinimumBidPrice,
        this.targetCurrencyMinimumBidSize,
        this.orderBookGroupRanges,
        this.targetCurrencyScaleSize,
        this.acquirableExecutionSize,
        this.executionLogger,
        newValue);
  }

  /**
   * This instance is equal to all instances of {@code MarketSetting} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof MarketSetting
        && equalTo((MarketSetting) another);
  }

  private boolean equalTo(MarketSetting another) {
    return baseCurrencyMinimumBidPrice.equals(another.baseCurrencyMinimumBidPrice)
        && targetCurrencyMinimumBidSize.equals(another.targetCurrencyMinimumBidSize)
        && Arrays.equals(orderBookGroupRanges, another.orderBookGroupRanges)
        && targetCurrencyScaleSize == another.targetCurrencyScaleSize
        && acquirableExecutionSize == another.acquirableExecutionSize
        && executionLogger.equals(another.executionLogger)
        && retryPolicy.equals(another.retryPolicy);
  }

  /**
   * Computes a hash code from attributes: {@code baseCurrencyMinimumBidPrice}, {@code targetCurrencyMinimumBidSize}, {@code orderBookGroupRanges}, {@code targetCurrencyScaleSize}, {@code acquirableExecutionSize}, {@code executionLogger}, {@code retryPolicy}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + baseCurrencyMinimumBidPrice.hashCode();
    h += (h << 5) + targetCurrencyMinimumBidSize.hashCode();
    h += (h << 5) + Arrays.hashCode(orderBookGroupRanges);
    h += (h << 5) + targetCurrencyScaleSize;
    h += (h << 5) + acquirableExecutionSize;
    h += (h << 5) + executionLogger.hashCode();
    h += (h << 5) + retryPolicy.hashCode();
    return h;
  }

  /**
   * Prints the immutable value {@code MarketSetting} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("MarketSetting")
        .omitNullValues()
        .add("baseCurrencyMinimumBidPrice", baseCurrencyMinimumBidPrice)
        .add("targetCurrencyMinimumBidSize", targetCurrencyMinimumBidSize)
        .add("orderBookGroupRanges", Arrays.toString(orderBookGroupRanges))
        .add("targetCurrencyScaleSize", targetCurrencyScaleSize)
        .add("acquirableExecutionSize", acquirableExecutionSize)
        .add("executionLogger", executionLogger)
        .add("retryPolicy", retryPolicy)
        .toString();
  }

  /**
   * Creates an immutable copy of a {@link MarketSettingData} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable MarketSetting instance
   */
  static MarketSetting copyOf(MarketSettingData instance) {
    if (instance instanceof MarketSetting) {
      return (MarketSetting) instance;
    }
    return MarketSetting.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link MarketSetting MarketSetting}.
   * <pre>
   * MarketSetting.builder()
   *    .baseCurrencyMinimumBidPrice(cointoss.util.Num) // required {@link MarketSetting#baseCurrencyMinimumBidPrice() baseCurrencyMinimumBidPrice}
   *    .targetCurrencyMinimumBidSize(cointoss.util.Num) // required {@link MarketSetting#targetCurrencyMinimumBidSize() targetCurrencyMinimumBidSize}
   *    .orderBookGroupRanges(cointoss.util.Num) // required {@link MarketSetting#orderBookGroupRanges() orderBookGroupRanges}
   *    .targetCurrencyScaleSize(int) // optional {@link MarketSetting#targetCurrencyScaleSize() targetCurrencyScaleSize}
   *    .acquirableExecutionSize(int) // optional {@link MarketSetting#acquirableExecutionSize() acquirableExecutionSize}
   *    .executionLogger(Class&amp;lt;? extends cointoss.execution.ExecutionLogger&amp;gt;) // optional {@link MarketSetting#executionLogger() executionLogger}
   *    .retryPolicy(cointoss.util.RetryPolicy) // optional {@link MarketSetting#retryPolicy() retryPolicy}
   *    .build();
   * </pre>
   * @return A new MarketSetting builder
   */
  public static MarketSetting.Builder builder() {
    return new MarketSetting.Builder();
  }

  /**
   * Builds instances of type {@link MarketSetting MarketSetting}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "MarketSettingData", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_BASE_CURRENCY_MINIMUM_BID_PRICE = 0x1L;
    private static final long INIT_BIT_TARGET_CURRENCY_MINIMUM_BID_SIZE = 0x2L;
    private static final long INIT_BIT_ORDER_BOOK_GROUP_RANGES = 0x4L;
    private static final long OPT_BIT_TARGET_CURRENCY_SCALE_SIZE = 0x1L;
    private static final long OPT_BIT_ACQUIRABLE_EXECUTION_SIZE = 0x2L;
    private long initBits = 0x7L;
    private long optBits;

    private @Nullable Num baseCurrencyMinimumBidPrice;
    private @Nullable Num targetCurrencyMinimumBidSize;
    private @Nullable Num[] orderBookGroupRanges;
    private int targetCurrencyScaleSize;
    private int acquirableExecutionSize;
    private @Nullable Class<? extends ExecutionLogger> executionLogger;
    private @Nullable RetryPolicy retryPolicy;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code MarketSetting} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder from(MarketSetting instance) {
      return from((MarketSettingData) instance);
    }

    /**
     * Copy abstract value type {@code MarketSettingData} instance into builder.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    final Builder from(MarketSettingData instance) {
      Objects.requireNonNull(instance, "instance");
      baseCurrencyMinimumBidPrice(instance.baseCurrencyMinimumBidPrice());
      targetCurrencyMinimumBidSize(instance.targetCurrencyMinimumBidSize());
      orderBookGroupRanges(instance.orderBookGroupRanges());
      targetCurrencyScaleSize(instance.targetCurrencyScaleSize());
      acquirableExecutionSize(instance.acquirableExecutionSize());
      executionLogger(instance.executionLogger());
      retryPolicy(instance.retryPolicy());
      return this;
    }

    /**
     * Initializes the value for the {@link MarketSetting#baseCurrencyMinimumBidPrice() baseCurrencyMinimumBidPrice} attribute.
     * @param baseCurrencyMinimumBidPrice The value for baseCurrencyMinimumBidPrice 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder baseCurrencyMinimumBidPrice(Num baseCurrencyMinimumBidPrice) {
      this.baseCurrencyMinimumBidPrice = Objects.requireNonNull(baseCurrencyMinimumBidPrice, "baseCurrencyMinimumBidPrice");
      initBits &= ~INIT_BIT_BASE_CURRENCY_MINIMUM_BID_PRICE;
      return this;
    }

    /**
     * Initializes the value for the {@link MarketSetting#targetCurrencyMinimumBidSize() targetCurrencyMinimumBidSize} attribute.
     * @param targetCurrencyMinimumBidSize The value for targetCurrencyMinimumBidSize 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder targetCurrencyMinimumBidSize(Num targetCurrencyMinimumBidSize) {
      this.targetCurrencyMinimumBidSize = Objects.requireNonNull(targetCurrencyMinimumBidSize, "targetCurrencyMinimumBidSize");
      initBits &= ~INIT_BIT_TARGET_CURRENCY_MINIMUM_BID_SIZE;
      return this;
    }

    /**
     * Initializes the value for the {@link MarketSetting#orderBookGroupRanges() orderBookGroupRanges} attribute.
     * @param orderBookGroupRanges The elements for orderBookGroupRanges
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder orderBookGroupRanges(Num... orderBookGroupRanges) {
      this.orderBookGroupRanges = orderBookGroupRanges.clone();
      initBits &= ~INIT_BIT_ORDER_BOOK_GROUP_RANGES;
      return this;
    }

    /**
     * Initializes the value for the {@link MarketSetting#targetCurrencyScaleSize() targetCurrencyScaleSize} attribute.
     * <p><em>If not set, this attribute will have a default value as returned by the initializer of {@link MarketSetting#targetCurrencyScaleSize() targetCurrencyScaleSize}.</em>
     * @param targetCurrencyScaleSize The value for targetCurrencyScaleSize 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder targetCurrencyScaleSize(int targetCurrencyScaleSize) {
      this.targetCurrencyScaleSize = targetCurrencyScaleSize;
      optBits |= OPT_BIT_TARGET_CURRENCY_SCALE_SIZE;
      return this;
    }

    /**
     * Initializes the value for the {@link MarketSetting#acquirableExecutionSize() acquirableExecutionSize} attribute.
     * <p><em>If not set, this attribute will have a default value as returned by the initializer of {@link MarketSetting#acquirableExecutionSize() acquirableExecutionSize}.</em>
     * @param acquirableExecutionSize The value for acquirableExecutionSize 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder acquirableExecutionSize(int acquirableExecutionSize) {
      this.acquirableExecutionSize = acquirableExecutionSize;
      optBits |= OPT_BIT_ACQUIRABLE_EXECUTION_SIZE;
      return this;
    }

    /**
     * Initializes the value for the {@link MarketSetting#executionLogger() executionLogger} attribute.
     * <p><em>If not set, this attribute will have a default value as returned by the initializer of {@link MarketSetting#executionLogger() executionLogger}.</em>
     * @param executionLogger The value for executionLogger 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder executionLogger(Class<? extends ExecutionLogger> executionLogger) {
      this.executionLogger = Objects.requireNonNull(executionLogger, "executionLogger");
      return this;
    }

    /**
     * Initializes the value for the {@link MarketSetting#retryPolicy() retryPolicy} attribute.
     * <p><em>If not set, this attribute will have a default value as returned by the initializer of {@link MarketSetting#retryPolicy() retryPolicy}.</em>
     * @param retryPolicy The value for retryPolicy 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder retryPolicy(RetryPolicy retryPolicy) {
      this.retryPolicy = Objects.requireNonNull(retryPolicy, "retryPolicy");
      return this;
    }

    /**
     * Builds a new {@link MarketSetting MarketSetting}.
     * @return An immutable instance of MarketSetting
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public MarketSetting build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new MarketSetting(this);
    }

    private boolean targetCurrencyScaleSizeIsSet() {
      return (optBits & OPT_BIT_TARGET_CURRENCY_SCALE_SIZE) != 0;
    }

    private boolean acquirableExecutionSizeIsSet() {
      return (optBits & OPT_BIT_ACQUIRABLE_EXECUTION_SIZE) != 0;
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_BASE_CURRENCY_MINIMUM_BID_PRICE) != 0) attributes.add("baseCurrencyMinimumBidPrice");
      if ((initBits & INIT_BIT_TARGET_CURRENCY_MINIMUM_BID_SIZE) != 0) attributes.add("targetCurrencyMinimumBidSize");
      if ((initBits & INIT_BIT_ORDER_BOOK_GROUP_RANGES) != 0) attributes.add("orderBookGroupRanges");
      return "Cannot build MarketSetting, some of required attributes are not set " + attributes;
    }
  }
}
