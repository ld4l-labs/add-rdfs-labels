package org.ld4l.addlabels;

import java.lang.reflect.Method;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LabelModifier {

    private static final Logger LOGGER = 
            LogManager.getLogger(LabelModifier.class); 
    
    private static enum Namespace {
        foaf("http://xmlns.com/foaf/0.1/"),
        ld4l("http://bib.ld4l.org/ontology/"),
        lingvo("http://www.lingvoj.org/ontology#"),
        madsrdf("http://www.loc.gov/mads/rdf/v1#"),
        prov("http://www.w3.org/ns/prov#"),
        rdf("http://www.w3.org/1999/02/22-rdf-syntax-ns#"),
        skos("http://www.w3.org/2004/02/skos/core#");
        
        private final String uri;
        
        Namespace(String uri) {
            this.uri = uri;
        }
    }
    
    
    LabelModifier() {
        // TODO Auto-generated constructor stub
    }
    
    String modifyLabel(Resource resource, String label) {
        
        // Contributions
        if (label.equals("Author")) {
            label = "Author Contribution";
        } else if (label.equals("Composer")) {
            label = "Composer Contribution";
        } else if (label.equals("Conductor")) {
            label = "Conductor Contribution";
        } else if (label.equals("Creator")) {
            label = "Creator Contribution";  
        } else if (label.equals("Editor")) {
            label = "Editor Contribution";        
        } else if (label.equals("Narrator")) {
            label = "Narrator Contribution";            
        } else if (label.equals("Performer")) {
            label = "Performer Contribution";
            
        // Provisions
        } else if (label.equals("Publisher")) {
            label = "Publisher Provision";
        }      
        
        return label;
    }

}
