package org.eclipse.ditto.wodt.common;

import java.util.Optional;

/*
 * Class representing a Thing Model field.
 */
public class ThingModelElement {
    private final String field;
    private final Optional<String> feature;
    private final Optional<String> type;
    private final Optional<String> domainPredicate;

    public ThingModelElement(String field, Optional<String> feature, Optional<String> type, Optional<String> domainPredicate) {
        this.field = field;
        this.feature = (feature.isPresent() && !feature.get().isEmpty()) ? feature : Optional.empty();
        this.type = (type.isPresent() && !type.get().isEmpty()) ? type : Optional.empty();
        this.domainPredicate = (domainPredicate.isPresent() && !domainPredicate.get().isEmpty()) ? domainPredicate : Optional.empty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ThingModelElement that = (ThingModelElement) o;
        
        if (!field.equals(that.field)) return false;
        if (!feature.equals(that.feature)) return false;
        if (!type.equals(that.type)) return false;
        return domainPredicate.equals(that.domainPredicate);
    }

    @Override
    public int hashCode() {
        int result = field.hashCode();
        result = 31 * result + feature.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + domainPredicate.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ThingModelElement{" +
                "field='" + field + '\'' +
                ", feature=" + feature +
                ", type=" + type +
                ", domainPredicate=" + domainPredicate +
                '}';
    }

    public String getField() {
        return this.field;
    }

    public Optional<String> getFeature() {
        return this.feature;
    }

    public Optional<String> getType() {
        return this.type;
    }

    public Optional<String> getDomainPredicate() {
        return this.domainPredicate;
    }
}