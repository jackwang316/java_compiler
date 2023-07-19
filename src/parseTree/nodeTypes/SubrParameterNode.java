package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import semanticAnalyzer.types.Type;
import tokens.Token;

public class SubrParameterNode extends ParseNode{
    public SubrParameterNode(ParseNode node) {
        super(node);
    }

    public SubrParameterNode(Token token) {
        super(token);
    }

	public static SubrParameterNode withChildren(Token token, Type type, ParseNode identifier) {
		SubrParameterNode node = new SubrParameterNode(token);
		node.setType(type);
		node.appendChild(identifier);
		return node;
	}

    public static SubrParameterNode withChildren(Token token, Type type, ParseNode identifier, ParseNode child) {
		SubrParameterNode node = new SubrParameterNode(token);
		node.setType(type);
		node.appendChild(identifier);
		node.appendChild(child);
		return node;
	}

    public void accept(ParseNodeVisitor visitor) {
		visitor.visitEnter(this);
		visitChildren(visitor);
		visitor.visitLeave(this);
	}
}
