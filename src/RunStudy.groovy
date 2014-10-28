

class RunStudy {
	//this class is supposed to integrate all the 4 steps involved to run the study
	public Study(){}
	
	public void run(String[] args){
		
		def projectsList = new File(args[0])
		projectsList.eachLine {
			runGitMiner(it)
			runGremlinQuery()
			runSedCommands()
			runConflictsAnalyzer()
		}
		
		
	}
	
	public void runGitMiner(String project){
		String[] projectData = project.split(',')
		String projectName = projectData[0].trim()
		String projectRepo = projectData[1].trim()
		updateConfigurationFile()
	}
	
	private void updateConfigurationFile(){
		
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
		study.run(args)
	}

}
