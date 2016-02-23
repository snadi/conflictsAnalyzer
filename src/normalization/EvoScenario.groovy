package normalization

import de.ovgu.cide.fstgen.ast.FSTTerminal
import java.util.Observable

import main.ExtractorResult;
import main.MergeCommit
import merger.FSTGenMerger;
import merger.MergeVisitor
import util.CompareFiles;

class EvoScenario implements Observer{

	String name

	ExtractorResult extractorResult

	boolean isMergeCommit

	//node type, number of changes
	Map <String, Integer> changesSummary

	int numberOfChangesInsideMethodsChunks

	int numberOfChangesInsideMethodsLines

	private FSTGenMerger fstGenMerge

	public EvoScenario(MergeCommit mc, ExtractorResult er){
		this.setIsMergeCommit(mc)
		this.extractorResult = er
		this.setName()
		this.preProcessFiles()
		this.changesSummary = new HashMap<String, Integer>()
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
		int value = this.changesSummary.get(nodeType)
		this.changesSummary.put(nodeType, value++)
		
	}
	
	private void computeChangesInsideMC(String newFile, String oldFile){
		//run unix diff and compute changes considering chunks and lines
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
	}

	public static void main (String[] args){
		MergeCommit mc = new MergeCommit()
		mc.sha = 'd6a2526b5420125ba543282720d7036340e2c7e0'
		mc.parent1 = '80b2502eff13fb63e8e875dbbe5356ef306940e7'
		mc.parent2 = '2fe0652b877b2bca7dabd9416d007c1c6dd87043'
		ExtractorResult er = new ExtractorResult()
		er.revisionFile = '/Users/paolaaccioly/Documents/Doutorado/workspace_fse/downloads/TGM/revisions/rev_d6a25/rev_80b25-2fe06.revisions'
		EvoScenario evo = new EvoScenario(mc, er)
	}
}
