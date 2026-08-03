// TrendsTab.ets - Apple Health / iOS 17 Premium Plant Telemetry Analytics Tab
import { SimulatedDeviceNode } from './PlantSelectSheet';
import { TelemetryService } from '../services/TelemetryService';

interface HistoryPoint {
  time: string;
  moisture: number;
  temp: number;
  light: number;
  npk: number;
}

interface DeviceAnalytics {
  moistureHistory: HistoryPoint[];
  moisture7dHistory: HistoryPoint[];
  moisture30dHistory: HistoryPoint[];
  moistureAvg: string;
  currentMoisture: string;
  lightPeak: string;
  tempAvg: string;
  dryCycle: string;
  sunlightHours: string;
  nextWatering: string;
  growthScore: string;
}

@Component
export struct TrendsTab {
  @Prop isDarkMode: boolean = false;
  @Prop isEnglish: boolean = false;
  @Link availableDevices: SimulatedDeviceNode[];

  @State selectedDeviceIndex: number = 0;
  @State selectedTimeRange: number = 0; // 0: 24h, 1: 7d, 2: 30d

  // 区分设备的独立历史 24 小时 / 7天 / 30天 监控数据模板
  private deviceDataMap: DeviceAnalytics[] = [
    {
      moistureHistory: [
        { time: '22:00', moisture: 62, temp: 22.5, light: 0, npk: 880 },
        { time: '01:00', moisture: 60, temp: 21.8, light: 120, npk: 880 },
        { time: '04:00', moisture: 58, temp: 24.2, light: 2400, npk: 875 },
        { time: '07:00', moisture: 75, temp: 26.5, light: 5200, npk: 870 },
        { time: '10:00', moisture: 72, temp: 25.0, light: 1800, npk: 868 },
        { time: '13:00', moisture: 68, temp: 23.4, light: 50, npk: 865 },
        { time: '实时', moisture: 65, temp: 24.8, light: 3200, npk: 880 }
      ],
      moisture7dHistory: [
        { time: '07-13', moisture: 68, temp: 23.0, light: 4200, npk: 890 },
        { time: '07-14', moisture: 65, temp: 24.1, light: 4500, npk: 885 },
        { time: '07-15', moisture: 61, temp: 23.8, light: 3800, npk: 880 },
        { time: '07-16', moisture: 78, temp: 25.2, light: 5100, npk: 875 },
        { time: '07-17', moisture: 72, temp: 24.5, light: 4800, npk: 870 },
        { time: '07-18', moisture: 67, temp: 24.0, light: 4300, npk: 868 },
        { time: '今天', moisture: 65, temp: 24.8, light: 3200, npk: 880 }
      ],
      moisture30dHistory: [
        { time: '4周前', moisture: 70, temp: 22.8, light: 4100, npk: 900 },
        { time: '3周前', moisture: 64, temp: 23.5, light: 4600, npk: 890 },
        { time: '2周前', moisture: 69, temp: 24.2, light: 4800, npk: 880 },
        { time: '上周', moisture: 66, temp: 24.0, light: 4300, npk: 872 },
        { time: '本周', moisture: 65, temp: 24.8, light: 3200, npk: 880 }
      ],
      moistureAvg: '65%',
      currentMoisture: '65%',
      lightPeak: '5200 LUX',
      tempAvg: '24.8°C',
      dryCycle: '水分极佳',
      sunlightHours: '6.2 小时',
      nextWatering: '后天 10:00',
      growthScore: '98 分 · 生长极佳'
    },
    {
      moistureHistory: [
        { time: '22:00', moisture: 45, temp: 24.0, light: 0, npk: 650 },
        { time: '01:00', moisture: 43, temp: 23.2, light: 300, npk: 645 },
        { time: '04:00', moisture: 40, temp: 26.0, light: 3600, npk: 640 },
        { time: '07:00', moisture: 38, temp: 28.5, light: 6500, npk: 630 },
        { time: '10:00', moisture: 55, temp: 27.0, light: 2100, npk: 625 },
        { time: '13:00', moisture: 48, temp: 25.2, light: 0, npk: 622 },
        { time: '实时', moisture: 42, temp: 26.0, light: 4500, npk: 620 }
      ],
      moisture7dHistory: [
        { time: '07-13', moisture: 52, temp: 25.0, light: 5800, npk: 680 },
        { time: '07-14', moisture: 48, temp: 25.5, light: 6100, npk: 670 },
        { time: '07-15', moisture: 44, temp: 26.2, light: 6300, npk: 660 },
        { time: '07-16', moisture: 60, temp: 24.8, light: 5900, npk: 650 },
        { time: '07-17', moisture: 53, temp: 25.8, light: 6200, npk: 640 },
        { time: '07-18', moisture: 47, temp: 26.5, light: 6400, npk: 630 },
        { time: '今天', moisture: 42, temp: 26.0, light: 4500, npk: 620 }
      ],
      moisture30dHistory: [
        { time: '4周前', moisture: 55, temp: 24.5, light: 5600, npk: 710 },
        { time: '3周前', moisture: 50, temp: 25.2, light: 6000, npk: 690 },
        { time: '2周前', moisture: 48, temp: 25.8, light: 6200, npk: 670 },
        { time: '上周', moisture: 53, temp: 26.0, light: 6100, npk: 650 },
        { time: '本周', moisture: 42, temp: 26.0, light: 4500, npk: 620 }
      ],
      moistureAvg: '45%',
      currentMoisture: '42%',
      lightPeak: '6500 LUX',
      tempAvg: '26.0°C',
      dryCycle: '偏干缺水',
      sunlightHours: '7.5 小时',
      nextWatering: '明天 14:00',
      growthScore: '85 分 · 需补水'
    }
  ];

