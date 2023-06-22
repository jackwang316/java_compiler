package semanticAnalyzer.signatures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import semanticAnalyzer.types.PrimitiveType;
import semanticAnalyzer.types.Type;

public class PromotedSignature {
    FunctionSignature signature;
    List<Promotion> promotions;
    List<Type> typeVariableSettings;

    public PromotedSignature(FunctionSignature signature, List<Promotion> promotions) {
        this.signature = signature;
        this.promotions = new ArrayList<Promotion>(promotions);
        this.typeVariableSettings = signature.typeVariableSettings();
    }

    public static List<PromotedSignature> promotedSignature(FunctionSignatures signatures, List<Type> types) {
        List<PromotedSignature> results = new ArrayList<PromotedSignature>();

        for (FunctionSignature signature : signatures) {
            results.addAll(findAll(signature, types));
        }
        return results;
    }
    
    public int numPromotions() {
        int result = 0;
        for (Promotion promotion : promotions) {
            if (!promotion.isNull()) {
                result++;
            }
        }
        return result;
    }

    public Type resultType() {
        setTypeVariabales();
        return signature.resultType().concreteType();
    }

    private void setTypeVariabales() {
        signature.setTypeVariables(typeVariableSettings);
    }

    private static List<PromotedSignature> findAll(FunctionSignature signature, List<Type> types) {
        List<PromotedSignature> promotedSignatures = new ArrayList<PromotedSignature>();
        List<Promotion> promotions = new ArrayList<Promotion>();
        List<Type> promotedTypes = new ArrayList<Type>();
        for (int i = 0; i < types.size(); i++) {
            promotions.add(Promotion.NONE);
            promotedTypes.add(PrimitiveType.NO_TYPE);
        }
        findAllRecursive(signature, types, promotions, promotedTypes, promotedSignatures, 0);
        return promotedSignatures;
    }

    private static void findAllRecursive(FunctionSignature signature, List<Type> types, List<Promotion> promotions, List<Type> promotedTypes, List<PromotedSignature> promotedSignatures, 
            int index) {
        if (index >= types.size()) {
            if (signature.accepts(promotedTypes)) {
                promotedSignatures.add(new PromotedSignature(signature, promotions));
            }
            return;
        }
        Type type = types.get(index);
        // try diff promotion
        for (Promotion promotion : Promotion.values()) {
            if (promotion.appliesTo(type)) {
                promotedTypes.set(index, promotion.apply(type));
                findAllRecursive(signature, types, promotions, promotedTypes, promotedSignatures, index + 1); 
            }
        }
        return;
    }

    public static PromotedSignature nullInstance() {
        return null;
    }

    public boolean accepts(List<Type> childTypes) {
        setTypeVariabales();
        return signature.accepts(childTypes);
    }

    public Object getVariant() {
        return signature.getVariant();
    }

	public Promotion promotion(int i) {
		return promotions.get(i);
	}

    
}
