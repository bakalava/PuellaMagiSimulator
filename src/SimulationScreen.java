import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Paint;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/** A class that displays the main JFrame of the simulation and the GUI 
 * elements associated with it.
 * 
 * @author Sally Hui
 * @author Jiayin Huang
 * @author Tony Cui
 */
public class SimulationScreen extends JFrame implements WindowListener, ActionListener
{
	private static final int NOTHING_SELECTED = 0;
	private static final int SETTINGS_SELECTED  = 1;
	private static final int STATS_SELECTED = 2;
	private static final int ZOOM_SELECTED = 3;
	private static final int LEGEND_SELECTED = 4;
	private static final int LEGACY_SELECTED = 5;
	private static final int HELP_SELECTED = 10;

	private CustomButton[] addEntity = new CustomButton [5];
	private CustomButton[] tools = new CustomButton [9];
	private BufferedImage iconImg;
	private Color blueButton = new Color (196, 215, 248);
	private Color selected = new Color (255, 108, 108);

	private Viewport vp;

	private String simName = "Untitled Simulation";
	private JTextField inputSimName = new JTextField (simName, 15);

	private JPanel middlePanel = new JPanel ();
	private JPanel glassPanel = new JPanel ();
	private Help helpPanel = new Help ();
	private Legend legendPanel = new Legend ();
	private Settings settingsPanel = new Settings ();
	private Stats statsPanel = new Stats ();
	private Legacy legacyPanel;
	private Zoom zoomPanel;		

	private int glassToolSelected = 0;	
	private JPanel rightToolBar = new JPanel ();

	/** The timer for advancing the simulation. Default is 30 ms delay. 
	 */
	public Timer timer = new Timer (30, this);
	private Spacetime spacetime;

	/** Creates a new SimulationScreen of the specified dimensions, using 
	 * a newly generated Viewport.
	 * 
	 * @param title		the title of the SimulationScreen JFrame
	 * @param width		the width in pixels of the SimulationScreen JFrame
	 * @param height	the height in pixels of the SimulationScreen JFrame
	 */
	public SimulationScreen (String title, int width, int height, WorldGenerator wg)
	{
		this ("Untitled Simulation    ", title, width, height, new Viewport (new Spacetime (wg)));
	}

