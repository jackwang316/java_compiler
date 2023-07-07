package semanticAnalyzer.types;

import java.util.Set;

public class Array implements Type {
	Type subtype;

	public Array() {
		
	}

	public Array (Type type) {
		this.subtype = type;
	}

	@Override
	public int getSize() {
		return 0;
	}

	@Override
	public String infoString() {
		return "[" + subtype.infoString() + "]";
	}

    @Override
    public boolean equivalent(Type other) {
		if(other instanceof Array) {
			Array arr = (Array) other;
			return subtype.equivalent(arr.getSubtype());
		}
		return false;
	}

	@Override
	public void addTypeVariables(Set<TypeVariable> typeVariables) {
		return;

	}

	@Override
	public Type concreteType() {
		return new Array(subtype.concreteType());
	}

    public void setSubtype(Type type) {
		this.subtype = type;
	}
	public Type getSubtype() {
		return subtype;
	}

}