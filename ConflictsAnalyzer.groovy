package conflictsAnalyzer

import merger.FSTGenMerger

class ConflictsAnalyzer {

	private ArrayList<Project> projects;

	public ConflictsAnalyzer(String projectData){
		this.createProjects(projectData)
	}

	public ArrayList<Project> getProjects(){
		return this.projects
	}

	public void setProjects(ArrayList<Project> p){
		this.projects = p
	}
	
	public void analyzeConflicts(String projectData){
		
		for(Project p : this.projects){
			
			p.analyzeConflicts()
				
		}
	}
	
	public void createProjects(String pData){
		this.projects = new ArrayList<Project>()
		def projectData = new File(pData)
		projectData.eachLine {
			String[] p = it.split(',')
			String projectName = p[0].trim()
			String projectMergeScenarios = p[1].trim()
			Project project = new Project(projectName, projectMergeScenarios)
			this.projects.add(project)
			
		}
	}
	
	public static void main (String[] args){
		
		ConflictsAnalyzer ca = new ConflictsAnalyzer('/Users/paolaaccioly/Desktop/teste.csv')
		ca.analyzeConflicts()
		
	}
}
