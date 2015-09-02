#to do list:
#place new column with conflict rate percentage

diffConflictRateFunc <- function(conflictRate, conflictRateWithoutFP){
  diffConflictRate <- c()
  
  numberOfRows <- length(conflictRateWithoutFP)
  
  for(i in 1:numberOfRows){
    
    if(conflictRate[i]==0){
      diff = 0
    } else{
      diff = (1- (conflictRateWithoutFP[i]/conflictRate[i]))*100
    }
    diffConflictRate  <- append(diffConflictRate, diff)
    
  }  
  return(diffConflictRate)
}

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

computeEditSameMCFPPercentages <- function(conflicts, editSameMethodCause){
  
  editSameMethodPercentages <- c()
  numberOfRows <- nrow(conflicts)

  for(i in 1:numberOfRows){
    sumConflicts <- conflicts[i,16]
    valueDS <- conflicts[i,17]
    valueCL <- conflicts[i,18]
    valueIFP <- conflicts[i,19]

    if(editSameMethodCause == "EditSameMC"){ 
      value <- sumConflicts - valueDS - valueCL + valueIFP
    }else if(editSameMethodCause == "EditSameMCDS"){
      value <- valueDS - valueIFP
    }else if(editSameMethodCause == "EditSameMCCL"){
      value <- valueCL - valueIFP
    }else if(editSameMethodCause == "EditSameMCIFP"){
      value <- valueIFP
    }
    
    if(sumConflicts == 0){
      percentage <- 0
    }else{
      percentage <- (value/sumConflicts)*100
    }
    
    editSameMethodPercentages  <- append(editSameMethodPercentages, percentage)
    
  }
  return(editSameMethodPercentages)
}

dataFrameEditSameMCFPPercentages <- function(conflicts){
  Possible.conflicts <- computeEditSameMCFPPercentages(conflicts, "EditSameMC")
  Different.identation <- computeEditSameMCFPPercentages(conflicts, "EditSameMCDS")
  Consecutive.lines <- computeEditSameMCFPPercentages(conflicts, "EditSameMCCL")
  Intersection <- computeEditSameMCFPPercentages(conflicts, "EditSameMCIFP")
  
  result <- data.frame(Possible.conflicts, Different.identation, Consecutive.lines, Intersection)
  return(result)
}

computeSameSignatureFPPercentages <- function(conflicts, sameSignatureCause){
  
  sameSignaturePercentages <- c()
  numberOfRows <- nrow(conflicts)
  
  for(i in 1:numberOfRows){
    sumConflicts <- conflicts[i,28]
    valueDS <- conflicts[i,29]
    
    if(sameSignatureCause == "SameSignatureCM"){ 
      value <- sumConflicts - valueDS
    }else if(sameSignatureCause == "SameSignatureCMDS"){
      value <- valueDS
    }
    
    if(sumConflicts == 0){
      percentage <- 0
    }else{
      percentage <- (value/sumConflicts)*100
    }
    
    sameSignaturePercentages  <- append(sameSignaturePercentages, percentage)
    
  }
  return(sameSignaturePercentages)
}

