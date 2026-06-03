/**
 * dh-choose-file TypeScript 类型声明
 * 供 uni-app / uni-app-x 项目使用
 *
 * @platform Android / iOS / HarmonyOS
 */

/** 选择到的单个文件信息 */
export interface ChooseFileItem {
  /** 文件本地路径（file:// 协议） */
  path: string
  /** 文件名（含扩展名） */
  name: string
  /** 文件大小（字节） */
  size: number
  /** MIME 类型，例如 "audio/mpeg" */
  mimeType: string
  /** 小写扩展名（不含点），例如 "mp3" */
  extension: string
  /**
   * 媒体时长（毫秒）
   * - 音频 / 视频文件：返回实际时长
   * - 其他文件类型：返回 `0`
   * - HarmonyOS：暂不支持，始终返回 `0`
   */
  duration: number
}

/** 选择成功回调结果 */
export interface ChooseFileSuccess {
  /** 选中的文件列表 */
  files: ChooseFileItem[]
}

/** 错误码 */
export type ChooseFileErrorCode = 8020001 | 8020002 | 8020003 | 8020004

/** 选择失败回调结果 */
export interface ChooseFileFail {
  errCode: ChooseFileErrorCode
  errMsg: string
  errSubject: string
}

/**
 * 选择文件的参数
 *
 * ### fileTypes 支持的值
 * | 值        | 说明                              |
 * |-----------|-----------------------------------|
 * | `'all'`   | 所有文件（默认）                  |
 * | `'image'` | 图片（jpg/png/gif/webp/bmp 等）   |
 * | `'video'` | 视频（mp4/mov/avi/mkv 等）        |
 * | `'audio'` | 音频（mp3/wav/aac/m4a 等）        |
 * | `'doc'`   | 文档（pdf/word/excel/ppt）        |
 * | `'text'`  | 文本（txt/json/xml/md 等）        |
 * | `'archive'`| 压缩包（zip/rar/7z 等）          |
 * | `'file'`  | 所有文档+文本+压缩包              |
 *
 * 可传多个值，如 `['image', 'video']`。
 *
 * ### extensions 优先级高于 fileTypes
 * 若同时提供 `extensions`，只显示匹配扩展名的文件，`fileTypes` 不再生效。
 */
export interface ChooseFileOptions {
  /**
   * 最多选择文件数量，默认 1
   */
  count?: number
  /**
   * 文件类型过滤，默认 `['all']`
   * @example ['audio']          // 只显示音频
   * @example ['image', 'video'] // 图片+视频
   * @example ['doc']            // PDF/Word/Excel/PPT
   */
  fileTypes?: string[]
  /**
   * 扩展名白名单（不含点，小写），优先级高于 fileTypes。
   * @example ['mp3', 'wav']     // 只显示 mp3 和 wav
   * @example ['jpg', 'mp4']     // 只显示 jpg 和 mp4
   */
  extensions?: string[]
  /** 成功回调 */
  success?: (res: ChooseFileSuccess) => void
  /** 失败回调 */
  fail?: (res: ChooseFileFail) => void
  /** 完成回调（无论成功或失败） */
  complete?: () => void
}

/**
 * 从设备存储选择文件
 *
 * @platform Android / iOS / HarmonyOS
 *
 * @example
 * ```typescript
 * import { chooseFile } from '@/uni_modules/dh-choose-file'
 *
 * // 按类型
 * chooseFile({
 *   fileTypes: ['doc'],
 *   success: (res) => console.log(res.files[0].name),
 * })
 *
 * // 按扩展名
 * chooseFile({
 *   extensions: ['mp3', 'wav'],
 *   count: 3,
 *   success: (res) => console.log(res.files),
 *   fail: (err) => {
 *     if (err.errCode === 8020001) console.log('用户取消')
 *   },
 * })
 * ```
 */
export declare function chooseFile(options: ChooseFileOptions): void

/**
 * 读取本地文本文件内容（同步）
 *
 * @param filePath 文件路径，支持 `file://` 协议或绝对路径
 * @returns 文件文本内容，读取失败返回空字符串
 * @platform Android / iOS
 */
export declare function readTextFile(filePath: string): string
