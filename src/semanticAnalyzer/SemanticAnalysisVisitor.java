package semanticAnalyzer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lexicalAnalyzer.Keyword;
import lexicalAnalyzer.Lextant;
import java.util.ArrayList;
import lexicalAnalyzer.Punctuator;
import logging.TanLogger;
import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import parseTree.nodeTypes.ArrayNode;
import parseTree.nodeTypes.AssignmentStatementNode;
import parseTree.nodeTypes.BlockStatementNode;
import parseTree.nodeTypes.BooleanConstantNode;
import parseTree.nodeTypes.CharacterConstantNode;
import parseTree.nodeTypes.DeclarationNode;
import parseTree.nodeTypes.ErrorNode;
import parseTree.nodeTypes.FloatingConstantNode;
import parseTree.nodeTypes.IdentifierNode;
import parseTree.nodeTypes.IndexNode;
import parseTree.nodeTypes.IntegerConstantNode;
import parseTree.nodeTypes.NewlineNode;
import parseTree.nodeTypes.OperatorNode;
import parseTree.nodeTypes.PrintStatementNode;
import parseTree.nodeTypes.ProgramNode;
import parseTree.nodeTypes.ReturnNode;
import parseTree.nodeTypes.SpaceNode;
import parseTree.nodeTypes.TabSpaceNode;
import parseTree.nodeTypes.TypeNode;
import parseTree.nodeTypes.StringConstantNode;
import parseTree.nodeTypes.SubrBlockNode;
import parseTree.nodeTypes.SubrCallNode;
import parseTree.nodeTypes.SubrDefinitionNode;
import parseTree.nodeTypes.SubrInvokeNode;
import parseTree.nodeTypes.SubrParameterNode;
import semanticAnalyzer.signatures.FunctionSignature;
import semanticAnalyzer.signatures.FunctionSignatures;
import semanticAnalyzer.types.Array;
import semanticAnalyzer.types.PrimitiveType;
import semanticAnalyzer.types.Type;
import symbolTable.Binding;
import symbolTable.Binding.Constancy;
import symbolTable.Scope;
import tokens.LextantToken;
import tokens.Token;

class SemanticAnalysisVisitor extends ParseNodeVisitor.Default {
	@Override
	public void visitLeave(ParseNode node) {
		throw new RuntimeException("Node class unimplemented in SemanticAnalysisVisitor: " + node.getClass());
	}
	
	///////////////////////////////////////////////////////////////////////////
	// constructs larger than statements
	@Override
	public void visitEnter(ProgramNode node) {
		enterScope(node);
	}
	public void visitLeave(ProgramNode node) {
		leaveScope(node);
	}
	public void visitEnter(BlockStatementNode node) {
		if (node.getParent() instanceof SubrBlockNode) {
			createProcedureScope(node);
		} else {
			createSubscope(node);
		}
		
		enterScope(node);
	}
	public void visitLeave(BlockStatementNode node) {
		leaveScope(node);
	}
	
	///////////////////////////////////////////////////////////////////////////
	// helper methods for scoping.
	private void enterProgramScope(ParseNode node) {
		Scope scope = Scope.createProgramScope();
		node.setScope(scope);
	}	

	private void createProcedureScope(ParseNode node) {
		Scope baseScope = node.getLocalScope();
		Scope scope = baseScope.createProcedureScope();
		node.setScope(scope);
	}

	private void createSubscope(ParseNode node) {
		Scope baseScope = node.getLocalScope();
		Scope scope = baseScope.createSubscope();
		node.setScope(scope);
	}

	private void enterSubscope(ParseNode node) {
		Scope baseScope = node.getLocalScope();
		Scope scope = baseScope.createSubscope();
		node.setScope(scope);
	}		
	private void leaveScope(ParseNode node) {
		node.getScope().leave();
	}

	private void enterScope(ParseNode node) {
		node.getScope().enter();
	}
	
	///////////////////////////////////////////////////////////////////////////
	// statements and declarations

