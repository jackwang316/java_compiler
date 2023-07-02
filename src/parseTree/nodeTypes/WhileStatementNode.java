package parseTree.nodeTypes;

import lexicalAnalyzer.Keyword;
import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import tokens.Token;

public class WhileStatementNode extends ParseNode{
    public WhileStatementNode(Token token) {
        super(token);
        assert(token.isLextant(Keyword.WHILE));
    }
    public WhileStatementNode(ParseNode node) {
        super(node);
    }
    public static WhileStatementNode withChildren(Token token, ParseNode expression, ParseNode block) {
        WhileStatementNode node = new WhileStatementNode(token);
        node.appendChild(expression);
        node.appendChild(block);
        return node;
    }
    public void accept(ParseNodeVisitor visitor) {
        visitor.visitEnter(this);
        visitChildren(visitor);
        visitor.visitLeave(this);
    }
}
