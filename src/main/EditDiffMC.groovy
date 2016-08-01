/**
 * @author paolaaccioly
 *
 */
package main

import java.util.ArrayList;
import java.util.HashMap
import java.util.List;
import java.util.Map;

import javax.management.InstanceOfQueryExp

import org.codehaus.groovy.antlr.ASTParserException
import org.codehaus.groovy.ast.stmt.ThrowStatement
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation
import org.eclipse.jdt.core.dom.TypeDeclaration;

import de.ovgu.cide.fstgen.ast.FSTTerminal
import merger.FSTGenMerger

class EditDiffMC extends ConflictPredictor{

	private String leftOrRight

	private ConflictPredictor predictor

	private boolean editionAddedMethodCall

	public EditDiffMC(FSTTerminal n, String msp){

		super(n, msp)
		this.editionAddedMethodCall = false
	}

	@Override
	public void setDiffSpacing(){
		this.diffSpacing = false
		this.setLeftOrRight()
		String [] nodeBodyWithoutSpacing = this.getNodeWithoutSpacing()
		if(this.leftOrRight.equals('left') && nodeBodyWithoutSpacing[0].equals(nodeBodyWithoutSpacing[1])){
			this.diffSpacing = true
		}else if(this.leftOrRight.equals('right') && nodeBodyWithoutSpacing[2].equals(nodeBodyWithoutSpacing[1])){
			this.diffSpacing = true
		}

	}

	private void setLeftOrRight(){
		String [] splitNodeBody = this.splitNodeBody().clone()
		if(splitNodeBody[0].equals(splitNodeBody[1])){
			this.leftOrRight = 'right'
		}else{
			this.leftOrRight = 'left'
		}
	}


	public boolean lookForReferencesOnConflictPredictors(Map<String, Integer> filesWithConflictPredictors){
		boolean hasReference = false

		/*for each file containing conflict predictors*/
		for(String filePath : filesWithConflictPredictors.keySet()){
			ArrayList<ConflictPredictor> predictors = filesWithConflictPredictors.get(filePath)

			/*for each conflict predictor on that file*/
			for(ConflictPredictor predictor : predictors ){
				/*check if predictor is not the same instance of this,
				 *  if it is an edited method, and if the changes come 
				 *  from different merge commit parents*/
				if((!this.referenceEquals(predictor)) &&
				(predictor instanceof EditSameMC || predictor instanceof EditDiffMC)  &&
				(this.changesComeFromDifferentCommits(predictor))){
					println 'vai buscar'
					hasReference = this.lookForReferenceOnConflictPredictor(predictor)
					println hasReference
				}
			}
		}
		return hasReference
	}

	/*check of changes on edited methods come from different merge commit parents
	 * as it does not makes sense to analyse conflicts from changes made by same commit*/
	public boolean changesComeFromDifferentCommits(ConflictPredictor predictor){
		boolean changesComeFromDifferentCommits = false

		if(predictor instanceof EditSameMC){
			changesComeFromDifferentCommits = true
		}else {
			EditDiffMC p = (EditDiffMC) predictor
			if((this.leftOrRight.equals('left') && p.leftOrRight.equals('right')) ||
			(this.leftOrRight.equals('right') && p.leftOrRight.equals('left'))){
				changesComeFromDifferentCommits = true
			}
		}

		return changesComeFromDifferentCommits
	}


	private boolean lookForReferenceOnConflictPredictor(ConflictPredictor predictor){
		boolean hasReference = false
		/*Step 1: check for potential EditDiffMC when grepping
		 * the method name inside the edited method */

		if(this.containsTextualReference(predictor)){

			/*Step 2: in case the edited method has
			 *  a textual reference, remove false positives using the
			 * method reference finder*/
			hasReference = this.checkForClassReference(predictor)
			if(hasReference){
				this.saveReference(predictor)
			}
		}
		return hasReference

	}

