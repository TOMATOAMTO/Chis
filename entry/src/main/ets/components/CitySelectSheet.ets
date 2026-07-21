// CitySelectSheet.ets
import { promptAction } from '@kit.ArkUI';

export interface CityNode {
  name: string;
  location: string;
}

@Component
export struct CitySelectSheet {
  @Link isCitySheetOpen: boolean;
  @Link currentCityName: string;
  @Link currentCityLocation: string;
  @Link isDarkMode: boolean;
  @Link isEnglish: boolean;
  @Prop chinaCities: CityNode[];

  build() {
    Column() {
      Row() {
        Text(this.isEnglish ? 'Select Weather City' : '选择天气观测城市')
          .fontSize(18)
          .fontWeight(FontWeight.Bold)
          .fontColor(this.isDarkMode ? '#FFFFFF' : '#222222')

        Blank()

        Image($r('app.media.left'))
          .width(20)
          .height(20)
          .fillColor(this.isDarkMode ? '#FFFFFF' : '#333333')
          .onClick(() => { this.isCitySheetOpen = false; })
      }.width('100%').margin({ bottom: 16 })

      List() {
        ForEach(this.chinaCities, (city: CityNode, index: number) => {
          ListItem() {
            Column() {
              Row() {
                Text(city.name)
                  .fontSize(16)
                  .fontColor(this.currentCityLocation === city.location ? '#224A32' : (this.isDarkMode ? '#E0E0E0' : '#333333'))
                  .fontWeight(this.currentCityLocation === city.location ? FontWeight.Bold : FontWeight.Regular)

                Blank()

                if (this.currentCityLocation === city.location) {
                  Text('✓').fontSize(16).fontWeight(FontWeight.Bold).fontColor('#224A32')
                }
              }
              .width('100%')
              .padding({ top: 14, bottom: 14, left: 4, right: 4 })

              if (index < this.chinaCities.length - 1) {
                Divider().color(this.isDarkMode ? '#283038' : '#F0F0F0').strokeWidth(1)
              }
            }
            .width('100%')
            .onClick(() => {
              if (city.location === 'auto') {
                this.currentCityName = this.isEnglish ? 'Locating...' : '定位中...';
              } else {
                this.currentCityName = city.name;
              }
              this.currentCityLocation = city.location;
              this.isCitySheetOpen = false;
              try {
                promptAction.showToast({ message: `已切换观测城市：${city.name}`, duration: 2000 });
              } catch (e) {}
            })
          }
        })
      }.width('100%').layoutWeight(1)
    }.padding({ left: 24, right: 24, top: 20, bottom: 24 }).width('100%').height('100%').backgroundColor(this.isDarkMode ? '#1E2226' : '#FFFFFF')
  }
}
