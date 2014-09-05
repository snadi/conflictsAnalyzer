package conflictsAnalyzer

import javax.swing.colorchooser.DefaultSwatchChooserPanel.MainSwatchKeyListener;

class FileCounter {

	def countFiles(dir) {
		
		
		def count=0;
		
		dir.eachFile{
			file ->
			
			if(file.directory){
				
				count += countFiles(file)
				
			} else if(file.toString().endsWith(".java")) {
				count++
			}
			
		}
	
		
		return count
	}
	
	def countRevFiles(file){
		
		def count = 0
		
		file.eachLine{
			revPath ->
			
			def dirName = revPath.substring(0, (revPath.length() - 10))
			def dir = new File(dirName)
			count += countFiles(dir)
			
		}
		
		return count
		
	}
	


	public static void main (String[] args){

		def file = new File('/Users/paolaaccioly/gitClones/dropwizard/revisions/rev_2ac8b_cb5b7/rev_2ac8b-cb5b7')
		def fc = new FileCounter()
		def count = fc.countFiles(file)
		println count
		

	}

}
