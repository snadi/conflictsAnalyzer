package main


import java.util.Observable;

import merger.FSTGenMerger;
import merger.MergeVisitor
import sun.tools.jar.Main;
import util.CompareFiles;
import composer.rules.ImplementsListMerging;
import de.ovgu.cide.fstgen.ast.FSTTerminal;


class MergeScenario implements Observer {

	private String path

	private String name

	private ArrayList<MergedFile> mergedFiles

	private Map<String,Integer> mergeScenarioSummary
	
	private int conflictsDueToDifferentSpacing
	
	private int conflictsDueToConsecutiveLines
	
	private int falsePositivesIntersection
	
	private int numberOfConflicts

	private boolean hasConflicts

	private CompareFiles compareFiles

	public MergeScenario(String path){
		this.path = path
		this.setName()
		//this.removeVarArgs()
		this.hasConflicts = false
		this.createMergeScenarioSummary()
		this.setMergedFiles()
	}

	public void setMergedFiles(){
		this.compareFiles = new CompareFiles(this.path)
		this.compareFiles.ignoreFilesWeDontMerge()
		this.mergedFiles = this.compareFiles.getFilesToBeMerged()
	}
	
	public ArrayList<MergedFile> getMergedFiles(){
		return this.mergedFiles
	}

	public void setName(){
		String [] temp = this.path.split('/')
		String revFile = temp[temp.size() -1]
		this.name = revFile.substring(0, revFile.length()-10)
	}

	public String getName(){
		return this.name
	}

	public void analyzeConflicts(){

		this.runSSMerge()
		this.compareFiles.restoreFilesWeDontMerge()
	}

	public void deleteMSDir(){
		String msPath = this.path.substring(0, (this.path.length()-26))
		File dir = new File(msPath)
		boolean deleted = dir.deleteDir()
		if(deleted){
			println 'Merge scenario ' + this.path + ' deleted!'
		}else{

			println 'Merge scenario ' + this.path + ' not deleted!'
		}
	}

	public void runSSMerge(){
		FSTGenMerger fstGenMerge = new FSTGenMerger()
		fstGenMerge.getMergeVisitor().addObserver(this)
		String[] files = ["--expression", this.path]
		fstGenMerge.run(files)
	}


	public void createMergeScenarioSummary(){
		this.mergeScenarioSummary = new HashMap<String, Integer>()
		for(SSMergeConflicts c : SSMergeConflicts.values()){

			String type = c.toString();
			this.mergeScenarioSummary.put(type, 0)
		}
	}

	public void updateMergeScenarioSummary(Conflict conflict){
		String conflictType = conflict.getType()
		Integer typeQuantity = this.mergeScenarioSummary.get(conflictType).value
		typeQuantity = typeQuantity + conflict.getNumberOfConflicts()
		this.mergeScenarioSummary.put(conflictType, typeQuantity)
		this.updateFalsePositives(conflict)
	}
	
	private void updateFalsePositives(Conflict conflict){
		this.numberOfConflicts = this.numberOfConflicts + conflict.getNumberOfConflicts()
		this.conflictsDueToDifferentSpacing = this.conflictsDueToDifferentSpacing + conflict.getDifferentSpacing()
		this.conflictsDueToConsecutiveLines = this.conflictsDueToConsecutiveLines + conflict.getConsecutiveLines()
		this.falsePositivesIntersection = this.falsePositivesIntersection + conflict.getFalsePositivesIntersection()
		
	}
	
	public String getId(){
		return this.id
	}

	public void setId(String id){
		this.id = id
	}

	public boolean getHasConflicts(){
		return this.hasConflicts
	}

	public void removeVarArgs(){
		String OS = System.getProperty("os.name").toLowerCase()
		String sSed = ""
		if (OS.contains('mac')){
			sSed = "xargs sed -i \'\' s/\\.\\.\\./[]/g"
		}else if(OS.contains('linux')){
			sSed = "xargs sed -i s/\\.\\.\\./[]/g"
		}
		String msPath = this.path.substring(0, (this.path.length()-26))
		String command = "grep -rl ... " + msPath
		def procGrep = command.execute()
		def procSed = sSed.execute()
		procGrep | procSed
		procSed.waitFor()
	}

	@Override
	public void update(Observable o, Object arg) {

		if(o instanceof MergeVisitor && arg instanceof FSTTerminal){

			FSTTerminal node = (FSTTerminal) arg

			if(!node.getType().contains("-Content")){
				this.hasConflicts = true
				this.createConflict(node)
			}
		}
	}

