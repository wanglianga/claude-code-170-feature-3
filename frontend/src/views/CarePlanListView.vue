<template>
  <div>
    <el-card shadow="never">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span class="section-title" style="margin:0">长期照护方案跟踪</span>
          <el-radio-group v-model="statusFilter" size="small" @change="load">
            <el-radio-button value="ACTIVE">生效观察中</el-radio-button>
            <el-radio-button value="DRAFT">待生效</el-radio-button>
            <el-radio-button value="ADJUSTED">已调整</el-radio-button>
            <el-radio-button value="ENDED">已结束</el-radio-button>
            <el-radio-button value="">全部</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <el-table :data="rows" v-loading="loading" stripe
                @row-click="r => $router.push(`/care-plans/${r.plan.id}`)" style="cursor:pointer">
        <el-table-column label="方案编号" width="140">
          <template #default="{ row }">{{ row.plan.planNo }}</template>
        </el-table-column>
        <el-table-column label="老人" width="100">
          <template #default="{ row }">
            <b>{{ row.elderName }}</b>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="PLAN_STATUS_TAG[row.plan.status]" effect="dark">
              {{ PLAN_STATUS_LABEL[row.plan.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="夜间求助（前同等→生效以来）" width="210">
          <template #default="{ row }">
            <template v-if="row.obs?.active">
              <span>{{ row.obs.nightHelp?.beforeSameWindow }} →</span>
              <b :style="{ color: row.obs.nightDecreased ? '#67c23a' : '#f56c6c', marginLeft: '6px' }">
                {{ row.obs.nightHelp?.current }}
              </b>
              <el-tag size="small" :type="row.obs.nightDecreased ? 'success' : 'danger'" style="margin-left:6px">
                {{ row.obs.nightDecreased ? '已下降' : '未下降' }}
              </el-tag>
            </template>
            <span v-else class="muted">未生效</span>
          </template>
        </el-table-column>
        <el-table-column label="助餐签到" width="90">
          <template #default="{ row }">{{ row.obs?.mealCheckin?.current ?? '—' }}</template>
        </el-table-column>
        <el-table-column label="药盒确认/漏服" width="110">
          <template #default="{ row }">
            <span v-if="row.obs?.active">{{ row.obs.medication?.confirmed }} / {{ row.obs.medication?.missed }}</span>
            <span v-else class="muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="家属接听率" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.obs?.active" size="small"
                    :type="(row.obs.familyAnswer?.answerRate ?? 0) >= 50 ? 'success' : 'danger'">
              {{ row.obs.familyAnswer?.answerRate ?? 0 }}%
            </el-tag>
            <span v-else class="muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="观察结论" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.obs?.noImprovement" type="danger" effect="dark" size="small">建议会商</el-tag>
            <el-tag v-else-if="row.obs?.improved" type="success" effect="plain" size="small">见效中</el-tag>
            <span v-else class="muted">观察中</span>
          </template>
        </el-table-column>
        <el-table-column label="生效/发起时间" min-width="150">
          <template #default="{ row }">
            {{ row.plan.effectiveAt ? fmt(row.plan.effectiveAt) : fmt(row.plan.initiatedAt) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import api from '../api'
import { PLAN_STATUS_LABEL, PLAN_STATUS_TAG } from '../api/dict'

const plans = ref([])
const rows = ref([])
const loading = ref(false)
const statusFilter = ref('ACTIVE')

async function load() {
  loading.value = true
  try {
    const params = {}
    if (statusFilter.value) params.status = statusFilter.value
    plans.value = await api.get('/care-plans', params)
    // 逐个方案取最新观察（方案数量少，直接并发）
    rows.value = await Promise.all(plans.value.map(async p => {
      let obs = null
      if (p.status === 'ACTIVE' || p.status === 'ADJUSTED') {
        try { obs = await api.get(`/care-plans/${p.id}/observation`) } catch { obs = null }
      }
      const detail = await api.get(`/care-plans/${p.id}`)
      return { plan: p, obs, elderName: detail.elder?.name }
    }))
  } finally {
    loading.value = false
  }
}

function fmt(s) { return s ? String(s).replace('T', ' ').substring(0, 16) : '' }
onMounted(load)
</script>
