//  #**************************************************************************
//  #
//  #    $file$  - the main program - handles command line a.s.o.
//  #
//  #    Copyright (C) 2003  Wolfram Diestel
//  #
//  #    This program is free software; you can redistribute it and/or modify
//  #    it under the terms of the GNU General Public License as published by
//  #    the Free Software Foundation; either version 2 of the License, or
//  #    (at your option) any later version.
//  #
//  #    This program is distributed in the hope that it will be useful,
//  #    but WITHOUT ANY WARRANTY; without even the implied warranty of
//  #    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  #    GNU General Public License for more details.
//  #
//  #    You should have received a copy of the GNU General Public License
//  #    along with this program; if not, write to the Free Software
//  #    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//  #
//  #    Send comments and bug fixes to diestel@steloj.de
//  #
//  #**************************************************************************/

package net.sourceforge.arbaro;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import net.sourceforge.arbaro.params.Params;

public class arbaro {
    static Tree tree;

    static int XMLinput = 0;
    static int CFGinput = 1;
    static int XMLoutput = 99;
    static void p(String s) { System.err.println(s); }
    static void p() { System.err.println(); }

    static void programname() {
	p("arbaro - creates trees for rendering with povray from xml parameter files");
	p("(c) 2003 by Wolfram Diestel (GPL see file COPYING)");
	p();
    }

    static void usage () {
	p("syntax:"); 
	p("arbaro [OPTIONS]  < <paramfile.xml> > <tree.inc>");
	p();
	p("options");
	p("     -h|--help            Show this helpscreen");
	p();
	p("     -q|--quiet           Only error messages are output to stderr no progress");
	p();
	p("     -d|--debug           Much debugging ouput should be interesting for developer only");
	p();
	p("     -s|--seed <seed>     Random seed for the tree, default is 13, but you won't all");
	p("                          trees look the same as mine, so giv something like -s 17 here");
	p("                          the seed is part of the  declaration string in the povray file");
	p();
	p("    -l|--levels <level>  1..Levels+1 -- calc and ouput only so much levels, usefull for");
	p("                         fast testing of parameter changes or to get a draft tree for");
	p("                         a first impression of a scene without all that small stems and");
	p("                         leaves. Levels+1 means calc alle Levels and Leaves, but this");
	p("                         is the same as not giving this option here");
	p();
	p("    -m|--mesh [<smooth>] Output stems as mesh2 objects the optional smooth value influences");
	p("                         how much vertices are used for every stem section and for which");
	p("                         levels normals should be used to hide the triangle borders");
	p();
	p("    -c|--cones           output stems as unions of cones and spheres, Lobes don't work");
	p("                         with this option, but source files are a little bit smaller.");
	p("                         Povray read Mesh2 objects faster. Cones are handy for use with");
	p("                         KPovmodeler, which doesn't support mesh objects yet.");
	p();
	p("    -r|--treecfg         Input file is a simple Param=Value list. Needs less typing for");
	p("                         a new tree than writing XML code");
	p();
	p("    -x|--xml             Output parameters as XML tree definition instead of creating");
	p("                         the tree and writing it as povray code. Useful for converting a");
	p("                         simple parameter list to a XML file: ");
	p("                            arbaro.py --treecfg -x < mytree.cfg > mytree.xml");
	p();
	p("example:");
	p();
	p("    arbaro.py < trees/quaking_aspen.xml > pov/quaking_aspen.inc");
	p();
    }

    class opt_val {
	public String opt;
	public String val;

	public opt_val(String o, String v) {
	    opt = o;
	    val = v;
	}
    }

    String getopt(java.util.Vector args, String options, java.util.Vector longOptions) {
	// if ((String)args.firstElement()).startsWith
	return "";
    }

    public static void main (String [] args) throws Exception{
	//	try {
	tree = new Tree();
      
	programname();

	boolean quiet = false;
	boolean debug = false;
	int seed = 13;
	int levels = -1;
	int output=Params.MESH;
	double smooth=-1;
	int input = XMLinput;

	for (int i=0; i<args.length; i++) {


	    if (args[i].equals("-d") || args[i].equals("--debug")) {
		debug = true;
	    } else if (args[i].equals("-h") || args[i].equals("--help")) {
		usage();
		System.exit(0);
	    } else if (args[i].equals("-q") || args[i].equals("--quiet")) {
		quiet = true;
	    } else if (args[i].equals("-s") || args[i].equals("--seed")) {
		seed = new Integer(args[++i]).intValue();
	    } else if (args[i].equals("-l") || args[i].equals("--levels")) {
		levels = new Integer(args[++i]).intValue();
	    } else if (args[i].equals("-c") || args[i].equals("--cones")) {
		output = Params.CONES;
	    } else if (args[i].equals("-m") || args[i].equals("--mesh")) {
		output = Params.MESH;
		if (args[i+1].charAt(0) == '0' || args[i+1].charAt(1) == '1') {
		    smooth = new Double(args[++i]).doubleValue();
		}
	    } else if (args[i].equals("-x") || args[i].equals("--xml")) {
		output = XMLoutput;
	    } else if (args[i].equals("-r") || args[i].equals("--treecfg")) {
		input = CFGinput;
	    }
	}
		       
    // rest of args should be files    
    // ...
    //if (optind < argc) {
    //   while (optind < argc)
    //                  argv[optind++]...
    //           }


    //########## read params from XML file ################


    tree.params.debug=debug;
    tree.params.output=output;
    // put here or later?
    if (smooth>=0) tree.params.Smooth = smooth;

    System.err.println("Reading parameters from STDIN...");
   
    if (input == CFGinput) tree.params.readFromCfg(System.in);
    else tree.params.readFromXML(System.in);

    // FIXME: put here or earlier?
    if (smooth>=0) tree.params.setParam("Smooth",new Double(smooth).toString());
   
    tree.params.verbose=(! quiet);
    tree.params.Seed=seed;
    tree.params.stopLevel = levels;

    if (output==XMLoutput) {
	// save parameters in XML file, don't create tree
	tree.params.toXML(new PrintWriter(new OutputStreamWriter(System.out)));
    } else {
	
	
	tree.make();
	
	PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out));
	tree.povray(out);
	System.exit(0);
    }

    };
}

  











