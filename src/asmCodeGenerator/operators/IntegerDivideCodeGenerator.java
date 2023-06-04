package asmCodeGenerator.operators;

import java.util.List;

import parseTree.ParseNode;
import asmCodeGenerator.codeStorage.ASMCodeFragment;


public interface IntegerDivideCodeGenerator {
	public ASMCodeFragment generate(ParseNode node, List<ASMCodeFragment> args);
}
