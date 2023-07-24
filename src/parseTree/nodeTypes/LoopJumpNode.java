package parseTree.nodeTypes;

import lexicalAnalyzer.Keyword;
import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import tokens.Token;

public class LoopJumpNode extends ParseNode{

    private String label;

    public LoopJumpNode(ParseNode node) {
        super(node);
    }
    public LoopJumpNode(Token token) {
        super(token);
    }
    public String getLabel() {
        return label;
    }
    public void setLabel(WhileStatementNode node) {
        if (token.isLextant(Keyword.BREAK)) {
            label = node.getEndLabel();
        } else if (token.isLextant(Keyword.CONTINUE)) {
            label = node.getStartLabel();
        } else {
            throw new IllegalArgumentException("Invalid loop jump node");
        }
    }

    ////////////////////////////////////////////////////////////
    public void accept(ParseNodeVisitor visitor) {
        visitor.visit(this);
    }

}