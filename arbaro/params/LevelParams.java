//  #**************************************************************************
//  #
//  #    $Id$  
//  #         LevelParams class - it holds the tree parameters for the levels
//  #         Random class - has methods getstate, setstate
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

package net.sourceforge.arbaro.params;

import java.io.PrintWriter;

class Random extends java.util.Random {

    public Random(long seed) {
	super(seed);
    }

    public double uniform(double low, double high) {
	return low+nextDouble()*(high-low);
    }

    public long getstate() {
	// the original random generator doesn't provide an interface
	// to read, and reset it's state, so this is a hack here, to make
	// this possible. The random generator is reseeded here with a seed
	// got from the generator, this seed are returned as state.
	long state = nextLong();
	setSeed(state);
	return state;
    }

    public void setstate(long state) {
	setSeed(state);
    }
};

public class LevelParams {
    // parameters for the 4 levels

    public int level;

    // stem length and appearance
    public double nTaper; // taper to a point (cone)
    public int nCurveRes;
    public double nCurve;
    public double nCurveV;
    public double nCurveBack;
    public double nLength;
    public double nLengthV;

    // splitting
    public double nSegSplits;
    public double nSplitAngle;
    public double nSplitAngleV;

    // substems
    public int nBranches; // FIXME: level0 not used, but why not use it
                                // for bushes? 
    public double nBranchDist; // SubstemDistance within segment 
                // 0: all substems at segment base
                // 1: distributed over full segment
    public double nBranchDistV; // Distance variation 
              // 0: evenly distribution of substems over segment
              // 1: distances variates between 0..nSubStemDist*seglen/substems

    public double nDownAngle;
    public double nDownAngleV;
    public double nRotate;
    public double nRotateV;

    public int mesh_points; // how many meshpoints per cross-section

    // Error values for splitting, substem and leaf distribution
    public double splitErrorValue;
    public double substemErrorValue;
    public double leavesErrorValue;

    // random genrators
    public Random random; 

    // param DB
    java.util.Hashtable paramDB;

    public LevelParams(int l, java.util.Hashtable parDB) {
	level = l;
	paramDB = parDB;
    }

    public long initRandom(long seed) {
	random = new Random(seed);
	return random.nextLong();
    }

    public double var(double variation) {
	// return a random variation value from (-variation,+variation)
	return random.uniform(-variation,variation);
    }

    // help methods for output of params
    private void xml_param(PrintWriter w, String name, int value) {
	name = "" + level + name.substring(1);
	w.println("    <param name='" + name + "'  value='"+value+"'/>");
    }
    
    private void xml_param(PrintWriter w, String name, double value) {
	name = "" + level + name.substring(1);
	w.println("    <param name='" + name + "'  value='"+value+"'/>");
    }

    void toXML(PrintWriter w) {
	w.println("    <!-- level " + level  + " -->");
	xml_param(w,"nTaper",nTaper);
	xml_param(w,"nCurveRes",nCurveRes);
	xml_param(w,"nCurve",nCurve);
	xml_param(w,"nCurveV",nCurveV);
	xml_param(w,"nCurveBack",nCurveBack);
	xml_param(w,"nLength",nLength);
	xml_param(w,"nLengthV",nLengthV);
	xml_param(w,"nSegSplits",nSegSplits);
	xml_param(w,"nSplitAngle",nSplitAngle);
	xml_param(w,"nSplitAngleV",nSplitAngleV);
	xml_param(w,"nBranches",nBranches);
	xml_param(w,"nBranchDist",nBranchDist);
	xml_param(w,"nBranchDistV",nBranchDistV);
	xml_param(w,"nDownAngle",nDownAngle);
	xml_param(w,"nDownAngleV",nDownAngleV);
	xml_param(w,"nRotate",nRotate);
	xml_param(w,"nRotateV",nRotateV);
    }

    // help method for loading params
    private int int_param(String name) throws ErrorParam {
	name = "" + level + name.substring(1);
	IntParam par = (IntParam)paramDB.get(name);
	if (par != null) {
	    return par.getValue();
	} else {
	    throw new ErrorParam("bug: param "+name+" not found!");
	}   
    }

    private double dbl_param(String name) throws ErrorParam {
	name = "" + level + name.substring(1);
	FloatParam par = (FloatParam)paramDB.get(name);
	if (par != null) {
	    return par.getValue();
	} else {
	    throw new ErrorParam("bug: param "+name+" not found!");
	}   
    }
    
    void fromDB() throws ErrorParam {
	nTaper = dbl_param("nTaper");
	nCurveRes = int_param("nCurveRes");
	nCurve = dbl_param("nCurve");
	nCurveV = dbl_param("nCurveV");
	nCurveBack = dbl_param("nCurveBack");
	nLength = dbl_param("nLength");
	nLengthV = dbl_param("nLengthV");
	nSegSplits = dbl_param("nSegSplits");
	nSplitAngle = dbl_param("nSplitAngle");
	nSplitAngleV = dbl_param("nSplitAngleV");
	nBranches = int_param("nBranches");
	nBranchDist = dbl_param("nBranchDist");
	nBranchDistV = dbl_param("nBranchDistV");
	nDownAngle = dbl_param("nDownAngle");
	nDownAngleV = dbl_param("nDownAngleV");
	nRotate = dbl_param("nRotate");
	nRotateV = dbl_param("nRotateV");
    }
};

























