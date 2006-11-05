package net.sourceforge.arbaro.tree;

/*
class TraversalException extends ArbaroError{
	public TraversalException(String errmsg) {
		super(errmsg);
	} 
};
*/

/**
 * An interface, for traversal of the segments and 
 * subsegments of a stem. (Compare Hierarchical Visitor Pattern)
 * 
 * @author Wolfram Diestel
 */

public interface TreeTraversal {
	   boolean enterTree(Tree tree) throws TraversalException; // going into a Tree
       boolean leaveTree(Tree tree) throws TraversalException; // coming out of a Tree
	   boolean enterStem(Stem stem) throws TraversalException; // going into a Stem
       boolean leaveStem(Stem stem) throws TraversalException; // coming out of a Stem
	/*   boolean enterSegment(Segment segment) throws TraversalException; // going into a Stem
       boolean leaveSegment(Segment segment) throws TraversalException; // coming out of a Stem
       boolean visitSubsegment(Subsegment subsegment) throws TraversalException; // process a Subsegment
       */
       boolean visitLeaf(Leaf leaf) throws TraversalException; // process a Leaf
}
