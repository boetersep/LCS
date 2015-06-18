package lcs;

import java.util.Enumeration;
import java.util.Hashtable;

public class LcsAction implements LcsListObject {
	private String name;
	private Hashtable<String, Object> outputList;

	public LcsAction()
	{
		outputList = new Hashtable<String, Object>();
	}
	
	public void addOutput(String outputName, Object value)
	{
		if (value.equals("on")) value = true;
		if (value.equals("off")) value = false;
		outputList.put(outputName, value);
	}
	public Hashtable<String, Object> getOutputList()
	{
		return outputList;
	}
	public String getName()
	{
		return name;
	}
	public void setName(String actionName)
	{
		this.name = actionName;
	}
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Action " + getName() + " properties:\n");
		sb.append("---------------------------------------------\n");
		for(Enumeration<String> actions = getOutputList().keys(); actions.hasMoreElements();)
		{
			String curAction = actions.nextElement();
			sb.append("     output: " + curAction + ", value: " + getOutputList().get(curAction) + "\n");
		}
		return sb.toString();
	}
}
