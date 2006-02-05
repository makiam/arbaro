/*
 * Created on 30.09.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.sourceforge.arbaro.output;

import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.Enumeration;

import net.sourceforge.arbaro.mesh.*;
import net.sourceforge.arbaro.params.FloatFormat;
import net.sourceforge.arbaro.params.FloatParam;
import net.sourceforge.arbaro.params.IntParam;
import net.sourceforge.arbaro.transformation.Vector;
import net.sourceforge.arbaro.tree.Leaf;
import net.sourceforge.arbaro.tree.Tree;

/**
 * @author wdiestel
 *
 */
public final class OBJOutput extends Output {
	long vertexProgressCount=0;
	long faceProgressCount=0;
	NumberFormat frm = FloatFormat.getInstance();
	Mesh mesh;
	LeafMesh leafMesh;
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
	public OBJOutput(Tree aTree, PrintWriter pw, Progress p) {
		super(aTree, pw, p);
	}

	private void incVertexProgressCount() {
		if (vertexProgressCount++ % 100 == 0) {
			progress.incProgress(100);
			if (tree.params.verbose) System.err.print(".");
		}
	}
	
	private void incFaceProgressCount() {
		if (faceProgressCount++ % 500 == 0) {
			progress.incProgress(500);
			if (tree.params.verbose) System.err.print(".");
		}
	}
	
	public void write() throws ErrorOutput {
		smoothingGroup=1;

		long objCount = 
			(tree.getStemCount()
			+tree.getLeafCount())*(outputNormals? 2 : 1); 

		try {
			mesh = tree.createStemMeshByLevel(true /*useQuads*/);
			leafMesh = tree.createLeafMesh(true /*useQuads*/);

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
			writeLeafFaces();
			progress.endPhase();
			w.flush();
			
		}	catch (Exception e) {
			System.err.println(e);
			throw new ErrorOutput(e.getMessage());
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
				
			incVertexProgressCount();
		}

	}
	
	private void writeLeafVertices(String type) {
		
		if (type == "vt") { 
			// texture vectors
			if (leafMesh.isFlat()) {
				for (int i=0; i<leafMesh.getShapeVertexCount(); i++) {
					writeUVVertex(leafMesh.shapeUVAt(i));
				}	
			}
			
		} else {
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
		}
	}
	
	
	
	private void writeStemFaces() throws Exception {
		// output mesh triangles
		vertexOffset = 1;
		boolean separate_trunk = false;
		
		for (int stemLevel = 0; stemLevel<tree.params.Levels; stemLevel++) {
		
			// => start a new group
			w.println("g "+
					(stemLevel==0 ? "trunk" : "stems_"+stemLevel));
			w.println("usemtl "+
					(stemLevel==0 ? "trunk" : "stems_"+stemLevel));
			
			for (Enumeration parts=mesh.allParts(stemLevel);
				parts.hasMoreElements();) { 

				MeshPart mp = (MeshPart)parts.nextElement();
				uvVertexOffset = 1 + mesh.firstUVIndex(mp.getStem().stemlevel);
				w.println("s "+smoothingGroup++);
				
				Enumeration faces=mp.allFaces(vertexOffset,false);
				Enumeration uvFaces=mp.allFaces(uvVertexOffset,true);
				
				while (faces.hasMoreElements()) {
					Face face = (Face)faces.nextElement();
					Face uvFace = (Face)uvFaces.nextElement();
					writeFace(face,0,uvFace,0,outputStemUVs,outputNormals);
				}
				
				vertexOffset += mp.vertexCount();
				
				// FIXME: only needed for last stem before leaves
				uvVertexOffset += mp.uvCount();
				
				
				//			offset += ((MeshPart)mesh.elementAt(i)).vertexCount();
				
				incFaceProgressCount();
			}
		}
	}
	
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
