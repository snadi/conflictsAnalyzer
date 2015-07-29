package main

import de.ovgu.cide.fstgen.ast.FSTTerminal

import java.util.Map;
import java.util.Observable;
import merger.MergeVisitor

class MergedFile {

	private ArrayList<Conflict> conflicts

	private String path

	private int numberOfConflicts

	private int methodsWithConflicts

	private int conflictsInsideMethods

	private int conflictsOutsideMethods

	private Map<String,Conflict> mergedFileSummary

	public MergedFile(String path){
		this.path = path
		this.conflicts = new ArrayList<Conflict>()
		this.createMergedFileSummary()
	}

	public void createMergedFileSummary(){
		this.mergedFileSummary = ConflictSummary.initializeConflictsSummary()
	}

	public void updateMergedFileSummary(Conflict conflict){
		this.mergedFileSummary = ConflictSummary.updateConflictsSummary(this.mergedFileSummary, conflict)
	}


	public int getNumberOfConflicts(){

		return this.numberOfConflicts;
	}

	public void updateMetrics(Conflict c){
		this.numberOfConflicts = this.numberOfConflicts + c.getNumberOfConflicts()
		if(c.getType().equals(SSMergeConflicts.EditSameMC.toString())){
			this.conflictsInsideMethods = this.conflictsInsideMethods +
					c.getNumberOfConflicts()

			this.methodsWithConflicts++
		}else{
			this.conflictsOutsideMethods++
		}

		this.updateMergedFileSummary(c)
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

	public int getConflictsInsideMethods(){

		return this.conflictsInsideMethods;
	}

	public int getMethodsWithConflicts(){

		return this.methodsWithConflicts
	}

	public int getConflictsOutsideMethods(){

		return this.conflictsOutsideMethods
	}


	public String toString(){

		String result = this.path + ' ' + this.getNumberOfConflicts() + ' ' +
				this.getConflictsInsideMethods() + ' '+ this.getMethodsWithConflicts() +
				' ' + this.getConflictsOutsideMethods() + ' '
				ConflictSummary.printConflictsSummary(this.mergedFileSummary) + '\n'

		return result
	}

}
