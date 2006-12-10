//  #**************************************************************************
//  #
//  #    Copyright (C) 2003-2006  Wolfram Diestel
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

package net.sourceforge.arbaro.tree;

import java.io.InputStream;
import java.io.PrintWriter;

import net.sourceforge.arbaro.export.Progress;
import net.sourceforge.arbaro.params.AbstractParam;
import net.sourceforge.arbaro.params.ParamException;
import net.sourceforge.arbaro.params.Params;

/**
 * @author wolfram
 *
 */
public class TreeGenerator {
	Params params;

	public Tree makeTree(Progress progress) throws Exception {
		TreeImpl tree = new TreeImpl(seed, params);
		tree.make(progress);
		
		return tree;
	}
	
	private int seed = 13;

	public TreeGenerator() {
		params = new Params();
	}
	
	public TreeGenerator(Params params) {
		this.params = params;
	}

	public void setSeed(int seed) {
		this.seed = seed;
	}
	
	public int getSeed() {
		return seed;
	}
	
	public Params getParams() {
		return params;
	}
	
	
	public void setParam(String param, String value) throws ParamException {
		params.setParam(param,value);
	}
	
	public AbstractParam getParam(String param) {
		return params.getParam(param);
	}
	
	/**
	 * Returns a parameter group
	 * 
	 * @param level The branch level (0..3)
	 * @param group The parameter group name
	 * @return A hash table with the parameters
	 */
	public java.util.TreeMap getParamGroup(int level, String group) {
		return params.getParamGroup(level,group);
	}

	/**
	 * Writes out the parameters to an XML definition file
	 * 
	 * @param out The output stream
	 * @throws ParamException
	 */
	public void writeParamsToXML(PrintWriter out) throws ParamException {
		params.toXML(out);
	}
	
	/**
	 * Clear all parameter values of the tree.
	 */
	public void clearParams() {
		params.clearParams();
	}
	
	/**
	 * Read parameter values from an XML definition file
	 * 
	 * @param is The input XML stream
	 * @throws ParamException
	 */
	public void readParamsFromXML(InputStream is) throws ParamException {
		params.readFromXML(is);
	}
	
	/**
	 * Read parameter values from an Config style definition file
	 * 
	 * @param is The input text stream
	 * @throws ParamException
	 */
	public void readParamsFromCfg(InputStream is) throws Exception {
		params.readFromCfg(is);
	}

}
