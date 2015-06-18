package lcs;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import k8061.K8061;


public class LcsServer implements Runnable {
	private ServerSocket serverSocket;
	private Socket socket;
	protected PrintWriter out;
	protected BufferedReader in;

	
	public void run()
	{
		try
		{
			serverSocket = new ServerSocket(11319);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		while (true)
		{
			try
			{
				socket = serverSocket.accept();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			new Thread(new Runnable()
			{
				
				public void run()
				{
					try
					{
						out = new PrintWriter(socket.getOutputStream(), true);
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
					try
					{
						in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
					
					String line = null;
					
					
					
					try
					{
						while ((line = in.readLine()) != null)
						{
							if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit"))
							{
								break;
							}
							if (line.equalsIgnoreCase("uptime"))
							{
								out.println(LcsEngine.getK8061InterfaceCard().getUptime() / 1000 + " seconds");
							}
							
							if (LcsEngine.getLcsConfig().getLcsOutputList().containsKey(line))
							{
								int type = LcsEngine.getLcsConfig().getLcsOutputList().get(line).getType();
								if (type == LcsOutput.TYPE_ANALOG)
								{
									out.print("enter a value between 0 and 255 and press enter ");
									out.flush();
									LcsEngine.executeAction(line, new Integer(in.readLine()));
								}
								else if (type == LcsOutput.TYPE_DIGITAL)
								{
									out.print("enter 'on' or 'off' and press enter ");
									out.flush();
									
									boolean bVal = false;
									
									if (in.readLine().equals("on"))
									{
										bVal = true;
									}
									
									LcsEngine.executeAction(line, bVal);
								}
							}
							else if (LcsEngine.getLcsConfig().getLcsActionList().containsKey(line))
							{
								out.print("enter 'on' or 'off' and press enter ");
								out.flush();
									
								boolean bVal = false;
									
								if (in.readLine().equals("on"))
								{
									bVal = true;
								}

								LcsEngine.executeAction(line, bVal);
							}
							else if (line.equals("list"))
							{
								for(Enumeration<String> lcsActionObjectNames = LcsEngine.getLcsConfig().getLcsActionList().keys(); lcsActionObjectNames.hasMoreElements();)
								{
									out.print(lcsActionObjectNames.nextElement() + "|");
								}
								out.println();
								out.flush();
							}
						}
						socket.close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}).start();
		}
	}		


}
