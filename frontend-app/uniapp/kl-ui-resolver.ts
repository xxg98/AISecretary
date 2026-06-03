import type { ComponentResolver } from '@uni-helper/vite-plugin-uni-components'
import { kebabCase } from '@uni-helper/vite-plugin-uni-components'

/**
 * npm 安装的 Kl UI 组件在 H5 端需通过 vite-plugin-uni-components 解析，
 * 才能正确挂载组件样式。
 */
export function KlResolver(): ComponentResolver {
  return {
    type: 'component',
    resolve: (name: string) => {
      if (name.match(/^Kl[A-Z]/)) {
        const compName = kebabCase(name)
        return {
          name,
          from: `@/components/${compName}/${compName}.vue`,
        }
      }
      if (name.startsWith('kl-')) {
        return {
          name,
          from: `@/components/${name}/${name}.vue`,
        }
      }
    },
  }
}
