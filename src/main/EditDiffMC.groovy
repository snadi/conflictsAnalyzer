package main

import javax.management.InstanceOfQueryExp;

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
		for(String filePath : filesWithConflictPredictors.keySet()){
			ArrayList<ConflictPredictor> predictors = filesWithConflictPredictors.get(filePath)

			for(ConflictPredictor predictor : predictors ){
				/*check if predictor is not the same instance of this,
				 *  and if it is an edited method*/
				if((!predictor.diffSpacing) && (!this.equals(predictor)) &&
				(predictor instanceof EditSameMC || predictor instanceof EditDiffMC)){
				
					hasReference = this.lookForReferenceOnConflictPredictor(predictor)
				}
			}
		}
		return hasReference
	}
	
	
	
	private boolean lookForReferenceOnConflictPredictor(ConflictPredictor predictor){
		boolean hasReference = false
		/*Step 1: check for potential EditDiffMC when grepping
		 * the method name inside the edited method */
		String thisMethodName = this.getMethodName()
		if(predictor.node.body.contains(thisMethodName)){
			
			/*Step 2: in case the edited method has
			 *  a textual reference, remove false positives using the
			 * reference finder*/
			hasReference = this.checkClassReference(predictor)
			if(hasReference){
				this.saveReference(predictor)
			}
		}
		return hasReference
		
	}
	
	private String getMethodName(){
		String methodName = ''
		String[] split = this.node.name.split('(')
		methodName = split[0]
		return methodName
	}
	
	private boolean checkClassReference(ConflictPredictor predictor){
		boolean isTheSameMethod = false
		//TO DO
		return isTheSameMethod
	}
	
	private void saveReference(ConflictPredictor predictor){
		this.predictor = predictor
		this.setEditionAddedMethodCall()
	}
	
	private void setEditionAddedMethodCall(){
		
	}
	
	public static void main(String[] args){
		
	}
}
