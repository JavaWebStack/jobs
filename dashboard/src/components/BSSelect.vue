<template>
    <div class="form-group">
        <label class="form-label" v-if="$slots.default">
            <slot />
        </label>
        <div class="input-group" v-if="$slots.prepend || $slots.append">
            <span class="input-group-text" v-if="$slots.prepend"><slot name="prepend" /></span>
            <select class="form-select" :size="size" v-bind="filter($attrs, 'options', 'input')" :value="modelValue" @input="e => $emit('update:modelValue', e.target.value)">
                <template v-for="(option, i) in options" :key="i">
                    <option
                        v-if="typeof option == 'string' || !('$options' in option)"
                        :disabled="typeof option == 'string' ? false : option.disabled"
                        :value="typeof option == 'string' ? option : option.value"
                    >
                        {{ typeof option == 'string' ? option : option.text }}
                    </option>
                    <optgroup v-else :disabled="option.disabled" :label="option.text">
                        <option
                            v-for="(option2, i2) in (option.$options || [])" :key="i2"
                            :disabled="typeof option2 == 'string' ? false : option2.disabled"
                            :value="typeof option2 == 'string' ? option2 : option2.value"
                        >
                            {{ typeof option2 == 'string' ? option2 : option2.text }}
                        </option>
                    </optgroup>
                </template>
            </select>
            <span class="input-group-text" v-if="$slots.append"><slot name="append" /></span>
        </div>
        <select v-else class="form-select" :size="size" v-bind="filter($attrs, 'options', 'input')" :value="modelValue" @input="e => $emit('update:modelValue', e.target.value)">
            <template v-for="(option, i) in options" :key="i">
                <option
                    v-if="typeof option == 'string' || !('$options' in option)"
                     :disabled="typeof option == 'string' ? false : option.disabled"
                    :value="typeof option == 'string' ? option : option.value"
                >
                    {{ typeof option == 'string' ? option : option.text }}
                </option>
                <optgroup v-else :disabled="option.disabled" :label="option.text">
                    <option
                        v-for="(option2, i2) in (option.$options || [])" :key="i2"
                        :disabled="typeof option2 == 'string' ? false : option2.disabled"
                        :value="typeof option2 == 'string' ? option2 : option2.value"
                    >
                        {{ typeof option2 == 'string' ? option2 : option2.text }}
                    </option>
                </optgroup>
            </template>
        </select>
    </div>
</template>

<script>
export default {
    props: {
        modelValue: {
            type: [ String, Number ],
            default: ''
        },
        size: {
            type: [ String, Number ],
            default: ''
        },
        options: {
            type: Array,
            default: () => []
        }
    },
    methods: {
        filter(listeners, key) {
            listeners = { ...listeners }
            delete listeners[key]
            return listeners
        }
    }
}
</script>