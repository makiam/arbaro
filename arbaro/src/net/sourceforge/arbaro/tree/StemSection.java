package net.sourceforge.arbaro.tree;

import net.sourceforge.arbaro.transformation.*;

public interface StemSection {

	public Vector getPosition();

	public double getRadius();

	/**
	 * 
	 * @return relative distance from stem origin
	 */
	public double getDistance();
	
//	public boolean isFirst();
//	
//	public boolean isLast();
	
	public Transformation getTransformation();
	
	public Vector getZ(); 
	//public StemSection getNext();
	
	public Vector[] getSectionPoints();
	
}
