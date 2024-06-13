/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package jgam.util;

import java.io.PrintStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A small framework to handle command lines.
 * 
 * <p>
 * Command line options can be defined beforehand, the command line be parsed
 * and set options be queried. It is possible to print a short usage message.
 * 
 * <p>
 * <i>Note</i> one can use {@code --} to end the list of command line options.
 * This is used if an argument bears a name of a command line option.
 * 
 * <h3>Typical usage</h3>
 * 
 * <pre>
 * CommandLine cl = new CommandLine();
 * cl.addOption(&quot;-help&quot;, null, &quot;Print usage&quot;);
 * cl.addOption(&quot;-noArg&quot;, null, &quot;Parameter w/o argument&quot;);
 * cl.addOption(&quot;-argument&quot;, &quot;argname&quot;, &quot;Parameter w/ argument&quot;);
 * 
 * // public static void main(String[] args) : 
 * cl.parse(args);
 * 
 * if (cl.isSet(CMDLINE_HELP)) {
 *     cl.printUsage(System.out);
 *     System.exit(0);
 * }
 * 
 * if (cl.isSet(&quot;-noArg&quot;)) {
 *     // do something
 * }
 * 
 * String s = cl.getString(&quot;-argument&quot;, &quot;defaultValue&quot;);
 * 
 * list = cl.getArguments();
 * // handle remaining arguments
 * </pre>
 */
public class CommandLine {
    
    /**
     * Option is a data only strucuture to capture a single cmd-line option.
     * Its description as well as its value 
     */
    private static class Option {
        String description;
        String image;
        String value;
        String parameter;
    }

    /**
     * default value for the length of a line for output.
     */
    private static final int DEFAULT_LINE_LENGTH = 80;
    
    /**
     * The options that have been defined. Mapped from their image.
     */
    private Map<String, Option> options = new LinkedHashMap<String, Option>();
    
    /**
     * The additional arguments which do not belong to an option.
     */
    private List<String> arguments = new LinkedList<String>();
    
    /**
     * the line length to be used for printing out usage.
     * Break lines which are longer
     */
    private int lineLength = DEFAULT_LINE_LENGTH;

    /**
     * Instantiates a new command line handling object.
     */
    public CommandLine() {
    }
    
    /**
     * Adds a command line option to this handler.
     * 
     * @param image
     *                the image of the option (e.g. {@code -help}
     * @param parameter
     *                simple description of the argument, null if there is no
     *                argument for this option
     * @param description
     *                the description of the option
     */
    public void addOption(String image, String parameter, String description) {
        
        if(!image.startsWith("-")) {
            throw new IllegalArgumentException("Parameters need to start with '-': " + image);
        }
        
        if(options.containsKey(image)) {
            throw new IllegalArgumentException(image + " has already been registered");
        }
        
        Option o = new Option();
        o.image = image;
        o.parameter = parameter;
        o.description = description;
        options.put(image, o);
    }
    
    /**
     * Parses the command line.
     * 
     * @param args
     *                typically the array of command line arguments passed to
     *                the main method.
     * 
     * @throws CommandLineException
     *                If a option is unknown or badly formatted.
     */
    public void parse(String args[]) throws CommandLineException {
        int cnt = 0;
        while (cnt < args.length && args[cnt].startsWith("-")) {
            
            if("--".equals(args[cnt])) {
                cnt++;
                break;
            }
            
            String current = args[cnt];
            Option option = options.get(current);
            
            if(option == null) {
                throw new CommandLineException("Unknown command line option: " + current);
            }
            
            if(option.parameter != null) {
                if(cnt == args.length - 1)
                    throw new CommandLineException("Command line option " + current + " expects a parameter but did not receive one");
                cnt ++;
                option.value = args[cnt];
            } else {
                option.value = "true";
            }
            
            cnt ++;
        }
        
        while(cnt < args.length) {
            arguments.add(args[cnt]);
            cnt ++;
        }
    }
    
    /**
     * Gets the non-option arguments of the command line
     * 
     * @return a read-only list of strings.
     */
    public List<String> getArguments() {
        return Collections.unmodifiableList(arguments);
    }
    
    /**
     * Checks if a boolean command line option is set.
     * 
     * @param param
     *                the image of the command line option to be checked
     * 
     * @return true if the option is set, false otherwise
     */
    public boolean isSet(String param) {
        
        Option option = options.get(param);
        if(option == null) {
            throw new IllegalArgumentException(param + " is unknown option");
        }
        
        return option.value != null;
    }
    
    /**
     * Gets the parameter string passed to an argument option.
     * 
     * If the parameter has not been specified, return the default value.
     * 
     * @param param
     *                the command line option. Needs to take an argument
     * @param defaultValue
     *                the default value to return if option is not set
     * 
     * @return either the set option or defaultValue if not set.
     */
    public String getString(String param, String defaultValue) {
        Option option = options.get(param);
        assert option != null : param + " is unknown option";
        assert option.parameter != null : param + " does not take arguments";
        
        String value = option.value;
        return value == null ? defaultValue : value; 
    }
    
    
    // TODO DOC
    /**
     * Gets the integer.
     * 
     * @param param
     *                the param
     * @param defaultValue
     *                the default value
     * 
     * @return the integer
     * 
     * @throws CommandLineException
     *                 the command line exception
     */
    public int getInteger(String param, int defaultValue) throws CommandLineException {
        Option option = options.get(param);
        assert option != null : param + " is unknown option";
        
        String value = option.value;
        if(value == null)
            return defaultValue;
        
        try {
            return Integer.decode(value);
        } catch (NumberFormatException e) {
            throw new CommandLineException(param + " expects an integer argument, but received: " + option.value);
        }
    }

    // TODO DOC
    public double getDouble(String param, double defaultValue) throws CommandLineException {
        
        Option option = options.get(param);
        assert option != null : param + " is unknown option";
        
        String value = option.value;
        if(value == null)
            return defaultValue;
        
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new CommandLineException(param + " expects an integer argument, but received: " + option.value);
        }
    }

    /**
     * Prints the usage for this command line.
     * 
     * @param stream
     *                the stream to print to (typically System.out)
     */
    public void printUsage(PrintStream stream) {
        int maxlen = 0;
        
        for (Option option : options.values()) {
            int len = option.image.length();
            if(option.parameter != null)
                len += 1 + option.parameter.length();
            maxlen = Math.max(len, maxlen);
        }
        
        maxlen += 2;
        
        int descLength = lineLength - maxlen;
        
        // TODO: Wrap line
        for (Option option : options.values()) {
            String s = option.image;
            if(option.parameter != null)
                s += " " + option.parameter;
            stream.print(s);
            indent(stream, maxlen - s.length());
        
            String message = option.description;
            
            while (message.length() > descLength) {
                int p = message.lastIndexOf(' ', descLength);
                if (p <= 0) {
                    p = message.indexOf(' ', descLength);
                }

                if (p > 0) {
                    stream.println(message.substring(0, p));
                    message = message.substring(p + 1);
                } else {
                    stream.println(message);
                    message = "";
                }
                
                indent(stream, maxlen);
            }
            
            stream.println(message);
        }
        
        stream.flush();
    }

    private void indent(PrintStream stream, int len) {
        for (int i = len; i > 0; i--) {
            stream.print(" ");
        }
    }

}
