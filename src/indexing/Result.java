package indexing;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;

public class Result {

	private String fileName;
	private String methodName;
	private String methodDeclaration;
	private String Path;
	private float score;
	private Document document;
	private ScoreDoc hit;
	int parameters;

	public ScoreDoc getHit() {
		return hit;
	}

	public void setHit(ScoreDoc hit) {
		this.hit = hit;
	}

	public Result(String fileName, String path) {		
		this.fileName = fileName;
		Path = path;
		//check = new Button(,SWT.CHECK);
	}

	public float getScore() {
		return score;
	}

	public void setScore(float score) {
		this.score = score;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getPath() {
		return Path;
	}

	public void setPath(String path) {
		Path = path;
	}
	
	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}
	
	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getMethodDeclaration() {
		return methodDeclaration;
	}

	public void setMethodDeclaration(String methodDeclaration) {
		this.methodDeclaration = methodDeclaration;
	}

	public int getParameters() {
		return parameters;
	}

	public void setParameters(int parameters) {
		this.parameters = parameters;
	}

	@Override
	public String toString() {
		return  fileName + "." + methodName + "\t   " + score;
	}
	
	


	
}
