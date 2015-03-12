#to do list:
#place new column with conflict rate percentage

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
exportPath = "/Users/paolaaccioly/Dropbox/Doutorado/resultados_experimento/scriptResults/"

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
labels <- c("DefaultValueAnnotation", "ImplementList", "ModifierList", "LineBasedMCFd", "SameSignatureCM", "SameIdFd" ) 
par(las=2)
par(mar=c(5,8,4,2))
barplot(slices, horiz=TRUE, names.arg=labels, xlim=c(0,4500), cex.names=0.8, col=c("darkviolet","chocolate4", "darkgreen", "darkblue", "red" , "darkgoldenrod2"))
dev.off

#read all patterns again
DefaultValueAnnotation <- conflictRateTemp$DefaultValueAnnotation
ImplementList <- conflictRateTemp$ImplementList
ModifierList <- conflictRateTemp$ModifierList
LineBasedMCFd <- conflictRateTemp$LineBasedMCFd
SameSignatureCM <- conflictRateTemp$SameSignatureCM
SameIdFd <- conflictRateTemp$SameIdFd

#boxplot all conflicts
boxPlotFileName = paste("BoxplotConflicts.png")
png(paste(exportPath, boxPlotFileName, sep=""))
par(las=2)
#par(cex.lab=0.8)
#par(mar=c(5,8,4,2))
op <- par(mar = c(8, 4, 4, 2) + 0.1)
boxplot(DefaultValueAnnotation, ImplementList, ModifierList, LineBasedMCFd, SameSignatureCM, SameIdFd, 
        names=labels, cex.axis = 0.8, outline=FALSE, ylim=c(0,200), col=c("darkviolet","chocolate4", "darkgreen", "darkblue", "red" , "darkgoldenrod2"))
par(op)
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

HTML("<hr><h2>Graphics - Conflict Patterns Occurrences</h2>", file=htmlFile, append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=barChartFileName, Align="center", append=TRUE)
HTML("<hr><h2>Conflict Patterns Boxplot</h2>", file=htmlFile, append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=boxPlotFileName, Align="center", append=TRUE)
time = Sys.time()
HTML("<hr><h2>Last Time Updated:</h2>", file=htmlFile, append=TRUE)
HTML(time, file=htmlFile, append=TRUE)

}

main()
