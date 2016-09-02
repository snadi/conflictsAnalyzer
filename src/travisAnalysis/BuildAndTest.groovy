package travisAnalysis

class BuildAndTest {
	
	public String mergeCommits
	public String conflictPredictors
	public String downloadPath
	
	public void run(){
		this.setProperties()
		File projectList = new File('projectList')
		projectList.eachLine {
			/*set parameters to instantiate a project object*/
			String repo = it
			String name = repo.split('/')[1]
			String merge = this.mergeCommits + File.separator + name +
			File.separator + 'mergeCommits.csv'
			String conflictPredictor = this.conflictPredictors + File.separator +
			name + File.separator + 'ConflictPredictor_MS_Report.csv'
			
			/*instantiate a project object and call its merge scenarios analysis*/
			Project project = new Project(repo, merge, conflictPredictor, this.downloadPath)
			project.analyzeMerges()
			
			/*print projects report*/
			PrintBuildAndTestAnalysis.printProjectSummary(project.computeProjectSummary())
		}
	}
	
	public void setProperties(){
		Properties configProps = new Properties()
		File propsFile = new File('travisAnalysis.properties')
		configProps.load(propsFile.newDataInputStream())

		this.mergeCommits = configProps.getProperty('mergeCommits.path')
		this.conflictPredictors = configProps.getProperty('conflictPredictors.path')
		this.downloadPath = configProps.getProperty('download.path')
	}
	
	public static void main(){
		BuildAndTest analysis = new BuildAndTest()
		analysis.run()
	}

}
