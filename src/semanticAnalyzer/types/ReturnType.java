package semanticAnalyzer.types;

import java.util.ArrayList;
import java.util.Set;

import asmCodeGenerator.codeStorage.ASMOpcode;
import semanticAnalyzer.signatures.FunctionSignature;

public class ReturnType implements Type {	
	private String infoString;
	private ArrayList<Type> typeList;
	private Type returnType;
	private int sizeInBytes = 8;
	
	public ReturnType() {
		returnType = PrimitiveType.parseType("void");
		typeList = new ArrayList<Type>();
	}
	public ReturnType(String infoString) {
		this.infoString = infoString;
	}
	public int getSize() {
		return sizeInBytes;
	}
	
// ACCESSORS
	
	public FunctionSignature getSignature() {
		return new FunctionSignature(ASMOpcode.Nop, typeList, returnType);
	}
	
	public void setReturnType(Type type) {
		this.returnType = type;
		updateInfoString();
	}
	public Type getReturnType() {
		return returnType;
	}
	
	public ArrayList<Type> getTypeList() {
		return typeList;
	}
	public void addType(Type type) {
		typeList.add(type);
		updateInfoString();
	}
	
	public String infoString() {
		return infoString;
	}
	private void updateInfoString() {
		ArrayList<String> types = new ArrayList<String>();
		typeList.forEach((type) -> types.add(type.infoString()));
		
		String typeString = types.toString();
		typeString = typeString.replace('[', '<');
		typeString = typeString.replace(']', '>');
		
		this.infoString = typeString + " -> " + returnType.infoString();
	}
	
// HELPER FUNCTIONS
	
	@Override
	public boolean equals(Object type) {
		if (!(type instanceof ReturnType)) {
			return false;
		}
		
		ReturnType compareType = (ReturnType) type;
		if (this.typeList.size() != compareType.typeList.size()) {
			return false;
		}

		for (int i = 0; i < this.typeList.size(); i++) {
			Type t1 = this.typeList.get(i);
			Type t2 = compareType.typeList.get(i);
			
			if (!t1.equals(t2)) {
				return false;
			}
		}

		return this.returnType.equals(compareType.returnType);
	}
    @Override
    public boolean equivalent(Type otherType) {
        return this.returnType.equals(otherType);
    }
    @Override
    public void addTypeVariables(Set<TypeVariable> typeVariables) {
    }

    @Override
    public Type concreteType() {
        return returnType;
    }

}
