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

	public static void printAnalizedProjectsReport(RunStudy rs){

		def fileName = 'ProjectsSummary.csv'
		def out = new File(fileName)
		// deleting old files if it exists
		out.delete()
		out = new File(fileName)

		def content = rs.projects.size + ' projects analyzed so far.' +
				'\nBelow you will find the list of analyzed projects and their conflict rate.\n'

		out.append(content)

		for(Project project : rs.projects){
			def row = project.getName() + ': ' +
					project.getConflictRate() + '%\n'
			out.append(row)
		}

		printInputDataToScriptR(rs)
	}

	private static void printInputDataToScriptR(RunStudy rs){
		printProjectsData(rs)
		callRScript()
	}


	private static void printProjectsData(RunStudy rs){
		String fileName = 'projectsPatternData.csv'
		def out = new File(fileName)

		// deleting old files if it exists
		out.delete()

		out = new File(fileName)

		def row = 'Project Merge_Scenarios Conflicting_Scenarios DefaultValueAnnotation ImplementList ModifierList EditSameMC SameSignatureCM AddSameFd EditSameFd\n'

		out.append(row)

		for(Project p : rs.getProjects()){
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
		
		
		def out = new File("ResultData" + File.separator + projectName + File.separator + 'MergeScenariosReport.csv')

		out.append('Merge scenario: ' + mergeScenario.path + '\n')

		Set<String> keys = mergeScenario.mergeScenarioSummary.keySet()
		for(String key: keys){

			def row = [key+": "+ mergeScenario.mergeScenarioSummary.get(key)]
			out.append row.join(',')
			out.append '\n'

		}

		printConflictsReport(mergeScenario.getConflicts(), mergeScenario.path, projectName)
	}

	public static void printConflictsReport(ArrayList<Conflict> conflicts, String mergeScenarioPath, String projectName){

		def out = new File("ResultData" + File.separator + projectName + File.separator + 'ConflictsReport.csv')

		def delimiter = '========================================================='
		out.append(delimiter)
		out.append '\n'

		out.append('Revision: ' + mergeScenarioPath + '\n')
		for(Conflict c: conflicts){

			def row = ['Conflict type: '+ c.getType() + '\n' + 'Conflict body: ' + '\n' + c.getBody() ]
			out.append row.join(',')
			out.append '\n'
			row = ['File path: ' + c.getFilePath()]
			out.append row.join(',')
			out.append '\n'

		}
		out.append '\n'
		out.append(delimiter)
	}

	public static void main (String[] args){
		String propsFile = "meuscript.r"
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
}
