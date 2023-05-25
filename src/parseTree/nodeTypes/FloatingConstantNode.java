package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import tokens.FloatingLiteralToken;
import tokens.IntegerLiteralToken;
import tokens.Token;

public class FloatingConstantNode extends ParseNode {
	public FloatingConstantNode(Token token) {
		super(token);
		assert(token instanceof FloatingLiteralToken);
	}
	public FloatingConstantNode(ParseNode node) {
		super(node);
	}

////////////////////////////////////////////////////////////
// attributes
	
	public double getValue() {
		return numberToken().getValue();
	}

	public FloatingLiteralToken numberToken() {
		return (FloatingLiteralToken)token;
	}	

///////////////////////////////////////////////////////////
// accept a visitor
	
	public void accept(ParseNodeVisitor visitor) {
		visitor.visit(this);
	}

}
