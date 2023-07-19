package parseTree.nodeTypes;

import lexicalAnalyzer.Lextant;
import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import semanticAnalyzer.signatures.FunctionSignature;
import tokens.LextantToken;
import tokens.Token;

public class SubrBlockNode extends ParseNode{
    private FunctionSignature signature;

    public SubrBlockNode(Token token) {
        super(token);
    }

    public SubrBlockNode(ParseNode node) {
        super(node);
        initChildren();
    }

    public Lextant getReturnType() {
		return lextantToken().getLextant();
	}

	public LextantToken lextantToken() {
		return (LextantToken)token;
	}

    public void setSignature(FunctionSignature signature) {
		this.signature = signature;
	}

	public FunctionSignature getSignature() {
		return signature;
	}

	public static SubrBlockNode withChildren(Token token, ParseNode types, ParseNode definitionBlock) {
		SubrBlockNode node = new SubrBlockNode(token);
		node.appendChild(types);
		node.appendChild(definitionBlock);
		return node;
	}

    public void accept(ParseNodeVisitor visitor) {
		visitor.visitEnter(this);
		visitChildren(visitor);
		visitor.visitLeave(this);
	}
}
