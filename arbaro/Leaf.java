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

package net.sourceforge.arbaro;

import java.io.PrintWriter;
import java.lang.Math;

import net.sourceforge.arbaro.transformation.*;
import net.sourceforge.arbaro.params.*;

class Leaf {
    // A class for the leaves of the tree
 
    Transformation transf;
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

	// FIXME: do this in Tree, not here - it takes time
	// leaf scaling should be in leaf declaration not
	// for every one leaf
	leaf_dimension();
	  
	// FIXME: should stem radius be added?
	// print self.parent.stem_radius(self.offset)
	// self.direction.radius = self.parent.stem_radius(self.offset)+self.length/2
	
	leaf_orientation();
    }

    private double atan2(double v, double u)  {
	// returns the angle of a 2-dimensional vector (u,v) with the u-axis 
	// returns a value from (-180..180)
	if (u==0) {
	    if (v>=0) return 90;
	    else return -90;
	} else if (u>0) {
	    return Math.atan(v/u)*180/Math.PI;
	} else if (v>=0) {
	    return 180 + Math.atan(v/u)*180/Math.PI;
	} else {
	    return Math.atan(v/u)*180/Math.PI-180;
	}
    }
      
    private void leaf_dimension() {
	// set the length and width of a leaf
	length = par.LeafScale/Math.sqrt(par.LeafQuality);
	width = par.LeafScale*par.LeafScaleX/Math.sqrt(par.LeafQuality);
    }

    private void leaf_orientation() {
	// leaf rotation toward light
	if (par.LeafBend==0) return;
	  
	// FIXME: make this function as fast as possible - a tree has a lot of leafs
	  
	// rotation outside 
	Vector pos = transf.getT();
	Vector norm = transf.getY();
	  
	double tpos = atan2(pos.getY(),pos.getX());
	double tbend = tpos - atan2(norm.getY(),norm.getX());
	if (tbend>180) tbend = 360-tbend;

	// parent.TRF("leaf_orient before",self.transf)
	
	double bend_angle = par.LeafBend*tbend;
	transf = transf.rotz(bend_angle);

	// self.parent.TRF("leaf_orient after rot outside",self.transf)

	// rotation up
	norm = transf.getY();
	double fbend = atan2(Math.sqrt(norm.getX()*norm.getX() + norm.getY()*norm.getY()),
			     norm.getZ());
	  
	double orientation = atan2(norm.getY(),norm.getX());
	bend_angle = par.LeafBend*fbend;

	//self.parent.DBG("leaf_orient: fbend: %.1f orient: %.1f x2y2: %.5f\n" % \
	    //	  	(fbend,orientation,sqrt(norm.x()**2+norm.y()**2)))
	    
	    // FIXME: maybe optimize this with a rotation doing all
	    // three rotations at once
	    /*
	      transf = transf.rotz(-orientation);
	      transf = transf.rotx(bend_angle);
	      transf = transf.rotz(orientation);
	    */
	    transf = transf.rotz(-orientation).rotx(bend_angle).rotz(orientation);
  
	    // self.parent.TRF("leaf_orient after rot up",self.transf)
    }
	
    private String whitespace(int len) {
	char[] ws = new char[len];
	for (int i=0; i<len; i++) {
	    ws[i] = ' ';
	}
	return new String(ws);
    }

    public void povray(PrintWriter w) {
	// prints povray code for the leaf
	String indent = whitespace(4);
	  
	//mid = self.position+self.direction # what about connecting stem len?
	// FIXME: move "scale..." to tree
	w.println(indent + "object { " + par.species + "_" + par.Seed 
		  + "_leaf " + "scale <" + width + "," + length + "," + width + "> "
		  + transf.povray()+"}");
    }

    void make() {
	// makes the leaf shape
	// FIXME: add code here if necessary
    }

  /*
      def dump(self):
		indent = " "*(self.parent.level*2+4)
		print indent+"LEAF:"
		print indent+"position: ",self.position
		print indent+"direction: ",self.direction
		print indent+"offset: ",self.offset
		print indent+"normal: ",self.normal
  */	   
};












