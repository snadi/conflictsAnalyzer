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
	
	/*print one line for each predictor*/
	public static void printProjectSummary(String name, String projectSummary){
		String header = 'predictor,total_merge_scenario,merge_scenarios,conflict_predictor,' +
		 'parents_build_passed,build_passed,test_passed\n'
		
		File file = new File('ResultData' + File.separator + name + File.separator + 'buildAndTest' +
			File.separator + 'ProjectReport.csv')
		if(!file.exists()){
			file.append(header)
		}
		
		file.append(projectSummary)
		
	}
	
}
