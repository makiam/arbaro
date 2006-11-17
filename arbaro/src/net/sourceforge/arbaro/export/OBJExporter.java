//  #**************************************************************************
//  #
//  #    Copyright (C) 2003-2006  Wolfram Diestel
//  #
//  #    This program is free software; you can redistribute it and/or modify
//  #    it under the terms of the GNU General Public License as published by
//  #    the Free Software Foundation; either version 2 of the License, or
//  #    (at your option) any later version.
//  #
//  #    This program is distributed in the hope that it will be useful,
//  #    but WITHOUT ANY WARRANTY; without even the implied warranty of
//  #    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  #    GNU General Public License for more details.
//  #
//  #    You should have received a copy of the GNU General Public License
//  #    along with this program; if not, write to the Free Software
//  #    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//  #
//  #    Send comments and bug fixes to diestel@steloj.de
//  #
//  #**************************************************************************/

package net.sourceforge.arbaro.export;

import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.Enumeration;

import net.sourceforge.arbaro.mesh.*;
import net.sourceforge.arbaro.meshfactory.*;
import net.sourceforge.arbaro.params.*;
import net.sourceforge.arbaro.transformation.Vector;
import net.sourceforge.arbaro.tree.*;




class OBJLeafWriter extends DefaultTreeTraversal {
	Progress progress;
	LeafMesh leafMesh;
	AbstractExporter exporter;
	public int leafVertexOffset;
	PrintWriter w;
	long leavesProgressCount=0;
	Tree tree;

	static final NumberFormat fmt = FloatFormat.getInstance();

	/**
	 * 
	 */
	public OBJLeafWriter(Tree tree,
			LeafMesh leafMesh,
			AbstractExporter exporter,
			int leafVertexOffset) {
		super();
		this.w = exporter.getWriter();
		this.leafMesh = leafMesh;
		this.leafVertexOffset = leafVertexOffset;
	}

	public boolean enterTree(Tree tree) {
		this.tree = tree;
		return true;
	}
	
	/*
	void incLeavesProgressCount() {
		if (leavesProgressCount++ % 500 == 0) {
			progress.incProgress(500);
			if (tree.params.verbose) System.err.print(".");
		}
	}
	
	void writeVector(Vector v) {
		// FIXME: why I cannot get a FloatFormat instance
		// when creating the class?
		// NumberFormat fmt = FloatFormat.getInstance();
		w.print("<"+fmt.format(v.getX())+","
		+fmt.format(v.getZ())+","
		+fmt.format(v.getY())+">");
	}
*/
}

/**
 * @author wolfram
 *
 */
class OBJLeafFaceWriter extends OBJLeafWriter {
	boolean firstLeaf;
	long faceProgressCount=0;
	
	long smoothingGroup;
	int uvVertexOffset;
	boolean outputLeafUVs=true;
	boolean outputStemUVs=true;

//	instead of Arbaro's normals use smoothing to interpolate normals
//  this should be give the same result	
	boolean outputNormals = false;

	/**
	 * @param pw
	 * @param leafMesh
	 * @param leafVertexOffset
	 */
	public OBJLeafFaceWriter(Tree tree, AbstractExporter exporter,
			LeafMesh leafMesh,
			int leafVertexOffset, int uvVertexOffset,
			long smoothingGroup,	
			boolean outputLeafUVs, boolean outputStemUVs) {
		super(tree, leafMesh, exporter, leafVertexOffset);
		
		firstLeaf = true;
		this.smoothingGroup = smoothingGroup;
		this.uvVertexOffset = uvVertexOffset;
		this.outputLeafUVs = outputLeafUVs;
		this.outputStemUVs = outputStemUVs;
	}
	
	
	public boolean visitLeaf(Leaf l) {
		if (firstLeaf) {
			
			w.println("g leaves");
			w.println("usemtl leaves");
	//		uvVertexOffset++;
			
			firstLeaf = false;
		}
	
		w.println("s "+smoothingGroup++);
		for (int i=0; i<leafMesh.getShapeFaceCount(); i++) {
			Face face = leafMesh.shapeFaceAt(i);
			writeFace(
					face,leafVertexOffset,
					face,uvVertexOffset,
					outputLeafUVs,outputNormals);
		}
		
		// increment face offset
		leafVertexOffset += leafMesh.getShapeVertexCount();
		
		exporter.incProgressCount(AbstractExporter.LEAF_PROGRESS_STEP);
		
		return true;
	}

	
	private void writeFace(Face f, long offset, Face uv, long uvOffset, boolean writeUVs, boolean writeNormals) {
		w.print("f "); 
				
		for (int i=0; i<f.points.length; i++) {
			w.print(offset+f.points[i]);
			if (writeUVs || writeNormals) {
				w.print("/");
				if (writeUVs) w.print(uvOffset+uv.points[i]);
				if (writeNormals) w.print("/"+offset+f.points[i]);
			}
			if (i<f.points.length-1) w.print(" ");
			else w.println();
		}
	}
}


