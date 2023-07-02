package asmCodeGenerator;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.PseudoColumnUsage;
import java.util.ArrayList;

import asmCodeGenerator.codeStorage.ASMCodeChunk;
import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMOpcode;
import asmCodeGenerator.operators.SimpleCodeGenerator;
import asmCodeGenerator.runtime.MemoryManager;
import asmCodeGenerator.runtime.RunTime;
import lexicalAnalyzer.Lextant;
import lexicalAnalyzer.Punctuator;
import parseTree.*;
import parseTree.nodeTypes.ArrayNode;
import parseTree.nodeTypes.AssignmentStatementNode;
import parseTree.nodeTypes.BlockStatementNode;
import parseTree.nodeTypes.BooleanConstantNode;
import parseTree.nodeTypes.CharacterConstantNode;
import parseTree.nodeTypes.DeclarationNode;
import parseTree.nodeTypes.FloatingConstantNode;
import parseTree.nodeTypes.IdentifierNode;
import parseTree.nodeTypes.IntegerConstantNode;
import parseTree.nodeTypes.NewlineNode;
import parseTree.nodeTypes.OperatorNode;
import parseTree.nodeTypes.PrintStatementNode;
import parseTree.nodeTypes.ProgramNode;
import parseTree.nodeTypes.SpaceNode;
import parseTree.nodeTypes.TabSpaceNode;
import parseTree.nodeTypes.TypeNode;
import parseTree.nodeTypes.StringConstantNode;
import semanticAnalyzer.signatures.FunctionSignature;
import static semanticAnalyzer.types.PrimitiveType.*;

import semanticAnalyzer.types.Array;
import semanticAnalyzer.types.PrimitiveType;
import semanticAnalyzer.types.Type;
import symbolTable.Binding;
import symbolTable.Scope;
import static asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType.*;
import static asmCodeGenerator.codeStorage.ASMOpcode.*;


// do not call the code generator if any errors have occurred during analysis.
public class ASMCodeGenerator {
	ParseNode root;
	public static final int ARRAY_IDENTIFIER = 5;
	public static final int ADDRESS_LENGTH = 4;
	public static final int HEADER_LENGTH = 16;
	public static final int REFERENCES_STATUS = 0x100;

	public static ASMCodeFragment generate(ParseNode syntaxTree) {
		ASMCodeGenerator codeGenerator = new ASMCodeGenerator(syntaxTree);
		return codeGenerator.makeASM();
	}
	public ASMCodeGenerator(ParseNode root) {
		super();
		this.root = root;
	}
	
	public ASMCodeFragment makeASM() {
		ASMCodeFragment code = new ASMCodeFragment(GENERATES_VOID);
		code.append(MemoryManager.codeForInitialization());
		code.append( RunTime.getEnvironment() );
		code.append( globalVariableBlockASM() );
		code.append( programASM() );
		code.append( MemoryManager.codeForAfterApplication() );
		
		return code;
	}
	private ASMCodeFragment globalVariableBlockASM() {
		assert root.hasScope();
		Scope scope = root.getScope();
		int globalBlockSize = scope.getAllocatedSize();
		
		ASMCodeFragment code = new ASMCodeFragment(GENERATES_VOID);
		code.add(DLabel, RunTime.GLOBAL_MEMORY_BLOCK);
		code.add(DataZ, globalBlockSize);
		return code;
	}
	private ASMCodeFragment programASM() {
		ASMCodeFragment code = new ASMCodeFragment(GENERATES_VOID);
		
		code.add(    Label, RunTime.MAIN_PROGRAM_LABEL);
		code.append( programCode());
		code.add(    Halt );
		
		return code;
	}
	private ASMCodeFragment programCode() {
		CodeVisitor visitor = new CodeVisitor();
		root.accept(visitor);
		return visitor.removeRootCode(root);
	}


	protected class CodeVisitor extends ParseNodeVisitor.Default {
		private Map<ParseNode, ASMCodeFragment> codeMap;
		ASMCodeFragment code;
		
		public CodeVisitor() {
			codeMap = new HashMap<ParseNode, ASMCodeFragment>();
		}


		////////////////////////////////////////////////////////////////////
        // Make the field "code" refer to a new fragment of different sorts.
		private void newAddressCode(ParseNode node) {
			code = new ASMCodeFragment(GENERATES_ADDRESS);
			codeMap.put(node, code);
		}
		private void newValueCode(ParseNode node) {
			code = new ASMCodeFragment(GENERATES_VALUE);
			codeMap.put(node, code);
		}
		private void newVoidCode(ParseNode node) {
			code = new ASMCodeFragment(GENERATES_VOID);
			codeMap.put(node, code);
		}

