<template>

  <GTabs class="level-2" :basePath="'/help'" :tabs="[
    { id: 'general', title: 'General' },
    { id: 'evaluation', title: 'Detailed Evaluation Help' },
    { id: 'formats', title: 'Formats' },
  ]" />

</template>

<script lang='ts'>
import { defineComponent } from 'vue'
import { GTabs } from '@/components'

export default defineComponent({
  name: "HelpView",
  components: {
    GTabs
  }
})
</script>
