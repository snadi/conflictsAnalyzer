package conflictsAnalyzer;


enum SSMergeConflicts {

	ModifierList, DefaultValueAnnotation, ImplementList, LineBasedMCFd, SameIdFd, SameSignatureCM

}

public  class Conflict {

	private String type;

	private String body;

	private String filePath;

	private String nodeType;

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


}
