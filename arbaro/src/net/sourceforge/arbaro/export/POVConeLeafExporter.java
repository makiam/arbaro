/**
 * 
 */
package net.sourceforge.arbaro.export;

import java.io.PrintWriter;
import java.text.NumberFormat;

import net.sourceforge.arbaro.params.FloatFormat;
import net.sourceforge.arbaro.transformation.Matrix;
import net.sourceforge.arbaro.transformation.Transformation;
import net.sourceforge.arbaro.transformation.Vector;
import net.sourceforge.arbaro.tree.Leaf;
import net.sourceforge.arbaro.tree.Stem;
import net.sourceforge.arbaro.tree.TraversalException;
import net.sourceforge.arbaro.tree.Tree;
import net.sourceforge.arbaro.tree.TreeTraversal;

/**
 * @author wolfram
 *
 */
public class POVConeLeafExporter implements TreeTraversal {
	Tree tree;
	Progress progress;
	PrintWriter w;
	private long leavesProgressCount=0;
	
	/**
	 * 
	 */
	public POVConeLeafExporter(PrintWriter pw) {
		super();
		this.w = pw;
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.arbaro.tree.TreeTraversal#enterStem(net.sourceforge.arbaro.tree.Stem)
	 */
	public boolean enterStem(Stem stem) throws TraversalException {
		return true;
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.arbaro.tree.TreeTraversal#enterTree(net.sourceforge.arbaro.tree.Tree)
	 */
	public boolean enterTree(Tree tree) throws TraversalException {
		this.tree = tree;
		this.progress = tree.getProgress();
		return true;
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.arbaro.tree.TreeTraversal#leaveStem(net.sourceforge.arbaro.tree.Stem)
	 */
	public boolean leaveStem(Stem stem) throws TraversalException {
		return true;
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.arbaro.tree.TreeTraversal#leaveTree(net.sourceforge.arbaro.tree.Tree)
	 */
	public boolean leaveTree(Tree tree) throws TraversalException {
		w.flush();
		return true;
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.arbaro.tree.TreeTraversal#visitLeaf(net.sourceforge.arbaro.tree.Leaf)
	 */
	public boolean visitLeaf(Leaf leaf) throws TraversalException {
		// prints povray code for the leaf
		String indent = "    ";
		
		w.println(indent + "object { " + povrayDeclarationPrefix() + "leaf " 
				+ transformationStr(leaf.transf)+"}");
		
//		increment progress count
		incLeavesProgressCount();
		
		return true;
	}

	private void incLeavesProgressCount() {
		if (leavesProgressCount++%500 == 0) {
			progress.incProgress(500);
			if (tree.params.verbose) System.err.print(".");
		}
	}
	
	private String povrayDeclarationPrefix() {
		return tree.params.Species + "_" + tree.params.Seed + "_";
	}
	
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
