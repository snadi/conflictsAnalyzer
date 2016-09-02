package travisAnalysis

class BuildAndTest {
	
	public String mergeCommits
	public String conflictPredictors
	public String downloadPath
	
	public void run(){
		this.setProperties()
		File projectList = new File('projectList')
		projectList.eachLine {
			String repo = it
			
		}
	}
	
	public void setProperties(){
		
	}
	
	public static void main(){
		BuildAndTest analysis = new BuildAndTest()
		analysis.run()
	}

}
