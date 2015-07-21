package main
import java.util.Hashtable

/*this class is supposed to integrate all the 3 steps involved to run the study
 * gitminer/gremlinQuery/ConflictsAnalyzer
 */

class RunStudy {


	private String gitminerConfigProps = 'gitminerConfiguration.properties'
	private String projectName
	private String projectRepo
	private String gitminerLocation
	private String downloadPath
	private ArrayList<Project> projects
	private int analyzedProjects, analyzedMergeScenarios,
	conflictingMergeScenarios, conflictsDueToDifferentSpacing,
	conflictsDueToConsecutiveLines

	private double projectsConflictRate

	private Hashtable<String, Integer> projectsSummary

	public RunStudy(){
		this.projects = new ArrayList<Project>()
		initializeProjectsMetrics()
		if(previousResultsExists()){
			loadPreviousResults()
		}
	}

	public void run(String[] args){
		def projectsList = new File(args[0])
		updateGitMinerConfig(args[1])
		projectsList.eachLine {
			setProjectNameAndRepo(it)
			String graphBase = runGitMiner()
			String revisionFile = runGremlinQuery(graphBase)
			runConflictsAnalyzer(this.projectName, revisionFile)
		}

	}

	public boolean previousResultsExists(){
		boolean result = false
		File projects = new File('projectsPatternData.csv')
		if(projects.exists()){
			result = true
		}
		return result
	}
	
	public ArrayList<Project> getProjects(){
		return this.projects
	}
	
	public Hashtable<String, Integer> getProjectsSummary(){
		return this.projectsSummary
	}

	public void initializeProjectsMetrics(){
		this.analyzedProjects = this.projects.size()
		this.analyzedMergeScenarios = sumAnalyzedMergeScenarios()
		this.conflictingMergeScenarios = sumConflictingMergeScenarios()
		computeConflictRate()
	}
	
	public int sumAnalyzedMergeScenarios(){
		int sum = 0
		for(Project project : this.projects){
			sum = sum + project.analyzedMergeScenarios
		}
		return sum
	}
	
	public int sumConflictingMergeScenarios(){
		int sum = 0
		for(Project project : this.projects){
			sum = sum + project.conflictingMergeScenarios
		}
		return sum
	}
	
	public void loadPreviousResults(){
		readProjectData()
		initializeProjectsMetrics()
	}

	public void readProjectData(){
		def projectFile = new File("projectsPatternData.csv")
		boolean header = true
		projectFile.eachLine {
			if((!header) && (!it.empty)){
				initializeProject(it)
			}
			header = false
		}

	}

	public void initializeProject(String line){
		String[] data = line.split(" ")
		String projectName = data[0]
		int totalScenarios = Integer.parseInt(data[1])
		int conflictingScenarios = Integer.parseInt(data[2])
		int conflictsDueToDifferentSpacing = Integer.parseInt(data[3])
		int conflictsDueToConsecutiveLines = Integer.parseInt(data[4])
		int DefaultValueAnnotation = Integer.parseInt(data[5])
		int ImplementList = Integer.parseInt(data[6])
		int ModifierList = Integer.parseInt(data[7])
		int EditSameMC = Integer.parseInt(data[8])
		int SameSignatureCM = Integer.parseInt(data[9])
		int AddSameFd = Integer.parseInt(data[10])
		int EditSameFd = Integer.parseInt(data[11])
		int ExtendsList = Integer.parseInt(data[12])
		Hashtable<String, Integer> projectSummary = new Hashtable<String, Integer>()
		projectSummary.put("DefaultValueAnnotation", DefaultValueAnnotation)
		projectSummary.put("ImplementList", ImplementList)
		projectSummary.put("ModifierList", ModifierList)
		projectSummary.put("EditSameMC", EditSameMC)
		projectSummary.put("SameSignatureCM", SameSignatureCM)
		projectSummary.put("AddSameFd", AddSameFd)
		projectSummary.put("EditSameFd", EditSameFd)
		projectSummary.put("ExtendsList", ExtendsList)
		Project project = new Project(projectName, totalScenarios, conflictingScenarios, conflictsDueToDifferentSpacing, 
			conflictsDueToConsecutiveLines, projectSummary)
		this.projects.add(project)
	}

