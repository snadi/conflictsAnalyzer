package travisAnalysis

import main.Extractor
import main.GremlinProject

class Project {
	
	ArrayList<MergeScenario> merges
	String repo
	String name
	String mergeReport
	String mergeCommits
	String downloadPath
	String resultPath
	Extractor extractor
	
	/*this structure maps each commit to its associated builds, and
	 *  each build to its associated state and finished time*/
	Hashtable<String, Hashtable<String,ArrayList<String>>> travisAnalysis
	
	public Project(String repo, String mergeCommits, String mergeReport, String downloadPath){
		this.repo = repo
		this.setName()
		this.mergeCommits = mergeCommits
		this.mergeReport = mergeReport
		this.merges = new ArrayList<MergeScenario>()
		this.downloadPath = downloadPath
		this.cloneProject()
		
	}
	
	public void setName(){
		String[] temp = this.repo.split('/')
		this.name = temp[1]
	}
	
	public analyzeMerges(){
		println 'loading conflict predictor analysis from project ' + this.name
		Hashtable<String, ArrayList<String>> mergeCommits = this.loadMergeCommitsFile()
		/*run travis analysis*/
		println 'executing build and tests analysis from project ' + this.name
		this.runTravisAnalysis()
		
		File mergeReport = new File(this.mergeReport)
		String text = mergeReport.getText()
		String[] lines = text.split('\n')
		
		/*for each merge commit*/
		
		for(int i = 1; i < lines.length; i++){
			int totalMerges = lines.length - 1
			println 'analysing merge scenario [' + i + '] from [' + totalMerges + ']'
			String metrics = lines[i]
			String revName = metrics.split(',')[0]
			ArrayList<String> value = mergeCommits.get(revName)
			
			/*in case the merge commit was discarded due to jgit API internal problems*/
			if(value!=null){
				String parent1 = value.get(0)
				String parent2 = value.get(1)
				String sha = value.get(2)
				
				Hashtable<String, ArrayList<String>> commitBuilds = this.travisAnalysis.get(sha)
				MergeScenario merge = new MergeScenario(this.name, sha, parent1, parent2, metrics, 
					this.downloadPath, commitBuilds, this.extractor)	
				PrintBuildAndTestAnalysis.printMergeScenario(this.resultPath, merge.toString())
				
				/*if there are more than one merge commit with the same parents,
				 * remove the one collected on this iteration*/
				if(value.size > 3){
					value.remove(2)
					mergeCommits.put(revName, value)
				}
			}
		}
		
		println 'finished the analysis for project ' + this.name
	}
	
	public Hashtable<String, ArrayList<String>> loadMergeCommitsFile(){
		ArrayList<String> shas = new ArrayList<String>()
		Hashtable<String, ArrayList<String>> result = new Hashtable<String, ArrayList<String>>()
		File mergeCommits = new File(this.mergeCommits)
		String text = mergeCommits.getText()
		String[] lines = text.split('\n')
		
		/*for each merge commit*/
		for(int i = 1; i < lines.length; i++){
			String[] line = lines[i].split(',')
			String sha = line[0]
			
			/*stores all merge commits shas to use as input for travis analysis*/
			shas.add(sha)
			
			String parent1 = line[1]
			String parent2 = line[2]
			String revName = 'rev_' + parent1.substring(0,5) + '-' + parent2.substring(0,5)
			/*in case more than one merge commit has the same parents*/
			ArrayList<String> value = result.get(revName)
			if(value == null){
				value = new ArrayList<String>()
				value.add(parent1)
				value.add(parent2)
				value.add(sha)
			}else{
				value.add(sha)
			}
			result.put(revName, value)
		}
		
		this.printShas(shas)
		return result
	}
	
	public void runTravisAnalysis(){
		/*instantiate result variable*/
		this.travisAnalysis = new Hashtable<String, Hashtable<String,ArrayList<String>>>()
		
		/*run travis script*/
		boolean scriptExecuted = this.executeScript()
		
		if(scriptExecuted){
			/*read resulting csv*/
			this.readCSV()
		}else{
			println 'error while executing travis script'
		}
		
		
	}
	
	public boolean executeScript(){
		boolean result = false
		try{
			/*set command*/
			String projectClone = this.downloadPath + File.separator + this.name + File.separator +'git'
			String commitsPath = System.getProperty("user.dir") + File.separator + this.resultPath
			String command = "ruby travisBuildAnalysis.rb " + projectClone + " " + commitsPath
			
			/*run command line*/
			Process process = Runtime.getRuntime().exec(command)
			process.waitFor()

			/*read script output and verify if it was executed correctly*/
			BufferedReader processIn = new BufferedReader(new InputStreamReader(process.getInputStream()))
			String line
			while ((line = processIn.readLine()) != null) {
				println line
				if(line.contains('Finish')){
					result = true
				}
			}
		}catch(Exception e){
			e.printStackTrace()
		}

		return result
	}
	
	public void readCSV(){
		File resultFile = new File('TravisResults' + File.separator + this.name + 'BUILDS.csv')
		String text = resultFile.getText()
		String[] lines = text.split('\n')
		/*for each lines of the resulting csv*/
		for(int i = 1; i < lines.length; i++){
			String[] line = lines[i].split(',')
			String state = line[0]
			String commit = line[1]
			String buildId = line[2]
			String finished_at = line[3]
			Hashtable<String, ArrayList<String>> commitBuilds = this.travisAnalysis.get(commit)
			if(commitBuilds == null){
				commitBuilds = new Hashtable<String, ArrayList<String>>()

			}
			ArrayList<String> buildData = new ArrayList<String>()
			buildData.add(state)
			buildData.add(finished_at)
			commitBuilds.put(buildId, buildData)
			this.travisAnalysis.put(commit, commitBuilds)
			
		}
	}
	
	public void printShas(ArrayList<String> shas){
		/*make dir*/
		String dirPath = 'ResultData' + File.separator + this.name + 
		File.separator + 'buildAndTest'
		File dir = new File(dirPath)
		dir.mkdir()
		
		/*create and print file*/
		String filePath = dirPath + File.separator + 'commits.csv'
		File file = new File(filePath)
		/*deletes file if it already exists*/
		file.delete()
		file = new File(filePath)
		String commits = 'Commits\n'
		for(String sha in shas){
			commits = commits + sha + '\n'
		}
		file.append(commits)
		this.resultPath = file.getParent()
	}
	
	public void cloneProject(){
		GremlinProject project = new GremlinProject(this.name, this.repo, 'graphbase')
		extractor = new Extractor(project, this.downloadPath)
	}
	
	
	public static void main (String[] args){
		Project project = new Project ('leusonmario/javaToy', 
			'/Users/paolaaccioly/Documents/Doutorado/workspace_CASM/conflictsAnalyzer/ResultData/javatoy/mergeCommits.csv',
			 '/Users/paolaaccioly/Documents/Doutorado/workspace_CASM/conflictsAnalyzer/ResultData/javatoy/ConflictPredictor_MS_Report.csv', 
			 '/Users/paolaaccioly/Documents/Doutorado/workspace_CASM/downloads')
		project.analyzeMerges()
	}
}
