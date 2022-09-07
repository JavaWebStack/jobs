<template>
  <ul class="nav nav-tabs">
    <li class="nav-item" v-for="t in tabs" :key="t.key">
      <a class="nav-link" :class="{ active: modelValue === t.key }" @click="updateValue(t.key)" style="cursor: pointer; user-select: none;">
        <slot :name="'item'" :item="t">
          {{ t.text }}
        </slot>
      </a>
    </li>
  </ul>
</template>

<script>
export default {
  name: "BSTab.vue",
  model: 'value',
  props: {
    tabs: {
      type: Array
    },
    modelValue: {
      type: String
    }
  },
  methods: {
    updateValue(k) {
      this.$emit('update:modelValue', k)
    }
  }
}
</script>