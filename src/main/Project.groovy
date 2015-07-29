package main

class Project {

	private ArrayList<MergeScenario> mergeScenarios

	private String name

	private int analyzedMergeScenarios

	private int conflictingMergeScenarios

	private double conflictRate

	private Hashtable<String, Conflict> projectSummary
	
	private File mergeScenarioFile

	public Project(String projectName, String mergeScenariosPath){

		this.name = projectName
		initializeProjectSummary()
		createMergeScenarios(mergeScenariosPath)
		initializeProjectMetrics()
		this.createProjectDir()
	}

	/*The following constructor is used to initialize projects
	 * that were already analyzed
	 */
	public Project(String projectName, int totalScenarios, int conflictingscenarios,
	Hashtable<String, Conflict> projectSummary){

		this.name = projectName
		this.analyzedMergeScenarios = totalScenarios
		this.conflictingMergeScenarios = conflictingscenarios
		this.computeConflictingRate()
		this.projectSummary = projectSummary
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
	}

	private void updateAndPrintSummary(MergeScenario ms){
		updateConflictingRate(ms)
		if(ms.hasConflicts){
			updateProjectSummary(ms)
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

		this.projectSummary = new Hashtable<String, Conflict>()

		for(SSMergeConflicts c : SSMergeConflicts.values()){

			String type = c.toString()
			projectSummary.put(type, new Conflict(type))
		}


	}

	private void updateProjectSummary(MergeScenario ms){
		for(SSMergeConflicts c : SSMergeConflicts.values()){
			Conflict conflict = ms.getMergeScenarioSummary().get(c.toString())
			Conflict c2 = this.projectSummary.get(c.toString())
			
			//get new values
			int numberOfConflicts = conflict.getNumberOfConflicts() + c2.getNumberOfConflicts()
			int differentSpacing = conflict.getDifferentSpacing() + c2.getDifferentSpacing()
			int consecutiveLines = conflict.getConsecutiveLines() + c2.getConsecutiveLines()
			int falsePositivesIntersection = conflict.falsePositivesIntersection +
			c2.getFalsePositivesIntersection()
			
			//set new values
			c2.setNumberOfConflicts(numberOfConflicts)
			c2.setDifferentSpacing(differentSpacing)
			c2.setConsecutiveLines(consecutiveLines)
			c2.setFalsePositivesIntersection(falsePositivesIntersection)
		}
	}
	
	public String toString(){
		String result = this.name + ' ' + this.analyzedMergeScenarios + ' ' +
		this.conflictingMergeScenarios + ' '
		
		String noPattern = SSMergeConflicts.NOPATTERN.toString()
		for(SSMergeConflicts c : SSMergeConflicts.values()){
			String type = c.toString()
			Conflict conflict = this.projectSummary.get(type)
			result = result + conflict.getNumberOfConflicts() + ' '
			if(!type.equals(noPattern)){
				result = result + conflict.getDifferentSpacing() + ' ' +
				conflict.getConsecutiveLines() + ' ' + conflict.getFalsePositivesIntersection() +
				' '
			}
		}
		
		return result.trim()
	}	
}
