/**
 * 
 */
package net.sourceforge.arbaro.export;

import java.io.PrintWriter;

import net.sourceforge.arbaro.mesh.Face;
import net.sourceforge.arbaro.mesh.LeafMesh;
import net.sourceforge.arbaro.tree.Leaf;

/**
 * @author wolfram
 *
 */
public class OBJLeafFaceExporter extends OBJLeafExporter {
	boolean firstLeaf;
	long faceProgressCount=0;
	
	long smoothingGroup;
	int uvVertexOffset;
	boolean outputLeafUVs=true;
	boolean outputStemUVs=true;

//	instead of Arbaro's normals use smoothing to interpolate normals
//  this should be give the same result	
	boolean outputNormals = false;

	/**
	 * @param pw
	 * @param leafMesh
	 * @param leafVertexOffset
	 */
	public OBJLeafFaceExporter(PrintWriter pw, LeafMesh leafMesh,
			int leafVertexOffset, int uvVertexOffset,
			long smoothingGroup,	
			boolean outputLeafUVs, boolean outputStemUVs) {
		super(pw, leafMesh, leafVertexOffset);
		
		firstLeaf = true;
		this.smoothingGroup = smoothingGroup;
		this.uvVertexOffset = uvVertexOffset;
		this.outputLeafUVs = outputLeafUVs;
		this.outputStemUVs = outputStemUVs;
	}
	
	
	public boolean visitLeaf(Leaf l) {
		if (firstLeaf) {
			
			w.println("g leaves");
			w.println("usemtl leaves");
	//		uvVertexOffset++;
			
			firstLeaf = false;
		}
	
		w.println("s "+smoothingGroup++);
		for (int i=0; i<leafMesh.getShapeFaceCount(); i++) {
			Face face = leafMesh.shapeFaceAt(i);
			writeFace(
					face,leafVertexOffset,
					face,uvVertexOffset,
					outputLeafUVs,outputNormals);
		}
		
		// increment face offset
		leafVertexOffset += leafMesh.getShapeVertexCount();
		
		incFaceProgressCount();
		
		return true;
	}

	private void incFaceProgressCount() {
		if (faceProgressCount++ % 500 == 0) {
			progress.incProgress(500);
			if (tree.params.verbose) System.err.print(".");
		}
	}
	
	private void writeFace(Face f, long offset, Face uv, long uvOffset, boolean writeUVs, boolean writeNormals) {
		w.print("f "); 
				
		for (int i=0; i<f.points.length; i++) {
			w.print(offset+f.points[i]);
			if (writeUVs || writeNormals) {
				w.print("/");
				if (writeUVs) w.print(uvOffset+uv.points[i]);
				if (writeNormals) w.print("/"+offset+f.points[i]);
			}
			if (i<f.points.length-1) w.print(" ");
			else w.println();
		}
	}
}
