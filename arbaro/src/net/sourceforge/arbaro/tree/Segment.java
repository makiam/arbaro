package net.sourceforge.arbaro.tree;

import net.sourceforge.arbaro.transformation.Transformation;
import net.sourceforge.arbaro.transformation.Vector;

public interface Segment {

	public abstract double getLength();

	public abstract Transformation getTransformation();

	/**
	 * Tests, if the segment is the first stem segment
	 * 
	 * @return true, if it's the first stem segment, false otherwise
	 */
	public abstract boolean isFirstStemSegment();

	/**
	 * Tests, if the segment ist the last stem segment
	 * 
	 * @return true, if it's the last stem segment, false otherwise
	 */
	public abstract boolean isLastStemSegment();

	/**
	 * Adds the segments to a mesh part. For every subsegment one ore
	 * two mesh sections are added.
	 * 
	 * @param meshpart the mesh part, to wich the segment should be added
	 */

	/*	
	 // TODO should be obsolete when Traversals are working
	 public void addToMeshpart(MeshPart meshpart) {
	 // creates the part of the mesh for this segment
	 //pt_cnt = self.tree.meshpoints[self.stem.level]
	 //smooth = self.stem.level<=self.tree.smooth_mesh_level
	 
	 double vLength = stem.getLength()+stem.stemRadius(0)+stem.stemRadius(stem.length);
	 double vBase = + stem.stemRadius(0);
	 
	 if (meshpart.size() == 0) { // first segment, create lower meshpoints
	 Subsegment ss = (Subsegment)subsegments.elementAt(0);
	 // one point at the stem origin, with normal in reverse z-direction
	 createSectionMeshpoints(ss.pos,0,meshpart,
	 isFirstStemSegment() && lpar.level==0,0);
	 ((MeshSection)meshpart.firstElement()).setNormalsToVector(transf.getZ().mul(-1));
	 
	 // more points around the stem origin
	 createSectionMeshpoints(ss.pos,ss.rad,meshpart,
	 isFirstStemSegment() && lpar.level==0,
	 vBase/vLength);
	 }
	 
	 // create meshpoints on top of each subsegment
	 for (int i=1; i<subsegments.size(); i++) {
	 Subsegment ss = (Subsegment)subsegments.elementAt(i);
	 createSectionMeshpoints(ss.pos,ss.rad,meshpart,false,
	 (vBase+index*length+ss.height)/vLength);
	 }
	 
	 // System.err.println("MESHCREATION, segmindex: "+index);
	 
	 // close mesh with normal in z-direction
	 if (isLastStemSegment()) {
	 if (rad2>0.000001) {
	 createSectionMeshpoints(posTo(),0,meshpart,false,
	 1);
	 }
	 //DBG System.err.println("LAST StemSegm, setting normals to Z-dir");
	 ((MeshSection)meshpart.lastElement()).setNormalsToVector(transf.getZ());
	 }
	 }
	 */

	public abstract boolean traverseStem(StemTraversal traversal)
			throws TraversalException;

	/**
	 * Position at the beginning of the segment
	 * 
	 * @return beginning point of the segment
	 */
	public abstract Vector getLowerPosition();

	/**
	 * Position of the end of the segment
	 * 
	 * @return end point of the segment
	 */
	public abstract Vector getUpperPosition();
	
	public abstract double getLowerRadius();
	
	public abstract double getUpperRadius();

	public abstract int getIndex();

	public abstract int getSubsegmentCount();

}