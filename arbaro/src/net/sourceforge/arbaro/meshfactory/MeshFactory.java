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

package net.sourceforge.arbaro.meshfactory;

import java.io.PrintWriter;

import net.sourceforge.arbaro.mesh.LeafMesh;
import net.sourceforge.arbaro.mesh.Mesh;
import net.sourceforge.arbaro.params.*;
import net.sourceforge.arbaro.params.AbstractParam;
import net.sourceforge.arbaro.tree.Tree;
import net.sourceforge.arbaro.export.Progress;

/**
 * @author wolfram
 *
 */
public class MeshFactory {
	Params params;
	boolean useQuads;
	
	public MeshFactory(Params params, boolean useQuads) {
		super();
		this.params = params;
		this.useQuads = useQuads;
	}
	
	public AbstractParam getParam(String name) {
		return params.getParam(name);
	}
	
	public void ParamsToXML(PrintWriter w) throws ParamException {
		params.toXML(w);
	}
	
	public Mesh createStemMesh(Tree tree, Progress progress) {
		progress.beginPhase("Creating mesh",tree.getStemCount());
		
		if (progress.consoleChar != ' ') {
			System.err.println("Output: mesh");
			for (int l=0; l<Math.min(params.Levels,4); l++) {
				System.err.println("  Level " + l + ": vertices/section: " 
						+ params.getLevelParams(l).mesh_points + ", smooth: " 
						+ (params.smooth_mesh_level>=l? "yes" : "no"));
			}
		}
		
		
		Mesh mesh = new Mesh(((IntParam)params.getParam("Levels")).intValue());
/*		for (int t=0; t<trunks.size(); t++) {
			((Stem)trunks.elementAt(t)).addToMesh(mesh,true,useQuads);
		}
		getProgress().incProgress(trunks.size());
		*/
		MeshCreator meshCreator = new MeshCreator(params, mesh, -1, useQuads, progress);
		tree.traverseTree(meshCreator);
		
		progress.endPhase();
		return mesh;
	}

	// FIXME move to MeshFactory
	public Mesh createStemMeshByLevel(Tree tree, Progress progress) throws Exception {
		progress.beginPhase("Creating mesh",tree.getStemCount());
		
		if (progress.consoleChar != ' ') {
			System.err.println("Output: mesh");
			for (int l=0; l<Math.min(params.Levels,4); l++) {
				System.err.println("  Level " + l + ": vertices/section: " 
						+ params.getLevelParams(l).mesh_points + ", smooth: " 
						+ (params.smooth_mesh_level>=l? "yes" : "no"));
			}
		}

		Mesh mesh = new Mesh(params.Levels);
		
		/*
		for (int level=0; level < params.Levels; level++) {
			Enumeration stems = allStems(level);
			while (stems.hasMoreElements()) {
				((Stem)stems.nextElement()).addToMesh(mesh,false,useQuads);
				getProgress().incProgress(1);
			}
		}
		*/
		for (int level=0; level < params.Levels; level++) {
			MeshCreator meshCreator = new MeshCreator(params, mesh, level, useQuads, progress);
			tree.traverseTree(meshCreator);
		}
			
		progress.endPhase();
		return mesh;
	}
	
	// FIXME move to MeshFactory
	public LeafMesh createLeafMesh() {
		double leafLength = params.LeafScale/Math.sqrt(params.LeafQuality);
		double leafWidth = params.LeafScale*params.LeafScaleX/Math.sqrt(params.LeafQuality);
		return new LeafMesh(params.LeafShape,leafLength,leafWidth,params.LeafStemLen,useQuads);
	}
	
}
