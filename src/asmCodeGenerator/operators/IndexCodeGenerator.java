package asmCodeGenerator.operators;

import java.util.List;

import asmCodeGenerator.Labeller;
import asmCodeGenerator.codeStorage.ASMCodeFragment;
import static asmCodeGenerator.codeStorage.ASMOpcode.*;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import asmCodeGenerator.runtime.RunTime;
import parseTree.ParseNode;
import semanticAnalyzer.types.Array;
import semanticAnalyzer.types.Type;

public class IndexCodeGenerator implements SimpleCodeGenerator{

    @Override
    public ASMCodeFragment generate(ParseNode node, List<ASMCodeFragment> args) {
        ASMCodeFragment frag = new ASMCodeFragment(CodeType.GENERATES_ADDRESS);
        Labeller labeller = new Labeller("index");
        String start = labeller.newLabel("start-index");
        String temp = labeller.newLabel("index-temp");
        frag.add(PushD, start);
        frag.add(Exchange);
        frag.add(StoreI);

        frag.add(PushD, temp);
        frag.add(Exchange);
        frag.add(StoreI);

        frag.add(PushD, start);
        frag.add(LoadI);

        frag.add(JumpNeg, RunTime.OUT_OF_BOUNDS_RUNTIME_ERROR);

        frag.add(PushD, start);
        frag.add(LoadI);

        frag.add(PushD, temp);
        frag.add(LoadI);
        frag.add(PushI, 12);
        frag.add(Add);
        frag.add(LoadI);
        frag.add(Subtract);

        frag.add(JumpNeg, RunTime.OUT_OF_BOUNDS_RUNTIME_ERROR);
        String end = labeller.newLabel("end-index");
        frag.add(Label, end);
        frag.add(PushD, temp);
        frag.add(LoadI);
        frag.add(PushI, 16);
        frag.add(Add);
        frag.add(PushD, start);
        frag.add(LoadI);
        Type subtype = ((Array) node.child(0).getType()).getSubtype();
        frag.add(PushI, subtype.getSize());
        frag.add(Multiply);
        frag.add(Add);

        return frag;
    }

    
}
