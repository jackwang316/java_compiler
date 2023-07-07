package semanticAnalyzer;

import java.util.Arrays; 	
import java.util.List;

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
		if(node.child(0) instanceof ErrorNode) {
			node.setType(PrimitiveType.ERROR);
			return;
		}
		
		IdentifierNode identifier = (IdentifierNode) node.child(0);
		ParseNode expression = node.child(1);
		
		Type expressionType = expression.getType();
		Type identifierType = identifier.getType();
		
		if(!expressionType.equals(identifierType)) {
			semanticError("types don't match in AssignmentStatement");
			return;
		}
		
		if(identifier.getBinding().isConstant()) {
			semanticError("reassignment to const identifer");
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

	private Type[][] promotionTableTypes = new Type[][]{
		new Type[] {CHARACTER, INTEGER},
		new Type[] {CHARACTER, FLOATING},
		new Type[] {INTEGER, FLOATING}
	};
	
	private Type[] promotionTableResultTypes = new Type[] {
		INTEGER,
		FLOATING,
		FLOATING
	};

	private int promotionLevel(Type type) {
		for(int i = 0; i < promotionTableTypes.length; i++) {
			if(type.equals(promotionTableTypes[i][0]) || type.equals(promotionTableTypes[i][1])) {
				return i;
			}
		}
		return -1;
	}

	private Type promotionResultType(Type type1, Type type2) {
		int level1 = promotionLevel(type1);
		int level2 = promotionLevel(type2);
		if(level1 == -1 || level2 == -1) {
			return PrimitiveType.ERROR;
		}
		return promotionTableResultTypes[Math.max(level1, level2)];
	}

	private FunctionSignature binaryPromotionSignature(OperatorNode node, FunctionSignatures group, ArrayList<Type> childTypes) {
		assert childTypes.size() == 2;

		Type type1 = childTypes.get(0);
		Type type2 = childTypes.get(1);
		Type resultType = promotionResultType(type1, type2);

		FunctionSignature originalSignature = group.acceptingSignature(childTypes);
		if(originalSignature != FunctionSignature.nullInstance() && !originalSignature.resultType().equals(PrimitiveType.ERROR)) {
			return originalSignature;
		}

		int[] startPositions = new int[childTypes.size()];
    	for (int i = 0; i < startPositions.length; i++) {
        	startPositions[i] = promotionLevel(childTypes.get(i));
    	}

    	int[] currPos = startPositions.clone();

		// LHS
		if(currPos[0]!=-1) {
			for (int i = currPos[0]; i < promotionTableTypes.length; i++) {
				FunctionSignature signature = group.acceptingSignature(Arrays.asList(promotionTableTypes[i][1], type2));
				if(signature != FunctionSignature.nullInstance() && !signature.resultType().equals(PrimitiveType.ERROR)) {
					implicitConversion(type1, promotionTableTypes[i][1], childTypes);
					return signature;
				}
			}
		}
		// RHS
		if(currPos[1]!=-1) {
			for (int i = currPos[1]; i < promotionTableTypes.length; i++) {
				FunctionSignature signature = group.acceptingSignature(Arrays.asList(type1, promotionTableTypes[i][1]));
				if(signature != FunctionSignature.nullInstance() && !signature.resultType().equals(PrimitiveType.ERROR)) {
					implicitConversion(type2, promotionTableTypes[i][1], childTypes);
					return signature;
				}
			}
		}
		// LHS & RHS
		if(currPos[0]!=-1 && currPos[1]!=-1) {
			ArrayList<FunctionSignature> howManyWork = new ArrayList<FunctionSignature>();
			// RHS: outer loop, LHS: inner loop
			for (int i = currPos[1]; i < promotionTableTypes.length; i++) {
				for (int j = currPos[0]; j < promotionTableTypes.length; j++) {
					FunctionSignature signature = group.acceptingSignature(Arrays.asList(promotionTableTypes[i][1], promotionTableTypes[j][1]));
					if(signature != FunctionSignature.nullInstance() && !signature.resultType().equals(PrimitiveType.ERROR)) {
						howManyWork.add(signature);
					}
				}
			}
			if(howManyWork.size() > 1) {
				// int[][] parameterPositions = new int[howManyWork.size()][];
				int[][] precendence = new int[howManyWork.size()][];
				for (int i = 0; i < precendence.length; i++) {
					Type[] parameterTypes = howManyWork.get(i).getParamTypes();
					precendence[i] = new int[]{promotionLevel(parameterTypes[0]), promotionLevel(parameterTypes[1])};
				}
				int x = 0, y = 0;
				for (int i = 1; i < precendence.length; i++) {
					if(precendence[i][0] > precendence[x][0]) {
						x = i;
					}
					if(precendence[i][1] > precendence[y][1]) {
						y = i;
					}

				}
				if(x != y){
					if(precendence[x][1] == precendence[y][1]){
						y = x;
					}
					else if(precendence[x][0] == precendence[y][0]){
						x = y;
					}
				}
				if (x==y){
					FunctionSignature signature = howManyWork.get(x);
					Type[] parameterTypes = signature.getParamTypes();
					for (int i = 0; i < parameterTypes.length; i++) {
						implicitConversion(childTypes.get(i), parameterTypes[i], childTypes);
					}
					return signature;
				}
				multiplePossiblePromotionError(node, node.getToken().getLexeme(), childTypes.get(0), childTypes.get(1));
			}
		}
		return originalSignature;
	}

	// private void implicitConversion(OperatorNode node, Type type, ArrayList<Type> childTypes) {
		// Type originalType = childTypes.get(index);
		// if(originalType == resultType) {
		// 	return;
		// }
		// implicitCast(node, index, resultType, originalType);

		// childTypes.set(index, resultType);
		
	// }

	private void implicitConversion(Type originalType, Type promoType, ArrayList<Type> childTypes) {
		if(originalType == promoType) {
			return;
		}
		implicitConversion(originalType, promoType);
		childTypes.set(childTypes.size()-1, promoType);
	}
	private void implicitConversion(Type originalType, Type promoType) {
		if(originalType == promoType) {
			return;
		}
		if(originalType == PrimitiveType.CHARACTER && promoType == PrimitiveType.INTEGER) {
			implicitConversion(originalType, INTEGER);
		}
		else if(originalType == PrimitiveType.CHARACTER && promoType == PrimitiveType.FLOATING) {
			implicitConversion(originalType, FLOATING);
		}
		else if(originalType == PrimitiveType.INTEGER && promoType == PrimitiveType.FLOATING) {
			implicitConversion(originalType, FLOATING);
		}
		else {
			semanticError("implicit conversion error");
		}

		TypeNode typeNode = TypeNode.withChildren(((ParseNode)originalType), promoType);
		typeNode.setType(promoType);
		typeNode.setSignature(FunctionSignatures.signature(Punctuator.CAST, Arrays.asList(originalType, promoType)));
		visitLeave(typeNode);
	}

	///////////////////////////////////////////////////////////////////////////
	// expressions
	@Override
	public void visitLeave(OperatorNode node) {
		// ArrayList<Type> childTypes = new ArrayList<Type>();
		List<Type> childTypes;
		Lextant operator = operatorFor(node);
		FunctionSignatures signatures = FunctionSignatures.signaturesOf(operator);
		FunctionSignature signature;

		ArrayList<Type> childTypesArrayList = new ArrayList<Type>();
		for (ParseNode child : node.getChildren()) {
			childTypesArrayList.add(child.getType());
		}

		if(node.nChildren() == 1) {
			ParseNode child = node.child(0);
			childTypes = Arrays.asList(child.getType());
		}
		else {
			assert node.nChildren() == 2;
			ParseNode left  = node.child(0);
			ParseNode right = node.child(1);
			
			childTypes = Arrays.asList(left.getType(), right.getType());
			signature = binaryPromotionSignature(node, signatures, childTypesArrayList);
		}
		
		signature = FunctionSignatures.signature(operator, childTypes);

		if(signature.accepts(childTypes)) {
			node.setType(signature.resultType());
			node.setSignature(signature);
		}
		else {
			typeCheckError(node, childTypes);
			node.setType(PrimitiveType.ERROR);
		}
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
				boolean found=false;
				for(Type type: types) {
					if(type.equivalent(childType)) {
						found=true;
						break;
					}
				}
				if(!found) {
					types.add(childType);
				}
			}

			Type selectedType = PrimitiveType.ERROR;
			if(types.size()==1) {
				selectedType = types.get(0);
				node.setType(selectedType);
			}
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