package exercise;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.util.FileUtils;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;
import java.io.PrintWriter;

public class ObligFour {
	
	private Model rdfSchema;
	private Model rdfData;
	private InfModel inferredModel;
	private Model resultModel;
	
	private final static boolean DEBUG = true;
	
	
	 // Creates object
	
	public static ObligFour create() {
		return new ObligFour();
	}
	
	private ObligFour(){
		rdfSchema = ModelFactory.createDefaultModel();
		resultModel = ModelFactory.createDefaultModel();
		rdfData = ModelFactory.createDefaultModel();
	}
	
	 // Loads an rdfSchema file in schemaModel
 
	public void loadSchema(String schemaFile) {
		FileManager.get().addLocatorClassLoader(ObligFour.class.getClassLoader());
		rdfSchema = FileManager.get().loadModel(schemaFile);
	}

	
	public void loadRdfGraph(String rdfDataFile) {
		loadFileIntoModel( rdfData, rdfDataFile, "TURTLE" );
	}
	
	private void loadFileIntoModel(Model model, String rdfFile) {
		loadFileIntoModel( model, rdfFile, FileUtils.guessLang(rdfFile) );
	}
	
	private void loadFileIntoModel(Model model, String rdfFile, String language) {
		try {
			model.read( rdfFile, language );
		} catch (Exception e) {
			System.out.printf("Something went wrong while trying load "
					+ " the model with data from the file: %s", rdfFile + "\n");
		}
	}
	
	
	public void reasoning() {
		inferredModel = ModelFactory.createRDFSModel( rdfSchema, rdfData );
	}
	
	// Execute query
	
	public void execute(String queryFile) {
		if (inferredModel == null)
			reasoning();
		
		try {
			Query query = QueryFactory.read( queryFile );
			QueryExecution qe = QueryExecutionFactory.create(
					query, inferredModel
			);
			resultModel = qe.execConstruct();
		} catch (Exception e) {
			System.out.printf("Something went wrong while trying execute "
					+ " the query in file: %s", queryFile + "\n");
		}
	}
	

	public void saveTo(String foafFile) {
		writeFile(resultModel, foafFile);
	}
	 //Writes the model to the given result file
	
	public void writeFile(Model model, String outputFile) {
		try (PrintWriter pw = new PrintWriter(outputFile)) {
			model.write( pw, "TURTLE" );
		} catch (Exception e) {
			System.out.printf("Something went wrong while trying to "
					+ " write to the file: %s", e.getMessage() + "\n");
		}
	}
	
	public void printStuff() {
		System.out.println(
				"\n\nRDF Schema ==========================================\n");
		rdfSchema.write(System.out, "TURTLE");
		System.out.println(
				"\n\nRDF data ==========================================\n");
		rdfData.write(System.out, "TURTLE");
		System.out.println(
				"\n\nInfered model ==========================================\n");
		inferredModel.write(System.out, "TURTLE");
		System.out.println(
				"\n\nQuery result ==========================================\n");
		resultModel.write(System.out, "TURTLE");
	}
	
	public static void main(String[] args) {
		String schemaFile, queryFile, foafFile;
		ObligFour obj;
		
		//Check for valid file names
		try {
			schemaFile = args[0];
			queryFile = args[1];
			foafFile = args[2];
		} catch (Exception e) {
			System.out.println("Please supply a input and output file. \n");
			return;
		}
		
		obj = ObligFour.create();
		obj.loadSchema( schemaFile );
		obj.loadRdfGraph(
				"https://www.uio.no/studier/emner/matnat/ifi/IN3060/v21/obliger/simpsons.ttl"
		);
		obj.reasoning();
		obj.execute( queryFile );
		obj.saveTo( foafFile );
		
		if (DEBUG) {
			obj.printStuff();
		}
	}
}