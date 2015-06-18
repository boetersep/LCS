package lcs;

import java.awt.Component;
import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JSlider;

import k8061.K8061;
import k8061.K8061Constants;
import k8061.K8061Emulator;
import k8061.K8061Listener;
import k8061.event.K8061Event;
import lcs.util.ColorTable;

public class LcsEngine implements Runnable, K8061Listener
{
	public static String versionString = "Light Control System v1.34";
	
	private String CONFIG;
	private static LcsLayout lcsLayoutFrame;
	private static K8061 k8061InterfaceCard;
	private static LcsConfigReader lcsConfig;
	private static LcsServer lcsServer;
	private ArrayList<String> timeTriggersSet;
	private ArrayList<String> timeTriggersClear;
	private ArrayList<String> movementSensorsSet;
	private Hashtable<LcsInput, LcsMovementSensor> movementSensors;
	private boolean isReloading;
	private long configFileStamp;
	
	
	public LcsEngine(String configFile)
	{
		versionString += " using " + configFile;
		CONFIG = configFile;
		
		k8061InterfaceCard = new K8061Emulator(0);
		//k8061InterfaceCard = new K8061(0, false);
		
		Thread t = new Thread(k8061InterfaceCard);
		t.start();
		
		lcsServer = new LcsServer();
		
		Thread t1 = new Thread(lcsServer);
		t1.start();
		
		timeTriggersSet = new ArrayList<String>();
		timeTriggersClear = new ArrayList<String>();
		movementSensors = new Hashtable<LcsInput, LcsMovementSensor>();
		movementSensorsSet = new ArrayList<String>();
		
		k8061InterfaceCard.addK8061Listener(this);		

		lcsLayoutFrame = new LcsLayout();
		lcsConfig = new LcsConfigReader();
		lcsConfig.loadConfig(CONFIG);
		configFileStamp = new File(CONFIG).lastModified();
		
		
		try
		{
			lcsConfig.parseConfig();
		}
		catch (ParseException e)
		{
			System.err.println("Config faulty: " + e.getMessage() + " @ line " + e.getErrorOffset());
			System.exit(-1);
		}
		
		initLayout();
		
		lcsLayoutFrame.setVisible(true);
		isReloading = false;
	}
	
	public void initLayout()
	{
		int componentWidth = 300;
		int componentHeight = 75;
		
		if (lcsConfig.getLcsLayoutProperties().containsKey(("background")))
		{
			String background = lcsConfig.getLcsLayoutProperties().get("background");
			lcsLayoutFrame.setBackground(ColorTable.parseColor(background));
			lcsLayoutFrame.repaint();
		}
		
		if (lcsConfig.getLcsLayoutProperties().containsKey(("componentsize")))
		{
			componentWidth = Integer.parseInt(lcsConfig.getLcsLayoutProperties().get("componentsize").split("x")[0]);
			componentHeight = Integer.parseInt(lcsConfig.getLcsLayoutProperties().get("componentsize").split("x")[1]);
		}
		
		Component[] components = lcsLayoutFrame.getContentPane().getComponents();
		
		for (int i = 0; i < components.length; i++) {
			if (components[i].getClass() != LcsConsole.class) 
			{
				lcsLayoutFrame.getContentPane().remove(components[i]);
				lcsLayoutFrame.repaint();
			}
		}
		
		for(Enumeration<String> lcsInputObjectNames = lcsConfig.getLcsInputList().keys(); lcsInputObjectNames.hasMoreElements();)
		{
			String curInputName = lcsInputObjectNames.nextElement();
			LcsInput lcsInputObj = lcsConfig.getLcsInputList().get(curInputName);
			
			lcsLayoutFrame.setComponentSize(componentWidth, componentHeight);
			
			Component c = lcsLayoutFrame.addComponent(lcsInputObj);
			if (c!=null)
			{
				lcsInputObj.setComp(c);
			}
			
			
			if (lcsInputObj.getType() == LcsInput.TYPE_SENSOR)
			{
				LcsMovementSensor lcsMovementSensorObj = new LcsMovementSensor();
				lcsMovementSensorObj.setDelay(lcsInputObj.getDelay());
				Thread t1 = new Thread(lcsMovementSensorObj);
				movementSensors.put(lcsInputObj, lcsMovementSensorObj);
				t1.start();
			}
		}
	}
	
