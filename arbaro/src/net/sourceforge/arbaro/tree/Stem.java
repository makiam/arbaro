package net.sourceforge.arbaro.tree;

import net.sourceforge.arbaro.transformation.Transformation;
import net.sourceforge.arbaro.transformation.Vector;

public interface Stem {

	public abstract java.util.Enumeration sections();
	
	// offset for clones, because uv-Coordinates should
	// be at same coordinates for stems and theire clones
	public int getCloneSectionOffset();
	
	public abstract Vector getMinPoint();

	public abstract Vector getMaxPoint();

	/**
	 * For debugging:
	 * Prints out the transformation to stderr nicely 
	 * (only if debugging is enabled)
	 * 
	 * @param where The position in the tree, i.e. wich stem
	 *              has this transformation
	 * @param trf  The transformation
	 */
	/*	
	 void TRF(String where, Transformation trf) {
	 DBG(where + ": " + trf.toString());
	 }
	 */
	/**
	 * Output debug string, when debugging ist enabled
	 * 
	 * @param dbgstr The output string
	 */
	/*
	 public void DBG(String dbgstr) {
	 // print debug string to stderr if debugging is enabled
	 if (par.debug) System.err.println(getTreePosition() + ":" + dbgstr);
	 }
	 */
	/**
	 * The position of the stem in the tree. 0.1c2.3 means:
	 * fourth twig of the third clone of the second branch growing
	 * out of the first (only?) trunk 
	 * 
	 * @return The stem position in the tree as a string
	 */
	public abstract String getTreePosition();

	public abstract double getLength();
	
	public abstract double getBaseRadius();
	public abstract double getPeakRadius();
	
	public abstract int getLevel();

	/**
	 * Adds the current stem to the mesh object
	 *  
	 * @param mesh the mesh object
	 * @throws Exception
	 */
	/*
	 // TODO should be obsolete, when TreeTraversals are working
	 void addToMesh(Mesh mesh, boolean withSubstems, boolean useQuads) throws Exception {
	 
	 if (par.verbose) {
	 if (stemlevel<=1 && cloneIndex.size()==0) System.err.print(".");
	 }
	 
	 //String indent = "    ";
	 
	 // create mesh part for myself
	 if (segments.size()>0) {
	 MeshPart meshpart = new MeshPart(this,stemlevel<=par.smooth_mesh_level, useQuads);
	 for (int i=0; i<segments.size(); i++) {
	 ((Segment)segments.elementAt(i)).addToMeshpart(meshpart);
	 }
	 mesh.addMeshpart(meshpart);
	 }
	 
	 if (withSubstems) {
	 // add clones to the mesh
	 if (clones != null) {
	 for (int i=0; i<clones.size(); i++) {
	 ((Stem)clones.elementAt(i)).addToMesh(mesh,withSubstems, useQuads);
	 }
	 }
	 
	 if (substems != null) {
	 tree.getProgress().incProgress(substems.size());
	 
	 for (int i=0; i<substems.size(); i++) {
	 ((Stem)substems.elementAt(i)).addToMesh(mesh,withSubstems, useQuads);
	 }
	 }
	 }
	 }
	 */

	public abstract boolean traverseTree(TreeTraversal traversal);

	// public abstract boolean traverseStem(StemTraversal traversal);

	// use with TreeTraversal
	public abstract long getLeafCount();

	public abstract boolean isClone();
	
	public abstract boolean isSmooth();

	public Transformation getTransformation();

}