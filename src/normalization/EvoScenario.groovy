package normalization

import java.util.Observable

import main.ExtractorResult;
import main.MergeCommit

class EvoScenario implements Observer{
	
	String name
	
	boolean isMergeCommit
	
	int numberOfChangesoOutsideMethods
	
	int numberOfChangesInsideMethodsChunks
	
	int numberOfChangesInsideMethodsLines
	
	public EvoScenario(MergeCommit mc, ExtractorResult er){
		this.setIsMergeCommit(mc)
		this.name = er.revisionFile
	}
	
	public void setIsMergeCommit(MergeCommit mc){
		if(!mc.parent2.equals('')){
			this.isMergeCommit = true
		}else{
			this.isMergeCommit = false
		}
	}
	
	@Override
	public void update(Observable o, Object arg) {
		
	}
	
	public void analyseChanges (){
		//TODO
	}
}
