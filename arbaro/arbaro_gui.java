package net.sourceforge.arbaro;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;    
import javax.swing.event.*;

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
    File treefile = null;
    JFileChooser fileChooser;

    public static void main(String[] args) {
	arbaro_gui gui = new arbaro_gui();
    }

    public arbaro_gui() {
	// create tree with paramDB
	tree = new Tree();

	frame = new JFrame("Arbaro");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	fileChooser = new JFileChooser();
	fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")+"/trees"));
	
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
	JMenuItem item;

	JMenuBar menubar=new JMenuBar();
	JMenu menu = new JMenu("File");
	menu.setMnemonic('f');

	// File new
	item = new JMenuItem("New");
	item.setMnemonic('N');
	item.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    FileNew();
		}
	    });
	menu.add(item);

	// File open
	item = new JMenuItem("Open...");
	item.setMnemonic('O');
	item.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    FileOpen();
		}
	    });
	menu.add(item);

	// File save
	item = new JMenuItem("Save");
	item.setMnemonic('S');
	item.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if (treefile != null) {
			FileSave();
		    } else {
			FileSaveAs();
		    }
		}
	    });
	menu.add(item);

	// File save as
	item = new JMenuItem("Save as...");
	item.setMnemonic('A');
	item.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    FileSaveAs();
		}
	    });
	menu.add(item);
	
	menu.add(new JSeparator());

	// Save POV file
	item = new JMenuItem("Save POVray file...");
	item.setMnemonic('P');
	item.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    FileSavePOV();
		}
	    });
	menu.add(item);

	menu.add(new JSeparator());

	// Quit
	item = new JMenuItem("Quit");
	item.setMnemonic('Q');
	item.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    // FIXME: ask if values should be saved
		    System.exit(0);
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
	    panel.add(new ParamField(6,p));
	}

	JPanel panel1 = new JPanel();
	panel1.add(panel);
	parent.add(panel1);
	//panel1.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    void FileNew() {
	//FIXME ... ask if should save when modified...
	tree.clearParams();
    }

    void FileOpen() {
	int returnVal = fileChooser.showOpenDialog(frame);
	if(returnVal == JFileChooser.APPROVE_OPTION) {
	    System.err.println("opening file: " +
			       fileChooser.getSelectedFile().getName());
	    try {
		tree.clearParams();
		treefile = fileChooser.getSelectedFile();
		// read parameters
		tree.readFromXML(new FileInputStream(treefile));
	    } catch (ErrorParam err) {
		JOptionPane.showMessageDialog(frame,err.getMessage(),
					      "Parameter Error",
					      JOptionPane.ERROR_MESSAGE);
	    } catch (FileNotFoundException err) {
		JOptionPane.showMessageDialog(frame,err.getMessage(),
					      "File not found",
					      JOptionPane.ERROR_MESSAGE);
	    }
	}	
    }

    void FileSaveAs() {
	int returnVal = fileChooser.showSaveDialog(frame);
	if(returnVal == JFileChooser.APPROVE_OPTION) {
	    treefile = fileChooser.getSelectedFile();
	    FileSave();
	}
    }

    void FileSave() {
	System.err.println("saving to file: " +
			       fileChooser.getSelectedFile().getName());
	try {
	    PrintWriter out = new PrintWriter(new FileWriter(treefile));
	    tree.toXML(out);
	} catch (ErrorParam err) {
	    JOptionPane.showMessageDialog(frame,err.getMessage(),
					  "Parameter Error",
					  JOptionPane.ERROR_MESSAGE);
	} catch (FileNotFoundException err) {
	    JOptionPane.showMessageDialog(frame,err.getMessage(),
					  "File not found",
					      JOptionPane.ERROR_MESSAGE);
	}
	catch (IOException err) {
	    JOptionPane.showMessageDialog(frame,err.getMessage(),
					  "Output error",
					  JOptionPane.ERROR_MESSAGE);
	}
    }	


    void FileSavePOV() {
	JFileChooser chooser = new JFileChooser();
	chooser.setCurrentDirectory(new File(System.getProperty("user.dir")+"/pov"));

	int returnVal = chooser.showSaveDialog(frame);
	if(returnVal == JFileChooser.APPROVE_OPTION) {
	    try {
		File povFile = chooser.getSelectedFile();
		PrintWriter out = new PrintWriter(new FileWriter(povFile));  
		tree.make();
		tree.povray(out);
	    } catch (Exception err) {
		JOptionPane.showMessageDialog(frame,err.getMessage(),
					      "Output error",
					      JOptionPane.ERROR_MESSAGE);
	    }
	}
    }
}

class ParamField extends JTextField {

    AbstractParam param;

    public ParamField(int width, AbstractParam par) {
	super(width);

	param = par;
	//setText("0");
	//abhängig von Klasse: setText(par.getDefaultValue());
	setText(param.getDefaultValue());
	setToolTipText(param.getShortDesc());

	if (param.getClass() != StringParam.class) {
	    setHorizontalAlignment(JTextField.RIGHT);
	}
	addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    setParamValue();
		}
	    });
	addFocusListener(new FocusAdapter() {
		public void focusLost(FocusEvent e) {
		    setParamValue();
		}
	    });
	param.addChangeListener(new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
		    setText(param.getValue());
		}
	    });
				
    }

    void setParamValue() {
	try {
	    param.setValue(getText());
	    setText(param.getValue());
	} catch (ErrorParam err) {
	    JOptionPane.showMessageDialog(getParent(),err.getMessage(),"Parameter Error",
					  JOptionPane.ERROR_MESSAGE);
	    // set focus to this field
	    requestFocusInWindow();
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






