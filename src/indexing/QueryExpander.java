package indexing;

import java.io.*;
import java.util.*;

import org.apache.lucene.analysis.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryParser.*;
import org.apache.lucene.search.*;
import org.apache.lucene.util.Version;

/**
 * Doing the query expansion using Rocchio's algorithm
 * 
 * The expansion is following such formula: new query = alpha*query + beta /
 * total amount of retrieved documents * sum( document * (1 - decay* rank) )
 * 
 * @author Zheyun Feng - fengzhey@msu.edu
 *
 */

public class QueryExpander {

	public static final int TERM_NUM= 20;
	

	private Analyzer analyzer;
	private Searcher searcher;
	private Similarity similarity;
	private Vector<TermQuery> expandedTerms;
	private String field;
	private float alpha;
	private float beta;
	private float gama;
	private int decay;

	/*
	 * Define the query expansion structure. The input of similarity is usually
	 * for Lucene's default scoring method.
	 * 
	 * @param analyzer: analyzer used to sparse the input query text
	 * 
	 * @param searcher: sercher used to search specific query
	 * 
	 * @param similarity: similarity between a query and every document in the
	 * collection. Only if the similarity is input, the expanded terms will be
	 * weighted using tf*idf, otherwise, just tf.
	 * 
	 * @param prop: the set of parameters used in the expansion
	 */
	public QueryExpander(Analyzer analyzer, Searcher searcher, Similarity similarity) {
		this.analyzer = analyzer;
		this.searcher = searcher;
		this.similarity = similarity;
	}

	public QueryExpander(Analyzer analyzer, Similarity similarity, Searcher searcher, final String field, float alpha,
			float beta, float gama, int decay) {
		this.analyzer = analyzer;
		this.field = field;
		this.alpha = alpha;
		this.beta = beta;
		this.gama = gama;
		this.decay = decay;
		this.similarity = similarity;
		this.searcher = searcher;
	}

	/*
	 * Define the query expansion structure. The input of similarity is usually
	 * for okapi scoring method.
	 * 
	 * @param analyzer: analyzer used to sparse the input query text
	 * 
	 * @param searcher: sercher used to search specific query
	 * 
	 * @param prop: the set of parameters used in the expansion
	 */
	public QueryExpander(Analyzer analyzer, Searcher searcher) {
		this.analyzer = analyzer;
		this.searcher = searcher;
		this.similarity = null;

	}

	/*
	 * parse the parameters, and regulate the expansion
	 */
	public Query expandQuery(String queryStr, Vector<TermFreqVector> relevantDocs, Vector<TermFreqVector> noRelevantDocs) throws IOException, ParseException {
		// Load Necessary Values from Properties (Input)

		int docNum = relevantDocs.size()+ noRelevantDocs.size();
		int termNum = TERM_NUM;

		// Create combine documents term vectors from the retrieved documents in
		// the first time
		// sum ( real term vectors * (1-decay * rank) )
		Vector<QueryTermVector> relevantDocsTermVector = getDocsTerms(relevantDocs, relevantDocs.size(), analyzer);
		Vector<QueryTermVector> noRelevantdocsTermVector = getDocsTerms(noRelevantDocs, noRelevantDocs.size(), analyzer);

		// Adjust term features of the docs with alpha * query; and beta; and
		// assign weights/boost to terms (tf*idf)
		Query expandedQuery = adjust(relevantDocsTermVector,noRelevantdocsTermVector, queryStr, alpha, beta, gama,decay, docNum, termNum);

		return expandedQuery;
	}

	/*
	 * When the input document is input in the format of vectors, there's no
	 * need to regulate the documents before combine them. Load parameters and
	 * expand query
	 */
	/*
	 * public Query expandQuery(Vector<QueryTermVector> docsTermVector, String
	 * queryStr, Properties prop) throws IOException, ParseException { // Load
	 * Necessary Values from Properties float alpha =
	 * Float.valueOf(prop.getProperty(QueryExpander.ROCCHIO_ALPHA_FLD)).
	 * floatValue(); float beta =
	 * Float.valueOf(prop.getProperty(QueryExpander.ROCCHIO_BETA_FLD)).
	 * floatValue(); float decay =
	 * Float.valueOf(prop.getProperty(QueryExpander.DECAY_FLD,
	 * "0.0")).floatValue(); int docNum =
	 * Integer.valueOf(prop.getProperty(QueryExpander.DOC_NUM_FLD)).intValue();
	 * int termNum =
	 * Integer.valueOf(prop.getProperty(QueryExpander.TERM_NUM_FLD)).intValue();
	 * 
	 * Query expandedQuery = adjust(docsTermVector, queryStr, alpha, beta,
	 * decay, docNum, termNum);
	 * 
	 * return expandedQuery; }
	 */

	


