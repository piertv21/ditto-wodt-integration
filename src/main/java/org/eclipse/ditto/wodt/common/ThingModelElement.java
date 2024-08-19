package org.eclipse.ditto.wodt.common;

import java.util.Optional;

/*
 * Class representing a Thing Model element (property, action or event)
 */
public class ThingModelElement {
    public final String name;
    public final Optional<String> feature;
    public final boolean isComplex;

    public ThingModelElement(String name, Optional<String> feature, boolean isComplex) {
        this.name = name;
        this.feature = feature;
        this.isComplex = isComplex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ThingModelElement that = (ThingModelElement) o;

        if (isComplex != that.isComplex) return false;
        if (!name.equals(that.name)) return false;
        return feature.equals(that.feature);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + feature.hashCode();
        result = 31 * result + (isComplex ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ModelElement{" +
                "name='" + name + '\'' +
                ", feature='" + feature + '\'' +
                ", isComplex=" + isComplex +
                '}';
    }
}