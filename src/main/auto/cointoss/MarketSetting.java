package cointoss;

import cointoss.execution.ExecutionCodec;
import cointoss.util.Num;
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
  private final ExecutionCodec executionCodec;

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
    if (builder.executionCodec != null) {
      initShim.executionCodec(builder.executionCodec);
    }
    this.targetCurrencyScaleSize = initShim.targetCurrencyScaleSize();
    this.acquirableExecutionSize = initShim.acquirableExecutionSize();
    this.executionCodec = initShim.executionCodec();
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

    private byte executionCodecBuildStage = STAGE_UNINITIALIZED;
    private ExecutionCodec executionCodec;

    ExecutionCodec executionCodec() {
      if (executionCodecBuildStage == STAGE_INITIALIZING) throw new IllegalStateException(formatInitCycleMessage());
      if (executionCodecBuildStage == STAGE_UNINITIALIZED) {
        executionCodecBuildStage = STAGE_INITIALIZING;
        this.executionCodec = Objects.requireNonNull(executionCodecInitialize(), "executionCodec");
        executionCodecBuildStage = STAGE_INITIALIZED;
      }
      return this.executionCodec;
    }

    void executionCodec(ExecutionCodec executionCodec) {
      this.executionCodec = executionCodec;
      executionCodecBuildStage = STAGE_INITIALIZED;
    }

    private String formatInitCycleMessage() {
      List<String> attributes = new ArrayList<>();
      if (targetCurrencyScaleSizeBuildStage == STAGE_INITIALIZING) attributes.add("targetCurrencyScaleSize");
      if (acquirableExecutionSizeBuildStage == STAGE_INITIALIZING) attributes.add("acquirableExecutionSize");
      if (executionCodecBuildStage == STAGE_INITIALIZING) attributes.add("executionCodec");
      return "Cannot build MarketSetting, attribute initializers form cycle " + attributes;
    }
  }

  private int targetCurrencyScaleSizeInitialize() {
    return MarketSettingData.super.targetCurrencyScaleSize();
  }

  private int acquirableExecutionSizeInitialize() {
    return MarketSettingData.super.acquirableExecutionSize();
  }

  private ExecutionCodec executionCodecInitialize() {
    return MarketSettingData.super.executionCodec();
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
   * Configure {@link ExecutionLog} codec.
   * 
   * @return
   */
  @Override
  public ExecutionCodec executionCodec() {
    InitShim shim = this.initShim;
    return shim != null
        ? shim.executionCodec()
        : this.executionCodec;
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
        && executionCodec.equals(another.executionCodec);
  }

  /**
   * Computes a hash code from attributes: {@code baseCurrencyMinimumBidPrice}, {@code targetCurrencyMinimumBidSize}, {@code orderBookGroupRanges}, {@code targetCurrencyScaleSize}, {@code acquirableExecutionSize}, {@code executionCodec}.
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
    h += (h << 5) + executionCodec.hashCode();
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
        .add("executionCodec", executionCodec)
        .toString();
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
   *    .executionCodec(cointoss.execution.ExecutionCodec) // optional {@link MarketSetting#executionCodec() executionCodec}
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
    private @Nullable ExecutionCodec executionCodec;

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
      executionCodec(instance.executionCodec());
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
     * Initializes the value for the {@link MarketSetting#executionCodec() executionCodec} attribute.
     * <p><em>If not set, this attribute will have a default value as returned by the initializer of {@link MarketSetting#executionCodec() executionCodec}.</em>
     * @param executionCodec The value for executionCodec 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder executionCodec(ExecutionCodec executionCodec) {
      this.executionCodec = Objects.requireNonNull(executionCodec, "executionCodec");
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
