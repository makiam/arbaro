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

public class StringParam extends Param {
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
