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
import java.io.PrintWriter;
import transformation.Vector;
import transformation.Matrix;
import params.FloatFormat;
import java.text.NumberFormat;

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



	

/*
string Transformation::str() const {
  return "x: %s, y: %s, z: %s, t: %s\n" % \
			(str(self.x()),str(self.y()),\
			str(self.z()),str(self.vector))
  */
    
	
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
	Matrix rm = new Matrix(Math.cos(angle),-Math.sin(angle),0.0,
			   Math.sin(angle),Math.cos(angle),0.0,
			   0.0,0.0,1.0);
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
		



