	public void createConflict(FSTTerminal node){
		Conflict conflict = new Conflict(node, this.path);
		this.matchConflictWithFile(conflict)
		this.updateMergeScenarioSummary(conflict)

	}

	private void matchConflictWithFile(Conflict conflict){
		String rev_base = this.compareFiles.baseRevName
		String conflictPath = conflict.filePath
		boolean matchedFile = false
		int i = 0
		while(!matchedFile && i < this.mergedFiles.size){
			String mergedFilePath = this.mergedFiles.elementData(i).path.replaceFirst(rev_base, this.name)
			if(conflictPath.equals(mergedFilePath)){
				matchedFile = true
				this.addConflictToFile(conflict, i)
			}else{
				i++
			}
		}
	}

	private void addConflictToFile(Conflict conflict, int index){
		
			this.mergedFiles.elementData(index).conflicts.add(conflict)
			this.mergedFiles.elementData(index).updateMetrics(conflict)
	}
	
	public String toString(){
		String report = this.name + ' ' + this.compareFiles.getNumberOfTotalFiles() + 
		' ' + this.compareFiles.getFilesEditedByOneDev() + ' ' +
		this.compareFiles.getFilesThatRemainedTheSame() + ' ' + this.mergedFiles.size() +
		' ' + this.getNumberOfFilesWithConflicts() + ' ' + this.getNumberOfConflicts() +
		' ' + this.getConflictsDueToDifferentSpacing() + ' ' + this.getConflictsDueToConsecutiveLines() +
		' ' + this.getFalsePositivesIntersection() + ' ' + this.conflictsSummary() + '\n'
		
		return report
	}
	
	
	
	
	public String printMetrics(){
		String result = ''
		for(MergedFile m : this.mergedFiles){
			if(m.conflicts.size != 0){
				result = result + m.toString()
			}
		}
		return result
	}
	
	private int getNumberOfFilesWithConflicts(){
		int result = 0
		for(MergedFile m : this.mergedFiles){
			if(m.hasConflicts()){
				result = result + 1
			}
		}
		return result
	}
	
	public int getNumberOfConflicts(){
		

		return this.numberOfConflicts
	}
	
	
	
	public int getConflictsDueToDifferentSpacing() {
		return conflictsDueToDifferentSpacing;
	}

	public void setConflictsDueToDifferentSpacing(int conflictsDueToDifferentSpacing) {
		this.conflictsDueToDifferentSpacing = conflictsDueToDifferentSpacing;
	}

	public int getConflictsDueToConsecutiveLines() {
		return conflictsDueToConsecutiveLines;
	}

	public void setConflictsDueToConsecutiveLines(int conflictsDueToConsecutiveLines) {
		this.conflictsDueToConsecutiveLines = conflictsDueToConsecutiveLines;
	}
	
	
	
	public void setNumberOfConflicts(int numberOfConflicts) {
		this.numberOfConflicts = numberOfConflicts;
	}

	public int getFalsePositivesIntersection() {
		return falsePositivesIntersection;
	}

	public void setFalsePositivesIntersection(int falsePositivesIntersection) {
		this.falsePositivesIntersection = falsePositivesIntersection;
	}

	public String conflictsSummary(){
		
		int DefaultValueAnnotation = this.mergeScenarioSummary.get("DefaultValueAnnotation")
		int ImplementList = this.mergeScenarioSummary.get("ImplementList")
		int ModifierList = this.mergeScenarioSummary.get("ModifierList")
		int EditSameMC = this.mergeScenarioSummary.get("EditSameMC")
		int SameSignatureCM = this.mergeScenarioSummary.get("SameSignatureCM")
		int AddSameFd = this.mergeScenarioSummary.get("AddSameFd")
		int EditSameFd = this.mergeScenarioSummary.get("EditSameFd")
		int ExtendsList = this.mergeScenarioSummary.get("ExtendsList")
		String result = DefaultValueAnnotation + ' ' + ImplementList + ' ' + 
		ModifierList + ' ' + EditSameMC + ' ' + SameSignatureCM + ' ' + AddSameFd + 
		' ' + EditSameFd + ' ' + ExtendsList
		return result
	}
	
	public static void main(String[] args){
		MergeScenario ms = new MergeScenario('/Users/paolaaccioly/Desktop/Teste/jdimeTests/rev.revisions')
		ms.analyzeConflicts()
	}

}
