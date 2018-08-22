package cointoss;

import cointoss.util.Num;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Var;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.processing.Generated;

/**
 * @version 2018/08/22 19:55:10
 */
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@Generated("org.immutables.processor.ProxyProcessor")
@org.immutables.value.Generated(from = "MarketSettingData", generator = "Immutables")
@Immutable
@CheckReturnValue
public final class MarketSetting implements MarketSettingData {
  private final Num baseCurrencyMinimumBidPrice;
  private final Num targetCurrencyMinimumBidSize;
  private final ImmutableList<Num> orderBookGroupRanges;
  private final int targetCurrencyScaleSize;

  private MarketSetting(MarketSetting.Builder builder) {
    this.baseCurrencyMinimumBidPrice = builder.baseCurrencyMinimumBidPrice;
    this.targetCurrencyMinimumBidSize = builder.targetCurrencyMinimumBidSize;
    this.orderBookGroupRanges = builder.orderBookGroupRanges.build();
    this.targetCurrencyScaleSize = builder.targetCurrencyScaleSizeIsSet()
        ? builder.targetCurrencyScaleSize
        : MarketSettingData.super.targetCurrencyScaleSize();
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
  public ImmutableList<Num> orderBookGroupRanges() {
    return orderBookGroupRanges;
  }

  /**
   * Get the human readable size of target currency.
   */
  @Override
  public int targetCurrencyScaleSize() {
    return targetCurrencyScaleSize;
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
        && orderBookGroupRanges.equals(another.orderBookGroupRanges)
        && targetCurrencyScaleSize == another.targetCurrencyScaleSize;
  }

  /**
   * Computes a hash code from attributes: {@code baseCurrencyMinimumBidPrice}, {@code targetCurrencyMinimumBidSize}, {@code orderBookGroupRanges}, {@code targetCurrencyScaleSize}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + baseCurrencyMinimumBidPrice.hashCode();
    h += (h << 5) + targetCurrencyMinimumBidSize.hashCode();
    h += (h << 5) + orderBookGroupRanges.hashCode();
    h += (h << 5) + targetCurrencyScaleSize;
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
        .add("orderBookGroupRanges", orderBookGroupRanges)
        .add("targetCurrencyScaleSize", targetCurrencyScaleSize)
        .toString();
  }

  /**
   * Creates a builder for {@link MarketSetting MarketSetting}.
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
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_BASE_CURRENCY_MINIMUM_BID_PRICE = 0x1L;
    private static final long INIT_BIT_TARGET_CURRENCY_MINIMUM_BID_SIZE = 0x2L;
    private static final long OPT_BIT_TARGET_CURRENCY_SCALE_SIZE = 0x1L;
    private long initBits = 0x3L;
    private long optBits;

    private @Nullable Num baseCurrencyMinimumBidPrice;
    private @Nullable Num targetCurrencyMinimumBidSize;
    private ImmutableList.Builder<Num> orderBookGroupRanges = ImmutableList.builder();
    private int targetCurrencyScaleSize;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code MarketSetting} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * Collection elements and entries will be added, not replaced.
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
      addAllOrderBookGroupRanges(instance.orderBookGroupRanges());
      targetCurrencyScaleSize(instance.targetCurrencyScaleSize());
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
     * Adds one element to {@link MarketSetting#orderBookGroupRanges() orderBookGroupRanges} list.
     * @param element A orderBookGroupRanges element
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder addOrderBookGroupRanges(Num element) {
      this.orderBookGroupRanges.add(element);
      return this;
    }

    /**
     * Adds elements to {@link MarketSetting#orderBookGroupRanges() orderBookGroupRanges} list.
     * @param elements An array of orderBookGroupRanges elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder addOrderBookGroupRanges(Num... elements) {
      this.orderBookGroupRanges.add(elements);
      return this;
    }


    /**
     * Sets or replaces all elements for {@link MarketSetting#orderBookGroupRanges() orderBookGroupRanges} list.
     * @param elements An iterable of orderBookGroupRanges elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder orderBookGroupRanges(Iterable<? extends Num> elements) {
      this.orderBookGroupRanges = ImmutableList.builder();
      return addAllOrderBookGroupRanges(elements);
    }

    /**
     * Adds elements to {@link MarketSetting#orderBookGroupRanges() orderBookGroupRanges} list.
     * @param elements An iterable of orderBookGroupRanges elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder addAllOrderBookGroupRanges(Iterable<? extends Num> elements) {
      this.orderBookGroupRanges.addAll(elements);
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

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_BASE_CURRENCY_MINIMUM_BID_PRICE) != 0) attributes.add("baseCurrencyMinimumBidPrice");
      if ((initBits & INIT_BIT_TARGET_CURRENCY_MINIMUM_BID_SIZE) != 0) attributes.add("targetCurrencyMinimumBidSize");
      return "Cannot build MarketSetting, some of required attributes are not set " + attributes;
    }
  }
}
