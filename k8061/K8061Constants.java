package k8061;

public interface K8061Constants
{
	/*
	 * USB
	 * 
	 */
	public static final byte MAX_PACKET_SIZE =			(byte)0x64;
	
	public static final byte USB_READ_ENDPOINT =		(byte)0x81;
	public static final byte USB_WRITE_ENDPOINT =		(byte)0x01;

	/*
	 * K8061 protocol
	 * 
	 */
	public static final byte COMMAND_READ_12V =			(byte)0x0D;
	public static final byte COMMAND_READ_CARDNUMBER =	(byte)0x0C;
	public static final byte COMMAND_READ_ANALOG =		(byte)0x01;
	public static final byte COMMAND_WRITE_ANALOG =		(byte)0x02;
	public static final byte COMMAND_READ_DIGITAL =		(byte)0x05;
	public static final byte COMMAND_WRITE_DIGITAL =	(byte)0x06;
	public static final byte COMMAND_WRITE_PWM =		(byte)0x04;


	public static final short ANALOG_MAX_VALUE = 		255;
	public static final short ANALOG_MIN_VALUE = 		0;
	
	/*
	 * K8061 event
	 * 
	 */
	public static final short EVENT_ANALOG_IN =			8061 + 1;
	public static final short EVENT_ANALOG_OUT =		8061 + 2;
	public static final short EVENT_DIGITAL_IN =		8061 + 3;
	public static final short EVENT_DIGITAL_OUT =		8061 + 4;
	public static final short EVENT_PWM_OUT =			8061 + 5;

}
