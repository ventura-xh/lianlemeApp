package com.example.helloandroid.utils

import com.example.helloandroid.entity.ActionLibEntity
import com.example.helloandroid.entity.MuscleEntity

/**
 * 预设数据提供者
 * 集中管理所有预设数据
 */
@Deprecated("不再使用硬编码预设数据，预设动作请从json引入")
object PresetDataProvider {

    /**
     * 获取所有预设动作
     */
    fun getPresetActions(): List<ActionLibEntity> {
        return listOf(
            // ========== 下肢 ==========
            ActionLibEntity(
                name = "杠铃深蹲",
                isPreset = true,
                category = "腿",
                description = "站立，双脚与肩同宽，下蹲至大腿与地面平行，然后站起",
                tips = "保持背部挺直，膝盖不要超过脚尖"
            ),
            ActionLibEntity(
                name = "杠铃硬拉",
                isPreset = true,
                category = "腿",
                description = "双脚与髋同宽，俯身握住杠铃，保持背部挺直，用腿部和臀部力量拉起",
                tips = "保持核心收紧，不要弓背"
            ),
            ActionLibEntity(
                name = "杠铃罗马尼亚硬拉",
                isPreset = true,
                category = "腿",
                description = "向前跨出一大步，前腿弯曲至90度，后腿膝盖接近地面，下蹲时吸气，站起时呼气",
                tips = "保持躯干直立，前膝不要超过脚尖"
            ),
            ActionLibEntity(
                name = "保加利亚深蹲",
                isPreset = true,
                category = "腿",
                description = "向前跨出一大步，前腿弯曲至90度，后腿膝盖接近地面，下蹲时吸气，站起时呼气",
                tips = "保持躯干直立，前膝不要超过脚尖"
            ),
            ActionLibEntity(
                name = "高脚杯深蹲",
                isPreset = true,
                category = "腿",
                description = "向前跨出一大步，前腿弯曲至90度，后腿膝盖接近地面，下蹲时吸气，站起时呼气",
                tips = "保持躯干直立，前膝不要超过脚尖"
            ),
            ActionLibEntity(
                name = "倒蹬",
                isPreset = true,
                category = "腿",
                description = "向前跨出一大步，前腿弯曲至90度，后腿膝盖接近地面，下蹲时吸气，站起时呼气",
                tips = "保持躯干直立，前膝不要超过脚尖"
            ),
            ActionLibEntity(
                name = "器械腿屈伸",
                isPreset = true,
                category = "腿",
                description = "向前跨出一大步，前腿弯曲至90度，后腿膝盖接近地面，下蹲时吸气，站起时呼气",
                tips = "保持躯干直立，前膝不要超过脚尖"
            ),
        )
    }

    fun getPresetMuscles(): List<MuscleEntity> {
        return listOf(
            // 腿
            MuscleEntity(name = "股四头肌", category = "腿"),
            MuscleEntity(name = "臀大肌", category = "腿"),
            MuscleEntity(name = "腘绳肌", category = "腿"),
            MuscleEntity(name = "小腿肌群", category = "腿"),
            MuscleEntity(name = "髋屈肌", category = "腿"),
            MuscleEntity(name = "臀中肌", category = "腿"),
            // 肩
            MuscleEntity(name = "三角肌前束", category = "肩"),
            MuscleEntity(name = "三角肌中束", category = "肩"),
            MuscleEntity(name = "三角肌后束", category = "肩"),
            MuscleEntity(name = "斜方肌", category = "肩"),
            // 胸
            MuscleEntity(name = "胸大肌", category = "胸"),
            // 背
            MuscleEntity(name = "背阔肌", category = "背"),
            MuscleEntity(name = "菱形肌", category = "背"),
            // 手
            MuscleEntity(name = "肱二头肌", category = "手臂"),
            MuscleEntity(name = "肱三头肌", category = "手臂"),
            MuscleEntity(name = "前臂肌群", category = "手臂"),
            // 核心
            MuscleEntity(name = "腹直肌", category = "核心"),
            MuscleEntity(name = "腹斜肌", category = "核心"),
            MuscleEntity(name = "腹横肌", category = "核心"),
            MuscleEntity(name = "竖脊肌", category = "核心"),
            MuscleEntity(name = "腰方肌", category = "核心"),
            // 全身
            MuscleEntity(name = "全身肌群", category = "全身")
        )
    }

    fun getActionMuscleMappings(): Map<String, List<String>> {
        return mapOf(
            // 下肢动作 → 目标肌肉
            "杠铃深蹲" to listOf("股四头肌", "臀大肌", "腘绳肌"),
            "硬拉" to listOf("臀大肌", "腘绳肌", "竖脊肌"),
            "弓箭步" to listOf("股四头肌", "臀大肌", "髋屈肌"),
            "臀桥" to listOf("臀大肌", "腘绳肌", "竖脊肌"),
            "保加利亚分腿蹲" to listOf("股四头肌", "臀大肌", "臀中肌"),

            // 上肢动作
            "俯卧撑" to listOf("胸大肌", "三角肌", "肱三头肌"),
            "哑铃弯举" to listOf("肱二头肌", "前臂肌群"),
            "臂屈伸" to listOf("肱三头肌", "胸大肌", "三角肌"),
            "引体向上" to listOf("背阔肌", "肱二头肌", "菱形肌"),
            "肩上推举" to listOf("三角肌", "肱三头肌", "斜方肌"),

            // 核心动作
            "平板支撑" to listOf("腹横肌", "腹直肌", "竖脊肌"),
            "卷腹" to listOf("腹直肌", "腹斜肌"),
            "登山者" to listOf("腹直肌", "髋屈肌", "三角肌"),
            "死虫式" to listOf("腹横肌", "腹直肌", "髋屈肌"),
            "俄罗斯转体" to listOf("腹斜肌", "腹直肌", "竖脊肌"),

            // 全身动作
            "波比跳" to listOf("全身肌群"),
            "开合跳" to listOf("全身肌群"),
            "高抬腿" to listOf("全身肌群", "髋屈肌"),
            "深蹲跳" to listOf("股四头肌", "臀大肌", "全身肌群")
        )
    }
}