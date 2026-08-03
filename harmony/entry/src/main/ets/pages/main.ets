// main.ets
import { router, promptAction } from '@kit.ArkUI';
import { util } from '@kit.ArkTS';
import { common } from '@kit.AbilityKit';
import { http } from '@kit.NetworkKit';
import { QWeatherCard } from '../components/QWeatherCard';
import { PlantEncyclopedia, PlantApiItem } from '../components/PlantEncyclopedia';

import { CitySelectSheet, CityNode } from '../components/CitySelectSheet';
import { PlantSelectSheet, PlantRawItem, SimulatedDeviceNode } from '../components/PlantSelectSheet';
import { AddDeviceSheet } from '../components/AddDeviceSheet';
import { SettingSheet } from '../components/SettingSheet';
import { AboutSheet } from '../components/AboutSheet';
import { DeviceCard } from '../components/DeviceCard';
import { TrendsTab } from '../components/TrendsTab';

const TAB_TEXT_SIZE: number = 12;
const TAB_ITEM_HEIGHT: number = 56;
const TAB_ICON_MARGIN_BOTTOM: number = 4;

interface TabItem { title: string; enTitle: string; icon: Resource; pad: number; iconSize?: number; offsetY?: number; }

interface MetricDiagnostic {
  status: 'LOW' | 'NORMAL' | 'HIGH';
  label: string;
  text: string;
  color: string;
}

interface OverallHealth {
  statusText: string;
  advice: string;
  color: string;
  badgeBg: string;
}

// ====== 华为云 API 嵌套接口结构（化用自 AirHttp.ets） ======
interface HwAccessKey { key: string; }
interface HwSecretKey { key: string; }
interface HwAkSk { access: HwAccessKey; secret: HwSecretKey; }
interface HwIdentity { methods: string[]; hw_ak_sk: HwAkSk; }
interface HwProject { name: string; }
interface HwScope { project: HwProject; }
interface HwAuthBody { identity: HwIdentity; scope: HwScope; }
interface HwAuthRequest { auth: HwAuthBody; }

interface PlantProperties {
  moisture?: number | string;
  temperature?: number | string;
  light?: number | string;
  npk?: number | string;
}
interface HwReported { properties?: PlantProperties; }
interface HwShadowItem { reported?: HwReported; service_id?: string; }
interface HwDeviceShadow { shadow?: HwShadowItem[]; }

@Entry
@Component
struct HomePage {
  @State currentTabIndex: number = 0;
  @State reportPeriod: string = 'Monthly';
  @State isSettingSheetOpen: boolean = false;
  @State isDarkMode: boolean = false;
  @State isEnglish: boolean = false;
  @State isAboutSheetOpen: boolean = false;
  @State lightUnit: string = 'LUX';

  private hwAK: string = 'HPUAW7NO99BVZZGSICTI';
  private hwSK: string = 'jb0SuenfbldK2czYRtNuyxvlc5roiwWYE3yNihLs';
  private hwToken: string = '';
  private hwTimerId: number = -1;
  private device1ShadowUrl: string = 'https://e1144d64d1.st1.iotda-app.cn-north-4.myhuaweicloud.com:443/v5/iot/8fa081c708324f619974bd5c2701f42d/devices/6a5bb330cbb0cf6bb9707167_SmartPot_01/shadow';
  private device2ShadowUrl: string = 'https://e1144d64d1.st1.iotda-app.cn-north-4.myhuaweicloud.com:443/v5/iot/8fa081c708324f619974bd5c2701f42d/devices/6a5bb330cbb0cf6bb9707167_PlantSensor_02/shadow';

  @State currentCityName: string = '定位中...';
  @State currentCityLocation: string = 'auto';
  @State isCitySheetOpen: boolean = false;

  @State isCustomDetailOpen: boolean = false;
  @State selectedPlant: PlantApiItem | null = null;
  @State detailPanelTranslateY: string = '100%';

  @State isAddDeviceSheetOpen: boolean = false;
  @State isScanning: boolean = true;
  @State boundDevicesCount: number = 0;

  @State isSelectPlantSheetOpen: boolean = false;
  @State targetBindingDevice: SimulatedDeviceNode | null = null;

  @State chinaCities: CityNode[] = [
    { name: '自动定位', location: 'auto' },
    { name: '北京', location: '116.41,39.92' },
    { name: '上海', location: '121.47,31.23' },
    { name: '广州', location: '113.26,23.13' },
    { name: '深圳', location: '114.05,22.54' },
    { name: '香港', location: '114.17,22.28' },
    { name: '澳门', location: '113.54,22.19' },
    { name: '杭州', location: '120.15,30.28' },
    { name: '成都', location: '104.06,30.67' },
    { name: '武汉', location: '114.30,30.59' },
    { name: '南京', location: '118.79,32.06' },
    { name: '西安', location: '108.94,34.34' }
  ];

