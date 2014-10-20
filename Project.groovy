package conflictsAnalyzer

class Project {

	private ArrayList<MergeScenario> mergeScenarios

	private String name

	
	
	private int conflictingRate

	public Project(String projectName, String mergeScenariosPath){

		this.name = projectName
		
		this.createMergeScenarios(mergeScenariosPath)
		
	}
	
	public void createMergeScenarios(String mergeScenariosPath){
		this.mergeScenarios = new ArrayList<MergeScenario>()
		def mergeScenarioFile = new File(mergeScenariosPath)
		mergeScenarioFile.eachLine {
			MergeScenario ms = new MergeScenario(it)
			this.mergeScenarios.add(ms)
		}
		
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

	public void analyzeConflicts(){
		int analyzedMergeScenariosNumber = 0
		for(MergeScenario ms : this.mergeScenarios){
			ms.analyzeConflicts()
			analyzedMergeScenariosNumber++
		}
	}
	
	private void updateConflictingMSRate(){}

	private Hashtable<String, Integer> initializeProjectSummary(){

		Hashtable<String, Integer> projectSummary = new Hashtable<String, Integer>()

		for(SSMergeConflicts c : SSMergeConflicts.values()){

			String type = c.toString()
			projectSummary.put(type, 0)
		}

		return projectSummary
	}
}
