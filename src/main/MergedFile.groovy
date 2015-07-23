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

	private int conflictsDueToDifferentSpacingMC

	private int conflictsDueToConsecutiveLinesMC

	private int falsePositivesIntersectionMC

	private int conflictsDueToDifferentSpacingFd

	private int conflictsDueToConsecutiveLinesFd

	private int falsePositivesIntersectionFd

	private Map<String,Integer> mergedFileSummary

	public MergedFile(String path){
		this.path = path
		this.conflicts = new ArrayList<Conflict>()
		this.createMergedFileSummary()
	}

	public void createMergedFileSummary(){
		this.mergedFileSummary = new HashMap<String, Integer>()
		for(SSMergeConflicts c : SSMergeConflicts.values()){

			String type = c.toString();
			this.mergedFileSummary.put(type, 0)
		}
	}

	public void updateMergedFileSummary(Conflict conflict){
		String conflictType = conflict.getType()
		Integer typeQuantity = this.mergedFileSummary.get(conflictType).value
		typeQuantity = typeQuantity + conflict.getNumberOfConflicts()
		this.mergedFileSummary.put(conflictType, typeQuantity)
	}

	public int getFalsePositivesIntersectionMC() {
		return falsePositivesIntersectionMC;
	}



	public void setFalsePositivesIntersectionMC(int falsePositivesIntersectionMC) {
		this.falsePositivesIntersectionMC = falsePositivesIntersectionMC;
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

		this.setFalsePositives(c)

		this.updateMergedFileSummary(c)
	}

	private void setFalsePositives(Conflict c){
		if(c.getType().equals(SSMergeConflicts.EditSameMC.toString())){
			this.conflictsDueToDifferentSpacingMC = this.conflictsDueToDifferentSpacingMC + c.getDifferentSpacing()
			this.conflictsDueToConsecutiveLinesMC = this.conflictsDueToConsecutiveLinesMC + c.getConsecutiveLines()
			this.falsePositivesIntersectionMC = this.falsePositivesIntersectionMC + c.getFalsePositivesIntersection()
		}else if(c.getType().equals(SSMergeConflicts.EditSameFd.toString())){
			this.conflictsDueToDifferentSpacingFd = this.conflictsDueToConsecutiveLinesFd + c.getDifferentSpacing()
			this.conflictsDueToConsecutiveLinesFd = this.conflictsDueToConsecutiveLinesFd + c.getConsecutiveLines()
			this.falsePositivesIntersectionFd = this.falsePositivesIntersectionFd + c.getFalsePositivesIntersection()
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

	public int getConflictsDueToDifferentSpacingMC(){

		return this.conflictsDueToDifferentSpacingMC;
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

	public int getConflictsDueToConsecutiveLinesMC(){

		return this.conflictsDueToConsecutiveLinesMC
	}

	public String toString(){

		String result = this.path + ' ' + this.getNumberOfConflicts() + ' ' +
				this.getConflictsInsideMethods() + ' '+ this.getMethodsWithConflicts() +
				' ' + this.getConflictsOutsideMethods() + ' ' + this.getConflictsDueToDifferentSpacingMC() +
				' ' + this.getConflictsDueToConsecutiveLinesMC() + ' ' +
				this.getFalsePositivesIntersectionMC() + ' ' + this.getConflictsDueToDifferentSpacingFd() + ' ' +
				this.getConflictsDueToConsecutiveLinesFd() + ' ' + this.getFalsePositivesIntersectionFd() + ' '+
				this.conflictsSummary() + '\n'

		return result
	}

	public String conflictsSummary(){

		int DefaultValueAnnotation = this.mergedFileSummary.get("DefaultValueAnnotation")
		int ImplementList = this.mergedFileSummary.get("ImplementList")
		int ModifierList = this.mergedFileSummary.get("ModifierList")
		int EditSameMC = this.mergedFileSummary.get("EditSameMC")
		int SameSignatureCM = this.mergedFileSummary.get("SameSignatureCM")
		int AddSameFd = this.mergedFileSummary.get("AddSameFd")
		int EditSameFd = this.mergedFileSummary.get("EditSameFd")
		int ExtendsList = this.mergedFileSummary.get("ExtendsList")
		String result = DefaultValueAnnotation + ' ' + ImplementList + ' ' +
				ModifierList + ' ' + EditSameMC + ' ' + SameSignatureCM + ' ' + AddSameFd +
				' ' + EditSameFd + ' ' + ExtendsList
		return result
	}

	public int getConflictsDueToDifferentSpacingFd() {
		return conflictsDueToDifferentSpacingFd;
	}

	public void setConflictsDueToDifferentSpacingFd(int conflictsDueToDifferentSpacingFd) {
		this.conflictsDueToDifferentSpacingFd = conflictsDueToDifferentSpacingFd;
	}

	public int getConflictsDueToConsecutiveLinesFd() {
		return conflictsDueToConsecutiveLinesFd;
	}

	public void setConflictsDueToConsecutiveLinesFd(int conflictsDueToConsecutiveLinesFd) {
		this.conflictsDueToConsecutiveLinesFd = conflictsDueToConsecutiveLinesFd;
	}

	public int getFalsePositivesIntersectionFd() {
		return falsePositivesIntersectionFd;
	}

	public void setFalsePositivesIntersectionFd(int falsePositivesIntersectionFd) {
		this.falsePositivesIntersectionFd = falsePositivesIntersectionFd;
	}

}
