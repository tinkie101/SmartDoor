package za.co.zebrav.smartdoor;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class LoggedInFragment extends Fragment
{
	private static final String LOG_TAG = "LoggedInFragment";
	private ListView list;
	private ArrayAdapter<String> adapter;
	private String[] commandOptions;
	private AbstractActivity mainActivity;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.logged_in, container, false);

		mainActivity = (AbstractActivity) getActivity();

		mainActivity.speakOut("Welcome, " + mainActivity.getUser().getFirstnames() + " "
							+ mainActivity.getUser().getSurname());

		if (mainActivity.getUser().getAdminRights())
			commandOptions = getResources().getStringArray(R.array.commandOptions);
		else
			commandOptions = getResources().getStringArray(R.array.basicCommandOptions);
		list = (ListView) view.findViewById(R.id.commandList);
		adapter = new ArrayAdapter<String>(getActivity().getBaseContext(), R.layout.command_list_item, commandOptions);
		list.setAdapter(adapter);

		setOnclickListener();
		return view;
	}

	@Override
	public void onResume()
	{
		super.onResume();
		mainActivity.startListeningForCommands(commandOptions);
		Log.d(LOG_TAG, "onResume");
	}

	@Override
	public void onStop()
	{
		super.onStop();
		mainActivity.stopListeningForCommands();
		Log.d(LOG_TAG, "onStop");
	}

	public void setOnclickListener()
	{
		list.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				String selectedName = adapter.getItem(position);

				mainActivity.userCommands.executeCommand(selectedName.toLowerCase());
			}
		});
	}

}