/**
 * @author wolfram
 *
 */
class OBJLeafVertexWriter extends OBJLeafWriter {
	String type;
	long vertexProgressCount=0;
	
	/**
	 * @param pw
	 * @param leafMesh
	 * @param leafVertexOffset
	 */
	public OBJLeafVertexWriter(Tree tree, AbstractExporter exporter, LeafMesh leafMesh,
			int leafVertexOffset, String type) {
		super(tree, leafMesh, exporter, leafVertexOffset);
		this.type=type;
	}

	public boolean visitLeaf(Leaf l) {
		for (int i=0; i<leafMesh.getShapeVertexCount(); i++) {
			
			if (type=="v") {
				writeVertex(l.getTransformation().apply(leafMesh.shapeVertexAt(i).point),type);
			} else {
				writeVertex(l.getTransformation().apply(leafMesh.shapeVertexAt(i).normal),type);
			}
		}
		
		exporter.incProgressCount(AbstractExporter.MESH_PROGRESS_STEP);
		
		return true;
	}

	/*
	private void incVertexProgressCount() {
		if (vertexProgressCount++ % 100 == 0) {
			progress.incProgress(100);
			if (verbose) System.err.print(".");
		}
	}
	*/
	
	private void writeVertex(Vector v, String type) {
		w.println(type+" "
				+fmt.format(v.getX())+" "
				+fmt.format(v.getZ())+" "
				+fmt.format(v.getY()));
	}
	
}


/**
 * Exports a tree mesh as Wavefront OBJ file 
 *
 */
final class OBJExporter extends MeshExporter {
	long vertexProgressCount=0;
	long faceProgressCount=0;
	NumberFormat frm = FloatFormat.getInstance();
	Mesh mesh;
	LeafMesh leafMesh;
	Tree tree;
	long smoothingGroup;
	int vertexOffset;
	int uvVertexOffset;
	public boolean outputLeafUVs=true;
	public boolean outputStemUVs=true;
	
//	instead of Arbaro's normals use smoothing to interpolate normals
//  this should be give the same result	
	boolean outputNormals = false;

	/**
	 * @param aTree
	 * @param pw
	 * @param p
	 */
	public OBJExporter(Tree tree, MeshFactory meshFactory) {
		super(meshFactory);
		this.tree = tree;
	}
/*
	private void incVertexProgressCount() {
		if (vertexProgressCount++ % 100 == 0) {
			progress.incProgress(100);
			progress.consoleProgress();
		}
	}
	
	private void incFaceProgressCount() {
		if (faceProgressCount++ % 500 == 0) {
			progress.incProgress(500);
			progress.consoleProgress();
		}
	}
	*/
	
	public void doWrite() throws ExportError {
		smoothingGroup=1;

		long objCount = 
			(tree.getStemCount()
			+tree.getLeafCount())*(outputNormals? 2 : 1); 

		try {
			mesh = meshFactory.createStemMeshByLevel(tree,progress);
			leafMesh = meshFactory.createLeafMesh();

			// vertices
			progress.beginPhase("Writing vertices",objCount);
			
			writeStemVertices("v");
			writeLeafVertices("v");
			
			if (outputStemUVs) writeStemVertices("vt");
			if (outputLeafUVs) writeLeafVertices("vt");

// use smoothing to interpolate normals			
			if (outputNormals) {
				writeStemVertices("vn");
				writeLeafVertices("vn");
			}
			
			progress.endPhase();
			
			// faces
			progress.beginPhase("Writing faces",objCount);
			writeStemFaces();
			//writeLeafFaces();
			OBJLeafFaceWriter faceExporter = 
				new OBJLeafFaceWriter(tree,this,leafMesh,
					vertexOffset, uvVertexOffset,smoothingGroup,
					outputLeafUVs, outputStemUVs);
			tree.traverseTree(faceExporter);
			vertexOffset = faceExporter.leafVertexOffset;
			progress.endPhase();
			w.flush();
			
		}	catch (Exception e) {
			System.err.println(e);
			throw new ExportError(e.getMessage());
			//e.printStackTrace(System.err);
		}
	}
	
	private void writeStemVertices(String type) throws Exception {
	
		if (type == "vt") { 
			// texture vectors
			for (Enumeration vertices = mesh.allVertices(true);
			vertices.hasMoreElements();) {
				UVVector vertex = (UVVector)vertices.nextElement();
				writeUVVertex(vertex);
			}
			// incStemsProgressCount();
		} else {
			// vertex and normal vectors
			for (Enumeration vertices = mesh.allVertices(false);
				vertices.hasMoreElements();) {
					Vertex vertex = (Vertex)vertices.nextElement();
					
					if (type=="v") {
						writeVertex(vertex.point,"v");
					} else {
						writeVertex(vertex.normal,"vn");
					}
			}
				
			incProgressCount(AbstractExporter.MESH_PROGRESS_STEP);
		}

	}
	
