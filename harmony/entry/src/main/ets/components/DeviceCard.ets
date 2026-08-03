// DeviceCard.ets - Standalone Modular Device Monitor Card Component
import { SimulatedDeviceNode } from './PlantSelectSheet';
import { TelemetryService, MetricDiagnostic, OverallHealth } from '../services/TelemetryService';
import { NotificationService } from '../services/NotificationService';

@Component
export struct DeviceCard {
  @Prop device: SimulatedDeviceNode;
  @Prop isDarkMode: boolean = false;
  @Prop lightUnit: string = 'LUX';

  private mDiag(): MetricDiagnostic {
    return TelemetryService.getMoistureDiagnostic(this.device.moisture);
  }

  private tDiag(): MetricDiagnostic {
    return TelemetryService.getTempDiagnostic(this.device.temp);
  }

  private lDiag(): MetricDiagnostic {
    return TelemetryService.getLightDiagnostic(this.device.light);
  }

  private nDiag(): MetricDiagnostic {
    return TelemetryService.getNpkDiagnostic(this.device.npk);
  }

  private health(): OverallHealth {
    let h = TelemetryService.getOverallDeviceHealth(
      this.device.moisture,
      this.device.temp,
      this.device.light,
      this.device.npk
    );
    if (h.statusText === '注意预警' || h.statusText === '养护关注') {
      NotificationService.publishAnomalyPush(`植物养护预警 · ${this.device.name}`, h.advice);
    }
    return h;
  }

  @State elapsedSeconds: number = 0;
  private timerId: number = -1;

  aboutToAppear() {
    this.timerId = setInterval(() => {
      this.elapsedSeconds += 1;
    }, 1000);
  }

  aboutToDisappear() {
    if (this.timerId !== -1) {
      clearInterval(this.timerId);
    }
  }

  private getElapsedText(): string {
    if (this.elapsedSeconds < 60) {
      return '刚刚';
    } else if (this.elapsedSeconds < 3600) {
      return `${Math.floor(this.elapsedSeconds / 60)}分钟前`;
    } else {
      return `${Math.floor(this.elapsedSeconds / 3600)}小时前`;
    }
  }

