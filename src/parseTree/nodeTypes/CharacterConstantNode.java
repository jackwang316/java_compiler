package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import tokens.CharacterLiteralToken;
import tokens.FloatingLiteralToken;
import tokens.IntegerLiteralToken;
import tokens.Token;

public class CharacterConstantNode extends ParseNode {
	public CharacterConstantNode(Token token) {
		super(token);
		assert(token instanceof CharacterLiteralToken);
	}
	public CharacterConstantNode(ParseNode node) {
		super(node);
	}

////////////////////////////////////////////////////////////
// attributes

	public int getValue() {
		return characterToken().getValue();
	}

	public CharacterLiteralToken characterToken() {
		return (CharacterLiteralToken)token;
	}

///////////////////////////////////////////////////////////
// accept a visitor

	public void accept(ParseNodeVisitor visitor) {
		visitor.visit(this);
	}

}
