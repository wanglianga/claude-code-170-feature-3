<template>
  <div v-loading="loading">
    <el-page-header @back="$router.push('/care-plans')" style="margin-bottom:12px">
      <template #content>
        <b style="font-size:16px">{{ plan.planNo }} {{ plan.planName }}</b>
        <el-tag :type="PLAN_STATUS_TAG[plan.status]" effect="dark" style="margin-left:10px">
          {{ PLAN_STATUS_LABEL[plan.status] }}
        </el-tag>
        <el-link style="margin-left:14px" @click="$router.push(`/elders/${elder.id}`)">
          {{ elder.name }} 老人画像 →
        </el-link>
      </template>
      <template #extra>
        <el-button v-if="plan.status==='DRAFT'" type="danger" @click="activate">方案生效，开始观察</el-button>
        <el-button v-if="plan.status==='ACTIVE'" type="success" plain @click="complete">观察期结束</el-button>
        <el-button v-if="plan.status==='ACTIVE'" type="warning" @click="reviewDialog = true">保存复评</el-button>
      </template>
    </el-page-header>

    <el-row :gutter="12">
      <el-col :span="16">
        <!-- 方案信息与服务项 -->
        <el-card shadow="never" style="margin-bottom:12px">
          <template #header><span class="section-title">方案目标与服务安排</span></template>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="制定社工">{{ plan.createdByName }}</el-descriptions-item>
            <el-descriptions-item label="观察周期">{{ plan.observeDays }} 天
              <span class="muted" v-if="effect">（已观察 {{ effect.daysElapsed }} 天 / 剩余 {{ effect.daysRemaining }} 天）</span>
            </el-descriptions-item>
            <el-descriptions-item label="生效时间">{{ fmt(plan.effectiveAt) || '尚未生效' }}</el-descriptions-item>
            <el-descriptions-item label="周期结束">{{ fmt(plan.observeEndAt) || '—' }}</el-descriptions-item>
            <el-descriptions-item label="方案目标" :span="2">{{ plan.goal || '—' }}</el-descriptions-item>
          </el-descriptions>

          <el-table :data="items" size="small" style="margin-top:10px">
            <el-table-column label="服务项" width="130">
              <template #default="{ row }">
                <el-tag size="small" :type="CARE_ITEM_TAG[row.itemType]">{{ CARE_ITEM_LABEL[row.itemType] }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="frequency" label="频次" width="150" />
            <el-table-column label="责任方">
              <template #default="{ row }">
                {{ row.providerName || '—' }}
                <span class="muted" v-if="row.providerPhone"> {{ row.providerPhone }}</span>
              </template>
            </el-table-column>
            <el-table-column label="已签到" width="90">
              <template #default="{ row }">
                <el-tag size="small" type="success" effect="plain">{{ row.checkInCount || 0 }}{{ row.plannedTimes ? '/' + row.plannedTimes : '' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column v-if="plan.status==='ACTIVE'" label="登记" width="100">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="openActivity(row)">登记</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 效果观察 -->
        <el-card shadow="never" style="margin-bottom:12px">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span class="section-title" style="margin:0">生效后效果观察（夜间求助 / 助餐签到 / 药盒提醒 / 家属接听率）</span>
              <el-button size="small" @click="load">刷新指标</el-button>
            </div>
          </template>

          <el-alert v-if="!plan.effectiveAt" type="info" :closable="false"
                    title="方案尚未生效；生效后平台将自动记录基线并持续观察四项指标变化" style="margin-bottom:10px" />

          <el-row v-if="effect" :gutter="10">
            <el-col :span="6">
              <div class="metric" :class="verdictClass(effect.verdict)">
                <div class="m-num">{{ effect.night.current }}</div>
                <div class="muted">夜间求助（{{ effect.night.unit }}）</div>
                <div class="m-sub">基线 {{ effect.night.baseline ?? '—' }}
                  <el-tag v-if="effect.night.delta!=null" size="small"
                          :type="effect.night.delta < 0 ? 'success' : effect.night.delta > 0 ? 'danger' : 'info'">
                    {{ effect.night.delta > 0 ? '+' : '' }}{{ effect.night.delta }}
                  </el-tag>
                </div>
                <div class="m-sub">周均 {{ effect.night.baselineWeekly }} → {{ effect.night.currentWeekly }}</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="metric">
                <div class="m-num" style="color:#e6a23c">{{ effect.meal.checkIns }}</div>
                <div class="muted">助餐签到（失约 {{ effect.meal.missed }}）</div>
                <div class="m-sub">签到率 {{ effect.meal.attendanceRate == null ? '—' : effect.meal.attendanceRate + '%' }}</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="metric">
                <div class="m-num" style="color:#409eff">{{ effect.medication.taken }}/{{ effect.medication.reminders }}</div>
                <div class="muted">药盒提醒（按时服药）</div>
                <div class="m-sub">服药依从率 {{ effect.medication.adherenceRate == null ? '—' : effect.medication.adherenceRate + '%' }}</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="metric">
                <div class="m-num" :style="{color:(effect.family.reachRate??0) < 40 ? '#f56c6c':'#67c23a'}">
                  {{ effect.family.reachRate == null ? '—' : effect.family.reachRate + '%' }}
                </div>
                <div class="muted">家属接听率（{{ effect.family.reached }}/{{ effect.family.attempts }}）</div>
                <div class="m-sub">基线 {{ effect.family.baselineReachRate == null ? '—' : effect.family.baselineReachRate + '%' }}
                  <el-tag v-if="effect.family.delta!=null" size="small"
                          :type="effect.family.delta > 0 ? 'success' : effect.family.delta < 0 ? 'danger' : 'info'">
                    {{ effect.family.delta > 0 ? '+' : '' }}{{ effect.family.delta }}
                  </el-tag>
                </div>
              </div>
            </el-col>
          </el-row>

          <div ref="trendChart" style="height:300px;margin-top:14px"></div>

          <div v-if="effect" style="margin-top:10px;display:flex;align-items:center;gap:12px">
            <span>综合判定：</span>
            <el-tag :type="VERDICT_TAG[effect.verdict]" effect="dark">{{ effect.verdictLabel }}</el-tag>
            <el-button v-if="effect.suggestConference" type="danger" :icon="Warning" @click="openConference">
              方案无改善，发起个案会商
            </el-button>
            <span v-if="plan.reviewedAfterConference" class="muted">已发起个案会商，协商结论已写回老人档案</span>
          </div>
        </el-card>

        <!-- 个案会商 -->
        <el-card shadow="never" style="margin-bottom:12px">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span class="section-title" style="margin:0">个案会商（{{ conferences.length }}）</span>
              <el-button type="danger" size="small" plain @click="openConference()">发起会商</el-button>
            </div>
          </template>
          <el-empty v-if="!conferences.length" :image-size="50"
                    description="方案观察期无改善时，可召集养老顾问、社区医生、家属会商，结论写回老人档案" />
          <el-timeline v-else>
            <el-timeline-item v-for="c in conferences" :key="c.id" :timestamp="fmt(c.heldAt)" type="primary">
              <b>{{ c.subject }}</b>
              <el-tag size="small" type="success" v-if="c.writtenBackToArchive" style="margin-left:8px">结论已写回档案</el-tag>
              <el-tag size="small" type="warning" v-if="c.planAdjusted">已调整方案</el-tag>
              <div class="muted" v-if="c.situation" style="margin:4px 0">会前情况：{{ c.situation }}</div>
              <div style="margin:4px 0">协商结论：{{ c.conclusion || '会商进行中，结论待回填' }}</div>
              <div v-if="c.adjustmentNote" class="muted">调整内容：{{ c.adjustmentNote }}</div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="never" style="margin-bottom:12px">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span class="section-title" style="margin:0">服务执行记录（{{ activities.length }}）</span>
              <el-button size="small" type="danger" plain v-if="plan.status==='ACTIVE'" @click="openActivity(null)">登记</el-button>
            </div>
          </template>
          <el-timeline class="timeline-dense">
            <el-timeline-item v-for="a in activities.slice().reverse()" :key="a.id"
                              :timestamp="fmt(a.activityAt)" placement="top">
              <el-tag size="small" :type="CARE_ITEM_TAG[a.activityType]">{{ CARE_ITEM_LABEL[a.activityType] }}</el-tag>
              <el-tag size="small" :type="a.recordType==='MISSED' ? 'danger' : 'info'" effect="plain" style="margin-left:4px">
                {{ ACTIVITY_RECORD_LABEL[a.recordType] }}
              </el-tag>
              <div class="muted" style="font-size:12px">
                {{ a.providerName || a.operatorName }}
                <template v-if="a.activityType==='MED_REMINDER'">
                  · {{ a.medicationTaken ? '已按时服药' : '未确认服药' }}
                </template>
                <template v-else-if="a.activityType==='FAMILY_REACH'">
                  · {{ a.familyContactName }} {{ a.familyReached ? '已接通' : '未接通' }}
                </template>
              </div>
              <div v-if="a.content" class="muted" style="font-size:12px">{{ a.content }}</div>
            </el-timeline-item>
          </el-timeline>
        </el-card>

        <el-card shadow="never">
          <template #header><span class="section-title" style="margin:0">评估依据</span></template>
          <el-empty v-if="!assessment" :image-size="50" description="方案未关联评估" />
          <el-descriptions v-else :column="1" size="small" border>
            <el-descriptions-item label="夜间求助">近{{ assessment.periodDays }}天 {{ assessment.nightHelpCount }} 次</el-descriptions-item>
            <el-descriptions-item label="孤独焦虑">
              <el-tag size="small" :type="assessment.lonelyAnxious ? 'success' : 'info'">
                {{ assessment.lonelyAnxious ? '是' : '否' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="药物管理混乱">
              <el-tag size="small" :type="assessment.medicationChaotic ? 'danger' : 'info'">
                {{ assessment.medicationChaotic ? '是' : '否' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="家属参与">
              <el-tag size="small" :type="FAMILY_INVOLVEMENT_TAG[assessment.familyInvolvement]">
                {{ FAMILY_INVOLVEMENT_LABEL[assessment.familyInvolvement] }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="照护强度">{{ CARE_LEVEL_LABEL[assessment.resultLevel] }}</el-descriptions-item>
            <el-descriptions-item label="评估结论">{{ assessment.conclusion || '—' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <!-- 服务签到登记 -->
    <el-dialog v-model="actDialog" :title="`登记${CARE_ITEM_LABEL[form.activityType] || '服务'}`" width="520px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="服务类型">
          <el-select v-model="form.activityType" style="width:100%">
            <el-option v-for="(v,k) in ACT_OPTIONS" :key="k" :label="v" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="登记类型">
          <el-radio-group v-model="form.recordType">
            <el-radio value="REGISTER">签到/已完成</el-radio>
            <el-radio value="CALL_PHONE">电话关怀</el-radio>
            <el-radio value="MISSED">未签到/失约</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="提供/执行方"><el-input v-model="form.providerName" /></el-form-item>
        <el-form-item v-if="form.activityType==='MED_REMINDER'" label="是否按时服药">
          <el-switch v-model="form.medicationTaken" active-text="已服药" inactive-text="未服药" />
        </el-form-item>
        <el-form-item v-if="form.activityType==='FAMILY_REACH'" label="家属联系人"><el-input v-model="form.familyContactName" /></el-form-item>
        <el-form-item v-if="form.activityType==='FAMILY_REACH'" label="是否接通">
          <el-switch v-model="form.familyReached" active-text="已接通" inactive-text="未接通" />
        </el-form-item>
        <el-form-item label="老人状态/情绪"><el-input v-model="form.elderMood" placeholder="平稳 / 焦虑 / 情绪低落" /></el-form-item>
        <el-form-item label="服务内容"><el-input v-model="form.content" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.note" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="服务时间">
          <el-date-picker v-model="form.activityAt" type="datetime" placeholder="留空为当前时间"
                          value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="actDialog = false">取消</el-button>
        <el-button type="danger" @click="submitActivity">保存登记</el-button>
      </template>
    </el-dialog>

    <!-- 个案会商 -->
    <el-dialog v-model="confDialog" title="发起个案会商（养老顾问 / 社区医生 / 家属）" width="720px" top="4vh">
      <el-form :model="confForm" label-width="92px">
        <el-form-item label="会商主题"><el-input v-model="confForm.subject" placeholder="如：郑秀珍夜间求助无改善个案会商" /></el-form-item>
        <el-form-item label="会商地点"><el-input v-model="confForm.location" placeholder="社区居委会会议室 / 线上" /></el-form-item>
        <el-form-item label="会前情况">
          <el-input v-model="confForm.situation" type="textarea" :rows="3" :placeholder="defaultSituation" />
        </el-form-item>

        <el-divider content-position="left">参与方</el-divider>
        <div v-for="(p, idx) in confForm.participants" :key="idx" class="part-row">
          <el-select v-model="p.role" style="width:120px">
            <el-option v-for="(v,k) in CONFERENCE_ROLE_OPTIONS" :key="k" :label="v" :value="k" />
          </el-select>
          <el-input v-model="p.name" placeholder="姓名" style="width:110px;margin:0 8px" />
          <el-input v-model="p.phone" placeholder="电话" style="width:130px;margin-right:8px" />
          <el-checkbox v-model="p.attended">到场</el-checkbox>
          <el-input v-model="p.opinion" placeholder="个人意见" style="flex:1;margin-left:8px" />
          <el-button link type="danger" @click="confForm.participants.splice(idx,1)">删除</el-button>
        </div>
        <el-button size="small" @click="addParticipant" style="margin:8px 0">+ 添加参与方</el-button>

        <el-form-item label="协商结论" style="margin-top:8px">
          <el-input v-model="confForm.conclusion" type="textarea" :rows="3"
                    placeholder="养老顾问、社区医生、家属协商一致的结论（提交后自动写回老人档案）" />
        </el-form-item>
        <el-form-item label="调整方案">
          <el-switch v-model="confForm.planAdjusted" />
        </el-form-item>
        <el-form-item v-if="confForm.planAdjusted" label="调整内容">
          <el-input v-model="confForm.adjustmentNote" type="textarea" :rows="2"
                    placeholder="如：探访加密为每日1次、家属每周固定来电2次、调整助餐与家庭医生随访频次" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="confDialog = false">取消</el-button>
        <el-button type="danger" @click="submitConference">提交并写回老人档案</el-button>
      </template>
    </el-dialog>

    <!-- 复评 -->
    <el-dialog v-model="reviewDialog" title="方案效果复评" width="460px">
      <el-form label-width="92px">
        <el-form-item label="判定结果">
          <el-radio-group v-model="reviewForm.result">
            <el-radio v-for="(v,k) in VERDICT_LABEL" :key="k" :value="k">{{ v }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="复评说明"><el-input v-model="reviewForm.note" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialog = false">取消</el-button>
        <el-button type="warning" @click="submitReview">保存复评</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import { Warning } from '@element-plus/icons-vue'
import api from '../api'
import {
  CARE_ITEM_LABEL, CARE_ITEM_TAG, PLAN_STATUS_LABEL, PLAN_STATUS_TAG,
  VERDICT_LABEL, VERDICT_TAG, ACTIVITY_RECORD_LABEL, CARE_LEVEL_LABEL,
  FAMILY_INVOLVEMENT_LABEL, FAMILY_INVOLVEMENT_TAG, CONFERENCE_ROLE_LABEL
} from '../api/dict'

const route = useRoute()
const planId = route.params.id
const loading = ref(false)
const plan = ref({})
const elder = computed(() => detail.value.elder || {})
const assessment = computed(() => detail.value.assessment || null)
const items = computed(() => detail.value.items || [])
const activities = computed(() => detail.value.activities || [])
const conferences = computed(() => detail.value.conferences || [])
const effect = computed(() => detail.value.effect || null)
const detail = ref({})
const trendChart = ref()
let chart = null

const ACT_OPTIONS = {
  REGULAR_VISIT: '定期探访',
  PSYCH_COMPANION: '心理陪伴',
  MEAL_SERVICE: '助餐签到',
  FAMILY_DOCTOR: '家庭医生随访',
  MED_REMINDER: '药盒提醒',
  FAMILY_REACH: '家属联系'
}
const CONFERENCE_ROLE_OPTIONS = CONFERENCE_ROLE_LABEL

async function load() {
  loading.value = true
  try {
    detail.value = await api.get(`/care-plans/${planId}`)
    plan.value = detail.value.plan
    await nextTick()
    renderChart()
  } finally {
    loading.value = false
  }
}

function renderChart() {
  const e = effect.value
  if (!e || !trendChart.value) return
  chart = chart || echarts.init(trendChart.value)
  const labels = e.weekLabels
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['夜间求助', '助餐签到', '按时服药'], top: 0 },
    grid: { left: 40, right: 20, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: labels },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      { name: '夜间求助', type: 'line', smooth: true, itemStyle: { color: '#f56c6c' },
        areaStyle: { color: 'rgba(245,108,108,0.12)' }, data: e.nightWeekly },
      { name: '助餐签到', type: 'bar', itemStyle: { color: '#e6a23c' }, data: e.meal.weekly },
      { name: '按时服药', type: 'bar', itemStyle: { color: '#409eff' }, data: e.medication.weekly }
    ]
  }, true)
}

function verdictClass(v) {
  return v === 'IMPROVING' ? 'good' : v === 'WORSENING' ? 'bad' : ''
}

async function activate() {
  await ElMessageBox.confirm('方案生效后将记录当前夜间求助与家属接听率基线，并开始持续观察，确认生效？',
    '方案生效', { type: 'warning' })
  await api.post(`/care-plans/${planId}/activate`)
  ElMessage.success('方案已生效')
  load()
}
async function complete() {
  await ElMessageBox.confirm('确认结束观察周期？', '确认', { type: 'warning' })
  await api.post(`/care-plans/${planId}/complete`)
  ElMessage.success('观察期已结束')
  load()
}

/* ---- 签到 ---- */
const actDialog = ref(false)
const form = reactive({})
function openActivity(item) {
  Object.assign(form, {
    itemId: item?.id || null,
    activityType: item?.itemType || 'REGULAR_VISIT',
    recordType: 'REGISTER', providerName: item?.providerName || '',
    medicationTaken: true, familyReached: true, familyContactName: '',
    elderMood: '', content: '', note: '', activityAt: ''
  })
  actDialog.value = true
}
async function submitActivity() {
  await api.post(`/care-plans/${planId}/activities`, form)
  ElMessage.success('执行记录已登记，观察指标已更新')
  actDialog.value = false
  load()
}

/* ---- 会商 ---- */
const confDialog = ref(false)
const confForm = reactive({ participants: [] })
const defaultSituation = computed(() => {
  const e = effect.value
  if (!e) return ''
  return `方案生效后夜间求助基线 ${e.night.baseline ?? '—'}，当前 ${e.night.current}；` +
    `助餐签到 ${e.meal.checkIns} 次；药盒提醒按时服药 ${e.medication.taken}/${e.medication.reminders}；` +
    `家属接听率 ${e.family.reachRate == null ? '—' : e.family.reachRate + '%'}。综合判定：${e.verdictLabel}。`
})
function addParticipant(role, name, phone) {
  confForm.participants.push({ role, name: name || '', phone: phone || '', attended: true, opinion: '' })
}
function openConference() {
  Object.assign(confForm, {
    subject: `${elder.value.name || ''}长期照护方案个案会商`,
    location: '幸福社区居委会会议室',
    situation: defaultSituation.value, conclusion: '', planAdjusted: false, adjustmentNote: '',
    participants: []
  })
  addParticipant('SOCIAL_WORKER', plan.value.createdByName || '李社工')
  addParticipant('ELDER_ADVISOR', '周顾问')
  addParticipant('COMMUNITY_DOCTOR', '钱医生')
  addParticipant('FAMILY', '')
  confDialog.value = true
}
async function submitConference() {
  if (!confForm.conclusion) { ElMessage.warning('请填写协商结论（将写回老人档案）'); return }
  await api.post('/conferences', { ...confForm, planId: Number(planId), elderId: elder.value.id })
  ElMessage.success('会商结论已写回老人档案')
  confDialog.value = false
  load()
}

/* ---- 复评 ---- */
const reviewDialog = ref(false)
const reviewForm = reactive({ result: 'NO_CHANGE', note: '' })
async function submitReview() {
  await api.post(`/care-plans/${planId}/review`, reviewForm)
  ElMessage.success('复评已保存并写入档案')
  reviewDialog.value = false
  load()
}

function fmt(s) { return s ? String(s).replace('T', ' ').substring(0, 16) : '' }
onMounted(load)
</script>

<style scoped>
.metric { background:#fafafa; border:1px solid #ebeef5; border-radius:8px; padding:12px; text-align:center; min-height:118px; }
.metric.good { border-color:#b3e19d; background:#f0f9eb; }
.metric.bad { border-color:#fbc4c4; background:#fef0f0; }
.m-num { font-size:26px; font-weight:700; color:#303133; line-height:1.2; }
.m-sub { font-size:12px; color:#909399; margin-top:3px; }
.part-row { display:flex; align-items:center; margin-bottom:8px; }
.timeline-dense :deep(.el-timeline-item__node) { display:none; }
.timeline-dense :deep(.el-timeline-item__tail) { border-left-style: dashed; }
</style>