	public void updateGitMinerConfig(String configFile){
		Properties gitminerProps =  new Properties()
		File gitminerPropsFile = new File(this.gitminerConfigProps)
		gitminerProps.load(gitminerPropsFile.newDataInputStream())


		Properties configProps = new Properties()
		File propsFile = new File(configFile)
		configProps.load(propsFile.newDataInputStream())

		this.gitminerLocation = configProps.getProperty('gitminer.path')
		this.downloadPath = configProps.getProperty('downloads.path')
		String graphDb = this.gitminerLocation + File.separator + 'graph.db'
		String repo_Loader = this.gitminerLocation + File.separator + 'repo_loader'

		gitminerProps.setProperty('net.wagstrom.research.github.dburl', graphDb)
		gitminerProps.setProperty('edu.unl.cse.git.localStore', repo_Loader)
		gitminerProps.setProperty('edu.unl.cse.git.repositories', graphDb)
		gitminerProps.setProperty('edu.unl.cse.git.dburl', graphDb)

		gitminerProps.setProperty('net.wagstrom.research.github.login', configProps.getProperty('github.login'))
		gitminerProps.setProperty('net.wagstrom.research.github.password', configProps.getProperty('github.password'))
		gitminerProps.setProperty('net.wagstrom.research.github.email', configProps.getProperty('github.email'))
		gitminerProps.setProperty('net.wagstrom.research.github.token', configProps.getProperty('github.token'))

		gitminerProps.store(gitminerPropsFile.newWriter(), null)

	}

	public void setProjectNameAndRepo(String project){
		String[] projectData = project.split('/')
		this.projectName = projectData[1].trim()
		this.projectRepo = project
		println "Starting project " + this.projectName
	}


	public String runGitMiner(){
		updateProjectRepo()
		println "Running gitminer"
		runGitminerCommand('./gitminer.sh')
		runGitminerCommand('./repository_loader.sh')
		String graphBase = renameGraph()
		println "Finished running gitminer"
		return graphBase
	}


	private String renameGraph(){

		String oldFile = this.gitminerLocation + File.separator + 'graph.db'
		String newFile = this.gitminerLocation + File.separator + this.projectName + 'graph.db'
		new File(oldFile).renameTo(new File(newFile))

		return newFile
	}

	public void runGitminerCommand(String command){
		String propsFile = new File("").getAbsolutePath() + File.separator + this.gitminerConfigProps
		ProcessBuilder pb = new ProcessBuilder(command, "-c", propsFile)
		pb.directory(new File(this.gitminerLocation))
		pb.redirectOutput(ProcessBuilder.Redirect.INHERIT)
		// Start the process.
		try {
			Process p = pb.start()
			p.waitFor()
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	private void updateProjectRepo(){
		Properties gitminerProps = new Properties()
		File gitminerPropsFile = new File(this.gitminerConfigProps)
		gitminerProps.load(gitminerPropsFile.newDataInputStream())
		gitminerProps.setProperty('net.wagstrom.research.github.projects', this.projectRepo)
		gitminerProps.setProperty('edu.unl.cse.git.repositories', this.projectRepo)
		gitminerProps.store(gitminerPropsFile.newWriter(), null)
	}

	public String runGremlinQuery(String graphBase){
		println "starting to query the gremlin database and download merge revision"
		GremlinQueryApp gq = new GremlinQueryApp()
		String revisionFile = gq.run(projectName, projectRepo, graphBase, this.downloadPath)
	}

	public void runConflictsAnalyzer(String projectName, String revisionFile){
		println "starting to run the conflicts analyzer on project " + projectName
		Project project = new Project(projectName, revisionFile)
		project.analyzeConflicts()
		this.projects.add(project)
		updateAndPrintResults(project)

	}

	public void updateAndPrintResults(Project p){
		updateConflictRate(p)
		ConflictPrinter.printAnalizedProjectsReport(this.projects)
	}

	public double getProjectsConflictRate(){
		return this.projectsConflictRate
	}

	public void updateConflictRate(Project p){
		this.analyzedProjects++
		this.analyzedMergeScenarios += p.analyzedMergeScenarios
		this.conflictingMergeScenarios += p.conflictingMergeScenarios
		computeConflictRate()
	}

	private computeConflictRate() {

		if(this.analyzedMergeScenarios!=0){
			double cr = (this.conflictingMergeScenarios/
					this.analyzedMergeScenarios) * 100
			this.projectsConflictRate = cr.round(2)
		}
		else{
			this.projectsConflictRate = 0
		}
	}

	public static void main (String[] args){
		RunStudy study = new RunStudy()
		String[] files= ['projectsList', 'configuration.properties']
		study.run(files)
	}

}
