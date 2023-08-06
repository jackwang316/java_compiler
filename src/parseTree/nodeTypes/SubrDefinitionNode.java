package parseTree.nodeTypes;

import lexicalAnalyzer.Keyword;
import lexicalAnalyzer.Lextant;
import logging.TanLogger;
import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import semanticAnalyzer.signatures.FunctionSignature;
import symbolTable.Binding;
import symbolTable.Scope;
import tokens.LextantToken;
import tokens.Token;

public class SubrDefinitionNode extends ParseNode{
    private FunctionSignature signature;
    private Binding binding;
    private Scope parameterScope;
    private String label;

    public SubrDefinitionNode(Token token) {
        super(token);
        assert(token.isLextant(Keyword.SUBR));
        this.binding = null;
	  }

    public SubrDefinitionNode(ParseNode node) {
        super(node);
        this.binding = null;
        if(node instanceof SubrDefinitionNode) {
            this.binding = ((SubrDefinitionNode) node).binding;
        } 
        initChildren();
    }

    public void setBinding(Binding binding) {
		    this.binding = binding;
	  }
	  public Binding getBinding() {
		    return binding;
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

    public static SubrDefinitionNode withChildren(Token token, ParseNode identifier, ParseNode definition) {
        SubrDefinitionNode node = new SubrDefinitionNode(token);
        node.appendChild(identifier);
        node.appendChild(definition);
        return node;
    }

    public Binding findVariableBinding() {
        String identifier = token.getLexeme();
  
        for(ParseNode current : pathToRoot()) {
          if(current.containsBindingOf(identifier)) {
            parameterScope = current.getScope();
            return current.bindingOf(identifier);
          }
        }
        undefinedError();
        return Binding.nullInstance();
    }

    public void setLabel(String label) {
		this.label = label;
	}
	public String getLabel() {
		return this.label;
	}

    public Scope getParameterScope() {
        findVariableBinding();
        return parameterScope;
    }

    public void undefinedError() {
        TanLogger log = TanLogger.getLogger("compiler.semanticAnalyzer.subrDefinitionNode");
        log.severe("subroutine " + getToken().getLexeme() + " used before defined at " + getToken().getLocation());
    }
  
    public void accept(ParseNodeVisitor visitor) {
        visitor.visitEnter(this);
        visitChildren(visitor);
        visitor.visitLeave(this);
	  }
  

}
