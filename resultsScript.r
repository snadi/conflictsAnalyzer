#to do list:
#place new column with conflict rate percentage

computePatternPercentages <- function(conflicts, patternName){
  
  patternPercentages <- c()
  
  numberOfRows <- nrow(conflicts)
  
  for(i in 1:numberOfRows){
    sumConflicts <- 0
    
    for(j in 4:9){
      sumConflicts <- sum(sumConflicts, conflicts[i,j])
    }
    
    value <- conflicts[i, patternName]
    if(sumConflicts == 0){
      percentage <- 0
    }else{
      percentage <- (value/sumConflicts)*100
    }
    
    patternPercentages  <- append(patternPercentages, percentage)
    
  }
  return(patternPercentages)
}

deleteAllFiles <- function(exportPath) {
  
  fileToRemove = paste(exportPath, "conflictResults.html", sep="")
  if (file.exists(fileToRemove)) {
    file.remove(fileToRemove)
  }
  
#   fileToRemove = paste(exportPath, "BarChart.png", sep="")
#     if (file.exists(fileToRemove)) {
#       file.remove(fileToRemove)
#     }
#   
#   fileToRemove = paste(exportPath, "BoxplotConflicts.png", sep="")
#   if (file.exists(fileToRemove)) {
#     file.remove(fileToRemove)
#   }
}


