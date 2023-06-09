package asmCodeGenerator.operators;

import java.util.List;

import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import parseTree.ParseNode;

public class CharToIntGenerator implements SimpleCodeGenerator{

	@Override
	public ASMCodeFragment generate(ParseNode node, List<ASMCodeFragment> args) {
		return new ASMCodeFragment(CodeType.GENERATES_VALUE);
	}

}