	/*
	 * Adjust term features of the docs with alpha * query; and beta; and assign
	 * weights/boost to terms (tf*idf).
	 *
	 * @param docsTermsVector of the terms of the top <code> docsRelevantCount
	 * </code> documents returned by original query
	 * 
	 * @param queryStr - that will be expanded
	 * 
	 * @param alpha - factor of the equation
	 * 
	 * @param beta - factor of the equation
	 * 
	 * @param docsRelevantCount - number of the top documents to assume to be
	 * relevant
	 * 
	 * @param maxExpandedQueryTerms - maximum number of terms in expanded query
	 * 
	 * @return expandedQuery with boost factors adjusted using Rocchio's
	 * algorithm
	 *
	 * @throws IOException
	 * 
	 * @throws ParseException
	 */
	

	public Query  adjust(Vector<QueryTermVector> relevantDocsTermsVector,
			Vector<QueryTermVector> noRelevantDocsTermsVector, String queryStr, float alpha, float beta, float gama,
			float decay, int docsRelevantCount,int maxExpandedQueryTerms) throws IOException, ParseException {
		   Query expandedQuery;
		// setBoost of docs terms
		beta = beta / relevantDocsTermsVector.size();
		Vector<TermQuery> relevantDocsTerms = setBoost(relevantDocsTermsVector, beta, decay);
		gama = gama/noRelevantDocsTermsVector.size();
		Vector<TermQuery> noRelevantDocsTerms = setBoost(noRelevantDocsTermsVector, gama, decay);
	
		// Get queryTerms from the query, setBoost of query terms
		QueryTermVector queryTermsVector = new QueryTermVector(queryStr, analyzer);
		Vector<TermQuery> queryTerms = setBoost(queryTermsVector, alpha);

		// combine weights according to expansion formula
		Vector<TermQuery> expandedQueryTerms = combine(queryTerms, relevantDocsTerms);		
		expandedQueryTerms = combineNoRelevant(expandedQueryTerms, noRelevantDocsTerms);
		setExpandedTerms(expandedQueryTerms);

		// Sort by boost=weight
		Comparator<Query> comparator = new QueryBoostComparator();
		Collections.sort(expandedQueryTerms, comparator);

		 // Create Expanded Query
        expandedQuery = mergeQueries( expandedQueryTerms, maxExpandedQueryTerms );
        
        return expandedQuery;
	}

	/*
	 * Merges query terms into a single query with each term appending a weight
	 * 
	 * @param termQueries - to merge
	 *
	 * @return query created from termQueries including boost parameters
	 */
	public Query mergeQueries(Vector<TermQuery> termQueries, int maxTerms) throws ParseException {
		Query query;

		// Select only the maxTerms number of terms
		int termCount = Math.min(termQueries.size(), maxTerms);

		// Create Query String
		StringBuffer qBuf = new StringBuffer();
		for (int i = 0; i < termCount; i++) {
			TermQuery termQuery = termQueries.elementAt(i);
			Term term = termQuery.getTerm();
			qBuf.append(term.text() + "^" + termQuery.getBoost() + " ");
		}

		// Parse StringQuery to create Query
		query = new QueryParser(Version.LUCENE_30, field, analyzer).parse(qBuf.toString());

		return query;
	}

	/*
	 * Extracts terms from the first retrieved documents
	 *
	 * @param doc - from which to extract terms
	 * 
	 * @param docsRelevantCount - number of the top documents to assume to be
	 * relevant
	 * 
	 * @param analyzer - to extract terms
	 */
	public Vector<QueryTermVector> getDocsTerms(Vector<TermFreqVector> hits, int docsRelevantCount, Analyzer analyzer)
			throws IOException {
		Vector<QueryTermVector> docsTerms = new Vector<QueryTermVector>();

		// Process each of the documents
		for (int i = 0; ((i < docsRelevantCount) && (i < hits.size())); i++) {
			TermFreqVector vector = hits.elementAt(i);

			// Get text of the document and append it
			StringBuffer docTxtBuffer = new StringBuffer();
			String[] docTxtFlds = vector.getTerms();
			int[] frequencies = vector.getTermFrequencies();
			for (int j = 0; j < docTxtFlds.length; j++) {
				for (int k = 0; k < frequencies[j]; k++) {
					docTxtBuffer.append(docTxtFlds[j] + " ");
				}
			}

			// Create termVector and add it to vector
			QueryTermVector docTerms = new QueryTermVector(docTxtBuffer.toString(), analyzer);
			docsTerms.add(docTerms);
		}

		return docsTerms;
	}

	/*
	 * Set boost for the original query
	 */
	public Vector<TermQuery> setBoost(QueryTermVector termVector, float factor) throws IOException {
		Vector<QueryTermVector> v = new Vector<QueryTermVector>();
		v.add(termVector);

		return setBoost(v, factor, 0);
	}

