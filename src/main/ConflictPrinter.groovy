package main


import java.util.ArrayList;

public class ConflictPrinter {

	static String conflictReportHeader 
	
	public static setconflictReportHeader(){
		this.conflictReportHeader = ''
		String noPattern = SSMergeConflicts.NOPATTERN.toString()
		for(SSMergeConflicts c : SSMergeConflicts.values()){
			String type = c.toString()
			this.conflictReportHeader = this.conflictReportHeader +
			type + ', '
			if(!type.equals(noPattern)){
				this.conflictReportHeader = this.conflictReportHeader +
				type + 'DS, ' + type + 'CL, ' +
				type + 'IFP, '
			}
		}
		
		for(PatternSameSignatureCM p : PatternSameSignatureCM.values()){
			String cause = p.toString()
			this.conflictReportHeader = this.conflictReportHeader + cause + ', '
		}
		this.conflictReportHeader = this.conflictReportHeader.substring(0, this.conflictReportHeader.length()-2)
	}
	
	public static String getConflictReportHeader(){
		return this.conflictReportHeader
	}

	public static void printProjectData(ArrayList<Project> projects){
		String fileName = 'projectsPatternData.csv'
		def out = new File(fileName)
		// deleting old files if it exists
		out.delete()
		out = new File(fileName)

		String row

		row = 'Project, Merge_Scenarios, Conflicting_Scenarios, ' +
		this.conflictReportHeader + '\n'

		out.append(row)

		for(Project p : projects){
			out.append(p.toString() + '\n')
		}
	}

	public static void printMergeScenarioReport(MergeScenario mergeScenario, String projectName){


		File out = new File("ResultData" + File.separator + projectName + File.separator + 'MergeScenariosReport.csv')

		if(!out.exists()){
			String fileHeader = 'Merge_scenario, Total_Files, Files_Edited_By_One_Dev, ' +
					'Files_That_Remained_The_Same, Files_Merged, Files_With_Conflicts, Total_Conflicts, ' +
					 this.conflictReportHeader + '\n'
			out.append(fileHeader)
		}
		out.append(mergeScenario.toString() + '\n')

		printMergeScenarioMetrics(mergeScenario, projectName)
		printConflictsReport(mergeScenario, projectName)
	}

	public static void printMergeScenarioMetrics(MergeScenario mergeScenario, String projectName){
		File out = new File('ResultData' + File.separator + projectName + File.separator +
				'Merge_Scenarios' + File.separator + mergeScenario.name + '.csv')
		if(!out.exists()){
			String header = 'File, Total_of_Conflicts, ' +
					'Conflicts_Inside_Methods, Methods_with_Conflicts, ' +
					'Conflicts_Outside_Methods, ' +
					this.conflictReportHeader + '\n'
			out.append(header)
		}

		out.append(mergeScenario.printMetrics() + '\n')


	}

	public static void printConflictsReport(MergeScenario mergeScenario, String projectName){

		def out = new File("ResultData" + File.separator + projectName + File.separator + 'ConflictsReport.csv')

		def delimiter = '========================================================='
		out.append(delimiter)
		out.append '\n'

		out.append('Revision: ' + mergeScenario.path + '\n')

		for(MergedFile mergedFile: mergeScenario.getMergedFiles()){

			for(Conflict c: mergedFile.getConflicts()){

				def row = ['Conflict type: '+ c.getType() + '\n' + 'Conflict body: ' + '\n' + c.getBody() ]
				out.append row.join(',')
				out.append '\n'
				row = ['File path: ' + c.getFilePath()]
				out.append row.join(',')
				out.append '\n'

			}

		}
		out.append '\n'
		out.append(delimiter)
	}

	public static void main (String[] args){
		ConflictPrinter.setconflictReportHeader()
		println ConflictPrinter.getConflictReportHeader()
	}
}
