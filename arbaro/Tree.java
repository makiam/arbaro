//  #**************************************************************************
//  #
//  #    $Id$  
//  #          - tree class - it generates the tree beginning from the trunk
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
import params.Params;
import transformation.Transformation;

class Tree {
    //A class for creation of threedimensional tree objects

    // FIXME: create set-functions for some parameters like seed, debug a.s.o.
    public Params params;
      
    // the trunk
    // FIXME: introduce new parameters nBranches[0], nDownAngle[0], nRotate[0]
    // to make bushes and oblique trees
    Stem trunk;

    public Tree() {
	params = new Params();
   }
    
    public void make() throws Exception {
	params.prepare();
	  
	if (params.verbose) {
	    // FIXME: move Seed back to Tree and give it to Params.prepare(Seed) only
	    System.err.println("Tree species: " + params.species + ", Seed: " 
			       + params.Seed);
	    System.err.println("Output: " + (params.output == Params.MESH? "mesh":"cones"));
	    if (params.output==Params.MESH) { 
		for (int l=0; l<Math.min(params.Levels,4); l++) {
		    System.err.println("  Level " + l + ": vertices/section: " 
				       + params.levelParams[l].mesh_points + ", smooth: " 
				       + (params.smooth_mesh_level>=l? "yes" : "no"));
		}
	    }
	  		
	    System.err.println("making " + params.species + "(" + params.Seed + ") ");
	}

	// create the trunk and all its stems and leaves
	Transformation transf = new Transformation();
	trunk = new Stem(params,params.levelParams[0],null,0,transf,0);
 	trunk.index=0;
	trunk.make();

	// making finished
	if (params.verbose) System.err.println(".");
    }
  
    public void povray(PrintWriter w) throws Exception {
	// output povray code
	if (params.verbose) System.err.print("writing povray code ");
	  
	// leaf declaration
	if (params.Leaves!=0) povray_leaf(w);
	  	  
	// stems
	for (int level=0; level < params.Levels; level++) {
	    w.println("#declare " + params.species + "_" + params.Seed + "_stems_"
		      + level + " = union {"); 
	    trunk.povray(w,level);
	    w.println("}");
	}

	// leaves
	if (params.Leaves!=0) {
	    w.println("#declare " + params.species + "_" + params.Seed + "_leaves = union {");
	    // FIXME split STem.povay into Stem.povray_stems and STem.povray_leaves
	    trunk.povray(w,params.Levels);
	    w.println("}");
	} else { // empty declaration
	    w.println("#declare " + params.species + "_" + params.Seed 
		      + "_leaves = sphere {<0,0,0>,0}"); 
	}

	// all stems together
	w.println("#declare " + params.species + "_" + params.Seed + "_stems = union {"); 
	for (int level=0; level < params.Levels; level++) {
	    w.println("  object {" + params.species + "_" + params.Seed + "_stems_" 
		      + level + "}");
	}
	w.println("}");
	w.flush();

	if (params.verbose) System.err.println();
    }
    
    void povray_leaf(PrintWriter w) {
	w.println("#include \"arbaro.inc\"");
	w.println("#declare " + params.species + "_" + params.Seed + "_leaf = " +
		"object { Arb_leaf_" + (params.LeafShape.equals("0")? "disc" : params.LeafShape)
		+ " translate " + (params.LeafStemLen+0.5) + "*y }");
    }	  	

    /*
void Tree::dump() const {
    cout << "TREE:\n";
    // trunk.dump();
}
    */
};