	private void verifyFunctionArguments(ParseNode node) {
		SubrInvokeNode function = null;

		if (node instanceof SubrCallNode) {
			function = (SubrInvokeNode) node.child(0);
		} else if (node instanceof SubrInvokeNode) {
			function = (SubrInvokeNode) node;
		}

		assert function != null;

		// Get the function signature
		function.setBinding(function.getVariableBinding());
		FunctionSignature signature = function.getBinding().getSignature();

		if (signature == null) {
			Token token = node.getToken();
			logError("No signature defined for " + token.getLexeme() + " at " + token.getLocation());
			node.setType(PrimitiveType.ERROR);
			return;
		}

		// Get child types
		List<Type> childTypes = new ArrayList<Type>();
		function.getChildren().forEach((child) -> childTypes.add(child.getType()));
		childTypes.remove(0); // Remove identifier node

		// Check that number of arguments is the same
		if (signature.accepts(childTypes)) {
			//node.setSignature(signature);
			node.setType(signature.resultType());
		} else {
			typeCheckError(function, childTypes);
		}
	}

	@Override
	public void visitLeave(SubrCallNode node){
		if (!(node.child(0) instanceof SubrInvokeNode)) {
			typeCheckError(node, Arrays.asList(node.child(0).getType()));
		}

		SubrInvokeNode function = (SubrInvokeNode) node.child(0);
		function.setBinding(function.getVariableBinding());

		List<Type> childTypes = new ArrayList<Type>();
		function.getChildren().forEach((child) -> childTypes.add(child.getType()));
		childTypes.remove(0); // Remove identifier node

		FunctionSignature signature = function.getBinding().getSignature();

		// Check that number of arguments is the same
		if (signature.accepts(childTypes)) {
			//node.setSignature(signature);
			node.setType(signature.resultType());
		} else {
			typeCheckError(function, childTypes);
		}
		verifyFunctionArguments(node);
	}

	@Override
	public void visitLeave(SubrInvokeNode node){
		if (node.child(0) instanceof IdentifierNode) {
			IdentifierNode identifier = (IdentifierNode) node.child(0);
			identifier.setBinding(identifier.findVariableBinding());
			Type functionType = identifier.getBinding().getType();

			if (functionType == PrimitiveType.NO_TYPE) {
				ParseNode parent = node.getParent();
				while (parent != null && !(parent instanceof SubrCallNode)) {
					parent = parent.getParent();
				}

				if (parent == null || !(parent instanceof SubrCallNode)) {
					Token token = node.getToken();
					String identifierName = identifier.getToken().getLexeme();
					logError("Cannot use VOID function \"" + identifierName + "\" as an expression at " + token.getLocation());

					node.setType(PrimitiveType.ERROR);
					return;
				}
			}
			verifyFunctionArguments(node);

			node.setType(functionType);
		}
	}

	@Override
	public void visitLeave(SubrDefinitionNode node){
	}

	@Override
	public void visitLeave(SubrBlockNode node){
	}

	@Override
	public void visitLeave(ReturnNode node){
		ParseNode parent = node.getParent();
		while (parent != null && !(parent instanceof SubrBlockNode)) {
			parent = parent.getParent();
		}

		if (parent == null) {
			Token token = node.getToken();
			logError("Cannot call return statement outside of a Lambda at " + token.getLocation());

			node.setType(PrimitiveType.ERROR);
			return;
		}

		Type lambdaReturnType = ((SubrBlockNode) parent).getReturnType();
		Type returnType;
		String returnTypeString = "";

		// Handle void return
		if (node.nChildren() == 0) {
			returnType = PrimitiveType.NO_TYPE;
		} else {
			returnType = node.child(0).getType();
		}

		if (returnType instanceof Array) {
			if (((Array)returnType).equals(lambdaReturnType)) {
				node.setType(returnType);
				return;
			}
			returnTypeString = returnType.infoString();
		}
		if (returnType instanceof SubrBlockNode) {
			returnType = ((SubrBlockNode) returnType).getReturnType();
			if (returnType == lambdaReturnType) {
				node.setType(returnType);
				return;
			}
			returnTypeString = returnType.infoString();
		}
		if (returnType instanceof PrimitiveType) {
			if (returnType == lambdaReturnType) {
				node.setType(returnType);
				return;
			}
			returnTypeString = returnType.infoString();
		}

		node.setType(PrimitiveType.ERROR);

		Token token = node.getToken();
		logError("Cannot return type " + returnTypeString + " for Lambda with return type " + lambdaReturnType + " at " + token.getLocation());
		return;

	}

