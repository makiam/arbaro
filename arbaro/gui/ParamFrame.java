//  #**************************************************************************
//  #
//  #    $Id$  - the Main window of the GUI
//  #
//  #    Copyright (C) 2003  Wolfram Diestel
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

package net.sourceforge.arbaro.gui;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;    
import javax.swing.event.*;
import javax.swing.border.*;

import java.util.TreeMap;
import java.util.Iterator;
import net.sourceforge.arbaro.tree.Tree;
import net.sourceforge.arbaro.params.*;    

public class ParamFrame {
    // static Tree tree;
    JFrame frame;
    JPanel mainPanel;
    JTabbedPane tabs;
    Statusbar statusbar;
    
    Tree tree;
    File treefile = null;
    JFileChooser fileChooser;
    SpeciesField species;
    Component lastFocused = null;
    boolean modified;
    Config config;
    
    // images
    final static ImageIcon shapeIcon = createImageIcon("images/shape.png","Tree shape");
    final static ImageIcon radiusIcon = createImageIcon("images/radius.png","Trunk radius");
    final static ImageIcon leavesIcon = createImageIcon("images/leaves.png","Leaves");
    final static ImageIcon pruneIcon = createImageIcon("images/pruning.png","Pruning/Envelope");
    final static ImageIcon miscIcon = createImageIcon("images/misc.png","Miscellaneous parameters");
    final static ImageIcon lentapIcon = createImageIcon("images/len_tapr.png","Length and taper");
    final static ImageIcon curveIcon = createImageIcon("images/curve.png","Curvature");
    final static ImageIcon splitIcon = createImageIcon("images/splitting.png","Splitting");
    final static ImageIcon substemIcon = createImageIcon("images/substem.png","Branching");

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path,
    		String description) {
    	java.net.URL imgURL = ParamFrame.class.getResource(path);
    	if (imgURL != null) {
    		return new ImageIcon(imgURL, description);
    	} else {
    		System.err.println("Couldn't find file: " + path);
    		return null;
    	}
    }
    
    
    public ParamFrame() {
    	// create tree with paramDB
    	tree = new Tree();
    	
    	frame = new JFrame("Arbaro");
    	frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    	frame.addWindowListener(new WindowAdapter() {
    		public void windowClosing(WindowEvent e) {
    			if (! shouldSave()) return;
    			//System.err.println("closing the frame...");
    			//frame.dispose();
    			System.exit(0);
    		}
    	});
    	// set Icon
    	java.net.URL imgURL = ParamFrame.class.getResource("images/arbaro32.png");
    	if (imgURL != null) {
    		Image icon = Toolkit.getDefaultToolkit().getImage(imgURL);
    		frame.setIconImage(icon);
    	}
    	
    	fileChooser = new JFileChooser();
    	fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")+"/trees"));
    	
    	config = new Config();
    	
    	AbstractParam.loading=true;
    	createGUI();
    	frame.setVisible(true);
    	setModified(false);
    	AbstractParam.loading=false;
    }

    void createGUI() {
    	createMenuBar();

//    	Container contentPane = frame.getContentPane();
//    	contentPane.setLayout(new BorderLayout());

    	// create Tabset
    	tabs = new JTabbedPane();
    	
    	// add tab for general params
    	JPanel panel1 = new JPanel();
    	panel1.setLayout(new GridLayout(2,3,20,20));
    	panel1.setBorder(BorderFactory.createEmptyBorder(10,30,30,30));
    	
    	// image area
    	JLabel imagelabel = new JLabel("", shapeIcon, JLabel.CENTER);
    	imagelabel.setBorder(BorderFactory.createTitledBorder(
    			BorderFactory.createEmptyBorder(5,10,5,10),"Tree shape",
				TitledBorder.CENTER,TitledBorder.TOP));
    	imagelabel.setOpaque(true);
    	imagelabel.setForeground(Color.WHITE);
    	
    	// param groups
    	panel1.add(new ParamGroup(this,"SHAPE","Tree shape",
    			AbstractParam.GENERAL,imagelabel,shapeIcon));
    	panel1.add(new ParamGroup(this,"TRUNK","Trunk radius",
    			AbstractParam.GENERAL,imagelabel,radiusIcon));
    	JPanel leavespanel = new JPanel();
    	leavespanel.setLayout(new GridLayout(2,1));
    	leavespanel.add(new ParamGroup(this,"LEAVES","Leaves",
    			AbstractParam.GENERAL,imagelabel,leavesIcon));
    	leavespanel.add(new ParamGroup(this,"LEAVESADD","Additional leaf parameters",
    			AbstractParam.GENERAL,imagelabel,leavesIcon));
    	panel1.add(leavespanel);
    	panel1.add(new ParamGroup(this,"PRUNING","Pruning/Envelope",
    			AbstractParam.GENERAL,imagelabel,pruneIcon));
    	panel1.add(new ParamGroup(this,"MISC","Miscellaneous parameters",
    			AbstractParam.GENERAL,imagelabel,miscIcon));
    	
    	panel1.add(imagelabel);
    	
    	// Species input
    	JPanel speciesPanel = new JPanel();
    	speciesPanel.setBorder(BorderFactory.createEmptyBorder(20,30,10,10));
    	speciesPanel.setLayout(new BoxLayout(speciesPanel,BoxLayout.X_AXIS));
    	speciesPanel.add(new JLabel("Species:"));
    	speciesPanel.add(Box.createRigidArea(new Dimension(5,0)));
    	species = new SpeciesField(20,tree);
    	//species.setBorder(BorderFactory.createEmptyBorder(0,10,0,0));
    	Dimension dim = species.getPreferredSize();
    	species.setMaximumSize(new Dimension((int)dim.getWidth()+5,(int)dim.getHeight()+5));
    	speciesPanel.add(species);
    	
    	JPanel panel2 = new JPanel();
    	panel2.setLayout(new BorderLayout());
    	
    	panel2.add(speciesPanel,BorderLayout.NORTH);
    	panel2.add(panel1,BorderLayout.CENTER);
    	
    	tabs.addTab("General", null, panel2, "General parameters");
    	tabs.setSelectedIndex(0);
    	
    	// add tabs for level params
    	JLabel imagelabel2;
    	JLabel imagelabel3;
    	for (int i=0; i<4; i++) {
    		JPanel panel = new JPanel();
    		panel.setLayout(new GridLayout(2,3,20,20));
    		panel.setBorder(BorderFactory.createEmptyBorder(30,30,30,30));
    		
    		tabs.addTab("Level "+i, null, panel, "Parameters for branching level "+i);
    		
    		// image area 1
    		imagelabel = new JLabel("", lentapIcon, JLabel.CENTER);
    		imagelabel.setBorder(BorderFactory.createTitledBorder(
    				BorderFactory.createEmptyBorder(5,10,5,10),"Length and taper",
					TitledBorder.CENTER,TitledBorder.TOP));
    		
    		// image area 2
    		imagelabel2 = new JLabel("", splitIcon, JLabel.CENTER);
    		imagelabel2.setBorder(BorderFactory.createTitledBorder(
    				BorderFactory.createEmptyBorder(5,10,5,10),"Splitting",
					TitledBorder.CENTER,TitledBorder.TOP));
    		
    		// image area 3
    		imagelabel3 = new JLabel("", substemIcon, JLabel.CENTER);
    		imagelabel3.setBorder(BorderFactory.createTitledBorder(
    				BorderFactory.createEmptyBorder(5,10,5,10),"Branching",
					TitledBorder.CENTER,TitledBorder.TOP));
    		
    		// param groups
    		JPanel lencurpanel = new JPanel();
    		lencurpanel.setLayout(new GridLayout(2,1));
    		lencurpanel.add(new ParamGroup(this,"LENTAPER","Length and taper",
    				i,imagelabel,lentapIcon));
    		lencurpanel.add(new ParamGroup(this,"CURVATURE","Curvature",
    				i,imagelabel,curveIcon));
    		panel.add(lencurpanel);
    		panel.add(new ParamGroup(this,"SPLITTING","Splitting",
    				i,imagelabel2,splitIcon));
    		panel.add(new ParamGroup(this,"BRANCHING","Branching",
    				i,imagelabel3,substemIcon));
    		//	    panel.add(new ParamGroup(this,"ADDBRANCH","Branch distribution",
    		//		     i,imagelabel,branchdistIcon));
    		
    		panel.add(imagelabel);
    		panel.add(imagelabel2);
    		panel.add(imagelabel3);
    	}
    	
    	// add tabs to content pane
    	Container contentPane = frame.getContentPane(); 
    	contentPane.add(tabs,BorderLayout.CENTER);
    	
    	// add status line
    	statusbar = new Statusbar();
    	Font font = statusbar.getFont().deriveFont(Font.PLAIN,12);
    	statusbar.setFont(font);
    	statusbar.addMouseListener(new StatusbarListener());
    	contentPane.add(statusbar,BorderLayout.PAGE_END);
    	
    	frame.pack();
    }

    void createMenuBar() {
	JMenuBar menubar;
	JMenu menu;
	JMenuItem item;
	
	/**** file menu ***/

	menubar=new JMenuBar();
	menu = new JMenu("File");
	menu.setMnemonic('F');

	// File new
	item = new JMenuItem("New");
	item.setMnemonic('N');
	item.addActionListener(new FileNewListener());
	menu.add(item);

	// File open
	item = new JMenuItem("Open...");
	item.setMnemonic('O');
	item.addActionListener(new FileOpenListener());
	menu.add(item);

	// File save
	item = new JMenuItem("Save");
	item.setMnemonic('S');
	item.addActionListener(new FileSaveListener());
	menu.add(item);

	// File save as
	item = new JMenuItem("Save as...");
	item.setMnemonic('A');
	item.addActionListener(new FileSaveAsListener());
	menu.add(item);
	
	menu.add(new JSeparator());

	// Save POV file
	item = new JMenuItem("Create tree...");
	item.setMnemonic('C');
	item.addActionListener(new CreateTreeListener());
	menu.add(item);

	menu.add(new JSeparator());

	// Quit
	item = new JMenuItem("Quit");
	item.setMnemonic('Q');
	item.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    // ask if values should be saved
		    if (! shouldSave()) return;
		    frame.dispose();
		}
	    });
	menu.add(item);
	
	menubar.add(menu);

	/**** setup menu ****/
	menu = new JMenu("Setup");
	menu.setMnemonic('S');

	// setup Arbaro
	item = new JMenuItem("Setup Arbaro");
	item.setMnemonic('S');
	item.addActionListener(new SetupArbaroListener());
	menu.add(item);	

	menubar.add(menu);


	/**** help menu ****/
	menu = new JMenu("Help");
	menu.setMnemonic('H');

	// help paramter
	item = new JMenuItem("Parameter");
	item.setMnemonic('P');
	item.addActionListener(new HelpParameterListener());
	menu.add(item);	

	// help about
	item = new JMenuItem("About Arbaro");
	item.setMnemonic('A');
	item.addActionListener(new HelpAboutListener());
	menu.add(item);

	menubar.add(menu);

	frame.setJMenuBar(menubar);
    }

    class ParamGroup extends JPanel {
	JLabel imagelabel;
	ImageIcon image;

	ParamGroup(ParamFrame parent, String group, String groupName, int level, 
			JLabel imglabel, ImageIcon img) {
		super();
		image = img;
		imagelabel = imglabel;
		
		// find params for this group and create labels and input fields
		TreeMap params = new TreeMap(tree.getParamGroup(level,group));
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(groupName),
				BorderFactory.createEmptyBorder(5,10,5,10)));
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		
		FocusListener groupListener = new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				imagelabel.setIcon(image);
				((TitledBorder)imagelabel.getBorder()).setTitle(image.getDescription());
				imagelabel.repaint();
			}
		};
		for (Iterator e=params.values().iterator();e.hasNext();) {
			AbstractParam p = (AbstractParam)e.next();
			
			JPanel panl = new JPanel();
			panl.setLayout(new BoxLayout(panl,BoxLayout.X_AXIS));
			panl.add(new JLabel(p.getName()));
			panl.add(Box.createHorizontalGlue());
			if (p.getName().equals("Shape")) {
				// create combo box for Shape param
				panl.add(Box.createRigidArea(new Dimension(7,0)));
				ShapeBox sh = new ShapeBox(parent,p); 
				sh.addFocusListener(groupListener);
				panl.add(sh);
			} else {
				// create text field
				ParamField pfield = new ParamField(parent,6,p);
				pfield.addFocusListener(groupListener);
				panl.add(pfield);
			}
			add(panl);
		}
		
	}
    };

    
    void setModified(boolean mod) {
    	modified = mod;
    	tree.params.enableDisable();
    }
    
