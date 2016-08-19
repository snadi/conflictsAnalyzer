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
		String filepath = System.getProperty("user.dir") + File.separator + 'projectsPatternData.csv'
		File file = new File(filepath)
		File out = new File('realConflictRate.csv')
		out.delete()
		out = new File('realConflictRate.csv')
		String line = 'Project,Merge Scenarios,Conflicting Scenarios\n'
		out.append(line)

		file.eachLine {
			String[] data = it.split(",")
			String projectName = data[0]
			String analyzedMergeScenarios = data[1]
			if(!projectName.equals("Project")){
				int i = countMergeScenarioWithRealConflicts(projectName)
				line = projectName + ',' + analyzedMergeScenarios + ',' + i + '\n'
				//println line
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



	public static void writeFileMetricsCSV(){
		File file = new File('/Users/paolaaccioly/Documents/testeConflictsAnalyzer/conflictsAnalyzer/projectsPatternData.csv')
		File out = new File('filesMetrics.csv')
		out.delete()
		out = new File('filesMetrics.csv')
		String line = 'Project,Total_files,Files_merged,Files_with_conflicts\n'
		out.append(line)
		
		file.eachLine {
			String[] data = it.split(",")
			String projectName = data[0]
			if(!projectName.equals("Project")){
				line = this.computeFileMetrics(projectName)
				out.append(line + '\n')
				println line
			}
		}
	}

	public static String computeFileMetrics(String projectName){
		File out = new File('moreThan10.csv')
		
		if(!out.exists()){
			out.append('Project,MergedFiles,FilesWithConflict\n')
		}
		
		String result = ''
		int totalFiles, mergedFiles, filesWithConflicts
		int[] wasMoreThan90 = [0,0]
		String mergeScenarioFile = 'ResultData' + File.separator + projectName + File.separator +
		'MergeScenariosReport.csv'
		String msFile = new File(mergeScenarioFile).text
		String [] lines = msFile.split('\n')
		for(int i = 1; i< lines.length;  i++){
			String[] data = lines[i].split(', ')
			totalFiles = totalFiles + Integer.parseInt(data[1]) + Integer.parseInt(data[4])
			mergedFiles = mergedFiles + Integer.parseInt(data[5])
			filesWithConflicts = filesWithConflicts + Integer.parseInt(data[6])
			int[] r = this.checkPercentagePerMC(totalFiles, mergedFiles,filesWithConflicts)
			 wasMoreThan90[0] =  wasMoreThan90[0] + r[0]
			 wasMoreThan90[1] =  wasMoreThan90[1] + r[1]
		}	
			String a = projectName + ',' + wasMoreThan90[0] +',' + wasMoreThan90[1]
			out.append(a + '\n')
			result = projectName + ',' +totalFiles + ',' + mergedFiles + ',' + filesWithConflicts
			return result
		}

		public static int[] checkPercentagePerMC(int totalFiles, int mergedFiles,int filesWithConflicts){
			int[] result = [0,0]
			int percentageMF = 0
			int percentageFWC =0
			if(totalFiles != 0){
				 percentageMF = (mergedFiles/totalFiles)*100
				 percentageFWC = (filesWithConflicts/totalFiles)*100
			}
			
			if(percentageMF >=10){
				result[0] = 1
			}
			if(percentageFWC>=10){
				result[1] = 1
			}
			
			return result
		}
		
		public static void printMetricsByMerges(){
			File file = new File('mergesSummary.csv')
			if(file.exists()){
				file.delete()
			}
			file = new File('mergesSummary.csv')
			file.append('Project,Merges,Merges_With_EditSameMC,Merges_With_NC_EditSameMC,' + 
				'Merges_With_EditSameFd,Merges_With_NC_EditSameFd,Merges_With_EditDiff,'+
				 'Merges_With_EditDiffAddsCall,Merges_With_EditDiff_Same,Merges_With_EditDiff_SameAddsCall\n')
			
			File result = new File('ConflictPredictor_Projects_Report.csv')
			result.eachLine {
				if(!it.contains('Project') && !it.equals('')){
					String[] projectMetrics = it.split(',')
					String projectName = projectMetrics[0]
					println projectName
					String numberOfMerges = projectMetrics[1]
					String summary = projectName + ',' + numberOfMerges + ',' +
					this.auxPrintMetricsByMerges(projectName)
					file.append(summary)
				}
			}
			
		}
		
		public static String auxPrintMetricsByMerges(String project){
			File file = new File('ResultData' + File.separator + project + File.separator + 'ConflictPredictor_MS_Report.csv')
			String text = file.getText()
			String[] lines = text.split('\n')
			int mergesWithEditSameMC = 0
			int mergesWithNCEditSameMC = 0
			int mergesWithEditSameFd = 0
			int mergesWithNCEditSameFd = 0
			
			int mergesEditDiffMC = 0
			int mergesEditDiffMCAdds = 0
			int mergesEditDiff_Same = 0
			int mergesEditDiff_SameAdds = 0
			
			/*for each analyzed merge scenario*/
			for(int i = 1; i < lines.length; i++){
				
				String[] mergeMetrics = lines[i].split(',')
				/*set editsamemc*/
				int editSameMC = Integer.parseInt(mergeMetrics[4]) - Integer.parseInt(mergeMetrics[5])
				if(editSameMC > 0){
					mergesWithEditSameMC++
				}else{
					int NCEditSameMC = Integer.parseInt(mergeMetrics[8]) - Integer.parseInt(mergeMetrics[9])
					if(NCEditSameMC > 0){
						mergesWithNCEditSameMC++
					}
				}
				
				/*set editsamefd*/
				int editSameFd = Integer.parseInt(mergeMetrics[6]) - Integer.parseInt(mergeMetrics[7])
				if(editSameFd > 0){
					mergesWithEditSameFd++
				}else{
					int NCEditSameFd = Integer.parseInt(mergeMetrics[10]) - Integer.parseInt(mergeMetrics[11])
					if(NCEditSameFd > 0){
						mergesWithNCEditSameFd++
					}
				}
				
				/*set editdiff*/
				int editDiff = Integer.parseInt(mergeMetrics[12])
				int editDiff_Same = Integer.parseInt(mergeMetrics[13])
				int editDiffAdds = Integer.parseInt(mergeMetrics[14])
				int editDiff_SameAdds = Integer.parseInt(mergeMetrics[15])
				
				if(editDiff > 0){
					mergesEditDiffMC++
					
					if(editDiffAdds >0){
						mergesEditDiffMCAdds++
					}
				}
				
				if(editDiff_Same > 0){
					mergesEditDiff_Same++
					
					if(editDiff_SameAdds > 0){
						mergesEditDiff_SameAdds++
					}
				}
				
			}
			String result = mergesWithEditSameMC + ',' + mergesWithNCEditSameMC + ',' +
			mergesWithEditSameFd + ',' + mergesWithNCEditSameFd + ',' + mergesEditDiffMC + 
			',' + mergesEditDiffMCAdds + ',' + mergesEditDiff_Same + ',' + mergesEditDiff_SameAdds +
			'\n'
			
			return result
		}
		
		public static void main(String[] args){
			//CSVAnalyzer.writeRealConflictsCSV()
			//CSVAnalyzer.writeFileMetricsCSV()
			CSVAnalyzer.printMetricsByMerges()
		}




	}
