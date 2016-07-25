package indexing;

import org.apache.lucene.document.Document;

public class Result {

	private String fileName;
	private String Path;
	private float score;
	private Document document;

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

	
}
