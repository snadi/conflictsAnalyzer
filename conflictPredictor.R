deleteHTMLFile <- function(exportPath) {
  
  fileToRemove = paste(exportPath, "conflictPredictorResults.html", sep="")
  if (file.exists(fileToRemove)) {
    file.remove(fileToRemove)
  }
  
  
}

main<-function(){
  importPath = "/Users/paolaaccioly/Documents/Doutorado/workspace_CASM/conflictsAnalyzer/"
  exportPath = "/Users/paolaaccioly/Documents/Doutorado/workspace_CASM/graphs/"
  
  predictorFile="ConflictPredictor_Projects_Report.csv"
  
  mergeScenarioPredictor="mergesSummary.csv"
  
  #HTML file
  htmlFile = paste(exportPath, "conflictPredictorResults.html", sep="")
  
  #delete previous html file
  deleteHTMLFile(exportPath)
  
  #read conflictPredictorFile
  predictorTemp = read.table(file=paste(importPath, predictorFile, sep=""), header=T, sep=",")
  
  #read merge scenario predictor file
  MSpredictorTemp = read.table(file=paste(importPath, mergeScenarioPredictor, sep=""), header=T, sep=",")
  
  #conflicting editsamemc
  conflictingEditSameMC = predictorTemp$Conflicting_EditSameMC - predictorTemp$Conflicting_EditSameMC_DS
  
  #non conflicting editsamemc
  nonConflictingEditSameMC = predictorTemp$NonConflicting_EditSameMC - predictorTemp$NonConflicting_EditSameMC_DS
  
  sumEditSameMC <- conflictingEditSameMC + nonConflictingEditSameMC
  
  conflictingEditSameMC_rate <- (conflictingEditSameMC/sumEditSameMC)*100
  nonConflictingEditSameMC_rate <- (nonConflictingEditSameMC/sumEditSameMC)*100
  
  #replace nan values by zeros
  conflictingEditSameMC_rate[is.na(conflictingEditSameMC_rate)] <- 0
  nonConflictingEditSameMC_rate[is.na(nonConflictingEditSameMC_rate)] <- 0
  
  library(beanplot)
  
  #beanplot conflicting vs nonconflicting editsamemc
  dataEditSameMC <- data.frame(conflictingEditSameMC_rate, nonConflictingEditSameMC_rate)
  colnames(dataEditSameMC) <- c ("Conflicting EditSameMC", "Non conflicting EditSameMC")
  beanplotEditSameMC = paste("beanplotEditSameMC.png")
  png(paste(exportPath, beanplotEditSameMC, sep=""))
  beanplot(dataEditSameMC,col="green", cex=1.5, bw="nrd0")
  dev.off()
  
  #boxplot conflicting vs nonconflicting editsamemc
  boxplotEditSameMC = paste("boxplotEditSameMC.png")
  png(paste(exportPath, boxplotEditSameMC, sep=""))
  boxplot(dataEditSameMC,col="green", cex=1.5, bw="nrd0")
  dev.off()
  
  #create table editSameMC
  tableEditSameMC <- data.frame(predictorTemp$Project, conflictingEditSameMC_rate, nonConflictingEditSameMC_rate)
  colnames(tableEditSameMC) <- c("Project", "Conflicting_EditSameMC_Rate", "Non_Conflicting_EditSameMC_Rate")
  
  totalConflictingEditSameMC <- (sum(conflictingEditSameMC)/sum(sumEditSameMC)) *100
  totalNonConflictingEditSameMC <- (sum(nonConflictingEditSameMC)/sum(sumEditSameMC)) *100
  totalEditSameMC <- data.frame(Project="TOTAL", Conflicting_EditSameMC_Rate=totalConflictingEditSameMC,Non_Conflicting_EditSameMC_Rate=totalNonConflictingEditSameMC)
  tableEditSameMC <- rbind(tableEditSameMC,totalEditSameMC)
  
  
  #table with means and standar deviation
  ConflictingEditSameMC <- c(mean(conflictingEditSameMC_rate), sd(conflictingEditSameMC_rate))
  NonConflictingEditSameMC <- c(mean(nonConflictingEditSameMC_rate),sd(nonConflictingEditSameMC_rate))
  metrics <- data.frame(ConflictingEditSameMC, NonConflictingEditSameMC)
  row.names(metrics) <- c("Mean", "Standard_deviation")
  
  #HTML code
  library(R2HTML)
  
  title = paste("<hr><h1>Results for conflict predictor analysis</h1>", sep="")
  HTML("<link rel=stylesheet type=text/css href=R2HTML.css>", file=htmlFile, append=TRUE)
  HTML.title(title, file=htmlFile, append=TRUE)
  
  HTML("<hr><h2>Conflicting and non conflicting EditSameMC rates</h2>", file=htmlFile, append=TRUE)
  HTML(tableEditSameMC, file=htmlFile, append=TRUE)
  HTML(metrics, file=htmlFile, append=TRUE)
  
  HTML("<hr><h2>EditSameMC Rates Beanplot</h2>", file=htmlFile, append=TRUE)
  HTMLInsertGraph(file=htmlFile, GraphFileName=beanplotEditSameMC, Align="center", append=TRUE)
  HTML("<hr><h2>EditSameMC Rates Boxplot</h2>", file=htmlFile, append=TRUE)
  HTMLInsertGraph(file=htmlFile, GraphFileName=boxplotEditSameMC, Align="center", append=TRUE)
  
}

main()