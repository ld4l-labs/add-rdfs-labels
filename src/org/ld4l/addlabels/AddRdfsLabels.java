package org.ld4l.addlabels;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class AddRdfsLabels {
    
    private static final Logger LOGGER = 
            LogManager.getLogger(AddRdfsLabels.class);  
    
    /** 
     * Read in program options and call appropriate processing functionality.
     * @param args
     */
    public static void main(String[] args) {
        
        // Define program options
        Options options = getOptions();
        
        // Get commandline options
        CommandLine cmd = getCommandLine(options, args);
        if (cmd == null) {
            return;
        }

        String absInputPath = getAbsoluteInputPath(cmd.getOptionValue("input"));                
        if (absInputPath == null) {
            return;
        }

        String absOutputDir = 
                createOutputDir(cmd.getOptionValue("outdir"));
        if (absOutputDir == null) {
            LOGGER.error("Can't create output directory. Aborting.");
            return;
        }
           
        // Log application configuration settings
        LOGGER.info("Settings: ");
        LOGGER.info("Input path: " + absInputPath);
        LOGGER.info("Output directory path: " + absOutputDir);
        
        Labeller labeller = new Labeller(absInputPath, absOutputDir); 
        labeller.addLabels();  
        LOGGER.info("Done!");
   
    }
  
    /**
     * Check for valid input directory. Return the absolute path to the input 
     * directory if it exists, otherwise log an error and return null. 
     * @param path - absolute or relative path to input directory
     * @return absolute path to the input directory if it exists, otherwise null
     */
    private static String getAbsoluteInputPath(String input) {

        String absInputPath = null;
        
        File in = new File(input);
        try {
            absInputPath = in.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return absInputPath;
    }
    
    /**
     * Make output directory and any intermediate directories. Return the 
     * output directory if it was successfully created, otherwise log an error
     * and return null.
     * @param outDirName - absolute or relative path to output directory. A child
     * directory named with current datetime will be created under it.
     * @return the output directory if it was successfully created, otherwise 
     * null
     */
    private static String createOutputDir(String outDirName) {
        
        String outDirCanonicalPath = null;
        
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
        Date date = new Date();
        String now = dateFormat.format(date);
        
        File outDir = new File(outDirName, now);
        
        try {
            outDirCanonicalPath = outDir.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
            outDirCanonicalPath = outDir.getAbsolutePath();
        }
        
        outDir.mkdirs();    
        
        return outDirCanonicalPath;
    }



    /**
     * Print help text.
     * @param options
     */
    private static void printHelp(Options options) {
        
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(80);
        formatter.printHelp("java -jar AddRdfsLabels.jar", options, true);
    }


    /**
     * Parse commandline options.
     * @param options
     * @param args
     * @return
     */
    private static CommandLine getCommandLine(Options options, String[] args) {
        
        // Parse program arguments
        CommandLineParser parser = new DefaultParser();
        try {
            return parser.parse(options, args);
        } catch (MissingOptionException e) {
            LOGGER.fatal(e.getMessage());
            printHelp(options);
        } catch (UnrecognizedOptionException e) {
            LOGGER.fatal(e.getMessage());
            printHelp(options);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            LOGGER.fatal(e.getStackTrace().toString());
        }
        return null;
    }
    
    
    /**
     * Define the commandline options accepted by the program.
     * @return an Options object
     */
    private static Options getOptions() {
        
        Options options = new Options();
        
        options.addOption(Option.builder("i")
                .longOpt("input")
                .hasArg()
                .argName("input")
                .desc("Absolute or relative path to input file or directory")
                .build());
        
        options.addOption(Option.builder("o")
                .longOpt("outdir")
                .required()
                .hasArg()
                .argName("output_directory")
                .desc("Absolute or relative path to output directory. "
                        + "Will be created if it does not exist.")
                .build());
 
        return options;
    }
        
}


