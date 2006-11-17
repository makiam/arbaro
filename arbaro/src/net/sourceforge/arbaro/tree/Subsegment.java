package net.sourceforge.arbaro.tree;

import net.sourceforge.arbaro.transformation.Vector;

public interface Subsegment {

	public abstract boolean traverseStem(StemTraversal traversal)
			throws TraversalException;

	public Vector getPosition();

	public double getRadius();

	public double getHeight();
	
	public boolean isLastSubsegment();
	
	public SubsegmentImpl getNext();
	
}