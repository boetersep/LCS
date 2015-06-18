package lcs;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.Hashtable;


public class LcsConfigReader {
	private RandomAccessFile configRes;
	
	private Hashtable<Integer, String> blockIdentifiers;

	
	private final Integer BLOCK_LAYOUT = new Integer(4);
	private final Integer BLOCK_OUTPUT = new Integer(0);
	private final Integer BLOCK_ACTION = new Integer(1);
	private final Integer BLOCK_INPUT = new Integer(2);
	private final Integer BLOCK_TIMETRIGGER = new Integer(3);
	
	
	private Hashtable<String, LcsOutput> lcsOutputList;
	private Hashtable<String, LcsAction> lcsActionList;
	private Hashtable<String, LcsInput> lcsInputList;
	private Hashtable<String, LcsTimetrigger> lcsTimetriggerList;
	private Hashtable<String, String> lcsLayoutProperties;
	
	
	private Hashtable<String, Integer> lcsBlockNames;
	
	
	public LcsConfigReader()
	{
		lcsBlockNames = new Hashtable<String, Integer>();

		lcsOutputList = new Hashtable<String, LcsOutput>();
		lcsActionList = new Hashtable<String, LcsAction>();
		lcsInputList = new Hashtable<String, LcsInput>();
		lcsTimetriggerList = new Hashtable<String, LcsTimetrigger>();
		lcsLayoutProperties = new Hashtable<String, String>();
		
		blockIdentifiers = new Hashtable<Integer, String>(5);
		blockIdentifiers.put(new Integer(BLOCK_LAYOUT), "layout( \\{|)");
		blockIdentifiers.put(new Integer(BLOCK_OUTPUT), "output (.*)( \\{|)");
		blockIdentifiers.put(new Integer(BLOCK_ACTION), "action (.*)( \\{|)");
		blockIdentifiers.put(new Integer(BLOCK_INPUT), "input (.*)( \\{|)");
		blockIdentifiers.put(new Integer(BLOCK_TIMETRIGGER), "timetrigger (.*)( \\{|)");

	}
	
