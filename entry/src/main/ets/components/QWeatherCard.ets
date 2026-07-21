// QWeatherCard.ets
import { http } from '@kit.NetworkKit';
import { geoLocationManager } from '@kit.LocationKit';
import { BusinessError } from '@kit.BasicServicesKit';
import { common, abilityAccessCtrl } from '@kit.AbilityKit';

export interface QWeatherNow {
  temp: string;
  feelsLike?: string;
  text: string;
  humidity: string;
  precip: string;
  windSpeed: string;
  pressure?: string;
  windDir?: string;
  cloud?: string;
  vis?: string;
}

export interface QWeatherDailyItem {
  fxDate: string;
  tempMax: string;
  tempMin: string;
  textDay: string;
  iconDay?: string;
  sunrise?: string;
  sunset?: string;
}

export interface QWeatherNowResponse { code: string; now: QWeatherNow; }
export interface QWeatherDaily7Response { code: string; daily: QWeatherDailyItem[]; }
export interface QWeatherGeoLocation { name: string; id: string; lat: string; lon: string; adm2?: string; adm1?: string; }
export interface QWeatherGeoResponse { code: string; location: QWeatherGeoLocation[]; }

export interface DailyForecastNode {
  date: string;
  dayText: string;
  icon: string;
  tempMax: string;
  tempMin: string;
  isToday: boolean;
}

export interface StyledGridItemParams {
  iconRes: Resource;
  val: string;
  label: string;
  isAlert: boolean;
}

@Component
export struct QWeatherCard {
  @Link currentCityName: string;
  @Watch('onLocationChange') @Link currentCityLocation: string;
  @Link isCitySheetOpen: boolean;
  @Link isDarkMode: boolean;
  @Watch('updateWeekDayTitles') @Link isEnglish: boolean;

  @State currentPageIndex: number = 0;
  @State weatherTemp: string = '28';
  @State weatherFeelsLike: string = '28';
  @State weatherText: string = '多云';
  @State weatherMinMax: string = '27° / 30°';
  @State weatherRain: string = '0.0';
  @State weatherWind: string = '4';
  @State weatherHumidity: string = '74';
  @State weatherPressure: string = '1013';
  @State weatherWindDir: string = '东南风';
  @State weatherCloud: string = '25';
  @State weatherSunTime: string = '05:43/19:10';

  @State weeklyForecast: DailyForecastNode[] = [
    { date: '1', dayText: '周一', icon: '🌧️', tempMax: '32°', tempMin: '28°', isToday: false },
    { date: '2', dayText: '周二', icon: '🌧️', tempMax: '29°', tempMin: '27°', isToday: false },
    { date: '3', dayText: '周三', icon: '🌧️', tempMax: '28°', tempMin: '27°', isToday: false },
    { date: '4', dayText: '周四', icon: '🌧️', tempMax: '28°', tempMin: '26°', isToday: false },
    { date: '5', dayText: '周五', icon: '🌧️', tempMax: '30°', tempMin: '27°', isToday: false },
    { date: '6', dayText: '周六', icon: '⛈️', tempMax: '30°', tempMin: '28°', isToday: true },
    { date: '7', dayText: '周日', icon: '⛅', tempMax: '29°', tempMin: '28°', isToday: false }
  ];

  private qWeatherKey: string = '04e6c11eab65489aa966a7f0bd83e6d6';
  private apiHost: string = 'ju5b676n2n.re.qweatherapi.com';

  aboutToAppear() {
    this.updateWeekDayTitles();
    if (this.currentCityLocation === 'auto') {
      this.requestLocationAndWeather();
    } else {
      this.executeQWeatherPipeline(this.currentCityLocation, this.currentCityName);
    }
  }

  private onLocationChange() {
    if (this.currentCityLocation === 'auto') {
      this.requestLocationAndWeather();
    } else if (this.currentCityLocation) {
      this.executeQWeatherPipeline(this.currentCityLocation, this.currentCityName);
    }
  }

  private updateWeekDayTitles() {
    let daysCn = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
    let daysEn = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
    for (let i = 0; i < this.weeklyForecast.length; i++) {
      if (!this.weeklyForecast[i].date.includes('-')) {
        this.weeklyForecast[i].dayText = this.isEnglish ? daysEn[i % 7] : daysCn[i % 7];
      }
    }
  }

