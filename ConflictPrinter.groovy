package conflictsAnalyzer

import java.util.ArrayList;

public class ConflictPrinter {

	public void writeConflicts(ArrayList<Conflict> conflictsList){}


	public void printConflictsReport(Hashtable<String, Integer> conflictsReport, String revisionFilePath){

		def out = new File('conflictReport.csv')

		out.append('Revision: ' + revisionFilePath + '\n')

		Set<String> keys = conflictsReport.keySet();
		for(String key: keys){

			def row = [key+": "+ conflictsReport.get(key)]
			out.append row.join(',')
			out.append '\n'

		}

	}

	def printConflictsList(ArrayList<Conflict> conflictsList, String revisionFilePath){

		def out = new File('conflictList.csv')

		def delimiter = '========================================================='
		out.append(delimiter)
		out.append '\n'

		out.append('Revision: ' + revisionFilePath + '\n')


		for(Conflict conflict : conflictsList){

			def row = ['Conflict type: '+ conflict.getType() + '\n' + 'Conflict body: ' + '\n' +conflict.getBody() ]
			out.append row.join(',')
			out.append '\n'
			row = ['File path: ' + conflict.getFilePath()]
			out.append row.join(',')
			out.append '\n'

		}

		out.append '\n'
		out.append(delimiter)

	}
	
	//three methods below that will be used after restructuring the architecture
	public static void printProjectsReport(ArrayList<Project> projects){

		for(Project p: projects){

		}


	}

	public static void printMergeScenariosReport(ArrayList<MergeScenario> mergeScenarios){
		for(MergeScenario ms: mergeScenarios){

		}
	}

	public static void printConflictsReport(ArrayList<Conflict> conflicts){
		for(Conflict c: conflicts){

		}
	}
}