  private timeRangeLabels: string[] = ['24小时', '近 7 天', '近 30 天'];

  // 生成符合时间轴逻辑的 24 小时动态滚动时间标签
  private getRollingTimestamps(): string[] {
    let now = new Date();
    let curHour = now.getHours();
    let timestamps: string[] = [];
    for (let i = 6; i >= 1; i--) {
      let h = (curHour - i * 3 + 24) % 24;
      let hStr = h < 10 ? `0${h}:00` : `${h}:00`;
      timestamps.push(hStr);
    }
    timestamps.push('实时');
    return timestamps;
  }

  // 获取当前所选时间范围对应的历史数据
  private getActiveHistoryPoints(baseData: DeviceAnalytics): HistoryPoint[] {
    if (this.selectedTimeRange === 1) {
      return baseData.moisture7dHistory;
    } else if (this.selectedTimeRange === 2) {
      return baseData.moisture30dHistory;
    }
    return baseData.moistureHistory;
  }

  // 动态融合华为云平台真实数据并实时计算生长分数
  private getActiveAnalytics(): DeviceAnalytics {
    let boundList = this.availableDevices.filter(d => d.isBound);
    if (boundList.length === 0) return this.deviceDataMap[0];
    let safeIdx = Math.min(this.selectedDeviceIndex, boundList.length - 1);
    let activeDev = boundList[safeIdx];
    let baseData = this.deviceDataMap[safeIdx % this.deviceDataMap.length];

    // 更新逻辑 24h 滚动时间标签
    let timestamps = this.getRollingTimestamps();
    for (let i = 0; i < baseData.moistureHistory.length; i++) {
      baseData.moistureHistory[i].time = timestamps[i];
    }

    // 将华为云最新的实时读数动态写入历史趋势最后一个节点
    let realM = parseFloat(activeDev.moisture);
    let realT = parseFloat(activeDev.temp);
    let realL = parseFloat(activeDev.light);

    if (!isNaN(realM)) {
      baseData.moistureHistory[baseData.moistureHistory.length - 1].moisture = Math.round(realM);
      baseData.moisture7dHistory[baseData.moisture7dHistory.length - 1].moisture = Math.round(realM);
      baseData.moisture30dHistory[baseData.moisture30dHistory.length - 1].moisture = Math.round(realM);
      baseData.currentMoisture = `${Math.round(realM)}%`;
    }
    if (!isNaN(realT)) {
      baseData.moistureHistory[baseData.moistureHistory.length - 1].temp = parseFloat(realT.toFixed(1));
      baseData.moisture7dHistory[baseData.moisture7dHistory.length - 1].temp = parseFloat(realT.toFixed(1));
      baseData.moisture30dHistory[baseData.moisture30dHistory.length - 1].temp = parseFloat(realT.toFixed(1));
      baseData.tempAvg = `${realT.toFixed(1)}°C`;
    }
    if (!isNaN(realL)) {
      baseData.moistureHistory[baseData.moistureHistory.length - 1].light = Math.round(realL);
      baseData.moisture7dHistory[baseData.moisture7dHistory.length - 1].light = Math.round(realL);
      baseData.moisture30dHistory[baseData.moisture30dHistory.length - 1].light = Math.round(realL);
      baseData.lightPeak = `${Math.round(realL)} LUX`;
    }

    // 动态计算综合健康生长得分
    baseData.growthScore = this.calculateGrowthScore(
      activeDev.moisture,
      activeDev.temp,
      activeDev.light,
      activeDev.npk
    );

    return baseData;
  }

