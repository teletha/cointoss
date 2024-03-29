/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package hypatia;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.SplittableRandom;
import java.util.function.DoubleBinaryOperator;
import java.util.stream.IntStream;

import com.google.common.math.DoubleMath;

import kiss.Decoder;
import kiss.Encoder;
import kiss.I;
import kiss.Managed;
import kiss.Signal;
import kiss.Singleton;
import kiss.Variable;
import primavera.function.DoublePentaFunction;
import primavera.function.DoubleTetraFunction;
import primavera.function.DoubleTriFunction;

/**
 * A signed real number with arbitrary precision that cannot be changed.
 */
@SuppressWarnings("serial")
public class Num extends Arithmetic<Num> {

    /** RANDOM Generator */
    private static final SplittableRandom RANDOM = new SplittableRandom();

    /** The acceptable decimal difference. */
    static final double Fuzzy = 1e-14;

    /** The base context. */
    static final MathContext CONTEXT = new MathContext(19, RoundingMode.HALF_UP);

    /** Reusable cache. */
    private static final Num[] CACHE = IntStream.rangeClosed(0, 100).mapToObj(i -> new Num(i, 0)).toArray(Num[]::new);

    /** reuse */
    public static final Num ZERO = CACHE[0];

    /** reuse */
    public static final Num ONE = CACHE[1];

    /** reuse */
    public static final Num TWO = CACHE[2];

    /** reuse */
    public static final Num THREE = CACHE[3];

    /** reuse */
    public static final Num FOUR = CACHE[4];

    /** reuse */
    public static final Num TEN = CACHE[10];

    /** reuse */
    public static final Num HUNDRED = CACHE[100];

    /** reuse */
    public static final Num MAX = new Num(Long.MAX_VALUE, 0);

    /** reuse */
    public static final Num MIN = new Num(Long.MIN_VALUE, 0);

    /** Express a real number as the product of an integer N and a power of 10. */
    private final long v;

    /** Express a real number as the product of an integer N and a power of 10. */
    private final int scale;

    /**
     * Construct the number as a binary format with dynamic fixed precision.
     * 
     * @param value
     * @param scale
     */
    protected Num(long value, int scale) {
        this.v = value;
        this.scale = scale;
        this.big = null;
    }

    /**
     * Use an arbitrary double-precision decimal point for real numbers that do not fit in the range
     * of Long.
     */
    private final BigDecimal big;

