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

	private Map<String,Conflict> mergeScenarioSummary

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
	
	public HashMap<String, Conflict> getMergeScenarioSummary(){
		return this.mergeScenarioSummary
	}
	
	public void setName(){
		String [] temp = this.path.split('/')
		String revFile = temp[temp.length -1]
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
		this.mergeScenarioSummary = new HashMap<String, Conflict>()
		for(SSMergeConflicts c : SSMergeConflicts.values()){

			String type = c.toString();
			this.mergeScenarioSummary.put(type, new Conflict(type))
		}
	}

	public void updateMergeScenarioSummary(Conflict conflict){
		String conflictType = conflict.getType()
		Conflict c2 = this.mergeScenarioSummary.get(conflictType)
		
		//get new values
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
	
	public String toString(){
		String report = this.name + ' ' + this.compareFiles.getNumberOfTotalFiles() +
				' ' + this.compareFiles.getFilesEditedByOneDev() + ' ' +
				this.compareFiles.getFilesThatRemainedTheSame() + ' ' + this.mergedFiles.size() +
				' ' + this.getNumberOfFilesWithConflicts() + ' ' + 
				this.conflictsSummary() + '\n'

		return report
	}

	public String conflictsSummary(){
		String result = ''
		
		for(SSMergeConflicts c : SSMergeConflicts.values()){
			int quantity = this.mergeScenarioSummary.get(c).getNumberOfTruePositives()
			result = result + quantity + ' '
		}

		return result.trim()
	}

	public static void main(String[] args){
		MergeScenario ms = new MergeScenario('/Users/paolaaccioly/Desktop/Teste/jdimeTests/rev.revisions')
		ms.analyzeConflicts()
		/*Map <String,Conflict> mergeScenarioSummary = new HashMap<String, Conflict>()
		 String type = SSMergeConflicts.EditSameMC.toString()
		 mergeScenarioSummary.put(type, new Conflict(type))
		 Conflict conflict = mergeScenarioSummary.get(type)
		 conflict.setNumberOfConflicts(5);
		 println 'hello world'*/
	}

}
