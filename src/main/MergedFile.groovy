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
	
	private int conflictsOutsideMethods
	
	private int conflictsDueToDifferentSpacing
	
	private int conflictsDueToConsecutiveLines

	public MergedFile(String path){
		this.path = path
		this.conflicts = new ArrayList<Conflict>()
		
	}

	public int getNumberOfConflicts(){
	
		return this.numberOfConflicts;
	}
	
	public void updateMetrics(Conflict c){
		this.numberOfConflicts = this.numberOfConflicts + c.getNumberOfConflicts()
		if(c.getType().equals(SSMergeConflicts.EditSameMC.toString())){
			this.conflictsInsideMethods = this.conflictsInsideMethods + c.getNumberOfConflicts()
			this.conflictsDueToDifferentSpacing = this.conflictsDueToDifferentSpacing + c.getDifferentSpacing()
			this.conflictsDueToConsecutiveLines = this.conflictsDueToConsecutiveLines + c.getConsecutiveLines()
			this.methodsWithConflicts++
		}else{
			this.conflictsOutsideMethods++
		}
		
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
	
	public int getConflictsDueToDifferentSpacing(){
		
		return this.conflictsDueToDifferentSpacing;
	}
	
	public int getConflictsInsideMethods(){
		
		return this.conflictsInsideMethods;
	}
	
	public int getMethodsWithConflicts(){
		
		return this.methodsWithConflicts
	}
	
	public int getConflictsOutsideMethods(){
		
		return this.conflictsOutsideMethods
	}
	
	public int getConflictsDueToConsecutiveLines(){
	
		return this.conflictsDueToConsecutiveLines
	}
	
	public String toString(){
		
		String result = this.path + ' ' + this.getNumberOfConflicts() + ' ' +
		 this.getConflictsInsideMethods() + ' '+ this.getMethodsWithConflicts() + 
		 ' ' + this.getConflictsOutsideMethods() + ' ' + this.getConflictsDueToDifferentSpacing() +
		 ' ' + this.getConflictsDueToConsecutiveLines() + '\n'
		
		return result
	}

}
