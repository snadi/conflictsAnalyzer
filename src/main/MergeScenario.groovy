package main


import java.util.Observable;

import merger.FSTGenMerger;
import merger.MergeVisitor
import util.CompareFiles;
import composer.rules.ImplementsListMerging;
import de.ovgu.cide.fstgen.ast.FSTTerminal;


class MergeScenario implements Observer {

	private String path

	private ArrayList<Conflict> conflicts

	private Map<String,Integer> mergeScenarioSummary

	private boolean hasConflicts
	
	private CompareFiles compareFiles

	MergeScenario(String path){
		this.path = path
		//this.removeVarArgs()
		this.conflicts = new ArrayList<Conflict>()
		this.hasConflicts = false
		this.createMergeScenarioSummary()
		this.compareFiles = new CompareFiles(this.path)
	}

	public void analyzeConflicts(){
		this.compareFiles.ignoreFilesWeDontMerge()
		this.runFstGenMerger()
		this.updateHasConflicts()
		this.compareFiles.restoreFilesWeDontMerge()
	}

	public void deleteMSDir(){
		String msPath = this.path.substring(0, (this.path.length()-26))
		def dir = new File(msPath)
		boolean deleted = dir.deleteDir()
		if(deleted){
			println 'Merge scenario ' + this.path + ' deleted!'
		}else{

			println 'Merge scenario ' + this.path + ' not deleted!'
		}
	}

	public void runFstGenMerger(){
		FSTGenMerger fstGenMerge = new FSTGenMerger()
		fstGenMerge.getMergeVisitor().addObserver(this)
		String[] files = ["--expression", this.path]
		fstGenMerge.run(files)
	}


	@Override
	public void update(Observable o, Object arg) {

		if(o instanceof MergeVisitor && arg instanceof FSTTerminal){

			FSTTerminal node = (FSTTerminal) arg

			if(!node.getType().contains("-Content")){
				this.createConflict(node)
			}
		}
	}

	public void createConflict(FSTTerminal node){
		Conflict conflict = new Conflict(node, this.path);
		this.conflicts.add(conflict)
		this.updateMergeScenarioSummary(conflict)
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

	public ArrayList<Conflict> getConflicts(){
		return this.conflicts
	}

	public void setConflicts(ArrayList<Conflict> conflicts){
		this.conflicts = conflicts
	}

	public boolean getHasConflicts(){
		return this.hasConflicts
	}

	private void updateHasConflicts(){
		if(!this.conflicts.isEmpty())
			this.hasConflicts = true
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

}