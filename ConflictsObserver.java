package conflictsAnalyzer;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import merger.FSTGenMerger;
import merger.MergeVisitor;

import de.ovgu.cide.fstgen.ast.FSTTerminal;

public class ConflictsObserver implements Observer{
	
	static final String SSMERGE_SEPARATOR = "##FSTMerge##";
	public static final String DIFF3MERGE_SEPARATOR = "|||||||";
	
	private ArrayList<Conflict> conflictsList;
	
	private FSTGenMerger fstgenmerger;
	
	public ConflictsObserver(){
			
		this.conflictsList = new ArrayList<Conflict>();
		this.fstgenmerger = new FSTGenMerger();
	}
	
	public void getConflictingNodes (String revisionFilePath){
		
		this.runFSTMerger(revisionFilePath);
		
		
	}
	
	
	public void runFSTMerger(String revisionFilePath){
		
		
		this.fstgenmerger.getMergeVisitor().addObserver(this);
		String files[] = {"--expression", revisionFilePath}; 
		this.fstgenmerger.run(files);

		
		
	}
	



	public ArrayList<Conflict> getConflictingNodes() {
		return conflictsList;
	}


	public void setConflictingNodes(ArrayList<Conflict> conflictingNodes) {
		this.conflictsList = conflictingNodes;
	}


	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		if(o instanceof MergeVisitor && arg instanceof FSTTerminal){
			
			FSTTerminal node = (FSTTerminal) arg;
			
			if(!node.getType().contains("-Content")){
				
				Conflict conflict = this.createConflict(node);			
				this.conflictsList.add(conflict);
				
			}
			
			
		}
	}
	
	public Conflict createConflict(FSTTerminal node){
		
		Conflict conflict = new Conflict();
		conflict.setType("");
		conflict.setNodeType(node.getType());
		conflict.setBody(node.getBody());
		String filePath = this.retrieveFilePath(node);
		conflict.setFilePath(filePath);
		
		return conflict;
	}
	
	public String retrieveFilePath(FSTTerminal node){
		String filePath = "";
		
		
		return filePath;
	}
}
