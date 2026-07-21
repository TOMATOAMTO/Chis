// SettingSheet.ets
import { promptAction } from '@kit.ArkUI';

@Component
export struct SettingSheet {
  @Link isSettingSheetOpen: boolean;
  @Link isDarkMode: boolean;
  @Link isEnglish: boolean;
  @Link lightUnit: string;

  build() {
    Column({ space: 18 }) {
      Row() {
        Text(this.isEnglish ? 'Settings' : '系统设置')
          .fontSize(18)
          .fontWeight(FontWeight.Bold)
          .fontColor(this.isDarkMode ? '#FFFFFF' : '#222222')

        Blank()

        Image($r('app.media.left'))
          .width(20)
          .height(20)
          .fillColor(this.isDarkMode ? '#FFFFFF' : '#333333')
          .onClick(() => { this.isSettingSheetOpen = false; })
      }.width('100%').margin({ bottom: 4 })

      Divider().color(this.isDarkMode ? '#242C33' : '#EFEFEF')

      Row() {
        Text(this.isEnglish ? 'Language' : '应用语言选择').fontSize(14).fontColor(this.isDarkMode ? '#E0E0E0' : '#333333');
        Row({ space: 8 }) {
          Text('中文').fontSize(13).fontWeight(!this.isEnglish ? FontWeight.Bold : FontWeight.Regular).fontColor(!this.isEnglish ? (this.isDarkMode ? '#34C759' : '#224A32') : '#888888').onClick(() => { this.isEnglish = false; });
          Text('|').fontSize(12).fontColor('#DDDDDD');
          Text('English').fontSize(13).fontWeight(this.isEnglish ? FontWeight.Bold : FontWeight.Regular).fontColor(this.isEnglish ? (this.isDarkMode ? '#34C759' : '#224A32') : '#888888').onClick(() => { this.isEnglish = true; });
        }
      }.width('100%').justifyContent(FlexAlign.SpaceBetween);

      Divider().color(this.isDarkMode ? '#242C33' : '#EFEFEF');

      Row() {
        Text(this.isEnglish ? 'Light Sensor Unit' : '光照单位选择').fontSize(14).fontColor(this.isDarkMode ? '#E0E0E0' : '#333333');
        Row({ space: 8 }) {
          Text('LUX').fontSize(13).fontWeight(this.lightUnit === 'LUX' ? FontWeight.Bold : FontWeight.Regular).fontColor(this.lightUnit === 'LUX' ? (this.isDarkMode ? '#34C759' : '#224A32') : '#888888').onClick(() => { this.lightUnit = 'LUX'; });
          Text('|').fontSize(12).fontColor('#DDDDDD');
          Text('PAR').fontSize(13).fontWeight(this.lightUnit === 'PAR' ? FontWeight.Bold : FontWeight.Regular).fontColor(this.lightUnit === 'PAR' ? (this.isDarkMode ? '#34C759' : '#224A32') : '#888888').onClick(() => { this.lightUnit = 'PAR'; });
        }
      }.width('100%').justifyContent(FlexAlign.SpaceBetween);

      Divider().color(this.isDarkMode ? '#242C33' : '#EFEFEF');

      Row() {
        Text(this.isEnglish ? 'Check for Updates' : '检查更新').fontSize(14).fontColor(this.isDarkMode ? '#E0E0E0' : '#333333');
        Row({ space: 4 }) {
          Text('v1.0.4 (已是最新)').fontSize(13).fontColor('#34C759').fontWeight(FontWeight.Medium);
          if (this.isDarkMode) {
            Image($r('app.media.right_night')).width(14).height(14);
          } else {
            Image($r('app.media.right')).width(14).height(14);
          }
        }
      }
      .width('100%')
      .justifyContent(FlexAlign.SpaceBetween)
      .onClick(() => {
        promptAction.showToast({ message: '已是最新版本 (v1.0.4)\n智能植宠系统已处于最新状态', duration: 2500 });
      });

      Divider().color(this.isDarkMode ? '#242C33' : '#EFEFEF');

      Row() { Text('设备固件版本').fontSize(14).fontColor(this.isDarkMode ? '#E0E0E0' : '#333333'); Text('v1.0.4').fontSize(13).fontColor('#888888'); }.width('100%').justifyContent(FlexAlign.SpaceBetween);
    }.padding(24).width('100%').height('100%').backgroundColor(this.isDarkMode ? '#1E2226' : '#FFFFFF')
  }
}