//    class MyOptionPane extends JOptionPane {
//    	public int getMaxCharactersPerLineCount() {
//    		return 70;
//    	}
//    }
    
    class Statusbar extends JLabel {
    	String longText;
    	
    	public Statusbar () {
    		super();
    		setBorder(BorderFactory.createEmptyBorder(10,5,5,5));
    	}
    	
    	public void setLongText(String text) {
    			longText = text;
    	}
    	
    	public void showLongText() {
    		JLabel msg = new JLabel(longText.replace('\n',' '));
//    		Dimension dim = msg.getMaximumSize();
//    		dim.setSize(100,dim.getHeight());
//    		msg.setMaximumSize(dim);
    		JOptionPane.showMessageDialog(frame,msg,
					"Parameter description",JOptionPane.INFORMATION_MESSAGE);
    	}
    	
    }

    class StatusbarListener extends MouseAdapter {
    	public void mousePressed(MouseEvent e) {
    		((Statusbar)e.getSource()).showLongText();
    	}
    };

    class FileNewListener implements ActionListener {
    	public void actionPerformed(ActionEvent e) {
    		// ask if should save when modified...
    		if (! shouldSave()) return;
    		AbstractParam.loading=true;
    		tree.clearParams();
    		tree.setSpecies("default");
    		setModified(false);
    		AbstractParam.loading=false;
    	}
    };

    class FileOpenListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    // ask if should saved
	    if (! shouldSave()) return;

	    int returnVal = fileChooser.showOpenDialog(frame);
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
		System.err.println("opening file: " +
				   fileChooser.getSelectedFile().getName());
		try {
    		AbstractParam.loading=true;
		    tree.clearParams();
		    treefile = fileChooser.getSelectedFile();
		    // read parameters
		    tree.readFromXML(new FileInputStream(treefile));
    		AbstractParam.loading=false;
		    setModified(false);
		} catch (ErrorParam err) {
		    setModified(false);
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
    };

    boolean shouldSave() {
	if (modified) {
	    int result = JOptionPane.showConfirmDialog(frame,
		   "Some parameters are modified. Should the tree definition be saved?",
						       "Tree definition modified",
						       JOptionPane.YES_NO_CANCEL_OPTION,
						       JOptionPane.QUESTION_MESSAGE);  
	    if (result == JOptionPane.YES_OPTION) {
		if (treefile != null) {
		    return fileSave();
		} else {
		    return fileSaveAs();
		}
	    }
	    return (result != JOptionPane.CANCEL_OPTION);
	} else
	    return true; // not modified, can proceed
    }


    boolean fileSaveAs() {
	int returnVal = fileChooser.showSaveDialog(frame);
	if(returnVal == JFileChooser.APPROVE_OPTION) {
	    treefile = fileChooser.getSelectedFile();
	    return fileSave();
	}
	return false;
    }

    boolean fileSave() {
	System.err.println("saving to file: " +
			       fileChooser.getSelectedFile().getName());
	try {
	    PrintWriter out = new PrintWriter(new FileWriter(treefile));
	    tree.toXML(out);
	    setModified(false);
	    return true;
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
	return false;
    }	

    class FileSaveListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    if (treefile != null) {
		fileSave();
	    } else {
		fileSaveAs();
	    }
	}
    };

    class FileSaveAsListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    fileSaveAs();
	}
    }

    class CreateTreeListener implements ActionListener {
    	public void actionPerformed(ActionEvent e) {
    		new PovDialog(frame,tree,config);
    	}
    }

    class SetupArbaroListener implements ActionListener {
    	public void actionPerformed(ActionEvent e) {
    		new CfgDialog(frame,config);
    	}
    }

    class HelpParameterListener implements ActionListener {
    	public void actionPerformed(ActionEvent e) {
    		// Component c = frame.getMostRecentFocusOwner();
    		// System.err.println(c.getClass());
    		if (lastFocused.getClass() == ParamField.class) {
    			JOptionPane.showMessageDialog(frame, 
    					"<html>"+((ParamField)lastFocused).param.getLongDesc()+"</html>",
						"Parameter description",JOptionPane.INFORMATION_MESSAGE);
    		}
    	}
    }

    class HelpAboutListener implements ActionListener {
    	public void actionPerformed(ActionEvent e) {
    		JOptionPane.showMessageDialog(frame, net.sourceforge.arbaro.arbaro.programName,
    				"About Arbaro",JOptionPane.INFORMATION_MESSAGE);
    	}
    }
}

