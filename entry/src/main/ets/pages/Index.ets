// LoginPage.ets
import { router } from '@kit.ArkUI';
import { promptAction } from '@kit.ArkUI';

enum ActiveView {
  LANDING,          // 落地页
  SIGNUP_OPTIONS,   // 注册选项
  LOGIN_OPTIONS,    // 登录选项
  FORM_LOGIN,       // 已有账号(用户名)登录
  EMAIL_SIGNUP,     // 电子邮件注册
  EMAIL_LOGIN       // 电子邮件登录
}

@Entry
@Component
struct LoginPage {
  @State activeView: ActiveView = ActiveView.LANDING;

  // 用于动画的方向控制：1 表示前进，-1 表示后退
  @State animDirection: number = 1;

  // 表单数据 (用户名/已有账号)
  @State username: string = '';
  @State password: string = '';

  // 电子邮件表单数据
  @State email: string = '';
  @State emailPassword: string = '';
  @State confirmPassword: string = '';

  // 动态存储新注册的邮箱账号信息，保证“注册后可立即登录”
  @State registeredEmail: string = '';
  @State registeredPassword: string = '';

  // 密码可见性独立状态控制
  @State isPasswordVisible: boolean = false;
  @State isEmailPasswordVisible: boolean = false;
  @State isConfirmPasswordVisible: boolean = false;

  // 交互反馈与状态
  @State isError: boolean = false;
  @State isLoading: boolean = false;

  // 焦点控制
  @State usernameFocus: boolean = false;
  @State passwordFocus: boolean = false;
  @State emailFocus: boolean = false;
  @State emailPasswordFocus: boolean = false;
  @State confirmPasswordFocus: boolean = false;

  // 静态默认测试数据配置
  private readonly CORRECT_USERNAME: string = 'Liuweishuang';
  private readonly CORRECT_PASSWORD: string = '000000';
  private readonly CORRECT_EMAIL: string = 'liuweishuang@qingshi.com';

  // 统一的平滑切换方法
  navigate(target: ActiveView, forward: boolean = true): void {
    this.isError = false;
    this.animDirection = forward ? 1 : -1;
    animateTo({ duration: 240, curve: Curve.FastOutSlowIn }, () => {
      this.activeView = target;
    });
  }

  // 全局返回键逻辑
  handleBack(): void {
    if (this.activeView === ActiveView.SIGNUP_OPTIONS || this.activeView === ActiveView.LOGIN_OPTIONS) {
      this.navigate(ActiveView.LANDING, false);
    } else if (this.activeView === ActiveView.FORM_LOGIN || this.activeView === ActiveView.EMAIL_LOGIN) {
      this.navigate(ActiveView.LOGIN_OPTIONS, false);
    } else if (this.activeView === ActiveView.EMAIL_SIGNUP) {
      this.navigate(ActiveView.SIGNUP_OPTIONS, false);
    }
  }

  // 校验邮箱格式
  isEmailValid(emailStr: string): boolean {
    const pattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    return pattern.test(emailStr);
  }

  // 执行用户名/已有账号登录
  handleLogin(): void {
    this.isError = false;

    if (this.username.trim() === '' || this.password.trim() === '') {
      this.isError = true;
      promptAction.showToast({ message: '用户名或密码不能为空', duration: 1500 });
      return;
    }

    this.isLoading = true;
    setTimeout(() => {
      if (this.username === this.CORRECT_USERNAME && this.password === this.CORRECT_PASSWORD) {
        this.isLoading = false;
        promptAction.showToast({
          message: '登录成功，正在进入植宠空间！',
          duration: 1500
        });
        setTimeout(() => {
          router.pushUrl({ url: 'pages/main' });
        }, 800);
      } else {
        this.isLoading = false;
        this.isError = true; // 仅触发文本变红
        promptAction.showToast({ message: '用户名或密码不正确', duration: 1500 });
      }
    }, 1200);
  }

