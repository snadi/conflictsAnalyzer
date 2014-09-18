package conflictsAnalyzer


class ProjectController {
	
	Hashtable<String, Integer> projectReport
	
	def runProjectMerges(String revisionFiles){
		
		def mergeRevisions = new File(revisionFiles)
		
		mergeRevisions.eachLine {
			
			if(!it.empty){
			String revision = it.substring(0, it.length()-25)
			println ('Running the following revision: ' + revision)
			//replaceUnwantedStrings(revision)
			runMerge(it.trim())
			}
			
			
		}
		
		
		
	}
	
	def replaceUnwantedStrings(String dir){
		
		//replace ... by []
		String c1 = "grep -rl '\\.\\.\\.' " + dir + " | xargs sed -i '' 's/\\.\\.\\./[]/g'" 
		c1.execute()
		
		//replace u0000 by u
		def c2 = "grep -rl '\\u0000' " + dir + " | xargs sed -i '' 's/\\u0000/u/g'"
		c2.execute()
		
		def c3 = "grep -rl '\\u07FF' " + dir + " | xargs sed -i '' 's/\\u07FF/u/g'"
		c3.execute()
		
		
		
	}
	
	def runMerge(String revisionFile){
		
		
		ConflictsController c = new ConflictsController()
		c.run(revisionFile)
		Hashtable<String, Integer> mergeReport = c.getConflictsReport()
		updateProjectReport(mergeReport)
		
		
		
	}
	
	
	def updateProjectReport(Hashtable<String, Integer> mergeReport){
		
		Set<String> keys = projectReport.keySet();
		for(String key: keys){
			
			int quantity = projectReport.get(key).value
			quantity = quantity + mergeReport.get(key).value
			projectReport.put(key, quantity)
			
		}
		
		
	}
	
	
	def printProjectReport(){
		
		def out = new File('ProjectReport.csv')
		
		// deleting old files if it exists
		out.delete()
		
		out = new File('ProjectReport.csv')
		
		Set<String> keys = projectReport.keySet();
		for(String key: keys){
			
			def row = [key+": "+ projectReport.get(key)]
			out.append row.join(',')
			out.append '\n'
			
		}
	}
	
	def initializeProjectReport(){
		
		this.projectReport = new Hashtable<String, Integer>()
		
		for(SSMergeConflicts c : SSMergeConflicts.values()){
			
			String type = c.toString();
			this.projectReport.put(type, 0)
		}
		
	}
	
	def analyzeProjectConflicts(String revisionFiles){
		
		initializeProjectReport()
		
		runProjectMerges(revisionFiles)
		
		printProjectReport()
	}
	
	public static void main (String[] args){
		String revisionFiles = '/Users/paolaaccioly/Documents/Doutorado/study_data/mockito/2nd_round/RevisionsFiles.csv'
		ProjectController pc = new ProjectController()
		pc.analyzeProjectConflicts(revisionFiles)
		
		
		
	}
	
	

}