  private getActiveDevice(): SimulatedDeviceNode {
    let boundList = this.availableDevices.filter(d => d.isBound);
    let safeIdx = Math.min(this.selectedDeviceIndex, Math.max(0, boundList.length - 1));
    return boundList[safeIdx];
  }

  // 动态评估湿度状态标签 (纯文本无括号)
  private getMoistureStatusCleanText(valStr: string): string {
    let num = parseFloat(valStr);
    if (isNaN(num)) return '水分极佳';
    if (num < 30) return '干旱缺水';
    if (num > 75) return '积水过涝';
    return '水分极佳';
  }

  private getMoistureStatusColor(valStr: string): string {
    let num = parseFloat(valStr);
    if (isNaN(num)) return '#34C759';
    if (num < 30) return '#FF9500';
    if (num > 75) return '#FF3B30';
    return '#34C759';
  }

  // 动态评估下次浇水建议 (根据湿度数值实时精准预测)
  private getNextWateringCleanText(valStr: string): string {
    let num = parseFloat(valStr);
    if (isNaN(num)) return '后天 10:00';
    if (num < 20) return '立即透浇';
    if (num < 35) return '今天 18:00';
    if (num < 50) return '明天 10:00';
    if (num < 70) return '后天 10:00';
    return '4天后 10:00';
  }

  // 动态打分系统
  private calculateGrowthScore(mStr: string, tStr: string, lStr: string, nStr: string): string {
    let score = 100;
    let m = TelemetryService.getMoistureDiagnostic(mStr);
    let t = TelemetryService.getTempDiagnostic(tStr);
    let l = TelemetryService.getLightDiagnostic(lStr);
    let n = TelemetryService.getNpkDiagnostic(nStr);

    if (m.status !== 'NORMAL') score -= 25;
    if (t.status !== 'NORMAL') score -= 15;
    if (l.status !== 'NORMAL') score -= 15;
    if (n.status !== 'NORMAL') score -= 20;

    if (score >= 90) return `${score} 分 · 生长极佳`;
    if (score >= 70) return `${score} 分 · 状态良好`;
    if (score >= 50) return `${score} 分 · 偏旱缺肥`;
    return `${score} 分 · 需紧急关照`;
  }