	@Override
	public void visitLeave(PrintStatementNode node) {
	}
	@Override
	public void visitLeave(DeclarationNode node) {
		if(node.child(0) instanceof ErrorNode) {
			node.setType(PrimitiveType.ERROR);
			return;
		}
		
		IdentifierNode identifier = (IdentifierNode) node.child(0);
		ParseNode initializer = node.child(1);
		
		Type declarationType = initializer.getType();
		node.setType(declarationType);
		
		identifier.setType(declarationType);
		Constancy constancy = (node.getToken().isLextant(Keyword.CONST)) ?
					Constancy.IS_CONSTANT:
					Constancy.IS_VARIABLE;
		addBinding(identifier, declarationType, constancy);
	}
	
	@Override
	public void visitLeave(AssignmentStatementNode node) {
		if(node.child(0) instanceof ErrorNode) {
			node.setType(PrimitiveType.ERROR);
			return;
		}

		ParseNode identifier = node.child(0);
		ParseNode expression = node.child(1);

		node.setType(identifier.getType());
		
		Type expressionType = expression.getType();
		Type identifierType = identifier.getType();

		if(expressionType instanceof Array && identifierType instanceof Array) {
			node.setType(expressionType);
			return;
		}

		if(identifierType instanceof Array) {
			identifierType = ((Array) identifierType).getSubtype();
		}

		if(!expressionType.equals(identifierType)) {
			semanticError("types don't match in AssignmentStatement");
			return;
		}
		
		if(identifier instanceof IdentifierNode) {
			if(((IdentifierNode) identifier).getBinding().isConstant()) {
				semanticError("reassignment to const identifer");
			}
		}

		node.setType(expressionType);
	}

	@Override
	public void visitLeave(TypeNode node) {
			ArrayList<Type> types = new ArrayList<>();
			types.add(node.getType());
			types.add(node.child(0).getType());
			FunctionSignature signature = FunctionSignatures.signature(Punctuator.CAST, types);
			if(signature.resultType().equals(PrimitiveType.ERROR)) {
				node.setType(PrimitiveType.ERROR);
				return;
			}
			node.setType(signature.resultType());
			node.setSignature(signature);
	}
	///////////////////////////////////////////////////////////////////////////
	// expressions
	@Override
	public void visitLeave(OperatorNode node) {
		List<Type> childTypes;  
		if(node.nChildren() == 1) {
			ParseNode child = node.child(0);
			childTypes = Arrays.asList(child.getType());
		}
		else {
			assert node.nChildren() == 2;
			ParseNode left  = node.child(0);
			ParseNode right = node.child(1);
			
			childTypes = Arrays.asList(left.getType(), right.getType());		
		}

		Lextant operator = operatorFor(node);
		FunctionSignature signature = operator.getLexeme().equals(Keyword.LENGTH.getLexeme()) 
				? FunctionSignatures.signaturesOf(operator).get(0) 
				: FunctionSignatures.signature(operator, childTypes);
		
		if(signature.accepts(childTypes) || childTypes.get(0) instanceof Array) {
			node.setType(signature.resultType());
			node.setSignature(signature);
		}
		else {
			typeCheckError(node, childTypes);
			node.setType(PrimitiveType.ERROR);
		}
	}

	
	@Override
	public void visitLeave(IndexNode node) {
		node.setType(node.child(0).getType());
		super.visitLeave(node);
	}

	private Lextant operatorFor(OperatorNode node) {
		LextantToken token = (LextantToken) node.getToken();
		return token.getLextant();
	}

