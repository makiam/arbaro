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

public abstract class Param {
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
