package main;


import java.io.File;

import de.ovgu.cide.fstgen.ast.FSTNode;
import de.ovgu.cide.fstgen.ast.FSTTerminal;
import merger.FSTGenMerger;


enum SSMergeConflicts {

	ModifierList, DefaultValueAnnotation, ImplementList, EditSameMC, AddSameFd, EditSameFd, SameSignatureCM, NOPATTERN

}

public  class Conflict {

	public static final String SSMERGE_SEPARATOR = "##FSTMerge##";

	public static final String DIFF3MERGE_SEPARATOR = "|||||||";

	private String type;

	private String body;

	private String filePath;

	private String nodeType;
	
	private boolean isFalsePositive;
	

	public Conflict(FSTTerminal node, String path){
		this.body = node.getBody();
		this.nodeType = node.getType();
		this.matchPattern();
		this.retrieveFilePath(node, path);
		this.setIsFalsePositive();
	}
	
	public void setIsFalsePositive(){
		if(this.type.equals(SSMergeConflicts.EditSameMC.toString()) || 
				this.type.equals(SSMergeConflicts.EditSameFd.toString())){
			this.isFalsePositive = this.checkFalsePositives();
			
		}else{
			this.isFalsePositive = false;
		}
	}
	
	public boolean checkFalsePositives(){
		boolean isFP = false;
		String [] splitConflictBody = this.splitConflictBody();
		isFP = this.checkDifferentSpacing(splitConflictBody);
		if(!isFP){
			isFP = this.checkConsecutiveLines(splitConflictBody);
		}
		return isFP;
	}
	
	public boolean checkDifferentSpacing(String [] splitConflictBody){
		boolean result = false;
		
		return result;
	}
	
	public boolean checkConsecutiveLines(String [] splitConflictBody){
		boolean result = false;
		
		return result;
	}
	
	public String [] splitConflictBody(){
		String [] splitBody = {"", "", ""};
		if(this.isMethodOrConstructor()){
			String temp = this.body.substring(13);
			String [] temp2 = temp.split("\\|\\|\\|\\|\\|\\|\\| base\n");
			String left = temp2[0];
			temp2 = temp2[1].split("======= right\n");
			String base = temp2[0];
			String right = temp2[1].replaceFirst(">>>>>>>", "");
			splitBody[0] = left.trim();
			splitBody[1] = base.trim();
			splitBody[2] = right.trim();
		}else{
			String[] tokens = body.split(FSTGenMerger.MERGE_SEPARATOR);
			splitBody[0] = tokens[0].replace(FSTGenMerger.SEMANTIC_MERGE_MARKER, "").trim();
			splitBody[1] = tokens[1].trim();
			splitBody[2] = tokens[2].trim();
			
		}
		
		return splitBody;
	}
	
	public boolean getIsFalsePositive(){
		return this.isFalsePositive;
	}
	
	public void matchPattern(){

		String nodeType = this.nodeType;
		String conflictType = "";

		if(nodeType.equals("Modifiers")){

			conflictType = SSMergeConflicts.ModifierList.toString();

		}else if(nodeType.equals("AnnotationMethodDecl")){

			conflictType = SSMergeConflicts.DefaultValueAnnotation.toString();

		}else if(nodeType.equals("ImplementsList")){

			conflictType = SSMergeConflicts.ImplementList.toString();

		}else if(nodeType.equals("FieldDecl") ){

			conflictType = this.setFieldDeclPattern();

		}

		else if(isMethodOrConstructor()){

			conflictType = this.setMethodPattern();

		}

		if (conflictType.equals("")){
			conflictType = SSMergeConflicts.NOPATTERN.toString();
		}
		
		this.setType(conflictType);

	}

	public boolean isMethodOrConstructor(){
		boolean result = nodeType.equals("MethodDecl") || nodeType.equals("ConstructorDecl");	
		return result;
	}

	public String setFieldDeclPattern(){

		String type = "";
		String [] fd = this.body.split(Conflict.SSMERGE_SEPARATOR);

		if(fd[1].equals(" ")){

			type = SSMergeConflicts.AddSameFd.toString();

		}else{
			type = SSMergeConflicts.EditSameFd.toString();
		}

		return type;

	}

	public String setMethodPattern(){

		String type = "";

		if(isInsideMethod()){
			type = SSMergeConflicts.EditSameMC.toString();
		}else{
			type = matchConflictOutsideMethod();
		}

		return type;

	}

	private String matchConflictOutsideMethod() {
		String type;
		String [] p1 = this.body.split("\\|\\|\\|\\|\\|\\|\\|");
		String [] p2 = p1[1].split("=======");
		String a = p2[0].substring(1, p2[0].length()-1);

		if(a.contains(" ")){

			type = SSMergeConflicts.EditSameMC.toString();
		}else{

			type = SSMergeConflicts.SameSignatureCM.toString();

		}
		return type;
	}

	public boolean isInsideMethod(){
		boolean isInsideMethod = false;

		String [] p1 = this.body.split("<<<<<<<");
		if(!p1[0].equals("")){
			isInsideMethod = true;
		}

		return isInsideMethod;
	}

	public void retrieveFilePath(FSTNode node, String path){

		int endIndex = path.length() - 10;
		String systemDir = path.substring(0, endIndex);

		this.filePath = systemDir + this.retrieveFolderPath(node);
	}

	public String retrieveFolderPath(FSTNode node){
		String filePath = "";
		String nodetype = node.getType();

		if(nodetype.equals("Java-File") || nodetype.equals("Folder")){

			filePath = this.retrieveFolderPath(node.getParent()) + File.separator + node.getName();

			return filePath;

		}else if(nodetype.equals("Feature")){

			return "";

		}else{

			return this.retrieveFolderPath(node.getParent());
		}




	}

	public int countConflictsInsideMethods(){
		String[] p = this.body.split("<<<<<<<");
		int result = p.length - 1;
		return result;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getNodeType() {
		return nodeType;
	}

	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}

}
