package za.co.zebrav.smartdoor.users;

import java.util.List;

import za.co.zebrav.smartdoor.R;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.app.Activity;
import android.graphics.Color;

public class ViewUserActivity extends Activity
{
	EditText edit;
	Button btInserir;
	Button btBuscar;
	Button btDeletar;
	UserProvider provider;
	LinearLayout linear;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_user);

		provider = new UserProvider(this);
		edit = (EditText) findViewById(R.id.et_palavra);
		btInserir = (Button) findViewById(R.id.bt_adicionar);
		btBuscar = (Button) findViewById(R.id.bt_buscar);
		btDeletar = (Button) findViewById(R.id.bt_deletar);
		linear = (LinearLayout) findViewById(R.id.linear_palavras);

		// A��o do botao inserir, insere o texto no do edittext na base
		btInserir.setOnClickListener(new View.OnClickListener()
		{

			public void onClick(View arg0)
			{
				User user = new User("", "", "", "");
				user.setFirstnames(edit.getText().toString());
				provider.saveUser(user);

			}
		});

		

		
		// A��o do botao buscar, primeiro remove todas as palavras do linear em
		// seguida busca todas as palavras na base
		// e por fim adiciona os textos no linear.
		btBuscar.setOnClickListener(new View.OnClickListener()
		{

			public void onClick(View v)
			{
				linear.removeAllViews();

				List<User> list = provider.getListOfAllUsers();

				if (!list.isEmpty())
				{
					TextView tv;

					for (User u : list)
					{
						tv = new TextView(getApplicationContext());
						tv.setText(u.getFirstnames());
						tv.setTextColor(Color.WHITE);
						linear.addView(tv);
					}
				}

			}
		});

		// A��o do bot�o deletar, remove todos os textos do linear e em seguida
		// deleta todas as palavras da base.
		btDeletar.setOnClickListener(new View.OnClickListener()
		{

			public void onClick(View v)
			{
				linear.removeAllViews();

				provider.clearAllUsersData();
			}
		});
		
		
		

	}

}
