package util

import org.apache.commons.io.FileUtils

import main.MergedFile;

class CompareFiles {

	private String leftRevName

	private String baseRevName

	private String rightRevName

	private String revDir

	private File tempDir

	private int filesEditedByOneDev

	private int filesThatRemainedTheSame

	private ArrayList<MergedFile> filesToBeMerged

	public CompareFiles(String revFile){

		this.setDirNames(revFile)
		this.filesToBeMerged = new ArrayList<MergedFile>()
	}

	private void setDirNames(String revFile){
		this.revDir = new File(revFile).getParent()
		String[] revs = new File(revFile).text.split('\n')
		this.leftRevName = revs[0]
		this.baseRevName = revs[1]
		this.rightRevName = revs[2]
		this.tempDir = new File(this.revDir + File.separator + 'temp')

	}

	public ArrayList<MergedFile> getFilesToBeMerged(){
		return this.filesToBeMerged
	}

	public void ignoreFilesWeDontMerge(){
		String baseFolder = this.revDir + File.separator + this.baseRevName
		this.iterateRevFolders(this.leftRevName, this.baseRevName, baseFolder, this.rightRevName)
	}

	private void iterateRevFolders(String leftRevName, String baseRevName, String baseFolder, String rightRevName){

		File directory = new File(baseFolder)
		if(directory.exists()){
			File[] fList = directory.listFiles()
			for (File file : fList){
				if (file.isDirectory()){
					iterateRevFolders(leftRevName, baseRevName, file.getAbsolutePath(), rightRevName)
				} else {
					String leftFilePath   = file.getAbsolutePath().replaceFirst(baseRevName, leftRevName)
					String rightFilePath  = file.getAbsolutePath().replaceFirst(baseRevName, rightRevName)
					this.compareAndMoveFiles(leftFilePath, file.getAbsolutePath() ,rightFilePath)
				}
			}
		}
	}


	private void compareAndMoveFiles(String leftFile, String baseFile, String rightFile){

		File left = new File(leftFile)
		File base = new File(baseFile)
		File right = new File(rightFile)

		if(left.exists() && base.exists() && right.exists()){
			this.compareFiles(left, base, right)
		}

	}

	private void compareFiles (File left, File base, File right){

		boolean leftEqualsBase = FileUtils.contentEquals(left, base)
		boolean rightEqualsBase = FileUtils.contentEquals(right, base)

		if(leftEqualsBase && rightEqualsBase){
			this.filesThatRemainedTheSame = this.filesThatRemainedTheSame + 1
			this.moveAndDeleteFiles(this.baseRevName, base, left, right)

		}else if((!leftEqualsBase) && rightEqualsBase){
			this.filesEditedByOneDev = this.filesEditedByOneDev + 1
			this.moveAndDeleteFiles(this.leftRevName, left, base, right)

		}else if(leftEqualsBase && (!rightEqualsBase)){
			this.filesEditedByOneDev = this.filesEditedByOneDev + 1
			this.moveAndDeleteFiles(this.rightRevName, right, base, left)

		}else if((!leftEqualsBase) && (!rightEqualsBase)){
			MergedFile mf = new MergedFile(base.getAbsolutePath())
			this.filesToBeMerged.add(mf)
		}

	}

	private void moveAndDeleteFiles(String revName, File toBeMoved, File toBeDeleted1, File toBeDeleted2){

		String temp = toBeMoved.getAbsolutePath().replaceFirst(revName, 'temp2')
		FileUtils.moveFile(toBeMoved, new File(temp))
		FileUtils.forceDelete(toBeDeleted1)
		FileUtils.forceDelete(toBeDeleted2)

	}

	public int getNumberOfTotalFiles(){

		int totalFiles = this.filesEditedByOneDev + this.filesThatRemainedTheSame + this.filesToBeMerged.size()
		return totalFiles
	}

	public void restoreFilesWeDontMerge(){
		//TO DO
	}

	public int getFilesEditedByOneDev() {
		return filesEditedByOneDev;
	}

	public int getFilesThatRemainedTheSame() {
		return filesThatRemainedTheSame;
	}

	//normalized study methods
	public void removeNonJavaFiles(){
		String dir = this.revDir + File.separator
		//remove from commit
		File file = new File(dir + this.baseRevName)
		this.auxRemoveNonJavaFiles(file)
		//remove from parent1
		file = new File(dir + leftRevName)
		this.auxRemoveNonJavaFiles(file)

		//remove from parent2 --- if it exists
		if(!this.rightRevName.contains('none')){
			file = new File(dir + rightRevName)
			this.auxRemoveNonJavaFiles(file)
		}
	}

	private void auxRemoveNonJavaFiles(File dir){
		File[] files = dir.listFiles()

		for(File file in files){

			if(file.isDirectory()){
				this.auxRemoveNonJavaFiles(file)
			}else{
				if(!file.name.endsWith('.java')){
					file.delete()
				}
			}
		}
	}

	public void removeEqualFiles(){

		String baseFolder =  this.revDir + File.separator + this.baseRevName

		if(this.rightRevName.contains('none')){
			this.auxRemoveEqualFiles(this.leftRevName, this.baseRevName, baseFolder, null)
		}else{

			this.auxRemoveEqualFiles(this.leftRevName, this.baseRevName, baseFolder, this.rightRevName)
		}

	}

	private void auxRemoveEqualFiles(String left, String commit, String folder, String right){
		File directory = new File (folder)
		if(directory.exists()){
			File[] files = directory.listFiles()
			for(File file in files){
				if(file.isDirectory()){
					auxRemoveEqualFiles(left, commit, file.getAbsolutePath(), right)
				}else{

					this.compareAndRemoveFiles(left, file, right)
				}
			}
		}
	}

	private void compareAndRemoveFiles(String left, File commitFile, String right){
		boolean leftEqualsBase, rightEqualsBase = false
		String leftFilePath  = commitFile.getAbsolutePath().replaceFirst(this.baseRevName, this.leftRevName)
		File l = new File(leftFilePath)
		if(commitFile.exists() && l.exists()){
			leftEqualsBase = FileUtils.contentEquals(l, commitFile)
		}

		if(right != null){
			String rightFilePath = commitFile.getAbsolutePath().replaceFirst(this.baseRevName, this.rightRevName)
			File r = new File(rightFilePath)
			if(commitFile.exists() && r.exists()){
				rightEqualsBase = FileUtils.contentEquals(r, commitFile)
				if(leftEqualsBase && rightEqualsBase){
					l.delete()
					r.delete()
					commitFile.delete()
				}
			}
		}else{
			if(leftEqualsBase){
				l.delete()
				commitFile.delete()
			}
		}



	}
	//normalized study methods

	public static void main(String[] args){
		CompareFiles cp = new CompareFiles("/Users/paolaaccioly/Documents/testeConflictsAnalyzer/testes/rev/rev.revisions")
		cp.ignoreFilesWeDontMerge()
	}
}
