computeBarPlot<-function(project, exportPath){
  
  name <- project$Project 
  DefaultValueAnnotation = project$DefaultValueAnnotation
  ImplementList = project$ImplementList
  ModifierList = project$ModifierList
  EditSameMC = project$EditSameMC
  SameSignatureCM = project$SameSignatureCM
  AddSameFd = project$AddSameFd
  EditSameFd = project$EditSameFd
  barChartFileName = paste( name, "BarPlot", ".png")
  png(paste(exportPath, barChartFileName, sep=""))
  slices <- c(DefaultValueAnnotation, ImplementList, ModifierList, EditSameMC, SameSignatureCM, EditSameFd )
  labels <- c("DefaultValueAnnotation", "ImplementList", "ModifierList", "EditSameMC", "SameSignatureCM", "EditSameFd" ) 
  par(las=2)
  par(mar=c(5,8,4,2))
  barplot(slices, main=name, horiz=TRUE, names.arg=labels, cex.names=0.8, col=c("darkviolet","chocolate4", "darkgreen", "darkblue", "red" , "darkgoldenrod2"))
  dev.off
  
}

main<-function(){
importPath = "/Users/paolaaccioly/Dropbox/Doutorado/resultados_experimento/scriptResults/"
exportPath = "/Users/paolaaccioly/Dropbox/Doutorado/resultados_experimento/scriptResults/"

conflictRateFile="projectsPatternData.csv"

conflictsTable = read.table(file=paste(importPath, conflictRateFile, sep=""), header=T)

numberOfRows = length(conflictsTable[,1])

for(n in 1:numberOfRows){
  project <- conflictsTable[n,]
  computeBarPlot(project, exportPath)
  }

}

main()
