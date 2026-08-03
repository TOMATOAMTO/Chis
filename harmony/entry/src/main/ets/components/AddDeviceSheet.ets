// AddDeviceSheet.ets
import { SimulatedDeviceNode } from './PlantSelectSheet';

@Component
export struct AddDeviceSheet {
  @Link isAddDeviceSheetOpen: boolean;
  @Link isScanning: boolean;
  @Link availableDevices: SimulatedDeviceNode[];
  @Link targetBindingDevice: SimulatedDeviceNode | null;
  @Link isSelectPlantSheetOpen: boolean;
  @Link isDarkMode: boolean;
  @Link isEnglish: boolean;

  @State private radarScale: number = 1.0;

  aboutToAppear() {
    if (this.isScanning) {
      this.radarScale = 1.0;
      setTimeout(() => {
        this.runRadarPulse();
      }, 200);
    }
  }

  private runRadarPulse() {
    if (!this.isScanning || !this.isAddDeviceSheetOpen) return;
    animateTo({
      duration: 1400,
      curve: Curve.EaseInOut,
      onFinish: () => {
        if (this.isScanning && this.isAddDeviceSheetOpen) {
          this.radarScale = this.radarScale === 1.0 ? 1.4 : 1.0;
          this.runRadarPulse();
        }
      }
    }, () => {
      this.radarScale = this.radarScale === 1.0 ? 1.4 : 1.0;
    });
  }

  private getShortDeviceName(rawName: string): string {
    if (!rawName) return '智能设备';
    if (rawName.includes('SmartPot_01')) return 'SmartPot_01';
    if (rawName.includes('PlantSensor_02')) return 'PlantSensor_02';
    let parts = rawName.split('_');
    return parts[parts.length - 1] || rawName;
  }

  build() {
    Column() {
      Row() {
        Text(this.isEnglish ? 'Add Smart Device' : '添加智能设备')
          .fontSize(18)
          .fontWeight(FontWeight.Bold)
          .fontColor(this.isDarkMode ? '#FFFFFF' : '#222222')

        Blank()

        Image($r('app.media.left'))
          .width(20)
          .height(20)
          .fillColor(this.isDarkMode ? '#FFFFFF' : '#333333')
          .onClick(() => { this.isAddDeviceSheetOpen = false; })
      }.width('100%').margin({ bottom: 12 })

      if (this.isScanning) {
        Column() {
          Text(this.isEnglish ? 'Bluetooth BLE Scanning' : '蓝牙雷达扫描')
            .fontSize(15).fontWeight(FontWeight.Bold).fontColor(this.isDarkMode ? '#FFFFFF' : '#222222').margin({ top: -22, bottom: 44 })

          Stack({ alignContent: Alignment.Center }) {
            Row()
              .width(130)
              .height(130)
              .borderRadius(65)
              .backgroundColor('rgba(34, 74, 50, 0.12)')
              .scale({ x: this.radarScale * 1.2, y: this.radarScale * 1.2 })

            Row()
              .width(90)
              .height(90)
              .borderRadius(45)
              .backgroundColor('rgba(34, 74, 50, 0.22)')
              .scale({ x: this.radarScale, y: this.radarScale })

            Stack({ alignContent: Alignment.Center }) {
              Image($r('app.media.flower_pot'))
                .width(26)
                .height(26)
                .fillColor('#FFFFFF')
                .margin({ left: 4 })
            }
            .width(54)
            .height(54)
            .borderRadius(27)
            .backgroundColor('#224A32')
          }
          .width(160)
          .height(160)
          .margin({ top: 4, bottom: 12 })

          Text(this.isEnglish ? 'Searching for nearby smart pots...' : '正在搜索 10cm 内的智能花盆与检测仪...')
            .fontSize(13)
            .fontColor(this.isDarkMode ? '#8AB399' : '#666666')
            .margin({ top: 48, bottom: 16 })
        }
        .width('100%')
        .justifyContent(FlexAlign.Center)
        .layoutWeight(1)
      } else {
        Column({ space: 12 }) {
          Row() {
            Text(this.isEnglish ? 'Discovered Nearby Hardware' : '已搜到附近智能设备')
              .fontSize(13).fontWeight(FontWeight.Bold).fontColor('#888888')
            Blank()
          }.width('100%').margin({ bottom: 4 })

          ForEach(this.availableDevices, (device: SimulatedDeviceNode) => {
            Row() {
              Row({ space: 12 }) {
                Stack({ alignContent: Alignment.Center }) {
                  Image($r('app.media.flower_pot'))
                    .width(21)
                    .height(21)
                    .fillColor('#FFFFFF')
                    .margin({ left: 3.5 })
                }
                .width(40)
                .height(40)
                .borderRadius(20)
                .backgroundColor('#224A32')

                Column({ space: 3 }) {
                  Text(this.getShortDeviceName(device.devHardwareName))
                    .fontSize(15)
                    .fontWeight(FontWeight.Bold)
                    .fontColor(this.isDarkMode ? '#FFFFFF' : '#222222')

                  Row({ space: 4 }) {
                    Text(device.type)
                      .fontSize(11)
                      .fontColor('#888888')
                      .fontWeight(FontWeight.Medium)

                    if (device.isBound && device.boundPlantName) {
                      Text(`· 已绑定: ${device.boundPlantName}`)
                        .fontSize(11)
                        .fontColor('#224A32')
                        .fontWeight(FontWeight.Bold)
                    }
                  }
                }.alignItems(HorizontalAlign.Start)
              }

              Blank()

              if (!device.isBound) {
                Button(this.isEnglish ? 'Bind' : '一键绑定')
                  .fontSize(12)
                  .fontWeight(FontWeight.Bold)
                  .fontColor('#FFFFFF')
                  .backgroundColor('#224A32')
                  .borderRadius(16)
                  .height(32)
                  .padding({ left: 14, right: 14 })
                  .onClick(() => {
                    this.targetBindingDevice = device;
                    this.isAddDeviceSheetOpen = false;
                    this.isSelectPlantSheetOpen = true;
                  })
              } else {
                Button(this.isEnglish ? 'Bound' : '已绑定')
                  .fontSize(12)
                  .fontColor('rgba(255,255,255,0.7)')
                  .backgroundColor('rgba(34,74,50,0.35)')
                  .borderRadius(16)
                  .height(32)
                  .padding({ left: 14, right: 14 })
                  .enabled(false)
              }
            }
            .width('100%')
            .padding(12)
            .backgroundColor(this.isDarkMode ? '#242C33' : '#F5F7F6')
            .borderRadius(14)
          })
        }
        .width('100%')
        .layoutWeight(1)
      }
    }
    .padding(24)
    .width('100%')
    .height('100%')
    .backgroundColor(this.isDarkMode ? '#1E2226' : '#FFFFFF')
  }
}