	@Override
	public void visitLeave(ArrayNode node){
		if(!node.isDynamic()) {
			ArrayList<Type> types = new ArrayList<Type>();
			for(ParseNode child: node.getChildren()) {
				Type childType = child.getType();
				if(!types.contains(childType)) {
					types.add(childType);
				}
			}

			Type selectedType = PrimitiveType.ERROR;
			if(types.size() == 1) {
				selectedType = types.get(0);
				node.setType(new Array(selectedType));
				return;
			}

			for(Type t: types){
				if(t instanceof Array || t == PrimitiveType.STRING){
					promotionError(node, PrimitiveType.STRING);
					node.setType(PrimitiveType.ERROR);
					return;
				}
			}
		}
		else{
			node.setType(new Array(node.child(0).getType()));
		}
	}
	///////////////////////////////////////////////////////////////////////////
	// simple leaf nodes
	@Override
	public void visit(BooleanConstantNode node) {
		node.setType(PrimitiveType.BOOLEAN);
	}
	@Override
	public void visit(ErrorNode node) {
		node.setType(PrimitiveType.ERROR);
	}
	@Override
	public void visit(IntegerConstantNode node) {
		node.setType(PrimitiveType.INTEGER);
	}
	@Override
	public void visit(FloatingConstantNode node) {
		node.setType(PrimitiveType.FLOATING);
	}
	@Override
	public void visit(CharacterConstantNode node) {
		node.setType(PrimitiveType.CHARACTER);
	}
	@Override
	public void visit(StringConstantNode node) {
		node.setType(PrimitiveType.STRING);
	}
	@Override
	public void visit(NewlineNode node) {
	}
	@Override
	public void visit(SpaceNode node) {
	}
	@Override
	public void visit(TabSpaceNode node) {
	}

	///////////////////////////////////////////////////////////////////////////
	// IdentifierNodes, with helper methods
	@Override
	public void visit(IdentifierNode node) {
		if(!isBeingDeclared(node) && !isSubrIdentifier(node) && isSubrArgs(node)) {		
			Binding binding = node.findVariableBinding();
			
			node.setType(binding.getType());
			node.setBinding(binding);
		}
		// else parent DeclarationNode does the processing.
	}

	private boolean isSubrIdentifier(IdentifierNode node) {
		return (node.getParent() instanceof SubrDefinitionNode) && (node == node.getParent().child(0));
	}

	private boolean isSubrArgs(IdentifierNode node) {
		return (node.getParent() instanceof SubrParameterNode) && (node == node.getParent().child(0));
	}
	
	private boolean isBeingDeclared(IdentifierNode node) {
		ParseNode parent = node.getParent();
		return (parent instanceof DeclarationNode) && (node == parent.child(0));
	}

	private void addBinding(IdentifierNode identifierNode, Type type, Constancy constancy) {
		Scope scope = identifierNode.getLocalScope();
		Binding binding = scope.createBinding(identifierNode, type, constancy);
		identifierNode.setBinding(binding);
	}

	private void addBinding(IdentifierNode identifierNode, Type type, Constancy constancy, FunctionSignature signature) {
		Scope scope = identifierNode.getLocalScope();
		Binding binding = scope.createBinding(identifierNode, type, constancy);
		binding.setSignature(signature);
		identifierNode.setBinding(binding);
	}
	
	///////////////////////////////////////////////////////////////////////////
	// error logging/printing
	private void semanticError(String message) {
		logError("Semantic error " + message);
	}

	private void promotionError(ParseNode node, Type operandTypes) {
		Token token = node.getToken();
		
		logError("operator for casting " + token.getLexeme() + " not defined for type " 
				 + operandTypes  + " at " + token.getLocation());
	}

	private void typeCheckError(ParseNode node, Type operandTypes){
		Token token = node.getToken();
		
		logError("operator " + token.getLexeme() + " not defined for type " 
				 + operandTypes  + " at " + token.getLocation());
	}
	
	private void typeCheckError(ParseNode node, List<Type> operandTypes) {
		Token token = node.getToken();
		
		logError("operator " + token.getLexeme() + " not defined for types " 
				 + operandTypes  + " at " + token.getLocation());	
	}
	private void logError(String message) {
		TanLogger log = TanLogger.getLogger("compiler.semanticAnalyzer");
		log.severe(message);
	}
}