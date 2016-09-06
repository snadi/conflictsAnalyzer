package travisAnalysis

class PrintBuildAndTestAnalysis {

	public static void printMergeScenario(String resultPath, String mergeSummary){
		String header = 'rev_name,sha,hasFSTMergeConflicts,hasRealFSTMergeConflicts,hasGitConflictsJava,' +
				'hasGitConflictsNonJava,discarded,buildCompiles,testsPass,' +
				'editSameMC,editSameFD,ncEditSameMC,ncEditSameFd,editDiffMC,editDiffEditSame,' +
				'editDiffAddsCall,editDiffEditSameAddsCall\n'
		
		File file = new File(resultPath + File.separator +
			'Merge_Scenario_Report.csv')
		/*print the header if the file is created now*/
		if(!file.exists()){
			file.append(header)
		}
		
		file.append(mergeSummary + '\n')
	}
	
	public static void printAnalyzedProjects(String projectName){
		File file = new File('analyzedProjects.csv')
		file.append(projectName + '\n')
	}
	
	/*print one report for each predictor*/
	/*public static void printProjectSummary(Hashtable<String, String> projectSummary){
				
		for(String predictor : projectSummary.keySet()){
			this.auxPrintProjectSummary(predictor,projectSummary)
		}
		
	}
	
	public static void auxPrintProjectSummary (String predictor, Hashtable<String, String> projectSummary){
		String header = 'project,merge_scenarios,conflict_predictor,' +
		'parents_build_passed,build_passed,build_failed,parents_build_failed\n'
		
		File file = new File(predictor + '_projectReport.csv')
		file.append(header)
		file.append(projectSummary.get(predictor) + '\n')
	}*/
}