/****************** SpeciesField ************************/

class SpeciesField extends JTextField {

    Tree tree;

    public SpeciesField(int width, Tree tr) {
	super(width);

	tree = tr;
	setText(tree.getSpecies());
	setToolTipText("tree species name, used for object names in the POVRay file");

	addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    tree.setSpecies(getText());
		}
	    });
	addFocusListener(new FocusAdapter() {
		public void focusLost(FocusEvent e) {
		    tree.setSpecies(getText());
		}
	    });
	// add ChangeListener to set species name if changed in the tree
	tree.params.addChangeListener(new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
		    setText(tree.getSpecies());
		}
	    });
    }
}

/********************** ParamField ****************************/

class ParamField extends JTextField {
	
	ParamFrame parent;
	AbstractParam param;
	
	public ParamField(ParamFrame pnt, int width, AbstractParam par) {
		super(width);
		parent = pnt;
		param = par;
		
		setText(param.getDefaultValue());
		// ? param.setValue(getText());
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
			public void focusGained(FocusEvent e) {
				ParamField field = (ParamField)e.getSource();
				parent.lastFocused = field;
				parent.statusbar.setText("<html><a href=\"longDesc\">"
						+field.param.getName()+"</a>: "
						+field.param.getShortDesc()
						+"</html>");
				parent.statusbar.setLongText("<html>"
						+field.param.getLongDesc()+"</html>");
			}
		});
		param.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!param.empty()) {
					setText(param.getValue());
				} else {
					setText(param.getDefaultValue());
				}
				setEnabled(param.getEnabled());
			}
		});
		
	}
	
	public Dimension getMaximumSize() {
		return getPreferredSize();
	}
	
	void setParamValue() {
		try {
			if (! param.getValue().equals(getText())) {
				System.err.println("change "+param.getName()+" from "+param.getValue()+
						" to "+getText());
				param.setValue(getText());
				setText(param.getValue());
				parent.setModified(true);
			}
		} catch (ErrorParam err) {
			JOptionPane.showMessageDialog(getParent(),err.getMessage(),"Parameter Error",
					JOptionPane.ERROR_MESSAGE);
			// set focus to this field
			requestFocusInWindow();
		}
	}
}

