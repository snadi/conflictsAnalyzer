#to do list:
#place new column with conflict rate percentage

computePatternPercentages <- function(conflicts, patternName){
  
  patternPercentages <- c()
  
  numberOfRows <- nrow(conflicts)
  ds <- paste(patternName, "DS", sep="")
  cl <- paste(patternName, "CL", sep="")
  ifp <- paste(patternName, "IFP", sep="")
  
  for(i in 1:numberOfRows){
    sumConflicts <- 0
    diffSpacing <- 0
    consecLines <- 0
    intersection <- 0
    
    indexes <- c(4,8,12,16,20,24,28,32)
    
    for(j in indexes){
      sumConflicts <- sum(sumConflicts, conflicts[i,j])
      diffSpacing <- sum(diffSpacing, conflicts[i,j+1])
      consecLines <- sum(consecLines, conflicts[i,j+2])
      intersection <- sum(intersection, conflicts[i,j+3])
    }
    realSumConflicts = sumConflicts - diffSpacing - consecLines + intersection
    value <- conflicts[i, patternName]
    valueDS <- conflicts[i, ds]
    valueCL <- conflicts[i, cl]
    valueIFP <- conflicts[i, ifp]
    realValue = value - valueDS - valueCL + valueIFP
    
    if(realSumConflicts == 0){
      percentage <- 0
    }else{
      percentage <- (realValue/realSumConflicts)*100
    }
    
    patternPercentages  <- append(patternPercentages, percentage)
    
  }
  return(patternPercentages)
}

computeSameSignatureCausesPercentages <- function(conflicts, causeName){
  
  causePercentages <- c()
  ds <- paste(causeName, "DS", sep="")
  
  numberOfRows <- nrow(conflicts)
  
  for(i in 1:numberOfRows){
    sumCauses <- 0
    sumCausesDS <- 0

    indexes <- c(37, 39, 41, 43, 45)
    
    for(j in indexes){
      sumCauses <- sum(sumCauses, conflicts[i,j])
      sumCausesDS <- sum(sumCausesDS, conflicts[i,j+1])

    }
    
    realSumCauses <- sumCauses - sumCausesDS
    causeValue <- conflicts[i, causeName]
    causeValueDS <- conflicts[i, ds]
    realCauseValue = causeValue - causeValueDS
    
    
    if(realSumCauses == 0){
      percentage <- 0
    }else{
      percentage <- (realCauseValue/realSumCauses)*100
    }
    
    causePercentages  <- append(causePercentages, percentage)
    
  }
  return(causePercentages)
}



deleteAllFiles <- function(exportPath) {
  
  fileToRemove = paste(exportPath, "conflictResults.html", sep="")
  if (file.exists(fileToRemove)) {
    file.remove(fileToRemove)
  }
  

}

