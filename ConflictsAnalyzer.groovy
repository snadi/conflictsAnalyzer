package conflictsAnalyzer

import merger.FSTGenMerger

class ConflictsAnalyzer {

	private ArrayList<Project> projects;

	public ConflictsAnalyzer(){
		this.projects = new ArrayList<Project>()
	}

	public ArrayList<Project> getProjects(){
		return this.projects
	}

	public void setProjects(ArrayList<Project> p){
		this.projects = p
	}
	
	public void analyzeConflicts(args){
		
		this.createProjects(args)
		
		for(Project p : this.projects){
			
			p.analyzeConflicts()
				
		}
	}
	
	public void createProjects(args){
		def projectData = new File(args[0])
		projectData.eachLine {
			String[] p = it.split(',')
			String projectName = p[0].trim()
			String projectMergeScenarios = p[1].trim()
			Project project = new Project(projectName, projectMergeScenarios)
			this.projects = project
			
		}
	}
	
	public static void main (String[] args){
		
		ConflictsAnalyzer ca = new ConflictsAnalyzer()
		ca.analyzeConflicts(args)
		
	}
}
