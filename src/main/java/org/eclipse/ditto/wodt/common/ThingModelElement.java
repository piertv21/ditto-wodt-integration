package org.eclipse.ditto.wodt.common;

import java.util.Optional;

/*
 * Class representing a Thing Model element.
 */
public class ThingModelElement {
    private final String element;
    private final Optional<String> value;
    private final Optional<String> additionalData;

    public ThingModelElement(String element, Optional<String> value, Optional<String> additionalData) {
        this.element = element;
        this.value = (value.isPresent() && !value.get().isEmpty()) ? value : Optional.empty();
        this.additionalData = (additionalData.isPresent() && !additionalData.get().isEmpty()) ? additionalData : Optional.empty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ThingModelElement that = (ThingModelElement) o;
        
        if (!element.equals(that.element)) return false;
        if (!value.equals(that.value)) return false;
        return additionalData.equals(that.additionalData);
    }

    @Override
    public int hashCode() {
        int result = element.hashCode();
        result = 31 * result + value.hashCode();
        result = 31 * result + additionalData.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ThingModelElement{" +
                "element='" + element + '\'' +
                ", value='" + value + '\'' +
                ", additionalData=" + additionalData +
                '}';
    }
    
    public String getElement() {
        return this.element;
    }

    public Optional<String> getValue() {
        return this.value;
    }

    public Optional<String> getAdditionalData() {
        return this.additionalData;
    }
}