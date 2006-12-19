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
import java.util.TreeMap;

import net.sourceforge.arbaro.export.Progress;
import net.sourceforge.arbaro.params.AbstractParam;
import net.sourceforge.arbaro.params.Params;
import net.sourceforge.arbaro.export.Console;

/**
 * @author wolfram
 *
 */
public class ShieldedTreeGenerator implements TreeGenerator {
	TreeGenerator treeGenerator;
	
	/**
	 * 
	 */
	public ShieldedTreeGenerator(TreeGenerator treeGenerator) {
		this.treeGenerator = treeGenerator;
	}
	
	protected void showException(Exception e) {
		Console.printException(e);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.arbaro.tree.TreeGenerator#clearParams()
	 */
	public void clearParams() {
		try {
			treeGenerator.clearParams();
		} catch (Exception e) {
			showException(e);
		}
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.arbaro.tree.TreeGenerator#getParam(java.lang.String)
	 */
	public AbstractParam getParam(String param) {
		try {
			return treeGenerator.getParam(param);
		} catch (Exception e) {
			showException(e);
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.arbaro.tree.TreeGenerator#getParamGroup(int, java.lang.String)
	 */
	public TreeMap getParamGroup(int level, String group) {
		try {
			return treeGenerator.getParamGroup(level,group);
		} catch (Exception e) {
			showException(e);
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.arbaro.tree.TreeGenerator#getParams()
	 */
	public Params getParams() {
		try {
			return treeGenerator.getParams();
		} catch (Exception e) {
			showException(e);
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.arbaro.tree.TreeGenerator#getSeed()
	 */
	public int getSeed() {
		try {
			return treeGenerator.getSeed();
		} catch (Exception e) {
			showException(e);
			return 13;
		}
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.arbaro.tree.TreeGenerator#makeTree(net.sourceforge.arbaro.export.Progress)
	 */
	public Tree makeTree(Progress progress) {
		try {
			return treeGenerator.makeTree(progress);
		} catch (Exception e) {
			showException(e);
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.arbaro.tree.TreeGenerator#readParamsFromCfg(java.io.InputStream)
	 */
	public void readParamsFromCfg(InputStream is) {
		try {
			treeGenerator.readParamsFromCfg(is);
		} catch (Exception e) {
			showException(e);
		}
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.arbaro.tree.TreeGenerator#readParamsFromXML(java.io.InputStream)
	 */
	public void readParamsFromXML(InputStream is) {
		try {
			treeGenerator.readParamsFromXML(is);
		} catch (Exception e) {
			showException(e);
		}
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.arbaro.tree.TreeGenerator#setParam(java.lang.String, java.lang.String)
	 */
	public void setParam(String param, String value) {
		try {
			treeGenerator.setParam(param,value);
		} catch (Exception e) {
			showException(e);
		}
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.arbaro.tree.TreeGenerator#setSeed(int)
	 */
	public void setSeed(int seed) {
		try {
			treeGenerator.setSeed(seed);
		} catch (Exception e) {
			showException(e);
		}
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.arbaro.tree.TreeGenerator#writeParamsToXML(java.io.PrintWriter)
	 */
	public void writeParamsToXML(PrintWriter out) {
		try {
			treeGenerator.writeParamsToXML(out);
		} catch (Exception e) {
			showException(e);
		}
	}
}
