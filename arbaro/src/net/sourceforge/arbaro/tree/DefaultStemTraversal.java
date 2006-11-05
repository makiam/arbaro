package net.sourceforge.arbaro.tree;

public class DefaultStemTraversal implements StemTraversal {

	public boolean enterSegment(Segment segment) throws TraversalException {
		return true;
	}

	public boolean enterStem(Stem stem) throws TraversalException {
		return true;
	}

	public boolean leaveSegment(Segment segment) throws TraversalException {
		return true;
	}

	public boolean leaveStem(Stem stem) throws TraversalException {
		return true;
	}

	public boolean visitSubsegment(Subsegment subsegment)
			throws TraversalException {
		return true;
	}

}
