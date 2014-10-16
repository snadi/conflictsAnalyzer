package conflictsAnalyzer

class Project {

	private ArrayList<MergeScenario> mergeScenarios

	private String name

	public Project(String projectName){

		this.mergeScenarios = new ArrayList<MergeScenario>()

		this.name = projectName
	}

	public void setMergeScenarios(ArrayList<MergeScenario> ms){

		this.mergeScenarios = ms

	}

	public ArrayList<MergeScenario> getMergeScenarios(){

		return this.mergeScenarios

	}

	public void setName(String name){

		this.name = name

	}

	public String getName(){

		return this.name

	}
	
	public Hashtable<String, Integer> computeProjectSummary(){
		
		Hashtable<String, Integer> projectSummary = this.initializeProjectSummary()
		
		
		
		return projectSummary
		
	}
	
	private Hashtable<String, Integer> initializeProjectSummary(){
		
		Hashtable<String, Integer> projectSummary = new Hashtable<String, Integer>()
		
		for(SSMergeConflicts c : SSMergeConflicts.values()){
			
			String type = c.toString();
			projectSummary.put(type, 0)
		}
		
		return projectSummary
	}
}
