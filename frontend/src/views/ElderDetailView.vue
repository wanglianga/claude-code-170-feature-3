<template>
  <div v-loading="loading">
    <el-page-header @back="$router.push('/elders')" style="margin-bottom:12px">
      <template #content>
        <b style="font-size:16px">{{ data.elder?.name }}</b>
        <el-tag :type="RISK_LEVEL_TAG[data.elder?.riskLevel]" effect="dark" style="margin-left:10px">
          {{ RISK_LEVEL_LABEL[data.elder?.riskLevel] }}
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
            <el-descriptions-item label="备注" :span="2">{{ data.elder?.remark || '—' }}</el-descriptions-item>
          </el-descriptions>
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

        <!-- 夜间求助观察（频繁求助照护方案调整入口） -->
        <el-card shadow="never" style="margin-bottom:12px">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span class="section-title" style="margin:0">
                夜间求助观察（近 {{ night.periodDays || 7 }} 天 · {{ night.totalNightCount || 0 }} 次）
              </span>
              <div>
                <el-radio-group v-model="nightDays" size="small" @change="loadNight" style="margin-right:10px">
                  <el-radio-button :value="7">近7天</el-radio-button>
                  <el-radio-button :value="14">近14天</el-radio-button>
                  <el-radio-button :value="30">近30天</el-radio-button>
                </el-radio-group>
                <el-button type="danger" size="small" :icon="MagicStick" @click="openAssessment"
                           :disabled="!(night.suggestAssessment)">发起长期照护评估</el-button>
                <el-button type="warning" size="small" plain @click="$router.push(`/care-plans?elderId=${id}`)">
                  照护方案
                </el-button>
              </div>
            </div>
          </template>

          <el-alert v-if="night.frequentFlag" type="error" :closable="false" show-icon style="margin-bottom:10px"
                    title="该老人近一周多次夜间按键求助，已构成“频繁夜间求助”，建议发起长期照护评估并制定长期照护方案" />

          <el-row :gutter="10" style="margin-bottom:10px">
            <el-col :span="6"><div class="obs-card"><div class="obs-num">{{ night.totalNightCount || 0 }}</div><div class="muted">夜间求助</div></div></el-col>
            <el-col :span="6"><div class="obs-card"><div class="obs-num" style="color:#67c23a">{{ night.lonelyAnxiousCount || 0 }}</div><div class="muted">孤独焦虑（次到场发现）</div></div></el-col>
            <el-col :span="6"><div class="obs-card"><div class="obs-num" style="color:#909399">{{ night.medicationChaoticCount || 0 }}</div><div class="muted">药物管理混乱</div></div></el-col>
            <el-col :span="6">
              <div class="obs-card">
                <div class="obs-num" :style="{color:(night.familyReachRate||0) < 40 ? '#f56c6c':'#67c23a'}">
                  {{ night.familyAttempts ? night.familyReachRate + '%' : '—' }}
                </div>
                <div class="muted">家属接听（{{ night.familyReached || 0 }}/{{ night.familyAttempts || 0 }}）</div>
              </div>
            </el-col>
          </el-row>

          <el-table :data="nightEvents" size="small" @row-click="r => $router.push(`/events/${r.id}`)" style="cursor:pointer">
            <el-table-column label="求助时间" width="150">
              <template #default="{ row }">{{ fmt(row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="编号" width="120" prop="eventNo" />
            <el-table-column label="求助原因" min-width="180">
              <template #default="{ row }">
                <div>{{ row.voiceContent || '（无语音）' }}</div>
                <el-tag size="small" type="warning" effect="plain" v-if="row.triageType">{{ TRIAGE_LABEL[row.triageType] }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="到场结果" min-width="170">
              <template #default="{ row }">
                <template v-for="v in (night.visitsByEvent?.[row.id] || [])" :key="v.id">
                  <el-tag size="small" type="success" effect="plain">{{ v.elderStatus }}</el-tag>
                  <el-tag v-if="v.hospitalNeeded" size="small" type="danger">需送医</el-tag>
                  <el-tag v-if="v.temporaryCareNeeded" size="small" type="warning">临时照护</el-tag>
                  <el-tag v-if="v.lonelyAnxious" size="small" type="success">孤独焦虑</el-tag>
                  <el-tag v-if="v.medicationChaotic" size="small" type="info">药物混乱</el-tag>
                </template>
                <span v-if="!(night.visitsByEvent?.[row.id] || []).length" class="muted">未上门</span>
              </template>
            </el-table-column>
            <el-table-column label="家属参与" width="150">
              <template #default="{ row }">
                <template v-for="(c, i) in (night.commsByEvent?.[row.id] || [])" :key="c.id">
                  <el-tag size="small" :type="c.reached ? 'success' : 'danger'" style="margin:1px">
                    {{ c.contactName }}{{ c.reached ? '接通' : '未接' }}
                  </el-tag>
                </template>
                <span v-if="!(night.commsByEvent?.[row.id] || []).length" class="muted">未联系</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 评估与方案 -->
        <el-card shadow="never" style="margin-bottom:12px">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span class="section-title" style="margin:0">长期照护评估与方案（{{ assessments.length }} 次评估 / {{ plans.length }} 个方案）</span>
              <el-button type="danger" size="small" plain :icon="MagicStick" @click="openAssessment">发起评估</el-button>
            </div>
          </template>
          <el-empty v-if="!assessments.length && !plans.length" :image-size="50"
                    description="暂无长期照护评估；夜间多次求助后可发起评估并制定长期照护方案" />
          <el-timeline v-else>
            <el-timeline-item v-for="p in plans" :key="'p'+p.id" :timestamp="fmt(p.effectiveAt || p.createdAt)" type="danger">
              <el-tag size="small" :type="PLAN_STATUS_TAG[p.status]">{{ PLAN_STATUS_LABEL[p.status] }}</el-tag>
              <b style="margin:0 6px">{{ p.planNo }} {{ p.planName }}</b>
              <el-link type="primary" @click.stop="$router.push(`/care-plans/${p.id}`)">跟踪观察 →</el-link>
              <div class="muted">
                观察 {{ p.observeDays }} 天
                <template v-if="p.lastReviewResult">· 最近判定：{{ VERDICT_LABEL[p.lastReviewResult] }}</template>
              </div>
            </el-timeline-item>
            <el-timeline-item v-for="a in assessments" :key="'a'+a.id" :timestamp="fmt(a.assessedAt)" type="warning">
              <el-tag size="small" type="warning" effect="plain">{{ CARE_LEVEL_LABEL[a.resultLevel] }}</el-tag>
              <b style="margin:0 6px">长期照护评估 #{{ a.id }}</b>
              <el-tag size="small" type="success" v-if="a.lonelyAnxious">孤独焦虑</el-tag>
              <el-tag size="small" type="info" v-if="a.medicationChaotic">药物混乱</el-tag>
              <el-tag size="small" :type="FAMILY_INVOLVEMENT_TAG[a.familyInvolvement]">
                家属{{ FAMILY_INVOLVEMENT_LABEL[a.familyInvolvement] }}
              </el-tag>
              <div class="muted">近{{ a.periodDays }}天夜间求助 {{ a.nightHelpCount }} 次 · 建议：{{ recommendText(a.recommendations) }}</div>
            </el-timeline-item>
          </el-timeline>
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

        <el-card shadow="never" style="margin-top:12px">
          <template #header><span class="section-title" style="margin:0">照护档案留痕（{{ archiveNotes.length }}）</span></template>
          <el-timeline class="timeline-dense">
            <el-timeline-item v-for="n in archiveNotes" :key="n.id" :timestamp="fmt(n.createdAt)" placement="top">
              <el-tag size="small" :type="ARCHIVE_TYPE_TAG[n.type]" effect="plain" style="margin-right:4px">
                {{ ARCHIVE_TYPE_LABEL[n.type] }}
              </el-tag>
              <b style="font-size:13px">{{ n.title }}</b>
              <div class="muted" style="font-size:12px;line-height:1.5">{{ n.content }}</div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>

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

    <!-- 长期照护评估 -->
    <el-dialog v-model="assessDialog" title="发起长期照护评估" width="680px" top="4vh">
      <el-alert type="info" :closable="false" style="margin-bottom:10px"
                :title="`系统已汇总近 ${night.periodDays || 7} 天夜间求助 ${night.totalNightCount || 0} 次、家属接听率 ${night.familyAttempts ? night.familyReachRate + '%' : '无数据'}，可在汇总基础上补充评估意见`" />
      <el-form :model="assessForm" label-width="118px">
        <el-form-item label="夜间求助原因">
          <el-input v-model="assessForm.nightReasonSummary" type="textarea" :rows="2"
                    placeholder="留空则由系统按每次夜间事件的语音/研判自动汇总" />
        </el-form-item>
        <el-form-item label="到场结果汇总">
          <el-input v-model="assessForm.onsiteResultSummary" type="textarea" :rows="2"
                    placeholder="留空则由系统按上门记录自动汇总" />
        </el-form-item>
        <el-row>
          <el-col :span="12"><el-form-item label="孤独焦虑"><el-switch v-model="assessForm.lonelyAnxious" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="药物管理混乱"><el-switch v-model="assessForm.medicationChaotic" /></el-form-item></el-col>
        </el-row>
        <el-form-item v-if="assessForm.lonelyAnxious" label="情绪评估说明">
          <el-input v-model="assessForm.emotionNote" placeholder="如：丧偶后长期独处、夜间焦虑害怕、情绪低落" />
        </el-form-item>
        <el-form-item v-if="assessForm.medicationChaotic" label="用药评估说明">
          <el-input v-model="assessForm.medicationNote" placeholder="如：多种慢病用药、药盒混放、经常漏服" />
        </el-form-item>
        <el-form-item label="家属参与情况">
          <el-radio-group v-model="assessForm.familyInvolvement">
            <el-radio v-for="(v,k) in FAMILY_INVOLVEMENT_LABEL" :key="k" :value="k">{{ v }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="家属参与说明"><el-input v-model="assessForm.familyNote" /></el-form-item>
        <el-form-item label="日常生活能力"><el-input v-model="assessForm.dailyLivingNote" /></el-form-item>
        <el-form-item label="慢病健康风险"><el-input v-model="assessForm.chronicRiskNote" /></el-form-item>
        <el-form-item label="居家安全情况"><el-input v-model="assessForm.homeSafetyNote" /></el-form-item>
        <el-form-item label="照护强度">
          <el-radio-group v-model="assessForm.resultLevel">
            <el-radio v-for="(v,k) in CARE_LEVEL_LABEL" :key="k" :value="k">{{ v }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="建议长期服务">
          <el-checkbox-group v-model="assessForm.recommendations">
            <el-checkbox value="REGULAR_VISIT">定期探访</el-checkbox>
            <el-checkbox value="PSYCH_COMPANION">心理陪伴</el-checkbox>
            <el-checkbox value="MEAL_SERVICE">助餐</el-checkbox>
            <el-checkbox value="FAMILY_DOCTOR">家庭医生随访</el-checkbox>
            <el-checkbox value="MED_REMINDER">药盒提醒</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="评估总结"><el-input v-model="assessForm.conclusion" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assessDialog = false">取消</el-button>
        <el-button type="danger" @click="submitAssessment">提交评估</el-button>
      </template>
    </el-dialog>

    <!-- 制定长期照护方案 -->
    <el-dialog v-model="planDialog" title="制定长期照护方案" width="720px" top="4vh">
      <el-form :model="planForm" label-width="96px">
        <el-form-item label="方案名称"><el-input v-model="planForm.planName" /></el-form-item>
        <el-form-item label="观察周期">
          <el-radio-group v-model="planForm.observeDays">
            <el-radio :value="14">2周</el-radio>
            <el-radio :value="28">4周</el-radio>
            <el-radio :value="56">8周</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="方案目标"><el-input v-model="planForm.goal" type="textarea" :rows="2"
          placeholder="如：夜间求助次数下降一半以上，助餐每日签到，规范服药，家属每周至少联系2次" /></el-form-item>

        <el-divider content-position="left">照护服务项</el-divider>
        <div v-for="(it, idx) in planForm.items" :key="idx" class="plan-item">
          <el-select v-model="it.itemType" style="width:130px" placeholder="服务类型">
            <el-option v-for="(v,k) in PLAN_ITEM_OPTIONS" :key="k" :label="v" :value="k" />
          </el-select>
          <el-input v-model="it.frequency" placeholder="频次，如 每周2次 / 每日 / 每月1次" style="width:200px;margin:0 8px" />
          <el-input v-model="it.providerName" placeholder="责任方，如 网格员/助餐点/钱医生" style="width:180px;margin-right:8px" />
          <el-input v-model="it.providerPhone" placeholder="电话" style="width:130px" />
          <el-button link type="danger" @click="planForm.items.splice(idx,1)">删除</el-button>
        </div>
        <el-button size="small" @click="addPlanItem" style="margin:8px 0">+ 添加服务项</el-button>
      </el-form>
      <template #footer>
        <el-button @click="planDialog = false">取消</el-button>
        <el-button type="warning" @click="submitPlan(false)">存为草案</el-button>
        <el-button type="danger" @click="submitPlan(true)">保存并立即生效</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CircleCheck, CircleClose, MagicStick } from '@element-plus/icons-vue'
import api from '../api'
import {
  RISK_LEVEL_LABEL, RISK_LEVEL_TAG, TRIAGE_LABEL, SEVERITY_LABEL, SEVERITY_TAG,
  RISK_LABEL, RISK_TAG, STATUS_LABEL, STATUS_TAG, FOLLOW_RESULT_LABEL,
  CARE_LEVEL_LABEL, FAMILY_INVOLVEMENT_LABEL, FAMILY_INVOLVEMENT_TAG,
  PLAN_STATUS_LABEL, PLAN_STATUS_TAG, VERDICT_LABEL, ARCHIVE_TYPE_LABEL, ARCHIVE_TYPE_TAG
} from '../api/dict'

const route = useRoute()
const id = route.params.id
const data = ref({})
const events = ref([])
const loading = ref(false)
const contactDialog = ref(false)
const neighborDialog = ref(false)
const contactForm = reactive({})
const neighborForm = reactive({})

const PLAN_ITEM_OPTIONS = {
  REGULAR_VISIT: '定期探访',
  PSYCH_COMPANION: '心理陪伴',
  MEAL_SERVICE: '助餐',
  FAMILY_DOCTOR: '家庭医生随访',
  MED_REMINDER: '药盒提醒',
  OTHER: '其他'
}

const night = ref({})
const nightDays = ref(7)
const nightEvents = computed(() => night.value.events || [])
const assessments = ref([])
const plans = ref([])
const archiveNotes = computed(() => data.value.archiveNotes || [])

function recommendText(csv) {
  if (!csv) return '无'
  return csv.split(',').filter(Boolean).map(k => PLAN_ITEM_OPTIONS[k] || k).join('、')
}

async function loadNight() {
  night.value = await api.get(`/elders/${id}/night-review`, { params: { days: nightDays.value } })
}
async function loadCare() {
  const [a, p] = await Promise.all([
    api.get(`/elders/${id}/assessments`),
    api.get('/care-plans', { params: { elderId: id } })
  ])
  assessments.value = a
  plans.value = p
}

async function load() {
  loading.value = true
  try {
    data.value = await api.get(`/elders/${id}`)
    events.value = await api.get('/events', { params: { elderId: id } })
    await Promise.all([loadNight(), loadCare()])
  } finally {
    loading.value = false
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

/* ---------- 长期照护评估 / 方案 ---------- */
const assessDialog = ref(false)
const assessForm = reactive({})
const planDialog = ref(false)
const planForm = reactive({ items: [] })

function openAssessment() {
  Object.assign(assessForm, {
    periodDays: night.value.periodDays || 7,
    nightReasonSummary: '', onsiteResultSummary: '',
    lonelyAnxious: (night.value.lonelyAnxiousCount || 0) > 0,
    emotionNote: '', medicationChaotic: (night.value.medicationChaoticCount || 0) > 0,
    medicationNote: '', familyInvolvement: (night.value.familyReachRate || 0) < 40 ? 'POOR' : 'AVERAGE',
    familyNote: '', dailyLivingNote: '', chronicRiskNote: '', homeSafetyNote: '',
    resultLevel: 'INTENSIVE', conclusion: '', recommendations: ['REGULAR_VISIT', 'PSYCH_COMPANION']
  })
  assessDialog.value = true
}

async function submitAssessment() {
  const a = await api.post(`/elders/${id}/assessments`, assessForm)
  ElMessage.success('评估已完成并写入老人档案')
  assessDialog.value = false
  await ElMessageBox.confirm(
    '评估已完成。是否立即依据评估建议制定长期照护方案（定期探访/心理陪伴/助餐/家庭医生随访/药盒提醒）？',
    '制定照护方案', { confirmButtonText: '去制定方案', cancelButtonText: '稍后', type: 'success' })
    .then(() => openPlanDialog(a.id)).catch(() => {})
  load()
}

function addPlanItem(type, provider, freq) {
  planForm.items.push({ itemType: type, frequency: freq || '', providerName: provider || '', providerPhone: '', targetNote: '', plannedTimes: null })
}
function openPlanDialog(assessmentId) {
  Object.assign(planForm, {
    assessmentId: assessmentId || null,
    planName: `${data.value.elder?.name || ''}长期照护方案`,
    goal: '', observeDays: 28, activate: false,
    items: []
  })
  // 按最近一次评估建议预填服务项
  const recs = (assessments.value[0]?.recommendations || '').split(',').filter(Boolean)
  const defaults = {
    REGULAR_VISIT: ['网格员赵网格员', '每周探访2次'],
    PSYCH_COMPANION: ['社区心理服务站', '每周心理陪伴1次'],
    MEAL_SERVICE: ['幸福社区助餐点', '每日助餐'],
    FAMILY_DOCTOR: ['钱医生', '每月家庭医生随访1次'],
    MED_REMINDER: ['智能药盒+网格员', '每日早晚药盒提醒']
  }
  const list = recs.length ? recs : ['REGULAR_VISIT', 'PSYCH_COMPANION', 'MEAL_SERVICE', 'FAMILY_DOCTOR', 'MED_REMINDER']
  list.forEach(k => defaults[k] && addPlanItem(k, defaults[k][0], defaults[k][1]))
  planDialog.value = true
}
async function submitPlan(activate) {
  if (!planForm.planName) { ElMessage.warning('请填写方案名称'); return }
  const body = { ...planForm, elderId: Number(id), activate }
  const p = await api.post('/care-plans', body)
  ElMessage.success(activate ? '方案已生效，开始观察求助频次变化' : '方案草案已保存')
  planDialog.value = false
  if (activate) router.push(`/care-plans/${p.id}`)
  else load()
}

const router = useRouter()

function fmt(s) { return s ? String(s).replace('T', ' ').substring(0, 16) : '' }
onMounted(load)
</script>

<style scoped>
.person { display: flex; justify-content: space-between; align-items: center; padding: 8px 0; border-bottom: 1px dashed #ebeef5; }
.obs-card { background:#faf7f7; border:1px solid #f3e3e3; border-radius:8px; padding:10px 12px; text-align:center; }
.obs-num { font-size:24px; font-weight:700; color:#f56c6c; line-height:1.2; }
.plan-item { display:flex; align-items:center; margin-bottom:8px; }
.timeline-dense :deep(.el-timeline-item__node) { display:none; }
.timeline-dense :deep(.el-timeline-item__tail) { border-left-style: dashed; }
</style>