	/** Creates a new SimulationScreen of the specified dimensions, using 
	 * a provided Viewport.
	 * 
	 * @param title		the title of the SimulationScreen JFrame
	 * @param width		the width in pixels of the SimulationScreen JFrame
	 * @param height	the height in pixels of the SimulationScreen JFrame
	 * @param port		the viewport which will be displayed in the SimulationScreen
	 */
	public SimulationScreen (String simulationName, String title, int width, int height, Viewport port)
	{		
		simName = simulationName.substring (0, simulationName.length() - 4);
		inputSimName = new JTextField (simName, 15);

		setLookAndFeel (true);

		// Import Image(s)
		try
		{
			iconImg = ImageIO.read (new File("images/iconImg.png")); //TODO: getResource is confusing
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		// Set up window
		setTitle(title);	
		vp = port; // set viewport	
		spacetime = port.grid;
		zoomPanel = new Zoom(spacetime); //create new zoom panel here
		legacyPanel = new Legacy(spacetime);
		vp.addMouseMotionListener(new ZoomPanelMouseListener());
		vp.addMouseListener(new StatsPanelMouseListener());

		setPreferredSize(new Dimension (width, height)); // set size
		setMinimumSize(new Dimension (width, height));
		setLocationRelativeTo(null); // open in middle of screen
		this.getContentPane().setLayout(new BorderLayout());  
		setIconImage (iconImg);
		addWindowListener (this);
		setContentPane (vp);

		// Set up buttons
		tools [0] = new CustomButton ("Save", blueButton);
		tools [1] = new CustomButton ("Help", blueButton);
		tools [2] = new CustomButton ("Select", blueButton);
		tools [3] = new CustomButton ("Kidnap", blueButton);		
		tools [4] = new CustomButton ("Explore", blueButton);
		tools [5] = new CustomButton ("Delete", blueButton);	
		tools [6] = new CustomButton ("Play", blueButton);
		tools [7] = new CustomButton ("Advance", blueButton);
		tools [8] = new CustomButton ("Exit", blueButton);

		setExploring(true);

		addEntity [0] = new CustomButton ("Human", Color.pink);
		addEntity [1] = new CustomButton ("Incubator", Color.pink);
		addEntity [2] = new CustomButton ("Puella", Color.pink);
		addEntity [3] = new CustomButton ("Witch", Color.pink);
		addEntity [4] = new CustomButton ("Deselect", Color.red);

		// Add action listeners
		inputSimName.addActionListener (this);
		for (int i = 0; i < tools.length; i++)
			tools [i].addActionListener(this);
		for (int i = 0; i < addEntity.length; i++)
			addEntity[i].addActionListener(this);

		// Set up glass pane
		glassPanel.setLayout (new BorderLayout());
		glassPanel.setOpaque(false);		

		// set up lower toolbar of JButtons
		JPanel lowerToolBar = new JPanel ();
		lowerToolBar.setLayout (new FlowLayout (FlowLayout.RIGHT, 3, 3)); // align to right of the JFrame
		lowerToolBar.setOpaque(false);
		lowerToolBar.add (inputSimName);
		for (int i = 0; i < addEntity.length; i++) // add buttons
			lowerToolBar.add(addEntity [i]);

		// set up right toolbar of JButtons
		rightToolBar.setPreferredSize (new Dimension (70, 200));
		rightToolBar.setLayout(new FlowLayout (FlowLayout.CENTER, 3, 3));
		rightToolBar.setOpaque (false);
		for (int i = 0; i < tools.length; i++) // add buttons
			rightToolBar.add(tools [i]);

		// middle of screen
		middlePanel.setOpaque(false);		

		// add all to glasspanel JPanel
		glassPanel.add(middlePanel, BorderLayout.CENTER);
		glassPanel.add (lowerToolBar, BorderLayout.SOUTH);
		glassPanel.add (rightToolBar, BorderLayout.EAST);	

		// set glass pane
		setGlassPane (glassPanel);
		getGlassPane().setVisible (true);

		// set up key event dispatcher
		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		manager.addKeyEventDispatcher(new MyDispatcher());	

		// start
		startTimer();
	}


	/** A class for the Spacetime panel that 
	 * displays the legend, listing what each shape
	 * color represent. This pops up whenever "4" is pressed.
	 * 
	 * @author Sally Hui
	 * @author Jiayin Huang
	 * @author Tony Cui
	 */
	private class Legend extends JPanel 
	{
		public Legend ()
		{
			// set up JPanel
			setOpaque (false); // set transparent
			setBackground(new Color(0,0,0,0));
			setPreferredSize (new Dimension(200, 300)); // set size
			setMinimumSize (new Dimension(200, 300));
			setMaximumSize (new Dimension(200, 300));
		}

		// paints gradient
		public void paintComponent (Graphics g)
		{

			// set up gradient
			Paint p = new GradientPaint(0.0f, getHeight(), new Color(240, 240, 240, 0), 0.0f, 0.0f, new Color(240, 240, 240, 255), true);

			// cast graphics object back to graphics2d object
			Graphics2D g2d = (Graphics2D)g;

			// paint gradient
			g2d.setPaint(p);
			g2d.fillRect(0, 0, getWidth(), getHeight());

			Rectangle2D human = new Rectangle2D.Double(10, 10, 16, 16); // male human
			g2d.setColor(new Color(128, 64, 0));
			g2d.fill(human);

			Rectangle2D human2 = new Rectangle2D.Double(10, 40, 16, 16); // female human
			g2d.setColor(Color.RED);
			g2d.fill(human2);

			Rectangle2D puella = new Rectangle2D.Double(10, 70, 19, 19);
			g2d.setColor(new Color(255, 0, 128)); // puella
			g2d.fill (puella);

			Gear gear = new Gear (19, 110, 10, 5, 2);
			g.setColor(Color.BLACK);
			gear.draw (g2d);

			GriefSeed gf = new GriefSeed (15, 138);
			g.setColor(Color.BLACK);
			gf.draw (g2d);

			Witch witch = new Witch (14, 158);
			g2d.setColor(new Color(0, 0, 64));
			witch.draw (g2d);

			Ellipse2D incubator = new Ellipse2D.Double (12, 175, 5, 5);	
			g2d.setColor(Color.white);
			g2d.fill(incubator);
			g2d.setColor(Color.black);
			g2d.draw(incubator);		

			// Draw labels

			g.drawString ("Male Human", 40, 22);
			g.drawString ("Female Human", 40, 52);
			g.drawString ("Puella Magi", 40, 82);
			g.drawString ("Walpurgisnacht", 40, 114);
			g.drawString ("Grief Seed", 40, 142);
			g.drawString ("Witch", 40, 163);
			g.drawString ("Incubator", 40, 182);			
		}
	}

	/** A class of the JScrollpane settings panel that pops up when "1" is
	 * pressed. It displays GUI elements for the user to manipulate the
	 * settings for their simulation.
	 * 
	 * @author Sally Hui
	 * @author Jiayin Huang
	 * @author Tony Cui
	 */
	private class Settings extends JScrollPane implements ActionListener, ChangeListener
	{
		private JSlider timerSpeedSlider = new JSlider (1, 10, 5);

		/** Creates a new Settings JScrollpane.
		 */
		public Settings ()
		{
			// set size
			setPreferredSize (new Dimension(220, 50));

			// set transparent
			setOpaque (false);
			getViewport().setOpaque (false);
			getViewport().setBackground(new Color(0, 0, 0, 0));
			setViewportBorder(null);
			setBorder (null);
			getViewport().getInsets().set(0,0,0,0);
			setBackground(new Color(0,0,0,0));

			// set up scrolling settings
			setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

			// add settings
			JPanel settingsPanel = new JPanel (); // settings panel to contain settings
			settingsPanel.setOpaque (false);
			settingsPanel.setLayout(new BoxLayout (settingsPanel, BoxLayout.Y_AXIS));

			timerSpeedSlider.addChangeListener(this);

			JPanel timerSpeedLabelPanel = new JPanel ();
			timerSpeedLabelPanel.add (new JLabel ("Timer Speed"));
			timerSpeedLabelPanel.setOpaque (false);

			settingsPanel.add (timerSpeedLabelPanel);
			settingsPanel.add (timerSpeedSlider);

			getViewport().add (settingsPanel); // add JPanel of settings to scrollpane
		}

		// paints gradient
		public void paintComponent (Graphics g)
		{
			// set up gradient
			Paint p = new GradientPaint(0.0f, getHeight(), new Color(240, 240, 240, 0), 0.0f, 0.0f, new Color(240, 240, 240, 255), true);

			// cast graphics object back to graphics2d object
			Graphics2D g2d = (Graphics2D)g;

			g2d.setPaint(p);

			// paint gradient
			g2d.fillRect(0, 0, getWidth(), getHeight());
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {	
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			Object source = (Object) e.getSource();
			if (source.equals (timerSpeedSlider))
				timer.setDelay (110 - 10 * timerSpeedSlider.getValue());

		}
	}

	/** A class of the statistics panel that pops up when 2 is pressed.
	 * It displays statistics about an entity currently moused over.
	 * 
	 * @author Sally Hui
	 * @author Jiayin Huang
	 * @author Tony Cui
	 */
	private class Stats extends JPanel
	{
		private EntityStalker stalker;
		private JTextArea title;
		private JTextArea text;
		/** Creates a new statistics JPanel.
		 */
		public Stats ()
		{
			// set up JPanel
			getContentPane().setLayout(new BorderLayout());
			setOpaque (false); // set transparent
			setBackground(new Color(0,0,0,0));
			setPreferredSize (new Dimension(200, 300)); // set size
			setMinimumSize (new Dimension(200, 300));
			setMaximumSize (new Dimension(200, 300));

			// Add statistics text
			text = new JTextArea ("Select an Entity to view stats.");
			text.setRows (15); // format JTextArea
			text.setColumns(15);
			text.setEditable (false);
			text.setOpaque (false);
			text.setLineWrap (true);
			text.setBackground(new Color(0, 0, 0, 0));
			text.setCaretPosition(0); 
			text.setBorder (null);

			title = new JTextArea (" ");
			title.setColumns(15);
			title.setRows(1);
			title.setFont(new Font ("MadokaRunes", Font.BOLD, 25));
			title.setEditable(false);
			title.setOpaque(false);
			title.setLineWrap(true);
			title.setBackground(new Color(0, 0, 0, 0));
			title.setCaretPosition(0); 
			title.setBorder (null);

			add (text, BorderLayout.CENTER);
			add (title, BorderLayout.NORTH);
		}

		// paints gradient
		public void paintComponent (Graphics g)
		{
			// set up gradient
			Paint p = new GradientPaint(0.0f, getHeight(), new Color(240, 240, 240, 0), 0.0f, 0.0f, new Color(240, 240, 240, 255), true);

			// cast graphics object back to graphics2d object
			Graphics2D g2d = (Graphics2D)g;

			// paint gradient
			g2d.setPaint(p);
			g2d.fillRect(0, 0, getWidth(), getHeight());
		}

		public void refreshStats ()
		{
			if (stalker != null)
				text.setText(stalker.toString());
			text.revalidate();
			title.revalidate();
			repaint();
		}

		public void updateTarget (Entity newTarget)
		{				
			if (newTarget != null)
			{
				stalker = new EntityStalker (newTarget);							
				title.setText(newTarget.toString());
				refreshStats();
			}			
		}				
	}

	/** A class of the zoom panel that pops up whenever "3" is pressed. It displays
	 * a zoomed in view of the area the mouse is over.
	 * 
	 * @author Sally Hui
	 * @author Jiayin Huang
	 * @author Tony Cui
	 */
	private class Zoom extends Viewport
	{
		/** Creates a new zoom Viewport.
		 */
		public Zoom (Spacetime spacetime)
		{
			super(spacetime);
			setOpaque (true); // set transparent
			setBackground(new Color(0,0,0,0));
			setPreferredSize (new Dimension(200, 200)); // set size
			setMinimumSize (new Dimension(200, 200));
			setMaximumSize (new Dimension(200, 200));			
			repaint();
		}

		// draw zoom area
		public void paintComponent (Graphics g)
		{		
			zoom = vp.zoom * 3;
			centerAt(vp.convertPointMouseToGrid(mouse));
			Graphics2D g2d = (Graphics2D)g;								
			grid.draw(this, g2d);
			g.setColor (Color.black);
			g.drawRect(0, 0, getWidth(), getHeight());
		}

		public void show (Point mouse)
		{		
			this.mouse = mouse;			
			revalidate();
			repaint();
		}				
	}

	/** Mouse listener to update to zoom panel whenever the mouse is moved.
	 */
	private class ZoomPanelMouseListener extends MouseAdapter
	{       
		public void mouseMoved (MouseEvent e)
		{                   
			zoomPanel.show(e.getPoint());     
		}
	}

	/** A mouse listener that updates the stats panel whenever
	 * a new Entity selection is made. 
	 * 
	 * @author Sally Hui
	 * @author Jiayin Huang
	 * @author Tony Cui
	 */
	private class StatsPanelMouseListener extends MouseAdapter
	{
		public void mouseReleased (MouseEvent e)
		{
			ArrayList<Entity> selection = vp.cursorEntity;
			Entity choice;

			if (selection.size() == 0)
			{
				Point2D location = vp.convertPointMouseToGrid(e.getPoint());
				choice = vp.grid.getEntity(location.getX(), location.getY());				
			}
			else
			{
				choice = selection.get(0);
			}

			statsPanel.updateTarget(choice);
		}
	}	

	/** A class for the Spacetime legacy stats panel that 
	 * pops up whenever "5" is pressed.
	 * 
	 * @author Sally Hui
	 * @author Jiayin Huang
	 * @author Tony Cui
	 */
	private class Legacy extends JPanel
	{
		private Statistics data;		
		private JTextArea text;

		/** Creates a new legacy stats JPanel.
		 */
		public Legacy (Spacetime spacetime)
		{
			// set up JPanel			
			setOpaque (false); // set transparent
			setBackground(new Color(0,0,0,0));
			setPreferredSize (new Dimension(200, 300)); // set size
			setMinimumSize (new Dimension(200, 300));
			setMaximumSize (new Dimension(200, 300));

			// Add statistics text	
			text = new JTextArea ();
			text.setRows (15); // format JTextArea
			text.setColumns(15);
			text.setEditable (false);
			text.setOpaque (false);
			text.setLineWrap (true);
			text.setBackground(new Color(0, 0, 0, 0));
			text.setCaretPosition(0); 
			text.setBorder (null);
			text.setFont(new Font("Arial", Font.PLAIN, 12));

			add (text);		

			// Set up Statistics object
			data = new Statistics (spacetime);
		}

		// paints gradient
		public void paintComponent (Graphics g)
		{
			// set up gradient
			Paint p = new GradientPaint(0.0f, getHeight(), new Color(240, 240, 240, 0), 0.0f, 0.0f, new Color(240, 240, 240, 255), true);

			// cast graphics object back to graphics2d object
			Graphics2D g2d = (Graphics2D)g;

			// paint gradient
			g2d.setPaint(p);
			g2d.fillRect(0, 0, getWidth(), getHeight());
		}

		public void refreshStats ()
		{
			data.update();
			if (data != null)
				text.setText(data.toString());
			text.revalidate();			
			repaint();
		}
	}

	/** A class for the help panel that pops up whenever "0" is pressed.
	 * 
	 * @author Sally Hui
	 * @author Jiayin Huang
	 * @author Tony Cui
	 */
	private class Help extends JPanel
	{		
		private JTextArea text;

		public Help ()
		{
			// set up JPanel

			setOpaque (false); // set transparent
			setBackground(new Color(0,0,0,0));
			setPreferredSize (new Dimension(200, 300)); // set size
			setMinimumSize (new Dimension(200, 300));
			setMaximumSize (new Dimension(200, 300));

			// Add statistics text
			text = new JTextArea ("Shortcut keys:\n"
					+ "1: Settings\n"
					+ "2: Stats\n"
					+ "3: Zoom\n"
					+ "4: Legend\n"
					+ "5: Legacy stats\n"
					+ "0: This help index\n"
					+ "SHIFT: hold to select\n"
					+ "CTRL: hold to lock location\n"
					+ "ESC: push to toggle timer");

			text.setRows (15); // format JTextArea
			text.setColumns(15);
			text.setEditable (false);
			text.setOpaque (false);
			text.setLineWrap (true);
			text.setBackground(new Color(0, 0, 0, 0));
			text.setCaretPosition(0); 
			text.setBorder (null);
			add (text);			
		}

		// paints gradient
		public void paintComponent (Graphics g)
		{
			// set up gradient
			Paint p = new GradientPaint(0.0f, getHeight(), new Color(240, 240, 240, 0), 0.0f, 0.0f, new Color(240, 240, 240, 255), true);

			// cast graphics object back to graphics2d object
			Graphics2D g2d = (Graphics2D)g;

			// paint gradient
			g2d.setPaint(p);
			g2d.fillRect(0, 0, getWidth(), getHeight());
		}
	}

	/** Deals with what happens when the user turns on one of the tools (settings,
	 * statistics, or zoom).
	 * 
	 * @param tool		which tool was selected (1 - settings, 2 - statistics, 3 - zoom)
	 */
	public void selectTool (int tool)
	{
		//  clear the middle panel and reformat
		middlePanel.removeAll();
		middlePanel = new JPanel ();
		middlePanel.setPreferredSize (new Dimension(650, 300));
		middlePanel.setMinimumSize (new Dimension(650, 300));
		middlePanel.setLayout (new FlowLayout (FlowLayout.LEFT));
		middlePanel.setOpaque(false);

		if (glassToolSelected != tool) // if the user didn't press the same button 
			// twice, in which case the tool would close
		{
			glassToolSelected = tool; // update glassToolSelected
			if (tool == SETTINGS_SELECTED) // pressed 1
			{
				// here: update settings	
				settingsPanel.revalidate();
				middlePanel.add (settingsPanel);
			}

			else if (tool == STATS_SELECTED) // pressed 2
			{
				// stats stuff
				statsPanel.revalidate();
				middlePanel.add (statsPanel);
			}

			else if (tool == ZOOM_SELECTED) // pressed 3
			{
				// zoom stuff
				zoomPanel.revalidate();
				middlePanel.add (zoomPanel);
			}
			else if (tool == LEGEND_SELECTED) // pressed 4
			{
				// legend stuff
				legendPanel.revalidate();
				middlePanel.add (legendPanel);
			}
			else if (tool == LEGACY_SELECTED) // pressed 5
			{
				// legacy stats stuff
				legacyPanel.revalidate();
				middlePanel.add(legacyPanel);
			}
			else if (tool == HELP_SELECTED) // pressed 0
			{
				// help and shortcut index
				helpPanel.revalidate();
				middlePanel.add(helpPanel);
			}
		}
		else
		{
			glassToolSelected = NOTHING_SELECTED; // closed tool; nothing selected
		}

		// update screen
		middlePanel.revalidate();
		glassPanel.add(middlePanel, BorderLayout.CENTER);
		glassPanel.revalidate();
		getGlassPane().revalidate();
		repaint();
	}

	/** Listens to the keyboard to open and close tools
	 * 
	 * @param e 	the key event fired
	 */
	public void inputKey (KeyEvent e)
	{
		char ch = Character.toLowerCase (e.getKeyChar());	

		if (e.getID() == KeyEvent.KEY_PRESSED)
		{		
			// User-controlled ant movements, for debugging purposes only	

			if (ch == '1')
				selectTool (SETTINGS_SELECTED);
			else if (ch == '2')
				selectTool (STATS_SELECTED);
			else if (ch == '3')
				selectTool (ZOOM_SELECTED);
			else if (ch == '4')
				selectTool (LEGEND_SELECTED);
			else if (ch == '5')
				selectTool (LEGACY_SELECTED);
			else if (ch == '0')
				selectTool (HELP_SELECTED);
			else if (e.getKeyCode() == KeyEvent.VK_SHIFT)
				setSelecting(true);	
			else if (e.getKeyCode() == KeyEvent.VK_CONTROL)
				setExploring(false);	
			else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
			{
				if (timer.isRunning())
					stopTimer();
				else
					startTimer();
			}
			repaint();
		}
		else if (e.getID() == KeyEvent.KEY_RELEASED)
		{	
			if (e.getKeyCode() == KeyEvent.VK_SHIFT)
			{
				setSelecting(false);
				setExploring(true);
			}
			else if (e.getKeyCode() == KeyEvent.VK_CONTROL)
				setExploring(true);
			repaint();
		}
		else if (e.getID() == KeyEvent.KEY_TYPED)
		{			
		}		
	}

	/**	Dispatches key events which are received by the inputKey method.
	 * 
	 * @author Sally Hui
	 * @author Jiayin Huang
	 * @author Tony Cui
	 */
	private class MyDispatcher implements KeyEventDispatcher 
	{
		@Override
		public boolean dispatchKeyEvent(KeyEvent e) 
		{
			inputKey (e);            
			return false;
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource ();
		if (source.equals (inputSimName))
		{
			simName = inputSimName.getText();			
			this.requestFocus ();
		}
		else if (source.equals(timer))
		{
			vp.advanceVP();			
			statsPanel.refreshStats();
			legacyPanel.refreshStats();
		}	
		else if (source.equals(tools [0]))
		{
			stopTimer();
			serialize();
			startTimer();
		}
		else if (source.equals(tools [1]))
		{
			selectTool (HELP_SELECTED);
		}
		else if (source.equals(tools [2]))
		{
			if (tools[2].getText().equals("Select"))
				setSelecting (true);
			else
				setSelecting (false);
		}
		else if (source.equals (tools [3]))
		{
			if (tools[3].getText().equals("Kidnap"))
				setKidnapping(true);
			else
				setKidnapping(false);				
		}
		else if (source.equals(tools [4]))
		{
			if (tools[4].getText().equals("Explore"))
				setExploring(true);
			else			
				setExploring(false);	

		}
		else if (source.equals (tools [5]))
		{
			if (tools[5].getText().equals("Delete"))
				setDeleting(true);
			else
				setDeleting(false);
		}
		else if (source.equals (tools [6]))
		{
			if (tools[6].getText().equals("Play"))			
				startTimer();							
			else			
				stopTimer();
		}
		else if (source.equals (tools [7]))
		{
			vp.advanceVP();
			statsPanel.refreshStats();
			legacyPanel.refreshStats();
		}		
		else if (source.equals (tools[8]))
		{
			windowClosing (new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
		}
		else if (source.equals (addEntity [0]))
		{
			vp.setAddEntityType (0);
			setAdding(true);
		}
		else if (source.equals (addEntity[1]))
		{
			vp.setAddEntityType (1);
			setAdding(true);
		}
		else if (source.equals (addEntity [2]))
		{
			vp.setAddEntityType (2);
			setAdding(true);
		}
		else if (source.equals(addEntity [3]))
		{
			vp.setAddEntityType (3);
			setAdding(true);
		}
		else if (source.equals(addEntity[4]))
		{
			setAdding(false);
		}
	}		

	/** Saves/serializes the Viewport object. Files created can be deserialized
	 * by the deserialize method in the MainMenu class.
	 */
	public void serialize ()
	{
		File path = new File ("./saves/" + simName + ".sav");
		boolean exists = path.exists();
		try
		{ // saves in "saves" folder of project folder                               
			FileOutputStream fout = new FileOutputStream(path);

			if (exists) // file (or something with the same name) exists in that directory
			{
				// ask if user wants to save over
				int event = JOptionPane.showConfirmDialog(null, "The file you are trying to write already exists. Would you like to overwrite the file?", "Save", JOptionPane.YES_NO_OPTION);
				if (event == JOptionPane.YES_OPTION) // yep! 
				{
					ObjectOutputStream oos = new ObjectOutputStream(fout);   
					oos.writeObject(vp); // serialize viewport
					oos.close();
					System.out.println("Done saving.");  
				}
			}    

			else 
			{
				ObjectOutputStream oos = new ObjectOutputStream(fout);   
				oos.writeObject(vp); // serialize viewport
				oos.close();
				System.out.println("Done saving.");  
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/** Either sets the LookAndFeel for the UIManager to the 
	 * default, or to Nimbus. In the program, Nimbus is used for
	 * the menus, while the default is used for in-game.
	 * 
	 * @param isDefault		true will set LookAndFeel to default
	 */
	public void setLookAndFeel (boolean isDefault)
	{
		if (isDefault) // Set default LookAndFeel
		{
			try
			{
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
			catch (Exception e2) 
			{		
			}
		}
		else // Set Nimbus LookAndFeel
		{
			try 
			{
				for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
					if ("Nimbus".equals(info.getName())) {
						UIManager.setLookAndFeel(info.getClassName());
						break;
					}
				}
			} 
			catch (Exception e2)
			{				
			}
		}
	}

	/** Sets adding mode in the Viewport.
	 * 
	 * @param mode	true means that clicking will add an Entity
	 */
	public void setAdding (boolean mode)
	{
		if (mode)
		{
			setDeleting(false);
			setKidnapping(false);
			vp.setIsAddingEntities(true);			
		}
		else
		{
			vp.setIsAddingEntities(false);
		}
	}

	/** Sets deleting mode in the Viewport.
	 * 
	 * @param mode	true means that clicking will delete an Entity
	 */
	public void setDeleting (boolean mode)
	{
		if (mode)
		{
			setKidnapping(false);
			tools[5].setText("!Delete");
			tools[5].setBackground(selected);
			vp.setIsDeletingEntities (true);			
		}
		else
		{
			tools[5].setText("Delete");
			tools[5].setBackground(blueButton);
			vp.setIsDeletingEntities (false);
		}
	}

	/** Sets navigation mode in the Viewport.
	 * 
	 * @param mode	true means clicking and dragging will move
	 * 			the Viewport around the Spacetime grid
	 */
	public void setExploring (boolean mode)
	{
		if (mode)
		{
			setSelecting(false);
			tools[4].setText("!Explore");
			tools[4].setBackground(selected);
			vp.setIsNavigating(true);
		}
		else
		{
			tools[4].setText("Explore");
			tools[4].setBackground(blueButton);
			vp.setIsNavigating(false);
		}
	}

	/** Toggles entity moving mode in the Viewport.
	 * 
	 * @param mode	true means clicking will either pick up an Entity
	 * 			or drop off an Entity
	 */
	public void setKidnapping (boolean mode)
	{
		if (mode)
		{
			setDeleting(false);
			tools[3].setText("!Kidnap");
			tools[3].setBackground(selected);
			vp.setMoveEntity(true);			
		}
		else
		{
			tools[3].setText("Kidnap");
			tools[3].setBackground(blueButton);
			vp.setMoveEntity(false);	
		}
	}

	/** Sets selection mode in the Viewport.
	 * 
	 * @param mode	true means clicking will either pick up an Entity
	 * 			or drop off an Entity
	 */
	public void setSelecting (boolean mode)
	{
		if (mode)
		{
			setExploring(false);
			tools[2].setText("!Select");
			tools[2].setBackground(selected);
			vp.setIsSelectingEntities(true);
		}
		else
		{
			tools[2].setText("Select");
			tools[2].setBackground(blueButton);
			vp.setIsSelectingEntities(false);
		}
	}

	/** Starts the timer if it is not already running. 
	 */
	public void startTimer ()
	{	
		timer.start();	
		tools[6].setText("Pause");
		tools[6].setBackground(blueButton);
	}

	/** Stops the timer. 
	 */
	public void stopTimer ()
	{
		timer.stop();
		tools[6].setText("Play");
		tools[6].setBackground(selected);
	}	

	/** Revalidates and repaints the simulation screen.
	 */
	public void updateRepaint ()
	{
		revalidate();
		repaint();
	}

	/** A class to create customized JButtons.
	 * 
	 * @author Sally Hui
	 * @author Jiayin Huang
	 * @author Tony Cui
	 */
	private class CustomButton extends JButton 
	{
		private String buttonText;

		/** Creates a new custom button.
		 * 
		 * @param btnTxt	the text to be added to the button
		 * @param btnClr	the colour of the button
		 */
		public CustomButton (String btnTxt, Color btnClr)
		{
			super(); // call JButton constructor
			buttonText = btnTxt;
			setBackground (btnClr); // set colour
			// set font
			Font font = new Font("WeblySleek UI Light", Font.PLAIN, 12); 
			setFont(font);
			setText (btnTxt);

			repaint();
		}
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosed(WindowEvent arg0) {

	}

	/** Sets the LookAndFeel to Nimbus, stops the timer
	 * for advancing the Spacetime grid, and reopens the
	 * Main Menu.
	 */
	@Override
	public void windowClosing(WindowEvent arg0) {
		setLookAndFeel(false);
		MainMenu backToStart = new MainMenu ();
		backToStart.setVisible(true);
		stopTimer();
		this.dispose();
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}
}
