//  #**************************************************************************
//  #
//  #    $Id$  
//  #          - a Mesh to hold the mesh pieces with points, triangles and normals
//  #               and output them to a povray mesh2 object
//  #
//  #    Copyright (C) 2003  Wolfram Diestel
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
//  #**************************************************************************

package net.sourceforge.arbaro.mesh;

import java.io.PrintWriter;

/**
 * A class for creation, handling and output of a mesh object.
 * A Mesh consists of MeshParts which are tube like objects,
 * made from MeshSections. See the this diagram:
 * <p>
 * <img src="doc-files/Mesh-1.png" />
 * <p>
 *  
 * 
 * @author Wolfram Diestel
 *
 * TODO 
 */
public class Mesh extends java.util.Vector {
    final boolean debugmesh = false;


    public Mesh() { }

    public void add_meshpart(MeshPart meshpart) {
	addElement(meshpart);
    }
    
    int vertex_cnt() {
	// count all meshpoints of all parts
	int cnt=0;
	
	for (int i = 0; i<size(); i++) {
	    cnt += ((MeshPart)elementAt(i)).vertex_cnt();
	}
	return cnt;
    }
    
    int face_cnt()  {
	// calcs how much faces have to be created, povray wants to know this
	// before the faces itself
	int cnt = 0;
	for (int i=0; i<size()-1; i++) {
	    cnt += ((MeshPart)elementAt(i)).face_cnt();
	}
	return cnt;
    }
    
    public void povray(PrintWriter w, boolean output_normals, String indent) 
	throws ErrorMesh {

	int vertex_cnt = vertex_cnt();
	int face_cnt = face_cnt();

	w.println(indent + "mesh2 {");

	// output section points
	w.println(indent+"  vertex_vectors { " + vertex_cnt);
	for (int i=0; i<size(); i++) {
	    ((MeshPart)elementAt(i)).povray_vertices(w,indent);
	    w.println();
	}
	w.println(indent + "  }");
	
	// output normals
	if (output_normals) {
	    w.println(indent + "  normal_vectors { " + vertex_cnt); 
	    
	    for (int i=0; i<size(); i++) try { 
		((MeshPart)elementAt(i)).povray_normals(w,indent);
		w.println();
	    } catch (Exception e) {
		throw new ErrorMesh("Error in MeshPart "+i+": "+e); //.getMessage());
	    }	    
	    w.println(indent+"  }");
	}
        
	// output mesh triangles
	w.println(indent + "  face_indices { " + face_cnt);
	int offset = 0;
	for (int i=0; i<size()-1; i++) { 
	    ((MeshPart)elementAt(i)).povray_faces(w,offset,indent);
	    offset += ((MeshPart)elementAt(i)).vertex_cnt();
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
}












