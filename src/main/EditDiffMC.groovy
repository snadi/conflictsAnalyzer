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
import org.eclipse.jdt.core.dom.MethodInvocation;

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

					hasReference = this.lookForReferenceOnConflictPredictor(predictor)
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
		String thisMethodName = this.getMethodName(this)
		if(predictor.node.body.contains(thisMethodName)){
			
			/*Step 2: in case the edited method has
			 *  a textual reference, remove false positives using the
			 * reference finder*/
			hasReference = this.checkForClassReference(predictor)
			if(hasReference){
				this.saveReference(predictor)
			}
		}
		return hasReference

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

		/*get method names*/
		String thisMethod = this.getMethodName(this)
		String methodCallingThisMethod = this.getMethodName(predictor)
		//get file contents
		String contents = getFileContents(this.filePath)

		if(contents!=null){

			// Create the ASTParser which will be a CompilationUnit
			ASTParser parser = ASTParser.newParser(AST.JLS8)
			parser.setKind(ASTParser.K_COMPILATION_UNIT)
			parser.setSource(contents.toCharArray())

			//FIXME change classPath, source, and encoding if needed

			//set classPath
			String[] classPaths = null

			/*set source (file containing this edited method*/
			String[] source = [predictor.filePath]

			/*set encodings*/
			String[] encoding = ["UFT_8"]

			/*set className*/
			String[] split = this.filePath.split('/')
			String temp = split[split.length-1]
			String classname = temp.split('\\.')[0]

			//Parsing
			parser.setEnvironment(classPaths, source, encoding, true);
			parser.setBindingsRecovery(true);
			parser.setResolveBindings(true);
			parser.setUnitName(classname);
			Map options = JavaCore.getOptions();
			options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
			parser.setCompilerOptions(options);
			CompilationUnit parse = (CompilationUnit) parser.createAST(null);

			/*create a table listing all methods invocations regarding one method declaration*/
			HashMap<MethodDeclaration, ArrayList<MethodInvocation>> invocationsForMethods =
					new HashMap<MethodDeclaration, ArrayList<MethodInvocation>>()
			ArrayList<String> listOfInvocations = new ArrayList<String>();
			//look for references of this method
			parse.accept(new ASTVisitor() {
						private MethodDeclaration activeMethod

						@Override
						public boolean visit(MethodDeclaration node) {
							activeMethod = node
							return super.visit(node)
						}

						@Override
						public boolean visit(MethodInvocation node) {
							if (invocationsForMethods.get(activeMethod) == null) {
								invocationsForMethods.put(activeMethod, new ArrayList<MethodInvocation>());
							}
							invocationsForMethods.get(activeMethod).add(node)

							IMethodBinding mb = node.resolveMethodBinding()
							if(mb!=null){
								if(mb.getKey()!=null){
									if(!listOfInvocations.contains(mb.getKey())){
										listOfInvocations.add(mb.getKey())
									}

									//SE CONTIVER UMA REFERÊNCIA, INFORME O MÉTODO QUE REFERENCIOU.
									String namespace = (mb.getKey().split("\\."))[0]
									//if(namespace.contains(className)){
									if(simplifyMethodSignature(mb).contains(renamedMethod)){
										//TODO
									}
									//}

									System.out.println(mb.toString())
								}
							}
							return super.visit(node)
						}

					});

		}

		return isTheSameMethod
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

	}
}