  @State plantCatalog: PlantRawItem[] = [];

  @State availableDevices: SimulatedDeviceNode[] = [
    {
      id: 'BLE-POT-01',
      devHardwareName: '6a5bb330cbb0cf6bb9707167_SmartPot_01',
      name: 'SmartPot_01',
      type: '智能花盆',
      rssi: '-42 dBm',
      isBound: false,
      boundPlantName: '',
      moisture: '60%',
      temp: '24.8°C',
      light: '3200',
      npk: '880 NPK'
    },
    {
      id: 'BLE-DET-02',
      devHardwareName: '6a5bb330cbb0cf6bb9707167_PlantSensor_02',
      name: 'PlantSensor_02',
      type: '智能花草检测仪',
      rssi: '-55 dBm',
      isBound: false,
      boundPlantName: '',
      moisture: '42%',
      temp: '26.0°C',
      light: '4500',
      npk: '620 NPK'
    }
  ];

  private tabItems: TabItem[] = [
    { title: '主页', enTitle: 'Home', icon: $r('app.media.home'), pad: 1.5, iconSize: 22, offsetY: 0 },
    { title: '数据', enTitle: 'Data', icon: $r('app.media.trend'), pad: 1.5, iconSize: 22, offsetY: 0 },
    { title: '植物志', enTitle: 'Flora', icon: $r('app.media.leaves'), pad: 1.5, iconSize: 25.5, offsetY: 2.5 },
    { title: '我的', enTitle: 'Profile', icon: $r('app.media.profile'), pad: 1.5, iconSize: 22, offsetY: 0 }
  ];

  aboutToAppear() {
    this.loadCitiesFromRawFile();
    this.loadPlantsFromRawFile();
    this.startHuaweiCloudPolling();
  }

  aboutToDisappear() {
    if (this.hwTimerId !== -1) {
      clearInterval(this.hwTimerId);
      this.hwTimerId = -1;
    }
  }

  private startHuaweiCloudPolling() {
    let httpRequest = http.createHttp();
    httpRequest.request(
      'https://iam.cn-north-4.myhuaweicloud.com/v3/auth/tokens',
      {
        method: http.RequestMethod.POST,
        header: { 'Content-Type': 'application/json' },
        extraData: this.getHwTokenRequestParam(),
        connectTimeout: 8000,
        readTimeout: 8000
      },
      (err, data) => {
        httpRequest.destroy();
        if (!err && data && data.header) {
          let headerObj = data.header as Record<string, string>;
          let token = headerObj['x-subject-token'] || headerObj['X-Subject-Token'];
          if (token) {
            this.hwToken = token;
            this.fetchAllDeviceShadows();
            if (this.hwTimerId === -1) {
              this.hwTimerId = setInterval(() => {
                this.fetchAllDeviceShadows();
              }, 3000);
            }
          }
        }
      }
    );
  }

  private getHwTokenRequestParam(): HwAuthRequest {
    return {
      auth: {
        identity: {
          methods: ['hw_ak_sk'],
          hw_ak_sk: {
            access: { key: this.hwAK },
            secret: { key: this.hwSK }
          }
        },
        scope: {
          project: { name: 'cn-north-4' }
        }
      }
    };
  }

  private fetchAllDeviceShadows() {
    this.fetchDeviceShadow(this.device1ShadowUrl, 0);
    this.fetchDeviceShadow(this.device2ShadowUrl, 1);
  }

  private fetchDeviceShadow(url: string, deviceIndex: number) {
    if (!this.hwToken) { return; }
    let httpRequest = http.createHttp();
    httpRequest.request(
      url,
      {
        method: http.RequestMethod.GET,
        header: {
          'Content-Type': 'application/json',
          'X-Auth-Token': this.hwToken
        },
        connectTimeout: 8000,
        readTimeout: 8000
      },
      (err, data) => {
        httpRequest.destroy();
        if (!err && data && data.result) {
          try {
            let resStr = typeof data.result === 'string' ? data.result : JSON.stringify(data.result);
            let response = JSON.parse(resStr) as HwDeviceShadow;
            if (response && response.shadow && Array.isArray(response.shadow)) {
              for (let i = 0; i < response.shadow.length; i++) {
                let props = response.shadow[i]?.reported?.properties;
                if (props && this.availableDevices && this.availableDevices[deviceIndex]) {
                  let dev = this.availableDevices[deviceIndex];
                  if (props.moisture !== undefined && props.moisture !== null) { dev.moisture = `${props.moisture}%`; }
                  if (props.temperature !== undefined && props.temperature !== null) { dev.temp = `${props.temperature}°C`; }
                  if (props.light !== undefined && props.light !== null) { dev.light = `${props.light}`; }
                  if (props.npk !== undefined && props.npk !== null) { dev.npk = `${props.npk} NPK`; }

                  this.availableDevices = [...this.availableDevices];
                }
              }
            }
          } catch (e) {}
        }
      }
    );
  }

