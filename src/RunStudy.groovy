import composer.FileLoader.MyFileFilter;

//import edu.unl.cse.git.App;
//import net.wagstrom.research.github.Github;



class RunStudy {
	//this class is supposed to integrate all the 4 steps involved to run the study
	
	private String gitminerConfigProps = 'gitminerConfiguration.properties'
	private String projectName
	private String projectRepo
	private String gitminerLocation
	public Study(){}
	
	public void run(String[] args){
		def projectsList = new File(args[0])
		updateGitMinerConfig(args[1])
		projectsList.eachLine {
			setProjectNameAndRepo(it)
			println "Starting project " + this.projectName
			println "Running gitminer"
			String graphBase = runGitMiner()
			println graphBase
			println "Finished running gitminer and starting to download revisions from github"
			String revisionFile = runGremlinQuery(graphBase)
			/*runSedCommands(revisionFile)
			runConflictsAnalyzer(revisionFile)*/
		}
	
	}
	
	public void updateGitMinerConfig(String configFile){
		Properties gitminerProps =  new Properties()
		File gitminerPropsFile = new File(this.gitminerConfigProps)
		gitminerProps.load(gitminerPropsFile.newDataInputStream())
		
		
		Properties configProps = new Properties()
		File propsFile = new File(configFile)
		configProps.load(propsFile.newDataInputStream())
		
		this.gitminerLocation = configProps.getProperty('gitminer.path')
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
	}
	
	
	public String runGitMiner(){
		updateProjectRepo()
		runGitminerCommand('./gitminer.sh')
		runGitminerCommand('./repository_loader.sh')
		String graphBase = renameGraph()
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
	
	public void runGremlinQuery(String graphBase){
		GremlinQueryApp gq = new GremlinQueryApp()
		gq.run(projectName, projectRepo, graphBase)
	}
	
	public void runSedCommands(String dir){
		//replace ... by []
		String c1 = "grep -rl '\\.\\.\\.' " + dir + " | xargs sed -i '' 's#\\.\\.\\.#[]#g'"
		c1.execute()
	}
	
	public void runConflictsAnalyzer(String projectData){
		ConflictsAnalyzer ca = new ConflictsAnalyzer(projectData)
		ca.analyzeConflicts()
	}
	
	
	public static void main (String[] args){
		RunStudy study = new RunStudy()
		String[] files= ['projectsList', 'configuration.properties']
		study.run(files)	
		
		
	}

}
