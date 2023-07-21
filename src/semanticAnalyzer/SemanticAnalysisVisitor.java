package semanticAnalyzer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import asmCodeGenerator.operators.LengthCodeGenerator;
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
import parseTree.nodeTypes.IfStatementNode;
import parseTree.nodeTypes.IntegerConstantNode;
import parseTree.nodeTypes.NewlineNode;
import parseTree.nodeTypes.OperatorNode;
import parseTree.nodeTypes.PrintStatementNode;
import parseTree.nodeTypes.ProgramNode;
import parseTree.nodeTypes.SpaceNode;
import parseTree.nodeTypes.TabSpaceNode;
import parseTree.nodeTypes.TypeNode;
import parseTree.nodeTypes.WhileStatementNode;
import parseTree.nodeTypes.StringConstantNode;
import semanticAnalyzer.signatures.FunctionSignature;
import semanticAnalyzer.signatures.FunctionSignatures;
import semanticAnalyzer.types.Array;
import semanticAnalyzer.types.PrimitiveType;
import static semanticAnalyzer.types.PrimitiveType.*;
import semanticAnalyzer.types.Type;
import semanticAnalyzer.types.TypeVariable;
import symbolTable.Binding;
import symbolTable.Binding.Constancy;
import symbolTable.Scope;
import tokens.LextantToken;
import tokens.Token;

class SemanticAnalysisVisitor extends ParseNodeVisitor.Default {
	@Override
	public void visitLeave(ParseNode node) {
		System.out.println("visitLeave(ParseNode node)");
		throw new RuntimeException("Node class unimplemented in SemanticAnalysisVisitor: " + node.getClass());
	}
	
