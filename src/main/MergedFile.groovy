package main

import de.ovgu.cide.fstgen.ast.FSTTerminal
import java.util.Observable;
import merger.MergeVisitor

class MergedFile {

	private ArrayList<Conflict> conflicts

	private ArrayList<MethodOrConstructor> methodsWithConflicts
	
	private String path

	public MergedFile(String path){
		this.path = path
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
	
	public String getPath(){
		return this.path
	}
	
	public ArrayList<Conflict> getConflicts(){
		return this.conflicts
	}
	
	public ArrayList<MethodOrConstructor> getMethodsWithConflicts(){
		return this.methodsWithConflicts
	}

}
