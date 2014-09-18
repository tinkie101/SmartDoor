package za.co.zebrav.smartdoor;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class LoggedInFragment extends Fragment
{
	private ListView list;
	private ArrayAdapter<String> adapter;
	private String[] commandOptions;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		View view = inflater.inflate(R.layout.logged_in, null);
		list = (ListView) view.findViewById(R.id.commandList);
		commandOptions =  getResources().getStringArray(R.array.commandOptions); 
		adapter = new ArrayAdapter<String>(getActivity().getBaseContext(), R.layout.command_list_item, commandOptions);
		list.setAdapter(adapter);
		
		setOnclickListener();
		return view;
	}
	
	public void setOnclickListener()
	{
		list.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{	
				String selectedName = adapter.getItem(position);
				
				Toast.makeText(getActivity(), selectedName, Toast.LENGTH_SHORT).show();
				if(selectedName.equals("Open door"))
				{
					
				}
				else if(selectedName.equals("Hello"))
				{
					
				}
			}
		});
	}
}
