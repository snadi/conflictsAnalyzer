package main

class FileWithConflicts {

	ArrayList<Conflict> conflicts

	ArrayList<MethodOrConstructor> methodsWithConflicts

	public FileWithConflicts(){
		this.conflicts = new ArrayList<Conflict>()
		this.methodsWithConflicts = new ArrayList<MethodOrConstructor>()
	}

	public int getNumberOfConflicts(){

		int numberOfConflicts = this.conflicts.size() + this.countConflictsInsideMethods()

		return numberOfConflicts
	}

	private int countConflictsInsideMethods(){
		int number = 0
		for(MethodOrConstructor m in this.methodsWithConflicts){
			number = number + m.getNumberOfConflicts()
		}
		return number
	}

}
