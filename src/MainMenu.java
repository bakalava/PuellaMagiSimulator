import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/* Credits:
 * Main menu background: http://www.wallconvert.com/wallpapers/anime/puella-magi-madoka-magica-16024.html
 * Body text font: http://www.dafont.com/weblysleek-ui.font
 * (Charlotte) Icon image: http://burnaroundme.livejournal.com/23799.html
 */

/** A class that displays the main menu. It has 3 screens to display:
 * options to create a new simulation, an explanation of the Puella Magi
 * universe, and credits.
 * 
 * @author Sally Hui
 * @author Jiayin Huang
 * @author Tony Cui
 */
public class MainMenu extends JFrame implements ActionListener
{
	private WorldGenerator worldGenerator = new WorldGenerator ();
	private WorldGeneratorOptions WGsettings;

	private JButton startJB = new JButton ("Start!");
	private JButton introductionJB = new JButton ("Introduction");
	private JButton creditsJB = new JButton ("Credits");
	private JButton exitJB = new JButton ("Exit");
	private BufferedImage mainBgrd, iconImg;
	private MainJPanel main = new MainJPanel ();
	private JPanel boxPanel = new JPanel ();

	private JTextArea text;
	private JScrollPane scrollText;
	private String textString = "";
	private JPanel options = new JPanel ();
	private JButton newSim = new JButton ("NEW SIMULATION"); 
	private JButton deleteFile = new JButton ("Delete File");
	private JButton loadFile = new JButton ("Load File");
	private JButton exportFile = new JButton ("Export File");
	private JButton importFile = new JButton ("Import File");
	private FileNameButton[] fileNames;
	private String selectedFileName;
	private Viewport load;
	private Font font; 

	/** Creates a new JFrame containing the main menu. 
	 */
	public MainMenu ()
	{	
		// Import Image(s)
		try
		{			
			mainBgrd = ImageIO.read (new File("images/mainBgrd.png")); //TODO: getResource is confusing
			iconImg = ImageIO.read (new File("images/iconImg.png"));
		}
		catch (IOException e)
		{
			e.printStackTrace(); // help diagnose problems
		}

		// Set up window
		setTitle("Puella Magi Simulator");
		setResizable(false);  
		setPreferredSize(new Dimension (700, 394));
		setMinimumSize(new Dimension (700, 394));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null); // open in middle of screen
		this.getContentPane().setLayout(new BorderLayout());  
		setIconImage (iconImg);

		// Add action listeners
		startJB.addActionListener (this);
		introductionJB.addActionListener (this);
		creditsJB.addActionListener (this);
		exitJB.addActionListener (this);
		newSim.addActionListener (this);
		loadFile.addActionListener (this);
		exportFile.addActionListener (this);
		importFile.addActionListener (this);
		deleteFile.addActionListener (this);

		// Format buttons
		font = new Font("WeblySleek UI Light", Font.PLAIN, 14); // set fonts
		startJB.setFont(font);
		introductionJB.setFont (font);
		creditsJB.setFont (font);
		exitJB.setFont (font);
		newSim.setFont (font);
		loadFile.setFont (font);
		exportFile.setFont (font);
		importFile.setFont (font);
		deleteFile.setFont(font);

		startJB.setBackground(new Color (115, 99, 99, 60)); // set colours
		introductionJB.setBackground(new Color (115, 99, 99, 60));
		creditsJB.setBackground(new Color (115, 99, 99, 60));
		exitJB.setBackground(new Color (115, 99, 99, 60));

		// Set up main menu options
		options.setPreferredSize (new Dimension (700,2)); // options is a JPanel
		options.setMinimumSize (new Dimension (700,2)); // that contains the 
		options.setMinimumSize (new Dimension (700,2)); // buttons.
		options.setLayout(new GridLayout (1, 0));
		options.add (startJB); // add buttons to options panel
		options.add (introductionJB);
		options.add (creditsJB);
		options.add (exitJB);
		options.setOpaque (false);

		// Set up main screen (boxPanel is a jpanel of the whole screen's glasspane)
		boxPanel.setLayout(new BoxLayout (boxPanel, BoxLayout.Y_AXIS));
		boxPanel.add(Box.createRigidArea(new Dimension(700, 333)));
		boxPanel.add(options);
		boxPanel.setOpaque(false);

		// Set boxpanel to glasspane
		setGlassPane (boxPanel);
		getGlassPane().setVisible(true);

		// Set up text area/scrollpane
		text = new JTextArea (textString);
		text.setRows (8);
		text.setColumns(40);
		text.setEditable (false);
		text.setOpaque (false);
		text.setLineWrap (true);
		text.setWrapStyleWord(true);
		text.setBackground(new Color(0, 0, 0, 0));
		text.setForeground (Color.white);
		text.setFont (font);
		text.setCaretPosition(0); 

