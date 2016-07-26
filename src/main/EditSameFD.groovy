package main

import de.ovgu.cide.fstgen.ast.FSTTerminal;

class EditSameFD extends ConflictPredictor{
	
	public EditSameFD(FSTTerminal node, String mergeScenarioPath){
		super(node, mergeScenarioPath)
	}
	
	@Override
	public void callBlame() {
		if(!this.diffSpacing){
			this.prepareForBlame()
			File[] files = this.createTempFiles()
			Blame blame = new Blame()
			String result = blame.annotateBlame(files[0], files[1], files[2])
			this.node.setBody(result)
			this.deleteTempFiles(files)
		}
		
	}
	
	private void prepareForBlame(){
		String [] nodeBody = this.node.getBody().split('\\s+')
		String result = ''
		for(String s : nodeBody){
			result = result + s + '\n'
		}
		this.node.setBody(result)
	}
	
	@Override
	public void setSignature() {
		this.signature = this.node.getName()
	}
}
