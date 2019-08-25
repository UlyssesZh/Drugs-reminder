package ulysses.apps.drugsreminder.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;

public final class BitmapCoder {
	@Contract("null -> !null")
	public static String code(Bitmap bitmap) {
		if (bitmap == null) return "";
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
		return Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT);
	}
	@Contract("null -> null")
	@Nullable
	public static Bitmap decode(String code) {
		if (code == null || code.isEmpty()) return null;
		byte[] bytes = Base64.decode(code, Base64.DEFAULT);
		return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
	}
}
