package conflictsAnalyzer

import java.util.ArrayList;

class ConflictPrinter {
	
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
	
	

}
