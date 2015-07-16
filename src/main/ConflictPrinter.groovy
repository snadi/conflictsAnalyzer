package main


import java.util.ArrayList;

public class ConflictPrinter {


	/*	public static void printProjectsReport(ConflictsAnalyzer ca){
	 def fileName = 'ProjectsSummary.csv'
	 def out = new File(fileName)
	 // deleting old files if it exists
	 out.delete()
	 out = new File(fileName)
	 def content = ca.analyzedProjects + ' projects analyzed so far.' +
	 '\nBelow you will find the list of analyzed projects and their conflict rate.\n'
	 out.append(content)
	 for(int i = 0; i < ca.analyzedProjects; i++){
	 def row = ca.getProjects().get(i).getName() + ': ' +
	 ca.getProjects().get(i).getConflictRate() + '%\n'
	 out.append(row)
	 }
	 content = 'Total conflict rate: ' + ca.getProjectsConflictRate() +
	 '%\nSummary of conflict patterns found:\n'
	 out.append(content)
	 Set<String> keys = ca.getProjectsSummary().keySet()
	 for(String key: keys){
	 def row = [key+": "+ ca.getProjectsSummary().get(key)]
	 out.append row.join(',')
	 out.append '\n'
	 }
	 }*/

	public static void printAnalizedProjectsReport(ArrayList<Project> projects){

		def fileName = 'ProjectsSummary.csv'
		def out = new File(fileName)
		// deleting old files if it exists
		out.delete()
		out = new File(fileName)

		def content = projects.size + ' projects analyzed so far.' +
				'\nBelow you will find the list of analyzed projects and their conflict rate.\n'

		out.append(content)

		for(Project project : projects){
			def row = project.getName() + ': ' +
					project.getConflictRate() + '%\n'
			out.append(row)
		}

		printInputDataToScriptR(projects)
	}

	private static void printInputDataToScriptR(ArrayList<Project> projects){
		printProjectsData(projects)
		callRScript()
	}


	private static void printProjectsData(ArrayList<Project> projects){
		String fileName = 'projectsPatternData.csv'
		def out = new File(fileName)

		// deleting old files if it exists
		out.delete()

		out = new File(fileName)

		def row = 'Project Merge_Scenarios Conflicting_Scenarios DefaultValueAnnotation ImplementList ModifierList EditSameMC SameSignatureCM AddSameFd EditSameFd\n'

		out.append(row)

		for(Project p : projects){
			int DefaultValueAnnotation = p.projectSummary.get("DefaultValueAnnotation")
			int ImplementList = p.projectSummary.get("ImplementList")
			int ModifierList = p.projectSummary.get("ModifierList")
			int EditSameMC = p.projectSummary.get("EditSameMC")
			int SameSignatureCM = p.projectSummary.get("SameSignatureCM")
			int AddSameFd = p.projectSummary.get("AddSameFd")
			int EditSameFd = p.projectSummary.get("EditSameFd")
			String conflicts = ' ' + DefaultValueAnnotation + ' ' + ImplementList + ' ' + ModifierList + ' ' + EditSameMC + ' ' + SameSignatureCM + ' ' + AddSameFd + ' ' + EditSameFd
			row = p.name + ' ' + p.analyzedMergeScenarios + ' ' + p.conflictingMergeScenarios + conflicts + '\n'
			out.append(row)
		}
	}


	private static void callRScript(){
		String propsFile = "resultsScript.r"
		ProcessBuilder pb = new ProcessBuilder("Rscript", propsFile)
		//pb.directory(new File(this.gitminerLocation))
		pb.redirectOutput(ProcessBuilder.Redirect.INHERIT)
		// Start the process.
		try {
			Process p = pb.start()
			p.waitFor()
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void printProjectReport(Project project){
		String fileName = "ResultData" + File.separator + project.getName() + File.separator + 'Project' + project.getName() + 'Report.csv'
		def out = new File(fileName)

		// deleting old files if it exists
		out.delete()

		out = new File(fileName)

		String projectSummary = 'Project ' + project.name + '\nAnalyzed merge scenarios: ' +
				project.analyzedMergeScenarios + '\nConflicting merge scenarios: ' +
				project.conflictingMergeScenarios + '\nProject conflict rate: ' +
				project.conflictRate + '%\nSummary of conflict patterns found:\n'

		out.append(projectSummary)
		Set<String> keys = project.projectSummary.keySet()
		for(String key: keys){

			def row = [key+": "+ project.projectSummary.get(key)]
			out.append row.join(',')
			out.append '\n'

		}
	}

	public static void printMergeScenarioReport(MergeScenario mergeScenario, String projectName){


		File out = new File("ResultData" + File.separator + projectName + File.separator + 'MergeScenariosReport.csv')

		if(!out.exists()){
			String fileHeader = 'Merge_scenario Total_Files Files_Edited_By_One_Dev ' +
					'Files_That_Remained_The_Same Files_Merged Files_With_Conflicts Total_Conflicts ' +
					'DefaultValueAnnotation ImplementList ModifierList EditSameMC' +
					'SameSignatureCM AddSameFd EditSameFd\n'
			out.append(fileHeader)
		}else{
			out.append(mergeScenario.toString())
			out.append('\n')
		}
		printMergeScenarioMetrics(mergeScenario, projectName)
		printConflictsReport(mergeScenario, projectName)
	}

	public static void printMergeScenarioMetrics(MergeScenario mergeScenario, String projectName){
		File out = new File('ResultData' + File.separator + projectName + File.separator +
				'Merge_Scenarios' + File.separator + mergeScenario.name + '.csv')
		String header = 'File Total_of_Conflicts Conflicts_Outside_Methods ' +
				'Conflicts_Inside_Methods Methods_with_Conflicts\n'
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

			for(MethodOrConstructor moc : mergedFile.getMethodsWithConflicts()){
				Conflict x = moc.getConflict()
				def row = ['Conflict type: '+ x.getType() + '\n' + 'Conflict body: ' + '\n' + x.getBody() ]
				out.append row.join(',')
				out.append '\n'
				row = ['File path: ' + x.getFilePath()]
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
