package net.sourceforge.arbaro.tree;

import net.sourceforge.arbaro.transformation.Transformation;

public interface Leaf {

	public abstract boolean traverseTree(TreeTraversal traversal)
			throws TraversalException;

	public abstract Transformation getTransformation();

}