<template>
  <div>
    <el-card shadow="never">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span class="section-title" style="margin:0">长期照护方案</span>
          <el-radio-group v-model="filter" size="small" @change="load">
            <el-radio-button value="">全部</el-radio-button>
            <el-radio-button value="ACTIVE">生效观察中</el-radio-button>
            <el-radio-button value="DRAFT">草案待生效</el-radio-button>
            <el-radio-button value="COMPLETED">观察期结束</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <el-table :data="plans" v-loading="loading" stripe
                @row-click="r => $router.push(`/care-plans/${r.id}`)" style="cursor:pointer">
        <el-table-column prop="planNo" label="方案编号" width="135" />
        <el-table-column label="老人" width="100">
          <template #default="{ row }">{{ row.elder?.name }}</template>
        </el-table-column>
        <el-table-column prop="planName" label="方案名称" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="115">
          <template #default="{ row }">
            <el-tag size="small" :type="PLAN_STATUS_TAG[row.status]">{{ PLAN_STATUS_LABEL[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="观察周期" width="120">
          <template #default="{ row }">{{ row.observeDays }} 天</template>
        </el-table-column>
        <el-table-column label="夜间求助基线→当前" width="160">
          <template #default="{ row }">
            <span class="muted">{{ row.baselineNightCount ?? '—' }} 次</span>
            <el-icon v-if="row.status==='ACTIVE'"><ArrowRightBold /></el-icon>
            <el-tag v-if="row.lastReviewResult" size="small"
                    :type="VERDICT_TAG[row.lastReviewResult]">{{ VERDICT_LABEL[row.lastReviewResult] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="生效时间" width="160">
          <template #default="{ row }">{{ fmt(row.effectiveAt) || '未生效' }}</template>
        </el-table-column>
        <el-table-column label="制定社工" width="100" prop="createdByName" />
      </el-table>
      <el-empty v-if="!loading && !plans.length" description="暂无照护方案；可在老人画像页对频繁夜间求助老人发起评估并制定方案" />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowRightBold } from '@element-plus/icons-vue'
import api from '../api'
import { PLAN_STATUS_LABEL, PLAN_STATUS_TAG, VERDICT_LABEL, VERDICT_TAG } from '../api/dict'

const route = useRoute()
const plans = ref([])
const loading = ref(false)
const filter = ref('')

async function load() {
  loading.value = true
  try {
    const params = {}
    if (filter.value) params.status = filter.value
    if (route.query.elderId) params.elderId = route.query.elderId
    plans.value = await api.get('/care-plans', { params })
  } finally {
    loading.value = false
  }
}
function fmt(s) { return s ? String(s).replace('T', ' ').substring(0, 16) : '' }
watch(() => route.query.elderId, load)
onMounted(load)
</script>
