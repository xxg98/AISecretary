# dh-choose-file

Android / iOS / HarmonyOS 原生文件选择插件，支持按类型过滤音频、视频、图片、文档等文件，也支持按扩展名精确过滤。

## 使用

```typescript
import { chooseFile, readTextFile } from '@/uni_modules/dh-choose-file'

// 按文件类型过滤
chooseFile({
  fileTypes: ['audio'],
  success: (res) => {
    const file = res.files[0]
    console.log('文件路径:', file.path)
    console.log('文件名:', file.name)
    console.log('文件大小:', file.size, 'bytes')
    console.log('MIME 类型:', file.mimeType)
    console.log('扩展名:', file.extension)
    console.log('时长:', file.duration, 'ms') // 音频/视频有效，其他为 0
  },
  fail: (err) => {
    if (err.errCode === 8020001) {
      console.log('用户取消选择')
    } else {
      console.error('选择失败:', err.errMsg)
    }
  },
})

// 多选（最多 3 个），同时支持音频和视频
chooseFile({
  count: 3,
  fileTypes: ['audio', 'video'],
  success: (res) => {
    for (const file of res.files) {
      console.log(file.name, file.path)
    }
  },
})

// 按扩展名精确过滤（优先级高于 fileTypes）
chooseFile({
  extensions: ['mp3', 'wav', 'flac'],
  success: (res) => { ... },
})

// 选择所有类型文件
chooseFile({
  fileTypes: ['all'],
  success: (res) => { ... },
})

// 选择文本文件后读取内容
chooseFile({
  extensions: ['txt', 'json', 'xml', 'kml'],
  success: (res) => {
    const content = readTextFile(res.files[0].path)
    console.log('文件内容:', content)
  },
})
```

## 参数说明

### ChooseFileOptions

| 参数       | 类型     | 默认值    | 说明                                           |
| ---------- | -------- | --------- | ---------------------------------------------- |
| count      | number   | `1`       | 最多选择文件数量                               |
| fileTypes  | string[] | `['all']` | 文件类型过滤，见下表                           |
| extensions | string[] | -         | 扩展名白名单（不含点），优先级高于 `fileTypes` |
| success    | function | -         | 成功回调                                       |
| fail       | function | -         | 失败回调                                       |
| complete   | function | -         | 完成回调（无论成功或失败均触发）               |

### fileTypes 支持的值

| 值          | 说明                 | 示例扩展名                                 |
| ----------- | -------------------- | ------------------------------------------ |
| `'all'`     | 所有文件（默认）     | -                                          |
| `'image'`   | 图片                 | jpg / png / gif / webp / heic 等           |
| `'video'`   | 视频                 | mp4 / mov / avi / mkv 等                   |
| `'audio'`   | 音频                 | mp3 / wav / aac / m4a / flac 等            |
| `'doc'`     | 文档                 | pdf / doc / docx / xls / xlsx / ppt / pptx |
| `'text'`    | 文本                 | txt / json / xml / md / csv / html 等      |
| `'archive'` | 压缩包               | zip / rar / 7z / tar / gz 等               |
| `'file'`    | 文档 + 文本 + 压缩包 | 以上三类合并                               |

可传多个值，如 `['audio', 'video']` 表示同时显示音频和视频文件。

> `extensions` 优先级高于 `fileTypes`：若同时传入 `extensions`，`fileTypes` 不生效。

### ChooseFileItem（文件信息）

| 字段      | 类型   | 说明                                                                            |
| --------- | ------ | ------------------------------------------------------------------------------- |
| path      | string | 文件本地路径（`file://` 协议，可直接读取）                                      |
| name      | string | 文件名（含扩展名）                                                              |
| size      | number | 文件大小（字节）                                                                |
| mimeType  | string | MIME 类型，如 `"audio/mpeg"`                                                    |
| extension | string | 小写扩展名（不含点），如 `"mp3"`                                                |
| duration  | number | 媒体时长（毫秒），音频/视频有效，其他文件为 `0`；HarmonyOS 暂不支持，始终为 `0` |

## readTextFile

同步读取本地文本文件内容。

```typescript
readTextFile(filePath: string): string
```

| 参数     | 类型   | 说明                                                |
| -------- | ------ | --------------------------------------------------- |
| filePath | string | 文件路径，支持 `file://` 协议或绝对路径             |
| 返回值   | string | 文件文本内容；文件不存在或读取失败返回空字符串 `''` |

> 该函数为同步操作，大文件建议配合 loading 提示。

## 错误码

| 错误码  | 说明                           |
| ------- | ------------------------------ |
| 8020001 | 用户取消选择                   |
| 8020002 | 存储读取权限被拒绝             |
| 8020003 | 文件读取失败（复制过程中出错） |
| 8020004 | 初始化或运行时失败             |

## 权限配置

三个平台均**无需声明任何权限**。

| 平台      | 原因                                                                                                                                                      |
| --------- | --------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Android   | 使用 `Intent.ACTION_GET_CONTENT`（Storage Access Framework），系统在用户选择文件时自动授予临时 URI 访问权，无需 `READ_EXTERNAL_STORAGE` 或 `READ_MEDIA_*` |
| iOS       | 使用系统 `UIDocumentPickerViewController`（`asCopy=true`），系统负责文件访问，无需在 `Info.plist` 声明权限                                                |
| HarmonyOS | 使用系统 `DocumentViewPicker`，系统负责文件访问，无需额外权限声明                                                                                         |

## 注意事项

- 返回的 `path` 是应用缓存目录中的文件副本（Android 复制自 content URI，iOS `asCopy=true`），应用退出后可能被系统清理
- 如需持久化存储，请将文件从 `path` 复制到应用 Documents 目录
- Android 的文件复制在 UI 线程执行，选择大文件时建议配合 loading 提示
- iOS 最低版本要求 14+（`UIDocumentPickerViewController(forOpeningContentTypes:asCopy:)` 在 iOS 14 引入）
