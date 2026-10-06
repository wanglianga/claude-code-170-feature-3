<template>
  <div v-loading="loading">
    <el-page-header @back="$router.push('/workbench')" style="margin-bottom:12px">
      <template #content>
        <span style="font-weight:700;font-size:16px">{{ d.event?.eventNo }}</span>
        <el-tag :type="STATUS_TAG[d.event?.status]" style="margin-left:10px">{{ STATUS_LABEL[d.event?.status] }}</el-tag>
        <el-tag v-if="d.event?.nightHelp" type="warning" effect="dark" style="margin-left:6px">夜间求助</el-tag>
        <el-tag v-for="f in flags" :key="f" :type="RISK_TAG[f]" effect="dark" size="small" style="margin-left:6px">
          {{ RISK_LABEL[f] }}
        </el-tag>
      </template>
      <template #extra>
        <el-button-group>
          <el-button :icon="Plus" @click="flagDialog = true">补充风险标记</el-button>
        </el-button-group>
      </template>
    </el-page-header>

    <el-row :gutter="12">
      <!-- 左：处置区 -->
      <el-col :span="17">
        <!-- 求助信息 -->
        <el-card shadow="never" style="margin-bottom:12px">
          <template #header><span class="section-title">求助信息</span></template>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="求助来源">
              {{ SOURCE_LABEL[d.event?.source] }}
              <span v-if="d.event?.sourceDetail" class="muted">（{{ d.event.sourceDetail }}）</span>
            </el-descriptions-item>
            <el-descriptions-item label="求助时间">{{ fmt(d.event?.createdAt) }}</el-descriptions-item>
            <el-descriptions-item label="定位">
              {{ d.event?.callAddress || '—' }}
              <span class="muted" v-if="d.event?.callLongitude">
                （{{ d.event.callLongitude }}, {{ d.event.callLatitude }}）
              </span>
              <el-tag v-if="d.event?.locationOffset" type="danger" size="small" style="margin-left:6px">
                偏移 {{ d.event.locationOffsetMeters }} 米
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="响应时长">
              <span v-if="d.event?.responseMinutes != null">{{ d.event.responseMinutes }} 分钟到场</span>
              <span v-else class="muted">尚未到场</span>
            </el-descriptions-item>
            <el-descriptions-item label="当前语音内容" :span="2">
              {{ d.event?.voiceContent || '（无语音内容）' }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 操作按钮 -->
        <el-card shadow="never" style="margin-bottom:12px" v-if="d.event?.status !== 'CLOSED'">
          <template #header><span class="section-title">处置操作</span></template>
          <el-space wrap>
            <el-button type="danger" :icon="Search" @click="openTriage"
                       v-if="!d.event?.triageType">① 值班研判</el-button>
            <el-button type="warning" :icon="Phone" @click="openDispatch"
                       v-if="d.event?.triageType && d.event.triageType !== 'FALSE_TRIGGER'">② 转介通知</el-button>
            <el-button type="primary" :icon="House" @click="openOnsite"
                       v-if="d.event?.triageType && d.event.triageType !== 'FALSE_TRIGGER'">③ 上门核实</el-button>
            <el-button type="info" :icon="ChatDotRound" @click="openComm"
                       v-if="d.event?.triageType && d.event.triageType !== 'FALSE_TRIGGER'">家属沟通</el-button>
            <el-button type="success" :icon="Files" @click="openService"
                       v-if="d.event?.triageType && d.event.triageType !== 'FALSE_TRIGGER'">④ 后续服务</el-button>
            <el-button type="danger" plain :icon="CircleCheck" @click="openFollow">
              {{ d.event?.triageType === 'FALSE_TRIGGER' ? '② 回访归档' : '⑤ 回访归档' }}
            </el-button>
            <el-button :icon="EditPen" @click="noteDialog = true">添加备注</el-button>
          </el-space>
        </el-card>

        <!-- 处置记录 -->
        <el-card shadow="never" style="margin-bottom:12px">
          <template #header><span class="section-title">转介与到场（{{ d.dispatches?.length || 0 }}）</span></template>
          <el-empty v-if="!d.dispatches?.length" :image-size="50" description="尚未转介" />
          <el-timeline v-else>
            <el-timeline-item v-for="x in d.dispatches" :key="x.id" :timestamp="fmt(x.notifiedAt)"
                              :type="x.effectiveResponse ? 'success' : 'primary'">
              <el-tag size="small">{{ TARGET_LABEL[x.targetType] }}</el-tag>
              <b style="margin:0 6px">{{ x.targetName }}</b>
              <span class="muted">{{ x.targetPhone }}</span>
              <el-tag size="small" :type="dispatchTag(x.status)" style="margin-left:8px">
                {{ DISPATCH_STATUS[x.status] }}
              </el-tag>
              <el-tag v-if="x.neighborReported" size="small"
                      :type="x.neighborInfoClear ? 'success' : 'danger'" style="margin-left:6px">
                邻居代报·{{ x.neighborInfoClear ? '信息清晰' : '信息不清' }}
              </el-tag>
              <div class="muted" style="margin-top:4px">
                通知人 {{ x.notifiedByName }} · {{ x.notifyChannel }}
                <el-button v-if="x.status !== 'ARRIVED' && x.status !== 'REJECTED'" link type="primary"
                           size="small" style="margin-left:10px"
                           @click="changeDispatch(x.id, 'ACCEPTED')">受理</el-button>
                <el-button v-if="x.status === 'ACCEPTED' || x.status === 'NOTIFIED'" link type="success"
                           size="small" @click="changeDispatch(x.id, 'ARRIVED')">到场</el-button>
                <el-button v-if="x.status !== 'REJECTED' && x.status !== 'ARRIVED'" link type="danger"
                           size="small" @click="changeDispatch(x.id, 'REJECTED')">无法响应</el-button>
              </div>
              <div v-if="x.note" class="muted">{{ x.note }}</div>
            </el-timeline-item>
          </el-timeline>
        </el-card>

        <el-card shadow="never" style="margin-bottom:12px">
          <template #header><span class="section-title">上门核实记录（{{ d.visits?.length || 0 }}）</span></template>
          <el-empty v-if="!d.visits?.length" :image-size="50" description="尚未上门" />
          <div v-for="v in d.visits" :key="v.id" class="record-box">
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="入户方式">
                {{ v.entryMethod }}
                <el-tag v-if="!v.entered" type="danger" size="small">未能入户</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="到场时间">{{ fmt(v.arrivedAt) }}（{{ v.visitorName }}）</el-descriptions-item>
              <el-descriptions-item label="老人状态" :span="2">{{ v.elderStatus }}</el-descriptions-item>
              <el-descriptions-item v-if="v.helpReason" label="求助原因" :span="2">
                <el-tag v-for="r in (v.helpReason||'').split(',').filter(Boolean)" :key="r"
                        size="small" type="warning" effect="plain" style="margin:1px">{{ r }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="到场结果" :span="2">
                <el-tag size="small" type="success">{{ ARRIVE_RESULT_LABEL[v.arriveResult] || v.arriveResult || '—' }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="孤独焦虑" :span="2">
                <el-tag size="small" :type="v.lonelyAnxious ? 'danger' : 'info'">
                  {{ v.lonelyAnxious ? '存在孤独焦虑' : '未发现' }}
                </el-tag>
                <span v-if="v.anxietyNote" class="muted" style="margin-left:6px">{{ v.anxietyNote }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="药物管理" :span="2">
                <el-tag size="small" :type="v.medicationChaos ? 'danger' : 'info'">
                  {{ v.medicationChaos ? '药物管理混乱' : '用药正常' }}
                </el-tag>
                <span v-if="v.medicationNote" class="muted" style="margin-left:6px">{{ v.medicationNote }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="家属参与" :span="2">
                <el-tag size="small" :type="v.familyInvolved ? 'success' : 'info'">
                  {{ v.familyInvolved ? '家属参与处置' : '家属未参与' }}
                </el-tag>
                <span v-if="v.familyInvolvementNote" class="muted" style="margin-left:6px">{{ v.familyInvolvementNote }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="联系家属">
                <el-tag size="small" :type="v.familyContacted ? 'success' : 'info'">
                  {{ v.familyContacted ? '已联系' : '未联系' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="处置判断">
                <el-tag v-if="v.hospitalNeeded" type="danger" size="small">需送医</el-tag>
                <el-tag v-if="v.refuseMedical" type="danger" size="small" effect="plain">拒绝就医</el-tag>
                <el-tag v-if="v.temporaryCareNeeded" type="warning" size="small">需临时照护</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="后续服务建议" :span="2">{{ v.followServiceSuggestions || '—' }}</el-descriptions-item>
              <el-descriptions-item label="家中安全隐患" :span="2">
                <span :style="{ color: v.safetyHazards ? '#f56c6c' : '' }">{{ v.safetyHazards || '无' }}</span>
              </el-descriptions-item>
              <el-descriptions-item v-if="v.photoUrls" label="现场照片" :span="2">
                <el-image v-for="(u, i) in v.photoUrls.split(',').filter(Boolean)" :key="i"
                          :src="u" :preview-src-list="v.photoUrls.split(',')" fit="cover"
                          style="width:88px;height:88px;border-radius:6px;margin-right:8px"
                          preview-teleported />
                <span class="muted">（演示环境为示例路径）</span>
              </el-descriptions-item>
              <el-descriptions-item v-if="v.note" label="备注" :span="2">{{ v.note }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>

        <el-row :gutter="12">
          <el-col :span="12">
            <el-card shadow="never" style="margin-bottom:12px">
              <template #header><span class="section-title">后续服务（{{ d.services?.length || 0 }}）</span></template>
              <el-empty v-if="!d.services?.length" :image-size="40" description="无" />
              <el-table v-else :data="d.services" size="small">
                <el-table-column label="类型" width="100">
                  <template #default="{ row }">{{ SERVICE_LABEL[row.serviceType] }}</template>
                </el-table-column>
                <el-table-column prop="providerName" label="提供方" />
                <el-table-column prop="content" label="内容" show-overflow-tooltip />
              </el-table>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="never" style="margin-bottom:12px">
              <template #header><span class="section-title">家属沟通（{{ d.communications?.length || 0 }}）</span></template>
              <el-empty v-if="!d.communications?.length" :image-size="40" description="无" />
              <div v-for="c in d.communications" :key="c.id" style="margin-bottom:8px">
                <el-tag size="small" :type="c.reached ? 'success' : 'danger'">
                  {{ c.reached ? '已接通' : '未接通' }}
                </el-tag>
                <b style="margin:0 6px">{{ c.contactName }}</b>
                <span class="muted">{{ c.attitude }} · {{ fmt(c.communicatedAt) }}</span>
                <div v-if="c.note" class="muted" style="padding-left:4px">{{ c.note }}</div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <el-card shadow="never" v-if="d.followUps?.length">
          <template #header><span class="section-title">回访记录</span></template>
          <div v-for="f in d.followUps" :key="f.id" class="record-box">
            <el-tag :type="f.result === 'IMPROVED' ? 'success' : f.result === 'WORSENED' ? 'danger' : 'info'">
              {{ FOLLOW_RESULT_LABEL[f.result] }}
            </el-tag>
            <span style="margin:0 8px">{{ f.method }} · 满意度 {{ f.satisfaction || '—' }}/5 · {{ fmt(f.followedUpAt) }}</span>
            <el-tag type="warning" effect="plain" v-if="f.nextCareType">
              下一次关怀：{{ SERVICE_LABEL[f.nextCareType] }} · {{ fmt(f.nextCareAt) }}
            </el-tag>
            <div v-if="f.note" class="muted" style="margin-top:4px">{{ f.note }}</div>
          </div>
        </el-card>
      </el-col>

      <!-- 右：老人画像 + 时间线 -->
      <el-col :span="7">
        <el-card shadow="never" style="margin-bottom:12px">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span class="section-title" style="margin:0">老人照护画像</span>
              <el-link type="primary" @click="$router.push(`/elders/${d.elder?.id}`)">完整档案</el-link>
            </div>
          </template>
          <template v-if="d.elder">
            <div class="elder-head">
              <el-avatar :size="46" style="background:#c45656">{{ d.elder.name?.[0] }}</el-avatar>
              <div>
                <div style="font-weight:700;font-size:16px">
                  {{ d.elder.name }}
                  <el-tag size="small" :type="RISK_LEVEL_TAG[d.elder.riskLevel]" effect="dark">
                    {{ RISK_LEVEL_LABEL[d.elder.riskLevel] }}
                  </el-tag>
                </div>
                <div class="muted">{{ d.elder.gender }} · {{ d.elder.age }}岁 · {{ d.elder.livingStatus }}</div>
              </div>
            </div>
            <el-descriptions :column="1" size="small" border>
              <el-descriptions-item label="基础病">{{ d.elder.chronicDiseases || '无' }}</el-descriptions-item>
              <el-descriptions-item label="用药备注">{{ d.elder.medicalNote || '—' }}</el-descriptions-item>
              <el-descriptions-item label="住址">{{ d.elder.address }}</el-descriptions-item>
              <el-descriptions-item label="门禁信息">
                <span style="color:#b88230">{{ d.elder.doorAccessInfo || '无' }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="安全设施">
                <el-icon :color="d.elder.gasAlarmInstalled ? '#67c23a' : '#f56c6c'">
                  <CircleCheck v-if="d.elder.gasAlarmInstalled" /><CircleClose v-else />
                </el-icon> 燃气报警器
                <el-icon style="margin-left:10px" v-if="d.elder.smokeAlarmInstalled" color="#67c23a"><CircleCheck /></el-icon> 烟感
                <el-icon style="margin-left:10px" v-if="d.elder.sosDeviceInstalled" color="#67c23a"><CircleCheck /></el-icon> SOS设备
              </el-descriptions-item>
              <el-descriptions-item label="最近家属探访">{{ d.elder.lastFamilyVisitDate || '无记录' }}</el-descriptions-item>
              <el-descriptions-item label="医生随访">{{ d.elder.lastDoctorFollowUpDate || '无记录' }}</el-descriptions-item>
              <el-descriptions-item label="随访结论">{{ d.elder.doctorFollowUpConclusion || '—' }}</el-descriptions-item>
              <el-descriptions-item label="家属响应评分">
                <el-rate :model-value="Math.round((d.elder.familyResponsivenessScore||0)/20)" disabled size="small" />
                {{ d.elder.familyResponsivenessScore }}
              </el-descriptions-item>
              <el-descriptions-item label="邻里可信度">
                <el-rate :model-value="Math.round((d.elder.neighborReliabilityScore||0)/20)" disabled size="small" />
                {{ d.elder.neighborReliabilityScore }}
              </el-descriptions-item>
              <el-descriptions-item label="历史误触">{{ d.elder.falseTriggerCount }} 次</el-descriptions-item>
              <el-descriptions-item label="服务建议">
                <el-tag v-if="d.elder.suggestDailyVisit" size="small" type="danger" effect="plain">巡访</el-tag>
                <el-tag v-if="d.elder.suggestMealService" size="small" type="warning" effect="plain">助餐</el-tag>
                <el-tag v-if="d.elder.suggestAgingRetrofit" size="small" type="info" effect="plain">适老化改造</el-tag>
                <el-tag v-if="d.elder.suggestPsychCompanion" size="small" type="success" effect="plain">心理陪伴</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="下次主动关怀">
                {{ d.elder.proactiveCareType || '—' }}
                <span class="muted" v-if="d.elder.nextProactiveCareAt">{{ fmt(d.elder.nextProactiveCareAt) }}</span>
              </el-descriptions-item>
            </el-descriptions>

            <div style="margin-top:10px">
              <div class="muted" style="margin-bottom:4px">紧急联系人</div>
              <div v-for="c in d.contacts" :key="c.id" class="person-row">
                <el-tag size="small" :type="c.primaryContact ? 'danger' : 'info'" effect="plain">
                  {{ c.relationship }}
                </el-tag>
                <b style="margin:0 6px">{{ c.name }}</b>
                <span class="muted">{{ c.phone }}</span>
                <el-tag v-if="c.oftenUnreachable" size="small" type="danger" style="margin-left:auto">长期不接</el-tag>
              </div>
            </div>
            <div style="margin-top:10px">
              <div class="muted" style="margin-bottom:4px">邻里响应人</div>
              <div v-for="n in d.neighbors" :key="n.id" class="person-row">
                <el-icon color="#409eff"><UserFilled /></el-icon>
                <b style="margin:0 6px">{{ n.name }}</b>
                <span class="muted">{{ n.phone }} · {{ n.proximity }}</span>
                <el-tag size="small" type="success" effect="plain" style="margin-left:auto">可信度 {{ n.reliabilityScore }}</el-tag>
              </div>
            </div>
          </template>
        </el-card>

        <el-card shadow="never">
          <template #header><span class="section-title">事件全流程时间线</span></template>
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

    <!-- ===== 研判 ===== -->
    <el-dialog v-model="triageDialog" title="值班社工研判" width="520px">
      <el-form :model="triage" label-width="92px">
        <el-form-item label="研判类型">
          <el-radio-group v-model="triage.triageType">
            <el-radio v-for="(v, k) in TRIAGE_LABEL" :key="k" :value="k">{{ v }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="严重程度">
          <el-radio-group v-model="triage.severity">
            <el-radio v-for="(v, k) in SEVERITY_LABEL" :key="k" :value="k">{{ v }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="研判说明">
          <el-input v-model="triage.triageNote" type="textarea" :rows="3"
                    placeholder="结合语音内容、档案基础病、历史求助与误触记录综合判断" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="triageDialog = false">取消</el-button>
        <el-button type="danger" @click="submitTriage">提交研判</el-button>
      </template>
    </el-dialog>

    <!-- ===== 转介 ===== -->
    <el-dialog v-model="dispatchDialog" title="转介 / 通知" width="540px">
      <el-form :model="dispatch" label-width="100px">
        <el-form-item label="通知对象">
          <el-radio-group v-model="dispatch.targetType" @change="onTargetChange">
            <el-radio v-for="(v, k) in TARGET_LABEL" :key="k" :value="k">{{ v }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="dispatch.targetName" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="dispatch.targetPhone" />
        </el-form-item>
        <el-form-item label="通知方式">
          <el-select v-model="dispatch.notifyChannel" style="width:100%">
            <el-option label="电话" value="电话" />
            <el-option label="平台推送" value="平台推送" />
            <el-option label="短信" value="短信" />
          </el-select>
        </el-form-item>
        <el-form-item label="邻居代报">
          <el-switch v-model="dispatch.neighborReported" />
        </el-form-item>
        <el-form-item v-if="dispatch.neighborReported" label="代报信息">
          <el-radio-group v-model="dispatch.neighborInfoClear">
            <el-radio :value="true">信息清晰</el-radio>
            <el-radio :value="false">信息不清</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="dispatch.note" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dispatchDialog = false">取消</el-button>
        <el-button type="warning" @click="submitDispatch">发出通知</el-button>
      </template>
    </el-dialog>

    <!-- ===== 上门 ===== -->
    <el-dialog v-model="onsiteDialog" title="上门核实记录" width="640px" top="5vh">
      <el-form :model="onsite" label-width="110px">
        <el-form-item label="入户方式">
          <el-select v-model="onsite.entryMethod" style="width:100%">
            <el-option v-for="m in ENTRY_METHODS" :key="m" :label="m" :value="m" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否入户">
          <el-switch v-model="onsite.entered" active-text="已入户" inactive-text="未能入户" />
        </el-form-item>
        <el-form-item label="现场照片">
          <el-upload :action="uploadUrl" :headers="uploadHeaders" name="file"
                     :on-success="onUploaded" :show-file-list="true" accept="image/*">
            <el-button :icon="Picture">上传现场照片</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="老人状态">
          <el-select v-model="onsite.elderStatus" style="width:100%" allow-create filterable>
            <el-option v-for="s in ELDER_STATUS_OPTIONS" :key="s" :label="s" :value="s" />
          </el-select>
        </el-form-item>
        <el-form-item label="求助原因">
          <el-checkbox-group v-model="onsite.reasons">
            <el-checkbox v-for="r in HELP_REASON_OPTIONS" :key="r" :value="r">{{ r }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="到场结果">
          <el-select v-model="onsite.arriveResult" style="width:100%" clearable placeholder="选择本次到场处置结果">
            <el-option v-for="(v, k) in ARRIVE_RESULT_LABEL" :key="k" :label="v" :value="k" />
          </el-select>
        </el-form-item>
        <el-row>
          <el-col :span="12"><el-form-item label="孤独焦虑"><el-switch v-model="onsite.lonelyAnxious" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="药物混乱"><el-switch v-model="onsite.medicationChaos" /></el-form-item></el-col>
        </el-row>
        <el-form-item v-if="onsite.lonelyAnxious" label="焦虑表现">
          <el-input v-model="onsite.anxietyNote" placeholder="如：反复按键求陪伴、夜间恐惧、情绪低落、睡眠差" />
        </el-form-item>
        <el-form-item v-if="onsite.medicationChaos" label="用药情况">
          <el-input v-model="onsite.medicationNote" placeholder="如：漏服降压药、早晚药混放、发现过期药" />
        </el-form-item>
        <el-row>
          <el-col :span="12"><el-form-item label="家属参与"><el-switch v-model="onsite.familyInvolved" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="联系家属"><el-switch v-model="onsite.familyContacted" /></el-form-item></el-col>
        </el-row>
        <el-form-item v-if="onsite.familyInvolved" label="家属参与情况">
          <el-input v-model="onsite.familyInvolvementNote" placeholder="如：儿子电话共同安抚/到场陪同送医" />
        </el-form-item>
        <el-row>
          <el-col :span="12"><el-form-item label="需要送医"><el-switch v-model="onsite.hospitalNeeded" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="拒绝就医"><el-switch v-model="onsite.refuseMedical" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="临时照护"><el-switch v-model="onsite.temporaryCareNeeded" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="后续服务建议">
          <el-checkbox-group v-model="onsite.suggestions">
            <el-checkbox value="日常巡访">日常巡访</el-checkbox>
            <el-checkbox value="助餐">助餐</el-checkbox>
            <el-checkbox value="适老化改造">适老化改造</el-checkbox>
            <el-checkbox value="心理陪伴">心理陪伴</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="家中安全隐患">
          <el-input v-model="onsite.safetyHazards" type="textarea" :rows="2"
                    placeholder="如：地滑、电线老化、燃气隐患、照明不足、无扶手等" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="onsite.note" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="onsiteDialog = false">取消</el-button>
        <el-button type="primary" @click="submitOnsite">提交上门记录</el-button>
      </template>
    </el-dialog>

    <!-- ===== 家属沟通 ===== -->
    <el-dialog v-model="commDialog" title="家属沟通记录" width="500px">
      <el-form :model="comm" label-width="92px">
        <el-form-item label="联系人">
          <el-select v-model="comm.contactId" style="width:100%" @change="onContactChange">
            <el-option v-for="c in d.contacts" :key="c.id"
                       :label="`${c.relationship} ${c.name} ${c.phone}`" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="姓名"><el-input v-model="comm.contactName" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="comm.contactPhone" /></el-form-item>
        <el-form-item label="方式">
          <el-select v-model="comm.channel" style="width:100%">
            <el-option label="电话" value="电话" />
            <el-option label="短信" value="短信" />
            <el-option label="微信" value="微信" />
            <el-option label="当面" value="当面" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否接通">
          <el-switch v-model="comm.reached" active-text="已接通" inactive-text="未接通" />
        </el-form-item>
        <el-form-item v-if="comm.reached" label="家属态度">
          <el-radio-group v-model="comm.attitude">
            <el-radio value="积极配合">积极配合</el-radio>
            <el-radio value="一般">一般</el-radio>
            <el-radio value="推诿拒绝">推诿拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="comm.note" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="commDialog = false">取消</el-button>
        <el-button type="info" @click="submitComm">保存沟通记录</el-button>
      </template>
    </el-dialog>

    <!-- ===== 后续服务 ===== -->
    <el-dialog v-model="serviceDialog" title="后续服务安排" width="500px">
      <el-form :model="service" label-width="92px">
        <el-form-item label="服务类型">
          <el-select v-model="service.serviceType" style="width:100%">
            <el-option v-for="(v, k) in SERVICE_LABEL" :key="k" :label="v" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="提供方"><el-input v-model="service.providerName" placeholder="如 康泰养老服务中心 / 助餐点" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="service.providerPhone" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="service.status">
            <el-radio value="PENDING">待服务</el-radio>
            <el-radio value="IN_PROGRESS">进行中</el-radio>
            <el-radio value="DONE">已完成</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="服务内容"><el-input v-model="service.content" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="serviceDialog = false">取消</el-button>
        <el-button type="success" @click="submitService">保存服务</el-button>
      </template>
    </el-dialog>

    <!-- ===== 回访归档 ===== -->
    <el-dialog v-model="followDialog" title="回访并归档（联动更新老人画像与关怀计划）" width="540px">
      <el-alert type="info" :closable="false" style="margin-bottom:12px"
                title="归档后将更新老人风险等级、家属响应表现、邻里协助可信度，并生成下一次主动关怀计划" />
      <el-form :model="follow" label-width="100px">
        <el-form-item label="回访方式">
          <el-radio-group v-model="follow.method">
            <el-radio value="电话回访">电话回访</el-radio>
            <el-radio value="上门回访">上门回访</el-radio>
            <el-radio value="小程序">小程序</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="回访结果">
          <el-radio-group v-model="follow.result">
            <el-radio v-for="(v, k) in FOLLOW_RESULT_LABEL" :key="k" :value="k">{{ v }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="满意度">
          <el-rate v-model="follow.satisfaction" />
        </el-form-item>
        <el-form-item label="回访备注">
          <el-input v-model="follow.note" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="关怀类型">
          <el-select v-model="follow.nextCareType" clearable placeholder="留空则系统按风险自动排期" style="width:100%">
            <el-option v-for="(v, k) in SERVICE_LABEL" :key="k" :label="v" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="关怀时间">
          <el-date-picker v-model="follow.nextCareAt" type="datetime" placeholder="留空则自动排期"
                          value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="followDialog = false">取消</el-button>
        <el-button type="danger" @click="submitFollow">完成回访并归档</el-button>
      </template>
    </el-dialog>

    <!-- 风险标记 / 备注 -->
    <el-dialog v-model="flagDialog" title="补充风险标记" width="420px">
      <el-radio-group v-model="manualFlag" style="display:flex;flex-direction:column;gap:8px">
        <el-radio v-for="(v, k) in RISK_LABEL" :key="k" :value="k">{{ v }}</el-radio>
      </el-radio-group>
      <template #footer>
        <el-button @click="flagDialog = false">取消</el-button>
        <el-button type="danger" @click="submitFlag">添加标记</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="noteDialog" title="添加事件备注" width="440px">
      <el-input v-model="noteContent" type="textarea" :rows="3" placeholder="其他需要留痕的信息" />
      <template #footer>
        <el-button @click="noteDialog = false">取消</el-button>
        <el-button type="primary" @click="submitNote">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Search, Phone, House, ChatDotRound, Files, CircleCheck, CircleClose,
  EditPen, Picture, UserFilled
} from '@element-plus/icons-vue'
import api from '../api'
import {
  SOURCE_LABEL, TRIAGE_LABEL, SEVERITY_LABEL, SEVERITY_TAG, TARGET_LABEL,
  SERVICE_LABEL, FOLLOW_RESULT_LABEL, RISK_LABEL, RISK_TAG, STATUS_LABEL, STATUS_TAG,
  RISK_LEVEL_LABEL, RISK_LEVEL_TAG, ENTRY_METHODS, ELDER_STATUS_OPTIONS,
  HELP_REASON_OPTIONS, ARRIVE_RESULT_LABEL
} from '../api/dict'

const route = useRoute()
const d = ref({})
const loading = ref(false)

const DISPATCH_STATUS = { NOTIFIED: '已通知', ACCEPTED: '已受理', ARRIVED: '已到场', REJECTED: '无法响应' }
const LOG_LABEL = {
  HELP: '求助', TRIAGE: '研判', DISPATCH: '转介', ONSITE: '上门', SERVICE: '服务',
  FAMILY_COMM: '家属沟通', FOLLOW_UP: '回访', CLOSE: '归档', SYSTEM: '系统', NOTE: '备注'
}
const LOG_TYPE = {
  HELP: 'danger', TRIAGE: 'warning', DISPATCH: 'warning', ONSITE: 'primary',
  SERVICE: 'success', FAMILY_COMM: 'info', FOLLOW_UP: 'success', CLOSE: 'success',
  SYSTEM: 'info', NOTE: 'info'
}

const flags = computed(() => (d.value.event?.riskFlags || '').split(',').filter(Boolean))
const uploadUrl = '/api/files/upload'
const uploadHeaders = { Authorization: `Bearer ${localStorage.getItem('token')}` }

async function load() {
  loading.value = true
  try {
    d.value = await api.get(`/events/${route.params.id}`)
  } finally {
    loading.value = false
  }
}
function fmt(s) { return s ? String(s).replace('T', ' ').substring(0, 16) : '' }
function dispatchTag(s) {
  return { NOTIFIED: 'info', ACCEPTED: 'warning', ARRIVED: 'success', REJECTED: 'danger' }[s] || 'info'
}

/* ---- 表单状态 ---- */
const triageDialog = ref(false)
const triage = reactive({ triageType: 'BODY_DISCOMFORT', severity: 'MEDIUM', triageNote: '' })
function openTriage() { Object.assign(triage, { triageType: 'BODY_DISCOMFORT', severity: 'MEDIUM', triageNote: '' }); triageDialog.value = true }
async function submitTriage() {
  await api.post(`/events/${route.params.id}/triage`, triage)
  ElMessage.success('研判已提交')
  triageDialog.value = false
  load()
}

const dispatchDialog = ref(false)
const dispatch = reactive({ targetType: 'GRID_WORKER', targetName: '', targetPhone: '', notifyChannel: '电话', neighborReported: false, neighborInfoClear: true, note: '' })
function openDispatch() {
  Object.assign(dispatch, { targetType: 'GRID_WORKER', targetName: '', targetPhone: '', notifyChannel: '电话', neighborReported: false, neighborInfoClear: true, note: '' })
  dispatchDialog.value = true
}
function onTargetChange(t) {
  if (t === 'NEIGHBOR_VOLUNTEER' && d.value.neighbors?.[0]) {
    dispatch.targetName = d.value.neighbors[0].name
    dispatch.targetPhone = d.value.neighbors[0].phone
  }
}
async function submitDispatch() {
  if (!dispatch.targetName) { ElMessage.warning('请填写姓名'); return }
  await api.post(`/events/${route.params.id}/dispatches`, dispatch)
  ElMessage.success('已通知')
  dispatchDialog.value = false
  load()
}
async function changeDispatch(id, status) {
  await api.put(`/events/dispatches/${id}/status`, null, { params: { status } })
  load()
}

const onsiteDialog = ref(false)
const onsite = reactive({})
function openOnsite() {
  Object.assign(onsite, {
    entryMethod: d.value.elder?.doorAccessInfo ? '智能锁授权' : '钥匙开门',
    entered: true, photoUrls: '', elderStatus: '清醒正常',
    reasons: [], arriveResult: null,
    lonelyAnxious: false, anxietyNote: '',
    medicationChaos: false, medicationNote: '',
    familyInvolved: false, familyInvolvementNote: '',
    familyContacted: false, hospitalNeeded: false, refuseMedical: false,
    temporaryCareNeeded: false, suggestions: [], safetyHazards: '', note: ''
  })
  onsiteDialog.value = true
}
function onUploaded(res) {
  if (res.code === 0) {
    onsite.photoUrls = onsite.photoUrls ? onsite.photoUrls + ',' + res.data.url : res.data.url
    ElMessage.success('照片已上传')
  }
}
async function submitOnsite() {
  if (!onsite.elderStatus) { ElMessage.warning('请填写老人状态'); return }
  await api.post(`/events/${route.params.id}/onsite`, {
    ...onsite,
    helpReason: onsite.reasons.join(','),
    followServiceSuggestions: onsite.suggestions.join(',')
  })
  ElMessage.success('上门记录已提交')
  onsiteDialog.value = false
  load()
}

const commDialog = ref(false)
const comm = reactive({})
function openComm() {
  const primary = d.value.contacts?.find(c => c.primaryContact) || d.value.contacts?.[0]
  Object.assign(comm, {
    contactId: primary?.id || null, contactName: primary?.name || '',
    contactPhone: primary?.phone || '', channel: '电话', reached: true, attitude: '积极配合', note: ''
  })
  commDialog.value = true
}
function onContactChange(id) {
  const c = d.value.contacts.find(x => x.id === id)
  if (c) { comm.contactName = c.name; comm.contactPhone = c.phone }
}
async function submitComm() {
  if (!comm.contactName) { ElMessage.warning('请填写联系人'); return }
  await api.post(`/events/${route.params.id}/family-communications`, comm)
  ElMessage.success('沟通记录已保存')
  commDialog.value = false
  load()
}

const serviceDialog = ref(false)
const service = reactive({})
function openService() {
  Object.assign(service, { serviceType: 'DAILY_VISIT', providerName: '', providerPhone: '', status: 'DONE', content: '' })
  serviceDialog.value = true
}
async function submitService() {
  if (!service.providerName) { ElMessage.warning('请填写提供方'); return }
  await api.post(`/events/${route.params.id}/services`, service)
  ElMessage.success('服务已保存')
  serviceDialog.value = false
  load()
}

const followDialog = ref(false)
const follow = reactive({})
function openFollow() {
  Object.assign(follow, { result: 'STABLE', method: '电话回访', satisfaction: 4, note: '', nextCareType: null, nextCareAt: null })
  followDialog.value = true
}
async function submitFollow() {
  await ElMessageBox.confirm('确认完成回访并归档？归档将联动更新老人画像', '归档确认', { type: 'warning' })
  await api.post(`/events/${route.params.id}/follow-up`, follow)
  ElMessage.success('事件已归档，画像与关怀计划已更新')
  followDialog.value = false
  load()
}

const flagDialog = ref(false)
const manualFlag = ref('CANNOT_OPEN_DOOR')
async function submitFlag() {
  await api.post(`/events/${route.params.id}/flags`, null, { params: { flag: manualFlag.value } })
  flagDialog.value = false
  load()
}
const noteDialog = ref(false)
const noteContent = ref('')
async function submitNote() {
  if (!noteContent.value) return
  await api.post(`/events/${route.params.id}/notes`, { content: noteContent.value })
  noteDialog.value = false
  noteContent.value = ''
  load()
}

function onBus() { if (d.value.event && d.value.event.status !== 'CLOSED') load() }
onMounted(() => {
  load()
  window.addEventListener('workbench-reload', onBus)
})
</script>

<style scoped>
.record-box { margin-bottom: 10px; }
.elder-head { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
.person-row { display: flex; align-items: center; gap: 2px; padding: 5px 0; font-size: 13px; border-bottom: 1px dashed #ebeef5; }
</style>
