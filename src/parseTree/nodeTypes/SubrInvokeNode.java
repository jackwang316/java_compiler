package parseTree.nodeTypes;

import logging.TanLogger;
import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import symbolTable.Binding;
import symbolTable.Scope;
import tokens.IdentifierToken;
import tokens.Token;

public class SubrInvokeNode extends ParseNode{
    private Binding binding;
    private Scope declarationScope;

    public SubrInvokeNode(Token token) {
        super(token);
        assert(token instanceof IdentifierToken);
        this.binding = null;
    }

    public SubrInvokeNode(ParseNode node) {
        super(node);
        this.binding = null;
        if(node instanceof SubrInvokeNode) {
            this.binding = ((SubrInvokeNode) node).binding;
        } 
        initChildren();
    }

    public IdentifierToken identifierToken() {
		return (IdentifierToken)token;
	}

	public void setBinding(Binding binding) {
		this.binding = binding;
	}
	public Binding getBinding() {
		return binding;
	}

	public Boolean isConstant() {
        if(binding != null){
            return binding.isConstant();
        }
        return null;
	}

    public static SubrInvokeNode withChildren(Token token, ParseNode index) {
		SubrInvokeNode node = new SubrInvokeNode(token);
		node.appendChild(index);
		return node;
	}

    public Binding getVariableBinding() {
		String identifier = token.getLexeme();

		for(ParseNode current : pathToRoot()) {
			if(current.containsBindingOf(identifier)) {
				this.declarationScope = current.getScope();
				return current.bindingOf(identifier);
			}
		}
		useBeforeDefineError();
		return Binding.nullInstance();
	}

    public void useBeforeDefineError() {
		TanLogger log = TanLogger.getLogger("compiler.semanticAnalyzer.identifierNode");
		log.severe("identifier " + getToken().getLexeme() + " used before defined at " + getToken().getLocation());
	}

    public Scope getDeclarationScope() {
		getVariableBinding();
		return declarationScope;
	}

    public void accept(ParseNodeVisitor visitor) {
	    visitor.visitEnter(this);
		visitChildren(visitor);
		visitor.visitLeave(this);
	}
}

