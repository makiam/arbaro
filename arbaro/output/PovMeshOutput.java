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
import net.sourceforge.arbaro.transformation.Transformation;
import net.sourceforge.arbaro.transformation.Vector;
import net.sourceforge.arbaro.params.FloatFormat;


/**
 * @author Wolfram Diestel
 *
 */

public class PovMeshOutput extends Output {
	Mesh mesh;
	LeafMesh lmesh;
	long leafFaceOffset;
	
	boolean output_normals;
	
	public PovMeshOutput(Tree aTree, PrintWriter pw) {
		super(aTree,pw);
	}
	
	
	public void write() throws ErrorOutput {
		try {
	    	NumberFormat frm = FloatFormat.getInstance();
	    	// tree scale
	    	w.println("#declare " + pov_prefix() + "scale = " 
	    			+ frm.format(tree.params.scale_tree) + ";");

			mesh_stems();
			mesh_leaves();
			w.flush();
		}
		catch (Exception e) {
			System.err.println(e);
			throw new ErrorOutput(e.getMessage());
			//e.printStackTrace(System.err);
	  	}
	}

    /**
     * Returns a prefix for the Povray objects names,
     * it consists of the species name and the random seed
     * 
     * @return the prefix string
     */
    private String pov_prefix() {
    	return tree.getSpecies() + "_" + tree.params.Seed + "_";
    }

    private void mesh_leaves() throws Exception {
//    	double leafLength = tree.params.LeafScale/Math.sqrt(tree.params.LeafQuality);
//    	double leafWidth = tree.params.LeafScale*tree.params.LeafScaleX/Math.sqrt(tree.params.LeafQuality);
//    	LeafMesh mesh = new LeafMesh(tree.params.LeafShape,leafLength,leafWidth,tree.params.LeafStemLen);

    	lmesh = tree.createLeafMesh(); 
    	long leafCount = tree.getLeafCount();
    	
    	if (leafCount>0) {
    		w.println("#declare " + pov_prefix() + "leaves = mesh2 {");
    		w.println("     vertex_vectors { "+lmesh.getShapeVertexCount()*leafCount);
    		for (int t=0; t<tree.trunks.size(); t++) {
    			leaves_points((Stem)tree.trunks.elementAt(t));
    		}
    		w.println("     }");
    		/* FIXME: add this if needed
    		 w.println("     normal_vectors { "+mesh.getShapeVertexCount()*leafCount);
    		 trunk.povray_leaves_normals(w,mesh);
    		 w.println("     }");
    		 */
    		leafFaceOffset=0;
    		
    		w.println("     face_indices { "+lmesh.getShapeFaceCount()*leafCount);
    		for (int t=0; t<tree.trunks.size(); t++) {
    			leaves_faces((Stem)tree.trunks.elementAt(t));
    		}
    		w.println("     }");
    		w.println("}");
    	} else {
    		// empty declaration
    	    w.println("#declare " + pov_prefix() + "leaves = sphere {<0,0,0>,0}");		
    	}
    }	
    
    /**
     * 	Outputs Povray code points section of the mesh2 object for the leaves
     *  
     * @param w the output stream
     * @param mesh the mesh object
     * @throws Exception
     */
    private void leaves_points(Stem s) throws Exception {
    	Enumeration leaves = tree.allLeaves();
    	String indent = "    ";
    	
    	while (leaves.hasMoreElements()) {
    		Leaf l = (Leaf)leaves.nextElement();

    		//tree.incPovProgress(s.substems.size());
			leafmesh_points(indent,l.transf);
    	}
    	
//    	if (tree.params.verbose) {
//    		if (s.stemlevel<=1 && s.clone_index.size()==0) System.err.print(".");
//    	}
//    	
//    	String indent = "    ";
//    	
//    	// output leaves points
//    	if (s.stemlevel==tree.params.Levels-1) {
//    		w.println( "/* " + s.tree_position() + " */");
//    		
//    		for (int i=0; i<s.leaves.size(); i++) {
//    			leafmesh_points(indent,((Leaf)s.leaves.elementAt(i)).transf);
//    		}
//    		
//    		if (s.clones != null) {
//    			for (int i=0; i<s.clones.size(); i++) {
//    				leaves_points((Stem)s.clones.elementAt(i));
//    			}
//    		}
//    		
//    	}
//    	
//    	// recursive call to substems
//    	else {
//    		// FIXME? more correct it would be inc by 1 in the for block,
//    		// but this would need more calls to synchronized incProgress
//    		// if this work ok, don't change this
//    		tree.incPovProgress(s.substems.size());
//    		for (int i=0; i<s.substems.size(); i++) {
//    			leaves_points((Stem)s.substems.elementAt(i));
//    		}
//    		if (s.clones != null) {
//    			for (int i=0; i<s.clones.size(); i++) {
//    				leaves_points((Stem)s.clones.elementAt(i));
//    			}	  	
//    		}
//    	}
    }


