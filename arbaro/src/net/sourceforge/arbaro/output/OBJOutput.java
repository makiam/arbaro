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
	long faceOffset;
	long uvFaceOffset;
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
			mesh = tree.createStemMesh();
			leafMesh = tree.createLeafMesh();

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
			for (int i=0; i<mesh.firstMeshPart.length; i++) { 
				
				MeshPart mp = (MeshPart)mesh.elementAt(mesh.firstMeshPart[i]);
				
				for (int j=0; j<mp.size(); j++) { 
					
					MeshSection ms = ((MeshSection)mp.elementAt(j));
					
					if (ms.size()==1) {
						writeUVVertex(ms.uvAt(0));
					} else {
						for (int k=0; k<ms.size()+1; k++) {
							writeUVVertex(ms.uvAt(k));
						}
					}
				}
				// incStemsProgressCount();
			}	
			
		} else {
			// vertex and normal vectors
			for (int i=0; i<mesh.size(); i++) {
				
				MeshPart mp =(MeshPart)mesh.elementAt(i);
				for (int j=0; j<mp.size(); j++) {
					
					MeshSection ms = (MeshSection)mp.elementAt(j);
					for (int k=0; k<ms.size(); k++) {
						
						if (type=="v") {
							writeVertex(((Vertex)ms.elementAt(k)).point,"v");
						} else {
							writeVertex(((Vertex)ms.elementAt(k)).normal,"vn");
						}
					}
				}
				
				incVertexProgressCount();
			}
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
		faceOffset = 1;
		boolean separate_trunk = false;
		
		if (((FloatParam)tree.getParam("0SegSplits")).doubleValue()>0 ||
			((IntParam)tree.getParam("0BaseSplits")).intValue()>0) {

//			 FIXME: It would be desirable to put the trunk into
//			a separate group for splitting stems too, but than
//	        the mesh must be ordered by stemlevel
//			
//			another problem is, that the uv coordinates of 
//			clones shouldn't start from below of the texture

			w.println("g stems");
			separate_trunk=false;
		} else {
			w.println("g trunk");
			separate_trunk=true;
		}
		
		for (int i=0; i<mesh.size(); i++) { 

			MeshPart mp = (MeshPart)mesh.elementAt(i);
			if (separate_trunk && mp.getStem().stemlevel>0) {
				separate_trunk=false;
				w.println("g stems");
			}
			
			uvFaceOffset = 1 + mesh.firstUVIndex(mp.getStem().stemlevel);
			
			w.println("s "+smoothingGroup++);
			for (int j=0; j<mp.size()-1; j++) {
				
				MeshSection ms = (MeshSection)mp.elementAt(j);
				java.util.Vector faces = mp.faces(faceOffset,ms);
				java.util.Vector uvFaces = mp.uvFaces(uvFaceOffset,ms);
				faceOffset += ms.size();
				uvFaceOffset += ms.size()==1? 1 : ms.size()+1;

				for (int k=0; k<faces.size(); k++) {
					writeFace((Face)faces.elementAt(k),0,
							(Face)uvFaces.elementAt(k),0,outputStemUVs,outputNormals);
				}
			}
			
			faceOffset +=((MeshSection)mp.elementAt(mp.size()-1)).size();
			
//			offset += ((MeshPart)mesh.elementAt(i)).vertexCount();
			
			incFaceProgressCount();
			
		}
		
		
	}
	
	private void writeLeafFaces() {
		
//		long leafFaceOffset=0;
		
		Enumeration leaves = tree.allLeaves();
		
		if (leaves.hasMoreElements()) {
			
			w.println("g leaves");
			uvFaceOffset++;
		
			while (leaves.hasMoreElements()) {
				// only leaf number is needed here
				Leaf l = (Leaf)leaves.nextElement();
				
				w.println("s "+smoothingGroup++);
				for (int i=0; i<leafMesh.getShapeFaceCount(); i++) {
					Face face = leafMesh.shapeFaceAt(i);
					writeFace(
							face,faceOffset,
							face,uvFaceOffset,
							outputLeafUVs,outputNormals);
				}
				
				// increment face offset
				faceOffset += leafMesh.getShapeVertexCount();
				
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
				
		for (int i=0; i<3; i++) {
			w.print(offset+f.points[i]);
			if (writeUVs || writeNormals) {
				w.print("/");
				if (writeUVs) w.print(uvOffset+uv.points[i]);
				if (writeNormals) w.print("/"+offset+f.points[i]);
			}
			if (i<2) w.print(" ");
			else w.println();
		}
	}

}