		// Set up scrollpane for introduction/credits
		scrollText = new JScrollPane (text, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollText.setOpaque (false);
		scrollText.getViewport().setOpaque (false);
		scrollText.setBackground(new Color(0, 0, 0, 0));
		scrollText.getViewport().setBackground(new Color(0, 0, 0, 0));
		scrollText.setBorder (null);
		scrollText.setViewportBorder(null);
		scrollText.getViewport().getInsets().set(0,0,0,0);

		// Add components to JFrame
		this.getContentPane().add (main, BorderLayout.CENTER);
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		Object source = (Object) e.getSource (); // find out which button fired the ActionEvent
		if (source.equals(startJB))
		{
			startOptions(); // display new simulation/load file options
		}
		else if (source.equals(introductionJB))
		{ // set text on screen to explanation text
			textString = "This program simulates the basic mechanics behind the magical girl cyclic system present in Puella Magi Madoka Magica. The cycle is instigated by a creature called Incubator. It selectively contracts female humans into becoming magical girls by granting each contractee one wish. These new magical girls then fight witches. If a magical girl’s taint parameter (increased by magic use and despair) exceeds a certain threshold, they turn into a witch, releasing a large amount of energy in the process. This energy is then harvested by the Incubator. The cycle repeats."
					+ "\n        When a female human is contracted to become a magical girl (a Puella Magi), her soul is sucked out of her and placed into a soul gem. If she does not purify her soul gem by defeating witches and purifying her own soul gem with the dropped Grief Seed, her soul gem will corrupt and eventually turn into a Grief Seed of its own."
					+ "\n        Walpurgisnacht is the strongest witch in the Puella Magi universe."
					+ "\n        During the simulation, press the keys 0 to 5 on your keyboard to access different hidden tools. The buttons to the right and bottom can be used to interact with the screen and entities. Read the user guide for more information.";
			setMainText (textString);
		}
		else if (source.equals(creditsJB))
		{ // set text on screen to credits text
			textString = "Programmed by Tony Cui, Jiayin Huang, and Sally Hui."
					+ "\n        Want to see the inspiration behind this 10/10 amazing program?         http://www.youtube.com/watch?v=LHIPlQS5KfA"
					+ "\n        This plays at the beginning of the anime. Unbeknownst to the innocent viewer but beknownst to you now (no that wasn't a word), this is actually a preview of the end of this fabulous anime. Man I love spoilers. But I bet you can't read Spanish anyways."
					+ "\n        [18+] http://www.youtube.com/watch?v=VFQRp6EP_JM"
					+ "\n        Note the Grief Seed, Soul Gems, and general depression. As you're enjoying the simulation, please imagine that one of these two videos is happening."
					+ "\n        Image credits:"
					+ "\n        Main menu background: http://www.wallconvert.com/wallpapers/anime/puella-magi-madoka-magica-16024.html"
					+ "\n        Body text font: http://www.dafont.com/weblysleek-ui.font"
					+ "\n        Madoka font: http://wiki.puella-magi.net/Deciphering_the_runes"
					+ "\n        Charlotte Icon image: http://burnaroundme.livejournal.com/23799.html";
			setMainText (textString);
		} 
		else if (source.equals(exitJB))
		{
			System.exit (0); // exit
		}
		else if (source.equals(newSim))
		{ // create new simulation screen
			WGsettings = configLoad(); // customize world generator
			WGsettings.setVisible (true);
			configSave(WGsettings); 
			if (WGsettings.shouldStartNewSim)
			{
				SimulationScreen newSim = new SimulationScreen ("Puella Magi Simulator", 700, 394, worldGenerator);
				newSim.setVisible (true);
				this.dispose(); // close main menu
			}
		}
		else if (source.equals(loadFile))
		{
			deserialize(); // load file and make new simulation of it
			if (selectedFileName != null)
			{
				SimulationScreen newSim = new SimulationScreen (selectedFileName, "Puella Magi Simulator", 700, 394, load);
				newSim.setVisible (true);
				this.dispose(); // close main menu
			}
		}
		else if (source.equals(exportFile)) // export file
		{
			if (selectedFileName != null)
				exportF();
		}
		else if (source.equals(importFile)) // import file
		{
			if (selectedFileName != null)
				importF();
		}
		else if (source.equals (deleteFile)) // delete file
		{
			if (selectedFileName != null)
				deleteF();
		}
	}

