//#**************************************************************************
//#
//#    $Id$  
//#      - Output class for writing Povray mesh2 objects
//#          
//#
//#    Copyright (C) 2004  Wolfram Diestel
//#
//#    This program is free software; you can redistribute it and/or modify
//#    it under the terms of the GNU General Public License as published by
//#    the Free Software Foundation; either version 2 of the License, or
//#    (at your option) any later version.
//#
//#    This program is distributed in the hope that it will be useful,
//#    but WITHOUT ANY WARRANTY; without even the implied warranty of
//#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//#    GNU General Public License for more details.
//#
//#    You should have received a copy of the GNU General Public License
//#    along with this program; if not, write to the Free Software
//#    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//#
//#    Send comments and bug fixes to diestel@steloj.de
//#
//#**************************************************************************/

package net.sourceforge.arbaro.output;

import java.io.PrintWriter;
import java.util.Enumeration;
import java.text.NumberFormat;

import net.sourceforge.arbaro.tree.*;
import net.sourceforge.arbaro.mesh.*;
import net.sourceforge.arbaro.transformation.Vector;
import net.sourceforge.arbaro.params.FloatFormat;


/**
 * @author Wolfram Diestel
 *
 */

public class PovMeshOutput extends Output {
	Mesh mesh;
	LeafMesh leafMesh;
	Progress progress;
	long leafFaceOffset;
	long stemsProgressCount=0;
	long leavesProgressCount=0;
	
	boolean outputStemNormals;
	
	public PovMeshOutput(Tree aTree, PrintWriter pw, Progress prg) {
		super(aTree,pw,prg);
	}
	
	public void write() throws ErrorOutput {
		try {
			NumberFormat frm = FloatFormat.getInstance();
			progress = tree.getProgress();
			
			// write tree definition as comment
			w.println("/*************** Tree made by: ******************");
			w.println();
			w.println(net.sourceforge.arbaro.arbaro.programName);
			w.println();
			tree.params.toXML(w);
			w.println("************************************************/");
			
			// tree scale
			w.println("#declare " + povrayDeclarationPrefix() + "height = " 
					+ frm.format(tree.getHeight()) + ";");
			
			writeStems();
			writeLeaves();
			
			w.flush();
		}
		catch (Exception e) {
			System.err.println(e);
			throw new ErrorOutput(e.getMessage());
			//e.printStackTrace(System.err);
		}
	}
	
	private void incStemsProgressCount() {
		if (stemsProgressCount++ % 100 == 0) {
			progress.incProgress(100);
			if (tree.params.verbose) System.err.print(".");
		}
	}
	
	private void incLeavesProgressCount() {
		if (leavesProgressCount++ % 500 == 0) {
			progress.incProgress(500);
			if (tree.params.verbose) System.err.print(".");
		}
	}
	
	/**
	 * Returns a prefix for the Povray objects names,
	 * it consists of the species name and the random seed
	 * 
	 * @return the prefix string
	 */
	private String povrayDeclarationPrefix() {
		return tree.params.Species + "_" + tree.params.Seed + "_";
	}
	
	private void writeLeaves() throws Exception {
		//    	double leafLength = tree.params.LeafScale/Math.sqrt(tree.params.LeafQuality);
		//    	double leafWidth = tree.params.LeafScale*tree.params.LeafScaleX/Math.sqrt(tree.params.LeafQuality);
		//    	LeafMesh mesh = new LeafMesh(tree.params.LeafShape,leafLength,leafWidth,tree.params.LeafStemLen);
		
		leafMesh = tree.createLeafMesh();
		
		progress.beginPhase("Writing leaf mesh",tree.getLeafCount()*2);
		long leafCount = tree.getLeafCount();
		
		if (leafCount>0) {
			w.println("#declare " + povrayDeclarationPrefix() + "leaves = mesh2 {");
			w.println("     vertex_vectors { "+leafMesh.getShapeVertexCount()*leafCount);
			writeLeavesPoints();
			w.println("     }");
			/* FIXME: add this if needed
			 w.println("     normal_vectors { "+mesh.getShapeVertexCount()*leafCount);
			 trunk.povray_leaves_normals(w,mesh);
			 w.println("     }");
			 */
			leafFaceOffset=0;
			
			w.println("     face_indices { "+leafMesh.getShapeFaceCount()*leafCount);
			writeLeavesFaces();
			w.println("     }");
			w.println("}");
		} else {
			// empty declaration
			w.println("#declare " + povrayDeclarationPrefix() + "leaves = sphere {<0,0,0>,0}");		
		}
		
		progress.endPhase();
	}	
	
