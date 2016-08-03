package org.ld4l.addlabels;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RiotException;
import org.apache.jena.vocabulary.RDFS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Labeller {

    private static final Logger LOGGER = 
            LogManager.getLogger(Labeller.class); 

    private String input;
    private String outputDir;
    private LabelMaker labelMaker;
    private LabelModifier labelModifier;

    
    public Labeller(String input, String outputDir) {

        this.input = input;
        this.outputDir = outputDir;
        this.labelMaker = new LabelMaker();
        this.labelModifier = new LabelModifier();
        
    }
    
    public void getLabels() {
        
        File inp = new File(input);
        File[] inputFiles;
        
        if (inp.isDirectory()) {
            LOGGER.debug("Got directory of files");
            inputFiles = inp.listFiles();
        } else {
            inputFiles = new File[] { inp };
        }
        
        for (File file : inputFiles) {
            LOGGER.debug("Processing file " + file.toString());
            addLabelsToFile(file);
        }
        
    }
    
    private void addLabelsToFile(File file) {

        Model model = readModelFromFile(file.toString());        
        Model newModel = getLabels(model);
  
        String basename = FilenameUtils.getBaseName(file.toString());
        writeModelToFile(newModel, basename);
             
    }
    
    private Model getLabels(Model model) {
 
        Model assertions = ModelFactory.createDefaultModel();
        Model retractions = ModelFactory.createDefaultModel();
        
        ResIterator subjects = model.listSubjects();

        int subjectCount = 0;
        int existingLabel = 0;
        int modifiedLabel = 0;
        int newLabel = 0;
        int noLabelMade = 0;
        
        while (subjects.hasNext()) {
            Resource subject = subjects.nextResource();
            subjectCount++;
            String subjectUri = subject.getURI();
            LOGGER.debug("Got subject " + subjectUri);
            
            Statement labelStmt = subject.getProperty(RDFS.label);

            // If the resource doesn't already have a label
            if (labelStmt == null) {
                LOGGER.debug("Getting label for subject " + subjectUri);
                String label = labelMaker.makeLabel(subject);
                if (label != null) {
                    assertions.add(subject, RDFS.label, label);    
                    newLabel++;
                } else {
                    noLabelMade++;
                }
            } else {
                LOGGER.debug("Subject " + subjectUri + " already has label \"" 
                        + labelStmt.getString() + "\"");
                String originalLabel = labelStmt.getString();
                String label = 
                        labelModifier.modifyLabel(subject, originalLabel);
                if (! label.equals(originalLabel)) {
                    assertions.add(subject, RDFS.label, label);
                    retractions.add(labelStmt);
                    modifiedLabel++;
                } else {
                    existingLabel++;
                }
            }            
        }  
        
        LOGGER.info("Processed " + subjectCount + " distinct resources."); 
        LOGGER.info("Retained existing labels for " + existingLabel + " resources.");
        LOGGER.info("Modified existing labels for " + modifiedLabel + " resources");
        LOGGER.info("Made new labels for " + newLabel + " resources.");
        LOGGER.info("No label created for " + noLabelMade + " resources.");
        
        return model.remove(retractions)
                    .add(assertions);
    }


    private void writeModelToFile(Model model, String basename) {
        
        FileOutputStream outStream;
        File outFile = new File(outputDir, basename + ".nt");
        
        try {
            outStream = new FileOutputStream(outFile);
            RDFDataMgr.write(outStream, model, RDFFormat.NTRIPLES);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }        
    }  
    
    private Model readModelFromFile(String filename) {
        
        //return RDFDataMgr.loadModel(filename);
        // LOGGER.debug("Reading file " + filename);
        
        Model model = ModelFactory.createDefaultModel(); 
        try {
            model.read(filename);
        } catch (RiotException e) {
           LOGGER.error("ERROR: RDF parsing error in file " 
                   + FilenameUtils.getName(filename) + ": " + e.getMessage()
                   + ". Skipping rest of file.");
        }
        return model;
    }
    
}
