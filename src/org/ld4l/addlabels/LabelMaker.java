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

public class LabelMaker {

    private static final Logger LOGGER = 
            LogManager.getLogger(LabelMaker.class); 
    
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
    
    private static enum Type {
        // Order is crucial!
        work(Namespace.ld4l, "Work"),
        instance(Namespace.ld4l, "Instance"),
        //item(Namespace.ld4l, "Item"),
        person(Namespace.foaf, "Person"),
        organization(Namespace.foaf, "Organization"),
        agent(Namespace.foaf, "Agent"),
        authority(Namespace.madsrdf, "Authority"), 
        topic(Namespace.ld4l, "Topic"),
        location(Namespace.prov, "Location"),
        language(Namespace.lingvo, "Lingvo");
        
        private final String uri;
        private final String localname;
        
        Type(Namespace namespace, String localname) {
            this.localname = localname;
            uri = namespace.uri + localname;
        }
    }
    
    private static enum LabelProperty {
        authLabel(Namespace.madsrdf, "authoritativeLabel"),
        name(Namespace.foaf, "name"),
        prefLabel(Namespace.skos, "prefLabel"),
        title(Namespace.ld4l, "hasTitle"),
        type(Namespace.rdf, "type"),
        value(Namespace.rdf, "value");
        
        private final String uri;
        
        LabelProperty(Namespace namespace, String localname) {
            uri = namespace.uri + localname;
        }
    }
    
    LabelMaker() {
        // TODO Auto-generated constructor stub
    }
    
    String makeLabel(Resource resource) {
        
        String label = makeLabelFromType(resource);
                


        if (label == null) {
            label = makeLabelFromRdfValue(resource);
        }
        
        if (label != null) {
            LOGGER.debug("Made new label \"" + label + "\" for " 
                    + resource.getURI());                
        } else {
            LOGGER.debug("No label made for " + resource.getURI());  
                                
        }
        
        return label;
    }
    
    private String makeLabelFromType(Resource resource) {

        String label = null;
        
        StmtIterator stmts = resource.listProperties(RDF.type);

        typestatements:
        while (stmts.hasNext()) {
            Resource resourceType = stmts.next().getResource();
            for (Type type : Type.values()) {
                if (resourceType.getURI().equals(type.uri)) {
                    LOGGER.debug("Resource " + resource.getURI() 
                        + " is a " + type.uri);
                    String methodName = getMethodName(type);
                    try {
                        Method method = this.getClass().getDeclaredMethod(
                                methodName, Resource.class);
                        LOGGER.debug("Got method " + method.getName());
                        label = (String) method.invoke(this, resource);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }  
                    // Don't look at any other types for this resource (for
                    // example, don't look at ld4l:Text for an ld4l:Work).
                    break typestatements;
                }                
            }
        }
        
        return label;
        
    }

    private String getMethodName(Type type) {
        return "make" + type.localname + "Label";
    }
    
    @SuppressWarnings("unused")
    private String makeWorkLabel(Resource resource) {
        return makeLabelFromTitle(resource);
    }
    
    @SuppressWarnings("unused")
    private String makeInstanceLabel(Resource resource) {
        return makeLabelFromTitle(resource);
    }
    
//    private String makeItemLabel(Resource resource) {
//        return "item label";
//    }
    
    @SuppressWarnings("unused")
    private String makePersonLabel(Resource resource) {
        return makeLabelFromFoafName(resource);
    }
    
    @SuppressWarnings("unused")
    private String makeOrganizationLabel(Resource resource) {
        return makeLabelFromFoafName(resource);
    }
    
    @SuppressWarnings("unused")
    private String makeAgentLabel(Resource resource) {
        return makeLabelFromFoafName(resource);
    }
    
    @SuppressWarnings("unused")
    private String makeLocationLabel(Resource resource) {
        return makeLabelFromFoafName(resource);
    }

    @SuppressWarnings("unused")
    private String makeAuthorityLabel(Resource resource) {
        return makeLabelFromDatatypeProperty(resource, LabelProperty.authLabel);
    }

//    @SuppressWarnings("unused")
//    private String makeLanguageLabel(Resource resource) {
//        return "lang label";
//    }

    @SuppressWarnings("unused")
    private String makeTopicLabel(Resource resource) {
        
        String label = null;
        
        label = makeLabelFromDatatypeProperty(
                resource, LabelProperty.prefLabel);
        
        if (label == null) {
            LOGGER.debug("FOUND TOPIC WITH NO skos:prefLabel");
        }   
        
        return label;
    }
        
    private String makeLabelFromFoafName(Resource resource) {        
        return makeLabelFromDatatypeProperty(resource, LabelProperty.name);
    }
    
    private String makeLabelFromDatatypeProperty(
            Resource resource, LabelProperty labelProperty) {

        Property property = ResourceFactory.createProperty(labelProperty.uri);
        return makeLabelFromDatatypeProperty(resource, property);       
    }

    private String makeLabelFromDatatypeProperty(
            Resource resource, Property property) {

        String label = null;
        
        Statement stmt = resource.getProperty(property);
        if (stmt != null) {
            label = stmt.getString();
        }
        
        return label;
        
    }    
    private String makeLabelFromTitle(Resource resource) {
        
        String label = null;
        
        Property titleProperty = 
                ResourceFactory.createProperty(LabelProperty.title.uri);
        Resource title = resource.getPropertyResourceValue(titleProperty);
        if (title != null) {
            LOGGER.debug("Found title " + title.getURI() + " for resource " 
                    + resource.getURI());
//            if (LOGGER.isDebugEnabled()) {
//                StmtIterator si = title.getModel().listStatements();  //(title, null, (RDFNode) null);
//                
//                while (si.hasNext()) {
//                    LOGGER.debug(si.next().toString());
//                }
//            }
            Statement titleLabelStmt = title.getProperty(RDFS.label);
            if (titleLabelStmt != null) {
                label = titleLabelStmt.getString();
                LOGGER.debug("Got title " + label + " for resource " 
                        + resource.getURI());
            }
        } else {
            LOGGER.debug("Not title found for resource " + resource.getURI());
        }
        
        return label;
    }
    
    private String makeLabelFromRdfValue(Resource resource) {

        String label = null;
        
        String value = 
                makeLabelFromDatatypeProperty(resource, LabelProperty.value);
        
        if (value != null) {
            label = value;
            Resource type = resource.getPropertyResourceValue(RDF.type);
            if (type != null) {               
                String typeLocalName = type.getLocalName().replaceAll(
                        "([a-z])([A-Z])", "$1 $2");
                label = label + " (" + typeLocalName + ")";
            }           
        }
                
        return label;        
    }

}
