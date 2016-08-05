package main

import java.util.Date;
import java.util.HashMap;
import java.util.Map

import com.sun.org.apache.bcel.internal.generic.RETURN;

import util.ConflictPredictorPrinter;;

class Project {

	private ArrayList<MergeScenario> mergeScenarios

	private String name

	private int analyzedMergeScenarios

	private int conflictingMergeScenarios

	private int conflictingScenariosOnlyNonJava

	private double conflictRate

	private Map<String, Conflict> projectSummary

	private File mergeScenarioFile

	private Map<String, Integer> sameSignatureCMSummary

	private ArrayList<Integer> conflictPredictorSummary

	private int possibleRenamings

	private List<ProjectPeriod> periods

	public Project(String projectName, List<ProjectPeriod> periods = null){
		this.mergeScenarios = new ArrayList<MergeScenario>()
		this.name = projectName
		initializeProjectSummary()
		initializeProjectMetrics()
		this.createSameSignatureCMSummary()
		this.createProjectDir()
		this.periods = periods

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
		this.conflictingScenariosOnlyNonJava = 0
		this.conflictRate = 0.0
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

	public SSMergeResult analyzeConflicts(String revisionFile, boolean resultGitMerge){

		MergeScenario ms = new MergeScenario(revisionFile, resultGitMerge)
		this.mergeScenarios.add(ms)
		ms.analyzeConflicts()
		SSMergeResult result = new SSMergeResult(ms.name, ms.hasConflictsThatWereNotSolved(), ms.getFilesWithMethodsToJoana())
		updateAndPrintSummary(ms)
		ms.deleteMSDir()

		return result
	}

	private printResults(MergeScenario ms, String ms_CPsummary) {
		/*print conflicts report*/
		ConflictPrinter.printMergeScenarioReport(ms, this.name)
		ConflictPrinter.updateProjectData(this)

		/*print conflict predictors report*/
		ConflictPredictorPrinter.printMergeScenarioReport(this, ms,ms_CPsummary)

	}

	private void updateAndPrintSummary(MergeScenario ms){
		updateConflictingRate(ms)
		String ms_summary = updateConflictPredictorSummary(ms)
		if(ms.hasConflicts){
			updateProjectSummary(ms)
			updateSameSignatureCMSummary(ms)
		}
		printResults(ms, ms_summary)
	}

	private String updateConflictPredictorSummary(MergeScenario ms){
		String result = ms.computeMSSummary()
		ArrayList<String> temp = new ArrayList<String>(Arrays.asList(result.split(',')))
		temp.remove(0)
		
		/*initializes conflict predictor summary with zero values*/
		if(this.conflictPredictorSummary==null){
			this.conflictPredictorSummary = new ArrayList<Integer>()
			for(String s: temp){
				this.conflictPredictorSummary.add(0)
			}

		}

		/*updates the array list with new values*/
		ArrayList<String> newConflictPredictorSummary = new ArrayList<Integer>()
		for(int i = 0; i < temp.size(); i++){
			int quantity = Integer.parseInt(temp.get(i))
			quantity = quantity + this.conflictPredictorSummary.get(i)
			newConflictPredictorSummary.add(quantity)
		}
		
		/*updates the reference*/
		this.conflictPredictorSummary = newConflictPredictorSummary

		return result
	}

	private void updateConflictingRate(MergeScenario ms) {
		this.analyzedMergeScenarios++
		if(ms.hasConflicts){
			this.conflictingMergeScenarios++
		}else if(ms.hasConflictsThatWereNotSolved())
		{
			this.conflictingScenariosOnlyNonJava++
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
		this.possibleRenamings = this.possibleRenamings + ms.getPossibleRenamings()
	}

	private void updateSameSignatureCMSummary(MergeScenario ms){
		for(PatternSameSignatureCM p : PatternSameSignatureCM.values()){
			//update cause
			String cause = p.toString()
			int quantity = ms.sameSignatureCMSummary.get(cause)
			quantity = quantity + this.sameSignatureCMSummary.get(cause)
			this.sameSignatureCMSummary.put(cause, quantity)

			//update false positives
			String diffSpacing = cause + 'DS'
			int quantity2 = ms.sameSignatureCMSummary.get(diffSpacing)
			quantity2 = quantity2 + this.sameSignatureCMSummary.get(diffSpacing)
			this.sameSignatureCMSummary.put(diffSpacing, quantity2)
		}
	}

	public String getProjectCSSummary(){
		String result = this.name 
		for(Integer i : this.conflictPredictorSummary){
			result = result + ',' + i
		}
		return result
	}

	public String toString(){
		String result = this.name + ', ' + this.analyzedMergeScenarios + ', ' +
				this.conflictingScenariosOnlyNonJava + ', ' +
				this.conflictingMergeScenarios + ', ' +
				ConflictSummary.printConflictsSummary(this.projectSummary) + ', ' +
				ConflictSummary.printSameSignatureCMSummary(this.sameSignatureCMSummary) + ', ' +
				this.possibleRenamings

		return result
	}

	public List<ProjectPeriod> getProjectPeriods()
	{
		periods
	}
	
	public static void main (String[] args){
		Project project = new Project('Teste')
		project.analyzeConflicts('/Users/paolaaccioly/Desktop/Teste/Example/RevisionsFiles.csv', true)
	}
}
