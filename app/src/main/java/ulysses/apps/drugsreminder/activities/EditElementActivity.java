package ulysses.apps.drugsreminder.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListAdapter;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.List;

import ulysses.apps.drugsreminder.R;
import ulysses.apps.drugsreminder.elements.Element;
import ulysses.apps.drugsreminder.libraries.ElementsLibrary;

public abstract class EditElementActivity<T extends Element> extends AppCompatActivity {
	private AlertDialog.Builder alertBuilder;
	private int ID;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(layoutFile());
		Button editDelete = findViewById(R.id.edit_delete);
		Toolbar editToolbar = findViewById(R.id.edit_toolbar);
		alertBuilder = new AlertDialog.Builder(this);
		ID = getIntent().getIntExtra("ID", 0);
		setupViews();
		if (isNotCreating(ID)) loadViews(getElement(ID));
		setSupportActionBar(editToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		editToolbar.setNavigationOnClickListener(view -> finish());
		editDelete.setOnClickListener(view -> {
			if (isNotCreating(ID)) {
				List<Integer> involvingReminderIDs = getElement(ID).getInvolvingReminderIDs();
				if (involvingReminderIDs.isEmpty())
					alert(R.string.delete_hint, (dialog, which) -> {
						deleteElement(ID);
						finish();
					});
				else
					alert(R.string.involved_hint, (dialog1, which1) -> {
						for (int reminderID : involvingReminderIDs)
							ElementsLibrary.deleteReminder(reminderID);
						deleteElement(ID);
						finish();
					});
			} else
				alert(R.string.delete_hint, (dialog, which) -> finish());
		});
	}
	protected abstract int layoutFile();
	protected abstract void setupViews();
	protected abstract void loadViews(T element);
	protected abstract void deleteElement(int ID);
	protected abstract boolean saveChanges(int ID);
	protected void alert(int stringID) {
		alertBuilder.setMessage(stringID);
		alertBuilder.setPositiveButton(R.string.positive_text, (dialog, which) -> {});
		alertBuilder.create();
		alertBuilder.show();
	}
	protected void alert(int stringID, DialogInterface.OnClickListener positiveListener) {
		alertBuilder.setMessage(stringID);
		alertBuilder.setPositiveButton(R.string.positive_text, positiveListener);
		alertBuilder.setNegativeButton(R.string.negative_text, (dialog, which) -> {});
		alertBuilder.create();
		alertBuilder.show();
	}
	protected void alert(ListAdapter adapter, DialogInterface.OnClickListener listener) {
		alertBuilder.setMessage(null);
		alertBuilder.setAdapter(adapter, listener);
		alertBuilder.setNegativeButton(R.string.negative_text, (dialog, which) -> {});
		alertBuilder.create();
		alertBuilder.show();
	}
	protected abstract boolean isNotCreating(int ID);
	protected abstract T getElement(int ID);
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.edit_elements_toolbar_menu, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_save && saveChanges(ID)) {
			finish();
			return true;
		} else return super.onOptionsItemSelected(item);
	}
}
