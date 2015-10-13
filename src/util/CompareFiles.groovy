package util

import java.io.File;

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
	
	private HashMap<String, Integer> extensions

	public CompareFiles(String revFile){

		this.setDirNames(revFile)
		this.filesToBeMerged = new ArrayList<MergedFile>()
		this.setExtensions()
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
		//this.moveNonJavaFiles()
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
	
	public void moveNonJavaFiles(File dir){
		
				File[] files = dir.listFiles()
		
				for(int i = 0; i < files.length; i++){
		
					if(files[i].isFile()){
		
						String filePath = files[i].getAbsolutePath()
		
						if(!(filePath.endsWith(".java"))){
		
							if(files[i].delete()){
								//println(files[i].getName() + " is deleted!");
							}else{
								println(files[i].getName() + " delete operation has failed.");
							}
						}
		
					} else if (files[i].isDirectory()){
		
						this.moveNonJavaFiles(files[i])
					}
		
				}
			}
	
	private void auxMoveNonJavaFiles(){
		
	}
	
	public void setExtensions(){
		this.extensions = new HashMap<String, Integer>()
		//set left
		File dir = new File(this.revDir + File.separator + this.leftRevName)
		this.auxSetExtensions(dir)
		
		//set base
		dir = new File(this.revDir + File.separator + this.baseRevName)
		this.auxSetExtensions(dir)
		
		//set right
		dir = new File(this.revDir + File.separator + this.rightRevName)
		this.auxSetExtensions(dir)
	}
	
	private void auxSetExtensions(File dir){
		File[] files = dir.listFiles()
		
		for(File file : files){
			if(file.isDirectory()){
				this.auxSetExtensions(file)
			}else{
				String extension = this.getExtension(file.getAbsolutePath())
				this.extensions.put(extension, new Integer(1))
			}
		}
	}
	
	private String getExtension(String file){
		String extension = ''
		String [] parts = file.split('/')
		String filename = parts[parts.length - 1]
		if(filename.contains('.')){
			parts = filename.split('\\.')
			extension = parts[parts.length - 1]
			
		}else{
			extension = filename
		}
		
		return extension
	}
	
	public Hashtable<String, Integer> getExtensions(){
		return this.extensions
	}
	public static void main(String[] args){
		CompareFiles cp = new CompareFiles("/Users/paolaaccioly/Documents/testeConflictsAnalyzer/testes/rev/rev.revisions")
		cp.ignoreFilesWeDontMerge()
	}
}
