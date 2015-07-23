package main


import java.util.ArrayList;

public class ConflictPrinter {

	public static void printProjectData(ArrayList<Project> projects){
		String fileName = 'projectsPatternData.csv'
		def out = new File(fileName)
		// deleting old files if it exists
		out.delete()
		out = new File(fileName)
		
		String row

		row = 'Project Merge_Scenarios Conflicting_Scenarios ' +
				'Conflicts_Due_To_Different_Spacing Conflicts_Due_To_Consecutive_Lines ' +
				'False_Positives_Intersection ' +
				'DefaultValueAnnotation ImplementList ModifierList EditSameMC ' +
				'SameSignatureCM AddSameFd EditSameFd ExtendsList\n'
		
		out.append(row)

		for(Project p : projects){
		int DefaultValueAnnotation = p.projectSummary.get("DefaultValueAnnotation")
		int ImplementList = p.projectSummary.get("ImplementList")
		int ModifierList = p.projectSummary.get("ModifierList")
		int EditSameMC = p.projectSummary.get("EditSameMC")
		int SameSignatureCM = p.projectSummary.get("SameSignatureCM")
		int AddSameFd = p.projectSummary.get("AddSameFd")
		int EditSameFd = p.projectSummary.get("EditSameFd")
		int ExtendsList = p.projectSummary.get("ExtendsList")
		String conflicts = ' ' + DefaultValueAnnotation + ' ' + ImplementList +
				' ' + ModifierList + ' ' + EditSameMC + ' ' + SameSignatureCM +
				' ' + AddSameFd + ' ' + EditSameFd + ' ' + ExtendsList
		row = p.name + ' ' + p.analyzedMergeScenarios + ' ' + p.conflictingMergeScenarios +
				' ' + p.getConflictsDueToDifferentSpacing() + ' ' +
				p.getConflictsDueToConsecutiveLines() + ' ' +
				p.getFalsePositivesIntersection() + ' ' +
				conflicts + '\n'
		out.append(row)
		}
	}

	public static void printMergeScenarioReport(MergeScenario mergeScenario, String projectName){


		File out = new File("ResultData" + File.separator + projectName + File.separator + 'MergeScenariosReport.csv')

		if(!out.exists()){
			String fileHeader = 'Merge_scenario Total_Files Files_Edited_By_One_Dev ' +
					'Files_That_Remained_The_Same Files_Merged Files_With_Conflicts Total_Conflicts ' +
					'Conflicts_Due_To_Different_Spacing Conflicts_Due_To_Consecutive_Lines '+
					'False_Positives_Intersection ' +
					'DefaultValueAnnotation ImplementList ModifierList EditSameMC, ' +
					'SameSignatureCM AddSameFd EditSameFd ExtendsList\n'
			out.append(fileHeader)
		}
		out.append(mergeScenario.toString())

		printMergeScenarioMetrics(mergeScenario, projectName)
		printConflictsReport(mergeScenario, projectName)
	}

	public static void printMergeScenarioMetrics(MergeScenario mergeScenario, String projectName){
		File out = new File('ResultData' + File.separator + projectName + File.separator +
				'Merge_Scenarios' + File.separator + mergeScenario.name + '.csv')
		String header = 'File Total_of_Conflicts ' +
				'Conflicts_Inside_Methods, Methods_with_Conflicts ' +
				'Conflicts_Outside_Methods ' +
				'Conflicts_Due_To_Different_Spacing ' +
				'Conflicts_Due_To_Consecutive_Lines + False_Positives_Intersection' +
				'DefaultValueAnnotation ImplementList ModifierList EditSameMC, ' +
					'SameSignatureCM AddSameFd EditSameFd ExtendsList\n'
		out.append(header)
		out.append(mergeScenario.printMetrics())


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
		File f = new File('teste.txt')
		if(!f.exists()){
			f.append('first row\n')
		}else{
			f.append('second row')
		}
	}
}
