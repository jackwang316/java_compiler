package asmCodeGenerator.operators;

import java.util.List;

import asmCodeGenerator.Labeller;
import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMOpcode;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import parseTree.ParseNode;

public class CharToBoolCodeGenerator implements SimpleCodeGenerator{
	@Override
	public ASMCodeFragment generate(ParseNode node, List<ASMCodeFragment> args) {
		ASMCodeFragment frag = new ASMCodeFragment(CodeType.GENERATES_VALUE);
		Labeller label = new Labeller("char-to-boolean-cast");
		String isTrueLabel  = label.newLabel("true");
		String isFalseLabel = label.newLabel("false");
		String join  = label.newLabel("join");

		frag.add(ASMOpcode.JumpTrue, isTrueLabel);
		frag.add(ASMOpcode.Jump, isFalseLabel);

		frag.add(ASMOpcode.Label, isTrueLabel);
		frag.add(ASMOpcode.PushI, 1);
		frag.add(ASMOpcode.Jump, join);
		frag.add(ASMOpcode.Label, isFalseLabel);
		frag.add(ASMOpcode.PushI, 0);
		frag.add(ASMOpcode.Jump, join);
		frag.add(ASMOpcode.Label, join);
		return frag;
	}
}