package main

class MethodOrConstructor {
	
	private Conflict conflict
	
	private int numberOfConflicts
	
	public MethodOrConstructor(Conflict conflict){
		this.conflict = conflict
		this.setNumberOfConflicts()
	}
	
	private void setNumberOfConflicts(){
		String[] p = this.conflict.body.split("<<<<<<<");
		this.numberOfConflicts = p.length - 1;
		
	}
	
	public int getNumberOfConflicts(){
		return this.numberOfConflicts
	}
}
