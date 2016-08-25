package travisAnalysis

class Project {
	
	ArrayList<MergeScenario> merges
	String repo
	String name
	String mergeReport
	String mergeCommits
	
	public Project(String repo, String mergeCommits, String mergeReport){
		this.repo = repo
		this.setName()
		this.mergeCommits = mergeCommits
		this.mergeReport = mergeReport
		this.merges = new ArrayList<MergeScenario>()
		
	}
	
	public void setName(){
		String temp = this.repo.split('/')
		this.name = temp[1]
	}
	
	public analyzeMerges(){
		Hashtable<String, ArrayList<String>> mergeCommits = this.loadMergeCommitsFile()
		File mergeReport = new File(this.mergeReport)
		String text = mergeReport.getText()
		String[] lines = text.split('\n')
		
		/*for each merge commit*/
		for(int i = 1; i < lines.length; i++){
			String metrics = lines[i]
			String revName = metrics.split(',')[0]
			ArrayList<String> value = mergeCommits.get(revName)
			
			/*in case the merge commit was discarded due to jgit API internal problems*/
			if(value!=null){
				String parent1 = value.get(0)
				String parent2 = value.get(1)
				String sha = value.get(2)
				MergeScenario merge = new MergeScenario(sha, parent1, parent2, metrics)
				this.merges.add(merge)
				/*if there are more than one merge commit with the same parents,
				 * remove the one collected on this iteration*/
				if(value.size > 3){
					value.remove(2)
					mergeCommits.put(revName, value)
				}
			}
		}
	}
	
	public Hashtable<String, ArrayList<String>> loadMergeCommitsFile(){
		Hashtable<String, ArrayList<String>> result = new Hashtable<String, ArrayList<String>>()
		File mergeCommits = new File(this.mergeCommits)
		String text = mergeCommits.getText()
		String[] lines = text.split('\n')
		
		/*for each merge commit*/
		for(int i = 1; i < lines.length; i++){
			String[] line = lines[i].split(',')
			String sha = line[0]
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
		return result
	}
	public void cloneProject(){
		
	}
	
	public static void main (String[] args){
		Project project = new Project ('jitsi/jitsi', '/Users/paolaaccioly/Desktop/Teste/mergeCommits/mergeCommits.csv',
			 '/Users/paolaaccioly/Desktop/Teste/mergeCommits/ConflictPredictor_MS_Report.csv')
		project.analyzeMerges()
	}
}
