package conflictsAnalyzer


class ProjectController {
	
	Hashtable<String, Integer> projectReport
	
	def runProjectMerges(String revisionFiles){
		
		def mergeRevisions = new File(revisionFiles)
		
		mergeRevisions.eachLine {
			
			runMerge(it)
			
		}
		
		
		
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
	
	
	def printProjectReport(){}
	
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
		String revisionFiles = ''
		ProjectController pc = new ProjectController()
		pc.analyzeProjectConflicts(revisionFiles)
	}
	
	

}
