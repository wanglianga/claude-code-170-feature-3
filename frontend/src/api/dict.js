// 平台字典
export const SOURCE_LABEL = {
  SMART_BUTTON: '智能按钮',
  PHONE: '电话',
  MINI_APP: '社区小程序'
}

export const TRIAGE_LABEL = {
  LIFE_ASSIST: '生活协助',
  BODY_DISCOMFORT: '身体不适',
  FALL_RISK: '跌倒风险',
  EMOTIONAL_CRISIS: '情绪危机',
  FALSE_TRIGGER: '误触'
}

export const SEVERITY_LABEL = {
  LOW: '低', MEDIUM: '中', HIGH: '高', CRITICAL: '紧急'
}

export const SEVERITY_TAG = {
  LOW: 'info', MEDIUM: 'warning', HIGH: 'danger', CRITICAL: 'danger'
}

export const TARGET_LABEL = {
  GRID_WORKER: '网格员',
  NEIGHBOR_VOLUNTEER: '邻里志愿者',
  FAMILY_DOCTOR: '家庭医生',
  EMERGENCY_120: '120急救'
}

export const SERVICE_LABEL = {
  DAILY_VISIT: '日常巡访',
  MEAL_SERVICE: '助餐',
  AGING_RETROFIT: '适老化改造',
  PSYCH_COMPANION: '心理陪伴',
  TEMP_CARE: '临时照护',
  HOSPITAL_ESCORT: '送医陪诊',
  OTHER: '其他服务'
}

export const FOLLOW_RESULT_LABEL = {
  IMPROVED: '好转',
  STABLE: '平稳',
  WORSENED: '恶化',
  NO_ANSWER: '未联系上'
}

export const RISK_LABEL = {
  CANNOT_OPEN_DOOR: '无法开门',
  LOCATION_OFFSET: '定位偏移',
  FAMILY_UNREACHABLE: '家属长期不接电话',
  NEIGHBOR_INFO_UNCLEAR: '邻居代报信息不清',
  REFUSE_HOSPITAL: '老人拒绝就医',
  HOME_SAFETY_HAZARD: '家中存在安全隐患',
  FREQUENT_NIGHT_HELP: '频繁夜间求助'
}

export const RISK_TAG = {
  CANNOT_OPEN_DOOR: 'danger',
  LOCATION_OFFSET: 'warning',
  FAMILY_UNREACHABLE: 'danger',
  NEIGHBOR_INFO_UNCLEAR: 'warning',
  REFUSE_HOSPITAL: 'danger',
  HOME_SAFETY_HAZARD: 'danger',
  FREQUENT_NIGHT_HELP: 'danger'
}

export const STATUS_LABEL = {
  NEW: '待研判',
  TRIAGED: '已研判',
  DISPATCHED: '已转介',
  ONSITE: '已上门',
  SERVICED: '已服务',
  COMMUNICATED: '已沟通家属',
  FOLLOWED_UP: '待回访归档',
  CLOSED: '已归档'
}

export const STATUS_TAG = {
  NEW: 'danger',
  TRIAGED: 'warning',
  DISPATCHED: 'warning',
  ONSITE: 'primary',
  SERVICED: 'primary',
  COMMUNICATED: 'primary',
  FOLLOWED_UP: 'warning',
  CLOSED: 'success'
}

export const RISK_LEVEL_LABEL = {
  LOW: '低风险', MEDIUM: '中风险', HIGH: '高风险', CRITICAL: '极高风险'
}

export const RISK_LEVEL_TAG = {
  LOW: 'success', MEDIUM: 'info', HIGH: 'warning', CRITICAL: 'danger'
}

export const ENTRY_METHODS = ['钥匙开门', '门禁密码', '智能锁授权', '邻居协助开门', '家属到场开门', '破锁入户', '未能入户']

export const ELDER_STATUS_OPTIONS = ['清醒正常', '身体不适', '已跌倒', '意识不清', '情绪激动', '未见到老人']

/* ============ 长期照护 ============ */

export const CARE_ITEM_LABEL = {
  REGULAR_VISIT: '定期探访',
  PSYCH_COMPANION: '心理陪伴',
  MEAL_SERVICE: '助餐',
  FAMILY_DOCTOR: '家庭医生随访',
  MED_REMINDER: '药盒提醒',
  FAMILY_REACH: '家属联系',
  OTHER: '其他'
}

export const CARE_ITEM_TAG = {
  REGULAR_VISIT: 'danger',
  PSYCH_COMPANION: 'success',
  MEAL_SERVICE: 'warning',
  FAMILY_DOCTOR: 'primary',
  MED_REMINDER: 'info',
  FAMILY_REACH: '',
  OTHER: 'info'
}

export const PLAN_STATUS_LABEL = {
  DRAFT: '草案待生效',
  ACTIVE: '生效观察中',
  COMPLETED: '观察期结束',
  TERMINATED: '已终止'
}

export const PLAN_STATUS_TAG = {
  DRAFT: 'info',
  ACTIVE: 'danger',
  COMPLETED: 'success',
  TERMINATED: 'warning'
}

export const CARE_LEVEL_LABEL = {
  LIGHT: '轻度关怀',
  MEDIUM: '重点照护',
  INTENSIVE: '强化照护'
}

export const FAMILY_INVOLVEMENT_LABEL = {
  GOOD: '参与良好',
  AVERAGE: '参与一般',
  POOR: '参与不足'
}

export const FAMILY_INVOLVEMENT_TAG = {
  GOOD: 'success',
  AVERAGE: 'warning',
  POOR: 'danger'
}

export const VERDICT_LABEL = {
  OBSERVING: '观察数据积累中',
  IMPROVING: '改善中',
  NO_CHANGE: '无明显变化',
  WORSENING: '未见改善且恶化'
}

export const VERDICT_TAG = {
  OBSERVING: 'info',
  IMPROVING: 'success',
  NO_CHANGE: 'warning',
  WORSENING: 'danger'
}

export const CONFERENCE_ROLE_LABEL = {
  SOCIAL_WORKER: '社工',
  ELDER_ADVISOR: '养老顾问',
  COMMUNITY_DOCTOR: '社区医生',
  FAMILY: '家属',
  GRID_WORKER: '网格员',
  OTHER: '其他'
}

export const ARCHIVE_TYPE_LABEL = {
  ASSESSMENT: '长期照护评估',
  CARE_PLAN: '照护方案',
  CONFERENCE: '个案会商',
  REVIEW: '效果观察',
  MANUAL: '社工记录'
}

export const ARCHIVE_TYPE_TAG = {
  ASSESSMENT: 'warning',
  CARE_PLAN: 'danger',
  CONFERENCE: 'primary',
  REVIEW: 'success',
  MANUAL: 'info'
}

export const ACTIVITY_RECORD_LABEL = {
  REGISTER: '签到/完成',
  CALL_PHONE: '电话关怀',
  MISSED: '未签到/失约'
}