	///////////////////////////////////////////////////////////////////////////
	// constructs larger than statements
	@Override
	public void visitEnter(ProgramNode node) {
		enterProgramScope(node);
	}
	public void visitLeave(ProgramNode node) {
		leaveScope(node);
	}
	public void visitEnter(BlockStatementNode node) {
		enterSubscope(node);
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
	private void enterSubscope(ParseNode node) {
		Scope baseScope = node.getLocalScope();
		Scope scope = baseScope.createSubscope();
		node.setScope(scope);
	}		
	private void leaveScope(ParseNode node) {
		node.getScope().leave();
	}
	
	///////////////////////////////////////////////////////////////////////////
	// statements and declarations
	@Override
	public void visitLeave(PrintStatementNode node) {
	}
	@Override
	public void visitLeave(IfStatementNode node){
		assertCorrectType(node, PrimitiveType.BOOLEAN, node.child(0).getType());
	}
	@Override
	public void visitLeave(WhileStatementNode node){
		assertCorrectType(node, PrimitiveType.BOOLEAN, node.child(0).getType());
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
		System.out.println("visitLeave(AssignmentStatementNode node)");
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

		// if(!expressionType.equals(identifierType)) {
		// 	semanticError("types don't match in AssignmentStatement");
		// 	return;
		// }
		
		
		if((identifierType == CHARACTER || identifierType == INTEGER || identifierType == FLOATING) && (expressionType == CHARACTER || expressionType == INTEGER)) {
			System.out.println("implicit conversion");
			implicitConversion(node, 1, identifierType, expressionType);
		}
		else {
			assertCorrectType(node, identifierType, expressionType);
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

	private Type[][] promotionTableTypes = {
		{CHARACTER, INTEGER},
		{CHARACTER, FLOATING},
		{INTEGER, FLOATING}
	};

	private FunctionSignature binaryPromotionSignature(OperatorNode node, FunctionSignatures signatures, ArrayList<Type> childTypes) {
		System.out.println("binaryPromotionSignature");
		Type type1 = childTypes.get(0);
		Type type2 = childTypes.get(1);
	
		FunctionSignature originalSignature = signatures.acceptingSignature(childTypes);
		if (originalSignature != FunctionSignature.nullInstance() && !originalSignature.resultType().equals(PrimitiveType.ERROR)) {
			return originalSignature;
		}
	
		ArrayList<FunctionSignature> matchingSignatures = new ArrayList<>();
	
		// Check all possible promotion paths
		for (Type[] promotionTypes : promotionTableTypes) {
			Type lhsType = promotionTypes[0];
			Type rhsType = promotionTypes[1];
	
			// Promotion: char -> int
			if (type1 == CHARACTER  && type2 == INTEGER) {
				System.out.println("char -> int");
				FunctionSignature signature = signatures.acceptingSignature(Arrays.asList(lhsType, rhsType));
				if (signature != FunctionSignature.nullInstance() && !signature.resultType().equals(PrimitiveType.ERROR)) {
					matchingSignatures.add(signature);
				}
			}
	
			// Promotion: char -> float
			if (type1 == CHARACTER  && type2 == FLOATING) {
				System.out.println("char -> float");
				FunctionSignature signature = signatures.acceptingSignature(Arrays.asList(lhsType, rhsType));
				if (signature != FunctionSignature.nullInstance() && !signature.resultType().equals(PrimitiveType.ERROR)) {
					matchingSignatures.add(signature);
				}
			}
	
			// Promotion: int -> float
			if (type1 == INTEGER && type2 == FLOATING) {
				System.out.println("int -> float");
				FunctionSignature signature = signatures.acceptingSignature(Arrays.asList(lhsType, rhsType));
				if (signature != FunctionSignature.nullInstance() && !signature.resultType().equals(PrimitiveType.ERROR)) {
					matchingSignatures.add(signature);
				}
			}
		}
	
		if (matchingSignatures.size() == 1) {
			FunctionSignature signature = matchingSignatures.get(0);
			Type[] parameterTypes = signature.getParamTypes();
			for (int i = 0; i < parameterTypes.length; i++) {
				implicitConversion(node, i, parameterTypes[i], childTypes);
			}
			return signature;
		} else if (matchingSignatures.size() > 1) {
			// Handle multiple possible promotions error
			multiplePossiblePromotionError(node, node.getToken().getLexeme(), childTypes.get(0), childTypes.get(1));
		}

		return originalSignature;
	}
	
	private void implicitConversion(ParseNode node, int index, Type promoType, ArrayList<Type> childTypes) {
		Type originalType = childTypes.get(index);
		if (originalType != promoType) {
			ParseNode expression = node.child(index);
			TypeNode typeNode = TypeNode.withChildren(expression, promoType);
			node.replaceNthChild(index, typeNode);
			visitLeave(typeNode);
			childTypes.set(index, promoType);
		}
	}

	// private void implicitConversion(ParseNode node, int index, Type promoType, ArrayList<Type> childTypes) {
	// 	Type originalType = childTypes.get(index);
	// 	if(originalType == promoType) {
	// 		return;
	// 	}
	// 	implicitConversion(node, index, originalType, promoType);
	// 	childTypes.set(index, promoType);
	// }

	// private void implicitConversion(ParseNode node, int index, Type promoType, Type originalType) {
	// 	if (originalType == promoType) {
	// 		return;
	// 	}
	
	// 	// Handle char to int conversion
	// 	System.out.println("originalType: " + originalType + " promoType: " + promoType);
	// 	if (originalType == CHARACTER && promoType == INTEGER) {
	// 		ParseNode expression = node.child(index);
	// 		TypeNode typeNode = TypeNode.withChildren(expression, promoType);
	// 		// System.out.println("typeNode: " + typeNode);
	// 		node.replaceNthChild(index, typeNode);
	// 		visitLeave(typeNode);
	// 	}
	
	// 	// Handle char to float conversion
	// 	if (originalType == CHARACTER && promoType == FLOATING) {
	// 		ParseNode expression = node.child(index);
	// 		// TypeNode typeNode = TypeNode.withChildren(expression, promoType);
	// 		// System.out.println("typeNode: " + typeNode);
	// 		// node.replaceNthChild(index, typeNode);
	// 		// visitLeave(typeNode);
			
	// 		// Convert CHAR to INT
	// 		TypeNode intTypeNode = TypeNode.withChildren(expression, INTEGER);
	// 		node.replaceNthChild(index, intTypeNode);
	// 		visitLeave(intTypeNode);
		
	// 		// Convert INT to FLOATING
	// 		ParseNode intExpression = intTypeNode.child(0);
	// 		TypeNode floatTypeNode = TypeNode.withChildren(intExpression, FLOATING);
	// 		intTypeNode.replaceChild(intExpression, floatTypeNode);
	// 		visitLeave(floatTypeNode);
	// 	}
	
	// 	// Handle int to float conversion
	// 	if (originalType == INTEGER && promoType == FLOATING) {
	// 		ParseNode expression = node.child(index);
	// 		TypeNode typeNode = TypeNode.withChildren(expression, promoType);
	// 		// System.out.println("typeNode: " + typeNode);
	// 		node.replaceNthChild(index, typeNode);
	// 		visitLeave(typeNode);
	// 	}
	// }
	private void implicitConversion(ParseNode node, int index, Type promoType, Type originalType){
		if(originalType == promoType){
			return;
		}
		if(originalType == CHARACTER && promoType != INTEGER) {
			implicitConversion(node, index, INTEGER, originalType);
		}

		if(originalType == CHARACTER && promoType == FLOATING) {
			implicitConversion(node, index, INTEGER, originalType);
			implicitConversion(node, index, FLOATING, INTEGER);
		}

		// if(originalType == INTEGER && promoType == FLOATING) {
		// 	implicitConversion(node, index, FLOATING, originalType);
		// }

		ParseNode expressionNode = node.child(index);
		TypeNode typeNode = TypeNode.withChildren(expressionNode, promoType);
		System.out.println("typeNode: " + typeNode);
		
		// node.replaceNthChild(index, typeNode);
		visitLeave(typeNode);
	}

	///////////////////////////////////////////////////////////////////////////
	// expressions
	@Override
	public void visitLeave(OperatorNode node) {
		List<Type> childTypes;
		Lextant operator = operatorFor(node);
		FunctionSignatures signatures = FunctionSignatures.signaturesOf(operator);
		FunctionSignature signature;

		// unary
		if(node.nChildren() == 1) {
			System.out.println("unary");
			ParseNode child = node.child(0);
			childTypes = Arrays.asList(child.getType());
			signature = signatures.acceptingSignature(childTypes);
		}

		// binary
		else{
			System.out.println("binary");
			ParseNode left  = node.child(0);
			ParseNode right = node.child(1);
			
			childTypes = Arrays.asList(left.getType(), right.getType());

			ArrayList<Type> childTypesArrayList = new ArrayList<Type>();
			for (ParseNode child : node.getChildren()) {
				childTypesArrayList.add(child.getType());
			}
			
			signature = binaryPromotionSignature(node, signatures, childTypesArrayList);
		}

		signature = operator.getLexeme().equals(Keyword.LENGTH.getLexeme()) 
				? FunctionSignatures.signaturesOf(operator).get(0) 
				: FunctionSignatures.signature(operator, childTypes);
		
		if(signature.accepts(childTypes) || childTypes.get(0) instanceof Array) {
			System.out.println("Child types: " + childTypes);
    		System.out.println("Signature: " + signature);
		}
		
		// signature = FunctionSignatures.signature(operator, childTypes);

		if(signature.accepts(childTypes)) {
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
		if(!isBeingDeclared(node)) {		
			Binding binding = node.findVariableBinding();
			
			node.setType(binding.getType());
			node.setBinding(binding);
		}
		// else parent DeclarationNode does the processing.
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
	private void multiplePossiblePromotionError(ParseNode node, String operator, Type left, Type right) {
		logError("Multiple possible promotions for operator " + operator + " with operands "+ left + " and " +
	right + " at " + node.getToken().getLocation());
	}
	private void logError(String message) {
		TanLogger log = TanLogger.getLogger("compiler.semanticAnalyzer");
		log.severe(message);
	}
	private void assertCorrectType(ParseNode node, Type expectedType, Type actualType) {
		if(!expectedType.equals(actualType)) {
			semanticError("expected " + expectedType + ", got " + actualType);
			node.setType(PrimitiveType.ERROR);
		}
	}
}