package conflictsAnalyzer

import java.util.Observable;

import merger.FSTGenMerger;
import merger.MergeVisitor;
import composer.rules.ImplementsListMerging;
import de.ovgu.cide.fstgen.ast.FSTTerminal;


class MergeScenario implements Observer {

	private String path

	private ArrayList<Conflict> conflicts


	MergeScenario(String path){
		this.path = path
		this.conflicts = new ArrayList<Conflict>()
	}

	public void analyzeConflicts(){
		this.runFstGenMerger()
	}

	public void runFstGenMerger(){
		FSTGenMerger fstGenMerge = new FSTGenMerger()
		fstGenMerge.getMergeVisitor().addObserver(this)
		String[] files = ["--expression", this.path]
		fstGenMerge.run(files);
	}


	@Override
	public void update(Observable o, Object arg) {

		if(o instanceof MergeVisitor && arg instanceof FSTTerminal){

			FSTTerminal node = (FSTTerminal) arg

			if(!node.getType().contains("-Content")){
				this.createConflict(node)
			}
		}
	}

	public void createConflict(FSTTerminal node){
		Conflict conflict = new Conflict(node);
		this.conflicts.add(conflict)
	}

	public String getId(){
		return this.id
	}

	public void setId(String id){
		this.id = id
	}

	public ArrayList<Conflict> getConflicts(){
		return this.conflicts
	}

	public void setConflicts(ArrayList<Conflict> conflicts){
		this.conflicts = conflicts
	}
}
