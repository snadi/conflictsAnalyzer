package normalization


class NormalizationStudy {
	
	ArrayList<NormalizedProject> projects
	
	
	public NormalizationStudy(){
		this.projects = new ArrayList<NormalizedProject>()
	}
	
	public void normalizeData(String pl, String resultData){
		File projectList = new File (pl)
		
		//for each project
		projectList.eachLine {
			String projectName = it
			NormalizedProject p = new NormalizedProject(projectName, resultData)
			
			//read conflict report
			p.loadConflictsSummary()
			
			//compute number of Changes
			p.computeNumberOfChanges()
			
			//print results
			NormalizedConflictPrinter.printNormalizedProjectData(p)
			
			//TODO run r script
			
		}
	}
	
	public void computeNumberOfChanges(){
		
	}
	
	public static void main (String[] args){
		NormalizationStudy normalization = new NormalizationStudy()
		normalization.normalizeData('projectsList', 'ResultData')
	} 
}