main<-function(){
importPath = "/Users/paolaaccioly/Documents/testeConflictsAnalyzer/conflictsAnalyzer/"
exportPath = "/Users/paolaaccioly/Dropbox/Public/conflictpattern/"

conflictRateFile="projectsPatternData.csv"
realConflictRateFile = "realConflictRate.csv"


#HTML file
htmlFile = paste(exportPath, "conflictResults.html", sep="")

#delete previous files
deleteAllFiles(exportPath)

#read and edit conflict rate table
conflictRateTemp = read.table(file=paste(importPath, conflictRateFile, sep=""), header=T, sep=",")
conflictRate2 = data.frame(conflictRateTemp$Project, conflictRateTemp$Merge_Scenarios, conflictRateTemp$Conflicting_Scenarios)
colnames(conflictRate2) <- c("Project", "Merge_Scenarios", "Conflicting_Scenarios")
sumMergeScenarios = sum(conflictRate2$Merge_Scenarios)
sumConflictionScenarios = sum(conflictRate2$Conflicting_Scenarios)
total = data.frame(Project="TOTAL", Merge_Scenarios=sumMergeScenarios, Conflicting_Scenarios=sumConflictionScenarios)
conflictRate = rbind(conflictRate2, total)

conflictRate["Conflict_Rate(%)"] <- (conflictRate$Conflicting_Scenarios/conflictRate$Merge_Scenarios)*100
attach(conflictRate)

#read and edit real conflict rate table
  realConflictRateFileTemp = read.table(file=paste(importPath,realConflictRateFile , sep=""), header=T, sep=",")
 realconflictRate2 = data.frame(realConflictRateFileTemp$Projects, realConflictRateFileTemp$Merge.Scenarios, realConflictRateFileTemp$Conflicting.Scenarios)
 colnames(realconflictRate2) <- c("Projects", "Merge.Scenarios", "Conflicting.Scenarios")
 realsumMergeScenarios = sum(realconflictRate2$Merge.Scenarios)
 realsumConflictionScenarios = sum(realconflictRate2$Conflicting.Scenarios)
 realtotal = data.frame(Projects="TOTAL", Merge.Scenarios=realsumMergeScenarios,
                        Conflicting.Scenarios=realsumConflictionScenarios)
 realconflictRate = rbind(realconflictRate2, realtotal)

 realconflictRate["Conflict.Rate(%)"] <- (realconflictRate$Conflicting.Scenarios/realconflictRate$Merge.Scenarios)*100
 attach(realconflictRate)




#beanplot conflicting rate
library(beanplot)
beanplotCRFileName = paste("BeanplotCR.png")
png(paste(exportPath, beanplotCRFileName, sep=""))
beanplot(conflictRate$Conflict_Rate, xlab="Projects", ylab="Conflict Rate %",col="green")
dev.off

#boxplot conflicting rate
boxplotCRFileName = paste("BoxplotCR.png")
png(paste(exportPath, boxplotCRFileName, sep=""))
boxplot(conflictRate$Conflict_Rate, xlab="Projects", ylab="Conflict Rate %",col="green")
dev.off

#beanplot real conflicting rate

realbeanplotCRFileName = paste("realBeanplotCR.png")
png(paste(exportPath, realbeanplotCRFileName, sep=""))
beanplot(realconflictRate$Conflict.Rate, xlab="Projects", ylab="Conflict Rate %",col="green")
dev.off

#boxplot real conflicting rate

realboxplotCRFileName = paste("realBoxplotCR.png")
png(paste(exportPath, realboxplotCRFileName, sep=""))
boxplot(realconflictRate$Conflict.Rate, xlab="Projects", ylab="Conflict Rate %",col="green")
dev.off

#read conflict patterns values 
DefaultValueAnnotation <- sum(conflictRateTemp$DefaultValueAnnotation)
ImplementList <- sum(conflictRateTemp$ImplementList)
ModifierList <- sum(conflictRateTemp$ModifierList)
EditSameMC <- sum(conflictRateTemp$EditSameMC)
SameSignatureCM <- sum(conflictRateTemp$SameSignatureCM)
AddSameFd <- sum(conflictRateTemp$AddSameFd)
EditSameFd <- sum(conflictRateTemp$EditSameFd)
ExtendsList <- sum(conflictRateTemp$ExtendsList)

# bar plot all conflicts
barChartFileName = paste("BarChart.png")
png(paste(exportPath, barChartFileName, sep=""))
slices <- c(DefaultValueAnnotation, ImplementList, ModifierList, EditSameMC, SameSignatureCM, AddSameFd, EditSameFd, ExtendsList)
labels <- c("DefaultValueA", "ImplementList", "ModifierList", "EditSameMC", "SameSignatureCM", "AddSameFd", "EditSameFd", "ExtendsList") 
dat <- data.frame(Frequency = slices,Conflicts = labels)
library(ggplot2)
p <- ggplot(dat, aes(x = Conflicts, y = Frequency)) +
  geom_bar(stat = "identity") +
  geom_text(aes(label = sprintf("%.2f%%", Frequency/sum(Frequency) * 100)), 
            vjust = -.5) + theme_grey(base_size = 8) 

print(p)
dev.off

#conflicts table
Conflicts_Patterns <- c("DefaultValueAnnotation", "ImplementList", "ModifierList", "EditSameMC", "SameSignatureCM", "AddSameFd", "EditSameFd", "ExtendsList", "TOTAL")
conflictsSum <- sum(DefaultValueAnnotation, ImplementList, ModifierList, EditSameMC, SameSignatureCM,AddSameFd, EditSameFd, ExtendsList)
Occurrences <- c(DefaultValueAnnotation, ImplementList, ModifierList, EditSameMC, SameSignatureCM,AddSameFd, EditSameFd, ExtendsList, conflictsSum)
conflictsTable <- data.frame(Conflicts_Patterns, Occurrences)

#boxplot for each conflict pattern percentages along all projects

#EditSameMC 
#boxplotLBMCF = paste("BoxplotLBMCF.png")
#png(paste(exportPath, boxplotLBMCF, sep=""))
EditSameMCpercentages <- computePatternPercentages(conflictRateTemp, "EditSameMC")
#boxplot(EditSameMCpercentages,xlab="Projects", ylab="EditSameMC (%)", col="blue")
#dev.off

#false positives EditSameMC
BarPlotESMCFP = paste("BarPlotESMCFP.png")
png(paste(exportPath, BarPlotESMCFP, sep=""))
sumEditSameMCDS = sum(conflictRateTemp$EditSameMCDS)
sumEditSameMCCL = sum(conflictRateTemp$EditSameMCCL)
sumEditSameMCIFP = sum(conflictRateTemp$EditSameMCIFP)
realEditSameMC = EditSameMC - sumEditSameMCDS - sumEditSameMCCL + sumEditSameMCIFP
EditSameMCDS = sumEditSameMCDS - sumEditSameMCIFP
EditSameMCCL = sumEditSameMCCL - sumEditSameMCIFP
#round percentages
percentageRealEditSameMC <- round((realEditSameMC/EditSameMC)*100,digit=1)
percentageEditSameMCDS <- round((EditSameMCDS/EditSameMC)*100,digit=1)
percentageEditSameMCCL <- round((EditSameMCCL/EditSameMC)*100,digit=1)
percentagesumEditSameMCIFP <- round((sumEditSameMCIFP/EditSameMC)*100,digit=1)


npercentageRealEditSameMC <- paste(c("Possible conflicts-",percentageRealEditSameMC , "%"), collapse = "")

npercentageEditSameMCDS <- paste(c("Conflicts due to different identation-",percentageEditSameMCDS ,
                                  "%"), collapse = "")
npercentageEditSameMCCL <- paste(c("Conflicts due to consecutive lines-", percentageEditSameMCCL,
                                  "%"), collapse = "")
npercentagesumEditSameMCIFP <- paste(c("Intersection-", percentagesumEditSameMCIFP, "%"), 
                                    collapse = "")

Values <- c(percentageRealEditSameMC , percentageEditSameMCDS , percentageEditSameMCCL, 
            percentagesumEditSameMCIFP)
Group <- c(npercentageRealEditSameMC, npercentageEditSameMCDS, npercentageEditSameMCCL,
           npercentagesumEditSameMCIFP)

df <- data.frame(Group, Values)
bp<- ggplot(df, aes(x="EditSameMC", y=Values, fill=Group))+
  geom_bar(width = 1, stat = "identity")

print(bp)
dev.off


#SameSignatureCM
#BoxplotSSCM = paste("BoxplotSSCM.png")
#png(paste(exportPath, BoxplotSSCM, sep=""))
SameSignatureCMpercentages <- computePatternPercentages(conflictRateTemp, "SameSignatureCM")
#boxplot(SameSignatureCMpercentages,xlab="Projects", ylab="SameSignatureCM (%)", col="red")
#dev.off

#false positives SameSignatureCM
BarPlotSSCMFP = paste("BarPlotSSCMFP.png")
png(paste(exportPath, BarPlotSSCMFP, sep=""))
sumSameSignatureMCDS = sum(conflictRateTemp$SameSignatureCMDS)
realSameSignatureMC = SameSignatureCM - sumSameSignatureMCDS

percentageSumSameSignatureMCDS <- round((sumSameSignatureMCDS/SameSignatureCM)*100, digit=1)
percentageRealSameSignatureMC <- round ((realSameSignatureMC/SameSignatureCM)*100, digit=1)

npercentageSumSameSignatureMCDS <- paste(c("Conflicts due to different identation-",percentageSumSameSignatureMCDS , "%"), 
                                         collapse = "")
npercentageRealSameSignatureMC <- paste(c("Possible conflicts-",percentageRealSameSignatureMC , "%"), 
                                        collapse = "")


Values <- c(percentageRealSameSignatureMC, percentageSumSameSignatureMCDS)
Group <- c(npercentageRealSameSignatureMC, npercentageSumSameSignatureMCDS)
df <- data.frame(Group, Values)
bp<- ggplot(df, aes(x="SameSignatureMC", y=Values, fill=Group))+
  geom_bar(width = 1, stat = "identity")
print(bp)
dev.off

#bar plot without false positives
realDefaultValueAnnotation <- sum(conflictRateTemp$DefaultValueAnnotation) - 
  sum(conflictRateTemp$DefaultValueAnnotationDS) - sum(conflictRateTemp$DefaultValueAnnotationCS) + 
  sum(conflictRateTemp$DefaultValueAnnotationIFP)
realImplementList <- sum(conflictRateTemp$ImplementList) - sum(conflictRateTemp$ImplementListDS) - 
  sum(conflictRateTemp$ImplementListCL) + sum(conflictRateTemp$ImplementListIFP)
realModifierList <- sum(conflictRateTemp$ModifierList) - sum(conflictRateTemp$ModifierListDS) - 
  sum(conflictRateTemp$ModifierListCL) + sum(conflictRateTemp$ModifierListIFP)
realAddSameFd <- sum(conflictRateTemp$AddSameFd) - sum(conflictRateTemp$AddSameFdDS) - 
  sum(conflictRateTemp$AddSameFdCL) + sum(conflictRateTemp$AddSameFdIFP)
realEditSameFd <- sum(conflictRateTemp$EditSameFd) - sum(conflictRateTemp$EditSameFdDS) - 
  sum(conflictRateTemp$EditSameFdCL) + sum(conflictRateTemp$EditSameFdIFP)
realExtendsList <- sum(conflictRateTemp$ExtendsList) - sum(conflictRateTemp$ExtendsListDS) - 
  sum(conflictRateTemp$ExtendsListCL) + sum(conflictRateTemp$ExtendsListIFP)

barChartFP = paste("barChartFP.png")
png(paste(exportPath, barChartFP, sep=""))
slices <- c(realDefaultValueAnnotation, realImplementList, realModifierList, realEditSameMC, 
            realSameSignatureMC, realAddSameFd, realEditSameFd, realExtendsList)
labels <- c("DefaultValueA", "ImplementList", "ModifierList", "EditSameMC", "SameSignatureCM", 
            "AddSameFd", "EditSameFd", "ExtendsList") 
dat <- data.frame(Frequency = slices,Conflicts = labels)

p <- ggplot(dat, aes(x = Conflicts, y = Frequency)) +
  geom_bar(stat = "identity") +
  geom_text(aes(label = sprintf("%.2f%%", Frequency/sum(Frequency) * 100)), 
            vjust = -.5) + theme_grey(base_size = 8) 

print(p)
dev.off

#conflicts table
Conflicts_Patterns <- c("DefaultValueAnnotation", "ImplementList", "ModifierList", "EditSameMC", 
                        "SameSignatureCM", "AddSameFd", "EditSameFd", "ExtendsList", "TOTAL")
conflictsSum <- sum(realDefaultValueAnnotation, realImplementList, realModifierList, realEditSameMC, 
                    realSameSignatureMC, realAddSameFd, realEditSameFd, realExtendsList)
Occurrences <- c(realDefaultValueAnnotation, realImplementList, realModifierList, realEditSameMC, 
                 realSameSignatureMC, realAddSameFd, realEditSameFd, realExtendsList, conflictsSum)
realconflictsTable <- data.frame(Conflicts_Patterns, Occurrences)

#causes for SameSignatureCM
BoxplotCSSCM = paste("CausesSameSignatureCM.png")
png(paste(exportPath, BoxplotCSSCM, sep=""))

sumSmallMethod = round(((sum(conflictRateTemp$smallMethod) - sum(conflictRateTemp$smallMethodDS))/
                          realSameSignatureMC)*100, digit=1)

sumRenamedMethod = round(((sum(conflictRateTemp$renamedMethod) - sum(conflictRateTemp$renamedMethodDS))/
  realSameSignatureMC)*100, digit=1)

sumCopiedMethod= round(((sum(conflictRateTemp$copiedMethod) - sum(conflictRateTemp$copiedMethodDS))/
  realSameSignatureMC)*100, digit=1)

sumCopiedFile = round(((sum(conflictRateTemp$copiedFile) - sum(conflictRateTemp$copiedFileDS))/
                         realSameSignatureMC)*100, digit=1)

sumNoPattern = round(((sum(conflictRateTemp$noPattern) - sum(conflictRateTemp$noPatternDS))/
                        realSameSignatureMC)*100, digit=1)

nsumSmallMethod <- paste(c("Small methods-", sumSmallMethod, "%"), collapse = "")
nsumRenamedMethod <- paste(c("Renamed methods-", sumRenamedMethod, "%"), collapse = "")
nsumCopiedMethod <- paste(c("Copied methods-", sumCopiedMethod, "%"), collapse = "")
nsumCopiedFile <- paste(c("Merge from the same branch-", sumCopiedFile, "%"), collapse = "")
nsumNoPattern <- paste(c("No pattern detected-", sumNoPattern, "%"), collapse = "")

Values <- c(sumSmallMethod, sumRenamedMethod, sumCopiedMethod, sumCopiedFile, sumNoPattern)
Causes <- c(nsumSmallMethod, nsumRenamedMethod, nsumCopiedMethod, nsumCopiedFile,
            nsumNoPattern)
df <- data.frame(Causes, Values)
bp<- ggplot(df, aes(x="Causes", y=Values, fill=Causes))+
      geom_bar(width = 1, stat = "identity")
print(bp)
dev.off

#boxplot with the samesignaturecm cause percentages
BoxplotAllCauses = paste("BoxplotAllCauses.png")
png(paste(exportPath, BoxplotAllCauses, sep=""))
smallMethod <- computeSameSignatureCausesPercentages(conflictRateTemp, "smallMethod")
renamedMethod <- computeSameSignatureCausesPercentages(conflictRateTemp, "renamedMethod")
copiedMethod <- computeSameSignatureCausesPercentages(conflictRateTemp, "copiedMethod")
copiedFile <- computeSameSignatureCausesPercentages(conflictRateTemp, "copiedFile")
noPattern <- computeSameSignatureCausesPercentages(conflictRateTemp, "noPattern")
allCausesPercentages <- data.frame(smallMethod, renamedMethod, copiedMethod,
                                   copiedFile, noPattern )
op <- par(mar = c(3, 8, 2, 2) + 0.1) #adjust margins, default is c(5, 4, 4, 2) + 0.1
boxplot(allCausesPercentages, xlab="", ylab="", col="green", horizontal = TRUE, las=1, cex.axis=1)
par(op)
dev.off

#boxplot

#ImplementList
#BoxplotIL = paste("BoxplotIL.png")
#png(paste(exportPath, BoxplotIL, sep=""))
ImplementListpercentages <- computePatternPercentages(conflictRateTemp, "ImplementList")
#boxplot(ImplementListpercentages,xlab="Projects", ylab="ImplementList (%)", col="chocolate4")
#dev.off

#ModifierList
#BoxplotML = paste("BoxplotML.png")
#png(paste(exportPath, BoxplotML, sep=""))
ModifierListpercentages <- computePatternPercentages(conflictRateTemp, "ModifierList")
#boxplot(ModifierListpercentages,xlab="Projects", ylab="ModifierList (%)", col="green")
#dev.off

#AddSameFd
#BoxplotSIF = paste("BoxplotSIF.png")
#png(paste(exportPath, BoxplotSIF, sep=""))
AddSameFdpercentages <- computePatternPercentages(conflictRateTemp, "AddSameFd")
#boxplot(AddSameFdpercentages,xlab="Projects", ylab="AddSameFd (%)", col="darkgoldenrod2")
#dev.off

#EditSameFd
#BoxplotESF = paste("BoxplotESF.png")
#png(paste(exportPath, BoxplotESF, sep=""))
EditSameFdpercentages <- computePatternPercentages(conflictRateTemp, "EditSameFd")
#boxplot(EditSameFdpercentages,xlab="Projects", ylab="EditSameFd (%)", col="gray")
#dev.off

#DefaultValueAnnotation
#BoxplotDVA = paste("BoxplotDVA.png")
#png(paste(exportPath, BoxplotDVA, sep=""))
DefaultValueAnnotationpercentages <- computePatternPercentages(conflictRateTemp, "DefaultValueAnnotation")
#boxplot(DefaultValueAnnotationpercentages,xlab="Projects", ylab="DefaultValueAnnotation (%)", col="darkviolet")
#dev.off

#ExtendsList
#BoxplotEL = paste("BoxplotEL.png")
#png(paste(exportPath, BoxplotEL, sep=""))
ExtendsListpercentages <- computePatternPercentages(conflictRateTemp, "ExtendsList")
#boxplot(ExtendsListpercentages,xlab="Projects", ylab="ExtendsList (%)", col="chocolate4")
#dev.off


#all conflicts percentages boxplot
BoxplotAllConflicts = paste("BoxplotAllConflicts.png")
png(paste(exportPath, BoxplotAllConflicts, sep=""))
EditSameMC <- EditSameMCpercentages
SameSignatureCM <- SameSignatureCMpercentages
ImplementList <- ImplementListpercentages
ModifierList <- ModifierListpercentages
AddSameFd <- AddSameFdpercentages
EditSameFd <- EditSameFdpercentages
DefaultValueA <- DefaultValueAnnotationpercentages
ExtendsList <- ExtendsListpercentages
allConflictsPercentage <- data.frame(EditSameMC, SameSignatureCM, 
                                     ImplementList, ModifierList, 
                                     AddSameFd, EditSameFd, 
                                     DefaultValueA, ExtendsList)
op <- par(mar = c(3, 8, 2, 2) + 0.1) #adjust margins, default is c(5, 4, 4, 2) + 0.1
boxplot(allConflictsPercentage, xlab="", ylab="", col="green", horizontal = TRUE, las=1, cex.axis=1)
par(op)
dev.off


#bar plot last project
numberOfRows <- length(conflictRateTemp[,1])
lastProject <- conflictRateTemp[numberOfRows,]
name <- lastProject$Project
DefaultValueAnnotation <- lastProject$DefaultValueAnnotation
ImplementList <- lastProject$ImplementList
ModifierList <- lastProject$ModifierList
EditSameMC <- lastProject$EditSameMC
SameSignatureCM <- lastProject$SameSignatureCM
AddSameFd <- lastProject$AddSameFd
EditSameFd <- lastProject$EditSameFd
ExtendsList <- lastProject$ExtendsList
barPlotFileName = paste(name, "BarPlot.png", sep="")
png(paste(exportPath, barPlotFileName, sep=""))
slices <- c(DefaultValueAnnotation, ImplementList, ModifierList, EditSameMC, SameSignatureCM, AddSameFd, EditSameFd, ExtendsList)
labels <- c("DefaultValueAnnotation", "ImplementList", "ModifierList", "EditSameMC", "SameSignatureCM", "AddSameFd", "EditSameFd", "ExtendsList") 
par(las=2)
par(mar=c(5,8,4,2))
barplot(slices, main=name, horiz=TRUE, names.arg=labels, cex.names=0.8, col=c("darkviolet","chocolate4", "darkgreen", "darkblue", "red" , "darkgoldenrod2"))
dev.off

#HTML code
library(R2HTML)

title = paste("<hr><h1>Results for Conflict Rate and Conflict Patterns Occurrences</h1>", sep="")
HTML("<link rel=stylesheet type=text/css href=R2HTML.css>", file=htmlFile, append=TRUE)
HTML.title(title, file=htmlFile, append=TRUE)

HTML("<hr><h2>Conflict Rate</h2>", file=htmlFile, append=TRUE)
HTML(conflictRate, file=htmlFile, append=TRUE)

HTML("<hr><h2>Conflict Rate Beanplot and Boxplot</h2>", file=htmlFile, append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=beanplotCRFileName, Align="center", append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=boxplotCRFileName, Align="center", append=TRUE)

HTML("<hr><h2>Conflict Patterns Occurrences</h2>", file=htmlFile, append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=barChartFileName, Align="center", append=TRUE)

HTML("<hr><h2>Conflicts Table</h2>", file=htmlFile, append=TRUE)
HTML(conflictsTable, file=htmlFile, append=TRUE)

HTML("<hr><h2>Conflicts Patterns Occurrences Without the False Positives</h2>", file=htmlFile, append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=barChartFP, Align="center", append=TRUE)
HTML(realconflictsTable, file=htmlFile, append=TRUE)

HTML("<hr><h2>False Positives Occurences</h2>", file=htmlFile, append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=BarPlotESMCFP, Align="center", append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=BarPlotSSCMFP, Align="center", append=TRUE)

HTML("<hr><h2>Conflict Rate Without False Positives</h2>", file=htmlFile, append=TRUE)
HTML(realconflictRate, file=htmlFile, append=TRUE)

HTML("<hr><h2>Conflict Rate Beanplot and Boxplot Without False Positives</h2>", file=htmlFile, append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=realbeanplotCRFileName, Align="center", append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=realboxplotCRFileName, Align="center", append=TRUE)

HTML("<hr><h2>Causes for SameSignatureCM occurrences</h2>", file=htmlFile, append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=BoxplotCSSCM, Align="center", append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=BoxplotAllCauses, Align="center", append=TRUE)

HTML("<hr><h2>Conflict Pattern Percentages by Project</h2>", file=htmlFile, append=TRUE)
#HTMLInsertGraph(file=htmlFile, GraphFileName=boxplotLBMCF, Align="center", append=TRUE)
#HTMLInsertGraph(file=htmlFile, GraphFileName=BoxplotSSCM, Align="center", append=TRUE)
#HTMLInsertGraph(file=htmlFile, GraphFileName=BoxplotIL, Align="center", append=TRUE)
#HTMLInsertGraph(file=htmlFile, GraphFileName=BoxplotML, Align="center", append=TRUE)
#HTMLInsertGraph(file=htmlFile, GraphFileName=BoxplotSIF, Align="center", append=TRUE)
#HTMLInsertGraph(file=htmlFile, GraphFileName=BoxplotESF, Align="center", append=TRUE)
#HTMLInsertGraph(file=htmlFile, GraphFileName=BoxplotDVA, Align="center", append=TRUE)
#HTMLInsertGraph(file=htmlFile, GraphFileName=BoxplotEL, Align="center", append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=BoxplotAllConflicts, Align="center", append=TRUE)

time = Sys.time()
HTML("<hr><h2>Last Time Updated:</h2>", file=htmlFile, append=TRUE)
HTML(time, file=htmlFile, append=TRUE)

}

main()
