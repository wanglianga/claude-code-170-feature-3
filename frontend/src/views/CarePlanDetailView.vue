<template>
  <div v-loading="loading">
    <el-page-header @back="$router.push(`/elders/${d.elder?.id}`)" style="margin-bottom:12px">
      <template #content>
        <span style="font-weight:700;font-size:16px">{{ d.plan?.planNo }}</span>
        <el-tag :type="PLAN_STATUS_TAG[d.plan?.status]" effect="dark" style="margin-left:10px">
          {{ PLAN_STATUS_LABEL[d.plan?.status] }}
        </el-tag>
        <el-link type="primary" style="margin-left:12px" @click="$router.push(`/elders/${d.elder?.id}`)">
          {{ d.elder?.name }}
        </el-link>
      </template>
      <template #extra>
        <el-space>
          <el-button v-if="d.plan?.status === 'DRAFT'" type="success" @click="activate">方案生效</el-button>
          <el-button v-if="d.plan?.status === 'ACTIVE'" type="warning" plain @click="endPlan">结束方案</el-button>
          <el-button type="danger" @click="openConference">发起个案会商</el-button>
        </el-space>
      </template>
    </el-page-header>

    <!-- 无改善提示 -->
    <el-alert v-if="obs.active && obs.noImprovement" type="error" show-icon :closable="false" style="margin-bottom:12px">
      <template #title>
        方案生效已 {{ obs.elapsedDays }} 天，夜间求助 {{ obs.nightHelp.current }} 次（生效前同等时长
        {{ obs.nightHelp.beforeSameWindow }} 次）<b>未见下降</b>，建议发起个案会商，由养老顾问、社区医生、家属共同协商。
        <el-button type="danger" size="small" style="margin-left:12px" @click="openConference(true)">立即发起会商</el-button>
      </template>
    </el-alert>
    <el-alert v-if="obs.active && obs.improved" type="success" show-icon :closable="false" style="margin-bottom:12px"
              :title="`方案见效：夜间求助由 ${obs.nightHelp.beforeSameWindow} 次降至 ${obs.nightHelp.current} 次，继续观察。`" />

    <el-row :gutter="12">
      <el-col :span="15">
        <!-- 评估与方案 -->
        <el-card shadow="never" style="margin-bottom:12px">
          <template #header><span class="section-title">长期照护评估与方案</span></template>
          <el-descriptions :column="2" border size="small" v-if="d.plan">
            <el-descriptions-item label="发起社工">{{ d.plan.initiatorName }}</el-descriptions-item>
            <el-descriptions-item label="发起时间">{{ fmt(d.plan.initiatedAt) }}</el-descriptions-item>
            <el-descriptions-item label="生效时间">{{ d.plan.effectiveAt ? fmt(d.plan.effectiveAt) : '待生效' }}</el-descriptions-item>
            <el-descriptions-item label="观察期">{{ d.plan.observeDays }} 天</el-descriptions-item>
            <el-descriptions-item label="触发原因" :span="2">{{ d.plan.triggerReason }}</el-descriptions-item>
            <el-descriptions-item label="孤独焦虑评分">{{ scoreText(d.plan.lonelinessScore) }}</el-descriptions-item>
            <el-descriptions-item label="用药管理评分">{{ scoreText(d.plan.medicationScore) }}</el-descriptions-item>
            <el-descriptions-item label="家属支持评分">{{ scoreText(d.plan.familySupportScore) }}</el-descriptions-item>
            <el-descriptions-item label="生活自理评分">{{ scoreText(d.plan.selfCareScore) }}</el-descriptions-item>
            <el-descriptions-item label="评估结论" :span="2">{{ d.plan.assessmentConclusion || '—' }}</el-descriptions-item>
            <el-descriptions-item label="方案目标" :span="2">{{ d.plan.goals || '—' }}</el-descriptions-item>
            <el-descriptions-item v-if="d.plan.endReason" label="结束/调整原因" :span="2">{{ d.plan.endReason }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 效果观察 -->
        <el-card shadow="never" style="margin-bottom:12px">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span class="section-title" style="margin:0">方案效果持续观察</span>
              <el-button size="small" @click="load">刷新</el-button>
            </div>
          </template>
          <el-alert v-if="!obs.active" type="info" :closable="false" style="margin-bottom:10px"
                    :title="obs.message || '方案生效后开始观察'" />
          <template v-else>
            <div class="muted" style="margin-bottom:8px">
              生效 {{ obs.elapsedDays }} 天，以下按“生效前同等 {{ obs.compareDays }} 天”对比
            </div>
            <el-table :data="metricRows" size="small" border>
              <el-table-column prop="metric" label="观察指标" width="150" />
              <el-table-column label="生效前基线" width="130">
                <template #default="{ row }">
                  <span v-if="row.rate">前30天 {{ row.baseline }}%</span>
                  <span v-else>{{ row.baseline }} {{ row.unit }}</span>
                </template>
              </el-table-column>
              <el-table-column label="前同等时长" width="110">
                <template #default="{ row }">{{ row.before }} {{ row.unit }}</template>
              </el-table-column>
              <el-table-column label="生效以来" width="110">
                <template #default="{ row }">
                  <b :style="{ color: row.good ? '#67c23a' : row.bad ? '#f56c6c' : '' }">{{ row.current }} {{ row.unit }}</b>
                </template>
              </el-table-column>
              <el-table-column label="变化趋势">
                <template #default="{ row }">
                  <el-tag size="small" :type="row.good ? 'success' : row.bad ? 'danger' : 'info'">
                    {{ row.trend }}
                  </el-tag>
                  <span class="muted" style="margin-left:6px">{{ row.extra }}</span>
                </template>
              </el-table-column>
            </el-table>
            <el-descriptions :column="3" border size="small" style="margin-top:10px">
              <el-descriptions-item label="定期探访执行">{{ obs.executions?.REGULAR_VISIT ?? 0 }} 次</el-descriptions-item>
              <el-descriptions-item label="心理陪伴执行">{{ obs.executions?.PSYCH_COMPANION ?? 0 }} 次</el-descriptions-item>
              <el-descriptions-item label="家庭医生随访">{{ obs.executions?.FAMILY_DOCTOR ?? 0 }} 次</el-descriptions-item>
            </el-descriptions>
          </template>
        </el-card>

        <!-- 服务安排 -->
        <el-card shadow="never" style="margin-bottom:12px">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span class="section-title" style="margin:0">长期服务安排（{{ d.items?.length || 0 }}）</span>
              <div>
                <el-button size="small" type="primary" @click="openExecution">登记服务执行</el-button>
                <el-button size="small" @click="itemDialog = true">追加安排</el-button>
              </div>
            </div>
          </template>
          <el-empty v-if="!d.items?.length" :image-size="50" description="暂无安排" />
          <el-table v-else :data="d.items" size="small">
            <el-table-column label="类型" width="120">
              <template #default="{ row }">{{ PLAN_SERVICE_LABEL[row.serviceType] }}</template>
            </el-table-column>
            <el-table-column prop="providerName" label="负责人/提供方" width="140" />
            <el-table-column prop="frequency" label="频次" width="130" />
            <el-table-column prop="content" label="说明" show-overflow-tooltip />
            <el-table-column label="执行" width="90">
              <template #default="{ row }">{{ row.executedCount || 0 }} 次</template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-select v-model="row.status" size="small" @change="changeItemStatus(row)">
                  <el-option v-for="(v, k) in ITEM_STATUS_LABEL" :key="k" :label="v" :value="k" />
                </el-select>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 执行记录 -->
        <el-card shadow="never" style="margin-bottom:12px">
          <template #header><span class="section-title">服务执行记录（{{ d.executions?.length || 0 }}）</span></template>
          <el-empty v-if="!d.executions?.length" :image-size="50" description="暂无执行记录" />
          <el-timeline v-else>
            <el-timeline-item v-for="x in d.executions" :key="x.id" :timestamp="fmt(x.executedAt)" type="success">
              <el-tag size="small" type="success">{{ PLAN_SERVICE_LABEL[x.serviceType] }}</el-tag>
              <b style="margin:0 6px">{{ x.executorName }}</b>
              <span class="muted">{{ x.content }}</span>
              <div v-if="x.doctorConclusion" class="muted">随访结论：{{ x.doctorConclusion }}</div>
            </el-timeline-item>
          </el-timeline>
        </el-card>

        <el-row :gutter="12">
          <el-col :span="12">
            <el-card shadow="never" style="margin-bottom:12px">
              <template #header>
                <div style="display:flex;justify-content:space-between;align-items:center">
                  <span class="section-title" style="margin:0">助餐签到</span>
                  <el-button size="small" type="warning" @click="openMeal">登记签到</el-button>
                </div>
              </template>
              <el-empty v-if="!d.meals?.length" :image-size="40" description="无" />
              <div v-for="m in d.meals" :key="m.id" class="log-line">
                <el-tag size="small" :type="m.checkedIn ? 'success' : 'info'">
                  {{ m.checkedIn ? '已签到' : '未用餐' }}
                </el-tag>
                <b style="margin:0 6px">{{ m.mealDate }} {{ m.mealType }}</b>
                <span class="muted">{{ m.deliveryMode === 'DELIVERY' ? '配送上门' : '堂食' }} · {{ m.operatorName }}</span>
                <div v-if="m.note" class="muted">{{ m.note }}</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="never" style="margin-bottom:12px">
              <template #header>
                <div style="display:flex;justify-content:space-between;align-items:center">
                  <span class="section-title" style="margin:0">药盒提醒</span>
                  <el-button size="small" type="warning" @click="openMed">新增提醒</el-button>
                </div>
              </template>
              <el-empty v-if="!d.reminders?.length" :image-size="40" description="无" />
              <div v-for="r in d.reminders" :key="r.id" class="log-line">
                <el-tag size="small" :type="MED_STATUS_TAG[r.status]">{{ MED_STATUS_LABEL[r.status] }}</el-tag>
                <b style="margin:0 6px">{{ r.medicineName }}（{{ r.dosageSlot }}）</b>
                <span class="muted">{{ fmt(r.remindAt) }}</span>
                <span v-if="r.status === 'PENDING'" style="margin-left:6px">
                  <el-button link type="success" size="small" @click="changeMed(r.id, 'CONFIRMED')">确认服药</el-button>
                  <el-button link type="danger" size="small" @click="changeMed(r.id, 'MISSED')">漏服</el-button>
                </span>
                <div v-if="r.confirmSource" class="muted">确认来源：{{ r.confirmSource }}</div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 家属接听 -->
        <el-card shadow="never" style="margin-bottom:12px">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span class="section-title" style="margin:0">家属接听情况（{{ d.familyCalls?.length || 0 }}）</span>
              <el-button size="small" type="info" @click="openCall">联系家属并记录</el-button>
            </div>
          </template>
          <el-empty v-if="!d.familyCalls?.length" :image-size="40" description="方案生效后暂无家属联系记录" />
          <div v-for="c in d.familyCalls" :key="c.id" class="log-line">
            <el-tag size="small" :type="c.reached ? 'success' : 'danger'">{{ c.reached ? '已接通' : '未接通' }}</el-tag>
            <b style="margin:0 6px">{{ c.contactName }}（{{ c.relationship }}）</b>
            <span class="muted">{{ c.channel }} · {{ c.attitude || '' }} · {{ fmt(c.calledAt) }}</span>
            <div v-if="c.note" class="muted">{{ c.note }}</div>
          </div>
        </el-card>

        <!-- 个案会商 -->
        <el-card shadow="never">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span class="section-title" style="margin:0">个案会商（{{ d.conferences?.length || 0 }}）</span>
              <el-button size="small" type="danger" @click="openConference">发起个案会商</el-button>
            </div>
          </template>
          <el-empty v-if="!d.conferences?.length" :image-size="50"
                    description="方案无改善时可发起会商，养老顾问/社区医生/家属协商结论写回老人档案" />
          <div v-for="c in d.conferences" :key="c.id" class="conf-box">
            <div>
              <b>{{ c.conferenceNo }}</b>
              <el-tag size="small" :type="CONFERENCE_STATUS_TAG[c.status]" style="margin-left:8px">
                {{ CONFERENCE_STATUS_LABEL[c.status] }}
              </el-tag>
              <div class="muted">召集：{{ c.convenerName }} · 计划 {{ fmt(c.scheduledAt) }}</div>
              <div class="muted">参与方：{{ c.participants || '待确认' }}</div>
              <div class="muted">原因：{{ c.reason }}</div>
              <div v-if="c.status === 'HELD'" style="margin-top:6px">
                <div><b>协商结论：</b>{{ c.conclusion }}</div>
                <div><b>后续行动：</b>{{ c.actionItems }}</div>
                <el-tag type="success" size="small" effect="plain">已写回老人档案</el-tag>
              </div>
            </div>
            <el-button v-if="c.status === 'SCHEDULED'" type="danger" size="small" @click="openConclusion(c)">填写会商结论</el-button>
          </div>
        </el-card>
      </el-col>

      <!-- 右：基线 + 时间线 -->
      <el-col :span="9">
        <el-card shadow="never" style="margin-bottom:12px">
          <template #header><span class="section-title">评估基线（生效前30天）</span></template>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="夜间求助">{{ d.plan?.baselineNightHelps ?? 0 }} 次</el-descriptions-item>
            <el-descriptions-item label="家属接听率">{{ d.plan?.baselineFamilyAnswerRate ?? 0 }}%</el-descriptions-item>
            <el-descriptions-item label="助餐签到">{{ d.plan?.baselineMealCheckins ?? 0 }} 次</el-descriptions-item>
            <el-descriptions-item label="药盒确认服药">{{ d.plan?.baselineMedReminders ?? 0 }} 次</el-descriptions-item>
          </el-descriptions>
        </el-card>
        <el-card shadow="never">
          <template #header><span class="section-title">方案全过程时间线</span></template>
          <el-timeline class="timeline-dense">
            <el-timeline-item v-for="l in d.logs" :key="l.id" :timestamp="fmt(l.createdAt)"
                              :type="LOG_TYPE[l.type] || 'info'" size="large">
              <el-tag size="small" effect="plain" style="margin-right:6px">{{ LOG_LABEL[l.type] || l.type }}</el-tag>
              <span style="font-size:13px">{{ l.content }}</span>
              <div class="muted">{{ l.operatorName }}</div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>

    <!-- 追加安排 -->
    <el-dialog v-model="itemDialog" title="追加长期服务安排" width="520px">
      <el-form :model="itemForm" label-width="100px">
        <el-form-item label="服务类型">
          <el-select v-model="itemForm.serviceType" style="width:100%">
            <el-option v-for="(v, k) in PLAN_SERVICE_LABEL" :key="k" :label="v" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人/提供方"><el-input v-model="itemForm.providerName" /></el-form-item>
        <el-form-item label="频次"><el-input v-model="itemForm.frequency" placeholder="如 每周2次 / 每日午餐" /></el-form-item>
        <el-form-item label="说明"><el-input v-model="itemForm.content" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="itemDialog = false">取消</el-button>
        <el-button type="primary" @click="submitItem">保存</el-button>
      </template>
    </el-dialog>

    <!-- 登记执行 -->
    <el-dialog v-model="executionDialog" title="登记长期服务执行" width="540px">
      <el-form :model="execForm" label-width="100px">
        <el-form-item label="对应安排">
          <el-select v-model="execForm.itemId" style="width:100%" clearable placeholder="可直接选择服务类型">
            <el-option v-for="i in d.items" :key="i.id"
                       :label="`${PLAN_SERVICE_LABEL[i.serviceType]} · ${i.providerName} · ${i.frequency}`" :value="i.id" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!execForm.itemId" label="服务类型">
          <el-select v-model="execForm.serviceType" style="width:100%">
            <el-option v-for="(v, k) in PLAN_SERVICE_LABEL" :key="k" :label="v" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="执行人"><el-input v-model="execForm.executorName" /></el-form-item>
        <el-form-item label="本次情况"><el-input v-model="execForm.content" type="textarea" :rows="2"
                                               placeholder="探访所见、陪伴反馈、老人状态变化" /></el-form-item>
        <el-form-item v-if="(execForm.serviceType === 'FAMILY_DOCTOR')" label="随访结论">
          <el-input v-model="execForm.doctorConclusion" type="textarea" :rows="2"
                    placeholder="随访结论将同步写回老人档案" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="executionDialog = false">取消</el-button>
        <el-button type="primary" @click="submitExecution">保存执行记录</el-button>
      </template>
    </el-dialog>

    <!-- 助餐签到 -->
    <el-dialog v-model="mealDialog" title="助餐签到登记" width="440px">
      <el-form :model="mealForm" label-width="90px">
        <el-form-item label="用餐日期"><el-date-picker v-model="mealForm.mealDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="餐次">
          <el-radio-group v-model="mealForm.mealType"><el-radio value="午餐">午餐</el-radio><el-radio value="晚餐">晚餐</el-radio></el-radio-group>
        </el-form-item>
        <el-form-item label="方式">
          <el-radio-group v-model="mealForm.deliveryMode"><el-radio value="DINE_IN">堂食</el-radio><el-radio value="DELIVERY">配送上门</el-radio></el-radio-group>
        </el-form-item>
        <el-form-item label="是否用餐"><el-switch v-model="mealForm.checkedIn" active-text="已签到用餐" inactive-text="未用餐" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="mealForm.note" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="mealDialog = false">取消</el-button>
        <el-button type="warning" @click="submitMeal">保存</el-button>
      </template>
    </el-dialog>

    <!-- 药盒提醒 -->
    <el-dialog v-model="medDialog" title="药盒提醒登记" width="440px">
      <el-form :model="medForm" label-width="90px">
        <el-form-item label="药品"><el-input v-model="medForm.medicineName" placeholder="如 降压药" /></el-form-item>
        <el-form-item label="药次">
          <el-radio-group v-model="medForm.dosageSlot"><el-radio value="早">早</el-radio><el-radio value="中">中</el-radio><el-radio value="晚">晚</el-radio><el-radio value="睡前">睡前</el-radio></el-radio-group>
        </el-form-item>
        <el-form-item label="提醒时间"><el-date-picker v-model="medForm.remindAt" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="medForm.status">
            <el-radio value="PENDING">待确认</el-radio><el-radio value="CONFIRMED">已确认服药</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="medForm.status === 'CONFIRMED'" label="确认来源">
          <el-input v-model="medForm.confirmSource" placeholder="老人按键确认/家属确认/社工确认" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="medDialog = false">取消</el-button>
        <el-button type="warning" @click="submitMed">保存</el-button>
      </template>
    </el-dialog>

    <!-- 家属联系 -->
    <el-dialog v-model="callDialog" title="联系家属并记录接听情况" width="480px">
      <el-form :model="callForm" label-width="90px">
        <el-form-item label="选择联系人">
          <el-select v-model="callForm.contactId" style="width:100%" clearable @change="onContactChange">
            <el-option v-for="c in contacts" :key="c.id" :label="`${c.relationship} ${c.name} ${c.phone}`" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="姓名"><el-input v-model="callForm.contactName" /></el-form-item>
        <el-form-item label="关系"><el-input v-model="callForm.relationship" /></el-form-item>
        <el-form-item label="方式">
          <el-radio-group v-model="callForm.channel"><el-radio value="电话">电话</el-radio><el-radio value="视频">视频</el-radio><el-radio value="微信语音">微信语音</el-radio></el-radio-group>
        </el-form-item>
        <el-form-item label="是否接通"><el-switch v-model="callForm.reached" active-text="已接通" inactive-text="未接通" /></el-form-item>
        <el-form-item v-if="callForm.reached" label="态度">
          <el-radio-group v-model="callForm.attitude">
            <el-radio value="积极配合">积极配合</el-radio><el-radio value="一般">一般</el-radio><el-radio value="推诿拒绝">推诿拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="沟通事项"><el-input v-model="callForm.note" type="textarea" :rows="2"
                                              placeholder="是否知晓方案、是否愿意每周固定接听/探访" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="callDialog = false">取消</el-button>
        <el-button type="info" @click="submitCall">保存</el-button>
      </template>
    </el-dialog>

    <!-- 发起会商 -->
    <el-dialog v-model="conferenceDialog" title="发起个案会商" width="540px">
      <el-alert type="warning" :closable="false" style="margin-bottom:10px"
                title="会商将召集养老顾问、社区医生、家属等；召开后把协商结论写回老人档案。" />
      <el-form :model="confForm" label-width="90px">
        <el-form-item label="发起原因">
          <el-input v-model="confForm.reason" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="参与方">
          <el-input v-model="confForm.participants" type="textarea" :rows="2"
                    placeholder="养老顾问-刘顾问,社区医生-钱医生,家属-周建国,网格员-赵网格员" />
        </el-form-item>
        <el-form-item label="计划时间">
          <el-date-picker v-model="confForm.scheduledAt" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="conferenceDialog = false">取消</el-button>
        <el-button type="danger" @click="submitConference">发起会商</el-button>
      </template>
    </el-dialog>

    <!-- 会商结论 -->
    <el-dialog v-model="conclusionDialog" title="个案会商结论（写回老人档案）" width="600px">
      <el-form :model="conclusionForm" label-width="100px">
        <el-form-item label="协商结论">
          <el-input v-model="conclusionForm.conclusion" type="textarea" :rows="3"
                    placeholder="养老顾问、社区医生、家属共同协商形成的结论" />
        </el-form-item>
        <el-form-item label="后续行动">
          <el-input v-model="conclusionForm.actionItems" type="textarea" :rows="3"
                    placeholder="如：加密巡访至每周3次、子女轮值每周来电、家庭医生两周上门、配智能药盒、转养老顾问评估助餐" />
        </el-form-item>
        <el-form-item label="调整方案">
          <el-switch v-model="conclusionForm.adjustPlan" active-text="同时把当前方案标记为“已调整”，随后按结论发起新方案" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="conclusionDialog = false">取消</el-button>
        <el-button type="danger" @click="submitConclusion">保存结论并写回档案</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'
import {
  PLAN_STATUS_LABEL, PLAN_STATUS_TAG, PLAN_SERVICE_LABEL, ITEM_STATUS_LABEL,
  MED_STATUS_LABEL, MED_STATUS_TAG, CONFERENCE_STATUS_LABEL, CONFERENCE_STATUS_TAG
} from '../api/dict'

const route = useRoute()
const planId = route.params.id
const d = ref({})
const obs = ref({})
const contacts = ref([])
const loading = ref(false)

const LOG_LABEL = {
  ASSESS: '评估', PLAN: '方案', SERVICE: '服务执行', MEAL: '助餐',
  MEDICATION: '药盒提醒', FAMILY: '家属联系', OBSERVE: '阶段观察', CONFERENCE: '个案会商', NOTE: '备注'
}
const LOG_TYPE = {
  ASSESS: 'warning', PLAN: 'danger', SERVICE: 'success', MEAL: 'warning',
  MEDICATION: 'primary', FAMILY: 'info', OBSERVE: 'primary', CONFERENCE: 'danger', NOTE: 'info'
}

const metricRows = computed(() => {
  const o = obs.value
  if (!o.active) return []
  const n = o.nightHelp || {}
  const meal = o.mealCheckin || {}
  const med = o.medication || {}
  const fam = o.familyAnswer || {}
  const b = o.baseline || {}
  return [
    {
      metric: '夜间求助次数', unit: '次', baseline: b.nightHelps30 ?? 0, before: n.beforeSameWindow ?? 0,
      current: n.current ?? 0, good: n.decreased, bad: !n.decreased,
      trend: n.decreased ? '↓ 已下降' : '— 未下降', extra: n.decreased ? '核心指标改善' : '核心指标未改善'
    },
    {
      metric: '助餐签到', unit: '次', baseline: b.mealCheckins30 ?? 0, before: meal.beforeSameWindow ?? 0,
      current: meal.current ?? 0, good: (meal.current ?? 0) > 0, bad: false,
      trend: (meal.current ?? 0) > 0 ? '服务已落实' : '尚无签到', extra: '观察助餐是否真正执行'
    },
    {
      metric: '药盒提醒', unit: '次', baseline: b.medConfirmed30 ?? 0, before: med.confirmedBefore ?? 0,
      current: med.confirmed ?? 0,
      good: med.managed, bad: (med.missed ?? 0) > 0,
      trend: med.managed ? '规范无漏服' : (med.missed ? `漏服 ${med.missed} 次` : '待积累数据'),
      extra: `依从率 ${med.adherenceRate ?? 0}%（待确认 ${med.pending ?? 0}）`
    },
    {
      metric: '家属接听率', unit: '%', rate: true, baseline: b.familyAnswerRate ?? 0,
      before: fam.beforeSameWindow ?? 0, current: fam.answerRate ?? 0,
      good: fam.improved, bad: (fam.answerRate ?? 0) < 50,
      trend: fam.improved ? '↑ 有改善' : '— 未改善',
      extra: `生效后联系 ${fam.calls ?? 0} 次接通 ${fam.reached ?? 0} 次`
    }
  ]
})

async function load() {
  loading.value = true
  try {
    d.value = await api.get(`/care-plans/${planId}`)
    obs.value = d.value.observation || {}
    if (d.value.elder?.id) {
      const ed = await api.get(`/elders/${d.value.elder.id}`)
      contacts.value = ed.contacts || []
    }
  } finally {
    loading.value = false
  }
}

function scoreText(v) {
  if (v == null) return '未评分'
  return v + ' / 10'
}
function fmt(s) { return s ? String(s).replace('T', ' ').substring(0, 16) : '' }

async function activate() {
  await ElMessageBox.confirm('确认方案立即生效并开始观察？', '方案生效', { type: 'warning' })
  await api.post(`/care-plans/${planId}/activate`)
  ElMessage.success('方案已生效')
  load()
}
async function endPlan() {
  const { value } = await ElMessageBox.prompt('结束原因', '结束长期照护方案', {
    confirmButtonText: '确认结束', inputType: 'textarea'
  })
  await api.post(`/care-plans/${planId}/end`, null, { params: { reason: value } })
  load()
}

const itemDialog = ref(false)
const itemForm = reactive({ serviceType: 'REGULAR_VISIT', providerName: '', frequency: '', content: '' })
async function submitItem() {
  if (!itemForm.providerName) { ElMessage.warning('请填写负责人'); return }
  await api.post(`/care-plans/${planId}/items`, itemForm)
  itemDialog.value = false
  ElMessage.success('已追加安排')
  load()
}
async function changeItemStatus(row) {
  await api.put(`/care-plans/items/${row.id}/status`, null, { params: { status: row.status } })
  load()
}

const executionDialog = ref(false)
const execForm = reactive({})
function openExecution() {
  Object.assign(execForm, { itemId: null, serviceType: 'REGULAR_VISIT', executorName: '', content: '', doctorConclusion: '' })
  executionDialog.value = true
}
async function submitExecution() {
  if (!execForm.itemId && !execForm.serviceType) { ElMessage.warning('请选择安排或类型'); return }
  await api.post(`/care-plans/${planId}/executions`, execForm)
  executionDialog.value = false
  ElMessage.success('执行已登记')
  load()
}

const mealDialog = ref(false)
const mealForm = reactive({})
function openMeal() {
  Object.assign(mealForm, { mealDate: '', mealType: '午餐', deliveryMode: 'DINE_IN', checkedIn: true, note: '' })
  mealDialog.value = true
}
async function submitMeal() {
  await api.post(`/care-plans/${planId}/meal-checkins`, mealForm)
  mealDialog.value = false
  ElMessage.success('助餐已登记')
  load()
}

const medDialog = ref(false)
const medForm = reactive({})
function openMed() {
  Object.assign(medForm, { medicineName: '', dosageSlot: '早', remindAt: '', status: 'PENDING', confirmSource: '' })
  medDialog.value = true
}
async function submitMed() {
  if (!medForm.medicineName) { ElMessage.warning('请填写药品'); return }
  await api.post(`/care-plans/${planId}/medication-reminders`, medForm)
  medDialog.value = false
  ElMessage.success('提醒已登记')
  load()
}
async function changeMed(id, status) {
  await api.put(`/care-plans/medication-reminders/${id}/status`, null, { params: { status } })
  load()
}

const callDialog = ref(false)
const callForm = reactive({})
function openCall() {
  const p = contacts.value.find(c => c.primaryContact) || contacts.value[0]
  Object.assign(callForm, {
    contactId: p?.id || null, contactName: p?.name || '', relationship: p?.relationship || '',
    channel: '电话', reached: true, attitude: '积极配合', note: ''
  })
  callDialog.value = true
}
function onContactChange(cid) {
  const c = contacts.value.find(x => x.id === cid)
  if (c) { callForm.contactName = c.name; callForm.relationship = c.relationship }
}
async function submitCall() {
  if (!callForm.contactName) { ElMessage.warning('请填写家属姓名'); return }
  await api.post(`/care-plans/${planId}/family-calls`, callForm)
  callDialog.value = false
  ElMessage.success('家属联系已记录')
  load()
}

const conferenceDialog = ref(false)
const confForm = reactive({})
function openConference(force) {
  const o = obs.value
  Object.assign(confForm, {
    reason: o.active && o.noImprovement
      ? `长期照护方案生效 ${o.elapsedDays} 天夜间求助未下降（${o.nightHelp?.beforeSameWindow} → ${o.nightHelp?.current} 次），需多方会商`
      : '长期照护方案观察指标未见明显改善，需多方会商',
    participants: '养老顾问-刘顾问,社区医生-钱医生,网格员-赵网格员,家属',
    scheduledAt: ''
  })
  conferenceDialog.value = true
}
async function submitConference() {
  await api.post('/care-plans/conferences', { ...confForm, planId: Number(planId) })
  conferenceDialog.value = false
  ElMessage.success('个案会商已发起')
  load()
}

const conclusionDialog = ref(false)
const conclusionForm = reactive({})
const currentConf = ref(null)
function openConclusion(c) {
  currentConf.value = c
  Object.assign(conclusionForm, { conclusion: '', actionItems: '', adjustPlan: true })
  conclusionDialog.value = true
}
async function submitConclusion() {
  if (!conclusionForm.conclusion) { ElMessage.warning('请填写协商结论'); return }
  await api.post(`/care-plans/conferences/${currentConf.value.id}/conclusion`, conclusionForm)
  conclusionDialog.value = false
  ElMessage.success('会商结论已写回老人档案')
  load()
}

onMounted(load)
</script>

<style scoped>
.log-line { padding:6px 0; border-bottom:1px dashed #ebeef5; }
.conf-box { display:flex; justify-content:space-between; gap:12px; padding:10px; border:1px solid #f3d4d4; border-radius:8px; margin-bottom:10px; background:#fcf8f8; }
</style>
