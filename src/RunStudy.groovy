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
	conflictingMergeScenarios

	private double projectsConflictRate

	private Hashtable<String, Integer> projectsSummary
	
	public RunStudy(){
		this.projects = new ArrayList<Project>()
		initializeProjectsSummary()
		initializeProjectsMetrics()
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
	
	public void removeUnwantedCharacters(ArrayList<String> unwantedStrings){
		
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
		updateProjectsSummary(p)
		ConflictPrinter.printAnalizedProjectsReport(this)
	}
	
	public double getProjectsConflictRate(){
		return this.projectsConflictRate
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
	
	
	public static void main (String[] args){
		RunStudy study = new RunStudy()
		String[] files= ['projectsList', 'configuration.properties']
		study.run(files)	
		
		
	}

}
