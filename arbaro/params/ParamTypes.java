//  #**************************************************************************
//  #
//  #    $Id$  
//  #            - Param classes for the several types of params (int,double,string)
//  #          
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

abstract class Param {
    String name;
    String short_desc;
    String long_desc;

    public Param(String nam, String sh, String lng) {
	name = nam;
	short_desc = sh;
	long_desc = lng;
    }

    public abstract void setValue(String val) throws ErrorParam;

    protected void warn(String warning) {
	System.err.println("WARNING: "+warning);
    }
}

class IntParam extends Param {
    private int min;
    private int max;
    private int deflt;
    private int value;

    IntParam(String nam,int mn, int mx, int def, String sh, String lng) {
	super(nam,sh,lng);
	min = mn;
	max = mx;
	deflt = def;
	value = Integer.MIN_VALUE;
    }

    public void setValue(String val) throws ErrorParam {
	Integer i = new Integer(val);
	
	if (i.intValue()<min) {
	    throw new ErrorParam("Value of "+name+" should be greater or equal to "+min);
	}

	if (i.intValue()>max) {
	    throw new ErrorParam("Value of "+name+" should be greater or equal to "+max);
	}
	
	value = i.intValue();
    }

    public int getValue() {
	if (value==Integer.MIN_VALUE) {
	    warn(name+" not given, using default value("+deflt+")");
	    // set value to default, t.e. don't warn again
	    value=deflt;
	}
	return value;
    }
}

class FloatParam extends Param {
    private double min;
    private double max;
    private double deflt;
    private double value;

    FloatParam(String nam, double mn, double mx, double def, String sh, String lng) {
	super(nam,sh,lng);
	min = mn;
	max = mx;
	deflt = def;
	value = Double.NaN;
    }

    public void setValue(String val) throws ErrorParam {
	Double d = new Double(val);
	if (d.doubleValue()<min) {
	    throw new ErrorParam("Value of "+name+" should be greater then or equal to "+min);
	}
	if (d.doubleValue()>max) {
	    throw new ErrorParam("Value of "+name+" should be less then or equal to "+max);
	}
	value = d.doubleValue();
    }

    public double getValue() {
	if (Double.isNaN(value)) {
	    warn(name+" not given, using default value("+deflt+")");
	    // set value to default, t.e. don't warn again
	    value=deflt;
	}
	return value;
    }
};

class StringParam extends Param {
    private String deflt;
    private  String value;

    StringParam(String nam, String def, String sh, String lng) {
	super(nam,sh,lng);
	deflt = def;
	value = "";
    }

    public void setValue(String val) {
	value = val;
    }

    public String getValue() {
	if (value.equals("")) {
	    warn(name+" not given, using default value("+deflt+")");
	    // set value to default, t.e. don't warn again
	    value=deflt;
	}
	return value;
    }
}
