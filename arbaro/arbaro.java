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

import java.io.PrintWriter;
import java.io.LineNumberReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class arbaro {
    static Tree tree;

    static void programname() {
	System.err.println("arbaro - creates trees for rendering with povray from xml parameter files");
	System.err.println("(c) 2003 by Wolfram Diestel (GPL see file COPYING)");
    }

    static void usage () {
	System.err.println ("syntax:"); 
	/*
       << "arbaro [OPTIONS]  < <paramfile.xml> > <tree.inc>\n"
       << "\n"
       << "options\n"
       << "     -h|--help            Show this helpscreen\n"
       << "\n"
       << "     -q|--quiet           Only error messages are output to stderr no progress\n"
       << "\n"
       << "     -d|--debug           Much debugging ouput should be interesting for developer only\n"
       << "\n"
       << "     -s|--seed <seed>     Random seed for the tree, default is 13, but you won't all\n"
       << "                          trees look the same as mine, so giv something like -s 17 here\n"
       << "                          the seed is part of the  declaration string in the povray file\n"
       << "\n"
       << "    -l|--levels <level>  1..Levels+1 -- calc and ouput only so much levels, usefull for\n"
       << "                         fast testing of parameter changes or to get a draft tree for\n"
       << "                         a first impression of a scene without all that small stems and\n"
       << "                         leaves. Levels+1 means calc alle Levels and Leaves, but this\n"
       << "                         is the same as not giving this option here\n"
       << "\n"
       << "    -m|--mesh [<smooth>] Output stems as mesh2 objects the optional smooth value influences\n"
       << "                         how much vertices are used for every stem section and for which\n"
       << "                         levels normals should be used to hide the triangle borders\n"
       << "\n"
       << "    -c|--cones           output stems as unions of cones and spheres, Lobes don't work\n"
       << "                         with this option, but source files are a little bit smaller.\n"
       << "                         Povray read Mesh2 objects faster. Cones are handy for use with\n"
       << "                         KPovmodeler, which doesn't support mesh objects yet.\n"
       << "\n"
       << "    --treecfg            Input file is a simple Param=Value list. Needs less typing for\n"
       << "                         a new tree than writing XML code\n"
       << "\n"
       << "    -x|--xml             Output parameters as XML tree definition instead of creating\n"
       << "                         the tree and writing it as povray code. Useful for converting a\n"
       << "                         simple parameter list to a XML file: \n"
       << "                            arbaro.py --treecfg -x < mytree.cfg > mytree.xml\n"
       << "\n"
       << "example:\n"
       << "\n"
       << "    arbaro.py < trees/quaking_aspen.xml > pov/quaking_aspen.inc\n"
       << "\n";
	*/
    }

    static void read_cfg_tree_file(InputStream is) throws Exception {
	LineNumberReader r = new LineNumberReader(new InputStreamReader(is));
	String line = r.readLine();
	String param;
	String value;
	while (line != null) {
	    if (line != "" && line.charAt(0) != '#') {
		int equ = line.indexOf('=');
		param = line.substring(0,equ).trim();
		value = line.substring(equ+1).trim();
		if (param.equals("species")) {
		    tree.params.species = value;
		} else {
		    tree.params.setParam(param,value);
		}
		line = r.readLine();
	    }
	}
    }

    public static void main (String [] args) throws Exception{
	//	try {
	tree = new Tree();
      
	programname();

	/*
	bool quiet = false;
	bool debug = false;
	int seed = 13;
	int levels = -1;
	OutputFormat output=MESH;
	int smooth=-1;
	InputFormat input = XML;

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
  };

  int c;
  int digit_optind = 0;

  while (1) {
    int this_option_optind = optind ? optind : 1;
    int option_index = 0;
    c = getopt_long (argc, argv, "hvdl:s:m:cx",
		     long_options, &option_index);
    if (c == -1)
      break;

    switch (c) {

    case 'h': //  print help information and exit
      usage();
      exit(0);
      break;

    case 'd': 
      debug = true;
      break;

    case 'q':
      quiet = true;
      break;
      
    case 's':
      if (optarg) {
	seed = atoi(optarg);
      }
      break;

    case 'l':
      if (optarg) {
	levels = atoi(optarg);
      }
      break;

    case 'c':
      output = CONES;
      break;

    case 'm':
      output = MESH;
      if (optarg) {
	smooth = float(optarg);
      }
      break;

    case 'x':
      output = XML;
      break;

    case 'r':
      input = CFG;
      break;
	
    }
  }
    // rest of args should be files    
    // ...
    //if (optind < argc) {
    //   while (optind < argc)
    //                  argv[optind++]...
    //           }


    //########## read params from XML file ################

    // FIXME: put this in another file Params.cpp
    Paramset paramset;
	*/


    //############ create tree #########################

	/*
    cerr << "Reading params from stdin...\n";
    if (input == XML) {
      //read_xml_tree_file(sys.stdin)
    } else {
	*/
	tree.params.debug=true;
	
	read_cfg_tree_file(System.in);
      //    }

    
      /*
      tree.setParams(paramset);
      tree.debug=debug;
      tree.verbose=(! quiet);
      tree.Seed=seed;
      tree.output = output;

    if (smooth>=0) tree.Smooth = smooth;
    // FIXME: mesh_smooth settings rely on
    // Levels, so maybe there should be a
    // different variable to halt tree creation on
    // a specific level
    if (levels>=0 && levels<=tree.Levels) {
      tree.Levels=levels;
      tree.Leaves=0;
    }

    if (output==XML) {
      // save parameters in XML file, don't create tree
      tree.saveParams(cout);
    } else {

      */
      tree.make();

      PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out));
      tree.povray(out);
      //    }

      //    exit(0);
      //	} catch (Exception e) { throw e;}
    }

};

  











