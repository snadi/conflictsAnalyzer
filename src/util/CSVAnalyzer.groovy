package util

class CSVAnalyzer {

	public static void verifyDiffsOnSameSignatureMC(){
		File file = new File('/Users/paolaaccioly/Documents/testeConflictsAnalyzer/conflictsAnalyzer/projectsPatternData.csv')
		file.eachLine {
			String[] data = it.split(",")
			String projectName = data[0]
			if(!projectName.equals("Project")){
				int sameSignatureCM = Integer.parseInt(data[27])
				int smallMethod = Integer.parseInt(data[36])
				int renamedMethod = Integer.parseInt(data[37])
				int copiedMethod = Integer.parseInt(data[38])
				int copiedFile = Integer.parseInt(data[39])
				int noPattern = Integer.parseInt(data[40])

				int sumCauses = smallMethod + renamedMethod + copiedMethod + copiedFile + noPattern

				int diff = sameSignatureCM - sumCauses

				if(diff != 0){
					println projectName + ' ' + diff
				}
			}
		}
	}
	
	public static void writeRealConflictsCSV(){
		File file = new File('/Users/paolaaccioly/Documents/testeConflictsAnalyzer/conflictsAnalyzer/projectsPatternData.csv')
		file.eachLine {
			String[] data = it.split(",")
			String projectName = data[0]
			if(!projectName.equals("Project")){
				int i = countMergeScenarioWithRealConflicts(projectName)
			}
		}
	}
	
	public static int countMergeScenarioWithRealConflicts(String projectName){
		int result = 0
		String mergeScenarioFile = 'ResultData' + File.separator + projectName + File.separator +
		'MergeScenariosReport.csv'
		String msFile = new File(mergeScenarioFile).text
		for(int i = 1; i< msFile ; i++){
			
		}
		
	}
	
	public static void main(String[] args){
		CSVAnalyzer.writeRealConflictsCSV()
	}




}
