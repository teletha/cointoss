/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util.decimal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Objects;

import com.google.common.math.DoubleMath;

import cointoss.util.Primitives;

/**
 * A signed real number with arbitrary precision that cannot be changed.
 */
public abstract class Decimal<Self extends Decimal<Self>> extends Arithmetic<Self> {

    private static final double[] positives = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000, 10000000000d,
            100000000000d, 1000000000000d, 10000000000000d, 100000000000000d, 1000000000000000d, 10000000000000000d, 100000000000000000d,
            1000000000000000000d, 10000000000000000000d};

    private static final double[] negatives = {1, 0.1, 0.01, 0.001, 0.0001, 0.00001, 0.000001, 0.0000001, 0.00000001, 0.000000001,
            0.0000000001, 0.00000000001, 0.000000000001, 0.0000000000001, 0.00000000000001, 0.000000000000001, 0.0000000000000001,
            0.00000000000000001, 0.000000000000000001, 0.0000000000000000001};

    private static double pow10(int scale) {
        if (0 <= scale) {
            return positives[scale];
        } else {
            return negatives[-scale];
        }
    }

    long v;

    int scale;

    /**
     * Construct the number as a binary format with dynamic fixed precision.
     * 
     * @param value
     * @param scale
     */
    protected Decimal(long value, int scale) {
        this.v = value;
        this.scale = scale;
    }

    BigDecimal big;

    /**
     * Constructs the number as a signed decimal number with arbitrary precision.
     * 
     * @param value
     */
    protected Decimal(BigDecimal value) {
        this.big = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Self create(int value) {
        return create(value, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Self create(long value) {
        return create(value, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Self create(double value) {
        int scale = computeScale(value);
        return create((long) (value * pow10(scale)), scale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Self create(String value) {
        return create(new BigDecimal(value, CONTEXT));
    }

    protected abstract Self create(long value, int scale);

    BigDecimal big() {
        if (big != null) {
            return big;
        } else {
            return BigDecimal.valueOf(v).scaleByPowerOfTen(-scale);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Self plus(Self value) {
        if (big != null) {
            return create(big.add(value.big(), CONTEXT));
        } else if (value.big != null) {
            return create(big().add(value.big, CONTEXT));
        } else {
            try {
                if (scale == value.scale) {
                    return create(Math.addExact(v, value.v), scale);
                } else if (scale < value.scale) {
                    return create(Math.addExact((long) (v * pow10(value.scale - scale)), value.v), value.scale);
                } else {
                    return create(Math.addExact(v, (long) (value.v * pow10(scale - value.scale))), scale);
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
    public Self minus(Self value) {
        if (big != null) {
            return create(big.subtract(value.big(), CONTEXT));
        } else if (value.big != null) {
            return create(big().subtract(value.big, CONTEXT));
        } else {
            try {
                if (scale == value.scale) {
                    return create(Math.subtractExact(v, value.v), scale);
                } else if (scale < value.scale) {
                    return create(Math.subtractExact((long) (v * pow10(value.scale - scale)), value.v), value.scale);
                } else {
                    return create(Math.subtractExact(v, (long) (value.v * pow10(scale - value.scale))), scale);
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
    public Self multiply(Self value) {
        if (big != null) {
            return create(big.multiply(value.big(), CONTEXT));
        } else if (value.big != null) {
            return create(big().multiply(value.big, CONTEXT));
        } else {
            try {
                return create(Math.multiplyExact(v, value.v), scale + value.scale);
            } catch (ArithmeticException e) {
                return create(big().multiply(value.big()));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Self divide(Self value) {
        if (big != null) {
            return create(big.divide(value.big(), CONTEXT));
        } else if (value.big != null) {
            return create(big().divide(value.big, CONTEXT));
        } else {
            Self result = create((double) v / value.v);
            result.scale = scale - value.scale + result.scale;
            return result;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Self remainder(Self value) {
        if (big != null) {
            return create(big.remainder(value.big(), CONTEXT));
        } else if (value.big != null) {
            return create(big().remainder(value.big, CONTEXT));
        } else {
            if (scale == value.scale) {
                return create(v % value.v, scale);
            } else if (scale < value.scale) {
                return create((long) (v * pow10(value.scale - scale)) % value.v, value.scale);
            } else {
                return create(v % (long) (value.v * pow10(scale - value.scale)), scale);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Self o) {
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
    public Self decuple(int n) {
        if (big != null) {
            return create(big.scaleByPowerOfTen(n));
        } else {
            return create(v, scale - n);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Self pow(int n) {
        if (big != null) {
            return create(big.pow(n));
        } else {
            try {
                double result = Math.pow(v, n);
                DoubleMath.roundToLong(result, RoundingMode.HALF_DOWN);
                Self self = create(result);
                self.scale += scale * n;
                return self;
            } catch (ArithmeticException e) {
                return create(big().pow(n));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Self pow(double n) {
        if (big != null) {
            return create(BigDecimal.valueOf(Math.pow(big.doubleValue(), n)));
        } else {
            try {
                double result = Math.pow(v, n);
                DoubleMath.roundToLong(result, RoundingMode.HALF_DOWN);
                Self self = create(result);
                self.scale += scale * n;
                return self;
            } catch (ArithmeticException e) {
                return create(big()).pow(n);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Self sqrt() {
        if (big != null) {
            return create(big.sqrt(CONTEXT));
        } else {
            Self result = create(Math.sqrt(v));
            result.scale += scale / 2;
            return result;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Self abs() {
        if (big != null) {
            return create(big.abs());
        } else {
            return create(Math.abs(v), scale);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Self negate() {
        if (big != null) {
            return create(big.negate());
        } else {
            try {
                return create(Math.negateExact(v), scale);
            } catch (ArithmeticException e) {
                return create(big().negate());
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
    public Self scale(int size, RoundingMode mode) {
        if (big != null) {
            return create(big.setScale(size, mode));
        } else {
            if (scale == size) {
                return (Self) this;
            } else if (scale < size) {
                return (Self) this;
            } else {
                return create(DoubleMath.roundToLong(v * pow10(size - scale), mode), size);
            }
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
    public String toString() {
        if (big != null) {
            return big.stripTrailingZeros().toPlainString();
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
            return Objects.hash(v, scale);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Decimal == false) {
            return false;
        }

        Decimal other = (Decimal) obj;
        if (big != null) {
            if (other.big != null) {
                return big.compareTo(other.big) == 0;
            } else {
                return checkEqualityBetweenPrimitiveAndBig(other, this);
            }
        } else {
            if (other.big != null) {
                return checkEqualityBetweenPrimitiveAndBig(this, other);
            } else {
                return this.scale == other.scale && this.v == other.v;
            }
        }
    }

    /**
     * Test equality between the primive type and wrapped type.
     * 
     * @param primitive
     * @param big
     * @return
     */
    private boolean checkEqualityBetweenPrimitiveAndBig(Decimal<Self> primitive, Decimal<Self> big) {
        if (primitive.scale == 0) {
            return primitive.v == big.big.longValue();
        } else {
            return big.big.compareTo(primitive.big()) == 0;
        }
    }

    protected static int computeScale(double value) {
        for (int i = 0; i < 18; i++) {
            double fixer = pow10(i);
            double fixed = ((long) (value * fixer)) / fixer;
            if (DoubleMath.fuzzyEquals(value, fixed, 1e-12)) {
                return i;
            }
        }
        return 18;
    }
}
