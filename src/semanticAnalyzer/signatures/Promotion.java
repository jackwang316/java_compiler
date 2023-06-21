package semanticAnalyzer.signatures;

import static semanticAnalyzer.types.PrimitiveType.*;

import semanticAnalyzer.types.PrimitiveType;

import semanticAnalyzer.types.Type;

public enum Promotion {
    CHAR_TO_INT(CHARACTER, INTEGER),
    CHAR_TO_FLOAT(CHARACTER, FLOATING),
    INT_TO_FLOAT(INTEGER, FLOATING),
    NONE(NO_TYPE, NO_TYPE) {
        boolean appliesTo(Type type) {
            return true;
        }

        Type apply(Type type) {
            return type;
        }
        boolean isNull() {
            return true;
        }
    };

    Type fromType;
    Type toType;

    Promotion(PrimitiveType fromType, PrimitiveType toType) {
        this.fromType = fromType;
        this.toType = toType;
    }

    boolean appliesTo(Type type) {
        return fromType == type;
    }

    Type apply(Type type) {
        assert (appliesTo(type));
        return toType;
    }

    boolean isNull() {
        return false;
    }
}
