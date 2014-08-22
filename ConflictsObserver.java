package conflictsAnalyzer;

import java.io.File;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import merger.FSTGenMerger;
import merger.MergeVisitor;

import de.ovgu.cide.fstgen.ast.FSTNode;
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
	
	
	public String retrieveFilePath(FSTNode node){
		
		StringBuffer sb = new StringBuffer(this.fstgenmerger.getFeaturePrintVisitor().getExpressionName());
		sb.setLength(sb.lastIndexOf("."));
		sb.delete(0, sb.lastIndexOf(File.separator) + 1);
		String featurePath = this.fstgenmerger.getFeaturePrintVisitor().getWorkingDir() + File.separator + sb.toString() + this.retrieveFolderPath(node);
		
		return featurePath;
	}
	
	public String retrieveFolderPath(FSTNode node){
		String filePath = "";
		String nodetype = node.getType();
		
		if(nodetype.equals("Java-File") || nodetype.equals("Folder")){
			
			filePath = this.retrieveFolderPath(node.getParent()) + File.separator + node.getName();
			
			return filePath;
			
		}else if(nodetype.equals("Feature")){
			
			return "";
			
		}else{
			
			
			return this.retrieveFolderPath(node.getParent());
		}
		
		
		
		
	}
}