  private async loadCitiesFromRawFile() {
    try {
      let context = getContext(this) as common.UIAbilityContext;
      let value = await context.resourceManager.getRawFileContent('cities.json');
      let textDecoder = util.TextDecoder.create('utf-8');
      let str = textDecoder.decodeWithStream(value);
      this.chinaCities = JSON.parse(str) as CityNode[];
    } catch (e) {
      // Keep initial default cities
    }
  }

  private async loadPlantsFromRawFile() {
    try {
      let context = getContext(this) as common.UIAbilityContext;
      let value = await context.resourceManager.getRawFileContent('plants.json');
      let textDecoder = util.TextDecoder.create('utf-8');
      let str = textDecoder.decodeWithStream(value);
      this.plantCatalog = JSON.parse(str) as PlantRawItem[];
    } catch (e) {
      this.plantCatalog = [
        { id: 'p1', name: '薄荷', category: '香草类', desc: '喜光湿润' },
        { id: 'p2', name: '多肉植物', category: '景天科', desc: '耐旱喜光' },
        { id: 'p3', name: '绿萝', category: '天南星科', desc: '极易耐阴' },
        { id: 'p4', name: '龟背竹', category: '观叶植物', desc: '喜温湿润' },
        { id: 'p5', name: '樱桃番茄', category: '蔬果盆栽', desc: '喜阳耐肥，适宜阳台盆栽' },
        { id: 'p6', name: '奶油草莓', category: '蔬果盆栽', desc: '喜湿润温暖，注意充足日光' },
        { id: 'p7', name: '迷迭香', category: '香草类', desc: '耐旱喜光，气味芳香' },
        { id: 'p8', name: '观赏彩椒', category: '蔬果盆栽', desc: '喜温喜日光，果实丰富多彩' },
        { id: 'p9', name: '矮生蓝莓', category: '浆果盆栽', desc: '喜酸性土壤，充足日照' }
      ];
    }
  }

  private startBleScanning() {
    this.fetchAllDeviceShadows();
    this.isScanning = true;
    this.isAddDeviceSheetOpen = true;

    setTimeout(() => {
      this.isScanning = false;
    }, 5000);
  }

  private getPlantType(cycle: string | null): string { return cycle?.toLowerCase().includes('perennial') ? '多年生草本观叶植物' : '室内常绿温带植物'; }
  private getFlowerPeriod(name: string | null): string { return name?.toLowerCase().includes('maple') ? '无明显花期' : '春末夏初 (5月 - 7月)'; }
  private getPlantSize(_name: string | null): string { return '中小型盆栽'; }
  private getOriginal(_name: string | null): string { return '温带森林/热带雨林区'; }

  private getMoistureDiagnostic(valStr: string): MetricDiagnostic {
    let num = parseFloat(valStr);
    if (isNaN(num)) return { status: 'NORMAL', label: '水分适宜', text: '🌱 土壤含水量正常，支持根系顺畅呼吸。', color: '#4CAF50' };
    if (num < 30) {
      return { status: 'LOW', label: '干旱缺水', text: '💧 土壤偏干(＜30%)，叶片易打蔫卷曲，建议沿盆边透浇一次水分。', color: '#E65100' };
    } else if (num > 75) {
      return { status: 'HIGH', label: '积水过涝', text: '🚨 土壤极度湿涝(＞75%)，易导致根系缺氧腐烂，建议加强通风并暂停浇水。', color: '#D32F2F' };
    }
    return { status: 'NORMAL', label: '水分适宜', text: '🌱 土壤湿度维持在最佳 30%~75% 区间，根系生长极佳。', color: '#4CAF50' };
  }

  private getTempDiagnostic(valStr: string): MetricDiagnostic {
    let num = parseFloat(valStr);
    if (isNaN(num)) return { status: 'NORMAL', label: '温度适宜', text: '☀️ 环境温度舒适。', color: '#4CAF50' };
    if (num < 15) {
      return { status: 'LOW', label: '低温偏冷', text: '❄️ 气温偏低(＜15°C)生长变缓，建议移入室内温暖避风处越冬。', color: '#1976D2' };
    } else if (num > 28) {
      return { status: 'HIGH', label: '高温闷热', text: '🔥 环境温度较高(＞28°C)蒸发加剧，建议遮阴防晒并向周围喷雾降温。', color: '#D32F2F' };
    }
    return { status: 'NORMAL', label: '温度舒适', text: '☀️ 15°C~28°C 舒适温域，有利于养分积累与新芽抽发。', color: '#4CAF50' };
  }

