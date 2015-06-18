package lcs;
import java.awt.Color;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public class LcsConsole extends JPanel implements Runnable
{

	private static final long serialVersionUID = -1548491808708416263L;

	public static final int MESSAGE_EVENT = 0;
	public static final int MESSAGE_ERROR = 1;
	public static final int MESSAGE_SYSTEM = 2;

	private static SimpleDateFormat dateFormat;
	private static JTextPane display;
	private static JScrollPane scrollPane;
	
	private int offset = 0;
	
	private boolean showConsole;
	private boolean hideConsole;
	private boolean consoleVisible;
	private boolean isMoving;
	
	private final int consoleHeight = 300;
	private final int consoleHiddenViewOffset = 55;
	
	private final int consoleClearance = 20;
	private final Insets frameInsets = JFrame.getFrames()[0].getInsets();
	private final int frameHeight = JFrame.getFrames()[0].getSize().height - frameInsets.top - frameInsets.bottom;
	private final int frameWidth = JFrame.getFrames()[0].getSize().width - frameInsets.left - frameInsets.right;
	private final int consoleWidth = frameWidth - frameInsets.left - frameInsets.right - consoleClearance * 2;
	private final int consoleYpos = frameHeight - consoleHiddenViewOffset + frameInsets.top + frameInsets.bottom;
	
	public LcsConsole()
	{
		dateFormat = new SimpleDateFormat("dd-MM-yy HH:mm:ss.SSS");
		display = new JTextPane();

		display.addMouseListener(new MouseListener()
		{
			public void mouseClicked(MouseEvent e)
			{
				if (!isMoving)
				{
					if (isConsoleVisible())
					{
						hideConsole();
					}
					else
					{
						showConsole();
					}
				}
			}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
		});
		scrollPane = new JScrollPane();
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.getViewport().add(display);
		add(scrollPane);
		setBounds(consoleClearance, consoleYpos ,consoleWidth, consoleHeight);
		scrollPane.setPreferredSize(getBounds().getSize());
	}

	public synchronized void showConsole()
	{
		showConsole = true;
		hideConsole = false;
		consoleVisible = true;
	}
	
	public synchronized void hideConsole()
	{
		hideConsole = true;
		showConsole = false;	
		consoleVisible = false;
	}
	
	public synchronized static void addEntry(String entry, int level)
	{
		display.setEditable(true);
		String dateString = dateFormat.format(new Date());
		Color messageColor = Color.BLACK;
		StyleContext sc = StyleContext.getDefaultStyleContext();
		AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, messageColor);
		aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Bold, true);
		display.setCaretPosition(0);
		display.setCharacterAttributes(aset, true);	
		display.replaceSelection(dateString + "  -  ");
		
		switch (level)
		{
		case LcsConsole.MESSAGE_EVENT:
			messageColor = Color.ORANGE;
			break;
		case LcsConsole.MESSAGE_SYSTEM:
			messageColor = new Color(38, 163, 38);
			break;
		case LcsConsole.MESSAGE_ERROR:
			messageColor = Color.RED;
			break;
		default:
			messageColor = Color.BLACK;
			break;
		}
		
		aset = sc.addAttribute(SimpleAttributeSet.EMPTY,StyleConstants.Foreground, messageColor);
		display.setCharacterAttributes(aset, true);
		display.setCaretPosition(dateString.length() + 5);
		display.replaceSelection(entry + "\n");
		display.setEditable(false);
	}
	
	public boolean isConsoleVisible()
	{
		return consoleVisible;
	}
	
	public void run()
	{
		while (true)
		{
			if (showConsole)
			{
				while ((consoleHeight + offset) / 8 != 0)
				{
					offset -= (consoleHeight + offset) / 8;
					setBounds(consoleClearance, consoleYpos - consoleHeight + Math.abs(300 - (0 - offset)) + consoleHiddenViewOffset, consoleWidth, consoleHeight);
					try
					{
						Thread.sleep(30);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
					isMoving = true;
				}
				isMoving = false;
				showConsole = false;
				offset = 0;
			}
			if (hideConsole)
			{
				while ((consoleHeight - offset) / 8 != 0)
				{
					offset += (consoleHeight - offset) / 8;
					setBounds(consoleClearance, consoleYpos - consoleHeight + Math.abs(offset) + 8, consoleWidth, consoleHeight);
					try
					{
						Thread.sleep(30);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
					isMoving = true;
				}
				isMoving = false;
				hideConsole = false;
				offset = 0;
			}
			
			try
			{
				Thread.sleep(50);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
}