	public void loadConfig(String configFile)
	{
		try {
			configRes = new RandomAccessFile(configFile, "r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void parseConfig() throws ParseException
	{
		for (int i = 0; i < blockIdentifiers.size(); i++)
		{
			String line = "";
			try {
				for (int j = 1 ; (line = configRes.readLine()) != null ; j++)
				{
					if (line.matches(blockIdentifiers.get(new Integer(i))))
					{
						StringBuffer innerBlock = new StringBuffer();
						String blockName = null;
						if (line.indexOf(' ') != -1)
						{
							blockName = line.split(" ")[1];
						}
						else
						{
							blockName = line.trim();
						}
						if (!lcsBlockNames.containsKey(blockName))
						{
							lcsBlockNames.put(blockName, j);
						}
						else
						{
							throw new ParseException("Error: duplicate block entry: " + blockName, lcsBlockNames.get(blockName));
						}
						int val;
						
						while ((val = configRes.read()) != '}')
						{
							if ((char)val == '\n') j++;
							if (val != '\t' && val != '{')
							{
								innerBlock.append((char)val);
							}
						}
						parseBlock(i, innerBlock.toString(), blockName);
					}
				}
				configRes.seek(0);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		LcsConsole.addEntry("Registered " + lcsOutputList.size() + " output" + ((lcsOutputList.size() > 1 || lcsOutputList.size() == 0) ? "s" : "") + ".", LcsConsole.MESSAGE_SYSTEM);
		LcsConsole.addEntry("Registered " + lcsActionList.size() + " action" + ((lcsActionList.size() > 1 || lcsActionList.size() == 0) ? "s" : "") + "/preset" + ((lcsActionList.size() > 1 || lcsActionList.size() == 0) ? "s" : "") + ".", LcsConsole.MESSAGE_SYSTEM);
		LcsConsole.addEntry("Registered " + lcsInputList.size() + " input" + ((lcsInputList.size() > 1 || lcsInputList.size() == 0) ? "s" : "") + ".", LcsConsole.MESSAGE_SYSTEM);
		LcsConsole.addEntry("Registered " + lcsTimetriggerList.size() + " timetrigger" + ((lcsTimetriggerList.size() > 1 || lcsTimetriggerList.size() == 0) ? "s" : "") + ".", LcsConsole.MESSAGE_SYSTEM);
	}
	
	private void parseBlock(int block, String innerBlock, String blockName) throws ParseException
	{
		Hashtable<String, String> params = new Hashtable<String, String>();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(innerBlock.getBytes());
		BufferedReader reader = new BufferedReader(new InputStreamReader(bais));
		String line = "";
		try {
			while((line = reader.readLine()) != null)
			{
				if (line.length() > 2)
				{
					line = line.trim();
					String[] parVal = line.split(" ");
					
					String param = parVal[0].trim();
					String value = "";
					
					for (int i = 1; i < parVal.length; i++) {
						value += parVal[i].trim() + " ";
					}
					value = value.trim();
					
					params.put(param, value);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (block == BLOCK_OUTPUT)
		{
			LcsOutput lcsOutputObj = new LcsOutput();
			
			if (params.containsKey("cardnumber")) lcsOutputObj.setCardNo(Integer.parseInt(params.get("cardnumber")));
			if (params.containsKey("channel")) lcsOutputObj.setChannel(Integer.parseInt(params.get("channel")));
			lcsOutputObj.setName(blockName);
			lcsOutputObj.setTxt(params.get("txt"));
			String type = params.get("type");
			if (type.equalsIgnoreCase("analog"))
			{
				lcsOutputObj.setType(LcsOutput.TYPE_ANALOG);
			}
			else if (type.equalsIgnoreCase("digital"))
			{
				lcsOutputObj.setType(LcsOutput.TYPE_DIGITAL);
			}
			if (lcsOutputObj.checkData())
			{
				lcsOutputList.put(blockName, lcsOutputObj);
				//System.out.println(lcsOutputObj);
			}
			else
			{
				throw new ParseException("Error: " + blockName + " failed to load parameters", lcsBlockNames.get(blockName));
			}
		}
		else if (block == BLOCK_ACTION)
		{
			LcsAction lcsActionObj = new LcsAction();
			lcsActionObj.setName(blockName);
			
			for(Enumeration<String> devicesInBlock = params.keys(); devicesInBlock.hasMoreElements();) {
				String deviceName = devicesInBlock.nextElement();
				if (lcsBlockNames.containsKey(deviceName))
				{
					lcsActionObj.addOutput(deviceName, params.get(deviceName));
				}
				else
				{
					throw new ParseException("Error: no such device " + deviceName + " in block " + blockName, lcsBlockNames.get(blockName));
				}
			}
			lcsActionList.put(blockName, lcsActionObj);
			//System.out.println(lcsActionObj);
		}
		else if (block == BLOCK_TIMETRIGGER)
		{
			LcsTimetrigger lcsTimetriggerObj = new LcsTimetrigger();
			lcsTimetriggerObj.setName(blockName);
			if (params.containsKey("action"))
			{
				String action = params.get("action");
				
				if (lcsBlockNames.containsKey(action))
				{
					lcsTimetriggerObj.setAction(action);
				}
				else
				{
					throw new ParseException("Error: no such action " + action + " in block " + blockName, lcsBlockNames.get(blockName));
				}
			}
			if (params.containsKey("time"))
			{
				String[] time = params.get("time").split("-");
				
				lcsTimetriggerObj.setStartTime(Integer.parseInt(time[0].split(":")[0]), Integer.parseInt(time[0].split(":")[1]));
				lcsTimetriggerObj.setStopTime(Integer.parseInt(time[1].split(":")[0]), Integer.parseInt(time[1].split(":")[1]));
			}
			if (lcsTimetriggerObj.checkData())
			{
				lcsTimetriggerList.put(blockName, lcsTimetriggerObj);
				//System.out.println(lcsTimetriggerObj);
			}
			else
			{
				throw new ParseException("Error: " + blockName + " failed to load parameters.", new Long(lcsBlockNames.get(blockName)).intValue());
			}
		}
		else if (block == BLOCK_INPUT)
		{
			LcsInput lcsInputObj = new LcsInput();
			if (params.containsKey("cardnumber")) lcsInputObj.setCardNo(Integer.parseInt(params.get("cardnumber")));
			if (params.containsKey("channel")) lcsInputObj.setChannel(Integer.parseInt(params.get("channel")));
			lcsInputObj.setName(blockName);
			lcsInputObj.setTxt(params.get("txt"));
			String type = "";
			if (params.containsKey("type")) type = params.get("type");
			if (params.containsKey("delay")) lcsInputObj.setDelay(Integer.parseInt(params.get("delay")));
			if (type.equalsIgnoreCase("analog"))
			{
				lcsInputObj.setType(LcsInput.TYPE_ANALOG);
			}
			else if (type.equalsIgnoreCase("digital"))
			{
				lcsInputObj.setType(LcsInput.TYPE_SWITCH);
			}
			else if (type.equalsIgnoreCase("slider"))
			{
				lcsInputObj.setType(LcsInput.TYPE_SLIDER);
			}
			else if (type.equalsIgnoreCase("button"))
			{
				lcsInputObj.setType(LcsInput.TYPE_BUTTON);
			}
			else if (type.equalsIgnoreCase("sensor"))
			{
				lcsInputObj.setType(LcsInput.TYPE_SENSOR);
			}
			
			if (lcsInputObj.getType() == LcsInput.TYPE_BUTTON ||
			lcsInputObj.getType() == LcsInput.TYPE_SLIDER)
			{
				if (params.containsKey("visibility"))
				{
					if (params.get("visibility").equalsIgnoreCase("hidden"))
					{
						lcsInputObj.setVisible(false);
					}
					else if (params.get("visibility").equalsIgnoreCase("visible"))
					{
						lcsInputObj.setVisible(true);
					}
					else
					{
						lcsInputObj.setVisible(false);
					}
				}
				if (params.containsKey("xpos")) lcsInputObj.setXPos(Integer.parseInt(params.get("xpos")));
				if (params.containsKey("ypos")) lcsInputObj.setYPos(Integer.parseInt(params.get("ypos")));
			}
			if (lcsInputObj.getType() == LcsInput.TYPE_SENSOR)
			{
				if (params.containsKey("time"))
				{
					String[] time = params.get("time").split("-");
					
					lcsInputObj.setStartTime(Integer.parseInt(time[0].split(":")[0]), Integer.parseInt(time[0].split(":")[1]));
					lcsInputObj.setStopTime(Integer.parseInt(time[1].split(":")[0]), Integer.parseInt(time[1].split(":")[1]));
				}
			}
			if (params.containsKey("action"))
			{
				String action = params.get("action");
				
				if (lcsBlockNames.containsKey(action))
				{
					lcsInputObj.setAction(action);
				}
				else
				{
					throw new ParseException("Error: no such action " + action + " in block " + blockName, lcsBlockNames.get(blockName));
				}
			}
			if (lcsInputObj.checkData())
			{
				lcsInputList.put(blockName, lcsInputObj);
				//System.out.println(lcsInputObj);
			}
			else
			{
				throw new ParseException("Error: " + blockName + " failed to load parameters.", lcsBlockNames.get(blockName));
			}	
		}
		else if (block == BLOCK_LAYOUT)
		{
			if (params.containsKey("background")) lcsLayoutProperties.put("background", params.get("background"));
			if (params.containsKey("componentsize")) lcsLayoutProperties.put("componentsize", params.get("componentsize"));
		}
	}

	public Hashtable<String, LcsAction> getLcsActionList()
	{
		return lcsActionList;
	}

	public Hashtable<String, LcsInput> getLcsInputList()
	{
		return lcsInputList;
	}

	public Hashtable<String, LcsOutput> getLcsOutputList()
	{
		return lcsOutputList;
	}

	public Hashtable<String, LcsTimetrigger> getLcsTimetriggerList()
	{
		return lcsTimetriggerList;
	}

	public Hashtable<String, String> getLcsLayoutProperties()
	{
		return lcsLayoutProperties;
	}

	
}
