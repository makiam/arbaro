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

package net.sourceforge.arbaro.export;

import net.sourceforge.arbaro.params.FloatFormat;
import net.sourceforge.arbaro.transformation.*;
import net.sourceforge.arbaro.tree.*;
import net.sourceforge.arbaro.params.*;

import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.Enumeration;

class POVConeLeafWriter implements TreeTraversal {
	Tree tree;
	//Progress progress;
	PrintWriter w;
	//private long leavesProgressCount=0;
	String povrayDeclarationPrefix;
	AbstractExporter exporter;
	
	/**
	 * 
	 */
	public POVConeLeafWriter(AbstractExporter exporter/*, Params params*/,
			Tree tree) {
		super();
		this.exporter = exporter;
		this.w = exporter.getWriter();
		this.tree = tree;
		this.povrayDeclarationPrefix =
			tree.getSpecies() + "_" + tree.getSeed() + "_";
		
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.arbaro.tree.TreeTraversal#enterStem(net.sourceforge.arbaro.tree.Stem)
	 */
	public boolean enterStem(Stem stem) {
		return true;
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.arbaro.tree.TreeTraversal#enterTree(net.sourceforge.arbaro.tree.Tree)
	 */
	public boolean enterTree(Tree tree) {
		this.tree = tree;
		return true;
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.arbaro.tree.TreeTraversal#leaveStem(net.sourceforge.arbaro.tree.Stem)
	 */
	public boolean leaveStem(Stem stem) {
		return true;
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.arbaro.tree.TreeTraversal#leaveTree(net.sourceforge.arbaro.tree.Tree)
	 */
	public boolean leaveTree(Tree tree) {
		w.flush();
		return true;
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.arbaro.tree.TreeTraversal#visitLeaf(net.sourceforge.arbaro.tree.Leaf)
	 */
	public boolean visitLeaf(Leaf leaf) {
		// prints povray code for the leaf
		String indent = "    ";
		
		w.println(indent + "object { " + povrayDeclarationPrefix + "leaf " 
				+ transformationStr(leaf.getTransformation())+"}");
		
//		increment progress count
		exporter.incProgressCount(AbstractExporter.LEAF_PROGRESS_STEP);
		
		return true;
	}

	/*
	private void incLeavesProgressCount() {
		if (leavesProgressCount++%500 == 0) {
			progress.incProgress(500);
			if (tree.params.verbose) System.err.print(".");
		}
	}
	*/
	
/*	private String povrayDeclarationPrefix() {
		return tree.params.Species + "_" + tree.params.Seed + "_";
	}
	*/
	private String transformationStr(Transformation trf) {
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

}

///**
// * @author wolfram
// *
// */
//class POVConeSegmentWriter extends DefaultStemTraversal {
//	PrintWriter w;
//	AbstractExporter exporter;
//	int stemlevel;
//	Params par;
//	/**
//	 * 
//	 */
//	public POVConeSegmentWriter(AbstractExporter exporter, Params params) {
//		super();
//		this.exporter = exporter;
//		this.w = exporter.getWriter();
//		this.par = params;
//	}
//	
//	public boolean enterStem(Stem stem) {
//		stemlevel = stem.getLevel();
//		return true;
//	}
//
//	/* (non-Javadoc)
//	 * @see net.sourceforge.arbaro.tree.StemTraversal#enterSegment(net.sourceforge.arbaro.tree.Segment)
//	 */
//	public boolean enterSegment(Segment s) {
//		String indent = whitespace(stemlevel*2+4);
//		NumberFormat fmt = FloatFormat.getInstance();
//		
//		// FIXME: for cone output - if starting direction is not 1*y, there is a gap 
//		// between earth and tree base
//		// best would be to add roots to the trunk(?)	  
//	/*	
//		// TODO instead of accessing subsegments this way
//		// it would be nicer to use visitSubsegment, but
//		// how to see when we visit the last but one subsegment?
//		// may be need an index in Subsegment
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
//*/		
//		// put sphere at segment end
//		LevelParams lpar = par.getLevelParams(stemlevel);
//		if ((s.getUpperRadius() > 0) && (! s.isLastStemSegment() || 
//				(lpar.nTaper>1 && lpar.nTaper<=2))) 
//		{  
//			w.println(indent + "sphere { " + vectorStr(s.getUpperPosition()) + ", "
//					+ fmt.format(s.getUpperRadius()-0.0001) + " }");
//		}
//	
//		return true;
//	}
//
//	/* (non-Javadoc)
//	 * @see net.sourceforge.arbaro.tree.StemTraversal#visitSubsegment(net.sourceforge.arbaro.tree.Subsegment)
//	 */
//	public boolean visitSubsegment(Subsegment subsegment) {
//		if (! subsegment.isLastSubsegment()) {
//			String indent = whitespace(stemlevel*2+4);
//			NumberFormat fmt = FloatFormat.getInstance();
//			LevelParams lpar = par.getLevelParams(stemlevel);
//
//			Subsegment ss1 = subsegment;
//			Subsegment ss2 = subsegment.getNext();
//			w.println(indent + "cone   { " + vectorStr(ss1.getPosition()) + ", "
//					+ fmt.format(ss1.getRadius()) + ", " 
//					+ vectorStr(ss2.getPosition()) + ", " 
//					+ fmt.format(ss2.getRadius()) + " }"); 
//			// for helix subsegs put spheres between
//			if (lpar.nCurveV<0 /*&& i<s.subsegments.size()-2*/) {
//				w.println(indent + "sphere { " 
//						+ vectorStr(ss1.getPosition()) + ", "
//						+ fmt.format(ss1.getRadius()-0.0001) + " }");
//			}
//		}
//		return true;
//	}
//
//	private String vectorStr(Vector v) {
//		NumberFormat fmt = FloatFormat.getInstance();
//		return "<"+fmt.format(v.getX())+","
//		+fmt.format(v.getZ())+","
//		+fmt.format(v.getY())+">";
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
//}
//


/**
 * Exports Stems of one specified level to a POV file
 * 
 * @author wolfram
 *
 */
class POVConeStemWriter implements TreeTraversal {
	Tree tree;
	AbstractExporter exporter;
	PrintWriter w;
	Params params;
	int level;
//	private long stemsProgressCount=0;
	
	/**
	 * 
	 */
	public POVConeStemWriter(AbstractExporter exporter, /*Params params,*/ int level) {
		super();
		this.exporter = exporter;
		this.w = exporter.getWriter();
//		this.params = params;
		this.level=level;
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.arbaro.tree.TreeTraversal#enterStem(net.sourceforge.arbaro.tree.Stem)
	 */
	public boolean enterStem(Stem stem) {
		if (level >= 0 && stem.getLevel() < level) {
			return true; // look further for stems
			
		} else if (level >= 0 && stem.getLevel() > level) {
			return false; // go back to higher level
			
		} else {
			
//			POVConeSegmentWriter writer = new POVConeSegmentWriter(exporter,params);
//			stem.traverseStem(writer);

			String indent = whitespace(stem.getLevel()*2+4);
			NumberFormat fmt = FloatFormat.getInstance();
			Enumeration sections = stem.sections();
//			LevelParams lpar = params.getLevelParams(stem.getLevel());
			
			if (sections.hasMoreElements()) {
				StemSection from = (StemSection)sections.nextElement();
				StemSection to = null;
			
				while (sections.hasMoreElements()) {
					to = (StemSection)sections.nextElement();

					w.println(indent + "cone   { " + vectorStr(from.getPosition()) + ", "
							+ fmt.format(from.getRadius()) + ", " 
							+ vectorStr(to.getPosition()) + ", " 
							+ fmt.format(to.getRadius()) + " }");
					
					// put spheres where z-directions changes
					if (! from.getZ().equals(to.getZ())) {
					
						w.println(indent + "sphere { " 
									+ vectorStr(from.getPosition()) + ", "
									+ fmt.format(to.getRadius()-0.0001) + " }");
					}
				
					from = to;
				}
			
				// put sphere at stem end
/* FIXME now using sections instead of segments, the spherical stem end
 *       will be made from several cones instead of one shpere ...
 				
				if ((to.getRadius() > 0.0001) || 
						(lpar.nTaper>1 && lpar.nTaper<=2)) 
				{  
					w.println(indent + "sphere { " + vectorStr(to.getPosition()) + ", "
							+ fmt.format(to.getRadius()-0.0001) + " }");
				}
			*/
			}
			
			exporter.incProgressCount(AbstractExporter.STEM_PROGRESS_STEP);
			
			return true;
		}
	}

	private String vectorStr(Vector v) {
		NumberFormat fmt = FloatFormat.getInstance();
		return "<"+fmt.format(v.getX())+","
		+fmt.format(v.getZ())+","
		+fmt.format(v.getY())+">";
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
	
	/* (non-Javadoc)
	 * @see net.sourceforge.arbaro.tree.TreeTraversal#enterTree(net.sourceforge.arbaro.tree.Tree)
	 */
	public boolean enterTree(Tree tree) {
		this.tree = tree;
		return true;
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.arbaro.tree.TreeTraversal#leaveStem(net.sourceforge.arbaro.tree.Stem)
	 */
	public boolean leaveStem(Stem stem) {
		return true;
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.arbaro.tree.TreeTraversal#leaveTree(net.sourceforge.arbaro.tree.Tree)
	 */
	public boolean leaveTree(Tree tree) {
		w.flush();
		return true;
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.arbaro.tree.TreeTraversal#visitLeaf(net.sourceforge.arbaro.tree.Leaf)
	 */
	public boolean visitLeaf(Leaf leaf) {
		// don't wirte leaves here
		return false;
	}
	
/*	private void incStemsProgressCount() {
		if (stemsProgressCount++%100 == 0) {
			progress.incProgress(100);
			if (tree.params.verbose) System.err.print(".");
		}
	}
	*/

}


/**
 * Exports a tree as Povray primitives like cylinders and spheres 
 *
 */
class POVConeExporter extends AbstractExporter {
	Tree tree;
//	Params params;
	private String povrayDeclarationPrefix;

	/**
	 * 
	 */
	public POVConeExporter(Tree tree/*, Params params*/) {
		super();
		this.tree = tree;
//		this.params = params;
		this.povrayDeclarationPrefix =
			tree.getSpecies() + "_" + tree.getSeed() + "_";
	}
	
	public void doWrite() {
//		try {
			// some declarations in the POV file
			NumberFormat frm = FloatFormat.getInstance();
			
			// tree scale
			w.println("#declare " + povrayDeclarationPrefix + "height = " 
					+ frm.format(tree.getHeight()) + ";");
			
			// leaf declaration
			if (tree.getLeafCount()!=0) writeLeafDeclaration();
	
			// stems
			progress.beginPhase("writing stem objects",tree.getStemCount());
			
			for (int level=0; level < tree.getLevels(); level++) {
				
				w.println("#declare " + povrayDeclarationPrefix + "stems_"
						+ level + " = union {");
				
				POVConeStemWriter writer = new POVConeStemWriter(this,/*params,*/level);
				tree.traverseTree(writer);
				
				w.println("}");
				
			}
			
			// leaves
			if (tree.getLeafCount()!=0) {
				
				progress.beginPhase("writing leaf objects",tree.getLeafCount());
				
				w.println("#declare " + povrayDeclarationPrefix + "leaves = union {");
				
				POVConeLeafWriter lexporter = new POVConeLeafWriter(this,/*params*/ tree);
				tree.traverseTree(lexporter);
				
				w.println("}");
				
			} else { // empty declaration
				w.println("#declare " + povrayDeclarationPrefix + "leaves = sphere {<0,0,0>,0}"); 
			}
			
			progress.endPhase();
			
			// all stems together
			w.println("#declare " + povrayDeclarationPrefix + "stems = union {"); 
			for (int level=0; level < tree.getLevels(); level++) {
				w.println("  object {" + povrayDeclarationPrefix + "stems_" 
						+ level + "}");
			}
			w.println("}");
			
			w.flush();
			
//		} catch (Exception e) {
//			e.printStackTrace();
//			System.err.println(e);
//			throw new ExportError(e.getMessage());
//		}	
	}

	/**
	 * Returns a prefix for the Povray objects names,
	 * it consists of the species name and the random seed
	 * 
	 * @return the prefix string
	 */
	/*
	private String povrayDeclarationPrefix() {
		return tree.params.Species + "_" + tree.params.Seed + "_";
	}
*/
	/**
	 * Outputs the Povray code for a leaf object
	 * 
	 * @param w The output stream
	 */
	private void writeLeafDeclaration() {
		double length = tree.getLeafLength();
		double width = tree.getLeafWidth();
		w.println("#include \"arbaro.inc\"");
		w.println("#declare " + povrayDeclarationPrefix + "leaf = " +
				"object { Arb_leaf_" + (tree.getLeafShape().equals("0")? "disc" : tree.getLeafShape())
				+ " translate " + (tree.getLeafStemLength()+0.5) + "*y scale <" 
				+ width + "," + length + "," + width + "> }");
	}	  	
	

}
