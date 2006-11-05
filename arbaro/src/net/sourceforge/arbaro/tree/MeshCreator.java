package net.sourceforge.arbaro.tree;

import net.sourceforge.arbaro.export.Progress;
import net.sourceforge.arbaro.mesh.*;

public class MeshCreator implements TreeTraversal {
	Mesh mesh;
	Tree tree;
	Progress progress;
	int level; // only stems of this level should be created
	boolean useQuads;
	
	//private Stack meshparts;

	public MeshCreator(Mesh mesh, int level, boolean useQuads, Progress progress) {
		super();
		this.mesh=mesh;
		this.level=level;
		this.useQuads = useQuads;
		this.progress = progress;
	}
	
	public boolean enterSegment(Segment segment) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean enterStem(Stem stem) throws TraversalException {
		// TODO instead of addToMesh, the traversal should
		// proceed into segments and subsegments itself
		// removing all mesh creation code from Stem, Segment, 
		// Subsegment

		if (level >= 0 && stem.stemlevel < level) {
			return true; // look further for stems
			
		} else if (level >= 0 && stem.stemlevel > level) {
			return false; // go back to higher level
			
		} else { 
			try {
				//stem.addToMesh(mesh,false,useQuads);
				MeshPartCreator traversal = new MeshPartCreator(useQuads);
				if (stem.traverseStem(traversal)) {
					mesh.addMeshpart(traversal.getMeshPart());
				}
				// show progress
				if (tree.params.verbose) {
					if (stem.stemlevel<=1 && stem.cloneIndex.size()==0) System.err.print(".");
				}
				progress.incProgress(1);
				return true; // proceed
				
			} catch(Exception e) {
				throw new TraversalException(e.toString());
			}
		}	
	}
	
	public boolean enterTree(Tree tree) {
		this.tree = tree; 
		return true;
	}

	public boolean leaveStem(Stem stem) {
		return true;
	}

	public boolean leaveTree(Tree tree) {
		return true; // Mesh created successfully
	}

	public boolean visitLeaf(Leaf leaf) {
		// TODO Auto-generated method stub
		return false;
	}
}
