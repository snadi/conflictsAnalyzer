package main

import java.util.Map;

class Project {

	private ArrayList<MergeScenario> mergeScenarios

	private String name

	private int analyzedMergeScenarios

	private int conflictingMergeScenarios

	private double conflictRate

	private Map<String, Conflict> projectSummary
	
	private File mergeScenarioFile
	
	private Map<String, Integer> sameSignatureCMSummary

	public Project(String projectName, String mergeScenariosPath){

		this.name = projectName
		initializeProjectSummary()
		createMergeScenarios(mergeScenariosPath)
		initializeProjectMetrics()
		this.createSameSignatureCMSummary()
		this.createProjectDir()
	}
	
	/*The following constructor is used to initialize projects
	 * that were already analyzed
	 */
	public Project(String projectName, int totalScenarios, int conflictingscenarios,
	HashMap<String, Conflict> projectSummary, HashMap<String, Integer> sscmSummary){

		this.name = projectName
		this.analyzedMergeScenarios = totalScenarios
		this.conflictingMergeScenarios = conflictingscenarios
		this.computeConflictingRate()
		this.projectSummary = projectSummary
		this.sameSignatureCMSummary = sscmSummary
	}
	
	
	public void createSameSignatureCMSummary(){
		this.sameSignatureCMSummary = ConflictSummary.initializeSameSignatureCMSummary()
	}

	private void createProjectDir(){
		String projectData = "ResultData" + File.separator + this.name
		new File(projectData).mkdir()
		new File(projectData + File.separator + 'Merge_Scenarios').mkdir()
	}

	private initializeProjectMetrics() {
		this.analyzedMergeScenarios = 0
		this.conflictingMergeScenarios = 0
		this.conflictRate = 0.0
	}

	public void createMergeScenarios(String mergeScenariosPath){
		this.mergeScenarios = new ArrayList<MergeScenario>()
		this.mergeScenarioFile = new File(mergeScenariosPath)

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

	public double getConflictRate(){
		return this.conflictRate
	}

	public Hashtable<String, Conflict> getProjectSummary(){
		return this.projectSummary
	}

	public void analyzeConflicts(){
		this.mergeScenarioFile.eachLine {
			MergeScenario ms = new MergeScenario(it)
			this.mergeScenarios.add(ms)
			ms.analyzeConflicts()
			updateAndPrintSummary(ms)
			ms.deleteMSDir()
		}
	}

	private printResults(MergeScenario ms) {
		ConflictPrinter.printMergeScenarioReport(ms, this.name)
		ConflictPrinter.updateProjectData(this)
	}

	private void updateAndPrintSummary(MergeScenario ms){
		updateConflictingRate(ms)
		if(ms.hasConflicts){
			updateProjectSummary(ms)
			updateSameSignatureCMSummary(ms)
		}
		printResults(ms)
	}

	private updateConflictingRate(MergeScenario ms) {
		this.analyzedMergeScenarios++
		if(ms.hasConflicts){
			this.conflictingMergeScenarios++
		}
		this.computeConflictingRate()
	}

	private void computeConflictingRate(){

		double cr = (this.conflictingMergeScenarios/
				this.analyzedMergeScenarios) * 100
		this.conflictRate = cr.round(2)

	}

	private void initializeProjectSummary(){

		this.projectSummary = ConflictSummary.initializeConflictsSummary()

	}

	private void updateProjectSummary(MergeScenario ms){
		
		for(SSMergeConflicts c : SSMergeConflicts.values()){
			Conflict conflict = ms.getMergeScenarioSummary().get(c.toString())
			this.projectSummary = ConflictSummary.updateConflictsSummary(this.projectSummary, conflict)
		}
	}
	
	private void updateSameSignatureCMSummary(MergeScenario ms){
		for(PatternSameSignatureCM p : PatternSameSignatureCM.values()){
			String cause = p.toString()
			int quantity = ms.sameSignatureCMSummary.get(cause)
			quantity = quantity + this.sameSignatureCMSummary.get(cause)
			this.sameSignatureCMSummary.put(cause, quantity)
		}
	}
	
	public String toString(){
		String result = this.name + ', ' + this.analyzedMergeScenarios + ', ' +
		this.conflictingMergeScenarios + ', ' +
		ConflictSummary.printConflictsSummary(this.projectSummary) + ', ' +
		ConflictSummary.printSameSignatureCMSummary(this.sameSignatureCMSummary)

		return result
	}	
}
