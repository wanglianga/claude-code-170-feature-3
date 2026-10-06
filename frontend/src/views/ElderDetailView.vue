<template>
  <div v-loading="loading">
    <el-page-header @back="$router.push('/elders')" style="margin-bottom:12px">
      <template #content>
        <b style="font-size:16px">{{ data.elder?.name }}</b>
        <el-tag :type="RISK_LEVEL_TAG[data.elder?.riskLevel]" effect="dark" style="margin-left:10px">
          {{ RISK_LEVEL_LABEL[data.elder?.riskLevel] }}
        </el-tag>
        <el-tag v-if="night.weekCount >= 2" type="danger" effect="dark" style="margin-left:8px">
          一周内夜间求助 {{ night.weekCount }} 次
        </el-tag>
      </template>
    </el-page-header>

    <el-row :gutter="12">
      <el-col :span="15">
        <el-card shadow="never" style="margin-bottom:12px">
          <template #header><span class="section-title">照护画像</span></template>
          <el-descriptions v-if="data.elder" :column="2" border size="small">
            <el-descriptions-item label="性别年龄">{{ data.elder.gender }} · {{ data.elder.age }}岁</el-descriptions-item>
            <el-descriptions-item label="电话">{{ data.elder.phone }}</el-descriptions-item>
            <el-descriptions-item label="居住状态">{{ data.elder.livingStatus }}</el-descriptions-item>
            <el-descriptions-item label="长期独居">
              <el-tag size="small" :type="data.elder.livingAloneLongTerm ? 'danger' : 'info'">
                {{ data.elder.livingAloneLongTerm ? '是' : '否' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="住址" :span="2">{{ data.elder.address }}</el-descriptions-item>
            <el-descriptions-item label="基础病" :span="2">{{ data.elder.chronicDiseases || '无' }}</el-descriptions-item>
            <el-descriptions-item label="病史用药" :span="2">{{ data.elder.medicalNote || '—' }}</el-descriptions-item>
            <el-descriptions-item label="门禁信息" :span="2">
              <span style="color:#b88230">{{ data.elder.doorAccessInfo || '无' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="安全设施" :span="2">
              燃气报警器
              <el-icon v-if="data.elder.gasAlarmInstalled" color="#67c23a"><CircleCheck /></el-icon>
              <el-icon v-else color="#f56c6c"><CircleClose /></el-icon>
              ｜独立烟感
              <el-icon v-if="data.elder.smokeAlarmInstalled" color="#67c23a"><CircleCheck /></el-icon>
              <el-icon v-else color="#f56c6c"><CircleClose /></el-icon>
              ｜SOS设备
              <el-icon v-if="data.elder.sosDeviceInstalled" color="#67c23a"><CircleCheck /></el-icon>
              <el-icon v-else color="#f56c6c"><CircleClose /></el-icon>
            </el-descriptions-item>
            <el-descriptions-item label="最近家属探访">{{ data.elder?.lastFamilyVisitDate || '无记录' }}</el-descriptions-item>
            <el-descriptions-item label="最近医生随访">{{ data.elder?.lastDoctorFollowUpDate || '无记录' }}</el-descriptions-item>
            <el-descriptions-item label="随访结论" :span="2">{{ data.elder?.doctorFollowUpConclusion || '—' }}</el-descriptions-item>
            <el-descriptions-item label="家属响应评分">
              <el-rate :model-value="Math.round((data.elder?.familyResponsivenessScore||0)/20)" disabled />
              {{ data.elder?.familyResponsivenessScore }}
              <div class="muted">{{ data.elder?.familyResponsivenessNote }}</div>
            </el-descriptions-item>
            <el-descriptions-item label="邻里协助可信度">
              <el-rate :model-value="Math.round((data.elder?.neighborReliabilityScore||0)/20)" disabled />
              {{ data.elder?.neighborReliabilityScore }}
            </el-descriptions-item>
            <el-descriptions-item label="过往误触记录">
              {{ data.elder?.falseTriggerCount }} 次
              <div class="muted">{{ data.elder?.falseTriggerNote }}</div>
            </el-descriptions-item>
            <el-descriptions-item label="服务建议">
              <el-space wrap>
                <el-tag v-if="data.elder?.suggestDailyVisit" type="danger" effect="plain">增加日常巡访</el-tag>
                <el-tag v-if="data.elder?.suggestMealService" type="warning" effect="plain">助餐服务</el-tag>
                <el-tag v-if="data.elder?.suggestAgingRetrofit" type="info" effect="plain">适老化改造</el-tag>
                <el-tag v-if="data.elder?.suggestPsychCompanion" type="success" effect="plain">心理陪伴</el-tag>
              </el-space>
            </el-descriptions-item>
            <el-descriptions-item label="下一次主动关怀" :span="2">
              <el-tag type="warning">{{ data.elder?.proactiveCareType || '未排期' }}</el-tag>
              {{ data.elder?.nextProactiveCareAt ? fmt(data.elder.nextProactiveCareAt) : '' }}
              <div class="muted">{{ data.elder?.proactiveCareNote }}</div>
            </el-descriptions-item>
            <el-descriptions-item label="最近个案会商" :span="2">
              <span v-if="data.elder?.lastCaseConferenceDate">
                <el-tag type="success" effect="dark" size="small">{{ data.elder.lastCaseConferenceDate }}</el-tag>
                <div class="muted" style="white-space:pre-line;margin-top:2px">{{ data.elder.caseConferenceNote }}</div>
              </span>
              <span v-else class="muted">无</span>
            </el-descriptions-item>
            <el-descriptions-item v-if="data.elder?.longTermCareNote" label="长期照护跟踪" :span="2">
              <span class="muted" style="white-space:pre-line">{{ data.elder.longTermCareNote }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">{{ data.elder?.remark || '—' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 频繁夜间求助分析 -->
        <el-card shadow="never" style="margin-bottom:12px">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span class="section-title" style="margin:0">频繁夜间求助分析（近30天）</span>
              <el-button type="danger" plain size="small" @click="openAssessment">发起长期照护评估</el-button>
            </div>
          </template>
          <el-row :gutter="10" style="margin-bottom:10px">
            <el-col :span="6"><div class="mini-stat"><div class="num" style="color:#722ed1">{{ night.totalNightHelps ?? 0 }}</div><div class="muted">夜间求助（30天）</div></div></el-col>
            <el-col :span="6"><div class="mini-stat"><div class="num" style="color:#f56c6c">{{ night.weekCount ?? 0 }}</div><div class="muted">一周内夜间求助</div></div></el-col>
            <el-col :span="6"><div class="mini-stat"><div class="num" style="color:#e6a23c">{{ night.lonelyAnxiousCount ?? 0 }}</div><div class="muted">现场确认孤独焦虑</div></div></el-col>
            <el-col :span="6"><div class="mini-stat"><div class="num" style="color:#fa8c16">{{ night.medicationChaosCount ?? 0 }}</div><div class="muted">药物管理混乱</div></div></el-col>
          </el-row>
          <el-empty v-if="!night.events?.length" :image-size="60" description="近30天暂无夜间求助" />
          <el-table v-else :data="night.events" size="small" style="cursor:pointer"
                    @row-click="r => $router.push(`/events/${r.eventId}`)">
            <el-table-column label="时间" width="145">
              <template #default="{ row }">{{ fmt(row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="来源/语音（求助原因）" min-width="200">
              <template #default="{ row }">
                <el-tag size="small" effect="plain">{{ SOURCE_LABEL[row.source] }}</el-tag>
                <div class="muted">{{ row.voiceContent || '（无语音）' }}</div>
                <el-tag v-for="r in reasonList(row)" :key="r" size="small" type="warning" effect="plain" style="margin:1px">{{ r }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="到场结果" width="110">
              <template #default="{ row }">
                <el-tag v-if="arriveResult(row)" size="small" type="success">{{ ARRIVE_RESULT_LABEL[arriveResult(row)] }}</el-tag>
                <span v-else class="muted">未上门</span>
              </template>
            </el-table-column>
            <el-table-column label="孤独焦虑" width="80">
              <template #default="{ row }">
                <el-tag v-if="row.lonelyAnxious" size="small" type="danger">是</el-tag>
                <span v-else class="muted">否</span>
              </template>
            </el-table-column>
            <el-table-column label="药物混乱" width="80">
              <template #default="{ row }">
                <el-tag v-if="row.medicationChaos" size="small" type="danger">是</el-tag>
                <span v-else class="muted">否</span>
              </template>
            </el-table-column>
            <el-table-column label="家属参与/接听" width="120">
              <template #default="{ row }">
                <el-tag v-if="row.familyInvolved" size="small" type="success">已参与</el-tag>
                <div class="muted">
                  接通 {{ row.familyCommunication?.reached ?? 0 }}/{{ row.familyCommunication?.attempts ?? 0 }}
                  <span v-if="row.familyCommunication?.answerRate != null">（{{ row.familyCommunication.answerRate }}%）</span>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 长期照护方案 -->
        <el-card shadow="never" style="margin-bottom:12px">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span class="section-title" style="margin:0">长期照护方案（{{ plans.length }}）</span>
              <el-button type="danger" size="small" @click="openAssessment">发起长期照护评估</el-button>
            </div>
          </template>
          <el-empty v-if="!plans.length" :image-size="60" description="尚未发起长期照护评估" />
          <div v-for="p in plans" :key="p.id" class="plan-row" @click="$router.push(`/care-plans/${p.id}`)">
            <div>
              <b>{{ p.planNo }}</b>
              <el-tag size="small" :type="PLAN_STATUS_TAG[p.status]" effect="dark" style="margin-left:8px">
                {{ PLAN_STATUS_LABEL[p.status] }}
              </el-tag>
              <div class="muted">{{ p.triggerReason }}</div>
              <div class="muted">发起：{{ p.initiatorName }} · {{ fmt(p.initiatedAt) }}
                <template v-if="p.effectiveAt"> · 生效：{{ fmt(p.effectiveAt) }}</template>
              </div>
            </div>
            <el-button link type="primary">查看方案与效果观察 →</el-button>
          </div>
        </el-card>

        <el-card shadow="never">
          <template #header><span class="section-title">历史求助事件（{{ events.length }}）</span></template>
          <el-table :data="events" size="small" @row-click="r => $router.push(`/events/${r.id}`)" style="cursor:pointer">
            <el-table-column prop="eventNo" label="编号" width="125" />
            <el-table-column prop="createdAt" label="时间" width="150">
              <template #default="{ row }">{{ fmt(row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="研判" width="100">
              <template #default="{ row }">{{ row.triageType ? TRIAGE_LABEL[row.triageType] : '—' }}</template>
            </el-table-column>
            <el-table-column label="严重度" width="80">
              <template #default="{ row }">
                <el-tag v-if="row.severity" size="small" :type="SEVERITY_TAG[row.severity]">{{ SEVERITY_LABEL[row.severity] }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="风险标记" min-width="180">
              <template #default="{ row }">
                <el-tag v-for="f in (row.riskFlags||'').split(',').filter(Boolean)" :key="f" size="small"
                        :type="RISK_TAG[f]" style="margin:1px">{{ RISK_LABEL[f] }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="回访" width="80">
              <template #default="{ row }">{{ row.followUpResult ? FOLLOW_RESULT_LABEL[row.followUpResult] : '—' }}</template>
            </el-table-column>
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag size="small" :type="STATUS_TAG[row.status]">{{ STATUS_LABEL[row.status] }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="9">
        <el-card shadow="never" style="margin-bottom:12px">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span class="section-title" style="margin:0">紧急联系人</span>
              <el-button size="small" type="danger" plain @click="openContact()">添加</el-button>
            </div>
          </template>
          <div v-for="c in data.contacts" :key="c.id" class="person">
            <div>
              <el-tag size="small" :type="c.primaryContact ? 'danger' : 'info'">{{ c.relationship }}</el-tag>
              <b style="margin:0 6px">{{ c.name }}</b>
              <span class="muted">{{ c.phone }}</span>
              <el-tag v-if="c.oftenUnreachable" size="small" type="danger" effect="dark" style="margin-left:6px">长期不接</el-tag>
              <div class="muted">联系 {{ c.contactAttempts }} 次 / 接通 {{ c.contactReached }} 次 {{ c.reachabilityNote }}</div>
            </div>
            <el-button link type="danger" size="small" @click="delContact(c.id)">删除</el-button>
          </div>
        </el-card>

        <el-card shadow="never">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span class="section-title" style="margin:0">邻里响应人</span>
              <el-button size="small" type="danger" plain @click="openNeighbor()">添加</el-button>
            </div>
          </template>
          <div v-for="n in data.neighbors" :key="n.id" class="person">
            <div>
              <b>{{ n.name }}</b>
              <span class="muted" style="margin-left:6px">{{ n.phone }} · {{ n.proximity }}</span>
              <div>
                <el-rate :model-value="Math.round(n.reliabilityScore/20)" disabled size="small" />
                <span class="muted">响应 {{ n.effectiveCount }}/{{ n.callCount }} 次</span>
                <span class="muted" v-if="n.note"> · {{ n.note }}</span>
              </div>
            </div>
            <el-button link type="danger" size="small" @click="delNeighbor(n.id)">删除</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 发起长期照护评估 -->
    <el-dialog v-model="assessDialog" title="发起长期照护评估" width="720px" top="4vh">
      <el-alert type="warning" :closable="false" style="margin-bottom:12px"
                :title="`近30天夜间求助 ${night.totalNightHelps ?? 0} 次，其中一周内 ${night.weekCount ?? 0} 次。评估后可安排定期探访、心理陪伴、助餐与家庭医生随访，方案生效进入持续观察。`" />
      <el-form :model="assess" label-width="110px">
        <el-form-item label="触发原因">
          <el-input v-model="assess.triggerReason" type="textarea" :rows="2" />
        </el-form-item>
        <el-row>
          <el-col :span="12"><el-form-item label="孤独焦虑(0-10)"><el-input-number v-model="assess.lonelinessScore" :min="0" :max="10" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="用药管理(0-10)"><el-input-number v-model="assess.medicationScore" :min="0" :max="10" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="家属支持(0-10)"><el-input-number v-model="assess.familySupportScore" :min="0" :max="10" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="自理能力(0-10)"><el-input-number v-model="assess.selfCareScore" :min="0" :max="10" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="评估结论">
          <el-input v-model="assess.assessmentConclusion" type="textarea" :rows="2"
                    placeholder="如：夜间孤独焦虑明显，用药自理困难，家属支持薄弱，需长期照护介入" />
        </el-form-item>
        <el-form-item label="方案目标">
          <el-input v-model="assess.goals" type="textarea" :rows="2"
                    placeholder="如：30天内夜间求助下降，助餐全覆盖，药盒提醒确认率90%以上，家属每周至少接听/回访1次" />
        </el-form-item>
        <el-form-item label="观察期(天)">
          <el-input-number v-model="assess.observeDays" :min="7" :max="180" :step="7" />
        </el-form-item>
        <el-form-item label="长期服务安排">
          <div style="width:100%">
            <div v-for="(it, i) in assess.items" :key="i" class="item-line">
              <el-select v-model="it.serviceType" style="width:150px" placeholder="服务类型">
                <el-option v-for="(v, k) in PLAN_SERVICE_LABEL" :key="k" :label="v" :value="k" />
              </el-select>
              <el-input v-model="it.providerName" placeholder="负责人/提供方" style="width:150px" />
              <el-input v-model="it.frequency" placeholder="频次，如每周2次/每日午餐" style="width:170px" />
              <el-button link type="danger" @click="assess.items.splice(i, 1)">删除</el-button>
            </div>
            <el-button size="small" @click="assess.items.push({ serviceType: 'REGULAR_VISIT', providerName: '', frequency: '', content: '' })">+ 添加服务安排</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assessDialog = false">取消</el-button>
        <el-button @click="submitAssessment(false)">保存评估（稍后生效）</el-button>
        <el-button type="danger" @click="submitAssessment(true)">提交并立即生效</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="contactDialog" title="添加紧急联系人" width="440px">
      <el-form :model="contactForm" label-width="80px">
        <el-form-item label="姓名"><el-input v-model="contactForm.name" /></el-form-item>
        <el-form-item label="关系"><el-input v-model="contactForm.relationship" placeholder="儿子/女儿" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="contactForm.phone" /></el-form-item>
        <el-form-item label="主要联系人"><el-switch v-model="contactForm.primaryContact" /></el-form-item>
        <el-form-item label="长期不接"><el-switch v-model="contactForm.oftenUnreachable" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="contactDialog = false">取消</el-button>
        <el-button type="danger" @click="saveContact">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="neighborDialog" title="添加邻里响应人" width="440px">
      <el-form :model="neighborForm" label-width="80px">
        <el-form-item label="姓名"><el-input v-model="neighborForm.name" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="neighborForm.phone" /></el-form-item>
        <el-form-item label="位置"><el-input v-model="neighborForm.proximity" placeholder="如 对门502" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="neighborDialog = false">取消</el-button>
        <el-button type="danger" @click="saveNeighbor">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CircleCheck, CircleClose } from '@element-plus/icons-vue'
import api from '../api'
import {
  RISK_LEVEL_LABEL, RISK_LEVEL_TAG, TRIAGE_LABEL, SEVERITY_LABEL, SEVERITY_TAG,
  RISK_LABEL, RISK_TAG, STATUS_LABEL, STATUS_TAG, FOLLOW_RESULT_LABEL,
  SOURCE_LABEL, ARRIVE_RESULT_LABEL, PLAN_STATUS_LABEL, PLAN_STATUS_TAG
} from '../api/dict'

const route = useRoute()
const id = route.params.id
const data = ref({})
const events = ref([])
const night = ref({})
const plans = ref([])
const loading = ref(false)
const contactDialog = ref(false)
const neighborDialog = ref(false)
const contactForm = reactive({})
const neighborForm = reactive({})
const assessDialog = ref(false)
const assess = reactive({ items: [] })

async function load() {
  loading.value = true
  try {
    const [d, ev, n, ps] = await Promise.all([
      api.get(`/elders/${id}`),
      api.get('/events', { params: { elderId: id } }),
      api.get(`/elders/${id}/night-help-summary`, { params: { days: 30 } }),
      api.get('/care-plans', { params: { elderId: id } })
    ])
    data.value = d
    events.value = ev
    night.value = n
    plans.value = ps
  } finally {
    loading.value = false
  }
}

function reasonList(row) {
  const reasons = []
  ;(row.helpReason || '').split(',').filter(Boolean).forEach(r => reasons.push(r))
  row.visits?.forEach(v => (v.helpReason || '').split(',').filter(Boolean).forEach(r => {
    if (!reasons.includes(r)) reasons.push(r)
  }))
  return reasons
}
function arriveResult(row) {
  return row.visits?.map(v => v.arriveResult).filter(Boolean).slice(-1)[0]
}

function openAssessment() {
  Object.assign(assess, {
    triggerReason: `近30天夜间求助 ${night.value.totalNightHelps ?? 0} 次，一周内 ${night.value.weekCount ?? 0} 次，社工发起长期照护评估`,
    lonelinessScore: night.value.lonelyAnxiousCount ? 8 : 5,
    medicationScore: night.value.medicationChaosCount ? 3 : 7,
    familySupportScore: Math.min(10, Math.round((data.value.elder?.familyResponsivenessScore ?? 60) / 10)),
    selfCareScore: 6, assessmentConclusion: '', goals: '', observeDays: 30,
    items: [
      { serviceType: 'REGULAR_VISIT', providerName: '赵网格员', frequency: '每周2次', content: '夜间求助后加密探访' },
      { serviceType: 'PSYCH_COMPANION', providerName: '心理陪伴志愿者', frequency: '每周1次', content: '缓解夜间孤独焦虑' }
    ]
  })
  assessDialog.value = true
}
async function submitAssessment(activateNow) {
  const payload = { ...assess, elderId: Number(id) }
  const plan = await api.post('/care-plans/assessments', payload)
  if (activateNow) {
    await api.post(`/care-plans/${plan.id}/activate`)
    ElMessage.success('长期照护方案已生效，进入观察期')
  } else {
    ElMessage.success('评估已保存，方案待生效')
  }
  assessDialog.value = false
  if (activateNow) {
    window.location.href = `/care-plans/${plan.id}`
  } else {
    load()
  }
}

function openContact() {
  Object.assign(contactForm, { name: '', relationship: '', phone: '', primaryContact: true, oftenUnreachable: false })
  contactDialog.value = true
}
async function saveContact() {
  await api.post(`/elders/${id}/contacts`, contactForm)
  ElMessage.success('已添加')
  contactDialog.value = false
  load()
}
async function delContact(cid) {
  await ElMessageBox.confirm('删除该联系人？', '确认', { type: 'warning' })
  await api.delete(`/elders/contacts/${cid}`)
  load()
}

function openNeighbor() {
  Object.assign(neighborForm, { name: '', phone: '', proximity: '' })
  neighborDialog.value = true
}
async function saveNeighbor() {
  await api.post(`/elders/${id}/neighbors`, neighborForm)
  ElMessage.success('已添加')
  neighborDialog.value = false
  load()
}
async function delNeighbor(nid) {
  await ElMessageBox.confirm('删除该邻里响应人？', '确认', { type: 'warning' })
  await api.delete(`/elders/neighbors/${nid}`)
  load()
}

function fmt(s) { return s ? String(s).replace('T', ' ').substring(0, 16) : '' }
onMounted(load)
</script>

<style scoped>
.person { display: flex; justify-content: space-between; align-items: center; padding: 8px 0; border-bottom: 1px dashed #ebeef5; }
.plan-row { display: flex; justify-content: space-between; align-items: center; padding: 10px 4px; border-bottom: 1px dashed #ebeef5; cursor: pointer; }
.plan-row:hover { background: #faf6f6; }
.mini-stat { background:#fafafa; border-radius:8px; padding:10px; text-align:center; }
.mini-stat .num { font-size:24px; font-weight:700; }
.item-line { display:flex; gap:8px; margin-bottom:8px; align-items:center; }
</style>
