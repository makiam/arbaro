//#**************************************************************************
//#
//#    $Id$  
//#      - Output class for writing Povray primitive objects
//#          
//#
//#    Copyright (C) 2004  Wolfram Diestel
//#
//#    This program is free software; you can redistribute it and/or modify
//#    it under the terms of the GNU General Public License as published by
//#    the Free Software Foundation; either version 2 of the License, or
//#    (at your option) any later version.
//#
//#    This program is distributed in the hope that it will be useful,
//#    but WITHOUT ANY WARRANTY; without even the implied warranty of
//#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//#    GNU General Public License for more details.
//#
//#    You should have received a copy of the GNU General Public License
//#    along with this program; if not, write to the Free Software
//#    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//#
//#    Send comments and bug fixes to diestel@steloj.de
//#
//#**************************************************************************/

package net.sourceforge.arbaro.output;

import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.Enumeration;

import net.sourceforge.arbaro.params.FloatFormat;
import net.sourceforge.arbaro.tree.*;
import net.sourceforge.arbaro.transformation.*;

/**
 * @author Wolfram Diestel
 */
public class PovConeOutput extends Output {

	private Progress progress;
	private long stems=0;
	private long leaves=0;
	
	/**
	 * @param aTree
	 * @param pw
	 */
	public PovConeOutput(Tree aTree, PrintWriter pw) {
		super(aTree, pw);
	}