	/** Exports a file that was selected in the start simulation screen to a
	 * user-selected directory.
	 * 
	 */
	public void exportF ()
	{
		File toExport = new File ("./saves/" + selectedFileName); // get file
		JFileChooser fc = new JFileChooser ();
		int retVal = fc.showSaveDialog (this); // get save location
		if (retVal == JFileChooser.APPROVE_OPTION)
		{
			File newLoc = fc.getSelectedFile();
			File path = new File (newLoc.getParentFile() + "\\" + fc.getSelectedFile().getName() + ".sav");
			boolean exists = path.exists(); // file already exists
			try
			{
				FileOutputStream fout = new FileOutputStream(path);
				if (exists) // file (or something with the same name) exists in that directory
				{
					// ask if user wants to save over
					int event = JOptionPane.showConfirmDialog(null, "The file you are trying to write already exists. Would you like to overwrite the file?", "Error", JOptionPane.YES_NO_OPTION);
					if (event == JOptionPane.YES_OPTION) // yep! 
					{
						ObjectOutputStream oos = new ObjectOutputStream(fout); // write file 
						oos.writeObject(toExport); 
						oos.close(); 
					}
				}    
				else 
				{
					ObjectOutputStream oos = new ObjectOutputStream(fout);   
					oos.writeObject(toExport); // serialize viewport
					oos.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		startOptions();
	}

	/** Imports a user-selected file.
	 */
	public void importF()
	{
		JFileChooser fc = new JFileChooser ();
		int retVal = fc.showOpenDialog (this); // show dialog to get file location
		if (retVal == JFileChooser.APPROVE_OPTION)
		{
			File importFile = fc.getSelectedFile(); // rename file to move it
			importFile.renameTo (new File ("./saves/" + "\\" + fc.getSelectedFile().getName()));
		}	
		startOptions(); // reload screen
	}

	/** Deletes a file.
	 */
	public void deleteF()
	{
		File toDelete = new File ("./saves/" + selectedFileName); // get file to delete
		toDelete.delete();
		startOptions(); // reload screen
	}

	/** Saves the configurations set in the specified WorldGeneratorOptions
	 * object to PuellaMagi.cfg.
	 * 
	 * @param options	the object to serialize
	 */
	public void configSave (WorldGeneratorOptions options)
	{		
		ArrayList<Integer> params = options.exportState();

		try 
		{
			FileOutputStream fout = new FileOutputStream("PuellaMagi.cfg"); // write file
			ObjectOutputStream oos = new ObjectOutputStream(fout);   
			oos.writeObject(params); // serialize viewport
			oos.close();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	/** Attempts to load the configuration settings from last time.
	 * If the deserialization is unsuccessful, it just creates a new
	 * WorldGeneratorOptions instance.
	 * 
	 * @return 	the deserialized object
	 */
	public WorldGeneratorOptions configLoad ()
	{
		WorldGeneratorOptions options = new WorldGeneratorOptions ();
		ArrayList<Integer> params;

		try
		{ 
			FileInputStream fin = new FileInputStream("PuellaMagi.cfg");
			ObjectInputStream ois = new ObjectInputStream(fin);
			params = (ArrayList<Integer>)ois.readObject();
			options.importState(params);		
			ois.close();
		}
		catch(Exception ex)
		{			
			// Do nothing
		}

		return options;
	}

	/** A class that allows the user to configure the world generator object used
	 * to generate new spacetime and viewport objects.
	 * 
	 * @author Sally Hui
	 * @author Jiayin Huang
	 * @author Tony Cui
	 */
	class WorldGeneratorOptions extends JDialog implements ActionListener
	{		
		private static final long serialVersionUID = 1L;

		private JSlider chunkSlider = new JSlider (0, 256, 128); // chunk
		private JSlider widthSlider = new JSlider (0, 100, 50); // width
		private JSlider heightSlider = new JSlider (0, 100, 50); // height

		private JSlider humanMinXSlider = new JSlider (0, 10000, 800);
		private JSlider humanMaxXSlider = new JSlider (0, 10000, 4800);
		private JSlider humanMinYSlider = new JSlider (0, 10000, 800);		
		private JSlider humanMaxYSlider = new JSlider (0, 10000, 4800);

		private JSlider witchMinXSlider = new JSlider (0, 10000, 800);
		private JSlider witchMaxXSlider = new JSlider (0, 10000, 4800);
		private JSlider witchMinYSlider = new JSlider (0, 10000, 800);		
		private JSlider witchMaxYSlider = new JSlider (0, 10000, 4800);

		private JSlider puellaMinXSlider = new JSlider (0, 10000, 800);
		private JSlider puellaMaxXSlider = new JSlider (0, 10000, 4800);
		private JSlider puellaMinYSlider = new JSlider (0, 10000, 800);		
		private JSlider puellaMaxYSlider = new JSlider (0, 10000, 4800);

		private JSlider puellaPopulationSlider = new JSlider (0, 50, 7);
		private JSlider humanPopulationSlider = new JSlider (0, 500, 200);
		private JSlider witchPopulationSlider = new JSlider (0, 100, 20);
		private JSlider humanMinAgeSlider = new JSlider (0, 100, 0);
		private JSlider humanMaxAgeSlider = new JSlider (0, 1000, 1000);

		private JCheckBox walpurgisnachtBtn = new JCheckBox ();
		private JButton startButton = new JButton ("Generate World");

		public boolean shouldStartNewSim = false;

		public WorldGeneratorOptions ()
		{
			// Set up dialog
			setSize (500,420);
			setModal (true);
			setResizable (false);
			setLocationRelativeTo (null);
			setTitle ("New Simulation Settings");

			// Terrain settings
			JPanel terrainSettings = new JPanel ();
			terrainSettings.setPreferredSize (new Dimension (300, 0));

			// Tile width and height (chunk) slider			
			JPanel chunkPanel = new JPanel ();
			chunkPanel.add(new JLabel ("Chunk: "));
			chunkSlider.setMajorTickSpacing(64); 
			chunkSlider.setMinorTickSpacing(16);
			chunkSlider.setPaintTicks(true);
			chunkSlider.setPaintLabels(true);
			chunkPanel.add (chunkSlider);

			// Spacetime width slider
			JPanel widthPanel = new JPanel();
			widthPanel.add (new JLabel ("Width: "));
			widthSlider.setMajorTickSpacing(20); 
			widthSlider.setMinorTickSpacing(5);
			widthSlider.setPaintTicks(true);
			widthSlider.setPaintLabels(true);
			widthPanel.add (widthSlider);

			// Spacetime height slider
			JPanel heightPanel = new JPanel();
			heightPanel.add (new JLabel ("Height: "));
			heightSlider.setMajorTickSpacing(20);
			heightSlider.setMinorTickSpacing(5);
			heightSlider.setPaintTicks(true);
			heightSlider.setPaintLabels(true);
			heightPanel.add (heightSlider);

			// Putting together terrain settings tab
			terrainSettings.add (chunkPanel);
			terrainSettings.add (widthPanel);
			terrainSettings.add (heightPanel);

			// Setting scroll pane for tab
			JScrollPane terrainSettingsScroll = new JScrollPane (terrainSettings, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			terrainSettingsScroll.setPreferredSize (new Dimension (300, 350));
			terrainSettingsScroll.setBorder (null);
			terrainSettingsScroll.setViewportBorder(null);

			// Entities settings
			JPanel entitiesSettings = new JPanel ();
			entitiesSettings.add (walpurgisnachtBtn);
			entitiesSettings.setPreferredSize (new Dimension (300, 1050));			

			// Enable or Disable Walpurgisnacht
			JPanel WNPanel = new JPanel();
			WNPanel.add (new JLabel ("Has Walpurgisnacht: "));
			WNPanel.add (walpurgisnachtBtn);
			walpurgisnachtBtn.setSelected(false);
			entitiesSettings.add (WNPanel);

			// human population
			JPanel humanPopPanel = new JPanel ();
			humanPopPanel.add(new JLabel ("                             Human Population: "));
			humanPopulationSlider.setMajorTickSpacing(200); 
			humanPopulationSlider.setMinorTickSpacing(50);
			humanPopulationSlider.setPaintTicks(true);
			humanPopulationSlider.setPaintLabels(true);
			humanPopPanel.add (humanPopulationSlider);
			entitiesSettings.add (humanPopPanel);

			// puella population
			JPanel puellaPopPanel = new JPanel ();
			puellaPopPanel.add(new JLabel ("                        Puella Magi Population: "));
			puellaPopulationSlider.setMajorTickSpacing(25); 
			puellaPopulationSlider.setMinorTickSpacing(5);
			puellaPopulationSlider.setPaintTicks(true);
			puellaPopulationSlider.setPaintLabels(true);
			puellaPopPanel.add (puellaPopulationSlider);
			entitiesSettings.add (puellaPopPanel);

			// witch population
			JPanel witchPopPanel = new JPanel ();
			witchPopPanel.add(new JLabel ("                             Witch Population: "));
			witchPopulationSlider.setMajorTickSpacing(50); 
			witchPopulationSlider.setMinorTickSpacing(10);
			witchPopulationSlider.setPaintTicks(true);
			witchPopulationSlider.setPaintLabels(true);
			witchPopPanel.add (witchPopulationSlider);
			entitiesSettings.add (witchPopPanel);

			// human minimum age 
			JPanel humanMinAgePanel = new JPanel ();
			humanMinAgePanel.add(new JLabel ("                     Human minimum age: "));
			humanMinAgeSlider.setMajorTickSpacing(50); 
			humanMinAgeSlider.setMinorTickSpacing(25);
			humanMinAgeSlider.setPaintTicks(true);
			humanMinAgeSlider.setPaintLabels(true);
			humanMinAgePanel.add (humanMinAgeSlider);
			entitiesSettings.add (humanMinAgePanel);

			// human maximum age
			JPanel humanMaxAgePanel = new JPanel ();
			humanMaxAgePanel.add(new JLabel ("                    Human maximum age: "));
			humanMaxAgeSlider.setMajorTickSpacing(250); 
			humanMaxAgeSlider.setMinorTickSpacing(50);
			humanMaxAgeSlider.setPaintTicks(true);
			humanMaxAgeSlider.setPaintLabels(true);
			humanMaxAgePanel.add (humanMaxAgeSlider);
			entitiesSettings.add (humanMaxAgePanel);

			// Set up custom JSlider labels for percentages

			Dictionary<Integer,JLabel> dict = new Hashtable<Integer,JLabel>();
			for (int i = 0; i <= 10000; i += 5000) 			
				dict.put(i, new JLabel(Double.toString(i / 10000.0)));			

			// Adding percentage JSliders
			JPanel humanMinXPanel = new JPanel (); // panel (for formatting)
			humanMinXPanel.add(new JLabel ("Min. Human Spawn X-coordinate (width%): ")); // label
			humanMinXSlider.setMajorTickSpacing(2500); // number markers
			humanMinXSlider.setMinorTickSpacing(500);
			humanMinXSlider.setPaintTicks(true);
			humanMinXSlider.setPaintLabels(true);
			humanMinXSlider.setLabelTable(dict);
			humanMinXPanel.add (humanMinXSlider);
			entitiesSettings.add (humanMinXPanel);

			JPanel humanMaxXPanel = new JPanel ();
			humanMaxXPanel.add(new JLabel ("Max. Human Spawn X-coordinate (width%): "));
			humanMaxXSlider.setMajorTickSpacing(2500); 
			humanMaxXSlider.setMinorTickSpacing(500);
			humanMaxXSlider.setPaintTicks(true);
			humanMaxXSlider.setPaintLabels(true);
			humanMaxXSlider.setLabelTable(dict);
			humanMaxXPanel.add (humanMaxXSlider);
			entitiesSettings.add (humanMaxXPanel);

			JPanel humanMinYPanel = new JPanel ();
			humanMinYPanel.add(new JLabel ("Min. Human Spawn Y-coordinate (height%): "));
			humanMinYSlider.setMajorTickSpacing(2500); 
			humanMinYSlider.setMinorTickSpacing(500);
			humanMinYSlider.setPaintTicks(true);
			humanMinYSlider.setPaintLabels(true);
			humanMinYSlider.setLabelTable(dict);
			humanMinYPanel.add (humanMinYSlider);
			entitiesSettings.add (humanMinYPanel);			

			JPanel humanMaxYPanel = new JPanel ();
			humanMaxYPanel.add(new JLabel ("Max. Human Spawn Y-coordinate (height%): "));
			humanMaxYSlider.setMajorTickSpacing(2500); 
			humanMaxYSlider.setMinorTickSpacing(500);
			humanMaxYSlider.setPaintTicks(true);
			humanMaxYSlider.setPaintLabels(true);
			humanMaxYSlider.setLabelTable(dict);
			humanMaxYPanel.add (humanMaxYSlider);
			entitiesSettings.add (humanMaxYPanel);

			JPanel puellaMinXPanel = new JPanel ();
			puellaMinXPanel.add(new JLabel ("Min. Puella Spawn X-coordinate (width%): "));
			puellaMinXSlider.setMajorTickSpacing(2500); 
			puellaMinXSlider.setMinorTickSpacing(500);
			puellaMinXSlider.setPaintTicks(true);
			puellaMinXSlider.setPaintLabels(true);
			puellaMinXSlider.setLabelTable(dict);
			puellaMinXPanel.add (puellaMinXSlider);
			entitiesSettings.add (puellaMinXPanel);

			JPanel puellaMaxXPanel = new JPanel ();
			puellaMaxXPanel.add(new JLabel ("Max. Puella Spawn X-coordinate (width%): "));
			puellaMaxXSlider.setMajorTickSpacing(2500); 
			puellaMaxXSlider.setMinorTickSpacing(500);
			puellaMaxXSlider.setPaintTicks(true);
			puellaMaxXSlider.setPaintLabels(true);
			puellaMaxXSlider.setLabelTable(dict);
			puellaMaxXPanel.add (puellaMaxXSlider);
			entitiesSettings.add (puellaMaxXPanel);

			JPanel puellaMinYPanel = new JPanel ();
			puellaMinYPanel.add(new JLabel ("Min. Puella Spawn Y-coordinate (height%): "));
			puellaMinYSlider.setMajorTickSpacing(2500); 
			puellaMinYSlider.setMinorTickSpacing(500);
			puellaMinYSlider.setPaintTicks(true);
			puellaMinYSlider.setPaintLabels(true);
			puellaMinYSlider.setLabelTable(dict);
			puellaMinYPanel.add (puellaMinYSlider);
			entitiesSettings.add (puellaMinYPanel);

			JPanel puellaMaxYPanel = new JPanel ();
			puellaMaxYPanel.add(new JLabel ("Max. Puella Spawn Y-coordinate (height%): "));
			puellaMaxYSlider.setMajorTickSpacing(2500); 
			puellaMaxYSlider.setMinorTickSpacing(500);
			puellaMaxYSlider.setPaintTicks(true);
			puellaMaxYSlider.setPaintLabels(true);
			puellaMaxYSlider.setLabelTable(dict);
			puellaMaxYPanel.add (puellaMaxYSlider);
			entitiesSettings.add (puellaMaxYPanel);

			JPanel witchMinXPanel = new JPanel ();
			witchMinXPanel.add(new JLabel ("Min. Witch Spawn X-coordinate (width%): "));
			witchMinXSlider.setMajorTickSpacing(2500); 
			witchMinXSlider.setMinorTickSpacing(500);
			witchMinXSlider.setPaintTicks(true);
			witchMinXSlider.setPaintLabels(true);
			witchMinXSlider.setLabelTable(dict);
			witchMinXPanel.add (witchMinXSlider);
			entitiesSettings.add (witchMinXPanel);

			JPanel witchMaxXPanel = new JPanel ();
			witchMaxXPanel.add(new JLabel ("Max. Witch Spawn X-coordinate (width%): "));
			witchMaxXSlider.setMajorTickSpacing(2500); 
			witchMaxXSlider.setMinorTickSpacing(500);
			witchMaxXSlider.setPaintTicks(true);
			witchMaxXSlider.setPaintLabels(true);
			witchMaxXSlider.setLabelTable(dict);
			witchMaxXPanel.add (witchMaxXSlider);
			entitiesSettings.add (witchMaxXPanel);

			JPanel witchMinYPanel = new JPanel ();
			witchMinYPanel.add(new JLabel ("Min. Witch Spawn Y-coordinate (height%): "));
			witchMinYSlider.setMajorTickSpacing(2500); 
			witchMinYSlider.setMinorTickSpacing(500);
			witchMinYSlider.setPaintTicks(true);
			witchMinYSlider.setPaintLabels(true);
			witchMinYSlider.setLabelTable(dict);
			witchMinYPanel.add (witchMinYSlider);
			entitiesSettings.add (witchMinYPanel);			

			JPanel witchMaxYPanel = new JPanel ();
			witchMaxYPanel.add(new JLabel ("Max. Witch Spawn Y-coordinate (height%): "));
			witchMaxYSlider.setMajorTickSpacing(2500); 
			witchMaxYSlider.setMinorTickSpacing(500);
			witchMaxYSlider.setPaintTicks(true);
			witchMaxYSlider.setPaintLabels(true);
			witchMaxYSlider.setLabelTable(dict);
			witchMaxYPanel.add (witchMaxYSlider);
			entitiesSettings.add (witchMaxYPanel);

			JScrollPane entitiesSettingsScroll = new JScrollPane (entitiesSettings, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			entitiesSettingsScroll.setPreferredSize (new Dimension (300, 350));
			entitiesSettingsScroll.setBorder (null);
			entitiesSettingsScroll.setViewportBorder(null);
			entitiesSettingsScroll.getVerticalScrollBar().setUnitIncrement(16);

			// Add to tabbed pane
			JTabbedPane tabbedPane = new JTabbedPane ();
			tabbedPane.addTab("Entities", entitiesSettingsScroll);
			tabbedPane.addTab("Terrain", terrainSettingsScroll);

			// Start button
			startButton.addActionListener(this);
			this.getContentPane().setLayout(new BorderLayout());
			this.getContentPane().add (tabbedPane, BorderLayout.CENTER);
			this.getContentPane().add (startButton, BorderLayout.SOUTH);
			setDefaultCloseOperation (JFrame.DISPOSE_ON_CLOSE);			
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Object source = (Object) e.getSource(); // find out which object produced the action event
			if (source.equals(startButton))
			{
				setValues();
				shouldStartNewSim = true;
				this.dispose(); // start
			}
		}
		
		/** Sets the variables in the WorldGenerator based on the
		 * current state of the sliders and checkboxes. 
		 */	
		public void setValues()
		{
			// terrain

			worldGenerator.setChunk(chunkSlider.getValue() + 1);			
			worldGenerator.setWidth(widthSlider.getValue() + 1);			
			worldGenerator.setHeight(heightSlider.getValue() + 1);

			// human spawn area

			worldGenerator.setHumanSpawnMinX(humanMinXSlider.getValue()/10000.0);			
			worldGenerator.setHumanSpawnMinY(humanMinYSlider.getValue()/10000.0);		
			worldGenerator.setHumanSpawnMaxX(humanMaxXSlider.getValue()/10000.0);		
			worldGenerator.setHumanSpawnMaxY(humanMaxYSlider.getValue()/10000.0);

			// witch spawn area

			worldGenerator.setWitchSpawnMinX(witchMinXSlider.getValue()/10000.0);			
			worldGenerator.setWitchSpawnMinY(witchMinYSlider.getValue()/10000.0);			
			worldGenerator.setWitchSpawnMaxX(witchMaxXSlider.getValue()/10000.0);			
			worldGenerator.setWitchSpawnMaxY(witchMaxYSlider.getValue()/10000.0);

			// puella spawn area

			worldGenerator.setPuellaSpawnMinX(puellaMinXSlider.getValue()/10000.0);			
			worldGenerator.setPuellaSpawnMinY(puellaMinYSlider.getValue()/10000.0);			
			worldGenerator.setPuellaSpawnMaxX(puellaMaxXSlider.getValue()/10000.0);			
			worldGenerator.setPuellaSpawnMaxY(puellaMaxYSlider.getValue()/10000.0);

			// populations

			worldGenerator.setHumanPopulation (humanPopulationSlider.getValue());			
			worldGenerator.setWitchPopulation (witchPopulationSlider.getValue());			
			worldGenerator.setPuellaPopulation (puellaPopulationSlider.getValue());

			// human age range

			worldGenerator.setHumanMinAge(humanMinAgeSlider.getValue() + 1);			
			worldGenerator.setHumanMaxAge(humanMaxAgeSlider.getValue() + 1);

			// Walpurgisnacht

			worldGenerator.setHasWalpurgisnacht(walpurgisnachtBtn.isSelected());
		}

		/** Exports the current state of this WorldGeneratorOptions object
		 * by using an ArrayList of integers, which is meant to be read
		 * by the corresponding importState method of this class.
		 * 
		 * @return	an ArrayList of integer parameters
		 */
		public ArrayList<Integer> exportState ()
		{
			ArrayList<Integer> out = new ArrayList<Integer> (21);

			out.add(chunkSlider.getValue());
			out.add(widthSlider.getValue());
			out.add(heightSlider.getValue());

			out.add(humanMinXSlider.getValue());
			out.add(humanMaxXSlider.getValue());
			out.add(humanMinYSlider.getValue());		
			out.add(humanMaxYSlider.getValue());

			out.add(witchMinXSlider.getValue());
			out.add(witchMaxXSlider.getValue());
			out.add(witchMinYSlider.getValue());		
			out.add(witchMaxYSlider.getValue());

			out.add(puellaMinXSlider.getValue());
			out.add(puellaMaxXSlider.getValue());;
			out.add(puellaMinYSlider.getValue());		
			out.add(puellaMaxYSlider.getValue());

			out.add(puellaPopulationSlider.getValue());
			out.add(humanPopulationSlider.getValue());
			out.add(witchPopulationSlider.getValue());
			out.add(humanMinAgeSlider.getValue());
			out.add(humanMaxAgeSlider.getValue());

			out.add(walpurgisnachtBtn.isSelected() ? 1 : 0);

			return out;
		}

		/** Sets the state of this WorldGeneratorOptions object by
		 * reading the inputed ArrayList of integers that was produced
		 * by the corresponding exportState() method of this class.
		 * 
		 * @param in	the ArrayList of integer parameters
		 */
		public void importState (ArrayList<Integer> in)
		{
			chunkSlider.setValue(in.get(0));
			widthSlider.setValue(in.get(1));
			heightSlider.setValue(in.get(2));

			humanMinXSlider.setValue(in.get(3));
			humanMaxXSlider.setValue(in.get(4));
			humanMinYSlider.setValue(in.get(5));		
			humanMaxYSlider.setValue(in.get(6));

			witchMinXSlider.setValue(in.get(7));
			witchMaxXSlider.setValue(in.get(8));
			witchMinYSlider.setValue(in.get(9));		
			witchMaxYSlider.setValue(in.get(10));

			puellaMinXSlider.setValue(in.get(11));
			puellaMaxXSlider.setValue(in.get(12));
			puellaMinYSlider.setValue(in.get(13));		
			puellaMaxYSlider.setValue(in.get(14));

			puellaPopulationSlider.setValue(in.get(15));
			humanPopulationSlider.setValue(in.get(16));
			witchPopulationSlider.setValue(in.get(17));
			humanMinAgeSlider.setValue(in.get(18));
			humanMaxAgeSlider.setValue(in.get(19));

			walpurgisnachtBtn.setSelected(in.get(20) == 1);			
		}	
	}

	/** The class that provides custom buttons for the file names.
	 * 
	 * @author Sally Hui
	 * @author Jiayin Huang
	 * @author Tony Cui
	 *
	 */
	class FileNameButton extends JButton implements ActionListener
	{
		public FileNameButton (String fileName)
		{
			// set up button
			super();
			setOpaque (false);
			setBackground(new Color (10,10,10,255));
			setForeground(Color.white);
			setText (fileName);
			addActionListener(this);
			repaint();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			JButton file = (JButton) e.getSource(); // to find out which button fired the actionevent			
			selectedFileName = file.getText(); // set file name to button text

			int i = 0;
			boolean found = false;
			for (i = 0; i < fileNames.length && !found; i++) // find button that fired action listener
			{
				found = fileNames [i] == file;				
			}
			for (int j = 0; j < fileNames.length; j++)
			{
				fileNames [j].getModel().setEnabled (true); // enable/disable accordingly
				fileNames [j].getModel().setPressed (false);
			}
		}
	}

	/** Deserializes the selected save file. That is, loads/unpacks it. Files
	 * to be deserialized by this method are serialized by the serialize method
	 * in the SimulationScreen class.
	 */
	public void deserialize ()
	{
		try
		{ // saved in "saves" folder in project folder
			FileInputStream fin = new FileInputStream("./saves/" + selectedFileName);
			ObjectInputStream ois = new ObjectInputStream(fin);
			load = (Viewport) ois.readObject(); // unpack file to load viewport object
			load.addMouseListeners();			
			ois.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();                           
		} 
	}

	/** Displays the screen to start a new simulation or load an existing one.
	 */
	public void startOptions ()
	{	
		// Get file names
		String path = "./saves/";  // save files saved in "saves" folder in project folder
		String files;
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles(); 
		fileNames = new FileNameButton [listOfFiles.length];

		// Get file names and set them to buttons
		for (int i = 0; i < listOfFiles.length; i++) 
		{
			if (listOfFiles[i].isFile()) 
			{
				files = listOfFiles[i].getName(); // get name of file
				fileNames [i] = new FileNameButton (files); // format buttons
			}
		}

		// Add buttons to JPanel
		JPanel filesPanel = new JPanel (); // JPanel to contain the file name buttons
		filesPanel.setOpaque(false); 
		filesPanel.setLayout(new BoxLayout (filesPanel, BoxLayout.Y_AXIS));
		for (int i = 0; i < fileNames.length; i++) // add buttons
			filesPanel.add (fileNames [i]);

		// Add JPanel containing buttons to JScrollPane
		JScrollPane filesScrollPane = new JScrollPane (filesPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		filesScrollPane.setOpaque(false);
		filesScrollPane.setPreferredSize(new Dimension (300, 250)); // set size
		filesScrollPane.setMaximumSize(new Dimension (300, 250));
		filesScrollPane.getViewport().setOpaque (false);
		filesScrollPane.setBackground(new Color(0, 0, 0, 0));
		filesScrollPane.getViewport().setBackground(new Color(0, 0, 0, 0));

		// File operations options
		JPanel fileOperations = new JPanel ();
		fileOperations.setOpaque (false);
		fileOperations.setPreferredSize(new Dimension (100, 120)); // set size of panel containing buttons
		fileOperations.setMaximumSize(new Dimension (100, 120));
		fileOperations.setLayout(new GridLayout (0, 1));
		fileOperations.add (loadFile); // add buttons
		fileOperations.add (deleteFile);
		fileOperations.add (exportFile);
		fileOperations.add (importFile);

		// Middle of start panel (scrollpane and file operations)
		JPanel midPanel = new JPanel();
		midPanel.setLayout(new BoxLayout (midPanel, BoxLayout.X_AXIS));
		midPanel.setOpaque(false);
		midPanel.add (filesScrollPane); // add scrollpane
		midPanel.add(fileOperations); // add file operations buttons

		JPanel newSimPanel = new JPanel (); // panel to display the "new simulation"
		newSimPanel.setMaximumSize(new Dimension (400, 50)); // button, for format
		newSimPanel.setLayout (new GridLayout (0, 1)); // purposes only
		newSimPanel.setOpaque(false);
		newSimPanel.add (newSim);

		JLabel availableSavesJL = new JLabel(" Available save files:");
		availableSavesJL.setForeground(Color.white);
		availableSavesJL.setFont (font);

		newSimPanel.add(availableSavesJL); // add "available save files" label under the "new simulation" panel

		// Add all to start panel
		JPanel startPanel = new JPanel(); // set up start panel
		startPanel.setPreferredSize(new Dimension (400, 300)); // set size
		startPanel.setMaximumSize(new Dimension (400, 300));
		startPanel.setLayout(new BoxLayout (startPanel, BoxLayout.Y_AXIS));
		startPanel.add(newSimPanel); // add
		startPanel.add (midPanel);
		startPanel.setOpaque (false);

		// set up entire screen
		boxPanel = new JPanel (); // jpanel of the entire screen
		boxPanel.setPreferredSize (new Dimension (700,394)); // set size
		boxPanel.setMinimumSize (new Dimension (700,394));
		boxPanel.setMinimumSize (new Dimension (700,394));
		boxPanel.setLayout(new BoxLayout (boxPanel, BoxLayout.Y_AXIS));
		boxPanel.add(Box.createRigidArea(new Dimension(700, 19))); // spacing
		boxPanel.add (startPanel);	// add new simulation/loading options panel
		boxPanel.add(Box.createRigidArea(new Dimension(700, 13)));
		boxPanel.add (options);
		boxPanel.setOpaque(false);
		boxPanel.revalidate();

		// set boxPanel to glass pane
		setGlassPane (boxPanel);
		this.getGlassPane().revalidate();
		this.getGlassPane().setVisible(true);

		// update
		revalidate();
		repaint();
	}

	/** For use with the introduction or credits screens. Sets
	 * the text in the JScrollPane. 
	 * 
	 * @param textString		the text to be added to the JScrollPane
	 */
	public void setMainText (String textString)
	{
		// Update text
		text.setText(textString);
		text.setCaretPosition(0);
		text.revalidate();

		// update scrollpane
		scrollText = new JScrollPane (text, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollText.setOpaque (false); // reformat the scrollpane
		scrollText.getViewport().setOpaque (false);
		scrollText.setBackground(new Color(0, 0, 0, 0));
		scrollText.getViewport().setBackground(new Color(0, 0, 0, 0));
		scrollText.setBorder (null);
		scrollText.setViewportBorder(null);
		scrollText.getViewport().getInsets().set(0,0,0,0);
		scrollText.revalidate();

		// For formatting purposes, a jpanel to contain the jScrollpane in order to
		// have an empty border between it and the side of the screen
		JPanel scrollTextContainer = new JPanel ();
		scrollTextContainer.setOpaque(false);
		scrollTextContainer.add (scrollText); // add scrollpane
		scrollText.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // add border

		// set up entire screen
		boxPanel = new JPanel (); // jpanel of entire screen
		boxPanel.setLayout(new BoxLayout (boxPanel, BoxLayout.Y_AXIS));
		boxPanel.add(Box.createRigidArea(new Dimension(700, 70))); // spacing
		boxPanel.add (scrollTextContainer);	// add scrollpane
		boxPanel.add(Box.createRigidArea(new Dimension(700, 10))); // spacing
		boxPanel.add (options);
		boxPanel.setOpaque(false);
		boxPanel.revalidate();

		// set boxpanel to glasspane
		setGlassPane (boxPanel);
		this.getGlassPane().revalidate();
		this.getGlassPane().setVisible(true);

		// update
		revalidate();
		repaint();
	}

	/** A class to draw the background onto the JPanel for the JContentPane
	 * of the main menu.
	 * 
	 * @author Sally Hui
	 * @author Jiayin Huang
	 * @author Tony Cui
	 */
	class MainJPanel extends JPanel
	{	
		public MainJPanel ()
		{
			repaint();
		}

		public void paintComponent(Graphics g)
		{
			super.paintComponent (g);
			g.drawImage(mainBgrd, 0, 0, null); // draw image
		}
	}

	// Main method
	public static void main (String [] args)
	{
		// set nimbus look and feel
		try 
		{
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} 
		catch (Exception e)
		{
			// if failed, try default system look and feel
			try
			{
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
			catch (UnsupportedLookAndFeelException e2) 
			{
				e2.printStackTrace();
			} 
			catch (ClassNotFoundException e2) 
			{
				e2.printStackTrace();
			} 
			catch (InstantiationException e2) 
			{
				e2.printStackTrace();
			} 
			catch (IllegalAccessException e2) 
			{
				e2.printStackTrace();
			}
		}

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

		// Register body text font	
		try 
		{
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("fonts/weblysleekuil.ttf")));
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("fonts/MadokaMusical.ttf")));
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("fonts/MadokaRunes-2.0.ttf")));
		} 
		catch (IOException e) 
		{
			System.out.println ("Missing font file");
			e.printStackTrace();
		}
		catch (FontFormatException e)
		{
			System.out.println ("Missing font file");
			e.printStackTrace();
		}

		// new window
		MainMenu window = new MainMenu (); 
		window.setVisible (true);
	}

}