	/*
	 * Set boost according to the input parameters in "prop"
	 */
	public Vector<TermQuery> setBoost(Vector<QueryTermVector> docsTerms, float factor, float decayFactor)
			throws IOException {
		Vector<TermQuery> terms = new Vector<TermQuery>();

		// setBoost for each of the terms of each of the docs
		for (int g = 0; g < docsTerms.size(); g++) {
			// Extract terms from existing documents
			QueryTermVector docTerms = docsTerms.elementAt(g);
			String[] termsTxt = docTerms.getTerms();
			int[] termFrequencies = docTerms.getTermFrequencies();

			// Increase decay according to the rank of the document
			float decay = decayFactor * g;

			// Populate terms: with TermQuries and set boost
			for (int i = 0; i < docTerms.size(); i++) {
				// Create Term
				String termTxt = termsTxt[i];
				Term term = new Term(field, termTxt);

				// Calculate weight
				float tf = termFrequencies[i];
				float weight = tf; // if no similarity is input when request
									// query expansion

				if (similarity != null)// else, similarity is input, the
										// original boost is tf*idf
				{
					float idf = similarity.idfExplain(term, searcher).getIdf(); // okapi
																				// without
																				// idf;
																				// lucene:with
																				// okapi
					weight = weight * idf;
				}

				// Adjust weight by decay factor
				weight = weight - (weight * decay);
				if (weight < 0) {
					continue;
				}

				// Create TermQuery and add it to the collection
				TermQuery termQuery = new TermQuery(term);
				// Calculate and set boost
				termQuery.setBoost(factor * weight);
				terms.add(termQuery);
			}
		}

		// Get rid of duplicates by merging termQueries with equal terms
		merge(terms);

		return terms;
	}

	/*
	 * Remove duplicates by merging termQueries with equal terms
	 * 
	 * @param terms
	 */
	private void merge(Vector<TermQuery> terms) {
		for (int i = 0; i < terms.size(); i++) {
			TermQuery term = terms.elementAt(i);
			// Check through terms and if term is equal then merge: combine the
			// boost and delete one of the terms
			for (int j = i + 1; j < terms.size(); j++) {
				TermQuery tmpTerm = terms.elementAt(j);

				// If equal then merge
				if (tmpTerm.getTerm().text().equals(term.getTerm().text())) {
					// Add boost factors of terms
					term.setBoost(term.getBoost() + tmpTerm.getBoost());

					// delete duplicated term
					terms.remove(j);
					// decrement j so that term is not skipped
					j--;
				}
			}
		}
	}

	/*
	 * combine weights according to expansion formula
	 */
	public Vector<TermQuery> combine(Vector<TermQuery> queryTerms, Vector<TermQuery> docsTerms) {
		Vector<TermQuery> terms = new Vector<TermQuery>();
		// Add Terms from the docsTerms
		terms.addAll(docsTerms);

		// Add Terms from queryTerms. If term already exists just combine their
		// boosts
		for (int i = 0; i < queryTerms.size(); i++) {
			TermQuery qTerm = queryTerms.elementAt(i);
			TermQuery term = find(qTerm, terms);

			// Term already exists update its boost
			if (term != null) {
				float weight = qTerm.getBoost() + term.getBoost();
				term.setBoost(weight);
			}
			// Term does not exist, add it
			else {
				terms.add(qTerm);
			}
		}

		return terms;
	}
	
	public Vector<TermQuery> combineNoRelevant(Vector<TermQuery> queryTerms, Vector<TermQuery> noRelevantdocsTerms) {
		Vector<TermQuery> terms = new Vector<TermQuery>();
		// Add Terms from the docsTerms
		terms.addAll(noRelevantdocsTerms);

		// Add Terms from queryTerms. If term already exists just combine their
		// boosts
		for (int i = 0; i < queryTerms.size(); i++) {
			TermQuery qTerm = queryTerms.elementAt(i);
			TermQuery term = find(qTerm, terms);

			// Term already exists update its boost
			if (term != null) {
				float weight = qTerm.getBoost() - term.getBoost();
				term.setBoost(weight);
			}
			// Term does not exist, add it
			else {
				terms.add(qTerm);
			}
		}
		return terms;
	}

	/*
	 * Find out duplicated terms
	 */
	public TermQuery find(TermQuery term, Vector<TermQuery> terms) {
		TermQuery termF = null;

		// using loop to check every term
		Iterator<TermQuery> iterator = terms.iterator();
		while (iterator.hasNext()) {
			TermQuery currentTerm = iterator.next();
			if (term.getTerm().equals(currentTerm.getTerm())) {
				termF = currentTerm;
			}
		}

		return termF;
	}

	/*
	 * Truncate the list of terms, the number of terms is set in "prop"
	 */
	public Vector<TermQuery> getExpandedTerms(int termNum) {
		Vector<TermQuery> terms = new Vector<TermQuery>();

		// Return only necessary number of terms
		List<TermQuery> list = this.expandedTerms.subList(0, termNum);
		terms.addAll(list);

		return terms;
	}

	/*
	 * return expanded terms
	 */
	private void setExpandedTerms(Vector<TermQuery> expandedTerms) {
		this.expandedTerms = expandedTerms;
	}

	class QueryBoostComparator implements Comparator<Query> {

		/*
		 * Compares queries based according to their boosts
		 */
		public int compare(Query q1, Query q2) {
			if (q1.getBoost() > q2.getBoost()) {
				return -1;
			} else if (q1.getBoost() < q2.getBoost()) {
				return 1;
			} else {
				return 0;
			}
		}

	}

}