    /**
     * Outputs Povray code points section of the mesh2 object for the leaves
     * 
     * @param w the output stream
     * @param mesh the mesh object
     * @throws Exception
     */
    private void leaves_faces(Stem s) throws Exception {
    	Enumeration leaves = tree.allLeaves();
    	String indent = "    ";
    	
    	while (leaves.hasMoreElements()) {
    		Leaf l = (Leaf)leaves.nextElement();

    		//tree.incPovProgress(s.substems.size());
			leafmesh_faces(indent);
    	}
    	
//    	if (tree.params.verbose) {
//    		if (s.stemlevel<=1 && s.clone_index.size()==0) System.err.print(".");
//    	}
//    	
//    	String indent = "    ";
//    	
//    	// output leaves points
//    	if (s.stemlevel==tree.params.Levels-1) {
//    		w.println( "/* " + s.tree_position() + " */");
//    		
//    		for (int i=0; i<s.leaves.size(); i++) {
//    			leafmesh_faces(indent);
//    		}
//    		
//    		if (s.clones != null) {
//    			for (int i=0; i<s.clones.size(); i++) {
//    				leaves_faces((Stem)s.clones.elementAt(i));
//    			}
//    		}
//    		
//    	}
//    	
//    	// recursive call to substems
//    	else {
//    		// FIXME? more correct it would be inc by 1 in the for block,
//    		// but this would need more calls to synchronized incProgress
//    		// if this work ok, don't change this
//    		tree.incPovProgress(s.substems.size());
//    		for (int i=0; i<s.substems.size(); i++) {
//    			leaves_faces((Stem)s.substems.elementAt(i));
//    		}
//    		if (s.clones != null) {
//    			for (int i=0; i<s.clones.size(); i++) {
//    				leaves_faces((Stem)s.clones.elementAt(i));
//    			}	  	
//    		}
//    	}
    }


    /**
     * Outputs Povray code normals section of the mesh2 object for the leaves
     *  
     * @param w the output stream
     * @param mesh the mesh object
     * @throws Exception
     */
    private void leaves_normals(Stem s) throws Exception {
    	Enumeration leaves = tree.allLeaves();
    	String indent = "    ";
    	
    	while (leaves.hasMoreElements()) {
    		Leaf l = (Leaf)leaves.nextElement();

    		//tree.incPovProgress(s.substems.size());
			leafmesh_normals(indent,l.transf);
    	}
    	
//    	if (tree.params.verbose) {
//    		if (s.stemlevel<=1 && s.clone_index.size()==0) System.err.print(".");
//    	}
//    	
//    	String indent = "    ";
//    	
//    	// output leaves points
//    	if (s.stemlevel==tree.params.Levels-1) {
//    		w.println( "/* " + s.tree_position() + " */");
//    		
//    		for (int i=0; i<s.leaves.size(); i++) {
//    			leafmesh_normals(indent,((Leaf)s.leaves.elementAt(i)).transf);
//    		}
//    		
//    		if (s.clones != null) {
//    			for (int i=0; i<s.clones.size(); i++) {
//    				leaves_normals((Stem)s.clones.elementAt(i));
//    			}
//    		}
//    		
//    	}
//    	
//    	// recursive call to substems
//    	else {
//    		// FIXME? more correct it would be inc by 1 in the for block,
//    		// but this would need more calls to synchronized incProgress
//    		// if this work ok, don't change this
//    		tree.incPovProgress(s.substems.size());
//    		for (int i=0; i<s.substems.size(); i++) {
//    			leaves_normals((Stem)s.substems.elementAt(i));
//    		}
//    		if (s.clones != null) {
//    			for (int i=0; i<s.clones.size(); i++) {
//    				leaves_normals((Stem)s.clones.elementAt(i));
//    			}	  	
//    		}
//    	}
    }

    void leafmesh_points(String indent, Transformation transf) {
    	for (int i=0; i<lmesh.getShapeVertexCount(); i++) {
    		w.print(transf.apply(lmesh.shapeVertexAt(i).point).povray());
    		// FIXME: add a comma too after each leaf but the last
    		if (i<lmesh.getShapeVertexCount()-1) {
    			w.print(",");
    		}
    		if (i % 3 == 2) {
    			// new line
    			w.println();
    			w.print(indent+"          ");
    		} 
    	}
    }
    
