package main

class MethodOrConstructor {
	
	private Conflict conflict
	
	private int numberOfConflicts
	
	public MethodOrConstructor(Conflict conflict){
		this.conflict = conflict
		this.numberOfConflicts = this.conflict.countConflictsInsideMethods()
	}
	
	public int getNumberOfConflicts(){
		return this.numberOfConflicts
	}
	
	public Conflict getConflict(){
		return this.conflict
	}
}
