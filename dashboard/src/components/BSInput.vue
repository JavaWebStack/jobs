<template>
    <div class="form-group">
        <div class="form-floating" v-if="float">
            <input class="form-control" v-bind="filter($attrs, 'input')" :value="modelValue" @input="e => $emit('update:modelValue', e.target.value)" :placeholder="placeholder" :disabled="disabled" :type="type">
            <label class="form-label" v-if="$slots.default">
                <slot />
            </label>
        </div>
        <template v-else>
            <label class="form-label" v-if="$slots.default">
                <slot />
            </label>
            <div class="input-group" v-if="$slots.prepend || $slots.append || $slots.appendRaw">
                <span class="input-group-text" v-if="$slots.prepend"><slot name="prepend" /></span>
                <input class="form-control" v-bind="filter($attrs, 'input')" :value="modelValue" @input="e => $emit('update:modelValue', e.target.value)" :placeholder="placeholder" :disabled="disabled" :type="type">
                <span class="input-group-text" v-if="$slots.append"><slot name="append" /></span>
                <slot name="appendRaw" />
            </div>
            <input v-else class="form-control" v-bind="filter($attrs, 'input')" :value="modelValue" @input="e => $emit('update:modelValue', e.target.value)" :placeholder="placeholder" :disabled="disabled" :type="type">
        </template>
    </div>
</template>

<script>
export default {
    props: {
        float: {
            type: Boolean,
            value: false
        },
        modelValue: {
            type: [ String, Number ]
        },
        type: {
            type: String,
            default: 'text'
        },
        disabled: {
            type: [String, Boolean],
            default: false
        },
        placeholder: {
            type: String,
            default: ''
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