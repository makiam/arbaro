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


/**
 * 
 * A class for creation, handling and output of a piece of a mesh object.
 * A mesh part represents a stem of the tree.
 * 
 * @author wdiestel
 *
 */
public class MeshPart extends java.util.Vector {
	public String tree_position;
	boolean useNormals;
	
	public MeshPart(String treepos, boolean normals) { 
		tree_position = treepos;
		// FIXME normals not yet used,
		// other mesh output format needed if theire are
		// less normals then vertices
		useNormals = normals;
	}
	
	/**
	 * Adds a mesh section to the mesh part.
	 * 
	 * @param section
	 */
	public void addSection(MeshSection section) {
		//if (stem.tree.verbose) cerr << (".");
		
		if (size() > 0) {
			// connect section with last of sections
			((MeshSection)lastElement()).next = section;
			section.previos = (MeshSection)lastElement();
		}
		
		addElement(section);
	}
	
	/**
	 * Sets the normals in all mesh sections
	 * 
	 */
	public void setNormals() {
		// normals for begin and end point (first and last section)
		// are set to a z-Vector by Segment.mesh()
		//System.err.println("MESHSECT 1: "+((MeshSection)elementAt(1)).size());
		
		if (size()>1) {
			((MeshSection)elementAt(1)).setNormalsUp();
			for (int i=2; i<size()-1; i++) {
				((MeshSection)elementAt(i)).setNormalsUpDown();
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
	
	
	
	/**
	 * Returns the number of all meshpoints of all sections
	 * 
	 * @return
	 */
	public int vertexCount() {
		int cnt=0;
		
		for (int i = 0; i<size(); i++) {
			cnt += ((MeshSection)elementAt(i)).size();
		}
		return cnt;
	}
	
	/**
	 * Returns the number of faces, that have to be created - povray wants 
	 * to know this before the faces itself
	 *
	 * @return
	 */
	public int faceCount()  {
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
	
	/**
	 * Returns the triangles between a section and the next
	 * section.
	 * 
	 * @param inx
	 * @param section
	 * @return
	 * @throws ErrorMesh
	 */
	public java.util.Vector faces(int inx, MeshSection section) throws ErrorMesh {
		MeshSection next = section.next;
		java.util.Vector faces = new java.util.Vector();
		
		if (section.size() ==1 && next.size() == 1) {
			// normaly this shouldn't occur, only for very small radius?
			// I should warn about this
			System.err.println("WARNING: two adjacent mesh sections with only one point.");
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