    /**
     * Constructs the number as a signed decimal number with arbitrary precision.
     * 
     * @param value
     */
    protected Num(BigDecimal value) {
        this.v = 0;
        this.scale = 0;
        this.big = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Num create(long value) {
        if (0 <= value && value <= 100) {
            return CACHE[(int) value];
        } else {
            return new Num(value, 0);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Num create(double value) {
        return create(value, 0);
    }

    /**
     * Create {@link Num} from primitive double value with the specified scale calculator.
     * 
     * @param value
     * @param scaler
     * @return
     */
    private Num create(double value, int sclaer) {
        try {
            int scale = computeScale(value);
            double longed = value * pow10(scale);
            if (Long.MIN_VALUE < longed && longed < Long.MAX_VALUE) {
                return new Num((long) longed, scale + sclaer);
            } else {
                // don't use BigDecimal constructor
                return create(BigDecimal.valueOf(value));
            }
        } catch (ArithmeticException e) {
            // don't use BigDecimal constructor
            return create(BigDecimal.valueOf(value));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Num create(String value) {
        int length = value.length();
        if (length <= 18) {
            int index = value.indexOf('.');
            if (index == -1) {
                try {
                    return create(Long.parseLong(value));
                } catch (NumberFormatException e) {
                    // parse as exponential expression
                }
            }

            int lastDigitIndex = findLastNonZeroDigit(value, length);

            // parse as long directly and insanely fast
            long result = 0;
            boolean negative = false;
            length = 1 + lastDigitIndex;
            for (int i = 0; i < length; i++) {
                switch (value.charAt(i)) {
                case '0':
                    result = result * 10;
                    break;
                case '1':
                    result = result * 10 - 1;
                    break;
                case '2':
                    result = result * 10 - 2;
                    break;
                case '3':
                    result = result * 10 - 3;
                    break;
                case '4':
                    result = result * 10 - 4;
                    break;
                case '5':
                    result = result * 10 - 5;
                    break;
                case '6':
                    result = result * 10 - 6;
                    break;
                case '7':
                    result = result * 10 - 7;
                    break;
                case '8':
                    result = result * 10 - 8;
                    break;
                case '9':
                    result = result * 10 - 9;
                    break;

                case '-':
                    if (i != 0) {
                        throw new NumberFormatException("Invalid Format [" + value + "]");
                    }
                    negative = true;
                    break;

                case '+':
                    if (i != 0) {
                        throw new NumberFormatException("Invalid Format [" + value + "]");
                    }
                    break; // ignore

                case '.':
                    break; // ignore

                // Don't apply exponential case, it occurs using lookuptable instruction
                // instead of tableswitch instruction.
                // case 'e':
                // case 'E':
                default:
                    char c = value.charAt(i);
                    if (c == 'E' || c == 'e') {
                        // reculculate the last digit index
                        lastDigitIndex = findLastNonZeroDigit(value, i);

                        // revert the extra calculation
                        result /= positives[i - 1 - lastDigitIndex];

                        // Estimate Scale
                        // scale
                        // = (textLength - pointIndex) - (textLength - lastDigitIndex) - exp
                        // = lastDigitIndex - pointIndex - exp
                        return new Num(negative ? result : -result, lastDigitIndex - Math.max(0, index) - parseInt(value, i + 1, length));
                    } else {
                        // fallback
                        return create(new BigDecimal(value, CONTEXT));
                    }
                }
            }

            // Estimate Scale
            // scale
            // = (textLength - pointIndex) - (textLength - lastDigitIndex)
            // = lastDigitIndex - pointIndex
            return new Num(negative ? result : -result, lastDigitIndex - index);
        }

        // https://github.com/eobermuhlner/big-math#why-is-there-bigdecimalmathtobigdecimalstring-if-java-already-has-a-bigdecimalstring-constructor
        //
        // The BigDecimal(String) constructor as provided by Java gets increasingly slower if
        // you pass longer strings to it. The implementation in Java 11 and before is O(n^2).
        //
        // If you want to convert very long strings (10000 characters or longer) then this slow
        // constructor may become an issue.
        //
        // BigDecimalMath.toBigDecimal(String) is a drop-in replacement with the same
        // functionality (converting a string representation into a BigDecimal) but it is using
        // a faster recursive implementation.
        return create(new BigDecimal(value, CONTEXT));
    }

    /**
     * Find index of the last non-zero digit.
     * 
     * @param value A targe digit expression.
     * @return A result.
     */
    private static int findLastNonZeroDigit(String value, int last) {
        for (int i = last - 1; 0 <= i; i--) {
            char c = value.charAt(i);
            if (c != '0') {
                return i;
            }
        }

        // If this exception will be thrown, it is bug of this program. So we must rethrow the
        // wrapped error in here.
        throw new Error("Fix Bug!");
    }

    /**
     * Parse the text as primitive int insanely fast.
     * 
     * @param value A digit expression.
     * @param start A start position to parse.
     * @param end A end position to parse.
     * @return A pased value.
     */
    private static int parseInt(String value, int start, int end) {
        int result = 0;
        boolean negative = false;
        for (int i = start; i < end; i++) {
            switch (value.charAt(i)) {
            case '0':
                result = result * 10;
                break;
            case '1':
                result = result * 10 - 1;
                break;
            case '2':
                result = result * 10 - 2;
                break;
            case '3':
                result = result * 10 - 3;
                break;
            case '4':
                result = result * 10 - 4;
                break;
            case '5':
                result = result * 10 - 5;
                break;
            case '6':
                result = result * 10 - 6;
                break;
            case '7':
                result = result * 10 - 7;
                break;
            case '8':
                result = result * 10 - 8;
                break;
            case '9':
                result = result * 10 - 9;
                break;

            case '-':
                if (i != start) {
                    throw new NumberFormatException("Invalid Format [" + value + "]");
                }
                negative = true;
                break;

            case '+':
                if (i != start) {
                    throw new NumberFormatException("Invalid Format [" + value + "]");
                }
                break; // ignore

            default:
                throw new NumberFormatException("Invalid Format [" + value + "]");
            }
        }
        return negative ? result : -result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Num create(BigDecimal value) {
        return new Num(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int signum() {
        if (big != null) {
            return big.signum();
        } else {
            return v == 0 ? 0 : v < 0 ? -1 : 1;
        }
    }

    /**
     * Convert to {@link BigDecimal}.
     * 
     * @return
     */
    private BigDecimal big() {
        if (big != null) {
            return big;
        } else if (scale == 0) {
            return new BigDecimal(v, CONTEXT);
        } else {
            return new BigDecimal(v, CONTEXT).scaleByPowerOfTen(-scale);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Num plus(Num value) {
        if (big != null) {
            return create(big.add(value.big(), CONTEXT));
        } else if (value.big != null) {
            return create(big().add(value.big, CONTEXT));
        } else {
            try {
                if (scale == value.scale) {
                    return new Num(Math.addExact(v, value.v), scale);
                } else if (scale < value.scale) {
                    return new Num(Math.addExact((long) (v * pow10(value.scale - scale)), value.v), value.scale);
                } else {
                    return new Num(Math.addExact(v, (long) (value.v * pow10(scale - value.scale))), scale);
                }
            } catch (ArithmeticException e) {
                return create(big().add(value.big()));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Num minus(Num value) {
        if (big != null) {
            return create(big.subtract(value.big(), CONTEXT));
        } else if (value.big != null) {
            return create(big().subtract(value.big, CONTEXT));
        } else {
            try {
                if (scale == value.scale) {
                    return new Num(Math.subtractExact(v, value.v), scale);
                } else if (scale < value.scale) {
                    return new Num(Math.subtractExact((long) (v * pow10(value.scale - scale)), value.v), value.scale);
                } else {
                    return new Num(Math.subtractExact(v, (long) (value.v * pow10(scale - value.scale))), scale);
                }
            } catch (ArithmeticException e) {
                return create(big().subtract(value.big()));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Num multiply(Num value) {
        if (big != null) {
            return create(big.multiply(value.big(), CONTEXT));
        } else if (value.big != null) {
            return create(big().multiply(value.big, CONTEXT));
        } else {
            try {
                return new Num(Math.multiplyExact(v, value.v), scale + value.scale);
            } catch (ArithmeticException e) {
                return create(big().multiply(value.big()));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Num divide(Num value) {
        if (big != null) {
            return create(big.divide(value.big(), CONTEXT));
        } else if (value.big != null) {
            return create(big().divide(value.big, CONTEXT));
        } else {
            if (value.v == 0) throw new ArithmeticException("Trying to divide " + this + " by 0.");

            return create((double) v / value.v, scale - value.scale);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Num remainder(Num value) {
        if (big != null) {
            return create(big.remainder(value.big()));
        } else if (value.big != null) {
            return create(big().remainder(value.big));
        } else {
            try {
                if (scale == value.scale) {
                    return new Num(v % value.v, scale);
                } else if (scale < value.scale) {
                    return new Num((Math.multiplyExact(v, (long) pow10(value.scale - scale))) % value.v, value.scale);
                } else {
                    return new Num(v % (long) (value.v * pow10(scale - value.scale)), scale);
                }
            } catch (ArithmeticException e) {
                return create(big().remainder(value.big()));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Num quotient(Num value) {
        if (big != null) {
            return create(big.divideToIntegralValue(value.big()));
        } else if (value.big != null) {
            return create(big().divideToIntegralValue(value.big));
        } else {
            return this.minus(this.remainder(value)).divide(value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Num calculate(int maxScale, Num param1, DoubleBinaryOperator calculation) {
        // inlined code for performance
        int max = scale;
        if (max < param1.scale) max = param1.scale;

        double v0 = this.v * negatives[scale];
        double v1 = param1.v * negatives[param1.scale];

        return new Num((long) (calculation.applyAsDouble(v0, v1) * positives[max]), max);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Num calculate(int maxScale, Num param1, Num param2, DoubleTriFunction calculation) {
        // inlined code for performance
        int max = scale;
        if (max < param1.scale) max = param1.scale;
        if (max < param2.scale) max = param2.scale;

        double v0 = this.v * negatives[scale];
        double v1 = param1.v * negatives[param1.scale];
        double v2 = param2.v * negatives[param2.scale];

        return new Num((long) (calculation.applyAsDouble(v0, v1, v2) * positives[max]), max);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Num calculate(int maxScale, Num param1, Num param2, Num param3, DoubleTetraFunction calculation) {
        // inlined code for performance
        int max = scale;
        if (max < param1.scale) max = param1.scale;
        if (max < param2.scale) max = param2.scale;
        if (max < param3.scale) max = param3.scale;

        double v0 = this.v * negatives[scale];
        double v1 = param1.v * negatives[param1.scale];
        double v2 = param2.v * negatives[param2.scale];
        double v3 = param3.v * negatives[param3.scale];

        return new Num((long) (calculation.applyAsDouble(v0, v1, v2, v3) * positives[max]), max);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Num calculate(int maxScale, Num param1, Num param2, Num param3, Num param4, DoublePentaFunction calculation) {
        // inlined code for performance
        int max = maxScale;
        if (max < param1.scale) max = param1.scale;
        if (max < param2.scale) max = param2.scale;
        if (max < param3.scale) max = param3.scale;
        if (max < param4.scale) max = param4.scale;

        double v0 = this.v * negatives[scale];
        double v1 = param1.v * negatives[param1.scale];
        double v2 = param2.v * negatives[param2.scale];
        double v3 = param3.v * negatives[param3.scale];
        double v4 = param4.v * negatives[param4.scale];

        return new Num((long) (calculation.applyAsDouble(v0, v1, v2, v3, v4) * positives[max]), max);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Num abs() {
        if (big != null) {
            return create(big.abs());
        } else if (v == Long.MIN_VALUE) {
            return create(big().abs());
        } else {
            return new Num(Math.abs(v), scale);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Num o) {
        if (big != null) {
            return big.compareTo(o.big());
        } else if (o.big != null) {
            return big().compareTo(o.big);
        } else {
            if (scale == o.scale) {
                return Long.compare(v, o.v);
            } else if (scale < o.scale) {
                return Long.compare((long) (v * pow10(o.scale - scale)), o.v);
            } else {
                return Long.compare(v, (long) (o.v * pow10(scale - o.scale)));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Num ceiling(Num base) {
        if (big != null) {
            return create(Num.ceiling(big, base.big()));
        } else if (base.big != null) {
            return create(Num.ceiling(big(), base.big));
        } else {
            if (base.v == 0) throw new ArithmeticException("Trying to divide " + this + " by 0.");

            try {
                if (scale == base.scale) {
                    long rem = v % base.v;
                    return rem == 0 ? this : new Num(v - rem + base.v, scale);
                } else if (scale < base.scale) {
                    long value = (Math.multiplyExact(v, (long) pow10(base.scale - scale)));
                    long rem = value % base.v;
                    return rem == 0 ? this : new Num(value - rem + base.v, base.scale);
                } else {
                    long value = Math.multiplyExact(base.v, (long) pow10(scale - base.scale));
                    long rem = v % value;
                    return rem == 0 ? this : new Num(v - rem + value, scale);
                }
            } catch (ArithmeticException e) {
                return create(Num.ceiling(big(), base.big()));
            }
        }
    }

    /**
     * Helper method to calculate round-up value on {@link BigDecimal} context.
     * 
     * @param value A target value.
     * @param base A base value.
     * @return A round-up value.
     */
    static BigDecimal ceiling(BigDecimal value, BigDecimal base) {
        BigDecimal rem = value.remainder(base);
        return rem.signum() == 0 ? value : value.subtract(rem).add(base);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Num floor(Num base) {
        if (big != null) {
            return create(Num.floor(big, base.big()));
        } else if (base.big != null) {
            return create(Num.floor(big(), base.big));
        } else {
            if (base.v == 0) throw new ArithmeticException("Trying to divide " + this + " by 0.");

            try {
                if (scale == base.scale) {
                    return new Num(v - v % base.v, scale);
                } else if (scale < base.scale) {
                    long value = (Math.multiplyExact(v, (long) pow10(base.scale - scale)));
                    return new Num(value - value % base.v, base.scale);
                } else {
                    return new Num(v - v % Math.multiplyExact(base.v, (long) pow10(scale - base.scale)), scale);
                }
            } catch (ArithmeticException e) {
                return create(Num.floor(big(), base.big()));
            }
        }
    }

    /**
     * Helper method to calculate round-down value on {@link BigDecimal} context.
     * 
     * @param value A target value.
     * @param base A base value.
     * @return A round-down value.
     */
    static BigDecimal floor(BigDecimal value, BigDecimal base) {
        BigDecimal rem = value.remainder(base);
        return rem.signum() == 0 ? value : value.subtract(rem);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Num decuple(int n) {
        if (big != null) {
            return create(big.scaleByPowerOfTen(n));
        } else if (n == 0) {
            return this;
        } else {
            int s = scale - n;
            if (0 < s) {
                return new Num(v, s);
            } else {
                try {
                    return new Num(Math.multiplyExact(v, (long) positives[-s]), 0);
                } catch (ArithmeticException | ArrayIndexOutOfBoundsException e) {
                    return create(big().scaleByPowerOfTen(n));
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Num negate() {
        if (big != null) {
            return create(big.negate());
        } else {
            try {
                return new Num(Math.negateExact(v), scale);
            } catch (ArithmeticException e) {
                return create(big().negate());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Num pow(int n) {
        if (big != null) {
            return create(big.pow(n));
        } else if (n == 0) {
            return Num.ONE; // by definition
        } else if (n == 1) {
            return this; // shortcut
        } else if (v == 0) {
            return Num.ZERO; // cache
        } else {
            try {
                double result = Math.pow(v, n);
                DoubleMath.roundToLong(result, RoundingMode.HALF_DOWN);
                return create(result, scale * n);
            } catch (ArithmeticException e) {
                return create(big().pow(n));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int scale() {
        if (big != null) {
            return big.stripTrailingZeros().scale();
        } else {
            return scale;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Num scale(int size, RoundingMode mode) {
        if (big != null) {
            return create(big.setScale(size, mode));
        } else {
            if (scale == size) {
                return this;
            } else if (scale < size) {
                return this;
            } else {
                return new Num(DoubleMath.roundToLong(v * pow10(size - scale), mode), size);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Num sqrt() {
        if (big != null) {
            return create(big.sqrt(CONTEXT));
        } else if (v < 0) {
            throw new ArithmeticException("Cannot calculate the square root of a negative number.");
        } else if (scale % 2 == 0) {
            return create(Math.sqrt(v), scale / 2);
        } else {
            return create(big().sqrt(CONTEXT));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int intValue() {
        if (big != null) {
            return big.intValue();
        } else {
            return (int) (v * pow10(-scale));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long longValue() {
        if (big != null) {
            return big.longValue();
        } else {
            return (long) (v * pow10(-scale));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float floatValue() {
        if (big != null) {
            return big.floatValue();
        } else {
            return (float) Primitives.roundDecimal(v * pow10(-scale), scale);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double doubleValue() {
        if (big != null) {
            return big.doubleValue();
        } else {
            return Primitives.roundDecimal(v * pow10(-scale), scale);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String format(NumberFormat format) {
        if (big != null) {
            return format.format(big);
        } else {
            return format.format(doubleValue());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (big != null) {
            return big.stripTrailingZeros().toPlainString();
        } else if (scale == 0) {
            return Long.toString(v);
        } else {
            return Primitives.roundString(v * pow10(-scale), scale);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        if (big != null) {
            return big.hashCode();
        } else {
            return (int) (v ^ ((scale + 1) >>> 32));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Num ? is((Num) obj) : false;
    }

    /**
     * Convert to {@link Num}.
     * 
     * @param value
     * @return
     */
    public static Num of(int value) {
        return ZERO.create(value);
    }

    /**
     * Convert to {@link Num}.
     * 
     * @param values
     * @return
     */
    public static Num[] of(int... values) {
        Num[] decimals = new Num[values.length];

        for (int i = 0; i < decimals.length; i++) {
            decimals[i] = of(values[i]);
        }
        return decimals;
    }

    /**
     * Convert to {@link Num}.
     * 
     * @param value
     * @return
     */
    public static Num of(long value) {
        return ZERO.create(value);
    }

    /**
     * Convert to {@link Num}.
     * 
     * @param values
     * @return
     */
    public static Num[] of(long... values) {
        Num[] decimals = new Num[values.length];

        for (int i = 0; i < decimals.length; i++) {
            decimals[i] = of(values[i]);
        }
        return decimals;
    }

    /**
     * Convert to {@link Num}.
     * 
     * @param value
     * @return
     */
    public static Num of(float value) {
        return ZERO.create(value);
    }

    /**
     * Convert to {@link Num}.
     * 
     * @param values
     * @return
     */
    public static Num[] of(float... values) {
        Num[] decimals = new Num[values.length];

        for (int i = 0; i < decimals.length; i++) {
            decimals[i] = of(values[i]);
        }
        return decimals;
    }

    /**
     * Convert to {@link Num}.
     * 
     * @param value
     * @return
     */
    public static Num of(double value) {
        return ZERO.create(value);
    }

    /**
     * Convert to {@link Num}.
     * 
     * @param values
     * @return
     */
    public static Num[] of(double... values) {
        Num[] decimals = new Num[values.length];

        for (int i = 0; i < decimals.length; i++) {
            decimals[i] = of(values[i]);
        }
        return decimals;
    }

    /**
     * Convert to {@link Num}.
     * 
     * @param value
     * @return
     */
    public static Num of(String value) {
        return ZERO.create(value);
    }

    /**
     * Convert to {@link Num}.
     * 
     * @param values
     * @return
     */
    public static Num[] of(String... values) {
        Num[] decimals = new Num[values.length];

        for (int i = 0; i < decimals.length; i++) {
            decimals[i] = of(values[i]);
        }
        return decimals;
    }

    public static Num of(BigDecimal value) {
        return ZERO.create(value);
    }

    /**
     * Detect max value.
     * 
     * @param decimals
     * @return
     */
    public static Num max(Num... decimals) {
        return max(Orientational.POSITIVE, decimals);
    }

    /**
     * Detect max value.
     * 
     * @param decimals
     * @return
     */
    public static Num max(Orientational direction, Num... decimals) {
        Num max = decimals[0];

        for (int i = 1; i < decimals.length; i++) {
            if (decimals[i] != null) {
                if (max == null || max.isLessThan(direction, decimals[i])) {
                    max = decimals[i];
                }
            }
        }
        return max;
    }

    /**
     * Detect min value.
     * 
     * @param one
     * @param other
     * @return
     */
    public static Num min(Variable<Num> one, Num other) {
        return min(one.v, other);
    }

    /**
     * Detect min value.
     * 
     * @param decimals
     * @return
     */
    public static Num min(Num... decimals) {
        return min(Orientational.POSITIVE, decimals);
    }

    /**
     * Detect min value.
     * 
     * @param decimals
     * @return
     */
    public static Num min(Orientational direction, Num... decimals) {
        Num min = decimals[0];

        for (int i = 1; i < decimals.length; i++) {
            if (decimals[i] != null) {
                if (min == null || min.isGreaterThan(direction, decimals[i])) {
                    min = decimals[i];
                }
            }
        }
        return min;
    }

    /**
     * Check the value range.
     * 
     * @param min A minimum value.
     * @param value A target value to check.
     * @param max A maximum value.
     * @return A target value in range.
     */
    public static boolean within(Num min, Num value, Num max) {
        if (min.isGreaterThan(value)) {
            return false;
        }

        if (value.isGreaterThan(max)) {
            return false;
        }
        return true;
    }

    /**
     * Check the value range.
     * 
     * @param min A minimum value.
     * @param value A target value to check.
     * @param max A maximum value.
     * @return A target value in range.
     */
    public static Num between(Num min, Num value, Num max) {
        return min(max, max(min, value));
    }

    /**
     * @param start
     * @param end
     * @return
     */
    public static Signal<Num> range(int start, int end) {
        return I.signal(IntStream.rangeClosed(start, end).mapToObj(Num::of)::iterator);
    }

    /**
     * Create {@link Num} with random number between {@link Integer#MIN_VALUE} and
     * {@link Integer#MAX_VALUE}.
     * 
     * @return A random number.
     */
    public static Num random() {
        return of(RANDOM.nextInt());
    }

    /**
     * Create {@link Num} with random number between min and max.
     * 
     * @return A random number.
     */
    public static Num random(int minInclusive, int maxExclusive) {
        return of(RANDOM.nextInt(minInclusive, maxExclusive));
    }

    /**
     * Create {@link Num} with random number between min and max.
     * 
     * @return A random number.
     */
    public static Num random(long minInclusive, long maxExclusive) {
        return of(RANDOM.nextLong(minInclusive, maxExclusive));
    }

    /**
     * Create {@link Num} with random number between min and max.
     * 
     * @return A random number.
     */
    public static Num random(double minInclusive, double maxExclusive) {
        return of(RANDOM.nextDouble(minInclusive, maxExclusive));
    }

    /**
     * Calculate the sum of all numbers.
     * 
     * @param nums
     * @return
     */
    public static Num sum(Num... nums) {
        return sum(List.of(nums));
    }

    /**
     * Calculate the sum of all numbers.
     * 
     * @param nums
     * @return
     */
    public static Num sum(Iterable<Num> nums) {
        Num sum = Num.ZERO;
        for (Num num : nums) {
            sum = sum.plus(num);
        }
        return sum;
    }

    static {
        I.load(Num.class);
    }

    /** The value of the power of 10 is calculated and cached in advance. (For 18 digits.) */
    private static final double[] positives = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000, 10000000000d,
            100000000000d, 1000000000000d, 10000000000000d, 100000000000000d, 1000000000000000d, 10000000000000000d, 100000000000000000d,
            1000000000000000000d, 10000000000000000000d, 100000000000000000000d, 1000000000000000000000d, 10000000000000000000000d,
            100000000000000000000000d, 1000000000000000000000000d, 10000000000000000000000000d, 100000000000000000000000000d};

    /** The value of the power of 10 is calculated and cached in advance. (For 18 digits.) */
    private static final double[] negatives = {1, 0.1, 0.01, 0.001, 0.0001, 0.00001, 0.000001, 0.0000001, 0.00000001, 0.000000001,
            0.0000000001, 0.00000000001, 0.000000000001, 0.0000000000001, 0.00000000000001, 0.000000000000001, 0.0000000000000001,
            0.00000000000000001, 0.000000000000000001, 0.0000000000000000001, 0.00000000000000000001, 0.000000000000000000001};

    /**
     * Fast cached power of ten.
     * 
     * @param scale
     * @return
     */
    private static double pow10(int scale) {
        if (0 <= scale) {
            return positives[scale];
        } else {
            return negatives[-scale];
        }
    }

    /**
     * Estimate scale of the target double value.
     * 
     * @param value
     * @return
     */
    static int computeScale(double value) {
        if (value != 0 && -Fuzzy <= value && value <= Fuzzy) {
            throw new ArithmeticException("Too small.");
        }

        for (int i = 0; i < 18; i++) {
            double fixer = pow10(i);
            double fixed = ((long) (value * fixer)) / fixer;
            if (DoubleMath.fuzzyEquals(value, fixed, Fuzzy)) {
                return i;
            }
        }
        return 14;
    }

    /**
     * 
     */
    @Managed(value = Singleton.class)
    private static class Codec implements Encoder<Num>, Decoder<Num> {

        /**
         * {@inheritDoc}
         */
        @Override
        public Num decode(String value) {
            return of(value);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String encode(Num value) {
            return value.toString();
        }
    }
}