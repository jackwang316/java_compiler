package semanticAnalyzer.types;

import java.util.List;
import java.util.Set;

public class TypeVariable implements Type{
    String name;
    Type constraint;

    public TypeVariable(String name) {
        this.name = name;
        reset();
    }

    public void reset() {
        constraint = PrimitiveType.NO_TYPE;
    }

    // Avoid confilct with .equals()
    public boolean equivalent(Type otherType) {
        if (constraint == PrimitiveType.NO_TYPE) {
            setConstraint(otherType);
            return true;
        }
        return constraint.equivalent(otherType);
    }

    public Type getConstraint() {
        return constraint;
    }
    public void setConstraint(Type constraint) {
        this.constraint = constraint;
    }

    @Override
    public int getSize() {
        return 0;
    }
    @Override
    public String infoString() {
        return toString();
    }

    @Override
    public String toString() {
        return "<"  + name + ">";
    }

    @Override
    public void addTypeVariables(Set<TypeVariable> typeVariables) {
        typeVariables.add(this);
        
    }

    @Override
    public Type concreteType() {
        return constraint.concreteType();
    }
}