package net.sourceforge.arbaro.tree;

/**
 * An interface, for traversal of the segments and 
 * subsegments of a stem. (Compare Hierarchical Visitor Pattern)
 * 
 * @author Wolfram Diestel
 */

public interface StemTraversal {
	   boolean enterStem(Stem stem) throws TraversalException; // going into a Stem
       boolean leaveStem(Stem stem) throws TraversalException; // coming out of a Stem
	   boolean enterSegment(Segment segment) throws TraversalException; // going into a Segment
       boolean leaveSegment(Segment segment) throws TraversalException; // coming out of a Segment
       boolean visitSubsegment(Subsegment subsegment) throws TraversalException; // process a Subsegment
}
