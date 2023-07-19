package parseTree.nodeTypes;

import lexicalAnalyzer.Lextant;
import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import tokens.LextantToken;
import tokens.Token;

public class SubrCallNode extends ParseNode{
    public SubrCallNode(Token token) {
        super(token);
    }

    public SubrCallNode(ParseNode node) {
        super(node);
    }

    public Lextant getOperator() {
		return lextantToken().getLextant();
	}
	public LextantToken lextantToken() {
		return (LextantToken)token;
	}

    public static SubrCallNode withChild(Token token, ParseNode child) {
		SubrCallNode node = new SubrCallNode(token);
		node.appendChild(child);
		return node;
	}

    public void accept(ParseNodeVisitor visitor) {
		visitor.visitEnter(this);
		visitChildren(visitor);
		visitor.visitLeave(this);
	}
}
