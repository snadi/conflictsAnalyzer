#to do list:
#place new column with conflict rate percentage

importPath = "/Users/paolaaccioly/Documents/Doutorado/conflictsStudy/script_R/"
exportPath = "/Users/paolaaccioly/Dropbox/Public/conflictpattern/conflictResults.html"

conflictRateFile="dadosConflitos.dat"
#conflictPatternFile=""

conflictRate = read.table(file=paste(importPath, conflictRateFile, sep=""), header=T)
attach(conflictRate)

#HTML file
htmlFile = paste(exportPath, sep="")

if (file.exists(htmlFile)) {
    file.remove(htmlFile)
  }


#HTML code
library(R2HTML)

title = paste("<hr><h1>Results for Conflict Rate and Conflict Patterns Occurrences</h1>", sep="")

HTML.title(title, file=htmlFile, append=TRUE)

HTML("<hr><h2>Conflict Rate</h2>", file=htmlFile, append=TRUE)
HTML(conflictRate, file=htmlFile, append=TRUE)

