package normalization

import de.ovgu.cide.fstgen.ast.FSTTerminal

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Observable

import main.ExtractorResult;
import main.MergeCommit
import main.SSMergeNodes;
import merger.FSTGenMerger;
import merger.MergeVisitor
import util.CompareFiles;

class EvoScenario implements Observer{

	String name

	ExtractorResult extractorResult

	boolean isMergeCommit
	
	boolean hasConflictsOnNonJavaFiles

	//node type, number of changes
	Map <String, Integer> changesSummary

	int numberOfChangesInsideMethodsChunks

	int numberOfChangesInsideMethodsLines

	private FSTGenMerger fstGenMerge

	public EvoScenario(MergeCommit mc, ExtractorResult er){
		this.setIsMergeCommit(mc)
		this.extractorResult = er
		this.setName()
		this.setHasConflictsOnNonJavaFiles()
		this.preProcessFiles()
		this.changesSummary = new HashMap<String, Integer>()
	}
	
	public void setHasConflictsOnNonJavaFiles(){
		if(this.extractorResult.nonJavaFilesWithConflict.size > 0){
			this.hasConflictsOnNonJavaFiles = true
		}else{
			this.hasConflictsOnNonJavaFiles = false
		}
	}

	public void setName(){
		String [] temp = this.extractorResult.revisionFile.split('/')
		String revFile = temp[temp.length -1]
		this.name = revFile.substring(0, revFile.length()-10)
	}

	public void preProcessFiles(){
		CompareFiles cp = new CompareFiles(this.extractorResult.revisionFile)
		cp.removeNonJavaFiles()
		cp.removeEqualFiles()
	}


	public void setIsMergeCommit(MergeCommit mc){
		if(!mc.parent2.equals('')){
			this.isMergeCommit = true
		}else{
			this.isMergeCommit = false
		}
	}


	@Override
	public void update(Observable o, Object arg) {
		if(o instanceof MergeVisitor && arg instanceof FSTTerminal){
			this.computeChanges(arg)
		}
	}

	public void computeChanges(FSTTerminal node){
		String [] tokens = this.getBody(node)
		String nodeType = node.getType()

		this.compareTwoNodes(tokens[0], tokens[1], nodeType)

		if(this.isMergeCommit){
			this.compareTwoNodes(tokens[2], tokens[1], nodeType)
		}
	}

	private void compareTwoNodes(String newFile, String oldFile, String nodeType){
		if(!newFile.equals(oldFile)){
			this.updateChangesSummary(nodeType)

			if(this.isMethodOrConstructor(nodeType)){
				this.computeChangesInsideMC(newFile, oldFile)
			}
		}
	}

	private void updateChangesSummary(String nodeType){
		if(!this.changesSummary.containsKey(nodeType)){
			this.changesSummary.put(nodeType, 1)
		}else{
			int value = this.changesSummary.get(nodeType)
			this.changesSummary.put(nodeType, value++)
		}


	}

	public void computeChangesInsideMC(String newFile, String oldFile){
		//if base is empty count as one change
		if(oldFile.equals('')){
			this.numberOfChangesInsideMethodsChunks++
			this.numberOfChangesInsideMethodsLines = this.numberOfChangesInsideMethodsLines +
					newFile.split('\n').length
		}else{
			//run unix diff and compute changes considering chunks and lines
			File oldF = new File('old')
			oldF.write(oldFile)
			File newF = new File('new')
			newF.write(newFile)

			String diffCmd = 'diff -u ' + newF.getAbsolutePath() + ' ' + oldF.getAbsolutePath()
			Runtime run = Runtime.getRuntime()
			Process pr = run.exec(diffCmd)
			String result = pr.text
			this.readUnixDiffResult(result)
			oldF.delete()
			newF.delete()

		}
	}

	private void readUnixDiffResult(String result){
		String [] lines = result.split('\n')
		boolean startedChunk = false
		for(String line in lines){
			if(line.startsWith('+ ') || line.startsWith('- ') || line.startsWith('! ')){
				this.numberOfChangesInsideMethodsLines++
				startedChunk = true
			}else{
				if(startedChunk){
					startedChunk = false
					this.numberOfChangesInsideMethodsChunks++
				}
			}

		}
	}


	public boolean isMethodOrConstructor(String nodeType){
		boolean result = nodeType.equals("MethodDecl") || nodeType.equals("ConstructorDecl");
		return result;
	}

	private String[] getBody(FSTTerminal node){
		String body = node.getBody() + " "
		String[] tokens = body.split(FSTGenMerger.MERGE_SEPARATOR);

		try {
			tokens[0] = tokens[0].replace(FSTGenMerger.SEMANTIC_MERGE_MARKER, "").trim();
			tokens[1] = tokens[1].trim();
			tokens[2] = tokens[2].trim();
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("|"+body+"|");
			e.printStackTrace();
		}

		return tokens
	}

	public void analyseChanges (){
		this.fstGenMerge = new FSTGenMerger()
		fstGenMerge.getMergeVisitor().addObserver(this)
		String[] files = ["--expression", this.extractorResult.revisionFile]
		fstGenMerge.run(files)
		println 'Finished analyzing scenario ' + this.name
	}

	public void deleteEvoDir(){
		int sub = 0
		if(this.isMergeCommit){
			sub = 26
		}else{
			sub = 25
		}
		String msPath = this.extractorResult.revisionFile.substring(0, (this.extractorResult.revisionFile.length()-sub))
		File dir = new File(msPath)
		boolean deleted = dir.deleteDir()
		if(deleted){
			println 'Evo scenario ' + msPath + ' deleted!'
		}else{

			println 'Evo scenario ' + msPath + ' not deleted!'
		}
	}

	public String toString(){
		String result = this.name + ', '
		
		//print changes considering nodes
		for(SSMergeNodes node in SSMergeNodes.values()){
			if(!this.changesSummary.containsKey(node)){
				result = result + 0 + ', '
			}else{
				result = result + this.changesSummary.get(node) + ', '
			}
		}
		
		//print other metrics
		
		result = result + ', ' + this.numberOfChangesInsideMethodsChunks + ', ' +
		this.numberOfChangesInsideMethodsLines
		
		return result
	}
	
	

	public static void main (String[] args){
		MergeCommit mc = new MergeCommit()
		mc.sha = 'd6a2526b5420125ba543282720d7036340e2c7e0'
		mc.parent1 = '80b2502eff13fb63e8e875dbbe5356ef306940e7'
		mc.parent2 = ''
		ExtractorResult er = new ExtractorResult()
		er.revisionFile = '/Users/paolaaccioly/Documents/Doutorado/workspace_fse/downloads/TGM/revisions/rev_d6a25/rev_80b25-2fe06.revisions'
		EvoScenario evo = new EvoScenario(mc, er)
		String newFile = new File('/Users/paolaaccioly/Desktop/left.txt').getText()
		String oldFile = new File('/Users/paolaaccioly/Desktop/base.txt').getText()
		evo.compareTwoNodes(newFile, oldFile, 'MethodDecl')
		println() 'hello'

		/*String diffCmd = 'diff -u /Users/paolaaccioly/Desktop/left.txt /Users/paolaaccioly/Desktop/base.txt'
		 Runtime run = Runtime.getRuntime()
		 Process pr = run.exec(diffCmd)
		 String result = pr.getText()
		 println result*/
	}
}
