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
//		 use smoothing to interpolate normals			
		boolean outputNormals = false;
		smoothingGroup=1;

		long objCount = 
			(tree.getStemCount()
			+tree.getLeafCount())*(outputNormals? 2 : 1); 

		try {
			mesh = tree.createStemMesh();
			leafMesh = tree.createLeafMesh();

			// vertices
			progress.beginPhase("Writing vertices",objCount);
			
			writeStemVertices(false);
			writeLeafVertices(false);

// use smoothing to interpolate normals			
			if (outputNormals) {
				writeStemVertices(true);
				writeLeafVertices(true);
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
	
	private void writeStemVertices(boolean normals) throws Exception {
	
		for (int i=0; i<mesh.size(); i++) {

			MeshPart mp =(MeshPart)mesh.elementAt(i);
			for (int j=0; j<mp.size(); j++) {

				MeshSection ms = (MeshSection)mp.elementAt(j);
				for (int k=0; k<ms.size(); k++) {
					
					if (normals) {
						writeVertex(((Vertex)ms.elementAt(k)).normal,true);
					} else {
						writeVertex(((Vertex)ms.elementAt(k)).point,false);
					}
				}
			}
			
			incVertexProgressCount();
		}
		

	}
	
	private void writeLeafVertices(boolean normals) {
		
		Enumeration leaves = tree.allLeaves();
		
		while (leaves.hasMoreElements()) {
			Leaf l = (Leaf)leaves.nextElement();
			
			for (int i=0; i<leafMesh.getShapeVertexCount(); i++) {
				
				if (normals) {
					writeVertex(l.transf.apply(leafMesh.shapeVertexAt(i).normal),true);
				} else {
					writeVertex(l.transf.apply(leafMesh.shapeVertexAt(i).point),false);
				}
			}
			
			incVertexProgressCount();
		}
	
	}
	
	
	
	private void writeStemFaces() throws Exception {
		w.println("g stems");
		
		// output mesh triangles
		faceOffset = 1;
		for (int i=0; i<mesh.size(); i++) { 

			MeshPart mp = (MeshPart)mesh.elementAt(i);
			
			w.println("s "+smoothingGroup++);
			for (int j=0; j<mp.size()-1; j++) {
				
				java.util.Vector faces = mp.faces(faceOffset,(MeshSection)mp.elementAt(j));
				faceOffset += ((MeshSection)mp.elementAt(j)).size();

				for (int k=0; k<faces.size(); k++) {
					writeFace((Face)faces.elementAt(k),0);
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
		
		if (leaves.hasMoreElements()) w.println("g leaves");
		
		while (leaves.hasMoreElements()) {
			// only leaf number is needed here
			Leaf l = (Leaf)leaves.nextElement();
			
			w.println("s "+smoothingGroup++);
			for (int i=0; i<leafMesh.getShapeFaceCount(); i++) {
				Face face = leafMesh.shapeFaceAt(i);
				writeFace(face,faceOffset);
			}
			
			// increment face offset
			faceOffset += leafMesh.getShapeVertexCount();
			
			incFaceProgressCount();
		}
	
		
	}
	
	private void writeVertex(Vector v, boolean normal) {
		w.println((normal?"vn ":"v ")
				+frm.format(v.getX())+" "
				+frm.format(v.getZ())+" "
				+frm.format(v.getY()));
	}
	
	private void writeFace(Face f, long offset) {
		w.println("f " 
				+ (offset+f.points[0]) + " " 
				+ (offset+f.points[1]) + " "	
				+ (offset+f.points[2]));
	}

}
