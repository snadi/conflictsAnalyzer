package conflictsAnalyzer;

import java.util.ArrayList;
import java.util.LinkedList;

import merger.FSTGenMerger;

import de.ovgu.cide.fstgen.ast.FSTNode;
import de.ovgu.cide.fstgen.ast.FSTNonTerminal;
import de.ovgu.cide.fstgen.ast.FSTTerminal;

public class FSTNodeParser {
	
	static final String SSMERGE_SEPARATOR = "##FSTMerge##";
	static final String DIFF3MERGE_SEPARATOR = "|||||||";
	
	public FSTNodeParser(){
		
		
	}
	
	public ArrayList<FSTTerminal> identifyConflictingNodes (String revisionFilePath){
		
		ArrayList<FSTTerminal> result = null;
		
		FSTNonTerminal mergedNode = this.getMergedNode(revisionFilePath);
		LinkedList<FSTNode> terminalNodes = this.getTerminalNodes(mergedNode);
		result = this.getConflictingNodes(terminalNodes);
		
		return result;
		
	}
	
	
	public FSTNonTerminal getMergedNode(String revisionFilePath){
		
		FSTGenMerger merger = new FSTGenMerger();
		
		String files[] = {"--expression", revisionFilePath}; 
		merger.run(files);
		FSTNonTerminal mergedNode = (FSTNonTerminal) merger.getMergedNode();
		
		return mergedNode;
		
		
	}
	
	public  LinkedList<FSTNode> getTerminalNodes(FSTNonTerminal mergedNode) {
		//FSTNode node
		boolean reachedTerminalNodes = false;
		FSTNonTerminal node = mergedNode;
		LinkedList<FSTNode> result = null;
		
		while(!reachedTerminalNodes){
			
			if( (!node.getChildren().isEmpty()) && (node.getChildren().get(0)instanceof FSTTerminal)){
				
				result = (LinkedList<FSTNode>) node.getChildren();
				reachedTerminalNodes = true;
				
			}else{
				node = (FSTNonTerminal) node.getChildren().get(0);
			}
			
		}
		
		return result;
		
	}
	
	public ArrayList<FSTTerminal> getConflictingNodes(LinkedList<FSTNode> terminalNodes){
		
		ArrayList<FSTTerminal> result = new ArrayList<FSTTerminal>();
		FSTTerminal temp = null;
		for(FSTNode node : terminalNodes){
			
			temp = (FSTTerminal) node;
			if(temp.getBody().contains(SSMERGE_SEPARATOR) || temp.getBody().contains(DIFF3MERGE_SEPARATOR) ){
				result.add(temp);
			}
			
		}
		
		return result;
	}
}
