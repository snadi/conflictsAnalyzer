package main

import java.util.List

import com.ibm.wala.shrikeBT.info.ThisAssignmentChecker;

import java.io.File


import de.ovgu.cide.fstgen.ast.FSTNode
import de.ovgu.cide.fstgen.ast.FSTNonTerminal;
import de.ovgu.cide.fstgen.ast.FSTTerminal
import merger.FSTGenMerger;
import util.Util
class MethodEditedByBothRevs {

	private String signature

	private ArrayList<Integer> leftLines

	private ArrayList<Integer> rightLines

	private FSTTerminal node

	private String filePath

	public String START_SEPARATOR

	public String END_SEPARATOR
	
	private String packageName
	
	private FSTTerminal constructor
	
	private List<String> imports
	
	private boolean diffSpacing
	
	public MethodEditedByBothRevs(FSTTerminal n, String path){
		this.node = n
		this.setDiffSpacing()
		this.callBlame()
		this.packageName = ''
		this.imports = new ArrayList<String>()
		this.setSeparatorStrings()
		this.retrieveFilePath(this.node, path)
		this.setSignature()
		this.annotateMethod()
		this.leftLines  = new ArrayList<Integer>()
		this.rightLines = new ArrayList<Integer>()
	}
	
	private void callBlame(){
		if(!this.diffSpacing){
			File[] files = this.createTempFiles()
			Blame blame = new Blame()
			String result = blame.annotateBlame(files[0], files[1], files[2])
			this.node.setBody(result)
			this.deleteTempFiles(files)
		}
	}
	
	private void deleteTempFiles(File[] files){
		File tmpDir = new File(files[0].getParent())
		files[0].delete();
		files[1].delete();
		files[2].delete();
		tmpDir.delete();
		
	}
	private File[] createTempFiles(){
		String [] splitNodeBody = this.splitNodeBody()
		long time = System.currentTimeMillis()
		File tmpDir = new File(System.getProperty("user.dir") + File.separator + "fstmerge_tmp"+time);
		tmpDir.mkdir()
		File fileVar1 = File.createTempFile("fstmerge_var1_", "", tmpDir)
		File fileBase = File.createTempFile("fstmerge_base_", "", tmpDir)
		File fileVar2 = File.createTempFile("fstmerge_var2_", "", tmpDir)
		fileVar1.append(splitNodeBody[0])
		fileBase.append(splitNodeBody[1])
		fileVar2.append(splitNodeBody[2])
		File[] result = [fileVar1, fileBase, fileVar2]
		return result
	}
	private void setDiffSpacing(){
		String [] splitNodeBody = this.splitNodeBody().clone()
		String [] nodeBodyWithoutSpacing = this.removeInvisibleChars(splitNodeBody)
		if(nodeBodyWithoutSpacing[0].equals(nodeBodyWithoutSpacing[1]) || 
			nodeBodyWithoutSpacing[2].equals(nodeBodyWithoutSpacing[1])){
			this.diffSpacing = true
		}else{
			this.diffSpacing = false
		}
	}
	
	private String[] splitNodeBody(){
		String [] splitBody = ['', '', '']
		String[] tokens = this.node.getBody().split(FSTGenMerger.MERGE_SEPARATOR)
		splitBody[0] = tokens[0].replace(FSTGenMerger.SEMANTIC_MERGE_MARKER, "").trim()
		splitBody[1] = tokens[1].trim()
		splitBody[2] = tokens[2].trim()
		
		return splitBody
	}
	
	public String[] removeInvisibleChars(String[] input){
		input[0] = input[0].replaceAll("\\s+","")
		input[1] = input[1].replaceAll("\\s+","")
		input[2] = input[2].replaceAll("\\s+","")
		return input;
	}
	
	public void setSignature(){
		String [] tokens = this.filePath.split(File.separator)
		String className = tokens[tokens.length-1]
		className = className.substring(0, className.length()-5)
		String methodName = Util.simplifyMethodSignature(this.node.getName())
		String returnType;
		if(this.node.getType().equals("ConstructorDecl"))
		{
			returnType = "void"
		} else {
			returnType = Util.getMethodReturnType(this.node.getBody(), imports, packageName, new File(filePath).getParent())
		}
		this.signature = returnType + " " +this.packageName + '.' + className + '.' + Util.includeFullArgsTypes(methodName, imports, packageName, new File(filePath).getParent())
	}
	
	public void retrieveFilePath(FSTNode n, String path){

		int endIndex = path.length() - 10;
		String systemDir = path.substring(0, endIndex);

		this.filePath = systemDir + this.retrieveFolderPath(n);
	}

	private void annotateMethod(){
		String body = this.node.getBody()
		String [] lines = body.split('\n')
		String firstLine = this.START_SEPARATOR + lines[0]
		String lastLine = this.END_SEPARATOR + lines[lines.length-1]
		String newBody = firstLine + '\n'
		for(int i = 1; i < (lines.length-1); i++){
			newBody = newBody + lines[i] + '\n'
		}
		newBody = newBody + lastLine

		this.node.setBody(newBody)
	}

