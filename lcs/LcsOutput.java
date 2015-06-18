package lcs;


public class LcsOutput implements LcsListObject {
	public final static int TYPE_DIGITAL = 0;
	public final static int TYPE_ANALOG =  1;
	
	private String name;
	private String txt;
	private int type = -1;
	private int channel = -1;
	private int cardNo = -1;
	
	public boolean checkData()
	{
		if (type != -1 &&
			channel != -1 &&
			cardNo != -1)
		{
			return true;
		}
		return false;
	}
	
	public int getCardNo() 
	{
		return cardNo;
	}
	public void setCardNo(int cardNo) 
	{
		this.cardNo = cardNo;
	}
	public int getChannel() 
	{
		return channel;
	}
	public void setChannel(int channel) 
	{
		this.channel = channel;
	}
	public String getName() 
	{
		return name;
	}
	public void setName(String name) 
	{
		this.name = name;
	}
	public String getTxt() 
	{
		return txt;
	}
	public void setTxt(String txt) 
	{
		this.txt = txt;
	}
	public int getType() 
	{
		return type;
	}
	public void setType(int type) 
	{
		this.type = type;
	}
	@Override
	public String toString() {
		String type = "";
		
		switch (getType()) {
		case TYPE_ANALOG:
			type = "Analog channel";
			break;
		case TYPE_DIGITAL:
			type = "Digital channel";
			break;
		}
		return "Output " + type + " " + getName() + " properties:\n" +
			   "--------------------------------------------------------------\n" +
			   "description: " + getTxt() + "\n" +
			   "card number: " + getCardNo() + "\n" +
			   "    channel: " + getChannel() + "\n";
	}
}