  public requestLocationAndWeather() {
    this.currentCityName = this.isEnglish ? 'Locating...' : '定位中...';
    try {
      let context = getContext(this) as common.UIAbilityContext;
      let atManager = abilityAccessCtrl.createAtManager();

      let timeoutTracker = setTimeout(() => {
        if (this.currentCityName.includes('定位') || this.currentCityName.includes('Locating')) {
          this.executeQWeatherPipeline('114.17,22.28', this.isEnglish ? 'Locating...' : '定位中...');
        }
      }, 2500);

      atManager.requestPermissionsFromUser(context, [
        'ohos.permission.LOCATION',
        'ohos.permission.APPROXIMATELY_LOCATION'
      ]).then((data) => {
        if (data.authResults[0] === 0 || data.authResults[1] === 0) {
          let requestInfo: geoLocationManager.LocationRequest = {
            priority: geoLocationManager.LocationRequestPriority.FIRST_FIX,
            scenario: 0x300,
            timeInterval: 1, distanceInterval: 0, maxAccuracy: 0
          };
          geoLocationManager.getCurrentLocation(requestInfo).then((location: geoLocationManager.Location) => {
            clearTimeout(timeoutTracker);
            let locString = `${location.longitude.toFixed(2)},${location.latitude.toFixed(2)}`;
            this.fetchCityNameFromQWeather(locString);
          }).catch((err: BusinessError) => {
            clearTimeout(timeoutTracker);
            this.executeQWeatherPipeline('114.17,22.28', this.isEnglish ? 'Locating...' : '定位中...');
          });
        } else {
          clearTimeout(timeoutTracker);
          this.executeQWeatherPipeline('114.17,22.28', this.isEnglish ? 'Locating...' : '定位中...');
        }
      }).catch((err: BusinessError) => {
        clearTimeout(timeoutTracker);
        this.executeQWeatherPipeline('114.17,22.28', this.isEnglish ? 'Locating...' : '定位中...');
      });
    } catch (e) {
      this.executeQWeatherPipeline('114.17,22.28', this.isEnglish ? 'Locating...' : '定位中...');
    }
  }

  public executeQWeatherPipeline(location: string, cityName: string) {
    if (cityName !== '自动定位' && !cityName.includes('定位') && !cityName.includes('Locating')) {
      this.currentCityName = cityName;
    }
    this.fetchCityNameFromQWeather(location);
  }

  private fetchCityNameFromQWeather(loc: string, useFallback: boolean = false) {
    let host = useFallback ? 'geoapi.qweather.com' : this.apiHost;
    let url = `https://${host}/v2/city/lookup?location=${encodeURIComponent(loc)}&key=${this.qWeatherKey}&lang=zh`;
    let httpRequest = http.createHttp();
    httpRequest.request(url, {
      method: http.RequestMethod.GET,
      header: { 'Content-Type': 'application/json' },
      connectTimeout: 5000,
      readTimeout: 5000
    }, (err: BusinessError, data: http.HttpResponse) => {
      let targetLoc = loc;
      if (!err && data && data.responseCode === 200) {
        try {
          let res = JSON.parse(data.result as string) as QWeatherGeoResponse;
          if (res && res.code === '200' && res.location && res.location.length > 0) {
            let locItem: QWeatherGeoLocation = res.location[0];
            let nameStr: string = locItem.adm2 ? locItem.adm2 : (locItem.name ? locItem.name : (locItem.adm1 ? locItem.adm1 : '香港'));
            if (nameStr.endsWith('市') || nameStr.endsWith('区') || nameStr.endsWith('县')) {
              nameStr = nameStr.substring(0, nameStr.length - 1);
            }
            this.currentCityName = nameStr;
            if (locItem.id) {
              targetLoc = locItem.id;
            }
            console.info('[QWeather Geo] Location ID resolved:', targetLoc, 'City:', nameStr);
          }
        } catch (e) {
          console.error('[QWeather Geo] JSON parse error');
        }
      } else if (!useFallback) {
        this.fetchCityNameFromQWeather(loc, true);
        return;
      } else {
        if (this.currentCityName.includes('定位') || this.currentCityName.includes('Locating')) {
          this.currentCityName = this.isEnglish ? 'Hong Kong' : '香港';
        }
      }

      this.fetchQWeatherNow(targetLoc);
      this.fetchQWeatherForecast(targetLoc);
    });
  }

