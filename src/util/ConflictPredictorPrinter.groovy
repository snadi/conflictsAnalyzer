package util

import main.ConflictPredictor;
import main.MergeScenario
import main.Project

class ConflictPredictorPrinter {
	
	public static String msSeparator = 'XXX'
	
	public static String conflictPredictorSeparator = '==='

	public static void printProjectReport(Project project){
		String header = 'Project,Conflicting_EditSameMC,Conflicting_EditSameMC_DS,' +
				'NonConflicting_EditSameMC,NonConflicting_EditSameMC_DS,Conflicting_EditSameFD,' +
				'Conflicting_EditSameFD_DS,NonConflicting_EditSameFD,NonConflicting_EditSameFD_DS,' +
				'EditDiffMC,EditDifffMC_EditSameMC,EditDiffMC_EditionAddsMethodInvocation,' +
				'EditDiffMC_EditionAddsMethodInvocation_EditSameMC\n'

		File file = new File('ResultData' + File.separator + project.name + File.separator +
				'ConflictPredictor_Projects_Report.csv')
		
		if(!file.exists()){
			file.append(header)
		}

		file.append(project.getProjectCSSummary()+ '\n')
	}

	public static void updateProjectData(Project project){
		String header = 'Project,Conflicting_EditSameMC,Conflicting_EditSameMC_DS,' +
				'NonConflicting_EditSameMC,NonConflicting_EditSameMC_DS,Conflicting_EditSameFD,' +
				'Conflicting_EditSameFD_DS,NonConflicting_EditSameFD,NonConflicting_EditSameFD_DS,' +
				'EditDiffMC,EditDifffMC_EditSameMC,EditDiffMC_EditionAddsMethodInvocation,' +
				'EditDiffMC_EditionAddsMethodInvocation_EditSameMC\n'

		File file = new File('ResultData' + File.separator + project.name + File.separator +
				'ConflictPredictor_Project_Report.csv')
		file.delete

		file = new File('ResultData' + File.separator + project.name + File.separator +
				'ConflictPredictor_Project_Report.csv')

		file.append(project.getProjectCSSummary()+ '\n')

	}

	public static void printMergeScenarioReport(Project project, MergeScenario ms){
		String header = 'Merge_Scenario,Conflicting_EditSameMC,Conflicting_EditSameMC_DS,' +
				'NonConflicting_EditSameMC,NonConflicting_EditSameMC_DS,Conflicting_EditSameFD,' +
				'Conflicting_EditSameFD_DS,NonConflicting_EditSameFD,NonConflicting_EditSameFD_DS,' +
				'EditDiffMC,EditDifffMC_EditSameMC,EditDiffMC_EditionAddsMethodInvocation,' +
				'EditDiffMC_EditionAddsMethodInvocation_EditSameMC\n'

		File file = new File('ResultData' + File.separator + project.name + File.separator +
				'ConflictPredictor_MS_Report.csv')

		if(!file.exists()){
			file.append(header)
		}else{
			String msReport = ms.computeMSSummary() + '\n'
		}

		ConflictPredictorPrinter.updateProjectData(project)
		ConflictPredictorPrinter.printConflictPredictors(project.name, ms)
	}

	public static void printConflictPredictors(String projectName, MergeScenario ms){
		
		File file = new File('ResultData' + File.separator + projectName + File.separator +
				'ConflictPredictor_Report.csv')
		file.append(ConflictPredictorPrinter.msSeparator + '\n')
		file.append(ms.name + '\n')
		file.append(ConflictPredictorPrinter.conflictPredictorSeparator + '\n')
		
		for(String filePath : ms.filesWithConflictPredictors.keySet()){
			ArrayList<ConflictPredictor> predictors = ms.filesWithConflictPredictors.get(filePath)
			for(ConflictPredictor predictor : predictors){
				file.append(predictor.toString() + '\n')
			}
		}
		
		file.append(ConflictPredictorPrinter.conflictPredictorSeparator  + '\n')
		file.append(ConflictPredictorPrinter.msSeparator + '\n')
	}
}
