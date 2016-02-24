package normalization

import main.Extractor
import main.ExtractorResult
import main.GremlinProject
import main.GremlinQueryApp
import main.MergeCommit
import main.SSMergeNodes;;

class NormalizedProject {
	
	String name
	
	int numberOfChangesInsideMethodsChunks
	
	int numberOfChangesInsideMethodsLines
	
	Map<String, Integer> changesSummary
	
	ArrayList<EvoScenario> evoScenarios
	
	public NormalizedProject(String n){
		this.name = n
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
			
			println 'Analyzing merge scenario [' + current + '] from a total of [' + end +
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
		scenario.analyseChanges()
		this.evoScenarios.add(scenario)
		this.updateProjectMetrics(scenario)
		NormalizedConflictPrinter.printEvoScenarioReport(scenario, this.name)
		if(scenario.hasConflictsOnNonJavaFiles){
			NormalizedConflictPrinter.printMergeScenariosWithConflictsOnNonJavaFiles(scenario, this.name)
		}
		scenario.deleteEvoDir()
	}
	
	private void updateProjectMetrics(EvoScenario evo){
		
		//update changesSummary
		for(Map.Entry<String, Integer> changes in evo.changesSummary.entrySet()){
			String node = changes.key
			int numberOfChanges = changes.value
			if(!this.changesSummary.containsKey(node)){
				this.changesSummary.put(node, changes.value)
			}else{	
				numberOfChanges = numberOfChanges + this.changesSummary.get(changes.key)
				this.changesSummary.put(node, numberOfChanges)
			}
		}
		
		//update other metrics
		this.numberOfChangesInsideMethodsChunks = this.numberOfChangesInsideMethodsChunks +
		evo.numberOfChangesInsideMethodsChunks
		
		this.numberOfChangesInsideMethodsLines = this.numberOfChangesInsideMethodsLines +
		evo.numberOfChangesInsideMethodsLines
	}
	
	public String toString(){
		String result = this.name + ', ' + this.evoScenarios.size() + ', '
		
		//print changesSummary
		for(SSMergeNodes node in SSMergeNodes){
			if(!this.changesSummary.containsKey(node)){
				result = result + 0 + ', '	
			}else{
				result = result + this.changesSummary.get(node) + ', '
			}
		}
		
		//print other metrics
		result = result + ', ' + this.numberOfChangesInsideMethodsChunks + ', ' +
		this.numberOfChangesInsideMethodsLines
		
		return result
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

}
