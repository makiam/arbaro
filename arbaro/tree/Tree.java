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
import java.util.Enumeration;
import java.util.NoSuchElementException;

import net.sourceforge.arbaro.params.*;
import net.sourceforge.arbaro.transformation.*;
import net.sourceforge.arbaro.mesh.*;
import net.sourceforge.arbaro.output.*;

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
      
    // FIXME: instead of making trunks and stems public
    // there should be stem (an) iterator(s)
    
    // the trunks (one for trees, many for bushes)
    public java.util.Vector trunks;
    double trunk_rotangle = 0;

//    private class TreeEnumerator extends StemEnumerator {
//    	
//    	public TreeEnumerator(int level) {
//    		super(level);
//    		stems = trunks.elements();
//    	}
//    	
////    	public boolean hasMoreElements() {
////    		if (indepth != null) {
////    			return indepth.hasMoreElements();
////    		} else {
////    			return stems.hasMoreElements();
////    		}
////    	}
////    	
////    	public Object nextElement() {
////    		if (indepth != null && indepth.hasMoreElements()) {
////    			return indepth.nextElement();
////    		} else {
////    			Stem s = (Stem)stems.nextElement();
////    			indepth = s.allStems(level);
////    			return s;
////    		}
////    	}
//
//    	// FIXME: put this code in a common basic class "TreeEnumerator"
//    	// and use it for the two Enumerators in Stem and for this here
//    	public boolean hasMoreElements() {
//    		if (level<0) {
//    			// all level, are ther more stems
//    			// on this level or on higher level?
//    			return stems.hasMoreElements() ||
//				  (indepth != null && indepth.hasMoreElements());
//    		} else if (level>0) {
//    			find_stem_with_substems();
//    			return indepth != null && indepth.hasMoreElements();
//    		} else if (level==0) {
//    			return stems.hasMoreElements();
//    		} else {
//    			// shouldn't go here?
//    			return false;
//    		}
//    	}
//
//    	private void find_stem_with_substems() {
//    		while ((indepth==null || !indepth.hasMoreElements()) &&
//    				stems.hasMoreElements()) {
//    			Stem s = (Stem)stems.nextElement();
//    			indepth = s.allStems(level);
//    		}
//    	}
//
//    	public Object nextElement() {
//    		if (indepth != null && indepth.hasMoreElements()) {
//    			return indepth.nextElement();
//    		} else {
//    			if (level<0) { // consider all levels
//    				Stem s = (Stem)stems.nextElement();
//    				indepth = s.allStems(level);
//    				return s;
//    			} else {
//    				find_stem_with_substems();
//    				// FIXME: when indepth==null, wrong Exception type
//    				// is raised
//    				return indepth.nextElement();
//    			}
//    		}
//    	}
//
//    }

    // FIXME: may be could use StemEnumerator as basis
    // and overload only find_next_stem and getNext a little???
    private class LeafEnumerator implements Enumeration {
    	private Enumeration stems;
    	private Enumeration leaves;
    	
    	public LeafEnumerator() {
    		stems = trunks.elements();
    		leaves = ((Stem)stems.nextElement()).allLeaves();
    	}
    	
    	public boolean hasMoreElements() {
    		if (params.Leaves==0) return false;
			else if (leaves.hasMoreElements()) return true;
			else if (! stems.hasMoreElements()) return false;
			else {
				// goto next trunk with leaves
				while (! leaves.hasMoreElements() && stems.hasMoreElements()) {
					Stem s = (Stem)stems.nextElement();
					leaves = s.allLeaves();
				}
				return leaves.hasMoreElements();
			}
    	}
    	
    	public Object nextElement() {
    		// this will go to the next trunk with leaves,
    		// if the current has no more of them
    		if (hasMoreElements()) {
    			return leaves.nextElement();
    		} else {
    			throw new NoSuchElementException("LeafEnumerator");
    		}
    	}
    }
    
    public Enumeration allStems(int level) {
    	return new StemEnumerator(level,trunks.elements(),0);
    }
    
    public Enumeration allLeaves() {
    	return new LeafEnumerator();
    }

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

    public void output(PrintWriter w) throws Exception {
    	setupMaxOutProgress();
    	incOutProgress(1); // for the trunk
    	
    	// output povray code
    	if (params.verbose) System.err.print("writing tree code ");
    	
    	Output output;
    	if (params.output == Params.CONES) {
    		output = new PovConeOutput(this,w);
    		output.write();
    	} else if (params.output == Params.MESH) {
    		output = new PovMeshOutput(this,w);
    		output.write();
    	}
    	
    	if (params.verbose) System.err.println();
    }


    
    public Mesh createStemMesh() throws Exception {
    	Mesh mesh = new Mesh();
    	for (int t=0; t<trunks.size(); t++) {
    		((Stem)trunks.elementAt(t)).add_to_mesh(mesh);
    	}
    	return mesh;
    }

    public LeafMesh createLeafMesh() {
    	double leafLength = params.LeafScale/Math.sqrt(params.LeafQuality);
    	double leafWidth = params.LeafScale*params.LeafScaleX/Math.sqrt(params.LeafQuality);
    	return new LeafMesh(params.LeafShape,leafLength,leafWidth,params.LeafStemLen);
    }

    
  
    /**
     * Outputs a simple Povray scene showing the generated tree
     * 
     * @param w
     */
    public void outputScene(PrintWriter w) throws Exception {
    	Output output = new PovSceneOutput(this,w);
    	output.write();
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

    public long getLeafCount() {
    	if (params.Leaves==0) return 0;
    	
    	long leafCount = 0;
    	
    	for (int t=0; t<trunks.size(); t++) {
    		leafCount += ((Stem)trunks.elementAt(t)).leafCount();
    	}
    	return leafCount;
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
    long maxOutProgress;
    final float genProgressRatio = 0.6F; // if no output will occur it should be set to 1.0F
    long genProgress;
    long outProgress;
    boolean writingCode; 
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
    	outProgress = 0;
    	progressMsg = "creating tree structure";
    }

    /**
     * Sets the maximum for the progress while writing Povray code
     * 
     */
    public synchronized void setupMaxOutProgress() {
    	maxOutProgress = 0;
    	// max progress is the total number of substems of all trunks
    	for (int t=0; t<trunks.size(); t++) {
    		maxOutProgress += ((Stem)trunks.elementAt(t)).substemTotal();
    	}
    	// generation of leaves occurs in a second pass, so double the
    	// progress maximum
    	if (params.Leaves != 0) maxOutProgress += maxOutProgress;
    	genProgress = maxGenProgress;
    	outProgress = 0;
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
    	if (genProgressRatio > 0.999999 || maxOutProgress == 0) {
    		// System.err.println("progr: "+makeProgress/(float)makeProgressMax * makeProgressRatio);
    		return genProgress/(float)maxGenProgress * genProgressRatio;
    	} else {
    		return genProgress/(float)maxGenProgress * genProgressRatio
			+ outProgress/(float)maxOutProgress * (1-genProgressRatio); 
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
    public synchronized void incOutProgress(long inc) {
    	outProgress +=inc;
    }

};























