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

package net.sourceforge.arbaro.mesh;

import java.text.NumberFormat;

import net.sourceforge.arbaro.params.FloatFormat;
import net.sourceforge.arbaro.transformation.Vector;

/**
 * A class holding a section of a mesh.
 * 
 * This is a number of vertices in one layer. 
 * Several layers build the mesh part for a stem.
 * 
 * @author wdiestel
 */
public class MeshSection extends java.util.Vector {
	
	public MeshSection previos;
	public MeshSection next;
	/*Stem stem;*/
	
	public MeshSection(/*Stem st,*/ int ptcnt) {
		super(ptcnt);
		/*stem = st;*/
	}
	
	/**
	 * Adds a point to the mesh section
	 * 
	 * @param pt
	 */
	public void addPoint(Vector pt) {
		addElement(new Vertex(pt,null));
	} 
	
	/**
	 * Returns the location point of the vertex i.
	 * 
	 * @param i
	 * @return
	 */
	public Vector pointAt(int i) {
		return ((Vertex)elementAt(i)).point;
	}
	
	/**
	 * Returns the normal of the vertex i.
	 * 
	 * @param i
	 * @return
	 * @throws Exception
	 */
	public Vector normalAt(int i) throws Exception {
		Vertex v = (Vertex)elementAt(i);
		if (v.normal == null) throw new ErrorMesh("Error: Normal not set for point "
				+vectorStr(v.point));
		return v.normal;
	}
	
	private String vectorStr(Vector v) {
		NumberFormat fmt = FloatFormat.getInstance();
		return "<"+fmt.format(v.getX())+","
		+fmt.format(v.getZ())+","
		+fmt.format(v.getY())+">";
	}

	
	/**
	 * Returns the point number i.
	 * 
	 * @param i
	 * @return
	 */
	public Vector here(int i) {
		
		return ((Vertex)elementAt(i)).point;
	}
	
	/**
	 * Returns the point to the left of the point number i
	 * 
	 * @param i
	 * @return
	 */
	public Vector left(int i) {
		return ((Vertex)elementAt((i-1+size())%size())).point;
	}
	
	/**
	 * Returns the point to the right of the point number i
	 * 
	 * @param i
	 * @return
	 */
	public Vector right(int i) {
		return ((Vertex)elementAt((i+1)%size())).point;
	}
	
	/**
	 * Returns the point on top of the point number i (from the next section).
	 * The next section has the same number of points or only one point.
	 * @param i
	 * @return
	 */
	public Vector up(int i) {
		return ((Vertex)(next.elementAt(i%next.size()))).point;
	}
	
	/**
	 * Returns the point below the point number i (from the previous section).
	 * The next section has the same number of points or only one point.
	 * @param i
	 * @return
	 */
	public Vector down(int i) {
		return ((Vertex)(previos.elementAt(i%previos.size()))).point;
	}	  	  
	
	/**
	 * Returns the normal of the plane built by the vectors a-b and c-b
	 * 
	 * @param a  
	 * @param b 
	 * @param c
	 * @return 
	 */
	public Vector normal(Vector a, Vector b, Vector c) {
		Vector u = (a.sub(b)).normalize();
		Vector v = (c.sub(b)).normalize();
		Vector norm = new Vector(u.getY()*v.getZ() - u.getZ()*v.getY(),
				u.getZ()*v.getX() - u.getX()*v.getZ(),
				u.getX()*v.getY() - u.getY()*v.getX()).normalize();
		if (Double.isNaN(norm.getX()) && Double.isNaN(norm.getY()) 
				&& Double.isNaN(norm.getZ())) {
			System.err.println("WARNING: invalid normal vector - stem radius too small?");
			norm = new Vector(0,0,1);
		}
		return norm;
	}
	
	/**
	 * Sets all normals to the vector vec
	 *
	 * @param vec
	 */
	public void setNormalsToVector(Vector vec) {
		for (int i=0; i<size(); i++) {
			((Vertex)elementAt(i)).normal=vec;
		}
	}
	
	/**
	 * Sets all normals to the average
	 * of the two left and right upper triangles
	 * 
	 */
	public void setNormalsUp() {
		for (int i=0; i<size(); i++) {
			((Vertex)elementAt(i)).normal = 
				(normal(up(i),here(i),left(i)).add(
						normal(right(i),here(i),up(i)))).normalize();
		}
	}
	
	/**
	 * Sets all normals to the average
	 * of the two left and right lower triangles
	 * 
	 */
	public void setNormalsDown() {
		for (int i=0; i<size(); i++) {
			((Vertex)elementAt(i)).normal = 
				(normal(down(i),here(i),right(i)).add(
						normal(left(i),here(i),down(i)))).normalize();
		}
	}
	
	/**
	 * Sets all normals to the average
	 * of the four left and right upper and lower triangles
	 */
	public void setNormalsUpDown() {
		for (int i=0; i<size(); i++) {
			((Vertex)elementAt(i)).normal = 
				(normal(up(i),here(i),left(i)).add(
						normal(right(i),here(i),up(i))).add(
								normal(down(i),here(i),right(i))).add(
										normal(left(i),here(i),down(i)))).normalize();
		}
	}
	
};








