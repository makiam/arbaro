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

package net.sourceforge.arbaro.tree;

import java.io.PrintWriter;
import java.io.InputStream;
import java.lang.Math;

import java.text.NumberFormat;
import net.sourceforge.arbaro.params.*;
import net.sourceforge.arbaro.transformation.*;
import net.sourceforge.arbaro.mesh.*;

/**
 * A class for creation of threedimensional tree objects.
 * A tree has one or more trunks, with several levels of
 * branches all of which are instances of the Stem class.
 * <p>
 * See this class diagram for the parts of a Tree:
 * <p>
 * <img src="doc-files/Tree-1.png" />
 * <p>
 * 
 * @author Wolfram Diestel
 *
 */
public class Tree {
	/*
	 * TODO progress information aren't consistent any more,
	 * maybe rechange the povray output to depth first before
	 * correcting the progress calculation.
	 */

    public Params params;
      
    // the trunks (one for trees, many for bushes)
    java.util.Vector trunks;
    double trunk_rotangle = 0;

    /**
     * Creates a new tree object 
     */
    public Tree() {
	params = new Params();
	trunks = new java.util.Vector();
    }

    /**
     * Creates a new tree object copying the parameters
     * from an other tree
     * 
     * @param other the other tree, from wich parameters are taken
     */
    public Tree(Tree other) {
	params = new Params(other.params);
	trunks = new java.util.Vector();
    }
    
    /**
     * Generates the tree. The following collaboration diagram
     * shows the recursion trough the make process:
     * <p>
     * <img src="doc-files/Tree-2.png" />
     * <p> 
     * 
     * @throws Exception
     */
    public void make() throws Exception {
    	setupMaxGenProgress();
    	params.prepare();
    	
    	if (params.verbose) {
    		// FIXME: move Seed back to Tree and give it to Params.prepare(Seed) only
    		System.err.println("Tree species: " + getSpecies() + ", Seed: " 
    				+ params.Seed);
    		System.err.println("Output: " + (params.output == Params.MESH? "mesh":"cones"));
    		if (params.output==Params.MESH) { 
    			for (int l=0; l<Math.min(params.Levels,4); l++) {
    				System.err.println("  Level " + l + ": vertices/section: " 
    						+ params.levelParams[l].mesh_points + ", smooth: " 
							+ (params.smooth_mesh_level>=l? "yes" : "no"));
    			}
    		}
    		
    		System.err.println("making " + getSpecies() + "(" + params.Seed + ") ");
    	}
    	
    	// create the trunk and all its stems and leaves
    	Transformation transf = new Transformation();
    	Transformation trf;
    	double angle;
    	double dist;
    	LevelParams lpar = params.levelParams[0];
    	for (int i=0; i<lpar.nBranches; i++) {
    		trf = trunk_direction(transf,lpar);
    		angle = lpar.var(360);
    		dist = lpar.var(lpar.nBranchDist);
    		trf = trf.translate(new Vector(dist*Math.sin(angle),
    				dist*Math.cos(angle),0));
    		Stem trunk = new Stem(this,params,params.levelParams[0],null,0,trf,0);
    		trunks.addElement(trunk);
    		trunk.index=0;
    		trunk.make();
    	}
    	
    	// making finished
    	if (params.verbose) System.err.println(".");
    }
    
    /**
     * Calcs the trunk direction. Of special importance for plants with
     * multiple trunks.
     * 
     * @param trf The original transformation
     * @param lpar The parameters for the trunk (level 0)
     * @return The transformation after giving the trunk a new direction
     */
    Transformation trunk_direction(Transformation trf, LevelParams lpar) {

	// get rotation angle
	double rotangle;
	if (lpar.nRotate>=0) { // rotating trunk
	    trunk_rotangle = (trunk_rotangle + lpar.nRotate+lpar.var(lpar.nRotateV)+360) % 360;
	    rotangle = trunk_rotangle;
	} else { // alternating trunks
	    if (Math.abs(trunk_rotangle) != 1) trunk_rotangle = 1;
	    trunk_rotangle = -trunk_rotangle;
	    rotangle = trunk_rotangle * (180+lpar.nRotate+lpar.var(lpar.nRotateV));
	}

	// get downangle
	double downangle;
	downangle = lpar.nDownAngle+lpar.var(lpar.nDownAngleV);
	  
	return trf.rotxz(downangle,rotangle);
    }

  
    /**
     * Returns a prefix for the Povray objects names,
     * it consists of the species name and the random seed
     * 
     * @return the prefix string
     */
    private String pov_prefix() {
    	return getSpecies() + "_" + params.Seed + "_";
    }