  private getLightDiagnostic(valStr: string): MetricDiagnostic {
    let num = parseFloat(valStr);
    if (isNaN(num)) return { status: 'NORMAL', label: '光照充足', text: '✨ 散射光充沛，进行高效光合作用。', color: '#4CAF50' };
    if (num < 1500) {
      return { status: 'LOW', label: '光照不足', text: '☁️ 环境较暗(＜1500 LUX)，长期易导致徒长黄叶，建议移至明亮阳台。', color: '#E65100' };
    } else if (num > 6000) {
      return { status: 'HIGH', label: '强光灼伤', text: '☀️ 阳光过强(＞6000 LUX)，可能晒伤娇嫩叶片，请适当拉帘遮阴。', color: '#D32F2F' };
    }
    return { status: 'NORMAL', label: '光效极佳', text: '✨ 光照强度处于理想散射光区，叶色浓绿光亮。', color: '#4CAF50' };
  }

  private getNpkDiagnostic(valStr: string): MetricDiagnostic {
    let num = parseFloat(valStr);
    if (isNaN(num)) return { status: 'NORMAL', label: '肥力均衡', text: '🪴 养分充沛，支撑健康生长。', color: '#4CAF50' };
    if (num < 400) {
      return { status: 'LOW', label: '养分缺乏', text: '📉 肥力偏低(＜400 NPK)，植物易黄叶瘦弱，建议适量追施稀释薄肥。', color: '#E65100' };
    } else if (num > 1200) {
      return { status: 'HIGH', label: '肥害风险', text: '⚠️ 养分浓度过高(＞1200 NPK)易烧根，建议大水浇透冲洗余肥。', color: '#D32F2F' };
    }
    return { status: 'NORMAL', label: '肥力均衡', text: '🪴 氮磷钾养分充沛，为植物抽枝发叶提供强劲动力。', color: '#4CAF50' };
  }

  private getOverallDeviceHealth(moisture: string, temp: string, light: string, npk: string): OverallHealth {
    let m = this.getMoistureDiagnostic(moisture);
    let t = this.getTempDiagnostic(temp);
    let l = this.getLightDiagnostic(light);
    let n = this.getNpkDiagnostic(npk);

    if (m.status === 'HIGH' || t.status === 'HIGH' || n.status === 'HIGH' || l.status === 'HIGH') {
      let warnText = m.status === 'HIGH' ? m.text : (t.status === 'HIGH' ? t.text : (n.status === 'HIGH' ? n.text : l.text));
      return { statusText: '注意预警', advice: warnText, color: '#D32F2F', badgeBg: 'rgba(211,47,47,0.1)' };
    }
    if (m.status === 'LOW' || t.status === 'LOW' || n.status === 'LOW' || l.status === 'LOW') {
      let adviceText = m.status === 'LOW' ? m.text : (t.status === 'LOW' ? t.text : (n.status === 'LOW' ? n.text : l.text));
      return { statusText: '养护关注', advice: adviceText, color: '#E65100', badgeBg: 'rgba(230,81,0,0.1)' };
    }
    return { statusText: '健康状态良好', advice: '🌟 当前各项指标均处于黄金舒适区间，植物茁壮成长中！', color: '#224A32', badgeBg: 'rgba(34,74,50,0.1)' };
  }

  @Builder
  citySheetBuilder() {
    CitySelectSheet({
      isCitySheetOpen: $isCitySheetOpen,
      currentCityName: $currentCityName,
      currentCityLocation: $currentCityLocation,
      isDarkMode: $isDarkMode,
      isEnglish: $isEnglish,
      chinaCities: this.chinaCities
    })
  }

  @Builder
  addDeviceSheetBuilder() {
    AddDeviceSheet({
      isAddDeviceSheetOpen: $isAddDeviceSheetOpen,
      isScanning: $isScanning,
      availableDevices: $availableDevices,
      targetBindingDevice: $targetBindingDevice,
      isSelectPlantSheetOpen: $isSelectPlantSheetOpen,
      isDarkMode: $isDarkMode,
      isEnglish: $isEnglish
    })
  }

  @Builder
  selectPlantSheetBuilder() {
    PlantSelectSheet({
      isSelectPlantSheetOpen: $isSelectPlantSheetOpen,
      isAddDeviceSheetOpen: $isAddDeviceSheetOpen,
      targetBindingDevice: $targetBindingDevice,
      availableDevices: $availableDevices,
      boundDevicesCount: $boundDevicesCount,
      isDarkMode: $isDarkMode,
      isEnglish: $isEnglish,
      plantCatalog: this.plantCatalog
    })
  }

  @Builder
  settingSheetBuilder() {
    SettingSheet({
      isSettingSheetOpen: $isSettingSheetOpen,
      isDarkMode: $isDarkMode,
      isEnglish: $isEnglish,
      lightUnit: $lightUnit
    })
  }

