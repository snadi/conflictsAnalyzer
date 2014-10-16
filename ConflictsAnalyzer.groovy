package conflictsAnalyzer

import merger.FSTGenMerger

class ConflictsAnalyzer {

	private ArrayList<Project> projects;

	private FSTGenMerger fstGenMerger

	public ConflictsAnalyzer(){

		this.projects = new ArrayList<Project>()
		this.fstGenMerger = new FSTGenMerger()

	}

	public ArrayList<Project> getProjects(){
		return this.projects
	}

	public void setProjects(ArrayList<Project> p){
		this.projects = p
	}
	
	public FSTGenMerger getFstGenMerger(){
		return this.fstGenMerger
	}
	
	public void setFstGenMerger(FSTGenMerger fstgm){
		this.fstGenMerger = fstgm
	}
}
