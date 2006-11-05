package net.sourceforge.arbaro.tree;

public class DefaultTreeTraversal implements TreeTraversal {

	public boolean enterStem(Stem stem) throws TraversalException {
		return true;
	}

	public boolean enterTree(Tree tree) throws TraversalException {
		return true;
	}

	public boolean leaveStem(Stem stem) throws TraversalException {
		return true;
	}

	public boolean leaveTree(Tree tree) throws TraversalException {
		return true;
	}

	public boolean visitLeaf(Leaf leaf) throws TraversalException {
		return true;
	}

}
