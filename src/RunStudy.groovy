import edu.unl.cse.git.App;
import net.wagstrom.research.github.Github;



class RunStudy {
	//this class is supposed to integrate all the 4 steps involved to run the study
	
	private String configurationProperties = 'configuration.properties'
	public Study(){}
	
	public void run(String projectData){
		
		def projectsList = new File(projectData)
		projectsList.eachLine {
			runGitMiner(it)
			/*runGremlinQuery()
			runSedCommands()
			runConflictsAnalyzer()*/
		}
		
		
	}
	
	public void runGitMiner(String project){
		String[] projectData = project.split('/')
		String projectName = projectData[1].trim()
		String projectRepo = project
		updateConfigurationFile(projectRepo)
		runGithub()
		runApp()
	}
	
	private void runGithub(){
		Github g = new Github()
		String[] files = ['-c', this.configurationProperties]
		g.run(files)
	}
	
	private void runApp(){
		App a = new App()
		String[] files = ["-c", this.configurationProperties]
		a.run(files)
	}
	
	private void updateConfigurationFile(String projectRepo){
		
		def myFile = new File(this.configurationProperties)
		def fileText = myFile.text
		String replace1 = 'projects=' + projectRepo
		String replace2 = 'git.repositories=' + projectRepo
		def regex1 = /projects=.*/
		def regex2 = /git.repositories=.*/
	
		fileText = fileText.replaceAll(regex1, replace1)
		fileText = fileText.replaceAll(regex2, replace2)
		
		myFile.write(fileText)
	}
	
	private void runGithubJava(){}
	
	private void runAppJava(){}
	
	public void runGremlinQuery(){
		
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
		study.run('projects.data')
	}

}