    /**
     * Outputs the Povray code for the tree.
     * The following diagrams show the process of mesh
     * creation and output for the stems and leave output:
     * <p>
     * <img src="doc-files/Tree-3.png" />
     * <p>
     * <img src="doc-files/Tree-4.png" />
     * <p>
     * 
     * @param w the output stream
     * @throws Exception
     */
    public void povray(PrintWriter w) throws Exception {
	setupMaxPovProgress();
	incPovProgress(1); // for the trunk

	NumberFormat frm = FloatFormat.getInstance();
	
	// output povray code
	if (params.verbose) System.err.print("writing povray code ");
	  
	// tree scale
	w.println("#declare " + pov_prefix() + "scale = " 
		  + frm.format(params.scale_tree) + ";");

	// leaf declaration
	if (params.Leaves!=0 && params.output == Params.CONES) povray_leaf(w);
	  	  
	// stems
	if (params.output == Params.CONES) {
	    for (int level=0; level < params.Levels; level++) {
		w.println("#declare " + pov_prefix() + "stems_"
			  + level + " = union {"); 
		for (int i=0; i<trunks.size(); i++) {
		    ((Stem)trunks.elementAt(i)).povray_stems(w,level);
		}
		w.println("}");
	    }

	} else if (params.output == Params.MESH) {
	    Mesh mesh = new Mesh();
	    w.println("#declare " + pov_prefix() + "stems = "); 
		for (int t=0; t<trunks.size(); t++) {
		    ((Stem)trunks.elementAt(t)).add_to_mesh(mesh);
		}
	    // FIXME: instead of output_normals = true use separate bool for every
	    // level
	    mesh.povray(w,true,"    ");
	}

	// leaves
	if (params.Leaves!=0) {

	    if (params.output == Params.CONES) {
		w.println("#declare " + pov_prefix() + "leaves = union {");
		for (int t=0; t<trunks.size(); t++) {
		    ((Stem)trunks.elementAt(t)).povray_leaves_objs(w);
		}
		w.println("}");
	    } else if (params.output == Params.MESH) {
		double leafLength = params.LeafScale/Math.sqrt(params.LeafQuality);
		double leafWidth = params.LeafScale*params.LeafScaleX/Math.sqrt(params.LeafQuality);
		LeafMesh mesh = new LeafMesh(params.LeafShape,leafLength,leafWidth,params.LeafStemLen);

		long leafCount = 0;
		for (int t=0; t<trunks.size(); t++) {
		    leafCount += ((Stem)trunks.elementAt(t)).leafCount();
		}
		w.println("#declare " + pov_prefix() + "leaves = mesh2 {");
		w.println("     vertex_vectors { "+mesh.getShapeVertexCount()*leafCount);
		for (int t=0; t<trunks.size(); t++) {
		    ((Stem)trunks.elementAt(t)).povray_leaves_points(w,mesh);
		}
		w.println("     }");
		/* FIXME: add this if needed
		w.println("     normal_vectors { "+mesh.getShapeVertexCount()*leafCount);
		trunk.povray_leaves_normals(w,mesh);
		w.println("     }");
		*/
		w.println("     face_indices { "+mesh.getShapeFaceCount()*leafCount);
		for (int t=0; t<trunks.size(); t++) {
		    ((Stem)trunks.elementAt(t)).povray_leaves_faces(w,mesh);
		}
		w.println("     }");
		w.println("}");
	    }
	} else { // empty declaration
	    w.println("#declare " + getSpecies() + "_" + params.Seed 
		      + "_leaves = sphere {<0,0,0>,0}"); 
	}

	// all stems together
	if (params.output == Params.CONES) {
	    w.println("#declare " + pov_prefix() + "stems = union {"); 
	    for (int level=0; level < params.Levels; level++) {
		w.println("  object {" + pov_prefix() + "stems_" 
			  + level + "}");
	    }
	    w.println("}");
	}

	w.flush();

	if (params.verbose) System.err.println();
    }
    
