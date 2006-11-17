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

import net.sourceforge.arbaro.export.Progress;
import net.sourceforge.arbaro.transformation.Transformation;

/**
 * @author wolfram
 *
 */
public class GeneratorBase {

	public boolean debug;
	public boolean verbose;
	protected Progress progress;

	public GeneratorBase(GeneratorBase other) {
		this.verbose = other.verbose;
		this.debug = other.debug;
		this.progress = other.progress;
	}
	
	public GeneratorBase(Progress progress, boolean verbose, boolean debug) {
		this.verbose = verbose;
		this.debug = debug;
		this.progress = progress;
	}

	/**
	 * Output debug string, when debugging ist enabled
	 * 
	 * @param dbgstr The output string
	 */
	public void DBG(StemImpl stem, String dbgstr) {
		// print debug string to stderr if debugging is enabled
		if (debug) System.err.println(stem.getTreePosition() + ":" + dbgstr);
	}

	/**
	 * For debugging:
	 * Prints out the transformation to stderr nicely 
	 * (only if debugging is enabled)
	 * 
	 * @param where The position in the tree, i.e. wich stem
	 *              has this transformation
	 * @param trf  The transformation
	 */
	protected void TRF(StemImpl stem, String where, Transformation trf) {
		DBG(stem,where + ": " + trf.toString());
	}

}
