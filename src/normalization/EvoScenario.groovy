package normalization

import java.util.Observable

import main.ExtractorResult;
import main.MergeCommit
import merger.FSTGenMerger;
import util.CompareFiles;

class EvoScenario implements Observer{
	
	String name
	
	ExtractorResult extractorResult
	
	boolean isMergeCommit
	
	int numberOfChangesoOutsideMethods
	
	int numberOfChangesInsideMethodsChunks
	
	int numberOfChangesInsideMethodsLines
	
	private FSTGenMerger fstGenMerge
	
	public EvoScenario(MergeCommit mc, ExtractorResult er){
		this.setIsMergeCommit(mc)
		this.extractorResult = er
		this.setName()
		this.preProcessFiles()
	}
	
	public void setName(){
		String [] temp = this.extractorResult.revisionFile.split('/')
		String revFile = temp[temp.length -1]
		this.name = revFile.substring(0, revFile.length()-10)
	}
	
	public void preProcessFiles(){
		CompareFiles cp = new CompareFiles(this.extractorResult.revisionFile)
		cp.removeNonJavaFiles()
		cp.removeEqualFiles()
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
		//TODO
	}
	
	public void analyseChanges (){
		this.fstGenMerge = new FSTGenMerger()
		fstGenMerge.getMergeVisitor().addObserver(this)
		String[] files = ["--expression", this.extractorResult.revisionFile]
		fstGenMerge.run(files)
	}
	
	public static void main (String[] args){
		MergeCommit mc = new MergeCommit()
		mc.sha = 'd6a2526b5420125ba543282720d7036340e2c7e0'
		mc.parent1 = '80b2502eff13fb63e8e875dbbe5356ef306940e7'
		mc.parent2 = '2fe0652b877b2bca7dabd9416d007c1c6dd87043'
		ExtractorResult er = new ExtractorResult()
		er.revisionFile = '/Users/paolaaccioly/Documents/Doutorado/workspace_fse/downloads/TGM/revisions/rev_d6a25/rev_80b25-2fe06.revisions'
		EvoScenario evo = new EvoScenario(mc, er)
	}
}
