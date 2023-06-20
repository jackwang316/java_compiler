package semanticAnalyzer.signatures;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import semanticAnalyzer.types.PrimitiveType;
import semanticAnalyzer.types.Type;

public class PromotedSignature {
	FunctionSignature signature;
	List<Promotion> promotions;
	
	List<Type> typeVariableSettings;
	
	public PromotedSignature(FunctionSignature signature, List<Promotion> promotions) {
		this.signature = signature;
		this.promotions = new ArrayList<Promotion>();
		this.typeVariableSettings = signature.typeVariableSettings();
	}
	
	static List<PromotedSignature> promotedSignature(FunctionSignatures signatures, List<Type> types) {
		List<PromotedSignature> result = new ArrayList<PromotedSignature>();
		for(FunctionSignature signature: signatures) {
			result.addAll(findAll(signature, types));
		}
		return result;
	}
	
	public int numPromotions() {
		int result = 0;
		for(Promotion promotion: promotions) {
			if(!promotion.isNull()) {
				result = result + 1;
			}
		}
		return result;
	}

	private static List<PromotedSignature> findAll(FunctionSignature signature, List<Type> types) {
		List<PromotedSignature> promotedSignatures = new ArrayList<>();
		List<Promotion> promotions = new ArrayList<>();
		List<Type> promotedTypes = new ArrayList<>();
		for(int i = 0; i < types.size(); i++) {
			promotions.add(Promotion.NONE);
			promotedTypes.add(PrimitiveType.NO_TYPE);
		}
		
		findAllRecursive(signature, types, promotions, promotedSignatures, promotedTypes, 0);
		return promotedSignatures;
	}

	private static void findAllRecursive(FunctionSignature signature, List<Type> types, List<Promotion> promotions,
			List<PromotedSignature> promotedSignatures, List<Type> promotedTypes, int index) {
		if(index >= types.size()) {
			if(signature.accepts(promotedTypes)) {
				promotedSignatures.add(new PromotedSignature(signature, promotions));
			}
			return;
		}
		Type type = types.get(index);
		for(Promotion promotion: Promotion.values()) {
			if(promotion.appliesTo(type)) {
				promotedTypes.set(index, promotion.apply(type));
			}
			findAllRecursive(signature, types, promotions, promotedSignatures, promotedTypes, index + 1);
		}
		return;
	}
}
