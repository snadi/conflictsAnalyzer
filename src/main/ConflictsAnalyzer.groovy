package main


import java.util.Hashtable;


import merger.FSTGenMerger

class ConflictsAnalyzer {

	private ArrayList<Project> projects

	private int analyzedProjects, analyzedMergeScenarios,
	conflictingMergeScenarios

	private double projectsConflictRate

	private Hashtable<String, Integer> projectsSummary

	public ConflictsAnalyzer(String projectData){
		createProjects(projectData)
		initializeProjectsSummary()
		initializeProjectsMetrics()
	}

	public void initializeProjectsSummary(){
		this.projectsSummary = new Hashtable<String, Integer>()

		for(SSMergeConflicts c : SSMergeConflicts.values()){

			String type = c.toString()
			projectsSummary.put(type, 0)
		}
	}

	public void initializeProjectsMetrics(){
		this.analyzedProjects; this.analyzedMergeScenarios;
		this.conflictingMergeScenarios = 0
		this.projectsConflictRate = 0.0
	}

	public void analyzeConflicts(String projectData){

		for(Project p : this.projects){
			p.analyzeConflicts()
			updateAndPrintResults(p)
		}
	}

	public void updateAndPrintResults(Project p){
		updateConflictRate(p)
		updateProjectsSummary(p)
		ConflictPrinter.printProjectsReport(this)
	}

	public void updateConflictRate(Project p){
		this.analyzedProjects++
		this.analyzedMergeScenarios += p.analyzedMergeScenarios
		this.conflictingMergeScenarios += p.conflictingMergeScenarios

		double cr = (this.conflictingMergeScenarios/
				this.analyzedMergeScenarios) * 100
		this.projectsConflictRate = cr.round(2)
	}

	public void updateProjectsSummary(Project p){
		Set<String> keys = this.projectsSummary.keySet();

		for(String key: keys){
			int mergeQuantity = p.getProjectSummary().get(key).value
			int projectsQuantity = this.projectsSummary.get(key).value
			projectsQuantity = projectsQuantity + mergeQuantity
			this.projectsSummary.put(key, projectsQuantity)
		}
	}

	public void createProjects(String pData){
		this.projects = new ArrayList<Project>()
		def projectData = new FileWithConflicts(pData)
		projectData.eachLine {
			String[] p = it.split(',')
			String projectName = p[0].trim()
			String projectMergeScenarios = p[1].trim()
			Project project = new Project(projectName, projectMergeScenarios)
			this.projects.add(project)

		}
	}

	public ArrayList<Project> getProjects(){
		return this.projects
	}

	public void setProjects(ArrayList<Project> p){
		this.projects = p
	}

	public int getAnalyzedProjects(){
		return this.analyzedProjects
	}

	public int getAnalyzedMergeScenarios(){
		return this.analyzedMergeScenarios
	}

	public int getConflictingMergeScenarios(){
		return this.conflictingMergeScenarios
	}

	public double getProjectsConflictRate(){
		return this.projectsConflictRate
	}

	public Hashtable<String, Integer> getProjectsSummary(){
		return this.projectsSummary
	}


	public static void main (String[] args){

		ConflictsAnalyzer ca = new ConflictsAnalyzer('/Users/paolaaccioly/Desktop/teste.csv')
		ca.analyzeConflicts()

	}
}
