//  #**************************************************************************
//  #
//  #    $file$  - Leaf class - creation, rotation and output of leafs
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
//  #**************************************************************************/

package net.sourceforge.arbaro.tree;

import java.lang.Math;

import net.sourceforge.arbaro.transformation.*;
import net.sourceforge.arbaro.params.*;

/**
 * A class for the leaves of the tree
 * 
 * @author Wolfram Diestel
 */
public class Leaf {
	
	public Transformation transf;
	Params par;
	//LevelParams lpar;
	
	//  const Tree &tree; // the tree
	//  const Stem &stem; // the parent stem
	//  const int level;  // which branch level
	double offset;    // how far from the parent's base
	double length;    // the length of the leaf (without leaf stem)
	double width;     // the width of the leaf
	
	public Leaf(Params params, Transformation trf, double offs) { /* offs = 0 */
		par = params;
		transf = trf;
		offset = offs;
		
		setLeafDimension();
		
		// FIXME: should stem radius be added?
		// print self.parent.stem_radius(self.offset)
		// self.direction.radius = self.parent.stem_radius(self.offset)+self.length/2
		
		setLeafOrientation();
	}
	
	/**
	 * Returns the angle of a 2-dimensional vector (u,v) with the u-axis 
	 *
	 * @param v v-coordinate of the vector
	 * @param u u-coordinate of the vector
	 * @return a value from (-180..180)
	 */
	private double atan2(double v, double u)  {
		if (u==0) {
			if (v>=0) return 90;
			else return -90;
		} 
		if (u>0)  return Math.atan(v/u)*180/Math.PI;
		if (v>=0) return 180 + Math.atan(v/u)*180/Math.PI;
		return Math.atan(v/u)*180/Math.PI-180;
	}
	
	/**
	 *	Sets the length and width of a leaf
	 */
	private void setLeafDimension() {
		length = par.LeafScale/Math.sqrt(par.LeafQuality);
		width = par.LeafScale*par.LeafScaleX/Math.sqrt(par.LeafQuality);
	}
	
	/**
	 *	Leaf rotation toward light
	 */
	private void setLeafOrientation() {
		if (par.LeafBend==0) return;
		
		// FIXME: make this function as fast as possible - a tree has a lot of leafs
		
		// rotation outside 
		Vector pos = transf.getT();
		Vector norm = transf.getY();
		
		double tpos = atan2(pos.getY(),pos.getX());
		double tbend = tpos - atan2(norm.getY(),norm.getX());
		if (tbend>180) tbend = 360-tbend;
		
		double bend_angle = par.LeafBend*tbend;
		transf = transf.rotz(bend_angle);
		
		// rotation up
		norm = transf.getY();
		double fbend = atan2(Math.sqrt(norm.getX()*norm.getX() + norm.getY()*norm.getY()),
				norm.getZ());
		
		double orientation = atan2(norm.getY(),norm.getX());
		bend_angle = par.LeafBend*fbend;
		
		// FIXME: maybe optimize this with a rotation doing all
		// three rotations at once
		transf = transf
			.rotz(-orientation)
			.rotx(bend_angle)
			.rotz(orientation);
	}
	
	/**
	 * Makes a string with a number of spaces
	 * 
	 * @param len the number of spaces
	 * @return a string of spaces
	 */
	private String whitespace(int len) {
		char[] ws = new char[len];
		for (int i=0; i<len; i++) {
			ws[i] = ' ';
		}
		return new String(ws);
	}
	
	
	/**
	 * Makes the leave. Does nothing at the moment, because
	 * all the values can be calculated in the constructor 
	 */
	void make() {
		// makes the leaf shape
		// add code here if necessary
	}
};












