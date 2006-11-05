package net.sourceforge.arbaro.tree;

public class LeafCounter implements TreeTraversal {
	long leafCount;
	
	public long getLeafCount() {
		return leafCount;
	}

	public boolean enterStem(Stem stem) {
		// add leaves of this stem
		leafCount += stem.getLeafCount(); 
		return true;
	}

	public boolean enterTree(Tree tree) {
		leafCount=0; // start counting leaves
		return true;
	}

	public boolean leaveStem(Stem stem) {
		return true; // successfully counted
	}

	public boolean leaveTree(Tree tree) {
		return true; // successfully counted
	}

	public boolean visitLeaf(Leaf leaf) {
		return false; // don't visit more leaves
		  // for efficency leaf count was got from the parent stem 
	}


}
