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
import transformation.Vector;

public class Matrix {
    // A matrix class
    final int X=0;
    final int Y=1;
    final int Z=2;

    private double[] data;
	
    public Matrix() {
	data = new double[(Z+1)*(Z+1)];
	for (int r=X; r<=Z; r++) {
	    for (int c=X; c<=Z; c++) {
		data[r*3+c] = c==r? 1:0;
	    }
	}
    }

    public Matrix(double xx, double xy, double xz,
		  double yx, double yy, double yz,
		  double zx, double zy, double zz) {
	data = new double[(Z+1)*(Z+1)];

	data[X*3+X] = xx;
	data[X*3+Y] = xy;
	data[X*3+Z] = xz;
	data[Y*3+X] = yx;
	data[Y*3+Y] = yy;
	data[Y*3+Z] = yz;
	data[Z*3+X] = zx;
	data[Z*3+Y] = zy;
	data[Z*3+Z] = zz;
    }

    public String toString() {
	return "x: "+row(X)+" y: "+row(Y)+" z: "+row(Z);
    }

    public Vector row(int r) {
	return new Vector(data[r*3+X],data[r*3+Y],data[r*3+Z]);
    }
	
    public Vector col(int c) {
	return new Vector(data[X*3+c],data[Y*3+c],data[Z*3+c]);
    }
		
    public double get(int r, int c) {
	return data[r*3+c];
    }
		
    public void set(int r, int c, double value)  {
	data[r*3+c] = value;
    }

    public Matrix transpose() {
	Matrix T = new Matrix();
	for (int r=X; r<=Z; r++) {
	    for (int c=X; c<=Z; c++) {
		T.set(r,c,data[c*3+r]);
	    }
	}
	return T;
    }

    public Matrix mul(double factor) {
	// scales the matrix with a factor
	Matrix R = new Matrix();
  
	for (int r=X; r<=Z; r++) {
	    for (int c=X; c<=Z; c++) {
		R.set(r,c,data[r*3+c]*factor);
	    }
	}
	return R;
    }
							
    public Matrix prod(Matrix M) {
	//returns the matrix product
	Matrix R = new Matrix();
  
	for (int r=X; r<=Z; r++) {
	    for (int c=X; c<=Z; c++) {
		R.set(r,c,row(r).prod(M.col(c)));
	    }
	}
  
	return R;
    }
	
    public Matrix add(Matrix M) {
	// returns the sum of the two matrices
	Matrix R = new Matrix();
  
	for (int r=X; r<=Z; r++) {
	    for (int c=X; c<=Z; c++) {
		R.set(r,c,data[r*3+c]+M.get(r,c));
	    }
	}
	return R;
    }
		
    public Vector prod(Vector v) {
	// returns the product of the matrix with a vector
	return new Vector(row(X).prod(v),row(Y).prod(v),row(Z).prod(v));
    }

    public Matrix div(double factor) {
	return mul(-1);
    }

    public Matrix sub(Matrix M) {
	return add(M.mul(-1));
    }

};  // class Matrix

/*
ostream& operator << (ostream& o, const Vector &v) {
  o << "<" << v(X) << "," << v(Y) << "," << v(Z) << ">";
  return o;
}

ostream& operator << (ostream& o, const Matrix &M) {
  o << "<" << M.col(X) << M.col(Y) << M.col(Z) << ">";
  return o;
}
*/
		


































