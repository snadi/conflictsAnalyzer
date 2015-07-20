package main

import de.ovgu.cide.fstgen.ast.FSTTerminal
import java.util.Observable;
import merger.MergeVisitor

class MergedFile {

	private ArrayList<Conflict> conflicts
	
	private String path
	
	private int numberOfConflicts
	
	private int methodsWithConflicts
	
	private int conflictsInsideMethods

	public MergedFile(String path){
		this.path = path
		this.conflicts = new ArrayList<Conflict>()
		
	}

	public int getNumberOfConflicts(){

		return this.numberOfConflicts
	}
	
	public String getPath(){
		return this.path
	}
	
	public ArrayList<Conflict> getConflicts(){
		return this.conflicts
	}
	
	public boolean hasConflicts(){
		boolean hasConflicts = false
		if(this.conflicts.size != 0){
			hasConflicts = true
		}
		return hasConflicts
	}
	
	public int countConflictsDueToDifferentSpacing(){
		int result = 0;
		for (Conflict c : this.conflicts){
			result = result + c.getDifferentSpacing()
		}

		return result;
	}
	
	private int countConflictsInsideMethods(){
		int result = 0;
		
		return result;
	}
	
	private int countMethodsWithConflicts(){
		int result = 0;
		
		return result;
	}
	
	private void setMetrics(){
		for(Conflict c : this.conflicts){
			int number = c.getNumberOfConflicts()
			this.numberOfConflicts = this.numberOfConflicts + number
			if(c.getType().equals(SSMergeConflicts.EditSameMC.toString())){
				this.methodsWithConflicts++
				this.conflictsInsideMethods = this.conflictsInsideMethods + number
			}
		}
	}
	
	public String toString(){
		this.setMetrics()
		
		String result = this.path + ' ' + this.getNumberOfConflicts() + ' ' + 
		this.conflicts.size + ' ' + this.countConflictsInsideMethods() + ' '+
		this.countMethodsWithConflicts() + '\n'
		
		return result
	}

}
