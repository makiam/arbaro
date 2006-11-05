//#**************************************************************************
//#
//#    $Id:PovConeOutput.java 77 2006-11-05 11:46:01 +0000 (So, 05 Nov 2006) wolfram $  
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

package net.sourceforge.arbaro.export;

import java.io.PrintWriter;
import net.sourceforge.arbaro.tree.*;

/**
 * @author Wolfram Diestel
 */
public class PovConeOutput extends Exporter {
	
	/*
	private Progress progress;
	private long stemsProgressCount=0;
	private long leavesProgressCount=0;
	*/
	
	/**
	 * @param aTree
	 * @param pw
	 */
	public PovConeOutput(Tree tree, PrintWriter pw, Progress progress) {
		super(tree, pw, progress);
	}
	
	public void write() throws ErrorOutput {
//		try {
//			progress = tree.getProgress();
//			writePovrayCode();
//			w.flush();
//		} catch (Exception e) {
//			e.printStackTrace();
//			System.err.println(e);
//			throw new ErrorOutput(e.getMessage());
//		}
	}
//	
//	private void incStemsProgressCount() {
//		if (stemsProgressCount++%100 == 0) {
//			progress.incProgress(100);
//			if (tree.params.verbose) System.err.print(".");
//		}
//	}
//	
//	private void incLeavesProgressCount() {
//		if (leavesProgressCount++%500 == 0) {
//			progress.incProgress(500);
//			if (tree.params.verbose) System.err.print(".");
//		}
//	}
//	
//	/**
//	 * Returns a prefix for the Povray objects names,
//	 * it consists of the species name and the random seed
//	 * 
//	 * @return the prefix string
//	 */
//	private String povrayDeclarationPrefix() {
//		return tree.params.Species + "_" + tree.params.Seed + "_";
//	}
//	
//	/**
//	 * Outputs the Povray code for the tree.
//	 * The following diagrams show the process of mesh
//	 * creation and output for the stems and leave output:
//	 * <p>
//	 * <img src="doc-files/Tree-3.png" />
//	 * <p>
//	 * <img src="doc-files/Tree-4.png" />
//	 * <p>
//	 * 
//	 * @param w the output stream
//	 * @throws Exception
//	 */
//	
//	private void writePovrayCode() throws Exception {
//		NumberFormat frm = FloatFormat.getInstance();
//		
//		// tree scale
//		w.println("#declare " + povrayDeclarationPrefix() + "height = " 
//				+ frm.format(tree.getHeight()) + ";");
//		
//		// leaf declaration
//		if (tree.params.Leaves!=0) writeLeafDeclaration();
//		
//		// stems
//		progress.beginPhase("writing stem objects",tree.getStemCount());
//		
//		for (int level=0; level < tree.params.Levels; level++) {
//			w.println("#declare " + povrayDeclarationPrefix() + "stems_"
//					+ level + " = union {");
//			Enumeration stems = tree.allStems(level);
//			while (stems.hasMoreElements()) {
//				Stem s = (Stem)stems.nextElement();
//				writeStem(s);
//				
//				incStemsProgressCount();
//			}
//			w.println("}");
//		}
//		
//		// leaves
//		if (tree.params.Leaves!=0) {
//			
//			progress.beginPhase("writing leaf objects",tree.getLeafCount());
//			
//			w.println("#declare " + povrayDeclarationPrefix() + "leaves = union {");
//			
//			Enumeration leaves = tree.allLeaves();
//			while (leaves.hasMoreElements()) {
//				Leaf l = (Leaf)leaves.nextElement();
//				writeLeafObject(l);
//				
//				// increment progress count
//				incLeavesProgressCount();
//			}
//			
//			w.println("}");
//			
//		} else { // empty declaration
//			w.println("#declare " + povrayDeclarationPrefix() + "leaves = sphere {<0,0,0>,0}"); 
//		}
//		
//		progress.endPhase();
//		
//		// all stems together
//		w.println("#declare " + povrayDeclarationPrefix() + "stems = union {"); 
//		for (int level=0; level < tree.params.Levels; level++) {
//			w.println("  object {" + povrayDeclarationPrefix() + "stems_" 
//					+ level + "}");
//		}
//		w.println("}");
//		
//		
//	}
//	
//	/**
//	 * Outputs the Povray code for a leaf object
//	 * 
//	 * @param w The output stream
//	 */
//	private void writeLeafDeclaration() {
//		double length = tree.params.LeafScale/Math.sqrt(tree.params.LeafQuality);
//		double width = tree.params.LeafScale*tree.params.LeafScaleX/Math.sqrt(tree.params.LeafQuality);
//		w.println("#include \"arbaro.inc\"");
//		w.println("#declare " + povrayDeclarationPrefix() + "leaf = " +
//				"object { Arb_leaf_" + (tree.params.LeafShape.equals("0")? "disc" : tree.params.LeafShape)
//				+ " translate " + (tree.params.LeafStemLen+0.5) + "*y scale <" 
//				+ width + "," + length + "," + width + "> }");
//	}	  	
//	
//	
//	
//	/**
//	 * Output stem as Povray code
//	 * 
//	 * @param w the output stream
//	 * @param level the stem level
//	 * @throws Exception
//	 */
//	private void writeStem(Stem s) throws Exception {
//		// output povray code for one stem level
//		
//		for (Enumeration segments = s.stemSegments(); 
//			segments.hasMoreElements();) 
//		{
//			writeSegment((Segment)segments.nextElement());
//		}
//	} 
//	
//	/**
//	 * Outputs Povray code for the segment
//	 * 
//	 * @param w the output stream
//	 */
//	public void writeSegment(Segment s) {
//		String indent = whitespace(s.lpar.level*2+4);
//		NumberFormat fmt = FloatFormat.getInstance();
//		
//		// FIXME: for cone output - if starting direction is not 1*y, there is a gap 
//		// between earth and tree base
//		// best would be to add roots to the trunk(?)	  
//		
//		for (int i=0; i<s.subsegments.size()-1; i++) {
//			Subsegment ss1 = (Subsegment)s.subsegments.elementAt(i);
//			Subsegment ss2 = (Subsegment)s.subsegments.elementAt(i+1);
//			w.println(indent + "cone   { " + vectorStr(ss1.pos) + ", "
//					+ fmt.format(ss1.rad) + ", " 
//					+ vectorStr(ss2.pos) + ", " 
//					+ fmt.format(ss2.rad) + " }"); 
//			// for helix subsegs put spheres between
//			if (s.lpar.nCurveV<0 && i<s.subsegments.size()-2) {
//				w.println(indent + "sphere { " 
//						+ vectorStr(ss1.pos) + ", "
//						+ fmt.format(ss1.rad-0.0001) + " }");
//			}
//		}
//		
//		// put sphere at segment end
//		if ((s.rad2 > 0) && (! s.isLastStemSegment() || 
//				(s.lpar.nTaper>1 && s.lpar.nTaper<=2))) 
//		{  
//			w.println(indent + "sphere { " + vectorStr(s.posTo()) + ", "
//					+ fmt.format(s.rad2-0.0001) + " }");
//		}
//	}
//	
//	/**
//	 * Returns a number of spaces
//	 * 
//	 * @param len number of spaces
//	 * @return string made from spaces
//	 */
//	private String whitespace(int len) {
//		char[] ws = new char[len];
//		for (int i=0; i<len; i++) {
//			ws[i] = ' ';
//		}
//		return new String(ws);
//	}
//	
//	/**
//	 * Outputs a leaf object as Povray code. 
//	 * 
//	 * @param w the output stream
//	 */
//	public void writeLeafObject(Leaf l) {
//		// prints povray code for the leaf
//		String indent = "    ";
//		
//		w.println(indent + "object { " + povrayDeclarationPrefix() + "leaf " 
//				+ transformationStr(l.transf)+"}");
//	}
//	
//	private String transformationStr(Transformation trf) {
//		NumberFormat fmt = FloatFormat.getInstance();
//		Matrix matrix = trf.matrix();
//		Vector vector = trf.vector();
//		return "matrix <" 
//		+ fmt.format(matrix.get(trf.X,trf.X)) + "," 
//		+ fmt.format(matrix.get(trf.Z,trf.X)) + "," 
//		+ fmt.format(matrix.get(trf.Y,trf.X)) + ","
//		+ fmt.format(matrix.get(trf.X,trf.Z)) + "," 
//		+ fmt.format(matrix.get(trf.Z,trf.Z)) + "," 
//		+ fmt.format(matrix.get(trf.Y,trf.Z)) + ","
//		+ fmt.format(matrix.get(trf.X,trf.Y)) + "," 
//		+ fmt.format(matrix.get(trf.Z,trf.Y)) + "," 
//		+ fmt.format(matrix.get(trf.Y,trf.Y)) + ","
//		+ fmt.format(vector.getX())   + "," 
//		+ fmt.format(vector.getZ())   + "," 
//		+ fmt.format(vector.getY()) + ">";
//	}
//	
//	private String vectorStr(Vector v) {
//		NumberFormat fmt = FloatFormat.getInstance();
//		return "<"+fmt.format(v.getX())+","
//		+fmt.format(v.getZ())+","
//		+fmt.format(v.getY())+">";
//	}

};
