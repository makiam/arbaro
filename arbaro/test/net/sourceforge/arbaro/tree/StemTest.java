/*
 * Created on 12.11.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.sourceforge.arbaro.tree;

import junit.framework.TestCase;
import java.util.Enumeration;
import net.sourceforge.arbaro.transformation.Transformation;
import net.sourceforge.arbaro.tree.Stem;
import net.sourceforge.arbaro.tree.Tree;
import net.sourceforge.arbaro.params.*;


/**
 * @author wdiestel
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class StemTest extends TestCase {

	static Tree aTestTree = makeTestTree();
	static Stem aTestStem = makeTestStem();
	
	static Stem makeTestStem() {
		Stem stem=new Stem(aTestTree,null,0,new Transformation(),0);
		stem.make();
		return stem;
	}
	
	static Tree makeTestTree() {
		try {
			Tree tree = new Tree();
			tree.make();
			return tree;
		} catch (Exception e) {
			fail(e.getMessage());
			return null;
		}
	}
	
	public static void main(String[] args) {
		junit.swingui.TestRunner.run(StemTest.class);
	}

	public void testGetTransformation() {
	}

	public void testGetMinPoint() {
	}

	public void testGetMaxPoint() {
	}

	public void testAllStems() {
	}

	public void testAllLeaves() {
	}

	public void testStemSegments() {
	}

	public void testStemLeaves() {
	}

	public void testStem() {
		Tree tree = new Tree();
		Stem stem=new Stem(tree,null,0,new Transformation(),0);
		assertNotNull(stem);
	}

	public void testGetTreePosition() {
	}

	public void testMake() {
		Stem parent = aTestStem;
		Stem stem = new Stem(aTestTree,parent,1,
				new Transformation(),0);
		stem.make();
		
		// count segments
		int segCount=0;
		for (Enumeration segs = stem.stemSegments();
			segs.hasMoreElements();) {
			segCount++;
			segs.nextElement();
		}
		
		assertEquals(segCount,
				((IntParam)aTestTree.getParam("1CurveRes")).intValue());
	}

	public void testPruning() {
	}

	public void testGetStemLength() {
	}

	public void testMakeSegments() {
	}

	public void testIsInsideEnvelope() {
	}

	public void testGetNewDirection() {
	}

	public void testGetStemBaseRadius() {
	}

	public void testGetStemRadius() {
	}

	public void testPrepareSubstemParams() {
	}

	public void testLeavesPerBranch() {
	}

	public void testMakeSubstems() {
	}

	public void testGetSubstemDirection() {
	}

	public void testMakeLeaves() {
	}

	public void testMakeClones() {
	}

	public void testSplit() {
	}

	public void testAddToMesh() {
	}

	public void testSubstemTotal() {
	}

	public void testLeafCount() {
	}

	public void testMinMaxTest() {
	}

}