	/*Checks if the string representing the method body declaration
	 * contains a textual reference to this method.
	 * Might report false positives. */
	private boolean containsTextualReference(ConflictPredictor predictor){
		boolean containsTextualReference = false
		String methodBody = this.extractMethodBody(predictor.node.body)
		String thisMethodName = this.getMethodName(this)
		if(methodBody.contains(thisMethodName)){
			containsTextualReference = true
		}
		return containsTextualReference
	}

	/*Receives as input the string of the method and returns just
	 *  the lines inside the method body declaration.
	 *  It helps to remove false positives before running
	 *  the compiler analysis*/
	private String extractMethodBody(String method){
		String methodBody = ''

		ArrayList<String> temp = method.split('\n')
		int firstBracket = 0
		int lastBracket = temp.size() -1
		boolean foundFirstBracket, foundLastBracket = false
		String a = ''

		/*get the first bracket index*/
		while(!foundFirstBracket){
			a = temp.elementData(firstBracket)
			if(a.contains('{')){
				foundFirstBracket = true
			}else{
				firstBracket++
			}
		}

		/*gets the last bracket index*/
		while(!foundLastBracket){
			a = temp.elementData(lastBracket)
			if(a.contains('}')){
				foundLastBracket = true
			}else{
				lastBracket--
			}
		}

		/*gets the string representing the method body declaration*/
		String [] temp2 = temp.subList(firstBracket + 1, lastBracket)

		for(String s: temp2){
			methodBody = methodBody + s + '\n'
		}

		return methodBody
	}

	private String getMethodName(ConflictPredictor predictor){
		String methodName = ''
		String predictorName = predictor.node.name
		String[] split = predictorName.split('\\(')
		methodName = split[0]
		return methodName
	}

	private boolean checkForClassReference(ConflictPredictor predictor) throws IOException{
		boolean isTheSameMethod = false

		/*sets the predictor signature*/
		String []  temp = predictor.signature.split('\\.')
		String predictorSignature = temp[temp.length-1]

		//get file contents
		String contents = getFileContents(predictor.filePath)

		/*setting compiler environment variables*/
		/*FIXME change classPath if needed*/

		//set classPath
		String[] classPaths = null

		/*set source folders*/
		String[] source = this.fillSources(predictor)

		/*set encodings*/
		String[] encoding = this.fillEncodings(source.length)

		/*set className*/
		File filePredictor = new File(predictor.filePath)
		String classname = filePredictor.getName()

		if(contents!=null){

			// Create the ASTParser which will be a CompilationUnit
			ASTParser parser = ASTParser.newParser(AST.JLS8)
			parser.setKind(ASTParser.K_COMPILATION_UNIT)
			parser.setSource(contents.toCharArray())

			//Parsing
			parser.setEnvironment(classPaths, source, encoding, true);
			parser.setBindingsRecovery(true);
			parser.setResolveBindings(true);
			parser.setUnitName(classname);
			Map options = JavaCore.getOptions();
			options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
			parser.setCompilerOptions(options);
			CompilationUnit parse = (CompilationUnit) parser.createAST(null);

			parse.accept(new ASTVisitor() {
						private MethodDeclaration activeMethod;

						/**
				 * @param MethodDeclaration node
				 * @return true if and only if this node is the method containing this 
				 * method invocation. Returns false otherwise
				 */
						@Override
						public boolean visit(MethodDeclaration node) {
							activeMethod = node;
							IMethodBinding activeMethodBinding = activeMethod.resolveBinding()
							if(isTheMethodCallingThisMethod(predictorSignature, activeMethodBinding)){
								return super.visit(node)
							}else{
								return false
							}
						}

						/**
				 * @param MethodInvocation node
				 * @return true if and only if it is a method invocation to this method.
				 * Returns false otherwise
				 */
						@Override
						public boolean visit(MethodInvocation node) {
							IMethodBinding thisMethodBinding = node.resolveMethodBinding()
							boolean isThisMethod = methodInvocationMatchesThisMethod(thisMethodBinding)
							if(isThisMethod){
								isTheSameMethod = true
								return true

							}
							return super.visit(node)
						}
					})

		}

		return isTheSameMethod
	}

