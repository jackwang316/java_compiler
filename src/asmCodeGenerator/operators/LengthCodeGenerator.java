package asmCodeGenerator.operators;

import java.util.List;

import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMOpcode;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import asmCodeGenerator.runtime.RunTime;
import parseTree.ParseNode;

public class LengthCodeGenerator implements SimpleCodeGenerator{
    public static final int LENGTH_OFFSET = 12;

    @Override
    public ASMCodeFragment generate(ParseNode node, List<ASMCodeFragment> args) {
        ASMCodeFragment frag = new ASMCodeFragment(CodeType.GENERATES_VALUE);
        frag.add(ASMOpcode.PushD, RunTime.ARR_LOC);
		frag.add(ASMOpcode.LoadI);
		frag.add(ASMOpcode.Duplicate);
		frag.add(ASMOpcode.JumpFalse, RunTime.NEGATIVE_INDEX_RUNTIME_ERROR);
		frag.add(ASMOpcode.PushI, LENGTH_OFFSET);
		frag.add(ASMOpcode.Add);
		frag.add(ASMOpcode.LoadI);

		return frag;
    }

}
