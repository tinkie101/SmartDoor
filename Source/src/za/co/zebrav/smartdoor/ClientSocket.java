package za.co.zebrav.smartdoor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;

public class ClientSocket extends AsyncTask<String, Void, String>
{
	private static final String LOG_TAG = "ClientSocket";

	private Socket socket;

	// TODO
	private static int SERVERPORT;
	private static String SERVER_IP;

	private PrintWriter out;
	private BufferedReader inFromServer;
	
	public ClientSocket(Activity act)
	{
		String PREFS_NAME = act.getResources().getString((R.string.settingsFileName));
		SharedPreferences settings = act.getSharedPreferences(PREFS_NAME, 0);
		SERVERPORT = Integer.parseInt(settings.getString("server_Port", "0"));
		if(SERVERPORT == 0)//not set, use defaults
		{
			SERVERPORT = Integer.parseInt(act.getResources().getString((R.string.server_Port)));
			SERVER_IP = act.getResources().getString((R.string.server_IP));
		}
		else
			SERVER_IP = settings.getString("server_IP", "0");
	}

	public void sendCommand(String command)
	{
		if (socket != null && out != null && inFromServer != null)
		{
			try
			{
				System.out.println("start");
				// send
				out.println(command);

				new Recieve().execute();

			}
			catch (Exception e)
			{

				e.printStackTrace();

			}
		}
		else
		{
			System.out.println(LOG_TAG + "\tSocket not Set!");
		}

	}

	@Override
	protected String doInBackground(String... arg)
	{
		try
		{
			InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
			socket = new Socket(serverAddr, SERVERPORT);
			System.out.println(LOG_TAG + "\t" + "Socket set");

			out = new PrintWriter(socket.getOutputStream(), true);
			inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			System.out.println("done");
		}
		catch (UnknownHostException e1)
		{
			e1.printStackTrace();
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
		return arg[0];
	}

	@Override
	protected void onPostExecute(String result)
	{
		sendCommand(result);
	}

	private class Recieve extends AsyncTask<Void, Void, Void>
	{

		@Override
		protected Void doInBackground(Void... params)
		{
			try
			{
				// recieve
				String response = inFromServer.readLine();
				System.out.println(LOG_TAG + "\t" + response);
				System.out.println("end");
			}
			catch (UnknownHostException e)
			{

				e.printStackTrace();

			}
			catch (IOException e)
			{

				e.printStackTrace();

			}
			return null;
		}

	}
}