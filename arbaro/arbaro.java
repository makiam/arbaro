//  #**************************************************************************
//  #
//  #    $Id$  - the main program - handles command line a.s.o.
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

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.File;

import net.sourceforge.arbaro.params.Params;
import net.sourceforge.arbaro.tree.*;

public class arbaro {
    static Tree tree;

    static int XMLinput = 0;
    static int CFGinput = 1;
    static int XMLoutput = 99;
    static void p(String s) { System.err.println(s); }
    static void p() { System.err.println(); }

    public static final String progname = 
	"Arbaro - creates trees for rendering with povray from xml parameter files\n"+
	"(c) 2003 by Wolfram Diestel (GPL see file COPYING)\n";

    static void programname() {
	p(progname);
	p();
    }

    static void usage () {
	p("syntax:"); 
	p("arbaro [OPTIONS] <paramfile.xml> > <tree.inc>");
	p();
	p("options");
	p("     -h|--help           Show this helpscreen");
	p();
	p("     -q|--quiet          Only error messages are output to stderr no progress");
	p();
	p("     -d|--debug          Much debugging ouput should be interesting for developer only");
	p();
	p("     -o|--output <file>  Output Povray code to this file instead of STDOUT");
	p();
	p("     -s|--seed <seed>    Random seed for the tree, default is 13, but you won't all");
	p("                         trees look the same as mine, so giv something like -s 17 here");
	p("                         the seed is part of the  declaration string in the povray file");
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
	p("    -p|--scene           output Povray scene file <species>.pov");
	p();
	p("example:");
	p();
	p("    arbaro.py < trees/quaking_aspen.xml > pov/quaking_aspen.inc");
	p();
    }

    /*    class opt_val {
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
    */

    public static void main (String [] args) throws Exception{
	//	try {
	tree = new Tree();

	boolean quiet = false;
	boolean debug = false;
	int seed = 13;
	int levels = -1;
	int output=Params.MESH;
	double smooth=-1;
	int input = XMLinput;
	String input_file = null;
	String output_file = null;
	String scene_file = null;

	for (int i=0; i<args.length; i++) {

	    if (args[i].equals("-d") || args[i].equals("--debug")) {
		debug = true;
	    } else if (args[i].equals("-h") || args[i].equals("--help")) {
		programname();
		usage();
		System.exit(0);
	    } else if (args[i].equals("-q") || args[i].equals("--quiet")) {
		quiet = true;
	    } else if (args[i].equals("-o") || args[i].equals("--output")) {
		output_file = args[++i];
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
	    } else if (args[i].equals("-p") || args[i].equals("--scene")) {
		scene_file = args[++i];
	    } else if (args[i].charAt(0) == '-') {
		programname();
		usage();
		System.err.println("Invalid option "+args[i]+"!");
		System.exit(1);
	    } else {
		// rest of args should be files 
		// input_files = new String[] = ...
		input_file = args[i];
		break;
	    }
	}
		       
	
	//########## read params from XML file ################
	
	if (! quiet) {
	    programname();
	}

	tree.params.debug=debug;
	tree.params.output=output;
	// put here or later?
	if (smooth>=0) tree.params.Smooth = smooth;
	
	if (! quiet) System.err.println("Reading parameters from "
					+ (input_file==null?"STDIN":input_file)
					+"...");

	InputStream in;
	if (input_file == null) {
	    in = System.in;
	} else {
	    in = new FileInputStream(input_file);
	}

	// read parameters
	if (input == CFGinput) tree.params.readFromCfg(in);
	else tree.params.readFromXML(in);
	
	// FIXME: put here or earlier?
	if (smooth>=0) tree.params.setParam("Smooth",new Double(smooth).toString());
	
	tree.params.verbose=(! quiet);
	tree.params.Seed=seed;
	tree.params.stopLevel = levels;

	PrintWriter out;
	if (output_file == null) {
	    out = new PrintWriter(new OutputStreamWriter(System.out));
	} else {
	    out = new PrintWriter(new FileWriter(new File(output_file)));
	}

	if (output==XMLoutput) {
	    // save parameters in XML file, don't create tree
	    tree.params.toXML(out);
	} else {
	    tree.make();
	    tree.povray(out);
	}

	if (scene_file != null) {
	    if (! quiet) System.err.println("Writing Povray scene to "+scene_file+"...");
	    PrintWriter scout = new PrintWriter(new FileWriter(new File(scene_file)));
	    tree.povray_scene(scout);
	}

    }
};

  











