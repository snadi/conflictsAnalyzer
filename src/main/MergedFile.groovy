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
		this.mergedFileSummary = new HashMap<String, Conflict>()
		for(SSMergeConflicts c : SSMergeConflicts.values()){

			String type = c.toString();
			this.mergedFileSummary.put(type, new Conflict(type))
		}
	}

	public void updateMergedFileSummary(Conflict conflict){
		String conflictType = conflict.getType()
		Conflict c2 = this.mergedFileSummary.get(conflictType)
		//compute new values
		int numberOfConflicts = conflict.getNumberOfConflicts() + c2.getNumberOfConflicts()
		int differentSpacing = conflict.getDifferentSpacing() + c2.getDifferentSpacing()
		int consecutiveLines = conflict.getConsecutiveLines() + c2.getConsecutiveLines()
		int falsePositivesIntersection = conflict.falsePositivesIntersection + 
		c2.getFalsePositivesIntersection()
		
		//set new values
		c2.setNumberOfConflicts(numberOfConflicts)
		c2.setDifferentSpacing(differentSpacing)
		c2.setConsecutiveLines(consecutiveLines)
		c2.setFalsePositivesIntersection(falsePositivesIntersection)
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
				this.conflictsSummary() + '\n'

		return result
	}

	public String conflictsSummary(){

		String result = ''
		
		for(SSMergeConflicts c : SSMergeConflicts.values()){
			int quantity = this.mergedFileSummary.get(c).getNumberOfTruePositives()
			result = result + quantity + ' '
		}

		return result.trim()
	}

}
