//#**************************************************************************
//#
//#    $Id$  
//#      - Output class for writing Povray scene with the tree
//#          
//#
//#    Copyright (C) 2004  Wolfram Diestel
//#
//#    This program is free software; you can redistribute it and/or modify
//#    it under the terms of the GNU General Public License as published by
//#    the Free Software Foundation; either version 2 of the License, or
//#    (at your option) any later version.
//#
//#    This program is distributed in the hope that it will be useful,
//#    but WITHOUT ANY WARRANTY; without even the implied warranty of
//#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//#    GNU General Public License for more details.
//#
//#    You should have received a copy of the GNU General Public License
//#    along with this program; if not, write to the Free Software
//#    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//#
//#    Send comments and bug fixes to diestel@steloj.de
//#
//#**************************************************************************/

package net.sourceforge.arbaro.output;

import java.io.PrintWriter;

import net.sourceforge.arbaro.tree.Tree;

/**
 * Class that creates a Povray scene file with the rendered tree
 * included.
 * 
 * @author wolfram
 */
public class PovSceneOutput extends Output {

	/**
	 * @param aTree
	 * @param pw
	 */
	public PovSceneOutput(Tree aTree, PrintWriter pw) {
		super(aTree, pw);
	}

    /**
     * Returns a prefix for the Povray objects names,
     * it consists of the species name and the random seed
     * 
     * @return the prefix string
     */
    private String povrayDeclarationPrefix() {
    	return tree.getSpecies() + "_" + tree.params.Seed + "_";
    }

    public void write() throws ErrorOutput {
		w.println("// render as 600x400");

		w.println("#include \"" + tree.getSpecies() + ".inc\"");
		w.println("background {rgb <0.95,0.95,0.9>}");

		w.println("light_source { <5000,5000,-3000>, rgb 1.2 }");
		w.println("light_source { <-5000,2000,3000>, rgb 0.5 shadowless }");

		w.println("#declare HEIGHT = " + povrayDeclarationPrefix() + "scale * 1.3;");
		w.println("#declare WIDTH = 2*HEIGHT/3;");

		w.println("camera { orthographic location <0, HEIGHT*0.45, -100>");
		w.println("         right <WIDTH, 0, 0> up <0, HEIGHT, 0>");
		w.println("         look_at <0, HEIGHT*0.45, -80> }");

		w.println("union { ");
		w.println("         object { " + povrayDeclarationPrefix() + "stems");
		w.println("                pigment {color rgb 0.9} }"); 
		w.println("         object { " + povrayDeclarationPrefix() + "leaves");
		w.println("                texture { pigment {color rgb 1} ");
		w.println("                          finish { ambient 0.15 diffuse 0.8 }}}");
		w.println("         rotate 90*y }");

		if (tree.params.Leaves > 0) {
		    w.println("         object { " + povrayDeclarationPrefix() + "stems");
		    w.println("                scale 0.7 rotate 45*y");  
		    w.println("                translate <WIDTH*0.33,HEIGHT*0.33,WIDTH>");
		    w.println("                pigment {color rgb 0.9} }"); 
		}
		w.flush();
    }


}