/********************** ShapeBox *****************************/

class ShapeBox extends JComboBox {

    ParamFrame parent;
    IntParam param;

    //Integer [] values;
    final static String[] items = { "conical", "spherical", "hemispherical", "cylindrical", 
				    "tapered cylindrical","flame","inverse conical","tend flame",
				    "envelope" };
    
    final static String[] values = {"0","1","2","3","4","5","6","7","8"};
    ImageIcon [] shapeIcons;

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path,
					       String description) {
	java.net.URL imgURL = ShapeBox.class.getResource(path);
	if (imgURL != null) {
	    return new ImageIcon(imgURL, description);
	} else {
	    System.err.println("Couldn't find file: " + path);
	    return null;
	}
    }

    public ShapeBox(ParamFrame pnt, AbstractParam p) {
    	super(values);
    	parent = pnt;
    	param = (IntParam)p;
    	
    	// load Icons
    	shapeIcons = new ImageIcon[items.length];
    	for (int i=0; i<items.length; i++) {
    		// values[i] = new Integer(i);
    		shapeIcons[i] = createImageIcon("images/shape"+i+".png","Shape 0");
    	}
    	//	values[8] = new Integer(8);
    	
    	CellRenderer renderer= new CellRenderer();
    	//renderer.setPreferredSize(new Dimension(200, 130));
    	setRenderer(renderer);
    	
    	setSelectedIndex(param.intValue());
    	
    	addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			setParamValue(getSelectedIndex());
    		}
    	});

    	addFocusListener(new FocusAdapter() {
    		public void focusGained(FocusEvent e) {
    			ShapeBox field = (ShapeBox)e.getSource();
    			parent.lastFocused = field;
    			parent.statusbar.setText("<html><a href=\"longDesc\">"
    					+field.param.getName()+"</a>: "
						+field.param.getShortDesc()
						+"</html>");
    			parent.statusbar.setLongText("<html>"
    					+field.param.getLongDesc()+"</html>");
    		}
    	});
    	
    	param.addChangeListener(new ChangeListener() {
    		public void stateChanged(ChangeEvent e) {
    			setSelectedIndex(param.intValue());
    		}
    	});
    }
    
    void setParamValue(int value) {
    	try {
    		if (! param.getValue().equals(""+value)) {
    			System.err.println("change "+param.getName()+" from "+param.getValue()+
    					" to "+value);
    			param.setValue(""+value);
    			parent.setModified(true);
    		}
    	} catch (ErrorParam err) {
    		JOptionPane.showMessageDialog(getParent(),err.getMessage(),"Parameter Error",
    				JOptionPane.ERROR_MESSAGE);
    		// set focus to this field
    		requestFocusInWindow();
    	}
    }

    public Dimension getMaximumSize() {
	return getPreferredSize();
    }


    class CellRenderer extends JLabel implements ListCellRenderer {
	public CellRenderer() {
	    setOpaque(true);
	}
	public Component getListCellRendererComponent(
		      JList list,
		      Object value,
		      int index,
		      boolean isSelected,
		      boolean cellHasFocus)
	{
	    int myIndex = Integer.parseInt(value.toString());

	    if (isSelected) {
		setBackground(list.getSelectionBackground());
		setForeground(list.getSelectionForeground());
	    } else {
		setBackground(list.getBackground());
		setForeground(list.getForeground());
	    }
	    
	    //Set the icon and text.  If icon was null, say so.
	    ImageIcon icon;
	    if (myIndex>=0 && myIndex<9) {
		icon = shapeIcons[myIndex];
		setIcon(icon);
	    }; 

	    setText(items[myIndex]);
	    
	    return this;
	}
    };

};