	private String[] fillSources(ConflictPredictor predictor){
		String[] sources = []
		ArrayList<String>folders = new ArrayList<String>()
		String rootPath = this.mergeScenarioPath.substring(0, this.mergeScenarioPath.length()-10)
		File rootDir = new File(rootPath)
		File [] fileList = rootDir.listFiles()
		for(File file : fileList){
			if(file.isDirectory()){
				folders.add(file.getAbsolutePath())
			}
		}

		File filePredictor = new File(predictor.filePath)
		File thisFileMethod = new File(this.filePath)

		folders.add(filePredictor.getParent())
		folders.add(thisFileMethod.getParent())
		folders.add(rootPath)
		sources = new String[folders.size()]
		for(int i = 0; i < folders.size(); i++){
			sources[i] = folders.get(i)
		}

		return sources
	}

	private String[] fillEncodings(int amount){
		String[] encodings = new String[amount]
		for(int i = 0; i<amount; i++){
			encodings[i] = 'UFT_8'
		}
		return encodings
	}

	public boolean isTheMethodCallingThisMethod(String predictorSignature, IMethodBinding activeMethod){
		boolean callsThisMethod = false
		String activeMethodSignature= this.simplifyMethodSignature(activeMethod)

		/*if active method is indeed the method calling this method*/
		if(activeMethodSignature.contains(predictorSignature)){
			callsThisMethod = true
		}
		return callsThisMethod
	}

	/**
	 * @param methodInvocation
	 * @return true if and only if the method invocation belongs to the same 
	 * class and same method of this method. Returns false otherwise
	 */
	public boolean methodInvocationMatchesThisMethod(IMethodBinding methodInvocation){
		boolean isTheSameMethod = false

		if( methodInvocation!=null && methodInvocation.getKey()!=null){
			String methodInvocationClass = this.simplifyClassName(methodInvocation)
			String methodInvocationSignature = this.simplifyMethodSignature(methodInvocation)
			String [] temp = this.filePath.split('/')
			String thisMethodClass = this.packageName + '.' + temp[temp.length-1].split('\\.')[0]
			temp = this.signature.split('\\.')
			String thisMethodSignature = temp[temp.length-1]

			/*if method invocation is indeed this method*/
			if(methodInvocationClass.contains(thisMethodClass) &&
			methodInvocationSignature.contains(thisMethodSignature)){
				isTheSameMethod = true
			}
		}

		return isTheSameMethod
	}

	private String simplifyClassName(IMethodBinding methodInvocation){
		String className = ''
		String temp = (methodInvocation.getKey().split("\\."))[0]
		className = temp.replaceAll(";", "")
		className = className.replace("/", ".")
		if(className.startsWith("L"))className = className.replaceFirst("L","")
		return className
	}
	private String simplifyMethodSignature(IMethodBinding mb) {
		String simplifiedMethodSignature = ((mb.toString()).replaceAll("(\\w)+\\.", "")).replaceAll("\\s+","");
		return simplifiedMethodSignature;
	}

	public String getFileContents(String filePath){
		String contents = ''
		File file = new File(filePath)
		file.eachLine {
			contents = contents + it + '\n'
		}
		return contents
	}

	private void saveReference(ConflictPredictor predictor){
		this.predictor = predictor
		this.setEditionAddedMethodCall()
	}

	private void setEditionAddedMethodCall(){
		//TODO
	}

	/*this method checks for objects' reference equality*/
	public boolean referenceEquals(ConflictPredictor b){
		boolean result = (this == b)
		return result
	}

	public static void main(String[] args){
		//String method ='public\n int\n sub\n (int a, int b) {\n int result = 0;\n int sub = a-b;\n if(sub>0){\n result = sub;\n }\n return result;\n}\n//comment1\n//comment2'
		//println method

	}
}
