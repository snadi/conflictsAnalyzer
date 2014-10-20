package conflictsAnalyzer;

import java.io.File;

import de.ovgu.cide.fstgen.ast.FSTNode;
import de.ovgu.cide.fstgen.ast.FSTTerminal;


enum SSMergeConflicts {

	ModifierList, DefaultValueAnnotation, ImplementList, LineBasedMCFd, SameIdFd, SameSignatureCM

}

public  class Conflict {

	private String type;

	private String body;

	private String filePath;

	private String nodeType;

	public Conflict(FSTTerminal node, String path){

		this.matchPattern(node);
		this.retrieveFilePath(node, path);



	}

	public Conflict(){}

	public void matchPattern(FSTTerminal node){

		String nodeType = node.getType();
		String body = node.getBody();
		String conflictType = "";

		if(nodeType.equals("Modifiers")){

			conflictType = SSMergeConflicts.ModifierList.toString();

		}else if(nodeType.equals("AnnotationMethodDecl")){

			conflictType = SSMergeConflicts.DefaultValueAnnotation.toString();

		}else if(nodeType.equals("ImplementsList")){

			conflictType = SSMergeConflicts.ImplementList.toString();

		}else if(nodeType.equals("FieldDecl") ){

			conflictType = this.setFieldDeclPattern(body);

		}

		else if(nodeType.equals("MethodDecl") || nodeType.equals("ConstructorDecl")){

			conflictType = this.setMCPattern(body);

		}


		this.setType(conflictType);

	}

	public String setFieldDeclPattern(String nodeBody){

		String type = "";
		String [] fd = nodeBody.split(ConflictsController.SSMERGE_SEPARATOR);

		if(fd[1].equals(" ")){

			type = SSMergeConflicts.SameIdFd.toString();

		}else{
			type = SSMergeConflicts.LineBasedMCFd.toString();
		}

		return type;

	}

	public String setMCPattern(String nodeBody){

		String type = "";

		String [] p1 = nodeBody.split("\\|\\|\\|\\|\\|\\|\\|");
		String [] p2 = p1[1].split("=======");
		String a = p2[0].substring(1, p2[0].length()-1);

		if(a.contains(" ")){

			type = SSMergeConflicts.LineBasedMCFd.toString();
		}else{

			type = SSMergeConflicts.SameSignatureCM.toString();

		}

		return type;

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
