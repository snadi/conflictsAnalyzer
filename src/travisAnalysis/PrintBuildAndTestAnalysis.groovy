package travisAnalysis

class PrintBuildAndTestAnalysis {

	public static void printMergeScenario(String resultPath, String mergeSummary){
		String header = 'rev_name,sha,hasFSTMergeConflicts,hasGitConflictsJava,' +
				'hasGitConflictsNonJava,discarded,buildCompiles,testsPass,' +
				'editSameMC,editSameFd,editDiffMC,editDiffEditSame,' +
				'editDiffAddsCall,editDiffEditSameAddsCall\n'
		
		File file = new File(resultPath + File.separator +
			'Merge_Scenario_Report.csv')
		/*print the header if the file is created now*/
		if(!file.exists()){
			file.append(header)
		}
		
		file.append(mergeSummary + '\n')
	}

	public static void printProjectSummary(String resultPath, String projectSummary){
		String header = 'project,merge_scenarios,hasFSTMergeConflicts,hasGitConflictsJava,' +
		'hasGitConflictsNonJava,discarded,buildCompiles,testsPass,' +
		'editSameMC,editSameFd,editDiffMC,editDiffEditSame,' +
		'editDiffAddsCall,editDiffEditSameAddsCall\n'
	}
}
