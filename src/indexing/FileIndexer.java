package indexing;


import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class FileIndexer {

	private static final String[] JAVA_STOP_WORDS = {
		    "public","private","protected","interface",
		    "abstract","implements","extends","null","new",
		    "switch","case", "default" ,"synchronized" ,
		    "do", "if", "else", "break","continue","this",
		    "assert" ,"for","instanceof", "transient",
		    "final", "static" ,"void","catch","try",
		    "throws","throw","class", "finally","return",
		    "const" , "native", "super","while", "import",
		    "package" ,"true", "false" };
	
	public static CharArraySet getJavaStopWords(){
		ArrayList<String> javaStopWords = new ArrayList<>();
    	for (int i = 0; i < JAVA_STOP_WORDS.length; i++) {
			javaStopWords.add(JAVA_STOP_WORDS[i]);
		}
    	return new CharArraySet(javaStopWords, false);
	}
	
	
    /*public static void main(String[] args) throws Exception {

        File indexDir = new File("c:/index/");
        File dataDir = new File("D:\\Mis documentos\\NetBeansProjects");
        String suffix = "java";

        FileIndexer indexer = new FileIndexer();

        int numIndex = indexer.index(indexDir, dataDir, suffix);

        System.out.println("Total files indexed " + numIndex);

    }*/

    public static int index(File indexDir, File dataDir, String suffix) throws Exception {
    	StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_30,getJavaStopWords()); 
    	IndexWriter indexWriter;
    	if(!IndexReader.indexExists(FSDirectory.open(indexDir))){
    		indexWriter = new IndexWriter(
            		//StandardAnalyzer.STOP_WORDS_SET;.
                    FSDirectory.open(indexDir),
                    analyzer,
                    true,
                    IndexWriter.MaxFieldLength.LIMITED);
    	}else{
         indexWriter = new IndexWriter(
        		//StandardAnalyzer.STOP_WORDS_SET;.
                FSDirectory.open(indexDir),
                analyzer,
                false,
                IndexWriter.MaxFieldLength.LIMITED);
    	}
        indexDirectory(indexWriter, dataDir, suffix);

        int numIndexed = indexWriter.maxDoc();
        indexWriter.optimize();
        indexWriter.close();

        return numIndexed;

    }

    private static void indexDirectory(IndexWriter indexWriter, File dataDir,
            String suffix) throws IOException, ClassNotFoundException, ParseException {

        File[] files = dataDir.listFiles();
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            if (f.isDirectory()) {
                indexDirectory(indexWriter, f, suffix);
            } else {
                indexFileWithIndexWriter(indexWriter, f, suffix);
            }
        }

    }

    private static void indexFileWithIndexWriter(IndexWriter indexWriter, File f,
            String suffix) throws IOException, ClassNotFoundException, ParseException {

        if (f.isHidden() || f.isDirectory() || !f.canRead() || !f.exists()) {
            return;
        }
        if (suffix != null && !f.getName().endsWith(suffix)) {
            return;
        }
        ;
        
		
        System.out.println("Indexing file " + f.getCanonicalPath());
        System.out.println(f.getAbsolutePath());
        CompilationUnit cu;
        cu = JavaParser.parse(f);
        MethodVisitior mv;
        mv = new MethodVisitior();
        mv.visit(cu, null);
       
        ArrayList<String> methods = mv.methods;

        //Class s = Class.forName(f.getName().replaceAll(".java", ""));
        Document doc = new Document();
        doc.add(new Field("contents", new FileReader(f), Field.TermVector.YES));
        //oc.add(Field.TermVector);
        doc.add(new Field("filename", f.getCanonicalPath(),
                Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));
      // Field
        
        for (String method : methods) {
            System.out.println("metodo " + method);
            doc.add(new Field("method", method, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));
        }
        indexWriter.addDocument(doc);

    }

    static class MethodVisitior extends VoidVisitorAdapter {

        ArrayList methods = new ArrayList();

        @Override
        public void visit(MethodDeclaration n, Object arg) {
            if (!n.getName().equals("main")) {
                methods.add(n.getName());                
            }
        }

    }

}
