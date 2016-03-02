package nonJavaFilesAnalysis

import main.Extractor;
import main.GremlinProject
import main.MergeCommit;

class NonJavaFilesAnalysis {

	String resultData
	String downloads
	String projectsList
	Map<String, ArrayList<String>> projectsSummary

	public NonJavaFilesAnalysis(String projectslist, String resultData, String downloads){
		this.projectsList = projectslist
		this.resultData = resultData
		this.downloads = downloads
		this.projectsSummary = new HashMap<String, ArrayList<String>>()
	}

	public void analyseNonJavaFiles(){
		File list = new File(this.projectsList)
		list.eachLine {
			String projectRepo = it.trim()
			String projectName = this.getProjectname(projectRepo)
			this.analyseProject(projectName, projectRepo)
		}
	}

	public String getProjectname(String repo){
		String[] projectData = repo.split('/')
		String result = projectData[1].trim()

		return result
	}

	public void analyseProject(String name, String repo){

		//read merge commit file
		ArrayList<MergeCommit> mergeCommits = this.readMergeCommitsFile(name)
		int start = 0
		int end = mergeCommits.size()
		
		//initialize extractor
		Extractor extractor = this.createExtractor(name, repo)
		while(start<end){
			MergeCommit mc = mergeCommits.getAt(start)
			
			start++
		}
	}

	public ArrayList<MergeCommit> readMergeCommitsFile(String name){
		ArrayList<MergeCommit> result = new ArrayList<MergeCommit>()
		String filePath = this.resultData + File.separator + name + File.separator + 'mergeCommits.csv'
		File mergeCommitsFile = new File(filePath)
		if(mergeCommitsFile.exists()){
			mergeCommitsFile.eachLine {
				if(!it.startsWith('Merge')){
					MergeCommit mc = this.readMergeCommit(it.trim())
					result.add(mc)
				}
			}
		}

		return result
	}

	public MergeCommit readMergeCommit(String mc){
		MergeCommit result = new MergeCommit()
		String [] tokens = mc.split(',')
		result.sha = tokens[0].trim()
		result.parent1 = tokens[1].trim()
		result.parent2 = tokens[2].trim()
		return result
	}
	
	private Extractor createExtractor(String projectName, String projectRepo){
		GremlinProject gProject = new GremlinProject(projectName, projectRepo)
		Extractor extractor = new Extractor(gProject, this.downloads)

		return extractor
	}

	public static void main(String[] args){
		NonJavaFilesAnalysis n = new NonJavaFilesAnalysis('projectsList', '/Users/paolaaccioly/Documents/testeConflictsAnalyzer/conflictsAnalyzer/ResultData',
				'/Users/paolaaccioly/Documents/testeConflictsAnalyzer/downloads')
		n.analyseNonJavaFiles()

	}

}
