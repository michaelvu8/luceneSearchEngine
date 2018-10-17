package com.mycompany.app;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.TokenStream; 
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import java.util.*;
import java.util.HashSet;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;



public class CustomAnalyzer extends Analyzer {
    @Override
    protected TokenStreamComponents createComponents(String s) {

        final Tokenizer standardTokenizer = new StandardTokenizer();
        TokenStream tokenizer = new StandardFilter(standardTokenizer);
        tokenizer = new LowerCaseFilter(tokenizer);
	Set<char[]> stopCharArray = new HashSet<char[]>();
	List stopWordsList = Arrays.asList
		("i", "me", "my", "myself", "we", "our", "ours", "ourselves", 			"you", "your", "yours", "yourself", "yourselves", "he", "him", "his", "himself", 			"she", "her", "hers", "herself", "it", "its", "itself", "they", "them", "their", 			"theirs", "themselves", "what", "which", "who", "whom", "this", "that", "these", 			"those", "am", "is", "are", "was", "were", "be", "been", "being", "have", "has", 			"had", "having", "do", "does", "did", "doing", "a", "an", "the", "and", "but", 			"if", "or", "because", "as", "until", "while", "of", "at", "by", "for", "with", 		"about", "against", "between", "into", "through", "during", "before", "after", 			"above", "below", "to", "from", "up", "down", "in", "out", "on", "off", "over", 		"under", "again", "further", "then", "once", "here", "there", "when", "where", 			"why", "how", "all", "any", "both", "each", "few", "more", "most", "other", 			"some", "such", "no", "nor", "not", "only", "own", "same", "so", "than", "too", 		"very", "s", "t", "can", "will", "just", "don", "should", "now");
       	CharArraySet stopWords = new CharArraySet(stopWordsList,true);
        tokenizer = new StopFilter(tokenizer, stopWords);
	tokenizer = new PorterStemFilter(tokenizer);
        return new TokenStreamComponents(standardTokenizer, tokenizer);

    }
}
