package parseTree.nodeTypes;

import lexicalAnalyzer.Lextant;
import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import tokens.LextantToken;
import tokens.Token;

public class ReturnNode extends ParseNode {
    public ReturnNode(Token token) {
		super(token);
		assert(token instanceof LextantToken);
	}

	public ReturnNode(ParseNode node) {
		super(node);
	}

    public static ReturnNode withChild(Token token, ParseNode child) {
		ReturnNode node = new ReturnNode(token);
		node.appendChild(child);
		return node;
	}

    public Lextant getOperator() {
		return lextantToken().getLextant();
	}
	public LextantToken lextantToken() {
		return (LextantToken)token;
	}

    public ParseNode getLambda() {
		ParseNode parent = this.getParent();
		
		while (parent != null && !(parent instanceof SubrBlockNode)) {
			parent = parent.getParent();
		}
		
		assert parent instanceof SubrBlockNode;
		
		return parent;
	}

	
	public void accept(ParseNodeVisitor visitor) {
		visitor.visitEnter(this);
		visitChildren(visitor);
		visitor.visitLeave(this);
	}
}
