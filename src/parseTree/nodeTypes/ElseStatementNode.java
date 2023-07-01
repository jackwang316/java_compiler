package parseTree.nodeTypes;

import lexicalAnalyzer.Keyword;
import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import tokens.Token;

public class ElseStatementNode extends ParseNode{

    public ElseStatementNode(Token token) {
        super(token);
        assert(token.isLextant(Keyword.ELSE));
    }

    public ElseStatementNode(ParseNode node) {
        super(node);
    }

    public void accept(ParseNodeVisitor visitor) {
        visitor.visitEnter(this);
        visitChildren(visitor);
        visitor.visitLeave(this);
    }

}
