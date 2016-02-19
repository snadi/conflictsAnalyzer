package normalization


class NormalizationStudy {
	
	ArrayList<NormalizedProject> projects
	
	
	public NormalizationStudy(){
		this.projects = new ArrayList<NormalizedProject>()
	}
	
	public void normalizeData(String pl, String resultData, String gitMinerDir, String downloadDir){
		File projectList = new File (pl)
		
		//for each project
		projectList.eachLine {
			String projectRepo = it
			String projectName = this.getProjectName(it)
			NormalizedProject p = new NormalizedProject(projectName, resultData)
			
			//read conflict report
			p.loadConflictsSummary()
			
			//compute number of Changes
			p.computeNumberOfChanges(projectRepo, gitMinerDir, downloadDir)
			
			//print results
			NormalizedConflictPrinter.printNormalizedProjectData(p)
			
			//TODO run r script
			
		}
	}
	
	
	public String getProjectName(String projectRepo){
		String[] projectData = projectRepo.split('/')
		String result = projectData[1].trim()
		return result
	}
	
	public static void main (String[] args){
		NormalizationStudy normalization = new NormalizationStudy()
		normalization.normalizeData('projectsList', 'ResultData', 
			'/Users/paolaaccioly/Documents/Doutorado/workspace_fse/gitminer', 
			'/Users/paolaaccioly/Documents/Doutorado/workspace_fse/downloads')
	} 
}
