package travisAnalysis

class CSVReviewer {

	/*compute total by predictor and total*/
	public static void computeTotalBuildAnalysis(String resultData){
		Hashtable<String, ArrayList<Integer>> predictors = fillPredictors()
		File folder = new File(resultData)
		File[] listOfFiles = folder.listFiles()
		for(File file in listOfFiles){
			if(file.isDirectory()){
				String projectReport = file.absolutePath + File.separator + 'buildAndTest' +
						File.separator + 'ProjectReport.csv'
				println file.getName()
				computeProjectResults(projectReport, predictors)

			}
		}
		printResults(predictors)
		
	}
	
	public static void printResults(Hashtable<String, ArrayList<Integer>> predictors){
		String header = 'predictor,total_merge_scenario,merge_scenarios,conflict_predictor,' +
		 'parents_build_passed,build_passed,test_passed\n'
		File file = new File('build_results.csv')
		file.delete()
		file = new File('build_results.csv')
		file.append(header)
		
		for(String predictor in predictors.keySet()){
			String p = predictor + auxPrintResults(predictors.get(predictor)) +'\n'
			file.append(p)
		}
	}
	
	public static String auxPrintResults(ArrayList<Integer> values){
		String result = ''
		for(Integer i in values){
			result = result + ','  + i 
		}
		return result
	}
	
	public static Hashtable<String, ArrayList<Integer>> computeProjectResults(String projectReport,
			Hashtable<String, ArrayList<Integer>> predictors){

		File file = new File(projectReport)
		String text = file.getText()
		String[] lines = text.split('\n')
		
		/*for each line in the project report*/
		for(int i = 1; i < lines.length; i++){
			String[] data = lines[i].split(',')
			String predictor = data[0]
			ArrayList<Integer> values = predictors.get(predictor)
			ArrayList<Integer> newValues = new ArrayList<Integer>()
			
			//get total merge scenarios
			newValues.add(values.get(0) + Integer.parseInt(data[1]))
			//get number of merge scenarios
			newValues.add(values.get(1) + Integer.parseInt(data[2]))
			//get number of predictors
			newValues.add(values.get(2) + Integer.parseInt(data[3]))
			//get number of parents_build_passed
			newValues.add(values.get(3) + Integer.parseInt(data[4]))
			//get number of build_passed
			newValues.add(values.get(4) + Integer.parseInt(data[5]))
			//get number of test_passed
			newValues.add(values.get(5) + Integer.parseInt(data[6]))
			predictors.put(predictor,newValues)
		}

	}
	public static Hashtable<String, ArrayList<Integer>> fillPredictors(){
		Hashtable<String, String> predictors = new Hashtable<String, String>()
		ArrayList<Integer> values = new ArrayList<Integer>()
		for(int i = 0; i < 6; i++){
			values.add(0)
		}
		predictors.put('ncEditSameMC', values)
		predictors.put('ncEditSameFd', values)
		predictors.put('editDiffMC', values)
		predictors.put('editDiffEditSame', values)
		predictors.put('editDiffAddsCall', values)
		predictors.put('editDiffEditSameAddsCall', values)
		
		return predictors
	}
	

	public static void main(String[] args){
		CSVReviewer.computeTotalBuildAnalysis('/Users/paolaaccioly/Desktop/conflictPredictorINES/ResultData')
	}
}