	private void writeLeafVertices(String type) throws Exception {
		
		if (type == "vt") { 
			// texture vectors
			if (leafMesh.isFlat()) {
				for (int i=0; i<leafMesh.getShapeVertexCount(); i++) {
					writeUVVertex(leafMesh.shapeUVAt(i));
				}	
			}
			
		} else {
			
			OBJLeafVertexWriter vertexExporter = 
				new OBJLeafVertexWriter(tree, this, leafMesh, 
					vertexOffset, type);
			tree.traverseTree(vertexExporter);
			vertexOffset = vertexExporter.leafVertexOffset;
/*
			// vertex and normal vectors
			Enumeration leaves = tree.allLeaves();
			
			while (leaves.hasMoreElements()) {
				Leaf l = (Leaf)leaves.nextElement();
				
				for (int i=0; i<leafMesh.getShapeVertexCount(); i++) {
					
					if (type=="v") {
						writeVertex(l.transf.apply(leafMesh.shapeVertexAt(i).point),type);
					} else {
						writeVertex(l.transf.apply(leafMesh.shapeVertexAt(i).normal),type);
					}
				}
				
				incVertexProgressCount();
			}
			*/
		}
	}
	
	
	
	private void writeStemFaces() throws Exception {
		// output mesh triangles
		vertexOffset = 1;
		//boolean separate_trunk = false;
		int levels = ((IntParam)meshFactory.getParam("Levels")).intValue();
		for (int stemLevel = 0; stemLevel<levels; stemLevel++) {
		
			// => start a new group
			w.println("g "+
					(stemLevel==0 ? "trunk" : "stems_"+stemLevel));
			w.println("usemtl "+
					(stemLevel==0 ? "trunk" : "stems_"+stemLevel));
			
			for (Enumeration parts=mesh.allParts(stemLevel);
				parts.hasMoreElements();) { 

				MeshPart mp = (MeshPart)parts.nextElement();
				uvVertexOffset = 1 + mesh.firstUVIndex(mp.getStem().getLevel());
				w.println("s "+smoothingGroup++);
				
				Enumeration faces=mp.allFaces(mesh,vertexOffset,false);
				Enumeration uvFaces=mp.allFaces(mesh,uvVertexOffset,true);
				
				while (faces.hasMoreElements()) {
					Face face = (Face)faces.nextElement();
					Face uvFace = (Face)uvFaces.nextElement();
					writeFace(face,0,uvFace,0,outputStemUVs,outputNormals);
				}
				
				vertexOffset += mp.vertexCount();
				
				// FIXME: only needed for last stem before leaves
				uvVertexOffset += mp.uvCount();
				
				
				//			offset += ((MeshPart)mesh.elementAt(i)).vertexCount();
				
				incProgressCount(AbstractExporter.MESH_PROGRESS_STEP);
			}
		}
	}
/*	
	private void writeLeafFaces() {
		
//		long leafFaceOffset=0;
		
		Enumeration leaves = tree.allLeaves();
		
		if (leaves.hasMoreElements()) {
			
			w.println("g leaves");
			w.println("usemtl leaves");
//			uvVertexOffset++;
		
			while (leaves.hasMoreElements()) {
				// only leaf number is needed here
				Leaf l = (Leaf)leaves.nextElement();
				
				w.println("s "+smoothingGroup++);
				for (int i=0; i<leafMesh.getShapeFaceCount(); i++) {
					Face face = leafMesh.shapeFaceAt(i);
					writeFace(
							face,vertexOffset,
							face,uvVertexOffset,
							outputLeafUVs,outputNormals);
				}
				
				// increment face offset
				vertexOffset += leafMesh.getShapeVertexCount();
				
				incFaceProgressCount();
			}
		}
		
	}
	*/
	private void writeVertex(Vector v, String type) {
		w.println(type+" "
				+frm.format(v.getX())+" "
				+frm.format(v.getZ())+" "
				+frm.format(v.getY()));
	}

	private void writeUVVertex(UVVector v) {
		w.println("vt "
				+frm.format(v.u)+" "
				+frm.format(v.v)+" "
				+frm.format(0));
	}
	
	private void writeFace(Face f, long offset, Face uv, long uvOffset, boolean writeUVs, boolean writeNormals) {
		w.print("f "); 
				
		for (int i=0; i<f.points.length; i++) {
			w.print(offset+f.points[i]);
			if (writeUVs || writeNormals) {
				w.print("/");
				if (writeUVs) w.print(uvOffset+uv.points[i]);
				if (writeNormals) w.print("/"+offset+f.points[i]);
			}
			if (i<f.points.length-1) w.print(" ");
			else w.println();
		}
	}

	
}
