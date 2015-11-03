package main
import java.util.HashMap;
import java.util.Hashtable

import util.CSVAnalyzer;
import org.apache.commons.io.FileUtils

/*this class is supposed to integrate all the 3 steps involved to run the study
 * gitminer/gremlinQuery/ConflictsAnalyzer
 */

class RunStudy {


	private String gitminerConfigProps = 'gitminerConfiguration.properties'
	private String projectName
	private String projectRepo
	private String gitminerLocation
	private String downloadPath
	private Hashtable<String, Conflict> projectsSummary

	public RunStudy(){
		ConflictPrinter.setconflictReportHeader()
	}

	public void run(String[] args){
		def projectsList = new File(args[0])
		updateGitMinerConfig(args[1])
		List<String> lines = projectsList.readLines()
		lines.remove(0)
		//for each project
		lines.each() {
			//run gitminer
			String[] projectInfo = it.split(",")
			setProjectNameAndRepo(projectInfo[0])
			Date startDate = null
			Date endDate = null
			String binPath = "/bin"
			String srcPath = "/src"
			if(projectInfo.length > 1 && !projectInfo[1].trim().equals(""))
			{
				startDate = Date.parse('dd/MM/yyyy', projectInfo[1])
			}

			if(projectInfo.length > 2 && !projectInfo[2].trim().equals(""))
			{
				endDate = Date.parse('dd/MM/yyyy', projectInfo[2])
			}
			if(projectInfo.length > 3 && !projectInfo[3].trim().equals("")){
				binPath = projectInfo[3].trim()
			}

			if(projectInfo.length > 4 && !projectInfo[4].trim().equals("")){
				srcPath = projectInfo[4].trim()
			}
			//attention, if you have already download gitminer base you can comment
			//the line below and use the second line below
			//String graphBase = runGitMiner()
			String graphBase = this.gitminerLocation + File.separator + this.projectName + 'graph.db'

			//get list of merge commits
			ArrayList<MergeCommit> listMergeCommits = runGremlinQuery(graphBase)

			//create project and extractor
			Extractor extractor = this.createExtractor(this.projectName, graphBase)
			Project project = new Project(this.projectName,startDate, endDate, binPath, srcPath)

			//for each merge scenario, clone and run SSMerge on it
			analyseMergeScenario(listMergeCommits, extractor, project)

			//print project report and call R script
			ConflictPrinter.printProjectData(project)
			//this.callRScript()
		}

	}

	private void analyseMergeScenario(ArrayList listMergeCommits, Extractor extractor,
			Project project) {

		//if project execution breaks, update current with next merge scenario number
		int current = 0;
		int end = listMergeCommits.size()
		Date startDate = project.getStartDate()
		Date finalDate = project.getEndDate()
		//for each merge scenario analyze it
		while(current < end){

			int index = current + 1;
			println 'Merge scenario [' + index + '] from a total of [' + end +
					'] merge scenarios\n'

			MergeCommit mc = listMergeCommits.get(current)

			if((startDate == null || mc.date.clearTime() >= startDate) && (finalDate == null || mc.date.clearTime() <= finalDate))
			{
				println 'Analyzing merge scenario...'

				/*download left, right, and base revisions, performs the merge and saves in a
				 separate file*/
				ExtractorResult mergeResult = extractor.extractCommit(mc)

				String revisionFile = mergeResult.getRevisionFile()

				if(!revisionFile.equals("")){

					//run ssmerge and conflict analysis
					SSMergeResult ssMergeResult = runConflictsAnalyzer(project, revisionFile,
							mergeResult.getNonJavaFilesWithConflict().isEmpty())

					boolean hasConflicts = ssMergeResult.getHasConflicts()
					println hasConflicts

					if(!hasConflicts){
						//get line of the files containing methods for joana analysis
						Map<String, ArrayList<MethodEditedByBothRevs>> filesWithMethodsToJoana = ssMergeResult.getFilesWithMethodsToJoana()
						if(filesWithMethodsToJoana.size() > 0)
						{
							println index + ", " + filesWithMethodsToJoana.keySet()
							String revPath = revisionFile.replace(".revisions", "")
							String revGitPath = revPath + File.separator + "git"
							File revGitFile = new File(revGitPath)

							def repoDir = new File(downloadPath +File.separator+ projectName + File.separator + "git")
							FileUtils.copyDirectory(new File(revPath), revGitFile)
							copyGitFiles(repoDir, repoDir, revGitFile)
							if(build(revGitPath))
							{
								//Call joana analysis
								println "Calling Joana..."
							}
						}
					}
				}
			}
			//increment current
			current++

		}

	}

	private def copyGitFiles(File baseDir, File srcDir, File destDir)
	{
		String basePath = baseDir.getAbsolutePath()
		String destPath = destDir.getAbsolutePath()
		File[] srcFiles = srcDir.listFiles()
		for(File file : srcFiles)
		{
			if(file.getName().contains(".git"))
			{
				if(file.isFile())
				{
					FileUtils.copyFile(file, new File(file.getAbsolutePath().replace(basePath, destPath)))
				}else if(file.isDirectory())
				{
					FileUtils.copyDirectory(file, new File(file.getAbsolutePath().replace(basePath, destPath)))
				}
			}
		}
	}

