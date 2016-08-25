package travisAnalysis

class MergeScenario {
	String sha
	String parent1
	String parent2
	String revName
	boolean hasFSTMergeConflicts
	boolean hasGitConflictsJava
	boolean hasGitConflictsNonJava
	Hashtable<String, Integer> predictors
	boolean buildPassed
	boolean testsPassed
	boolean discarded
	
	public MergeScenario (String sha, String parent1, String parent2, String metrics){
		this.sha = sha
		this.parent1 = parent1
		this.parent2 = parent2
		this.loadMetrics(metrics)
		this.checkBuildAndTest()
		if(!discarded){
			this.runGitMerge()
		}
		
	}
	
	public void checkBuildAndTest(){
		
	}
	
	public void loadMetrics(String metrics){
		String[] m = metrics.split(',')
		//set name
		this.revName = m[0]

		//set hasFSTMergeConflicts
		int fstConf = Integer.parseInt(m[1])
		if(fstConf==1){
			this.hasFSTMergeConflicts = true
		}else{
			this.hasFSTMergeConflicts = false
		}

		//set predictors
		this.loadPredictors(metrics)

	}
	
	public void loadPredictors(String metrics){
		this.predictors = new Hashtable<String, Integer>()
		String[] m = metrics.split(',')
		int editSameMC = Integer.parseInt(m[8]) - Integer.parseInt(m[9])
		this.predictors.put('editSameMC',editSameMC)
		int editSameFd = Integer.parseInt(m[10]) - Integer.parseInt(m[11])
		this.predictors.put('editSameFd',editSameFd)
		int editDiffMC = Integer.parseInt(m[12])
		this.predictors.put('editDiffMC',editDiffMC)
		int editDiffEditSame = Integer.parseInt(m[13])
		this.predictors.put('editDiffEditSame',editDiffEditSame)
		int editDiffAddsCall = Integer.parseInt(m[14])
		this.predictors.put('editDiffAddsCall',editDiffAddsCall)
		int editDiffEditSameAddsCall = Integer.parseInt(m[15])
		this.predictors.put('editDiffEditSameAddsCall',editDiffEditSameAddsCall)
		
	}
	
	public void runGitMerge(){
		
	}
}
