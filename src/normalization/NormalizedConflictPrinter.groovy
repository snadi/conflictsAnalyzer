package normalization

import main.SSMergeNodes;

class NormalizedConflictPrinter {
	static String conflictReportHeader
	
	public String getReportHeader(){
		String result = ''
		
		//print nodes
		for(SSMergeNodes node in SSMergeNodes.values()){
			result = result + node + ', '
		}
		
		result = result + ', ChangesInsideMethodsChunk, ChangesInsideMethodsLines'
		 
		return result
	}
	
	public static void printNormalizedProjectData(NormalizedProject p){
		String fileName = 'projectsChanges.csv'
		File out = new File(fileName)
		
		if(!out.exists()){
			out.append('Project , NumberOfScenarios, '+ this.getReportHeader() + '\n')
		}
		
		out.append(p.toString() + '\n')
		
	}
	
	public static void printEvoScenarioReport (EvoScenario evo, String projectName){
		String fileName = 'ResultData' + File.separator + projectName + File.separator + 'EvoScenarios.csv'
		File out = new File(fileName)
		
		if(!out.exists()){
			out.append('Scenario , '+ this.getReportHeader() + '\n')
		}
		out.append(evo.toString() + '\n')
	}
	
	public static void printMergeScenariosWithConflictsOnNonJavaFiles(EvoScenario evo, String projectName){
		String fileName = 'ResultData' + File.separator + projectName + File.separator + 'EvoScenarios.csv'
		File out = new File(fileName)
		
		if(!out.exists()){
			out.append('MergeScenario\n')
		}
		
		out.append(evo.name + '\n')
		
	}
}
