package conflictsAnalyzer;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import merger.FSTGenMerger;
import merger.MergeVisitor;

import de.ovgu.cide.fstgen.ast.FSTTerminal;

public class FSTNodeParser implements Observer{
	
	static final String SSMERGE_SEPARATOR = "##FSTMerge##";
	public static final String DIFF3MERGE_SEPARATOR = "|||||||";
	
	private ArrayList<FSTTerminal> conflictingNodes;
	
	public FSTNodeParser(){
			
		this.conflictingNodes = new ArrayList<FSTTerminal>();
	}
	
	public void getConflictingNodes (String revisionFilePath){
		
		this.runFSTMerger(revisionFilePath);
		
		
	}
	
	
	public void runFSTMerger(String revisionFilePath){
		
		FSTGenMerger merger = new FSTGenMerger();
		merger.getMergeVisitor().addObserver(this);
		String files[] = {"--expression", revisionFilePath}; 
		merger.run(files);

		
		
	}
	



	public ArrayList<FSTTerminal> getConflictingNodes() {
		return conflictingNodes;
	}


	public void setConflictingNodes(ArrayList<FSTTerminal> conflictingNodes) {
		this.conflictingNodes = conflictingNodes;
	}


	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		if(o instanceof MergeVisitor && arg instanceof FSTTerminal){
			
			FSTTerminal node = (FSTTerminal) arg;
			
			if(!node.getType().contains("-Content")){
				
				this.conflictingNodes.add(node);
			}
			
			
		}
	}
}
