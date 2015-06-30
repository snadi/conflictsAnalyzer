package util

import org.apache.commons.io.FileUtils

class CompareFiles {

	private String leftRevName

	private String baseRevName

	private String rightRevName

	private String revDir

	private File tempDir

	public CompareFiles(String revFile){

		this.setDirNames(revFile)
	}

	private void setDirNames(String revFile){
		this.revDir = new File(revFile).getParent()
		String[] revs = new File(revFile).text.split('\n')
		this.leftRevName = revs[0]
		this.baseRevName = revs[1]
		this.rightRevName = revs[2]
		this.tempDir = new File(this.revDir + File.separator + 'temp')

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

			this.moveAndDeleteFiles(this.baseRevName, base, left, right)

		}else if((!leftEqualsBase) && rightEqualsBase){

			this.moveAndDeleteFiles(this.leftRevName, left, base, right)

		}else if(leftEqualsBase && (!rightEqualsBase)){

			this.moveAndDeleteFiles(this.rightRevName, right, base, left)

		}

	}

	private void moveAndDeleteFiles(String revName, File toBeMoved, File toBeDeleted1, File toBeDeleted2){

		String temp = toBeMoved.getAbsolutePath().replaceFirst(revName, 'temp')
		FileUtils.moveFile(toBeMoved, new File(temp))
		FileUtils.forceDelete(toBeDeleted1)
		FileUtils.forceDelete(toBeDeleted2)

	}

	public void restoreFilesWeDontMerge(){
		//TO DO
	}

	public static void main(String[] args){
		CompareFiles cp = new CompareFiles("/Users/paolaaccioly/Documents/testeConflictsAnalyzer/testes/rev/rev.revisions")
		cp.ignoreFilesWeDontMerge()
	}
}
