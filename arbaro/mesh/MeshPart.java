//  #**************************************************************************
//  #
//  #    $Id$  
//  #        - a class to hold a piece of the mesh
//  #               and output it's vertices, faces and normals as povray code
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

import java.lang.Math;

// A class for creation, handling and output a piece of a mesh object
public class MeshPart extends java.util.Vector {
    public String tree_position;
    boolean normals;

    public MeshPart(String treepos, boolean normls) { 
	tree_position = treepos;
	// FIXME normals not yes used,
	// other mesh output format needed if theire are
	// less normals then vertices
	normals = normls;
    }

    public void add_section(MeshSection section) {
	//if (stem.tree.verbose) cerr << (".");
	
	if (size() > 0) {
	    // connect section with last of sections
	    ((MeshSection)lastElement()).next = section;
	    section.previos = (MeshSection)lastElement();
	}
	
	addElement(section);
    }
    
    public void set_normals() {
	// normals for begin and end point (first and last section)
	// are set to a z-Vector by Segment.mesh()
	//System.err.println("MESHSECT 1: "+((MeshSection)elementAt(1)).size());

	if (size()>1) {
	    ((MeshSection)elementAt(1)).set_normals_up();
	    for (int i=2; i<size()-1; i++) {
		((MeshSection)elementAt(i)).set_normals_updown();
	    }
	    //DBG
	    //MeshSection m = (MeshSection)elementAt(i);
	    //System.err.println("MeshSection "+i+" vert: "+m.size());

	} else {
	    System.err.println("WARNING: degnerated MeshPart with only "+size()+" sections at"+
			       " tree position "+tree_position+".");
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
    
    public int vertex_cnt() {
	// count all meshpoints of all sections
	int cnt=0;
	
	for (int i = 0; i<size(); i++) {
	    cnt += ((MeshSection)elementAt(i)).size();
	}
	return cnt;
    }
    
    public int face_cnt()  {
	// calcs how much faces have to be created, povray wants to know this
	// before the faces itself
	int cnt = 0;
	for (int i=0; i<size()-1; i++) {
	    int c_i = ((MeshSection)elementAt(i)).size();
	    int c_i1 = ((MeshSection)elementAt(i+1)).size();
	    
	    if (c_i != c_i1) {
		cnt += Math.max(c_i,c_i1);
	    } else if (c_i > 1) {
		cnt += 2 * c_i;
	    }
	}
	return cnt;
    }
    
    public java.util.Vector faces(int inx, MeshSection section) throws ErrorMesh {
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

	
};