	public void reloadConfig()
	{
		reloadConfig(CONFIG);
	}
	
	public void reloadConfig(String configFile)
	{
		LcsConsole.addEntry("Reload config.", LcsConsole.MESSAGE_SYSTEM);
		
		isReloading = true;
		
		movementSensors.clear();
		movementSensorsSet.clear();
		timeTriggersClear.clear();
		timeTriggersSet.clear();
		LcsConfigReader tmpConfig = new LcsConfigReader();
		tmpConfig.loadConfig(CONFIG);
		
		boolean configFaulty = false;
		
		try
		{
			tmpConfig.parseConfig();
		}
		catch (ParseException e)
		{
			LcsConsole.addEntry("Config faulty: " + e.getMessage() + " @ line " + e.getErrorOffset(), LcsConsole.MESSAGE_ERROR);
			LcsConsole.addEntry("Continue with old config.", LcsConsole.MESSAGE_ERROR);
			configFaulty = true;
		}
		
		if (!configFaulty)
		{
			lcsConfig = tmpConfig;
		}
		
		initLayout();
		isReloading = false;
	}
	
	public static void executeAction(String actionName, Object value)
	{
		try
		{
			// is het een actie/preset?
			for(Enumeration<String> lcsActionsObjectNames = lcsConfig.getLcsActionList().keys(); lcsActionsObjectNames.hasMoreElements();)
			{
				String curActionName = lcsActionsObjectNames.nextElement();
				
				// Als het daadwerkelijk een actie is worden alle outputs die in het actie object staan behandeld.
				if (curActionName.equals(actionName))
				{
					LcsAction lcsActionObj = lcsConfig.getLcsActionList().get(curActionName);
					
					// Splits de analoge en de digitale outputs.
					for(Enumeration<String> actionsInObj = lcsActionObj.getOutputList().keys(); actionsInObj.hasMoreElements();)
					{
						String curOutputName = actionsInObj.nextElement();
						Object curOutputValue = lcsActionObj.getOutputList().get(curOutputName);
						LcsOutput lcsOutputObj = lcsConfig.getLcsOutputList().get(curOutputName);
						
						// Als het een analoge output betreft wordt de waarde ingesteld op 0 als waarde 
						// van de gui false is. Als het true is wordt de waarde gebruikt uit het actie object.
						if (lcsOutputObj.getType() == LcsOutput.TYPE_ANALOG)
						{
							// gebruik de waarde uit het actie object
							if ((Boolean)value)
							{
								k8061InterfaceCard.OutputAnalogChannel(lcsOutputObj.getChannel(), Short.parseShort((String)curOutputValue), true, 2);
								LcsConsole.addEntry("Analog channel #" + lcsOutputObj.getChannel() + " set value " + curOutputValue + ".", LcsConsole.MESSAGE_EVENT);
							}
							// zet de waarde op 0
							else if (!(Boolean)value)
							{
								k8061InterfaceCard.OutputAnalogChannel(lcsOutputObj.getChannel(), (short)0, true, 3.5);
								LcsConsole.addEntry("Analog channel #" + lcsOutputObj.getChannel() + " set value 0.", LcsConsole.MESSAGE_EVENT);
	
							}
						}
						
						
						// Als het een digitale output betreft worden de waarden van de gui en
						// de waarden uit het aktie object ge-xorred.
						if (lcsOutputObj.getType() == LcsOutput.TYPE_DIGITAL)
						{
							if ((Boolean)value^(Boolean)curOutputValue)
							{
								k8061InterfaceCard.setDigitalChannel(lcsOutputObj.getChannel());
								LcsConsole.addEntry("Digital channel #" + lcsOutputObj.getChannel() + " activate.", LcsConsole.MESSAGE_EVENT);
							}
							else if (!(Boolean)value^(Boolean)curOutputValue)
							{
								k8061InterfaceCard.clearDigitalChannel(lcsOutputObj.getChannel());
								LcsConsole.addEntry("Digital channel #" + lcsOutputObj.getChannel() + " deactivate.", LcsConsole.MESSAGE_EVENT);
							}
						}
					}
				}
			}
	
			// is het een output?
			for(Enumeration<String> lcsOutputObjectNames = lcsConfig.getLcsOutputList().keys(); lcsOutputObjectNames.hasMoreElements();)
			{
				String curOutputName = lcsOutputObjectNames.nextElement();
				
				if (curOutputName.equals(actionName))
				{
					LcsOutput lcsOutputObj = lcsConfig.getLcsOutputList().get(curOutputName);
			
					if (lcsOutputObj.getType() == LcsOutput.TYPE_ANALOG)
					{
						k8061InterfaceCard.OutputAnalogChannel(lcsOutputObj.getChannel(), ((Integer)value).shortValue(), true, 2);
						LcsConsole.addEntry("Analog channel #" + lcsOutputObj.getChannel() + " set value " + value + ".", LcsConsole.MESSAGE_EVENT);
					}
					// Als het een digitale output betreft 
					if (lcsOutputObj.getType() == LcsOutput.TYPE_DIGITAL)
					{
						if ((Boolean)value)
						{
							k8061InterfaceCard.setDigitalChannel(lcsOutputObj.getChannel());
							LcsConsole.addEntry("Digital channel #" + lcsOutputObj.getChannel() + " activate.", LcsConsole.MESSAGE_EVENT);
						}
						else if (!(Boolean)value)
						{
							k8061InterfaceCard.clearDigitalChannel(lcsOutputObj.getChannel());
							LcsConsole.addEntry("Digital channel #" + lcsOutputObj.getChannel() + " deactivate.", LcsConsole.MESSAGE_EVENT);
						}
					}
				}
			}
		}
		catch (Exception e) {
			
		}
	}
	
