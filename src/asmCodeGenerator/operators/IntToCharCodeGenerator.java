package asmCodeGenerator.operators;

import java.util.List;

import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import asmCodeGenerator.codeStorage.ASMOpcode;
import parseTree.ParseNode;

public class IntToCharCodeGenerator implements SimpleCodeGenerator{
	public final static int _7bitANDValue = 127;

	@Override
	public ASMCodeFragment generate(ParseNode node, List<ASMCodeFragment> args) {
		ASMCodeFragment frag = new ASMCodeFragment(CodeType.GENERATES_VALUE);
		frag.add(ASMOpcode.PushI, _7bitANDValue);
		frag.add(ASMOpcode.BTAnd);
		return frag;
	}
	
}