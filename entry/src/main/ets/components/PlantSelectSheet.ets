// PlantSelectSheet.ets
import { promptAction } from '@kit.ArkUI';

export interface PlantRawItem {
  id: string;
  name: string;
  category: string;
  desc: string;
}

export interface SimulatedDeviceNode {
  id: string;
  devHardwareName: string;
  name: string;
  type: string;
  rssi: string;
  isBound: boolean;
  boundPlantName: string;
  moisture: string;
  temp: string;
  light: string;
  npk: string;
}

@Component
export struct PlantSelectSheet {
  @Link isSelectPlantSheetOpen: boolean;
  @Link isAddDeviceSheetOpen: boolean;
  @Link targetBindingDevice: SimulatedDeviceNode | null;
  @Link availableDevices: SimulatedDeviceNode[];
  @Link boundDevicesCount: number;
  @Link isDarkMode: boolean;
  @Link isEnglish: boolean;
  @Prop plantCatalog: PlantRawItem[];

  @State searchQuery: string = '';

  private getFilteredPlants(): PlantRawItem[] {
    if (!this.searchQuery || !this.searchQuery.trim()) {
      return this.plantCatalog;
    }
    const query = this.searchQuery.trim().toLowerCase();
    return this.plantCatalog.filter((plant: PlantRawItem) => {
      return (
        (plant.name && plant.name.toLowerCase().includes(query)) ||
        (plant.category && plant.category.toLowerCase().includes(query)) ||
        (plant.desc && plant.desc.toLowerCase().includes(query))
      );
    });
  }

  build() {
    Column() {
      Row() {
        Text(this.isEnglish ? 'Select Plant Species' : '选择关联种植的植物种类')
          .fontSize(16).fontWeight(FontWeight.Bold).fontColor(this.isDarkMode ? '#FFFFFF' : '#222222')
        Blank()
        Image($r('app.media.left'))
          .width(20).height(20).fillColor(this.isDarkMode ? '#FFFFFF' : '#333333')
          .onClick(() => { this.isSelectPlantSheetOpen = false; })
      }.width('100%').margin({ bottom: 12 })

      // 精简且紧凑靠左的搜索框
      Row({ space: 6 }) {
        Image($r('app.media.search'))
          .width(15)
          .height(15)
          .fillColor('#888888')

        TextInput({ text: this.searchQuery, placeholder: this.isEnglish ? 'Search plant name...' : '搜索植物名称、科属...' })
          .fontSize(13)
          .placeholderColor('#999999')
          .placeholderFont({ size: 13 })
          .fontColor(this.isDarkMode ? '#FFFFFF' : '#222222')
          .backgroundColor('transparent')
          .height(36)
          .padding({ left: 0, right: 0 })
          .layoutWeight(1)
          .onChange((value: string) => {
            this.searchQuery = value;
          })

        if (this.searchQuery.length > 0) {
          Text('✕')
            .fontSize(12)
            .fontColor('#888888')
            .padding(4)
            .onClick(() => {
              this.searchQuery = '';
            })
        }
      }
      .width('100%')
      .height(38)
      .padding({ left: 10, right: 10 })
      .backgroundColor(this.isDarkMode ? '#242C33' : '#F5F7F6')
      .borderRadius(19)
      .margin({ bottom: 12 })

      List({ space: 10 }) {
        ForEach(this.getFilteredPlants(), (plant: PlantRawItem) => {
          ListItem() {
            Row() {
              Stack({ alignContent: Alignment.Center }) {
                Image($r('app.media.flower_pot')).width(20).height(20).fillColor('#FFFFFF').margin({ left: 2 })
              }
              .width(36).height(36).borderRadius(18).backgroundColor('#224A32')

              Column({ space: 2 }) {
                Row({ space: 6 }) {
                  Text(plant.name).fontSize(14).fontWeight(FontWeight.Bold).fontColor(this.isDarkMode ? '#FFFFFF' : '#222222')
                  Text(plant.category).fontSize(10).fontColor('#224A32').backgroundColor('rgba(34,74,50,0.12)').padding({ left: 6, right: 6, top: 2, bottom: 2 }).borderRadius(6)
                }
                Text(plant.desc).fontSize(11).fontColor('#888888').maxLines(1).textOverflow({ overflow: TextOverflow.Ellipsis })
              }.alignItems(HorizontalAlign.Start).margin({ left: 10 }).layoutWeight(1)

              Text('绑定').fontSize(12).fontWeight(FontWeight.Bold).fontColor('#FFFFFF').padding({ left: 14, right: 14, top: 5, bottom: 5 }).backgroundColor('#224A32').borderRadius(14)
            }
            .width('100%').padding(10).backgroundColor(this.isDarkMode ? '#242C33' : '#F5F7F6').borderRadius(12)
            .onClick(() => {
              if (this.targetBindingDevice) {
                let targetId = this.targetBindingDevice.id;
                this.availableDevices = this.availableDevices.map((dev: SimulatedDeviceNode) => {
                  if (dev.id === targetId) {
                    dev.isBound = true;
                    dev.boundPlantName = plant.name;
                    dev.name = `${plant.name}`;
                  }
                  return dev;
                });
                this.availableDevices = [...this.availableDevices];
                this.boundDevicesCount = this.availableDevices.filter((d: SimulatedDeviceNode) => d.isBound).length;
                let remainingUnbound = this.availableDevices.some((d: SimulatedDeviceNode) => !d.isBound);
                this.isSelectPlantSheetOpen = false;
                if (remainingUnbound) {
                  this.isAddDeviceSheetOpen = true;
                } else {
                  this.isAddDeviceSheetOpen = false;
                }
                try {
                  promptAction.showToast({ message: `已成功绑定：${plant.name}`, duration: 2500 });
                } catch (e) {}
              }
            })
          }
        })
      }.width('100%').layoutWeight(1)
    }.padding(20).width('100%').height('100%').backgroundColor(this.isDarkMode ? '#1E2226' : '#FFFFFF')
  }
}
