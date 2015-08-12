package util

import main.SSMergeConflicts

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
		File out = new File('realConflictRate.csv')
		out.delete()
		out = new File('realConflictRate.csv')
		String line = 'Projects,Merge Scenarios,Conflicting Scenarios\n'
		out.append(line)
		
		file.eachLine {
			String[] data = it.split(",")
			String projectName = data[0]
			String analyzedMergeScenarios = data[1]
			if(!projectName.equals("Project")){
				int i = countMergeScenarioWithRealConflicts(projectName)
				line = projectName + ',' + analyzedMergeScenarios + ',' + i + '\n'
				println line
				out.append(line)
			}
		}
	}
	
	public static int countMergeScenarioWithRealConflicts(String projectName){
		
		int result = 0
		String mergeScenarioFile = 'ResultData' + File.separator + projectName + File.separator +
		'MergeScenariosReport.csv'
		String msFile = new File(mergeScenarioFile).text
		String [] lines = msFile.split('\n')
		for(int i = 1; i< lines.length;  i++){
			
			if(hasRealConflicts(lines[i])){
				result++
			}
			
		}
		return result
	}
	
	public static boolean hasRealConflicts(String line){
		boolean hasRealConflicts = false
		String[] data = line.split(', ')
		int i = 7
		for(SSMergeConflicts c : SSMergeConflicts.values()){
			if(!c.toString().equals(SSMergeConflicts.NOPATTERN.toString())){
				int total = Integer.parseInt(data[i])
				i++
				int ds = Integer.parseInt(data[i])
				i++
				int cl = Integer.parseInt(data[i])
				i++
				int ifp = Integer.parseInt(data[i])
				int realConflicts = total - ds - cl + ifp
				if(realConflicts > 0){
					hasRealConflicts = true
				}
				i++
			}
			
		}
		
		return hasRealConflicts
	}
	
	public static void main(String[] args){
		CSVAnalyzer.writeRealConflictsCSV()
	}




}
