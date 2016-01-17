import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;

public class GUI extends JFrame 
{
	Viewport vp = new Viewport ();
	
	public static void main (String[] args)
	{
		new GUI ("Test - W: forwards, S: backwards, A: turn left, D: turn right, Q: left, E: right", 640, 480);
	}
	
	public GUI (String title, int width, int height)
	{
		super (title);		
		
		setContentPane (vp);
		
		setVisible (true);
		setSize (width, height);
		setDefaultCloseOperation (EXIT_ON_CLOSE);		
		
		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new MyDispatcher());
	}		
	
	private class MyDispatcher implements KeyEventDispatcher 
	{
        @Override
        public boolean dispatchKeyEvent(KeyEvent e) 
        {
            vp.inputKey (e);            
            return false;
        }
    }
}