    void leafmesh_faces(String indent) {
    	for (int i=0; i<lmesh.getShapeFaceCount(); i++) {
    		Face face = lmesh.shapeFaceAt(i);
    		w.print("<" + (leafFaceOffset+face.points[0]) + "," 
    				+ (leafFaceOffset+face.points[1]) + "," 
					+ (leafFaceOffset+face.points[2]) + ">");
    		if (i<lmesh.getShapeFaceCount()-1) {
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
    	leafFaceOffset += lmesh.getShapeVertexCount();
    }
    
    void leafmesh_normals(String indent, Transformation transf) {
    	for (int i=0; i<lmesh.getShapeVertexCount(); i++) {
    		w.print(transf.apply(lmesh.shapeVertexAt(i).normal).povray());
    		// FIXME: add a comma too after each leaf but the last
    		if (i<lmesh.getShapeVertexCount()-1) {
    			w.print(",");
    		}
    		if (i % 3 == 2) {
    			// new line
    			w.println();
    			w.print(indent+"          ");
    		} 
    	}
    }

	private void mesh_stems() 
	throws Exception {
		String indent="  ";
	    // FIXME: instead of output_normals = true use separate bool for every
	    // level
	    output_normals = true;
		mesh = tree.createStemMesh();

		w.println("#declare " + pov_prefix() + "stems = "); 

		
		int vertex_cnt = mesh.vertex_cnt();
		int face_cnt = mesh.face_cnt();
		
		w.println(indent + "mesh2 {");
		
		// output section points
		w.println(indent+"  vertex_vectors { " + vertex_cnt);
		for (int i=0; i<mesh.size(); i++) {
			meshpart_vertices((MeshPart)mesh.elementAt(i),indent);
			w.println();
		}
		w.println(indent + "  }");
		
		// output normals
		if (output_normals) {
			w.println(indent + "  normal_vectors { " + vertex_cnt); 
			
			for (int i=0; i<mesh.size(); i++) try { 
				meshpart_normals((MeshPart)mesh.elementAt(i),indent);
				w.println();
			} catch (Exception e) {
				throw new ErrorMesh("Error in MeshPart "+i+": "+e); //.getMessage());
			}	    
			w.println(indent+"  }");
		}
		
		// output mesh triangles
		w.println(indent + "  face_indices { " + face_cnt);
		int offset = 0;
		for (int i=0; i<mesh.size()-1; i++) { 
			meshpart_faces((MeshPart)mesh.elementAt(i),offset,indent);
			offset += ((MeshPart)mesh.elementAt(i)).vertex_cnt();
			w.println();
		}
		w.println(indent + "  }");
		
		// use less memory
		// w.println(indent+"  hierarchy off");
		
		w.println(indent + "}");
		
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
	
	
    private void meshpart_vertices(MeshPart mp, String indent) {
    	w.println(indent + "  /* stem " + mp.tree_position + "*/ ");
    	for (int i=0; i<mp.size(); i++) {
    	    w.print(indent + "  /*" + i + "*/ ");
    	    meshsection_points((MeshSection)mp.elementAt(i),indent);
    	    w.println();
    	}
        }	
    	
    public void meshpart_faces(MeshPart mp, int firstPt, String indent) 
    	throws ErrorMesh {

    	if (mp.face_cnt() == 0) {
    	    //FIXME: stem radius to small, can avoid this?
    	    // maybe could avoid this, not makin stems with too small length
    	    System.err.println("WARNING: no faces in mesh part of stem "+
    			       mp.tree_position + " - stem radius too small");
    	    return;
    	}

    	w.println(indent + "  /* stem " + mp.tree_position + "*/ ");
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
    	
    private void meshpart_normals(MeshPart mp, String indent) 
    	throws ErrorMesh {

    	mp.set_normals();

    	w.println(indent + "  /* stem " + mp.tree_position + "*/ ");
    	for (int i=0; i<mp.size(); i++) try { 
    	    w.print(indent + "  /*" + i + "*/");
    	    meshsection_normals((MeshSection)mp.elementAt(i),indent);
    	    w.println();
    	} catch (Exception e) {
    	    // e.printStackTrace(System.err);
    	    throw new ErrorMesh("Error in MeshSection "+i+": "+e); //.getMessage());
    	}	    
    }

    public void meshsection_points(MeshSection ms, String indent) {
    	for (int i=0; i<ms.size(); i++) {
    	    w.print(vector(((Vertex)ms.elementAt(i)).point));
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

        public void meshsection_normals(MeshSection ms, String indent) {
    	for (int i=0; i<ms.size(); i++) {
    	    w.print(vector(((Vertex)ms.elementAt(i)).normal));

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
    
        private String vector(Vector v) {
        	NumberFormat fmt = FloatFormat.getInstance();
        	return "<"+fmt.format(v.getX())+","
        	    +fmt.format(v.getZ())+","
        	    +fmt.format(v.getY())+">";
            }
    
}

