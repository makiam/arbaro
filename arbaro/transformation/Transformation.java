// #**************************************************************************
// #
// #    $Id$  
// #                   - Transformation class  for rotating and translating point in space
// #                   - Matrix class for the rotation part
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


package net.sourceforge.arbaro.transformation;

import java.lang.String;
import java.lang.Math;

import java.io.PrintWriter;

import java.text.NumberFormat;
import net.sourceforge.arbaro.params.FloatFormat;

class Matrix {
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
		




































public class Transformation {
    // A transformation class - a matrix for rotations and a vector for translations
    final int X=0;
    final int Y=1;
    final int Z=2;
    final int T=3;
	
    Matrix matrix;
    Vector vector;

    public Transformation() {
	matrix = new Matrix();
	vector = new Vector();
    }

    public Transformation(Matrix m, Vector v) {
	matrix = m;
	vector = v;
    }

    Matrix matrix() {
	return matrix;
    }

    Vector vector() {
	return vector;
    }

    public Transformation prod(Transformation T) {
	// the product of two transformations, .i.e. the tranformation
	// resulting of the two transformations aplied one after the other
	return new Transformation(matrix.prod(T.matrix()),
			      matrix.prod(T.vector()).add(vector));
    }
	
    public Vector apply(Vector v) {
	// aplies transformation to vector v
	return matrix.prod(v).add(vector);
    }

    public Vector getX() {
	return matrix.col(X);
    }

    public Vector getY() {
	return matrix.col(Y);
    }

    public Vector getZ() {
	return matrix.col(Z);
    }

    public Vector getT() { // for convenince same as vector()
	return vector;
    }

    /*	
ostream& operator << (ostream &os, const Transformation &trf) {
  os << "(X:" << trf[X] << "; Y:" << trf[Y] << "; Z:" << trf[Z] 
     << "; T:" << trf[T] << ")";
  return os;
}
    */



	

    
    public String toString() {
	return "x: "+getX()+", y: "+getY()+", z: "+getZ()+", t: "+getT();
    }
    
	
    public String povray() {
	NumberFormat fmt = FloatFormat.getInstance();
	return "matrix <" + fmt.format(matrix.get(X,X)) + "," 
	    + fmt.format(matrix.get(X,Z)) + "," 
	    + fmt.format(matrix.get(X,Y)) + ","
	    + fmt.format(matrix.get(Z,X)) + "," 
	    + fmt.format(matrix.get(Z,Z)) + "," 
	    + fmt.format(matrix.get(Z,Y)) + ","
	    + fmt.format(matrix.get(Y,X)) + "," 
	    + fmt.format(matrix.get(Y,Z)) + "," 
	    + fmt.format(matrix.get(Y,Y)) + ","
	    + fmt.format(vector.getX())   + "," 
	    + fmt.format(vector.getZ())   + "," 
	    + fmt.format(vector.getY()) + ">";
    }

    public Transformation rotz(double angle) {
	// local rotation about z-axis
	angle = angle*Math.PI/180;
	Matrix rm = new Matrix(Math.cos(angle),-Math.sin(angle),0,
			   Math.sin(angle),Math.cos(angle),0,
			   0,0,1);
	return new Transformation(matrix.prod(rm),vector);
    }
		
    public Transformation roty(double angle) {
	// local rotation about z-axis
	angle = angle*Math.PI/180;
	Matrix rm = new Matrix(Math.cos(angle),0,-Math.sin(angle),
			   0,1,0,
			   Math.sin(angle),0,Math.cos(angle));
	return new Transformation(matrix.prod(rm),vector);
    }

    public Transformation rotx(double angle) {
	// local rotation about the x axis
	angle = angle*Math.PI/180;
	Matrix rm = new Matrix(1,0,0,
			   0,Math.cos(angle),-Math.sin(angle),
			   0,Math.sin(angle),Math.cos(angle));
	return new Transformation(matrix.prod(rm),vector);
    }

    public Transformation rotxz(double delta, double rho) {
	// local rotation about the x and z axees - for the substems
	delta = delta*Math.PI/180;
	rho = rho*Math.PI/180;
	double sir = Math.sin(rho);
	double cor = Math.cos(rho);
	double sid = Math.sin(delta);
	double cod = Math.cos(delta);
  
	Matrix rm = new Matrix(cor,-sir*cod,sir*sid,
				 sir,cor*cod,-cor*sid,
				 0,sid,cod);
	return new Transformation(matrix.prod(rm),vector);
    }

    public Transformation rotaxisz(double delta, double rho) {
	// local rotation away from the local z-axis 
	// about an angle delta using an axis given by rho 
	// - used for splitting and random rotations
	delta = delta*Math.PI/180;
	rho = rho*Math.PI/180;
  
	double a = Math.cos(rho);
	double b = Math.sin(rho);
	double si = Math.sin(delta);
	double co = Math.cos(delta);
  
	Matrix rm = new Matrix((co+a*a*(1-co)),(b*a*(1-co)),(b*si),
			   (a*b*(1-co)),(co+b*b*(1-co)),(-a*si),
			   (-b*si),(a*si),(co));
	return new Transformation(matrix.prod(rm),vector);
    }
    
    public Transformation translate(Vector v) {
	return new Transformation(matrix,vector.add(v));
    }
    
    public Transformation rotaxis(double angle,Vector axis) {
	// rotation about an axis
	angle = angle*Math.PI/180;
	axis=axis.normalize();
	double a = axis.getX();
	double b = axis.getY();
	double c = axis.getZ();
	double si = Math.sin(angle);
	double co = Math.cos(angle);
  
	Matrix rm = new Matrix(
				 (co+a*a*(1-co)),(-c*si+b*a*(1-co)),(b*si+c*a*(1-co)),
				 (c*si+a*b*(1-co)),(co+b*b*(1-co)),(-a*si+c*b*(1-co)),
				 (-b*si+a*c*(1-co)),(a*si+b*c*(1-co)),(co+c*c*(1-co)));
	return new Transformation(rm.prod(matrix),vector);
    }

    public Transformation inverse() {
	// get inverse transformation M+t -> M'-M'*t"
	Matrix T = matrix.transpose();
	return new Transformation(T,T.prod(vector.mul(-1)));
    }
};
		



