  build() {
    Column() {
      // 1. Apple-Style Header
      Row() {
        Column({ space: 4 }) {
          Text(this.isEnglish ? 'Plant Data' : '养护数据')
            .fontSize(26)
            .fontWeight(FontWeight.Bold)
            .fontColor(this.isDarkMode ? '#FFFFFF' : '#1C1C1E')
          Text(this.isEnglish ? '24h Real-Time Ecosystem & Telemetry' : '24 小时实时体征与生态监测')
            .fontSize(12)
            .fontColor(this.isDarkMode ? 'rgba(255,255,255,0.5)' : '#8E8E93')
        }.alignItems(HorizontalAlign.Start)
      }
      .width('100%')
      .padding({ left: 20, right: 20, top: 48, bottom: 12 })

      if (this.availableDevices.filter(d => d.isBound).length === 0) {
        Column({ space: 16 }) {
          Image($r('app.media.flower_pot')).width(64).height(64).fillColor(this.isDarkMode ? '#58B582' : '#1E5F41')
          Text(this.isEnglish ? 'No Bound Smart Devices' : '暂无已绑定设备的数据').fontSize(16).fontWeight(FontWeight.Bold).fontColor(this.isDarkMode ? '#FFFFFF' : '#333333')
          Text(this.isEnglish ? 'Please add and bind a smart device on the Home tab first.' : '请先在【主页】绑定智能设备，即可解锁 24 小时实时生态数据图表与健康报告。')
            .fontSize(13).fontColor('#8E8E93').textAlign(TextAlign.Center).lineHeight(18).margin({ left: 32, right: 32 })
        }
        .width('100%')
        .layoutWeight(1)
        .justifyContent(FlexAlign.Center)
      } else {
        Scroll() {
          Column({ space: 14 }) {
            // 2. 苹果风多设备横向 Pill 切换器 (高对比度清晰视觉)
            Row({ space: 8 }) {
              ForEach(this.availableDevices.filter(d => d.isBound), (dev: SimulatedDeviceNode, index: number) => {
                Row({ space: 6 }) {
                  Image($r('app.media.flower_pot')).width(14).height(14)
                    .fillColor(this.selectedDeviceIndex === index ? '#FFFFFF' : (this.isDarkMode ? '#58B582' : '#1E5F41'))
                  Text(dev.name)
                    .fontSize(13)
                    .fontWeight(FontWeight.Bold)
                    .fontColor(this.selectedDeviceIndex === index ? '#FFFFFF' : (this.isDarkMode ? '#E5E5EA' : '#1E5F41'))
                }
                .padding({ left: 16, right: 16, top: 8, bottom: 8 })
                .borderRadius(20)
                .backgroundColor(this.selectedDeviceIndex === index ?
                  (this.isDarkMode ? '#34C759' : '#1E5F41') :
                  (this.isDarkMode ? '#1C1C1E' : '#FFFFFF'))
                .border({
                  width: 1.5,
                  color: this.selectedDeviceIndex === index ?
                    (this.isDarkMode ? '#34C759' : '#1E5F41') :
                    (this.isDarkMode ? '#3A3A3C' : 'rgba(34,74,50,0.25)')
                })
                .shadow(this.selectedDeviceIndex !== index ? { radius: 3, color: 'rgba(0,0,0,0.03)', offsetY: 1 } : { radius: 0, color: Color.Transparent })
                .onClick(() => {
                  animateTo({ duration: 250, curve: Curve.FastOutSlowIn }, () => {
                    this.selectedDeviceIndex = index;
                  });
                })
              })
            }
            .width('100%')
            .padding({ left: 20, right: 20 })

            // 3. Apple Health 平滑滑动 Pill Segmented Control (时间范围)
            Stack({ alignContent: Alignment.TopStart }) {
              // 平滑滑动的白底浮动胶囊
              Row()
                .width('33.33%')
                .height(30)
                .borderRadius(11)
                .backgroundColor(this.isDarkMode ? '#2C2C2E' : '#FFFFFF')
                .shadow({ radius: 4, color: 'rgba(0,0,0,0.08)', offsetY: 1 })
                .offset({ x: `${this.selectedTimeRange * 33.33}%` })
                .animation({ duration: 250, curve: Curve.FastOutSlowIn })

              Row() {
                ForEach(this.timeRangeLabels, (label: string, idx: number) => {
                  Text(label)
                    .fontSize(12)
                    .fontWeight(this.selectedTimeRange === idx ? FontWeight.Bold : FontWeight.Regular)
                    .fontColor(this.selectedTimeRange === idx ? (this.isDarkMode ? '#FFFFFF' : '#1C1C1E') : (this.isDarkMode ? 'rgba(255,255,255,0.45)' : '#8E8E93'))
                    .layoutWeight(1)
                    .textAlign(TextAlign.Center)
                    .height(30)
                    .onClick(() => {
                      animateTo({ duration: 250, curve: Curve.FastOutSlowIn }, () => {
                        this.selectedTimeRange = idx;
                      });
                    })
                })
              }.width('100%')
            }
            .width('90%')
            .padding(3)
            .backgroundColor(this.isDarkMode ? '#1C1C1E' : '#EFEFF4')
            .borderRadius(14)

            // 4. Apple Health Hero Card 1 - 💧 土壤湿度概览
            Column({ space: 12 }) {
              Row() {
                Column({ space: 4 }) {
                  Row({ space: 6 }) {
                    Image($r('app.media.humidity')).width(15).height(15).fillColor(this.getMoistureStatusColor(this.getActiveDevice().moisture))
                    Text(this.selectedTimeRange === 1 ? '7天土壤湿度演变' : (this.selectedTimeRange === 2 ? '30天月度湿度周期' : '土壤湿度概览'))
                      .fontSize(13)
                      .fontWeight(FontWeight.Bold)
                      .fontColor(this.isDarkMode ? 'rgba(255,255,255,0.6)' : '#8E8E93')
                  }
                  Text(this.getActiveDevice().moisture)
                    .fontSize(34)
                    .fontWeight(FontWeight.Bold)
                    .fontColor(this.isDarkMode ? '#FFFFFF' : '#1C1C1E')
                    .margin({ top: 2 })
                }.alignItems(HorizontalAlign.Start)
              }.width('100%')

              // Apple Vertical Pill Histogram
              Row({ space: 10 }) {
                ForEach(this.getActiveHistoryPoints(this.getActiveAnalytics()), (pt: HistoryPoint) => {
                  Column({ space: 6 }) {
                    Text(`${pt.moisture}%`).fontSize(9).fontColor(this.isDarkMode ? 'rgba(255,255,255,0.4)' : '#8E8E93')
                    Stack({ alignContent: Alignment.Bottom }) {
                      Row()
                        .width('100%')
                        .height('100%')
                        .borderRadius(6)
                        .backgroundColor(this.isDarkMode ? 'rgba(255,255,255,0.06)' : '#F2F2F7')
                      Row()
                        .width('100%')
                        .height(`${Math.max(8, pt.moisture)}%`)
                        .borderRadius(6)
                        .backgroundColor(pt.moisture > 70 ? '#30B0C7' : (pt.moisture < 30 ? '#FF9500' : '#34C759'))
                    }
                    .height(86)
                    .width(18)

                    Text(pt.time).fontSize(9.5).fontColor(this.isDarkMode ? 'rgba(255,255,255,0.5)' : '#8E8E93')
                  }.layoutWeight(1)
                })
              }
              .width('100%')
              .padding({ top: 8, bottom: 4 })
            }
            .width('90%')
            .padding(18)
            .backgroundColor(this.isDarkMode ? '#1C1C1E' : '#FFFFFF')
            .borderRadius(20)
            .border({ width: 1, color: this.isDarkMode ? 'rgba(255,255,255,0.08)' : 'rgba(0,0,0,0.03)' })
            .shadow({ radius: 10, color: this.isDarkMode ? 'rgba(0,0,0,0.3)' : 'rgba(0,0,0,0.03)', offsetY: 4 })

            // 5. Apple Weather Style Bento Grid - 光照(#FFCC00) & 温度(#FF9500)
            GridRow({ columns: 2, gutter: 12 }) {
              // ☀️ 日光照射便当盒卡片 (亮黄色 #FFCC00)
              GridCol() {
                Column({ space: 10 }) {
                  Row({ space: 6 }) {
                    Image($r('app.media.brightness')).width(14).height(14).fillColor('#FFCC00')
                    Text('日光照射').fontSize(12).fontWeight(FontWeight.Bold).fontColor(this.isDarkMode ? 'rgba(255,255,255,0.6)' : '#8E8E93')
                  }.width('100%')

                  Text(`${this.getActiveDevice().light} LUX`)
                    .fontSize(22)
                    .fontWeight(FontWeight.Bold)
                    .fontColor(this.isDarkMode ? '#FFFFFF' : '#1C1C1E')
                    .width('100%')

                  // Mini Sunlight Yellow Pill Sparkline
                  Row({ space: 4 }) {
                    ForEach(this.getActiveHistoryPoints(this.getActiveAnalytics()), (pt: HistoryPoint) => {
                      Stack({ alignContent: Alignment.Bottom }) {
                        Row().width('100%').height('100%').borderRadius(3).backgroundColor(this.isDarkMode ? 'rgba(255,255,255,0.06)' : '#F2F2F7')
                        Row().width('100%').height(`${Math.min(100, Math.max(12, (pt.light / 6500) * 100))}%`).borderRadius(3).backgroundColor('#FFCC00')
                      }.height(42).layoutWeight(1)
                    })
                  }.width('100%').margin({ top: 4 })
                }
                .padding(14)
                .backgroundColor(this.isDarkMode ? '#1C1C1E' : '#FFFFFF')
                .borderRadius(18)
                .border({ width: 1, color: this.isDarkMode ? 'rgba(255,255,255,0.08)' : 'rgba(0,0,0,0.03)' })
                .shadow({ radius: 8, color: this.isDarkMode ? 'rgba(0,0,0,0.3)' : 'rgba(0,0,0,0.03)', offsetY: 3 })
              }

              // 🌡️ 环境温度便当盒卡片 (暖橙色 #FF9500)
              GridCol() {
                Column({ space: 10 }) {
                  Row({ space: 6 }) {
                    Image($r('app.media.temperature')).width(14).height(14).fillColor('#FF9500')
                    Text('环境温度').fontSize(12).fontWeight(FontWeight.Bold).fontColor(this.isDarkMode ? 'rgba(255,255,255,0.6)' : '#8E8E93')
                  }.width('100%')

                  Text(this.getActiveDevice().temp)
                    .fontSize(22)
                    .fontWeight(FontWeight.Bold)
                    .fontColor(this.isDarkMode ? '#FFFFFF' : '#1C1C1E')
                    .width('100%')

                  // Mini Temperature Orange Pill Sparkline
                  Row({ space: 4 }) {
                    ForEach(this.getActiveHistoryPoints(this.getActiveAnalytics()), (pt: HistoryPoint) => {
                      Stack({ alignContent: Alignment.Bottom }) {
                        Row().width('100%').height('100%').borderRadius(3).backgroundColor(this.isDarkMode ? 'rgba(255,255,255,0.06)' : '#F2F2F7')
                        Row().width('100%').height(`${Math.min(100, Math.max(12, (pt.temp / 35) * 100))}%`).borderRadius(3).backgroundColor('#FF9500')
                      }.height(42).layoutWeight(1)
                    })
                  }.width('100%').margin({ top: 4 })
                }
                .padding(14)
                .backgroundColor(this.isDarkMode ? '#1C1C1E' : '#FFFFFF')
                .borderRadius(18)
                .border({ width: 1, color: this.isDarkMode ? 'rgba(255,255,255,0.08)' : 'rgba(0,0,0,0.03)' })
                .shadow({ radius: 8, color: this.isDarkMode ? 'rgba(0,0,0,0.3)' : 'rgba(0,0,0,0.03)', offsetY: 3 })
              }
            }.width('90%')

            // 6. Apple Health Inset Grouped List - 数据报告 (光照Icon变绿, 浇水Icon变alarm.svg)
            Column({ space: 12 }) {
              Row() {
                Row({ space: 6 }) {
                  Image($r('app.media.tip')).width(15).height(15).fillColor(this.isDarkMode ? '#34C759' : '#1E5F41')
                  Text('数据报告').fontSize(15).fontWeight(FontWeight.Bold).fontColor(this.isDarkMode ? '#FFFFFF' : '#1C1C1E')
                }
                Blank()
                Text(this.getActiveAnalytics().growthScore)
                  .fontSize(12)
                  .fontWeight(FontWeight.Bold)
                  .fontColor(this.getMoistureStatusColor(this.getActiveDevice().moisture))
              }.width('100%')

              Column() {
                // Inset Row 1: 土壤干湿状态
                Row() {
                  Row({ space: 8 }) {
                    Image($r('app.media.humidity')).width(15).height(15).fillColor(this.getMoistureStatusColor(this.getActiveDevice().moisture))
                    Text('土壤干湿状态').fontSize(13.5).fontColor(this.isDarkMode ? '#E5E5EA' : '#1C1C1E')
                  }
                  Blank()
                  Text(this.getMoistureStatusCleanText(this.getActiveDevice().moisture))
                    .fontSize(13.5)
                    .fontWeight(FontWeight.Bold)
                    .fontColor(this.getMoistureStatusColor(this.getActiveDevice().moisture))
                }
                .width('100%')
                .padding({ top: 12, bottom: 12, left: 4, right: 4 })

                Divider().color(this.isDarkMode ? 'rgba(255,255,255,0.08)' : 'rgba(0,0,0,0.06)')

                // Inset Row 2: 日均有效光合时长 (Icon统一为绿色 #34C759)
                Row() {
                  Row({ space: 8 }) {
                    Image($r('app.media.brightness')).width(15).height(15).fillColor('#34C759')
                    Text('日均有效光合时长').fontSize(13.5).fontColor(this.isDarkMode ? '#E5E5EA' : '#1C1C1E')
                  }
                  Blank()
                  Text(this.getActiveAnalytics().sunlightHours)
                    .fontSize(13.5)
                    .fontWeight(FontWeight.Bold)
                    .fontColor('#34C759')
                }
                .width('100%')
                .padding({ top: 12, bottom: 12, left: 4, right: 4 })

                Divider().color(this.isDarkMode ? 'rgba(255,255,255,0.08)' : 'rgba(0,0,0,0.06)')

                // Inset Row 3: 智能预测浇水提醒 (Icon使用 alarm.svg，时间根据湿度动态变化)
                Row() {
                  Row({ space: 8 }) {
                    Image($r('app.media.alarm')).width(15).height(15).fillColor(this.getMoistureStatusColor(this.getActiveDevice().moisture))
                    Text('智能预测浇水提醒').fontSize(13.5).fontColor(this.isDarkMode ? '#E5E5EA' : '#1C1C1E')
                  }
                  Blank()
                  Text(this.getNextWateringCleanText(this.getActiveDevice().moisture))
                    .fontSize(13.5)
                    .fontWeight(FontWeight.Bold)
                    .fontColor(this.getMoistureStatusColor(this.getActiveDevice().moisture))
                }
                .width('100%')
                .padding({ top: 12, bottom: 12, left: 4, right: 4 })
              }
              .width('100%')
            }
            .width('90%')
            .padding(18)
            .backgroundColor(this.isDarkMode ? '#1C1C1E' : '#FFFFFF')
            .borderRadius(20)
            .border({ width: 1, color: this.isDarkMode ? 'rgba(255,255,255,0.08)' : 'rgba(0,0,0,0.03)' })
            .shadow({ radius: 10, color: this.isDarkMode ? 'rgba(0,0,0,0.3)' : 'rgba(0,0,0,0.03)', offsetY: 4 })

          }.width('100%').alignItems(HorizontalAlign.Center).padding({ bottom: 80 })
        }.width('100%').layoutWeight(1)
      }
    }
    .width('100%')
    .height('100%')
    .backgroundColor(this.isDarkMode ? '#000000' : '#F2F2F7')
  }
}
