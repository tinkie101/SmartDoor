package za.co.zebrav.smartdoor.users;

import java.util.List;

import com.db4o.ObjectSet;

import za.co.zebrav.smartdoor.R;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.graphics.Color;

public class ViewUserActivity extends Activity
{
	EditText edit;
	Button insertButton;
	Button viewAllButton;
	Button deleteButton;
	UserProvider provider;
	LinearLayout linear;
	Activity thisAct;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_user);

		thisAct = this;
		provider = new UserProvider(this);
		edit = (EditText) findViewById(R.id.et_palavra);
		insertButton = (Button) findViewById(R.id.bt_adicionar);
		viewAllButton = (Button) findViewById(R.id.bt_buscar);
		deleteButton = (Button) findViewById(R.id.bt_deletar);
		linear = (LinearLayout) findViewById(R.id.linear_palavras);

		
		insertButton.setOnClickListener(new View.OnClickListener()
		{

			public void onClick(View arg0)
			{
				User user = new User("", "", "", "");
				user.setFirstnames(edit.getText().toString());
				provider.saveUser(user);

			}
		});

		viewAllButton.setOnClickListener(new View.OnClickListener()
		{

			public void onClick(View v)
			{
				linear.removeAllViews();
				
				List<User> result = provider.getListOfAllUsers();
				
				if (!result.isEmpty())
				{
					TextView tv;

					for (User u : result)
					{
						tv = new TextView(getApplicationContext());
						tv.setText(u.getFirstnames());
						tv.setTextColor(Color.WHITE);
						linear.addView(tv);
					}
				}
				provider.close();//must close after data has been used
			}
		});
		
		
		deleteButton.setOnClickListener(new View.OnClickListener()
		{

			public void onClick(View v)
			{
				linear.removeAllViews();

				provider.clearAllUsersData();
			}
		});
		
		
		

	}

}
