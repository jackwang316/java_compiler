package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import asmCodeGenerator.Labeller;
import lexicalAnalyzer.Keyword;
import tokens.Token;

public class ForStatementNode extends ParseNode{
    private String startLabel, endLabel;

    public ForStatementNode(Token token) {
        super(token);
        assert(token.isLextant(Keyword.FOR));
        Labeller labeller = new Labeller("for");
        startLabel = labeller.newLabel("start");
        endLabel = labeller.newLabel("end");
    }
    public ForStatementNode(ParseNode node) {
        super(node);
        assert(node instanceof ForStatementNode);
        ForStatementNode forNode = (ForStatementNode) node;
        this.startLabel = forNode.startLabel;
        this.endLabel = forNode.endLabel;
    }
    public static ForStatementNode withChildren(Token token, ParseNode identifier, ParseNode startIndex, ParseNode endIndex, ParseNode block) {
        ForStatementNode node = new ForStatementNode(token);
        node.appendChild(identifier);
        node.appendChild(startIndex);
        node.appendChild(endIndex);
        node.appendChild(block);
        return node;
    }

    public void accept(ParseNodeVisitor visitor) {
        visitor.visitEnter(this);
        // System.out.println("entering for statement node");
        // visitChildren(visitor);
        int n = nChildren();
		for(int i = 0;i < n; i++) {
			child(i).accept(visitor);
            // System.out.println("child: " + child(i));
		}
        // System.out.println("leaving for statement node");
        visitor.visitLeave(this);
    }
    public String getStartLabel() {
        return startLabel;
    }
    public String getEndLabel() {
        return endLabel;
    }
}
