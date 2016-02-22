package normalization

import main.Extractor
import main.ExtractorResult
import main.GremlinProject
import main.GremlinQueryApp
import main.MergeCommit;

class NormalizedProject {
	
	String name
	
	String resultDir 
	
	int numberChangesOutsideMethods
	
	int numberChangesInsideMethods
	
	Map<String, Integer> conflictsSummary
	
	Map<String, Double> normalizedConflictSummary
	
	ArrayList<EvoScenario> evoScenarios
	
	public NormalizedProject(String n, String resultData){
		this.name = n
		this.resultDir = resultData
		this.evoScenarios = new ArrayList<EvoScenario>()
	}
	
	public void loadConflictsSummary(){
		
	}
	
	public void computeNumberOfChanges(String projectRepo, String gitMinerDir, String downloadDir){
		//get commits list
		ArrayList<MergeCommit> commits = this.runGremLinQuery(projectRepo, gitMinerDir)
		
		//create extractor
		Extractor extractor = this.createExtractor(this.name, projectRepo, downloadDir)
		
		//analyse commit scenarios
		this.analyseEvoScenarios(commits, extractor)
	}
	
	public void analyseEvoScenarios(ArrayList<MergeCommit> commits, Extractor extractor ){
		
		int current = 1; //jumps the first project commit
		int end = commits.size()
		
		//if project execution breaks, update current with next merge scenario number
		while(current < end){
			
			int index = current + 1;
			println 'Analyzing merge scenario [' + index + '] from a total of [' + end +
					'] merge scenarios\n'

			MergeCommit mc = commits.get(current)
			
			if(!mc.parent1.equals('')){
				this.analyseEvoScenario(mc, extractor)
			}
			
			current++
			
		}
	}
	
	public void analyseEvoScenario(MergeCommit scenario, Extractor extractor){
		
		
		ExtractorResult er = extractor.extractEvoScenario(scenario)
		String revisionFile = er.getRevisionFile()
		if(!revisionFile.equals("")){
			this.analyseChanges(scenario, er)
		}
	}
	
	public void analyseChanges (MergeCommit mc, ExtractorResult er){
		EvoScenario scenario = new EvoScenario(mc, er)
		this.evoScenarios.add(scenario)
		scenario.analyseChanges()
		
	}
	
	
	public ArrayList<MergeCommit> runGremLinQuery(String projectRepo, String gitMinerDir){
		ArrayList<MergeCommit> result = new ArrayList<MergeCommit>()
		String gitminerBase = gitMinerDir + File.separator + this.name + 'graph.db'
		
		println "starting to query the gremlin database and download merge revision"
		GremlinQueryApp gq = new GremlinQueryApp()
		result = gq.runAllCommits(this.name, projectRepo, gitminerBase)
		
		
		return result
	}
	
	private Extractor createExtractor(String projectName, String projectRepo, String downloadDir){
		GremlinProject gProject = new GremlinProject(this.name, projectRepo)
		Extractor extractor = new Extractor(gProject, downloadDir)
	   
	   return extractor
	}
	
	public void getCommitsList(){
		
	}
	
	public String toString(){
		
	}
}
