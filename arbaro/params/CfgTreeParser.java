//  #**************************************************************************
//  #
//  #    $Id$  - class for reading a tree species definition from a file
//  #             with key=value lines
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

package params;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.File;
import java.io.FileReader;

public class CfgTreeParser {
  
    public void parse(String fileName, Params params) throws Exception {
	File inputFile = new File(fileName);
	LineNumberReader r = 
	    new LineNumberReader(new FileReader(inputFile));
	parse(r,params);
    }

    public void parse(InputStream is, Params params) throws Exception {
	LineNumberReader r = new LineNumberReader(new InputStreamReader(is));
	parse(r,params);
    }
    
    public void parse(LineNumberReader r, Params params) throws Exception {
	String line = r.readLine();
	String param;
	String value;
	while (line != null) {
	    if (line != "" && line.charAt(0) != '#') {
		int equ = line.indexOf('=');
		param = line.substring(0,equ).trim();
		value = line.substring(equ+1).trim();
		if (param.equals("species")) {
		    params.species = value;
		} else {
		    params.setParam(param,value);
		}
		line = r.readLine();
	    }
	}
    }
}






