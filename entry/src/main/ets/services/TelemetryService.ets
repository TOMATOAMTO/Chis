// TelemetryService.ets - Intelligent Horticultural Telemetry Diagnostic Service
export interface MetricDiagnostic {
  status: 'LOW' | 'NORMAL' | 'HIGH';
  label: string;
  text: string;
  color: string;
}

export interface OverallHealth {
  statusText: string;
  advice: string;
  color: string;
  badgeBg: string;
}

export class TelemetryService {
  // 1. 土壤湿度诊断 (Moisture)
  static getMoistureDiagnostic(valStr: string): MetricDiagnostic {
    let num = parseFloat(valStr);
    if (isNaN(num)) return { status: 'NORMAL', label: '水分适宜', text: '土壤含水量正常，支持根系顺畅呼吸。', color: '#4CAF50' };
    if (num < 30) {
      return { status: 'LOW', label: '干旱缺水', text: '土壤偏干(＜30%)，叶片易打蔫卷曲，建议沿盆边透浇一次水分。', color: '#E65100' };
    } else if (num > 75) {
      return { status: 'HIGH', label: '积水过涝', text: '土壤极度湿涝(＞75%)，易导致根系缺氧腐烂，建议加强通风并暂停浇水。', color: '#D32F2F' };
    }
    return { status: 'NORMAL', label: '水分适宜', text: '土壤湿度维持在最佳 30%~75% 区间，根系生长极佳。', color: '#4CAF50' };
  }

  // 2. 土壤温度诊断 (Temperature)
  static getTempDiagnostic(valStr: string): MetricDiagnostic {
    let num = parseFloat(valStr);
    if (isNaN(num)) return { status: 'NORMAL', label: '温度适宜', text: '环境温度舒适，适合植物维持正常生长。', color: '#4CAF50' };
    if (num < 15) {
      return { status: 'LOW', label: '低温偏冷', text: '气温偏低(＜15°C)生长变缓，建议移入室内温暖避风处。', color: '#1976D2' };
    } else if (num > 28) {
      return { status: 'HIGH', label: '高温闷热', text: '环境温度较高(＞28°C)蒸发加剧，建议遮阴防晒并向周围喷雾降温。', color: '#D32F2F' };
    }
    return { status: 'NORMAL', label: '温度舒适', text: '15°C~28°C 舒适温域，有利于养分积累与新芽抽发。', color: '#4CAF50' };
  }

  // 3. 光照强度诊断 (Light)
  static getLightDiagnostic(valStr: string): MetricDiagnostic {
    let num = parseFloat(valStr);
    if (isNaN(num)) return { status: 'NORMAL', label: '光照充足', text: '散射光充沛，进行高效光合作用。', color: '#4CAF50' };
    if (num < 1500) {
      return { status: 'LOW', label: '光照不足', text: '环境较暗(＜1500 LUX)，长期易导致徒长黄叶，建议移至明亮阳台。', color: '#E65100' };
    } else if (num > 6000) {
      return { status: 'HIGH', label: '强光灼伤', text: '阳光过强(＞6000 LUX)，可能晒伤娇嫩叶片，请适当拉帘遮阴。', color: '#D32F2F' };
    }
    return { status: 'NORMAL', label: '光效极佳', text: '光照强度处于理想散射光区，叶色浓绿光亮。', color: '#4CAF50' };
  }

  // 4. 土壤养分诊断 (NPK)
  static getNpkDiagnostic(valStr: string): MetricDiagnostic {
    let num = parseFloat(valStr);
    if (isNaN(num)) return { status: 'NORMAL', label: '肥力均衡', text: '养分充沛，支撑健康生长。', color: '#4CAF50' };
    if (num < 400) {
      return { status: 'LOW', label: '养分缺乏', text: '肥力偏低(＜400 NPK)，植物易黄叶瘦弱，建议适量追施稀释薄肥。', color: '#E65100' };
    } else if (num > 1200) {
      return { status: 'HIGH', label: '肥害风险', text: '养分浓度过高(＞1200 NPK)易烧根，建议大水浇透冲洗余肥。', color: '#D32F2F' };
    }
    return { status: 'NORMAL', label: '肥力均衡', text: '氮磷钾养分充沛，为植物抽枝发叶提供强劲动力。', color: '#4CAF50' };
  }

  // 5. 综合健康评估 (Overall Health)
  static getOverallDeviceHealth(moisture: string, temp: string, light: string, npk: string): OverallHealth {
    let m = TelemetryService.getMoistureDiagnostic(moisture);
    let t = TelemetryService.getTempDiagnostic(temp);
    let l = TelemetryService.getLightDiagnostic(light);
    let n = TelemetryService.getNpkDiagnostic(npk);

    if (m.status === 'HIGH' || t.status === 'HIGH' || n.status === 'HIGH' || l.status === 'HIGH') {
      let warnText = m.status === 'HIGH' ? m.text : (t.status === 'HIGH' ? t.text : (n.status === 'HIGH' ? n.text : l.text));
      return { statusText: '注意预警', advice: warnText, color: '#D32F2F', badgeBg: 'rgba(211,47,47,0.1)' };
    }
    if (m.status === 'LOW' || t.status === 'LOW' || n.status === 'LOW' || l.status === 'LOW') {
      let adviceText = m.status === 'LOW' ? m.text : (t.status === 'LOW' ? t.text : (n.status === 'LOW' ? n.text : l.text));
      return { statusText: '养护关注', advice: adviceText, color: '#E65100', badgeBg: 'rgba(230,81,0,0.1)' };
    }
    return { statusText: '健康状态良好', advice: '当前各项指标均处于黄金舒适区间，植物茁壮成长中。', color: '#224A32', badgeBg: 'rgba(34,74,50,0.1)' };
  }
}
