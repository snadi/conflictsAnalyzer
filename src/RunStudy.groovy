import composer.FileLoader.MyFileFilter;

import edu.unl.cse.git.App;
import net.wagstrom.research.github.Github;



class RunStudy {
	//this class is supposed to integrate all the 4 steps involved to run the study
	
	private String gitminerConfigProps = 'gitminerConfiguration.properties'
	private String projectName
	private String projectRepo
	public Study(){}
	
	public void run(String[] args){
		def projectsList = new File(args[0])
		updateGitMinerConfig(args[1])
		projectsList.eachLine {
			setProjectNameAndRepo(it)
			String graphBase = runGitMiner()
			runGremlinQuery(graphBase)
			/*runSedCommands()
			runConflictsAnalyzer()*/
		}
	
	}
	
	public void updateGitMinerConfig(String configFile){
		Properties gitminerProps =  new Properties()
		File gitminerPropsFile = new File(this.gitminerConfigProps)
		gitminerProps.load(gitminerPropsFile.newDataInputStream())
		
		
		Properties configProps = new Properties()
		File propsFile = new File(configFile)
		configProps.load(propsFile.newDataInputStream())
		
		String gitminerPath = configProps.getProperty('gitminer.path')
		String graphDb = gitminerPath + File.separator + 'graph.db'
		String repo_Loader = gitminerPath + File.separator + 'repo_loader'
		
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
		runGithub()
		runApp()
		String graphBase = renameGraph()
		
		return graphBase
	}
	
	
	private String renameGraph(){
		Properties props = new Properties()
		File myfile = new File(this.gitminerConfigProps)
		props.load(myfile.newDataInputStream())
		String oldFile = props.getProperty('edu.unl.cse.git.dburl')
		String newFile = oldFile.substring(0, (oldFile.length() - 8) ) + this.projectName + 'graph.db'
		new File(oldFile).renameTo(new File(newFile))
		
		return newFile
	}
	
	private void runGithub(){
		Github g = new Github()
		String[] files = ['-c', this.gitminerConfigProps]
		g.run(files)
	}
	
	private void runApp(){
		App a = new App()
		String[] files = ["-c", this.gitminerConfigProps]
		a.run(files)
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
