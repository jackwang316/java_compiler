package semanticAnalyzer;

import java.util.ArrayList;
import java.util.List;

import asmCodeGenerator.codeStorage.ASMOpcode;
import logging.TanLogger;
import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import parseTree.nodeTypes.IdentifierNode;
import parseTree.nodeTypes.ProgramNode;
import parseTree.nodeTypes.SubrBlockNode;
import parseTree.nodeTypes.SubrDefinitionNode;
import parseTree.nodeTypes.SubrParameterNode;
import parseTree.nodeTypes.TypeNode;
import semanticAnalyzer.signatures.FunctionSignature;
import semanticAnalyzer.types.Array;
import semanticAnalyzer.types.PrimitiveType;
import semanticAnalyzer.types.ReturnType;
import semanticAnalyzer.types.Type;
import symbolTable.Binding;
import symbolTable.Scope;
import tokens.Token;
import symbolTable.Binding.Constancy;

public class Preprocessor extends ParseNodeVisitor.Default{
    @Override
	public void visitLeave(ParseNode node) {
		throw new RuntimeException("Node class unimplemented in SemanticAnalysisVisitor: " + node.getClass());
	}

    @Override
	public void visitEnter(ProgramNode node) {
		Scope scope = Scope.createProgramScope();
		node.setScope(scope);
	}

    private void createParameterScope(ParseNode node) {
		Scope scope = Scope.createParameterScope();
		node.setScope(scope);
	}
	private void createProcedureScope(ParseNode node) {
		Scope baseScope = node.getLocalScope();
		Scope scope = baseScope.createSubscope();
		node.setScope(scope);
	}
	private void enterScope(ParseNode node) {
		node.getScope().enter();
	}
	private void leaveScope(ParseNode node) {
		node.getScope().leave();
	}

    @Override	
	public void visitLeave(SubrDefinitionNode node) {
		if (node.child(0) instanceof IdentifierNode && node.child(1) instanceof SubrBlockNode) {
			IdentifierNode identifier = (IdentifierNode) node.child(0);
			SubrBlockNode lambda = (SubrBlockNode) node.child(1);

			Type functionType = getType(lambda.child(0));
			FunctionSignature signature = ((SubrBlockNode) node.child(1)).getSignature();

			node.setSignature(signature);
			node.setType(functionType);
			identifier.setType(functionType);

			addBinding(identifier, functionType);			
		}		
	}

    @Override
	public void visitEnter(SubrBlockNode node) {
		createParameterScope(node);
		enterScope(node);
	}

    @Override
    public void visitLeave(SubrParameterNode node) {
        if (node.child(0) instanceof IdentifierNode) {
			IdentifierNode identifier = (IdentifierNode) node.child(0);

			Type paramType = getType(node);
			if (paramType == PrimitiveType.NO_TYPE) {
				node.setType(PrimitiveType.ERROR);
				Token token = node.getToken();
				logError("Parameter cannot be defined as type VOID at " + token.getLocation());
				return;
			}
			
			addBinding(identifier, paramType);
		}
    }

    @Override
	public void visitLeave(SubrBlockNode node) {		
		if (node.child(0) instanceof SubrParameterNode) {
			SubrParameterNode params = (SubrParameterNode) node.child(0);

			List<Type> childTypes = new ArrayList<Type>();
			ReturnType lambdaType = new ReturnType();

			params.getChildren().forEach((child) -> {
				lambdaType.addType(child.getType());
				childTypes.add(getType(child));
			});

			Type returnType = getType(params);

			FunctionSignature signature = new FunctionSignature(ASMOpcode.Nop, childTypes, returnType);
			lambdaType.setReturnType(returnType);

			node.setSignature(signature);
			node.setType(lambdaType);
		}

		leaveScope(node);
	}	

	@Override
	public void visitLeave(TypeNode node) {
	}

    private void addBinding(IdentifierNode identifierNode, Type type) {
		Scope scope = identifierNode.getLocalScope();
        Binding binding = scope.createBinding(identifierNode, type, Constancy.IS_CONSTANT);
		identifierNode.setBinding(binding);
	}

    private Type getType(ParseNode node) {
		Type t = node.getType();

		if (t instanceof Array || t instanceof ReturnType) {
			return t;
		}

		return PrimitiveType.parseType(t);
	}

    private void logError(String message) {
		TanLogger log = TanLogger.getLogger("compiler.semanticAnalyzer");
		log.severe(message);
	}
}