	public void run()
	{
		while(true)
		{
			while (!isReloading)
			{
				if (configFileStamp < new File(CONFIG).lastModified())
				{
					reloadConfig();
					configFileStamp = new File(CONFIG).lastModified();
				}
				
				Calendar current = Calendar.getInstance();
				current.set(1970, 0, 1);
				for(Enumeration<String> lcsTimetriggerObjectNames = lcsConfig.getLcsTimetriggerList().keys(); lcsTimetriggerObjectNames.hasMoreElements();)
				{		
					String curTimetriggerName = lcsTimetriggerObjectNames.nextElement();
					LcsTimetrigger lcsTimetriggerObject = lcsConfig.getLcsTimetriggerList().get(curTimetriggerName);
	
					if (lcsTimetriggerObject.getStartTime().before(current))
					{	
						if (!timeTriggersSet.contains(curTimetriggerName))
						{
							timeTriggersSet.add(curTimetriggerName);
							timeTriggersClear.remove(curTimetriggerName);
							executeAction(lcsTimetriggerObject.getAction(), true);
							LcsConsole.addEntry("Activate timetrigger meganism " + curTimetriggerName + ".", LcsConsole.MESSAGE_EVENT);
						}
					}
	
					if (current.before(lcsTimetriggerObject.getStartTime()) && timeTriggersSet.contains(curTimetriggerName))
					{
						timeTriggersSet.remove(curTimetriggerName);
					}
					
					if (lcsTimetriggerObject.getStopTime().before(current))
					{	
						if (!timeTriggersClear.contains(curTimetriggerName))
						{
							timeTriggersClear.add(curTimetriggerName);
							executeAction(lcsTimetriggerObject.getAction(), false);
							LcsConsole.addEntry("Deactivate time trigger meganism " + curTimetriggerName + ".", LcsConsole.MESSAGE_EVENT);
						}
					}
					
				}
				
				for(Enumeration<LcsInput> lcsInputObjects = movementSensors.keys(); lcsInputObjects.hasMoreElements();)
				{
					boolean isTimeOk = true;
					LcsInput lcsInputObj = lcsInputObjects.nextElement();
					
					if (lcsInputObj.getStartTime() != null && lcsInputObj.getStopTime() != null)
					{
						if (current.before(lcsInputObj.getStartTime()) && current.after(lcsInputObj.getStopTime()))
						{
							isTimeOk = false;
						}
					}
					
					LcsMovementSensor lcsMovementSensorObj = movementSensors.get(lcsInputObj);
					
					if (!movementSensorsSet.contains(lcsInputObj.getAction()))
					{
						if (lcsMovementSensorObj.isActive() && isTimeOk)
						{
							executeAction(lcsInputObj.getAction(), true);
							LcsConsole.addEntry("Movement detectected at sensor " + lcsInputObj.getTxt() + ".", LcsConsole.MESSAGE_EVENT);
							movementSensorsSet.add(lcsInputObj.getAction());
						}					
					}
					
					if (!lcsMovementSensorObj.isActive())
					{
						if (movementSensorsSet.contains(lcsInputObj.getAction()))
						{
							executeAction(lcsInputObj.getAction(), false);
							movementSensorsSet.remove(lcsInputObj.getAction());
						}
						
					}
				}
				
				try
				{
					Thread.sleep(100);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	public void valueChanged(K8061Event event)
	{
		if (event.getEventType() == K8061Constants.EVENT_DIGITAL_IN)
		{
			for(Enumeration<LcsInput> lcsInputObjects = movementSensors.keys(); lcsInputObjects.hasMoreElements();)
			{
				LcsInput lcsInputObj = lcsInputObjects.nextElement();
				if (lcsInputObj.getType() == LcsInput.TYPE_SENSOR)
				{
					LcsMovementSensor lcsMovementSensorObj = movementSensors.get(lcsInputObj);
					if (k8061InterfaceCard.getDigitalChannel(lcsInputObj.getChannel()))
					{
						lcsMovementSensorObj.movementDetected();
					}
				}
			}
			for(Enumeration<String> lcsInputObjectNames = lcsConfig.getLcsInputList().keys(); lcsInputObjectNames.hasMoreElements();)
			{
				String curInputName = lcsInputObjectNames.nextElement();
				LcsInput lcsInputObj = lcsConfig.getLcsInputList().get(curInputName);
							
				if (lcsInputObj.getType() == LcsInput.TYPE_SWITCH)
				{
					LcsConsole.addEntry("Switch signal from " + lcsInputObj.getName() + " received.", LcsConsole.MESSAGE_EVENT);
					executeAction(lcsInputObj.getAction(), k8061InterfaceCard.getDigitalChannel(lcsInputObj.getChannel()));
				}
			}
		}
		if (event.getEventType() == K8061Constants.EVENT_ANALOG_OUT)
		{
			
			
			for(Enumeration<String> lcsInputObjectNames = lcsConfig.getLcsInputList().keys(); lcsInputObjectNames.hasMoreElements();)
			{
				String curInputName = lcsInputObjectNames.nextElement();
				LcsInput lcsInputObj = lcsConfig.getLcsInputList().get(curInputName);
							
				if (lcsInputObj.getType() == LcsInput.TYPE_SLIDER)
				{
					JSlider slider = (JSlider) lcsInputObj.getComp();
					
					
					if (lcsConfig.getLcsActionList().contains(lcsInputObj.getAction()))
					{
					//	lcsConfig.getLcsActionList().get(lcsInputObj.getAction()))
					}
					
					if (lcsConfig.getLcsOutputList().contains(lcsInputObj.getAction()))
					{
						int channel = lcsConfig.getLcsOutputList().get(lcsInputObj.getAction()).getChannel();
						System.out.println(channel);
						
						slider.setValue(k8061InterfaceCard.readAnalogChannel(channel));
					}
					
				}
			}
		}
	}

	public static K8061 getK8061InterfaceCard()
	{
		return k8061InterfaceCard;
	}

	public static LcsConfigReader getLcsConfig()
	{
		return lcsConfig;
	}
}
