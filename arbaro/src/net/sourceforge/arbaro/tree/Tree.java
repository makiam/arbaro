package net.sourceforge.arbaro.tree;

import net.sourceforge.arbaro.transformation.Vector;
import java.io.PrintWriter;

public interface Tree {


	public abstract boolean traverseTree(TreeTraversal traversal);

	
	public abstract long getStemCount();

	public abstract long getLeafCount();

	public abstract Vector getMaxPoint();


	public abstract Vector getMinPoint();

	public int getSeed();


	public double getHeight();


	public double getWidth();
	
	public void paramsToXML(PrintWriter w);
	
	public String getSpecies();
	
	public int getLevels();
	
	public String getLeafShape();
	
	public double getLeafWidth();
	
	public double getLeafLength();
	
	public double getLeafStemLength();

	public String getVertexInfo(int level);
	
}