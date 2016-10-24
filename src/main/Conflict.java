package main;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

import de.ovgu.cide.fstgen.ast.FSTNode;
import de.ovgu.cide.fstgen.ast.FSTTerminal;
import merger.FSTGenMerger;
import util.StringSimilarity;

enum PatternSameSignatureCM {
	smallMethod, renamedMethod, copiedMethod, copiedFile, noPattern
}

public class Conflict {

	public static final String SSMERGE_SEPARATOR = "##FSTMerge##";

	public static final String DIFF3MERGE_SEPARATOR = "|||||||";

	private String type;

	private String body;

	private String filePath;

	private String nodeType;

	private int differentSpacing;

	private int consecutiveLines;

	private int numberOfConflicts;

	private int falsePositivesIntersection;

	private int possibleRenaming;

	private String causeSameSignatureCM;

	private ArrayList<String> conflicts;

	private double similarityThreshold;

	private String nodeName;

	private String[] shaFamily;

	public Conflict(FSTTerminal node, String path, String[] shaFamily) {
		this.body = node.getBody();
		this.nodeName = node.getName();
		this.nodeType = node.getType();
		this.conflicts = splitConflictsInsideMethods();
		this.countConflictsInsideMethods();
		this.matchPattern();
		this.retrieveFilePath(node, path);
		if (this.body.contains(DIFF3MERGE_SEPARATOR))
			this.checkFalsePositives();
		this.causeSameSignatureCM = "";
		this.similarityThreshold = 0.7;
		this.shaFamily = shaFamily;
	}

	public Conflict(String type) {
		this.type = type;
	}

	public String getCauseSameSignatureCM() {
		return causeSameSignatureCM;
	}

	public String getChildSHA() {
		return shaFamily[0];
	}

	public String getParent1SHA() {
		return shaFamily[1];
	}

	public String getParent2SHA() {
		return shaFamily[2];
	}

	public void setCauseSameSignatureCM(LinkedList<FSTNode> baseNodes, boolean fileAddedByBothDevs) {
		if (!fileAddedByBothDevs) {
			String[] splitConflictBody = this.splitConflictBody(this.conflicts.get(0));
			boolean isSmallMethod = this.isSmallMethod(splitConflictBody);
			if (!isSmallMethod) {
				this.isRenamedOrCopiedMethod(baseNodes, splitConflictBody);
			}
			if (this.causeSameSignatureCM.equals("")) {
				this.causeSameSignatureCM = PatternSameSignatureCM.noPattern.toString();
			}
		} else {
			this.causeSameSignatureCM = PatternSameSignatureCM.copiedFile.toString();
		}

	}

