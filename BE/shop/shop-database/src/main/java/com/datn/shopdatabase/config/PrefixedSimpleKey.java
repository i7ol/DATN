package com.datn.shopdatabase.config;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Arrays;

public class PrefixedSimpleKey implements Serializable {
    private final String prefix;
    private final Object[] params;
    private final String methodName;
    private final int hashCode;

    public PrefixedSimpleKey(String prefix, String methodName, Object... elements) {
        Assert.notNull(prefix, "Prefix must not be null");
        Assert.notNull(elements, "Elements must not be null");
        this.prefix = prefix;
        this.methodName = methodName;
        this.params = Arrays.copyOf(elements, elements.length);
        int hc = prefix.hashCode();
        hc = 31 * hc + methodName.hashCode();
        hc = 31 * hc + Arrays.deepHashCode(this.params);
        this.hashCode = hc;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof PrefixedSimpleKey that &&
                this.prefix.equals(that.prefix) &&
                this.methodName.equals(that.methodName) &&
                Arrays.deepEquals(this.params, that.params));
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public String toString() {
        return this.prefix + " " + this.getClass().getSimpleName() + this.methodName +
                " [" + StringUtils.arrayToCommaDelimitedString(this.params) + "]";
    }
}
