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
    
    public int vertex_cnt() {
	// count all meshpoints of all parts
	int cnt=0;
	
	for (int i = 0; i<size(); i++) {
	    cnt += ((MeshPart)elementAt(i)).vertex_cnt();
	}
	return cnt;
    }
    
    public int face_cnt()  {
	// calcs how much faces have to be created, povray wants to know this
	// before the faces itself
	int cnt = 0;
	for (int i=0; i<size(); i++) {
	    cnt += ((MeshPart)elementAt(i)).face_cnt();
	}
	return cnt;
    }
    
};    