	/**
	 * 	Outputs Povray code points section of the mesh2 object for the leaves
	 *  
	 * @param w the output stream
	 * @param mesh the mesh object
	 * @throws Exception
	 */
	private void writeLeavesPoints() throws Exception {
		Enumeration leaves = tree.allLeaves();
		String indent = "    ";
		
		while (leaves.hasMoreElements()) {
			Leaf l = (Leaf)leaves.nextElement();
			
			for (int i=0; i<leafMesh.getShapeVertexCount(); i++) {
				writeVector(l.transf.apply(leafMesh.shapeVertexAt(i).point));
				
				if (i<leafMesh.getShapeVertexCount()-1) {
					w.print(",");
				}
				if (i % 3 == 2) {
					// new line
					w.println();
					w.print(indent+"          ");
				} 
			}
			
			incLeavesProgressCount();
		}
	}
	
	/**
	 * Outputs Povray code points section of the mesh2 object for the leaves
	 * 
	 * @param w the output stream
	 * @param mesh the mesh object
	 * @throws Exception
	 */
	private void writeLeavesFaces() throws Exception {
		Enumeration leaves = tree.allLeaves();
		String indent = "    ";
		
		while (leaves.hasMoreElements()) {
			// only leaf number is needed here
			Leaf l = (Leaf)leaves.nextElement();
			
			for (int i=0; i<leafMesh.getShapeFaceCount(); i++) {
				Face face = leafMesh.shapeFaceAt(i);
				w.print("<" + (leafFaceOffset+face.points[0]) + "," 
						+ (leafFaceOffset+face.points[1]) + "," 
						+ (leafFaceOffset+face.points[2]) + ">");
				if (i<leafMesh.getShapeFaceCount()-1) {
					w.print(",");
				}
				if (i % 6 == 4) {
					// new line
					w.println();
					w.print(indent + "          ");
				}
			}
			w.println();
			
			// increment face offset
			leafFaceOffset += leafMesh.getShapeVertexCount();
			
			incLeavesProgressCount();
		}
	}
	
	
	/**
	 * Outputs Povray code normals section of the mesh2 object for the leaves
	 *  
	 * @param w the output stream
	 * @param mesh the mesh object
	 * @throws Exception
	 */
	private void writeLeavesNormals() throws Exception {
		Enumeration leaves = tree.allLeaves();
		String indent = "    ";
		
		while (leaves.hasMoreElements()) {
			Leaf l = (Leaf)leaves.nextElement();
			
			for (int i=0; i<leafMesh.getShapeVertexCount(); i++) {
				writeVector(l.transf.apply(leafMesh.shapeVertexAt(i).normal));
				
				if (i<leafMesh.getShapeVertexCount()-1) {
					w.print(",");
				}
				if (i % 3 == 2) {
					// new line
					w.println();
					w.print(indent+"          ");
				} 
			}
			
			incLeavesProgressCount();
			throw new Exception("Not implemented: if using normals for leaves use factor "+
			"3 instead of 2 in progress.beginPhase");
		}
	}
	
	private void writeStems() throws Exception {
		String indent="  ";
		
		// FIXME: instead of outputStemNormals = true use separate boolean
		// for every level
		outputStemNormals = true;
		
		mesh = tree.createStemMesh();
		int vertex_cnt = mesh.vertexCount();
		int face_cnt = mesh.faceCount();
		
		progress.beginPhase("Writing stem mesh",tree.getStemCount()*3);
		
		w.println("#declare " + povrayDeclarationPrefix() + "stems = "); 
		w.println(indent + "mesh2 {");
		
		
		// output section points
		w.println(indent+"  vertex_vectors { " + vertex_cnt);
		for (int i=0; i<mesh.size(); i++) {
			writeStemPoints((MeshPart)mesh.elementAt(i),indent);
			w.println();
			
			incStemsProgressCount();
		}
		w.println(indent + "  }");
		
		
		// output normals
		if (outputStemNormals) {
			w.println(indent + "  normal_vectors { " + vertex_cnt); 
			
			for (int i=0; i<mesh.size(); i++) try { 
				writeStemNormals((MeshPart)mesh.elementAt(i),indent);
				w.println();
				
				incStemsProgressCount();
				
			} catch (Exception e) {
				throw new ErrorMesh("Error in MeshPart "+i+": "+e); //.getMessage());
			}	    
			w.println(indent+"  }");
		}
		
		
		// output mesh triangles
		w.println(indent + "  face_indices { " + face_cnt);
		long offset = 0;
		for (int i=0; i<mesh.size(); i++) { 
			writeStemFaces((MeshPart)mesh.elementAt(i),offset,indent);
			offset += ((MeshPart)mesh.elementAt(i)).vertexCount();
			w.println();
			
			incStemsProgressCount();
			
		}
		w.println(indent + "  }");
		
		
		// use less memory
		// w.println(indent+"  hierarchy off");
		
		w.println(indent + "}");
		progress.endPhase();
		
		/*
		 if (debugmesh) try {
		 // draw normals as cones
		  w.println("union {");
		  for (int i=0; i<size(); i++) { 
		  MeshSection section = ((MeshSection)elementAt(i));
		  for (int j=0; i<section.size(); j++) {
		  w.println("  cone {" 
		  + section.point_at(j).povray()
		  + ",0.01," + (section.point_at(j).add( 
		  section.normal_at(j)).mul(0.2).povray()) 
		  + ",0}");
		  }
		  }
		  w.println("}");
		  } catch (Exception e) {}
		  */
		
	}
	
	
	private void writeStemPoints(MeshPart mp, String indent) {
		w.println(indent + "  /* stem " + mp.getTreePosition() + "*/ ");
		for (int i=0; i<mp.size(); i++) {
			w.print(indent + "  /*" + i + "*/ ");
			writeSectionPoints((MeshSection)mp.elementAt(i),indent);
			w.println();
		}
	}	
	
