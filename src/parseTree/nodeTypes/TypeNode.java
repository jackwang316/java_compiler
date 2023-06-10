package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import semanticAnalyzer.signatures.FunctionSignature;
import semanticAnalyzer.types.Type;
import lexicalAnalyzer.Keyword;
import lexicalAnalyzer.Lextant;
import tokens.LextantToken;
import tokens.Token;

public class TypeNode extends ParseNode {
	private FunctionSignature signature = null;

	public TypeNode(Token token) {
		super(token);
	}

	public TypeNode(ParseNode node) {
		super(node);
	}
	
	
	////////////////////////////////////////////////////////////
	// attributes
	
	public Lextant getDeclarationType() {
		return lextantToken().getLextant();
	}
	public LextantToken lextantToken() {
		return (LextantToken)token;
	}	
	
	public FunctionSignature getSignature() {
		return this.signature;
	}
	
	public void setSignature(FunctionSignature signature) {
		this.signature = signature;
		this.setType(signature.resultType());
	}
	
	
	////////////////////////////////////////////////////////////
	// convenience factory
	
	public static TypeNode withChildren(ParseNode expression, Type type) {
		TypeNode node = new TypeNode(expression.getToken());
		node.appendChild(expression);
		node.setType(type);
		return node;
	}
	
	
	///////////////////////////////////////////////////////////
	// boilerplate for visitors
			
	public void accept(ParseNodeVisitor visitor) {
		visitor.visitEnter(this);
		visitChildren(visitor);
		visitor.visitLeave(this);
	}
}