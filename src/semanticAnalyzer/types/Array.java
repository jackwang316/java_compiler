package semanticAnalyzer.types;

import java.util.Set;

public class Array implements Type {
	Type subtype;
	
	public Array(Type type) {
		this.subtype = type;
	}
	
	@Override
	public int getSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String infoString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean equivalent(Type otherType) { //check if otherType is array type and otherType's subtype is equivalent to subtype.
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addTypeVariables(Set<TypeVariable> typeVariables) { //Pass type to its subtype
		// TODO Auto-generated method stub

	}

	@Override
	public Type concreteType() {
		// TODO Auto-generated method stub
		return null;
	}

}
