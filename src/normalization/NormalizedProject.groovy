package normalization

class NormalizedProject {
	
	String name
	
	String resultDir 
	
	int numberChangesOutsideMethods
	
	int numberChangesInsideMethods
	
	Map<String, Integer> conflictsSummary
	
	Map<String, Double> normalizedConflictSummary
	
	public NormalizedProject(String n, String resultData){
		this.name = n
		this.resultDir = resultData
	}
	
	public void loadConflictsSummary(){
		
	}
	
	public void computeNumberOfChanges(){
		
	}
	
	public String toString(){
		
	}
}
