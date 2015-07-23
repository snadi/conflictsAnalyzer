package main

class Project {

	private ArrayList<MergeScenario> mergeScenarios

	private String name

	private int analyzedMergeScenarios

	private int conflictingMergeScenarios

	private double conflictRate
	
	private int conflictsDueToDifferentSpacingMC
	
	private int conflictsDueToConsecutiveLinesMC
	
	private int falsePositivesIntersectionMC
	
	private int conflictsDueToDifferentSpacingFd
	
	private int conflictsDueToConsecutiveLinesFd
	
	private int falsePositivesIntersectionFd

	private Hashtable<String, Integer> projectSummary

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
	int conflictsDiffSpacingMC, int conflictsConsecLinesMC, falsePositivesIntersectionMC, conflictsDueToDifferentSpacingFd, 
			conflictsDueToConsecutiveLinesFd, falsePositivesIntersectionFd ,Hashtable<String, Integer> projectSummary){

		this.name = projectName
		this.analyzedMergeScenarios = totalScenarios
		this.conflictingMergeScenarios = conflictingscenarios
		this.computeConflictingRate()
		this.projectSummary = projectSummary
		this.conflictsDueToDifferentSpacingMC = conflictsDiffSpacingMC
		this.conflictsDueToConsecutiveLinesMC = conflictsConsecLinesMC
		this.falsePositivesIntersectionMC = falsePositivesIntersectionMC
		this.conflictsDueToDifferentSpacingFd = conflictsDueToDifferentSpacingFd
		this.conflictsDueToConsecutiveLinesFd = conflictsDueToConsecutiveLinesFd
		this.falsePositivesIntersectionFd = falsePositivesIntersectionFd
	}

	/*This constructor is used by the CsvAnalyzer class*/
	public Project(String projectName){
		this.name = projectName
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

	public double getConflictRate(){
		return this.conflictRate
	}

	public Hashtable<String, Integer> getProjectSummary(){
		return this.projectSummary
	}

	public void analyzeConflicts(){

		for(MergeScenario ms : this.mergeScenarios){
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

		this.projectSummary = new Hashtable<String, Integer>()

		for(SSMergeConflicts c : SSMergeConflicts.values()){

			String type = c.toString()
			projectSummary.put(type, 0)
		}


	}

	private void updateProjectSummary(MergeScenario ms){
		Set<String> keys = this.projectSummary.keySet()

		for(String key: keys){
			int mergeQuantity = ms.mergeScenarioSummary.get(key).value
			int projectQuantity = this.projectSummary.get(key).value
			projectQuantity = projectQuantity + mergeQuantity
			this.projectSummary.put(key, projectQuantity)
		}
		this.updateFalsePositives(ms)
	}
	
	private void updateFalsePositives(MergeScenario ms){
		this.conflictsDueToDifferentSpacingMC = this.conflictsDueToDifferentSpacingMC +
		 ms.getConflictsDueToDifferentSpacingMC()
		
		this.conflictsDueToConsecutiveLinesMC = this.conflictsDueToConsecutiveLinesMC +
		ms.getConflictsDueToConsecutiveLinesMC()
		
		this.falsePositivesIntersectionMC = this.falsePositivesIntersectionMC +
		ms.getFalsePositivesIntersectionMC()
		
		this.conflictsDueToDifferentSpacingFd = this.conflictsDueToDifferentSpacingFd +
		ms.getConflictsDueToDifferentSpacingFd()
	   
	   this.conflictsDueToConsecutiveLinesFd = this.conflictsDueToConsecutiveLinesFd +
	   ms.getConflictsDueToConsecutiveLinesFd()
	   
	   this.falsePositivesIntersectionFd = this.falsePositivesIntersectionFd +
	   ms.getFalsePositivesIntersectionFd()
		
	}
	
	public int getConflictsDueToDifferentSpacingMC() {
		return conflictsDueToDifferentSpacingMC;
	}

	public void setConflictsDueToDifferentSpacingMC(int conflictsDueToDifferentSpacingMC) {
		this.conflictsDueToDifferentSpacingMC = conflictsDueToDifferentSpacingMC;
	}

	public int getConflictsDueToConsecutiveLinesMC() {
		return conflictsDueToConsecutiveLinesMC;
	}

	public void setConflictsDueToConsecutiveLinesMC(int conflictsDueToConsecutiveLinesMC) {
		this.conflictsDueToConsecutiveLinesMC = conflictsDueToConsecutiveLinesMC;
	}

	public int getFalsePositivesIntersectionMC() {
		return falsePositivesIntersectionMC;
	}

	public void setFalsePositivesIntersectionMC(int falsePositivesIntersectionMC) {
		this.falsePositivesIntersectionMC = falsePositivesIntersectionMC;
	}

	public int getConflictsDueToDifferentSpacingFd() {
		return conflictsDueToDifferentSpacingFd;
	}

	public void setConflictsDueToDifferentSpacingFd(int conflictsDueToDifferentSpacingFd) {
		this.conflictsDueToDifferentSpacingFd = conflictsDueToDifferentSpacingFd;
	}

	public int getConflictsDueToConsecutiveLinesFd() {
		return conflictsDueToConsecutiveLinesFd;
	}

	public void setConflictsDueToConsecutiveLinesFd(int conflictsDueToConsecutiveLinesFd) {
		this.conflictsDueToConsecutiveLinesFd = conflictsDueToConsecutiveLinesFd;
	}

	public int getFalsePositivesIntersectionFd() {
		return falsePositivesIntersectionFd;
	}

	public void setFalsePositivesIntersectionFd(int falsePositivesIntersectionFd) {
		this.falsePositivesIntersectionFd = falsePositivesIntersectionFd;
	}
		
}