    /**
     * Outputs the Povray code for a leaf object
     * (only for primitives output, not for mesh)
     * 
     * @param w The output stream
     */
    void povray_leaf(PrintWriter w) {
	double length = params.LeafScale/Math.sqrt(params.LeafQuality);
	double width = params.LeafScale*params.LeafScaleX/Math.sqrt(params.LeafQuality);
	w.println("#include \"arbaro.inc\"");
	w.println("#declare " + pov_prefix() + "leaf = " +
		"object { Arb_leaf_" + (params.LeafShape.equals("0")? "disc" : params.LeafShape)
		  + " translate " + (params.LeafStemLen+0.5) + "*y scale <" 
		  + width + "," + length + "," + width + "> }");
    }	  	

    /**
     * Outputs a simple Povray scene showing the generated tree
     * 
     * @param w
     */
    public void povray_scene(PrintWriter w) {
	w.println("// render as 600x400");

	w.println("#include \"" + getSpecies() + ".inc\"");
	w.println("background {rgb <0.95,0.95,0.9>}");

	w.println("light_source { <5000,5000,-3000>, rgb 1.2 }");
	w.println("light_source { <-5000,2000,3000>, rgb 0.5 shadowless }");

	w.println("#declare HEIGHT = " + pov_prefix() + "scale * 1.3;");
	w.println("#declare WIDTH = 2*HEIGHT/3;");

	w.println("camera { orthographic location <0, HEIGHT*0.45, -100>");
	w.println("         right <WIDTH, 0, 0> up <0, HEIGHT, 0>");
	w.println("         look_at <0, HEIGHT*0.45, -80> }");

	w.println("union { ");
	w.println("         object { " + pov_prefix() + "stems");
	w.println("                pigment {color rgb 0.9} }"); 
	w.println("         object { " + pov_prefix() + "leaves");
	w.println("                texture { pigment {color rgb 1} ");
	w.println("                          finish { ambient 0.15 diffuse 0.8 }}}");
	w.println("         rotate 90*y }");

	if (params.Leaves > 0) {
	    w.println("         object { " + pov_prefix() + "stems");
	    w.println("                scale 0.7 rotate 45*y");  
	    w.println("                translate <WIDTH*0.33,HEIGHT*0.33,WIDTH>");
	    w.println("                pigment {color rgb 0.9} }"); 
	}
	w.flush();
    }


    /*
void Tree::dump() const {
    cout << "TREE:\n";
    // trunk.dump();
}
    */

    /**
     * Returns a parameter group
     * 
     * @param level The branch level (0..3)
     * @param group The parameter group name
     * @return A hash table with the parameters
     */
    public java.util.Hashtable getParamGroup(int level, String group) {
	return params.getParamGroup(level,group);
    }

    /**
     * Clear all parameter values of the tree.
     */
    public void clearParams() {
	params.clearParams();
    }

    /**
     * Read parameter values from an XML definition file
     * 
     * @param is The input XML stream
     * @throws ErrorParam
     */
    public void readFromXML(InputStream is) throws ErrorParam {
	params.readFromXML(is);
    }

    /**
     * Writes out the parameters to an XML definition file
     * 
     * @param out The output stream
     * @throws ErrorParam
     */
    public void toXML(PrintWriter out) throws ErrorParam {
	params.toXML(out);
    }

    /**
     * Sets the species name of the tree
     * 
     * @param sp
     */
    public void setSpecies(String sp) {
	params.setSpecies(sp);
    }

    /**
     * Returns the species name of the tree
     * 
     * @return the species name
     */
    public String getSpecies() {
	return params.getSpecies();
    }

    /**
     * Returns the random seed for the tree
     * 
     * @return the random seed
     */
    public int getSeed() {
	return params.Seed;
    }

    /**
     * Sets the random seed for the tree
     * 
     * @param s
     */
    public void setSeed(int s) {
	params.Seed = s;
    }

