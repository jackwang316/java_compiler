package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import tokens.Token;

public class IndexNode extends ParseNode{

    public IndexNode(ParseNode node) {
        super(node);
    }

    public IndexNode(Token token) {
        super(token);    
    }

    public static IndexNode withChildren(ParseNode identifier, ParseNode index) {
		IndexNode node = new IndexNode(identifier.getToken());
        node.setType(identifier.getType());
        node.appendChild(identifier);
        node.appendChild(index);
		return node;
	}
    
    
	public void accept(ParseNodeVisitor visitor) {
		visitor.visitEnter(this);
		visitChildren(visitor);
		visitor.visitLeave(this);
	}
}
