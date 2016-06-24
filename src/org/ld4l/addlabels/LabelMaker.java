package org.ld4l.addlabels;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LabelMaker {

    private static final Logger LOGGER = 
            LogManager.getLogger(LabelMaker.class); 
    
    private static enum Namespace {
        foaf("http://xmlns.com/foaf/0.1/"),
        ld4l("http://bib.ld4l.org/ontology/"),
        lingvo("http://www.lingvoj.org/ontology#"),
        madsrdf("http://www.loc.gov/mads/rdf/v1#");
        
        private final String uri;
        
        Namespace(String uri) {
            this.uri = uri;
        }
    }
    
    private static enum Type {
        // Order is crucial!
        work(Namespace.ld4l, "Work"),
        instance(Namespace.ld4l, "Instance"),
        item(Namespace.ld4l, "Item"),
        person(Namespace.foaf, "Person"),
        organization(Namespace.foaf, "Organization"),
        agent(Namespace.foaf, "Agent"),
        authority(Namespace.madsrdf, "Authority"), 
        topic(Namespace.ld4l, "Topic"),
        language(Namespace.lingvo, "Lingvo");
        
        private final String uri;
        private final String localname;
        
        Type(Namespace namespace, String localname) {
            this.localname = localname;
            uri = namespace.uri + localname;
        }
    }
    
    LabelMaker() {
        // TODO Auto-generated constructor stub
    }
    
    String makeLabel(Resource resource) {
        
        String label = null;
                
        StmtIterator stmts = resource.listProperties(RDF.type);
        while (stmts.hasNext()) {
            Resource type = stmts.next().getResource();
            for (Type t : Type.values()) {
                if (type.getURI().equals(t.uri)) {
                    LOGGER.debug("Resource " + resource.getURI() 
                        + " is of type " + t.uri);
                    String methodName = getMethodName(t);
                    try {
                        Method method = this.getClass().getDeclaredMethod(
                                methodName, Resource.class);
                        LOGGER.debug("Got method " + method.getName());
                        label = (String) method.invoke(this, resource);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    break;
                }
            }
            
            if (label == null) {
                // assign rdf:value if exists
                label = resource.getLocalName();
            }
        }
        
        return label;
    }
    
    private String makeWorkLabel(Resource resource) {
        return "work label";
    }
    
    private String makeInstanceLabel(Resource resource) {
        return "instance label";
    }
    
    private String makeItemLabel(Resource resource) {
        return "item label";
    }
    
    private String makePersonLabel(Resource resource) {
        return "person label";
    }
    
    private String makeOrganizationLabel(Resource resource) {
        return "org label";
    }
    
    private String makeAgentLabel(Resource resource) {
        return "agent label";
    }
    
    private String makeAuthorityLabel(Resource resource) {
        return "auth label";
    }
    
    private String makeLanguageLabel(Resource resource) {
        return "lang label";
    }
    
    private String makeTopicLabel(Resource resource) {
        return "topic label";
    }
    
    private String getMethodName(Type type) {
        return "make" + type.localname + "Label";
    }

}
