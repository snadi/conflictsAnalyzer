package main

public class ConflictSummary {


	public static String printConflictsSummary(HashMap<String, Conflict> projectSummary){
		String result = ''

		String noPattern = SSMergeConflicts.NOPATTERN.toString()
		for(SSMergeConflicts c : SSMergeConflicts.values()){
			String type = c.toString()
			Conflict conflict = projectSummary.get(type)
			result = result + conflict.getNumberOfConflicts() + ' '
			if(!type.equals(noPattern)){
				result = result + conflict.getDifferentSpacing() + ' ' +
						conflict.getConsecutiveLines() + ' ' + conflict.getFalsePositivesIntersection() +
						' '
			}
		}

		return result.trim()
	}

	public static HashMap<String, Conflict> initializeConflictsSummary(){
		HashMap<String, Conflict> conflictSummary = new HashMap<String, Conflict>()
		for(SSMergeConflicts c : SSMergeConflicts.values()){

			String type = c.toString();
			conflictSummary.put(type, new Conflict(type))
		}
		return conflictSummary
	}

	public static HashMap<String, Conflict> updateConflictsSummary(HashMap<String, Conflict> projectSummary, Conflict conflict){
			
			String conflictType = conflict.getType()
			Conflict c2 = projectSummary.get(conflictType)
			
			//get new values
			int numberOfConflicts = conflict.getNumberOfConflicts() + c2.getNumberOfConflicts()
			int differentSpacing = conflict.getDifferentSpacing() + c2.getDifferentSpacing()
			int consecutiveLines = conflict.getConsecutiveLines() + c2.getConsecutiveLines()
			int falsePositivesIntersection = conflict.falsePositivesIntersection +
			c2.getFalsePositivesIntersection()
			
			//set new values
			c2.setNumberOfConflicts(numberOfConflicts)
			c2.setDifferentSpacing(differentSpacing)
			c2.setConsecutiveLines(consecutiveLines)
			c2.setFalsePositivesIntersection(falsePositivesIntersection)
			
			return projectSummary
			
	}

}
