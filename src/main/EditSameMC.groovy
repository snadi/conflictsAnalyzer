package main

import java.util.List

import com.ibm.wala.shrikeBT.info.ThisAssignmentChecker;

import java.io.File


import de.ovgu.cide.fstgen.ast.FSTNode
import de.ovgu.cide.fstgen.ast.FSTNonTerminal
import de.ovgu.cide.fstgen.ast.FSTTerminal
import merger.FSTGenMerger
import util.Util

class EditSameMC extends ConflictPredictor{
	
	public EditSameMC(FSTTerminal n, String path){
		super(n, path)
		
	}

}