	    ////////////////////////////////////////////////////////////////////
        // Get code from the map.
		private ASMCodeFragment getAndRemoveCode(ParseNode node) {
			ASMCodeFragment result = codeMap.get(node);
			codeMap.remove(node);
			return result;
		}
	    public  ASMCodeFragment removeRootCode(ParseNode tree) {
			return getAndRemoveCode(tree);
		}		
		ASMCodeFragment removeValueCode(ParseNode node) {
			ASMCodeFragment frag = getAndRemoveCode(node);
			makeFragmentValueCode(frag, node);
			return frag;
		}		
		private ASMCodeFragment removeAddressCode(ParseNode node) {
			ASMCodeFragment frag = getAndRemoveCode(node);
			assert frag.isAddress();
			return frag;
		}		
		ASMCodeFragment removeVoidCode(ParseNode node) {
			ASMCodeFragment frag = getAndRemoveCode(node);
			assert frag.isVoid();
			return frag;
		}
		
	    ////////////////////////////////////////////////////////////////////
        // convert code to value-generating code.
		private void makeFragmentValueCode(ASMCodeFragment code, ParseNode node) {
			assert !code.isVoid();
			
			if(code.isAddress()) {
				turnAddressIntoValue(code, node);
			}	
		}
		private void turnAddressIntoValue(ASMCodeFragment code, ParseNode node) {
			if(node.getType() == PrimitiveType.INTEGER) {
				code.add(LoadI);
			}	
			else if(node.getType() == PrimitiveType.BOOLEAN) {
				code.add(LoadC);
			}	
			else if(node.getType() == PrimitiveType.CHARACTER) {
				code.add(LoadC);
			}
			else if(node.getType() == PrimitiveType.STRING) {
				code.add(LoadC);
			}
			else if(node.getType() == PrimitiveType.FLOATING) {
				code.add(LoadF);
			}	
			else if(node.getType() instanceof Array) {
				code.add(LoadI);
			}
			else {
				assert false : "node " + node;
			}
			code.markAsValue();
		}
		