  build() {
    Column() {
      // 1. Header with Name & Health Badge & BLE Status
      Row() {
        Row({ space: 8 }) {
          Image($r('app.media.flower_pot')).width(18).height(18).fillColor(this.isDarkMode ? '#58B582' : '#1E5F41')
          Text(this.device.name).fontSize(15).fontWeight(FontWeight.Bold).fontColor(this.isDarkMode ? '#FFFFFF' : '#222222')
        }
        Blank()
        Row({ space: 6 }) {
          Row({ space: 4 }) {
            Row().width(6).height(6).borderRadius(3).backgroundColor(this.health().color)
            Text(this.health().statusText).fontSize(11).fontColor(this.health().color).fontWeight(FontWeight.Bold)
          }.padding({ left: 8, right: 8, top: 3, bottom: 3 }).backgroundColor(this.health().badgeBg).borderRadius(10)

          Row({ space: 3 }) {
            Row().width(5).height(5).borderRadius(2.5).backgroundColor('#4CAF50')
            Text('BLE 在线').fontSize(10).fontColor('#4CAF50').fontWeight(FontWeight.Medium)
          }
        }
      }.width('100%').margin({ bottom: 12 })

      // 2. Metrics 2x2 Grid
      GridRow({ columns: 2, gutter: 10 }) {
        GridCol() {
          Column({ space: 2 }) {
            Row({ space: 6 }) {
              Stack({ alignContent: Alignment.Center }) {
                Image($r('app.media.humidity')).width(14).height(14).fillColor(this.isDarkMode ? '#58B582' : '#1E5F41')
              }.width(18).height(18)
              Text('土壤湿度').fontSize(11).fontColor(this.isDarkMode ? 'rgba(255,255,255,0.6)' : '#666666')
              Blank()
              Text(this.mDiag().label).fontSize(9.5).fontWeight(FontWeight.Bold).fontColor(this.mDiag().color)
            }.width('100%')
            Text(this.device.moisture).fontSize(16).fontWeight(FontWeight.Bold).fontColor(this.isDarkMode ? '#FFFFFF' : '#222222')
          }
          .width('100%').padding(10).backgroundColor(this.isDarkMode ? 'rgba(255,255,255,0.04)' : '#F5F7F6').borderRadius(10)
          .alignItems(HorizontalAlign.Start)
        }
        GridCol() {
          Column({ space: 2 }) {
            Row({ space: 6 }) {
              Stack({ alignContent: Alignment.Center }) {
                Image($r('app.media.temperature')).width(14).height(14).fillColor(this.isDarkMode ? '#58B582' : '#1E5F41')
              }.width(18).height(18)
              Text('土壤温度').fontSize(11).fontColor(this.isDarkMode ? 'rgba(255,255,255,0.6)' : '#666666')
              Blank()
              Text(this.tDiag().label).fontSize(9.5).fontWeight(FontWeight.Bold).fontColor(this.tDiag().color)
            }.width('100%')
            Text(this.device.temp).fontSize(16).fontWeight(FontWeight.Bold).fontColor(this.isDarkMode ? '#FFFFFF' : '#222222')
          }
          .width('100%').padding(10).backgroundColor(this.isDarkMode ? 'rgba(255,255,255,0.04)' : '#F5F7F6').borderRadius(10)
          .alignItems(HorizontalAlign.Start)
        }
        GridCol() {
          Column({ space: 2 }) {
            Row({ space: 6 }) {
              Stack({ alignContent: Alignment.Center }) {
                Image($r('app.media.brightness')).width(15).height(15).fillColor(this.isDarkMode ? '#58B582' : '#1E5F41')
              }.width(18).height(18)
              Text('光照强度').fontSize(11).fontColor(this.isDarkMode ? 'rgba(255,255,255,0.6)' : '#666666')
              Blank()
              Text(this.lDiag().label).fontSize(9.5).fontWeight(FontWeight.Bold).fontColor(this.lDiag().color)
            }.width('100%')
            Text(this.lightUnit === 'PAR' ? `${(parseInt(this.device.light) * 0.0185).toFixed(1)} PAR` : `${this.device.light} LUX`).fontSize(16).fontWeight(FontWeight.Bold).fontColor(this.isDarkMode ? '#FFFFFF' : '#222222')
          }
          .width('100%').padding(10).backgroundColor(this.isDarkMode ? 'rgba(255,255,255,0.04)' : '#F5F7F6').borderRadius(10)
          .alignItems(HorizontalAlign.Start)
        }
        GridCol() {
          Column({ space: 2 }) {
            Row({ space: 6 }) {
              Stack({ alignContent: Alignment.Center }) {
                Image($r('app.media.nutrition')).width(14).height(14).fillColor(this.isDarkMode ? '#58B582' : '#1E5F41')
              }.width(18).height(18)
              Text('土壤养分').fontSize(11).fontColor(this.isDarkMode ? 'rgba(255,255,255,0.6)' : '#666666')
              Blank()
              Text(this.nDiag().label).fontSize(9.5).fontWeight(FontWeight.Bold).fontColor(this.nDiag().color)
            }.width('100%')
            Text(this.device.npk).fontSize(16).fontWeight(FontWeight.Bold).fontColor(this.isDarkMode ? '#FFFFFF' : '#222222')
          }
          .width('100%').padding(10).backgroundColor(this.isDarkMode ? 'rgba(255,255,255,0.04)' : '#F5F7F6').borderRadius(10)
          .alignItems(HorizontalAlign.Start)
        }
      }.width('100%').margin({ bottom: 10 })

      // 3. HarmonyOS System Push Notification Card Style
      Column({ space: 6 }) {
        Row({ space: 6 }) {
          Image($r('app.media.tip')).width(13).height(13).fillColor(this.health().color)
          Text('养护诊断通知').fontSize(11).fontWeight(FontWeight.Bold).fontColor(this.health().color)
          Blank()
          Text(this.getElapsedText()).fontSize(10).fontColor(this.isDarkMode ? 'rgba(255,255,255,0.4)' : '#999999')
        }.width('100%')

        Text(this.health().advice)
          .fontSize(11.5)
          .fontColor(this.isDarkMode ? 'rgba(255,255,255,0.85)' : '#333333')
          .lineHeight(16)
          .width('100%')
      }
      .width('100%')
      .padding(10)
      .backgroundColor(this.isDarkMode ? 'rgba(255,255,255,0.05)' : '#F2F6F3')
      .borderRadius(10)
      .border({ width: 1, color: this.health().color + '33' })
    }
    .width('100%')
    .padding(14)
    .backgroundColor(this.isDarkMode ? '#1E2226' : '#FFFFFF')
    .borderRadius(16)
    .border({ width: 1, color: this.isDarkMode ? '#283038' : '#EEEEEE' })
  }
}