	private boolean build(String revGitPath) {
		println "Building..."
		def gradlewPath = revGitPath + File.separator+"gradlew"
		ProcessBuilder builder = new ProcessBuilder("/bin/bash","-c","chmod +x "+gradlewPath + " && "+gradlewPath+" build -p"+revGitPath);
		builder.redirectErrorStream(true);
		Process p = builder.start();
		BufferedReader buffer 	= new BufferedReader(new InputStreamReader(p.getInputStream()));
		String currentLine 		= "";
		def buildLines = new String[3];
		while ((currentLine=buffer.readLine())!=null) {
			buildLines[0] = buildLines[1];
			buildLines[1] = buildLines[2];
			buildLines[2] = currentLine;
			println currentLine
		}
		return buildLines[0].equals("BUILD SUCCESSFUL")
	}

	private Extractor createExtractor(String projectName, String graphBase){
		GremlinProject gProject = new GremlinProject(this.projectName,
				this.projectRepo, graphBase)
		Extractor extractor = new Extractor(gProject, this.downloadPath)

		return extractor
	}

	public Hashtable<String, Conflict> getProjectsSummary(){
		return this.projectsSummary
	}

	public void updateGitMinerConfig(String configFile){
		Properties gitminerProps =  new Properties()
		File gitminerPropsFile = new File(this.gitminerConfigProps)
		gitminerProps.load(gitminerPropsFile.newDataInputStream())


		Properties configProps = new Properties()
		File propsFile = new File(configFile)
		configProps.load(propsFile.newDataInputStream())

		this.gitminerLocation = configProps.getProperty('gitminer.path')
		this.downloadPath = configProps.getProperty('downloads.path')
		String graphDb = this.gitminerLocation + File.separator + 'graph.db'
		String repo_Loader = this.gitminerLocation + File.separator + 'repo_loader'

		gitminerProps.setProperty('net.wagstrom.research.github.dburl', graphDb)
		gitminerProps.setProperty('edu.unl.cse.git.localStore', repo_Loader)
		gitminerProps.setProperty('edu.unl.cse.git.repositories', graphDb)
		gitminerProps.setProperty('edu.unl.cse.git.dburl', graphDb)

		gitminerProps.setProperty('net.wagstrom.research.github.login', configProps.getProperty('github.login'))
		gitminerProps.setProperty('net.wagstrom.research.github.password', configProps.getProperty('github.password'))
		gitminerProps.setProperty('net.wagstrom.research.github.email', configProps.getProperty('github.email'))
		gitminerProps.setProperty('net.wagstrom.research.github.token', configProps.getProperty('github.token'))

		gitminerProps.store(gitminerPropsFile.newWriter(), null)

	}

	public void setProjectNameAndRepo(String project){
		String[] projectData = project.split('/')
		this.projectName = projectData[1].trim()
		this.projectRepo = project
		println "Starting project " + this.projectName
	}


	public String runGitMiner(){
		updateProjectRepo()
		println "Running gitminer"
		runGitminerCommand('./gitminer.sh')
		runGitminerCommand('./repository_loader.sh')
		String graphBase = renameGraph()
		println "Finished running gitminer"
		return graphBase
	}


	private String renameGraph(){

		String oldFile = this.gitminerLocation + File.separator + 'graph.db'
		String newFile = this.gitminerLocation + File.separator + this.projectName + 'graph.db'
		new File(oldFile).renameTo(new File(newFile))

		return newFile
	}

	public void runGitminerCommand(String command){
		String propsFile = new File("").getAbsolutePath() + File.separator + this.gitminerConfigProps
		ProcessBuilder pb = new ProcessBuilder(command, "-c", propsFile)
		pb.directory(new File(this.gitminerLocation))
		pb.redirectOutput(ProcessBuilder.Redirect.INHERIT)
		// Start the process.
		try {
			Process p = pb.start()
			p.waitFor()
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	private void updateProjectRepo(){
		Properties gitminerProps = new Properties()
		File gitminerPropsFile = new File(this.gitminerConfigProps)
		gitminerProps.load(gitminerPropsFile.newDataInputStream())
		gitminerProps.setProperty('net.wagstrom.research.github.projects', this.projectRepo)
		gitminerProps.setProperty('edu.unl.cse.git.repositories', this.projectRepo)
		gitminerProps.store(gitminerPropsFile.newWriter(), null)
	}

	public ArrayList<MergeCommit> runGremlinQuery(String graphBase){
		println "starting to query the gremlin database and download merge revision"
		GremlinQueryApp gq = new GremlinQueryApp()
		ArrayList<MergeCommit> listMergeCommits = gq.run(projectName, projectRepo, graphBase)
		return listMergeCommits
	}

	public SSMergeResult runConflictsAnalyzer(Project project, String revisionFile, boolean resultGitMerge){
		println "starting to run the conflicts analyzer on revision " + revisionFile
		SSMergeResult result = project.analyzeConflicts(revisionFile, resultGitMerge)
		return result
	}

	public void callRScript(){

		CSVAnalyzer.writeRealConflictsCSV()
		String propsFile = "resultsScript.r"
		ProcessBuilder pb = new ProcessBuilder("Rscript", propsFile)
		pb.redirectOutput(ProcessBuilder.Redirect.INHERIT)
		// Start the process.
		try {
			Process p = pb.start()
			p.waitFor()
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void main (String[] args){
		RunStudy study = new RunStudy()
		String[] files= ['projectsList', 'configuration.properties']
		study.run(files)
	}

}
