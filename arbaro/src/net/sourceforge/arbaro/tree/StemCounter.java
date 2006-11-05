package net.sourceforge.arbaro.tree;

public class StemCounter extends DefaultTreeTraversal {
	long stemCount;
	
	public long getStemCount() {
		return stemCount;
	}

	public boolean enterStem(Stem stem) {
		stemCount++; // one more stem
		return true;
	}

	public boolean enterTree(Tree tree) {
		stemCount = 0; // start stem counting
		return true;
	}

	public boolean visitLeaf(Leaf leaf) {
		return false; // don't count leaves
	}

}
