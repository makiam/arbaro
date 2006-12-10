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

package net.sourceforge.arbaro.export;

import net.sourceforge.arbaro.meshfactory.*;
import net.sourceforge.arbaro.tree.Tree;
import net.sourceforge.arbaro.params.Params;




/**
 * @author wolfram
 *
 */
public class ExporterFactory {
	// Outputformats
	public final static int POV_MESH = 0;
	public final static int POV_CONES = 1;
	public final static int DXF = 2;
	public final static int OBJ = 3;
	//public final static int POV_SCENE = 4;

	
	int exportFormat = ExporterFactory.POV_MESH;
	String outputPath = System.getProperty("user.dir")
		+System.getProperty("file.separator")+"pov";
	
	// TODO move render parameters to a special RenderCmd class?
	int renderW = 400;
	int renderH = 600;

	boolean outputStemUVs = false;
	boolean outputLeafUVs = false;

	final static String[] formats = { "Povray meshes","Povray primitives","AutoCAD DXF","Wavefront OBJ" };

	
	/**
	 * Sets the output type for the Povray code 
	 * (primitives like cones, spheres and discs or
	 * triangle meshes)
	 * 
	 * @param output
	 */
	public void setExportFormat(int output) {
		exportFormat = output;
	}
	
	public int getExportFormat() {
		return exportFormat;
	}
	
	public String getOutputPath() {
		return outputPath;
	}
	
	public void setOutputPath(String p) {
		outputPath=p;
	}
	
	public void setRenderW(int w) {
		renderW = w;
	}
	
	public void setRenderH(int h) {
		renderH=h;
	}
	
	public int getRenderH() {
		return renderH;
	}
	
	public int getRenderW() {
		return renderW;
	}

	public void setOutputStemUVs(boolean oUV) {
		outputStemUVs = oUV;
	}
	
	public boolean getOutputStemUVs() {
		return outputStemUVs;
	}

	public void setOutputLeafUVs(boolean oUV) {
		outputLeafUVs = oUV;
	}

	public boolean getOutputLeafUVs() {
		return outputLeafUVs;
	}

	public Exporter createExporter(Tree tree, Params params) 
		throws InvalidExportFormatError {
		
		Exporter exporter = null;
		MeshFactory meshFactory;
		boolean useQuads = false;
		
		if (exportFormat == POV_CONES) {
			exporter = new POVConeExporter(tree,params);
		}
		else if (exportFormat == POV_MESH) {
			meshFactory = new MeshFactory(params,useQuads);
			exporter = new POVMeshExporter(tree,meshFactory);
			((POVMeshExporter)exporter).outputStemUVs = outputStemUVs;
			((POVMeshExporter)exporter).outputLeafUVs = outputLeafUVs;
		} else if (exportFormat == DXF) {
			meshFactory = new MeshFactory(params,useQuads);
			exporter = new DXFExporter(tree,meshFactory);
		} else if (exportFormat == OBJ) {
			useQuads = true;
			meshFactory = new MeshFactory(params,useQuads);
			exporter = new OBJExporter(tree,meshFactory);
			((OBJExporter)exporter).outputStemUVs = outputStemUVs;
			((OBJExporter)exporter).outputLeafUVs = outputLeafUVs;
		} else {
			throw new InvalidExportFormatError("Invalid export format");
		}

		return exporter;
	}

	public Exporter createSceneExporter(Tree tree, Params params) { 
		return new POVSceneExporter(tree,params,renderW,renderH);
	}

	public static String[] getExportFormats() {
		return formats;
	}

		
}