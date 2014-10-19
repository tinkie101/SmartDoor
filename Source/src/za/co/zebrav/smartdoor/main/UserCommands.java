package za.co.zebrav.smartdoor.main;

import za.co.zebrav.smartdoor.database.AddUserActivity;
import za.co.zebrav.smartdoor.database.ViewUserActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class UserCommands
{
	private MainActivity mainContext;
	
		public UserCommands(MainActivity m)
		{
			mainContext = m;
		}
		
		public void executeCommand(String command)
		{
			System.out.println(command);
			if(command.equals("open door") || command.equals("please open the door"))
			{
				openDoor(command);
			}
			else if(command.equals("add user") || command.equals("manage users"))
			{
				addUser();
			}
			else if(command.equals("manage user") || command.equals("manage users"))
			{
				searchUser();
			}
			else if(command.equals("search user") || command.equals("manage users"))
			{
				searchUser();
			}
			else if(command.equals("remove user") || command.equals("remove users"))
			{
				searchUser();
			}
			else if(command.equals("settings"))
			{
				settings();
			}
			else if(command.equals("logout"))
			{
				logout();
				return;
			}
			else
			{
				Toast.makeText(mainContext, "No such command", Toast.LENGTH_LONG);
				return;
			}
			mainContext.getLogoutTimer().cancel();
			mainContext.getLogoutTimer().start();
		}
	
		//----------------------------------------------------------------------------Execution of commands
		private void openDoor(String command)
		{			
			ClientSocket clientSocket = new ClientSocket(mainContext);
			clientSocket.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, command);
			Toast.makeText(mainContext, "Openning the door", Toast.LENGTH_SHORT).show();
		}
		
		private void logout()
		{
			mainContext.speakOut("Logging out");
			mainContext.logout();
		}
		
		private void addUser()
		{
			mainContext.speakOut("Going to add user");
			
			Intent intent = new Intent(mainContext, AddUserActivity.class);
			mainContext.startActivity(intent);
		}
		
		private void searchUser()
		{
			mainContext.speakOut("Going to manage users");
			
			Intent intent = new Intent(mainContext,ViewUserActivity.class);
			mainContext.startActivity(intent);
		}
		
		private void settings()
		{
			mainContext.speakOut("Going to settings");
			
			mainContext.switchToSettingsFragment();
		}
}
