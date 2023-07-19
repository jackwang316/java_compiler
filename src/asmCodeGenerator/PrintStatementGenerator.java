package asmCodeGenerator;

import static asmCodeGenerator.codeStorage.ASMOpcode.*;

import javax.crypto.Mac;

import parseTree.ParseNode;
import parseTree.nodeTypes.NewlineNode;
import parseTree.nodeTypes.PrintStatementNode;
import parseTree.nodeTypes.SpaceNode;
import parseTree.nodeTypes.TabSpaceNode;
import semanticAnalyzer.types.Array;
import semanticAnalyzer.types.PrimitiveType;
import semanticAnalyzer.types.Type;
import asmCodeGenerator.ASMCodeGenerator.CodeVisitor;
import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMOpcode;
import asmCodeGenerator.operators.CharToBoolCodeGenerator;
import asmCodeGenerator.runtime.RunTime;

public class PrintStatementGenerator {
	ASMCodeFragment code;
	ASMCodeGenerator.CodeVisitor visitor;
	public static int ARR_SUBTYPE_POS_START = 8;
	public static int ARR_LENGTH_POS_START = 12;
	public static int ARR_ELEM_START = 16;
	
	public PrintStatementGenerator(ASMCodeFragment code, CodeVisitor visitor) {
		super();
		this.code = code;
		this.visitor = visitor;
	}

	public void generate(PrintStatementNode node) {
		for(ParseNode child : node.getChildren()) {
			if(child instanceof NewlineNode || child instanceof SpaceNode || child instanceof TabSpaceNode) {
				ASMCodeFragment childCode = visitor.removeVoidCode(child);
				code.append(childCode);
			}
			else {
				appendPrintCode(child);
			}
		}
	}

	private void appendPrintCode(ParseNode node) {
		code.append(visitor.removeValueCode(node));
		makeStringPrintable(node);
		convertToStringIfBoolean(node);
		makeArrayPrintable(node);
		if(node.getType() instanceof Array == false){
			String format = printFormat(node.getType());
			code.add(PushD, format);
			code.add(Printf);
		}
	}
	
	private void makeArrayPrintable(ParseNode node) {
		if(node.getType() instanceof Array == false){
			return;
		}

		Type subtype = ((Array) node.getType()).getSubtype();


		Labeller labeller = new Labeller("array-printing");
		String startLabel = labeller.newLabel("start");
		String loopLabel  = labeller.newLabel("loop");
		String endLabel  = labeller.newLabel("end");
		
		String lengthAddr = labeller.newLabel("length-addr");
		String elemAddr = labeller.newLabel("elem-start-addr");
		String typeAddr = labeller.newLabel("type-addr");

		initLocation(lengthAddr);
		initLocation(elemAddr);
		initLocation(typeAddr);

		code.add(Label, startLabel);
		
		Macros.loadIFrom(code, RunTime.ARR_LOC);
		Macros.readIOffset(code, ARR_LENGTH_POS_START);
		Macros.storeITo(code, lengthAddr);
		
		Macros.loadIFrom(code, RunTime.ARR_LOC);
		Macros.readCOffset(code, ARR_SUBTYPE_POS_START);
		Macros.storeITo(code, typeAddr);

		code.add(PushD, elemAddr);	
		code.add(PushI, ARR_ELEM_START);
		code.add(StoreI);
		
		code.add(PushI, '[');
		code.add(PushD, RunTime.CHARACTER_PRINT_FORMAT);
		code.add(Printf);

		code.add(Label, loopLabel);
		Macros.loadIFrom(code, lengthAddr);
		code.add(JumpFalse, endLabel);

		Macros.loadIFrom(code, RunTime.ARR_LOC);
		Macros.loadIFrom(code, elemAddr);
		code.add(Add);
			
		code.add(getAddressCode(subtype));

		if(subtype == PrimitiveType.BOOLEAN) {
			convertToBool();
		}

		code.add(PushD, printFormat(subtype));
		code.add(Printf);
		
		Macros.loadIFrom(code, elemAddr);
		Macros.loadIFrom(code, typeAddr);
		code.add(Add);

		Macros.storeITo(code, elemAddr);

		Macros.loadIFrom(code, lengthAddr);
		code.add(PushI, 1);
		code.add(Subtract);
		Macros.storeITo(code, lengthAddr);
			
		Macros.loadIFrom(code, lengthAddr);
		code.add(JumpFalse, endLabel);
			
		code.add(PushI, ',');
		code.add(PushD, RunTime.CHARACTER_PRINT_FORMAT);
		code.add(Printf);
		code.add(PushI, ' ');
		code.add(PushD, RunTime.CHARACTER_PRINT_FORMAT);
		code.add(Printf);
			
		code.add(Jump, loopLabel);
	
		code.add(Label, endLabel);
		
		code.add(PushI, ']');
		code.add(PushD, RunTime.CHARACTER_PRINT_FORMAT);
		code.add(Printf);
	}

	private void initLocation(String location) {
		code.add(DLabel, location);
		code.add(DataI, 0);
	}

	public ASMOpcode getAddressCode(Type subtype) {
			if(subtype == PrimitiveType.INTEGER) {
				return LoadI;
			}	
			else if(subtype == PrimitiveType.BOOLEAN) {
				return LoadC;
			}	
			else if(subtype == PrimitiveType.CHARACTER) {
				return LoadC;
			}
			else if(subtype == PrimitiveType.STRING) {
				return LoadC;
			}
			else if(subtype == PrimitiveType.FLOATING) {
				return LoadF;
			}	
			else if(subtype instanceof Array) {
				return LoadI;
			}
			else{
				return null;
			}
		}

	private void makeStringPrintable(ParseNode node) {
		if(node.getType() != PrimitiveType.STRING) {
			return;
		}
		code.add(PushD, RunTime.STR_LOC);
		code.add(LoadI);
		code.add(PushI, 12);
		code.add(Add);
	}
	private void convertToStringIfBoolean(ParseNode node) {
		if(node.getType() != PrimitiveType.BOOLEAN) {
			return;
		}
		
		convertToBool();
	}

	private void convertToBool() {
		Labeller labeller = new Labeller("print-boolean");
		String trueLabel = labeller.newLabel("true");
		String endLabel = labeller.newLabel("join");

		code.add(JumpTrue, trueLabel);
		code.add(PushD, RunTime.BOOLEAN_FALSE_STRING);
		code.add(Jump, endLabel);
		code.add(Label, trueLabel);
		code.add(PushD, RunTime.BOOLEAN_TRUE_STRING);
		code.add(Label, endLabel);
	}


	private static String printFormat(Type type) {
		assert type instanceof PrimitiveType;
		
		switch((PrimitiveType)type) {
		case INTEGER:	return RunTime.INTEGER_PRINT_FORMAT;
		case BOOLEAN:	return RunTime.BOOLEAN_PRINT_FORMAT;
		case FLOATING: 	return RunTime.FLOATING_PRINT_FORMAT;
		case CHARACTER: return RunTime.CHARACTER_PRINT_FORMAT;
		case STRING: 	return RunTime.STRING_PRINT_FORMAT;
		default:		
			assert false : "Type " + type + " unimplemented in PrintStatementGenerator.printFormat()";
			return "";
		}
	}
}
