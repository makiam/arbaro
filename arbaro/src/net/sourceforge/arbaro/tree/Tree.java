package net.sourceforge.arbaro.tree;

import net.sourceforge.arbaro.transformation.Vector;

public interface Tree {


	public abstract boolean traverseTree(TreeTraversal traversal)
			throws TraversalException;

	
	public abstract long getStemCount();

	public abstract long getLeafCount();

	public abstract Vector getMaxPoint();


	public abstract Vector getMinPoint();

	public int getSeed();


	public double getHeight();


	public double getWidth();
	
}