main<-function(){
importPath = "/Users/paolaaccioly/Documents/Doutorado/conflictsStudy/ConflictsAnalyzer/"
exportPath = "/Users/paolaaccioly/Dropbox/Public/conflictpattern/"

conflictRateFile="projectsPatternData.csv"
#conflictPatternFile="patternsData.csv"

#HTML file
htmlFile = paste(exportPath, "conflictResults.html", sep="")

#delete previous files
deleteAllFiles(exportPath)

#read and edit conflict rate table
conflictRateTemp = read.table(file=paste(importPath, conflictRateFile, sep=""), header=T)
conflictRate2 = data.frame(conflictRateTemp$Project, conflictRateTemp$Merge_Scenarios, conflictRateTemp$Conflicting_Scenarios)
colnames(conflictRate2) <- c("Project", "Merge_Scenarios", "Conflicting_Scenarios")
sumMergeScenarios = sum(conflictRate2$Merge_Scenarios)
sumConflictionScenarios = sum(conflictRate2$Conflicting_Scenarios)
total = data.frame(Project="TOTAL", Merge_Scenarios=sumMergeScenarios, Conflicting_Scenarios=sumConflictionScenarios)
conflictRate = rbind(conflictRate2, total)

conflictRate["Conflict_Rate(%)"] <- (conflictRate$Conflicting_Scenarios/conflictRate$Merge_Scenarios)*100
attach(conflictRate)

#boxplot conflicting rate
boxplotCRFileName = paste("BoxplotCR.png")
png(paste(exportPath, boxplotCRFileName, sep=""))
boxplot(conflictRate$Conflict_Rate,xlab="Projects", ylab="Conflict Rate %", col="darkgreen")
dev.off

#read conflict patterns values 
DefaultValueAnnotation <- sum(conflictRateTemp$DefaultValueAnnotation)
ImplementList <- sum(conflictRateTemp$ImplementList)
ModifierList <- sum(conflictRateTemp$ModifierList)
LineBasedMCFd <- sum(conflictRateTemp$LineBasedMCFd)
SameSignatureCM <- sum(conflictRateTemp$SameSignatureCM)
SameIdFd <- sum(conflictRateTemp$SameIdFd)

# bar plot all conflicts
barChartFileName = paste("BarChart.png")
png(paste(exportPath, barChartFileName, sep=""))
slices <- c(DefaultValueAnnotation, ImplementList, ModifierList, LineBasedMCFd, SameSignatureCM, SameIdFd )
labels <- c("DefaultValueA", "ImplementList", "ModifierList", "LineBasedMCFd", "SameSignatureCM", "SameIdFd" ) 
dat <- data.frame(Frequency = slices,Conflicts = labels)
library(ggplot2)
p <- ggplot(dat, aes(x = Conflicts, y = Frequency)) +
  geom_bar(stat = "identity") +
  geom_text(aes(label = sprintf("%.2f%%", Frequency/sum(Frequency) * 100)), 
            vjust = -.5)

print(p)
dev.off

#conflicts table
Conflicts_Patterns <- c("DefaultValueAnnotation", "ImplementList", "ModifierList", "LineBasedMCFd", "SameSignatureCM", "SameIdFd", "TOTAL")
conflictsSum <- sum(DefaultValueAnnotation, ImplementList, ModifierList, LineBasedMCFd, SameSignatureCM,SameIdFd)
Occurrences <- c(DefaultValueAnnotation, ImplementList, ModifierList, LineBasedMCFd, SameSignatureCM,SameIdFd, conflictsSum)
conflictsTable <- data.frame(Conflicts_Patterns, Occurrences)

#boxplot for each conflict pattern percentages along all projects

#LineBasedMCFd 
boxplotLBMCF = paste("BoxplotLBMCF.png")
png(paste(exportPath, boxplotLBMCF, sep=""))
percentages <- computePatternPercentages(conflictRateTemp, "LineBasedMCFd")
boxplot(percentages,xlab="Projects", ylab="LineBasedMCFd (%)", col="blue", outline=FALSE)
dev.off

#SameSignatureCM
BoxplotSSCM = paste("BoxplotSSCM.png")
png(paste(exportPath, BoxplotSSCM, sep=""))
percentages <- computePatternPercentages(conflictRateTemp, "SameSignatureCM")
boxplot(percentages,xlab="Projects", ylab="SameSignatureCM (%)", col="red", outline=FALSE)
dev.off

#ImplementList
BoxplotIL = paste("BoxplotIL.png")
png(paste(exportPath, BoxplotIL, sep=""))
percentages <- computePatternPercentages(conflictRateTemp, "ImplementList")
boxplot(percentages,xlab="Projects", ylab="ImplementList (%)", col="chocolate4", outline=FALSE)
dev.off

#ModifierList
BoxplotML = paste("BoxplotML.png")
png(paste(exportPath, BoxplotML, sep=""))
percentages <- computePatternPercentages(conflictRateTemp, "ModifierList")
boxplot(percentages,xlab="Projects", ylab="ModifierList (%)", col="green", outline=FALSE)
dev.off

#SameIdFd
BoxplotSIF = paste("BoxplotSIF.png")
png(paste(exportPath, BoxplotSIF, sep=""))
percentages <- computePatternPercentages(conflictRateTemp, "SameIdFd")
boxplot(percentages,xlab="Projects", ylab="SameIdFd (%)", col="darkgoldenrod2", outline=FALSE)
dev.off

#DefaultValueAnnotation
BoxplotDVA = paste("BoxplotDVA.png")
png(paste(exportPath, BoxplotDVA, sep=""))
percentages <- computePatternPercentages(conflictRateTemp, "DefaultValueAnnotation")
boxplot(percentages,xlab="Projects", ylab="DefaultValueAnnotation (%)", col="darkviolet", outline=FALSE)
dev.off


#bar plot last project
numberOfRows <- length(conflictRateTemp[,1])
lastProject <- conflictRateTemp[numberOfRows,]
name <- lastProject$Project
DefaultValueAnnotation <- lastProject$DefaultValueAnnotation
ImplementList <- lastProject$ImplementList
ModifierList <- lastProject$ModifierList
LineBasedMCFd <- lastProject$LineBasedMCFd
SameSignatureCM <- lastProject$SameSignatureCM
SameIdFd <- lastProject$SameIdFd
barPlotFileName = paste(name, "BarPlot.png", sep="")
png(paste(exportPath, barPlotFileName, sep=""))
slices <- c(DefaultValueAnnotation, ImplementList, ModifierList, LineBasedMCFd, SameSignatureCM, SameIdFd )
labels <- c("DefaultValueAnnotation", "ImplementList", "ModifierList", "LineBasedMCFd", "SameSignatureCM", "SameIdFd" ) 
par(las=2)
par(mar=c(5,8,4,2))
barplot(slices, main=name, horiz=TRUE, names.arg=labels, cex.names=0.8, col=c("darkviolet","chocolate4", "darkgreen", "darkblue", "red" , "darkgoldenrod2"))
dev.off

#HTML code
library(R2HTML)

title = paste("<hr><h1>Results for Conflict Rate and Conflict Patterns Occurrences</h1>", sep="")

HTML.title(title, file=htmlFile, append=TRUE)

HTML("<hr><h2>Conflict Rate</h2>", file=htmlFile, append=TRUE)
HTML(conflictRate, file=htmlFile, append=TRUE)

HTML("<hr><h2>Conflict Rate Boxplot</h2>", file=htmlFile, append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=boxplotCRFileName, Align="center", append=TRUE)

HTML("<hr><h2>Conflict Patterns Occurrences</h2>", file=htmlFile, append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=barChartFileName, Align="center", append=TRUE)

HTML("<hr><h2>Conflicts Table</h2>", file=htmlFile, append=TRUE)
HTML(conflictsTable, file=htmlFile, append=TRUE)

HTML("<hr><h2>Conflict Patterns Percentages by Project Boxplots</h2>", file=htmlFile, append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=boxplotLBMCF, Align="center", append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=BoxplotSSCM, Align="center", append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=BoxplotIL, Align="center", append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=BoxplotML, Align="center", append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=BoxplotSIF, Align="center", append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=BoxplotDVA, Align="center", append=TRUE)

time = Sys.time()
HTML("<hr><h2>Last Time Updated:</h2>", file=htmlFile, append=TRUE)
HTML(time, file=htmlFile, append=TRUE)

}

main()
