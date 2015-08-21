computeMensConjecture <-function(projectNames, importPath){
    fmpercentages <- c()
    fwcpercentages <- c()
    fwc_fmpercentages <- c()
    
    numberOfProjects <- length(projectNames)
    
    for(i in 1:numberOfProjects){
        projectName = projectNames[i]
        mergeReportFile <- paste(importPath, "ResultData/",projectName , "/MergeScenariosReport.csv", sep="")
        mergeReport <- read.table(file=mergeReportFile, header=T, sep=",")
        numberOfMS <- nrow(mergeReport)
        for (j in 1:numberOfMS){
            totalFiles <- mergeReport[j,2] + mergeReport[j,5]
            filesmerged <- mergeReport[j,6]
            filesWithConflicts <- mergeReport[j,7]
            
            if(totalFiles == 0){
                fm <- 0
                fwc <- 0
            }else{
                fm <- (filesmerged/totalFiles)*100
                fwc <- (filesWithConflicts/totalFiles)*100
            }
            fmpercentages <- append(fmpercentages, fm)
            fwcpercentages <- append(fwcpercentages, fwc)
            
            if(filesmerged == 0){
              percentage <- 0
            }else{
              percentage <- (filesWithConflicts/filesmerged)*100
            }
            
            fwc_fmpercentages  <- append(fwc_fmpercentages, percentage)
            
        }
    }
    result <- data.frame(fmpercentages, fwcpercentages, fwc_fmpercentages)
    colnames(result) <- c("Files_merged", "Files_with_conflicts", "Fwc_Fm")
    return (result)
}

main <-function(){
    importPath = "/Users/paolaaccioly/Documents/testeConflictsAnalyzer/conflictsAnalyzer/"
    exportPath = "/Users/paolaaccioly/Dropbox/Public/conflictpattern/"
    conflictRateFile="projectsPatternData.csv"
    
    conflictRateTemp = read.table(file=paste(importPath, conflictRateFile, sep=""), header=T, sep=",")
    
    #mens conjecture analysis
    mens <- computeMensConjecture(conflictRateTemp$Project, importPath)
    
    filesMergedGreaterThan10 <- sum(mens$Files_merged > 10)
    meanFM <- mean(mens$Files_merged)
    sdFM <- sd(mens$Files_merged)
    
    library(beanplot)
    
    #beanplot files merged
    files_merged = paste("files_merged.pdf")
    pdf(paste(exportPath, files_merged, sep=""))
    beanplot(mens$Files_merged, xlab="Files Merged %",col="green", bw="nrd0")
    dev.off()
    
    filesWithConflictsGreaterThan10 <- sum(mens$Files_with_conflicts > 10)
    meanFM <- mean(mens$Files_with_conflicts)
    sdFM <- sd(mens$Files_with_conflicts)
    
    #beanplot files with conflicts
    files_with_conflicts = paste("files_with_conflicts.pdf")
    pdf(paste(exportPath, files_with_conflicts, sep=""))
    beanplot(mens$Files_with_conflicts, xlab="Files With Conflicts %",col="green", bw="nrd0")
    dev.off()
    
    #fwc/fm analysis
    mean <- mean(mens$Fwc_Fm)
    sd <- sd (mens$Fwc_Fm)
    
    #beanplot files merged
    fwc_fm = paste("fwc_fm.pdf")
    pdf(paste(exportPath, fwc_fm, sep=""))
    beanplot(mens$Fwc_Fm, xlab="Files With Conflicts/Files Merged %",col="green", bw="nrd0")
    dev.off()
    
    
}

main()