  // 执行电子邮件登录
  handleEmailLogin(): void {
    this.isError = false;

    if (!this.isEmailValid(this.email) || this.emailPassword.length < 6) {
      this.isError = true;
      promptAction.showToast({ message: '请输入有效的邮箱和6位以上密码', duration: 1500 });
      return;
    }

    this.isLoading = true;
    setTimeout(() => {
      const isDefaultValid = (this.email === this.CORRECT_EMAIL && this.emailPassword === this.CORRECT_PASSWORD);
      const isRegisteredValid = (this.email === this.registeredEmail && this.emailPassword === this.registeredPassword);

      if (isDefaultValid || isRegisteredValid) {
        this.isLoading = false;
        promptAction.showToast({
          message: '邮箱登录成功，欢迎来到青莳！',
          duration: 1500
        });
        setTimeout(() => {
          router.pushUrl({ url: 'pages/main' });
        }, 800);
      } else {
        this.isLoading = false;
        this.isError = true; // 仅触发文本变红
        promptAction.showToast({ message: '邮箱或密码不匹配', duration: 1500 });
      }
    }, 1200);
  }

  // 执行电子邮件注册
  handleEmailSignup(): void {
    this.isError = false;

    if (!this.isEmailValid(this.email)) {
      this.isError = true;
      promptAction.showToast({ message: '请输入有效的电子邮箱地址', duration: 1500 });
      return;
    }
    if (this.emailPassword.length < 6) {
      this.isError = true;
      promptAction.showToast({ message: '密码长度不能少于6位', duration: 1500 });
      return;
    }
    if (this.emailPassword !== this.confirmPassword) {
      this.isError = true;
      promptAction.showToast({ message: '两次输入的密码不一致', duration: 1500 });
      return;
    }

    this.isLoading = true;
    setTimeout(() => {
      this.isLoading = false;
      this.registeredEmail = this.email;
      this.registeredPassword = this.emailPassword;

      promptAction.showToast({
        message: '注册成功！请使用该邮箱登录。',
        duration: 2000
      });
      this.navigate(ActiveView.EMAIL_LOGIN, true);
    }, 1500);
  }

