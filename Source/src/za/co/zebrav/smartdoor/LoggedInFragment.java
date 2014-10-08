package za.co.zebrav.smartdoor;

import java.io.File;

import za.co.zebrav.smartdoor.database.User;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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

		// get user image
		File path = mainActivity.getDir("data", 0);
		User user = mainActivity.getUser();

		Bitmap image = BitmapFactory.decodeFile(path + "/photos/" + user.getID() + "-0.png");

		ImageView imgUser = (ImageView) view.findViewById(R.id.imgLogedInImage);

		if (imgUser != null)
			imgUser.setImageBitmap(image);

		TextView txtName = (TextView) view.findViewById(R.id.txtLogedinName);

		if (txtName != null)
			txtName.setText("Name: " + user.getFirstnames());

		TextView txtSurname = (TextView) view.findViewById(R.id.txtLogedinSurname);

		if (txtSurname != null)
			txtSurname.setText("Surname: " + user.getSurname());

		TextView txtUserName = (TextView) view.findViewById(R.id.txtLogedinUsername);

		if (txtUserName != null)
			txtUserName.setText("Username: " + user.getUsername());

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
