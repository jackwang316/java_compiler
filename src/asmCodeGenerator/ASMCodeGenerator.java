package asmCodeGenerator;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.PseudoColumnUsage;
import java.util.ArrayList;

import asmCodeGenerator.codeStorage.ASMCodeChunk;
import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
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
import parseTree.nodeTypes.IfStatementNode;
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
import parseTree.nodeTypes.WhileStatementNode;
import parseTree.nodeTypes.StringConstantNode;
import parseTree.nodeTypes.SubrBlockNode;
import parseTree.nodeTypes.SubrCallNode;
import parseTree.nodeTypes.SubrDefinitionNode;
import parseTree.nodeTypes.SubrInvokeNode;
import parseTree.nodeTypes.SubrParameterNode;
import parseTree.nodeTypes.SubrTypeNode;
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
	private static final int STRING_ID = 3;
	private static final int STRING_HEADER_LENGTH = 12;
	private static final int STRING_STATUS = 5;

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
		code.append(initCallStack());
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

	private ASMCodeFragment initCallStack() {
		ASMCodeFragment code = new ASMCodeFragment(GENERATES_VOID);
		code.add(DLabel, RunTime.STACK_POINTER);
		code.add(DataI, 0);
		code.add(DLabel, RunTime.FRAME_POINTER);
		code.add(DataI, 0);
		code.add(DLabel, RunTime.STACK_POINTER);
		code.add(Memtop);
		code.add(StoreI);
		code.add(PushD, RunTime.FRAME_POINTER);
		code.add(Memtop);
		code.add(StoreI);
		return code;
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
				code.add(LoadI);
			}
			else if(node.getType() instanceof Array) {
				code.add(LoadI);
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
		public void visitLeave(IfStatementNode node) {
			newVoidCode(node);
			ASMCodeFragment expressionCode = removeValueCode(node.child(0));
			ASMCodeFragment ifBodyCode = removeVoidCode(node.child(1));

			Labeller labeller = new Labeller("if-statement");
			String elseLabel  = labeller.newLabel("else");
			String endLabel   = labeller.newLabel("end");

			code.append(expressionCode);
			code.add(JumpFalse, elseLabel);
			code.append(ifBodyCode);
			code.add(Jump, endLabel);
			code.add(Label, elseLabel);
			if(node.nChildren() == 3) {
				ASMCodeFragment elseBodyCode = removeVoidCode(node.child(2));
				code.append(elseBodyCode);
			}
			code.add(Label, endLabel);
		}
		public void visitLeave(WhileStatementNode node) {
			newVoidCode(node);
			ASMCodeFragment expressionCode = removeValueCode(node.child(0));
			ASMCodeFragment whileBodyCode = removeVoidCode(node.child(1));

			Labeller labeller = new Labeller("while-statement");
			String startLabel = labeller.newLabel("start");
			String endLabel   = labeller.newLabel("end");

			code.add(Label, startLabel);
			code.append(expressionCode);
			code.add(JumpFalse, endLabel);
			code.append(whileBodyCode);
			code.add(Jump, startLabel);
			code.add(Label, endLabel);
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
			ASMCodeFragment rvalue = removeValueCode(node.child(1));
			if(node.child(0) instanceof IndexNode){
				IndexNode indexNode = (IndexNode) node.child(0);
				int index = Integer.parseInt(indexNode.child(1).getToken().getLexeme());
				Type subType = ((Array) indexNode.child(0).getType()).getSubtype();
				int subTypeSize = subType.getSize();
				Macros.loadIFrom(code, RunTime.ARR_LOC);
				code.add(Duplicate);
				code.add(PushI, 12);
				code.add(Add);
				code.add(LoadI);
				code.add(PushI, index + 1);
				code.add(Subtract);
				code.add(JumpNeg, RunTime.OUT_OF_BOUNDS_RUNTIME_ERROR);
				Macros.loadIFrom(code, RunTime.ARR_LOC);
				code.add(Duplicate);
				appendToPtr(code, RunTime.ARR_LOC, HEADER_LENGTH + (index * subTypeSize), rvalue, opcodeForStore(subType));
			}

			ASMCodeFragment lvalue = removeAddressCode(node.child(0));	
			
			
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

		private ASMOpcode opcodeForLoad(Type type) {
			if(type == PrimitiveType.INTEGER) {
				return LoadI;
			}
			if(type == PrimitiveType.BOOLEAN) {
				return LoadC;
			}
			if(type == PrimitiveType.CHARACTER) {
				return LoadC;
			}
			if(type == PrimitiveType.STRING) {
				return LoadI;
			}
			if(type == PrimitiveType.FLOATING) {
				return LoadF;
			}
			if(type instanceof Array) {
				return LoadI;
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

		public void visitEnter(SubrDefinitionNode node) {
			newVoidCode(node);
			ASMCodeFragment child = removeVoidCode(node.child(1));
			code.append(child);
		}

		public void visitEnter(SubrBlockNode node) {
			node.generateLabels();
		}

		public void visitLeave(SubrBlockNode node) {
			newValueCode(node);

			code.add(Label, node.getStartLabel());

			// Put return address on Frame Stack below Dynamic Link
			code.add(PushD, RunTime.STACK_POINTER, "%% store return addr.");
			code.add(LoadI);
			code.add(PushI, -8);
			code.add(Add);
			code.add(Exchange);
			code.add(StoreI);

			// Store Dynamic Link (current value of Frame Pointer) below the Stack Pointer
			code.add(PushD, RunTime.STACK_POINTER, "%% store dyn. link");
			code.add(LoadI);
			code.add(PushI, -4);
			code.add(Add);
			code.add(PushD, RunTime.FRAME_POINTER);
			code.add(LoadI);
			code.add(StoreI);

			// Move Frame Pointer to Stack Pointer
			code.add(PushD, RunTime.FRAME_POINTER, "%% move frame pointer");
			code.add(PushD, RunTime.STACK_POINTER);
			code.add(LoadI);
			code.add(StoreI);

			// Move Stack Pointer to end of frame
			code.add(PushD, RunTime.STACK_POINTER, "%% move stack pointer");
			code.add(PushD, RunTime.STACK_POINTER);
			code.add(LoadI);
			code.add(PushI, node.getFrameSize());
			code.add(Subtract);
			code.add(StoreI);


			// Lambda execution code
			ASMCodeFragment childCode = removeVoidCode(node.child(1));
			code.append(childCode);


			// Runoff error handling
			code.add(Label, node.getExitErrorLabel());
			code.add(Jump, RunTime.FUNCTION_RUNOFF_RUNTIME_ERROR);

			// Exit handshake
			code.add(Label, node.getExitHandshakeLabel());

			// Push the return address onto the accumulator stack
			code.add(PushD, RunTime.FRAME_POINTER, "%% get return addr.");
			code.add(LoadI);
			code.add(PushI, -8);
			code.add(Add);
			code.add(LoadI);

			// Replace the Frame Pointer with the dynamic link
			code.add(PushD, RunTime.FRAME_POINTER, "%% restore frame pointer");
			code.add(PushD, RunTime.FRAME_POINTER);
			code.add(LoadI);
			code.add(PushI, -4);
			code.add(Add);
			code.add(LoadI);
			code.add(StoreI);

			// Move Stack Pointer above current Parameter Scope
			code.add(PushD, RunTime.STACK_POINTER, "%% pop frame stack");
			code.add(PushD, RunTime.STACK_POINTER);
			code.add(LoadI);
			code.add(PushI, node.getFrameSize());
			code.add(Add);
			code.add(PushI, node.getArgSize());
			code.add(Add);

			// Decrease the stack pointer by the return value size
			Type returnType = node.getReturnType();
			code.add(PushI, returnType.getSize(), "%% store return val.");
			code.add(Subtract);
			code.add(StoreI);

			// Bring the return value back to the top of the ASM accumulator stack.
			// (Swap return value with return address)
			code.add(Exchange);

			// Store return value
			code.add(PushD, RunTime.STACK_POINTER);
			code.add(LoadI);
			code.add(Exchange);

			code.add(opcodeForStore(returnType));

			code.add(Return);
		}

		public void visitLeave(SubrTypeNode node) {
		}

		public void visitLeave(SubrParameterNode node) {
		}

		public void visitLeave(SubrCallNode node) {
			newVoidCode(node);
		}

		public void visitLeave(SubrInvokeNode node) {
			newValueCode(node);


			for (int i = 1; i < node.nChildren(); i++) {
				Type argType = node.child(i).getType();
				int argSize = argType.getSize();

				// Move Stack Pointer
				ASMCodeFragment argFrag = new ASMCodeFragment(CodeType.GENERATES_VALUE);
				argFrag.add(PushD, RunTime.STACK_POINTER);
				argFrag.add(PushD, RunTime.STACK_POINTER);
				argFrag.add(LoadI);
				argFrag.add(PushI, argSize);
				argFrag.add(Subtract);
				argFrag.add(StoreI);
				code.append(argFrag);

				// Put argument value
				code.add(PushD, RunTime.STACK_POINTER, "%% store arg " + i);
				code.add(LoadI);
				ASMCodeFragment argValue = removeValueCode(node.child(i));
				code.append(argValue);

				code.add(opcodeForStore(argType));
			}

			if (node.child(0) instanceof IdentifierNode) {				
				ASMCodeFragment identifier = removeAddressCode(node.child(0));
				code.append(identifier);
				code.add(LoadI);
			}

			// Call function
			code.add(CallV);

			// Get return value
			Type returnType = node.getType();
			code.add(PushD, RunTime.STACK_POINTER);
			code.add(LoadI);
			code.add(opcodeForLoad(returnType));

			// Move the Stack Pointer up by the size of the return value
			code.add(PushD, RunTime.STACK_POINTER, "%% restore stack pointer");
			code.add(PushD, RunTime.STACK_POINTER);
			code.add(LoadI);
			code.add(PushI, returnType.getSize());
			code.add(Add);
			code.add(StoreI);
		}

		public void visitLeave(ReturnNode node) {
			newVoidCode(node);
			ASMCodeFragment returnValue = removeValueCode(node.child(0));
			code.append(returnValue);
			SubrBlockNode lambda = (SubrBlockNode) node.getLambda();
			code.add(Jump, lambda.getExitHandshakeLabel());
		}

		public void visitLeave(SubrDefinitionNode node) {
			newVoidCode(node);

			ASMCodeFragment childCode = removeValueCode(node.child(1));
			code.add(DLabel, node.getLabel());
			code.append(childCode);
		}

		public void visitLeave(IndexNode node){
			newAddressCode(node);
			int index = Integer.parseInt(node.child(1).getToken().getLexeme());
			Macros.loadIFrom(code, RunTime.ARR_LOC);
			code.add(Duplicate);
			code.add(PushI, 12);
			code.add(Add);
			code.add(LoadI);
			code.add(PushI, index);
			code.add(Subtract);
			code.add(JumpNeg, RunTime.OUT_OF_BOUNDS_RUNTIME_ERROR);
			code.add(Duplicate);
			code.add(PushI, HEADER_LENGTH);
			code.add(Add);
			code.add(LoadI);
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
				Type type = node.getSubtype();
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
				Type type = node.getSubtype();
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
					ASMCodeFragment frag = removeValueCode(children.get(i));
					appendToPtr(code, tempLoc, HEADER_LENGTH + i * typeSize, frag, opcodeForStore(type));
				}
				code.add(Duplicate);
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
			String value = node.getValue();
			int totalLength = (value.length() + 1) * PrimitiveType.CHARACTER.getSize() + STRING_HEADER_LENGTH;
			String loc = RunTime.STR_LOC;
			code.add(PushI, totalLength);
			code.add(Call, MemoryManager.MEM_MANAGER_ALLOCATE);
			code.add(PushD, loc);
			code.add(Exchange);
			code.add(StoreI);
			appendToPtr(code, loc, 0, STRING_ID);
			appendToPtr(code, loc, ADDRESS_LENGTH, STRING_STATUS);
			appendToPtr(code, loc, 2 * ADDRESS_LENGTH, value.length());
			
			char[] strArr = value.toCharArray();

			for(int i = 0; i < strArr.length; i++) {
				ASMCodeFragment frag = new ASMCodeFragment(GENERATES_VALUE);
				frag.add(PushI, strArr[i]);
				appendToPtr(code, loc, STRING_HEADER_LENGTH + (i * PrimitiveType.CHARACTER.getSize()), frag, StoreC);
			}

			ASMCodeFragment frag = new ASMCodeFragment(GENERATES_VALUE);
			frag.add(PushI, 0);
			appendToPtr(code, loc, STRING_HEADER_LENGTH + (strArr.length * PrimitiveType.CHARACTER.getSize()), frag, StoreC);

			Macros.loadIFrom(code, loc);
			code.add(Duplicate);
			code.add(PushI, 12);
			code.add(Add);
			code.add(LoadI);
		}
	}

}
