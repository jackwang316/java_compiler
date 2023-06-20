package semanticAnalyzer.types;

import java.util.List;
import java.util.Set;

import lexicalAnalyzer.Keyword;

public enum PrimitiveType implements Type {
	BOOLEAN(1),
	CHARACTER(1),
	INTEGER(4),
	STRING(4),
	FLOATING(8),
	ERROR(0),			// use as a value when a syntax error has occurred
	NO_TYPE(0, "");		// use as a value when no type has been assigned.
	
	private int sizeInBytes;
	private String infoString;
	
	private PrimitiveType(int size) {
		this.sizeInBytes = size;
		this.infoString = toString();
	}
	private PrimitiveType(int size, String infoString) {
		this.sizeInBytes = size;
		this.infoString = infoString;
	}
	public int getSize() {
		return sizeInBytes;
	}
	public String infoString() {
		return infoString;
	}
	
	public static Type parseType(String type) {
		if(type.equals(Keyword.FLOAT.getLexeme())) {
			return PrimitiveType.FLOATING;
		}
		else if(type.equals(Keyword.INT.getLexeme())) {
			return PrimitiveType.INTEGER;
		}
		else if(type.equals(Keyword.CHAR.getLexeme())) {
			return PrimitiveType.CHARACTER;
		}
		else if(type.equals(Keyword.STRING.getLexeme())) {
			return PrimitiveType.STRING;
		}
		else if(type.equals(Keyword.BOOL.getLexeme())) {
			return PrimitiveType.BOOLEAN;
		}
		else {
			return PrimitiveType.ERROR;
		}
	}
	@Override
	public boolean equivalent(Type otherType) {
		return this==otherType;
	}
	@Override
	public void addTypeVariables(Set<TypeVariable> typeVariables) {
		return;
	}
	@Override
	public Type concreteType() {
		return this;
	}
}