	    ////////////////////////////////////////////////////////////////////
        // ensures all types of ParseNode in given AST have at least a visitLeave	
		public void visitLeave(ParseNode node) {
			assert false : "node " + node + " not handled in ASMCodeGenerator";
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructs larger than statements
		public void visitLeave(ProgramNode node) {
			newVoidCode(node);
			for(ParseNode child : node.getChildren()) {
				ASMCodeFragment childCode = removeVoidCode(child);
				code.append(childCode);
			}
		}
		public void visitLeave(BlockStatementNode node) {
			newVoidCode(node);
			for(ParseNode child : node.getChildren()) {
				ASMCodeFragment childCode = removeVoidCode(child);
				code.append(childCode);
			}
		}

		///////////////////////////////////////////////////////////////////////////
		// statements and declarations

		public void visitLeave(PrintStatementNode node) {
			newVoidCode(node);
			new PrintStatementGenerator(code, this).generate(node);	
		}
		public void visit(NewlineNode node) {
			newVoidCode(node);
			code.add(PushD, RunTime.NEWLINE_PRINT_FORMAT);
			code.add(Printf);
		}
		public void visit(SpaceNode node) {
			newVoidCode(node);
			code.add(PushD, RunTime.SPACE_PRINT_FORMAT);
			code.add(Printf);
		}
		public void visit(TabSpaceNode node) {
			newVoidCode(node);
			code.add(PushD, RunTime.TAB_SPACE_PRINT_FORMAT);
			code.add(Printf);
		}
		

		public void visitLeave(DeclarationNode node) {
			newVoidCode(node);
			ASMCodeFragment lvalue = removeAddressCode(node.child(0));	
			ASMCodeFragment rvalue = removeValueCode(node.child(1));
			
			code.append(lvalue);
			code.append(rvalue);
			
			Type type = node.getType();
			code.add(opcodeForStore(type));
		}
		
		public void visitLeave(AssignmentStatementNode node) {
			newVoidCode(node);
			ASMCodeFragment lvalue = removeAddressCode(node.child(0));	
			ASMCodeFragment rvalue = removeValueCode(node.child(1));
			
			code.append(lvalue);
			code.append(rvalue);
			
			Type type = node.getType();
			code.add(opcodeForStore(type));
		}
		private ASMOpcode opcodeForStore(Type type) {
			if(type == PrimitiveType.INTEGER) {
				return StoreI;
			}
			if(type == PrimitiveType.BOOLEAN) {
				return StoreC;
			}
			if(type == PrimitiveType.CHARACTER) {
				return StoreC;
			}
			if(type == PrimitiveType.STRING) {
				return StoreI;
			}
			if(type == PrimitiveType.FLOATING) {
				return StoreF;
			}
			if(type instanceof Array) {
				return StoreI;
			}
			assert false: "Type " + type + " unimplemented in opcodeForStore()";
			return null;
		}


		///////////////////////////////////////////////////////////////////////////
		// expressions
		public void visitLeave(OperatorNode node) {
			Lextant operator = node.getOperator();
			FunctionSignature signature = node.getSignature();
			Object variant = signature.getVariant();
			
			if (variant instanceof ASMOpcode) {
				Labeller labeller = new Labeller("Operator");
				String startLabel = labeller.newLabel("args");
				String opLabel   = labeller.newLabel("op");
				
				newValueCode(node);
				code.add(Label, startLabel);
				for (ParseNode child: node.getChildren()) {
					code.append(removeValueCode(child));
				}
				code.add((ASMOpcode)variant);
				
			}
			
			else if (variant instanceof SimpleCodeGenerator) {
				SimpleCodeGenerator generator = (SimpleCodeGenerator) variant;
				ASMCodeFragment fragment = generator.generate(node, childValueCode(node));
				codeMap.put(node, fragment);
			}
			
			else if (Punctuator.isComparison(operator)) {
				visitComparisonOperatorNode(node, (Punctuator)operator);
			}
			
//			if(operator == Punctuator.SUBTRACT || operator == Punctuator.DIVIDE) {
//			visitUnaryOperatorNode(node);
//			}
//			else if(operator == Punctuator.GREATER) {
//				visitComparisonOperatorNode(node, operator);
//			
//			}
//			else {
//				visitNormalBinaryOperatorNode(node);
//			}
			
		}
		private List<ASMCodeFragment> childValueCode(OperatorNode node){
			List<ASMCodeFragment> result = new ArrayList<>();
			for (ParseNode child: node.getChildren()) {
				result.add(removeValueCode(child));
			}
			return result;
		}
		
		public void visitLeave(TypeNode node) {
			newValueCode(node);
			ASMCodeFragment arg = removeValueCode(node.child(0));
			code.append(arg);
			Object variant = node.getSignature().getVariant();
			if(variant instanceof ASMOpcode) {
				ASMOpcode opcode = (ASMOpcode) variant;
				code.add(opcode);
			}
			else if(variant instanceof SimpleCodeGenerator) {
				SimpleCodeGenerator generator = (SimpleCodeGenerator) variant;
				ASMCodeFragment fragment = generator.generate(node, Collections.singletonList(arg));
				code.append(fragment);
			}
		}

		public void visitLeave(ArrayNode node) {
			if (node.isDynamic()) {
				newValueCode(node);
				Type type = node.getType();
				int status = getStatus(type);	
				int length = parseInt(node);
				int typeSize = isArrayOrString(type) ? PrimitiveType.INTEGER.getSize() : type.getSize();
				code.add(PushI, length);
				code.add(Duplicate);
				code.add(JumpNeg, RunTime.NEGATIVE_INDEX_RUNTIME_ERROR);
				int totalSize = length * type.getSize() + HEADER_LENGTH;
				code.add(PushI, totalSize);
				code.add(Call, MemoryManager.MEM_MANAGER_ALLOCATE);
				String tempLoc = RunTime.ARR_LOC;
				code.add(PushD, tempLoc);
				code.add(Exchange);
				code.add(StoreI);
				appendToPtr(code, tempLoc, 0, ARRAY_IDENTIFIER);
				appendToPtr(code, tempLoc, ADDRESS_LENGTH, status);
				appendToPtr(code, tempLoc, 2 * ADDRESS_LENGTH, typeSize);
				appendToPtr(code, tempLoc, 3 * ADDRESS_LENGTH, length);

				for(int i = 0; i < length; i++) {
					appendToPtr(code, tempLoc, HEADER_LENGTH + i * typeSize, 0);
				}
				code.add(ASMOpcode.PushD, RunTime.ARR_LOC);
				code.add(ASMOpcode.LoadI);

			} else {
				newAddressCode(node);
				Type type = node.getType();
				System.out.println(node.getType());
				int status = getStatus(type);
				int length = node.nChildren();
				int typeSize = isArrayOrString(type) ? PrimitiveType.INTEGER.getSize() : type.getSize();
				int totalSize = length * type.getSize() + HEADER_LENGTH;
				code.add(PushI, totalSize);
				code.add(Call, MemoryManager.MEM_MANAGER_ALLOCATE);
				String tempLoc = RunTime.ARR_LOC;
				code.add(PushD, tempLoc);
				code.add(Exchange);
				code.add(StoreI);
				appendToPtr(code, tempLoc, 0, ARRAY_IDENTIFIER);
				appendToPtr(code, tempLoc, ADDRESS_LENGTH, status);
				appendToPtr(code, tempLoc, 2 * ADDRESS_LENGTH, typeSize);
				appendToPtr(code, tempLoc, 3 * ADDRESS_LENGTH, length);

				List<ParseNode> children = node.getChildren();

				for(int i = 0; i< length; i++) {
					appendToPtr(code, tempLoc, HEADER_LENGTH + i * typeSize, removeValueCode(children.get(i)), opcodeForStore(type));
				}
				code.add(Duplicate);
				// code.add(PushD, tempLoc);
				// code.add(Exchange);
				// code.add(StoreI);
				// code.add(ASMOpcode.PushD, RunTime.ARR_LOC);
				// code.add(ASMOpcode.LoadI);
				// code.add(Duplicate);
				// code.add(PushI, 12);
				// code.add(Add);
				// code.add(LoadI);
				// code.add(PStack);
			} 
		}

		public int parseInt(ParseNode node) {
			String token = node.child(0).getToken().getLexeme();
			if(token.equals("-")) {
				return Integer.parseInt(token + node.child(0).child(0).getToken().getLexeme());
			}
			return Integer.parseInt(token);
		}

		public int getStatus(Type type) {
			if(isArrayOrString(type)) {
				return REFERENCES_STATUS;
			}
			else {
				return 0;
			}
		}

		public boolean isArrayOrString(Type type){
			return type instanceof Array || type == PrimitiveType.STRING;
		}

		private void appendToPtr(ASMCodeFragment code, String location, int offset, ASMCodeFragment val, ASMOpcode asmOpcode) {
			code.add(PushD, location);
			code.add(LoadI);
			code.add(PushI, offset);
			code.add(Add);
			code.append(val);
			System.out.println(val);
			code.add(asmOpcode);
		}

		private void appendToPtr(ASMCodeFragment code, String location, int offset, int val) {
			code.add(PushD, location);
			code.add(LoadI);
			code.add(PushI, offset);
			code.add(Add);
			code.add(PushI, val);
			code.add(StoreI);
		}

		private void visitComparisonOperatorNode(OperatorNode node,
				Punctuator operator) {

			ASMCodeFragment arg1 = removeValueCode(node.child(0));
			ASMCodeFragment arg2 = removeValueCode(node.child(1));
			
			Labeller labeller = new Labeller("compare");

			Type types[] = node.getSignature().getParamTypes();
			Type first_type = types[0];
			
			String startLabel = labeller.newLabel("arg1");
			String arg2Label  = labeller.newLabel("arg2");
			String subLabel   = labeller.newLabel("sub");
			String trueLabel  = labeller.newLabel("true");
			String falseLabel = labeller.newLabel("false");
			String joinLabel  = labeller.newLabel("join");
			
			newValueCode(node);
			code.add(Label, startLabel);
			code.append(arg1);
			code.add(Label, arg2Label);
			code.append(arg2);
			code.add(Label, subLabel);
			
			// TODO: cahr and string type
			if (first_type == INTEGER || first_type == CHARACTER) {
				code.add(Subtract);
				switch (operator) {
					case GREATER:
						code.add(JumpPos, trueLabel);
						code.add(Jump, falseLabel);
						break;
					case GREATER_EQUAL:
						code.add(JumpNeg, falseLabel);
						code.add(Jump, trueLabel);
						break;
					case LESS:
						code.add(JumpNeg, trueLabel);
						code.add(Jump, falseLabel);
						break;
					case LESS_EQUAL:
						code.add(JumpPos, falseLabel);
						code.add(Jump, trueLabel);
						break;
					case EQUAL:
						code.add(JumpFalse, trueLabel);
						code.add(Jump, falseLabel);
						break;
					case NOT_EQUAL:
						code.add(JumpTrue, trueLabel);
						code.add(Jump, falseLabel);
						break;
				}
			}
			else if (first_type == FLOATING){
				code.add(FSubtract);
				switch (operator) {
					case GREATER:
						code.add(JumpFPos, trueLabel);
						code.add(Jump, falseLabel);
						break;
					case GREATER_EQUAL:
						code.add(JumpFNeg, falseLabel);
						code.add(Jump, trueLabel);
						break;
					case LESS:
						code.add(JumpFNeg, trueLabel);
						code.add(Jump, falseLabel);
						break;
					case LESS_EQUAL:
						code.add(JumpFPos, falseLabel);
						code.add(Jump, trueLabel);
						break;
					case EQUAL:
						code.add(JumpFZero, trueLabel);
						code.add(Jump, falseLabel);
						break;
					case NOT_EQUAL:
						code.add(JumpFZero, falseLabel);
						code.add(Jump, trueLabel);
						break;
				}
			}
			else if (first_type == BOOLEAN || first_type == STRING) {
				code.add(Subtract);
				switch (operator) {
					case EQUAL:
						code.add(JumpFalse, trueLabel);
						code.add(Jump, falseLabel);
						break;
					case NOT_EQUAL:
						code.add(JumpTrue, trueLabel);
						code.add(Jump, falseLabel);
						break;
				}
			}
			else {
				System.out.println("Unimplemented type!");
			}
			
			code.add(Label, trueLabel);
			code.add(PushI, 1);
			code.add(Jump, joinLabel);
			code.add(Label, falseLabel);
			code.add(PushI, 0);
			code.add(Jump, joinLabel);
			code.add(Label, joinLabel);

		}		
		private void visitUnaryOperatorNode(OperatorNode node) {
			newValueCode(node);
			ASMCodeFragment arg1 = removeValueCode(node.child(0));
			
			code.append(arg1);
			
			ASMOpcode opcode = opcodeForOperator(node.getOperator());
			code.add(opcode);							// type-dependent! (opcode is different for floats and for ints)
		}
		private void visitNormalBinaryOperatorNode(OperatorNode node) {
			newValueCode(node);
			ASMCodeFragment arg1 = removeValueCode(node.child(0));
			ASMCodeFragment arg2 = removeValueCode(node.child(1));
			
			code.append(arg1);
			code.append(arg2);
			
			ASMOpcode opcode = opcodeForOperator(node.getOperator());
			code.add(opcode);							// type-dependent! (opcode is different for floats and for ints)
		}
		private ASMOpcode opcodeForOperator(Lextant lextant) {
			assert(lextant instanceof Punctuator);
			Punctuator punctuator = (Punctuator)lextant;
			switch(punctuator) {
			case ADD: 	   		return Add;				// type-dependent!
			case SUBTRACT:		return Negate;			// (unary subtract only) type-dependent!
			case MULTIPLY: 		return Multiply;		// type-dependent!
			default:
				assert false : "unimplemented operator in opcodeForOperator";
			}
			return null;
		}


		///////////////////////////////////////////////////////////////////////////
		// leaf nodes (ErrorNode not necessary)
		public void visit(BooleanConstantNode node) {
			newValueCode(node);
			code.add(PushI, node.getValue() ? 1 : 0);
		}
		public void visit(IdentifierNode node) {
			newAddressCode(node);
			Binding binding = node.getBinding();
			
			binding.generateAddress(code);
		}		
		public void visit(IntegerConstantNode node) {
			newValueCode(node);
			
			code.add(PushI, node.getValue());
		}
		public void visit(FloatingConstantNode node) {
			newValueCode(node);
			
			code.add(PushF, node.getValue());
		}
		public void visit(CharacterConstantNode node) {
			newValueCode(node);
			code.add(PushI, node.getValue());
		}
		public void visit(StringConstantNode node) {
			newValueCode(node);
			String stringLabelName = new Labeller("String").newLabel("StringLabel");
			code.add(DLabel, stringLabelName);
			code.add(DataI, 3);
			code.add(DataI, 9);
			code.add(DataI, node.getValue().length());
			code.add(DataS, node.getValue());
			code.add(PushD, stringLabelName);
		}
	}

}