    /**
     * Returns the smooth value. It influences the number of vertices 
     * and usage of vertice normals in the generated mesh,
     * 
     * @return the smooth value (0.0...1.0)
     */
    public double getSmooth() {
	return params.Smooth;
    }

    /**
     * Sets the smooth value. It influences the number of vertices 
     * and usage of vertice normals in the generated mesh,
     */
    public void setSmooth(double s) {
	params.Smooth = s;
    }

    /**
     * Sets the output type for the Povray code 
     * (primitives like cones, spheres and discs or
     * triangle meshes)
     * 
     * @param output
     */
    public void setOutput(int output) {
	params.output = output;
    }

    /*** calculation of progress */
    long maxGenProgress;
    long maxPovProgress;
    final float genProgressRatio = 0.6F; // if no output will occur it should be set to 1.0F
    long genProgress;
    long povProgress;
    boolean writingPovray; 
    String progressMsg = "";

    /**
     * Sets the maximum for the progress while generating the tree 
     */
    public synchronized void setupMaxGenProgress() {
    	// max progress = trunks * trunk segments * (first level branches + 1) 
    	maxGenProgress = 
    		((IntParam)params.getParam("0Branches")).intValue()
			* ((IntParam)params.getParam("0CurveRes")).intValue()
			* (((IntParam)params.getParam("1Branches")).intValue()+1);
    	genProgress = 0;
    	povProgress = 0;
    	progressMsg = "creating tree structure";
    }

    /**
     * Sets the maximum for the progress while writing Povray code
     * 
     */
    public synchronized void setupMaxPovProgress() {
    	maxPovProgress = 0;
    	// max progress is the total number of substems of all trunks
    	for (int t=0; t<trunks.size(); t++) {
    		maxPovProgress += ((Stem)trunks.elementAt(t)).substemTotal();
    	}
    	// generation of leaves occurs in a second pass, so double the
    	// progress maximum
    	if (params.Leaves != 0) maxPovProgress += maxPovProgress;
    	genProgress = maxGenProgress;
    	povProgress = 0;
    	progressMsg = "writing povray code";
    }

    /**
     * Returns the progress of making the tree object and writing
     * it's Povray code.
     * 
     * @return a progress ratio between 0.0 and 1.0
     */
    public synchronized float getProgress() {
    	// System.err.println("mMax:"+makeProgressMax+" m:"+makeProgress
    	//		   +" pMax:"+povrayProgressMax+" p:"+povrayProgress);
    	if (maxGenProgress == 0) return 0;
    	if (genProgressRatio > 0.999999 || maxPovProgress == 0) {
    		// System.err.println("progr: "+makeProgress/(float)makeProgressMax * makeProgressRatio);
    		return genProgress/(float)maxGenProgress * genProgressRatio;
    	} else {
    		return genProgress/(float)maxGenProgress * genProgressRatio
			+ povProgress/(float)maxPovProgress * (1-genProgressRatio); 
    	}
    }
    
    /**
     * Returns a progress message, that describes what is
     * going on, e.g. "writing Povray code..."
     * 
     * @return the message string
     */
    public synchronized String getProgressMsg() {
    	return progressMsg;
    }

    /**
     * Sets (i.e. calcs) the progress for the process of making the tree
     * object.
     */
    public synchronized void updateGenProgress() {
    	// how much of 0Branches*0CurveRes*(1Branches+1) are created yet
    	long sum = 0;
		for (int i=0; i<trunks.size(); i++) {
		    Stem trunk = ((Stem)trunks.elementAt(i));
		    if (trunk.substems != null) {
		    	sum += trunk.segments.size() * (trunk.substems.size()+1);
		    } else {
		    		sum += trunk.segments.size();
		    }
		}
		genProgress = sum;
		
		// System.err.println(""+genProgress+"/"+maxGenProgress);
    }
    
    /*    public synchronized void updatePovProgress(int prog) {
	  povProgress = prog;
	}*/
    
    
    /**
     * Incerements the progress for the process of writing Povray code
     * of the generated tree
     * 
     * @param inc the increment for the progress
     */
    public synchronized void incPovProgress(long inc) {
    	povProgress +=inc;
    }

};























