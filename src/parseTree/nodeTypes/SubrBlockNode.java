package parseTree.nodeTypes;

import java.util.List;

import asmCodeGenerator.Labeller;
import lexicalAnalyzer.Lextant;
import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import semanticAnalyzer.signatures.FunctionSignature;
import semanticAnalyzer.types.ReturnType;
import semanticAnalyzer.types.Type;
import tokens.LextantToken;
import tokens.Token;

public class SubrBlockNode extends ParseNode{
    private FunctionSignature signature;
	private String startLabel;
	private String exitHandshakeLabel;
	private String exitErrorLabel;

    public SubrBlockNode(Token token) {
        super(token);
    }

    public SubrBlockNode(ParseNode node) {
        super(node);
        initChildren();
    }

    public Type getReturnType() {
		return this.getReturnType();
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

	public void generateLabels() {
		Labeller labeller;

		if (this.getParent() instanceof SubrDefinitionNode) {
			 labeller = new Labeller("function");				
		} else {
			 labeller = new Labeller("lambda");
		}

		// Generate labels
		this.startLabel = labeller.getPrefix();
		this.exitHandshakeLabel = labeller.newLabel("exit-handshake");
		this.exitErrorLabel = labeller.newLabel("exit-error");
	}
	public String getStartLabel() {
		return this.startLabel;
	}
	public String getExitHandshakeLabel() {
		return this.exitHandshakeLabel;
	}
	public String getExitErrorLabel() {
		return this.exitErrorLabel;
	}

	public int getFrameSize() {
		int size = 8;		

		List<ParseNode> localVars = this.child(1).getChildren();
		for (ParseNode child : localVars) {
			size += child.getType().getSize();
		}

		return size;
	}

	public int getArgSize() {
		int size = 0;

		List<ParseNode> paramArgs = this.child(0).getChildren();
		for (ParseNode child : paramArgs) {
			size += child.getType().getSize();
		}

		return size;
	}
}