	public String retrieveFolderPath(FSTNode n){
		String filePath = "";
		String nodetype = n.getType();
		
		if(nodetype.equals("CompilationUnit")){
			this.setPackageName(n)
			this.setImportList(n)
			
		}
		
		if(nodetype.equals("ClassDeclaration")){
			this.setConstructor(n)
			
		}
		

		if(nodetype.equals("Java-File") || nodetype.equals("Folder")){

			filePath = this.retrieveFolderPath(n.getParent()) + File.separator + n.getName();

			return filePath;

		}else if(nodetype.equals("Feature")){

			return "";

		}else{

			return this.retrieveFolderPath(n.getParent());
		}
	}
	
	private setImportList(FSTNode node){
		boolean foundPackage = false
		FSTNonTerminal nonterminal = (FSTNonTerminal) node;
		ArrayList<FSTNode> children = nonterminal.getChildren()
		int i = 0

		while(i < children.size()){
			FSTNode child = children.elementData(i)
			if(child.getType().equals('ImportDeclaration')){
				imports.add(child.getBody().replace("import ", "").replace(";", ""))
			}
			i++
		}
	}
	
	private setPackageName(FSTNode node){
		boolean foundPackage = false
		FSTNonTerminal nonterminal = (FSTNonTerminal) node;
		ArrayList<FSTNode> children = nonterminal.getChildren()
		int i = 0
		
		while(!foundPackage && i < children.size()){
			FSTNode child = children.elementData(i)
			if(child.getType().equals('PackageDeclaration')){
				String [] tokens = child.getBody().split(' ')
				this.packageName = tokens[1].substring(0, tokens[1].length()-1)
				foundPackage = true
			}
			i++
		}
		
	}

	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	public ArrayList<Integer> getLeftLines() {
		return leftLines;
	}
	public void setLeftLines(ArrayList<Integer> leftLines) {
		this.leftLines = leftLines;
	}
	public ArrayList<Integer> getRightLines() {
		return rightLines;
	}
	public void setRightLines(ArrayList<Integer> rightLines) {
		this.rightLines = rightLines;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	public List<String> getImportsList()
	{
		imports
	}
	
	public FSTTerminal getConstructor() {
		return constructor;
	}

	public void setConstructor(FSTNode node) {
		boolean foundConstructor = false
		FSTNonTerminal nonterminal = (FSTNonTerminal) node;
		ArrayList<FSTNode> children = nonterminal.getChildren()
		int i = 0
		FSTNode privateConst = null

		while(!foundConstructor && i < children.size()){
			FSTNode child = children.elementData(i)
			if(child.getType().equals('ConstructorDecl')){
				
				FSTTerminal childTerm = (FSTTerminal) child;
				List<String> modifiersList = Util.getModifiersList(childTerm.getBody())
				if(!Util.isPrivateMethod(modifiersList))
				{
					this.constructor = child
					foundConstructor = true
				}else
				{
					privateConst = child
				}
			}
			i++
		}
		if(!foundConstructor)
		{
			this.constructor = privateConst
		}
	}

	public void assignLeftAndRight(){
		File file = new File(this.filePath)
		int i = 1;
		String newFile = ''

		boolean isMethodBody = false

		file.eachLine {

			if(it.contains(this.START_SEPARATOR)){
				isMethodBody = true
				String s = it.replace(this.START_SEPARATOR, '')
				newFile = newFile + this.processMethodLine(s, i) + '\n'
			}else{
			
				if(!isMethodBody){

					newFile = newFile + it + '\n'

				}else{

					if(it.contains(this.END_SEPARATOR)){
						isMethodBody = false
						String s2 = it.replace(this.END_SEPARATOR, '')
						newFile = newFile + this.processMethodLine(s2,i) + '\n'
					}else{
						newFile = newFile + this.processMethodLine(it, i) + '\n'
					}

				}
			}



			i++
		}

		//delete old file and write new content
		file.delete()
		new File(this.filePath).write(newFile)
	}


	private String processMethodLine(String line, int i){
		String result = ''
		if(line.contains(Blame.LEFT_SEPARATOR)){
			this.leftLines.add(new Integer(i))
			result = line.replace(Blame.LEFT_SEPARATOR, '')
		}else if(line.contains(Blame.RIGHT_SEPARATOR)){
			this.rightLines.add(new Integer(i))
			result = line.replace(Blame.RIGHT_SEPARATOR, '')
		}else{
			result = line
		}
		return result
	}
	
	private String[] linesToString(){
		String left = '['
		String right = '['
		for(Integer i : this.leftLines){
			left = left + i + ','
		}
		for(Integer x : this.rightLines){
			right = right + x + ','
		}
		left = left + ']'
		right = right + ']'
		String[] result = [left,right]
		
		return result
	}
	
	public void setSeparatorStrings(){
		this.START_SEPARATOR = '// START ' + this.node.getName() + '//'
		this.END_SEPARATOR = '// END ' + this.node.getName() + '//'
	}

}
