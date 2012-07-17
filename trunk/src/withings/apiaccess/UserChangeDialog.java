/*
 *   Copyright 2011 Withings
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *   
 *   http://www.apache.org/licenses/LICENSE-2.0
 *   
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package withings.apiaccess;

import java.util.Vector;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;


public class UserChangeDialog extends Dialog {

	public class UsersListAdapter extends ArrayAdapter<String>{
		
		public UsersListAdapter(Context context) {
			super(context, android.R.layout.simple_expandable_list_item_1);
		}
	}
	
	private Vector<WithingsUser> mUsersList;

	public UserChangeDialog(Context context, Vector<WithingsUser> usersList) {
		super(context);
		mUsersList = usersList;
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		UsersListAdapter usersListAdapter = new UsersListAdapter(getContext());
		
		for (WithingsUser user : mUsersList) {
			usersListAdapter.add(user.name);
		}
		
		this.setTitle("Choose a user"); 
		this.setContentView(android.wellness.R.layout.user_change_dialog);
		
		ListView list = (ListView) this.findViewById(android.R.id.list);
		list.setAdapter(usersListAdapter);
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				try {
					WithingsAPI.getInstance(getContext()).selectUser(mUsersList.get(position));
				} catch (Exception e) {
					e.printStackTrace();
				}
				dismiss();
			}
		});        
        
	}
}