	public void writeStemFaces(MeshPart mp, long firstPt, String indent) 
	throws ErrorMesh {
		
		if (mp.faceCount() == 0) {
			// stem radius to small, this error should be gone
			// after not making stems with too small length or radius
			System.err.println("WARNING: no faces in mesh part of stem "+
					mp.getTreePosition() + " - stem radius too small");
			return;
		}
		
		w.println(indent + "  /* stem " + mp.getTreePosition() + "*/ ");
		for (int i=0; i<mp.size()-1; i++) { 
			java.util.Vector faces = mp.faces(firstPt,(MeshSection)mp.elementAt(i));
			firstPt += ((MeshSection)mp.elementAt(i)).size();
			w.print(indent + "  /*" + i + "*/ ");
			for (int j=0; j<faces.size(); j++) {
				w.print("<" + ((Face)faces.elementAt(j)).points[0] + "," 
						+ ((Face)faces.elementAt(j)).points[1] + "," 
						+ ((Face)faces.elementAt(j)).points[2] + ">");
				if ((i<mp.size()-2) || (j<faces.size()-1)) {
					w.print(",");
				}
				if (j % 6 == 4) {
					// new line
					w.println();
					w.print(indent + "          ");
				}
			}
			w.println();
		}
		
		//? return firstPt;
	}
	
	private void writeStemNormals(MeshPart mp, String indent) 
	throws ErrorMesh {
		
		mp.setNormals();
		
		w.println(indent + "  /* stem " + mp.getTreePosition() + "*/ ");
		for (int i=0; i<mp.size(); i++) try { 
			w.print(indent + "  /*" + i + "*/");
			writeSectionNormals((MeshSection)mp.elementAt(i),indent);
			w.println();
		} catch (Exception e) {
			// e.printStackTrace(System.err);
			throw new ErrorMesh("Error in MeshSection "+i+": "+e); //.getMessage());
		}	    
	}
	
	public void writeSectionPoints(MeshSection ms, String indent) {
		for (int i=0; i<ms.size(); i++) {
			writeVector(((Vertex)ms.elementAt(i)).point);
			if (ms.next != null || i<ms.size()-1) {
				w.print(",");
			}
			if (i % 3 == 2) {
				// new line
				w.println();
				w.print(indent+"          ");
			} 
		}
	}
	
	public void writeSectionNormals(MeshSection ms, String indent) {
		for (int i=0; i<ms.size(); i++) {
			writeVector(((Vertex)ms.elementAt(i)).normal);
			
			//DBG
			/*
			 Vector v = ((Vertex)elementAt(i)).normal;
			 String s = v.povray();
			 if (s.length() < 10) {
			 System.err.println("STRANGENORMAL: x:"+v.getX()+" y:"+v.getY()+" z:"+v.getZ());
			 }
			 */
			
			if (ms.next != null|| i<ms.size()-1) {
				w.print(",");
			}
			if (i % 3 == 2) {
				// new line
				w.println();
				w.print(indent+"          ");
			} 
		}
	}
	
	private void writeVector(Vector v) {
		NumberFormat fmt = FloatFormat.getInstance();
		w.print("<"+fmt.format(v.getX())+","
		+fmt.format(v.getZ())+","
		+fmt.format(v.getY())+">");
	}
	
}

