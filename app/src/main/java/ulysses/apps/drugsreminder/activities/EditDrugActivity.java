package ulysses.apps.drugsreminder.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.IOException;

import ulysses.apps.drugsreminder.R;
import ulysses.apps.drugsreminder.elements.Drug;
import ulysses.apps.drugsreminder.libraries.ElementsLibrary;

public class EditDrugActivity extends EditElementActivity<Drug> {
	private static final int REQUEST_IMAGE_CAPTURE = 1;
	private static final int REQUEST_IMAGE_SELECT = 2;
	private static final double THUMBNAIL_AREA = 47628.0;
	private EditText editName;
	private ImageView imageView;
	private Bitmap bitmap;
	@Override
	protected int layoutFile() {
		return R.layout.edit_drug_activity;
	}
	@Override
	protected void setupViews() {
		editName = findViewById(R.id.edit_drug_name);
		imageView = findViewById(R.id.drug_image_view);
		findViewById(R.id.select_from_album).setOnClickListener(view -> {
			Intent intent = new Intent(Intent.ACTION_PICK,
					MediaStore.Images.Media.INTERNAL_CONTENT_URI);
			if (intent.resolveActivity(getPackageManager()) == null)
				alert(R.string.no_album_hint);
			else
				startActivityForResult(intent, REQUEST_IMAGE_SELECT);
		});
		findViewById(R.id.take_from_camera).setOnClickListener(view -> {
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			if (intent.resolveActivity(getPackageManager()) == null)
				alert(R.string.no_camera_hint);
			else
				startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
		});
	}
	@Override
	protected void loadViews(Drug drug) {
		editName.setText(drug.getName());
		bitmap = drug.getBitmap();
		if (bitmap != null) imageView.setImageBitmap(bitmap);
	}
	@Override
	protected void deleteElement(int ID) {
			ElementsLibrary.deleteDrug(ID);
	}
	@Override
	protected boolean saveChanges(int ID) {
		String drugName = editName.getText().toString();
		if (drugName.isEmpty()) {
			alert(R.string.name_empty_hint);
			return false;
		} else {
			ElementsLibrary.addDrug(new Drug(ID, drugName, bitmap));
			return true;
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_IMAGE_CAPTURE) {
				bitmap = (Bitmap) data.getExtras().get("data");
				imageView.setImageBitmap(bitmap);
			} else if (requestCode == REQUEST_IMAGE_SELECT)
				try {
					bitmap = MediaStore.Images.Media.getBitmap(
							this.getContentResolver(), data.getData());
					int width = bitmap.getWidth();
					int height = bitmap.getHeight();
					double scale = Math.sqrt(THUMBNAIL_AREA / (width * height));
					bitmap = ThumbnailUtils.extractThumbnail(bitmap,
							(int) (width * scale),(int) (height * scale));
					imageView.setImageBitmap(bitmap);
				} catch (IOException e) {
					e.printStackTrace();
					alert(R.string.fail_load_image);
				}
		}
	}
	@Override
	protected boolean isNotCreating(int ID) {
		return !ElementsLibrary.drugIDOutOfBound(ID);
	}
	@Override
	protected Drug getElement(int ID) {
		return ElementsLibrary.findDrugByID(ID);
	}
}