  @Builder
  topGardenBar() {
    Row() {
      Column() {
        Text(this.isEnglish ? 'My Plants' : '我的植物').fontSize(22).fontWeight(FontWeight.Bold).fontColor('#FFFFFF')
        Text(this.isEnglish ? `${this.boundDevicesCount} Devices connected` : `共 ${this.boundDevicesCount} 个智能设备绑定`).fontSize(13).fontColor('rgba(255,255,255,0.7)').margin({ top: 4 })
      }.alignItems(HorizontalAlign.Start)
      Blank()
      Button() { Image($r('app.media.add')).width(20).height(20).fillColor('#FFFFFF') }
      .width(40).height(40).backgroundColor('rgba(255,255,255,0.15)').borderRadius(20).onClick(() => { this.startBleScanning(); })
    }.width('100%').padding({ left: 24, right: 24, top: 48, bottom: 10 }).backgroundColor(this.isDarkMode ? '#12161A' : '#1E5F41')
  }

  @Builder
  homeContent() {
    Column() {
      this.topGardenBar()

      Scroll() {
        Column() {
          QWeatherCard({
            currentCityName: $currentCityName,
            currentCityLocation: $currentCityLocation,
            isCitySheetOpen: $isCitySheetOpen,
            isDarkMode: $isDarkMode,
            isEnglish: $isEnglish
          })
            .width('92%')
            .height(215)
            .backgroundColor(this.isDarkMode ? '#1E2226' : '#1E5F41')
            .borderRadius(20)
            .margin({ top: 10, bottom: 16 })

          Column() {
            if (this.boundDevicesCount === 0) {
              Column() {
                Image($r('app.media.app')).width(60).height(60).objectFit(ImageFit.Contain).margin({ bottom: 12 })
                Text(this.isEnglish ? 'No Smart Pot Bound' : '尚未绑定智能设备').fontSize(15).fontWeight(FontWeight.Medium).fontColor(this.isDarkMode ? '#E0E0E0' : '#333333').margin({ bottom: 4 })
                Text(this.isEnglish ? 'Connect smart pots to enable automated plant care.' : '当前环境适合植物生长，绑定智能设备即可开启数据监测。')
                  .fontSize(13).fontColor(this.isDarkMode ? '#8AB399' : '#777777').textAlign(TextAlign.Center).lineHeight(18).margin({ bottom: 16, left: 24, right: 24 })
                Button(this.isEnglish ? 'Add Smart Device' : '立即添加智能设备').width(180).height(40).fontSize(13).fontWeight(FontWeight.Bold).fontColor('#FFFFFF').backgroundColor('#1E5F41').borderRadius(20).onClick(() => { this.startBleScanning(); })
              }.width('92%').padding({ top: 28, bottom: 32 }).alignItems(HorizontalAlign.Center)
            } else {
              Column({ space: 14 }) {
                Row() {
                  Text(this.isEnglish ? 'BOUND SMART DEVICES' : '已绑定智能设备监测').fontSize(13).fontWeight(FontWeight.Bold).fontColor(this.isDarkMode ? '#8AB399' : '#5C7A69').letterSpacing(0.8)
                }.width('100%').padding({ left: 4, right: 4 })

                ForEach(this.availableDevices, (device: SimulatedDeviceNode) => {
                  if (device.isBound) {
                    DeviceCard({
                      device: device,
                      isDarkMode: this.isDarkMode,
                      lightUnit: this.lightUnit
                    })
                  }
                })
              }.width('92%').margin({ top: 16, bottom: 20 })
            }
          }
        }.width('100%').alignItems(HorizontalAlign.Center)
      }.width('100%').layoutWeight(1).padding({ bottom: TAB_ITEM_HEIGHT + 16 })
    }
    .width('100%').height('100%').backgroundColor(this.isDarkMode ? '#12161A' : '#F4F6F4')
  }

  @Builder
  sceneContent() {
    TrendsTab({
      availableDevices: $availableDevices,
      isDarkMode: this.isDarkMode,
      isEnglish: this.isEnglish
    })
  }

