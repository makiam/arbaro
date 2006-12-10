package net.sourceforge.arbaro.tree;

import net.sourceforge.arbaro.transformation.Transformation;

public interface Leaf {

	public abstract boolean traverseTree(TreeTraversal traversal);

	public abstract Transformation getTransformation();

}