  build() {
    Stack({ alignContent: Alignment.TopStart }) {

      // 全局日式清新墨绿背景
      Column() {
        Stack() {

          // ==================== 1. LANDING (落地首屏) ====================
          if (this.activeView === ActiveView.LANDING) {
            Column() {
              Blank();

              Column() {
                Image($r('app.media.app'))
                  .width(100)
                  .height(100)
                  .objectFit(ImageFit.Contain)
                  .interpolation(ImageInterpolation.High);

                Column() {
                  Text('开启青莳，')
                    .fontSize(24)
                    .fontWeight(FontWeight.Bold)
                    .fontColor('#FFFFFF')
                    .textAlign(TextAlign.Center)
                    .offset({ x: 6 });
                  Text('守护方寸生机。')
                    .fontSize(24)
                    .fontWeight(FontWeight.Bold)
                    .fontColor('#FFFFFF')
                    .textAlign(TextAlign.Center)
                    .margin({ top: 8 })
                    .offset({ x: 6 });
                }
                .margin({ top: 28 })
                .width('100%')
                .padding({ left: 24, right: 24 });
              }
              .margin({ top: 40 })

              Blank();

              Column({ space: 16 }) {
                Button('免费注册')
                  .width('100%')
                  .height(50)
                  .fontSize(16)
                  .fontWeight(FontWeight.Bold)
                  .fontColor('#1e5f41')
                  .backgroundColor('#FFFFFF')
                  .borderRadius(25)
                  .onClick(() => {
                    this.navigate(ActiveView.SIGNUP_OPTIONS, true);
                  });

                Button('登录')
                  .width('100%')
                  .height(50)
                  .fontSize(16)
                  .fontWeight(FontWeight.Bold)
                  .fontColor('#FFFFFF')
                  .backgroundColor('transparent')
                  .border({ width: 1.5, color: 'rgba(255,255,255,0.45)' })
                  .borderRadius(25)
                  .onClick(() => {
                    this.navigate(ActiveView.LOGIN_OPTIONS, true);
                  });
              }
              .width('100%')
              .margin({ bottom: 65 });
            }
            .width('100%')
            .height('100%')
            .transition(
              TransitionEffect.asymmetric(
                TransitionEffect.move(this.animDirection > 0 ? TransitionEdge.END : TransitionEdge.START),
                TransitionEffect.move(this.animDirection > 0 ? TransitionEdge.START : TransitionEdge.END)
              )
            );

            // ==================== 2. SIGNUP_OPTIONS (注册选择) ====================
          } else if (this.activeView === ActiveView.SIGNUP_OPTIONS) {
            Column() {
              Blank();

              Column() {
                Image($r('app.media.app'))
                  .width(100)
                  .height(100)
                  .objectFit(ImageFit.Contain)
                  .interpolation(ImageInterpolation.High);

                Column() {
                  Text('连接智能花盆，')
                    .fontSize(24)
                    .fontWeight(FontWeight.Bold)
                    .fontColor('#FFFFFF')
                    .textAlign(TextAlign.Center)
                    .offset({ x: 6 });
                  Text('聆听花的呼吸。')
                    .fontSize(24)
                    .fontWeight(FontWeight.Bold)
                    .fontColor('#FFFFFF')
                    .textAlign(TextAlign.Center)
                    .margin({ top: 8 })
                    .offset({ x: 6 });
                }
                .margin({ top: 28 })
                .width('100%')
                .padding({ left: 24, right: 24 });
              }
              .margin({ top: 40 })

              Blank();

              Column({ space: 20 }) {
                Button() {
                  Stack({ alignContent: Alignment.Start }) {
                    Image($r('app.media.email'))
                      .width(32)
                      .height(32)
                      .fillColor('#FFFFFF')
                      .margin({ left: 16 });

                    Row() {
                      Text('使用电子邮件继续')
                        .fontSize(15)
                        .fontWeight(FontWeight.Bold)
                        .fontColor('#FFFFFF');
                    }
                    .width('100%')
                    .justifyContent(FlexAlign.Center)
                  }
                  .width('100%')
                  .height(50)
                }
                .width('100%')
                .height(50)
                .backgroundColor('#2b7b58')
                .border({ width: 1.5, color: 'rgba(255,255,255,0.25)' })
                .borderRadius(25)
                .onClick(() => {
                  this.navigate(ActiveView.EMAIL_SIGNUP, true);
                });

                Row() {
                  Text('已拥有账号？')
                    .fontSize(13)
                    .fontColor('rgba(255, 255, 255, 0.6)');
                  Text('登录')
                    .fontSize(13)
                    .fontWeight(FontWeight.Bold)
                    .fontColor('#FFFFFF')
                    .margin({ left: 4 })
                    .onClick(() => {
                      this.navigate(ActiveView.LOGIN_OPTIONS, true);
                    });
                }
              }
              .width('100%')
              .margin({ bottom: 65 });
            }
            .width('100%')
            .height('100%')
            .transition(
              TransitionEffect.asymmetric(
                TransitionEffect.move(this.animDirection > 0 ? TransitionEdge.END : TransitionEdge.START),
                TransitionEffect.move(this.animDirection > 0 ? TransitionEdge.START : TransitionEdge.END)
              )
            );

            // ==================== 3. LOGIN_OPTIONS (登录选择) ====================
          } else if (this.activeView === ActiveView.LOGIN_OPTIONS) {
            Column() {
              Blank();

              Column() {
                Image($r('app.media.app'))
                  .width(100)
                  .height(100)
                  .objectFit(ImageFit.Contain)
                  .interpolation(ImageInterpolation.High);

                Column() {
                  Text('回归纯粹自然，')
                    .fontSize(24)
                    .fontWeight(FontWeight.Bold)
                    .fontColor('#FFFFFF')
                    .textAlign(TextAlign.Center)
                    .offset({ x: 6 });
                  Text('感知土壤温度。')
                    .fontSize(24)
                    .fontWeight(FontWeight.Bold)
                    .fontColor('#FFFFFF')
                    .textAlign(TextAlign.Center)
                    .margin({ top: 8 })
                    .offset({ x: 6 });
                }
                .margin({ top: 28 })
                .width('100%')
                .padding({ left: 24, right: 24 });
              }
              .margin({ top: 40 })

              Blank();

              Column({ space: 16 }) {
                Button('使用已有账号继续')
                  .width('100%')
                  .height(50)
                  .fontSize(15)
                  .fontWeight(FontWeight.Bold)
                  .fontColor('#1e5f41')
                  .backgroundColor('#FFFFFF')
                  .borderRadius(25)
                  .onClick(() => {
                    this.navigate(ActiveView.FORM_LOGIN, true);
                  });

                Button() {
                  Stack({ alignContent: Alignment.Start }) {
                    Image($r('app.media.email'))
                      .width(32)
                      .height(32)
                      .fillColor('#FFFFFF')
                      .margin({ left: 16 });

                    Row() {
                      Text('使用电子邮件继续')
                        .fontSize(15)
                        .fontWeight(FontWeight.Bold)
                        .fontColor('#FFFFFF');
                    }
                    .width('100%')
                    .justifyContent(FlexAlign.Center)
                  }
                  .width('100%')
                  .height(50)
                }
                .width('100%')
                .height(50)
                .backgroundColor('transparent')
                .border({ width: 1.5, color: 'rgba(255,255,255,0.3)' })
                .borderRadius(25)
                .onClick(() => {
                  this.navigate(ActiveView.EMAIL_LOGIN, true);
                });

                Row() {
                  Text('没有账号？')
                    .fontSize(13)
                    .fontColor('rgba(255, 255, 255, 0.6)');
                  Text('立即注册')
                    .fontSize(13)
                    .fontWeight(FontWeight.Bold)
                    .fontColor('#FFFFFF')
                    .margin({ left: 4 })
                    .onClick(() => {
                      this.navigate(ActiveView.SIGNUP_OPTIONS, true);
                    });
                }
              }
              .width('100%')
              .margin({ bottom: 65 });
            }
            .width('100%')
            .height('100%')
            .transition(
              TransitionEffect.asymmetric(
                TransitionEffect.move(this.animDirection > 0 ? TransitionEdge.END : TransitionEdge.START),
                TransitionEffect.move(this.animDirection > 0 ? TransitionEdge.START : TransitionEdge.END)
              )
            );

            // ==================== 4. FORM_LOGIN (已有用户名密码登录) ====================
          } else if (this.activeView === ActiveView.FORM_LOGIN) {
            Column() {
              Blank();

              Column() {
                Image($r('app.media.app'))
                  .width(100)
                  .height(100)
                  .objectFit(ImageFit.Contain)
                  .interpolation(ImageInterpolation.High);

                Column() {
                  Text('同步植宠空间，')
                    .fontSize(24)
                    .fontWeight(FontWeight.Bold)
                    .fontColor('#FFFFFF')
                    .textAlign(TextAlign.Center)
                    .offset({ x: 6 });
                  Text('开始绿色对话。')
                    .fontSize(24)
                    .fontWeight(FontWeight.Bold)
                    .fontColor('#FFFFFF')
                    .textAlign(TextAlign.Center)
                    .margin({ top: 8 })
                    .offset({ x: 6 });
                }
                .margin({ top: 28 })
                .width('100%')
                .padding({ left: 24, right: 24 });
              }
              .margin({ top: 40 })

              Blank();

              Column({ space: 16 }) {
                // 用户名输入框
                Column({ space: 6 }) {
                  Text('用户名')
                    .fontSize(11)
                    .fontColor('#FFFFFF')
                    .alignSelf(ItemAlign.Start);

                  TextInput({ text: this.username })
                    .placeholderColor('rgba(255,255,255,0.25)')
                    .fontSize(15)
                    .fontColor(this.isError ? '#FF6B6B' : '#FFFFFF')
                    .backgroundColor('rgba(255, 255, 255, 0.12)')
                    .border({
                      width: 1.5,
                      color: this.usernameFocus ? '#FFFFFF' : 'transparent'
                    })
                    .borderRadius(12)
                    .height(48)
                    .onChange((value: string) => {
                      this.username = value;
                      if (this.isError) this.isError = false;
                    })
                    .onFocus(() => { this.usernameFocus = true })
                    .onBlur(() => { this.usernameFocus = false });
                }
                .width('100%');

                // 自定义密码框：彻底让你的两个小眼睛SVG强制变白
                Column({ space: 6 }) {
                  Text('密码')
                    .fontSize(11)
                    .fontColor('#FFFFFF')
                    .alignSelf(ItemAlign.Start);

                  Row() {
                    TextInput({ text: this.password })
                      .placeholderColor('rgba(255,255,255,0.25)')
                      .fontSize(15)
                      .fontColor(this.isError ? '#FF6B6B' : '#FFFFFF')
                      .backgroundColor('transparent')
                      .type(this.isPasswordVisible ? InputType.Normal : InputType.Password)
                      .showPasswordIcon(false) // 禁用默认原厂眼睛
                      .layoutWeight(1)
                      .height(48)
                      .onChange((value: string) => {
                        this.password = value;
                        if (this.isError) this.isError = false;
                      })
                      .onFocus(() => { this.passwordFocus = true })
                      .onBlur(() => { this.passwordFocus = false });

                    // 使用 Image 组件配合 fillColor，将 SVG 100% 漂白
                    Image(this.isPasswordVisible ? $r('app.media.preview_open') : $r('app.media.preview_close'))
                      .width(22)
                      .height(22)
                      .fillColor('#FFFFFF')
                      .margin({ right: 12 })
                      .onClick(() => {
                        this.isPasswordVisible = !this.isPasswordVisible;
                      });
                  }
                  .width('100%')
                  .backgroundColor('rgba(255, 255, 255, 0.12)')
                  .border({
                    width: 1.5,
                    color: this.passwordFocus ? '#FFFFFF' : 'transparent'
                  })
                  .borderRadius(12);
                }
                .width('100%');

                Button() {
                  Row({ space: 8 }) {
                    if (this.isLoading) {
                      LoadingProgress().width(18).height(18).color('#1e5f41');
                      Text('正在接入...').fontSize(16).fontWeight(FontWeight.Bold).fontColor('#1e5f41');
                    } else {
                      Text('立即登录')
                        .fontSize(16)
                        .fontWeight(FontWeight.Bold)
                        .fontColor('#1e5f41');
                    }
                  }
                  .justifyContent(FlexAlign.Center)
                }
                .width('100%')
                .height(50)
                .enabled(!this.isLoading)
                .backgroundColor('#FFFFFF')
                .borderRadius(25)
                .margin({ top: 12 })
                .onClick(() => {
                  this.handleLogin();
                });
              }
              .width('100%')
              .margin({ bottom: 65 });
            }
            .width('100%')
            .height('100%')
            .transition(
              TransitionEffect.asymmetric(
                TransitionEffect.move(this.animDirection > 0 ? TransitionEdge.END : TransitionEdge.START),
                TransitionEffect.move(this.animDirection > 0 ? TransitionEdge.START : TransitionEdge.END)
              )
            );

            // ==================== 5. EMAIL_SIGNUP (邮箱注册页) ====================
          } else if (this.activeView === ActiveView.EMAIL_SIGNUP) {
            Column() {
              Blank();

              Column() {
                Image($r('app.media.app'))
                  .width(100)
                  .height(100)
                  .objectFit(ImageFit.Contain)
                  .interpolation(ImageInterpolation.High);

                Column() {
                  Text('开启青莳，')
                    .fontSize(24)
                    .fontWeight(FontWeight.Bold)
                    .fontColor('#FFFFFF')
                    .textAlign(TextAlign.Center)
                    .offset({ x: 6 });
                  Text('守护方寸生机。')
                    .fontSize(24)
                    .fontWeight(FontWeight.Bold)
                    .fontColor('#FFFFFF')
                    .textAlign(TextAlign.Center)
                    .margin({ top: 8 })
                    .offset({ x: 6 });
                }
                .margin({ top: 28 })
                .width('100%')
                .padding({ left: 24, right: 24 });
              }
              .margin({ top: 40 })

              Blank();

              Column({ space: 14 }) {
                // 注册邮箱
                Column({ space: 4 }) {
                  Text('电子邮箱')
                    .fontSize(11)
                    .fontColor('#FFFFFF')
                    .alignSelf(ItemAlign.Start);

                  TextInput({ text: this.email })
                    .placeholderColor('rgba(255,255,255,0.25)')
                    .fontSize(15)
                    .fontColor(this.isError ? '#FF6B6B' : '#FFFFFF')
                    .backgroundColor('rgba(255, 255, 255, 0.12)')
                    .border({ width: 1.5, color: this.emailFocus ? '#FFFFFF' : 'transparent' })
                    .borderRadius(12)
                    .height(46)
                    .onChange((value: string) => {
                      this.email = value;
                      if (this.isError) this.isError = false;
                    })
                    .onFocus(() => { this.emailFocus = true })
                    .onBlur(() => { this.emailFocus = false });
                }
                .width('100%');

                // 注册密码（自定义白眼睛）
                Column({ space: 4 }) {
                  Text('注册密码')
                    .fontSize(11)
                    .fontColor('#FFFFFF')
                    .alignSelf(ItemAlign.Start);

                  Row() {
                    TextInput({ text: this.emailPassword })
                      .placeholderColor('rgba(255,255,255,0.25)')
                      .fontSize(15)
                      .fontColor(this.isError ? '#FF6B6B' : '#FFFFFF')
                      .backgroundColor('transparent')
                      .type(this.isEmailPasswordVisible ? InputType.Normal : InputType.Password)
                      .showPasswordIcon(false)
                      .layoutWeight(1)
                      .height(46)
                      .onChange((value: string) => {
                        this.emailPassword = value;
                        if (this.isError) this.isError = false;
                      })
                      .onFocus(() => { this.emailPasswordFocus = true })
                      .onBlur(() => { this.emailPasswordFocus = false });

                    Image(this.isEmailPasswordVisible ? $r('app.media.preview_open') : $r('app.media.preview_close'))
                      .width(22)
                      .height(22)
                      .fillColor('#FFFFFF')
                      .margin({ right: 12 })
                      .onClick(() => {
                        this.isEmailPasswordVisible = !this.isEmailPasswordVisible;
                      });
                  }
                  .width('100%')
                  .backgroundColor('rgba(255, 255, 255, 0.12)')
                  .border({ width: 1.5, color: this.emailPasswordFocus ? '#FFFFFF' : 'transparent' })
                  .borderRadius(12);
                }
                .width('100%');

                // 确认密码（自定义白眼睛）
                Column({ space: 4 }) {
                  Text('确认密码')
                    .fontSize(11)
                    .fontColor('#FFFFFF')
                    .alignSelf(ItemAlign.Start);

                  Row() {
                    TextInput({ text: this.confirmPassword })
                      .placeholderColor('rgba(255,255,255,0.25)')
                      .fontSize(15)
                      .fontColor(this.isError ? '#FF6B6B' : '#FFFFFF')
                      .backgroundColor('transparent')
                      .type(this.isConfirmPasswordVisible ? InputType.Normal : InputType.Password)
                      .showPasswordIcon(false)
                      .layoutWeight(1)
                      .height(46)
                      .onChange((value: string) => {
                        this.confirmPassword = value;
                        if (this.isError) this.isError = false;
                      })
                      .onFocus(() => { this.confirmPasswordFocus = true })
                      .onBlur(() => { this.confirmPasswordFocus = false });

                    Image(this.isConfirmPasswordVisible ? $r('app.media.preview_open') : $r('app.media.preview_close'))
                      .width(22)
                      .height(22)
                      .fillColor('#FFFFFF')
                      .margin({ right: 12 })
                      .onClick(() => {
                        this.isConfirmPasswordVisible = !this.isConfirmPasswordVisible;
                      });
                  }
                  .width('100%')
                  .backgroundColor('rgba(255, 255, 255, 0.12)')
                  .border({ width: 1.5, color: this.confirmPasswordFocus ? '#FFFFFF' : 'transparent' })
                  .borderRadius(12);
                }
                .width('100%');

                Button() {
                  Row({ space: 8 }) {
                    if (this.isLoading) {
                      LoadingProgress().width(18).height(18).color('#1e5f41');
                      Text('注册中...').fontSize(16).fontWeight(FontWeight.Bold).fontColor('#1e5f41');
                    } else {
                      Text('确认注册并登录')
                        .fontSize(16)
                        .fontWeight(FontWeight.Bold)
                        .fontColor('#1e5f41');
                    }
                  }
                  .justifyContent(FlexAlign.Center)
                }
                .width('100%')
                .height(50)
                .enabled(!this.isLoading)
                .backgroundColor('#FFFFFF')
                .borderRadius(25)
                .margin({ top: 8 })
                .onClick(() => {
                  this.handleEmailSignup();
                });
              }
              .width('100%')
              .margin({ bottom: 65 });
            }
            .width('100%')
            .height('100%')
            .transition(
              TransitionEffect.asymmetric(
                TransitionEffect.move(this.animDirection > 0 ? TransitionEdge.END : TransitionEdge.START),
                TransitionEffect.move(this.animDirection > 0 ? TransitionEdge.START : TransitionEdge.END)
              )
            );

            // ==================== 6. EMAIL_LOGIN (邮箱登录页) ====================
          } else if (this.activeView === ActiveView.EMAIL_LOGIN) {
            Column() {
              Blank();

              Column() {
                Image($r('app.media.app'))
                  .width(100)
                  .height(100)
                  .objectFit(ImageFit.Contain)
                  .interpolation(ImageInterpolation.High);

                Column() {
                  Text('开启青莳，')
                    .fontSize(24)
                    .fontWeight(FontWeight.Bold)
                    .fontColor('#FFFFFF')
                    .textAlign(TextAlign.Center)
                    .offset({ x: 6 });
                  Text('守护方寸生机。')
                    .fontSize(24)
                    .fontWeight(FontWeight.Bold)
                    .fontColor('#FFFFFF')
                    .textAlign(TextAlign.Center)
                    .margin({ top: 8 })
                    .offset({ x: 6 });
                }
                .margin({ top: 28 })
                .width('100%')
                .padding({ left: 24, right: 24 });
              }
              .margin({ top: 40 })

              Blank();

              Column({ space: 16 }) {
                // 邮箱输入
                Column({ space: 6 }) {
                  Text('电子邮箱')
                    .fontSize(11)
                    .fontColor('#FFFFFF')
                    .alignSelf(ItemAlign.Start);

                  TextInput({ text: this.email })
                    .placeholderColor('rgba(255,255,255,0.25)')
                    .fontSize(15)
                    .fontColor(this.isError ? '#FF6B6B' : '#FFFFFF')
                    .backgroundColor('rgba(255, 255, 255, 0.12)')
                    .border({ width: 1.5, color: this.emailFocus ? '#FFFFFF' : 'transparent' })
                    .borderRadius(12)
                    .height(48)
                    .onChange((value: string) => {
                      this.email = value;
                      if (this.isError) this.isError = false;
                    })
                    .onFocus(() => { this.emailFocus = true })
                    .onBlur(() => { this.emailFocus = false });
                }
                .width('100%');

                // 密码输入（自定义白眼睛）
                Column({ space: 6 }) {
                  Text('密码')
                    .fontSize(11)
                    .fontColor('#FFFFFF')
                    .alignSelf(ItemAlign.Start);

                  Row() {
                    TextInput({ text: this.emailPassword })
                      .placeholderColor('rgba(255,255,255,0.25)')
                      .fontSize(15)
                      .fontColor(this.isError ? '#FF6B6B' : '#FFFFFF')
                      .backgroundColor('transparent')
                      .type(this.isEmailPasswordVisible ? InputType.Normal : InputType.Password)
                      .showPasswordIcon(false)
                      .layoutWeight(1)
                      .height(48)
                      .onChange((value: string) => {
                        this.emailPassword = value;
                        if (this.isError) this.isError = false;
                      })
                      .onFocus(() => { this.emailPasswordFocus = true })
                      .onBlur(() => { this.emailPasswordFocus = false });

                    Image(this.isEmailPasswordVisible ? $r('app.media.preview_open') : $r('app.media.preview_close'))
                      .width(22)
                      .height(22)
                      .fillColor('#FFFFFF')
                      .margin({ right: 12 })
                      .onClick(() => {
                        this.isEmailPasswordVisible = !this.isEmailPasswordVisible;
                      });
                  }
                  .width('100%')
                  .backgroundColor('rgba(255, 255, 255, 0.12)')
                  .border({ width: 1.5, color: this.emailPasswordFocus ? '#FFFFFF' : 'transparent' })
                  .borderRadius(12);
                }
                .width('100%');

                Button() {
                  Row({ space: 8 }) {
                    if (this.isLoading) {
                      LoadingProgress().width(18).height(18).color('#1e5f41');
                      Text('正在接入...').fontSize(16).fontWeight(FontWeight.Bold).fontColor('#1e5f41');
                    } else {
                      Text('确认登录')
                        .fontSize(16)
                        .fontWeight(FontWeight.Bold)
                        .fontColor('#1e5f41');
                    }
                  }
                  .justifyContent(FlexAlign.Center)
                }
                .width('100%')
                .height(50)
                .enabled(!this.isLoading)
                .backgroundColor('#FFFFFF')
                .borderRadius(25)
                .margin({ top: 12 })
                .onClick(() => {
                  this.handleEmailLogin();
                });
              }
              .width('100%')
              .margin({ bottom: 45 });
            }
            .width('100%')
            .height('100%')
            .transition(
              TransitionEffect.asymmetric(
                TransitionEffect.move(this.animDirection > 0 ? TransitionEdge.END : TransitionEdge.START),
                TransitionEffect.move(this.animDirection > 0 ? TransitionEdge.START : TransitionEdge.END)
              )
            );
          }

        }
        .width('100%')
        .height('100%')
      }
      .width('100%')
      .height('100%')
      .padding({ left: 32, right: 32 })
      .backgroundColor('#1e5f41')
      .expandSafeArea([SafeAreaType.SYSTEM], [SafeAreaEdge.TOP, SafeAreaEdge.BOTTOM]);

      // ========== 顶部左侧超平服极简返回键 ==========
      if (this.activeView !== ActiveView.LANDING) {
        Image($r('app.media.left'))
          .width(26)
          .height(26)
          .fillColor('#FFFFFF')
          .margin({ top: 52, left: 16 })
          .onClick(() => {
            this.handleBack();
          })
          .transition(TransitionEffect.OPACITY.animation({ duration: 120 }));
      }
    }
    .width('100%')
    .height('100%');
  }
}