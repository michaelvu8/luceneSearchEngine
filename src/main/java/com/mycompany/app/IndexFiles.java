
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

public class IndexFiles {

  private IndexFiles() {}

  /** Index all text files under a directory. */
  public static void main(String[] args) {
    String usage = "java org.apache.lucene.demo.IndexFiles"
    + " [-index INDEX_PATH] [-docs DOCS_PATH] [-update]\n\n"
    + "This indexes the documents in DOCS_PATH, creating a Lucene index"
    + "in INDEX_PATH that can be searched with SearchFiles";
    String indexPath = "index";
    String docsPath = null;
    boolean create = true;
    for(int i=0;i<args.length;i++) {
      if ("-index".equals(args[i])) {
        indexPath = args[i+1];
        i++;
      } else if ("-docs".equals(args[i])) {
        docsPath = args[i+1];
        i++;
      } else if ("-update".equals(args[i])) {
        create = false;
      }
    }

    if (docsPath == null) {
      System.err.println("Usage: " + usage);
      System.exit(1);
    }

    final Path docDir = Paths.get(docsPath);
    if (!Files.isReadable(docDir)) {
      System.out.println("Document directory '" +docDir.toAbsolutePath()+ "' does not exist or is not readable, please check the path");
      System.exit(1);
    }
    
    Date start = new Date();
    try {
      System.out.println("Indexing to directory '" + indexPath + "'...");

      Directory dir = FSDirectory.open(Paths.get(indexPath));
      Analyzer analyzer = new CustomAnalyzer();
      // Analyzer analyzer = new StandardAnalyzer();
      IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

      if (create) {
        iwc.setOpenMode(OpenMode.CREATE);
      } else {
        iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
      }

      IndexWriter writer = new IndexWriter(dir, iwc);
      indexDocs(writer, docDir);

      writer.close();

      Date end = new Date();
      System.out.println(end.getTime() - start.getTime() + " total milliseconds");

    } catch (IOException e) {
      System.out.println(" caught a " + e.getClass() +
       "\n with message: " + e.getMessage());
    }
  }


  static void indexDocs(final IndexWriter writer, Path path) throws IOException {
    if (Files.isDirectory(path)) {
      Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          try {
            indexDoc(writer, file, attrs.lastModifiedTime().toMillis());
          } catch (IOException ignore) {
            // don't index files that can't be read.
          }
          return FileVisitResult.CONTINUE;
        }
      });
    } else {
      indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis());
    }
  }

  /** Indexes a single document */
  static void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException {
    try (InputStream stream = Files.newInputStream(file)) {
      BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));

      String currentLine = br.readLine();
      Document doc;
      String fieldType = "";


      while(currentLine != null){
        if(currentLine.substring(0,2).equals(".I")){
          doc = new Document();
          Field pathField = new StringField("path", currentLine.substring(2,currentLine.length()), Field.Store.YES);
          doc.add(pathField);
          currentLine = br.readLine();
          while(!(currentLine.substring(0,2).equals(".I"))){
            if(currentLine.substring(0,2).equals(".T")){
              fieldType = "Title";
              currentLine = br.readLine();       
            } else if(currentLine.substring(0,2).equals(".A")){
              fieldType = "Author";
              currentLine = br.readLine();
            } else if(currentLine.substring(0,2).equals(".B")){
              fieldType = "Book";
              currentLine = br.readLine();
            } else if(currentLine.substring(0,2).equals(".W")){
              fieldType = "Words";
              currentLine = br.readLine();
            }
            doc.add(new TextField(fieldType, currentLine, Field.Store.YES));
            System.out.println(currentLine);
            currentLine = br.readLine();
            if(currentLine == null){
              break;
            }
          }

          if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
            System.out.println("adding " + file);
            writer.addDocument(doc);
          } else {
            System.out.println("updating " + file);
            writer.updateDocument(new Term("path", file.toString()), doc);
          }

        }
      }      
    }
  }
}



























































