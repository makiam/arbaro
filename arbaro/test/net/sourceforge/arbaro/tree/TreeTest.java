/*
 * Created on 12.11.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.sourceforge.arbaro.tree;

import java.io.File;
import java.io.FileInputStream;
import java.util.Enumeration;

import junit.framework.TestCase;
import net.sourceforge.arbaro.tree.Tree;
import net.sourceforge.arbaro.params.*;

/**
 * @author wdiestel
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TreeTest extends TestCase {

	public static void main(String[] args) {
		junit.swingui.TestRunner.run(TreeTest.class);
	}
	
	static Tree aTestTree = makeTestTree();
	
	static private Tree makeTestTree() {
		try {
			Tree tree = new Tree();
			tree.setParam("Leaves","3");
			tree.make();
			return tree;
		} catch (Exception e) {
			fail(e.getMessage());
			return null;
		}
	}

	public void testGetMaxPoint() {
	}

	public void testGetMinPoint() {
	}

	public void testGetHeight() {
	}

	public void testGetWidth() {
	}

	/*
	 * Class under test for void Tree()
	 */
	public void testTree() {
		Tree tree = new Tree();
		assertNotNull(tree);
	}

	/*
	 * Class under test for void Tree(Tree)
	 */
	public void testTreeTree() throws Exception {
		Tree tree=makeTestTree();
		tree.setParam("Leaves","5");
		Tree copy=new Tree(tree);
		assertEquals(((IntParam)tree.getParam("Leaves")).intValue(),
					((IntParam)copy.getParam("Leaves")).intValue());
	}

	public void testClear() throws Exception {
		Tree tree=makeTestTree();
		
		assertTrue(tree.getStemCount()>0);
		tree.clear();
		assertFalse(tree.getStemCount()>0);
	}

	public void testMake() throws Exception {
		Tree tree=makeTestTree();
		assertTrue(tree.getStemCount()>0);
	}

	public void testTrunkDirection() {
	}
	
	public void testTraverseTree() throws Exception{
		// test some Treetraversals here
		Tree tree=makeTestTree();
		
		// test LeafCounter
		LeafCounter leafCounter = new LeafCounter();
		assertTrue(leafCounter.getLeafCount()==0);
		tree.traverseTree(leafCounter);
		assertTrue(leafCounter.getLeafCount()>0);
		assertEquals(tree.getLeafCount(),leafCounter.getLeafCount());

		// test StemCounter
		StemCounter stemCounter = new StemCounter();
		assertTrue(stemCounter.getStemCount()==0);
		tree.traverseTree(stemCounter);
		assertTrue(stemCounter.getStemCount()>0);
		assertEquals(tree.getStemCount(),stemCounter.getStemCount());
	}

	public void testOutput() {
	}

	public void testCreateStemMesh() {
	}

	public void testCreateLeafMesh() {
	}

	public void testMinMaxTest() {
	}

	public void testOutputScene() {
	}

	public void testGetParamGroup() {
	}

	public void testClearParams() {
	}

	public void testReadFromXML() throws Exception {
		Tree tree = new Tree();
		
		File dir = new File(System.getProperty("user.dir")+"/trees");
		
		FileInputStream is = new FileInputStream(dir+"/black_tupelo.xml");
		tree.readFromXML(is);
		assertEquals(tree.getParam("Species").toString(),"black_tupelo");
		assertEquals(tree.getParam("3Branches").toString(),"12");

		is = new FileInputStream(dir+"/sassafras.xml");
		tree.readFromXML(is);
		assertEquals(tree.getParam("Species").toString(),"sassafras");
		assertEquals(((FloatParam)tree.getParam("3CurveV")).doubleValue(),200.0,0.0001);
	
	}

	public void testToXML() {
	}

	public void testSetSeed() {
		Tree tree=new Tree();
		tree.setSeed(1024);
		assertEquals(tree.getSeed(),1024);
	}

	public void testGetLeafCount() {
	}

	public void testSetParam() throws Exception {
		Tree tree=new Tree();
		tree.setParam("LeafBend","0.3");
		assertEquals(tree.getParam("LeafBend").toString(),"0.3");
	}

	public void testSetOutputType() {
		Tree tree=new Tree();
		tree.setOutputType(Tree.OBJ);
		assertEquals(tree.getOutputType(),Tree.OBJ);
	}

	public void testGetOutputType() {
	}

	public void testGetOutputTypes() {
		assertTrue(Tree.getOutputTypes().length>0);
	}

	public void testSetOutputPath() {
		Tree tree=new Tree();
		tree.setOutputPath("xyz");
		assertEquals(tree.getOutputPath(),"xyz");
	}

	public void testGetOutputPath() {
	}

	public void testSetRenderW() {
		Tree tree=new Tree();
		tree.setRenderW(1022);
		assertEquals(tree.getRenderW(),1022);
	}

	public void testGetRenderW() {
	}

	public void testSetRenderH() {
		Tree tree=new Tree();
		tree.setRenderH(999);
		assertEquals(tree.getRenderH(),999);
	}

	public void testGetRenderH() {
	}

	public void testNewProgress() {
		Tree tree = makeTestTree();
		assertTrue(tree.getProgress().getPercent()>0);
		tree.newProgress();
		assertEquals(tree.getProgress().getPercent(),0);
	}

	public void testGetProgress() {
	}

	public void testSetupGenProgress() {
	}

	public void testGetStemCount() {
	}

	public void testUpdateGenProgress() {
	}

}
