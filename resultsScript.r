#to do list:
#place new column with conflict rate percentage

deleteAllFiles <- function(exportPath, htmlFile) {
  if (file.exists(htmlFile)) {
    file.remove(htmlFile)
  }
  
  fileToRemove = paste(exportPath, "BarChart.png", sep="")
    if (file.exists(fileToRemove)) {
      file.remove(fileToRemove)
    }
}


main<-function(){
importPath = "/Users/paolaaccioly/Documents/Doutorado/conflictsStudy/ConflictsAnalyzer/"
exportPath = "/Users/paolaaccioly/Dropbox/Public/conflictpattern/"

conflictRateFile="projectsData.csv"
conflictPatternFile="patternsData.csv"

#HTML file
htmlFile = paste(exportPath, "conflictResults.html", sep="")

#delete previous files
deleteAllFiles(exportPath, htmlFile)

#read and edit conflict rate table
conflictRateTemp = read.table(file=paste(importPath, conflictRateFile, sep=""), header=T)

sumMergeScenarios = sum(conflictRateTemp$Merge_Scenarios)
sumConflictionScenarios = sum(conflictRateTemp$Conflicting_Scenarios)
total = data.frame(Project="TOTAL", Merge_Scenarios=sumMergeScenarios, Conflicting_Scenarios=sumConflictionScenarios)
conflictRate = rbind(conflictRateTemp, total)

conflictRate["Conflict_Rate(%)"] <- (conflictRate$Conflicting_Scenarios/conflictRate$Merge_Scenarios)*100
attach(conflictRate)

#read and edit patterns pie chart
conflictPatterns= read.table(file=paste(importPath, conflictPatternFile, sep=""), header=T)

# bar chart 
library(ggplot2)
barChartFileName = paste("BarChart.png")
png(paste(exportPath, barChartFileName, sep=""))

df <- data.frame(patternName = factor(conflictPatterns$Pattern), occurrences = c(conflictPatterns$Occurrences))

immigration_theme <- theme_update(axis.text.x = element_text(angle = 45, hjust = 0.50))
p <- ggplot(data=df, aes(x= patternName, y= occurrences)) + geom_bar(stat="identity")
print(p)
p
dev.off

#HTML code
library(R2HTML)

title = paste("<hr><h1>Results for Conflict Rate and Conflict Patterns Occurrences</h1>", sep="")

HTML.title(title, file=htmlFile, append=TRUE)

HTML("<hr><h2>Conflict Rate</h2>", file=htmlFile, append=TRUE)
HTML(conflictRate, file=htmlFile, append=TRUE)

HTML("<hr><h2>Graphics - Conflict Patterns Occurrences</h2>", file=htmlFile, append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=barChartFileName, Align="center", append=TRUE)
time = Sys.time()
HTML("<hr><h2>Last Time Updated:</h2>", file=htmlFile, append=TRUE)
HTML(time, file=htmlFile, append=TRUE)

}

main()
