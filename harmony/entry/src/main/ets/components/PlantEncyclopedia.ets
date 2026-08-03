// PlantEncyclopedia.ets
import { http } from '@kit.NetworkKit';
import { util } from '@kit.ArkTS';
import { BusinessError } from '@kit.BasicServicesKit';

export interface PlantImage { thumbnail: string; original_url: string; }
export interface PlantApiItem {
  id: number; common_name: string | null; scientific_name: string[] | null;
  other_name: string[] | null; cycle: string | null; watering: string | null;
  sunlight: string[] | null; default_image: PlantImage | null;
}
export interface PlantApiResponse { data: PlantApiItem[]; }

interface GenusNode { cn: string; type: string; alias: string[]; }
interface EpithetNode { cn: string; position: string; }

@Component
export struct PlantEncyclopedia {
  @Link isDarkMode: boolean;
  @Link isEnglish: boolean;
  @Link isCustomDetailOpen: boolean;
  @Link selectedPlant: PlantApiItem | null;
  @Link detailPanelTranslateY: string;

  @State searchQuery: string = '';
  @State plantList: PlantApiItem[] = [];
  @State isSearching: boolean = false;

  private apiKey: string = 'sk-n9rO6a5a4027caa3c18826';
  private genusMap: Record<string, GenusNode> = {};
  private epithetMap: Record<string, EpithetNode> = {};
  private aliasMap: Record<string, string> = {};
  private invertedIndexTable: Record<string, string> = {};

  aboutToAppear() {
    this.initSearchEngine();
  }

  private async initSearchEngine() {
    try {
      let resMgr = getContext(this).resourceManager;
      let decoder = util.TextDecoder.create('utf-8');
      let genusBytes = await resMgr.getRawFileContent('genus_lib.json');
      this.genusMap = JSON.parse(decoder.decodeWithStream(genusBytes)) as Record<string, GenusNode>;
      let epithetBytes = await resMgr.getRawFileContent('epithet_lib.json');
      this.epithetMap = JSON.parse(decoder.decodeWithStream(epithetBytes)) as Record<string, EpithetNode>;
      let aliasBytes = await resMgr.getRawFileContent('common_alias.json');
      this.aliasMap = JSON.parse(decoder.decodeWithStream(aliasBytes)) as Record<string, string>;

      let genusKeys = Object.keys(this.genusMap);
      for (let i = 0; i < genusKeys.length; i++) {
        let genusKey = genusKeys[i];
        let node = this.genusMap[genusKey];
        if (node && node.alias) {
          for (let j = 0; j < node.alias.length; j++) {
            this.invertedIndexTable[node.alias[j].toLowerCase()] = genusKey;
          }
        }
      }
    } catch (error) {
      console.error('资源库读取失败');
    }
    this.fetchPlantData('');
  }

  private chineseToEnglishEngine(chineseKey: string): string {
    let cleanKey = chineseKey.trim().toLowerCase();
    if (!cleanKey) return 'indoor';
    if (this.invertedIndexTable[cleanKey] !== undefined) return this.invertedIndexTable[cleanKey];
    let indexKeys = Object.keys(this.invertedIndexTable);
    for (let i = 0; i < indexKeys.length; i++) {
      if (cleanKey.includes(indexKeys[i])) return this.invertedIndexTable[indexKeys[i]];
    }
    return cleanKey;
  }

