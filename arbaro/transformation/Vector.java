// #**************************************************************************
// #
// #    $Id$  - Transformation class 
// #                        for rotating and translating point in space
// #
// #    Copyright (C) 2003  Wolfram Diestel
// #
// #    This program is free software; you can redistribute it and/or modify
// #    it under the terms of the GNU General Public License as published by
// #    the Free Software Foundation; either version 2 of the License, or
// #    (at your option) any later version.
// #
// #    This program is distributed in the hope that it will be useful,
// #    but WITHOUT ANY WARRANTY; without even the implied warranty of
// #    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// #    GNU General Public License for more details.
// #
// #    You should have received a copy of the GNU General Public License
// #    along with this program; if not, write to the Free Software
// #    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
// #
// #    Send comments and bug fixes to diestel@steloj.de
// #
// #**************************************************************************/


package transformation;

import java.lang.String;
import java.lang.Math;
import params.FloatFormat;
import java.text.NumberFormat;

public class Vector {
    final int X=0;
    final int Y=1;
    final int Z=2;


    private double[] coord = {0,0,0};
    
    public Vector() {
	coord = new double[Z+1];
	//coord = {0,0,0};
    }

    public Vector(double x, double y, double z) {
	coord = new double[Z+1];
	coord[X] = x;
	coord[Y] = y;
	coord[Z] = z;
    }
    
    public double abs() {
	//returns the length of the vector
	return Math.sqrt(coord[X]*coord[X] + coord[Y]*coord[Y] + coord[Z]*coord[Z]);
    }
    
    public String povray() {
	NumberFormat fmt = FloatFormat.getInstance();
	return "<"+fmt.format(coord[X])+","
	    +fmt.format(coord[Z])+","
	    +fmt.format(coord[Y])+">";
    }

    public String toString() {
	NumberFormat fmt = FloatFormat.getInstance();
	return "<"+fmt.format(coord[X])+","
	    +fmt.format(coord[Y])+","
	    +fmt.format(coord[Z])+">";
    }	

    public Vector normalize() {
	double abs = this.abs();
	return new Vector(coord[X]/abs,coord[Y]/abs,coord[Z]/abs);
    }

    public double getX() {
	return coord[X];
    }

    public double getY() {
	return coord[Y];
    }

    public double getZ() {
	return coord[Z];
    }

    public Vector mul(double factor) {
	// scales the vector
	return new Vector(coord[X]*factor,coord[Y]*factor,coord[Z]*factor);
    } 

    public double prod(Vector v) {
	// inner product of two vectors
	return coord[X]*v.getX() + coord[Y]*v.getY() + coord[Z]*v.getZ();
    }

    public Vector div (double factor)  {
	return this.mul(1/factor);
    }
				
    public Vector add(Vector v) {
	return new Vector(coord[X]+v.getX(), coord[Y]+v.getY(), coord[Z]+v.getZ());
    }
		
    public Vector sub(Vector v) {
	return this.add(v.mul(-1));
    }

};
		







