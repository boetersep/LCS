package lcs;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import k8061.K8061;

public class LcsLayout extends JFrame
{
	private static final long serialVersionUID = 1365299028017888295L;
	private Container contentPane;
	private Hashtable<JButton, Boolean> buttonStatus;
	private LcsConsole lcsConsoleObj;
	public static final String TEXT_ON = " AAN";
	public static final String TEXT_OFF = " UIT";
	private int componentWidth;
	private int componentHeight;
	
	public LcsLayout()
	{
		

		setCursor(Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon(new byte[]{}).getImage(),new Point(), "empty"));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(Toolkit.getDefaultToolkit().getScreenSize());
		setExtendedState(getExtendedState()|JFrame.MAXIMIZED_BOTH);
		setVisible(true);
		contentPane = getContentPane();
		contentPane.setLayout(null);
		
		lcsConsoleObj = new LcsConsole();
		contentPane.add(lcsConsoleObj);
		lcsConsoleObj.setPreferredSize(getSize());
		Thread t = new Thread(lcsConsoleObj);
		t.start();
		setVisible(true);
		buttonStatus = new Hashtable<JButton, Boolean>();
		
		LcsConsole.addEntry(LcsEngine.versionString, LcsConsole.MESSAGE_SYSTEM);	
	}
	
	@Override
	public void setBackground(Color c) {
		getContentPane().setBackground(c);
		super.setBackground(c);
	}
	
	
	public Component addComponent(final LcsInput lcsInputObj)
	{
		Component returnComp = null;
		if (lcsInputObj.isVisible())
		{
			if (lcsInputObj.getType() == LcsInput.TYPE_BUTTON)
			{
				final JButton comp = new JButton(lcsInputObj.getTxt() + TEXT_ON);
				buttonStatus.put(comp, false);
				comp.setToolTipText(lcsInputObj.getTxt());
				comp.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						JButton source = (JButton) e.getSource();
						if (buttonStatus.get((JButton)e.getSource()))
						{
							buttonStatus.put((JButton)e.getSource(), false);
							source.setText(lcsInputObj.getTxt() + TEXT_ON);
						}
						else if (!buttonStatus.get((JButton)e.getSource()))
						{
							buttonStatus.put((JButton)e.getSource(), true);
							source.setText(lcsInputObj.getTxt() + TEXT_OFF);
						}
						
						LcsEngine.executeAction(lcsInputObj.getAction(), buttonStatus.get((JButton)e.getSource()));
					}
				});
				
				
				returnComp = contentPane.add(comp);
				comp.setBounds(lcsInputObj.getXPos(), lcsInputObj.getYPos(), componentWidth, componentHeight);
				comp.setFont(new Font("Arial",Font.PLAIN, 20));
			}
			else if (lcsInputObj.getType() == LcsInput.TYPE_SLIDER)
			{
				final JSlider comp = new JSlider();
				comp.setBounds(lcsInputObj.getXPos(), lcsInputObj.getYPos(), componentWidth, componentHeight);
				comp.setToolTipText(lcsInputObj.getTxt());
				comp.setMinimum(K8061.ANALOG_MIN_VALUE);
				comp.setMaximum(K8061.ANALOG_MAX_VALUE);
				comp.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						LcsEngine.executeAction(lcsInputObj.getAction(), ((JSlider)e.getSource()).getValue());
					}
				});
				returnComp = contentPane.add(comp);
			}
		}
		return returnComp;
	}

	public void setComponentSize(int componentWidth, int componentHeight) {
		this.componentWidth = componentWidth;
		this.componentHeight = componentHeight;
	}

	public Hashtable<JButton, Boolean> getButtonStatus() {
		return buttonStatus;
	}

	public void setButtonStatus(Hashtable<JButton, Boolean> buttonStatus) {
		this.buttonStatus = buttonStatus;
	}

}
