package util

import main.ConflictPrinter;
import main.Project

class CsvAnalyzer {
	private ArrayList<Project> projects



	public CsvAnalyzer(){
		this.projects = new ArrayList<Project>()
	}

	public void updateProjectPatternData(){
		File resultDir = new File('ResultData')
		File[] projects = resultDir.listFiles()
		
		for(File projectDir in projects){
			
			if(projectDir.isDirectory()){
				
				Project p = this.loadProjectData(projectDir)
				this.projects.add(p)
				ConflictPrinter.printAnalizedProjectsReport(this.projects)
				println 'Finished analysis of project' + projectDir.name
			}
		}
		
	}

	private Project loadProjectData(File projectDir){
		
		String projectName = projectDir.name
		String conflictReportPath = projectDir.absolutePath + File.separator + 'ConflictsReport.csv'
		File conflictReport = new File(conflictReportPath)
		
	}



	public static void main(String[] args){
		CsvAnalyzer ca = new CsvAnalyzer()
		ca.updateProjectPatternData()
	}

}
