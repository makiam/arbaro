/**
 * 
 */
package net.sourceforge.arbaro.export;

import net.sourceforge.arbaro.tree.*;
import java.io.PrintWriter;

/**
 * Exports Stems of one specified level to a POV file
 * 
 * @author wolfram
 *
 */
public class POVConeStemExporter implements TreeTraversal {
	Tree tree;
	Progress progress;
	PrintWriter w;
	int level;
	private long stemsProgressCount=0;
	
	/**
	 * 
	 */
	public POVConeStemExporter(PrintWriter pw, int level) {
		super();
		this.w = pw;
		this.level=level;
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.arbaro.tree.TreeTraversal#enterStem(net.sourceforge.arbaro.tree.Stem)
	 */
	public boolean enterStem(Stem stem) throws TraversalException {
		if (level >= 0 && stem.stemlevel < level) {
			return true; // look further for stems
			
		} else if (level >= 0 && stem.stemlevel > level) {
			return false; // go back to higher level
			
		} else {
			
			POVConeSegmentExporter exporter = new POVConeSegmentExporter(w);
			stem.traverseStem(exporter);
			
			incStemsProgressCount();
			
			return true;
		}
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
		// don't wirte leaves here
		return false;
	}
	
	private void incStemsProgressCount() {
		if (stemsProgressCount++%100 == 0) {
			progress.incProgress(100);
			if (tree.params.verbose) System.err.print(".");
		}
	}

}
