package indexing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.osgi.service.prefs.Preferences;

public class FileSearcher {

	private static final String FIELD_NAME = "contents";
	private IndexSearcher searcher;
	private Analyzer analyzer;
	private static File indexDir;

	public static File getIndexDir() {
		return indexDir;
	}

	public static void setIndexDir(File indexDir) {
		FileSearcher.indexDir = indexDir;
	}

	public FileSearcher() throws CorruptIndexException, IOException {
		Preferences preferences = ConfigurationScope.INSTANCE.getNode("IRRFTest");
		Preferences sub1 = preferences.node("index");
		String index = sub1.get("indexFile","c:/index");
		System.out.println("index= " + index);
		indexDir = new File(index);
		analyzer = new StandardAnalyzer(Version.LUCENE_30, FileIndexer.getJavaStopWords());
	}

	public Query buildQuery(String queryStr) throws ParseException {
		QueryParser parser = new QueryParser(Version.LUCENE_30, FIELD_NAME, analyzer);
		return parser.parse(queryStr);
	}

	public ArrayList<Result> searchIndex(Query query, int maxHits) throws IOException, FileNotFoundException {
		ArrayList<Result> results = new ArrayList<>();
		Directory directory = FSDirectory.open(indexDir);
		System.out.println("indexDir: " +indexDir.getAbsolutePath());
		searcher = new IndexSearcher(directory);
		System.out.println(query.toString());		
		TopDocs topDocs = searcher.search(query, maxHits);
		ScoreDoc[] hits = topDocs.scoreDocs;
		for (int i = 0; i < hits.length; i++) {
			int docId = hits[i].doc;
			Document d = searcher.doc(docId);

			File f = new File(d.get("filename"));
			Result result = new Result(f.getName().replaceAll(".java", ""), f.getAbsolutePath());
			result.setScore(hits[i].score);
			result.setDocument(d);
			result.setHit(hits[i]);
			if (result.getScore() > 0) {
				results.add(result);
			}
		}
		System.out.println("Found " + hits.length);
		return results;
	}

	public Query expand(Query original, float alpha, float beta, float gama, List<Document> relevantDocs)
			throws CorruptIndexException, LockObtainFailedException, ParseException, IOException {
		RocchioExpander rocchio = new RocchioExpander(analyzer, "contents", alpha, beta, gama, 50, 50);
		return rocchio.expandRocchio(original, relevantDocs);
	}

	public Query expand(Query original, float alpha, float beta, float gama, int decay, List<ScoreDoc> relevantDocs,
			List<ScoreDoc> noRelevantDocs)
					throws CorruptIndexException, LockObtainFailedException, ParseException, IOException {
		Vector<TermFreqVector> relevantDocuments = new Vector<TermFreqVector>();
		Vector<TermFreqVector> noRelevantDocuments = new Vector<TermFreqVector>();
		System.out.println("relevant docs hits size: " + relevantDocs.size());
		Directory dir = FSDirectory.open(indexDir);
		IndexReader indexReader = IndexReader.open(dir);
		for (ScoreDoc hit : relevantDocs) {
			TermFreqVector vector = indexReader.getTermFreqVector(hit.doc, FIELD_NAME);
			relevantDocuments.add(vector);
		}
		for (ScoreDoc hit : noRelevantDocs) {
			TermFreqVector vector = indexReader.getTermFreqVector(hit.doc, FIELD_NAME);
			noRelevantDocuments.add(vector);
		}
		indexReader.close();
		QueryExpander queryExpander = new QueryExpander(analyzer, original.getSimilarity(searcher), searcher,
				FIELD_NAME, alpha, beta, gama, decay);
		return queryExpander.expandQuery(original.toString(), relevantDocuments, noRelevantDocuments);
	}

}
