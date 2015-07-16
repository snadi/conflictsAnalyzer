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

		if(conflict.methodOrConstructor && conflict.isInsideMethod()){
			typeQuantity = typeQuantity + conflict.countConflictsInsideMethods()
		}else{
			typeQuantity++
		}

		this.mergeScenarioSummary.put(conflictType, typeQuantity)

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
		if(conflict.type.equals(SSMergeConflicts.EditSameMC.toString())){
			MethodOrConstructor moc = new MethodOrConstructor(conflict)
			this.mergedFiles.elementData(index).methodsWithConflicts.add(moc)
		}else{
			this.mergedFiles.elementData(index).conflicts.add(conflict)
		}
	}
	
	public String toString(){
		String report = this.name + ' ' + this.compareFiles.getNumberOfTotalFiles() + 
		' ' + this.compareFiles.getFilesEditedByOneDev() + ' ' +
		this.compareFiles.getFilesThatRemainedTheSame() + ' ' + this.mergedFiles.size() +
		' '+ this.getNumberOfFilesWithConflicts() + ' ' + this.getNumberOfConflicts() + ' ' + this.conflictsSummary()
		
		return report
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
		int result = 0
		for(MergedFile mf : this.mergedFiles){
			result = result + mf.getNumberOfConflicts()
		}
		
		return result
	}
	
	public String conflictsSummary(){
		
		int DefaultValueAnnotation = this.mergeScenarioSummary.get("DefaultValueAnnotation")
		int ImplementList = this.mergeScenarioSummary.get("ImplementList")
		int ModifierList = this.mergeScenarioSummary.get("ModifierList")
		int EditSameMC = this.mergeScenarioSummary.get("EditSameMC")
		int SameSignatureCM = this.mergeScenarioSummary.get("SameSignatureCM")
		int AddSameFd = this.mergeScenarioSummary.get("AddSameFd")
		int EditSameFd = this.mergeScenarioSummary.get("EditSameFd")
		String result = ' ' + DefaultValueAnnotation + ' ' + ImplementList + ' ' + 
		ModifierList + ' ' + EditSameMC + ' ' + SameSignatureCM + ' ' + AddSameFd + 
		' ' + EditSameFd
		return result
	}
	
	public static void main(String[] args){
		MergeScenario ms = new MergeScenario('/Users/paolaaccioly/Desktop/Teste/jdimeTests/rev.revisions')
		println ms.getName()
	}

}