	public void write() throws ErrorOutput {
		try {
			progress = tree.getProgress();
			povray();
	    	w.flush();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e);
			throw new ErrorOutput(e.getMessage());
		}
	}
	
	private void incStems() {
		if (stems++%100 == 0) progress.incProgress(100);
	}
	
	private void incLeaves() {
		if (leaves++%100 == 0) progress.incProgress(100);
	}

	/**
     * Returns a prefix for the Povray objects names,
     * it consists of the species name and the random seed
     * 
     * @return the prefix string
     */
    private String pov_prefix() {
    	return tree.getSpecies() + "_" + tree.params.Seed + "_";
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

    private void povray() throws Exception {
    	NumberFormat frm = FloatFormat.getInstance();
    	
    	// tree scale
    	w.println("#declare " + pov_prefix() + "scale = " 
    			+ frm.format(tree.params.scale_tree) + ";");
    	
    	// leaf declaration
    	if (tree.params.Leaves!=0) povray_leaf();
    	
    	// stems
    	progress.beginPhase("writing stem objects",tree.getStemCount());
    	
    	for (int level=0; level < tree.params.Levels; level++) {
    		w.println("#declare " + pov_prefix() + "stems_"
    				+ level + " = union {");
    		Enumeration stems = tree.allStems(level);
    		while (stems.hasMoreElements()) {
    			Stem s = (Stem)stems.nextElement();
    			povray_stems(s);
    			
    			incStems();
    		}
    		w.println("}");
    	}
    	
    	// leaves
    	if (tree.params.Leaves!=0) {
    		
        	progress.beginPhase("writing leaf objects",tree.getLeafCount());

        	w.println("#declare " + pov_prefix() + "leaves = union {");

        	Enumeration leaves = tree.allLeaves();
        	while (leaves.hasMoreElements()) {
        		Leaf l = (Leaf)leaves.nextElement();
        		leaf_povray(l);
    	
        		// FIXME: pogress still based on stem count, not leaf count
        		// so this gives wrong result
        		incLeaves();
        	}
        	
   			w.println("}");

    	} else { // empty declaration
    		w.println("#declare " + pov_prefix() + "leaves = sphere {<0,0,0>,0}"); 
    	}
    	
    	progress.endPhase();
    	
    	// all stems together
    	w.println("#declare " + pov_prefix() + "stems = union {"); 
    	for (int level=0; level < tree.params.Levels; level++) {
    		w.println("  object {" + pov_prefix() + "stems_" 
    				+ level + "}");
    	}
    	w.println("}");
    	
    	
    }

    /**
     * Outputs the Povray code for a leaf object
     * 
     * @param w The output stream
     */
    private void povray_leaf() {
    	double length = tree.params.LeafScale/Math.sqrt(tree.params.LeafQuality);
    	double width = tree.params.LeafScale*tree.params.LeafScaleX/Math.sqrt(tree.params.LeafQuality);
    	w.println("#include \"arbaro.inc\"");
    	w.println("#declare " + pov_prefix() + "leaf = " +
    			"object { Arb_leaf_" + (tree.params.LeafShape.equals("0")? "disc" : tree.params.LeafShape)
				+ " translate " + (tree.params.LeafStemLen+0.5) + "*y scale <" 
				+ width + "," + length + "," + width + "> }");
    }	  	


    
    /**
     * Output stem as Povray code
     * 
     * @param w the output stream
     * @param level the stem level
     * @throws Exception
     */
    private void povray_stems(Stem s) throws Exception {
    	// output povray code for one stem level
    	
    	if (tree.params.verbose) {
    		if (s.stemlevel<=1 && s.clone_index.size()==0) System.err.print(".");
    	}
    	
    	String indent = "    ";
    	
    	
    	//    		boolean union=false;
    	//    		if (s.segments.size()>1 || (s.substems != null && s.substems.size()>0)) union = true;
    	//    		
    	//    		if (union) w.print(indent + "union { ");
    	//    		w.println("/* " + s.tree_position() + " */");
    	
    	for (int i=0; i<s.segments.size(); i++) {
    		// FIXME: eliminate SEgment.povray()
    		povray_segment((Segment)s.segments.elementAt(i));
    	}
    	
    	//    		if (union) w.println(indent + "}");
    	

        //    		tree.incPovProgress(s.substems.size());
    } 
    
    /**
     * Outputs Povray code for the segment
     * 
     * @param w the output stream
     */
    public void povray_segment(Segment s) {
	String indent = whitespace(s.lpar.level*2+4);
	NumberFormat fmt = FloatFormat.getInstance();
    
	// FIXME: for cone output - if starting direction is not 1*y, there is a gap 
	// between earth and tree base
	// best would be to add roots to the trunk(?)	  
    
	for (int i=0; i<s.subsegs.size()-1; i++) {
	    Subsegment ss1 = (Subsegment)s.subsegs.elementAt(i);
	    Subsegment ss2 = (Subsegment)s.subsegs.elementAt(i+1);
	    w.println(indent + "cone   { " + ss1.pos.povray() + ", "
		      + fmt.format(ss1.rad) + ", " 
		      + ss2.pos.povray() + ", " 
		      + fmt.format(ss2.rad) + " }"); 
	    // for helix subsegs put spheres between
	    if (s.lpar.nCurveV<0 && i<s.subsegs.size()-2) {
		w.println(indent + "sphere { " 
			  + ss1.pos.povray() + ", "
			  + fmt.format(ss1.rad-0.0001) + " }");
	    }
	}
    
	// put sphere at segment end
	if ((s.rad2 > 0) && (! s.is_last_stem_segment() || 
			   (s.lpar.nTaper>1 && s.lpar.nTaper<=2))) 
	    {  
		w.println(indent + "sphere { " + s.pos_to().povray() + ", "
			  + fmt.format(s.rad2-0.0001) + " }");
	    }
    }

    /**
     * Returns a number of spaces
     * 
     * @param len number of spaces
     * @return string made from spaces
     */
    private String whitespace(int len) {
	char[] ws = new char[len];
	for (int i=0; i<len; i++) {
	    ws[i] = ' ';
	}
	return new String(ws);
    }
  

    /**
     * Outputs the Povray code for the leaves as primitives (e.g. discs)
     * 
     * @param w the output stream
     * @throws Exception
     */
//    void povray_leaves_objs(Stem s) throws Exception {
//    	Enumeration leaves = tree.allLeaves();
//    	String indent = "    ";
//    	
//    	while (leaves.hasMoreElements()) {
//    		Leaf l = (Leaf)leaves.nextElement();
//    		leaf_povray(l);
//	
//    		// FIXME: pogress still based on stem count, not leaf count
//    		// so this gives wrong result
//    		progress++;
//			if (progress%100==0) tree.incOutProgress(100);
//    	}
    	
//    	if (tree.params.verbose) {
//    		if (s.stemlevel<=1 && s.clone_index.size()==0) System.err.print(".");
//    	}
//    	
//    	String indent = "    ";
//    	
//    	// output leaves
//    	if (s.stemlevel==tree.params.Levels-1) {
//    		boolean union=false;
//    		if (s.leaves.size()>0) union=true;
//    		
//    		if (union) w.print(indent + "union { ");
//    		w.println( "/* " + s.tree_position() + " */");
//    		
//    		for (int i=0; i<s.leaves.size(); i++) {
//    			leaf_povray((Leaf)s.leaves.elementAt(i));
//    		}
//    		
//    		if (s.clones != null) {
//    			for (int i=0; i<s.clones.size(); i++) {
//    				povray_leaves_objs((Stem)s.clones.elementAt(i));
//    			}
//    		}
//    		
//    		if (union) w.println(indent + "}");
//    	}
//    	
//    	// recursive call to substems
//    	else {
//    		// FIXME? more correct it would be inc by 1 in the for block,
//    		// but this would need more calls to synchronized incProgress
//    		// if this work ok, don't change this
//    		tree.incPovProgress(s.substems.size());
//    		for (int i=0; i<s.substems.size(); i++) {
//    			povray_leaves_objs((Stem)s.substems.elementAt(i));
//    		}
//    		if (s.clones != null) {
//    			for (int i=0; i<s.clones.size(); i++) {
//    				povray_leaves_objs((Stem)s.clones.elementAt(i));
//    			}	  	
//    		}
//    		
//    	}
//    }
    
    /**
     * Outputs leaves as Povray code when primitive output is used
     * 
     * @param w the output stream
     */
    public void leaf_povray(Leaf l) {
    	// prints povray code for the leaf
    	String indent = "    ";
    	
    	//mid = self.position+self.direction # what about connecting stem len?
    	// FIXME: move "scale..." to tree
    	/*
    	 w.println(indent + "object { " + par.getSpecies() + "_" + par.Seed 
    	 + "_leaf " + "scale <" + width + "," + length + "," + width + "> "
    	 + transf.povray()+"}");
    	 */
    	w.println(indent + "object { " + pov_prefix() + "leaf " + transf_povray(l.transf)+"}");
    }
    
    private String transf_povray(Transformation trf) {
    	NumberFormat fmt = FloatFormat.getInstance();
    	Matrix matrix = trf.matrix();
    	Vector vector = trf.vector();
    	return "matrix <" 
	    + fmt.format(matrix.get(trf.X,trf.X)) + "," 
    	    + fmt.format(matrix.get(trf.Z,trf.X)) + "," 
    	    + fmt.format(matrix.get(trf.Y,trf.X)) + ","
    	    + fmt.format(matrix.get(trf.X,trf.Z)) + "," 
    	    + fmt.format(matrix.get(trf.Z,trf.Z)) + "," 
    	    + fmt.format(matrix.get(trf.Y,trf.Z)) + ","
    	    + fmt.format(matrix.get(trf.X,trf.Y)) + "," 
    	    + fmt.format(matrix.get(trf.Z,trf.Y)) + "," 
    	    + fmt.format(matrix.get(trf.Y,trf.Y)) + ","
    	    + fmt.format(vector.getX())   + "," 
    	    + fmt.format(vector.getZ())   + "," 
    	    + fmt.format(vector.getY()) + ">";
        }

    
};
