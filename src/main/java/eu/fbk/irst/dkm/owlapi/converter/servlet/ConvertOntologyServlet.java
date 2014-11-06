package eu.fbk.irst.dkm.owlapi.converter.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxOntologyFormat;
import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.semanticweb.owlapi.io.OWLFunctionalSyntaxOntologyFormat;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import eu.fbk.irst.dkm.owlapi.apibinding.OWLManager;
import eu.fbk.irst.dkm.owlapi.converter.OWLOntologyConverter;
import eu.fbk.irst.dkm.owlapi.io.OWLLatexStyleSyntaxOntologyFormat;

public class ConvertOntologyServlet extends HttpServlet {
 
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(ConvertOntologyServlet.class.getCanonicalName());
	
	private enum Syntax { RDFXML,OWLXML,TURTLE,MANCHESTER,FUNCTIONAL,TEXOWL }
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		final String input = request.getParameter("input");
		LOGGER.log(Level.INFO, "Input text: {0}", input);
		final Syntax exportSyntax = Syntax.valueOf(request.getParameter("export_syntax").toUpperCase());
		LOGGER.log(Level.INFO, "Export syntax: {0}", exportSyntax);
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntologyFormat outputFormat = null;
		switch (exportSyntax) {
		case RDFXML:
			outputFormat = new RDFXMLOntologyFormat();
			break;
		case OWLXML:
			outputFormat = new OWLXMLOntologyFormat();
			break;
		case TURTLE:
			outputFormat = new TurtleOntologyFormat();
			break;
		case MANCHESTER:
			outputFormat = new ManchesterOWLSyntaxOntologyFormat();
			break;
		case FUNCTIONAL:
			outputFormat = new OWLFunctionalSyntaxOntologyFormat();
			break;
		case TEXOWL:
			outputFormat = new OWLLatexStyleSyntaxOntologyFormat();
			break;
		}
		
		InputStream inputStream = IOUtils.toInputStream(input);
		OutputStream outputStream = response.getOutputStream();
		try {			
			OWLOntology ontology = manager.loadOntologyFromOntologyDocument(inputStream);
			OWLOntologyConverter.convert(ontology, outputFormat, outputStream);
			outputStream.flush();
		} catch (OWLOntologyCreationException e) {
			response.sendError(400, "Error on loading the input as an ontology");
		} catch (OWLOntologyStorageException e) {
			response.sendError(500, "Error on converting the input with " + outputFormat);
		}
	}
}
