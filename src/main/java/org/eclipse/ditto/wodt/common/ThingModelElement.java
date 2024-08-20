package org.eclipse.ditto.wodt.common;

import java.util.Optional;

/*
 * Class representing a Thing Model element (context extension, property, action or event)
 */
public class ThingModelElement {
    public final String element;
    public final Optional<String> value;
    public final boolean isComplex;

    public ThingModelElement(String element, Optional<String> value, boolean isComplex) {
        this.element = element;
        this.value = value;
        this.isComplex = isComplex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ThingModelElement that = (ThingModelElement) o;

        if (isComplex != that.isComplex) return false;
        if (!element.equals(that.element)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        int result = element.hashCode();
        result = 31 * result + value.hashCode();
        result = 31 * result + (isComplex ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ModelElement{" +
                "element='" + element + '\'' +
                ", value='" + value + '\'' +
                ", isComplex=" + isComplex +
                '}';
    }
}