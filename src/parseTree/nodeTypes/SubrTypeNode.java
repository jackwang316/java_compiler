package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import semanticAnalyzer.types.ReturnType;
import semanticAnalyzer.types.Type;
import tokens.Token;

public class SubrTypeNode extends ParseNode{
	public SubrTypeNode(Token token) {
		super(token);
		this.setType(new ReturnType());
	}

	public SubrTypeNode(ParseNode node) {
		super(node);
		initChildren();
	}

	public Type returnType() {
		return ((ReturnType)this.getType()).getReturnType();
	}
	public void setReturnType(Type type) {
		((ReturnType)this.getType()).setReturnType(type);
	}
	public void addChildType(Type type) {
		((ReturnType)this.getType()).addType(type);
	}

	public void accept(ParseNodeVisitor visitor) {
		visitor.visitEnter(this);
		visitChildren(visitor);
		visitor.visitLeave(this);
	}
}
