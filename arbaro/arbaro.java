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

/* TODO:

  conical 0.1+... or 0.2+... - may be use special fir-mode
  evenly leaf distribution for tree with needles like fir, lark,...
  for needles use a leaf wich hase many needles with downangle 0
  try using one big mesh for leaves instead of union of discs

*/

import java.io.PrintWriter;
//import java.io.InputStreamReader;
//import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import params.Params;
import params.CfgTreeParser;
import params.XMLTreeParser;
import org.xml.sax.InputSource;

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
	p("    --treecfg            Input file is a simple Param=Value list. Needs less typing for");
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
	/*
  struct option long_options[] = {
    {"help", no_argument, 0, 'h'},
    {"quiet", no_argument, 0, 'q'},
    {"debug", no_argument, 0, 'd'},
    {"seed", required_argument, 0, 's'},
    {"levels", required_argument, 0, 'l'},
    {"mesh", optional_argument, 0, 'm'},
    {"cones", no_argument, 0, 'c'},
    {"xml", no_argument, 0, 'x'},
    {"treecfg", no_argument, 0, 'r'},
    {0, 0, 0, 0}
    };*/


	for (int i=0; i<args.length; i++) {


	    if (args[i].equals("-d")) {
		debug = true;
	    } else if (args[i].equals("-h")) {
		usage();
		System.exit(0);
	    } else if (args[i].equals("-q")) {
		quiet = true;
	    } else if (args[i].equals("-s")) {
		seed = new Integer(args[++i]).intValue();
	    } else if (args[i].equals("-l")) {
		levels = new Integer(args[++i]).intValue();
	    } else if (args[i].equals("-c")) {
		output = Params.CONES;
	    } else if (args[i].equals("-m")) {
		output = Params.MESH;
		smooth = new Double(args[++i]).doubleValue();
	    } else if (args[i].equals("-x")) {
		output = XMLoutput;
	    } else if (args[i].equals("-r")) {
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
   
    if (input == CFGinput) {
	CfgTreeParser parser = new CfgTreeParser();
	parser.parse(System.in,tree.params);
    } else {
	XMLTreeParser parser = new XMLTreeParser();
	parser.parse(new InputSource(System.in),tree.params);
    }

    // FIXME: put here or earlier?
    if (smooth>=0) tree.params.setParam("Smooth",new Double(smooth).toString());
   
    tree.params.verbose=(! quiet);
    tree.params.Seed=seed;
    

    // FIXME: mesh_smooth settings rely on
    // Levels, so maybe there should be a
    // different variable to halt tree creation on
    // a specific level

    //FIXME: params are not filled yet, the values are still in
    // the paramDB
    if (levels>=0 && levels<=tree.params.Levels) {
	tree.params.setParam("Levels",new Integer(levels).toString());
	tree.params.setParam("Leaves","0");
    }
    
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

  