  private fetchQWeatherNow(loc: string, useFallback: boolean = false) {
    let host = useFallback ? 'devapi.qweather.com' : this.apiHost;
    let url = `https://${host}/v7/weather/now?location=${encodeURIComponent(loc)}&key=${this.qWeatherKey}&lang=zh`;
    let httpRequest = http.createHttp();
    httpRequest.request(url, {
      method: http.RequestMethod.GET,
      header: { 'Content-Type': 'application/json' },
      connectTimeout: 5000,
      readTimeout: 5000
    }, (err: BusinessError, data: http.HttpResponse) => {
      if (!err && data && data.responseCode === 200) {
        try {
          let res = JSON.parse(data.result as string) as QWeatherNowResponse;
          if (res && res.code === '200' && res.now) {
            let nowData = res.now;
            this.weatherTemp = nowData.temp;
            this.weatherFeelsLike = nowData.feelsLike ? nowData.feelsLike : nowData.temp;
            this.weatherText = nowData.text;
            this.weatherHumidity = nowData.humidity;
            this.weatherRain = nowData.precip || '0.0';
            this.weatherWind = nowData.windSpeed;
            this.weatherPressure = nowData.pressure ? nowData.pressure : '1013';
            this.weatherWindDir = nowData.windDir ? nowData.windDir : (this.isEnglish ? 'SE Wind' : '东南风');
            this.weatherCloud = (nowData.cloud && nowData.cloud.length > 0) ? nowData.cloud : (nowData.vis ? nowData.vis : '20');

            console.info('[QWeather NOW] Real API Data Received:', JSON.stringify(nowData));
            return;
          } else if (!useFallback) {
            this.fetchQWeatherNow(loc, true);
          }
        } catch (e) {
          if (!useFallback) { this.fetchQWeatherNow(loc, true); }
        }
      } else {
        if (!useFallback) {
          this.fetchQWeatherNow(loc, true);
        }
      }
    });
  }

  private fetchQWeatherForecast(loc: string, useFallback: boolean = false) {
    let host = useFallback ? 'devapi.qweather.com' : this.apiHost;
    let url = `https://${host}/v7/weather/7d?location=${encodeURIComponent(loc)}&key=${this.qWeatherKey}&lang=zh`;
    let httpRequest = http.createHttp();
    httpRequest.request(url, {
      method: http.RequestMethod.GET,
      header: { 'Content-Type': 'application/json' },
      connectTimeout: 5000,
      readTimeout: 5000
    }, (err: BusinessError, data: http.HttpResponse) => {
      if (!err && data && data.responseCode === 200) {
        try {
          let res = JSON.parse(data.result as string) as QWeatherDaily7Response;
          if (res && res.code === '200' && res.daily && res.daily.length > 0) {
            let todayDaily = res.daily[0];
            this.weatherMinMax = `${todayDaily.tempMin}° / ${todayDaily.tempMax}°`;
            if (todayDaily.sunrise && todayDaily.sunset) {
              this.weatherSunTime = `${todayDaily.sunrise}/${todayDaily.sunset}`;
            }
            
            let dailyList: DailyForecastNode[] = [];
            let daysCn = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
            let daysEn = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
            let now = new Date();
            let todayYear = now.getFullYear();
            let todayMonth = String(now.getMonth() + 1).padStart(2, '0');
            let todayDay = String(now.getDate()).padStart(2, '0');
            let todayStr = `${todayYear}-${todayMonth}-${todayDay}`;

            let nodesMap: DailyForecastNode[] = [];
            for (let i = 0; i < res.daily.length && i < 7; i++) {
              let item = res.daily[i];
              let dateObj = new Date(item.fxDate.replace(/-/g, '/'));
              let dayOfWeek = dateObj.getDay();
              let monIndex = (dayOfWeek + 6) % 7;
              let isToday = (i === 0) || (item.fxDate === todayStr);

              let dayTitle = this.isEnglish ? daysEn[monIndex] : (isToday ? '今天' : daysCn[monIndex]);
              let weatherText = item.textDay || '';
              let icon = weatherText.includes('雷') ? '⛈️' :
                         (weatherText.includes('雨') ? '🌧️' :
                         (weatherText.includes('雪') ? '❄️' :
                         (weatherText.includes('阴') || weatherText.includes('云') ? '⛅' : '☀️')));

              nodesMap[monIndex] = {
                date: item.fxDate,
                dayText: dayTitle,
                icon: icon,
                tempMax: `${item.tempMax}°`,
                tempMin: `${item.tempMin}°`,
                isToday: isToday
              };
            }

            for (let i = 0; i < 7; i++) {
              if (nodesMap[i]) {
                dailyList.push(nodesMap[i]);
              }
            }
            this.weeklyForecast = dailyList;
          } else if (!useFallback) {
            this.fetchQWeatherForecast(loc, true);
          }
        } catch (e) {
          if (!useFallback) { this.fetchQWeatherForecast(loc, true); }
        }
      } else {
        if (!useFallback) {
          this.fetchQWeatherForecast(loc, true);
        }
      }
    });
  }

