package ulysses.apps.drugsreminder.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import ulysses.apps.drugsreminder.R;

public class AboutActivity extends AppCompatActivity {
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_activity);
		Toolbar toolbar = findViewById(R.id.about_toolbar);
		setSupportActionBar(toolbar);
		toolbar.setNavigationOnClickListener(view -> finish());
	}
}
