package parseTree.nodeTypes;

import java.util.List;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import semanticAnalyzer.types.Array;
import semanticAnalyzer.types.Type;
import tokens.Token;

public class ArrayNode extends ParseNode{
    private boolean isDynamic;

    public ArrayNode(Token token) {
        super(token);
    }

    public ArrayNode(ParseNode node) {
        super(node);
    }

    public void setDynamic(boolean dynamic) {
        isDynamic = dynamic;
    }

    public void setSubtype(Type subtype) {
        super.setType(new Array(subtype));
    }

    public Type getSubtype() {
        return ((Array)super.getType()).getSubtype();
    }

    public boolean isDynamic() {
        return isDynamic;
    }

    public static ArrayNode dynamicMake(Token token, Type type, ParseNode size) {
        ArrayNode node = new ArrayNode(token);
        node.setType(type);
        node.appendChild(size);
        node.setDynamic(true);
        return node;
    }

    public static ArrayNode staticMake(Token token, List<ParseNode> expressions) {
        ArrayNode node = new ArrayNode(token);
        for(ParseNode expression : expressions) {
            node.appendChild(expression);
        }
        node.setDynamic(false);
        return node;
    }

    public void accept(ParseNodeVisitor visitor) {
        visitor.visitEnter(this);
        visitChildren(visitor);
        visitor.visitLeave(this);
    }
}
