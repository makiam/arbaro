package net.sourceforge.arbaro.tree;

import net.sourceforge.arbaro.transformation.Vector;

public interface Subsegment {

	public abstract boolean traverseStem(StemTraversal traversal);

	public Vector getPosition();

	public double getRadius();

	public double getHeight();
	
	public boolean isLastSubsegment();
	
	public SubsegmentImpl getNext();
	
}