/**
 * 
 */
package net.sourceforge.arbaro.export;

import java.text.NumberFormat;
import java.io.PrintWriter;
import net.sourceforge.arbaro.params.FloatFormat;
import net.sourceforge.arbaro.transformation.Vector;
import net.sourceforge.arbaro.tree.*;

/**
 * @author wolfram
 *
 */
public class POVConeSegmentExporter extends DefaultStemTraversal {
	PrintWriter w;
	/**
	 * 
	 */
	public POVConeSegmentExporter(PrintWriter pw) {
		super();
		this.w = pw;
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.arbaro.tree.StemTraversal#enterSegment(net.sourceforge.arbaro.tree.Segment)
	 */
	public boolean enterSegment(Segment s) throws TraversalException {
		String indent = whitespace(s.lpar.level*2+4);
		NumberFormat fmt = FloatFormat.getInstance();
		
		// FIXME: for cone output - if starting direction is not 1*y, there is a gap 
		// between earth and tree base
		// best would be to add roots to the trunk(?)	  
		
		// TODO instead of accessing subsegments this way
		// it would be nicer to use visitSubsegment, but
		// how to see when we visit the last but one subsegment?
		// may be need an index in Subsegment
		for (int i=0; i<s.subsegments.size()-1; i++) {
			Subsegment ss1 = (Subsegment)s.subsegments.elementAt(i);
			Subsegment ss2 = (Subsegment)s.subsegments.elementAt(i+1);
			w.println(indent + "cone   { " + vectorStr(ss1.pos) + ", "
					+ fmt.format(ss1.rad) + ", " 
					+ vectorStr(ss2.pos) + ", " 
					+ fmt.format(ss2.rad) + " }"); 
			// for helix subsegs put spheres between
			if (s.lpar.nCurveV<0 && i<s.subsegments.size()-2) {
				w.println(indent + "sphere { " 
						+ vectorStr(ss1.pos) + ", "
						+ fmt.format(ss1.rad-0.0001) + " }");
			}
		}
		
		// put sphere at segment end
		if ((s.rad2 > 0) && (! s.isLastStemSegment() || 
				(s.lpar.nTaper>1 && s.lpar.nTaper<=2))) 
		{  
			w.println(indent + "sphere { " + vectorStr(s.posTo()) + ", "
					+ fmt.format(s.rad2-0.0001) + " }");
		}
	
		return true;
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.arbaro.tree.StemTraversal#visitSubsegment(net.sourceforge.arbaro.tree.Subsegment)
	 */
	public boolean visitSubsegment(Subsegment subsegment)
			throws TraversalException {
		// do nothing with subsegments at the moment
		return false;
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
}
