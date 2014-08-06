package conflictsAnalyzer

class ConflictPrinter {
	
	public void writeConflicts(ArrayList<Conflict> conflictsList){}
	
	
	public void writeConflictsReport(Hashtable<String, Integer> conflictsReport){
		
		def out = new File('conflictReport.csv')
		
		// deleting old files if it exists
		out.delete()
		
		out = new File('conflictReport.csv')
		
		Set<String> keys = conflictsReport.keySet();
        for(String key: keys){
			
			def row = [key+": "+ conflictsReport.get(key)]
			out.append row.join(',')
			out.append '\n'
            
        }
		
	}
	
	

}
