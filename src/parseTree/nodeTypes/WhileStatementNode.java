package parseTree.nodeTypes;

import asmCodeGenerator.Labeller;
import lexicalAnalyzer.Keyword;
import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import tokens.Token;

public class WhileStatementNode extends ParseNode{
    private String startLabel, endLabel;

    public WhileStatementNode(Token token) {
        super(token);
        assert(token.isLextant(Keyword.WHILE));
        Labeller labeller = new Labeller("while");
        startLabel = labeller.newLabel("start");
        endLabel = labeller.newLabel("end");
    }
    public WhileStatementNode(ParseNode node) {
        super(node);
        assert(node instanceof WhileStatementNode);
        WhileStatementNode whileNode = (WhileStatementNode) node;
        this.startLabel = whileNode.startLabel;
        this.endLabel = whileNode.endLabel;
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
    public String getStartLabel() {
        return startLabel;
    }
    public String getEndLabel() {
        return endLabel;
    }
}
