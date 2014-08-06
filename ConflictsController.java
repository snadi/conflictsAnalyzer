package conflictsAnalyzer;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import de.ovgu.cide.fstgen.ast.FSTTerminal;


public class ConflictsController {
	

	
	private ArrayList<Conflict> conflictsList;
	
	private Hashtable<String, Integer> conflictsReport;
	
	
	public ConflictsController(String revisionFilePath){
		
		//identify conflicting nodes
		FSTNodeParser parser = new FSTNodeParser();
		ArrayList<FSTTerminal> conflictingNodes = parser.identifyConflictingNodes(revisionFilePath);
		
		//match the conflicting nodes with their respective pattern
		this.conflictsList = new ArrayList<Conflict>();
		this.identifyConflictsPatterns(conflictingNodes);
		
		//compute the report
		this.conflictsReport = new Hashtable<String, Integer>();
		this.reportConflicts();
		
		//print the report
		ConflictPrinter cp = new ConflictPrinter();
		cp.writeConflictsReport(this.conflictsReport);
		
	}
	
	

	public ArrayList<Conflict> getConflictsList() {
		return conflictsList;
	}

	public void setConflictsList(ArrayList<Conflict> conflictsList) {
		this.conflictsList = conflictsList;
	}
	
	public void identifyConflictsPatterns(ArrayList<FSTTerminal> conflictingNodes){
		
		for (FSTTerminal node : conflictingNodes){
			
			
			Conflict conflict = this.matchConflict(node);
			
			if(conflict.type != null){
			conflict.setBody(node.getBody());
			this.conflictsList.add(conflict);
			}
			
		}
		
	}
	
	public Conflict matchConflict(FSTTerminal node){
		Conflict conflict = new Conflict();
		
		String type = node.getType();
		
		if(type.equals("Modifiers")){
			
			conflict.setType(SSMergeConflicts.ModifierList.toString());
		
		}else if(type.equals("AnnotationMethodDecl")){
			
			conflict.setType(SSMergeConflicts.DefaultValue.toString());
			
		}else if(type.equals("ImplementsList")){
			
			conflict.setType(SSMergeConflicts.ImplementList.toString());
			
		}else if(type.equals("FieldDecl") ){
			
			String [] fd = node.getBody().split(FSTNodeParser.SSMERGE_SEPARATOR);
			
			if(fd[1].equals(" ")){
				
				conflict.setType(SSMergeConflicts.SameIdFd.toString());
				
			}else{
				conflict.setType(SSMergeConflicts.LineBasedMCFd.toString());
			}
			
		}
		
		else if(type.equals("MethodDecl") || type.equals("ConstructorDecl")){
			
			String body = node.getBody();
			String [] p1 = body.split("\\|\\|\\|\\|\\|\\|\\|");
			String [] p2 = p1[1].split("=======");
			String a = p2[0].substring(1, p2[0].length()-1);
			
			if(a.contains(" ")){
				
				conflict.setType(SSMergeConflicts.LineBasedMCFd.toString());
			}else{
				
				conflict.setType(SSMergeConflicts.SameSignatureCM.toString());
				
			}
			
		}

		
		return conflict;
	}
	

	
	public void reportConflicts(){
		
		for(SSMergeConflicts c : SSMergeConflicts.values()){
			
			String type = c.toString();
			int quantity = this.countConflicts(type);
			this.conflictsReport.put(type, quantity);
		}
		
	}
	
	public int countConflicts(String type){
		
		int result = 0;
		
		for(Conflict c : this.conflictsList){
			
			if(c.type.equals(type)){
				
				result++;
			}
			
		}
		
		return result;
	}
	
	

	public Map<String, Integer> getConflictsReport() {
		return conflictsReport;
	}



	public void setConflictsReport(Hashtable<String, Integer> conflictsReport) {
		this.conflictsReport = conflictsReport;
	}



	public static void main(String[] args) {
		String file = "/Users/paolaaccioly/gitClones/fse_2011_artifacts/examples/SSMergeCatalog/6/rev_6.revisions";
		ConflictsController cc = new ConflictsController(file);
		System.out.println(cc.getConflictsReport().toString());
		
		
	}
	

}