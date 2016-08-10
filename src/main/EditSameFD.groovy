package main

import de.ovgu.cide.fstgen.ast.FSTTerminal;

class EditSameFD extends ConflictPredictor{
	
	public EditSameFD(FSTTerminal node, String mergeScenarioPath){
		super(node, mergeScenarioPath)
	}
	
	@Override
	public void setSignature() {
		this.signature = this.node.getName()
	}
}
