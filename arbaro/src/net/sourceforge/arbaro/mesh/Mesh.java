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
 */

// FIXME: a vector can hold only MAX_INT elements,
// this may exeeded for big trees like weeping willow:
// 25*10*300 = 75.000 - really there are less stems,
// because only the longest have the maximum number
// of substems.
public class Mesh extends java.util.Vector {
	final boolean debugMesh = false;
	public int[] firstMeshPart; // first mesh part of each level 
		
	public Mesh(int levels) { 
		firstMeshPart = new int[levels];
		for (int i=0; i<levels; i++) {
			firstMeshPart[i]=-1;
		}
	}
	
	/**
	 * Adds a mesh part (i.e. a stem) to the mesh.
	 * 
	 * @param meshpart
	 */
	public void addMeshpart(MeshPart meshpart) {
		addElement(meshpart);
		if (firstMeshPart[meshpart.stem.stemlevel]<0) 
			firstMeshPart[meshpart.stem.stemlevel] = size()-1;
	}
	
	/**
	 * Returns the total number of vertices in the mesh.
	 * 
	 * @return
	 */
	public int vertexCount() {
		// count all meshpoints of all parts
		int cnt=0;
		
		for (int i = 0; i<size(); i++) {
			cnt += ((MeshPart)elementAt(i)).vertexCount();
		}
		return cnt;
	}
	
	/**
	 * Returns the total number of faces, that has to be
	 * created for the mesh. Povray wants to know this
	 * before the faces itself.
	 * 
	 * @return
	 */
	public int faceCount()  {
		int cnt = 0;
		for (int i=0; i<size(); i++) {
			cnt += ((MeshPart)elementAt(i)).faceCount();
		}
		return cnt;
	}

	/**
	 * Returns the total number of uv-vectors, that has to be
	 * created for the mesh. Povray wants to know this
	 * before the uv vectors itself.
	 * 
	 * @return
	 */
	public int uvCount()  {
		int cnt = 0;
		
		for (int i=0; i<firstMeshPart.length; i++) {
			cnt += ((MeshPart)elementAt(firstMeshPart[i])).uvCount();
		}
		return cnt;
	}
	
	/**
	 * Returns the index of the first uv-vector, of the given level
	 * 
	 * @return
	 */
	public int firstUVIndex(int level) {
		int cnt = 0;
		
		for (int i=0; i<level ; i++) {
			cnt += ((MeshPart)elementAt(firstMeshPart[i])).uvCount();
		}
		return cnt;
	}

};    