  @Builder
  customDetailPanel() {
    if (this.selectedPlant !== null) {
      Stack({ alignContent: Alignment.Bottom }) {
        Row()
          .width('100%')
          .height('100%')
          .backgroundColor('rgba(0, 0, 0, 0.45)')
          .onClick(() => {
            animateTo({ duration: 250, curve: Curve.FastOutSlowIn }, () => {
              this.detailPanelTranslateY = '100%';
            });
            setTimeout(() => {
              this.isCustomDetailOpen = false;
            }, 250);
          })

        Column() {
          Column() {
            Row()
              .width(36)
              .height(4)
              .borderRadius(2)
              .backgroundColor(this.isDarkMode ? '#3A444C' : '#E0E0E0')
          }
          .width('100%')
          .padding({ top: 10, bottom: 6 })
          .alignItems(HorizontalAlign.Center)

          Row() {
            Column({ space: 4 }) {
              Text(this.selectedPlant.common_name).fontSize(20).fontWeight(FontWeight.Bold).fontColor(this.isDarkMode ? '#FFFFFF' : '#222222')
              Text(this.selectedPlant.scientific_name?.[0] || '暂无学名').fontSize(13).fontColor('#888888')
            }.alignItems(HorizontalAlign.Start)
            Blank()
            Image($r('app.media.left'))
              .width(20).height(20).fillColor(this.isDarkMode ? '#FFFFFF' : '#333333')
              .onClick(() => {
                animateTo({ duration: 250, curve: Curve.FastOutSlowIn }, () => {
                  this.detailPanelTranslateY = '100%';
                });
                setTimeout(() => {
                  this.isCustomDetailOpen = false;
                }, 250);
              })
          }.width('100%').padding({ left: 20, right: 20, top: 8, bottom: 14 })

          Scroll() {
            Column({ space: 14 }) {
              Image(this.selectedPlant.default_image?.original_url || this.selectedPlant.default_image?.thumbnail || $r('app.media.app'))
                .width('100%')
                .height(180)
                .borderRadius(14)
                .objectFit(ImageFit.Cover)
                .backgroundColor(this.isDarkMode ? '#242C33' : '#F5F7F6')

              Column({ space: 8 }) {
                Text(this.isEnglish ? 'Botanical Info' : '基础特征信息').fontSize(14).fontWeight(FontWeight.Bold).fontColor(this.isDarkMode ? '#58B582' : '#1E5F41')
                this.detailRowItem('植物类型', this.getPlantType(this.selectedPlant.cycle))
                this.detailRowItem('属名学名', this.selectedPlant.scientific_name?.[0] || '暂无记录')
                this.detailRowItem('预期花期', this.getFlowerPeriod(this.selectedPlant.common_name))
                this.detailRowItem('株高尺寸', this.getPlantSize(this.selectedPlant.common_name))
                this.detailRowItem('自然产地', this.getOriginal(this.selectedPlant.common_name))
              }.width('100%').padding(14).backgroundColor(this.isDarkMode ? '#242C33' : '#F5F7F6').borderRadius(12)
            }.width('100%').padding({ left: 20, right: 20, bottom: 30 })
          }.layoutWeight(1)
        }
        .width('100%')
        .height('78%')
        .backgroundColor(this.isDarkMode ? '#1E2226' : '#FFFFFF')
        .borderRadius({ topLeft: 16, topRight: 16 })
        .translate({ y: this.detailPanelTranslateY })
      }
      .width('100%')
      .height('100%')
    }
  }

  @Builder
  detailRowItem(label: string, value: string) {
    Column() {
      Row() {
        Text(label).fontSize(14).fontWeight(FontWeight.Bold).fontColor(this.isDarkMode ? '#58B582' : '#1E5F41').width(76)
        Text(value).fontSize(14).fontColor(this.isDarkMode ? '#E0E5E8' : '#333333').layoutWeight(1)
      }.width('100%').padding({ top: 11, bottom: 11 })
      if (label !== '自然产地') { Divider().color(this.isDarkMode ? '#2D343A' : '#EEEEEE') }
    }.width('100%')
  }

