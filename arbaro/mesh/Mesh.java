//  #**************************************************************************
//  #
//  #    {$file}  - a Mesh to hold the mesh points, triangles and normals
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

package mesh;

import java.io.PrintWriter;
import java.lang.Math;
import mesh.MeshSection;

class ErrorMesh extends Exception {
    public ErrorMesh(String msg) {
	super(msg);
    }
};

class Face {
    int [] points;
    Face(int i, int j, int k) {
	points = new int[3];
	points[0]=i;
	points[1]=j;
	points[2]=k;
    }
}

public class Mesh extends java.util.Vector {
    final boolean debugmesh = false;


    // A class for creation, handling and output a mesh object
    public Mesh() { }

    public void add_section(MeshSection section) {
	//if (stem.tree.verbose) cerr << (".");
	
	if (size() > 0) {
	    // connect section with last of sections
	    ((MeshSection)lastElement()).next = section;
	    section.previos = (MeshSection)lastElement();
	}
	
	addElement(section);
    }
    
    void set_normals() {
	// normals for begin and end point (first and last section)
	// are set to a z-Vector by Segment.mesh()
	//System.err.println("MESHSECT 1: "+((MeshSection)elementAt(1)).size());

	((MeshSection)elementAt(1)).set_normals_up();
	for (int i=2; i<size()-1; i++) {
	    ((MeshSection)elementAt(i)).set_normals_updown();
	    
	    //DBG
	    //MeshSection m = (MeshSection)elementAt(i);
	    //System.err.println("MeshSection "+i+" vert: "+m.size());

	}

	//DBG
	/*
	try {
	MeshSection m = (MeshSection)lastElement();
	System.err.println("MeshSection last vert: "+m.size()+" normal[0] "+m.normal_at(0).povray());
	} catch (Exception e) {}
	*/
	// sections.last().set_normals_down();
    }
    
    int vertex_cnt() {
	// count all meshpoints of all sections
	int cnt=0;
	
	for (int i = 0; i<size(); i++) {
	    cnt += ((MeshSection)elementAt(i)).size();
	}
	return cnt;
    }
    
    int face_cnt()  {
	// calcs how much faces have to be created, povray wants to know this
	// before the faces itself
	int cnt = 0;
	for (int i=0; i<size()-1; i++) {
	    int c_i = ((MeshSection)elementAt(i)).size();
	    int c_i1 = ((MeshSection)elementAt(i)).size();
	    
	    if (c_i != c_i1) {
		cnt += Math.max(c_i,c_i1);
	    } else if (c_i > 1) {
		cnt += 2 * c_i;
	    }
	}
	return cnt;
    }
    
    java.util.Vector faces(int inx, MeshSection section) throws ErrorMesh {
	//returns the triangles between section sec and next
	MeshSection next = section.next;
	java.util.Vector faces = new java.util.Vector();

	if (section.size() ==1 && next.size() == 1) {
	    //FIXME: normaly this shouldn't occur, only for very small radius?
	    // I should warn about this?
	    return faces;
	}

	if (section.size() == 1) {
	    for (int i=0; i<next.size(); i++) {
		faces.addElement(new Face(inx,inx+1+i,inx+1+(i+1)%next.size()));
	    }
	} else if (next.size() == 1) {
	    int ninx = inx+section.size();
	    for (int i=0; i<section.size(); i++) {
		faces.addElement(new Face(inx+i,inx+(i+1)%section.size(),ninx));
	    }
	} else { // section and next must have same point_cnt>1!!!
	    int ninx = inx+section.size();
	    if (section.size() != next.size()) {
		throw new ErrorMesh("Error: vertice numbers of two sections "
				+ "differ ("+inx+","+ninx+")");
	    }
	    for (int i=0; i<section.size(); i++) {
		faces.addElement(new Face(inx+i,inx+(i+1)%section.size(),ninx+i));
		faces.addElement(new Face(inx+(i+1)%section.size(),ninx+i,
				      ninx+(i+1)%next.size()));
	    }
	}
      return faces;
    }
    
    public void povray(PrintWriter w, boolean output_normals, String indent) throws ErrorMesh {
	int face_cnt = face_cnt();

	if (face_cnt == 0) {
	    //FIXME: stem radius to small, can avoid this?
	    // maybe could avoid this, not makin stems with to small length
	    System.err.println("WARNING: no faces in mesh - stem radius to small");
	    return;
	}

	w.println(indent + "mesh2 {");
	// output section points
	w.println(indent+"  vertex_vectors { " + vertex_cnt());
	for (int i=0; i<size(); i++) {
	    w.print(indent + "  /*" + i + "*/ ");
	    ((MeshSection)elementAt(i)).povray_points(w,indent);
	    w.println();
	}
	w.println(indent + "  }");
	
	// output normals
	if (output_normals) {
	    set_normals();
	    w.println(indent + "  normal_vectors { " + vertex_cnt()); 
	    
	    for (int i=0; i<size(); i++) try { 
		w.print(indent + "  /*" + i + "*/");
		((MeshSection)elementAt(i)).povray_normals(w,indent);
		w.println();
	    } catch (Exception e) {
		throw new ErrorMesh("Error in MeshSection "+i+": "+e.getMessage());
	    }	    
	    w.println(indent+"  }");
	}
        
	// output mesh triangles
	w.println(indent + "  face_indices { " + face_cnt());
	int inx = 0;
	for (int i=0; i<size()-1; i++) { 
	    java.util.Vector faces = faces(inx,(MeshSection)elementAt(i));
	    inx += ((MeshSection)elementAt(i)).size();
	    w.print(indent + "  /*" + i + "*/ ");
	    for (int j=0; j<faces.size(); j++) {
		w.print("<" + ((Face)faces.elementAt(j)).points[0] + "," 
				 + ((Face)faces.elementAt(j)).points[1] + "," 
				 + ((Face)faces.elementAt(j)).points[2] + ">");
		if ((i<size()-2) || (j<faces.size()-1)) {
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
	w.println(indent + "  }");
	w.println(indent + "}");
	
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
    }
}












