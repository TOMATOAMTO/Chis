// AboutSheet.ets - Apple Style About Application Modal Sheet
@Component
export struct AboutSheet {
  @Link isAboutSheetOpen: boolean;
  @Link isDarkMode: boolean;
  @Link isEnglish: boolean;

  build() {
    Column({ space: 18 }) {
      // Header Bar
      Row() {
        Text(this.isEnglish ? 'About App' : '关于软件')
          .fontSize(18)
          .fontWeight(FontWeight.Bold)
          .fontColor(this.isDarkMode ? '#FFFFFF' : '#222222')

        Blank()

        Image($r('app.media.left'))
          .width(20)
          .height(20)
          .fillColor(this.isDarkMode ? '#FFFFFF' : '#333333')
          .onClick(() => { this.isAboutSheetOpen = false; })
      }.width('100%').margin({ bottom: 4 })

      Divider().color(this.isDarkMode ? '#242C33' : '#EFEFEF')

      // App Logo & Header Section
      Column({ space: 8 }) {
        Image($r('app.media.icon'))
          .width(68)
          .height(68)
          .borderRadius(16)
          .shadow({ radius: 8, color: 'rgba(0,0,0,0.1)', offsetY: 2 })

        Text(this.isEnglish ? 'Smart Plant Pet' : '智能植宠')
          .fontSize(20)
          .fontWeight(FontWeight.Bold)
          .fontColor(this.isDarkMode ? '#FFFFFF' : '#1C1C1E')
          .margin({ top: 4 })

        Text('Version 1.0.4 (Build 2026)')
          .fontSize(12)
          .fontColor(this.isDarkMode ? 'rgba(255,255,255,0.5)' : '#8E8E93')
      }
      .width('100%')
      .alignItems(HorizontalAlign.Center)
      .padding({ top: 8, bottom: 12 })

      // Statement Card (Apple Inset Grouped Box)
      Column({ space: 12 }) {
        Row({ space: 8 }) {
          Image($r('app.media.tip'))
            .width(16)
            .height(16)
            .fillColor(this.isDarkMode ? '#58B582' : '#224A32')
          Text('软件开发与使用声明')
            .fontSize(13.5)
            .fontWeight(FontWeight.Bold)
            .fontColor(this.isDarkMode ? '#FFFFFF' : '#1C1C1E')
        }.width('100%')

        Text('此 APP 为刘威爽用于个人学习开发展示，无商业用途。')
          .fontSize(13)
          .fontColor(this.isDarkMode ? '#E5E5EA' : '#3A3A3C')
          .lineHeight(20)
          .width('100%')

        Divider().color(this.isDarkMode ? 'rgba(255,255,255,0.08)' : 'rgba(0,0,0,0.06)')

        Text('系统架构：HarmonyOS NEXT · ArkUI · 华为云 IoTDA 硬件物联网')
          .fontSize(11.5)
          .fontColor(this.isDarkMode ? 'rgba(255,255,255,0.5)' : '#8E8E93')
          .width('100%')
      }
      .width('100%')
      .padding(16)
      .backgroundColor(this.isDarkMode ? '#242C33' : '#F5F7F6')
      .borderRadius(14)

    }
    .padding(24)
    .width('100%')
    .height('100%')
    .backgroundColor(this.isDarkMode ? '#1E2226' : '#FFFFFF')
  }
}
