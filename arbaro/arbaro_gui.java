package net.sourceforge.arbaro;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;    
//import javax.swing.event.*;

import java.util.TreeMap;
import java.util.Iterator;
import net.sourceforge.arbaro.tree.Tree;
import net.sourceforge.arbaro.params.*;    

public class arbaro_gui {
    // static Tree tree;
    JFrame frame;
    JPanel mainPanel;
    JTabbedPane tabs;
    Tree tree;

    public static void main(String[] args) {
	arbaro_gui gui = new arbaro_gui();
    }

    public arbaro_gui() {
	// create tree with paramDB
	tree = new Tree();

	frame = new JFrame("Arbaro");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	createGUI();
	frame.setVisible(true);
    }

    void createGUI() {
	createMenuBar();

	tabs = new JTabbedPane();

	JPanel panel1 = new JPanel();
	//panel1.setLayout(new FlowLayout());
	panel1.setLayout(new GridLayout(2,3));
	tabs.addTab("General", null, panel1, "General parameters");
	tabs.setSelectedIndex(0);

	addParamGroupWidget("SHAPE","Tree shape",AbstractParam.GENERAL,panel1);
	addParamGroupWidget("TRUNK","Trunk radius",AbstractParam.GENERAL,panel1);
	addParamGroupWidget("LEAVES","Leaves",AbstractParam.GENERAL,panel1);
	addParamGroupWidget("LEAVESADD","Additional leaf parameters",AbstractParam.GENERAL,panel1);
	addParamGroupWidget("PRUNING","Pruning",AbstractParam.GENERAL,panel1);
	addParamGroupWidget("MISC","Miscellaneous parameters",AbstractParam.GENERAL,panel1);

	for (int i=0; i<4; i++) {
	    JPanel panel = new JPanel();
	    panel.setLayout(new GridLayout(2,3));
	    tabs.addTab("Level "+i, null, panel, "Parameters for branching level "+i);
	
	    addParamGroupWidget("LENTAPER","Length and tapering",i,panel);
	    addParamGroupWidget("CURVATURE","Curvature",i,panel);
	    addParamGroupWidget("SPLITTING","Splitting",i,panel);
	    addParamGroupWidget("BRANCHING","Branching",i,panel);
	    addParamGroupWidget("ADDBRANCH","Branch distribution",i,panel);
	}

	// add more
	frame.getContentPane().add(tabs);

        frame.pack();
    }

    void createMenuBar() {

	JMenuBar menubar=new JMenuBar();
	JMenu menu = new JMenu("File");
	menu.setMnemonic('f');
	JMenuItem item = new JMenuItem("Open");
	item.setMnemonic('O');

	item.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    JFileChooser chooser = new JFileChooser();
	
		    int returnVal = chooser.showOpenDialog(frame);
		    if(returnVal == JFileChooser.APPROVE_OPTION) {
			System.out.println("You chose to open this file: " +
					   chooser.getSelectedFile().getName());
		    }
		}
	    });


	menu.add(item);
	
	menubar.add(menu);
	frame.setJMenuBar(menubar);
    }

    void addParamGroupWidget(String group, String groupName, int level, JPanel parent) {
	TreeMap params = new TreeMap(tree.getParamGroup(level,group));
	
	JPanel panel = new JPanel();
	panel.setLayout(new GridLayout(params.size(),2));
	panel.setBorder(BorderFactory.createCompoundBorder(
                      BorderFactory.createTitledBorder(groupName),
                      BorderFactory.createEmptyBorder(5,10,5,10)));

	for (Iterator e=params.values().iterator();e.hasNext();) {
	    AbstractParam p = (AbstractParam)e.next();
	    panel.add(new JLabel(p.getName()));
	    panel.add(new ParamField(12,p));
	}

	JPanel panel1 = new JPanel();
	panel1.add(panel);
	parent.add(panel1);
	//panel.setAlignmentY(Component.TOP_ALIGNMENT);
    }
}

class ParamField extends JTextField {

    AbstractParam param;

    public ParamField(int width, AbstractParam par) {
	super(width);

	param = par;
	//setText("0");
	//abhängig von Klasse: setText(par.getDefaultValue());
	if (param.getClass() == StringParam.class) {
	    setText(((StringParam)param).getDefaultValue());
	} else if (param.getClass() == IntParam.class) {
	    Integer i = new Integer(((IntParam)param).getDefaultValue());
	    setText(i.toString());
	    setHorizontalAlignment(JTextField.RIGHT);
	} else if (param.getClass() == FloatParam.class) {
	    Double d = new Double(((FloatParam)param).getDefaultValue());
	    setText(d.toString());
	    setHorizontalAlignment(JTextField.RIGHT);
	}
    }

    /*
  protected Document createDefaultModel() {
              return new UpperCaseDocument();
     }
 
     static class UpperCaseDocument extends PlainDocument {
 
         public void insertString(int offs, String str, AttributeSet a) 
                  throws BadLocationException {
 
                  if (str == null) {
                      return;
                  }
                  char[] upper = str.toCharArray();
                  for (int i = 0; i < upper.length; i++) {
                      upper[i] = Character.toUpperCase(upper[i]);
                  }
                  super.insertString(offs, new String(upper), a);
              }
     }
     }*/
}