  // 🎯【全量找回】：高级分词拼装引擎，彻底消灭单字“枫”字显示，恢复高级翻译
  private getTransName(plant: PlantApiItem | null): string {
    if (!plant) return "未知植物";
    if (this.isEnglish) return plant.common_name || "Unknown Plant";
    if (plant.scientific_name && plant.scientific_name.length > 0) {
      let fullScientific = plant.scientific_name[0];
      let parts = fullScientific.split(' ');
      if (parts.length >= 2) {
        let genusStr = parts[0].toLowerCase();
        let epithetStr = parts[1].toLowerCase().replace(/['"’“]/g, '');
        let genusNode = this.genusMap[genusStr];
        let resultCn = "";

        if (!genusNode) {
          let genusKeys = Object.keys(this.genusMap);
          for (let i = 0; i < genusKeys.length; i++) {
            if (genusStr.startsWith(genusKeys[i]) && genusKeys[i].length >= 4) {
              genusNode = this.genusMap[genusKeys[i]]; break;
            }
          }
        }
        if (genusNode) {
          resultCn = genusNode.cn.split('/')[0];
          let epithetNode = this.epithetMap[epithetStr];
          if (!epithetNode) {
            let epithetKeys = Object.keys(this.epithetMap);
            for (let i = 0; i < epithetKeys.length; i++) {
              if (epithetStr.startsWith(epithetKeys[i]) && epithetKeys[i].length >= 4) {
                epithetNode = this.epithetMap[epithetKeys[i]]; break;
              }
            }
          }
          if (epithetNode) {
            resultCn = (epithetNode.position === 'prefix') ? (epithetNode.cn + resultCn) : (resultCn + '·' + epithetNode.cn);
          }
        }
        let cultivarMatch = fullScientific.match(/'(.+?)'/);
        if (cultivarMatch && cultivarMatch[1]) {
          let cultivarKey = cultivarMatch[1].toLowerCase();
          let cultivarCn = this.epithetMap[cultivarKey] ? this.epithetMap[cultivarKey].cn : cultivarMatch[1];
          resultCn = resultCn ? (resultCn + '·' + cultivarCn) : cultivarCn;
        }
        if (resultCn) return resultCn;
      }
    }
    return this.aliasMap[plant.common_name?.toLowerCase() || ''] || plant.common_name || "常绿自然植卉";
  }

  private fetchPlantData(keyword: string) {
    this.isSearching = true;
    let finalQuery = this.chineseToEnglishEngine(keyword);
    let url = keyword.trim() ? `https://perenual.com/api/species-list?key=${this.apiKey}&q=${encodeURIComponent(finalQuery)}` : `https://perenual.com/api/species-list?key=${this.apiKey}&page=1`;

    let httpRequest = http.createHttp();
    httpRequest.request(url, { method: http.RequestMethod.GET, readTimeout: 8000 }, (err: BusinessError, data: http.HttpResponse) => {
      if (!err && data.responseCode === 200) {
        try {
          let resObj = JSON.parse(data.result as string) as PlantApiResponse;
          if (resObj && resObj.data) {
            if (keyword.trim().length > 0) {
              let searchWord = keyword.trim().toLowerCase();
              let targetLatinRoot = finalQuery.toLowerCase();
              this.plantList = resObj.data.filter((item: PlantApiItem) => {
                let cnTrans = this.getTransName(item);
                return cnTrans.includes(searchWord) || (item.scientific_name?.[0].toLowerCase().includes(targetLatinRoot) ?? false);
              });
            } else {
              this.plantList = resObj.data;
            }
          }
        } catch (e) {}
      }
      this.isSearching = false;
    });
  }

  build() {
    Column() {
      Row() {
        Column() {
          Text(this.isEnglish ? 'Encyclopedia' : '百草图鉴').fontSize(22).fontWeight(FontWeight.Bold).fontColor(this.isDarkMode ? '#FFFFFF' : '#222222')
          Text('开放式智能倒排分词搜索引擎已就绪').fontSize(13).fontColor(this.isDarkMode ? '#8AB399' : '#666666').margin({ top: 4 })
        }.alignItems(HorizontalAlign.Start)
      }.width('100%').padding({ left: 24, right: 24, top: 52, bottom: 12 })

      Row() {
        Image($r('app.media.search')).width(16).height(16).fillColor(this.isDarkMode ? '#FFFFFF' : '#555555').margin({ left: 14, right: 4 })
        TextInput({ text: this.searchQuery, placeholder: '搜索植物' })
          .layoutWeight(1).height(40).backgroundColor('transparent').placeholderColor('#999999').fontSize(14).fontColor(this.isDarkMode ? '#FFFFFF' : '#222222')
          .onChange((value: string) => {
            this.searchQuery = value;
            if (value.trim().length >= 1) { this.fetchPlantData(value); }
          })
          .onSubmit(() => { this.fetchPlantData(this.searchQuery); })
        Text('搜索').fontSize(14).fontWeight(FontWeight.Bold).fontColor('#1E5F41').margin({ right: 14 }).onClick(() => { this.fetchPlantData(this.searchQuery); })
      }
      .width('90%').height(42).backgroundColor(this.isDarkMode ? '#1E2226' : '#FFFFFF').borderRadius(10).margin({ bottom: 16 })

      if (this.isSearching) {
        Column() { LoadingProgress().width(36).height(36).color('#1E5F41') }.width('100%').layoutWeight(1).justifyContent(FlexAlign.Center)
      } else {
        List({ space: 10 }) {
          ForEach(this.plantList, (plant: PlantApiItem) => {
            ListItem() {
              Row() {
                Image(plant.default_image?.thumbnail || $r('app.media.home')).width(52).height(52).borderRadius(10).objectFit(ImageFit.Cover).margin({ right: 16 })
                Column() {
                  Text(this.getTransName(plant)).fontSize(16).fontWeight(FontWeight.Medium).fontColor(this.isDarkMode ? '#FFFFFF' : '#222222')
                  Text(plant.scientific_name?.[0] || 'Unknown Sp.').fontSize(11).fontStyle(FontStyle.Italic).fontColor('#999999').margin({ top: 3 })
                }.alignItems(HorizontalAlign.Start).layoutWeight(1)
                if (this.isDarkMode) {
                  Image($r('app.media.right_night')).width(16).height(16)
                } else {
                  Image($r('app.media.right')).width(16).height(16)
                }
              }
              .padding(12).backgroundColor(this.isDarkMode ? '#1E2226' : '#FFFFFF').borderRadius(12)
              .onClick(() => {
                this.selectedPlant = plant;
                this.isCustomDetailOpen = true;
                animateTo({ duration: 300, curve: Curve.FastOutSlowIn }, () => { this.detailPanelTranslateY = '0%'; });
              })
            }.padding({ left: 16, right: 16 })
          })
        }.width('100%').layoutWeight(1)
      }
    }
    .width('100%').height('100%').backgroundColor(this.isDarkMode ? '#12161A' : '#F5F7F6')
  }
}