//  #**************************************************************************
//  #
//  #    Copyright (C) 2003-2006  Wolfram Diestel
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

import java.io.InputStream;
import java.io.PrintWriter;

import net.sourceforge.arbaro.params.*;
import net.sourceforge.arbaro.transformation.*;
import net.sourceforge.arbaro.export.Progress;

/**
 * @author wolfram
 *
 */
public class TreeGenerator extends GeneratorBase {
	Params par;
	LevelParams lpar;
	
	private int seed = 13;
	private TreeImpl tree;

/*	
	public int stemlevel; // the branch level, could be > 4
	double offset; // how far from the parent's base
	
	java.util.Vector segments; // the segments forming the stem
	java.util.Vector clones;      // the stem clones (for splitting)
	java.util.Vector substems;    // the substems
	java.util.Vector leaves;     // the leaves
	
	double length; 
	public double getLength() { return length; }
	double segmentLength;
	int segmentCount;
	double baseRadius;
	
	double lengthChildMax;
	double substemsPerSegment;
	double substemRotangle;
	
	double leavesPerSegment;
	double splitCorrection;
	
	boolean pruneTest; // flag for pruning cycles
	*/
	
	public TreeGenerator(Progress progress, boolean verbose, boolean debug) {
		super(progress, verbose, debug);
		par = new Params();
	}
	
	public TreeGenerator(Params params, Progress progress, boolean verbose, boolean debug) {
		super(progress, verbose, debug);
		par = new Params(params);
	}
	
	public void setSeed(int seed) {
		this.seed = seed;
	}
	
	public int getSeed() {
		return seed;
	}
	
	public Params getParams() {
		return par;
	}
	
	
	public void setParam(String param, String value) throws ParamError {
		par.setParam(param,value);
	}
	
	public AbstractParam getParam(String param) {
		return par.getParam(param);
	}
	
	/**
	 * Returns a parameter group
	 * 
	 * @param level The branch level (0..3)
	 * @param group The parameter group name
	 * @return A hash table with the parameters
	 */
	public java.util.TreeMap getParamGroup(int level, String group) {
		return par.getParamGroup(level,group);
	}

	/**
	 * Writes out the parameters to an XML definition file
	 * 
	 * @param out The output stream
	 * @throws ParamError
	 */
	public void writeParamsToXML(PrintWriter out) throws ParamError {
		par.toXML(out);
	}
	
	/**
	 * Clear all parameter values of the tree.
	 */
	public void clearParams() {
		par.clearParams();
	}
	
	/**
	 * Read parameter values from an XML definition file
	 * 
	 * @param is The input XML stream
	 * @throws ParamError
	 */
	public void readParamsFromXML(InputStream is) throws ParamError {
		par.readFromXML(is);
	}
	
	/**
	 * Read parameter values from an Config style definition file
	 * 
	 * @param is The input text stream
	 * @throws ParamError
	 */
	public void readParamsFromCfg(InputStream is) throws Exception {
		par.readFromCfg(is);
	}
	
	/**
	 * Sets the maximum for the progress while generating the tree 
	 */
	public void setupGenProgress() {
		if (progress != null) {
			// max progress = trunks * trunk segments * (first level branches + 1) 
			long maxGenProgress = 
				((IntParam)par.getParam("0Branches")).intValue()
				* ((IntParam)par.getParam("0CurveRes")).intValue()
				* (((IntParam)par.getParam("1Branches")).intValue()+1);
			
			progress.beginPhase("Creating tree structure",maxGenProgress);
		}
	}
	
	/**
	 * Sets (i.e. calcs) the progress for the process of making the tree
	 * object.
	 */
 	 long genProgress;
	
	 public synchronized void updateGenProgress() {
			try {
				// how much of 0Branches*0CurveRes*(1Branches+1) are created yet
				long sum = 0;
				for (int i=0; i<tree.trunks.size(); i++) {
					StemImpl trunk = ((StemImpl)tree.trunks.elementAt(i));
					if (trunk.substems != null) {
						sum += trunk.segments.size() * (trunk.substems.size()+1);
					} else {
						sum += trunk.segments.size();
					}
				}
				
				if (sum-genProgress > progress.getMaxProgress()/100) {
					genProgress = sum;
					progress.setProgress(genProgress);
				}
			} catch (Exception e) {
				System.err.println(e);
			}
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
	public TreeImpl makeTree(Progress progress) throws Exception {
		TreeImpl tree = new TreeImpl(seed);
		this.progress = progress;
		setupGenProgress();
		par.prepare(tree.getSeed());
		tree.maxPoint = new Vector(-Double.MAX_VALUE,-Double.MAX_VALUE,-Double.MAX_VALUE);
		tree.minPoint = new Vector(Double.MAX_VALUE,Double.MAX_VALUE,Double.MAX_VALUE);
		
		if (verbose) {
			// FIXME: move Seed back to Tree and give it to Params.prepare(Seed) only
			System.err.println("Tree species: " + par.Species + ", Seed: " 
					+ seed);
			
			System.err.println("making " + par.Species + "(" + seed + ") ");
		}		
		// create the trunk and all its stems and leaves
		Transformation transf = new Transformation();
		Transformation trf;
		double angle;
		double dist;
		LevelParams lpar = par.getLevelParams(0);
		StemGenerator stemFactory = new StemGenerator(tree,this);
		for (int i=0; i<lpar.nBranches; i++) {
			trf = trunkDirection(tree,transf,lpar);
			angle = lpar.var(360);
			dist = lpar.var(lpar.nBranchDist);
			trf = trf.translate(new Vector(dist*Math.sin(angle),
					dist*Math.cos(angle),0));
			StemImpl trunk = stemFactory.makeStem(null,0,trf,0);
			trunk.index=0;
			tree.trunks.addElement(trunk);
		}
		
		// making finished
		if (verbose) System.err.println(".");
		progress.endPhase();
		
		// set leafCount and stemCount for the tree
		if (par.Leaves==0) tree.setLeafCount(0);
		else {
			LeafCounter leafCounter = new LeafCounter();
			tree.setLeafCount(leafCounter.getLeafCount());
		}
		StemCounter stemCounter = new StemCounter();
		tree.setStemCount(stemCounter.getStemCount());
		
		return tree;
	}
	
	/**
	 * Calcs the trunk direction. Of special importance for plants with
	 * multiple trunks.
	 * 
	 * @param trf The original transformation
	 * @param lpar The parameters for the trunk (level 0)
	 * @return The transformation after giving the trunk a new direction
	 */
	Transformation trunkDirection(TreeImpl tree, Transformation trf, LevelParams lpar) {
		
		// get rotation angle
		double rotangle;
		if (lpar.nRotate>=0) { // rotating trunk
			tree.trunk_rotangle = (tree.trunk_rotangle + lpar.nRotate+lpar.var(lpar.nRotateV)+360) % 360;
			rotangle = tree.trunk_rotangle;
		} else { // alternating trunks
			if (Math.abs(tree.trunk_rotangle) != 1) tree.trunk_rotangle = 1;
			tree.trunk_rotangle = -tree.trunk_rotangle;
			rotangle = tree.trunk_rotangle * (180+lpar.nRotate+lpar.var(lpar.nRotateV));
		}
		
		// get downangle
		double downangle;
		downangle = lpar.nDownAngle+lpar.var(lpar.nDownAngleV);
		
		return trf.rotxz(downangle,rotangle);
	}
	
	


}
