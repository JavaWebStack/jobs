<template>
    <table :class="{ table: true, ['table-' + variant]: variant }" v-bind="$attrs">
        <thead>
            <tr>
                <th v-for="(header, i) in headers" :key="i">
                    <slot :name="'header.' + (typeof header == 'string' ? header : header.key)" :header="header">
                        {{ typeof header == 'string' ? header : header.text }}
                    </slot>
                </th>
            </tr>
        </thead>
        <tbody>
            <tr v-for="(entry, i) in items" :key="i" :class="selectVariant(entry)">
                <template v-if="Array.isArray(entry)">
                    <td v-for="(value, i2) in entry" :key="i2">
                        {{ value }}
                    </td>
                </template>
                <template v-else>
                    <td v-for="(header, i2) in headers" :key="i2">
                        <slot :name="'item.' + (typeof header == 'string' ? header : header.key)" :item="entry">
                            {{ entry[typeof header == 'string' ? header : header.key] }}
                        </slot>
                    </td>
                </template>
            </tr>
        </tbody>
    </table>
</template>

<script>
export default {
    props: {
        variant: {
            type: String,
            default: ''
        },
        variantSelector: {
            type: [String, Function],
            default: 'variant'
        },
        headers: {
            type: Array,
            default: () => []
        },
        items: {
            type: Array,
            default: () => []
        }
    },
    methods: {
        selectVariant(item) {
            let variant = ''
            if(typeof this.variantSelector == 'string') {
                variant = item[this.variantSelector]
            } else {
                variant = this.variantSelector(item)
            }
            return { ['table-' + variant ]: variant }
        }
    }
}
</script>