	private boolean isSmallMethod(String[] splitConflict) {
		boolean smallMethod = false;
		try {
			if (isGetOrSet()) {
				smallMethod = true;
				this.causeSameSignatureCM = PatternSameSignatureCM.smallMethod.toString();
			}

			if (splitConflict[0].equals("") && (splitConflict[2].split("\n").length <= 6)) {
				smallMethod = true;
				this.causeSameSignatureCM = PatternSameSignatureCM.smallMethod.toString();

			} else if ((splitConflict[0].split("\n").length <= 6) && (splitConflict[2].split("\n").length <= 6)) {
				smallMethod = true;
				this.causeSameSignatureCM = PatternSameSignatureCM.smallMethod.toString();
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}

		return smallMethod;
	}

	private boolean isGetOrSet() {
		boolean result = false;
		if (this.nodeName.contains("get") || this.nodeName.contains("set")) {
			result = true;
		}
		return result;
	}

	private void isRenamedOrCopiedMethod(LinkedList<FSTNode> baseNodes, String[] splitConflict) {

		double similarity = this.getSimilarity(splitConflict);

		if (similarity >= this.similarityThreshold) {
			boolean foundOnBaseNodes = this.checkBaseNodes(baseNodes, splitConflict[2]);
			if (!foundOnBaseNodes) {
				this.causeSameSignatureCM = PatternSameSignatureCM.copiedMethod.toString();
			}
		}
	}

	private double getSimilarity(String[] splitConflict) {
		double similarity = 0;
		if (!this.body.contains("|||||||")) {
			similarity = 1;
		} else {
			String[] input = this.removeInvisibleChars(splitConflict);
			similarity = StringSimilarity.similarity(input[0], input[2]);
		}
		return similarity;
	}

	private boolean checkBaseNodes(LinkedList<FSTNode> baseNodes, String right) {
		boolean found = false;
		int i = 0;
		right = right.replaceAll("\\s+", "");
		while (!found && i < baseNodes.size()) {
			FSTTerminal temp = (FSTTerminal) baseNodes.get(i);
			String base = temp.getBody().replaceAll("\\s+", "");
			double similarity = StringSimilarity.similarity(base, right);
			if (similarity >= this.similarityThreshold) {
				found = true;
				baseNodes.remove(i);
				this.causeSameSignatureCM = PatternSameSignatureCM.renamedMethod.toString();
			}
			i++;
		}

		return found;
	}

	public int getFalsePositivesIntersection() {
		return falsePositivesIntersection;
	}

	public void setFalsePositivesIntersection(int falsePositivesIntersection) {
		this.falsePositivesIntersection = falsePositivesIntersection;
	}

	public void checkFalsePositives() {

		if (conflicts.size() > 1) {
			for (String s : conflicts) {
				this.auxCheckFalsePositives(s);
			}
		} else {
			this.auxCheckFalsePositives(conflicts.get(0));

		}
	}

	private void auxCheckFalsePositives(String s) {
		String[] splitConflictBody = this.splitConflictBody(s);
		boolean diffSpacing = this.checkDifferentSpacing(splitConflictBody);

		if (this.type.equals(SSMergeConflicts.EditSameMC.toString())) {
			boolean consecLines = false;
			if (this.possibleRenaming == 0) {
				consecLines = this.checkConsecutiveLines(splitConflictBody);
			}

			if (diffSpacing && consecLines) {
				this.falsePositivesIntersection++;
			}
		}

	}

	private int isPossibleRenaming() {
		int result = 0;
		String[] splitBody = this.splitConflictBody(this.conflicts.get(0));

		if (!splitBody[1].equals("") && (splitBody[0].equals("") || splitBody[2].equals(""))) {
			result = 1;
		}
		return result;
	}

	private ArrayList<String> splitConflictsInsideMethods() {
		ArrayList<String> conflicts = new ArrayList<String>();
		if (this.body.contains("<<<<<<<") && this.body.contains(">>>>>>>")) {
			String[] temp = this.body.split("<<<<<<<");
			for (int i = 1; i < temp.length; i++) {
				String temp2 = temp[i].split(">>>>>>>")[0];
				conflicts.add(temp2);
			}
		} else {
			conflicts.add(this.body);
		}

		return conflicts;
	}

	public boolean checkDifferentSpacing(String[] splitConflictBody) {
		boolean falsePositive = false;

		String[] temp = splitConflictBody.clone();
		String[] threeWay = this.removeInvisibleChars(temp);
		if (!threeWay[1].equals("")) {
			if (threeWay[0].equals(threeWay[1]) || threeWay[2].equals(threeWay[1])) {
				this.differentSpacing++;
				falsePositive = true;
			}
		} else {

			if (threeWay[0].equals("") || threeWay[0].equals(threeWay[2])) {
				this.differentSpacing++;
				falsePositive = true;
			}

		}

		return falsePositive;
	}

	public String[] removeInvisibleChars(String[] input) {
		input[0] = input[0].replaceAll("\\s+", "");
		input[1] = input[1].replaceAll("\\s+", "");
		input[2] = input[2].replaceAll("\\s+", "");
		return input;
	}

	public boolean checkConsecutiveLines(String[] splitConflictBody) {
		boolean falsePositive = false;
		try {
			if (!splitConflictBody[0].equals("")
					&& (!splitConflictBody[0].equals("") && !splitConflictBody[2].equals(""))) {
				String[] leftLines = splitConflictBody[0].split("\n");
				String[] baseLines = splitConflictBody[1].split("\n");
				String[] rightLines = splitConflictBody[2].split("\n");
				if (!baseLines[0].equals("")) {
					String fixedElement = baseLines[0];
					boolean foundOnLeft = this.searchFixedElement(fixedElement, leftLines);
					if (foundOnLeft) {
						falsePositive = true;
						this.consecutiveLines++;
					} else {
						boolean foundOnRight = this.searchFixedElement(fixedElement, rightLines);
						if (foundOnRight) {
							falsePositive = true;
							this.consecutiveLines++;
						}
					}
				}

			}
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}

		return falsePositive;
	}

	private boolean searchFixedElement(String fixedElement, String[] variant) {
		boolean foundFixedElement = false;
		int i = 0;
		while (!foundFixedElement && i < variant.length) {
			if (variant[i].equals(fixedElement)) {
				foundFixedElement = true;
			}
			i++;
		}
		return foundFixedElement;
	}

	public String[] splitConflictBody(String s) {
		String[] splitBody = { "", "", "" };
		try {
			if (this.isMethodOrConstructor()) {
				if (s.contains("|||||||")) {
					String[] temp = s.split("\\|\\|\\|\\|\\|\\|\\|");

					String[] temp2 = temp[0].split("\n");
					splitBody[0] = extractLines(temp2);

					String[] baseRight = temp[1].split("=======");
					temp2 = baseRight[0].split("\n");
					splitBody[1] = extractLines(temp2);
					temp2 = baseRight[1].split("\n");
					splitBody[2] = extractLines(temp2);
				}// else {
					//splitBody[1] = "";
					//splitBody[0] = extractLines(s.split("=======")[0].split("\n"));
					//splitBody[2] = extractLines(s.split("=======")[1].split("\n"));
				//}

			} else {
				String[] tokens = body.split(FSTGenMerger.MERGE_SEPARATOR);
				splitBody[0] = tokens[0].replace(FSTGenMerger.SEMANTIC_MERGE_MARKER, "").trim();
				splitBody[1] = tokens[1].trim();
				if (tokens.length == 3) {
					splitBody[2] = tokens[2].trim();
				}

			}
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		return splitBody;
	}

	private String extractLines(String[] conflict) {
		String lines = "";
		if (conflict.length > 1) {
			for (int i = 1; i < conflict.length; i++) {
				if (i != conflict.length - 1) {
					lines = lines + conflict[i] + "\n";
				} else {
					lines = lines + conflict[i];
				}

			}

		}
		return lines;
	}

	public void matchPattern() {

		String nodeType = this.nodeType;
		String conflictType = "";

		if (nodeType.equals("Modifiers")) {

			conflictType = SSMergeConflicts.ModifierList.toString();

		} else if (nodeType.equals("AnnotationMethodDecl")) {

			conflictType = SSMergeConflicts.DefaultValueAnnotation.toString();

		} else if (nodeType.equals("ImplementsList")) {

			conflictType = SSMergeConflicts.ImplementList.toString();

		} else if (nodeType.equals("FieldDecl")) {

			conflictType = this.setFieldDeclPattern();

		} else if (isMethodOrConstructor()) {

			conflictType = this.setMethodPattern();

		} else if (nodeType.equals("ExtendsList")) {

			conflictType = SSMergeConflicts.ExtendsList.toString();
		}

		if (conflictType.equals("")) {
			conflictType = SSMergeConflicts.NOPATTERN.toString();
		}

		this.setType(conflictType);

	}

	public boolean isMethodOrConstructor() {
		boolean result = nodeType.equals("MethodDecl") || nodeType.equals("ConstructorDecl");
		return result;
	}

	public String setFieldDeclPattern() {

		String type = "";
		String[] fd;
		if (this.body.contains(SSMERGE_SEPARATOR))
			fd = this.body.split(Conflict.SSMERGE_SEPARATOR);
		else
			fd = this.body.split(Conflict.DIFF3MERGE_SEPARATOR);

		try {
			if (fd[1].equals(" ")) {

				type = SSMergeConflicts.AddSameFd.toString();

			} else {
				type = SSMergeConflicts.EditSameFd.toString();
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		return type;

	}

	public String setMethodPattern() {

		String type = "";

		if (isInsideMethod()) {
			type = SSMergeConflicts.EditSameMC.toString();

		} else {
			type = this.matchConflictOutsideMethod();
		}

		return type;

	}

	public boolean isInsideMethod() {

		boolean isInsideMethod = false;

		if (this.numberOfConflicts > 1) {
			isInsideMethod = true;
		} else {
			try {
				String[] p1, p2;
				if (this.body.contains(DIFF3MERGE_SEPARATOR)) {
					p1 = this.body.split("<<<<<<<");
					p2 = this.body.split(">>>>>>>");
				} else {
					return false;
				}
				String[] p3 = p2[1].split("\n");

				if (!p1[0].equals("") && p3.length > 1) {

					isInsideMethod = true;
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				e.printStackTrace();
			}
		}

		return isInsideMethod;
	}

	private String matchConflictOutsideMethod() {
		String type = "";
		try {
			if (this.body.contains("|||||||")) {
				String[] p1 = this.body.split("\\|\\|\\|\\|\\|\\|\\|");
				String[] p2 = p1[1].split("=======");
				String[] a = p2[0].split("\n");

				if (a.length > 1) {

					type = SSMergeConflicts.EditSameMC.toString();
					if (this.numberOfConflicts == 1) {
						this.possibleRenaming = this.isPossibleRenaming();
					}
				} else {

					type = SSMergeConflicts.SameSignatureCM.toString();

				}
			} else {
				String[] a1 = this.body.split("=======");
				String[] a2 = a1[0].split("\n");
				if (a2.length > 1) {
					type = SSMergeConflicts.EditSameMC.toString();
				} else {
					type = SSMergeConflicts.SameSignatureCM.toString();
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		return type;
	}

	public void retrieveFilePath(FSTNode node, String path) {

		int endIndex = path.length() - 10;
		String systemDir = path.substring(0, endIndex);

		this.filePath = systemDir + this.retrieveFolderPath(node);
	}

	public String retrieveFolderPath(FSTNode node) {
		String filePath = "";
		String nodetype = node.getType();

		if (nodetype.equals("Java-File") || nodetype.equals("Folder")) {

			filePath = this.retrieveFolderPath(node.getParent()) + File.separator + node.getName();

			return filePath;

		} else if (nodetype.equals("Feature")) {

			return "";

		} else {

			return this.retrieveFolderPath(node.getParent());
		}

	}

	public void countConflictsInsideMethods() {
		String[] p = this.body.split("<<<<<<<");
		if (p.length > 1) {
			this.numberOfConflicts = p.length - 1;
		} else {
			this.numberOfConflicts = 1;
		}

	}

	public int getNumberOfTruePositives() {
		int truePositives = this.numberOfConflicts - this.differentSpacing - this.consecutiveLines
				+ this.falsePositivesIntersection;

		return truePositives;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getNodeType() {
		return nodeType;
	}

	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}

	public int getNumberOfConflicts() {
		return numberOfConflicts;
	}

	public void setNumberOfConflicts(int numberOfConflicts) {
		this.numberOfConflicts = numberOfConflicts;
	}

	public int getDifferentSpacing() {
		return differentSpacing;
	}

	public void setDifferentSpacing(int differentSpacing) {
		this.differentSpacing = differentSpacing;
	}

	public int getConsecutiveLines() {
		return consecutiveLines;
	}

	public void setConsecutiveLines(int consecutiveLines) {
		this.consecutiveLines = consecutiveLines;
	}

	public int getPossibleRenaming() {
		return possibleRenaming;
	}

	public void setPossibleRenaming(int possibleRenaming) {
		this.possibleRenaming = possibleRenaming;
	}

	public static void main(String[] args) {
		/*
		 * String example = "public void m(){\n" +
		 * "<<<<<<< /Users/paolaaccioly/Desktop/Teste/jdimeTests/left/Example.java\n"
		 * + "        int a1;\n" +
		 * "||||||| /Users/paolaaccioly/Desktop/Teste/jdimeTests/base/Example.java\n"
		 * + "        int a;\n" + "=======\n" + "            int a;\n" +
		 * ">>>>>>> /Users/paolaaccioly/Desktop/Teste/jdimeTests/right/Example.java\n"
		 * + "        int b;\n" + "        int c;\n" +
		 * "<<<<<<< /Users/paolaaccioly/Desktop/Teste/jdimeTests/left/Example.java\n"
		 * + "        int d1;\n" +
		 * "||||||| /Users/paolaaccioly/Desktop/Teste/jdimeTests/base/Example.java\n"
		 * + "        int d;\n" + "=======\n" + "        int d2;\n" +
		 * ">>>>>>> /Users/paolaaccioly/Desktop/Teste/jdimeTests/right/Example.java\n"
		 * + "    }"; String example2 = "hello world";
		 * System.out.println(example2.split("mamae")[0]);
		 */
		/*
		 * String s =
		 * "<<<<<<< /Users/paolaaccioly/Documents/testeConflictsAnalyzer/conflictsAnalyzer/fstmerge_tmp1437435093749/fstmerge_var1_6882939852718786152\n"
		 * + "		int x;" +
		 * "||||||| /Users/paolaaccioly/Documents/testeConflictsAnalyzer/conflictsAnalyzer/fstmerge_tmp1437435093749/fstmerge_base_7436445259957106246\n"
		 * + "=======\n" + "		int y;\n"+
		 * ">>>>>>> /Users/paolaaccioly/Documents/testeConflictsAnalyzer/conflictsAnalyzer/fstmerge_tmp1437435093749/fstmerge_var2_5667963733764531246\n"
		 * ;
		 */
	}

}
