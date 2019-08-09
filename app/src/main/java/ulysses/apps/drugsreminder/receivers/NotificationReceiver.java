package ulysses.apps.drugsreminder.receivers;

import android.app.Service;

import ulysses.apps.drugsreminder.services.NotificationService;

public class NotificationReceiver extends ReceiverForService {
	@Override
	protected Class<? extends Service> targetServiceClass() {
		return NotificationService.class;
	}
}