  @Builder
  profileContent() {
    Scroll() {
      Column() {
        Column() {
          Row() {
            Image($r('app.media.icon')).width(60).height(60).borderRadius(30).border({ width: 2, color: 'rgba(255,255,255,0.45)' }).backgroundColor('#FFFFFF');
            Column() {
              Text('Hello Liu.').fontSize(20).fontWeight(FontWeight.Bold).fontColor('#FFFFFF');
              Row() { Text(this.isEnglish ? 'STANDARD ACCOUNT' : '普通账户').fontSize(10).fontWeight(FontWeight.Bold).fontColor('#FFFFFF').letterSpacing(0.8); }.backgroundColor(this.isDarkMode ? 'rgba(255, 255, 255, 0.08)' : 'rgba(255, 255, 255, 0.18)').border({ width: 1, color: 'rgba(255, 255, 255, 0.3)' }).borderRadius(12).padding({ left: 10, right: 10, top: 3, bottom: 3 }).margin({ top: 6 });
            }.margin({ left: 16 }).alignItems(HorizontalAlign.Start);
          }.width('100%').padding({ left: 24, right: 24, top: 52, bottom: 24 });
        }.width('100%').backgroundColor(this.isDarkMode ? '#1A1E22' : '#1E5F41');

        Column() {
          Column() {
            Column() {
              Row() {
                Row({ space: 12 }) {
                  if (this.isDarkMode) { Image($r('app.media.moon')).width(20).height(20).fillColor('#FFFFFF'); }
                  else { Image($r('app.media.moon')).width(20).height(20); }
                  Text(this.isEnglish ? 'Dark Mode' : '夜间模式').fontSize(15).fontWeight(FontWeight.Medium).fontColor(this.isDarkMode ? '#FFFFFF' : '#222222');
                }
                Toggle({ type: ToggleType.Switch, isOn: this.isDarkMode }).selectedColor('#1E5F41').onChange((isOn: boolean) => { this.isDarkMode = isOn; });
              }.width('100%').justifyContent(FlexAlign.SpaceBetween).padding({ top: 16, bottom: 16 });

              Divider().color(this.isDarkMode ? '#242C33' : '#F5F5F5');

              Row() {
                Row({ space: 12 }) {
                  if (this.isDarkMode) { Image($r('app.media.setting')).width(20).height(20).fillColor('#FFFFFF'); }
                  else { Image($r('app.media.setting')).width(20).height(20); }
                  Text(this.isEnglish ? 'Settings' : '系统设置').fontSize(15).fontWeight(FontWeight.Medium).fontColor(this.isDarkMode ? '#FFFFFF' : '#222222');
                }
                if (this.isDarkMode) {
                  Image($r('app.media.right_night')).width(16).height(16);
                } else {
                  Image($r('app.media.right')).width(16).height(16);
                }
              }.width('100%').justifyContent(FlexAlign.SpaceBetween).padding({ top: 16, bottom: 16 }).onClick(() => { this.isSettingSheetOpen = true; });

              Divider().color(this.isDarkMode ? '#242C33' : '#F5F5F5');

              Row() {
                Row({ space: 12 }) {
                  if (this.isDarkMode) { Image($r('app.media.about')).width(20).height(20).fillColor('#FFFFFF'); }
                  else { Image($r('app.media.about')).width(20).height(20); }
                  Text(this.isEnglish ? 'About App' : '关于软件').fontSize(15).fontWeight(FontWeight.Medium).fontColor(this.isDarkMode ? '#FFFFFF' : '#222222');
                }
                if (this.isDarkMode) {
                  Image($r('app.media.right_night')).width(16).height(16);
                } else {
                  Image($r('app.media.right')).width(16).height(16);
                }
              }.width('100%').justifyContent(FlexAlign.SpaceBetween).padding({ top: 16, bottom: 16 }).onClick(() => { this.isAboutSheetOpen = true; });

              Divider().color(this.isDarkMode ? '#242C33' : '#F5F5F5');

              Row() {
                Row({ space: 12 }) { Image($r('app.media.logout')).width(20).height(20); Text(this.isEnglish ? 'Log Out' : '退出账号').fontSize(15).fontWeight(FontWeight.Medium).fontColor('#E15C43'); }
              }.width('100%').padding({ top: 16, bottom: 16 })
              .onClick(() => {
                AlertDialog.show({
                  title: '确认退出',
                  message: '您确定要退出当前的账号吗？',
                  primaryButton: { value: '取消', action: () => {} },
                  secondaryButton: { value: '确定', action: () => {
                    try {
                      router.pushUrl({ url: 'pages/Index' });
                    } catch (e) {
                      console.error(JSON.stringify(e));
                    }
                  } }
                })
              });
            }.width('100%').backgroundColor(this.isDarkMode ? '#1E2226' : '#FFFFFF').borderRadius(16).padding({ left: 16, right: 16 }).margin({ top: 20, bottom: 40 });
          }.width('100%').padding({ left: 16, right: 16 });
        }.width('100%').layoutWeight(1).backgroundColor(this.isDarkMode ? '#12161A' : '#F5F7F6');
      }.width('100%')
    }.width('100%').height('100%')
  }

