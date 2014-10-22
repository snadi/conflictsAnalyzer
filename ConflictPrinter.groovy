package conflictsAnalyzer

import java.util.ArrayList;

public class ConflictPrinter {

	
	public static void printProjectsReport(ConflictsAnalyzer ca){
		
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

	}
	
	public static void printProjectReport(Project project){
		String fileName = 'Project' + project.getName() + 'Report.csv'
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
	
	public static void printMergeScenarioReport(MergeScenario mergeScenario){
		def out = new File('MergeScenariosReport.csv')

		out.append('Merge scenario: ' + mergeScenario.path + '\n')

		Set<String> keys = mergeScenario.mergeScenarioSummary.keySet()
		for(String key: keys){

			def row = [key+": "+ mergeScenario.mergeScenarioSummary.get(key)]
			out.append row.join(',')
			out.append '\n'

		}

		printConflictsReport(mergeScenario.getConflicts(), mergeScenario.path)
	}

	public static void printConflictsReport(ArrayList<Conflict> conflicts, String mergeScenarioPath){

		def out = new File('ConflictsReport.csv')

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
}
