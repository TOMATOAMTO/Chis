// NotificationService.ets - Real HarmonyOS System Push Notification Service
import { notificationManager } from '@kit.NotificationKit';
import { BusinessError } from '@kit.BasicServicesKit';

export class NotificationService {
  private static lastPublishedText: string = '';

  public static async requestPermission() {
    try {
      let enabled = await notificationManager.isNotificationEnabled();
      if (!enabled) {
        await notificationManager.requestEnableNotification();
      }
    } catch (err) {
      console.error('Notification permission error: ' + JSON.stringify(err));
    }
  }

  public static publishAnomalyPush(title: string, text: string) {
    if (NotificationService.lastPublishedText === text) {
      return;
    }
    NotificationService.lastPublishedText = text;

    NotificationService.requestPermission();

    let request: notificationManager.NotificationRequest = {
      id: 2001,
      notificationSlotType: notificationManager.SlotType.SERVICE_INFORMATION,
      content: {
        notificationContentType: notificationManager.ContentType.NOTIFICATION_CONTENT_BASIC_TEXT,
        normal: {
          title: title,
          text: text
        }
      }
    };

    notificationManager.publish(request, (err: BusinessError) => {
      if (err) {
        console.error(`Publish notification failed: ${err.code}, ${err.message}`);
      } else {
        console.info(`Successfully published system push notification: ${title}`);
      }
    });
  }
}