dataFrameSameSignatureFPPercentages <- function(conflicts){
  Possible.conflicts <- computeSameSignatureFPPercentages(conflicts, "SameSignatureCM")
  Different.identation <- computeSameSignatureFPPercentages(conflicts, "SameSignatureCMDS")
  
  result <- data.frame(Possible.conflicts, Different.identation)
  return(result)
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
conflictRate2 = data.frame(conflictRateTemp$Project, conflictRateTemp$Merge_Scenarios, 
                           conflictRateTemp$Conflicting_Scenarios)
colnames(conflictRate2) <- c("Projects", "Merge_Scenarios", "Conflicting_Scenarios")
sumMergeScenarios = sum(conflictRate2$Merge_Scenarios)
sumConflictionScenarios = sum(conflictRate2$Conflicting_Scenarios)
total = data.frame(Projects="TOTAL", Merge_Scenarios=sumMergeScenarios, 
                   Conflicting_Scenarios=sumConflictionScenarios)
conflictRate = rbind(conflictRate2, total)

conflictRate["Conflict_Rate(%)"] <- (conflictRate$Conflicting_Scenarios/conflictRate$Merge_Scenarios)*100
attach(conflictRate)

newTable <- head(conflictRate, -1)
Mean <- mean(newTable$Conflict_Rate)
Standard.deviation <- sd(newTable$Conflict_Rate)
metrics <- data.frame(Mean, Standard.deviation)

#read and edit real conflict rate table
realConflictRateFileTemp = read.table(file=paste(importPath,realConflictRateFile , sep=""), header=T, sep=",")
realconflictRate2 = data.frame(realConflictRateFileTemp$Project, realConflictRateFileTemp$Merge.Scenarios, 
                                realConflictRateFileTemp$Conflicting.Scenarios)
 colnames(realconflictRate2) <- c("Project", "Merge.Scenarios", "Conflicting.Scenarios")
 realsumMergeScenarios = sum(realconflictRate2$Merge.Scenarios)
 realsumConflictingScenarios = sum(realconflictRate2$Conflicting.Scenarios)
 realtotal = data.frame(Project="TOTAL", Merge.Scenarios=realsumMergeScenarios,
                        Conflicting.Scenarios=realsumConflictingScenarios)
 realconflictRate = rbind(realconflictRate2, realtotal)

 realconflictRate["Conflict.Rate(%)"] <- 
  (realconflictRate$Conflicting.Scenarios/realconflictRate$Merge.Scenarios)*100
 attach(realconflictRate)

realNewTable <- head(realconflictRate, -1)
Mean <- mean(realNewTable$Conflict.Rate)
Standard.deviation <- sd(realNewTable$Conflict.Rate)
realMetrics <- data.frame(Mean, Standard.deviation)

#beanplot conflicting rate
library(beanplot)

#boxplot conflicting rate with and without false positives
boxplotCRFileName = paste("BoxplotCR.png")
png(paste(exportPath, boxplotCRFileName, sep=""))
conflictRateWFP <- realconflictRate
dataConflict <-data.frame(conflictRate$Conflict_Rate, conflictRateWFP$Conflict.Rate)
colnames(dataConflict) <- c("CR", "CR without spacing and consecutive lines conflicts")
boxplot(dataConflict, ylab="Conflict Rate %",col="green")
dev.off()

#beanplot conflicting rate with and without false positives

realbeanplotCRFileName = paste("realBeanplotCR.png")
png(paste(exportPath, realbeanplotCRFileName, sep=""))
beanplot(dataConflict,  ylab="Conflict Rate %",col="green", cex=1.5)
dev.off()


#boxplot diff conflict rates
diffConflictRates <- diffConflictRateFunc(newTable$Conflict_Rate, realNewTable$Conflict.Rate)
boxplotDiffCR = paste("boxplotDiffCR.png")
png(paste(exportPath, boxplotDiffCR, sep=""))
boxplot(diffConflictRates, xlab="Projects", ylab="Difference of Conflict Rates %",col="green")
dev.off()

diffConflictRatesTable <- data.frame(mean(diffConflictRates), sd(diffConflictRates) )
colnames(diffConflictRatesTable) <- c("Mean", "Standard Deviation")

#beanplot diff conflict rates
beanplotDiffCR = paste("beanplotDiffCR.png")
png(paste(exportPath, beanplotDiffCR, sep=""))
beanplot(diffConflictRates, xlab="Projects", ylab="Difference of Conflict Rates %",col="green")
dev.off()

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
slices <- c(DefaultValueAnnotation, ImplementList, ModifierList, EditSameMC, SameSignatureCM, AddSameFd, 
            EditSameFd, ExtendsList)
labels <- c("DefaultValueA", "ImplementsList", "ModifierList", "EditSameMC", "SameSignatureMC", "AddSameFd", 
            "EditSameFd", "ExtendsList") 
dat <- data.frame(Frequency = slices,Conflicts = labels)
dat$Conflicts <- reorder(dat$Conflicts, dat$Frequency)
library(ggplot2)
p <- ggplot(dat, aes(y = Frequency)) +
  geom_bar(aes(x = Conflicts),stat = "identity",fill="green", colour="black") +
  geom_text(aes(x = Conflicts, label = sprintf("%.2f%%", Frequency/sum(Frequency) * 100)), hjust = -.1) + coord_flip() +
  theme_grey(base_size = 10) + labs(x=NULL, y=NULL)  + ylim(c(0,26000)) + ggtitle("FSTMerge")

print(p)
dev.off()

#conflicts table
Conflicts_Patterns <- c("DefaultValueAnnotation", "ImplementList", "ModifierList", "EditSameMC", 
                        "SameSignatureCM", "AddSameFd", "EditSameFd", "ExtendsList", "TOTAL")
conflictsSum <- sum(DefaultValueAnnotation, ImplementList, ModifierList, EditSameMC, SameSignatureCM,
                    AddSameFd, EditSameFd, ExtendsList)
Occurrences <- c(DefaultValueAnnotation, ImplementList, ModifierList, EditSameMC, SameSignatureCM,
                 AddSameFd, EditSameFd, ExtendsList, conflictsSum)
conflictsTable <- data.frame(Conflicts_Patterns, Occurrences)

#boxplot for each conflict pattern percentages along all projects

EditSameMCpercentages <- computePatternPercentages(conflictRateTemp, "EditSameMC")


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
bp<- ggplot(df, aes(x="", y=Values, fill=Group))+
  geom_bar(width = 1, stat = "identity") + ggtitle("EditSameMC")

print(bp)
dev.off()

#boxplot with the editSameMC cause percentages
BoxplotFPEditSameMC = paste("BoxplotFPEditSameMC.png")
png(paste(exportPath, BoxplotFPEditSameMC, sep=""))
allEditSameMCFPPercentages <- dataFrameEditSameMCFPPercentages(conflictRateTemp)
op <- par(mar = c(3, 8, 2, 2) + 0.1) #adjust margins, default is c(5, 4, 4, 2) + 0.1 bottom, left, top and right
boxplot(allEditSameMCFPPercentages, xlab="", ylab="", col="green", horizontal = TRUE, las=1, cex.axis=1)
par(op)
dev.off()



SameSignatureCMpercentages <- computePatternPercentages(conflictRateTemp, "SameSignatureCM")


#false positives SameSignatureCM
BarPlotSSCMFP = paste("BarPlotSSCMFP.png")
png(paste(exportPath, BarPlotSSCMFP, sep=""))
sumSameSignatureMCDS = sum(conflictRateTemp$SameSignatureCMDS)
realSameSignatureMC = SameSignatureCM - sumSameSignatureMCDS

percentageSumSameSignatureMCDS <- round((sumSameSignatureMCDS/SameSignatureCM)*100, digit=1)
percentageRealSameSignatureMC <- round ((realSameSignatureMC/SameSignatureCM)*100, digit=1)

npercentageSumSameSignatureMCDS <- paste(c("Conflicts due to different identation-",
                                           percentageSumSameSignatureMCDS , "%"), 
                                         collapse = "")
npercentageRealSameSignatureMC <- paste(c("Possible conflicts-",percentageRealSameSignatureMC , "%"), 
                                        collapse = "")


Values <- c(percentageRealSameSignatureMC, percentageSumSameSignatureMCDS)
Group <- c(npercentageRealSameSignatureMC, npercentageSumSameSignatureMCDS)
df <- data.frame(Group, Values)
bp<- ggplot(df, aes(x="SameSignatureMC", y=Values, fill=Group))+
  geom_bar(width = 1, stat = "identity")
print(bp)
dev.off()

#boxplot with the sameSignatureCM cause percentages
BoxplotFPSameSigCM = paste("BoxplotFPSameSigCM.png")
png(paste(exportPath, BoxplotFPSameSigCM, sep=""))
allSameSigPercentages <- dataFrameSameSignatureFPPercentages(conflictRateTemp)
op <- par(mar = c(3, 8, 2, 2) + 0.1) #adjust margins, default is c(5, 4, 4, 2) + 0.1
boxplot(allSameSigPercentages, xlab="", ylab="", col="green", horizontal = TRUE, las=1, cex.axis=1)
par(op)
dev.off()

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
labels <- c("DefaultValueA", "ImplementsList", "ModifierList", "EditSameMC", "SameSignatureMC", 
            "AddSameFd", "EditSameFd", "ExtendsList") 
dat2 <- data.frame(Conflicts = labels, Frequency = slices)
dat2$Conflicts <- reorder(dat2$Conflicts, dat2$Frequency)
p2 <- ggplot(dat2, aes(y = Frequency)) +
  geom_bar(aes(x = Conflicts),stat = "identity", fill="green", colour="black") +
  geom_text(aes(x = Conflicts, label = sprintf("%.2f%%", Frequency/sum(Frequency) * 100)), hjust = -.1) +
  coord_flip() + theme_grey(base_size = 10) + labs(x=NULL, y=NULL) + ylim(c(0,26000)) + 
  ggtitle("FSTMerge without spacing and consecutive lines conflicts")

print(p2)
dev.off()

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

Frequency <- c(sumSmallMethod, sumRenamedMethod, sumCopiedMethod, sumCopiedFile, sumNoPattern)
Causes <- c("Small methods", "Renamed Methods", "Copied Methods", "Copied Files",
            "No Pattern")
df <- data.frame(Frequency, Causes)
df$Causes <- reorder(df$Causes, df$Frequency)
p <- ggplot(df, aes(y = Frequency)) +
  geom_bar(aes(x = Causes),stat = "identity",fill="green", colour="black", width=.8) +
  geom_text(aes(x = Causes, label = sprintf("%.2f%%", Frequency/sum(Frequency) * 100)), hjust = -.1) + coord_flip() +
  theme_grey(base_size = 13) + labs(x=NULL, y=NULL) + ylim(c(0,70))

print(p)
dev.off()

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
boxplot(allCausesPercentages, xlab="", ylab="", col="green", horizontal = TRUE, las=1)
par(op)
dev.off()

ImplementListpercentages <- computePatternPercentages(conflictRateTemp, "ImplementList")

ModifierListpercentages <- computePatternPercentages(conflictRateTemp, "ModifierList")

AddSameFdpercentages <- computePatternPercentages(conflictRateTemp, "AddSameFd")

EditSameFdpercentages <- computePatternPercentages(conflictRateTemp, "EditSameFd")

DefaultValueAnnotationpercentages <- computePatternPercentages(conflictRateTemp, "DefaultValueAnnotation")

ExtendsListpercentages <- computePatternPercentages(conflictRateTemp, "ExtendsList")

#all conflicts percentages beanplot
BeanplotAllConflicts = paste("BeanplotAllConflicts.png")
png(paste(exportPath, BeanplotAllConflicts, sep=""))
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
colnames(allConflictsPercentage) <- c("EditSameMC","SameSignatureMC", "ImplementsList", "ModifierList", 
                                      "AddSameFd", "EditSameFd", "DefaultValueA", "ExtendsList")
op <- par(mar = c(2, 9, 1, 1) + 0.1) #adjust margins, default is c(5, 4, 4, 2) + 0.1
beanplot(allConflictsPercentage, col="green", horizontal = TRUE, las=1, cex.axis=1.1, bw="nrd0")
par(op)
dev.off()

#all conflicts percentages boxplot
BoxplotAllConflicts = paste("BoxplotAllConflicts.png")
png(paste(exportPath, BoxplotAllConflicts, sep=""))
op <- par(mar = c(2, 8, 1, 1) + 0.1) #adjust margins, default is c(5, 4, 4, 2) + 0.1
boxplot(allConflictsPercentage, col="green", horizontal = TRUE, las=1, cex.axis=1)
par(op)
dev.off()

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
slices <- c(DefaultValueAnnotation, ImplementList, ModifierList, EditSameMC, SameSignatureCM, AddSameFd, 
            EditSameFd, ExtendsList)
labels <- c("DefaultValueAnnotation", "ImplementList", "ModifierList", "EditSameMC", "SameSignatureCM", 
            "AddSameFd", "EditSameFd", "ExtendsList") 
par(las=2)
par(mar=c(5,8,4,2))
barplot(slices, main=name, horiz=TRUE, names.arg=labels, cex.names=0.8, col=c("darkviolet","chocolate4", 
                                                                              "darkgreen", "darkblue", "red" ,
                                                                              "darkgoldenrod2"))
dev.off()

#HTML code
library(R2HTML)

title = paste("<hr><h1>Results for Conflict Rate and Conflict Patterns Occurrences</h1>", sep="")
HTML("<link rel=stylesheet type=text/css href=R2HTML.css>", file=htmlFile, append=TRUE)
HTML.title(title, file=htmlFile, append=TRUE)

HTML("<hr><h2>Conflict Rate</h2>", file=htmlFile, append=TRUE)
HTML(conflictRate, file=htmlFile, append=TRUE)
HTML(metrics, file=htmlFile, append=TRUE)
HTML("<hr><h2>Conflict Rate Without False Positives</h2>", file=htmlFile, append=TRUE)
HTML(realconflictRate, file=htmlFile, append=TRUE)
HTML(realMetrics, file=htmlFile, append=TRUE)

HTML("<hr><h2>Conflict Rate Beanplot and Boxplot with and without false positives</h2>", file=htmlFile, 
     append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=realbeanplotCRFileName, Align="center", append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=boxplotCRFileName, Align="center", append=TRUE)

HTML("<hr><h2>Difference of Conflict Rates with and without false positives</h2>", file=htmlFile, append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=boxplotDiffCR, Align="center", append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=beanplotDiffCR, Align="center", append=TRUE)
HTML(diffConflictRatesTable, file=htmlFile, append=TRUE)


HTML("<hr><h2>Conflict Patterns Occurrences</h2>", file=htmlFile, append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=barChartFileName, Align="center", append=TRUE)

HTML("<hr><h2>Conflicts Table</h2>", file=htmlFile, append=TRUE)
HTML(conflictsTable, file=htmlFile, append=TRUE)


HTML("<hr><h2>Conflicts Patterns Occurrences Without the False Positives</h2>", file=htmlFile, append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=barChartFP, Align="center", append=TRUE)
HTML(realconflictsTable, file=htmlFile, append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=BeanplotAllConflicts, Align="center", append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=BoxplotAllConflicts, Align="center", append=TRUE)

HTML("<hr><h2>False Positives Occurences</h2>", file=htmlFile, append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=BarPlotESMCFP, Align="center", append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=BoxplotFPEditSameMC, Align="center", append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=BarPlotSSCMFP, Align="center", append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=BoxplotFPSameSigCM, Align="center", append=TRUE)

HTML("<hr><h2>Causes for SameSignatureCM occurrences</h2>", file=htmlFile, append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=BoxplotCSSCM, Align="center", append=TRUE)
HTMLInsertGraph(file=htmlFile, GraphFileName=BoxplotAllCauses, Align="center", append=TRUE)

time = Sys.time()
HTML("<hr><h2>Last Time Updated:</h2>", file=htmlFile, append=TRUE)
HTML(time, file=htmlFile, append=TRUE)

}

main()