  @Builder
  weekForecastDayNode(node: DailyForecastNode) {
    Column({ space: 2 }) {
      Text(node.dayText)
        .fontSize(10)
        .fontWeight(node.isToday ? FontWeight.Bold : FontWeight.Medium)
        .fontColor(node.isToday ? '#FFFFFF' : 'rgba(255,255,255,0.75)')

      Text(node.icon)
        .fontSize(13)

      Text(node.tempMax)
        .fontSize(11)
        .fontWeight(FontWeight.Bold)
        .fontColor('#FFFFFF')

      Text(node.tempMin)
        .fontSize(9)
        .fontColor('rgba(255,255,255,0.5)')
    }
    .padding({ top: 4, bottom: 4, left: 3, right: 3 })
    .backgroundColor(node.isToday ? 'rgba(255,255,255,0.14)' : 'transparent')
    .borderRadius(8)
    .border({ width: node.isToday ? 1 : 0, color: 'rgba(255,255,255,0.25)' })
  }

  @Builder
  styledGridItem(params: StyledGridItemParams) {
    Column() {
      Stack({ alignContent: Alignment.Center }) {
        Image(params.iconRes)
          .width(15)
          .height(15)
          .fillColor('#FFFFFF')
          .objectFit(ImageFit.Contain)
      }
      .width(18)
      .height(18)
      .margin({ bottom: 3 })

      Row() {
        Text(params.val)
          .fontSize(params.val.length > 8 ? 9.5 : 12.5)
          .fontWeight(FontWeight.Bold)
          .fontColor('#FFFFFF')
          .textAlign(TextAlign.Center)
          .maxLines(1)
      }
      .width('100%')
      .height(16)
      .justifyContent(FlexAlign.Center)
      .alignItems(VerticalAlign.Center)

      Text(params.label)
        .fontSize(8.5)
        .fontWeight(FontWeight.Bold)
        .fontColor('rgba(255,255,255,0.45)')
        .textAlign(TextAlign.Center)
        .margin({ top: 1 })
    }
    .layoutWeight(1)
    .padding({ top: 6, bottom: 6, left: 3, right: 3 })
    .backgroundColor('rgba(255,255,255,0.06)')
    .borderRadius(10)
    .border({ width: 1, color: params.isAlert ? 'rgba(233,170,50,0.3)' : 'rgba(255,255,255,0.1)' })
    .alignItems(HorizontalAlign.Center)
    .justifyContent(FlexAlign.Center)
  }