  @Builder
  customSheetModal() {
    if (this.isCitySheetOpen || this.isAddDeviceSheetOpen || this.isSelectPlantSheetOpen || this.isSettingSheetOpen || this.isAboutSheetOpen) {
      Stack({ alignContent: Alignment.Bottom }) {
        Row()
          .width('100%')
          .height('100%')
          .backgroundColor('rgba(0, 0, 0, 0.45)')
          .onClick(() => {
            this.closeAllSheets();
          })

        Column() {
          Column() {
            Row()
              .width(36)
              .height(4)
              .borderRadius(2)
              .backgroundColor(this.isDarkMode ? '#3A444C' : '#E0E0E0')
          }
          .width('100%')
          .padding({ top: 10, bottom: 6 })
          .alignItems(HorizontalAlign.Center)

          if (this.isCitySheetOpen) {
            CitySelectSheet({
              isCitySheetOpen: $isCitySheetOpen,
              currentCityName: $currentCityName,
              currentCityLocation: $currentCityLocation,
              isDarkMode: $isDarkMode,
              isEnglish: $isEnglish,
              chinaCities: this.chinaCities
            })
          } else if (this.isAddDeviceSheetOpen) {
            AddDeviceSheet({
              isAddDeviceSheetOpen: $isAddDeviceSheetOpen,
              isScanning: $isScanning,
              availableDevices: $availableDevices,
              targetBindingDevice: $targetBindingDevice,
              isSelectPlantSheetOpen: $isSelectPlantSheetOpen,
              isDarkMode: $isDarkMode,
              isEnglish: $isEnglish
            })
          } else if (this.isSelectPlantSheetOpen) {
            PlantSelectSheet({
              isSelectPlantSheetOpen: $isSelectPlantSheetOpen,
              isAddDeviceSheetOpen: $isAddDeviceSheetOpen,
              targetBindingDevice: $targetBindingDevice,
              availableDevices: $availableDevices,
              boundDevicesCount: $boundDevicesCount,
              isDarkMode: $isDarkMode,
              isEnglish: $isEnglish,
              plantCatalog: this.plantCatalog
            })
          } else if (this.isSettingSheetOpen) {
            SettingSheet({
              isSettingSheetOpen: $isSettingSheetOpen,
              isDarkMode: $isDarkMode,
              isEnglish: $isEnglish,
              lightUnit: $lightUnit
            })
          } else if (this.isAboutSheetOpen) {
            AboutSheet({
              isAboutSheetOpen: $isAboutSheetOpen,
              isDarkMode: $isDarkMode,
              isEnglish: $isEnglish
            })
          }
        }
        .width('100%')
        .height(this.isSelectPlantSheetOpen ? '78%' : '56%')
        .backgroundColor(this.isDarkMode ? '#1E2226' : '#FFFFFF')
        .borderRadius({ topLeft: 16, topRight: 16 })
        .transition(TransitionEffect.move(TransitionEdge.BOTTOM).animation({ duration: 300, curve: Curve.FastOutSlowIn }))
      }
      .width('100%')
      .height('100%')
    }
  }

  private closeAllSheets() {
    this.isCitySheetOpen = false;
    this.isAddDeviceSheetOpen = false;
    this.isSelectPlantSheetOpen = false;
    this.isSettingSheetOpen = false;
    this.isAboutSheetOpen = false;
  }

  build() {
    Column() {
      Stack({ alignContent: Alignment.Bottom }) {
        Column() {
          if (this.currentTabIndex === 0) { this.homeContent() }
          else if (this.currentTabIndex === 1) { this.sceneContent() }
          else if (this.currentTabIndex === 2) {
            PlantEncyclopedia({
              isDarkMode: $isDarkMode,
              isEnglish: $isEnglish,
              isCustomDetailOpen: $isCustomDetailOpen,
              selectedPlant: $selectedPlant,
              detailPanelTranslateY: $detailPanelTranslateY
            }).width('100%').height('100%').padding({ bottom: TAB_ITEM_HEIGHT })
          }
          else if (this.currentTabIndex === 3) { this.profileContent() }
        }.width('100%').height('100%')

        Stack({ alignContent: Alignment.TopStart }) {
          Row() {
            ForEach(this.tabItems, (item: TabItem, index: number) => {
              Column() {
                Stack({ alignContent: Alignment.Center }) {
                  Image(item.icon)
                    .width(item.iconSize || 22)
                    .height(item.iconSize || 22)
                    .objectFit(ImageFit.Contain)
                    .fillColor(this.currentTabIndex === index ? (this.isDarkMode ? '#58B582' : '#1E5F41') : '#888888')
                    .offset({ y: item.offsetY || 0 })
                }
                .width(26)
                .height(26)
                .margin({ bottom: 2 })

                Text(item.title).fontSize(TAB_TEXT_SIZE).fontWeight(this.currentTabIndex === index ? FontWeight.Bold : FontWeight.Regular).fontColor(this.currentTabIndex === index ? (this.isDarkMode ? '#58B582' : '#1E5F41') : '#888888')
              }
              .layoutWeight(1).height(TAB_ITEM_HEIGHT).backgroundColor('transparent').justifyContent(FlexAlign.Center)
              .onClick(() => {
                animateTo({ duration: 250, curve: Curve.FastOutSlowIn }, () => {
                  this.currentTabIndex = index;
                });
              })
            })
          }.width('100%').height(TAB_ITEM_HEIGHT)
        }.width('100%').height(TAB_ITEM_HEIGHT).backgroundColor(this.isDarkMode ? '#1E2226' : '#FFFFFF').border({ width: { top: 0.5 }, color: this.isDarkMode ? '#242C33' : '#EEEEEE' })

        if (this.isCustomDetailOpen) { this.customDetailPanel() }
        this.customSheetModal()
      }.width('100%').height('100%')
    }
    .width('100%').height('100%')
  }
}