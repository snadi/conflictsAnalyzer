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
  
  ############# EditSameMC analysis cases  ############# 
  
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
  
  
  #table with means and standard deviation
  ConflictingEditSameMC <- c(mean(conflictingEditSameMC_rate), sd(conflictingEditSameMC_rate))
  NonConflictingEditSameMC <- c(mean(nonConflictingEditSameMC_rate),sd(nonConflictingEditSameMC_rate))
  metrics <- data.frame(ConflictingEditSameMC, NonConflictingEditSameMC)
  row.names(metrics) <- c("Mean", "Standard_deviation")
  
  ############# EditSameMC analysis cases  ############# 
  
  ############# EditSameMC analysis per merge scenarios  ############# 
  sum_MC_EditSameMC <- MSpredictorTemp$Merges_With_EditSameMC + MSpredictorTemp$Merges_With_NC_EditSameMC
  mc_EditSameMC_rate <- (MSpredictorTemp$Merges_With_EditSameMC/sum_MC_EditSameMC)*100
  mc_NC_EditSameMC_rate <- (MSpredictorTemp$Merges_With_NC_EditSameMC/sum_MC_EditSameMC)*100
  #replace nan values by zeros
  mc_EditSameMC_rate[is.na(mc_EditSameMC_rate)] <- 0
  mc_NC_EditSameMC_rate[is.na(mc_NC_EditSameMC_rate)] <- 0
  
  #beanplot mc editsamemc
  data_mc_EditSameMC <- data.frame(mc_EditSameMC_rate,mc_NC_EditSameMC_rate)
  colnames(data_mc_EditSameMC) <- c("Merges_with_EditSameMC", "Merge_with_NC_EditSameMC")
  beanplotMergesEditSameMC = paste("beanplotMergesEditSameMC.png")
  png(paste(exportPath, beanplotMergesEditSameMC, sep=""))
  beanplot(data_mc_EditSameMC,col="green", cex=1.5, bw="nrd0")
  dev.off()
  
  #boxplot merges conflicting vs nonconflicting editsamemc
  boxplotMergesEditSameMC = paste("boxplotMergesEditSameMC.png")
  png(paste(exportPath, boxplotMergesEditSameMC, sep=""))
  boxplot(data_mc_EditSameMC,col="green", cex=1.5, bw="nrd0")
  dev.off()
  
  #table with means and standard deviation
  ConflictingMergesEditSameMC <- c(mean(mc_EditSameMC_rate), sd(mc_EditSameMC_rate))
  NonConflictingMergesEditSameMC <- c(mean(mc_NC_EditSameMC_rate),sd(mc_NC_EditSameMC_rate))
  merge_metrics <- data.frame(ConflictingMergesEditSameMC, NonConflictingMergesEditSameMC)
  row.names(merge_metrics) <- c("Mean", "Standard_deviation")
  
  #table summarizing merges
  total_merges_EditSameMC <- sum(MSpredictorTemp$Merges_With_EditSameMC) + sum(MSpredictorTemp$Merges_With_NC_EditSameMC)
  merges_with_EditSameMC <- (sum(MSpredictorTemp$Merges_With_EditSameMC)/total_merges_EditSameMC)*100
  merges_with_NC_EditSameMC <- (sum(MSpredictorTemp$Merges_With_NC_EditSameMC)/total_merges_EditSameMC)*100
  numbers <- c(total_merges_EditSameMC,sum(MSpredictorTemp$Merges_With_EditSameMC), 
               sum(MSpredictorTemp$Merges_With_NC_EditSameMC))
  rates <- c(100, merges_with_EditSameMC, merges_with_NC_EditSameMC)
  summary_merges_editSameMC <- data.frame(numbers, rates)
  row.names(summary_merges_editSameMC) <- c("Merges_with_EditSameMC", "Conflicting", "Non_conflicting")
  ############# EditSameMC analysis per merge scenarios  ############# 
  
  ############# EditSameFD analysis per cases ############# 
  conflictingEditSameFD <- sum(predictorTemp$Conflicting_EditSameFD) - sum(predictorTemp$Conflicting_EditSameFD_DS)
  nonConflictingEditSameFD <- sum(predictorTemp$NonConflicting_EditSameFD) - sum(predictorTemp$NonConflicting_EditSameFD_DS)
  totalEditSameFD <- conflictingEditSameFD + nonConflictingEditSameFD
  number <- c(totalEditSameFD, conflictingEditSameFD, nonConflictingEditSameFD)
  editSameFdRate <- (conflictingEditSameFD/totalEditSameFD)*100
  ncEditSameFdRate <- (nonConflictingEditSameFD/totalEditSameFD)*100
  rate <- c(100,editSameFdRate, ncEditSameFdRate)
  table_editsamefd <- data.frame(number,rate)
  row.names(table_editsamefd) <- c("Total_EditSameFd_cases", "Conflicting", "Non_conflicting")
  ############# EditSameFD analysis per cases ############# 
  
  ############# EditDiffMC analysis per cases ############# 
  totalCases <- sum(predictorTemp$EditDiffMC) + sum(predictorTemp$EditDifffMC_EditSameMC)
  cases <- c(sum(predictorTemp$EditDiffMC), sum(predictorTemp$EditDifffMC_EditSameMC), totalCases)
  totalAdds <- sum(predictorTemp$EditDiffMC_EditionAddsMethodInvocation) + 
    sum(predictorTemp$EditDiffMC_EditionAddsMethodInvocation_EditSameMC)
  adds_call <- c(sum(predictorTemp$EditDiffMC_EditionAddsMethodInvocation), 
                 sum(predictorTemp$EditDiffMC_EditionAddsMethodInvocation_EditSameMC), totalAdds)
  editDiff_rate <- (sum(predictorTemp$EditDiffMC_EditionAddsMethodInvocation)/sum(predictorTemp$EditDiffMC))*100
  editSame_rate <- (sum(predictorTemp$EditDiffMC_EditionAddsMethodInvocation_EditSameMC)/sum(predictorTemp$EditDifffMC_EditSameMC))*100
  total_rate <- (totalAdds/totalCases)*100
  adds_call_rate <- c(editDiff_rate, editSame_rate, total_rate)
  summary_EditDiffMC <- data.frame(cases, adds_call, adds_call_rate)
  
  row.names(summary_EditDiffMC) <- c("EditDiffMC", "EditSameMC", "Total")
  ############# EditDiffMC analysis per cases ############# 
  
  ############# EditDiffMC analysis per merge scenarios #############
  totalMergeCases <- sum(MSpredictorTemp$Merges_With_EditDiff) + 
    sum(MSpredictorTemp$Merges_With_EditDiff_Same)
  
  totalMergeAddsCall <- sum(MSpredictorTemp$Merges_With_EditDiffAddsCall) + 
    sum(MSpredictorTemp$Merges_With_EditDiff_SameAddsCall)
  
  merges_editDiff_rate <- (sum(MSpredictorTemp$Merges_With_EditDiffAddsCall)/sum(MSpredictorTemp$Merges_With_EditDiff))*100
  merges_editSame_rate  <- (sum(MSpredictorTemp$Merges_With_EditDiff_SameAddsCall)/sum(MSpredictorTemp$Merges_With_EditDiff_Same))*100
  merges_total_rate <- (totalMergeAddsCall/totalMergeCases)*100
  
  merges <- c(sum(MSpredictorTemp$Merges_With_EditDiff) , 
                sum(MSpredictorTemp$Merges_With_EditDiff_Same), totalMergeCases)
  m_adds_call <- c(sum(MSpredictorTemp$Merges_With_EditDiffAddsCall) , 
                     sum(MSpredictorTemp$Merges_With_EditDiff_SameAddsCall), totalMergeAddsCall)
  m_adds_rate <- c(merges_editDiff_rate,merges_editSame_rate, merges_total_rate)
  summary_mergesEditDiff <- data.frame(merges,m_adds_call,m_adds_rate)
  row.names(summary_mergesEditDiff) <- c("EditDiffMC", "EditSameMC", "Total")
  ############# EditDiffMC analysis per merge scenarios #############
  
  ############# HTML code  ############# 
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
  
  HTML("<hr><h2>Merge Scenarios Rates With Conflicting and Non Conflicting EditSameMC</h2>", file=htmlFile, append=TRUE)
  HTML("<hr><h2>EditSameMC Rates per Merge Scenario Beanplot</h2>", file=htmlFile, append=TRUE)
  HTMLInsertGraph(file=htmlFile, GraphFileName=beanplotMergesEditSameMC, Align="center", append=TRUE)
  HTML("<hr><h2>EditSameMC Rates per Merge Scenario Boxplot</h2>", file=htmlFile, append=TRUE)
  HTMLInsertGraph(file=htmlFile, GraphFileName=boxplotMergesEditSameMC, Align="center", append=TRUE)
  HTML(summary_merges_editSameMC, file=htmlFile, append=TRUE)
  HTML(merge_metrics, file=htmlFile, append=TRUE)
  
  HTML("<hr><h2>EditSameFD summary cases</h2>", file=htmlFile, append=TRUE)
  HTML(table_editsamefd, file=htmlFile, append=TRUE)
  
  HTML("<hr><h2>EditDiffMC summary cases</h2>", file=htmlFile, append=TRUE)
  HTML(summary_EditDiffMC, file=htmlFile, append=TRUE)
  HTML("<hr><h2>EditDiffMC summary cases rate per merge scenario</h2>", file=htmlFile, append=TRUE)
  HTML(summary_mergesEditDiff, file=htmlFile, append=TRUE)
}

main()