  build() {
    Stack({ alignContent: Alignment.Bottom }) {
      Swiper() {
        // 第一页：今日精简核心气象
        Column() {
          Row() {
            Row({ space: 4 }) {
              Text(`📍 ${this.currentCityName}`).fontSize(11.5).fontWeight(FontWeight.Bold).fontColor('#FFFFFF')
              Text('▼').fontSize(8).fontColor('rgba(255,255,255,0.7)')
            }
            .padding({ left: 10, right: 10, top: 5, bottom: 5 })
            .backgroundColor('rgba(255,255,255,0.18)')
            .borderRadius(10)
            .onClick(() => { this.isCitySheetOpen = true; })
            Blank()
          }.width('100%').margin({ bottom: 4 })

          Row() {
            Column() {
              Row() {
                Text(this.weatherTemp).fontSize(56).fontWeight(FontWeight.Bold).fontColor('#FFFFFF')
                Text('°').fontSize(32).fontColor('#FFFFFF').margin({ bottom: 14 })
              }.alignItems(VerticalAlign.Bottom)
              Text(this.weatherMinMax).fontSize(13).fontColor('rgba(255,255,255,0.75)')
            }.alignItems(HorizontalAlign.Start)
            Blank()
            Text(this.weatherText.includes('雷') ? '⛈️' : (this.weatherText.includes('雨') ? '🌧️' : (this.weatherText.includes('阴') || this.weatherText.includes('云') ? '⛅' : '☀️'))).fontSize(54)
          }.width('100%').margin({ bottom: 6 })

          Blank()

          Row({ space: 8 }) {
            this.styledGridItem({ iconRes: $r('app.media.humidity'), val: `${this.weatherHumidity}%`, label: this.isEnglish ? 'HUMIDITY' : '湿度', isAlert: false })
            this.styledGridItem({ iconRes: $r('app.media.pressure'), val: `${this.weatherPressure} hPa`, label: this.isEnglish ? 'PRESSURE' : '气压', isAlert: false })
            this.styledGridItem({ iconRes: $r('app.media.dogwane'), val: `${this.weatherWindDir}`, label: this.isEnglish ? 'WIND DIR.' : '风向', isAlert: false })
          }.width('100%')
        }
        .width('100%')
        .height('100%')
        .padding({ left: 12, right: 12, top: 10, bottom: 22 })

        // 第二页：周气象预测
        Column() {
          Row() {
            Row({ space: 4 }) {
              Text(`📍 ${this.currentCityName}`).fontSize(11.5).fontWeight(FontWeight.Bold).fontColor('#FFFFFF')
              Text('▼').fontSize(8).fontColor('rgba(255,255,255,0.7)')
            }
            .padding({ left: 10, right: 10, top: 5, bottom: 5 })
            .backgroundColor('rgba(255,255,255,0.18)')
            .borderRadius(10)
            .onClick(() => { this.isCitySheetOpen = true; })
            Blank()
          }.width('100%').margin({ bottom: 3 })

          Row() {
            Text(this.isEnglish ? '7-DAY FORECAST' : '本周 7 天天气预报').fontSize(11).fontWeight(FontWeight.Bold).fontColor('rgba(255,255,255,0.65)')
          }.width('100%').margin({ bottom: 4 })

          Row() {
            ForEach(this.weeklyForecast, (node: DailyForecastNode) => {
              this.weekForecastDayNode(node)
            })
          }.width('100%').justifyContent(FlexAlign.SpaceBetween)

          Blank()

          Row({ space: 8 }) {
            this.styledGridItem({ iconRes: $r('app.media.cloud'), val: `${this.weatherCloud}%`, label: this.isEnglish ? 'CLOUD' : '云量', isAlert: false })
            this.styledGridItem({ iconRes: $r('app.media.wind'), val: `${this.weatherWind} km/h`, label: this.isEnglish ? 'WIND SPEED' : '风速', isAlert: false })
            this.styledGridItem({ iconRes: $r('app.media.sun'), val: `${this.weatherSunTime}`, label: this.isEnglish ? 'SUNRISE/SET' : '日出日落', isAlert: false })
          }.width('100%')
        }
        .width('100%')
        .height('100%')
        .padding({ left: 12, right: 12, top: 10, bottom: 22 })
      }
      .width('100%')
      .height('100%')
      .loop(false)
      .indicator(
        DotIndicator.dot()
          .itemWidth(5)
          .itemHeight(5)
          .selectedItemWidth(14)
          .selectedItemHeight(5)
          .color('rgba(255, 255, 255, 0.35)')
          .selectedColor('#FFFFFF')
      )
    }
    .width('100%')
    .height('100%')
  }
}