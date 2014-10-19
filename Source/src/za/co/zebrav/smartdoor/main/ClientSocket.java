package za.co.zebrav.smartdoor.main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import za.co.zebrav.smartdoor.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

public class ClientSocket extends AsyncTask<String, Void, String>
{
	private static final String LOG_TAG = "ClientSocket";

	private Socket socket;

	private static int SERVERPORT;
	private static String SERVER_IP;

	private PrintWriter out;
	private BufferedReader inFromServer;
	private Activity act;

	public ClientSocket(Activity act)
	{
		this.act = act;
		String PREFS_NAME = act.getResources().getString((R.string.settingsFileName));
		SharedPreferences settings = act.getSharedPreferences(PREFS_NAME, 0);
		SERVERPORT = Integer.parseInt(settings.getString("server_Port", "0"));
		if (SERVERPORT == 0)// not set, use defaults
		{
			SERVERPORT = Integer.parseInt(act.getResources().getString((R.string.server_Port)));
			SERVER_IP = act.getResources().getString((R.string.server_IP));
		}
		else
			SERVER_IP = settings.getString("server_IP", "0");
	}

	public int getServerPort()
	{
		return SERVERPORT;
	}

	public String getServerIP()
	{
		return SERVER_IP;
	}

	public boolean sendCommand(String command)
	{
		boolean result = false;
		if (socket != null && out != null && inFromServer != null)
		{
			try
			{
				// send
				out.println(command);
				result = true;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			Log.d(LOG_TAG, "Socket not Set!");
		}
		return result;
	}

	@Override
	protected String doInBackground(String... arg)
	{
		String result = null;
		try
		{
			InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
			socket = new Socket(serverAddr, SERVERPORT);
			socket.setSoTimeout(5000);
			Log.d(LOG_TAG, "Socket set");

			out = new PrintWriter(socket.getOutputStream(), true);
			inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			// Send to server
			String command = null;
			if(arg.length > 0)
				command = arg[0];
			else
				Log.d(LOG_TAG, "no command given");
			
			if (command != null && sendCommand(command))
			{
				try
				{
					// Get response
					result = inFromServer.readLine();
				}
				catch (SocketTimeoutException e)
				{
					Log.d(LOG_TAG, "Server Timed out");
				}

				Log.d(LOG_TAG, "done");
			}
		}
		catch (Exception e)
		{
			Log.d(LOG_TAG, e.toString());
		}
		return result;
	}

	@Override
	protected void onPostExecute(String result)
	{
		if (result != null)
		{
			Log.d(LOG_TAG, result);
			Log.d(LOG_TAG, "end");
			
			if(act instanceof MainActivity)
			{
				((MainActivity) act).postOpenDoor();
			}
		}
		else
		{
			Log.d(LOG_TAG, "Error, No Response");
			((MainActivity) act).postDoorNotOpened();
		}

		try
		{
			socket.close();
		}
		catch (Exception e)
		{
			Log.d(LOG_TAG, e.toString());
		}
	}
}