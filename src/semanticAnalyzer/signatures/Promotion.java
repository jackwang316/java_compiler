package semanticAnalyzer.signatures;

import static semanticAnalyzer.types.PrimitiveType.*;

import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMOpcode;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import semanticAnalyzer.types.PrimitiveType;

import semanticAnalyzer.types.Type;

public enum Promotion {
    CHAR_TO_INT(CHARACTER, INTEGER, ASMOpcode.Nop),
    CHAR_TO_FLOAT(CHARACTER, FLOATING, ASMOpcode.ConvertF),
    INT_TO_FLOAT(INTEGER, FLOATING, ASMOpcode.ConvertF),
    NONE(NO_TYPE, NO_TYPE, ASMOpcode.Nop) {
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
    ASMOpcode opcode;

    Promotion(PrimitiveType fromType, PrimitiveType toType, ASMOpcode opcode) {
        this.fromType = fromType;
        this.toType = toType;
        this.opcode = opcode;
    }

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

    public ASMCodeFragment codeFor() {
        ASMCodeFragment result = new ASMCodeFragment(CodeType.GENERATES_VALUE);
        result.add(opcode);
        return result;
    }
}
